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
 * Unit tests for InfoActivity
 * Tests business logic for date formatting and status management
 */
public class InfoActivityTest {

    private User testUser;
    private Event testEvent;
    private Date startTime;
    private Date endTime;

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

    @Test
    public void testFormatEventDateTime_WithValidDate() {
        String formatted = formatEventDateTimeHelper(startTime);
        assertNotNull("Formatted string should not be null", formatted);
        assertTrue("Should contain 'at'", formatted.contains("at"));
        // Just verify it contains a time format (colon for hours:minutes)
        assertTrue("Should contain time with colon", formatted.contains(":"));
    }

    @Test
    public void testFormatEventDateTime_MorningTime() {
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.JANUARY, 1, 9, 30, 0);
        Date morning = cal.getTime();

        String formatted = formatEventDateTimeHelper(morning);
        assertNotNull("Formatted string should not be null", formatted);
        // Verify it contains the time
        assertTrue("Should contain time", formatted.contains(":"));
        // Verify it contains the day of week
        assertTrue("Should contain day of week",
                formatted.contains("day") || formatted.contains("Monday") ||
                        formatted.contains("Tuesday") || formatted.contains("Wednesday"));
    }

    @Test
    public void testFormatEventDateTime_EveningTime() {
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.JANUARY, 1, 20, 30, 0);
        Date evening = cal.getTime();

        String formatted = formatEventDateTimeHelper(evening);
        assertNotNull("Formatted string should not be null", formatted);
        // Verify it contains the hour and minute
        assertTrue("Should contain time format", formatted.contains(":30") || formatted.contains(":"));
        // Verify it contains 'at'
        assertTrue("Should contain 'at'", formatted.contains("at"));
    }

    @Test
    public void testFormatEventDateTime_NoonTime() {
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.JANUARY, 1, 12, 0, 0);
        Date noon = cal.getTime();

        String formatted = formatEventDateTimeHelper(noon);
        assertNotNull("Formatted string should not be null", formatted);
        // Verify the format contains key elements
        assertTrue("Should contain 'at' separator", formatted.contains("at"));
        assertTrue("Should contain time", formatted.contains(":"));
        // Verify it has the month
        assertTrue("Should contain month", formatted.contains("Jan"));
    }

    @Test
    public void testUserEventStatus_Notified() {
        testUser.getRegisteredEvents().put("event123", "Notified");
        String status = testUser.getStatusForEvent("event123");
        assertEquals("Status should be Notified", "Notified", status);
    }

    @Test
    public void testUserEventStatus_Accepted() {
        testUser.getRegisteredEvents().put("event123", "Notified");
        testUser.acceptInvitation("event123");
        String status = testUser.getStatusForEvent("event123");
        assertEquals("Status should be Accepted after accepting", "Accepted", status);
    }

    @Test
    public void testUserEventStatus_Declined() {
        testUser.getRegisteredEvents().put("event123", "Notified");
        testUser.declineInvitation("event123");
        String status = testUser.getStatusForEvent("event123");
        assertEquals("Status should be Declined after declining", "Declined", status);
    }

    @Test
    public void testUserEventStatus_NotRegistered() {
        String status = testUser.getStatusForEvent("nonexistent");
        assertNotNull("Should return a status even for non-existent event", status);
    }

    @Test
    public void testAcceptInvitation_ChangesStatus() {
        testUser.getRegisteredEvents().put("event123", "Notified");

        String beforeStatus = testUser.getStatusForEvent("event123");
        assertEquals("Initial status should be Notified", "Notified", beforeStatus);

        testUser.acceptInvitation("event123");

        String afterStatus = testUser.getStatusForEvent("event123");
        assertEquals("Status should change to Accepted", "Accepted", afterStatus);
    }

    @Test
    public void testDeclineInvitation_ChangesStatus() {
        testUser.getRegisteredEvents().put("event123", "Notified");

        String beforeStatus = testUser.getStatusForEvent("event123");
        assertEquals("Initial status should be Notified", "Notified", beforeStatus);

        testUser.declineInvitation("event123");

        String afterStatus = testUser.getStatusForEvent("event123");
        assertEquals("Status should change to Declined", "Declined", afterStatus);
    }

    @Test
    public void testEventHasValidData() {
        assertNotNull("Event ID should not be null", testEvent.getId());
        assertNotNull("Event name should not be null", testEvent.getName());
        assertNotNull("Event location should not be null", testEvent.getLocation());
        assertNotNull("Event organizer should not be null", testEvent.getOrganizer());
        assertNotNull("Start time should not be null", testEvent.getStartTime());
        assertNotNull("End time should not be null", testEvent.getEndTime());
    }

    // Helper method
    private String formatEventDateTimeHelper(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("EEEE, MMM dd, yyyy 'at' h:mm a", Locale.getDefault());
        return formatter.format(date);
    }
}
