package com.example.eventlottery.view;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventlottery.R;
import com.example.eventlottery.events.Event;
import com.example.eventlottery.users.User;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


/** USER STORY 01.05.05 - InfoActivity Class
 * @author Jensen Lee
 * @version 1
 * */
public class InfoActivity extends AppCompatActivity {

    private TextView infoTextView;
    private TextView statusTextView;
    private Button acceptButton;
    private Button declineButton;

    // Mock user and event for testing purposes
    private User testUser;
    private Event testEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        // Bind UI elements
        infoTextView = findViewById(R.id.infoTextView);
        statusTextView = findViewById(R.id.statusTextView);
        acceptButton = findViewById(R.id.acceptButton);
        declineButton = findViewById(R.id.declineButton);

        // Create mock user and event
        testUser = new User("001", "Stephen Curry", "stephencurry@gmail.com");
        Date startdate = new GregorianCalendar(2026, Calendar.AUGUST, 3).getTime();
        Date enddate = new GregorianCalendar(2026, Calendar.AUGUST, 3).getTime();
        testEvent = new Event("Marathon 2025", "Join us for the yearly marathon at City Park", "City Park", startdate, enddate);

        // Initially register the event as "Notified" (as if the user was invited)
        testUser.getEntrant().addRegisteredEvent(testEvent, "Notified");
        updateStatusDisplay();

        // Display info text (subject to change)
        infoTextView.setText(
        "🎟️ Lottery Selection Guidelines:\n\n" +
                "• Entrants are selected randomly from the waiting list.\n" +
                "• If a chosen entrant declines, another will be selected from the waitlist.\n" +
                "• Accepted entrants must confirm before the event deadline.\n\n" +
                "Use the buttons below to simulate accepting or declining an invitation."
        );

        // Accept button
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testUser.acceptInvitation(testEvent);
                Toast.makeText(InfoActivity.this, "You accepted the invitation!", Toast.LENGTH_SHORT).show();
                updateStatusDisplay();
            }
        });

        declineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testUser.declineInvitation(testEvent);
                Toast.makeText(InfoActivity.this, "You declined the invitation.", Toast.LENGTH_SHORT).show();
                updateStatusDisplay();
            }
        });

    }

    /** Helper function to display status
     * param None
     * */
    private void updateStatusDisplay() {
        String status = testUser.getEntrant().getStatusForEvent(testEvent);
        statusTextView.setText("Current Status for " + testEvent.getName() + ": " + status);
    }
}
