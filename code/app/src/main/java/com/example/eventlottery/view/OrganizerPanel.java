package com.example.eventlottery.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eventlottery.R;
import com.example.eventlottery.events.Event;
import com.example.eventlottery.users.User;
import androidx.appcompat.app.AlertDialog;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;

public class OrganizerPanel extends AppCompatActivity {
    LinearLayout previous;
    ListView eventList;
    Button viewWaitlist;
    int selectedEventIndex = 0;  // default is first item
    Event selectedEvent;
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

        eventList = findViewById(R.id.eventList);
        previous = findViewById(R.id.previous);
        viewWaitlist = findViewById(R.id.viewWaitlistButton);
        setClickListeners();


        // TODO: REMOVE FILLER DATA. REPLACE WITH FIREBASE STUFF ONCE IT'S IMPLEMENTED
        eventList = findViewById(R.id.eventList);
        User userOne = new User("1", "testing", "testing");
        User userTwo = new User("2", "filler", "filler");
        User userThree = new User("3", "name", "name");


        Date date = new Date();
        Event eventOne = new Event("Event One", "Event Description", "Event Location", "Organizer ID", 0, date, date);
        Event eventTwo = new Event("Event Two", "Event Description", "Event Location", "Organizer ID", 0, date, date);
        Event eventThree = new Event("Event Three", "Event Description", "Event Location", "Organizer ID", 0, date, date);

        eventOne.addToWaitlist(userOne);
        eventOne.addToWaitlist(userTwo);
        eventOne.addToWaitlist(userThree);
        eventTwo.addToWaitlist(userOne);
        eventTwo.addToWaitlist(userTwo);
        eventThree.addToWaitlist(userOne);

        data.add(eventOne);
        data.add(eventTwo);
        data.add(eventThree);

        EventAdapter adapter = new EventAdapter(this, data);
        eventList.setAdapter(adapter);
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
    }
}