package com.example.eventlottery;

import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.eventlottery.view.OrganizerEventInfoFragment;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

/**
 * UI tests for OrganizerEventInfoFragment
 * Tests entrant count display and notification functionality
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class OrganizerEventInfoFragmentUITest {

    private FirebaseFirestore db;
    private String testEventId = "test_event_789";
    private String testEventName = "Test Conference 2024";
    private String testUserId1 = "test_user_001";
    private String testUserId2 = "test_user_002";
    private String testUserId3 = "test_user_003";

    /**
     * Simple test activity to host the fragment
     */
    public static class TestFragmentActivity extends FragmentActivity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_organizer_panel);
        }

        public void setFragment(OrganizerEventInfoFragment fragment) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.eventListFragment, fragment)
                    .commitNow();
        }
    }

    @Before
    public void setUp() {
        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Create test data
        setupTestEvent();
        setupTestUsers();
    }

    @After
    public void tearDown() {
        // Clean up test data
        cleanupTestData();
    }

    /**
     * Sets up a test event in Firestore with waitlist, selected, and cancelled entrants
     */
    private void setupTestEvent() {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("id", testEventId);
        eventData.put("name", testEventName);
        eventData.put("description", "A test conference event");

        // Create waitlist with one user
        Map<String, Object> waitlist = new HashMap<>();
        List<Map<String, Object>> waitlistedUsers = new ArrayList<>();
        Map<String, Object> waitlistedUser = new HashMap<>();
        waitlistedUser.put("id", testUserId1);
        waitlistedUsers.add(waitlistedUser);
        waitlist.put("waitlistedUsers", waitlistedUsers);
        eventData.put("waitlist", waitlist);

        // Add selected users
        List<String> selectedIds = new ArrayList<>();
        selectedIds.add(testUserId2);
        eventData.put("selectedIds", selectedIds);

        // Add cancelled entrants
        List<String> cancelledEntrants = new ArrayList<>();
        cancelledEntrants.add(testUserId3);
        eventData.put("cancelledEntrants", cancelledEntrants);

        db.collection("event-p4").document(testEventId).set(eventData);

        // Wait for Firestore operation
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets up test users in Firestore
     */
    private void setupTestUsers() {
        // User 1 - Waitlisted
        Map<String, Object> user1Data = new HashMap<>();
        user1Data.put("id", testUserId1);
        user1Data.put("name", "Test User One");
        user1Data.put("email", "user1@test.com");
        user1Data.put("fcmToken", "test_token_1");
        db.collection("users-p4").document(testUserId1).set(user1Data);

        // User 2 - Selected
        Map<String, Object> user2Data = new HashMap<>();
        user2Data.put("id", testUserId2);
        user2Data.put("name", "Test User Two");
        user2Data.put("email", "user2@test.com");
        user2Data.put("fcmToken", "test_token_2");
        db.collection("users-p4").document(testUserId2).set(user2Data);

        // User 3 - Cancelled
        Map<String, Object> user3Data = new HashMap<>();
        user3Data.put("id", testUserId3);
        user3Data.put("name", "Test User Three");
        user3Data.put("email", "user3@test.com");
        user3Data.put("fcmToken", "test_token_3");
        db.collection("users-p4").document(testUserId3).set(user3Data);

        // Wait for Firestore operations
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
        db.collection("event-p4").document(testEventId).delete();
        db.collection("users-p4").document(testUserId1).delete();
        db.collection("users-p4").document(testUserId2).delete();
        db.collection("users-p4").document(testUserId3).delete();
    }

    /**
     * Creates fragment with test arguments
     */
    private OrganizerEventInfoFragment createTestFragment() {
        return OrganizerEventInfoFragment.newInstance(
                testEventId,
                testEventName,
                1, // waitlist count
                1, // selected count
                1  // cancelled count
        );
    }

    /**
     * Launches the fragment in a test activity
     */
    private ActivityScenario<TestFragmentActivity> launchFragment(OrganizerEventInfoFragment fragment) {
        ActivityScenario<TestFragmentActivity> scenario = ActivityScenario.launch(TestFragmentActivity.class);
        scenario.onActivity(activity -> activity.setFragment(fragment));
        return scenario;
    }

    @Test
    public void testFragmentLaunches() {
        OrganizerEventInfoFragment fragment = createTestFragment();
        ActivityScenario<TestFragmentActivity> scenario = launchFragment(fragment);

        // Verify fragment is displayed
        onView(withId(R.id.headerText)).check(matches(isDisplayed()));

        scenario.close();
    }

    @Test
    public void testEventNameDisplayed() {
        OrganizerEventInfoFragment fragment = createTestFragment();
        ActivityScenario<TestFragmentActivity> scenario = launchFragment(fragment);

        // Wait for data to load
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify event name is displayed
        onView(withId(R.id.headerText)).check(matches(withText(testEventName)));

        scenario.close();
    }

    @Test
    public void testWaitlistCountDisplayed() {
        OrganizerEventInfoFragment fragment = createTestFragment();
        ActivityScenario<TestFragmentActivity> scenario = launchFragment(fragment);

        // Wait for data to load
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify waitlist count is displayed
        onView(withId(R.id.waitingCount)).check(matches(withText("1")));

        scenario.close();
    }

    @Test
    public void testSelectedCountDisplayed() {
        OrganizerEventInfoFragment fragment = createTestFragment();
        ActivityScenario<TestFragmentActivity> scenario = launchFragment(fragment);

        // Wait for data to load
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify selected count is displayed
        onView(withId(R.id.selectedCount)).check(matches(withText("1")));

        scenario.close();
    }

    @Test
    public void testCancelledCountDisplayed() {
        OrganizerEventInfoFragment fragment = createTestFragment();
        ActivityScenario<TestFragmentActivity> scenario = launchFragment(fragment);

        // Wait for data to load
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify cancelled count is displayed
        onView(withId(R.id.cancelledCount)).check(matches(withText("1")));

        scenario.close();
    }

    @Test
    public void testWaitingListCardDisplayed() {
        OrganizerEventInfoFragment fragment = createTestFragment();
        ActivityScenario<TestFragmentActivity> scenario = launchFragment(fragment);

        // Verify waiting list card is displayed
        onView(withId(R.id.waitingListCard)).check(matches(isDisplayed()));

        scenario.close();
    }

    @Test
    public void testSelectedCardDisplayed() {
        OrganizerEventInfoFragment fragment = createTestFragment();
        ActivityScenario<TestFragmentActivity> scenario = launchFragment(fragment);

        // Verify selected card is displayed
        onView(withId(R.id.selectedCard)).check(matches(isDisplayed()));

        scenario.close();
    }

    @Test
    public void testCancelledCardDisplayed() {
        OrganizerEventInfoFragment fragment = createTestFragment();
        ActivityScenario<TestFragmentActivity> scenario = launchFragment(fragment);

        // Verify cancelled card is displayed
        onView(withId(R.id.cancelledCard)).check(matches(isDisplayed()));

        scenario.close();
    }

    @Test
    public void testCloseButtonDisplayed() {
        OrganizerEventInfoFragment fragment = createTestFragment();
        ActivityScenario<TestFragmentActivity> scenario = launchFragment(fragment);

        // Verify close button is displayed
        onView(withId(R.id.closeButton)).check(matches(isDisplayed()));

        scenario.close();
    }

    @Test
    public void testWaitingListCardClickShowsDialog() {
        OrganizerEventInfoFragment fragment = createTestFragment();
        ActivityScenario<TestFragmentActivity> scenario = launchFragment(fragment);

        // Wait for data to load
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Click waiting list card
        onView(withId(R.id.waitingListCard)).perform(click());

        // Wait for dialog to appear
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify notification dialog is displayed
        onView(withId(R.id.messageInput)).check(matches(isDisplayed()));
        onView(withId(R.id.sendButton)).check(matches(isDisplayed()));
        onView(withId(R.id.cancelButton)).check(matches(isDisplayed()));

        scenario.close();
    }

    @Test
    public void testSelectedCardClickShowsDialog() {
        OrganizerEventInfoFragment fragment = createTestFragment();
        ActivityScenario<TestFragmentActivity> scenario = launchFragment(fragment);

        // Wait for data to load
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Click selected card
        onView(withId(R.id.selectedCard)).perform(click());

        // Wait for dialog to appear
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify notification dialog is displayed
        onView(withId(R.id.messageInput)).check(matches(isDisplayed()));

        scenario.close();
    }

    @Test
    public void testCancelledCardClickShowsDialog() {
        OrganizerEventInfoFragment fragment = createTestFragment();
        ActivityScenario<TestFragmentActivity> scenario = launchFragment(fragment);

        // Wait for data to load
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Click cancelled card
        onView(withId(R.id.cancelledCard)).perform(click());

        // Wait for dialog to appear
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify notification dialog is displayed
        onView(withId(R.id.messageInput)).check(matches(isDisplayed()));

        scenario.close();
    }

    @Test
    public void testRecipientCountDisplayed() {
        OrganizerEventInfoFragment fragment = createTestFragment();
        ActivityScenario<TestFragmentActivity> scenario = launchFragment(fragment);

        // Wait for data to load
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Click waiting list card
        onView(withId(R.id.waitingListCard)).perform(click());

        // Wait for dialog
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify recipient count chip is displayed
        onView(withId(R.id.recipientCount)).check(matches(isDisplayed()));
        onView(withId(R.id.recipientCount)).check(matches(withText("1 recipient")));

        scenario.close();
    }

    @Test
    public void testSuggestionChipsDisplayed() {
        OrganizerEventInfoFragment fragment = createTestFragment();
        ActivityScenario<TestFragmentActivity> scenario = launchFragment(fragment);

        // Wait for data to load
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Click waiting list card
        onView(withId(R.id.waitingListCard)).perform(click());

        // Wait for dialog
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify suggestion chips are displayed
        onView(withId(R.id.suggestionChip1)).check(matches(isDisplayed()));
        onView(withId(R.id.suggestionChip2)).check(matches(isDisplayed()));
        onView(withId(R.id.suggestionChip3)).check(matches(isDisplayed()));

        scenario.close();
    }

    @Test
    public void testSendButtonInitiallyDisabled() {
        OrganizerEventInfoFragment fragment = createTestFragment();
        ActivityScenario<TestFragmentActivity> scenario = launchFragment(fragment);

        // Wait for data to load
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Click waiting list card
        onView(withId(R.id.waitingListCard)).perform(click());

        // Wait for dialog
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify send button is initially disabled
        onView(withId(R.id.sendButton)).check(matches(not(isEnabled())));

        scenario.close();
    }

    @Test
    public void testSuggestionChipFillsMessageInput() {
        OrganizerEventInfoFragment fragment = createTestFragment();
        ActivityScenario<TestFragmentActivity> scenario = launchFragment(fragment);

        // Wait for data to load
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Click waiting list card
        onView(withId(R.id.waitingListCard)).perform(click());

        // Wait for dialog
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Click first suggestion chip
        onView(withId(R.id.suggestionChip1)).perform(click());

        // Verify message input is filled
        onView(withId(R.id.messageInput)).check(matches(not(withText(""))));

        scenario.close();
    }

    @Test
    public void testSendButtonEnabledAfterTyping() {
        OrganizerEventInfoFragment fragment = createTestFragment();
        ActivityScenario<TestFragmentActivity> scenario = launchFragment(fragment);

        // Wait for data to load
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Click waiting list card
        onView(withId(R.id.waitingListCard)).perform(click());

        // Wait for dialog
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Type a message
        onView(withId(R.id.messageInput))
                .perform(replaceText("Test notification message"), closeSoftKeyboard());

        // Wait for UI update
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify send button is enabled
        onView(withId(R.id.sendButton)).check(matches(isEnabled()));

        scenario.close();
    }

    @Test
    public void testCancelButtonClosesDialog() {
        OrganizerEventInfoFragment fragment = createTestFragment();
        ActivityScenario<TestFragmentActivity> scenario = launchFragment(fragment);

        // Wait for data to load
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Click waiting list card
        onView(withId(R.id.waitingListCard)).perform(click());

        // Wait for dialog
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Click cancel button
        onView(withId(R.id.cancelButton)).perform(click());

        // Wait for dialog to close
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify cards are still displayed (dialog closed)
        onView(withId(R.id.waitingListCard)).check(matches(isDisplayed()));

        scenario.close();
    }

    @Test
    public void testRealtimeCountUpdates() {
        OrganizerEventInfoFragment fragment = createTestFragment();
        ActivityScenario<TestFragmentActivity> scenario = launchFragment(fragment);

        // Wait for initial data load
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify initial count
        onView(withId(R.id.waitingCount)).check(matches(withText("1")));

        // Update Firestore data
        Map<String, Object> waitlist = new HashMap<>();
        List<Map<String, Object>> waitlistedUsers = new ArrayList<>();

        Map<String, Object> user1 = new HashMap<>();
        user1.put("id", testUserId1);
        waitlistedUsers.add(user1);

        Map<String, Object> user2 = new HashMap<>();
        user2.put("id", "new_user_id");
        waitlistedUsers.add(user2);

        waitlist.put("waitlistedUsers", waitlistedUsers);

        db.collection("event-p4")
                .document(testEventId)
                .update("waitlist", waitlist);

        // Wait for real-time update
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify count updated
        onView(withId(R.id.waitingCount)).check(matches(withText("2")));

        scenario.close();
    }

    @Test
    public void testEmptyWaitlistShowsToast() {
        // Create fragment with zero counts
        OrganizerEventInfoFragment fragment = OrganizerEventInfoFragment.newInstance(
                testEventId,
                testEventName,
                0, // empty waitlist
                0,
                0
        );

        ActivityScenario<TestFragmentActivity> scenario = launchFragment(fragment);

        // Wait for data to load
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Update Firestore to have empty waitlist
        Map<String, Object> waitlist = new HashMap<>();
        waitlist.put("waitlistedUsers", new ArrayList<>());
        db.collection("event-p4")
                .document(testEventId)
                .update("waitlist", waitlist);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Click waiting list card - should show toast, not dialog
        onView(withId(R.id.waitingListCard)).perform(click());

        // Wait to ensure no dialog appears
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify card is still displayed (no dialog opened)
        onView(withId(R.id.waitingListCard)).check(matches(isDisplayed()));

        scenario.close();
    }

    @Test
    public void testMultipleRecipients() {
        // Create fragment with multiple recipients
        OrganizerEventInfoFragment fragment = OrganizerEventInfoFragment.newInstance(
                testEventId,
                testEventName,
                3, // 3 recipients
                1,
                1
        );

        ActivityScenario<TestFragmentActivity> scenario = launchFragment(fragment);

        // Wait for data to load
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Click waiting list card
        onView(withId(R.id.waitingListCard)).perform(click());

        // Wait for dialog
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify recipient count shows plural
        onView(withId(R.id.recipientCount)).check(matches(withText("3 recipients")));

        scenario.close();
    }
}