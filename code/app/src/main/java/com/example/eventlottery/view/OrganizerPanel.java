package com.example.eventlottery.view;


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
import java.util.Date;

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
                    adapter.notifyDataSetChanged();
                } else {
                    Log.d("OrganizerPanel", "No organizer found");
                }
            } else {
                Log.e("OrganizerPanel", "Error loading organizer info", task.getException());
            }
        });
    }

    /**
     * Displays waitlisted users.
     * @param waitlistedUsers
     */
    private void showWaitlistedUsers(ArrayList<User> waitlistedUsers) {
        // Inflate dialog view & get listView
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_waitlist, null);
        ListView userWaitlist = dialogView.findViewById(R.id.userWaitlist);

        // Get names of all users
        ArrayList<String> userNames = new ArrayList<>();
        for (User user : waitlistedUsers) {
            userNames.add(user.getName());
        }

        // Get adapter & set it
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, userNames);
        userWaitlist.setAdapter(adapter);

        // Build and show the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Waitlisted Users");
        builder.setView(dialogView);
        builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    /**
     * Displays the menu for creating an event. The user can save the new settings or cancel.
     * TODO: Menu is missing a ton of parameters. Also need to save to Firestore!
     * NOTE: Menu will look the same as edit event.
     */
    private void createEvent() {
        // Inflate dialog view
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_event, null);

        // Set up variables for getting input
        EditText waitlistMax = dialogView.findViewById(R.id.waitlistMaxInput);

        // Build and show AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New Event Parameters");
        builder.setView(dialogView);
        builder.setNegativeButton("Close", (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton("Save", (dialog, which) -> {
            String text = waitlistMax.getText().toString();
            int maxSize = Integer.MAX_VALUE;  // Assume no waitlist limit unless there is valid text input
            // Get valid integer if there's any input
            if (!text.isEmpty()) {
                try {
                    maxSize = Integer.parseInt(text);
                    // Input must contain an integer
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
            // Create event if all inputs are valid, add to this organizer's createdEvents, and input into Firestore.
            // TODO: This can only set waiting list max right now. Implement more later
            Date date = new Date();
            Event newEvent = new Event("Filler Title", "Event Description", "Event Location", "Organizer ID", 0, date, date);
            newEvent.setWaitlistMax(maxSize);
            data.add(newEvent);
            organizer.createEvent(newEvent.getId());
            System.out.println(newEvent.getId());
            organizerEventDatabase.insert(newEvent);
            userDatabase.updateOrganizerCreatedEvents(organizer);
        });
        builder.show();

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
             * Displays the waitlist for the most recently clicked event.
             * @param v The view that was clicked.
             */
            @Override
            public void onClick(View v) {
                selectedEvent = data.get(selectedEventIndex);
                ArrayList<User> users = selectedEvent.getWaitlist().getWaitlistedUsers();
                showWaitlistedUsers(users);
            }
        });

        editEvent.setOnClickListener(new View.OnClickListener() {
            /**
             * Sets the listener for editing events.
             * @param v The view that was clicked.
             */
            @Override
            public void onClick(View v) {
                // Show menu
                editEvent();
            }
        });

        createEvent.setOnClickListener(new View.OnClickListener() {
            /**
             * Sets listener for creating events.
             * @param v The view that was clicked.
             */
            @Override
            public void onClick(View v) {
                // Show menu
                createEvent();
            }
        });
    }
}