package com.example.eventlottery.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.eventlottery.R;
import com.example.eventlottery.events.Event;
import com.example.eventlottery.users.User;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

/** USER STORY - 01.02.03
 * <p>
 * The {@code InfoActivity} class displays detailed information about an event invitation.
 * Users can view event details and choose to either accept or decline the invitation.
 * Updates are reflected in the main user data.
 * @author Jensen Lee
 */
public class InfoActivity extends AppCompatActivity {

    /** Header TextView displaying the name of the event. */
    private TextView eventNameHeader;

    /** TextView showing the detailed description of the event. */
    private TextView eventDescriptionText;

    /** TextView indicating the location where the event will take place. */
    private TextView eventLocationText;

    /** TextView displaying the organizer or host of the event. */
    private TextView eventOrganizerText;

    /** TextView showing the formatted start and/or end date and time of the event. */
    private TextView eventDateTimeText;

    /** Badge TextView displaying the user's current registration status for the event
     *  (e.g., "Accepted", "Declined", "Notified"). */
    private TextView statusBadge;

    /** Button that allows the user to accept the event invitation. */
    private Button acceptButton;

    /** Button that allows the user to decline the event invitation. */
    private Button declineButton;

    /** ImageButton that navigates back to the previous screen or activity. */
    private ImageButton backButton;

    /** The currently logged-in user interacting with the event details. */
    private User currentUser;

    /** The event object whose details are being displayed in this activity. */
    private Event currentEvent;

    /** String representing the current registration status of the user for this event. */
    private String currentStatus;

    private FirebaseFirestore db;
  
    /**
     * Called when the activity is created.
     * Initializes UI elements, retrieves intent data, constructs User and Event objects,
     * and sets up event button listeners.
     *
     * @param savedInstanceState The saved instance state bundle.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Bind UI elements
        eventNameHeader = findViewById(R.id.eventNameHeader);
        eventDescriptionText = findViewById(R.id.eventDescriptionText);
        eventLocationText = findViewById(R.id.eventLocationText);
        eventOrganizerText = findViewById(R.id.eventOrganizerText);
        eventDateTimeText = findViewById(R.id.eventDateTimeText);
        statusBadge = findViewById(R.id.statusBadge);
        acceptButton = findViewById(R.id.acceptButton);
        declineButton = findViewById(R.id.declineButton);
        backButton = findViewById(R.id.backButton);

        // Get user ID from intent
        Intent intent = getIntent();
        String userId = intent.getStringExtra("USER_ID");

        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Invalid user ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        //cleanupTestData(); // Clean up data function

        // Retrieve user from Firestore FIRST
        db.collection("users-p4").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentUser = documentSnapshot.toObject(User.class);

                        if (currentUser == null) {
                            Toast.makeText(this, "Failed to parse user data", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }

                        // Defensive: make sure registeredEvents and waitlistedEvents are non-null
                        if (currentUser.getRegisteredEvents() == null) {
                            currentUser.setRegisteredEvents(new HashMap<>());
                        }
                        if (currentUser.getWaitlistedEvents() == null) {
                            currentUser.setWaitlistedEventIds(new ArrayList<>());
                        }

                        // NOW load event data after user is loaded
                        loadEventData(intent);
                        //setupTestData(); // TESTING DATA
                    }
                    else {
                        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to retrieve user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });

        // Back button
        backButton.setOnClickListener(v -> finish());

        // Accept button
        acceptButton.setOnClickListener(v -> showConfirmationDialog(true));

        // Decline button
        declineButton.setOnClickListener(v -> showConfirmationDialog(false));
    }

    /**
     * Shows a confirmation dialog for accepting or declining an event invitation.
     *
     * @param isAccepting {@code true} if the user is accepting the invitation,
     *                    {@code false} if declining.
     */
    private void showConfirmationDialog(boolean isAccepting) {
        String title = isAccepting ? "Accept Invitation?" : "Decline Invitation?";
        String message = isAccepting
                ? "Are you sure you want to accept this event invitation?"
                : "Are you sure you want to decline this event invitation?";
        String positiveText = isAccepting ? "Accept" : "Decline";

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);

