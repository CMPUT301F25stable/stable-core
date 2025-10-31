package com.example.eventlottery.events;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class Event implements Serializable {
    private String id; // UUID as a string
    private String name;
    private String description;
    private String location; // Could also be a HashMap so we can easily grab individual location info (street, city, etc..)
    private String organizer;
    private int image;
    private Date startTime;
    private Date endTime;
    private String[] formattedStartTime;
    private String[] formattedEndTime;
    private QRCode qrCode;
    private Waitlist waitlist;

    @NonNull
    private String generateUUID() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    private void formatDates() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy'-'h:mm a", Locale.CANADA);
        String formattedStart = dateFormat.format(this.startTime);
        String formattedEnd = dateFormat.format(this.endTime);
        this.formattedStartTime = formattedStart.split("-");
        this.formattedEndTime = formattedEnd.split("-");
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
        this.waitlist = new Waitlist();
        formatDates();
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
        this.waitlist = new Waitlist();
        formatDates();
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

    // Returns a String with a date (ex. October 30, 2023)
    public String getFormattedStartDate() {
        return formattedStartTime[0];
    }

    public String getFormattedEndDate() {
        return formattedEndTime[0];
    }

    // Returns a String with a time (ex. 3:30 p.m.)
    public String getFormattedStartTime() {
        return formattedStartTime[1];
    }

    public String getFormattedEndTime() {
        return formattedEndTime[1];
    }

    public QRCode getQrCode() {
        return qrCode;
    }

    public void setQrCode(QRCode qrCode) {
        this.qrCode = qrCode;
    }
}