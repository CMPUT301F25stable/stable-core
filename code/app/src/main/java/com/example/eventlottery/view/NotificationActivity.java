package com.example.eventlottery.view;

import android.os.Bundle;
import android.util.Log;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eventlottery.R;
import com.example.eventlottery.events.DBConnector;
import com.example.eventlottery.users.User;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * The class to turn on and off from admin and notifications for users
 * @author Jensen Lee
 * */
public class NotificationActivity extends AppCompatActivity {

    // Variables
    private static final String TAG = "NotificationActivity";
    private String userId;
    // Objects
    private User currentUser;
    private DBConnector dbConnector;

    // Other stuff
    private Switch notificationSwitch;
    private FirebaseFirestore db;

    /**
     * Lifecycle method called when the activity is created.
     * Initializes Firestore, DBConnector, and UI elements
     * Setups the click listeners
     * @param savedInstanceState Bundle containing saved instance state, if any.
     * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notification);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize DBConnector to get the userID
        dbConnector = new DBConnector(this);
        userId = dbConnector.getUserId();
        Log.d(TAG, "User ID" + userId);

        // Initialize the switch
        notificationSwitch = findViewById(R.id.notificationSwitch);

        // Load user data and set initial switch state
        loadUserNotificationPreference();

        // Set up switch listener
        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (currentUser != null) {
                // Update the user's notification preference
                currentUser.setNotifications(isChecked);

                // Update in Firestore
                updateNotificationPreferenceInFirestore(isChecked);
            }
        });
    }

    /**
     * Load the user's notification preference from Firestore
     * */
    private void loadUserNotificationPreference() {
        if (userId == null || userId.isEmpty()) {
            // Default to on if no user ID
            notificationSwitch.setChecked(true);
            return;
        }

        db.collection("users-p4").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentUser = documentSnapshot.toObject(User.class);
                        if (currentUser != null) {
                            // Set switch to user's preference, default to true if not set
                            boolean notificationsEnabled = currentUser.getNotifications();
                            notificationSwitch.setChecked(notificationsEnabled);
                        }
                        else {
                            // Deafult to on
                            notificationSwitch.setChecked(true);
                        }
                    }
                    else {
                        // Deafult to on if user doesn't exist
                        notificationSwitch.setChecked(true);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "failed to load notification", Toast.LENGTH_SHORT).show();
                    notificationSwitch.setChecked(true);
                });
    }

    /**
     * Update the notification preference in Firestore
     * @param isEnabled True if notifications are enabled, false otherwise
     * */
    private void updateNotificationPreferenceInFirestore(boolean isEnabled) {
        if (userId == null || userId.isEmpty()) {
            return;
        }

        db.collection("users-p4").document(userId)
                .update("notifications", isEnabled)
                .addOnSuccessListener(aVoid -> {
                    //String message = isEnabled ? "Notifications enabled" : "Notifications disabled";
                    //Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "failed to update notification", Toast.LENGTH_SHORT).show();
                    notificationSwitch.setChecked(!isEnabled);
                });
    }
}
