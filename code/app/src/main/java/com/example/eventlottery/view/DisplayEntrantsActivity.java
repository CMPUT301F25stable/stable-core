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
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicInteger;

public class DisplayEntrantsActivity extends AppCompatActivity {

    private static final String TAG = "DisplayEntrantsActivity";

    private LinearLayout entrantsContainer;
    private DBConnector userDatabase;
    private FirebaseFirestore db;
    private final ArrayList<User> users = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_entrants);

        TextView title = findViewById(R.id.chosenEntrantsTitle);
        entrantsContainer = findViewById(R.id.chosenEntrantsContainer);

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

        if (eventName == null) {
            eventName = "";
        }

        if ("chosen".equals(type)) {
            title.setText("Chosen Entrants - " + eventName);
        } else if ("cancelled".equals(type)) {
            title.setText("Cancelled Entrants - " + eventName);
        } else {
            title.setText("Entrants - " + eventName);
        }

        userDatabase = new DBConnector(this);
        db = FirebaseFirestore.getInstance();

        fetchUserIdsForEvent(eventId, type);
    }

    /**
     * Fetches the list of user IDs for this event and type from Firestore.
     * "chosen"  -> selectedIds
     * "cancelled" -> cancelledEntrants
     */
    private void fetchUserIdsForEvent(String eventId, String type) {
        db.collection("event-p4")
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

        String type = getIntent().getStringExtra("type");
        String eventId = getIntent().getStringExtra("eventId");

        for (User user : users) {
            View row = inflater.inflate(R.layout.item_display_entrant, entrantsContainer, false);

            TextView nameText = row.findViewById(R.id.entrantNameText);
            TextView emailText = row.findViewById(R.id.entrantEmailText);
            Button cancelButton = row.findViewById(R.id.cancelEntrantButton);

            nameText.setText("Name: " + user.getName());

            String email = user.getEmailAddress();
            if (email == null || email.trim().isEmpty()) {
                emailText.setText("Email: N/A");
            } else {
                emailText.setText("Email: " + email);
            }

            // Show cancel button for the entrants
            cancelButton.setVisibility(View.VISIBLE);
            cancelButton.setOnClickListener(v ->
                showCancelConfirmation(user, eventId)
            );

            entrantsContainer.addView(row);
        }
    }

    /**
     * Shows confirmation dialog before cancelling an entrant
     */
    private void showCancelConfirmation(User user, String eventId) {
        String message =
            "Cancel " +
            user.getName() +
            " from this event?\n\n" +
            "They will be moved to the cancelled list and will not be allowed to renter.";

        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("Cancel Entrant")
            .setMessage(message)
            .setPositiveButton("Cancel Entrant", (d, which) ->
                cancelEntrant(user, eventId)
            )
            .setNegativeButton("Keep", null) // does nothing if not canceled
            .show();

        // Style the buttons
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        positiveButton.setTextColor(Color.BLACK);
        negativeButton.setTextColor(Color.BLACK);
    }

    /**
     * Cancels an entrant
     */
    private void cancelEntrant(User user, String eventId) {
        // Remove from selectedIds and add to cancelledEntrants
        db
            .collection("event-p4")
            .document(eventId)
            .update("selectedIds", FieldValue.arrayRemove(user.getId()), "cancelledEntrants", FieldValue.arrayUnion(user.getId()))
            .addOnSuccessListener(aVoid -> {
                // Update user's status from Notified/Accepted to Declined
                updateCancelledUserStatus(user.getId(), eventId);

                Toast.makeText(this, user.getName() + " has been cancelled from the event", Toast.LENGTH_SHORT).show();

                // Reload the display
                users.remove(user);
                displayEntrants(users);

                Log.d(TAG, "Successfully cancelled entrant: " + user.getId());
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to cancel entrant", e);
                Toast.makeText(this, "Failed to cancel entrant: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
    }

    /**
     * Updates the cancelled user's status in their user document on firebase
     */
    private void updateCancelledUserStatus(String userId, String eventId) {
        Map<String, Object> userUpdates = new HashMap<>();

        // Update registeredEvents status to "Declined"
        userUpdates.put("registeredEvents." + eventId, "Declined");

        db
            .collection("users-p4")
            .document(userId)
            .update(userUpdates)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Updated user status for cancelled entrant: " + userId);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to update user status: " + userId, e);
            });
    }
}
