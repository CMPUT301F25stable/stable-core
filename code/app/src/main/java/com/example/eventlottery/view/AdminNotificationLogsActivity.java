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
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * Activity for administrators to review all notification logs.
 * Displays notifications sent by organizers to entrants.
 */
public class AdminNotificationLogsActivity extends AppCompatActivity {
    private static final String TAG = "AdminNotificationLogs";

    private LinearLayout logsContainer;
    private FirebaseFirestore db;
    private TextView emptyStateText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_notification_logs);

        // Initialize views
        logsContainer = findViewById(R.id.logsContainer);
        emptyStateText = findViewById(R.id.emptyStateText);
        Button backButton = findViewById(R.id.backButton);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        backButton.setOnClickListener(v -> finish());

        loadNotificationLogs();
    }

    /**
     * Loads all notification logs from Firestore ordered by timestamp descending
     */
    private void loadNotificationLogs() {
        db.collection("notification")
                // Sort by timestamp in descending order
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        emptyStateText.setVisibility(View.VISIBLE);
                        logsContainer.setVisibility(View.GONE);
                        return;
                    }

                    // Clear existing logs and display new ones (disables empty state text if there are logs)
                    emptyStateText.setVisibility(View.GONE);
                    logsContainer.setVisibility(View.VISIBLE);
                    logsContainer.removeAllViews();

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        displayLogEntry(doc);
                    }
                })

                // Handle failure to load logs
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load notification logs", e);
                    Toast.makeText(this, "Failed to load logs: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    /**
     * Displays a single log entry
     */
    private void displayLogEntry(DocumentSnapshot doc) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View logItem = inflater.inflate(R.layout.item_notification_log, logsContainer, false);

        // Initialize views to be displayed
        TextView timestampText = logItem.findViewById(R.id.timestampText);
        TextView organizerText = logItem.findViewById(R.id.organizerText);
        TextView eventNameText = logItem.findViewById(R.id.eventNameText);
        TextView typeText = logItem.findViewById(R.id.typeText);
        TextView recipientCountText = logItem.findViewById(R.id.recipientCountText);
        TextView messageText = logItem.findViewById(R.id.messageText);
        TextView recipientsText = logItem.findViewById(R.id.recipientsText);

        // Get data from document (firestore)
        Timestamp timestamp = doc.getTimestamp("timestamp");
        String senderName = doc.getString("senderUserName");
        String senderId = doc.getString("senderUserId");
        String eventName = doc.getString("eventName");
        String type = doc.getString("type");
        Long recipientCount = doc.getLong("recipientCount");
        String message = doc.getString("message");
        Map<String, Object> recipients = (Map<String, Object>) doc.get("recipients");

        // Format and display timestamp
        if (timestamp != null) {
            Date date = timestamp.toDate();
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault());
            timestampText.setText(dateFormat.format(date));
        } else {
            timestampText.setText("Unknown time");
        }

        // Display organizer info (just in case
        organizerText.setText("Organizer: " + senderName);

        // Display event name
        eventNameText.setText("Event: " + (eventName != null ? eventName : "Unknown"));

        // Display notification type
        typeText.setText("Type: " + capitalizeFirst(type != null ? type : "unknown"));

        // Display recipient count
        if (recipientCount != null) {
            recipientCountText.setText("Recipients: " + recipientCount);
        } else {
            recipientCountText.setText("Recipients: Unknown");
        }

        // Display message
        messageText.setText("Message: " + (message != null ? message : "No message"));

        // Display recipients (first 3 only so log is not overwhelmed with data
        // checks if someone received the notification
        if (recipients != null && !recipients.isEmpty()) {
            StringBuilder recipientsList = new StringBuilder("Recipients: ");
            int count = 0;
            for (Map.Entry<String, Object> entry : recipients.entrySet()) {
                if (count >= 3) {
                    recipientsList.append("...");
                    break;
                }
                if (count > 0) {
                    recipientsList.append(", ");
                }
                recipientsList.append(entry.getValue());
                count++;
            }
            recipientsText.setText(recipientsList.toString());
        } else {
            recipientsText.setText("Recipients: None");
        }

        logsContainer.addView(logItem);
    }

    /**
     * Capitalizes the first letter of a string (it helps match the format of the rest of the logs)
     */
    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
