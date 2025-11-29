package com.example.eventlottery.view;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventlottery.R;
import com.example.eventlottery.events.Event;
import com.example.eventlottery.events.NotificationSystem;
import com.example.eventlottery.events.OrganizerService;
import com.example.eventlottery.users.User;

import java.util.List;

/**
 * OrganizerEventActivity handles sending invitations to chosen entrants.
 * allows to enter custom text and button to send invitations.
 */
public class OrganizerEventActivity extends AppCompatActivity {

    private NotificationSystem notificationSystem;
    private OrganizerService organizerService;
    private Event currentEvent;
    private String organizerId;
    private String organizerName;

    private Button sendNotificationsButton;
    private EditText customMessageInput;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_notification);

        // Get organizer info from Intent
        organizerId = getIntent().getStringExtra("organizerId");
        organizerName = getIntent().getStringExtra("organizerName");

        // Initialize systems and services with organizer info
        notificationSystem = new NotificationSystem(this, organizerId, organizerName);
        organizerService = new OrganizerService(notificationSystem);

        // Example: the currentEvent would normally be passed via Intent
        currentEvent = (Event) getIntent().getSerializableExtra("currentEvent");

        // Link layout elements
        sendNotificationsButton = findViewById(R.id.sendNotificationsButton);
        customMessageInput = findViewById(R.id.customMessageInput);

        sendNotificationsButton.setOnClickListener(v -> sendInvitations());
    }

    /**
     * Handles sending invitations to all chosen entrants.
     */
    private void sendInvitations() {
        // checks if there is an event and informs the organizer if there isn't
        if (currentEvent == null) {
            Toast.makeText(this, "Error: No event found.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<User> chosenEntrants = currentEvent.getChosenEntrants();
        if (chosenEntrants == null || chosenEntrants.isEmpty()) {
            Toast.makeText(this, "No chosen entrants to notify.", Toast.LENGTH_SHORT).show();
            return;
        }

        String customMessage = customMessageInput.getText().toString().trim();
        if (customMessage.isEmpty()) {
            // default message so error is not thrown
            customMessage = "You've been invited to sign up for " + currentEvent.getName() + "!";
        }

        // Use OrganizerService to send invitations to chosen entrants
        int sentCount = organizerService.sendInvitationsToEntrants(
                chosenEntrants,
                currentEvent.getName(),
                currentEvent.getId(),
                customMessage
        );

        // Let the organizer see the result of their action so they can verify their input
        if (sentCount > 0) {
            Toast.makeText(this, "Invitations sent to " + sentCount + " entrant(s).", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No invitations sent. All entrants have notifications disabled.", Toast.LENGTH_SHORT).show();
        }
    }
}