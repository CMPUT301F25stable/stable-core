package com.example.eventlottery.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.eventlottery.R;
import com.example.eventlottery.model.EventDatabase;
import com.example.eventlottery.users.User;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The {@code EventJoinAndLeave} class allows a user to view event details
 * and either join or leave an event. It updates both the UI and Firestore
 * to reflect the user's participation status.
 * Reference: https://firebase.google.com/docs/firestore/manage-data/add-data#update_fields_in_nested_objects
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
    private User user;
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
        db = FirebaseFirestore.getInstance(); // get firestore instance
        userDoc = db.collection("users-p4").document(user.getId()); // get user

        // Check if user has already joined any events before loading
        userDoc.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) { // If user exists, check if they are in any events
                List<String> joinedWaitlist = (List<String>) documentSnapshot.get("waitlistedEvents");
                user.setWaitlistedEventIds(joinedWaitlist);

                isJoined = user.isWaitlisted(eventId); // Check if user is joined in current event
                updateJoinButton(isJoined);
            } else {
                user.setWaitlistedEventIds(null); // If user doesn't exist, set joinedEventIds to null
                isJoined = false;
                updateJoinButton(false);
            }
        });

        joinButton.setOnClickListener(v -> {
            toggleJoin(eventId, user, showWaitlistSize);
        });
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
     * @param user the userID
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
    }

    /**
     * Updates waitlistedUsers in DB when a user leaves an event
     * @param eventId the eventID
     * @param user the userID
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
                            int size = waitlistUsers.size();
                            textView.setText("\n🧍 Waitlist: " + size);
                        } else {
                            int size = 0;
                            textView.append("\n🧍 Waitlist: " + size);
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



}
