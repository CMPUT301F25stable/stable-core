package com.example.eventlottery.users;


import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User implements Serializable {
    private String id; // Device identifier
    private String name;
    private String emailAddress;
    private String phoneNumber;
    private ArrayList<String> waitlistedEvents;
    private HashMap<String, String> registeredEvents; // Event ID: Notified/Rejected/Accepted
    private ArrayList<String> joinedEventIds = new ArrayList<>();

    private String fcmToken;

    // Firestore needs this for some reason
    public User() {}

    // Empty constructor for new users (needs Context though)
    public User(Context context) {
        // Gets unique device ID
        this.id = Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        this.name = "Your Name"; // Needed to add to display on your name on userPanel
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

    public void setId(String id) {
        this.id = id;
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

    // in User.java
    public HashMap<String, String> getRegisteredEvents() {
        if (registeredEvents == null) registeredEvents = new HashMap<>();
        return registeredEvents;
    }


    /**
     * Checks if the user has joined an event.
     * @param eventId
     * @return
     */
    public boolean isWaitlisted(String eventId) {
        return waitlistedEvents != null && waitlistedEvents.contains(eventId);
    }

    /**
     * Sets the list of joined event IDs.
     * @param ids
     */
    public void setWaitlistedEventIds(List<String> ids) {
        // called after reading Firestore
        if (ids == null) this.waitlistedEvents = new ArrayList<>();
        else this.waitlistedEvents = new ArrayList<>(ids);
    }

    /**
     * Gets the list of joined event IDs.
     * @return
     */
    public List<String> getWaitlistedEventIds() {
        return waitlistedEvents == null ? Collections.emptyList() : waitlistedEvents;
    }

    /**
     * Adds an event to the joined list.
     * @param eventId
     */
    public void AddJoinedWaitlist(String eventId) {
        if (waitlistedEvents == null) waitlistedEvents = new ArrayList<>();
        if (!waitlistedEvents.contains(eventId)) waitlistedEvents.add(eventId);
    }

    /**
     * Removes an event from the joined list.
     * @param eventId
     */
    public void RemoveLeftWaitlist(String eventId) {
        if (waitlistedEvents != null) waitlistedEvents.remove(eventId);
    }

    /**
     * Checks if the user has joined an event.
     * @param eventId
     * @return
     */
    public boolean isJoined(String eventId) {
        return joinedEventIds != null && joinedEventIds.contains(eventId);
    }

    /**
     * Sets the list of joined event IDs.
     * @param ids
     */
    public void setJoinedEventIds(List<String> ids) {
        // called after reading Firestore
        if (ids == null) this.joinedEventIds = new ArrayList<>();
        else this.joinedEventIds = new ArrayList<>(ids);
    }

    /**
     * Gets the list of joined event IDs.
     * @return
     */
    public List<String> getJoinedEventIds() {
        return joinedEventIds == null ? Collections.emptyList() : joinedEventIds;
    }

    /**
     * Adds an event to the joined list.
     * @param eventId
     */
    public void markJoined(String eventId) {
        if (joinedEventIds == null) joinedEventIds = new ArrayList<>();
        if (!joinedEventIds.contains(eventId)) joinedEventIds.add(eventId);
    }

    /**
     * Removes an event from the joined list.
     * @param eventId
     */
    public void markLeft(String eventId) {
        if (joinedEventIds != null) joinedEventIds.remove(eventId);
    }


    /**
     * Adds an event to the waitlist.
     * @param registeredEvents
     */
    public void setRegisteredEvents(HashMap<String, String> registeredEvents) {
        this.registeredEvents = registeredEvents;
    }

    /**
     * Given an event ID, remove it from registered events.
     * @param eventToRemove The event ID to remove.
     */
    public void removeRegisteredEvent(String eventToRemove) {
        registeredEvents.remove(eventToRemove);
    }

    /** USER STORY 01.05.02 - Accept invitation
     * Updates both local and the firebase
     * @param eventId The event ID to accept
     * */
    public void acceptInvitation(String eventId) {
        if (registeredEvents == null || !registeredEvents.containsKey(eventId)) {
            Log.e("User", "Event not found in registered events");
            return;
        }

        // Update local state
        registeredEvents.put(eventId, "Accepted");

        // Update Firebase
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> updates = new HashMap<>();
        updates.put("registeredEvents." + eventId, "Accepted");

        db.collection("users").document(id)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d("User", "Invitation accepted successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e("User", "Error acception invitation", e);
                    // Revert back
                    registeredEvents.put(eventId, "Notified");
                });
    }


    /** USER STORY 01.05.03 - Decline invitation
     * Updates both local state and Firebase
     * @param eventId an Event object is passed which we verify if its an actual event
     * */
    public void declineInvitation(String eventId) {
        if (registeredEvents == null || !registeredEvents.containsKey(eventId)) {
            Log.e("User", "Event not found in registered events");
            return;
        }

        // Update local state
        registeredEvents.put(eventId, "Declined");

        // Update Firebase
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> updates = new HashMap<>();
        updates.put("registeredEvents." + eventId, "Declined");

        db.collection("users").document(id)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d("User", "Invitation declined successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e("User", "Error declining invitation", e);
                    // Revert local state on failure
                    registeredEvents.put(eventId, "Notified");
                });
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

    // FCM Token
    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public String getFcmToken() {
        return fcmToken;
    }
}