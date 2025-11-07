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
import java.util.Locale;

import static org.junit.Assert.*;

/**
 * Unit tests for InfoActivity
 * Tests business logic for date formatting and status management
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