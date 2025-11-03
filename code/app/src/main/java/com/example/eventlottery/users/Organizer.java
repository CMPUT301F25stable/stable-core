package com.example.eventlottery.users;

import com.example.eventlottery.events.Event;

import java.util.ArrayList;

public class Organizer extends User {
    private ArrayList<String> createdEvents;

    // Firestore needs this for some reason
    public Organizer() {};

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

    public ArrayList<String> getCreatedEvents() {
        return createdEvents;
    }

    public void setCreatedEvents(ArrayList<String> createdEvents) {
        this.createdEvents = createdEvents;
    }

    public void createEvent(String newEvent) {
        createdEvents.add(newEvent);
    }

    public void removeEvent(String eventToRemove) {
        createdEvents.remove(eventToRemove);
    }
}