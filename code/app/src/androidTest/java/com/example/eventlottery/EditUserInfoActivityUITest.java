package com.example.eventlottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Intent; // ✅ added
import androidx.test.core.app.ActivityScenario; // ✅ added
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.eventlottery.view.EditUserInfoActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * UI Tests for EditUserInfoActivity
 * Tests editing and saving user information
 */
@RunWith(AndroidJUnit4.class)
public class EditUserInfoActivityUITest {

    @Rule
    public ActivityScenarioRule<EditUserInfoActivity> scenarioRule =
            new ActivityScenarioRule<>(EditUserInfoActivity.class);

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
    public void testEnterValidUserInfo() {
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
     */
    @Test
    public void testConfirmButtonWithValidData() {
        // Enter valid data
        onView(withId(R.id.name_edit_text))
                .perform(replaceText("Jane Smith"), closeSoftKeyboard());

        onView(withId(R.id.email_edit_text))
                .perform(replaceText("jane@example.com"), closeSoftKeyboard());

        // Click confirm button
        onView(withId(R.id.confirm_btn))
                .perform(scrollTo(), click());

        // Note: Testing toast messages requires additional custom matchers
        // or waiting mechanisms, which are complex with just Espresso
    }

    /**
     * Test clicking confirm button with empty name
     */
    @Test
    public void testConfirmButtonWithEmptyName() {
        // Clear name field
        onView(withId(R.id.name_edit_text))
                .perform(replaceText(""), closeSoftKeyboard());

        // Enter email
        onView(withId(R.id.email_edit_text))
                .perform(replaceText("test@example.com"), closeSoftKeyboard());

        // Click confirm
        onView(withId(R.id.confirm_btn))
                .perform(scrollTo(), click());

        // App should show toast (testing this requires custom toast matchers)
        // Fields should still be visible (activity doesn't close)
        onView(withId(R.id.name_edit_text))
                .check(matches(isDisplayed()));
    }

    /**
     * Test clicking confirm button with empty email
     */
    @Test
    public void testConfirmButtonWithEmptyEmail() {
        // Enter name
        onView(withId(R.id.name_edit_text))
                .perform(replaceText("John Doe"), closeSoftKeyboard());

        // Clear email
        onView(withId(R.id.email_edit_text))
                .perform(replaceText(""), closeSoftKeyboard());

        // Click confirm
        onView(withId(R.id.confirm_btn))
                .perform(scrollTo(), click());

        // Fields should still be visible
        onView(withId(R.id.email_edit_text))
                .check(matches(isDisplayed()));
    }

    /**
     * Test phone number is optional
     */
    @Test
    public void testPhoneNumberIsOptional() {
        // Enter name and email only
        onView(withId(R.id.name_edit_text))
                .perform(replaceText("Jane Doe"), closeSoftKeyboard());

        onView(withId(R.id.email_edit_text))
                .perform(replaceText("jane@example.com"), closeSoftKeyboard());

        // Leave phone empty
        onView(withId(R.id.phoneNum_edit_text))
                .perform(replaceText(""), closeSoftKeyboard());

        // Click confirm - should work
        onView(withId(R.id.confirm_btn))
                .perform(scrollTo(), click());
    }

    /**
     * Test delete account button shows confirmation dialog
     */
    @Test
    public void testDeleteAccountDialogAppears() {
        // Click delete button
        onView(withId(R.id.delete_account_btn))
                .perform(scrollTo(), click());

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
                .perform(scrollTo(), click());

        // Click No
        onView(withText("No"))
                .perform(click());

        // Activity should still be active - verify fields still visible
        onView(withId(R.id.name_edit_text))
                .check(matches(isDisplayed()));
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
    public void testEditExistingInfo() {
        // Type new information
        onView(withId(R.id.name_edit_text))
                .perform(replaceText("Updated Name"), closeSoftKeyboard());

        // Verify the text was updated
        onView(withId(R.id.name_edit_text))
                .check(matches(withText("Updated Name")));
    }
}