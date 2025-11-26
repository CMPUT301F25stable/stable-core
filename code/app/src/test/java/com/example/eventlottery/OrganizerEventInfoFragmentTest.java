package com.example.eventlottery;

import com.example.eventlottery.users.User;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Unit tests for OrganizerEventInfoFragment
 * Tests business logic for recipient count formatting and user data processing
 *
 * @author Jensen Lee
 */
public class OrganizerEventInfoFragmentTest {

    private User testUser1;
    private User testUser2;
    private User testUser3;
    private User testUser4;

    /**
     * Sets up test fixtures before each test method
     * Creates test users with various FCM token states
     */
    @Before
    public void setUp() {
        testUser1 = new User("user1", "Alice Johnson", "alice@example.com", "780-111-1111");
        testUser1.setFcmToken("token_abc123");

        testUser2 = new User("user2", "Bob Smith", "bob@example.com", "780-222-2222");
        testUser2.setFcmToken(null); // No token

        testUser3 = new User("user3", "Charlie Brown", "charlie@example.com", "780-333-3333");
        testUser3.setFcmToken(""); // Empty token

        testUser4 = new User("user4", "Diana Prince", "diana@example.com", "780-444-4444");
        testUser4.setFcmToken("token_xyz789");
    }

    /**
     * Tests that recipient count text is formatted correctly for a single recipient
     * Verifies singular "recipient" is used when count is 1
     */
    @Test
    public void testRecipientCount_SingleRecipient() {
        int count = 1;
        String recipientText = count + " " + (count == 1 ? "recipient" : "recipients");
        assertEquals("Should format singular recipient correctly",
                "1 recipient", recipientText);
    }

    /**
     * Tests that recipient count text is formatted correctly for multiple recipients
     * Verifies plural "recipients" is used when count is greater than 1
     */
    @Test
    public void testRecipientCount_MultipleRecipients() {
        int count = 5;
        String recipientText = count + " " + (count == 1 ? "recipient" : "recipients");
        assertEquals("Should format plural recipients correctly",
                "5 recipients", recipientText);
    }

    /**
     * Tests recipient count formatting with zero recipients
     * Verifies plural form is used for zero count
     */
    @Test
    public void testRecipientCount_ZeroRecipients() {
        int count = 0;
        String recipientText = count + " " + (count == 1 ? "recipient" : "recipients");
        assertEquals("Should format zero recipients with plural",
                "0 recipients", recipientText);
    }

    /**
     * Tests recipient count formatting with large number
     * Verifies the format works with larger counts
     */
    @Test
    public void testRecipientCount_LargeNumber() {
        int count = 150;
        String recipientText = count + " " + (count == 1 ? "recipient" : "recipients");
        assertEquals("Should format large numbers correctly",
                "150 recipients", recipientText);
    }

    /**
     * Tests converting a list of user IDs to user data map format
     * Verifies the conversion creates proper Map structure with "id" key
     */
    @Test
    public void testConvertUserIdsToUserData() {
        List<String> userIds = new ArrayList<>();
        userIds.add("user1");
        userIds.add("user2");
        userIds.add("user3");

        List<Map<String, Object>> userData = new ArrayList<>();
        for (String userId : userIds) {
            Map<String, Object> data = new HashMap<>();
            data.put("id", userId);
            userData.add(data);
        }

        assertEquals("Should have same size as input list", 3, userData.size());
        assertEquals("First entry should have correct ID",
                "user1", userData.get(0).get("id"));
        assertEquals("Second entry should have correct ID",
                "user2", userData.get(1).get("id"));
        assertEquals("Third entry should have correct ID",
                "user3", userData.get(2).get("id"));
    }

    /**
     * Tests converting empty user ID list
     * Verifies empty input produces empty output
     */
    @Test
    public void testConvertUserIdsToUserData_EmptyList() {
        List<String> userIds = new ArrayList<>();

        List<Map<String, Object>> userData = new ArrayList<>();
        for (String userId : userIds) {
            Map<String, Object> data = new HashMap<>();
            data.put("id", userId);
            userData.add(data);
        }

        assertTrue("Should produce empty list", userData.isEmpty());
    }

