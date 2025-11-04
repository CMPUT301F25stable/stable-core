package com.example.eventlottery.view;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

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

import androidx.appcompat.app.AlertDialog;


import java.util.ArrayList;

public class OrganizerPanel extends AppCompatActivity {
    String userID;
    LinearLayout previous;
    ListView eventList;
    Button viewWaitlist;
    Button editEvent;
    Button createEvent;
    int selectedEventIndex = 0;  // default is first item
    Event selectedEvent;
    EventAdapter adapter;
    EventDatabase organizerEventDatabase;
    DBConnector userDatabase;
    Organizer organizer;
    ArrayList<Event> data = new ArrayList<>();


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

        // Get buttons & such, & set listeners
        eventList = findViewById(R.id.eventList);
        previous = findViewById(R.id.previous);
        viewWaitlist = findViewById(R.id.viewWaitlistButton);
        editEvent = findViewById(R.id.editEventButton);
        createEvent = findViewById(R.id.createEventButton);
        setClickListeners();

        // Initialize other variables such as the databases & users
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
     * Gets the organizer object from Firestore, & sets up 'data'.
     */
    public void getOrganizerInfo() {
        // References: https://firebase.google.com/docs/firestore/query-data/get-data#java
        // Specifically, Get a Document -> Java
        userDatabase.loadUserInfo(userID, task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    organizer = document.toObject(Organizer.class);
                    Log.d("OrganizerPanel", "Organizer loaded");

                    // Note: Called here because this function is asynchronous
                    // Will cause a crash if organizer is not loaded in by then
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
     * Displays the menu for editing an event. The user can save the new settings or cancel.
     * TODO: Menu is missing a ton of parameters. Also need to save to Firestore!
     */
    private void editEvent() {
        // Inflate dialog view & get selected event
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_event, null);
        selectedEvent = data.get(selectedEventIndex);

        // Set up variables for getting input
        EditText waitlistMax = dialogView.findViewById(R.id.waitlistMaxInput);

        // Display currentMax if there is one
        String currentMax = String.valueOf(selectedEvent.getWaitlistMax());
        if (!currentMax.equals(Integer.toString(Integer.MAX_VALUE))) {
            waitlistMax.setText(currentMax);
        }

        // Build and show AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Event Parameters");
        builder.setView(dialogView);
        builder.setNegativeButton("Close", (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton("Save", (dialog, which) -> {
            String text = waitlistMax.getText().toString();
            int maxSize = Integer.MAX_VALUE;
            // Get valid integer if any input
            if (!text.isEmpty()) {
                try {
                    maxSize = Integer.parseInt(text);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            // Check if input is negative
            if (maxSize < 0) {
                Toast.makeText(this, "Waitlist max can't be negative", Toast.LENGTH_SHORT).show();
                return;
            }
            // Change waitlistMax & update firestore
            selectedEvent.setWaitlistMax(maxSize);
            organizerEventDatabase.organizerUpdateEvent(selectedEvent);
        });
        builder.show();
    }

    /**
     * Sets the click listeners for all buttons in the organizer panel.
     * TODO: Not all event listeners are implemented!
     */
    private void setClickListeners() {
        eventList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            /**
             * Updates selectedEventIndex to the most recently clicked event.
             * @param parent The AdapterView where the click happened.
             * @param view The view within the AdapterView that was clicked (this
             *            will be a view provided by the adapter)
             * @param position The position of the view in the adapter.
             * @param id The row id of the item that was clicked.
             */
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedEventIndex = position;
            }
        });

        previous.setOnClickListener(new View.OnClickListener() {
            /**
             * Makes the previousButton end this activity, to go back to the previous one.
             * @param v The view that was clicked.
             */
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        viewWaitlist.setOnClickListener(new View.OnClickListener() {
            /**
             * Sets the listener for the view waitlist button. Opens the dialog when clicked
             * @param v The view that was clicked.
             */
            @Override
            public void onClick(View v) {
                // Event we want to view waitlist for
                selectedEvent = data.get(selectedEventIndex);

                // Get ArrayList of users & pass into dialog
                ArrayList<User> users = selectedEvent.getWaitlist().getWaitlistedUsers();
                WaitlistDialog waitlistDialog = WaitlistDialog.newInstance(users);

                // Display waitlist
                waitlistDialog.show(getSupportFragmentManager(), "WaitlistDialog");
            }
        });

        editEvent.setOnClickListener(v -> {
            // Event we want to edit
            selectedEvent = data.get(selectedEventIndex);

            // Create dialog and pass in the event
            EditEventDialog dialog = EditEventDialog.newInstance(selectedEvent);

            // Set listener for updating firestore & notify that a change was made
            dialog.setOnEventUpdatedListener(updatedEvent -> {
                organizerEventDatabase.organizerUpdateEvent(updatedEvent);
                adapter.notifyDataSetChanged();
            });

            // Display dialog
            dialog.show(getSupportFragmentManager(), "EditEventDialog");
        });

        createEvent.setOnClickListener(new View.OnClickListener() {
            /**
             * Sets listener for creating events. Displays the dialog, & updates the firestore.
             * @param v The view that was clicked.
             */
            @Override
            public void onClick(View v) {
                // Create the dialog
                CreateEventDialog createDialog = new CreateEventDialog();

                // Set listener for creating an event: Updates data & the firestore database as well.
                createDialog.setOnEventCreatedListener(event -> {
                    data.add(event);
                    organizer.createEvent(event.getId());
                    organizerEventDatabase.insert(event);
                    userDatabase.updateOrganizerCreatedEvents(organizer);
                    adapter.notifyDataSetChanged();
                });

                // Display dialog
                createDialog.show(getSupportFragmentManager(), "CreateEventDialog");
            }
        });
    }
}