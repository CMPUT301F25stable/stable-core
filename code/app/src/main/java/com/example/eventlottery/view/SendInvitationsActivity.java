//package com.example.eventlottery.view;
//
//import android.os.Bundle;
//import android.widget.Button;
//import android.widget.CheckBox;
//import android.widget.EditText;
//import android.widget.ListView;
//import android.widget.TextView;
//import android.widget.Toast;
//import androidx.appcompat.app.AlertDialog;
//import androidx.appcompat.app.AppCompatActivity;
//import com.example.eventlottery.R;
//import com.example.eventlottery.events.NotificationSystem;
//import com.example.eventlottery.services.OrganizerService;
//import com.example.eventlottery.users.User;
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * US 02.05.01: Activity for organizers to send invitations to chosen entrants
// */
//public class SendInvitationsActivity extends AppCompatActivity {
//
//    private NotificationSystem notificationSystem;
//    private OrganizerService organizerService;
//
//    private String eventId;
//    private String eventName;
//    private List<User> availableEntrants;
//    private List<User> selectedEntrants;
//
//    private ListView entrantsListView;
//    private EditText customMessageEditText;
//    private Button sendInvitationsButton;
//    private Button selectAllButton;
//    private TextView selectedCountText;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_send_invitations);
//
//        // Get event details from intent
//        eventId = getIntent().getStringExtra("event_id");
//        eventName = getIntent().getStringExtra("event_name");
//
//        // Initialize services
//        notificationSystem = new NotificationSystem(this);
//        organizerService = new OrganizerService(notificationSystem);
//
//        // Initialize lists
//        selectedEntrants = new ArrayList<>();
//
//        // Initialize views
//        initializeViews();
//
//        // Load available entrants
//        loadAvailableEntrants();
//
//        // Set up button listeners
//        setupButtonListeners();
//    }
//
//    private void initializeViews() {
//        entrantsListView = findViewById(R.id.entrants_list_view);
//        customMessageEditText = findViewById(R.id.custom_message_edit_text);
//        sendInvitationsButton = findViewById(R.id.send_invitations_button);
//        selectAllButton = findViewById(R.id.select_all_button);
//        selectedCountText = findViewById(R.id.selected_count_text);
//
//        // Set event name in title
//        TextView eventNameText = findViewById(R.id.event_name_text);
//        eventNameText.setText("Send Invitations for: " + eventName);
//    }
//
//    private void setupButtonListeners() {
//        // Send invitations button
//        sendInvitationsButton.setOnClickListener(v -> sendInvitations());
//
//        // Select all button
//        selectAllButton.setOnClickListener(v -> toggleSelectAll());
//    }
//
//    /**
//     * US 02.05.01: Load list of available entrants who can be invited
//     */
//    private void loadAvailableEntrants() {
//        // TODO: Replace with actual database query
//        // This should fetch users from your database/Firestore
//        availableEntrants = createSampleEntrants();
//
//        // Set up list adapter
//        // TODO: Create custom adapter to show checkboxes for selection
//        updateSelectedCount();
//    }
//
//    /**
//     * US 02.05.01: Send invitations to selected entrants
//     */
//    private void sendInvitations() {
//        if (selectedEntrants.isEmpty()) {
//            Toast.makeText(this, "Please select at least one entrant", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // Get custom message
//        String customMessage = customMessageEditText.getText().toString().trim();
//
//        // Show confirmation dialog
//        new AlertDialog.Builder(this)
//                .setTitle("Send Invitations")
//                .setMessage("Send invitations to " + selectedEntrants.size() + " entrant(s)?")
//                .setPositiveButton("Send", (dialog, which) -> {
//                    // Send invitations
//                    int sentCount = organizerService.sendInvitationsToEntrants(
//                            selectedEntrants,
//                            eventName,
//                            eventId,
//                            customMessage.isEmpty() ? null : customMessage
//                    );
//
//                    // Show success message
//                    Toast.makeText(this,
//                            "✓ Invitations sent to " + sentCount + " entrant(s)!",
//                            Toast.LENGTH_LONG).show();
//
//                    // Clear selection
//                    selectedEntrants.clear();
//                    customMessageEditText.setText("");
//                    updateSelectedCount();
//                })
//                .setNegativeButton("Cancel", null)
//                .show();
//    }
//
//    /**
//     * Toggle select all entrants
//     */
//    private void toggleSelectAll() {
//        if (selectedEntrants.size() == availableEntrants.size()) {
//            // Deselect all
//            selectedEntrants.clear();
//            selectAllButton.setText("Select All");
//        } else {
//            // Select all
//            selectedEntrants.clear();
//            selectedEntrants.addAll(availableEntrants);
//            selectAllButton.setText("Deselect All");
//        }
//        updateSelectedCount();
//    }
//
//    /**
//     * Update the count of selected entrants
//     */
//    private void updateSelectedCount() {
//        selectedCountText.setText("Selected: " + selectedEntrants.size() + " / " + availableEntrants.size());
//        sendInvitationsButton.setEnabled(!selectedEntrants.isEmpty());
//    }
//
//    /**
//     * Handle entrant selection
//     */
//    public void onEntrantSelected(User entrant, boolean isSelected) {
//        if (isSelected) {
//            if (!selectedEntrants.contains(entrant)) {
//                selectedEntrants.add(entrant);
//            }
//        } else {
//            selectedEntrants.remove(entrant);
//        }
//        updateSelectedCount();
//    }
//
//    /**
//     * Create sample entrants for testing
//     * TODO: Replace with actual database query
//     */
//    private List<User> createSampleEntrants() {
//        List<User> entrants = new ArrayList<>();
//
//        User user1 = new User("user1", "Alice Johnson", "alice@example.com");
//        user1.getEntrant().setNotificationsEnabled(true);
//        entrants.add(user1);
//
//        User user2 = new User("user2", "Bob Smith", "bob@example.com");
//        user2.getEntrant().setNotificationsEnabled(true);
//        entrants.add(user2);
//
//        User user3 = new User("user3", "Carol Williams", "carol@example.com");
//        user3.getEntrant().setNotificationsEnabled(true);
//        entrants.add(user3);
//
//        User user4 = new User("user4", "David Brown", "david@example.com");
//        user4.getEntrant().setNotificationsEnabled(false); // Notifications disabled
//        entrants.add(user4);
//
//        User user5 = new User("user5", "Emma Davis", "emma@example.com");
//        user5.getEntrant().setNotificationsEnabled(true);
//        entrants.add(user5);
//
//        return entrants;
//    }
//}