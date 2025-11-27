package com.example.eventlottery;

import static androidx.test.espresso.Espresso.onData;
import static org.hamcrest.CoreMatchers.anything;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.InstrumentationRegistry.getTargetContext;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

import android.os.SystemClock;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.eventlottery.view.AdminPanel;
import com.example.eventlottery.view.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * UI test for the Admin Panel.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AdminPanelUITest {
    @Rule
    public ActivityScenarioRule<MainActivity> scenario = new ActivityScenarioRule<>(MainActivity.class);

    /**
     * US 03.04.01, US03.05.01
     * Tests opening the AdminPanel
     * Assumes that as a user, you are an admin
     */
    @Test
    public void testOpenAdminPanel() {
        // Accept notification permissions before running UI test
        getInstrumentation().getUiAutomation().grantRuntimePermission(
                getTargetContext().getPackageName(),
                android.Manifest.permission.POST_NOTIFICATIONS
        );

        // Initialize intents for espresso
        Intents.init();

        // Wait for AdminPanel to load
        SystemClock.sleep(1000);

        // Navigate to AdminPanel
        onView(withId(R.id.adminPanelIcon)).perform(click());

        // Verify AdminPanel activity opened
        intended(hasComponent(AdminPanel.class.getName()));

        // Release intents
        Intents.release();
    }

    /**
     * US03.01.01
     * Tests removing an event
     * Assumes that as a user, you are an admin, and there is at least one event in the database
     */
    @Test
    public void testRemoveEvent() {
        // Accept notification permissions before running UI test
        getInstrumentation().getUiAutomation().grantRuntimePermission(
                getTargetContext().getPackageName(),
                android.Manifest.permission.POST_NOTIFICATIONS
        );

        // Initialize intents for espresso
        Intents.init();

        // Wait for AdminPanel to load
        SystemClock.sleep(3000);

        // Navigate to AdminPanel
        onView(withId(R.id.adminPanelIcon)).perform(click());

        // Click on first item in event list
        onData(anything())
                .inAdapterView(withId(R.id.eventList))
                .atPosition(0)
                .perform(click());

        // Wait for event details to load
        SystemClock.sleep(3000);

        // Click delete button
        onView(withId(R.id.adminDeleteButton)).perform(click());

        // Release intents
        Intents.release();
    }
}
