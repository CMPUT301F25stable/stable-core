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

import static org.junit.Assert.*;

/**
 * Unit tests for UserPanel
 * Tests business logic that can be extracted and tested independently
 *
 * @author Jensen Lee
 */
public class UserPanelTest {
    private User testUser;
    private ArrayList<Event> testEvents;
    private Event testEvent1;
    private Event testEvent2;
    private Event testEvent3;

    /**
     * Sets up test fixtures before each test method
     * Creates a test user and multiple test events with proper dates
     */
    @Before
    public void setUp() {
        // Create test user
        testUser = new User("user123", "Test User", "test@example.com", "780-123-4567");

        // Create test events
        Calendar cal = Calendar.getInstance();
        Date startTime = cal.getTime();
        cal.add(Calendar.HOUR, 2);
        Date endTime = cal.getTime();

        testEvent1 = new Event("event1", "Test Event 1", "Description 1",
                "Location 1", "Organizer 1", "", startTime, endTime);
        testEvent2 = new Event("event2", "Test Event 2", "Description 2",
                "Location 2", "Organizer 2", "", startTime, endTime);
        testEvent3 = new Event("event3", "Test Event 3", "Description 3",
                "Location 3", "Organizer 3", "", startTime, endTime);

        testEvents = new ArrayList<>();
        testEvents.add(testEvent1);
        testEvents.add(testEvent2);
        testEvents.add(testEvent3);
    }

    /**
     * Tests that findEventById correctly finds an event that exists in the list
     * Verifies both the event is found and that it's the correct event
     */
    @Test
    public void testFindEventById_EventExists() {
        Event found = findEventByIdHelper(testEvents, "event1");
        assertNotNull("Event should be found", found);
        assertEquals("Should find correct event", "event1", found.getId());
        assertEquals("Should have correct name", "Test Event 1", found.getName());
    }

    /**
     * Tests that findEventById returns null when searching for a non-existent event
     * Ensures the method handles missing events gracefully
     */
    @Test
    public void testFindEventById_EventDoesNotExist() {
        Event found = findEventByIdHelper(testEvents, "nonexistent");
        assertNull("Event should not be found", found);
    }

    /**
     * Tests that findEventById returns null when given an empty event list
     * Verifies proper handling of edge case with no events
     */
    @Test
    public void testFindEventById_EmptyList() {
        ArrayList<Event> emptyList = new ArrayList<>();
        Event found = findEventByIdHelper(emptyList, "event1");
        assertNull("Should return null for empty list", found);
    }

    /**
     * Tests that findEventById handles null event ID parameter safely
     * Ensures the method doesn't throw exceptions with invalid input
     */
    @Test
    public void testFindEventById_NullId() {
        Event found = findEventByIdHelper(testEvents, null);
        assertNull("Should return null for null ID", found);
    }

    /**
     * Tests that findEventById works correctly with a larger list of events
     * Verifies the search algorithm scales properly with more events
     */
    @Test
    public void testFindEventById_MultipleEvents() {
        // Add more events
        Calendar cal = Calendar.getInstance();
        for (int i = 4; i <= 6; i++) {
            Event e = new Event("event" + i, "Event " + i, "Desc", "Loc", "Org", "",
                    cal.getTime(), cal.getTime());
            testEvents.add(e);
        }

        Event found = findEventByIdHelper(testEvents, "event5");
        assertNotNull("Should find event in larger list", found);
        assertEquals("Should find correct event", "event5", found.getId());
    }

