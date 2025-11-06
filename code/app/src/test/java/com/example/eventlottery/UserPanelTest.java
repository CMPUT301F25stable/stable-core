package com.example.eventlottery;


import com.example.eventlottery.events.Event;
import com.example.eventlottery.users.User;

import org.junit.Before;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static org.junit.Assert.*;

/**
 * Unit tests for UserPanel
 * Tests business logic that can be extracted and tested independently
 */
public class UserPanelTest {
    private User testUser;
    private ArrayList<Event> testEvents;
    private Event testEvent1;
    private Event testEvent2;

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

        testEvents = new ArrayList<>();
        testEvents.add(testEvent1);
        testEvents.add(testEvent2);
    }

    @Test
    public void testFindEventById_EventExists() {
        Event found = findEventByIdHelper(testEvents, "event1");
        assertNotNull("Event should be found", found);
        assertEquals("Should find correct event", "event1", found.getId());
        assertEquals("Should have correct name", "Test Event 1", found.getName());
    }

    @Test
    public void testFindEventById_EventDoesNotExist() {
        Event found = findEventByIdHelper(testEvents, "nonexistent");
        assertNull("Event should not be found", found);
    }

    @Test
    public void testFindEventById_EmptyList() {
        ArrayList<Event> emptyList = new ArrayList<>();
        Event found = findEventByIdHelper(emptyList, "event1");
        assertNull("Should return null for empty list", found);
    }

    @Test
    public void testFindEventById_NullId() {
        Event found = findEventByIdHelper(testEvents, null);
        assertNull("Should return null for null ID", found);
    }

    @Test
    public void testFindEventById_MultipleEvents() {
        // Add more events
        Calendar cal = Calendar.getInstance();
        for (int i = 3; i <= 5; i++) {
            Event e = new Event("event" + i, "Event " + i, "Desc", "Loc", "Org", "",
                    cal.getTime(), cal.getTime());
            testEvents.add(e);
        }

        Event found = findEventByIdHelper(testEvents, "event4");
        assertNotNull("Should find event in larger list", found);
        assertEquals("Should find correct event", "event4", found.getId());
    }

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

    @Test
    public void testFormatEventDateTime_CurrentDate() {
        Date now = new Date();
        String formatted = formatEventDateTimeHelper(now);
        assertNotNull("Should format current date", formatted);
        assertTrue("Should contain time separator", formatted.contains(":"));
    }

    @Test
    public void testFormatEventDateTime_MidnightTime() {
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.DECEMBER, 1, 0, 0, 0);
        Date midnight = cal.getTime();

        String formatted = formatEventDateTimeHelper(midnight);
        assertNotNull("Should format midnight", formatted);
        assertTrue("Should contain time", formatted.contains("00:00"));
    }

    @Test
    public void testEventListNotEmpty() {
        assertFalse("Test events list should not be empty", testEvents.isEmpty());
        assertEquals("Should have 2 events", 2, testEvents.size());
    }

    @Test
    public void testUserHasNoEvents_Initially() {
        User newUser = new User("user999", "New User", "new@example.com", "780-999-9999");
        assertTrue("New user should have no registered events",
                newUser.getRegisteredEvents().isEmpty());
        assertTrue("New user should have no waitlisted events",
                newUser.getWaitlistedEvents().isEmpty());
    }

    @Test
    public void testUserHasEvents() {
        testUser.getRegisteredEvents().put("event1", "Accepted");
        testUser.getWaitlistedEvents().add("event2");

        assertFalse("User should have registered events",
                testUser.getRegisteredEvents().isEmpty());
        assertFalse("User should have waitlisted events",
                testUser.getWaitlistedEvents().isEmpty());
    }

    // Helper methods that replicate logic from UserPanel
    private Event findEventByIdHelper(ArrayList<Event> events, String eventId) {
        if (eventId == null) return null;
        for (Event event : events) {
            if (event.getId().equals(eventId)) {
                return event;
            }
        }
        return null;
    }

    private String formatEventDateTimeHelper(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        return formatter.format(date);
    }

}
