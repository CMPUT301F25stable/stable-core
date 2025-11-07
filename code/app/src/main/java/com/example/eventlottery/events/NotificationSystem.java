package com.example.eventlottery.events;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.example.eventlottery.R;
import com.example.eventlottery.users.User;
import com.example.eventlottery.view.MainActivity;

/**
 * Handles sending user notifications for lottery events in the EventLottery app.
 * <p>
 * This class is responsible for notifying users when they are selected (winners)
 * or not selected (losers) for a particular event. It creates and manages
 * the notification channel and builds notifications with appropriate content and actions.
 * more notifications may be added later
 * </p>
 */
public class NotificationSystem {
    /** Tag used for logging messages related to the notification service. */
    private static final String TAG = "NotificationService";
    /** Unique identifier for the notification channel used for lottery winner notifications. */
    private static final String CHANNEL_ID = "lottery_winner_notifications";
    /** User-visible name for the notification channel that displays lottery winner notifications. */
    private static final String CHANNEL_NAME = "Lottery Winner Notifications";

    private Context context;
    /**
     * Constructs a new {@code NotificationSystem} with the provided application context.
     * Automatically creates a notification channel if it does not already exist.
     *
     * @param context the application context used for sending notifications
     */
    public NotificationSystem(Context context) {
        this.context = context;
        createNotificationChannel();
    }

    /**
     * Sends a "winner" notification to a user when they are selected for an event.
     * The notification congratulates the user and opens the app when tapped.
     * @param winner    the {@link User} who has been selected
     * @param eventName the name of the event the user was selected for
     */
    public void notifyLotteryWinner(User winner, String eventName) {
        Log.d(TAG, "Sending notification to winner: " + winner.getName());

        // Create intent to open app when notification is clicked
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("winner_notification", true);
        intent.putExtra("event_name", eventName);

        // Create pending intent to open activity when notification is clicked and update current activity
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                generateNotificationId(winner.getId()),
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // TODO make notification svg
                .setContentTitle("Congratulations!")
                .setContentText("You've been selected for " + eventName + "!")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Congratulations " + winner.getName() + "! You have been selected for  " + eventName + ". Tap to view details."))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVibrate(new long[]{0, 500, 250, 500});

        // Send notification
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // prints in logcat if the notification is sent successfully
        if (notificationManager != null) {
            int notificationId = generateNotificationId(winner.getId());
            notificationManager.notify(notificationId, builder.build());
            Log.d(TAG, "Notification sent successfully to " + winner.getName());
        }
    }

    /**
     * Sends a "not selected" notification to a user when they are not chosen for an event.
     * The notification thanks the user for participating and encourages future engagement.
     *
     * @param user      the {@link User} who was not selected
     * @param eventName the name of the event the user entered
     */
    public void notifyLotteryLoser(User user, String eventName) {
        Log.d(TAG, "Sending notification to loser: " + user.getName());

        // Create intent to open app when notification is clicked
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("loser_notification", true);
        intent.putExtra("event_name", eventName);

        // PendingIntent setup
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                generateNotificationId(user.getId() + "_lose"), // Change in id to avoid same ID problems
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Build "not selected" notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Thank you for entering!")
                .setContentText("You were not selected for " + eventName + " this time.")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Hi " + user.getName() + ", unfortunately you were not selected for "
                                + eventName + " this time. We appreciate your interest — keep an eye out for future opportunities!"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVibrate(new long[]{0, 250, 100, 250});

        // Send the notification
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            int notificationId = generateNotificationId(user.getId() + "_lose");
            notificationManager.notify(notificationId, builder.build());
            Log.d(TAG, "Loser notification sent successfully to " + user.getName());
        }
    }



    /**
     * Creates a notification channel for lottery notifications.
     * The channel is configured with vibration and high importance
     * so that notifications appear prominently to users.
     */
    private void createNotificationChannel() {
        // creates a high priority notification channel
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
        // prints in logcat if the creation of notification channel is successful
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
            Log.d(TAG, "Notification channel created");
        }
    }

    /**
     * Generates a unique notification ID based on the user's ID.
     * This ensures each user's notifications do not overlap.
     * @param userId the ID of the user
     * @return an integer hash code representing the notification ID
     */
    private int generateNotificationId(String userId) {
        return userId.hashCode();
    }
}