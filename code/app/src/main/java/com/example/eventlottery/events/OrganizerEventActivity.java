package com.example.eventlottery.events;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventlottery.R;
import com.example.eventlottery.users.User;

import java.util.List;

public class OrganizerEventActivity extends AppCompatActivity {
    private NotificationSystem notificationSystem;
    private Event currentEvent;
    private Button sendNotificationsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_event);

        notificationSystem = new NotificationSystem(this);
        sendNotificationsButton = findViewById(R.id.sendNotificationsButton);

        sendNotificationsButton.setOnClickListener(v -> {
            List<User> chosenEntrants = currentEvent.getChosenEntrants();
            for (User entrant : chosenEntrants) {
                notificationSystem.notifyLotteryWinner(entrant, currentEvent.getName());
            }
            Toast.makeText(this, "Notifications sent to chosen entrants!", Toast.LENGTH_SHORT).show();
        });
    }
}