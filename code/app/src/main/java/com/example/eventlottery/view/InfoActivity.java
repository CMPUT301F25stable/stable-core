package com.example.eventlottery.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventlottery.R;
import com.example.eventlottery.events.Event;
import com.example.eventlottery.users.User;

import java.util.Date;

/** USER STORY - 01.02.03
 * @author: Jensen Lee*/
public class InfoActivity extends AppCompatActivity {

    private TextView eventNameHeader;
    private TextView eventNameText;
    private TextView eventDescriptionText;
    private TextView eventLocationText;
    private TextView eventOrganizerText;
    private TextView statusTextView;
    private Button acceptButton;
    private Button declineButton;
    private User currentUser;
    private Event currentEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        // Bind UI elements
        eventNameHeader = findViewById(R.id.eventNameHeader);
        eventNameText = findViewById(R.id.eventNameText);
        eventDescriptionText = findViewById(R.id.eventDescriptionText);
        eventLocationText = findViewById(R.id.eventLocationText);
        eventOrganizerText = findViewById(R.id.eventOrganizerText);
        statusTextView = findViewById(R.id.statusTextView);
        acceptButton = findViewById(R.id.acceptButton);
        declineButton = findViewById(R.id.declineButton);

        // Get data from intent
        Intent intent = getIntent();

        // Reconstruct User
        String userId = intent.getStringExtra("USER_ID");
        String userName = intent.getStringExtra("USER_NAME");
        String userEmail = intent.getStringExtra("USER_EMAIL");
        String userPhone = intent.getStringExtra("USER_PHONE");
        currentUser = new User(userId, userName, userEmail, userPhone);

        // Reconstruct Event
        String eventId = intent.getStringExtra("EVENT_ID");
        String eventName = intent.getStringExtra("EVENT_NAME");
        String eventDescription = intent.getStringExtra("EVENT_DESCRIPTION");
        String eventLocation = intent.getStringExtra("EVENT_LOCATION");
        String eventOrganizer = intent.getStringExtra("EVENT_ORGANIZER");
        long startTimeMillis = intent.getLongExtra("EVENT_START_TIME", 0);
        long endTimeMillis = intent.getLongExtra("EVENT_END_TIME", 0);
        String eventStatus = intent.getStringExtra("EVENT_STATUS");

        Date startTime = new Date(startTimeMillis);
        Date endTime = new Date(endTimeMillis);

        currentEvent = new Event(eventId, eventName, eventDescription, eventLocation,
                eventOrganizer, "", startTime, endTime);

        // Register the event with current status
        currentUser.getRegisteredEvents().put(currentEvent.getId(), eventStatus);

        // Populate UI
        eventNameHeader.setText(eventName);
        eventNameText.setText(eventName);
        eventDescriptionText.setText(eventDescription);
        eventLocationText.setText(eventLocation);
        eventOrganizerText.setText(eventOrganizer);

        updateStatusDisplay();

        // Accept button
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentUser.acceptInvitation(currentEvent.getId());
                Toast.makeText(InfoActivity.this, "✅ You accepted the invitation!", Toast.LENGTH_SHORT).show();
                updateStatusDisplay();

                // Disable buttons after accepting
                acceptButton.setEnabled(false);
                declineButton.setEnabled(false);

                // TODO: Make it save the accept and save it to firebase and return
            }
        });

        // Decline button
        declineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentUser.declineInvitation(currentEvent.getId());
                Toast.makeText(InfoActivity.this, "❌ You declined the invitation.", Toast.LENGTH_SHORT).show();
                updateStatusDisplay();

                // Disable buttons after declining
                acceptButton.setEnabled(false);
                declineButton.setEnabled(false);

                // TODO: Make it save the decline and save it to firebase and return
            }
        });
    }

    // TODO: Structure change broke this
    private void updateStatusDisplay() {
        String status = currentUser.getStatusForEvent(currentEvent.getId());
        statusTextView.setText("Status: " + status);
    }
}