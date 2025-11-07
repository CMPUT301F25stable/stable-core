package com.example.eventlottery.view;

import android.content.Intent;
import android.widget.ImageButton;
import android.widget.Toast;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlottery.R;
import com.example.eventlottery.events.Event;
import com.example.eventlottery.users.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.Date;




public class MainActivity extends AppCompatActivity {
    private String DEVICE_ID;
    private SearchView searchView;
    private MyAdapter adapter;
    ArrayList<Event> data = new ArrayList<>();
    private User currentUser;
    public static MainActivity instance;
    private FirebaseFirestore db;
    private ListenerRegistration eventsListener;


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

        instance = this;
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        ImageButton filterBtn = findViewById(R.id.Filterbtn);

        filterBtn.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, FilterActivity.class))
        );

        DEVICE_ID = getDeviceId(this);

        // Load or create user
        currentUser = loadOrCreateUser();

        //User exampleUser = new User(DEVICE_ID, "Example User", "user@example.com");

        db = FirebaseFirestore.getInstance();


        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new MyAdapter(data, (item, position) -> {
            Intent intent = new Intent(MainActivity.this, EventJoinAndLeave.class);
            intent.putExtra("id", item.getId());
            intent.putExtra("name", item.getName());
            intent.putExtra("description", item.getDescription());
            intent.putExtra("dateStart", item.getFormattedStartDate());
            intent.putExtra("timeStart", item.getFormattedStartTime());
            intent.putExtra("dateEnd", item.getFormattedEndDate());
            intent.putExtra("timeEnd", item.getFormattedEndTime());
            intent.putExtra("location", item.getLocation());
            intent.putExtra("organizer", item.getOrganizer());
            intent.putExtra("image", item.getImage());
            intent.putExtra("waitlistMax", item.getWaitlistMax());
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);
        loadEventsFromFirestore();

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
    private Date dateOf(int year, int month, int day, int hour24, int minute) {
        Calendar c = Calendar.getInstance(Locale.CANADA);
        c.set(year, month, day, hour24, minute, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    private void filterList(String text) {

        String q = text == null ? "" : text.trim().toLowerCase();
        if (q.isEmpty()) {
            adapter.setFilteredList(new ArrayList<>(data));
            return;
        }

        List<Event> filteredList = new ArrayList<>();
        for (Event item : data) {
            if (item.getName().toLowerCase().contains(q)) {
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

    public ArrayList<Event> getAllEvents() {
        return data;
    }

    public User getCurrentUser() {
        return currentUser;
    }


    // User persistence methods (you can replace these with Firebase later)
    private User loadOrCreateUser() {
        // TODO: Load from Firebase/Database
        // For now, create a new user
        return new User(DEVICE_ID, "John Doe", "john.doe@example.com", "780-123-4567");
    }

    public void saveUser(User user) {
        // TODO: Save to Firebase/Database
        this.currentUser = user;
    }


    private void loadEventsFromFirestore() {

        db.collection("event")
                .orderBy("startTime")
                .get()
                .addOnSuccessListener(qs -> {
                    data.clear();
                    for (DocumentSnapshot doc : qs) {
                        Event ev = doc.toObject(Event.class);
                        if (ev != null) {
                            if (ev.getId() == null || ev.getId().isEmpty()) ev.setId(doc.getId());
                            data.add(ev);
                        }
                    }
                    adapter.setFilteredList(new ArrayList<>(data));
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load events: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (eventsListener != null) {
            eventsListener.remove();
            eventsListener = null;
        }
    }


}