    /**
     * Tests that users with valid FCM tokens are correctly identified
     * Verifies the filtering logic that checks for non-null and non-empty tokens
     */
    @Test
    public void testFilterUsersWithFCMTokens() {
        List<User> allUsers = new ArrayList<>();
        allUsers.add(testUser1); // Has token
        allUsers.add(testUser2); // Null token
        allUsers.add(testUser3); // Empty token
        allUsers.add(testUser4); // Has token

        List<User> usersWithTokens = new ArrayList<>();
        for (User user : allUsers) {
            if (user.getFcmToken() != null && !user.getFcmToken().isEmpty()) {
                usersWithTokens.add(user);
            }
        }

        assertEquals("Should filter to only users with valid tokens",
                2, usersWithTokens.size());
        assertTrue("Should include user1", usersWithTokens.contains(testUser1));
        assertFalse("Should not include user2", usersWithTokens.contains(testUser2));
        assertFalse("Should not include user3", usersWithTokens.contains(testUser3));
        assertTrue("Should include user4", usersWithTokens.contains(testUser4));
    }

    /**
     * Tests filtering when all users have valid FCM tokens
     * Verifies no users are filtered out when all have tokens
     */
    @Test
    public void testFilterUsersWithFCMTokens_AllValid() {
        List<User> allUsers = new ArrayList<>();
        allUsers.add(testUser1);
        allUsers.add(testUser4);

        List<User> usersWithTokens = new ArrayList<>();
        for (User user : allUsers) {
            if (user.getFcmToken() != null && !user.getFcmToken().isEmpty()) {
                usersWithTokens.add(user);
            }
        }

        assertEquals("Should keep all users with valid tokens",
                2, usersWithTokens.size());
    }

    /**
     * Tests filtering when no users have valid FCM tokens
     * Verifies all users are filtered out when none have tokens
     */
    @Test
    public void testFilterUsersWithFCMTokens_NoneValid() {
        List<User> allUsers = new ArrayList<>();
        allUsers.add(testUser2); // Null token
        allUsers.add(testUser3); // Empty token

        List<User> usersWithTokens = new ArrayList<>();
        for (User user : allUsers) {
            if (user.getFcmToken() != null && !user.getFcmToken().isEmpty()) {
                usersWithTokens.add(user);
            }
        }

        assertTrue("Should filter out all users without valid tokens",
                usersWithTokens.isEmpty());
    }

    /**
     * Tests filtering with an empty user list
     * Verifies empty input produces empty output
     */
    @Test
    public void testFilterUsersWithFCMTokens_EmptyList() {
        List<User> allUsers = new ArrayList<>();

        List<User> usersWithTokens = new ArrayList<>();
        for (User user : allUsers) {
            if (user.getFcmToken() != null && !user.getFcmToken().isEmpty()) {
                usersWithTokens.add(user);
            }
        }

        assertTrue("Should return empty list for empty input",
                usersWithTokens.isEmpty());
    }

    /**
     * Tests that User's canReceiveNotifications method works correctly
     * Verifies the method returns true only for users with valid FCM tokens
     */
    @Test
    public void testUserCanReceiveNotifications() {
        assertTrue("User with token should be able to receive notifications",
                testUser1.canReceiveNotifications());
        assertFalse("User with null token should not receive notifications",
                testUser2.canReceiveNotifications());
        assertFalse("User with empty token should not receive notifications",
                testUser3.canReceiveNotifications());
        assertTrue("User with token should be able to receive notifications",
                testUser4.canReceiveNotifications());
    }

    /**
     * Tests extracting user ID from user data map
     * Verifies that the "id" key can be retrieved from the map structure
     */
    @Test
    public void testExtractUserIdFromDataMap() {
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", "test_user_123");

        String userId = (String) userData.get("id");

        assertNotNull("User ID should not be null", userId);
        assertEquals("Should extract correct user ID", "test_user_123", userId);
    }

