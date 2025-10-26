package com.example.eventlottery.users;

import com.example.eventlottery.events.Event;

import java.util.ArrayList;

public class Organizer extends User {
    private ArrayList<Event> createdEvents;

    public Organizer(String id, String name, String emailAddress) {
        super(id, name, emailAddress);
        createdEvents = new ArrayList<>();
    }

    public Organizer(String id, String name, String emailAddress, String phoneNumber) {
        super(id, name, emailAddress, phoneNumber);
        createdEvents = new ArrayList<>();
    }

    @Override
    public String getUserType() {
        return "Organizer";
    }

    public ArrayList<Event> getCreatedEvents() {
        return createdEvents;
    }

    public void setCreatedEvents(ArrayList<Event> createdEvents) {
        this.createdEvents = createdEvents;
    }

    public void createEvent(Event newEvent) {
        createdEvents.add(newEvent);
    }

    public void removeEvent(Event eventToRemove) {
        createdEvents.remove(eventToRemove);
    }
}