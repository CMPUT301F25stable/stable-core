package com.example.eventlottery.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.eventlottery.R;
import com.example.eventlottery.model.EventDatabase;
import com.example.eventlottery.users.User;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The {@code EventJoinAndLeave} class allows a user to view event details
 * and either join or leave an event. It updates both the UI and Firestore
 * to reflect the user's participation status.
 * Reference: https://firebase.google.com/docs/firestore/manage-data/add-data#update_fields_in_nested_objects
 * https://www.geeksforgeeks.org/android/how-to-get-user-location-in-android/
 */
public class EventJoinAndLeave extends AppCompatActivity {
    private static final String TAG = "EventJoinAndLeave";
    private Button joinButton;
    private String eventId;
    private FirebaseFirestore db;
    private DocumentReference userDoc;
    private ListenerRegistration userListener;
    private Date registrationEnd;
    private Date registrationStart;
    private User currentUser;
    private User user;
    /** For checking if waitlist is full */
    private int waitlistMax;
    private int size;


    // For US02.02.03 - geolocation
    private FusedLocationProviderClient mFusedLocationClient;
    // Arbitrary integer for permission_id
    private int PERMISSION_ID = 44;
    private Map<String, Object> userLocation;
    private boolean geolocation;


    private boolean isJoined = false;