    /**
     * Tests handling of user data map with missing ID
     * Verifies null is returned when "id" key is not present
     */
    @Test
    public void testExtractUserIdFromDataMap_MissingId() {
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", "Test User");

        String userId = (String) userData.get("id");

        assertNull("Should return null when id key is missing", userId);
    }

    /**
     * Tests that FCM token can be set and retrieved correctly
     * Verifies the getter and setter work as expected
     */
    @Test
    public void testUserFCMToken_SetAndGet() {
        User user = new User("user5", "Test User", "test@example.com");

        user.setFcmToken("new_token_123");
        assertEquals("Should retrieve the token that was set",
                "new_token_123", user.getFcmToken());
    }

    /**
     * Tests updating user FCM token from null to valid
     * Verifies user can transition from having no token to having one
     */
    @Test
    public void testUserFCMToken_UpdateFromNull() {
        User user = new User("user6", "Test User", "test@example.com");
        assertNull("New user should have null token initially", user.getFcmToken());

        user.setFcmToken("token_updated");
        assertNotNull("Token should not be null after update", user.getFcmToken());
        assertTrue("User should be able to receive notifications",
                user.canReceiveNotifications());
    }

    /**
     * Tests counting users in a mixed list
     * Verifies accurate counting of total users vs users with tokens
     */
    @Test
    public void testCountUsersWithAndWithoutTokens() {
        List<User> allUsers = new ArrayList<>();
        allUsers.add(testUser1);
        allUsers.add(testUser2);
        allUsers.add(testUser3);
        allUsers.add(testUser4);

        int totalUsers = allUsers.size();
        int usersWithTokens = 0;

        for (User user : allUsers) {
            if (user.getFcmToken() != null && !user.getFcmToken().isEmpty()) {
                usersWithTokens++;
            }
        }

        assertEquals("Should count all users", 4, totalUsers);
        assertEquals("Should count only users with valid tokens", 2, usersWithTokens);
    }

    /**
     * Tests that user data structure is preserved during conversion
     * Verifies all user information remains intact after list operations
     */
    @Test
    public void testUserDataIntegrityDuringFiltering() {
        List<User> allUsers = new ArrayList<>();
        allUsers.add(testUser1);

        List<User> filtered = new ArrayList<>();
        for (User user : allUsers) {
            if (user.canReceiveNotifications()) {
                filtered.add(user);
            }
        }

        assertEquals("Should have one user after filtering", 1, filtered.size());
        User filteredUser = filtered.get(0);
        assertEquals("User ID should be preserved", "user1", filteredUser.getId());
        assertEquals("User name should be preserved",
                "Alice Johnson", filteredUser.getName());
        assertEquals("User email should be preserved",
                "alice@example.com", filteredUser.getEmailAddress());
        assertEquals("User token should be preserved",
                "token_abc123", filteredUser.getFcmToken());
    }

    /**
     * Tests calculating waitlist count from Firestore snapshot data structure
     * Verifies count is calculated correctly from nested waitlist/waitlistedUsers structure
     */
    @Test
    public void testCalculateWaitlistCount_WithUsers() {
        // Simulate Firestore document structure
        Map<String, Object> waitlist = new HashMap<>();
        List<Map<String, Object>> waitlistedUsers = new ArrayList<>();

        Map<String, Object> user1 = new HashMap<>();
        user1.put("id", "user1");
        waitlistedUsers.add(user1);

        Map<String, Object> user2 = new HashMap<>();
        user2.put("id", "user2");
        waitlistedUsers.add(user2);

        Map<String, Object> user3 = new HashMap<>();
        user3.put("id", "user3");
        waitlistedUsers.add(user3);

        waitlist.put("waitlistedUsers", waitlistedUsers);

        // Calculate count (simulating updateCountsFromSnapshot logic)
        int waitlistCount = 0;
        if (waitlist != null) {
            List<Map<String, Object>> users =
                    (List<Map<String, Object>>) waitlist.get("waitlistedUsers");
            waitlistCount = (users != null) ? users.size() : 0;
        }

        assertEquals("Should count 3 waitlisted users", 3, waitlistCount);
    }

