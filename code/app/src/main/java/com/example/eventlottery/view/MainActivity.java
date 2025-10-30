package com.example.eventlottery.view;

import android.widget.Toast;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlottery.R;
import com.example.eventlottery.model.EventListData;

import java.util.ArrayList;
import java.util.List;

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

        RecyclerView recyclerView = findViewById(R.id.recycler_view);

        ArrayList<EventListData> data = new ArrayList<>();
        data.add(new EventListData("Event 1", R.drawable.anime,  "Description 1", "Date 1", "Time 1", "Location 1", "Organizer 1"));
        data.add(new EventListData("Event 2", R.drawable.hockey, "Description 2", "Date 2", "Time 2", "Location 2", "Organizer 2"));
        data.add(new EventListData("Event 3", R.drawable.dance, "Description 3", "Date 3", "Time 3", "Location 3", "Organizer 3"));

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recyclerView.setAdapter(new MyAdapter(data, (item, position) -> {
            Toast.makeText(this, "Clicked: " + item.getEventName(), Toast.LENGTH_SHORT).show();
        }));
        DEVICE_ID = getDeviceId(this);



    }
}