    /**
     * Initializes the activity, sets up the UI, retrieves event details from intent extras,
     * and checks whether the user has already joined the event.
     *
     * @param savedInstanceState the saved instance state of the activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_join_leave_page);

        ImageView image = findViewById(R.id.imageView);
        TextView title = findViewById(R.id.eventTitle);
        TextView subtitle = findViewById(R.id.eventSubtitle);
        TextView desc = findViewById(R.id.eventDescription);
        TextView details = findViewById(R.id.eventDetails);
        TextView showWaitlistSize = findViewById(R.id.showWaitlistSize);
        joinButton = findViewById(R.id.joinButton);
        Button homeButton = findViewById(R.id.homeButton);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        userLocation = new HashMap<>();

        // ---- Get intent extras (must include a stable eventId) ----
        eventId     = getIntent().getStringExtra("id");
        String name = getIntent().getStringExtra("name");
        String description = getIntent().getStringExtra("description");

        String dateStart = getIntent().getStringExtra("dateStart");
        String timeStart = getIntent().getStringExtra("timeStart");
        String dateEnd   = getIntent().getStringExtra("dateEnd");
        String timeEnd   = getIntent().getStringExtra("timeEnd");
        registrationEnd = (Date) getIntent().getSerializableExtra("registrationEnd");
        registrationStart = (Date) getIntent().getSerializableExtra("registrationStart");
        geolocation = (boolean) getIntent().getSerializableExtra("geolocation");
        waitlistMax = (int) getIntent().getSerializableExtra("waitlistMax");

        String location  = getIntent().getStringExtra("location");
        String organizer = getIntent().getStringExtra("organizer");

        String imageURL = getIntent().getStringExtra("image");

        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Missing event ID.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // ---- Bind UI ----
        title.setText(name);
        subtitle.setText("Official Event");
        desc.setText(description);

        String when = (dateEnd != null && timeEnd != null)
                ? String.format("📅 %s %s → %s %s", dateStart, timeStart, dateEnd, timeEnd)
                : String.format("📅 %s  🕒 %s", dateStart, timeStart);

        details.setText(when + "\n\n📍 Location: " + location + "\n\n🎟️ Organizer: " + organizer);

        // Shows the waitlist size of the event
        getWaitListSize(eventId, showWaitlistSize);

        Glide.with(this).load(imageURL).placeholder(R.drawable.placeholder).into(image);

        homeButton.setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));

        user = new User(this);
        userLocation.put("userId", user.getId());
        db = FirebaseFirestore.getInstance(); // get firestore instance
        userDoc = db.collection("users-p4").document(user.getId()); // get user

        // Check if user has already joined any events before loading
        userDoc.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) { // If user exists, check if they are in any events
                List<String> joinedWaitlist = (List<String>) documentSnapshot.get("waitlistedEvents");
                user.setWaitlistedEventIds(joinedWaitlist);
                currentUser = documentSnapshot.toObject(User.class);

                isJoined = user.isWaitlisted(eventId); // Check if user is joined in current event
                updateJoinButton(isJoined);
            } else {
                user.setWaitlistedEventIds(null); // If user doesn't exist, set joinedEventIds to null
                isJoined = false;
                updateJoinButton(false);
            }
        });

        joinButton.setOnClickListener(v -> {
            toggleJoin(eventId, currentUser, showWaitlistSize);
        });

        // Check if geolocation is enabled & entrant has not joined. Request location if so
        if (geolocation && !isJoined) {
            getLastLocation();
        }
    }

    /**
     * Toggles the user's join status for the event.
     * Checks first if the registration date is already past.
     * If joined, the user leaves the event; if not, they join.
     * Updates Firestore accordingly and refreshes the button state.
     */
    private void toggleJoin(String eventId, User user, TextView showWaitListSize) {
        // Check if registration is open. If it isn't, cancel joining waitlist.
        Date now = new Date();

        // Case 1: Registration is over
        if (now.after(registrationEnd)) {
            Toast.makeText(this, "Registration has ended", Toast.LENGTH_SHORT).show();
            return;
        }

        // Case 2: Registration hasn't started
        if (now.before(registrationStart)) {
            Toast.makeText(this, "Registration hasn't started", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if waitlist is full
        if (waitlistMax <= size) {
            Toast.makeText(this, "Waitlist is full", Toast.LENGTH_SHORT).show();
            return;
        }
      
        boolean newState = !isJoined;
        updateJoinButton(newState); // Update the button to reflect the new state

        if (newState) { // If joined
            userDoc.update("waitlistedEvents", FieldValue.arrayUnion(eventId)) // Add to Firestore
                    .addOnSuccessListener(v -> {
                        user.AddJoinedWaitlist(eventId);
                        updateJoinEventWaitlist(eventId, user);
                        isJoined = true;
                        getWaitListSize(eventId, showWaitListSize);
                    })
                    .addOnFailureListener(e -> {
                        updateJoinButton(isJoined);
                        Toast.makeText(this, "Failed to join. Try again.", Toast.LENGTH_SHORT).show();
                    });
        } else { // If left
            userDoc.update("waitlistedEvents", FieldValue.arrayRemove(eventId)) // Remove from Firestore
                    .addOnSuccessListener(v -> {
                        user.RemoveLeftWaitlist(eventId);
                        updateLeaveEventWaitlist(eventId, user);
                        isJoined = false;
                        getWaitListSize(eventId, showWaitListSize);
                    })
                    .addOnFailureListener(e -> {
                        updateJoinButton(isJoined);
                        Toast.makeText(this, "Failed to leave. Try again.", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    /**
     * Updates waitlistedUsers in DB when a user joins an event
     * @param eventId the eventID
     * @param user the current User
     */
    private void updateJoinEventWaitlist(String eventId, User user) {
        db = FirebaseFirestore.getInstance();
        DocumentReference documentReference = db.collection("event-p4").document(eventId);
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("name", user.getName());
        userInfo.put("email", user.getEmailAddress());

        documentReference.update("waitlist.waitlistedUsers", FieldValue.arrayUnion(userInfo))
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "user joined waitlist in event " + eventId);
                        }
                ).addOnFailureListener(e -> {
                    Log.e(TAG, "user failed to joined waitlist in event " + eventId, e);
                });

        // If geolocation is on, store that data
        if (geolocation) {
            // Only store if longitude & latitude were actually added
            if (userLocation.containsKey("longitude") && userLocation.containsKey("latitude")) {
                documentReference.update("userLocations", FieldValue.arrayUnion(userLocation));
            }
        }
    }

    /**
     * Updates waitlistedUsers in DB when a user leaves an event
     * @param eventId the eventID
     * @param user the the current User
     */
    private void updateLeaveEventWaitlist(String eventId, User user) {
        db = FirebaseFirestore.getInstance();
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("name", user.getName());
        userInfo.put("email", user.getEmailAddress());
        DocumentReference documentReference = db.collection("event-p4").document(eventId);
        documentReference.update("waitlist.waitlistedUsers", FieldValue.arrayRemove(userInfo))
                .addOnSuccessListener(unused -> {
                            Log.d(TAG, "user left waitlist in event " + eventId);
                        }
                ).addOnFailureListener(e -> {
                    Log.e(TAG, "user failed to leave waitlist in event " + eventId, e);
                });

        // If geolocation is on, remove that data
        if (geolocation) {
            if (userLocation != null) {
                // Load in userLocations from firebase & check if user is in it
                // Note: Done this way because an exact match in longitude & latitude is unlikely (so FieldValue.arrayRemove doesn't work)
                documentReference.get().addOnSuccessListener(snapshot -> {
                    // Get all userLocations from event & return if null
                    ArrayList<Map<String, Object>> userLocations = (ArrayList<Map<String, Object>>) snapshot.get("userLocations");
                    if (userLocations == null)
                        return;

                    // Make a new array that will have every user location but the user leaving
                    ArrayList<Map<String, Object>> newUserLocations = new ArrayList<>();
                    for (Map<String, Object> userLocation : userLocations) {
                        if (!currentUser.getId().equals(userLocation.get("userId"))) {
                            newUserLocations.add(userLocation);
                        }
                    }

                    // Update in firestore
                    documentReference.update("userLocations", newUserLocations);
                });
            }
        }

    }

    /**
     * Updates the "Join"/"Leave" button text and color
     * based on whether the user has joined the event.
     *
     * @param joined true if the user is currently joined; false otherwise
     */
    private void updateJoinButton(boolean joined) {
        if (joined) {
            joinButton.setText("Leave Waitlist");
            joinButton.setBackgroundColor(ContextCompat.getColor(this, R.color.Red));
        } else {
            joinButton.setText("Join Waitlist");
            joinButton.setBackgroundColor(ContextCompat.getColor(this, R.color.Green));
        }

        this.isJoined = joined;
    }

    /**
     * Removes the Firestore listener when the activity is destroyed.
     * Prevents potential memory leaks.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userListener != null) {
            userListener.remove();
            userListener = null;
        }
    }

    /**
     * Updates textview showing the description of the event to include the number of
     * people the in waitlist
     * @param eventId the eventID
     * @param textView the textview to update
     */
    private void getWaitListSize(String eventId, TextView textView) {
        EventDatabase eventDatabase = new EventDatabase();

        eventDatabase.get(eventId, task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot doc = task.getResult();
                if (doc.exists()) {
                    Map<String, Object> waitlistMap = (Map<String, Object>) doc.get("waitlist");
                    if (waitlistMap != null) {

                        List<Object> waitlistUsers = (List<Object>) waitlistMap.get("waitlistedUsers");
                        if (waitlistUsers != null) {
                            size = waitlistUsers.size();
                            textView.setText("\n🧍 Waitlist: " + size);
                        } else {
                            size = 0;
                            textView.setText("\n🧍 Waitlist: " + size);
                        }
                    } else {
                        Log.d(TAG, "waitlistMap DNE: " + eventId);
                    }
                } else {
                    Log.d(TAG, "Doc DNE: " + eventId);
                }
            } else {
                Log.d(TAG, "Failed to get Event: " + eventId);
            }
        });
    }

    /**
     * Gets the last location of the user.
     */
    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        boolean precise = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean coarse  = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        // Check if permissions are enabled for either precise or coarse location
        if (!precise && !coarse) {
            requestPermissions();
            return;
        }


        // Check if location is enabled
        if (!isLocationEnabled()) {
            Toast.makeText(this, "Please turn on your location", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
            return;
        }

        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                if (location == null) {
                    requestNewLocationData();
                } else {
                    userLocation.put("latitude", location.getLatitude());
                    userLocation.put("longitude", location.getLongitude());
                }
            }
        });
    }

    /**
     * Method to request for geolocation permission
     * Reference: https://www.geeksforgeeks.org/android/how-to-get-user-location-in-android/
     */
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ID);
    }

    /**
     * Method to check if location is enabled.
     * @return true if it is, false otherwise
     * Reference: https://www.geeksforgeeks.org/android/how-to-get-user-location-in-android/
     */
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    /**
     * Requests new location data from the user.
     * Reference: https://www.geeksforgeeks.org/android/how-to-get-user-location-in-android/
     */
    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {
        // Initializing LocationRequest
        // object with appropriate methods
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(500);
        mLocationRequest.setNumUpdates(1);

        // setting LocationRequest
        // on FusedLocationClient
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    // If everything is alright then
    // References: https://www.geeksforgeeks.org/android/how-to-get-user-location-in-android/
    @Override
    public void
    onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }

    /**
     * Define the callback that runs everytime FusedLocationProvider delivers a new location update.
     * References: https://www.geeksforgeeks.org/android/how-to-get-user-location-in-android/
     */
    private LocationCallback mLocationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            userLocation.put("latitude", mLastLocation.getLatitude());
            userLocation.put("longitude", mLastLocation.getLongitude());
        }
    };
}
