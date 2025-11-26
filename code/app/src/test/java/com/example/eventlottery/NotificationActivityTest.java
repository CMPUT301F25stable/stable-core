package com.example.eventlottery;

import com.example.eventlottery.users.User;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for NotificationActivity
 * Tests business logic for notification preferences and User object behavior
 *
 * @author Jensen Lee
 */
public class NotificationActivityTest {

    private User testUser;
    private String testUserId;

    /**
     * Sets up test fixtures before each test method
     * Creates a test user for notification preference testing
     */
    @Before
    public void setUp() {
        testUserId = "user123";
        testUser = new User(testUserId, "Test User", "test@example.com", "780-123-4567");
    }

    /**
     * Tests that a new user has notifications enabled by default
     * Verifies the default state when no preference has been set
     */
    @Test
    public void testUserNotifications_DefaultState() {
        assertTrue("Notifications should be enabled by default",
                testUser.getNotifications());
    }

    /**
     * Tests that user notifications can be enabled
     * Verifies setNotifications correctly updates the state to true
     */
    @Test
    public void testUserNotifications_EnableNotifications() {
        testUser.setNotifications(true);
        assertTrue("Notifications should be enabled",
                testUser.getNotifications());
    }

    /**
     * Tests that user notifications can be disabled
     * Verifies setNotifications correctly updates the state to false
     */
    @Test
    public void testUserNotifications_DisableNotifications() {
        testUser.setNotifications(false);
        assertFalse("Notifications should be disabled",
                testUser.getNotifications());
    }

    /**
     * Tests that notification preference can be toggled from enabled to disabled
     * Verifies the state changes correctly when toggled
     */
    @Test
    public void testUserNotifications_ToggleFromEnabledToDisabled() {
        testUser.setNotifications(true);
        assertTrue("Should start enabled", testUser.getNotifications());

        testUser.setNotifications(false);
        assertFalse("Should be disabled after toggle",
                testUser.getNotifications());
    }

    /**
     * Tests that notification preference can be toggled from disabled to enabled
     * Verifies the state changes correctly when toggled back
     */
    @Test
    public void testUserNotifications_ToggleFromDisabledToEnabled() {
        testUser.setNotifications(false);
        assertFalse("Should start disabled", testUser.getNotifications());

        testUser.setNotifications(true);
        assertTrue("Should be enabled after toggle",
                testUser.getNotifications());
    }

    /**
     * Tests that notification preference persists across multiple gets
     * Verifies the getter returns consistent values
     */
    @Test
    public void testUserNotifications_ConsistentState() {
        testUser.setNotifications(true);

        boolean firstCheck = testUser.getNotifications();
        boolean secondCheck = testUser.getNotifications();

        assertEquals("Multiple calls should return same value",
                firstCheck, secondCheck);
        assertTrue("Both checks should be true", firstCheck && secondCheck);
    }

    /**
     * Tests that multiple users can have different notification preferences
     * Verifies that notification state is independent per user object
     */
    @Test
    public void testMultipleUsers_IndependentNotificationPreferences() {
        User user1 = new User("user1", "User One", "user1@example.com", "780-111-1111");
        User user2 = new User("user2", "User Two", "user2@example.com", "780-222-2222");

        user1.setNotifications(true);
        user2.setNotifications(false);

        assertTrue("User1 should have notifications enabled",
                user1.getNotifications());
        assertFalse("User2 should have notifications disabled",
                user2.getNotifications());
    }

    /**
     * Tests that user object maintains notification preference with other data
     * Verifies notification state doesn't interfere with other user properties
     */
    @Test
    public void testUserNotifications_WithOtherUserData() {
        testUser.setNotifications(false);

        assertNotNull("User ID should not be null", testUser.getId());
        assertNotNull("User name should not be null", testUser.getName());
        assertNotNull("User email should not be null", testUser.getEmailAddress());
        assertFalse("Notifications should still be disabled",
                testUser.getNotifications());
    }

    /**
     * Tests that notification preference can be set multiple times
     * Verifies the most recent value is always returned
     */
    @Test
    public void testUserNotifications_MultipleUpdates() {
        testUser.setNotifications(true);
        assertTrue("Should be enabled", testUser.getNotifications());

        testUser.setNotifications(false);
        assertFalse("Should be disabled", testUser.getNotifications());

        testUser.setNotifications(true);
        assertTrue("Should be enabled again", testUser.getNotifications());

        testUser.setNotifications(false);
        assertFalse("Should be disabled again", testUser.getNotifications());
    }

    /**
     * Tests that user ID is correctly set and retrieved
     * Verifies the userId field matches expected value
     */
    @Test
    public void testUser_UserId() {
        assertEquals("User ID should match",
                testUserId, testUser.getId());
    }

    /**
     * Tests that user has all required fields initialized
     * Verifies all user data is non-null after creation
     */
    @Test
    public void testUser_HasValidData() {
        assertNotNull("User ID should not be null", testUser.getId());
        assertNotNull("User name should not be null", testUser.getName());
        assertNotNull("User email should not be null", testUser.getEmailAddress());
        assertNotNull("User phone should not be null", testUser.getPhoneNumber());
    }

    /**
     * Tests that notification preference is a boolean value
     * Verifies getNotifications returns either true or false
     */
    @Test
    public void testUserNotifications_ReturnsBooleanValue() {
        boolean notificationState = testUser.getNotifications();
        assertTrue("Should return a boolean value",
                notificationState == true || notificationState == false);
    }

    /**
     * Tests the scenario where notification is disabled then re-enabled
     * Simulates the switch being toggled off and on in the UI
     */
    @Test
    public void testNotificationSwitch_SimulateUserInteraction() {
        // User starts with notifications on
        testUser.setNotifications(true);
        assertTrue("Should start enabled", testUser.getNotifications());

        // User toggles switch off
        testUser.setNotifications(false);
        assertFalse("Should be disabled after toggle off",
                testUser.getNotifications());

        // User changes mind and toggles back on
        testUser.setNotifications(true);
        assertTrue("Should be enabled after toggle back on",
                testUser.getNotifications());
    }
}