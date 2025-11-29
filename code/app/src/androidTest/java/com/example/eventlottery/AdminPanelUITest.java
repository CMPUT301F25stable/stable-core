package com.example.eventlottery;

import static androidx.test.espresso.Espresso.onData;
import static org.hamcrest.CoreMatchers.anything;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.InstrumentationRegistry.getTargetContext;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

import android.os.SystemClock;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.eventlottery.events.DBConnector;
import com.example.eventlottery.view.AdminPanel;
import com.example.eventlottery.view.MainActivity;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;

/**
 * UI test for the Admin Panel.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AdminPanelUITest {
    @Rule
    public ActivityScenarioRule<MainActivity> scenario = new ActivityScenarioRule<>(MainActivity.class);

    /**
     * Initialize intents for espresso before each test
     */
    @Before
    public void setUp() throws Exception {
        // Accept notification permissions before running UI test
        getInstrumentation().getUiAutomation().grantRuntimePermission(
                getTargetContext().getPackageName(),
                android.Manifest.permission.POST_NOTIFICATIONS
        );

        // Change user to admin
        changeUserToAdmin();

        Intents.init();
    }

    /**
     * Release intents after each test
     */
    @After
    public void tearDown() throws Exception {
        Intents.release();
    }

    /**
     * US 03.04.01, US03.05.01
     * Tests opening the AdminPanel
     * Assumes that as a user, you are an admin
     */
    @Test
    public void testOpenAdminPanel() {
        // Wait for AdminPanel to load
        SystemClock.sleep(1000);

        // Navigate to AdminPanel
        onView(withId(R.id.adminPanelIcon)).perform(click());

        // Verify AdminPanel activity opened
        intended(hasComponent(AdminPanel.class.getName()));
    }

    /**
     * US03.01.01
     * Tests removing an event
     * Assumes that as a user, you are an admin, and there is at least one event in the database
     */
    @Test
    public void testRemoveEvent() {
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
    }

    /**
     * US 03.02.01
     * Tests deleting a user
     */
    @Test
    public void deleteUserTest() {
        // Wait for AdminPanel to load
        SystemClock.sleep(3000);

        // Navigate to AdminPanel
        onView(withId(R.id.adminPanelIcon)).perform(click());

        // Click on first item in event list
        onData(anything())
                .inAdapterView(withId(R.id.userList))
                .atPosition(0)
                .perform(click());

        // Wait for event details to load
        SystemClock.sleep(3000);

        // Test if the delete option is there
        onView(withText("Delete Account")).check(matches(isDisplayed()));

        // Click delete option
        onView(withText("Delete Account")).perform(click());

        // Tests confirm message
        onView(withText("Yes")).check(matches(isDisplayed()));
        onView(withText("No")).check(matches(isDisplayed()));

        // Click No option
        onView(withText("No")).perform(click());

        // Click Yes option
        // Note: this is commented out for now since I don't want to accidentally delete a team
        // members doc. If you want to test, comment out Click No option above and uncomment
        // below and see firestore for changes
        //onView(withText("Yes")).perform(click());
    }

    /**
     * Changes the test emulator device into an Admin"
     */
    private void changeUserToAdmin() throws ExecutionException, InterruptedException {
        // Get the ID
        DBConnector db = new DBConnector(ApplicationProvider.getApplicationContext());
        String userID = db.getUserId();

        // Change test user to admin
        DocumentReference reference = db.getUserDoc(userID);
        Tasks.await(reference.update("admin", true));

        // Check if user info changed
        DocumentSnapshot documentSnapshot = Tasks.await(db.getUserDoc(userID).get());
        assertNotNull(documentSnapshot);
        assertTrue(documentSnapshot.exists());
        assertEquals(Boolean.TRUE, documentSnapshot.getBoolean("admin"));
    }
}
