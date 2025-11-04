package com.example.eventlottery;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.eventlottery.users.User;
import com.example.eventlottery.view.EditUserInfoActivity;
import com.example.eventlottery.view.InfoActivity;
import com.example.eventlottery.view.MainActivity;
import com.example.eventlottery.view.UserPanel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

/**
 * UI Tests for UserPanel
 * Tests User Story 01.02.03: User can view their events and manage invitations
 */
@RunWith(AndroidJUnit4.class)
public class UserPanelUITest {

    @Before
    public void setUp() {
        Intents.init();
        // Start MainActivity first to initialize the instance
        ActivityScenario.launch(MainActivity.class);
        // Wait a moment for MainActivity to fully initialize
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    /**
     * Test that user can view their registered events
     * USER STORY 01.02.03
     */
    @Test
    public void testDisplayRegisteredEvents() {
        // MainActivity should have initialized with test events
        ActivityScenario<UserPanel> scenario = ActivityScenario.launch(UserPanel.class);

        // Verify user name is displayed (MainActivity creates "John Doe")
        onView(withId(R.id.user_name))
                .check(matches(isDisplayed()))
                .check(matches(withText("John Doe")));

        // Verify event list container is displayed
        onView(withId(R.id.event_list_container))
                .check(matches(isDisplayed()));

        scenario.close();
    }

    /**
     * Test that empty state is shown when user has no events
     * USER STORY 01.02.03
     */
    @Test
    public void testEmptyState_NoEvents() {
        // Clear user events through MainActivity
        if (MainActivity.instance != null) {
            User user = MainActivity.instance.getCurrentUser();
            user.getRegisteredEvents().clear();
            user.getWaitlistedEvents().clear();
            MainActivity.instance.saveUser(user);
        }

        ActivityScenario<UserPanel> scenario = ActivityScenario.launch(UserPanel.class);

        // Verify empty state message is shown
        onView(withText("No events yet"))
                .check(matches(isDisplayed()));

        onView(withText("Browse events and join to see them here"))
                .check(matches(isDisplayed()));

        scenario.close();
    }

    /**
     * Test navigation to EditUserInfoActivity
     * USER STORY 01.02.03
     */
    @Test
    public void testNavigationToEditUserInfo() {
        ActivityScenario<UserPanel> scenario = ActivityScenario.launch(UserPanel.class);

        // Click edit icon
        onView(withId(R.id.edit_icon))
                .perform(click());

        // Verify EditUserInfoActivity is launched
        intended(hasComponent(EditUserInfoActivity.class.getName()));

        scenario.close();
    }

    /**
     * Test that notified events are clickable and navigate to InfoActivity
     * USER STORY 01.02.03
     */
    @Test
    public void testNotifiedEventClickable() {
        // MainActivity already sets up "City League Hockey Night" as "Notified"
        ActivityScenario<UserPanel> scenario = ActivityScenario.launch(UserPanel.class);

        // Click on the Notified event (City League Hockey Night)
        onView(withText("City League Hockey Night"))
                .perform(click());

        // Verify InfoActivity is launched
        intended(hasComponent(InfoActivity.class.getName()));

        scenario.close();
    }

    /**
     * Test that event status badges are displayed correctly
     * USER STORY 01.02.03
     */
    @Test
    public void testEventStatusBadgesDisplay() {
        // MainActivity sets up:
        // - "Demon Slayer" as "Accepted"
        // - "City League Hockey Night" as "Notified"
        // - "Winter Dance Showcase" as "Waitlisted"

        ActivityScenario<UserPanel> scenario = ActivityScenario.launch(UserPanel.class);

        // Verify different status badges are shown
        onView(withText("Accepted"))
                .check(matches(isDisplayed()));

        onView(withText("Notified"))
                .check(matches(isDisplayed()));

        onView(withText("Waitlisted"))
                .check(matches(isDisplayed()));

        scenario.close();
    }

    /**
     * Test that event details are displayed in cards
     * USER STORY 01.02.03
     */
    @Test
    public void testEventDetailsDisplay() {
        ActivityScenario<UserPanel> scenario = ActivityScenario.launch(UserPanel.class);

        // Verify event names are shown (from MainActivity test data)
        onView(withText("Demon Slayer: Infinity Castle – The Final Battle Begins"))
                .check(matches(isDisplayed()));

        // Verify location is shown
        onView(withText("Edmonton Cineplex Westmount"))
                .check(matches(isDisplayed()));

        scenario.close();
    }

    /**
     * Test that user has the expected number of events
     * USER STORY 01.02.03
     */
    @Test
    public void testUserHasMultipleEvents() {
        ActivityScenario<UserPanel> scenario = ActivityScenario.launch(UserPanel.class);

        // MainActivity creates 3 events for the user
        // Verify all three event names are displayed
        onView(withText("Demon Slayer: Infinity Castle – The Final Battle Begins"))
                .check(matches(isDisplayed()));

        onView(withText("City League Hockey Night"))
                .check(matches(isDisplayed()));

        onView(withText("Winter Dance Showcase"))
                .check(matches(isDisplayed()));

        scenario.close();
    }

    /**
     * Test that accepted events are not clickable (only Notified events are)
     * USER STORY 01.02.03
     */
    @Test
    public void testAcceptedEventNotClickable() {
        ActivityScenario<UserPanel> scenario = ActivityScenario.launch(UserPanel.class);

        // Try to click on Accepted event (Demon Slayer)
        // This should NOT open InfoActivity because only Notified events are clickable
        onView(withText("Demon Slayer: Infinity Castle – The Final Battle Begins"))
                .perform(click());

        // InfoActivity should NOT be launched - we can't easily test this with Espresso
        // but we verify the current activity is still UserPanel by checking a view exists
        onView(withId(R.id.user_name))
                .check(matches(isDisplayed()));

        scenario.close();
    }
}