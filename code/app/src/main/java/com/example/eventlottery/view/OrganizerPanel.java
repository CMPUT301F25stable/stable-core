package com.example.eventlottery.view;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eventlottery.R;
import com.example.eventlottery.events.Event;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;

public class OrganizerPanel extends AppCompatActivity {
    Button previousButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_organizer_panel);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        previousButton = findViewById(R.id.previousButton);
        previousButton.setOnClickListener(new View.OnClickListener() {
            /**
             * Makes the previousButton end this activity, to go back to the previous one.
             * @param v The view that was clicked.
             */
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ListView eventList = findViewById(R.id.eventList);

        ArrayList<Event> data = new ArrayList<>();
        Date date = new Date();
        data.add(new Event("Event Name", "Event Description", "Event Location", date, date));
        data.add(new Event("Event Name", "Event Description", "Event Location", date, date));
        data.add(new Event("Event Name", "Event Description", "Event Location", date, date));

        EventAdapter adapter = new EventAdapter(this, data);
        eventList.setAdapter(adapter);
    }
}