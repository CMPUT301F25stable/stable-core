package com.example.eventlottery.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eventlottery.R;
import com.example.eventlottery.events.DBConnector;
import com.example.eventlottery.events.Event;
import com.example.eventlottery.events.FinalizedList;
import com.example.eventlottery.events.NotificationSystem;
import com.example.eventlottery.model.EventDatabase;
import com.example.eventlottery.model.QRCode;
import com.example.eventlottery.users.User;
import com.google.firebase.firestore.DocumentSnapshot;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    /** Tag for logging debug information. */
    private static final String TAG = "OrganizerPanel";

    /** The unique Android device ID for the current organizer. */
    private String userID;

    /** The eventlist fragment. */
    private EventlistFragment eventList;

    /** The button used to view the waitlist of the selected event. */
    private Button viewWaitlist;

    /** The button used to edit the currently selected event. */
    private Button editEvent;

    /** The button used to create a new event. */
    private Button createEvent;

    /** The button used to view the final list. */
    private Button viewFinalList;

    /** The button used to download an event's QR code. */
    private Button downloadQRCode;

    /** The button used to view where entrants have joined */
    private Button map;

    /** The index of the currently selected event in the list (default is -1). */
    private int selectedEventIndex = -1;

    /** The {@link Event} currently selected by the organizer. */
    private Event selectedEvent;

    private QRCode selectedEventQR;

    /** The adapter used to bind event data to the {@link ListView}. */
    private EventAdapter adapter;

    /** The {@link EventDatabase} managing event-related Firestore operations. */
    private EventDatabase organizerEventDatabase;

    /** The {@link DBConnector} used to load and update organizer information from Firestore. */
    private DBConnector userDatabase;

    /** The {@link User} object representing the logged-in organizer. */
    private User organizer;

    /** The list of events owned or created by the organizer. */
    private ArrayList<Event> data = new ArrayList<>();

    /** The button used to view where entrants have joined */
    private Button chosenEntrantsBtn;

    private Button cancelledEntrantsBtn;

    private Button redrawButton;




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
        chosenEntrantsBtn = findViewById(R.id.viewChosenEntrantsButton);
        eventList = (EventlistFragment) getSupportFragmentManager().findFragmentById(R.id.eventListFragment);
        viewWaitlist = findViewById(R.id.viewWaitlistButton);
        editEvent = findViewById(R.id.editEventButton);
        createEvent = findViewById(R.id.createEventButton);
        viewFinalList = findViewById(R.id.view_finalized_list_button);
        downloadQRCode = findViewById(R.id.downloadQRCode);
        map = findViewById(R.id.mapButton);
        chosenEntrantsBtn = findViewById(R.id.viewChosenEntrantsButton);
        cancelledEntrantsBtn = findViewById(R.id.viewCancelledEntrantsButton);
        redrawButton = findViewById(R.id.redrawEventButton);
        setClickListeners();


        // Hide buttons until an event is selected
        chosenEntrantsBtn.setVisibility(View.GONE);
        cancelledEntrantsBtn.setVisibility(View.GONE);
        downloadQRCode.setVisibility(View.GONE);


        // Initialize databases and organizer info
        userID = Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
        organizerEventDatabase = new EventDatabase();
        userDatabase = new DBConnector(this);
        getOrganizerInfo();

        // Handle back button press using OnBackPressedDispatcher
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Check if we're showing the event info fragment
                if (getSupportFragmentManager().findFragmentById(R.id.eventListFragment) instanceof OrganizerEventInfoFragment) {
                    showEventList();
                }
                else {

                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        // USED FOR CLEARING NOTIFICAITONS IN FIREBASE, COMMENT OUT EVERYTHING HERE IF U NEED TO USE THIS
        //NotificationSystem tempNotificationSystem = new NotificationSystem(this, userID, "Organizer");
        //tempNotificationSystem.clearAllNotifications();
        //tempNotificationSystem.shutdown();


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
                    organizer = document.toObject(User.class);
                    Log.d(TAG, "Organizer loaded");

                    // Load events after organizer data is retrieved
                    adapter = new EventAdapter(OrganizerPanel.this, data);
                    organizerEventDatabase.organizerGetEvents(organizer, data, adapter);
                    eventList.setAdapter(adapter);

                    // Replace taskbar with admin taskbar if user is an admin
                    TaskbarFragment taskbar = TaskbarFragment.newInstance(organizer);
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragmentContainerView, taskbar)
                            .commitAllowingStateLoss();
                } else {
                    Log.d(TAG, "No organizer found");
                }
            } else {
                Log.e(TAG, "Error loading organizer info", task.getException());
            }
        });
    }

    /**
     * Sets up all click listeners for buttons and list items in the organizer panel.
     * This includes viewing waitlists, editing events, and creating new events.
     */
    private void setClickListeners() {
        /**
         * Updates when the user selects a new event from the list.
         *
         * @param parent The AdapterView where the click occurred.
         * @param view The view representing the clicked item.
         * @param position The position of the clicked item.
         * @param id The row ID of the clicked item.
         */
        eventList.setOnItemClickListener((parent, view, position, id) -> {
            selectedEventIndex = position;
            selectedEvent = data.get(position);

            int waitlistCount = selectedEvent.getWaitlist().getWaitlistedUsers().size();
            int selectedCount = selectedEvent.getSelectedIds().size();
            int cancelledCount = selectedEvent.getCancelledEntrants().size();
            int acceptedCount = selectedEvent.getFinalizedList() != null &&
                    selectedEvent.getFinalizedList().getFinalizedUsers() != null ?
                    selectedEvent.getFinalizedList().getFinalizedUsers().size() : 0;
            chosenEntrantsBtn.setVisibility(View.VISIBLE);
            cancelledEntrantsBtn.setVisibility(View.VISIBLE);
            downloadQRCode.setVisibility(View.VISIBLE);

            // Show the fragment container (which dims the background)
            //findViewById(R.id.fragment_container).setVisibility(View.VISIBLE);

            // Create and show the fragment in the inner content area
            OrganizerEventInfoFragment fragment = OrganizerEventInfoFragment.newInstance(
                    selectedEvent.getId(),
                    selectedEvent.getName(),
                    waitlistCount,
                    selectedCount,
                    cancelledCount,
                    acceptedCount,
                    organizer.getId(),
                    organizer.getName()
            );

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.eventListFragment, fragment)
                    .addToBackStack(null)
                    .commit();
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
                if (selectedEventIndex != -1) {
                    selectedEvent = data.get(selectedEventIndex);
                    ArrayList<User> users = selectedEvent.getWaitlist().getWaitlistedUsers();
                    WaitlistDialog waitlistDialog = WaitlistDialog.newInstance(users);
                    waitlistDialog.show(getSupportFragmentManager(), "WaitlistDialog");
                } else {
                    Toast.makeText(OrganizerPanel.this, "Please click on an event first", Toast.LENGTH_SHORT).show();
                }
            }
        });

        map.setOnClickListener(new View.OnClickListener() {
            /**
             * Handles the click listener for the maps button.
             * Starts the maps activity given that the event has geolocation enabled.
             * Serializes latitudes & longitudes, and the user's name.
             * @param v The view that was clicked.
             */
            @Override
            public void onClick(View v) {
                if (selectedEventIndex == -1) {
                    Toast.makeText(OrganizerPanel.this, "Please click on an event first", Toast.LENGTH_SHORT).show();
                    return;
                }

                selectedEvent = data.get(selectedEventIndex);
                boolean geolocationOn = selectedEvent.getGeolocation();
                if (!geolocationOn) {
                    Toast.makeText(OrganizerPanel.this, "This event doesn't have geolocation on", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Grab userLocations & user objects (so I can extract their name)
                ArrayList<Map<String, Object>> userLocations = selectedEvent.getUserLocations();
                ArrayList<User> waitlistedUsers = selectedEvent.getWaitlist().getWaitlistedUsers();

                // Turn from Map<String, Object> to ArrayList since maps aren't serializable
                ArrayList<Double> latitudes = new ArrayList<>();
                ArrayList<Double> longitudes = new ArrayList<>();
                ArrayList<String> names = new ArrayList<>();

                for (Map<String, Object> userLocation : userLocations) {
                    // grab user Id & initialize name String
                    String userId = (String) userLocation.get("userId");
                    String name = "Your Name";  // Filler name

                    // Match userId to name
                    for (User user : waitlistedUsers) {
                        if (user.getId().equals(userId)) {
                            name = user.getName();
                        }
                    }

                    // Add to the user's name, latitude, and longitude to arraylist (they're parallel)
                    names.add(name);
                    latitudes.add((Double) userLocation.get("latitude"));
                    longitudes.add((Double) userLocation.get("longitude"));
                }

                // Make intent & serialize coordinates
                Intent intent = new Intent(OrganizerPanel.this, MapActivity.class);
                intent.putExtra("latitudes", latitudes);
                intent.putExtra("longitudes", longitudes);
                intent.putExtra("names", names);
                startActivity(intent);
            }
        });

        editEvent.setOnClickListener(v -> {
            if (selectedEventIndex != -1) {
                selectedEvent = data.get(selectedEventIndex);
                // checks if the selected event is an edit event
                EditEventDialog dialog = EditEventDialog.newInstance(
                        selectedEvent,
                        true,
                        organizer.getId(),
                        organizer.getName());

                // Updates event in Firestore and refreshes adapter when an edit is made
                dialog.setOnEventUpdatedListener(updatedEvent -> {
                    organizerEventDatabase.organizerUpdateEvent(updatedEvent);
                    adapter.notifyDataSetChanged();
                });

                dialog.show(getSupportFragmentManager(), "EditEventDialog");
            } else {
                Toast.makeText(this, "Please click on an event first", Toast.LENGTH_SHORT).show();
            }
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
                createDialog.setOrganizerName(organizer.getName());

                createDialog.setOnEventCreatedListener(event -> {
                    organizer.createEvent(event.getId());
                    organizerEventDatabase.insert(data, adapter, event);
                    userDatabase.updateOrganizerCreatedEvents(organizer);
                });

                createDialog.show(getSupportFragmentManager(), "CreateEventDialog");
            }
        });

        viewFinalList.setOnClickListener(v -> {
            if (selectedEventIndex != -1) {
                selectedEvent = data.get(selectedEventIndex);
                FinalizedList finalizedList = selectedEvent.getFinalizedList();
                ArrayList<User> users;

                // finalizedList is initialized as null this checks for that
                if (finalizedList != null && finalizedList.getFinalizedUsers() != null) {
                    users = finalizedList.getFinalizedUsers();
                } else {
                    users = new ArrayList<>();
                }

                FinalListDialog finalListDialog = FinalListDialog.newInstance(users);
                finalListDialog.show(getSupportFragmentManager(), "FinalListDialog");
            } else {
                Toast.makeText(this, "Please click on an event first", Toast.LENGTH_SHORT).show();
            }
        });

        downloadQRCode.setOnClickListener(v -> {
            if (selectedEventIndex != -1) {
                selectedEvent = data.get(selectedEventIndex);
                String eventId = selectedEvent.getId();
                String eventName = selectedEvent.getName();
                boolean success = false;

                if (selectedEventQR == null) {
                    selectedEventQR = new QRCode(eventId);
                    selectedEventQR.generateQRCode();
                }

                if (!Objects.equals(selectedEventQR.getEventId(), eventId)) {
                    selectedEventQR = new QRCode(eventId);
                    success = selectedEventQR.generateQRCode();
                } else {
                    success = true;
                }

                // Reference: https://developer.android.com/training/data-storage/shared/documents-files#create-file
                if (success) {
                    Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/png");
                    intent.putExtra(Intent.EXTRA_TITLE, eventName.concat(" QR Code.png"));
                    downloadLauncher.launch(intent);
                } else {
                    Toast.makeText(this, "Failed to download the event QR Code. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        chosenEntrantsBtn.setOnClickListener(v -> {
            if (selectedEventIndex == -1) {
                Toast.makeText(this, "Please click an event first", Toast.LENGTH_SHORT).show();
                return;
            }

            Event event = data.get(selectedEventIndex);

            Intent intent = new Intent(OrganizerPanel.this, DisplayEntrantsActivity.class);
            intent.putExtra("eventId", event.getId());
            intent.putExtra("eventName", event.getName());
            intent.putExtra("type", "chosen");
            intent.putExtra("organizerId", organizer.getId());
            intent.putExtra("organizerName", organizer.getName());
            startActivity(intent);
        });

        cancelledEntrantsBtn.setOnClickListener(v -> {
            if (selectedEventIndex == -1) {
                Toast.makeText(this, "Please click an event first", Toast.LENGTH_SHORT).show();
                return;
            }

            Event event = data.get(selectedEventIndex);

            Intent intent = new Intent(OrganizerPanel.this, DisplayEntrantsActivity.class);
            intent.putExtra("eventId", event.getId());
            intent.putExtra("eventName", event.getName());
            intent.putExtra("type", "cancelled");
            intent.putExtra("organizerId", organizer.getId());
            intent.putExtra("organizerName", organizer.getName());
            startActivity(intent);
        });

        redrawButton.setOnClickListener(v -> {
            if (selectedEventIndex == -1) {
                Toast.makeText(this, "Please click on an event first", Toast.LENGTH_SHORT).show();
                return;
            }

            selectedEvent = data.get(selectedEventIndex);

            // Get current waitlist size
            int waitlistSize = selectedEvent.getWaitlist() != null &&
                    selectedEvent.getWaitlist().getWaitlistedUsers() != null ?
                    selectedEvent.getWaitlist().getWaitlistedUsers().size() : 0;

            if (waitlistSize == 0) {
                Toast.makeText(this, "No users on waitlist to draw from", Toast.LENGTH_SHORT).show();
                return;
            }

            // Show redraw dialog with organizer information for notifications
            RedrawEventDialog dialog = RedrawEventDialog.newInstance(
                    selectedEvent.getId(),
                    selectedEvent.getName(),
                    waitlistSize,
                    organizer.getId(),
                    organizer.getName()
            );

            // Refresh event list after redraw completes
            dialog.setOnRedrawCompleteListener(drawnCount -> {
                // Reload the event data to show updated counts
                organizerEventDatabase.organizerGetEvents(organizer, data, adapter);
                adapter.notifyDataSetChanged();
            });

            dialog.show(getSupportFragmentManager(), "RedrawEventDialog");
        });
    }

    /**
     * Helper method to return to the event list view
     */
    private void showEventList() {
        // Show the search bar and button panel again
        findViewById(R.id.searchBar).setVisibility(View.VISIBLE);
        findViewById(R.id.linearLayout5).setVisibility(View.VISIBLE);

        // Pop the back stack to return to the event list
        getSupportFragmentManager().popBackStack();
    }

    /* Reference: https://developer.android.com/training/data-storage/shared/documents-files#perform-operations */
    ActivityResultLauncher<Intent> downloadLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            if (data != null) {
                if (data.getData() != null) {
                    downloadQRCode(data.getData());
                }
            }
        }
    });

    /* Reference: https://stackoverflow.com/a/3013625 */
    private void downloadQRCode(Uri uri) {
        Bitmap qrCode = selectedEventQR.getQrCode();
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            try {
                OutputStream outputStream = getContentResolver().openOutputStream(uri);

                if (outputStream != null) {
                    qrCode.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                    outputStream.close();
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to download QR Code.");

                runOnUiThread(() -> {
                    Toast.makeText(this, "Failed to download the event QR Code. Please try again.", Toast.LENGTH_SHORT).show();
                });
            }
        });
        executor.close();
    }
}