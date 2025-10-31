package com.example.eventlottery.users;

import com.example.eventlottery.events.Event;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class Entrant implements Serializable {
    private ArrayList<Event> waitlistedEvents;
    private HashMap<Event, String> registeredEvents; // Event: Notified/Rejected/Accepted

    public Entrant() {
        waitlistedEvents = new ArrayList<>();
        registeredEvents = new HashMap<>();
    }

    public ArrayList<Event> getWaitlistedEvents() {
        return waitlistedEvents;
    }
    public HashMap<Event, String> getRegisteredEvents() {
        return registeredEvents;
    }
    public void setWaitlistedEvents(ArrayList<Event> waitlistedEvents) {
        this.waitlistedEvents = waitlistedEvents;
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

    public void removeWaitlistedEvent(int eventIndex) {
        waitlistedEvents.remove(eventIndex);
    }

    public void removeRegisteredEvent(Event eventToRemove) {
        registeredEvents.remove(eventToRemove);
    }

    /** USER STORY 01.05.02 - Accept invitation
     * @param event an Event object is passed which we verify if its an actual event
     * */
    public void acceptInvitation(Event event) {
        if (registeredEvents.containsKey(event)) {
            registeredEvents.put(event, "Accepted");
        }
    }

    /** USER STORY 01.05.03 - Decline invitation
     * @param event an Event object is passed which we verify if its an actual event
     * */
    public void declineInvitation(Event event) {
        if (registeredEvents.containsKey(event)) {
            registeredEvents.put(event, "Declined");
        }
    }

    // Helper function to get check status
    public String getStatusForEvent(Event event) {
        return registeredEvents.getOrDefault(event, "Not Registered");
    }
}