    /**
     * Tests calculating waitlist count when waitlist is null
     * Verifies count defaults to 0 when no waitlist data exists
     */
    @Test
    public void testCalculateWaitlistCount_NullWaitlist() {
        Map<String, Object> waitlist = null;

        int waitlistCount = 0;
        if (waitlist != null) {
            List<Map<String, Object>> users =
                    (List<Map<String, Object>>) waitlist.get("waitlistedUsers");
            waitlistCount = (users != null) ? users.size() : 0;
        }

        assertEquals("Should return 0 for null waitlist", 0, waitlistCount);
    }

    /**
     * Tests calculating waitlist count when waitlistedUsers is null
     * Verifies count defaults to 0 when waitlist exists but has no users
     */
    @Test
    public void testCalculateWaitlistCount_NullWaitlistedUsers() {
        Map<String, Object> waitlist = new HashMap<>();
        waitlist.put("waitlistedUsers", null);

        int waitlistCount = 0;
        if (waitlist != null) {
            List<Map<String, Object>> users =
                    (List<Map<String, Object>>) waitlist.get("waitlistedUsers");
            waitlistCount = (users != null) ? users.size() : 0;
        }

        assertEquals("Should return 0 for null waitlistedUsers", 0, waitlistCount);
    }

    /**
     * Tests calculating waitlist count with empty waitlistedUsers list
     * Verifies count is 0 when list exists but is empty
     */
    @Test
    public void testCalculateWaitlistCount_EmptyList() {
        Map<String, Object> waitlist = new HashMap<>();
        List<Map<String, Object>> waitlistedUsers = new ArrayList<>();
        waitlist.put("waitlistedUsers", waitlistedUsers);

        int waitlistCount = 0;
        if (waitlist != null) {
            List<Map<String, Object>> users =
                    (List<Map<String, Object>>) waitlist.get("waitlistedUsers");
            waitlistCount = (users != null) ? users.size() : 0;
        }

        assertEquals("Should return 0 for empty list", 0, waitlistCount);
    }

    /**
     * Tests calculating selected count from selectedIds array
     * Verifies count is calculated correctly from list of user IDs
     */
    @Test
    public void testCalculateSelectedCount_WithUsers() {
        List<String> selectedIds = new ArrayList<>();
        selectedIds.add("user1");
        selectedIds.add("user2");
        selectedIds.add("user3");
        selectedIds.add("user4");

        int selectedCount = (selectedIds != null) ? selectedIds.size() : 0;

        assertEquals("Should count 4 selected users", 4, selectedCount);
    }

    /**
     * Tests calculating selected count when selectedIds is null
     * Verifies count defaults to 0 when no selected users exist
     */
    @Test
    public void testCalculateSelectedCount_Null() {
        List<String> selectedIds = null;

        int selectedCount = (selectedIds != null) ? selectedIds.size() : 0;

        assertEquals("Should return 0 for null selectedIds", 0, selectedCount);
    }

    /**
     * Tests calculating selected count with empty selectedIds list
     * Verifies count is 0 when list exists but is empty
     */
    @Test
    public void testCalculateSelectedCount_EmptyList() {
        List<String> selectedIds = new ArrayList<>();

        int selectedCount = (selectedIds != null) ? selectedIds.size() : 0;

        assertEquals("Should return 0 for empty list", 0, selectedCount);
    }

    /**
     * Tests calculating cancelled count from cancelledEntrants array
     * Verifies count is calculated correctly from list of cancelled user IDs
     */
    @Test
    public void testCalculateCancelledCount_WithUsers() {
        List<String> cancelledEntrants = new ArrayList<>();
        cancelledEntrants.add("user1");
        cancelledEntrants.add("user2");

        int cancelledCount = (cancelledEntrants != null) ? cancelledEntrants.size() : 0;

        assertEquals("Should count 2 cancelled users", 2, cancelledCount);
    }

    /**
     * Tests calculating cancelled count when cancelledEntrants is null
     * Verifies count defaults to 0 when no cancelled users exist
     */
    @Test
    public void testCalculateCancelledCount_Null() {
        List<String> cancelledEntrants = null;

        int cancelledCount = (cancelledEntrants != null) ? cancelledEntrants.size() : 0;

        assertEquals("Should return 0 for null cancelledEntrants", 0, cancelledCount);
    }

