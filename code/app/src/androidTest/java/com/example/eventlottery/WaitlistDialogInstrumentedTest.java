package com.example.eventlottery;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.anything;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import android.os.SystemClock;

import androidx.test.filters.LargeTest;

import com.example.eventlottery.view.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * UI tests for the WaitlistDialog class.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class WaitlistDialogInstrumentedTest {
    @Rule
    public ActivityScenarioRule<MainActivity> scenario = new ActivityScenarioRule<>(MainActivity.class);

    /**
     * Tests if viewing the waitlist actually works. Assumes you have at least one event created.
     * Verifies if the dialog closes.
     */
    @Test
    public void testViewWaitlist() {
        // Navigate to Organizer Panel
        onView(withId(R.id.OrganizerIcon)).perform(click());

        // Wait 2 seconds for events to load
        SystemClock.sleep(2000);

        // Click on first item in list
        onData(anything()).inAdapterView(withId(R.id.eventList)).atPosition(0).perform(click());

        // Click view waitlist
        onView(withId(R.id.viewWaitlistButton)).perform(click());

        // Close waitlist
        onView(withText("Close")).perform(click());

        // Verify dialog closed
        onView(withText("Close")).check(doesNotExist());
    }
}