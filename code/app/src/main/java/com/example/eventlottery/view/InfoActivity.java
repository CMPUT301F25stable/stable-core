package com.example.eventlottery.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eventlottery.R;
import com.example.eventlottery.events.Event;
import com.example.eventlottery.users.Organizer;
import com.example.eventlottery.users.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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
    private FirebaseFirestore db;
    private View loadingIndicator;

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

        // Get data from intent
        Intent intent = getIntent();
        String eventId = intent.getStringExtra("EVENT_ID");
        String userId = intent.getStringExtra("USER_ID");
        currentStatus = intent.getStringExtra("EVENT_STATUS");

        // Validate required data
        if (eventId == null || userId == null) {
            Toast.makeText(this, "Error: Missing required data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        backButton.setOnClickListener(v -> finish());

        // Load data from Firebase
        loadUserAndEventData(userId, eventId);
    }

    /**
     * Load user and event data from Firebase
     */
    private void loadUserAndEventData(String userId, String eventId) {
        // Load user data
        db.collection("users").document(userId).get()
                .addOnSuccessListener(userDoc -> {
                    if (userDoc.exists()) {
                        // Load user as Organizer (since all users are organizers in your app)
                        currentUser = userDoc.toObject(Organizer.class);

                        if (currentUser == null) {
                            Toast.makeText(this, "Error loading user data", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }

                        // Update user's registered events from Firebase
                        Map<String, Object> registeredEventsMap = (Map<String, Object>) userDoc.get("registeredEvents");
                        if (registeredEventsMap != null) {
                            HashMap<String, String> registeredEvents = new HashMap<>();
                            for (Map.Entry<String, Object> entry : registeredEventsMap.entrySet()) {
                                registeredEvents.put(entry.getKey(), entry.getValue().toString());
                            }
                            currentUser.setRegisteredEvents(registeredEvents);
                        } else {
                            currentUser.setRegisteredEvents(new HashMap<>());
                        }

                        // After user is loaded, load event data
                        loadEventData(eventId);
                    } else {
                        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("InfoActivity", "Error loading user", e);
                    Toast.makeText(this, "Error loading user data", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    /**
     * Load event data from Firebase
     */
    private void loadEventData(String eventId) {
        db.collection("event").document(eventId).get()
                .addOnSuccessListener(eventDoc -> {
                    if (eventDoc.exists()) {
                        currentEvent = eventDoc.toObject(Event.class);

                        if (currentEvent == null) {
                            Toast.makeText(this, "Error loading event data", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }

                        // Update UI with loaded data
                        // Update UI with loaded data
                        populateUI();
                        showLoading(false);
                    } else {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("InfoActivity", "Error loading event", e);
                    Toast.makeText(this, "Error loading event data", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    /**
     * Populate UI with event and user data
     */
    private void populateUI() {
        // Populate event details
        eventNameHeader.setText(currentEvent.getName());
        eventDescriptionText.setText(currentEvent.getDescription());
        eventLocationText.setText(currentEvent.getLocation());
        eventOrganizerText.setText(currentEvent.getOrganizer());
        eventDateTimeText.setText(formatEventDateTime(currentEvent.getStartTime()));

        updateStatusDisplay();

        // Accept Button
        acceptButton.setOnClickListener(v -> showConfirmationDialog(true));

        // Decline Button
        declineButton.setOnClickListener(v -> showConfirmationDialog(false));
    }

    /**
     * Show/hide loading indicator
     */
    private void showLoading(boolean show) {
        if (show) {
            // Hide all content
            eventNameHeader.setVisibility(View.GONE);
            eventDescriptionText.setVisibility(View.GONE);
            eventLocationText.setVisibility(View.GONE);
            eventOrganizerText.setVisibility(View.GONE);
            eventDateTimeText.setVisibility(View.GONE);
            statusBadge.setVisibility(View.GONE);
            acceptButton.setVisibility(View.GONE);
            declineButton.setVisibility(View.GONE);

            // You could add a ProgressBar to your layout and show it here
            // loadingIndicator.setVisibility(View.VISIBLE);
        } else {
            // Show all content
            eventNameHeader.setVisibility(View.VISIBLE);
            eventDescriptionText.setVisibility(View.VISIBLE);
            eventLocationText.setVisibility(View.VISIBLE);
            eventOrganizerText.setVisibility(View.VISIBLE);
            eventDateTimeText.setVisibility(View.VISIBLE);
            statusBadge.setVisibility(View.VISIBLE);
            acceptButton.setVisibility(View.VISIBLE);
            declineButton.setVisibility(View.VISIBLE);

            // loadingIndicator.setVisibility(View.GONE);
        }
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

        Toast.makeText(this, "✅ You accepted the invitation!", Toast.LENGTH_SHORT).show();
        updateStatusDisplay();

        // Disable buttons after accepting
        acceptButton.setEnabled(false);
        declineButton.setEnabled(false);
        acceptButton.setAlpha(0.5f);
        declineButton.setAlpha(0.5f);

        finish();
    }

    /**
     * Handles declining the invitation
     */
    private void handleDecline() {
        currentUser.declineInvitation(currentEvent.getId());
        currentStatus = "Declined";

        Toast.makeText(this, "❌ You declined the invitation.", Toast.LENGTH_SHORT).show();
        updateStatusDisplay();

        // Disable buttons after declining
        acceptButton.setEnabled(false);
        declineButton.setEnabled(false);
        acceptButton.setAlpha(0.5f);
        declineButton.setAlpha(0.5f);

        finish();
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