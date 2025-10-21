package com.example.eventlottery.events;

import androidx.annotation.NonNull;

import java.util.Date;
import java.util.UUID;

public class Event {
    private String id; // UUID as a string
    private String name;
    private String description;
    private String location; // Could also be a HashMap so we can easily grab individual location info (street, city, etc..)
    private Date startTime;
    private Date endTime;

    @NonNull
    private String generateUUID() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    public Event(String name, String description, String location, Date startTime, Date endTime) { // For new events (not yet in database)
        this.id = generateUUID();
        this.name = name;
        this.description = description;
        this.location = location;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Event(String id, String name, String description, String location, Date startTime, Date endTime) { // For pre-existing events
        this.id = id;
        this.name = name;
        this.description = description;
        this.location = location;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
}
