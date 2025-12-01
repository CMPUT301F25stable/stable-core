package com.example.eventlottery.view;

import android.content.Context;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eventlottery.R;
import com.example.eventlottery.events.DBConnector;
import com.example.eventlottery.events.Event;
import com.example.eventlottery.users.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Create the user panel where the user can accept, decline invitation, and see all there events
 * User can also click to go edit their profile
 * Shows all events (waitlisted and registered) in the UI
 * Shows their name in the UI
 * @author Jensen Lee
 * */
public class UserPanel extends AppCompatActivity {

    // User object
    private User currentUser;

    // UI & Firebase
    private LinearLayout eventListContainer;
    private ArrayList<Event> allEvents;
    private FirebaseFirestore db;
    private com.google.firebase.firestore.ListenerRegistration userListener;

    /**
     * Lifecycle method called when the activity is created
     * Initialize the UI, firebase, and load the user data
     * @param savedInstanceState The saved state of the activity
     *
     * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // Set to the userPanel xml
        setContentView(R.layout.user_event_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.user_panel), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize the container for events
        eventListContainer = findViewById(R.id.event_list_container);

        // Initialize the firebase
        db = FirebaseFirestore.getInstance();

        // Show loading indicator
        TextView loadingText = new TextView(this);
        loadingText.setText("Loading your profile...");
        loadingText.setTextSize(18);
        loadingText.setPadding(16, 16, 16, 16);
        eventListContainer.addView(loadingText);

        Intent intent = getIntent();
        String userIdFromIntent = intent.getStringExtra("USER_ID");

        // Get Device ID
        String deviceId = Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        String userIdToLoad = (userIdFromIntent != null && !userIdFromIntent.isEmpty())
                ? userIdFromIntent
                : deviceId;

        // Load user from database
        DBConnector connector = new DBConnector(this);
        connector.loadUserInfo(userIdToLoad, task -> {
            eventListContainer.removeAllViews(); // Remove Loading text

            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // User exists - load their data
                    currentUser = document.toObject(User.class);
                } else {
                    // First time user - create new user with default name
                    currentUser = new User(this);
                    // Save to database
                    connector.saveNewUser(this);
                }
            } else {
                // Error loading - create new user anyway
                currentUser = new User(this);
                connector.saveNewUser(this);
            }

            // Get Events
            if (MainActivity.instance != null) {
                allEvents = MainActivity.instance.getAllEvents();
            } else {
                allEvents = new ArrayList<>();
            }

            // Update username in the UI
            // Reference: https://firebase.google.com/docs/firestore/query-data/get-data#java
            TextView userNameView = findViewById(R.id.user_name);
            DocumentReference userDoc = db.collection("users-p4").document(currentUser.getId());;

            // Asynchronously fetch the user document from Firestore
            userDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    // Check if the document was successfully retrieved
                    if (task.isSuccessful()) {
                        // Get the document snapshot from the completed task
                        DocumentSnapshot document = task.getResult();

                        // Verify that the document actually exists in the database
                        if (document.exists()) {
                            // Extract the "name" field from the document
                            String name = document.getString("name");
                            // Update the TextView with the user's name
                            userNameView.setText(name);
                        }
                        else {
                            // Document reference exists but no data found
                            Log.d("Firestore", "No such document");
                        }
                    }
                    else {
                        // Handle any errors that occurred during the fetch
                        Log.d("Firestore", "get failed with ", task.getException());
                    }
                }
            });

            setupRealtimeListener();

            // Go to EditUserInfoActivity
            findViewById(R.id.edit_icon).setOnClickListener(v -> {
                Intent editIntent = new Intent(UserPanel.this, EditUserInfoActivity.class);
                startActivity(editIntent);
            });
        });
    }

    /**
     * Sets up a real-time listener for user data changes
     */
    private void setupRealtimeListener() {
        if (currentUser == null) {
            return;
        }

        DocumentReference userDoc = db.collection("users-p4").document(currentUser.getId());

        // Remove old listener if it exists
        if (userListener != null) {
            userListener.remove();
        }

        // Set up real-time listener
        userListener = userDoc.addSnapshotListener((documentSnapshot, error) -> {
            if (error != null) {
                Log.e("RealtimeListener", "Listen failed: " + error.getMessage());
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                // Update username in real-time
                String name = documentSnapshot.getString("name");
                TextView userNameView = findViewById(R.id.user_name);
                if (name != null && userNameView != null) {
                    userNameView.setText(name);
                    currentUser.setName(name);
                }

                // Update events in real-time
                displayEvents();
            }
        });
    }


