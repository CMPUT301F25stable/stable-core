package com.example.eventlottery.view;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlottery.R;
import com.example.eventlottery.events.DBConnector;
import com.example.eventlottery.events.Event;
import com.example.eventlottery.events.NotificationSystem;
import com.example.eventlottery.users.Organizer;
import com.example.eventlottery.users.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * MainActivity serves as the central hub of the Event Lottery application.
 * It displays a list of available events, allows searching and filtering events,
 * manages user data, and integrates with the notification system.
 */
public class MainActivity extends AppCompatActivity {

    // --- Constants ---
    private static final int REQ_FILTER = 1001;
  
    /** Unique device identifier for this installation. */
    private String DEVICE_ID;

    /** Search view for filtering the displayed events. */
    private SearchView searchView;

    /** Adapter for binding event data to the RecyclerView. */
    private MyAdapter adapter;

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

    /** Static reference to the MainActivity instance for global access. */
    public static MainActivity instance;
    private DBConnector connector;

    /**
     * Retrieves the device UUID from SharedPreferences, or generates a new one if it does not exist.
     * @param context The context used to access SharedPreferences.
     * @return A string representing the unique device ID.
     */
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

    /**
     * Lifecycle method called when the activity is created.
     * Initializes UI elements, sets up RecyclerView and SearchView,
     * loads or creates the user, and populates sample event data.
     * @param savedInstanceState Bundle containing saved instance state, if any.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        instance = this;
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Adjust UI for system insets
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
        db = FirebaseFirestore.getInstance();
        connector = new DBConnector(this);
        connector.loadUserInfo(DEVICE_ID, task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    currentUser = document.toObject(Organizer.class);
                }
            }
        });

        // Test notification system
        User testUser = new User("u123", "Alice", "alice@gmail.com");
        NotificationSystem notifier = new NotificationSystem(this);
        notifier.notifyLotteryLoser(testUser, "Tyler the Creator Concert Lottery");

        // Initialize RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Populate sample event data
        populateSampleEvents();

        // TESTING: Simulate user joining some events
        // Comment this out if you do not want to populate the events
        //currentUser.markJoined("evt-demon-slayer-2025-11-15");
        //currentUser.getRegisteredEvents().put("evt-demon-slayer-2025-11-15", "Accepted");
        //currentUser.markJoined("evt-city-league-hockey-night-2025-12-02");
        //currentUser.getRegisteredEvents().put("evt-city-league-hockey-night-2025-12-02", "Notified");
        //currentUser.getJoinedEventIds().add("evt-winter-dance-showcase-2025-12-12");
        //saveUser(currentUser);
        // TESTING END

        // Initialize the adapter
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
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);
        loadEventsFromFirestore();

        // Configure SearchView filtering
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

    }
  
    @Override
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


    /**
     * Creates a Date object for the specified year, month, day, hour, and minute.
     * @param year The year (e.g., 2025)
     * @param month The month (0-based, e.g., Calendar.NOVEMBER)
     * @param day The day of the month
     * @param hour24 The hour in 24-hour format
     * @param minute The minute
     * @return A Date object representing the specified date and time
     */
    private Date dateOf(int year, int month, int day, int hour24, int minute) {
        Calendar c = Calendar.getInstance(Locale.CANADA);
        c.set(year, month, day, hour24, minute, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    /**
     * Filters the list of events based on the given search text.
     * @param text The search text used to filter events by name
     */
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

      /**
     * Returns a list of all events currently loaded in the activity.
     * @return ArrayList of all Event objects
     */
    public ArrayList<Event> getAllEvents() { return data; }
  
      /**
     * Returns the currently logged-in user.
     * @return The User object representing the current user
     */
    public User getCurrentUser() { return currentUser; }

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
     * Returns the event corresponding to the given event ID.
     * @param eventId The unique ID of the event
     * @return The Event object with the matching ID, or null if not found
     */
    /* TODO: Function is broken. Wasn't being used so I just commented it out for a quick fix - John
    public Event getEventById(String eventId) {
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
     */

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

    /**
     * Saves the current user.
     * <p>
     * Currently stores the user in memory; intended to be replaced with
     * Firebase or other database persistence in future updates.
     * </p>
     * @param user The User object to save
     */
    public void saveUser(User user) {
        this.currentUser = user;
    }

    /**
     * Populates sample events for demonstration and testing purposes.
     */
    private void populateSampleEvents() {
        Date start1 = dateOf(2025, Calendar.NOVEMBER, 15, 19, 30);
        Date end1 = dateOf(2025, Calendar.NOVEMBER, 15, 21, 30);
        data.add(new Event(
                "evt-demon-slayer-2025-11-15",
                "Demon Slayer: Infinity Castle – The Final Battle Begins",
                "Enter the Infinity Castle — the ever-shifting fortress where Tanjiro Kamado and the Hashira face their greatest challenge yet.",
                "Edmonton Cineplex Westmount",
                "Anime Alberta",
                "https://storage.googleapis.com/cmput-301-stable-21008.firebasestorage.app/anime.webp", start1, end1));

        Date start2 = dateOf(2025, Calendar.DECEMBER, 2, 18, 0);
        Date end2 = dateOf(2025, Calendar.DECEMBER, 2, 20, 0);
        data.add(new Event(
                "evt-city-league-hockey-night-2025-12-02",
                "City League Hockey Night",
                "Weekly rec league double-header.",
                "Terwillegar Rec Centre",
                "YEG Sports",
                "https://storage.googleapis.com/cmput-301-stable-21008.firebasestorage.app/hockey.webp", start2, end2));

        Date start3 = dateOf(2025, Calendar.DECEMBER, 12, 17, 0);
        Date end3 = dateOf(2025, Calendar.DECEMBER, 12, 19, 0);
        data.add(new Event(
                "evt-winter-dance-showcase-2025-12-12",
                "Winter Dance Showcase",
                "Contemporary + hip-hop student performances.",
                "U of A Timms Centre",
                "Dance Society",
                "https://storage.googleapis.com/cmput-301-stable-21008.firebasestorage.app/dance.jpg", start3, end3));
    }

    /**
     * Simulates user participation in events for testing purposes.
     */
    private void simulateUserEventParticipation() {
        currentUser.markJoined("evt-demon-slayer-2025-11-15");
        currentUser.getRegisteredEvents().put("evt-demon-slayer-2025-11-15", "Accepted");
        currentUser.markJoined("evt-city-league-hockey-night-2025-12-02");
        currentUser.getRegisteredEvents().put("evt-city-league-hockey-night-2025-12-02", "Notified");
        currentUser.getJoinedEventIds().add("evt-winter-dance-showcase-2025-12-12");
        saveUser(currentUser);

        // TESTING END

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

        // recyclerView.setAdapter(adapter);

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
}
