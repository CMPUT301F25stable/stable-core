package com.example.eventlottery.view;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eventlottery.R;
import com.example.eventlottery.events.DBConnector;
import com.example.eventlottery.events.Event;
import com.example.eventlottery.users.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class AdminPanel extends AppCompatActivity {
    /** The admin using this panel */
    private User admin;
    /** Device Id (identifier for user) */
    private String deviceId;
    /** Firebase instance for retrieving events */
    private FirebaseFirestore db;
    /** DBConnector instance for retrieving admin details */
    private DBConnector userDatabase;
    /** Eventlist fragment instance for setting adapter, etc */
    private EventlistFragment eventlistFragment;
    /** UserList fragment instance */
    private UserListFragment userListFragment;
    /** Holds event data locally */
    private ArrayList<Event> eventListData;
    /** Adapter for event object */
    private EventAdapter eventAdapter;
    /** Holds user profile data locally */
    private ArrayList<User> userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_panel);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //


        // Get deviceId, connect to DB & load in user
        deviceId = Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
        userDatabase = new DBConnector(this);
        loadAdmin();

        // Initialize other activity variables
        eventlistFragment = (EventlistFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainerView1);
        userListFragment = (UserListFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainerView2);
        eventListData = new ArrayList<Event>();
        userList = new ArrayList<User>();
        db = FirebaseFirestore.getInstance();

        // Load in events & users
        loadEventsFromFirestore();
        loadProfilesFromFirestore();
    }

    /**
     * Loads in the admin's details from firebase.
     */
    private void loadAdmin() {
        userDatabase.loadUserInfo(deviceId, task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    admin = document.toObject(User.class);
                    Log.d("AdminPanel", "Admin loaded");

                    // Create adapter & load all events from firestore
                    eventAdapter = new EventAdapter(AdminPanel.this, eventListData);
                    eventlistFragment.setAdapter(eventAdapter);
                } else {
                    Log.d("OrganizerPanel", "No organizer found");
                }
            } else {
                Log.e("OrganizerPanel", "Error loading organizer info", task.getException());
            }
        });
    }

    /**
     * Adds every event from firestore into eventListData.
     */
    private void loadEventsFromFirestore() {
        db.collection("event-p4")
                .orderBy("startTime")
                .get()
                .addOnSuccessListener(query -> {
                    eventListData.clear();

                    for (DocumentSnapshot doc : query) {
                        Event event = doc.toObject(Event.class);
                        if (event == null) continue;

                        if (event.getId() == null || event.getId().isEmpty()) {
                            event.setId(doc.getId());
                        }

                        eventListData.add(event);
                    }

                    eventAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load events: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    /**
     * Adds every profile (but the admin using this) into userList.
     */
    private void loadProfilesFromFirestore() {
        db.collection("users-p4").get()
                .addOnSuccessListener(query -> {
                   userList.clear();

                   for (DocumentSnapshot doc : query) {
                       User user = doc.toObject(User.class);
                       // If user doesn't exist or is the admin, go to next user
                       if (user == null) continue;
                       if (deviceId.equals(user.getId())) continue;

                       userList.add(user);

                   }

                    UserAdapter userAdapter = new UserAdapter(AdminPanel.this, userList);
                    userListFragment.setAdapter(userAdapter);
                    userAdapter.notifyDataSetChanged();
                });
    }
}