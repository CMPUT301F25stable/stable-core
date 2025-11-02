package com.example.eventlottery.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eventlottery.R;
import com.example.eventlottery.events.Event;
import com.example.eventlottery.users.User;

public class EventActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        // User user = (User) intent.getSerializableExtra("User");
        Event event = (Event) intent.getSerializableExtra("Event");
        if (event != null) {
            TextView details = findViewById(R.id.eventDetails);
            String detailsStr = "Event ID: %s\nEvent Description: %s\nEvent Location: %s\nEvent Date: %s\nEvent Time: %s";
            details.setText(String.format(detailsStr, event.getId(), event.getDescription(), event.getLocation(), event.getFormattedStartDate(), event.getFormattedStartTime()));
        }
    }
}