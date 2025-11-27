package com.example.eventlottery;

import static androidx.test.InstrumentationRegistry.getTargetContext;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.CoreMatchers.anything;

import android.os.SystemClock;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.eventlottery.view.MainActivity;
import com.example.eventlottery.view.MapActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * UI test for the map activity.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class MapActivityUITest {
    @Rule
    public ActivityScenarioRule<MainActivity> scenario = new ActivityScenarioRule<>(MainActivity.class);

    /**
     * US 02.02.02, US02.02.03
     * Tests opening the google maps activity.
     * Assumes that as an organizer, your first event has geolocation on & has an entrant that joined.
     */
    @Test
    public void testOpenMaps() {
        // Accept notification permissions before running UI test
        getInstrumentation().getUiAutomation().grantRuntimePermission(
                getTargetContext().getPackageName(),
                android.Manifest.permission.POST_NOTIFICATIONS
        );

        // Initialize intents for espresso
        Intents.init();

        // Navigate to Organizer Panel
        onView(withId(R.id.OrganizerIcon)).perform(click());

        // Wait 2 seconds for events to load
        SystemClock.sleep(2000);

        // Click on first item in list
        onData(anything()).inAdapterView(withId(R.id.eventList)).atPosition(0).perform(click());

        // Click on map button
        onView(withId(R.id.mapButton)).perform(click());

        // Verify map activity opened
        intended(hasComponent(MapActivity.class.getName()));

        // Release intents
        Intents.release();
    }
}
