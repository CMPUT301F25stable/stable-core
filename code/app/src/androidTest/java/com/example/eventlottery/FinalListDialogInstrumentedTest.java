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
 * UI tests for the FinalListDialog class.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class FinalListDialogInstrumentedTest {
    @Rule
    public ActivityScenarioRule<MainActivity> scenario = new ActivityScenarioRule<>(MainActivity.class);

    /**
     * Tests if the FinalizedList of an event is displayed. Assuming at least one event is created
     * by the user
     */
    @Test
    public void testViewFinalList() {
        onView(withId(R.id.OrganizerIcon)).perform(click());

        // Wait for events to load
        SystemClock.sleep(2000);

        onData(anything()).inAdapterView(withId(R.id.eventList)).atPosition(0).perform(click());

        onView(withId(R.id.view_finalized_list_button)).perform(click());

        onView(withText("Close")).perform(click());

        // Verify dialog closed
        onView(withText("Close")).check(doesNotExist());
    }
}