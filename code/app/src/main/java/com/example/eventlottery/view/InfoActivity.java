package com.example.eventlottery.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.eventlottery.R;
import com.example.eventlottery.events.Event;
import com.example.eventlottery.users.Organizer;
import com.example.eventlottery.users.User;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** USER STORY - 01.02.03
 * <p>
 * The {@code InfoActivity} class displays detailed information about an event invitation.
 * Users can view event details and choose to either accept or decline the invitation.
 * Updates are reflected in the main user data.
 * @author Jensen Lee
 */
public class InfoActivity extends AppCompatActivity {

    /** Header TextView displaying the name of the event. */
    private TextView eventNameHeader;

    /** TextView showing the detailed description of the event. */
    private TextView eventDescriptionText;

    /** TextView indicating the location where the event will take place. */
    private TextView eventLocationText;

    /** TextView displaying the organizer or host of the event. */
    private TextView eventOrganizerText;

    /** TextView showing the formatted start and/or end date and time of the event. */
    private TextView eventDateTimeText;

    /** Badge TextView displaying the user's current registration status for the event
     *  (e.g., "Accepted", "Declined", "Notified"). */
    private TextView statusBadge;

    /** Button that allows the user to accept the event invitation. */
    private Button acceptButton;

    /** Button that allows the user to decline the event invitation. */
    private Button declineButton;

    /** ImageButton that navigates back to the previous screen or activity. */
    private ImageButton backButton;

    /** The currently logged-in user interacting with the event details. */
    private User currentUser;

    /** The event object whose details are being displayed in this activity. */
    private Event currentEvent;

    /** String representing the current registration status of the user for this event. */
    private String currentStatus;

    private FirebaseFirestore db;
  
    /**
     * Called when the activity is created.
     * Initializes UI elements, retrieves intent data, constructs User and Event objects,
     * and sets up event button listeners.
     *
     * @param savedInstanceState The saved instance state bundle.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Bind UI elements
        eventNameHeader = findViewById(R.id.eventNameHeader);
        eventDescriptionText = findViewById(R.id.eventDescriptionText);
        eventLocationText = findViewById(R.id.eventLocationText);
        eventOrganizerText = findViewById(R.id.eventOrganizerText);
        eventDateTimeText = findViewById(R.id.eventDateTimeText);
        statusBadge = findViewById(R.id.statusBadge);
        acceptButton = findViewById(R.id.acceptButton);
        declineButton = findViewById(R.id.declineButton);
        backButton = findViewById(R.id.backButton);

        // Get user ID from intent
        Intent intent = getIntent();
        String userId = intent.getStringExtra("USER_ID");

        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Invalid user ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Retrieve user from Firestore FIRST
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentUser = documentSnapshot.toObject(Organizer.class);

                        if (currentUser == null) {
                            Toast.makeText(this, "Failed to parse user data", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }

                        // Defensive: make sure registeredEvents and waitlistedEvents are non-null
                        if (currentUser.getRegisteredEvents() == null) {
                            currentUser.setRegisteredEvents(new HashMap<>());
                        }
                        if (currentUser.getWaitlistedEvents() == null) {
                            currentUser.setWaitlistedEventIds(new ArrayList<>());
                        }

                        // NOW load event data after user is loaded
                        loadEventData(intent);
                    } else {
                        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to retrieve user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });

        // Back button
        backButton.setOnClickListener(v -> finish());

        // Accept button
        acceptButton.setOnClickListener(v -> showConfirmationDialog(true));

        // Decline button
        declineButton.setOnClickListener(v -> showConfirmationDialog(false));
    }

    /**
     * Shows a confirmation dialog for accepting or declining an event invitation.
     *
     * @param isAccepting {@code true} if the user is accepting the invitation,
     *                    {@code false} if declining.
     */
    private void showConfirmationDialog(boolean isAccepting) {
        String title = isAccepting ? "Accept Invitation?" : "Decline Invitation?";
        String message = isAccepting
                ? "Are you sure you want to accept this event invitation?"
                : "Are you sure you want to decline this event invitation?";
        String positiveText = isAccepting ? "Accept" : "Decline";

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);

