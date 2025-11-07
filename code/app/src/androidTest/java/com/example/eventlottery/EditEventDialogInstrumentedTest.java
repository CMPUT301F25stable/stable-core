package com.example.eventlottery;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
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
 * UI tests for the edit event dialog class.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class EditEventDialogInstrumentedTest {
    @Rule
    public ActivityScenarioRule<MainActivity> scenario = new ActivityScenarioRule<>(MainActivity.class);

    /**
     * Tests editing an event with successful inputs. Assumes you have at least 1 event.
     * Verifies if the dialog closes.
     */
    @Test
    public void testSuccessfulEditEvent() {
        // Navigate to Organizer Panel
        onView(withId(R.id.OrganizerIcon)).perform(click());

        // Wait 2 seconds for events to load
        SystemClock.sleep(2000);

        // Click on first item in list
        onData(anything()).inAdapterView(withId(R.id.eventList)).atPosition(0).perform(click());

        // Click edit event
        onView(withId(R.id.editEventButton)).perform(click());

        // Type in valid inputs
        onView(withId(R.id.waitlistMaxInput)).perform(replaceText("20"), closeSoftKeyboard());
        onView(withId(R.id.startDateInput)).perform(typeText("2025-01-01"));
        onView(withId(R.id.endDateInput)).perform(typeText("2025-01-30"));

        // Save event
        onView(withText("Save")).perform(click());

        // Verify dialog closed
        onView(withText("Save")).check(doesNotExist());
    }
}
