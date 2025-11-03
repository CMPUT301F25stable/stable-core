package com.example.eventlottery.users;


import android.content.Context;
import android.provider.Settings;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class User implements Serializable {
    private String id; // Device identifier
    private String name;
    private String emailAddress;
    private String phoneNumber;
    private ArrayList<String> waitlistedEvents;
    private HashMap<String, String> registeredEvents; // Event ID: Notified/Rejected/Accepted

    // Firestore needs this for some reason
    public User() {}

    // Empty constructor for new users (needs Context though)
    public User(Context context) {
        // Gets unique device ID
        this.id = Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
        this.name = "";
        this.emailAddress = "";
        this.phoneNumber = "";
        this.waitlistedEvents = new ArrayList<>();
        this.registeredEvents = new HashMap<>();
    }

    public User(String id, String name, String emailAddress) {
        this.id = id;
        this.name = name;
        this.emailAddress = emailAddress;
        this.phoneNumber = "";
        this.waitlistedEvents = new ArrayList<>();
        this.registeredEvents = new HashMap<>();
    }

    public User(String id, String name, String emailAddress, String phoneNumber) {
        this.id = id;
        this.name = name;
        this.emailAddress = emailAddress;
        this.phoneNumber = phoneNumber;
        this.waitlistedEvents = new ArrayList<>();
        this.registeredEvents = new HashMap<>();
    }

    public String getUserType() {
        return "User";
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

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public ArrayList<String> getWaitlistedEvents() {
        return waitlistedEvents;
    }

    public void setWaitlistedEvents(ArrayList<String> waitlistedEvents) {
        this.waitlistedEvents = waitlistedEvents;
    }

    public HashMap<String, String> getRegisteredEvents() {
        return registeredEvents;
    }

    public void setRegisteredEvents(HashMap<String, String> registeredEvents) {
        this.registeredEvents = registeredEvents;
    }

    /**
     * Given an event ID, remove it from the waitlist.
     * @param eventToRemove The event ID to remove.
     */
    public void removeWaitlistedEvent(String eventToRemove) {
        waitlistedEvents.remove(eventToRemove);
    }

    /**
     * Given an event index, remove it from the waitlist.
     * @param eventIndex The event index to remove.
     */
    public void removeWaitlistedEvent(int eventIndex) {
        waitlistedEvents.remove(eventIndex);
    }

    /**
     * Given an event ID, remove it from registered events.
     * @param eventToRemove The event ID to remove.
     */
    public void removeRegisteredEvent(String eventToRemove) {
        registeredEvents.remove(eventToRemove);
    }

    /** USER STORY 01.05.02 - Accept invitation
     * @param event an Event object is passed which we verify if its an actual event
     * */
    public void acceptInvitation(String event) {
        if (registeredEvents.containsKey(event)) {
            registeredEvents.put(event, "Accepted");
        }
    }


    /** USER STORY 01.05.03 - Decline invitation
     * @param event an Event object is passed which we verify if its an actual event
     * */
    public void declineInvitation(String event) {
        if (registeredEvents.containsKey(event)) {
            registeredEvents.put(event, "Declined");
        }
    }

    // Helper function to get check status
    public String getStatusForEvent(String event) {
        return registeredEvents.getOrDefault(event, "Not Registered");
    }

    @NonNull
    @Override
    public String toString() {
        return "ID: " + getId() + "\nName: " + getName() + "\nEmail Address: " + getEmailAddress() + "\nPhone Number: " + getPhoneNumber();
    }
}