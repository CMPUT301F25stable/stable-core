package com.example.eventlottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.containsString;

import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.eventlottery.view.EventJoinAndLeave;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests getWaitListSize() in EventJoinAndLeave
 * Tests if the waitlist size is on screen
 */
@RunWith(AndroidJUnit4.class)
public class WaitListSizeTest {

    /**
     * Note: waitListSize() will only be displayed with a valid eventID. Below is a hardcoded
     * existing eventID from firebase with placeholder values. The eventID must be in firebase
     * in order to pass
     */
    @Rule
    public ActivityScenarioRule<EventJoinAndLeave> scenarioRule =
            new ActivityScenarioRule<>(new Intent(ApplicationProvider.getApplicationContext(), EventJoinAndLeave.class)
                    .putExtra("id", "4b89c209-33e7-4d2f-8b5f-2b3b0a569784") // This is hard coded
                    .putExtra("name", "placeholder")
                    .putExtra("description", "placeholder")
                    .putExtra("dateStart", "placeholder")
                    .putExtra("timeStart","placeholder")
                    .putExtra("dateEnd", "placeholder")
                    .putExtra("timeEnd", "placeholder")
                    .putExtra("location", "placeholder")
                    .putExtra("organizer", "placeholder")
                    .putExtra("image", "placeholder")
                    .putExtra("waitlistMax", 1));

    @Test
    public void testCheckWaitListSize() throws InterruptedException {

        // Check we in the right activity
        onView(withId(R.id.recycler_view)).check(ViewAssertions.doesNotExist());

        // Check EventJoinAndLeave UI
        onView(withId(R.id.imageView)).check(ViewAssertions.matches(isDisplayed()));
        onView(withId(R.id.eventTitle)).check(matches(withText("placeholder")));
        onView(withId(R.id.eventDetails)).check(ViewAssertions.matches(isDisplayed()));

        // Wait for db load
        Thread.sleep(2000);

        // Will be displayed with a valid eventID from firebase
        onView(withId(R.id.eventDetails)).check(matches(withText(containsString("Waitlist: "))));
    }
}
