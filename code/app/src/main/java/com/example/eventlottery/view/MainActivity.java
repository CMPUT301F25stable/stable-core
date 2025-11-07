package com.example.eventlottery.view;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;
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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    // --- Constants ---
    private static final int REQ_FILTER = 1001;

    // --- Firebase and Database ---
    private FirebaseFirestore db;
    private ListenerRegistration eventsListener;

    // --- Data and Models ---
    private final ArrayList<Event> data = new ArrayList<>();
    private User currentUser;

    // --- Filters ---
    public static ArrayList<String> selectedTags = new ArrayList<>();
    public static Set<Long> selectedDatesMidnight = new HashSet<>();

    // --- UI Components ---
    private SearchView searchView;
    private MyAdapter adapter;

    // --- Device and Activity ---
    private String DEVICE_ID;
    public static MainActivity instance;


    private String getDeviceId(Context context) {
        SharedPreferences storedData = context.getSharedPreferences("DeviceId", Context.MODE_PRIVATE);
        String storedUUID = storedData.getString("UUID", "");
        if (storedUUID.isEmpty()) {
            String randomUUID = String.valueOf(UUID.randomUUID());
            storedUUID = randomUUID;
            storedData.edit().putString("UUID", randomUUID).apply();
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

        findViewById(R.id.Filterbtn).setOnClickListener(v -> {
            ArrayList<String> allTags = collectAllTags(data);
            Intent intent = new Intent(MainActivity.this, FilterActivity.class);
            intent.putStringArrayListExtra("allTags", allTags);
            intent.putStringArrayListExtra("preSelectedTags", new ArrayList<>(selectedTags));
            intent.putExtra("preSelectedDates", new ArrayList<>(selectedDatesMidnight)); // ArrayList<Long>
            startActivityForResult(intent, REQ_FILTER);
        });

        DEVICE_ID = getDeviceId(this);
        currentUser = loadOrCreateUser();
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

    @Override
    protected void onResume() {
        super.onResume();

        if ((!selectedTags.isEmpty()) || (!selectedDatesMidnight.isEmpty())) {
            applyFilters(selectedTags, selectedDatesMidnight);
        } else {
            adapter.setFilteredList(new ArrayList<>(data));
        }

    }@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent dataIntent) {
        super.onActivityResult(requestCode, resultCode, dataIntent);

        if (requestCode == REQ_FILTER && resultCode == RESULT_OK && dataIntent != null) {

            ArrayList<String> tags = dataIntent.getStringArrayListExtra("selectedTags");
            ArrayList<Long> dates = (ArrayList<Long>) dataIntent.getSerializableExtra("selectedDates");

            if (tags != null) {
                selectedTags = new ArrayList<>(tags);
            }

            selectedDatesMidnight.clear();
            if (dates != null) {
                selectedDatesMidnight.addAll(dates);
            }

            if (!selectedTags.isEmpty() || !selectedDatesMidnight.isEmpty()) {
                applyFilters(selectedTags, selectedDatesMidnight);
            }
            else {
                adapter.setFilteredList(new ArrayList<>(data));
            }
        }
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
            // Reapply current filters (tags/dates) or show all
            if (!selectedTags.isEmpty() || !selectedDatesMidnight.isEmpty()) {
                applyFilters(selectedTags, selectedDatesMidnight);
            } else {
                adapter.setFilteredList(new ArrayList<>(data));
            }
            return;
        }

        List<Event> filteredList = new ArrayList<>();
        for (Event item : data) {
            if (item.getName() != null && item.getName().toLowerCase().contains(q)) {
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

    public ArrayList<Event> getAllEvents() { return data; }
    public User getCurrentUser() { return currentUser; }

    private User loadOrCreateUser() {
        return new User(DEVICE_ID, "John Doe", "john.doe@example.com", "780-123-4567");
    }

    public void saveUser(User user) { this.currentUser = user; }

    private void loadEventsFromFirestore() {
        db.collection("event")
                .orderBy("startTime")
                .get()
                .addOnSuccessListener(query -> {
                    data.clear();

                    for (DocumentSnapshot doc : query) {
                        Event event = doc.toObject(Event.class);
                        if (event == null) continue;

                        if (event.getId() == null || event.getId().isEmpty()) {
                            event.setId(doc.getId());
                        }
                        data.add(event);
                    }

                    if (!selectedTags.isEmpty() || !selectedDatesMidnight.isEmpty()) {
                        applyFilters(selectedTags, selectedDatesMidnight);
                    } else {
                        adapter.setFilteredList(new ArrayList<>(data));
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load events: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void applyFilters(List<String> tags, Set<Long> datesMidnight) {
        boolean hasTagFilter = (tags != null && !tags.isEmpty());
        boolean hasDateFilter = (datesMidnight != null && !datesMidnight.isEmpty());

        if (!hasTagFilter && !hasDateFilter) {
            adapter.setFilteredList(new ArrayList<>(data));
            return;
        }

        List<Event> filtered = new ArrayList<>();

        for (Event event : data) {
            boolean passesTags = true;
            boolean passesDates = true;

            if (hasTagFilter) {
                List<String> eventTags = event.getFilterTags();

                passesTags = (eventTags != null) && !java.util.Collections.disjoint(eventTags, tags);
            }

            if (hasDateFilter) {
                passesDates = occursOnAnySelectedDate(event, datesMidnight);
            }

            if (passesTags && passesDates) {
                filtered.add(event);
            }
        }

        adapter.setFilteredList(filtered);
    }

    /**
     * Returns true if the event happens on ANY of the selected days.
     * Each selected day is a 24-hour window [midnight..11:59:59.999].
     */
    private boolean occursOnAnySelectedDate(Event event, Set<Long> selectedDaysMidnight) {
        Date start = event.getStartTime();
        Date end = event.getEndTime();
        if (start == null) return false;
        if (end == null) end = start;

        long startMs = start.getTime();
        long endMs = Math.max(end.getTime(), startMs);

        for (Long dayStartMs : selectedDaysMidnight) {
            long dayEndMs = dayStartMs + (24L * 60 * 60 * 1000) - 1L;
            boolean overlaps = (startMs <= dayEndMs) && (endMs >= dayStartMs);
            if (overlaps) return true;
        }
        return false;
    }

    private ArrayList<String> collectAllTags(List<Event> events) {
        Set<String> allTags = new HashSet<>();
        for (Event event : events) {
            List<String> tags = event.getFilterTags();
            if (tags != null) {
                allTags.addAll(tags);
            }
        }
        return new ArrayList<>(allTags);
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
