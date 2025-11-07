package com.example.eventlottery.users;


import android.content.Context;
import android.provider.Settings;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Represents a user within the Event Lottery application.
 * <p>
 * Each {@code User} has identifying information. The class also tracks the user's participation
 * in events — including events they have joined, are waitlisted for, or are registered in.
 * </p>
 * <p>
 * This class implements {@link Serializable}
 * </p>
 */
public class User implements Serializable {
    /** Unique device-based user identifier. */
    private String id; // Device identifier
    /** User's name. */
    private String name;
    /** User's email address. */
    private String emailAddress;
    /** User's phone number. */
    private String phoneNumber;
    /** List of events the user has joined. */
    private ArrayList<String> waitlistedEvents;
    /** Map of events the user is registered in. */
    private HashMap<String, String> registeredEvents; // Event ID: Notified/Rejected/Accepted
    /** List of joined event IDs. */
    private ArrayList<String> joinedEventIds = new ArrayList<>();
    /** Firebase Cloud Messaging token for push notifications. */
    private String fcmToken;
    /** Default empty constructor required by Firestore. */
    public User() {}

    /**
     * Creates a new {@code User} based on device-specific information.
     * Primarily used when a user first opens the app on a new device.
     *
     * @param context The application context used to retrieve the device ID.
     */
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

    /**
     * Constructs a user with the specified ID, name, and email.
     *
     * @param id The user's unique identifier.
     * @param name The user's display name.
     * @param emailAddress The user's email address.
     */
    public User(String id, String name, String emailAddress) {
        this.id = id;
        this.name = name;
        this.emailAddress = emailAddress;
        this.phoneNumber = "";
        this.waitlistedEvents = new ArrayList<>();
        this.registeredEvents = new HashMap<>();
    }


    /**
     * Constructs a user with the specified ID, name, email, and phone number.
     *
     * @param id The user's unique identifier.
     * @param name The user's display name.
     * @param emailAddress The user's email address.
     * @param phoneNumber The user's phone number.
     */
    public User(String id, String name, String emailAddress, String phoneNumber) {
        this.id = id;
        this.name = name;
        this.emailAddress = emailAddress;
        this.phoneNumber = phoneNumber;
        this.waitlistedEvents = new ArrayList<>();
        this.registeredEvents = new HashMap<>();
    }

    /**
     * Returns the user type as a string.
     * Subclasses (e.g., Organizer, Admin) may override this.
     *
     * @return The user type label.
     */
    public String getUserType() {
        return "User";
    }

    /** @return The user’s unique identifier. */
    public String getId() {
        return id;
    }
    /** @param id Sets the user’s unique identifier. */
    public void setId(String id) {
        this.id = id;
    }
    /** @return The user’s display name. */
    public String getName() {
        return name;
    }
    /** @param name Sets the user’s display name. */
    public void setName(String name) {
        this.name = name;
    }
    /** @return The user’s email address. */
    public String getEmailAddress() {
        return emailAddress;
    }
    /** @param emailAddress Sets the user’s email address. */
    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }
    /** @return The user’s phone number. */
    public String getPhoneNumber() {
        return phoneNumber;
    }
    /** @param phoneNumber Sets the user’s phone number. */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    /** @return A list of event IDs the user is waitlisted for. */
    public ArrayList<String> getWaitlistedEvents() {
        return waitlistedEvents;
    }
    /** @param waitlistedEvents Sets the list of waitlisted event IDs. */
    public void setWaitlistedEvents(ArrayList<String> waitlistedEvents) {
        this.waitlistedEvents = waitlistedEvents;
    }
    /** @return A map of registered event IDs and their statuses. */
    public HashMap<String, String> getRegisteredEvents() {
        return registeredEvents;
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

    /**
     * Retrieves the user’s registration status for a given event.
     *
     * @param event The event ID.
     * @return The status string (e.g., "Accepted", "Declined", "Not Registered").
     */
    public String getStatusForEvent(String event) {
        return registeredEvents.getOrDefault(event, "Not Registered");
    }

    /** @return A string representation of the user and their key details. */
    @NonNull
    @Override
    public String toString() {
        return "ID: " + getId() + "\nName: " + getName() + "\nEmail Address: " + getEmailAddress() + "\nPhone Number: " + getPhoneNumber();
    }

    /** @param fcmToken Sets the user’s Firebase Cloud Messaging token. */
    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    /** @return The user’s Firebase Cloud Messaging token. */
    public String getFcmToken() {
        return fcmToken;
    }
}