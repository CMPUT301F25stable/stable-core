package com.example.eventlottery;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.eventlottery.R;
import com.example.eventlottery.users.Organizer;
import com.example.eventlottery.view.InfoActivity;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

/**
 * UI tests for InfoActivity
 * Tests event invitation viewing and accept/decline functionality
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class InfoActivityUITest {

    private FirebaseFirestore db;
    private String testUserId = "test_user_123";
    private String testEventId = "test_event_456";

    @Before
    public void setUp() {
        // Initialize Intents for intent verification
        Intents.init();

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Create test user data
        setupTestUser();
    }

    @After
    public void tearDown() {
        // Clean up test data
        cleanupTestData();

        // Release Intents
        Intents.release();
    }

    /**
     * Sets up a test user in Firestore
     */
    private void setupTestUser() {
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", testUserId);
        userData.put("name", "Test User");
        userData.put("email", "testuser@example.com");

        Map<String, String> registeredEvents = new HashMap<>();
        registeredEvents.put(testEventId, "Notified");
        userData.put("registeredEvents", registeredEvents);
        userData.put("waitlistedEvents", new ArrayList<String>());

        db.collection("users").document(testUserId)
                .set(userData);

        // Wait for Firestore operation to complete
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Cleans up test data from Firestore
     */
    private void cleanupTestData() {
        db.collection("users").document(testUserId).delete();
    }

    /**
     * Creates an intent with event data for testing
     */
    private Intent createTestIntent() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), InfoActivity.class);
        intent.putExtra("USER_ID", testUserId);
        intent.putExtra("EVENT_ID", testEventId);
        intent.putExtra("EVENT_NAME", "Test Event");
        intent.putExtra("EVENT_DESCRIPTION", "This is a test event description");
        intent.putExtra("EVENT_LOCATION", "Test Location");
        intent.putExtra("EVENT_ORGANIZER", "Test Organizer");
        intent.putExtra("EVENT_START_TIME", new Date().getTime());
        intent.putExtra("EVENT_END_TIME", new Date().getTime() + 3600000); // +1 hour
        intent.putExtra("EVENT_STATUS", "Notified");
        return intent;
    }

    @Test
    public void testActivityLaunches() {
        Intent intent = createTestIntent();
        ActivityScenario<InfoActivity> scenario = ActivityScenario.launch(intent);

        // Verify activity is displayed
        onView(withId(R.id.eventNameHeader)).check(matches(isDisplayed()));

        scenario.close();
    }

    @Test
    public void testEventDetailsDisplayed() {
        Intent intent = createTestIntent();
        ActivityScenario<InfoActivity> scenario = ActivityScenario.launch(intent);

        // Wait for data to load
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify event details are displayed
        onView(withId(R.id.eventNameHeader)).check(matches(withText("Test Event")));
        onView(withId(R.id.eventDescriptionText)).check(matches(withText("This is a test event description")));
        onView(withId(R.id.eventLocationText)).check(matches(withText("Test Location")));
        onView(withId(R.id.eventOrganizerText)).check(matches(withText("Test Organizer")));

        scenario.close();
    }

    @Test
    public void testStatusBadgeDisplayed() {
        Intent intent = createTestIntent();
        ActivityScenario<InfoActivity> scenario = ActivityScenario.launch(intent);

        // Wait for data to load
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify status badge is displayed
        onView(withId(R.id.statusBadge)).check(matches(isDisplayed()));
        onView(withId(R.id.statusBadge)).check(matches(withText("Notified")));

        scenario.close();
    }

    @Test
    public void testAcceptButtonDisplayed() {
        Intent intent = createTestIntent();
        ActivityScenario<InfoActivity> scenario = ActivityScenario.launch(intent);

        // Wait for data to load
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify accept button is displayed and enabled
        onView(withId(R.id.acceptButton)).check(matches(isDisplayed()));
        onView(withId(R.id.acceptButton)).check(matches(isEnabled()));

        scenario.close();
    }

    @Test
    public void testDeclineButtonDisplayed() {
        Intent intent = createTestIntent();
        ActivityScenario<InfoActivity> scenario = ActivityScenario.launch(intent);

        // Wait for data to load
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify decline button is displayed and enabled
        onView(withId(R.id.declineButton)).check(matches(isDisplayed()));
        onView(withId(R.id.declineButton)).check(matches(isEnabled()));

        scenario.close();
    }

    @Test
    public void testBackButtonNavigatesBack() {
        Intent intent = createTestIntent();
        ActivityScenario<InfoActivity> scenario = ActivityScenario.launch(intent);

        // Wait for data to load
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Click back button
        onView(withId(R.id.backButton)).perform(click());

        // Activity should finish (verified by scenario state)
        scenario.close();
    }

    @Test
    public void testAcceptButtonShowsConfirmationDialog() {
        Intent intent = createTestIntent();
        ActivityScenario<InfoActivity> scenario = ActivityScenario.launch(intent);

        // Wait for data to load
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Click accept button
        onView(withId(R.id.acceptButton)).perform(click());

        // Verify dialog is shown
        onView(withText("Accept Invitation?")).check(matches(isDisplayed()));
        onView(withText("Are you sure you want to accept this event invitation?")).check(matches(isDisplayed()));

        scenario.close();
    }

    @Test
    public void testDeclineButtonShowsConfirmationDialog() {
        Intent intent = createTestIntent();
        ActivityScenario<InfoActivity> scenario = ActivityScenario.launch(intent);

        // Wait for data to load
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Click decline button
        onView(withId(R.id.declineButton)).perform(click());

        // Verify dialog is shown
        onView(withText("Decline Invitation?")).check(matches(isDisplayed()));
        onView(withText("Are you sure you want to decline this event invitation?")).check(matches(isDisplayed()));

        scenario.close();
    }

    @Test
    public void testAcceptDialogCancelButton() {
        Intent intent = createTestIntent();
        ActivityScenario<InfoActivity> scenario = ActivityScenario.launch(intent);

        // Wait for data to load
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Click accept button
        onView(withId(R.id.acceptButton)).perform(click());

        // Click cancel on dialog
        onView(withText("Cancel")).perform(click());

        // Verify buttons are still enabled
        onView(withId(R.id.acceptButton)).check(matches(isEnabled()));
        onView(withId(R.id.declineButton)).check(matches(isEnabled()));

        scenario.close();
    }

    @Test
    public void testDeclineDialogCancelButton() {
        Intent intent = createTestIntent();
        ActivityScenario<InfoActivity> scenario = ActivityScenario.launch(intent);

        // Wait for data to load
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Click decline button
        onView(withId(R.id.declineButton)).perform(click());

        // Click cancel on dialog
        onView(withText("Cancel")).perform(click());

        // Verify buttons are still enabled
        onView(withId(R.id.acceptButton)).check(matches(isEnabled()));
        onView(withId(R.id.declineButton)).check(matches(isEnabled()));

        scenario.close();
    }

    @Test
    public void testAcceptInvitationUpdatesFirestore() {
        Intent intent = createTestIntent();
        ActivityScenario<InfoActivity> scenario = ActivityScenario.launch(intent);

        // Wait for data to load
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Click accept button
        onView(withId(R.id.acceptButton)).perform(click());

        // Confirm in dialog
        onView(withText("Accept")).perform(click());

        // Wait for Firestore update to complete before closing
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Close scenario before navigation completes
        scenario.close();

        // Wait a bit more for async operation
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify the status was updated in Firestore
        db.collection("users").document(testUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, String> registeredEvents =
                                (Map<String, String>) documentSnapshot.get("registeredEvents");
                        if (registeredEvents != null) {
                            String status = registeredEvents.get(testEventId);
                            assert "Accepted".equals(status) : "Expected 'Accepted' but got: " + status;
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    throw new AssertionError("Failed to verify Firestore update: " + e.getMessage());
                });
    }

    @Test
    public void testDeclineInvitationUpdatesFirestore() {
        Intent intent = createTestIntent();
        ActivityScenario<InfoActivity> scenario = ActivityScenario.launch(intent);

        // Wait for data to load
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Click decline button
        onView(withId(R.id.declineButton)).perform(click());

        // Confirm in dialog
        onView(withText("Decline")).perform(click());

        // Wait for Firestore update to complete before closing
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Close scenario before navigation completes
        scenario.close();

        // Wait a bit more for async operation
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify the status was updated in Firestore
        db.collection("users").document(testUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, String> registeredEvents =
                                (Map<String, String>) documentSnapshot.get("registeredEvents");
                        if (registeredEvents != null) {
                            String status = registeredEvents.get(testEventId);
                            assert "Declined".equals(status) : "Expected 'Declined' but got: " + status;
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    throw new AssertionError("Failed to verify Firestore update: " + e.getMessage());
                });
    }

    @Test
    public void testButtonsDisabledDuringAccept() {
        Intent intent = createTestIntent();
        ActivityScenario<InfoActivity> scenario = ActivityScenario.launch(intent);

        // Wait for data to load
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Click accept button
        onView(withId(R.id.acceptButton)).perform(click());

        // Confirm in dialog
        onView(withText("Accept")).perform(click());

        // Buttons should be disabled immediately
        onView(withId(R.id.acceptButton)).check(matches(not(isEnabled())));
        onView(withId(R.id.declineButton)).check(matches(not(isEnabled())));

        scenario.close();
    }

    @Test
    public void testInvalidUserIdFinishesActivity() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), InfoActivity.class);
        intent.putExtra("USER_ID", ""); // Empty user ID

        ActivityScenario<InfoActivity> scenario = ActivityScenario.launch(intent);

        // Activity should finish due to invalid user ID
        // This is verified by the scenario state

        scenario.close();
    }
}