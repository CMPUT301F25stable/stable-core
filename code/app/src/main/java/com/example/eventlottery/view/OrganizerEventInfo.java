package com.example.eventlottery.view;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventlottery.R;

public class OrganizerEventInfo extends AppCompatActivity {

    private TextView eventName;
    private TextView waitingCountText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_event_info);

        // Initialize UI elements
        String eventId = getIntent().getStringExtra("EVENT_NAME");
        int waitingCount = getIntent().getIntExtra("WAITLIST_COUNT", 0);
        Log.d("OrganizerEventInfo", "Waiting Count: " + waitingCount);;

        eventName = findViewById(R.id.headerText);
        eventName.setText(eventId);
        waitingCountText = findViewById(R.id.waitingCount);
        waitingCountText.setText("" + waitingCount);

        setClickListeners();
    }

    private void setClickListeners() {
        // Set click listeners

    }
}
