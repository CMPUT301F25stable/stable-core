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


public class NotificationSystem {
    private static final String TAG = "NotificationService";
    private static final String CHANNEL_ID = "lottery_winner_notifications";
    private static final String CHANNEL_NAME = "Lottery Winner Notifications";

    private Context context;

    public void NotificationSystem(Context context) {
        this.context = context;
        createNotificationChannel();
    }

    public void notifyWinner(User winner, String eventName) {
        Log.d(TAG, "Sending notification to winner: " + winner.getName());

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
            Log.d(TAG, "Notification sent successfully to " + winner.getName());
        }
    }

    /**
     * Create notification channel
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

    private int generateNotificationId(String userId) {
        return userId.hashCode();
    }
}