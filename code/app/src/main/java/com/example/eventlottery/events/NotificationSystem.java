package com.example.eventlottery.events;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.eventlottery.users.User;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Handles sending user notifications for lottery events using FCM API V1.
 * Works with FCM tokens stored in users-p4 collection by MainActivity.
 */
public class NotificationSystem {

    /** TAG: Used for debugging */
    private static final String TAG = "NotificationService";

    /** Other Strings */
    private static final String CHANNEL_ID = "lottery_winner_notifications";
    private static final String CHANNEL_NAME = "Lottery Winner Notifications";
    private static final String PROJECT_ID = "cmput-301-stable-21008";
    private static final String FCM_V1_URL = "https://fcm.googleapis.com/v1/projects/" + PROJECT_ID + "/messages:send";

    // Service account JSON file name in assets folder
    private static final String SERVICE_ACCOUNT_FILE = "service-account.json";

    // Regular attributes
    private String organizerId;
    private String organizerName;


    // Other
    private Context context;
    private OkHttpClient httpClient;
    private GoogleCredentials googleCredentials;
    private ExecutorService executorService;
    private Handler mainHandler;
    private FirebaseFirestore db;

    /**
     * Constructor for NotificationSystem.
     * @param context The application context.
     * */
    public NotificationSystem(Context context, String organizerId, String organizerName) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
        this.organizerId = organizerId;
        this.organizerName = organizerName;
        createNotificationChannel();

