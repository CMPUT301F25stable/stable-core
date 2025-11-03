package com.example.eventlottery.events;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.example.eventlottery.R;
import com.example.eventlottery.users.User;
import com.example.eventlottery.view.MainActivity;

/**
 * Service to send notifications to lottery winners and losers
 */
public class NotificationService {
    private static final String TAG = "NotificationService";
    private static final String CHANNEL_ID = "lottery_notifications";
    private static final String CHANNEL_NAME = "Lottery Notifications";

    private Context context;

    public NotificationService(Context context) {
        this.context = context;
        createNotificationChannel();
    }

    /**
     * Send notification to winner when selected from waitlist
     * @param winner The user who won the lottery
     * @param eventName The name of the event they won
     */
    public void notifyWinner(User winner, String eventName) {
        Log.d(TAG, "Sending WINNER notification to: " + winner.getName());

        // Create intent to open app when notification is clicked
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("winner_notification", true);
        intent.putExtra("event_name", eventName);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                generateNotificationId(winner.getId()),
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("🎉 Congratulations!")
                .setContentText("You've been selected for " + eventName + "!")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Congratulations " + winner.getName() + "! You have been selected from the waiting list for " + eventName + ". Tap to view details."))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVibrate(new long[]{0, 500, 250, 500});

        // Send notification
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            int notificationId = generateNotificationId(winner.getId());
            notificationManager.notify(notificationId, builder.build());
            Log.d(TAG, "Winner notification sent successfully to " + winner.getName());
        }
    }

    /**
     * Send notification to loser when NOT selected from waitlist
     * @param loser The user who was not selected
     * @param eventName The name of the event they didn't win
     */
    public void notifyLoser(User loser, String eventName) {
        Log.d(TAG, "Sending LOSER notification to: " + loser.getName());

        // Create intent to open app when notification is clicked
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("loser_notification", true);
        intent.putExtra("event_name", eventName);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                generateNotificationId(loser.getId()),
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Build notification with empathetic message
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Lottery Results for " + eventName)
                .setContentText("Unfortunately, you were not selected this time.")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Thank you for your interest in " + eventName + ". Unfortunately, you were not selected in this lottery. Keep an eye out for future events!"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        // Send notification
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            int notificationId = generateNotificationId(loser.getId());
            notificationManager.notify(notificationId, builder.build());
            Log.d(TAG, "Loser notification sent successfully to " + loser.getName());
        }
    }

    /**
     * Create notification channel for the android app
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for lottery results (winners and losers)");
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 500, 250, 500});

            NotificationManager notificationManager =
                    context.getSystemService(NotificationManager.class);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
                Log.d(TAG, "Notification channel created");
            }
        }
    }

    /**
     * Generate unique notification ID for each user
     */
    private int generateNotificationId(String userId) {
        return userId.hashCode();
    }
}