package com.example.eventlottery.events;

import androidx.annotation.NonNull;

import com.example.eventlottery.users.User;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class Event implements Serializable {
    private String id; // UUID as a string
    private String name;
    private String description;
    private String location; // Could also be a HashMap so we can easily grab individual location info (street, city, etc..)
    private String organizer;
    private String image;
    private Date startTime;
    private Date endTime;
    private String formattedStartDate;
    private String formattedStartTime;
    private String formattedEndDate;
    private String formattedEndTime;
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

        String[] startParts = formattedStart.split("-");
        String[] endParts = formattedEnd.split("-");

        this.formattedStartDate = startParts[0];
        this.formattedStartTime = startParts[1];
        this.formattedEndDate = endParts[0];
        this.formattedEndTime = endParts[1];
    }

    // Firestore needs an empty constructor
    public Event() {
        this.waitlist = new Waitlist();  // ensure it's non-null to stop crash
    }

    public Event(String name, String description, String location, String organizer, String image, Date startTime, Date endTime) { // For new events (not yet in database)
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

    public Event(String id, String name, String description, String location, String organizer, String image, Date startTime, Date endTime) { // For pre-existing events
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

    public void setId(String id) { this.id = id; }

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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
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

    public String getFormattedStartDate() {
        return formattedStartDate;
    }

    public void setFormattedStartDate(String formattedStartDate) {
        this.formattedStartDate = formattedStartDate;
    }

    public String getFormattedStartTime() {
        return formattedStartTime;
    }

    public void setFormattedStartTime(String formattedStartTime) {
        this.formattedStartTime = formattedStartTime;
    }

    public String getFormattedEndDate() {
        return formattedEndDate;
    }

    public void setFormattedEndDate(String formattedEndDate) {
        this.formattedEndDate = formattedEndDate;
    }

    public String getFormattedEndTime() {
        return formattedEndTime;
    }

    public void setFormattedEndTime(String formattedEndTime) {
        this.formattedEndTime = formattedEndTime;
    }

    public QRCode getQrCode() {
        return qrCode;
    }

    public void setQrCode(QRCode qrCode) {
        this.qrCode = qrCode;
    }


    public Waitlist getWaitlist() { return waitlist; }

    public void setWaitlist(Waitlist waitlist) { this.waitlist = waitlist; }

    /**
     * Adds one user to the waitlist.
     * @param user The user to add to waitlist.
     */
    public void addToWaitlist(User user) {
        this.waitlist.addUser(user);
    }

    /**
     * Removes a user from the waitlist.
     * @param user The user to remove.
     */
    public void removeFromWaitlist(User user) {
        this.waitlist.removeUser(user);
    }

    /**
     * Sets the waitlist max
     * @param newMax The new waitlist max.
     */
    public void setWaitlistMax(int newMax) {this.waitlist.setMaxSize(newMax);}

    /**
     * Gets current waitlist max.
     */
    public int getWaitlistMax() {return this.waitlist.getMaxSize();};

    /**
     * Searches for an Event in an ArrayList of events.
     * @param events The Event ArrayList to be searched.
     * @param eventId The Event ID that will be compared to the Event ID of the events in the ArrayList.
     * @return The index of an event if found, otherwise returns -1.
     */
    public static int findEventById(ArrayList<Event> events, String eventId) {
        for (int i = 0; i < events.size(); i++) {
            Event event = events.get(i);
            if (event.getId().equalsIgnoreCase(eventId)) {
                return i;
            }
        }
        return -1;
    }
}