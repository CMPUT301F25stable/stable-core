package com.example.eventlottery.users;

import com.example.eventlottery.events.Event;

import java.util.ArrayList;

public class Organizer extends User {
    private ArrayList<Event> createdEvents;

    public Organizer(String id, String name, String emailAddress, ArrayList<Event> createdEvents) {
        super(id, name, emailAddress);
        this.createdEvents = createdEvents;
    }

    public Organizer(String id, String name, String emailAddress, String phoneNumber, ArrayList<Event> createdEvents) {
        super(id, name, emailAddress, phoneNumber);
        this.createdEvents = createdEvents;
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
}