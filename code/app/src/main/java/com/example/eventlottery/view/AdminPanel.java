package com.example.eventlottery.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.appcompat.widget.SearchView;

import com.example.eventlottery.R;
import com.example.eventlottery.events.DBConnector;
import com.example.eventlottery.events.Event;
import com.example.eventlottery.model.EventDatabase;
import com.example.eventlottery.users.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageKt;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AdminPanel extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {
    /** Tag for logging debug information. */
    private static final String TAG = "AdminPanel";
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
    /** Holds the most recently selected event's index */
    private int selectedEventIndex;
    /** The selected user pressed on */
    private User selectedUser;
    /** Adapter for user object */
    private UserAdapter userAdapter;
    /** Button for going back */
    private Button backButton;
    /** SearchView for filtering events */
    private SearchView eventSearchBar;
    /** SearchView for filtering users */
    private SearchView userSearchBar;
    /** Holds ALL events for filtering */
    private ArrayList<Event> allEvents;
    /** Holds ALL users for filtering */
    private ArrayList<User> allUsers;
    /** Firestore database connector for performing user operations. */
    private EventDatabase eventDatabase;




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
        backButton = findViewById(R.id.backButton);
        eventListData = new ArrayList<Event>();
        userList = new ArrayList<User>();
        allEvents = new ArrayList<>();
        allUsers = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
        userAdapter = new UserAdapter(AdminPanel.this, userList);
        userListFragment.setAdapter(userAdapter);
        eventSearchBar = findViewById(R.id.eventSearchBar);
        userSearchBar = findViewById(R.id.userSearchBar);

        loadProfilesFromFirestore();

        // Set up AdminEventViewLauncher for deleting an event
        ActivityResultLauncher<Intent> EventViewResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            // Here, no request code
                            Intent data = result.getData();
                            if (data != null && data.getBooleanExtra("Remove Event", false)) {
                                deleteEventFromFirebase();
                            }
                        }
                    }
                });

        // Set click listener for clicking an event
        eventlistFragment.setOnItemClickListener((parent, view, position, id) -> {
            // References: https://stackoverflow.com/questions/61455381/how-to-replace-startactivityforresult-with-activity-result-apis
            Intent intent = new Intent(this, AdminEventView.class);
            selectedEventIndex = position;

            // Get event & serialize
            Event event = eventListData.get(position);
            intent.putExtra("id", event.getId());
            intent.putExtra("name", event.getName());
            intent.putExtra("description", event.getDescription());
            intent.putExtra("dateStart", event.getFormattedStartDate());
            intent.putExtra("timeStart", event.getFormattedStartTime());
            intent.putExtra("dateEnd", event.getFormattedEndDate());
            intent.putExtra("timeEnd", event.getFormattedEndTime());
            intent.putExtra("location", event.getLocation());
            intent.putExtra("organizer", event.getOrganizer());
            intent.putExtra("image", event.getImage());
            intent.putExtra("storagePath", event.getStoragePath());
            EventViewResultLauncher.launch(intent);
        });

        // Set click listener for going back
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button viewNotificationLogsButton = findViewById(R.id.viewNotificationLogsButton);
        viewNotificationLogsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminPanel.this, AdminNotificationLogsActivity.class);
                startActivity(intent);
            }
        });

        // Delete Selected User
        userListFragment.setOnItemClickListener((parent, v, p, id) -> {
            selectedUser = (User) parent.getItemAtPosition(p);
            PopupMenu popupMenu = new PopupMenu(AdminPanel.this, v);
            popupMenu.setOnMenuItemClickListener(this);
            popupMenu.inflate(R.menu.menu);
            popupMenu.show();
        });

        // Configure SearchView filtering for events
        eventSearchBar.clearFocus();
        eventSearchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return false; }
            @Override public boolean onQueryTextChange(String newText) {
                filterEventList(newText);
                return true;
            }
        });
        // Configure SearchView filtering for users
        userSearchBar.clearFocus();
        userSearchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return false; }
            @Override public boolean onQueryTextChange(String newText) {
                filterUserList(newText);
                return true;
            }
        });
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
                    loadEventsFromFirestore();
                } else {
                    Log.d("OrganizerPanel", "No organizer found");
                }
            } else {
                Log.e("OrganizerPanel", "Error loading organizer info", task.getException());
            }
        });
    }

    /**
     * Adds every event from firestore into eventListData. Loads again if any event was changed
     */
    private void loadEventsFromFirestore() {
        db.collection("event-p4")
                .orderBy("startTime")
                .addSnapshotListener((query, error) -> {
                    if (error != null || query == null) {
                        return;
                    }

                    // Clear both the master list and the displayed list
                    allEvents.clear();
                    eventListData.clear();

                    for (DocumentSnapshot doc : query) {
                        Event event = doc.toObject(Event.class);
                        if (event == null) continue;

                        if (event.getId() == null || event.getId().isEmpty()) {
                            event.setId(doc.getId());
                        }

                        allEvents.add(event);
                        eventListData.add(event);
                    }

                    if (eventAdapter != null) {
                        // Show all events by default
                        eventAdapter.setFilteredList(new ArrayList<>(allEvents));
                    }
                });
    }


    /**
     * Adds every profile into userList.
     */
    private void loadProfilesFromFirestore() {
        db.collection("users-p4")
                .addSnapshotListener((query, error) -> {
                    if (error != null || query == null) {
                        return;
                    }

                    allUsers.clear();
                    userList.clear();

                    for (DocumentSnapshot doc : query) {
                        User user = doc.toObject(User.class);
                        if (user == null) continue;

                        List<String> waitlistedEvents = (List<String>) doc.get("waitlistedEvents");
                        user.setWaitlistedEventIds(waitlistedEvents);

                        allUsers.add(user);
                        userList.add(user);
                    }

                    if (userAdapter != null) {
                        userAdapter.setFilteredList(new ArrayList<>(allUsers));
                    }
                });
    }


    /**
     * Helper function, deletes the event from firebase.
     */
    private void deleteEventFromFirebase() {
        // Get event & event id for the event to be deleted
        Event selectedEvent = eventListData.get(selectedEventIndex);
        String eventId = selectedEvent.getId();

        // 1. Delete eventId from all users locally & in firebase
        for (User user : userList) {
            // Remove eventId from user's CreatedEvents() array (if they made it)
            ArrayList<String> createdEvents = user.getCreatedEvents();
            if (createdEvents != null && createdEvents.contains(eventId)) {
                db.collection("users-p4")
                        .document(user.getId())
                        .update("createdEvents", FieldValue.arrayRemove(eventId));
            }

            // Remove eventId from user's Waitlist (if they joined it)
            ArrayList<String> waitlistedEvents = user.getWaitlistedEvents();
            if (waitlistedEvents != null && waitlistedEvents.contains(eventId)) {
                db.collection("users-p4")
                        .document(user.getId())
                        .update("waitlistedEvents", FieldValue.arrayRemove(eventId));
            }

            // Remove eventId from user's registeredEvents (if they registered)
            HashMap<String, String> registeredEvents = user.getRegisteredEvents();
            if (registeredEvents != null && registeredEvents.containsKey(eventId)) {
                db.collection("users-p4")
                        .document(user.getId())
                        .update("registeredEvents." + eventId, FieldValue.delete());
            }
        }

        // 2. Delete the image uploaded to firebase
        // Get storage path & set up storage reference
        String storagePath = selectedEvent.getStoragePath();
        FirebaseStorage storage = StorageKt.getStorage(Firebase.INSTANCE);
        StorageReference storageReference = storage.getReference();


        if (storagePath != null && !storagePath.isEmpty()) {
            StorageReference imageReference = storageReference.child(storagePath);
            imageReference.delete();
        }

        // 3. Delete event from "event-p4" in firebase
        db.collection("event-p4").document(eventId).delete();
    }

    /**
     * Deletes the selected User
     * @param user: the user to be deleted
     */
    private void deleteSelectedUser(User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(AdminPanel.this);
        builder.setTitle("This will delete " + user.getName() + "'s account?")
                .setMessage("Are You Sure?")
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", (dialogInterface, i) -> {
                    userDatabase.deleteUserCreatedEvents(selectedUser, selectedUser.getCreatedEvents());
                    userDatabase.deleteUserAcc(user.getId(), AdminPanel.this::deleteUser);
                })
                .show();
    }

    /**
     * Callback method called after Firestore completes a delete user request
     * @param task: firestore get data request
     */
    private void deleteUser(Task<Void> task) {
        if (task.isSuccessful()) {
            Toast.makeText(AdminPanel.this, "Deleted Account", Toast.LENGTH_SHORT).show();
            eventDatabase.deleteUserFromEventLists(selectedUser);
        } else {
            Log.d(TAG, "Failed deleting user");
        }
    }

    /**
     * Updates the selected {@link User}'s organizer permissions.
     * @param user The user who will have their organizer permission changed.
     */
    private void updateOrganizerPerms(User user) {
        boolean creationBan = user.isCreationBan();
        String dialogTitle;
        String name = user.getName();

        if (creationBan) {
            dialogTitle = "This will give " + name + " organizer permissions.";
        } else {
            dialogTitle = "This will revoke " + name + "'s organizer permissions.";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(dialogTitle)
                .setMessage("Are you sure?")
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", (dialogInterface, i) -> {
                    userDatabase.updateOrganizerPerms(user.getId(), !creationBan, AdminPanel.this::updatePermsListener);
                })
                .show();
    }

    /**
     * Callback method which is called when Firestore completes updating the user's permissions.
     * @param task The listener given by a completed Firestore request.
     */
    private void updatePermsListener(Task<Void> task) {
        if (task.isSuccessful()) {
            Toast.makeText(this, "Organizer permissions successfully updated.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to update organizer permissions.", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Failed to update organizer permissions.");
        }
    }

    /**
     * Option menu for selecting a user:
     * - Deleting the User
     * - Organizer permission change
     * @param menuItem: the option the user selected
     */
    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.delete_user) {
            deleteSelectedUser(selectedUser);
            return true;
        } else if (menuItem.getItemId() == R.id.organizer_perms) {
            updateOrganizerPerms(selectedUser);
            return true;
        }
        return false;
    }

    /**
     * Filters the event list by event name and updates the adapter.
     */
    private void filterEventList(String text) {
        String q = (text == null) ? "" : text.trim().toLowerCase();

        // If search is empty, show all events again
        if (q.isEmpty()) {
            if (eventAdapter != null) {
                eventAdapter.setFilteredList(new ArrayList<>(allEvents));
            }
            return;
        }

        ArrayList<Event> filteredList = new ArrayList<>();
        for (Event item : allEvents) {
            if (item.getName() != null &&
                    item.getName().toLowerCase().contains(q)) {
                filteredList.add(item);
            }
        }

        if (eventAdapter != null) {
            if (filteredList.isEmpty()) {
                Toast.makeText(this, "No events found.", Toast.LENGTH_SHORT).show();
                eventAdapter.setFilteredList(new ArrayList<>());
            } else {
                eventAdapter.setFilteredList(filteredList);
            }
        }
    }


    /**
     * Filters the user list by user name (and/or ID) and updates the adapter.
     */
    private void filterUserList(String text) {
        String q = (text == null) ? "" : text.trim().toLowerCase();

        // If search is empty, show all users again
        if (q.isEmpty()) {
            if (userAdapter != null) {
                userAdapter.setFilteredList(new ArrayList<>(allUsers));
            }
            return;
        }

        ArrayList<User> filteredList = new ArrayList<>();
        for (User user : allUsers) {
            boolean matchesName = user.getName() != null &&
                    user.getName().toLowerCase().contains(q);
            boolean matchesId = user.getId() != null &&
                    user.getId().toLowerCase().contains(q);

            if (matchesName || matchesId) {
                filteredList.add(user);
            }
        }

        if (userAdapter != null) {
            if (filteredList.isEmpty()) {
                Toast.makeText(this, "No users found.", Toast.LENGTH_SHORT).show();
                userAdapter.setFilteredList(new ArrayList<>());
            } else {
                userAdapter.setFilteredList(filteredList);
            }
        }
    }

}