        // Initialize HTTP client
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        // Initialize executor for background tasks
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        // Initialize Google Credentials for OAuth2
        initializeCredentials();
    }

    /**
     * Initializes Google Credentials from the service account JSON file.
     */
    private void initializeCredentials() {
        try {
            InputStream serviceAccount = context.getAssets().open(SERVICE_ACCOUNT_FILE);
            googleCredentials = GoogleCredentials
                    .fromStream(serviceAccount)
                    .createScoped(Arrays.asList("https://www.googleapis.com/auth/firebase.messaging"));

            Log.d(TAG, "✓ Google Credentials initialized successfully");
        } catch (IOException e) {
            Log.e(TAG, "✗ Failed to initialize Google Credentials", e);
            Log.e(TAG, "Make sure " + SERVICE_ACCOUNT_FILE + " is in the assets folder");
        }
    }

    /**
     * Gets a fresh OAuth2 access token (runs on background thread).
     */
    private String getAccessToken() throws IOException {
        if (googleCredentials == null) {
            Log.e(TAG, "Credentials not initialized");
            return null;
        }

        googleCredentials.refreshIfExpired();
        String token = googleCredentials.getAccessToken().getTokenValue();
        Log.d(TAG, "✓ Access token obtained");
        return token;
    }

    /**
     * Sends a "winner" notification to a user when they are selected for an event.
     */
    public void notifyLotteryWinner(User winner, String eventName) {
        Log.d(TAG, "Sending notification to winner: " + winner.getName());

        String title = "Congratulations!";
        String body = "You've been selected for " + eventName + "!";

        sendFCMNotification(winner, title, body, "winner", eventName, null);
    }

    /**
     * Sends a "not selected" notification to a user.
     */
    public void notifyLotteryLoser(User user, String eventName) {
        Log.d(TAG, "Sending notification to loser: " + user.getName());

        String title = "Thank you for entering!";
        String body = "You were not selected for " + eventName + " this time.";

        sendFCMNotification(user, title, body, "loser", eventName, null);
    }

    /**
     * Sends invitation notification to chosen entrants.
     */
    public void notifyInvitedEntrants(List<User> entrants, String eventName, String eventId, String message) {
        Log.d(TAG, "Sending invitation notifications to " + entrants.size() + " entrants");

        for (User entrant : entrants) {
            notifyInvitedEntrant(entrant, eventName, eventId, message);
        }
    }

    /**
     * Sends an individual invitation notification.
     */
    public void notifyInvitedEntrant(User entrant, String eventName, String eventId, String message) {
        Log.d(TAG, "Sending invitation notification to: " + entrant.getName());

        String title = "You're Invited! 🎉";
        String body = "You're invited to sign up for " + eventName + "!";

        sendFCMNotification(entrant, title, body, "invitation", eventName, eventId);
    }

    /**
     * Sends notification to waitlisted entrants with custom message.
     * Works with FCM tokens from users-p4 collection.
     * @param entrants List of users to send notifications to.
     * @param eventName Name of the event.
     * @param eventId ID of the event.
     * @param message Custom message to send.
     */
    public void notifyWaitlistedEntrants(List<User> entrants, String eventName, String eventId, String message) {
        Log.d(TAG, "Sending waitlist notifications to " + entrants.size() + " entrants");

        for (User entrant : entrants) {
            notifyWaitlistedEntrant(entrant, eventName, eventId, message);
        }
    }

    /**
     * Sends an individual notification to a waitlisted entrant.
     * Works with FCM tokens from users-p4 collection.
     * @param entrant User to send notification to.
     * @param eventName Name of the event.
     * @param eventId ID of the event.
     * @param message Custom message to send.
     */
    private void notifyWaitlistedEntrant(User entrant, String eventName, String eventId, String message) {
        Log.d(TAG, "Sending waitlist notification to: " + entrant.getName());

        String title = "Waiting List Update 📢";
        String body = message;

        sendFCMNotification(entrant, title, body, "waitlist", eventName, eventId);
    }

    /**
     * Sends notification to selected entrants with custom message.
     * Works with FCM tokens from users-p4 collection.
     * @param entrants List of users to send notifications to.
     * @param eventName Name of the event.
     * @param eventId ID of the event.
     * @param message Custom message to send.
     */
    public void notifySelectedEntrants(List<User> entrants, String eventName, String eventId, String message) {
        Log.d(TAG, "Sending selected notifications to " + entrants.size() + " entrants");

        for (User entrant : entrants) {
            notifySelectedEntrant(entrant, eventName, eventId, message);
        }
    }

    /**
     * Sends an individual notification to a selected entrant.
     * Works with FCM tokens from users-p4 collection.
     * @param entrant User to send notification to.
     * @param eventName Name of the event.
     * @param eventId ID of the event.
     * @param message Custom message to send.
     */
    private void notifySelectedEntrant(User entrant, String eventName, String eventId, String message) {
        Log.d(TAG, "Sending selected notification to: " + entrant.getName());

        String title = "You've Been Selected! 🎉";
        String body = message;

        sendFCMNotification(entrant, title, body, "selected", eventName, eventId);
    }

    /**
     * Core method to send FCM push notifications using V1 API.
     * This now runs on a background thread to avoid NetworkOnMainThreadException.
     * Uses FCM tokens stored by MainActivity in users-p4 collection.
     * @param user User to send notification to.
     * @param title Title of the notification.
     * @param body Body of the notification.
     * @param type Type of notification (e.g., selected, waitlist, invitation).
     * @param eventName Name of the event.
     * @param eventId ID of the event.
     */
    private void sendFCMNotification(User user, String title, String body,
                                     String type, String eventName, String eventId) {
        // Get FCM token stored by MainActivity
        String fcmToken = user.getFcmToken();

        if (fcmToken == null || fcmToken.isEmpty()) {
            Log.w(TAG, "User " + user.getName() + " has no FCM token");
            return;
        }

        // Check to see if notifications are disabled, if so, do not send
        if (!user.getNotifications()) {
            Log.d(TAG, "User " + user.getName() + " has notifications disabled");
            return;
        }

        // Run network operation on background thread
        executorService.execute(() -> {
            try {
                // Get OAuth2 access token (this makes network calls)
                String accessToken = getAccessToken();
                if (accessToken == null) {
                    Log.e(TAG, "Cannot send notification: no access token");
                    return;
                }

                // Build FCM V1 message JSON structure
                JSONObject message = new JSONObject();
                JSONObject messageContent = new JSONObject();
                JSONObject notification = new JSONObject();
                JSONObject data = new JSONObject();
                JSONObject android = new JSONObject();
                JSONObject androidNotification = new JSONObject();

                // Notification payload
                notification.put("title", title);
                notification.put("body", body);

                // Data payload
                data.put("type", type);
                data.put("eventName", eventName);
                if (eventId != null) {
                    data.put("eventId", eventId);
                }
                data.put("userName", user.getName());

                androidNotification.put("sound", "default");
                androidNotification.put("channel_id", CHANNEL_ID);

                android.put("priority", "high");
                android.put("notification", androidNotification);

                // Assemble message
                messageContent.put("token", fcmToken);
                messageContent.put("notification", notification);
                messageContent.put("data", data);
                messageContent.put("android", android);

                message.put("message", messageContent);

                Log.d(TAG, "FCM Message JSON: " + message.toString());

                // Create HTTP request
                RequestBody requestBody = RequestBody.create(
                        message.toString(),
                        MediaType.parse("application/json")
                );

                Request request = new Request.Builder()
                        .url(FCM_V1_URL)
                        .addHeader("Authorization", "Bearer " + accessToken)
                        .addHeader("Content-Type", "application/json")
                        .post(requestBody)
                        .build();

                // Send the request (OkHttp already handles this asynchronously)
                httpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e(TAG, "Failed to send FCM notification to " + user.getName(), e);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String responseBody = response.body() != null ? response.body().string() : "";

                        if (response.isSuccessful()) {
                            Log.d(TAG, "FCM notification sent successfully to " + user.getName());
                            Log.d(TAG, "Response: " + responseBody);

                            // Log to Firebase after successful send
                            logNotificationToFirebase(
                                    user.getId(),
                                    user.getName(),
                                    body,
                                    eventName,
                                    type
                            );
                        } else {
                            Log.e(TAG, "FCM notification failed for " + user.getName());
                            Log.e(TAG, "Response code: " + response.code());
                            Log.e(TAG, "Response: " + responseBody);

                            if (response.code() <= 404 || response.code() >= 400) {
                                Log.e(TAG, "Error: Invalid FCM token or token has been unregistered");
                                Log.e(TAG, "User may need to re-enable notifications");
                            }
                        }
                    }
                });

            } catch (JSONException e) {
                Log.e(TAG, "✗ Error creating FCM message JSON", e);
            } catch (IOException e) {
                Log.e(TAG, "✗ Error getting access token", e);
            }
        });
    }

    /**
     * Creates notification channel for Android 8.0+
     */
    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription("Notifications when you win a lottery");
        channel.enableVibration(true);
        channel.setVibrationPattern(new long[]{0, 500, 250, 500});

        NotificationManager notificationManager =
                context.getSystemService(NotificationManager.class);

        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
            Log.d(TAG, "Notification channel created");
        }
    }

    /**
     * Sends notification to cancelled entrants with custom message.
     * Works with FCM tokens from users-p4 collection.
     * Add this method to your NotificationSystem class.
     * @param entrants List of users to send notifications to.
     * @param eventName Name of the event.
     * @param eventId ID of the event.
     * @param message Custom message to send.
     */
    public void notifyCancelledEntrants(List<User> entrants, String eventName, String eventId, String message) {
        Log.d(TAG, "Sending cancelled notifications to " + entrants.size() + " entrants");

        for (User entrant : entrants) {
            notifyCancelledEntrant(entrant, eventName, eventId, message);
        }
    }

    /**
     * Sends notification to accepted entrants with custom message.
     * @param entrants the list of accepted entrants
     * @param eventName the event name
     * @param eventId the event id
     * @param message custom message
     */
    public void notifyAcceptedEntrants(List<User> entrants, String eventName, String eventId, String message) {
        Log.d(TAG, "Sending accepted notifications to " + entrants.size() + " entrants");

        for (User entrant : entrants) {
            notifyAcceptedEntrant(entrant, eventName, eventId, message);
        }
    }

    /**
     * Sends an individual notification to an accepted entrant.
     * @param entrant the entrant that is accepted
     * @param eventName the eventName
     * @param eventId the event id
     * @param message the message going to be sent to the entrant
     */
    private void notifyAcceptedEntrant(User entrant, String eventName, String eventId, String message) {
        Log.d(TAG, "Sending accepted notification to: " + entrant.getName());

        String title = "Event Confirmed ✓";
        String body = message;

        sendFCMNotification(entrant, title, body, "accepted", eventName, eventId);
    }


    /**
     * Sends an individual notification to a cancelled entrant.
     * Add this method to your NotificationSystem class.
     * @param entrant User to send notification to.
     * @param eventName Name of the event.
     * @param eventId ID of the event.
     * @param message Custom message to send.
     */
    private void notifyCancelledEntrant(User entrant, String eventName, String eventId, String message) {
        Log.d(TAG, "Sending cancelled notification to: " + entrant.getName());

        String title = "Event Update 📌";
        String body = message;

        sendFCMNotification(entrant, title, body, "cancelled", eventName, eventId);
    }

    /**
     * Cleanup method - call this when done with NotificationSystem
     */
    public void shutdown() {
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    /**
     * Logs a notification to Firebase Firestore.
     * @param recipientUserId The ID of the user receiving the notification
     * @param recipientUserName The name of the user receiving the notification
     * @param message The notification message sent
     * @param eventName The name of the event
     * @param notificationType The type of notification (waitlist, selected, cancelled, accepted)
     */
    private void logNotificationToFirebase(String recipientUserId, String recipientUserName,
                                           String message, String eventName, String notificationType) {
        Map<String, Object> notificationData = new HashMap<>();

        // Organizer info (sender)
        notificationData.put("senderUserId", organizerId);
        notificationData.put("senderUserName", organizerName);

        // Recipient info
        notificationData.put("recipientUserId", recipientUserId);
        notificationData.put("recipientUserName", recipientUserName);

        // Notification details
        notificationData.put("message", message);
        notificationData.put("eventName", eventName);
        notificationData.put("type", notificationType);
        notificationData.put("timestamp", FieldValue.serverTimestamp());

        db.collection("notification")
                .add(notificationData)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Notification logged with ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error logging notification to Firebase", e);
                });
    }

    /**
     * Generates a unique notification ID based on the user's ID.
     */
    private int generateNotificationId(String userId) {
        return userId.hashCode();
    }
}