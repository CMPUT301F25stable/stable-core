package com.example.eventlottery.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.eventlottery.R;
import com.example.eventlottery.model.EventDatabase;
import com.example.eventlottery.users.User;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;
import java.util.Map;

public class EventJoinAndLeave extends AppCompatActivity {
    private static final String TAG = "EventJoinAndLeave";
    private Button joinButton;
    private String eventId;

    private FirebaseFirestore db;
    private DocumentReference userDoc;
    private ListenerRegistration userListener;

    private User user;
    private boolean isJoined = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_join_leave_page);

        ImageView image = findViewById(R.id.imageView);
        ImageView background = findViewById(R.id.backgroundImage);
        TextView title = findViewById(R.id.eventTitle);
        TextView subtitle = findViewById(R.id.eventSubtitle);
        TextView desc = findViewById(R.id.eventDescription);
        TextView details = findViewById(R.id.eventDetails);
        joinButton = findViewById(R.id.joinButton);
        Button homeButton = findViewById(R.id.homeButton);

        // ---- Get intent extras (must include a stable eventId) ----
        eventId     = getIntent().getStringExtra("id");
        String name = getIntent().getStringExtra("name");
        String description = getIntent().getStringExtra("description");

        String dateStart = getIntent().getStringExtra("dateStart");
        String timeStart = getIntent().getStringExtra("timeStart");
        String dateEnd   = getIntent().getStringExtra("dateEnd");
        String timeEnd   = getIntent().getStringExtra("timeEnd");

        String location  = getIntent().getStringExtra("location");
        String organizer = getIntent().getStringExtra("organizer");

        String imageURL = getIntent().getStringExtra("image");

        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Missing event ID.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // ---- Bind UI ----
        title.setText(name);
        subtitle.setText("Official Event");
        desc.setText(description);

        String when = (dateEnd != null && timeEnd != null)
                ? String.format("📅 %s %s → %s %s", dateStart, timeStart, dateEnd, timeEnd)
                : String.format("📅 %s  🕒 %s", dateStart, timeStart);

        details.setText(when + "\n\n📍 Location: " + location + "\n\n🎟️ Organizer: " + organizer);

        // Note: This does not show up for the test events on the MainActivity unless they are valid
        // events in firebase
        getWaitListSize(eventId, details);

        Glide.with(this).load(imageURL).placeholder(R.drawable.placeholder).into(image);
        Glide.with(this).load(imageURL).placeholder(R.drawable.placeholder).into(background);

        homeButton.setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));

        user = new User(this);
        db = FirebaseFirestore.getInstance(); // get firestore instance
        userDoc = db.collection("users").document(user.getId()); // get user

        // Check if user has already joined any events before loading
        userDoc.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) { // If user exists, check if they are in any events
                List<String> joinedWaitlist = (List<String>) documentSnapshot.get("waitlistedEvents");
                user.setWaitlistedEventIds(joinedWaitlist);

                isJoined = user.isWaitlisted(eventId); // Check if user is joined in current event
                updateJoinButton(isJoined);
            } else {
                user.setWaitlistedEventIds(null); // If user doesn't exist, set joinedEventIds to null
                isJoined = false;
                updateJoinButton(false);
            }
        });

        joinButton.setOnClickListener(v -> toggleJoin());
    }

    /**
     * Toggle the user's join status.
     * If the user is already joined, leave the event.
     * If the user is not joined, join the event.
     */
    private void toggleJoin() {
        boolean newState = !isJoined;
        updateJoinButton(newState); // Update the button to reflect the new state


        if (newState) { // If joined
            userDoc.update("waitlistedEvents", FieldValue.arrayUnion(eventId)) // Add to Firestore
                    .addOnSuccessListener(v -> {
                        user.AddJoinedWaitlist(eventId);
                        isJoined = true;
                    })
                    .addOnFailureListener(e -> {
                        updateJoinButton(isJoined);
                        Toast.makeText(this, "Failed to join. Try again.", Toast.LENGTH_SHORT).show();
                    });
        } else { // If left
            userDoc.update("waitlistedEvents", FieldValue.arrayRemove(eventId)) // Remove from Firestore
                    .addOnSuccessListener(v -> {
                        user.RemoveLeftWaitlist(eventId);
                        isJoined = false;
                    })
                    .addOnFailureListener(e -> {
                        updateJoinButton(isJoined);
                        Toast.makeText(this, "Failed to leave. Try again.", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void updateJoinButton(boolean joined) {
        if (joined) {
            joinButton.setText("Leave Waitlist");
            joinButton.setBackgroundColor(ContextCompat.getColor(this, R.color.Red));
        } else {
            joinButton.setText("Join Waitlist");
            joinButton.setBackgroundColor(ContextCompat.getColor(this, R.color.Green));
        }

        this.isJoined = joined;
    }

    /**
     * Removes the listener when the activity is destroyed.
     * Prevents memory leaks
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userListener != null) {
            userListener.remove();
            userListener = null;
        }
    }

    private void getWaitListSize(String eventId, TextView textView) {
        EventDatabase eventDatabase = new EventDatabase();

        eventDatabase.get(eventId, task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot doc = task.getResult();
                if (doc.exists()) {
                    Map<String, Object> waitlistMap = (Map<String, Object>) doc.get("waitlist");
                    if (waitlistMap != null) {

                        List<Object> waitlistUsers = (List<Object>) waitlistMap.get("waitlistedUsers");
                        if (waitlistUsers != null) {
                            int size = waitlistUsers.size();
                            textView.append("\n🧍 Waitlist: " + size);
                        } else {
                            int size = 0;
                            textView.append("\n🧍 Waitlist: " + size);
                        }
                    } else {
                        Log.d(TAG, "waitlistMap DNE: " + eventId);
                    }
                } else {
                    Log.d(TAG, "Doc DNE: " + eventId);
                }
            } else {
                Log.d(TAG, "Failed to get Event: " + eventId);
            }
        });
    }



}