        builder.setPositiveButton(positiveText, (dialog, which) -> {
            if (isAccepting) {
                handleAccept();
            } else {
                handleDecline();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        // Customize button colors
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                isAccepting ? getColor(android.R.color.holo_green_dark) : getColor(android.R.color.holo_red_dark)
        );
    }

    /**
     * Handles the logic for when a user accepts an event invitation.
     * Updates user status, displays confirmation, and disables further interaction.
     */
    private void handleAccept() {
        if (currentEvent == null || currentUser == null) {
            Toast.makeText(this, "Event data not loaded yet", Toast.LENGTH_SHORT).show();
            return;
        }

        final String eventId = currentEvent.getId();

        // Disable buttons immediately
        acceptButton.setEnabled(false);
        declineButton.setEnabled(false);
        acceptButton.setAlpha(0.5f);
        declineButton.setAlpha(0.5f);

        // Single-field Firestore update
        Map<String, Object> updates = new HashMap<>();
        updates.put("registeredEvents." + eventId, "Accepted");

        db.collection("users-p4").document(currentUser.getId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    // Update local model safely
                    currentUser.getRegisteredEvents().put(eventId, "Accepted");
                    currentStatus = "Accepted";
                    updateStatusDisplay();

                    Toast.makeText(this, "✅ You accepted the invitation!", Toast.LENGTH_SHORT).show();

                    // Tell the caller something changed and finish
                    Intent out = new Intent(this, UserPanel.class);
                    out.putExtra("USER_ID", currentUser.getId());
                    out.putExtra("UPDATED_EVENT_ID", eventId);
                    out.putExtra("UPDATED_STATUS", "Accepted");
                    setResult(RESULT_OK, out);
                    startActivity(out);
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Re-enable buttons on failure
                    acceptButton.setEnabled(true);
                    declineButton.setEnabled(true);
                    acceptButton.setAlpha(1.0f);
                    declineButton.setAlpha(1.0f);

                    Toast.makeText(this, "Failed to save: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Handles the logic for when a user declines an event invitation.
     * Implements User Story 01.05.01: Replace declined user with new one from waitlist.
     *
     * Winners are defined as users with "Notified" or "Accepted" status.
     * When a winner declines, they are replaced by someone from the waitlist.
     */
    private void handleDecline() {
        if (currentEvent == null || currentUser == null) {
            Toast.makeText(this, "Event data not loaded yet", Toast.LENGTH_SHORT).show();
            return;
        }

        final String eventId = currentEvent.getId();
        final String userId = currentUser.getId();

        // Disable buttons immediately
        acceptButton.setEnabled(false);
        declineButton.setEnabled(false);
        acceptButton.setAlpha(0.5f);
        declineButton.setAlpha(0.5f);

        // Check if this user was a winner BEFORE declining
        String currentUserStatus = currentUser.getRegisteredEvents().get(eventId);
        boolean wasWinner = "Notified".equals(currentUserStatus) || "Accepted".equals(currentUserStatus);

        android.util.Log.d("InfoActivity", "User declining with status: " + currentUserStatus);
        android.util.Log.d("InfoActivity", "Was user a winner? " + wasWinner);

        // Update the declining user's status in Firestore
        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("registeredEvents." + eventId, "Declined");
      
        db.collection("users-p4").document(currentUser.getId())
                .update(userUpdates)
                .addOnSuccessListener(aVoid -> {
                    // Local update
                    currentUser.getRegisteredEvents().put(eventId, "Declined");
                    currentStatus = "Declined";
                    updateStatusDisplay();

                    // If user was NOT a winner, no need to do lottery replacement
                    if (!wasWinner) {
                        android.util.Log.d("InfoActivity", "User was not a winner, no replacement needed");
                        Toast.makeText(this, "❌ You declined the invitation.", Toast.LENGTH_SHORT).show();
                        finishActivityWithResult(eventId, "Declined");
                        return;
                    }

                    // User WAS a winner - need to find replacement from waitlist
                    android.util.Log.d("InfoActivity", "User was a winner, searching for replacement...");

                    // Step 2: Retrieve event document to access lottery system data
                    db.collection("event").document(eventId)
                            .get()
                            .addOnSuccessListener(eventDoc -> {
                                if (!eventDoc.exists()) {
                                    Toast.makeText(this, "❌ You declined the invitation.", Toast.LENGTH_SHORT).show();
                                    finishActivityWithResult(eventId, "Declined");
                                    return;
                                }

                                // Get waitlist object from event
                                Map<String, Object> waitlistMap = (Map<String, Object>) eventDoc.get("waitlist");
                                if (waitlistMap == null) {
                                    android.util.Log.d("InfoActivity", "No waitlist found in event");
                                    Toast.makeText(this, "❌ You declined the invitation.", Toast.LENGTH_SHORT).show();
                                    finishActivityWithResult(eventId, "Declined");
                                    return;
                                }

                                // Get waitlistedUsers array from waitlist map
                                ArrayList<Map<String, Object>> waitlistedUsers =
                                        (ArrayList<Map<String, Object>>) waitlistMap.get("waitlistedUsers");

                                if (waitlistedUsers == null) {
                                    waitlistedUsers = new ArrayList<>();
                                }

                                android.util.Log.d("InfoActivity", "Waitlist size: " + waitlistedUsers.size());

                                if (waitlistedUsers.isEmpty()) {
                                    // No replacement available - winner declined but no one to replace them
                                    android.util.Log.d("InfoActivity", "Waitlist is empty, no replacement available");
                                    Toast.makeText(this, "❌ You declined. No replacement available.", Toast.LENGTH_SHORT).show();
                                    finishActivityWithResult(eventId, "Declined");
                                    return;
                                }

                                // Step 3: User was a winner - select random replacement from waitlist
                                Random random = new Random();
                                int randomIndex = random.nextInt(waitlistedUsers.size());
                                Map<String, Object> replacementUserMap = waitlistedUsers.get(randomIndex);

                                String replacementUserId = (String) replacementUserMap.get("id");
                                String replacementUserName = (String) replacementUserMap.get("name");

                                android.util.Log.d("InfoActivity", "Random index selected: " + randomIndex);
                                android.util.Log.d("InfoActivity", "Replacement user: " + replacementUserName + " (ID: " + replacementUserId + ")");

                                if (replacementUserId == null || replacementUserId.isEmpty()) {
                                    Toast.makeText(this, "Failed to find replacement user", Toast.LENGTH_SHORT).show();
                                    reEnableButtons();
                                    return;
                                }

                                // Remove replacement from waitlist
                                waitlistedUsers.remove(randomIndex);

                                // Step 4: Update event document with new waitlist
                                Map<String, Object> eventUpdates = new HashMap<>();
                                eventUpdates.put("waitlist.waitlistedUsers", waitlistedUsers);

                                db.collection("event").document(eventId)
                                        .update(eventUpdates)
                                        .addOnSuccessListener(aVoid2 -> {
                                            android.util.Log.d("InfoActivity", "✅ Waitlist updated successfully");

                                            // Step 5: Update replacement user's status to "Notified" (making them a winner)
                                            Map<String, Object> replacementUpdates = new HashMap<>();
                                            replacementUpdates.put("registeredEvents." + eventId, "Notified");

                                            db.collection("users").document(replacementUserId)
                                                    .update(replacementUpdates)
                                                    .addOnSuccessListener(aVoid3 -> {
                                                        android.util.Log.d("InfoActivity", "✅ Replacement user notified (now a winner)");

                                                        // Step 6: Remove event from replacement's waitlistedEvents array
                                                        db.collection("users").document(replacementUserId)
                                                                .update("waitlistedEvents",
                                                                        com.google.firebase.firestore.FieldValue.arrayRemove(eventId))
                                                                .addOnSuccessListener(aVoid4 -> {
                                                                    android.util.Log.d("InfoActivity", "✅ Removed event from replacement's waitlist");
                                                                    Toast.makeText(this,
                                                                            "❌ You declined. " + replacementUserName + " selected from waitlist!",
                                                                            Toast.LENGTH_SHORT).show();
                                                                    finishActivityWithResult(eventId, "Declined");
                                                                })
                                                                .addOnFailureListener(e -> {
                                                                    // Still successful, just couldn't update array
                                                                    android.util.Log.w("InfoActivity", "Couldn't remove from waitlistedEvents: " + e.getMessage());
                                                                    Toast.makeText(this,
                                                                            "❌ You declined. Replacement selected from waitlist.",
                                                                            Toast.LENGTH_SHORT).show();
                                                                    finishActivityWithResult(eventId, "Declined");
                                                                });
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        android.util.Log.e("InfoActivity", "❌ Failed to notify replacement: " + e.getMessage());
                                                        Toast.makeText(this,
                                                                "Failed to notify replacement: " + e.getMessage(),
                                                                Toast.LENGTH_SHORT).show();
                                                        reEnableButtons();
                                                    });
                                        })
                                        .addOnFailureListener(e -> {
                                            android.util.Log.e("InfoActivity", "❌ Failed to update waitlist: " + e.getMessage());
                                            Toast.makeText(this,
                                                    "Failed to update waitlist: " + e.getMessage(),
                                                    Toast.LENGTH_SHORT).show();
                                            reEnableButtons();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                android.util.Log.e("InfoActivity", "❌ Failed to retrieve event: " + e.getMessage());
                                Toast.makeText(this,
                                        "Failed to retrieve event: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                                reEnableButtons();
                            });
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("InfoActivity", "❌ Failed to save decline: " + e.getMessage());
                    Toast.makeText(this, "Failed to save: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    reEnableButtons();
                });
    }

    /**
     * Helper method to re-enable buttons after a failed operation.
     */
    private void reEnableButtons() {
        acceptButton.setEnabled(true);
        declineButton.setEnabled(true);
        acceptButton.setAlpha(1.0f);
        declineButton.setAlpha(1.0f);
    }

    /**
     * Helper method to finish activity and return result to caller.
     *
     * @param eventId The ID of the event
     * @param status The final status of the user
     */
    private void finishActivityWithResult(String eventId, String status) {
        Intent out = new Intent(this, UserPanel.class);
        out.putExtra("USER_ID", currentUser.getId());
        out.putExtra("UPDATED_EVENT_ID", eventId);
        out.putExtra("UPDATED_STATUS", status);
        setResult(RESULT_OK, out);
        startActivity(out);
        finish();
    }

    /**
     * Updates the display of the user's current event status.
     * Changes the text and background colors of the status badge based on the current state.
     */
    private void updateStatusDisplay() {
        if (statusBadge == null) return;

        if (currentUser == null || currentEvent == null || currentEvent.getId() == null) {
            statusBadge.setText("Unknown");
            statusBadge.setBackgroundColor(android.graphics.Color.parseColor("#F5F5F5"));
            statusBadge.setTextColor(android.graphics.Color.GRAY);
            return;
        }

        // Use the User class's built-in method
        String status = currentUser.getStatusForEvent(currentEvent.getId());
        if (status == null || status.isEmpty() || status.equals("Not Registered")) {
            status = "Unknown";
        }

        statusBadge.setText(status);

        int backgroundColor;
        int textColor;

        switch (status) {
            case "Accepted":
                backgroundColor = ContextCompat.getColor(this, android.R.color.holo_green_light);
                textColor = ContextCompat.getColor(this, android.R.color.holo_green_dark);
                break;
            case "Declined":
                backgroundColor = ContextCompat.getColor(this, android.R.color.holo_red_light);
                textColor = ContextCompat.getColor(this, android.R.color.holo_red_dark);
                break;
            case "Notified":
                backgroundColor = ContextCompat.getColor(this, android.R.color.holo_blue_light);
                textColor = ContextCompat.getColor(this, android.R.color.holo_blue_dark);
                break;
            default:
                backgroundColor = android.graphics.Color.parseColor("#F5F5F5");
                textColor = android.graphics.Color.GRAY;
        }

        statusBadge.setBackgroundColor(backgroundColor);
        statusBadge.setTextColor(textColor);
    }

    /**
     * Formats a {@link Date} object into a readable string for display in the UI.
     *
     * @param date The date to be formatted.
     * @return A formatted string representing the event date and time.
     */
    private String formatEventDateTime(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("EEEE, MMM dd, yyyy 'at' h:mm a", Locale.getDefault());
        return formatter.format(date);
    }

    /**
     * Loads event data from intent and populates the UI
     * @param intent Intent containing event data
     */
    private void loadEventData(Intent intent) {
        String eventId = intent.getStringExtra("EVENT_ID");
        String eventName = intent.getStringExtra("EVENT_NAME");
        String eventDescription = intent.getStringExtra("EVENT_DESCRIPTION");
        String eventLocation = intent.getStringExtra("EVENT_LOCATION");
        String eventOrganizer = intent.getStringExtra("EVENT_ORGANIZER");
        long startTimeMillis = intent.getLongExtra("EVENT_START_TIME", 0);
        long endTimeMillis = intent.getLongExtra("EVENT_END_TIME", 0);
        currentStatus = intent.getStringExtra("EVENT_STATUS");

        Date startTime = new Date(startTimeMillis);
        Date endTime = new Date(endTimeMillis);

        currentEvent = new Event(eventId, eventName, eventDescription, eventLocation,
                eventOrganizer, "", startTime, endTime);

        // Don't manually add to registeredEvents - it should already be there from Firestore
        // Only ensure the status is set if it's missing
        if (!currentUser.getRegisteredEvents().containsKey(eventId) && currentStatus != null) {
            currentUser.getRegisteredEvents().put(eventId, currentStatus);
        }

        // Populate UI
        eventNameHeader.setText(eventName);
        eventDescriptionText.setText(eventDescription);
        eventLocationText.setText(eventLocation);
        eventOrganizerText.setText(eventOrganizer);
        eventDateTimeText.setText(formatEventDateTime(startTime));

        // Update status display
        updateStatusDisplay();
    }

    /**
     * AUTOMATIC TEST DATA SETUP
     *
     * Add this to the END of your onCreate() method in InfoActivity.
     * It will automatically run when you open the event details.
     *
     * To enable: Uncomment the setupTestData() call
     * To disable: Comment it out
     *
     * This creates:
     * 1. Sets your user as a winner ("Notified" status)
     * 2. Adds test users to the event's waitlist
     * 3. Creates the test user documents
     */

    /**
     * Automatically sets up test data when activity loads
     */
    private void setupTestData() {
        final String eventId = "4b89c209-33e7-4d2f-8b5f-2b3b0a569784"; // Your Demon Slayer event

        if (currentUser == null) {
            android.util.Log.e("TestSetup", "Current user is null, skipping test setup");
            return;
        }

        final String yourUserId = currentUser.getId();

        android.util.Log.d("TestSetup", "Starting automatic test data setup...");

        // Step 1: Make yourself a winner
        Map<String, Object> makeYouWinner = new HashMap<>();
        makeYouWinner.put("registeredEvents." + eventId, "Notified");

        db.collection("users").document(yourUserId)
                .update(makeYouWinner)
                .addOnSuccessListener(aVoid -> {
                    android.util.Log.d("TestSetup", "✅ You are now a winner!");

                    // Step 2: Create test waitlist users
                    createTestWaitlistUsers(eventId);
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("TestSetup", "❌ Failed to make you winner: " + e.getMessage());
                });
    }

    /**
     * Creates test users and adds them to the event's waitlist
     */
    private void createTestWaitlistUsers(String eventId) {
        android.util.Log.d("TestSetup", "Creating test users...");

        // Create 2 test users
        createTestUser("testUser1", "Alice Wonderland", "alice@test.com", eventId);
        createTestUser("testUser2", "Bob Builder", "bob@test.com", eventId);

        // Wait a bit for users to be created, then add them to event waitlist
        new android.os.Handler().postDelayed(() -> {
            addUsersToEventWaitlist(eventId);
        }, 2000); // Wait 2 seconds
    }

    /**
     * Creates a single test user document
     */
    private void createTestUser(String userId, String name, String email, String eventId) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", userId);
        userData.put("name", name);
        userData.put("email", email);

        // Add this event to their waitlisted events
        ArrayList<String> waitlistedEvents = new ArrayList<>();
        waitlistedEvents.add(eventId);
        userData.put("waitlistedEvents", waitlistedEvents);

        // Empty registered events map
        userData.put("registeredEvents", new HashMap<String, String>());

        db.collection("users").document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    android.util.Log.d("TestSetup", "✅ Created user: " + name);
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("TestSetup", "❌ Failed to create " + name + ": " + e.getMessage());
                });
    }

    /**
     * Adds the test users to the event's waitlist
     */
    private void addUsersToEventWaitlist(String eventId) {
        android.util.Log.d("TestSetup", "Adding users to event waitlist...");

        // Create waitlist user objects
        ArrayList<Map<String, Object>> waitlistUsers = new ArrayList<>();

        Map<String, Object> user1 = new HashMap<>();
        user1.put("id", "testUser1");
        user1.put("name", "Alice Wonderland");
        user1.put("email", "alice@test.com");
        waitlistUsers.add(user1);

        Map<String, Object> user2 = new HashMap<>();
        user2.put("id", "testUser2");
        user2.put("name", "Bob Builder");
        user2.put("email", "bob@test.com");
        waitlistUsers.add(user2);

        // Update the event's waitlist
        Map<String, Object> waitlistUpdate = new HashMap<>();
        waitlistUpdate.put("waitlist.waitlistedUsers", waitlistUsers);

        db.collection("event").document(eventId)
                .update(waitlistUpdate)
                .addOnSuccessListener(aVoid -> {
                    android.util.Log.d("TestSetup", "✅ TEST DATA READY! You can now test decline.");
                    Toast.makeText(this, "✅ Test data loaded - Ready to test decline!",
                            Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("TestSetup", "❌ Failed to update waitlist: " + e.getMessage());
                });
    }

// ============================================
// OPTIONAL: CLEANUP FUNCTION
// Call this manually if you want to reset
// ============================================
    /**
     * Cleans up test data
     * To use: Temporarily call cleanupTestData() in onCreate() instead of setupTestData()
     */
    private void cleanupTestData() {
        final String eventId = "4b89c209-33e7-4d2f-8b5f-2b3b0a569784";

        android.util.Log.d("TestSetup", "Cleaning up test data...");

        // Remove test users from Firebase
        db.collection("users").document("testUser1").delete()
                .addOnSuccessListener(aVoid -> android.util.Log.d("TestSetup", "Deleted testUser1"))
                .addOnFailureListener(e -> android.util.Log.e("TestSetup", "Failed to delete testUser1"));

        db.collection("users").document("testUser2").delete()
                .addOnSuccessListener(aVoid -> android.util.Log.d("TestSetup", "Deleted testUser2"))
                .addOnFailureListener(e -> android.util.Log.e("TestSetup", "Failed to delete testUser2"));

        // Clear the event's waitlist
        Map<String, Object> clearWaitlist = new HashMap<>();
        clearWaitlist.put("waitlist.waitlistedUsers", new ArrayList<>());

        db.collection("event").document(eventId)
                .update(clearWaitlist)
                .addOnSuccessListener(aVoid -> {
                    android.util.Log.d("TestSetup", "✅ Test data cleaned up!");
                    Toast.makeText(this, "✅ Test data cleaned up!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("TestSetup", "❌ Cleanup failed: " + e.getMessage());
                });
    }

}
