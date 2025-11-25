package com.example.eventlottery.view;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.eventlottery.R;
import com.example.eventlottery.model.EventDatabase;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;
import java.util.Map;

public class AdminEventView extends AppCompatActivity {
    private String TAG = "AdminEventView";
    private String eventId;
    private Button deleteButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_event_view);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Deserialize event details
        eventId     = getIntent().getStringExtra("id");
        String name = getIntent().getStringExtra("name");
        String description = getIntent().getStringExtra("description");
        String dateStart = getIntent().getStringExtra("dateStart");
        String timeStart = getIntent().getStringExtra("timeStart");
        String dateEnd   = getIntent().getStringExtra("dateEnd");
        String timeEnd   = getIntent().getStringExtra("timeEnd");
        String location  = getIntent().getStringExtra("location");
        String organizer = getIntent().getStringExtra("organizer");
        String imageURL = getIntent().getStringExtra("image");

        // Set up buttons & such
        deleteButton = findViewById(R.id.adminDeleteButton);
        ImageView image = findViewById(R.id.adminImageView);
        TextView title = findViewById(R.id.adminEventTitle);
        TextView subtitle = findViewById(R.id.adminEventSubtitle);
        TextView desc = findViewById(R.id.adminEventDescription);
        TextView details = findViewById(R.id.adminEventDetails);
        TextView showWaitlistSize = findViewById(R.id.adminShowWaitlistSize);

        // Set up click listeners
        setClickListeners();

        // ---- Bind UI ----
        title.setText(name);
        subtitle.setText("Official Event");
        desc.setText(description);

        String when = (dateEnd != null && timeEnd != null)
                ? String.format("📅 %s %s → %s %s", dateStart, timeStart, dateEnd, timeEnd)
                : String.format("📅 %s  🕒 %s", dateStart, timeStart);

        details.setText(when + "\n\n📍 Location: " + location + "\n\n🎟️ Organizer: " + organizer);

        // Shows the waitlist size of the event
        getWaitListSize(eventId, showWaitlistSize);

        Glide.with(this).load(imageURL).placeholder(R.drawable.placeholder).into(image);
    }

    /**
     * Sets the click listeners for the buttons in this activity.
     */
    private void setClickListeners() {
        deleteButton.setOnClickListener(new View.OnClickListener() {
            /**
             * On click, it goes back to the admin panel & deletes the event locally + on firebase.
             * @param v The view that was clicked.
             */
            @Override
            public void onClick(View v) {

            }
        });
    }

    /**
     * Updates textview showing the description of the event to include the number of
     * people the in waitlist
     * @param eventId the eventID
     * @param textView the textview to update
     */
    private void getWaitListSize(String eventId, TextView textView) {
        EventDatabase eventDatabase = new EventDatabase();

        eventDatabase.get(eventId, task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot doc = task.getResult();
                if (doc.exists()) {
                    Map<String, Object> waitlistMap = (Map<String, Object>) doc.get("waitlist");
                    if (waitlistMap != null) {

                        List<Object> waitlistUsers = (List<Object>) waitlistMap.get("waitlistedUsers");
                        if (waitlistUsers != null) {
                            int size = waitlistUsers.size();
                            textView.setText("\n🧍 Waitlist: " + size);
                        } else {
                            int size = 0;
                            textView.setText("\n🧍 Waitlist: " + size);
                        }
                    } else {
                        Log.d(TAG, "waitlistMap DNE: " + eventId);
                    }
                } else {
                    Log.d(TAG, "Doc DNE: " + eventId);
                }
            } else {
                Log.d(TAG, "Failed to get Event: " + eventId);
            }
        });
    }
}