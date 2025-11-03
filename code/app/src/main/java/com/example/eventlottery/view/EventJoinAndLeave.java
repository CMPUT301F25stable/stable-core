package com.example.eventlottery.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.eventlottery.R;

public class EventJoinAndLeave extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_join_leave_page);

        ImageView image = findViewById(R.id.imageView);
        ImageView background = findViewById(R.id.backgroundImage);
        TextView title = findViewById(R.id.eventTitle);
        TextView subtitle = findViewById(R.id.eventSubtitle);
        TextView desc = findViewById(R.id.eventDescription);
        TextView details = findViewById(R.id.eventDetails);
        Button joinButton = findViewById(R.id.joinButton);
        Button homeButton = findViewById(R.id.homeButton);


        String name = getIntent().getStringExtra("name");
        String description = getIntent().getStringExtra("description");
        String date = getIntent().getStringExtra("date");
        String time = getIntent().getStringExtra("time");
        String location = getIntent().getStringExtra("location");
        String organizer = getIntent().getStringExtra("organizer");
        String imageRes = getIntent().getStringExtra("imageRes");

        Glide.with(this)
                .load(imageRes)
                .placeholder(R.drawable.placeholder)
                .into(image);
        Glide.with(this)
                .load(imageRes)
                .placeholder(R.drawable.placeholder)
                .into(background);
        // image.setImageResource(imageRes);
        // background.setImageResource(imageRes);
        title.setText(name);
        subtitle.setText("Official Movie Screening Event");
        desc.setText(description);
        details.setText("📅 Date: " + date + "\n🕒 Time: " + time + "\n📍 Location: " + location + "\n🎟️ Organizer: " + organizer);

        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(EventJoinAndLeave.this, MainActivity.class);
            startActivity(intent);
        });

        joinButton.setOnClickListener(v -> {

            if (joinButton.getText().equals("Leave Event")) {
                joinButton.setText("Join Event");
                joinButton.setBackgroundColor(getResources().getColor(R.color.Green));
                joinButton.setTextColor(getResources().getColor(R.color.white));

            } else {
                joinButton.setText("Leave Event");
                joinButton.setBackgroundColor(getResources().getColor(R.color.Red));
                joinButton.setTextColor(getResources().getColor(R.color.white));

            }
        });



    }

}
