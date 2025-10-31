package com.example.eventlottery.view;

import android.widget.Toast;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlottery.R;
import com.example.eventlottery.model.EventListData;
import com.example.eventlottery.users.User;

import java.util.ArrayList;
import java.util.List;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private String DEVICE_ID;
    private SearchView searchView;
    private MyAdapter adapter;
    ArrayList<EventListData> data = new ArrayList<>();

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

        User exampleUser = new User(DEVICE_ID, "Example User", "user@example.com");

        RecyclerView recyclerView = findViewById(R.id.recycler_view);

        data.add(new EventListData("Event 1", R.drawable.anime,  "Description 1", "Date 1", "Time 1", "Location 1", "Organizer 1"));
        data.add(new EventListData("Event 2", R.drawable.hockey, "Description 2", "Date 2", "Time 2", "Location 2", "Organizer 2"));
        data.add(new EventListData("Event 3", R.drawable.dance,  "Description 3", "Date 3", "Time 3", "Location 3", "Organizer 3"));
        data.add(new EventListData("Event 4", R.drawable.hockey, "Description 4", "Date 4", "Time 4", "Location 4", "Organizer 4"));
        data.add(new EventListData("Event 5", R.drawable.anime,  "Description 5", "Date 5", "Time 5", "Location 5", "Organizer 5"));
        data.add(new EventListData("Event 1", R.drawable.anime,  "Description 1", "Date 1", "Time 1", "Location 1", "Organizer 1"));
        data.add(new EventListData("Event 2", R.drawable.hockey, "Description 2", "Date 2", "Time 2", "Location 2", "Organizer 2"));
        data.add(new EventListData("Event 3", R.drawable.dance,  "Description 3", "Date 3", "Time 3", "Location 3", "Organizer 3"));
        data.add(new EventListData("Event 4", R.drawable.hockey, "Description 4", "Date 4", "Time 4", "Location 4", "Organizer 4"));
        data.add(new EventListData("Event 5", R.drawable.anime,  "Description 5", "Date 5", "Time 5", "Location 5", "Organizer 5"));
        data.add(new EventListData("Event 1", R.drawable.anime,  "Description 1", "Date 1", "Time 1", "Location 1", "Organizer 1"));
        data.add(new EventListData("Event 2", R.drawable.hockey, "Description 2", "Date 2", "Time 2", "Location 2", "Organizer 2"));
        data.add(new EventListData("Event 3", R.drawable.dance,  "Description 3", "Date 3", "Time 3", "Location 3", "Organizer 3"));
        data.add(new EventListData("Event 4", R.drawable.hockey, "Description 4", "Date 4", "Time 4", "Location 4", "Organizer 4"));
        data.add(new EventListData("Event 5", R.drawable.anime,  "Description 5", "Date 5", "Time 5", "Location 5", "Organizer 5"));


        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new MyAdapter(data, (item, position) ->
                Toast.makeText(this, "Clicked: " + item.getEventName(), Toast.LENGTH_SHORT).show()
        );
        recyclerView.setAdapter(adapter);

        adapter = new MyAdapter(data, (item, position) ->
                Toast.makeText(this, "Clicked: " + item.getEventName(), Toast.LENGTH_SHORT).show()
        );
        recyclerView.setAdapter(adapter);

        searchView = findViewById(R.id.searchView);
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return false; }
            @Override public boolean onQueryTextChange(String newText) {
                filterList(newText);
                return true;
            }
        });
    }

    private void filterList(String text) {

        String q = text == null ? "" : text.trim().toLowerCase();
        if (q.isEmpty()) {
            adapter.setFilteredList(new ArrayList<>(data));
            return;
        }

        List<EventListData> filteredList = new ArrayList<>();
        for (EventListData item : data) {
            if (item.getEventName().toLowerCase().contains(q)) {
                filteredList.add(item);
            }
        }

        if (filteredList.isEmpty()) {
            Toast.makeText(this, "No Data Found..", Toast.LENGTH_SHORT).show();
            adapter.setFilteredList(new ArrayList<>());
        } else {
            adapter.setFilteredList(filteredList);
        }

    }
}