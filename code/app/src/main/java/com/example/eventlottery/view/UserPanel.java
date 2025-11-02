package com.example.eventlottery.view;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventlottery.R;
import com.example.eventlottery.events.Event;
import com.example.eventlottery.users.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * USER STORY 01.02.03
 * Create the user panel where the user can accept, decline invitation, and see all there events
 * @author Jensen Lee
 * */
public class UserPanel extends AppCompatActivity {

    private User currentUser;
    private LinearLayout eventListContainer;
    private ArrayList<Event> allEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set to the userPanel xml
        setContentView(R.layout.user_event_list);

        // Initialize the container for events
        eventListContainer = findViewById(R.id.event_list_container);
        allEvents = new ArrayList<>();

        // Create example user with events
        createExampleUserWithEvents();

        // Display the events
        displayEvents();

        // Go to EditUserInfoActivity
        findViewById(R.id.edit_icon).setOnClickListener(v -> {
            Intent intent = new Intent(UserPanel.this, EditUserInfoActivity.class);
            startActivity(intent);
        });

    }

    /**
     * Creates example user with different event statuses
     */
    private void createExampleUserWithEvents() {
        // Create a user
        currentUser = new User("device123", "John Doe", "john.doe@example.com", "780-123-4567");

        // Create dates for events
        Calendar calendar = Calendar.getInstance();

        // Event 1: Summer Music Festival (Accepted/Confirmed)
        calendar.set(2025, Calendar.JULY, 15, 18, 0);
        Date movie1Start = calendar.getTime();
        calendar.set(2025, Calendar.JULY, 15, 23, 0);
        Date movie1End = calendar.getTime();
        Event confirmedEvent = new Event(
                "Anime",
                "Watch the Demon Slayer: Infinity Castle",
                "West Edmonton Mall Cineplex, Edmonton, AB",
                "Movie Inc.",
                R.drawable.anime,
                movie1Start,
                movie1End
        );
        allEvents.add(confirmedEvent);

        // Event 2: Tech Conference 2025 (Notified/Registered)
        calendar.set(2025, Calendar.AUGUST, 20, 9, 0);
        Date sports1Start = calendar.getTime();
        calendar.set(2025, Calendar.AUGUST, 20, 17, 0);
        Date sports1End = calendar.getTime();
        Event registeredEvent = new Event(
                "Oilers vs Kings",
                "Watch the Edmonton Oilers face off against the LA Kings",
                "Rogers Place, Edmonton, AB",
                "Sports Alberta",
                R.drawable.hockey, // Replace with your actual drawable
                sports1Start,
                sports1End
        );
        allEvents.add(registeredEvent);

        // Event 3: Food & Wine Expo (Waitlisted)
        calendar.set(2025, Calendar.SEPTEMBER, 10, 14, 0);
        Date food1Start = calendar.getTime();
        calendar.set(2025, Calendar.SEPTEMBER, 10, 20, 0);
        Date food1End = calendar.getTime();
        Event waitlistedEvent = new Event(
                "Food & Wine Expo",
                "Culinary showcase featuring local chefs and wineries",
                "Edmonton Expo Centre, Edmonton, AB",
                "Culinary Events Ltd.",
                R.drawable.dance, // Replace with your actual drawable
                food1Start,
                food1End
        );
        allEvents.add(waitlistedEvent);

        // Add events with different statuses
        currentUser.getRegisteredEvents().put(confirmedEvent.getId(), "Accepted");
        currentUser.getRegisteredEvents().put(registeredEvent.getId(), "Notified");
        currentUser.getWaitlistedEvents().add(waitlistedEvent.getId());

        // Update user name in the UI
        TextView userName = findViewById(R.id.user_name);
        userName.setText(currentUser.getName());
    }

    /**
     * Displays all events (waitlisted and registered) in the UI
    */
    private void displayEvents() {
        // Display registered events
        for (Map.Entry<String, String> entry : currentUser.getRegisteredEvents().entrySet()) {
            String eventId = entry.getKey();
            String status = entry.getValue();

            // Find the corresponding event object
            Event event = findEventById(eventId);
            if (event != null) {
                addEventCard(event, status);
            }
        }

        // Display waitlisted events
        for (String eventId : currentUser.getWaitlistedEvents()) {
            Event event = findEventById(eventId);
            if (event != null) {
                addEventCard(event, "Waitlisted");
            }
        }
    }

    /**
     * Helper method to find an event by its ID
     * @param eventId The event ID to search for
     * @return The Event object, or null if not found
     */
    private Event findEventById(String eventId) {
        for (Event event : allEvents) {
            if (event.getId().equals(eventId)) {
                return event;
            }
        }
        return null;
    }

    /**
     * Creates and adds an event card to the UI
     * @param event Given an Event object
     * @param status Given a string of the status of the registered event
     */
    private void addEventCard(Event event, String status) {
        // Create the card container
        LinearLayout card = new LinearLayout(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, dpToPx(8));
        card.setLayoutParams(cardParams);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(Color.parseColor("#DDDDDD"));
        card.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));

        // Make card clickable only if status is "Notified"
        if (status.equals("Notified")) {
            card.setClickable(true);
            card.setFocusable(true);
            // Add ripple effect for feedback
            card.setBackgroundResource(android.R.drawable.list_selector_background);
            card.setBackgroundColor(Color.parseColor("#DDDDDD"));

            card.setOnClickListener(v -> {
                openInfoActivity(event, status);
            });
        }

        // Event name
        TextView nameView = new TextView(this);
        nameView.setText(event.getName());
        nameView.setTextSize(16);
        nameView.setTextColor(Color.BLACK);
        nameView.setTypeface(null, android.graphics.Typeface.BOLD);
        card.addView(nameView);

        // Bottom row (date/time and status)
        LinearLayout bottomRow = new LinearLayout(this);
        bottomRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams bottomParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        bottomParams.setMargins(0, dpToPx(2), 0, 0);
        bottomRow.setLayoutParams(bottomParams);

        // Event date/time
        TextView dateTimeView = new TextView(this);
        dateTimeView.setText(formatEventDateTime(event.getStartTime()));
        dateTimeView.setTextSize(14);
        LinearLayout.LayoutParams dateParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1.0f
        );
        dateTimeView.setLayoutParams(dateParams);
        bottomRow.addView(dateTimeView);

        // Status
        TextView statusView = new TextView(this);
        statusView.setText(status);
        statusView.setTypeface(null, android.graphics.Typeface.BOLD);

        // Color code based on status
        switch (status) {
            case "Accepted":
                statusView.setTextColor(Color.parseColor("#2E7D32")); // Green
                break;
            case "Notified":
                statusView.setTextColor(Color.parseColor("#1976D2")); // Blue
                break;
            case "Waitlisted":
                statusView.setTextColor(Color.parseColor("#F57C00")); // Orange
                break;
            case "Declined":
                statusView.setTextColor(Color.parseColor("#C62828")); // Red
                break;
            default:
                statusView.setTextColor(Color.GRAY);
        }
        bottomRow.addView(statusView);

        card.addView(bottomRow);
        eventListContainer.addView(card);
    }

    /**
     * Opens InfoActivity with event details
     */
    private void openInfoActivity(Event event, String status) {
        Intent intent = new Intent(UserPanel.this, InfoActivity.class);

        // Pass event data
        intent.putExtra("EVENT_ID", event.getId());
        intent.putExtra("EVENT_NAME", event.getName());
        intent.putExtra("EVENT_DESCRIPTION", event.getDescription());
        intent.putExtra("EVENT_LOCATION", event.getLocation());
        intent.putExtra("EVENT_ORGANIZER", event.getOrganizer());
        intent.putExtra("EVENT_START_TIME", event.getStartTime().getTime());
        intent.putExtra("EVENT_END_TIME", event.getEndTime().getTime());
        intent.putExtra("EVENT_STATUS", status);

        // Pass user data
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
}
