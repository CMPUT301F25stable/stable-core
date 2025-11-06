package com.example.eventlottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.eventlottery.view.InfoActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * UI Tests for InfoActivity
 * Tests User Story 01.02.03: User can accept/decline invitations
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class InfoActivityUITest {

    private Intent testIntent;

    @Before
    public void setUp() {
        Intents.init();

        // Create test intent with event data
        testIntent = new Intent(ApplicationProvider.getApplicationContext(), InfoActivity.class);
        testIntent.putExtra("EVENT_ID", "event123");
        testIntent.putExtra("EVENT_NAME", "Test Event");
        testIntent.putExtra("EVENT_DESCRIPTION", "Test Description");
        testIntent.putExtra("EVENT_LOCATION", "Test Location");
        testIntent.putExtra("EVENT_ORGANIZER", "Test Organizer");
        testIntent.putExtra("EVENT_START_TIME", System.currentTimeMillis());
        testIntent.putExtra("EVENT_END_TIME", System.currentTimeMillis() + 3600000);
        testIntent.putExtra("EVENT_STATUS", "Notified");
        testIntent.putExtra("USER_ID", "user123");
        testIntent.putExtra("USER_NAME", "Test User");
        testIntent.putExtra("USER_EMAIL", "test@example.com");
        testIntent.putExtra("USER_PHONE", "780-123-4567");
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    /**
     * Test that event information is displayed correctly
     * USER STORY 01.02.03
     */
    @Test
    public void testEventInformationDisplay() {
        ActivityScenario<InfoActivity> scenario = ActivityScenario.launch(testIntent);

        // Verify event details are displayed
        onView(withId(R.id.eventNameHeader))
                .check(matches(withText("Test Event")));

        onView(withId(R.id.eventDescriptionText))
                .check(matches(withText("Test Description")));

        onView(withId(R.id.eventLocationText))
                .check(matches(withText("Test Location")));

        onView(withId(R.id.eventOrganizerText))
                .check(matches(withText("Test Organizer")));

        onView(withId(R.id.statusBadge))
                .check(matches(withText("Notified")));

        scenario.close();
    }

    /**
     * Test that accept and decline buttons are visible
     * USER STORY 01.02.03
     */
    @Test
    public void testActionButtonsVisible() {
        ActivityScenario<InfoActivity> scenario = ActivityScenario.launch(testIntent);

        // Verify buttons are displayed and enabled
        onView(withId(R.id.acceptButton))
                .check(matches(isDisplayed()))
                .check(matches(isEnabled()));

        onView(withId(R.id.declineButton))
                .check(matches(isDisplayed()))
                .check(matches(isEnabled()));

        scenario.close();
    }

    /**
     * Test accepting an invitation
     * USER STORY 01.02.03
     */
    @Test
    public void testAcceptInvitation() {
        ActivityScenario<InfoActivity> scenario = ActivityScenario.launch(testIntent);

        // Click accept button
        onView(withId(R.id.acceptButton))
                .perform(click());

        // Confirm in dialog
        onView(withText("Accept"))
                .perform(click());

        // Status should update to "Accepted"
        onView(withId(R.id.statusBadge))
                .check(matches(withText("Accepted")));

        // Buttons should be disabled
        onView(withId(R.id.acceptButton))
                .check(matches(not(isEnabled())));

        onView(withId(R.id.declineButton))
                .check(matches(not(isEnabled())));

        scenario.close();
    }

    /**
     * Test declining an invitation
     * USER STORY 01.02.03
     */
    @Test
    public void testDeclineInvitation() {
        ActivityScenario<InfoActivity> scenario = ActivityScenario.launch(testIntent);

        // Click decline button
        onView(withId(R.id.declineButton))
                .perform(click());

        // Confirm in dialog
        onView(withText("Decline"))
                .perform(click());

        // Status should update to "Declined"
        onView(withId(R.id.statusBadge))
                .check(matches(withText("Declined")));

        // Buttons should be disabled
        onView(withId(R.id.acceptButton))
                .check(matches(not(isEnabled())));

        onView(withId(R.id.declineButton))
                .check(matches(not(isEnabled())));

        scenario.close();
    }

    /**
     * Test canceling accept dialog
     * USER STORY 01.02.03
     */
    @Test
    public void testCancelAcceptDialog() {
        ActivityScenario<InfoActivity> scenario = ActivityScenario.launch(testIntent);

        // Click accept button
        onView(withId(R.id.acceptButton))
                .perform(click());

        // Cancel dialog
        onView(withText("Cancel"))
                .perform(click());

        // Status should remain unchanged
        onView(withId(R.id.statusBadge))
                .check(matches(withText("Notified")));

        // Buttons should still be enabled
        onView(withId(R.id.acceptButton))
                .check(matches(isEnabled()));

        onView(withId(R.id.declineButton))
                .check(matches(isEnabled()));

        scenario.close();
    }

    /**
     * Test canceling decline dialog
     * USER STORY 01.02.03
     */
    @Test
    public void testCancelDeclineDialog() {
        ActivityScenario<InfoActivity> scenario = ActivityScenario.launch(testIntent);

        // Click decline button
        onView(withId(R.id.declineButton))
                .perform(click());

        // Cancel dialog
        onView(withText("Cancel"))
                .perform(click());

        // Status should remain unchanged
        onView(withId(R.id.statusBadge))
                .check(matches(withText("Notified")));

        // Buttons should still be enabled
        onView(withId(R.id.acceptButton))
                .check(matches(isEnabled()));

        scenario.close();
    }

    /**
     * Test back button navigation
     * USER STORY 01.02.03
     */
    @Test
    public void testBackButtonExists() {
        ActivityScenario<InfoActivity> scenario = ActivityScenario.launch(testIntent);

        // Verify back button is displayed
        onView(withId(R.id.backButton))
                .check(matches(isDisplayed()));

        scenario.close();
    }

    /**
     * Test accept dialog displays correct message
     * USER STORY 01.02.03
     */
    @Test
    public void testAcceptDialogMessage() {
        ActivityScenario<InfoActivity> scenario = ActivityScenario.launch(testIntent);

        // Click accept button
        onView(withId(R.id.acceptButton))
                .perform(click());

        // Verify dialog content
        onView(withText("Accept Invitation?"))
                .check(matches(isDisplayed()));

        onView(withText("Are you sure you want to accept this event invitation?"))
                .check(matches(isDisplayed()));

        // Cancel to clean up
        onView(withText("Cancel"))
                .perform(click());

        scenario.close();
    }

    /**
     * Test decline dialog displays correct message
     * USER STORY 01.02.03
     */
    @Test
    public void testDeclineDialogMessage() {
        ActivityScenario<InfoActivity> scenario = ActivityScenario.launch(testIntent);

        // Click decline button
        onView(withId(R.id.declineButton))
                .perform(click());

        // Verify dialog content
        onView(withText("Decline Invitation?"))
                .check(matches(isDisplayed()));

        onView(withText("Are you sure you want to decline this event invitation?"))
                .check(matches(isDisplayed()));

        // Cancel to clean up
        onView(withText("Cancel"))
                .perform(click());

        scenario.close();
    }
}
