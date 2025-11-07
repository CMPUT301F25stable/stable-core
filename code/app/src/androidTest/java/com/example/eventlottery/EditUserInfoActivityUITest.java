package com.example.eventlottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.eventlottery.events.DBConnector;
import com.example.eventlottery.view.EditUserInfoActivity;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;

/**
 * UI Tests for EditUserInfoActivity
 * Tests editing and saving user information
 * Reference: https://stackoverflow.com/questions/63905160/android-tasks-await-blocks-ui-and-anr
 */
@RunWith(AndroidJUnit4.class)
public class EditUserInfoActivityUITest {
    private static final String MOCK_ID = "MockUser";
    DBConnector db;

    @Rule
    public ActivityScenarioRule<EditUserInfoActivity> scenarioRule =
            new ActivityScenarioRule<>(new Intent(ApplicationProvider.getApplicationContext(), EditUserInfoActivity.class)
                    .putExtra("mockID", MOCK_ID));

    @Before
    public void setUp() {
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    /**
     * Test that user info fields are displayed
     */
    @Test
    public void testUserInfoFieldsDisplay() {
        onView(withId(R.id.name_edit_text))
                .check(matches(isDisplayed()));

        onView(withId(R.id.email_edit_text))
                .check(matches(isDisplayed()));

        onView(withId(R.id.phoneNum_edit_text))
                .check(matches(isDisplayed()));

        onView(withId(R.id.confirm_btn))
                .check(matches(isDisplayed()));

        onView(withId(R.id.delete_account_btn))
                .check(matches(isDisplayed()));
    }

    /**
     * Test that back button is displayed
     */
    @Test
    public void testBackButtonDisplay() {
        onView(withId(R.id.back_btn))
                .check(matches(isDisplayed()));
    }

    /**
     * Test entering valid user information
     */
    @Test
    public void testEnterValidUserInfo() throws InterruptedException {
        // wait for db to load info
        Thread.sleep(2000);

        // Enter valid data
        onView(withId(R.id.name_edit_text))
                .perform(replaceText("John Doe"), closeSoftKeyboard());

        onView(withId(R.id.email_edit_text))
                .perform(replaceText("john.doe@example.com"), closeSoftKeyboard());

        onView(withId(R.id.phoneNum_edit_text))
                .perform(replaceText("780-123-4567"), closeSoftKeyboard());

        // Verify text was entered
        onView(withId(R.id.name_edit_text))
                .check(matches(withText("John Doe")));

        onView(withId(R.id.email_edit_text))
                .check(matches(withText("john.doe@example.com")));

        onView(withId(R.id.phoneNum_edit_text))
                .check(matches(withText("780-123-4567")));
    }

    /**
     * Test clicking confirm button with valid data
     * Note: This also tests phone number optionality
     */
    @Test
    public void testConfirmButtonWithValidData() throws InterruptedException, ExecutionException {
        // wait for db to load info
        Thread.sleep(2000);

        // Enter valid data
        onView(withId(R.id.name_edit_text))
                .perform(replaceText("Mock"), closeSoftKeyboard());

        onView(withId(R.id.email_edit_text))
                .perform(replaceText("Mock@example.com"), closeSoftKeyboard());

        // Click confirm button
        onView(withId(R.id.confirm_btn)).perform(click());

        // connection and snapshot
        db = new DBConnector(ApplicationProvider.getApplicationContext());
        DocumentSnapshot documentSnapshot = Tasks.await(db.getUserDoc(MOCK_ID).get());

        // Check if name and email matches
        assertNotNull(documentSnapshot);
        assertTrue(documentSnapshot.exists());
        assertEquals("Mock", documentSnapshot.getString("name"));
        assertEquals("Mock@example.com", documentSnapshot.getString("emailAddress"));
    }

    /**
     * Test clicking confirm button with empty name
     */
    @Test
    public void testConfirmButtonWithEmptyName() throws InterruptedException, ExecutionException {
        // wait for db to load info
        Thread.sleep(2000);

        // Clear name field
        onView(withId(R.id.name_edit_text))
                .perform(replaceText(""), closeSoftKeyboard());

        // Enter email
        onView(withId(R.id.email_edit_text))
                .perform(replaceText("test@example.com"), closeSoftKeyboard());

        // Click confirm
        onView(withId(R.id.confirm_btn))
                .perform(click());

        // App should show toast (testing this requires custom toast matchers)
        // Fields should still be visible (activity doesn't close)
        onView(withId(R.id.name_edit_text))
                .check(matches(isDisplayed()));

        // connection and snapshot
        db = new DBConnector(ApplicationProvider.getApplicationContext());
        DocumentSnapshot documentSnapshot = Tasks.await(db.getUserDoc(MOCK_ID).get());

        // Check if name and email are not updated (Since both are required to update)
        assertNotNull(documentSnapshot);
        assertTrue(documentSnapshot.exists());
        assertNotEquals("", documentSnapshot.getString("name"));
        assertNotEquals("test@example.com", documentSnapshot.getString("emailAddress"));
    }

    /**
     * Test clicking confirm button with empty email
     */
    @Test
    public void testConfirmButtonWithEmptyEmail() throws InterruptedException, ExecutionException {
        // wait for db to load info
        Thread.sleep(2000);

        // Enter name
        onView(withId(R.id.name_edit_text))
                .perform(replaceText("John Doe"), closeSoftKeyboard());

        // Clear email
        onView(withId(R.id.email_edit_text))
                .perform(replaceText(""), closeSoftKeyboard());

        // Click confirm
        onView(withId(R.id.confirm_btn)).perform(click());

        // Fields should still be visible
        onView(withId(R.id.email_edit_text))
                .check(matches(isDisplayed()));

        // connection and snapshot
        db = new DBConnector(ApplicationProvider.getApplicationContext());
        DocumentSnapshot documentSnapshot = Tasks.await(db.getUserDoc(MOCK_ID).get());

        // Check if name and email are not updated (Since both are required to update)
        assertNotNull(documentSnapshot);
        assertTrue(documentSnapshot.exists());
        assertNotEquals("John Doe", documentSnapshot.getString("name"));
        assertNotEquals("", documentSnapshot.getString("emailAddress"));
    }

    /**
     * Test phone number is optional
     * Note: In testConfirmButtonWithValidData() we tested that the phone number was
     * optional and only updated name and email, here we add a phone number
     */
    @Test
    public void testPhoneNumberIsOptional() throws InterruptedException, ExecutionException {
        // wait for db to load info
        Thread.sleep(2000);

        // Enter name and email only
        onView(withId(R.id.name_edit_text))
                .perform(replaceText("Jane Doe"), closeSoftKeyboard());

        onView(withId(R.id.email_edit_text))
                .perform(replaceText("jane@example.com"), closeSoftKeyboard());

        // Leave phone empty
        onView(withId(R.id.phoneNum_edit_text))
                .perform(replaceText("123-456-7890"), closeSoftKeyboard());

        // Click confirm - should work
        onView(withId(R.id.confirm_btn))
                .perform(click());

        // connection and snapshot
        db = new DBConnector(ApplicationProvider.getApplicationContext());
        DocumentSnapshot documentSnapshot = Tasks.await(db.getUserDoc(MOCK_ID).get());

        // Check if name and email matches
        assertNotNull(documentSnapshot);
        assertTrue(documentSnapshot.exists());
        assertEquals("Jane Doe", documentSnapshot.getString("name"));
        assertEquals("jane@example.com", documentSnapshot.getString("emailAddress"));
        assertEquals("123-456-7890", documentSnapshot.getString("phoneNumber"));
    }

    /**
     * Test delete account button shows confirmation dialog
     */
    @Test
    public void testDeleteAccountDialogAppears() {
        // Click delete button
        onView(withId(R.id.delete_account_btn))
                .perform(click());

        // Verify dialog appears
        onView(withText("This Will Delete Your Account"))
                .check(matches(isDisplayed()));

        onView(withText("Are You Sure?"))
                .check(matches(isDisplayed()));

        onView(withText("Yes"))
                .check(matches(isDisplayed()));

        onView(withText("No"))
                .check(matches(isDisplayed()));
    }

    /**
     * Test canceling delete account
     */
    @Test
    public void testCancelDeleteAccount() {
        // Click delete button
        onView(withId(R.id.delete_account_btn))
                .perform(click());

        // Click No
        onView(withText("No"))
                .perform(click());

        // Activity should still be active - verify fields still visible
        onView(withId(R.id.name_edit_text))
                .check(matches(isDisplayed()));
    }

    /**
     * Test the Delete Button on MockUser
     */
    @Test
    public void testDeleteAccountWithMockUser() throws InterruptedException, ExecutionException {
        // connection
        db = new DBConnector(ApplicationProvider.getApplicationContext());

        // Check if MockUser is still in the db
        DocumentSnapshot snapshot = Tasks.await(db.getUserDoc(MOCK_ID).get());
        assertNotNull(snapshot);
        assertTrue(snapshot.exists());

        // Click delete button
        onView(withId(R.id.delete_account_btn))
                .perform(click());

        // Click Yes
        onView(withText("Yes"))
                .perform(click());

        // Check if MockUser is deleted
        snapshot = Tasks.await(db.getUserDoc(MOCK_ID).get());
        assertNotNull(snapshot);
        assertFalse(snapshot.exists());
    }

    /**
     * Test all buttons are clickable
     */
    @Test
    public void testAllButtonsClickable() {
        onView(withId(R.id.confirm_btn))
                .check(matches(isEnabled()));

        onView(withId(R.id.delete_account_btn))
                .check(matches(isEnabled()));

        onView(withId(R.id.back_btn))
                .check(matches(isEnabled()));
    }

    /**
     * Test editing existing user information
     */
    @Test
    public void testEditExistingInfo() throws InterruptedException {
        // wait for db to load info
        Thread.sleep(2000);

        // Type new information
        onView(withId(R.id.name_edit_text))
                .perform(replaceText("Updated Name"), closeSoftKeyboard());

        // Verify the text was updated
        onView(withId(R.id.name_edit_text))
                .check(matches(withText("Updated Name")));
    }
}