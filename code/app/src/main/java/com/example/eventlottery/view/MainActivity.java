package com.example.eventlottery.view;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlottery.R;
import com.example.eventlottery.events.DBConnector;
import com.example.eventlottery.events.Event;
import com.example.eventlottery.events.NotificationSystem;
import com.example.eventlottery.users.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * MainActivity serves as the central hub of the Event Lottery application.
 * It displays a list of available events, allows searching and filtering events,
 * manages user data, and integrates with the notification system.
 */
public class MainActivity extends AppCompatActivity {

    // --- Constants ---
    private static final int REQ_FILTER = 1001;
  
    /** Unique device identifier for this installation. */
    private String DEVICE_ID;

    /** Search view for filtering the displayed events. */
    private SearchView searchView;

    /** Adapter for binding event data to the RecyclerView. */
    private MyAdapter adapter;

    // --- Firebase and Database ---
    private FirebaseFirestore db;
    private ListenerRegistration eventsListener;

    // --- Data and Models ---
    private final ArrayList<Event> data = new ArrayList<>();
    private User currentUser;

    // --- Filters ---
    public static ArrayList<String> selectedTags = new ArrayList<>();
    public static Set<Long> selectedDatesMidnight = new HashSet<>();

    // --- UI Components ---

    /** Static reference to the MainActivity instance for global access. */
    public static MainActivity instance;
    private DBConnector connector;