    /**
     * Tests that formatEventDateTime produces a valid formatted string for a specific date
     * Verifies the format includes month, day, year, and time components
     */
    @Test
    public void testFormatEventDateTime_ValidDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.NOVEMBER, 15, 14, 30, 0);
        Date testDate = cal.getTime();

        String formatted = formatEventDateTimeHelper(testDate);
        assertNotNull("Formatted string should not be null", formatted);
        assertTrue("Should contain month", formatted.contains("Nov"));
        assertTrue("Should contain day", formatted.contains("15"));
        assertTrue("Should contain year", formatted.contains("2024"));
    }

    /**
     * Tests that formatEventDateTime correctly formats the current date and time
     * Ensures the formatter works with real-time data
     */
    @Test
    public void testFormatEventDateTime_CurrentDate() {
        Date now = new Date();
        String formatted = formatEventDateTimeHelper(now);
        assertNotNull("Should format current date", formatted);
        assertTrue("Should contain time separator", formatted.contains(":"));
    }

    /**
     * Tests that formatEventDateTime handles midnight (00:00) time correctly
     * Verifies proper formatting of edge case time values
     */
    @Test
    public void testFormatEventDateTime_MidnightTime() {
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.DECEMBER, 1, 0, 0, 0);
        Date midnight = cal.getTime();

        String formatted = formatEventDateTimeHelper(midnight);
        assertNotNull("Should format midnight", formatted);
        assertTrue("Should contain time", formatted.contains("00:00"));
    }

    /**
     * Tests that the test events list is properly initialized
     * Verifies setUp() method creates the expected number of events
     */
    @Test
    public void testEventListNotEmpty() {
        assertFalse("Test events list should not be empty", testEvents.isEmpty());
        assertEquals("Should have 3 events", 3, testEvents.size());
    }

    /**
     * Tests that a newly created user has no events initially
     * Verifies default state of user's registered and waitlisted event lists
     */
    @Test
    public void testUserHasNoEvents_Initially() {
        User newUser = new User("user999", "New User", "new@example.com", "780-999-9999");
        assertTrue("New user should have no registered events",
                newUser.getRegisteredEvents().isEmpty());
        assertTrue("New user should have no waitlisted events",
                newUser.getWaitlistedEvents().isEmpty());
    }

    /**
     * Tests that a user correctly stores registered and waitlisted events
     * Verifies the user object can maintain event state
     */
    @Test
    public void testUserHasEvents() {
        testUser.getRegisteredEvents().put("event1", "Accepted");
        testUser.getWaitlistedEvents().add("event2");

        assertFalse("User should have registered events",
                testUser.getRegisteredEvents().isEmpty());
        assertFalse("User should have waitlisted events",
                testUser.getWaitlistedEvents().isEmpty());
    }

    /**
     * Tests that registered events maintain their status correctly
     * Verifies the status string is stored and retrieved properly
     */
    @Test
    public void testRegisteredEventStatus() {
        testUser.getRegisteredEvents().put("event1", "Notified");

        assertEquals("Event should have Notified status",
                "Notified", testUser.getRegisteredEvents().get("event1"));
    }

    /**
     * Tests that multiple events can be added to waitlist
     * Verifies the waitlist collection properly stores multiple event IDs
     */
    @Test
    public void testMultipleWaitlistedEvents() {
        testUser.getWaitlistedEvents().add("event1");
        testUser.getWaitlistedEvents().add("event2");
        testUser.getWaitlistedEvents().add("event3");

        assertEquals("User should have 3 waitlisted events",
                3, testUser.getWaitlistedEvents().size());
        assertTrue("Should contain event1", testUser.getWaitlistedEvents().contains("event1"));
        assertTrue("Should contain event2", testUser.getWaitlistedEvents().contains("event2"));
        assertTrue("Should contain event3", testUser.getWaitlistedEvents().contains("event3"));
    }

    /**
     * Tests that different event statuses are properly distinguished
     * Verifies all status types (Accepted, Notified, Waitlisted, Declined) work correctly
     */
    @Test
    public void testMultipleEventStatuses() {
        testUser.getRegisteredEvents().put("event1", "Accepted");
        testUser.getRegisteredEvents().put("event2", "Notified");
        testUser.getRegisteredEvents().put("event3", "Declined");

        assertEquals("Event1 should be Accepted",
                "Accepted", testUser.getRegisteredEvents().get("event1"));
        assertEquals("Event2 should be Notified",
                "Notified", testUser.getRegisteredEvents().get("event2"));
        assertEquals("Event3 should be Declined",
                "Declined", testUser.getRegisteredEvents().get("event3"));
    }

    /**
     * Tests that setWaitlistedEventIds correctly replaces the entire waitlist
     * Verifies the method used when loading data from Firestore
     */
    @Test
    public void testSetWaitlistedEventIds() {
        List<String> eventIds = new ArrayList<>();
        eventIds.add("event1");
        eventIds.add("event2");

        testUser.setWaitlistedEventIds(eventIds);

        assertEquals("Should have 2 waitlisted events",
                2, testUser.getWaitlistedEvents().size());
        assertTrue("Should contain event1", testUser.getWaitlistedEvents().contains("event1"));
        assertTrue("Should contain event2", testUser.getWaitlistedEvents().contains("event2"));
    }

    /**
     * Tests that setRegisteredEvents correctly replaces the entire registered events map
     * Verifies the method used when loading data from Firestore
     */
    @Test
    public void testSetRegisteredEvents() {
        HashMap<String, String> registeredEvents = new HashMap<>();
        registeredEvents.put("event1", "Accepted");
        registeredEvents.put("event2", "Notified");

        testUser.setRegisteredEvents(registeredEvents);

        assertEquals("Should have 2 registered events",
                2, testUser.getRegisteredEvents().size());
        assertEquals("Event1 should be Accepted",
                "Accepted", testUser.getRegisteredEvents().get("event1"));
        assertEquals("Event2 should be Notified",
                "Notified", testUser.getRegisteredEvents().get("event2"));
    }

    /**
     * Helper method that replicates findEventById logic from UserPanel
     * Searches through a list of events to find one matching the given ID
     *
     * @param events The list of events to search through
     * @param eventId The ID of the event to find
     * @return The matching Event object, or null if not found or eventId is null
     */
    private Event findEventByIdHelper(ArrayList<Event> events, String eventId) {
        if (eventId == null) return null;
        for (Event event : events) {
            if (event.getId().equals(eventId)) {
                return event;
            }
        }
        return null;
    }

    /**
     * Helper method that replicates formatEventDateTime logic from UserPanel
     * Formats a Date object into a readable string format
     *
     * @param date The Date object to format
     * @return A formatted string in "MMM dd, yyyy HH:mm" format
     */
    private String formatEventDateTimeHelper(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        return formatter.format(date);
    }
}