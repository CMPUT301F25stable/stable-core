package com.example.eventlottery.users;

import com.example.eventlottery.events.Event;

import java.util.ArrayList;

public class Admin extends Organizer {
    public Admin(String id, String name, String emailAddress, ArrayList<Event> createdEvents) {
        super(id, name, emailAddress, createdEvents);
    }

    public Admin(String id, String name, String emailAddress, String phoneNumber, ArrayList<Event> createdEvents) {
        super(id, name, emailAddress, phoneNumber, createdEvents);
    }

    @Override
    public String getUserType() {
        return "Admin";
    }
}