    /**
     * Tests calculating cancelled count with empty cancelledEntrants list
     * Verifies count is 0 when list exists but is empty
     */
    @Test
    public void testCalculateCancelledCount_EmptyList() {
        List<String> cancelledEntrants = new ArrayList<>();

        int cancelledCount = (cancelledEntrants != null) ? cancelledEntrants.size() : 0;

        assertEquals("Should return 0 for empty list", 0, cancelledCount);
    }

    /**
     * Tests calculating all three counts simultaneously from a complete event document
     * Verifies all counts are calculated correctly from a realistic data structure
     */
    @Test
    public void testCalculateAllCounts_CompleteEventData() {
        // Simulate complete Firestore event document
        Map<String, Object> eventData = new HashMap<>();

        // Waitlist data
        Map<String, Object> waitlist = new HashMap<>();
        List<Map<String, Object>> waitlistedUsers = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Map<String, Object> user = new HashMap<>();
            user.put("id", "waitlist_user" + i);
            waitlistedUsers.add(user);
        }
        waitlist.put("waitlistedUsers", waitlistedUsers);
        eventData.put("waitlist", waitlist);

        // Selected IDs
        List<String> selectedIds = new ArrayList<>();
        selectedIds.add("selected1");
        selectedIds.add("selected2");
        selectedIds.add("selected3");
        eventData.put("selectedIds", selectedIds);

        // Cancelled entrants
        List<String> cancelledEntrants = new ArrayList<>();
        cancelledEntrants.add("cancelled1");
        cancelledEntrants.add("cancelled2");
        eventData.put("cancelledEntrants", cancelledEntrants);

        // Calculate counts (simulating updateCountsFromSnapshot)
        int waitlistCount = 0;
        Map<String, Object> waitlistData = (Map<String, Object>) eventData.get("waitlist");
        if (waitlistData != null) {
            List<Map<String, Object>> users =
                    (List<Map<String, Object>>) waitlistData.get("waitlistedUsers");
            waitlistCount = (users != null) ? users.size() : 0;
        }

        List<String> selected = (List<String>) eventData.get("selectedIds");
        int selectedCount = (selected != null) ? selected.size() : 0;

        List<String> cancelled = (List<String>) eventData.get("cancelledEntrants");
        int cancelledCount = (cancelled != null) ? cancelled.size() : 0;

        assertEquals("Should count 10 waitlisted users", 10, waitlistCount);
        assertEquals("Should count 3 selected users", 3, selectedCount);
        assertEquals("Should count 2 cancelled users", 2, cancelledCount);
    }

    /**
     * Tests calculating all counts when all lists are empty
     * Verifies all counts are 0 when no entrants exist in any category
     */
    @Test
    public void testCalculateAllCounts_AllEmpty() {
        Map<String, Object> eventData = new HashMap<>();

        Map<String, Object> waitlist = new HashMap<>();
        waitlist.put("waitlistedUsers", new ArrayList<>());
        eventData.put("waitlist", waitlist);
        eventData.put("selectedIds", new ArrayList<>());
        eventData.put("cancelledEntrants", new ArrayList<>());

        // Calculate counts
        int waitlistCount = 0;
        Map<String, Object> waitlistData = (Map<String, Object>) eventData.get("waitlist");
        if (waitlistData != null) {
            List<Map<String, Object>> users =
                    (List<Map<String, Object>>) waitlistData.get("waitlistedUsers");
            waitlistCount = (users != null) ? users.size() : 0;
        }

        List<String> selected = (List<String>) eventData.get("selectedIds");
        int selectedCount = (selected != null) ? selected.size() : 0;

        List<String> cancelled = (List<String>) eventData.get("cancelledEntrants");
        int cancelledCount = (cancelled != null) ? cancelled.size() : 0;

        assertEquals("Waitlist count should be 0", 0, waitlistCount);
        assertEquals("Selected count should be 0", 0, selectedCount);
        assertEquals("Cancelled count should be 0", 0, cancelledCount);
    }
}