    /**
     * Make sure to save the user data if they relaunch the userPanel
     * */
    @Override
    @SuppressLint("HardwareIds")
    protected void onResume() {
        super.onResume();
        // Only refresh if currentUser is already loaded
        if (currentUser != null && eventListContainer != null) {
            if (MainActivity.instance != null) {
                User updatedUser = MainActivity.instance.getCurrentUser();
                // Only update if we got a valid user back
                if (updatedUser != null) {
                    currentUser = updatedUser;
                }
                allEvents = MainActivity.instance.getAllEvents();
            }

            // Re-setup listener in case it was removed
            setupRealtimeListener();
        }
    }

    /**
     * Clean up listener when activity is destroyed
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userListener != null) {
            userListener.remove();
        }
    }

    /**
     * Clears all events from the event list container
     */
    private void clearEvents() {
        if (eventListContainer != null) {
            eventListContainer.removeAllViews();
        }
    }

    /**
     * Displays all events (waitlisted and registered) in the UI
     */
    private void displayEvents() {
        // Clear existing events before displaying
        clearEvents();

        // Get user document from Firestore
        DocumentReference userDoc = db.collection("users-p4").document(currentUser.getId());

        userDoc.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> waitlistedEvents = (List<String>) documentSnapshot.get("waitlistedEvents");
                Map<String, Object> registeredEventsMap = (Map<String, Object>) documentSnapshot.get("registeredEvents");

                // FIXED: Always update the lists, even if empty (to clear them)
                if (waitlistedEvents != null) {
                    currentUser.setWaitlistedEventIds(waitlistedEvents);
                } else {
                    currentUser.setWaitlistedEventIds(new ArrayList<>());
                }

                // Convert Map<String, Object> to HashMap<String, String> for registeredEvents
                if (registeredEventsMap != null) {
                    HashMap<String, String> registeredEvents = new HashMap<>();
                    for (Map.Entry<String, Object> entry : registeredEventsMap.entrySet()) {
                        registeredEvents.put(entry.getKey(), entry.getValue().toString());
                    }
                    currentUser.setRegisteredEvents(registeredEvents);
                } else {
                    currentUser.setRegisteredEvents(new HashMap<>());
                }

                // Check if user has any events
                if (currentUser.getRegisteredEvents().isEmpty() && currentUser.getWaitlistedEvents().isEmpty()) {
                    showEmptyState();
                    return;
                }

                // Display registered events
                for (Map.Entry<String, String> entry : currentUser.getRegisteredEvents().entrySet()) {
                    String eventId = entry.getKey();
                    String status = entry.getValue();
                    Event event = findEventById(eventId);

                    if (event != null) {
                        addEventCard(event, status);
                    }
                }

                // FIXED: Display waitlisted events ONLY if they're not already in registeredEvents
                for (String eventId : currentUser.getWaitlistedEvents()) {
                    // Skip if this event is already shown as a registered event
                    if (!currentUser.getRegisteredEvents().containsKey(eventId)) {
                        Event event = findEventById(eventId);
                        if (event != null) {
                            addEventCard(event, "Waitlisted");
                        }
                    }
                }
            }
            else {
                // User document doesn't exist yet - show empty state without modifying user object
                showEmptyState();
            }
        }).addOnFailureListener(e -> {
            // Handle error
            Log.e("DisplayEvents", "Error fetching user data", e);
            Toast.makeText(this, "Error loading events", Toast.LENGTH_SHORT).show();
            showEmptyState();
        });
    }

    /**
     * Helper method to find an event by its ID
     * @param eventId The event ID to search for
     * @return The Event object, or null if not found
     */
    private Event findEventById(String eventId) {
        if (allEvents == null) {
            return null;
        }
        for (Event event : allEvents) {
            if (event.getId().equals(eventId)) {
                return event;
            }
        }
        return null;
    }

    /**
     * Shows empty state when no events are registered
     */
    private void showEmptyState() {
        LinearLayout emptyState = new LinearLayout(this);
        emptyState.setOrientation(LinearLayout.VERTICAL);
        emptyState.setGravity(android.view.Gravity.CENTER);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        emptyState.setLayoutParams(params);
        emptyState.setPadding(dpToPx(32), dpToPx(64), dpToPx(32), dpToPx(64));

        TextView emptyText = new TextView(this);
        emptyText.setText("No events yet");
        emptyText.setTextSize(18);
        emptyText.setTextColor(Color.parseColor("#757575"));
        emptyText.setGravity(android.view.Gravity.CENTER);

        TextView emptySubtext = new TextView(this);
        emptySubtext.setText("Browse events and join to see them here");
        emptySubtext.setTextSize(14);
        emptySubtext.setTextColor(Color.parseColor("#9E9E9E"));
        emptySubtext.setGravity(android.view.Gravity.CENTER);
        LinearLayout.LayoutParams subtextParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        subtextParams.setMargins(0, dpToPx(8), 0, 0);
        emptySubtext.setLayoutParams(subtextParams);

        emptyState.addView(emptyText);
        emptyState.addView(emptySubtext);
        eventListContainer.addView(emptyState);
    }

    /**
     * Creates and adds an event card to the UI with modern design
     * @param event Given an Event object
     * @param status Given a string of the status of the registered event
     */
    private void addEventCard(Event event, String status) {
        // Create CardView for modern look
        CardView cardView = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, dpToPx(12));
        cardView.setLayoutParams(cardParams);
        cardView.setCardElevation(dpToPx(2));
        cardView.setRadius(dpToPx(12));
        cardView.setCardBackgroundColor(Color.WHITE);

        // Inner container
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));

        // Make card clickable only if status is "Notified"
        if (status.equals("Notified")) {
            cardView.setClickable(true);
            cardView.setFocusable(true);

            // Get the ripple effect from theme
            android.util.TypedValue outValue = new android.util.TypedValue();
            getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
            cardView.setForeground(getDrawable(outValue.resourceId));

            cardView.setOnClickListener(v -> openInfoActivity(event, status));
        }

        // Status badge
        LinearLayout statusBadge = new LinearLayout(this);
        LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        badgeParams.setMargins(0, 0, 0, dpToPx(8));
        statusBadge.setLayoutParams(badgeParams);
        statusBadge.setPadding(dpToPx(12), dpToPx(6), dpToPx(12), dpToPx(6));
        statusBadge.setOrientation(LinearLayout.HORIZONTAL);

        // Status styling
        int statusColor;
        int statusBgColor;
        switch (status) {
            case "Accepted":
                statusColor = Color.parseColor("#2E7D32");
                statusBgColor = Color.parseColor("#E8F5E9");
                break;
            case "Notified":
                statusColor = Color.parseColor("#1976D2");
                statusBgColor = Color.parseColor("#E3F2FD");
                break;
            case "Waitlisted":
                statusColor = Color.parseColor("#F57C00");
                statusBgColor = Color.parseColor("#FFF3E0");
                break;
            case "Declined":
                statusColor = Color.parseColor("#C62828");
                statusBgColor = Color.parseColor("#FFEBEE");
                break;
            default:
                statusColor = Color.GRAY;
                statusBgColor = Color.parseColor("#F5F5F5");
        }

        statusBadge.setBackgroundColor(statusBgColor);
        android.graphics.drawable.GradientDrawable badgeShape = new android.graphics.drawable.GradientDrawable();
        badgeShape.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        badgeShape.setCornerRadius(dpToPx(16));
        badgeShape.setColor(statusBgColor);
        statusBadge.setBackground(badgeShape);

        TextView statusView = new TextView(this);
        statusView.setText(status);
        statusView.setTextSize(12);
        statusView.setTextColor(statusColor);
        statusView.setTypeface(null, android.graphics.Typeface.BOLD);
        statusBadge.addView(statusView);
        card.addView(statusBadge);

        // Event name
        TextView nameView = new TextView(this);
        nameView.setText(event.getName());
        nameView.setTextSize(18);
        nameView.setTextColor(Color.parseColor("#212121"));
        nameView.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        nameParams.setMargins(0, 0, 0, dpToPx(8));
        nameView.setLayoutParams(nameParams);
        card.addView(nameView);

        // Location with icon
        LinearLayout locationRow = new LinearLayout(this);
        locationRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams locationParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        locationParams.setMargins(0, 0, 0, dpToPx(4));
        locationRow.setLayoutParams(locationParams);

        TextView locationIcon = new TextView(this);
        locationIcon.setText("📍 ");
        locationIcon.setTextSize(14);
        locationRow.addView(locationIcon);

        TextView locationView = new TextView(this);
        locationView.setText(event.getLocation());
        locationView.setTextSize(14);
        locationView.setTextColor(Color.parseColor("#616161"));
        locationRow.addView(locationView);
        card.addView(locationRow);

        // Date/time with icon
        LinearLayout dateRow = new LinearLayout(this);
        dateRow.setOrientation(LinearLayout.HORIZONTAL);

        TextView dateIcon = new TextView(this);
        dateIcon.setText("🕒 ");
        dateIcon.setTextSize(14);
        dateRow.addView(dateIcon);

        TextView dateTimeView = new TextView(this);
        dateTimeView.setText(formatEventDateTime(event.getStartTime()));
        dateTimeView.setTextSize(14);
        dateTimeView.setTextColor(Color.parseColor("#616161"));
        dateRow.addView(dateTimeView);
        card.addView(dateRow);

        cardView.addView(card);
        eventListContainer.addView(cardView);
    }

    /**
     * Opens InfoActivity with event details from Firebase
     * @param event Given an Event object
     * @param status Given a string of the status of the registered event
     */
    private void openInfoActivity(Event event, String status) {
        Intent intent = new Intent(UserPanel.this, InfoActivity.class);
        intent.putExtra("EVENT_ID", event.getId());
        intent.putExtra("EVENT_NAME", event.getName());
        intent.putExtra("EVENT_DESCRIPTION", event.getDescription());
        intent.putExtra("EVENT_LOCATION", event.getLocation());
        intent.putExtra("EVENT_ORGANIZER", event.getOrganizer());
        intent.putExtra("EVENT_START_TIME", event.getStartTime().getTime());
        intent.putExtra("EVENT_END_TIME", event.getEndTime().getTime());
        intent.putExtra("EVENT_STATUS", status);
        intent.putExtra("USER_ID", currentUser.getId());
        intent.putExtra("USER_NAME", currentUser.getName());
        intent.putExtra("USER_EMAIL", currentUser.getEmailAddress());
        intent.putExtra("USER_PHONE", currentUser.getPhoneNumber());
        startActivity(intent);
    }

    /**
     * Formats the event date/time for display
     * @param date takes in a date object and formats the date
     * @return a formatted string
     */
    private String formatEventDateTime(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        return formatter.format(date);
    }

    /**
     * Helper method to convert dp to pixels to help look the same size on all screens
     */
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    /**
     * TESTING FUNCTION
     * */
    private void createTestNotifiedEvent() {
        if (currentUser == null || allEvents == null || allEvents.isEmpty()) {
            Log.e("TestEvent", "Cannot create test event - user or events not loaded");
            return;
        }

        // Get the first event ID for testing
        String testEventId = allEvents.get(2).getId();

        // Update Firestore
        Map<String, Object> updates = new HashMap<>();
        updates.put("registeredEvents." + testEventId, "Notified");

        db.collection("users-p4").document(currentUser.getId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d("TestEvent", "Test notified event created successfully!");
                })
                .addOnFailureListener(e -> {
                    Log.e("TestEvent", "Error creating test event: " + e.getMessage());
                });
    }
}