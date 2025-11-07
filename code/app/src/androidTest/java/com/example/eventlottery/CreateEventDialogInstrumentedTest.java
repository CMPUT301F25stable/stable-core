package com.example.eventlottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import androidx.test.filters.LargeTest;
import com.example.eventlottery.view.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * UI tests for the CreateEventDialog class.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class CreateEventDialogInstrumentedTest {
    @Rule
    public ActivityScenarioRule<MainActivity> scenario = new ActivityScenarioRule<>(MainActivity.class);

    /**
     * Tests creating an event. Verifies if the dialog actually closes.
     */
    @Test
    public void testSuccessfulCreateEvent() {
        // Navigate to Organizer Panel
        onView(withId(R.id.OrganizerIcon)).perform(click());

        // Click create event
        onView(withId(R.id.createEventButton)).perform(click());

        // Type in valid inputs
        onView(withId(R.id.waitlistMaxInput)).perform(typeText("20"));
        onView(withId(R.id.startDateInput)).perform(typeText("2025-01-01"));
        onView(withId(R.id.endDateInput)).perform(typeText("2025-01-30"));

        // Save event
        onView(withText("Save")).perform(click());

        // Verify dialog closed
        onView(withText("Save")).check(doesNotExist());
    }
}