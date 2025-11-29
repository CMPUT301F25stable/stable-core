package com.example.eventlottery.view;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventlottery.R;
import com.example.eventlottery.events.DBConnector;
import com.example.eventlottery.users.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
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
}
