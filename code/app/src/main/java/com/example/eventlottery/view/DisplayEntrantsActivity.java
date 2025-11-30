package com.example.eventlottery.view;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.eventlottery.R;
import com.example.eventlottery.events.DBConnector;
import com.example.eventlottery.events.Event;
import com.example.eventlottery.events.NotificationSystem;
import com.example.eventlottery.users.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class DisplayEntrantsActivity extends AppCompatActivity {

    private static final String TAG = "DisplayEntrantsActivity";

    private LinearLayout entrantsContainer;
    private DBConnector userDatabase;
    private FirebaseFirestore db;
    private final ArrayList<User> users = new ArrayList<>();
    private Button drawReplacementButton;
    private Event currentEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_entrants);

        TextView title = findViewById(R.id.chosenEntrantsTitle);
        entrantsContainer = findViewById(R.id.chosenEntrantsContainer);
        drawReplacementButton = findViewById(R.id.drawReplacementButton);

        String eventName = getIntent().getStringExtra("eventName");
        String type = getIntent().getStringExtra("type");
        String eventId = getIntent().getStringExtra("eventId");

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        if (eventId == null || type == null) {
            Toast.makeText(this, "Missing event information", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        final String finalEventName = eventName != null ? eventName : "";

        if ("chosen".equals(type)) {
            title.setText("Chosen Entrants - " + finalEventName);
        } else if ("cancelled".equals(type)) {
            title.setText("Cancelled Entrants - " + finalEventName);
            // Show draw replacement button only for cancelled entrants
            drawReplacementButton.setVisibility(View.VISIBLE);
            drawReplacementButton.setOnClickListener(v ->
                showDrawReplacementDialog(eventId, finalEventName)
            );
        } else {title.setText("Entrants - " + finalEventName);
        }

        userDatabase = new DBConnector(this);
        db = FirebaseFirestore.getInstance();

        fetchUserIdsForEvent(eventId, type);
        loadEventData(eventId);
    }

    /**
     * Fetches the list of user IDs for this event and type from Firestore.
     * "chosen"  -> selectedIds
     * "cancelled" -> cancelledEntrants
     */
    private void fetchUserIdsForEvent(String eventId, String type) {
        db
            .collection("event-p4")
            .document(eventId)
            .get()
            .addOnSuccessListener(doc -> {
                if (!doc.exists()) {
                    Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                    return;
                }

                List<String> userIds;

                if ("chosen".equals(type)) {
                    // selectedIds: list of user IDs
                    userIds = (List<String>) doc.get("selectedIds");
                } else if ("cancelled".equals(type)) {
                    // cancelledEntrants: list of user IDs
                    userIds = (List<String>) doc.get("cancelledEntrants");
                } else {
                    Toast.makeText(this, "Unknown entrant type", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (userIds == null || userIds.isEmpty()) {
                    Toast.makeText(this, "No users found for this event", Toast.LENGTH_SHORT).show();
                    return;
                }

                fetchUsersFromIds(new ArrayList<>(userIds));
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to fetch event document", e);
                Toast.makeText(this, "Failed to load entrants.", Toast.LENGTH_SHORT).show();
            });
    }

    /**
     * Given a list of user IDs, fetches each User from Firestore via DBConnector
     * and then displays them once all are loaded.
     */
    private void fetchUsersFromIds(ArrayList<String> userIds) {
        AtomicInteger remaining = new AtomicInteger(userIds.size());

        for (String userId : userIds) {
            userDatabase.loadUserInfo(userId, task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc != null && doc.exists()) {
                        User user = doc.toObject(User.class);
                        if (user != null) {
                            users.add(user);
                        }
                    }
                } else {
                    Log.e(TAG, "Failed to load user " + userId, task.getException());
                }

                // When all async calls are done, display the entrants
                if (remaining.decrementAndGet() == 0) {
                    if (users.isEmpty()) {
                        Toast.makeText(this, "No users found for this event", Toast.LENGTH_SHORT).show();
                    } else {
                        displayEntrants(users);
                    }
                }
            });
        }
    }

    /**
     * Populates the LinearLayout with rows for each user.
     */
    private void displayEntrants(ArrayList<User> users) {
        LayoutInflater inflater = LayoutInflater.from(this);
        entrantsContainer.removeAllViews();

        for (User user : users) {
            View row = inflater.inflate(R.layout.item_display_entrant, entrantsContainer, false);

            TextView nameText = row.findViewById(R.id.entrantNameText);
            TextView emailText = row.findViewById(R.id.entrantEmailText);

            nameText.setText("Name: " + user.getName());

            String email = user.getEmailAddress();
            if (email == null || email.trim().isEmpty()) {
                emailText.setText("Email: N/A");
            } else {
                emailText.setText("Email: " + email);
            }

            entrantsContainer.addView(row);
        }
    }

    /**
     * Loads the event data from Firestore given the event ID
     */
    private void loadEventData(String eventId) {
        db
            .collection("event-p4")
            .document(eventId)
            .get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    currentEvent = doc.toObject(Event.class);
                    if (currentEvent != null) {
                        currentEvent.setId(eventId);
                    }
                }
            })

            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to load event data", e);
            });
    }

    /**
     * Shows a selection dialog for choosing a replacement applicant for the event
     */
    private void showDrawReplacementDialog(String eventId, String eventName) {
        if (currentEvent == null) {
            Toast.makeText(
                this,
                "Event data not loaded yet. Please try again.",
                Toast.LENGTH_SHORT
            ).show();
            return;
        }

        // Check if there are eligible users (losers) to select from
        List<User> eligibleUsers = currentEvent.getLosers();
        if (eligibleUsers == null || eligibleUsers.isEmpty()) {
            Toast.makeText(
                this,
                "No eligible applicants remaining on waitlist",
                Toast.LENGTH_LONG
            ).show();
            return;
        }

        // Create array of user names for display (also includes email for added info)
        String[] userNames = new String[eligibleUsers.size()];
        for (int i = 0; i < eligibleUsers.size(); i++) {
            User user = eligibleUsers.get(i);
            String email = user.getEmailAddress();
            if (email == null || email.trim().isEmpty()) {
                userNames[i] = user.getName();
            } else {
                userNames[i] = user.getName() + " (" + email + ")";
            }
        }

        // Show the selection dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("Select Replacement Applicant")
            .setItems(userNames, (d, which) -> {
                // User selected an applicant at index 'which'
                User selectedUser = eligibleUsers.get(which);
                confirmReplacementSelection(eventId, eventName, selectedUser);
            })
            .setNegativeButton("Cancel", null)
            .show();

        // Makes it clear the cancel button is to be used for cancellation
        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        if (negativeButton != null) {
            negativeButton.setTextColor(Color.BLACK);
        }
    }

    /**
     * Shows confirmation dialog before selecting the replacement
     */
    private void confirmReplacementSelection(
        String eventId,
        String eventName,
        User selectedUser
    ) {
        String message =
            "Select " +
            selectedUser.getName() +
            " as a replacement?\n\n" +
            "They will be notified and added to the selected list.";

        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("Confirm Replacement")
            .setMessage(message)
            .setPositiveButton("Confirm", (d, which) ->
                selectReplacement(eventId, eventName, selectedUser)
            )
            .setNegativeButton("Cancel", null)
            .show();

        // Style the buttons
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        positiveButton.setTextColor(Color.BLACK);
        negativeButton.setTextColor(Color.BLACK);
    }

    /**
     * Selects a user as a replacement
     */
    private void selectReplacement(
        String eventId,
        String eventName,
        User selectedUser
    ) {
        if (currentEvent == null) {
            Toast.makeText(
                this,
                "Event data not loaded",
                Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (selectedUser == null) {
            Toast.makeText(
                this,
                "Invalid user selection",
                Toast.LENGTH_SHORT
            ).show();
            return;
        }

        // Add the selected user to the chosen entrants list
        if (!currentEvent.getSelectedIds().contains(selectedUser.getId())) {
            currentEvent.getSelectedIds().add(selectedUser.getId());
        }
        if (!currentEvent.getChosenEntrants().contains(selectedUser)) {
            currentEvent.getChosenEntrants().add(selectedUser);
        }

        // Update Firestore with the new selected user
        updateFirestoreAfterReplacement(eventId, eventName, selectedUser);
    }


    /**
     * Updates Firestore after drawing a replacement
     */
    private void updateFirestoreAfterReplacement(
        String eventId,
        String eventName,
        User replacement
    ) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("selectedIds", FieldValue.arrayUnion(replacement.getId()));

        db
            .collection("event-p4")
            .document(eventId)
            .update(updates)
            .addOnSuccessListener(aVoid -> {
                // Update the user's document
                updateReplacementUserDocument(eventId, replacement);

                // Notify the replacement user
                notifyReplacement(eventName, replacement);

                Toast.makeText(
                    this,
                    "Replacement drawn: " + replacement.getName(),
                    Toast.LENGTH_LONG
                ).show();

                Log.d(
                    TAG,
                    "Replacement drawn successfully: " + replacement.getId()
                );
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to update after drawing replacement", e);
                Toast.makeText(
                    this,
                    "Failed to draw replacement: " + e.getMessage(),
                    Toast.LENGTH_LONG
                ).show();
            });
    }

    /**
     * Updates the replacement user's document to reflect their new status
     */
    private void updateReplacementUserDocument(
        String eventId,
        User replacement
    ) {
        Map<String, Object> userUpdates = new HashMap<>();

        // Add to registeredEvents with "Notified" status
        userUpdates.put("registeredEvents." + eventId, "Notified");

        // Remove from waitlistedEvents array
        userUpdates.put("waitlistedEvents", FieldValue.arrayRemove(eventId));
        userUpdates.put("waitlistedEventIds", FieldValue.arrayRemove(eventId));

        db
            .collection("users-p4")
            .document(replacement.getId())
            .update(userUpdates)
            .addOnSuccessListener(aVoid -> {
                Log.d(
                    TAG,
                    "Updated user document for replacement: " +
                        replacement.getId()
                );
            })
            .addOnFailureListener(e -> {
                Log.e(
                    TAG,
                    "Failed to update user document for: " +
                        replacement.getId(),
                    e
                );
            });
    }

    /**
     * Sends a notification to the replacement applicant after they have been selected by the organizer
     */
    private void notifyReplacement(String eventName, User replacement) {

        // Get organizer info from the intent
        String organizerId = getIntent().getStringExtra("organizerId");
        String organizerName = getIntent().getStringExtra("organizerName");

        // If organizer info is not available for any reason use the defaults
        if (organizerId == null) organizerId = "";
        if (organizerName == null) organizerName = "Event Organizer";

        // Uses Notification system to inform who was drawn from organizer
        NotificationSystem notificationSystem = new NotificationSystem(
            this,
            organizerId,
            organizerName
        );
        notificationSystem.notifyLotteryWinner(replacement, eventName);
    }
}
