package com.example.eventlottery.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eventlottery.R;
import com.example.eventlottery.events.Event;
import com.example.eventlottery.users.User;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** USER STORY - 01.02.03
 * @author: Jensen Lee
 *
 * The {@code InfoActivity} class displays detailed information about an event invitation.
 * Users can view event details and choose to either accept or decline the invitation.
 * Updates are reflected in the main user data.
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

        // Get data from intent
        Intent intent = getIntent();

        // Reconstruct User
        String userId = intent.getStringExtra("USER_ID");
        String userName = intent.getStringExtra("USER_NAME");
        String userEmail = intent.getStringExtra("USER_EMAIL");
        String userPhone = intent.getStringExtra("USER_PHONE");
        currentUser = new User(userId, userName, userEmail, userPhone);

        // Reconstruct Event
        String eventId = intent.getStringExtra("EVENT_ID");
        String eventName = intent.getStringExtra("EVENT_NAME");
        String eventDescription = intent.getStringExtra("EVENT_DESCRIPTION");
        String eventLocation = intent.getStringExtra("EVENT_LOCATION");
        String eventOrganizer = intent.getStringExtra("EVENT_ORGANIZER");
        long startTimeMillis = intent.getLongExtra("EVENT_START_TIME", 0);
        long endTimeMillis = intent.getLongExtra("EVENT_END_TIME", 0);
        String eventStatus = intent.getStringExtra("EVENT_STATUS");

        Date startTime = new Date(startTimeMillis);
        Date endTime = new Date(endTimeMillis);

        currentEvent = new Event(eventId, eventName, eventDescription, eventLocation,
                eventOrganizer, "", startTime, endTime);

        // Register the event with current status
        currentUser.getRegisteredEvents().put(currentEvent.getId(), eventStatus);

        // Populate UI
        eventNameHeader.setText(eventName);
        eventDescriptionText.setText(eventDescription);
        eventLocationText.setText(eventLocation);
        eventOrganizerText.setText(eventOrganizer);
        eventDateTimeText.setText(formatEventDateTime(startTime));

        updateStatusDisplay();

        backButton.setOnClickListener(v -> finish());

        // Accept Button
        acceptButton.setOnClickListener(v -> showConfirmationDialog(true));

        // Decline Button
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
        currentUser.acceptInvitation(currentEvent.getId());
        currentStatus = "Accepted";

        // Update MainActivity's user data
        if (MainActivity.instance != null) {
            MainActivity.instance.getCurrentUser().acceptInvitation(currentEvent.getId());
            MainActivity.instance.saveUser(MainActivity.instance.getCurrentUser());
        }

        Toast.makeText(this, "✅ You accepted the invitation!", Toast.LENGTH_SHORT).show();
        updateStatusDisplay();

        // Disable buttons after accepting
        acceptButton.setEnabled(false);
        declineButton.setEnabled(false);
        acceptButton.setAlpha(0.5f);
        declineButton.setAlpha(0.5f);

        // Return to UserPanel after a short delay
        acceptButton.postDelayed(() -> {
            finish(); // Go back to UserPanel
        }, 1500);
    }

    /**
     * Handles the logic for when a user declines an event invitation.
     * Updates user status, displays confirmation, and disables further interaction.
     */
    private void handleDecline() {
        currentUser.declineInvitation(currentEvent.getId());
        currentStatus = "Declined";

        // Update MainActivity's user data
        if (MainActivity.instance != null) {
            MainActivity.instance.getCurrentUser().declineInvitation(currentEvent.getId());
            MainActivity.instance.saveUser(MainActivity.instance.getCurrentUser());
        }

        Toast.makeText(this, "❌ You declined the invitation.", Toast.LENGTH_SHORT).show();
        updateStatusDisplay();

        // Disable buttons after declining
        acceptButton.setEnabled(false);
        declineButton.setEnabled(false);
        acceptButton.setAlpha(0.5f);
        declineButton.setAlpha(0.5f);

        // Return to UserPanel after a short delay
        declineButton.postDelayed(() -> {
            finish(); // Go back to UserPanel
        }, 1500);
    }

    /**
     * Updates the display of the user's current event status.
     * Changes the text and background colors of the status badge based on the current state.
     */
    private void updateStatusDisplay() {
        String status = currentUser.getStatusForEvent(currentEvent.getId());
        statusBadge.setText(status);

        // Update badge styling based on status
        int backgroundColor;
        int textColor;

        switch (status) {
            case "Accepted":
                backgroundColor = getColor(android.R.color.holo_green_light);
                textColor = getColor(android.R.color.holo_green_dark);
                break;
            case "Declined":
                backgroundColor = getColor(android.R.color.holo_red_light);
                textColor = getColor(android.R.color.holo_red_dark);
                break;
            case "Notified":
                backgroundColor = getColor(android.R.color.holo_blue_light);
                textColor = getColor(android.R.color.holo_blue_dark);
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
}
