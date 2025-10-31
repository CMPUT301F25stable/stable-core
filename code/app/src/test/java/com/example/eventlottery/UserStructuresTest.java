package com.example.eventlottery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import com.example.eventlottery.events.Event;
import com.example.eventlottery.users.Entrant;
import com.example.eventlottery.users.Organizer;
import com.example.eventlottery.users.User;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class UserStructuresTest {
    private final String DEVICE_ID = "TEST_ID";

    private Entrant createTestEntrant() {
        Event waitlist = new Event("Waitlisted Event", "This is my waitlisted event.", "TestCity", "test", 0, new Date(), new Date(System.currentTimeMillis() + 100000));
        Event regDeclined = new Event("Declined Event", "This is my declined event.", "TestCity 2", "test 2", 1, new Date(), new Date(System.currentTimeMillis() + 200000));

        Entrant entrant = new Entrant();
        entrant.addWaitlistedEvent(waitlist);
        entrant.addRegisteredEvent(regDeclined, "Declined");
        return entrant;
    }

    private void eventPrinting(Event event) {
        System.out.println("Event Name: " + event.getName());
        System.out.println("Event Description: " + event.getDescription());
        System.out.println("Event Location: " + event.getLocation());
        System.out.println("Event Start Time: " + event.getStartTime().toString());
        System.out.println("Event End Time: " + event.getEndTime().toString());
        System.out.println("Event Start Time (Formatted): " + event.getFormattedStartTime());
        System.out.println("Event End Time (Formatted): " + event.getFormattedEndTime());
    }

    private Event randomEvent() {
        Random random = new Random();
        long dateNum = random.nextLong(1700000000000L);
        String randomName = String.valueOf(UUID.randomUUID());
        return new Event(randomName, randomName.concat(" Description"), randomName.concat(" Location"), randomName.concat(" Organizer ID"), 0, new Date(dateNum), new Date(dateNum + 7200000));
    }

    private void addTestEvents(Organizer organizer) {
        for (int i = 0; i < 20; i++) {
            Event newEvent = randomEvent();
            organizer.createEvent(newEvent);
        }
    }

    private ArrayList<Event> createTestEvents() {
        ArrayList<Event> events = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Event newEvent = randomEvent();
            events.add(newEvent);
        }
        return events;
    }

    @Test
    public void entrantTest() {
        User user = new User(DEVICE_ID, "Test Name", "test@example.com", "000-000-0000");

        Entrant testEntrant = createTestEntrant();
        ArrayList<Event> waitlistedEvents = testEntrant.getWaitlistedEvents();
        for (int i = 0; i < waitlistedEvents.size(); i++) {
            Event event = waitlistedEvents.get(i);
            eventPrinting(event);
            System.out.println("------------------------------------------------------");
        }

        HashMap<Event, String> registeredEvents = testEntrant.getRegisteredEvents();
        registeredEvents.forEach((event, status) -> {
            eventPrinting(event);
            System.out.println("Event Registration Status: " + status);
            System.out.println("------------------------------------------------------");
        });

        user.setEntrant(testEntrant);
        assertSame(user.getEntrant(), testEntrant);
    }

    @Test
    public void userTest() {
        User user = new User(DEVICE_ID, "Test Name", "test@example.com", "000-000-0000");
        Entrant userEntrant = user.getEntrant();

        assertEquals(0, userEntrant.getWaitlistedEvents().size());
        userEntrant.addWaitlistedEvent(new Event("Test 1", "Test 1", "Test 1", "Test 1", 1, new Date(), new Date()));
        assertEquals(1, userEntrant.getWaitlistedEvents().size());
        assertSame(userEntrant, user.getEntrant());

        Event testEvent = new Event("Test 2", "Test 2", "Test 2", "Test 2", 2, new Date(10000), new Date());
        userEntrant.addWaitlistedEvent(testEvent);
        userEntrant.addWaitlistedEvent(new Event("Test 3", "Test 3", "Test 3", "Test 3", 3, new Date(10000), new Date()));
        assertEquals(3, userEntrant.getWaitlistedEvents().size());
        userEntrant.removeWaitlistedEvent(testEvent);
        assertEquals(2, userEntrant.getWaitlistedEvents().size());

        ArrayList<Event> waitlistedEvents = userEntrant.getWaitlistedEvents();
        for (int i = 0; i < waitlistedEvents.size(); i++) {
            Event event = waitlistedEvents.get(i);
            eventPrinting(event);
            System.out.println("------------------------------------------------------");
        }

        userEntrant.removeWaitlistedEvent(0);
        assertEquals(1, userEntrant.getWaitlistedEvents().size());
    }

    @Test
    public void organizerTest() {
        Organizer user = new Organizer(DEVICE_ID, "Test Organizer", "test@example.com", "000-000-0000");
        assertEquals("Organizer", user.getUserType());
        assertEquals(0, user.getCreatedEvents().size());

        addTestEvents(user);
        assertEquals(20, user.getCreatedEvents().size());
        System.out.println(user.getCreatedEvents().get(15).getId());

        ArrayList<Event> testEvents = createTestEvents();
        user.setCreatedEvents(testEvents);
        assertEquals(testEvents, user.getCreatedEvents());
        assertSame(testEvents, user.getCreatedEvents());

        ArrayList<Event> createdEvents = user.getCreatedEvents();
        for (int i = 0; i < createdEvents.size(); i++) {
            Event event = createdEvents.get(i);
            eventPrinting(event);
            System.out.println("------------------------------------------------------");
        }
    }
}