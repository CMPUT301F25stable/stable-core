package com.example.eventlottery;

import com.example.eventlottery.events.Event;
import com.example.eventlottery.users.User;

import org.junit.Before;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * Unit tests for InfoActivity
 * Tests business logic for date formatting, status management, and waitlist replacement
 *
 * @author Jensen Lee
 */
public class InfoActivityTest {

    private User testUser;
    private Event testEvent;
    private Date startTime;
    private Date endTime;

    /**
     * Sets up test fixtures before each test method
     * Creates a test user and event with specific dates for testing
     */
    @Before
    public void setUp() {
        testUser = new User("user123", "Test User", "test@example.com", "780-123-4567");

        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.DECEMBER, 25, 18, 0, 0);
        startTime = cal.getTime();
        cal.add(Calendar.HOUR, 3);
        endTime = cal.getTime();

        testEvent = new Event("event123", "Christmas Party", "Annual holiday celebration",
                "Community Center", "John Doe", "", startTime, endTime);
    }

    /**
     * Tests that formatEventDateTime produces a valid formatted string for a specific date
     * Verifies the format includes the 'at' separator and time with colons
     */
    @Test
    public void testFormatEventDateTime_WithValidDate() {
        String formatted = formatEventDateTimeHelper(startTime);
        assertNotNull("Formatted string should not be null", formatted);
        assertTrue("Should contain 'at'", formatted.contains("at"));
        assertTrue("Should contain time with colon", formatted.contains(":"));
    }

    /**
     * Tests that formatEventDateTime correctly handles morning times (AM)
     * Verifies the formatted string contains day of week and proper time format
     */
    @Test
    public void testFormatEventDateTime_MorningTime() {
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.JANUARY, 1, 9, 30, 0);
        Date morning = cal.getTime();

        String formatted = formatEventDateTimeHelper(morning);
        assertNotNull("Formatted string should not be null", formatted);
        assertTrue("Should contain time", formatted.contains(":"));
        assertTrue("Should contain day of week",
                formatted.contains("day") || formatted.contains("Monday") ||
                        formatted.contains("Tuesday") || formatted.contains("Wednesday"));
    }

    /**
     * Tests that formatEventDateTime correctly handles evening times (PM)
     * Verifies the format includes both the time and 'at' separator
     */
    @Test
    public void testFormatEventDateTime_EveningTime() {
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.JANUARY, 1, 20, 30, 0);
        Date evening = cal.getTime();

        String formatted = formatEventDateTimeHelper(evening);
        assertNotNull("Formatted string should not be null", formatted);
        assertTrue("Should contain time format", formatted.contains(":30") || formatted.contains(":"));
        assertTrue("Should contain 'at'", formatted.contains("at"));
    }

    /**
     * Tests that formatEventDateTime handles noon (12:00 PM) correctly
     * Verifies all key components (month, separator, time) are present
     */
    @Test
    public void testFormatEventDateTime_NoonTime() {
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.JANUARY, 1, 12, 0, 0);
        Date noon = cal.getTime();

        String formatted = formatEventDateTimeHelper(noon);
        assertNotNull("Formatted string should not be null", formatted);
        assertTrue("Should contain 'at' separator", formatted.contains("at"));
        assertTrue("Should contain time", formatted.contains(":"));
        assertTrue("Should contain month", formatted.contains("Jan"));
    }

    /**
     * Tests that formatEventDateTime handles midnight (12:00 AM) correctly
     * Ensures edge case time values are formatted properly
     */
    @Test
    public void testFormatEventDateTime_MidnightTime() {
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.DECEMBER, 1, 0, 0, 0);
        Date midnight = cal.getTime();

        String formatted = formatEventDateTimeHelper(midnight);
        assertNotNull("Should format midnight", formatted);
        assertTrue("Should contain time", formatted.contains(":"));
        assertTrue("Should contain 'at' separator", formatted.contains("at"));
    }

    /**
     * Tests that the user correctly retrieves "Notified" status for an event
     * Verifies getStatusForEvent returns the correct status string
     */
    @Test
    public void testUserEventStatus_Notified() {
        testUser.getRegisteredEvents().put("event123", "Notified");
        String status = testUser.getStatusForEvent("event123");
        assertEquals("Status should be Notified", "Notified", status);
    }

    /**
     * Tests that user can have "Accepted" status stored in registered events
     * Verifies the registeredEvents map correctly stores and retrieves Accepted status
     */
    @Test
    public void testUserEventStatus_Accepted() {
        testUser.getRegisteredEvents().put("event123", "Accepted");
        String status = testUser.getStatusForEvent("event123");
        assertEquals("Status should be Accepted", "Accepted", status);
    }

    /**
     * Tests that user can have "Declined" status stored in registered events
     * Verifies the registeredEvents map correctly stores and retrieves Declined status
     */
    @Test
    public void testUserEventStatus_Declined() {
        testUser.getRegisteredEvents().put("event123", "Declined");
        String status = testUser.getStatusForEvent("event123");
        assertEquals("Status should be Declined", "Declined", status);
    }

    /**
     * Tests that getStatusForEvent handles non-existent events gracefully
     * Ensures the method returns a valid status string even for unregistered events
     */
    @Test
    public void testUserEventStatus_NotRegistered() {
        String status = testUser.getStatusForEvent("nonexistent");
        assertNotNull("Should return a status even for non-existent event", status);
        assertEquals("Should return 'Not Registered' for non-existent event",
                "Not Registered", status);
    }

    /**
     * Tests that Event object contains all required non-null fields
     * Verifies event data integrity after creation
     */
    @Test
    public void testEventHasValidData() {
        assertNotNull("Event ID should not be null", testEvent.getId());
        assertNotNull("Event name should not be null", testEvent.getName());
        assertNotNull("Event location should not be null", testEvent.getLocation());
        assertNotNull("Event organizer should not be null", testEvent.getOrganizer());
        assertNotNull("Start time should not be null", testEvent.getStartTime());
        assertNotNull("End time should not be null", testEvent.getEndTime());
    }

    /**
     * Tests that event description is properly stored and retrieved
     * Verifies the description field matches expected value
     */
    @Test
    public void testEventDescription() {
        assertEquals("Event description should match",
                "Annual holiday celebration", testEvent.getDescription());
    }

    /**
     * Tests that event organizer information is correctly stored
     * Verifies the organizer name matches expected value
     */
    @Test
    public void testEventOrganizer() {
        assertEquals("Event organizer should be John Doe",
                "John Doe", testEvent.getOrganizer());
    }

    /**
     * Tests that event times are correctly ordered (end time after start time)
     * Ensures logical consistency of event duration
     */
    @Test
    public void testEventTimeOrdering() {
        assertTrue("End time should be after start time",
                testEvent.getEndTime().after(testEvent.getStartTime()));
    }

    /**
     * Tests that user has proper initial state with initialized collections
     * Verifies registeredEvents and waitlistedEvents are not null after creation
     */
    @Test
    public void testUserInitialState() {
        User newUser = new User("user456", "New User", "new@example.com", "780-456-7890");
        assertNotNull("Registered events should not be null", newUser.getRegisteredEvents());
        assertNotNull("Waitlisted events should not be null", newUser.getWaitlistedEvents());
    }

    /**
     * Tests that user's registeredEvents map can store multiple events
     * Verifies the map correctly maintains multiple event statuses
     */
    @Test
    public void testMultipleRegisteredEvents() {
        testUser.getRegisteredEvents().put("event1", "Accepted");
        testUser.getRegisteredEvents().put("event2", "Notified");
        testUser.getRegisteredEvents().put("event3", "Declined");

        assertEquals("Should have 3 registered events",
                3, testUser.getRegisteredEvents().size());
        assertEquals("Event1 should be Accepted",
                "Accepted", testUser.getStatusForEvent("event1"));
        assertEquals("Event2 should be Notified",
                "Notified", testUser.getStatusForEvent("event2"));
        assertEquals("Event3 should be Declined",
                "Declined", testUser.getStatusForEvent("event3"));
    }

    /**
     * Tests that setRegisteredEvents correctly replaces the entire map
     * Verifies the method used when loading data from Firestore in InfoActivity
     */
    @Test
    public void testSetRegisteredEvents() {
        HashMap<String, String> newEvents = new HashMap<>();
        newEvents.put("event1", "Accepted");
        newEvents.put("event2", "Notified");

        testUser.setRegisteredEvents(newEvents);

        assertEquals("Should have 2 registered events",
                2, testUser.getRegisteredEvents().size());
        assertTrue("Should contain event1",
                testUser.getRegisteredEvents().containsKey("event1"));
        assertTrue("Should contain event2",
                testUser.getRegisteredEvents().containsKey("event2"));
    }

    /**
     * Tests that setWaitlistedEventIds correctly updates the waitlist
     * Verifies the method handles empty lists properly
     */
    @Test
    public void testSetWaitlistedEventIds_EmptyList() {
        testUser.getWaitlistedEvents().add("event1");

        ArrayList<String> emptyList = new ArrayList<>();
        testUser.setWaitlistedEventIds(emptyList);

        assertTrue("Waitlisted events should be empty",
                testUser.getWaitlistedEvents().isEmpty());
    }

    /**
     * Tests that user data remains consistent when status is changed
     * Verifies the user object maintains data integrity through operations
     */
    @Test
    public void testUserDataConsistency() {
        testUser.getRegisteredEvents().put("event123", "Notified");

        assertEquals("Should have 1 registered event",
                1, testUser.getRegisteredEvents().size());

        // Directly change status without Firebase
        testUser.getRegisteredEvents().put("event123", "Accepted");

        assertEquals("Should still have 1 registered event",
                1, testUser.getRegisteredEvents().size());
        assertEquals("Status should be updated",
                "Accepted", testUser.getStatusForEvent("event123"));
    }

    /**
     * Tests determining if a user was a winner based on their status
     * Winners have "Notified" or "Accepted" status
     */
    @Test
    public void testIsWinner_NotifiedStatus() {
        testUser.getRegisteredEvents().put("event123", "Notified");
        String status = testUser.getRegisteredEvents().get("event123");
        boolean isWinner = "Notified".equals(status) || "Accepted".equals(status);

        assertTrue("User with Notified status should be a winner", isWinner);
    }

    /**
     * Tests determining if a user was a winner with Accepted status
     */
    @Test
    public void testIsWinner_AcceptedStatus() {
        testUser.getRegisteredEvents().put("event123", "Accepted");
        String status = testUser.getRegisteredEvents().get("event123");
        boolean isWinner = "Notified".equals(status) || "Accepted".equals(status);

        assertTrue("User with Accepted status should be a winner", isWinner);
    }

    /**
     * Tests that user with Declined status is not a winner
     */
    @Test
    public void testIsWinner_DeclinedStatus() {
        testUser.getRegisteredEvents().put("event123", "Declined");
        String status = testUser.getRegisteredEvents().get("event123");
        boolean isWinner = "Notified".equals(status) || "Accepted".equals(status);

        assertFalse("User with Declined status should not be a winner", isWinner);
    }

    /**
     * Tests that user with no status is not a winner
     */
    @Test
    public void testIsWinner_NoStatus() {
        String status = testUser.getRegisteredEvents().get("event123");
        boolean isWinner = "Notified".equals(status) || "Accepted".equals(status);

        assertFalse("User with no status should not be a winner", isWinner);
    }

    /**
     * Tests extracting waitlisted users from event document structure
     * Verifies the nested waitlist.waitlistedUsers structure can be parsed
     */
    @Test
    public void testExtractWaitlistedUsers_FromEventDocument() {
        // Simulate event document structure
        Map<String, Object> waitlistMap = new HashMap<>();
        ArrayList<Map<String, Object>> waitlistedUsers = new ArrayList<>();

        Map<String, Object> user1 = new HashMap<>();
        user1.put("id", "user1");
        user1.put("name", "Alice");
        waitlistedUsers.add(user1);

        Map<String, Object> user2 = new HashMap<>();
        user2.put("id", "user2");
        user2.put("name", "Bob");
        waitlistedUsers.add(user2);

        waitlistMap.put("waitlistedUsers", waitlistedUsers);

        // Extract users
        ArrayList<Map<String, Object>> extracted =
                (ArrayList<Map<String, Object>>) waitlistMap.get("waitlistedUsers");

        assertNotNull("Waitlisted users should not be null", extracted);
        assertEquals("Should have 2 waitlisted users", 2, extracted.size());
        assertEquals("First user ID should be user1", "user1", extracted.get(0).get("id"));
        assertEquals("Second user ID should be user2", "user2", extracted.get(1).get("id"));
    }

    /**
     * Tests extracting waitlisted users when waitlist is null
     */
    @Test
    public void testExtractWaitlistedUsers_NullWaitlist() {
        Map<String, Object> waitlistMap = null;

        ArrayList<Map<String, Object>> extracted = null;
        if (waitlistMap != null) {
            extracted = (ArrayList<Map<String, Object>>) waitlistMap.get("waitlistedUsers");
        }

        assertNull("Extracted users should be null when waitlist is null", extracted);
    }

    /**
     * Tests selecting random user from waitlist
     * Verifies random selection returns a valid user from the list
     */
    @Test
    public void testRandomWaitlistSelection() {
        ArrayList<Map<String, Object>> waitlistedUsers = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            Map<String, Object> user = new HashMap<>();
            user.put("id", "user" + i);
            user.put("name", "User " + i);
            waitlistedUsers.add(user);
        }

        Random random = new Random(42); // Use seed for reproducible test
        int randomIndex = random.nextInt(waitlistedUsers.size());
        Map<String, Object> selectedUser = waitlistedUsers.get(randomIndex);

        assertTrue("Random index should be valid", randomIndex >= 0 && randomIndex < 5);
        assertNotNull("Selected user should not be null", selectedUser);
        assertNotNull("Selected user should have an ID", selectedUser.get("id"));
    }

    /**
     * Tests removing user from waitlist after selection
     * Verifies list size decreases and correct user is removed
     */
    @Test
    public void testRemoveUserFromWaitlist() {
        ArrayList<Map<String, Object>> waitlistedUsers = new ArrayList<>();

        Map<String, Object> user1 = new HashMap<>();
        user1.put("id", "user1");
        user1.put("name", "Alice");
        waitlistedUsers.add(user1);

        Map<String, Object> user2 = new HashMap<>();
        user2.put("id", "user2");
        user2.put("name", "Bob");
        waitlistedUsers.add(user2);

        assertEquals("Should start with 2 users", 2, waitlistedUsers.size());

        // Remove first user (index 0)
        Map<String, Object> removed = waitlistedUsers.remove(0);

        assertEquals("Should have 1 user after removal", 1, waitlistedUsers.size());
        assertEquals("Removed user should be Alice", "Alice", removed.get("name"));
        assertEquals("Remaining user should be Bob", "Bob", waitlistedUsers.get(0).get("name"));
    }

    /**
     * Tests complete decline and replacement workflow logic
     * Simulates the process of a winner declining and being replaced
     */
    @Test
    public void testDeclineAndReplacement_CompleteWorkflow() {
        // Setup: User is a winner
        testUser.getRegisteredEvents().put("event123", "Notified");
        String initialStatus = testUser.getRegisteredEvents().get("event123");
        boolean wasWinner = "Notified".equals(initialStatus) || "Accepted".equals(initialStatus);
        assertTrue("User should start as a winner", wasWinner);

        // Setup: Waitlist has users
        ArrayList<Map<String, Object>> waitlistedUsers = new ArrayList<>();
        Map<String, Object> replacement = new HashMap<>();
        replacement.put("id", "replacement1");
        replacement.put("name", "Replacement User");
        waitlistedUsers.add(replacement);

        assertFalse("Waitlist should not be empty", waitlistedUsers.isEmpty());

        // Step 1: User declines
        testUser.getRegisteredEvents().put("event123", "Declined");
        assertEquals("Status should be Declined", "Declined",
                testUser.getStatusForEvent("event123"));

        // Step 2: Select replacement from waitlist
        int randomIndex = 0; // For testing, just take first
        Map<String, Object> selectedReplacement = waitlistedUsers.get(randomIndex);
        String replacementId = (String) selectedReplacement.get("id");

        assertNotNull("Replacement should be selected", selectedReplacement);
        assertEquals("Should select correct replacement", "replacement1", replacementId);

        // Step 3: Remove from waitlist
        waitlistedUsers.remove(randomIndex);
        assertTrue("Waitlist should be empty after removal", waitlistedUsers.isEmpty());
    }

    /**
     * Tests that empty waitlist prevents replacement
     * Verifies decline works even when no replacement is available
     */
    @Test
    public void testDeclineWithEmptyWaitlist() {
        testUser.getRegisteredEvents().put("event123", "Notified");

        ArrayList<Map<String, Object>> waitlistedUsers = new ArrayList<>();
        assertTrue("Waitlist should be empty", waitlistedUsers.isEmpty());

        // User declines
        testUser.getRegisteredEvents().put("event123", "Declined");

        assertEquals("Status should be Declined even with empty waitlist",
                "Declined", testUser.getStatusForEvent("event123"));
    }

    /**
     * Tests creating user info map for finalizedUsers
     * Verifies the structure matches what's expected in Firestore
     */
    @Test
    public void testCreateUserInfoMap() {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", testUser.getId());
        userInfo.put("name", testUser.getName());
        userInfo.put("emailAddress", testUser.getEmailAddress());
        userInfo.put("phoneNumber", testUser.getPhoneNumber());

        assertEquals("Should have correct ID", "user123", userInfo.get("id"));
        assertEquals("Should have correct name", "Test User", userInfo.get("name"));
        assertEquals("Should have correct email", "test@example.com", userInfo.get("emailAddress"));
        assertEquals("Should have correct phone", "780-123-4567", userInfo.get("phoneNumber"));
        assertEquals("Should have 4 fields", 4, userInfo.size());
    }

    /**
     * Tests that accept invitation updates status correctly
     * Verifies acceptInvitation method works as expected
     */
    @Test
    public void testAcceptInvitation() {
        testUser.getRegisteredEvents().put("event123", "Notified");
        testUser.acceptInvitation("event123");

        assertEquals("Status should be Accepted after accepting",
                "Accepted", testUser.getStatusForEvent("event123"));
    }

    /**
     * Tests that decline invitation updates status correctly
     * Verifies declineInvitation method works as expected
     */
    @Test
    public void testDeclineInvitation() {
        testUser.getRegisteredEvents().put("event123", "Notified");
        testUser.declineInvitation("event123");

        assertEquals("Status should be Declined after declining",
                "Declined", testUser.getStatusForEvent("event123"));
    }

    /**
     * Tests that accept/decline only works for registered events
     * Verifies methods don't create new entries for non-existent events
     */
    @Test
    public void testAcceptDecline_OnlyWorksForRegisteredEvents() {
        int initialSize = testUser.getRegisteredEvents().size();

        testUser.acceptInvitation("nonexistent");
        assertEquals("Should not add new event when accepting non-existent",
                initialSize, testUser.getRegisteredEvents().size());

        testUser.declineInvitation("nonexistent");
        assertEquals("Should not add new event when declining non-existent",
                initialSize, testUser.getRegisteredEvents().size());
    }

    /**
     * Helper method that replicates formatEventDateTime logic from InfoActivity
     * Formats a Date object into a readable string with day of week, date, and time
     * Uses the same format as InfoActivity: "EEEE, MMM dd, yyyy 'at' h:mm a"
     *
     * @param date The Date object to format
     * @return A formatted string with full date and time information
     */
    private String formatEventDateTimeHelper(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("EEEE, MMM dd, yyyy 'at' h:mm a", Locale.getDefault());
        return formatter.format(date);
    }
}