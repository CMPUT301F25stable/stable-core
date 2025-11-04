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
 * @author: Jensen Lee*/
public class InfoActivity extends AppCompatActivity {

    private TextView eventNameHeader;
    private TextView eventDescriptionText;
    private TextView eventLocationText;
    private TextView eventOrganizerText;
    private TextView eventDateTimeText;
    private TextView statusBadge;
    private Button acceptButton;
    private Button declineButton;
    private ImageButton backButton;
    private User currentUser;
    private Event currentEvent;
    private String currentStatus;

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
     * Shows confirmation dialog for accept/decline action
     * @param isAccepting true if accepting, false if declining
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
     * Handles accepting the invitation
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
     * Handles declining the invitation
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
     * Updates the status badge display
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
     * Formats the event date/time for display
     */
    private String formatEventDateTime(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("EEEE, MMM dd, yyyy 'at' h:mm a", Locale.getDefault());
        return formatter.format(date);
    }

}