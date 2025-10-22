package com.example.eventlottery.users;

import com.example.eventlottery.events.Event;

import java.util.ArrayList;
import java.util.HashMap;

public class Entrant {
    private ArrayList<Event> waitlistedEvents;
    private HashMap<Event, String> registeredEvents; // Event: Notified/Rejected/Accepted

    public Entrant() {
        waitlistedEvents = new ArrayList<>();
        registeredEvents = new HashMap<>();
    }

    public ArrayList<Event> getWaitlistedEvents() {
        return waitlistedEvents;
    }

    public void setWaitlistedEvents(ArrayList<Event> waitlistedEvents) {
        this.waitlistedEvents = waitlistedEvents;
    }

    public HashMap<Event, String> getRegisteredEvents() {
        return registeredEvents;
    }

    public void setRegisteredEvents(HashMap<Event, String> registeredEvents) {
        this.registeredEvents = registeredEvents;
    }

    public void addWaitlistedEvent(Event newEvent) {
        waitlistedEvents.add(newEvent);
    }

    public void addRegisteredEvent(Event newEvent, String status) {
        registeredEvents.put(newEvent, status);
    }

    public void removeWaitlistedEvent(Event eventToRemove) {
        waitlistedEvents.remove(eventToRemove);
    }

    public void removeRegisteredEvent(Event eventToRemove) {
        registeredEvents.remove(eventToRemove);
    }
}