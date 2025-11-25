package com.example.eventlottery.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventlottery.R;
import com.example.eventlottery.users.User;

import java.util.ArrayList;

public class DisplayEntrantsActivity extends AppCompatActivity {

    private LinearLayout entrantsContainer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_entrants);

        TextView title = findViewById(R.id.chosenEntrantsTitle);
        entrantsContainer = findViewById(R.id.chosenEntrantsContainer);

        String eventName = getIntent().getStringExtra("eventName");
        String type = getIntent().getStringExtra("type");

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        if (type != null && eventName != null) {
            if (type.equals("chosen")) {
                title.setText("Chosen Entrants - " + eventName);
            } else {
                title.setText("Cancelled Entrants - " + eventName);
            }
        }

        ArrayList<User> users = (ArrayList<User>) getIntent().getSerializableExtra("users");

        if (users == null || users.isEmpty()) {
            Toast.makeText(this, "No users found for this event", Toast.LENGTH_SHORT).show();
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);

        for (User user : users) {
            View row = inflater.inflate(R.layout.item_display_entrant, entrantsContainer, false);

            TextView nameText = row.findViewById(R.id.entrantNameText);
            TextView emailText = row.findViewById(R.id.entrantEmailText);

            nameText.setText("Name: " + user.getName());

            String email = user.getEmailAddress();
            if (email == null || email.trim().isEmpty()) {
                emailText.setText("Email: N/A");
            } else {
                emailText.setText("Email: " + email);
            }

            entrantsContainer.addView(row);
        }
    }
}
