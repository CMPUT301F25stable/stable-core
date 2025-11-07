package com.example.eventlottery.view;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eventlottery.R;
import com.example.eventlottery.events.DBConnector;
import com.example.eventlottery.events.Event;
import com.example.eventlottery.model.EventDatabase;
import com.example.eventlottery.users.Organizer;
import com.example.eventlottery.users.User;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;

/**
 * The {@code OrganizerPanel} class represents the main interface for organizers.
 * It allows them to view, edit, and create events, as well as access the waitlists
 * of their current events. This activity connects to Firebase to load organizer data
 * and synchronize event information.
 * <p><b>Features:</b></p>
 * <ul>
 *     <li>Displays a list of events owned by the organizer</li>
 *     <li>Allows organizers to edit, create, and view event waitlists</li>
 *     <li>Synchronizes organizer and event data using Firestore</li>
 * </ul>
 * <p>
 */
public class OrganizerPanel extends AppCompatActivity {

    /** The unique Android device ID for the current organizer. */
    private String userID;

    /** The layout representing the "previous" section or navigation area. */
    private LinearLayout previous;

    /** The {@link ListView} displaying the organizer's events. */
    private ListView eventList;

    /** The button used to view the waitlist of the selected event. */
    private Button viewWaitlist;

    /** The button used to edit the currently selected event. */
    private Button editEvent;

    /** The button used to create a new event. */
    private Button createEvent;

    /** The index of the currently selected event in the list (default is 0). */
    private int selectedEventIndex = 0;

    /** The {@link Event} currently selected by the organizer. */
    private Event selectedEvent;

    /** The adapter used to bind event data to the {@link ListView}. */
    private EventAdapter adapter;

    /** The {@link EventDatabase} managing event-related Firestore operations. */
    private EventDatabase organizerEventDatabase;

    /** The {@link DBConnector} used to load and update organizer information from Firestore. */
    private DBConnector userDatabase;

    /** The {@link Organizer} object representing the logged-in organizer. */
    private Organizer organizer;

    /** The list of events owned or created by the organizer. */
    private ArrayList<Event> data = new ArrayList<>();

    /**
     * Called when the activity is created. Initializes the layout, retrieves organizer data,
     * and sets up button listeners and the event list adapter.
     *
     * @param savedInstanceState The previously saved state of the activity (if any).
     */
    @SuppressLint("HardwareIds")
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

        // Initialize UI components and event listeners
        eventList = findViewById(R.id.eventList);
        previous = findViewById(R.id.previous);
        viewWaitlist = findViewById(R.id.viewWaitlistButton);
        editEvent = findViewById(R.id.editEventButton);
        createEvent = findViewById(R.id.createEventButton);
        setClickListeners();

        // Initialize databases and organizer info
        userID = Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
        organizerEventDatabase = new EventDatabase();
        userDatabase = new DBConnector(this);
        getOrganizerInfo();
        adapter = new EventAdapter(this, data);
        eventList.setAdapter(adapter);
    }

    /**
     * Retrieves the organizer's information from Firestore and loads their associated events.
     * The result is asynchronously handled and triggers a database update for event listings.
     * <p>References:
     * <a href="https://firebase.google.com/docs/firestore/query-data/get-data#java">Firestore: Get a Document (Java)</a></p>
     */
    public void getOrganizerInfo() {
        userDatabase.loadUserInfo(userID, task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    organizer = document.toObject(Organizer.class);
                    Log.d("OrganizerPanel", "Organizer loaded");

                    // Load events after organizer data is retrieved
                    organizerEventDatabase.organizerGetEvents(organizer, data, adapter);
                } else {
                    Log.d("OrganizerPanel", "No organizer found");
                }
            } else {
                Log.e("OrganizerPanel", "Error loading organizer info", task.getException());
            }
        });
    }

    /**
     * Sets up all click listeners for buttons and list items in the organizer panel.
     * This includes viewing waitlists, editing events, and creating new events.
     */
    private void setClickListeners() {
        eventList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            /**
             * Updates {@link #selectedEventIndex} when the user selects a new event from the list.
             *
             * @param parent The AdapterView where the click occurred.
             * @param view The view representing the clicked item.
             * @param position The position of the clicked item.
             * @param id The row ID of the clicked item.
             */
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedEventIndex = position;
            }
        });

        previous.setOnClickListener(new View.OnClickListener() {
            /**
             * Handles the "Previous" button click. Ends this activity to return to the previous screen.
             *
             * @param v The view that was clicked.
             */
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        viewWaitlist.setOnClickListener(new View.OnClickListener() {
            /**
             * Handles the "View Waitlist" button click.
             * Opens a dialog showing all users currently on the selected event's waitlist.
             *
             * @param v The view that was clicked.
             */
            @Override
            public void onClick(View v) {
                selectedEvent = data.get(selectedEventIndex);
                ArrayList<User> users = selectedEvent.getWaitlist().getWaitlistedUsers();
                WaitlistDialog waitlistDialog = WaitlistDialog.newInstance(users);
                waitlistDialog.show(getSupportFragmentManager(), "WaitlistDialog");
            }
        });

        editEvent.setOnClickListener(v -> {
            selectedEvent = data.get(selectedEventIndex);
            EditEventDialog dialog = EditEventDialog.newInstance(selectedEvent);

            // Updates event in Firestore and refreshes adapter when an edit is made
            dialog.setOnEventUpdatedListener(updatedEvent -> {
                organizerEventDatabase.organizerUpdateEvent(updatedEvent);
                adapter.notifyDataSetChanged();
            });

            dialog.show(getSupportFragmentManager(), "EditEventDialog");
        });

        createEvent.setOnClickListener(new View.OnClickListener() {
            /**
             * Handles the "Create Event" button click.
             * Displays a dialog for creating a new event, updates Firestore, and refreshes the event list.
             *
             * @param v The view that was clicked.
             */
            @Override
            public void onClick(View v) {
                CreateEventDialog createDialog = new CreateEventDialog();

                createDialog.setOnEventCreatedListener(event -> {
                    data.add(event);
                    organizer.createEvent(event.getId());
                    organizerEventDatabase.insert(event);
                    userDatabase.updateOrganizerCreatedEvents(organizer);
                    adapter.notifyDataSetChanged();
                });

                createDialog.show(getSupportFragmentManager(), "CreateEventDialog");
            }
        });
    }
}