    /** Launcher for requesting notification permission.
     * Source: https://www.youtube.com/watch?v=r5DeECeorAo */
    private ActivityResultLauncher<String> resultLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission Granted
                    // Get Device token from firebase
                    getDeviceToken();
                }
                else {
                    // Permission Denied
                }
            }
    );

    /**
     * Lifecycle method called when the activity is created.
     * Initializes UI elements, sets up RecyclerView and SearchView,
     * loads or creates the user, and populates sample event data.
     * @param savedInstanceState Bundle containing saved instance state, if any.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        instance = this;
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        requestPermission();

        // Adjust UI for system insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findViewById(R.id.Filterbtn).setOnClickListener(v -> {
            ArrayList<String> allTags = collectAllTags(data);
            Intent intent = new Intent(MainActivity.this, FilterActivity.class);
            intent.putStringArrayListExtra("allTags", allTags);
            intent.putStringArrayListExtra("preSelectedTags", new ArrayList<>(selectedTags));
            intent.putExtra("preSelectedDates", new ArrayList<>(selectedDatesMidnight)); // ArrayList<Long>
            startActivityForResult(intent, REQ_FILTER);
        });

        DEVICE_ID = Settings.System.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        db = FirebaseFirestore.getInstance();
        connector = new DBConnector(this);
        connector.loadUserInfo(DEVICE_ID, task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    currentUser = document.toObject(User.class);
                    loadEventsFromFirestore();

                    // Replace taskbar with admin taskbar if user is an admin
                    TaskbarFragment taskbar = TaskbarFragment.newInstance(currentUser);
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragmentContainerView, taskbar)
                            .commitAllowingStateLoss();
                }
            }
            else {
                // User does not exist - create a new user
                Log.d("MainActivity", "User doesn't exist, creating new user");
                DBConnector dbConnector = new DBConnector(this);
                dbConnector.saveNewUser(this);
            }
        });

        // Test notification system
        //User testUser = new User("u123", "Alice", "alice@gmail.com");
        //NotificationSystem notifier = new NotificationSystem(this);
        //notifier.notifyLotteryLoser(testUser, "Tyler the Creator Concert Lottery");

        // Initialize RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the adapter
        adapter = new MyAdapter(data, (item, position) -> {
            Intent intent = new Intent(MainActivity.this, EventJoinAndLeave.class);
            intent.putExtra("id", item.getId());
            intent.putExtra("name", item.getName());
            intent.putExtra("description", item.getDescription());
            intent.putExtra("dateStart", item.getFormattedStartDate());
            intent.putExtra("timeStart", item.getFormattedStartTime());
            intent.putExtra("dateEnd", item.getFormattedEndDate());
            intent.putExtra("timeEnd", item.getFormattedEndTime());
            intent.putExtra("location", item.getLocation());
            intent.putExtra("organizer", item.getOrganizer());
            intent.putExtra("image", item.getImage());
            intent.putExtra("registrationEnd", item.getEndTime());      // Unformatted end date.
            intent.putExtra("registrationStart", item.getStartTime());  // Unformatted start date.
            intent.putExtra("geolocation", item.getGeolocation());
            intent.putExtra("waitlistMax", item.getWaitlistMax());
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);

        // Configure SearchView filtering
        searchView = findViewById(R.id.searchView);
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return false; }
            @Override public boolean onQueryTextChange(String newText) {
                filterList(newText);
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if ((!selectedTags.isEmpty()) || (!selectedDatesMidnight.isEmpty())) {
            applyFilters(selectedTags, selectedDatesMidnight);
        } else {
            adapter.setFilteredList(new ArrayList<>(data));
        }

    }
  
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent dataIntent) {
        super.onActivityResult(requestCode, resultCode, dataIntent);

        if (requestCode == REQ_FILTER && resultCode == RESULT_OK && dataIntent != null) {

            ArrayList<String> tags = dataIntent.getStringArrayListExtra("selectedTags");
            ArrayList<Long> dates = (ArrayList<Long>) dataIntent.getSerializableExtra("selectedDates");

            if (tags != null) {
                selectedTags = new ArrayList<>(tags);
            }

            selectedDatesMidnight.clear();
            if (dates != null) {
                selectedDatesMidnight.addAll(dates);
            }

            if (!selectedTags.isEmpty() || !selectedDatesMidnight.isEmpty()) {
                applyFilters(selectedTags, selectedDatesMidnight);
            }
            else {
                adapter.setFilteredList(new ArrayList<>(data));
            }
        }
    }

    /**
     * Creates a Date object for the specified year, month, day, hour, and minute.
     * @param year The year (e.g., 2025)
     * @param month The month (0-based, e.g., Calendar.NOVEMBER)
     * @param day The day of the month
     * @param hour24 The hour in 24-hour format
     * @param minute The minute
     * @return A Date object representing the specified date and time
     */
    private Date dateOf(int year, int month, int day, int hour24, int minute) {
        Calendar c = Calendar.getInstance(Locale.CANADA);
        c.set(year, month, day, hour24, minute, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    /**
     * Filters the list of events based on the given search text.
     * @param text The search text used to filter events by name
     */
    private void filterList(String text) {
        String q = text == null ? "" : text.trim().toLowerCase();
        if (q.isEmpty()) {
            // Reapply current filters (tags/dates) or show all
            if (!selectedTags.isEmpty() || !selectedDatesMidnight.isEmpty()) {
                applyFilters(selectedTags, selectedDatesMidnight);
            } else {
                adapter.setFilteredList(new ArrayList<>(data));
            }
            return;
        }

        List<Event> filteredList = new ArrayList<>();
        for (Event item : data) {
            if (item.getName() != null && item.getName().toLowerCase().contains(q)) {
                filteredList.add(item);
            }
        }
        if (filteredList.isEmpty()) {
            Toast.makeText(this, "No Data Found..", Toast.LENGTH_SHORT).show();
            adapter.setFilteredList(new ArrayList<>());
        } else {
            adapter.setFilteredList(filteredList);
        }
    }

      /**
     * Returns a list of all events currently loaded in the activity.
     * @return ArrayList of all Event objects
     */
    public ArrayList<Event> getAllEvents() { return data; }
  
      /**
     * Returns the currently logged-in user.
     * @return The User object representing the current user
     */
    public User getCurrentUser() { return currentUser; }

    /**
     * Adds valid events to data. A valid event is:
     * 1. An event not created by the user
     */
    private void loadEventsFromFirestore() {
        db.collection("event-p4")
                .orderBy("startTime")
                .addSnapshotListener((query, error) -> {
                    // Return if there is an error
                    if (error != null) {
                        return;
                    }

                    // Return if no query found
                    if (query == null) return;

                    data.clear();

                    for (DocumentSnapshot doc : query) {
                        Event event = doc.toObject(Event.class);
                        if (event == null) continue;

                        if (event.getId() == null || event.getId().isEmpty()) {
                            event.setId(doc.getId());
                        }

                        // Add null check for currentUser
                        String eventId = event.getId();
                        boolean shouldAddEvent = true;

                        // Only filter out created events if currentUser exists
                        if (currentUser != null && currentUser.getCreatedEvents() != null) {
                            ArrayList<String> createdEvents = currentUser.getCreatedEvents();

                            // Don't add events created by this user
                            if (createdEvents.contains(eventId)) {
                                shouldAddEvent = false;
                            }
                        }

                        if (shouldAddEvent) {
                            data.add(event);
                        }
                    }

                    if (!selectedTags.isEmpty() || !selectedDatesMidnight.isEmpty()) {
                        applyFilters(selectedTags, selectedDatesMidnight);
                    } else {
                        adapter.setFilteredList(new ArrayList<>(data));
                    }
                });
    }

    /**
     * Applies the given filters to the data.
     * @param tags
     * @param datesMidnight
     */
    private void applyFilters(List<String> tags, Set<Long> datesMidnight) {
        boolean hasTagFilter = (tags != null && !tags.isEmpty());
        boolean hasDateFilter = (datesMidnight != null && !datesMidnight.isEmpty());

        if (!hasTagFilter && !hasDateFilter) {
            adapter.setFilteredList(new ArrayList<>(data));
            return;
        }

        List<Event> filtered = new ArrayList<>();

        for (Event event : data) {
            boolean passesTags = true;
            boolean passesDates = true;

            if (hasTagFilter) {
                List<String> eventTags = event.getFilterTags();
                passesTags = (eventTags != null) && !java.util.Collections.disjoint(eventTags, tags);
            }

            if (hasDateFilter) {
                passesDates = occursOnAnySelectedDate(event, datesMidnight);
            }

            if (passesTags && passesDates) {
                filtered.add(event);
            }
        }

        adapter.setFilteredList(filtered);
    }

    /**
     * Returns true if the event happens on ANY of the selected days.
     * Each selected day is a 24-hour window [midnight..11:59:59.999].
     */
    private boolean occursOnAnySelectedDate(Event event, Set<Long> selectedDaysMidnight) {
        Date start = event.getStartTime();
        Date end = event.getEndTime();
        if (start == null) return false;
        if (end == null) end = start;

        long startMs = start.getTime();
        long endMs = Math.max(end.getTime(), startMs);

        for (Long dayStartMs : selectedDaysMidnight) {
            long dayEndMs = dayStartMs + (24L * 60 * 60 * 1000) - 1L;
            boolean overlaps = (startMs <= dayEndMs) && (endMs >= dayStartMs);
            if (overlaps) return true;
        }
        return false;
    }

    private ArrayList<String> collectAllTags(List<Event> events) {
        Set<String> allTags = new HashSet<>();
        for (Event event : events) {
            List<String> tags = event.getFilterTags();
            if (tags != null) {
                allTags.addAll(tags);
            }
        }
        return new ArrayList<>(allTags);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (eventsListener != null) {
            eventsListener.remove();
            eventsListener = null;
        }
    }

    /**
     * Saves the FCM token to the current user's document in Firestore.
     * @param token The FCM token to save.
     * */
    private void saveFCMTokenToFirestore(String token) {
        String deviceId = Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        if (deviceId != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("users-p4")
                    .document(deviceId)
                    .update("fcmToken", token)
                    .addOnSuccessListener(aVoid ->
                            Log.d("FCM", "Token saved to Firestore"))
                    .addOnFailureListener(e -> {
                        Log.e("FCM", "Failed to save token", e);
                        // If update fails (user doesn't exist), try to create/set
                        db.collection("users-p4")
                                .document(deviceId)
                                .set(new HashMap<String, Object>() {{
                                    put("fcmToken", token);
                                }}, com.google.firebase.firestore.SetOptions.merge());
                    });
        }
    }


    /**
     * Check to see if the user has notifications enabled
     * If not, request permission
     * If yes, get device token
     * Source: https://www.youtube.com/watch?v=r5DeECeorAo
     * */
    public void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED) {
                //Permission is granted
                getDeviceToken();
            }
            else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // You can explain user that why do you need permission by showing Toast Message
            }
            else {
                // Request for Permission
                resultLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
        else {
            // Get Device Token from Firebase
            getDeviceToken();
        }
    }

    /**
     * Get the token from Firebase
     * If successful, save to Firestore
     * If not, try again
     * Source: https://www.youtube.com/watch?v=r5DeECeorAo
     * */
    public void getDeviceToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()) {
                    Log.e("FirebaseLogs", "Fetching Token Failed" + task.getException());
                    return;
                }

                // Get Device Token
                String token = task.getResult();
                Log.v("FireBaseLogs", "Device Token: " + token);

                // Save the token to Firestore
                saveFCMTokenToFirestore(token);
            }

        });
    }
}
