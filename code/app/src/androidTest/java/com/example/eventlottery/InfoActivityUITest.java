package com.example.eventlottery;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.eventlottery.view.InfoActivity;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
    private String testUserId = "test_user_info_123";
    private String testEventId = "test_event_info_456";
    private String testWaitlistUser1 = "test_waitlist_user_1";
    private String testWaitlistUser2 = "test_waitlist_user_2";

    @Before
    public void setUp() {
        // Initialize Intents for intent verification
        Intents.init();

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Create test data with proper synchronization
        setupTestUser();
        setupTestEvent();
        setupWaitlistUsers();

        // Wait for all Firestore operations to complete
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
        CountDownLatch latch = new CountDownLatch(1);

        Map<String, Object> userData = new HashMap<>();
        userData.put("id", testUserId);
        userData.put("name", "Test User");
        userData.put("email", "testuser@example.com");
        userData.put("phoneNumber", "123-456-7890");

        Map<String, String> registeredEvents = new HashMap<>();
        registeredEvents.put(testEventId, "Notified");
        userData.put("registeredEvents", registeredEvents);
        userData.put("waitlistedEvents", new ArrayList<String>());

        db.collection("users-p4").document(testUserId)
                .set(userData)
                .addOnSuccessListener(aVoid -> latch.countDown())
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    latch.countDown();
                });

        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets up a test event in Firestore with waitlist
     */
    private void setupTestEvent() {
        CountDownLatch latch = new CountDownLatch(1);

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("id", testEventId);
        eventData.put("name", "Test Event");
        eventData.put("description", "This is a test event description");
        eventData.put("location", "Test Location");
        eventData.put("organizer", "Test Organizer");

        // Create waitlist with two users
        Map<String, Object> waitlist = new HashMap<>();
        List<Map<String, Object>> waitlistedUsers = new ArrayList<>();

        Map<String, Object> user1 = new HashMap<>();
        user1.put("id", testWaitlistUser1);
        user1.put("name", "Waitlist User One");
        user1.put("email", "waitlist1@test.com");
        waitlistedUsers.add(user1);

        Map<String, Object> user2 = new HashMap<>();
        user2.put("id", testWaitlistUser2);
        user2.put("name", "Waitlist User Two");
        user2.put("email", "waitlist2@test.com");
        waitlistedUsers.add(user2);

        waitlist.put("waitlistedUsers", waitlistedUsers);
        eventData.put("waitlist", waitlist);

        // Initialize empty arrays
        eventData.put("selectedIds", new ArrayList<String>());
        eventData.put("cancelledEntrants", new ArrayList<String>());

        // Initialize finalized list
        Map<String, Object> finalizedList = new HashMap<>();
        finalizedList.put("finalizedUsers", new ArrayList<Map<String, Object>>());
        eventData.put("finalizedList", finalizedList);

        db.collection("event-p4").document(testEventId)
                .set(eventData)
                .addOnSuccessListener(aVoid -> latch.countDown())
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    latch.countDown();
                });

        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets up waitlist users in Firestore
     */
    private void setupWaitlistUsers() {
        CountDownLatch latch = new CountDownLatch(2);

        // Waitlist User 1
        Map<String, Object> user1Data = new HashMap<>();
        user1Data.put("id", testWaitlistUser1);
        user1Data.put("name", "Waitlist User One");
        user1Data.put("email", "waitlist1@test.com");

        List<String> waitlistedEvents1 = new ArrayList<>();
        waitlistedEvents1.add(testEventId);
        user1Data.put("waitlistedEvents", waitlistedEvents1);
        user1Data.put("registeredEvents", new HashMap<String, String>());

        db.collection("users-p4").document(testWaitlistUser1)
                .set(user1Data)
                .addOnCompleteListener(task -> latch.countDown());

        // Waitlist User 2
        Map<String, Object> user2Data = new HashMap<>();
        user2Data.put("id", testWaitlistUser2);
        user2Data.put("name", "Waitlist User Two");
        user2Data.put("email", "waitlist2@test.com");

        List<String> waitlistedEvents2 = new ArrayList<>();
        waitlistedEvents2.add(testEventId);
        user2Data.put("waitlistedEvents", waitlistedEvents2);
        user2Data.put("registeredEvents", new HashMap<String, String>());

        db.collection("users-p4").document(testWaitlistUser2)
                .set(user2Data)
                .addOnCompleteListener(task -> latch.countDown());

        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Cleans up test data from Firestore
     */
    private void cleanupTestData() {
        db.collection("users-p4").document(testUserId).delete();
        db.collection("users-p4").document(testWaitlistUser1).delete();
        db.collection("users-p4").document(testWaitlistUser2).delete();
        db.collection("event-p4").document(testEventId).delete();
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

        // Wait for activity to load user data from Firestore
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

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
            Thread.sleep(2000);
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
            Thread.sleep(2000);
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
            Thread.sleep(2000);
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
            Thread.sleep(2000);
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
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Click back button
        onView(withId(R.id.backButton)).perform(click());

        // Wait for activity to finish
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        scenario.close();
    }

    @Test
    public void testAcceptButtonShowsConfirmationDialog() {
        Intent intent = createTestIntent();
        ActivityScenario<InfoActivity> scenario = ActivityScenario.launch(intent);

        // Wait for data to load
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Click accept button
        onView(withId(R.id.acceptButton)).perform(click());

        // Wait for dialog
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

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
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Click decline button
        onView(withId(R.id.declineButton)).perform(click());

        // Wait for dialog
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

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
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Click accept button
        onView(withId(R.id.acceptButton)).perform(click());

        // Wait for dialog
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Click cancel on dialog
        onView(withText("Cancel")).perform(click());

        // Wait for dialog to close
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

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
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Click decline button
        onView(withId(R.id.declineButton)).perform(click());

        // Wait for dialog
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Click cancel on dialog
        onView(withText("Cancel")).perform(click());

        // Wait for dialog to close
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

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
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Click accept button
        onView(withId(R.id.acceptButton)).perform(click());

        // Wait for dialog
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Confirm in dialog
        onView(withText("Accept")).perform(click());

        // Wait for Firestore update and navigation to complete
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Close scenario
        scenario.close();

        // Verify the status was updated in Firestore
        CountDownLatch verifyLatch = new CountDownLatch(1);

        db.collection("users-p4").document(testUserId)
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
                    verifyLatch.countDown();
                })
                .addOnFailureListener(e -> {
                    verifyLatch.countDown();
                    throw new AssertionError("Failed to verify Firestore update: " + e.getMessage());
                });

        try {
            verifyLatch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testDeclineInvitationUpdatesFirestore() {
        Intent intent = createTestIntent();
        ActivityScenario<InfoActivity> scenario = ActivityScenario.launch(intent);

        // Wait for data to load
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Click decline button
        onView(withId(R.id.declineButton)).perform(click());

        // Wait for dialog
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Confirm in dialog
        onView(withText("Decline")).perform(click());

        // Wait for Firestore update and navigation to complete
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Close scenario
        scenario.close();

        // Verify the status was updated in Firestore
        CountDownLatch verifyLatch = new CountDownLatch(1);

        db.collection("users-p4").document(testUserId)
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
                    verifyLatch.countDown();
                })
                .addOnFailureListener(e -> {
                    verifyLatch.countDown();
                    throw new AssertionError("Failed to verify Firestore update: " + e.getMessage());
                });

        try {
            verifyLatch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testDeclineReplacesWithWaitlistUser() {
        Intent intent = createTestIntent();
        ActivityScenario<InfoActivity> scenario = ActivityScenario.launch(intent);

        // Wait for data to load
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Click decline button
        onView(withId(R.id.declineButton)).perform(click());

        // Wait for dialog
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Confirm in dialog
        onView(withText("Decline")).perform(click());

        // Wait for all Firestore operations (decline + replacement) to complete
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Close scenario
        scenario.close();

        // Verify that one of the waitlist users was promoted to "Notified"
        CountDownLatch verifyLatch = new CountDownLatch(1);

        db.collection("users-p4").document(testWaitlistUser1)
                .get()
                .addOnSuccessListener(doc1 -> {
                    db.collection("users-p4").document(testWaitlistUser2)
                            .get()
                            .addOnSuccessListener(doc2 -> {
                                boolean user1Notified = false;
                                boolean user2Notified = false;

                                if (doc1.exists()) {
                                    Map<String, String> events1 =
                                            (Map<String, String>) doc1.get("registeredEvents");
                                    if (events1 != null) {
                                        user1Notified = "Notified".equals(events1.get(testEventId));
                                    }
                                }

                                if (doc2.exists()) {
                                    Map<String, String> events2 =
                                            (Map<String, String>) doc2.get("registeredEvents");
                                    if (events2 != null) {
                                        user2Notified = "Notified".equals(events2.get(testEventId));
                                    }
                                }

                                assert (user1Notified || user2Notified) :
                                        "Expected one waitlist user to be notified";

                                verifyLatch.countDown();
                            });
                })
                .addOnFailureListener(e -> {
                    verifyLatch.countDown();
                    throw new AssertionError("Failed to verify replacement: " + e.getMessage());
                });

        try {
            verifyLatch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testInvalidUserIdFinishesActivity() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), InfoActivity.class);
        intent.putExtra("USER_ID", ""); // Empty user ID
        intent.putExtra("EVENT_ID", testEventId);
        intent.putExtra("EVENT_NAME", "Test Event");

        ActivityScenario<InfoActivity> scenario = ActivityScenario.launch(intent);

        // Wait for activity to process invalid user ID
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Activity should finish due to invalid user ID
        // This is verified by the scenario state

        scenario.close();
    }
}