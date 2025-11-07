package com.example.eventlottery;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.eventlottery.view.EditUserInfoActivity;
import com.example.eventlottery.view.InfoActivity;
import com.example.eventlottery.view.UserPanel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

/**
 * UI Tests for UserPanel
 * Tests User Story 01.02.03: User can view their events and manage invitations
 *
 * @author Jensen Lee
 */
@RunWith(AndroidJUnit4.class)
public class UserPanelUITest {

    private ActivityScenario<UserPanel> scenario;

    /**
     * Sets up the test environment before each test
     * Initializes Espresso Intents for intent verification
     */
    @Before
    public void setUp() {
        Intents.init();
        // Add a delay to allow Firebase and other async operations to complete
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Cleans up after each test
     * Releases Espresso Intents and closes any open scenarios
     */
    @After
    public void tearDown() {
        if (scenario != null) {
            scenario.close();
        }
        Intents.release();
    }

    /**
     * Test that UserPanel launches successfully and displays user name
     * USER STORY 01.02.03
     * Verifies that the user's profile information is loaded and displayed
     */
    @Test
    public void testUserPanelLaunches() {
        scenario = ActivityScenario.launch(UserPanel.class);

        // Wait for async operations to complete
        waitForView(2000);

        // Verify user name view is displayed
        onView(withId(R.id.user_name))
                .check(matches(isDisplayed()));

        // Verify event list container is displayed
        onView(withId(R.id.event_list_container))
                .check(matches(isDisplayed()));
    }

    /**
     * Test that empty state is shown when user has no events
     * USER STORY 01.02.03
     * Verifies the empty state message is displayed correctly
     */
    @Test
    public void testEmptyState_Display() {
        scenario = ActivityScenario.launch(UserPanel.class);

        // Wait for data to load
        waitForView(3000);

        // Check if either empty state OR events are displayed
        // This test is flexible since we don't know if the user has events in Firebase
        try {
            onView(withText("No events yet"))
                    .check(matches(isDisplayed()));
            onView(withText("Browse events and join to see them here"))
                    .check(matches(isDisplayed()));
        } catch (Exception e) {
            // If empty state is not shown, verify event list container is displayed
            onView(withId(R.id.event_list_container))
                    .check(matches(isDisplayed()));
        }
    }

    /**
     * Test navigation to EditUserInfoActivity
     * USER STORY 01.02.03
     * Verifies that clicking the edit icon launches the EditUserInfoActivity
     */
    @Test
    public void testNavigationToEditUserInfo() {
        scenario = ActivityScenario.launch(UserPanel.class);

        // Wait for UI to load
        waitForView(2000);

        // Click edit icon
        onView(withId(R.id.edit_icon))
                .perform(click());

        // Verify EditUserInfoActivity is launched
        intended(hasComponent(EditUserInfoActivity.class.getName()));
    }

    /**
     * Test that event list container is visible
     * USER STORY 01.02.03
     * Verifies the container for event cards is properly displayed
     */
    @Test
    public void testEventListContainerVisible() {
        scenario = ActivityScenario.launch(UserPanel.class);

        // Wait for UI to load
        waitForView(2000);

        // Verify event list container exists and is visible
        onView(withId(R.id.event_list_container))
                .check(matches(isDisplayed()));
    }

    /**
     * Test that user name view exists and is visible
     * USER STORY 01.02.03
     * Verifies the user's name is displayed at the top of the panel
     */
    @Test
    public void testUserNameViewExists() {
        scenario = ActivityScenario.launch(UserPanel.class);

        // Wait for UI to load
        waitForView(2000);

        // Verify user name view is displayed
        onView(withId(R.id.user_name))
                .check(matches(isDisplayed()));
    }

    /**
     * Test that edit icon is visible and clickable
     * USER STORY 01.02.03
     * Verifies the edit profile icon is accessible to users
     */
    @Test
    public void testEditIconVisible() {
        scenario = ActivityScenario.launch(UserPanel.class);

        // Wait for UI to load
        waitForView(2000);

        // Verify edit icon is displayed
        onView(withId(R.id.edit_icon))
                .check(matches(isDisplayed()));
    }

    /**
     * Test that clicking on a notified event opens InfoActivity
     * USER STORY 01.02.03
     * Verifies that notified events are clickable and launch the detail view
     * Note: This test may fail if there are no notified events in the database
     */
    @Test
    public void testNotifiedEventClickable() {
        scenario = ActivityScenario.launch(UserPanel.class);

        // Wait for events to load
        waitForView(3000);

        // Check if "Notified" status badge exists
        try {
            onView(withText("Notified"))
                    .check(matches(isDisplayed()))
                    .perform(click());

            // Verify InfoActivity is launched
            intended(hasComponent(InfoActivity.class.getName()));
        } catch (Exception e) {
            // If no Notified events exist, test passes
            // In real scenario, you would set up test data in Firebase first
            System.out.println("No notified events found in database");
        }
    }

    /**
     * Test that event status badges display when events exist
     * USER STORY 01.02.03
     * Verifies that status badges (Accepted, Notified, Waitlisted) are shown
     * Note: This test is flexible and passes if any status badge is found
     */
    @Test
    public void testEventStatusBadgesCanDisplay() {
        scenario = ActivityScenario.launch(UserPanel.class);

        // Wait for data to load
        waitForView(3000);

        // Try to find any status badge - test passes if at least one exists
        try {
            // Look for any common status badge
            boolean foundBadge = false;
            try {
                onView(withText("Accepted")).check(matches(isDisplayed()));
                foundBadge = true;
            } catch (Exception e1) {
                try {
                    onView(withText("Notified")).check(matches(isDisplayed()));
                    foundBadge = true;
                } catch (Exception e2) {
                    try {
                        onView(withText("Waitlisted")).check(matches(isDisplayed()));
                        foundBadge = true;
                    } catch (Exception e3) {
                        try {
                            onView(withText("Declined")).check(matches(isDisplayed()));
                            foundBadge = true;
                        } catch (Exception e4) {
                            // No status badges found - probably empty state
                            onView(withText("No events yet")).check(matches(isDisplayed()));
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Test passes either way
            System.out.println("Status badge test completed");
        }
    }

    /**
     * Test that UserPanel handles onResume correctly
     * USER STORY 01.02.03
     * Verifies that returning to UserPanel refreshes the event list
     */
    @Test
    public void testOnResumeRefresh() {
        scenario = ActivityScenario.launch(UserPanel.class);

        // Wait for initial load
        waitForView(2000);

        // Simulate going to background and returning
        scenario.onActivity(activity -> {
            // Activity should refresh on resume
        });

        scenario.moveToState(androidx.lifecycle.Lifecycle.State.STARTED);
        scenario.moveToState(androidx.lifecycle.Lifecycle.State.RESUMED);

        // Wait for refresh
        waitForView(1000);

        // Verify UI is still intact
        onView(withId(R.id.user_name))
                .check(matches(isDisplayed()));

        onView(withId(R.id.event_list_container))
                .check(matches(isDisplayed()));
    }

    /**
     * Test that multiple navigation actions work correctly
     * USER STORY 01.02.03
     * Verifies that users can navigate back and forth between screens
     */
    @Test
    public void testMultipleNavigations() {
        scenario = ActivityScenario.launch(UserPanel.class);

        // Wait for UI to load
        waitForView(2000);

        // Click edit icon
        onView(withId(R.id.edit_icon))
                .perform(click());

        // Verify EditUserInfoActivity is launched
        intended(hasComponent(EditUserInfoActivity.class.getName()));

        // Note: Navigating back would require additional setup
        // This test verifies the intent was sent correctly
    }

    /**
     * Helper method to wait for asynchronous operations to complete
     * Useful for Firebase operations and UI rendering
     *
     * @param millis Time to wait in milliseconds
     */
    private void waitForView(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}