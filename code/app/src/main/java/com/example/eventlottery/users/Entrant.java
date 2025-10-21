package com.example.eventlottery.users;

import com.example.eventlottery.events.Event;

import java.util.ArrayList;
import java.util.HashMap;

public class Entrant {
    private ArrayList<Event> waitlistedEvents;
    private HashMap<Event, String> registeredEvents; // Event: Sent/Rejected/Accepted

    public Entrant(ArrayList<Event> waitlistedEvents, HashMap<Event, String> registeredEvents) {
        this.waitlistedEvents = waitlistedEvents;
        this.registeredEvents = registeredEvents;
    }
}