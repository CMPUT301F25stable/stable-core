package com.example.eventlottery.events;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;

public class Event {
    private String id; // UUID as a string
    private String name;
    private String description;
    private String location; // Could also be a HashMap so we can easily grab individual location info (street, city, etc..)
    private String organizer;
    private int image;
    private Date startTime;
    private Date endTime;
    private QRCode qrCode;

    @NonNull
    private String generateUUID() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    public Event(String name, String description, String location, String organizer, int image, Date startTime, Date endTime) { // For new events (not yet in database)
        this.id = generateUUID();
        this.name = name;
        this.description = description;
        this.location = location;
        this.organizer = organizer;
        this.image = image;
        this.startTime = startTime;
        this.endTime = endTime;
        this.qrCode = new QRCode(this.id);
    }

    public Event(String id, String name, String description, String location, String organizer, int image, Date startTime, Date endTime) { // For pre-existing events
        this.id = id;
        this.name = name;
        this.description = description;
        this.location = location;
        this.organizer = organizer;
        this.image = image;
        this.startTime = startTime;
        this.endTime = endTime;
        this.qrCode = new QRCode(this.id);
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

    public String getOrganizer() {
        return organizer;
    }

    public void setOrganizer(String organizer) {
        this.organizer = organizer;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
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

    public QRCode getQrCode() {
        return qrCode;
    }

    public void setQrCode(QRCode qrCode) {
        this.qrCode = qrCode;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof Event) {
            Event otherEvent = (Event) obj;
            return Objects.equals(this.id, otherEvent.getId());
        }
        return false;
    }
}