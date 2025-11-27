package com.example.eventlottery;

import static androidx.test.InstrumentationRegistry.getTargetContext;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

import static org.hamcrest.CoreMatchers.anything;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import androidx.test.filters.LargeTest;
import com.example.eventlottery.view.MainActivity;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.OutputStream;

/**
 * UI tests for the CreateEventDialog class.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class CreateEventDialogInstrumentedTest {
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
     * Tests creating an event. Verifies if the dialog actually closes.
     */
    @Test
    public void testSuccessfulCreateEvent() throws IOException {
        // Wait
        SystemClock.sleep(1000);

        // Navigate to Organizer Panel
        onView(withId(R.id.OrganizerIcon)).perform(click());

        // Wait
        SystemClock.sleep(1000);

        // Click create event
        onView(withId(R.id.createEventButton)).perform(click());

        // Type in valid inputs
        onView(withId(R.id.titleInput)).perform(typeText("Test Event"));
        onView(withId(R.id.descriptionInput)).perform(typeText("Test Event"));
        onView(withId(R.id.locationInput)).perform(typeText("Test Event"));
        onView(withId(R.id.waitlistMaxInput)).perform(typeText("20"));

        onView(withId(R.id.startDateInput)).perform(click());
        onView(withText("OK")).perform(click());
        SystemClock.sleep(1000);

        onView(withId(R.id.endDateInput)).perform(click());
        onView(withText("OK")).perform(click());
        SystemClock.sleep(1000);

        // Creates an imitation of a user selecting an image so that it is successful,
        // since espresso can't select an image from the devices storage
        Intent resultData = new Intent();
        Uri testImage = createTestImage(ApplicationProvider.getApplicationContext());
        resultData.setData(testImage);
        Instrumentation.ActivityResult result =
                new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);
        intending(hasAction(Intent.ACTION_PICK)).respondWith(result);

        // Wait
        SystemClock.sleep(2000);

        onView(withId(R.id.select_image_btn)).perform(click());

        // Wait for selection
        SystemClock.sleep(1000);

        onView(withId(R.id.upload_image_btn)).perform(click());

        // Wait for image upload
        SystemClock.sleep(2000);

        // Save event
        onView(withText("Save")).perform(click());

        // Verify dialog closed
        onView(withText("Save")).check(doesNotExist());
    }

    /**
     * Tests exporting the final list of users as CSV file
     * Assumes that an event has been made and there are users in the list
     */
    @Test
    public void testExportCSV() {
        // Wait
        SystemClock.sleep(1000);

        // Navigate to Organizer Panel
        onView(withId(R.id.OrganizerIcon)).perform(click());

        // Wait
        SystemClock.sleep(1000);

        // Click on first item in event list
        onData(anything())
                .inAdapterView(withId(R.id.eventList))
                .atPosition(0)
                .perform(click());

        // Click view final list
        onView(withId(R.id.closeButton)).perform(click());

        // Wait
        SystemClock.sleep(1000);

        // Click view final list
        onView(withId(R.id.view_finalized_list_button)).perform(click());

        // Click export button
        onView(withId(R.id.export_btn)).perform(click());

        // Wait for file to write then you can check on the device explorer
        SystemClock.sleep(1000);
    }

    /**
     * references: <a href="https://medium.com/%40h2osolid/android-how-to-provide-photos-to-the-gallery-for-running-ui-automated-tests-37a232c0668d">...</a>
     * <a href="https://stackoverflow.com/questions/28019657/camera-operation-ui-testing-with-espresso?utm_source=chatgpt.com">...</a>
     * and ChatGPT to help find errors
     * Creates an image to the devices gallery so it can be used in the creation
     * of a new Event.
     */
    private Uri createTestImage(Context context) throws IOException {
        Bitmap bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(Color.BLUE);

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, "test_image.jpg");
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

        Uri uri = context.getContentResolver()
                .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Assert.assertNotNull(uri);
        OutputStream out = context.getContentResolver().openOutputStream(uri);
        Assert.assertNotNull(out);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        out.close();

        return uri;
    }
}