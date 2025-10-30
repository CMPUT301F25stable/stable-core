package com.example.eventlottery.view;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlottery.R;
import com.example.eventlottery.events.Event;

import java.util.ArrayList;
import java.util.Date;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private String DEVICE_ID;

    private String getDeviceId(Context context) {
        SharedPreferences storedData = context.getSharedPreferences("DeviceId", Context.MODE_PRIVATE);
        String storedUUID = storedData.getString("UUID", "");

        if (storedUUID.isEmpty()) {
            String randomUUID = String.valueOf(UUID.randomUUID());
            storedUUID = randomUUID;
            SharedPreferences.Editor editor = storedData.edit();
            editor.putString("UUID", randomUUID);
            editor.apply();
        }
        return storedUUID;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        DEVICE_ID = getDeviceId(this);
        // Load user data here using DEVICE_ID

        RecyclerView recyclerView = findViewById(R.id.recycler_view);

        ArrayList<Event> data = new ArrayList<>();
        data.add(new Event("Event 1", "Description 1", "Location 1", "Organizer 1", R.drawable.anime, new Date(1700000000000L), new Date(2000000000000L)));
        data.add(new Event("Event 2", "Description 2", "Location 2", "Organizer 2", R.drawable.hockey, new Date(2000000000000L), new Date(2200000000000L)));
        data.add(new Event("Event 3", "Description 3", "Location 3", "Organizer 3", R.drawable.dance, new Date(2200000000000L), new Date(2400000000000L)));

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recyclerView.setAdapter(new MyAdapter(data, (item, position) -> {
            Intent intent = new Intent(this, EventActivity.class);
            intent.putExtra("Event", item);
            intent.putExtra("Position", position);
            startActivity(intent);
        }));
    }
}