        builder.setPositiveButton(positiveText, (dialog, which) -> {
            if (isAccepting) {
                handleAccept();
            } else {
                handleDecline();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        // Customize button colors
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                isAccepting ? getColor(android.R.color.holo_green_dark) : getColor(android.R.color.holo_red_dark)
        );
    }

    /**
     * Handles the logic for when a user accepts an event invitation.
     * Updates user status, displays confirmation, and disables further interaction.
     */
    private void handleAccept() {
        if (currentEvent == null || currentUser == null) {
            Toast.makeText(this, "Event data not loaded yet", Toast.LENGTH_SHORT).show();
            return;
        }

        final String eventId = currentEvent.getId();

        // Disable buttons immediately
        acceptButton.setEnabled(false);
        declineButton.setEnabled(false);
        acceptButton.setAlpha(0.5f);
        declineButton.setAlpha(0.5f);

        // Single-field Firestore update
        Map<String, Object> updates = new HashMap<>();
        updates.put("registeredEvents." + eventId, "Accepted");

        db.collection("users").document(currentUser.getId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    // Update local model safely
                    currentUser.getRegisteredEvents().put(eventId, "Accepted");
                    currentStatus = "Accepted";
                    updateStatusDisplay();

                    Toast.makeText(this, "✅ You accepted the invitation!", Toast.LENGTH_SHORT).show();

                    // Tell the caller something changed and finish
                    Intent out = new Intent(this, UserPanel.class);
                    out.putExtra("USER_ID", currentUser.getId());
                    out.putExtra("UPDATED_EVENT_ID", eventId);
                    out.putExtra("UPDATED_STATUS", "Accepted");
                    setResult(RESULT_OK, out);
                    startActivity(out);
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Re-enable buttons on failure
                    acceptButton.setEnabled(true);
                    declineButton.setEnabled(true);
                    acceptButton.setAlpha(1.0f);
                    declineButton.setAlpha(1.0f);

                    Toast.makeText(this, "Failed to save: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Handles the logic for when a user declines an event invitation.
     * Updates user status, displays confirmation, and disables further interaction.
     */
    private void handleDecline() {
        if (currentEvent == null || currentUser == null) {
            Toast.makeText(this, "Event data not loaded yet", Toast.LENGTH_SHORT).show();
            return;
        }

        final String eventId = currentEvent.getId();

        // Disable buttons immediately
        acceptButton.setEnabled(false);
        declineButton.setEnabled(false);
        acceptButton.setAlpha(0.5f);
        declineButton.setAlpha(0.5f);

        // Single-field Firestore update
        Map<String, Object> updates = new HashMap<>();
        updates.put("registeredEvents." + eventId, "Declined");

        db.collection("users").document(currentUser.getId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    currentUser.getRegisteredEvents().put(eventId, "Declined");
                    currentStatus = "Declined";
                    updateStatusDisplay();

                    Toast.makeText(this, "❌ You declined the invitation.", Toast.LENGTH_SHORT).show();

                    Intent out = new Intent(this, UserPanel.class);
                    out.putExtra("USER_ID", currentUser.getId());
                    out.putExtra("UPDATED_EVENT_ID", eventId);
                    out.putExtra("UPDATED_STATUS", "Declined");
                    setResult(RESULT_OK, out);
                    startActivity(out);
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Re-enable buttons on failure
                    acceptButton.setEnabled(true);
                    declineButton.setEnabled(true);
                    acceptButton.setAlpha(1.0f);
                    declineButton.setAlpha(1.0f);

                    Toast.makeText(this, "Failed to save: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Updates the display of the user's current event status.
     * Changes the text and background colors of the status badge based on the current state.
     */
    private void updateStatusDisplay() {
        if (statusBadge == null) return;

        if (currentUser == null || currentEvent == null || currentEvent.getId() == null) {
            statusBadge.setText("Unknown");
            statusBadge.setBackgroundColor(android.graphics.Color.parseColor("#F5F5F5"));
            statusBadge.setTextColor(android.graphics.Color.GRAY);
            return;
        }

        // Use the User class's built-in method
        String status = currentUser.getStatusForEvent(currentEvent.getId());
        if (status == null || status.isEmpty() || status.equals("Not Registered")) {
            status = "Unknown";
        }

        statusBadge.setText(status);

        int backgroundColor;
        int textColor;

        switch (status) {
            case "Accepted":
                backgroundColor = ContextCompat.getColor(this, android.R.color.holo_green_light);
                textColor = ContextCompat.getColor(this, android.R.color.holo_green_dark);
                break;
            case "Declined":
                backgroundColor = ContextCompat.getColor(this, android.R.color.holo_red_light);
                textColor = ContextCompat.getColor(this, android.R.color.holo_red_dark);
                break;
            case "Notified":
                backgroundColor = ContextCompat.getColor(this, android.R.color.holo_blue_light);
                textColor = ContextCompat.getColor(this, android.R.color.holo_blue_dark);
                break;
            default:
                backgroundColor = android.graphics.Color.parseColor("#F5F5F5");
                textColor = android.graphics.Color.GRAY;
        }

        statusBadge.setBackgroundColor(backgroundColor);
        statusBadge.setTextColor(textColor);
    }

    /**
     * Formats a {@link Date} object into a readable string for display in the UI.
     *
     * @param date The date to be formatted.
     * @return A formatted string representing the event date and time.
     */
    private String formatEventDateTime(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("EEEE, MMM dd, yyyy 'at' h:mm a", Locale.getDefault());
        return formatter.format(date);
    }

    /**
     * Loads event data from intent and populates the UI
     * @param intent Intent containing event data
     */
    private void loadEventData(Intent intent) {
        String eventId = intent.getStringExtra("EVENT_ID");
        String eventName = intent.getStringExtra("EVENT_NAME");
        String eventDescription = intent.getStringExtra("EVENT_DESCRIPTION");
        String eventLocation = intent.getStringExtra("EVENT_LOCATION");
        String eventOrganizer = intent.getStringExtra("EVENT_ORGANIZER");
        long startTimeMillis = intent.getLongExtra("EVENT_START_TIME", 0);
        long endTimeMillis = intent.getLongExtra("EVENT_END_TIME", 0);
        currentStatus = intent.getStringExtra("EVENT_STATUS");

        Date startTime = new Date(startTimeMillis);
        Date endTime = new Date(endTimeMillis);

        currentEvent = new Event(eventId, eventName, eventDescription, eventLocation,
                eventOrganizer, "", startTime, endTime);

        // Don't manually add to registeredEvents - it should already be there from Firestore
        // Only ensure the status is set if it's missing
        if (!currentUser.getRegisteredEvents().containsKey(eventId) && currentStatus != null) {
            currentUser.getRegisteredEvents().put(eventId, currentStatus);
        }

        // Populate UI
        eventNameHeader.setText(eventName);
        eventDescriptionText.setText(eventDescription);
        eventLocationText.setText(eventLocation);
        eventOrganizerText.setText(eventOrganizer);
        eventDateTimeText.setText(formatEventDateTime(startTime));

        // Update status display
        updateStatusDisplay();
    }
}
