package com.example.eventlottery.events;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.eventlottery.R;
import com.example.eventlottery.view.MainActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Service to handle FCM token registration and incoming push notifications.
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FCMService";
    private static final String CHANNEL_ID = "lottery_notifications";

    /**
     * Called when a new FCM token is generated.
     * This happens when the app is first installed or when the token is refreshed.
     */
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "New FCM token: " + token);

        // Save token to Firestore for current user
        saveTokenToFirestore(token);
    }

    /**
     * Called when a push notification message is received while app is in foreground.
     */
    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        Log.d(TAG, "Message received from: " + message.getFrom());

        // Check if message contains a notification payload
        if (message.getNotification() != null) {
            String title = message.getNotification().getTitle();
            String body = message.getNotification().getBody();

            Log.d(TAG, "Notification Title: " + title);
            Log.d(TAG, "Notification Body: " + body);

            // Display notification
            sendNotification(title, body, message.getData());
        }

        // Check if message contains data payload
        if (message.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + message.getData());
            // Handle data payload here if needed
        }
    }

    /**
     * Saves the FCM token to Firestore for the current user.
     */
    private void saveTokenToFirestore(String token) {
        String deviceId = android.provider.Settings.Secure.getString(
                getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID
        );

        if (deviceId != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Update to use the correct collection name
            db.collection("users-p4")  // Changed from "users"
                    .document(deviceId)
                    .update("fcmToken", token)
                    .addOnSuccessListener(aVoid ->
                            Log.d(TAG, "FCM token successfully saved to Firestore"))
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error saving FCM token to Firestore", e);
                        // If update fails, try to set with merge
                        db.collection("users-p4")
                                .document(deviceId)
                                .set(new java.util.HashMap<String, Object>() {{
                                    put("fcmToken", token);
                                }}, com.google.firebase.firestore.SetOptions.merge());
                    });
        }
    }

    /**
     * Displays a notification when a push message is received.
     */
    private void sendNotification(String title, String messageBody,
                                  java.util.Map<String, String> data) {
        // Create intent to open MainActivity when notification is tapped
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add any extra data from the notification
        for (java.util.Map.Entry<String, String> entry : data.entrySet()) {
            intent.putExtra(entry.getKey(), entry.getValue());
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Create notification channel (required for Android 8.0+)
        createNotificationChannel();

        // Build the notification
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(messageBody));

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            notificationManager.notify(0, notificationBuilder.build());
        }
    }

    /**
     * Creates notification channel for Android 8.0+
     */
    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Event Lottery Notifications",
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription("Notifications for event lottery updates");
        channel.enableVibration(true);

        NotificationManager notificationManager =
                getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }
}