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
    /** List of events that the user has created */
    private ArrayList<String> createdEvents;
    /** Map of events the user is registered in. */
    private HashMap<String, String> registeredEvents; // Event ID: Notified/Rejected/Accepted
    /** List of joined event IDs. */
    private ArrayList<String> joinedEventIds = new ArrayList<>();
    /** Firebase Cloud Messaging token for push notifications. */
    private String fcmToken;
    /** Indicates if the user is banned from creating events. (US 03.07.01) */
    private boolean creationBan = false;
    private boolean isAdmin;
    /** A boolean for if a user has notifications turn on or off */
    private boolean notifications = true;

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

        this.name = "Your Name"; // Needed to add to display on your name on userPanel
        this.emailAddress = "";
        this.phoneNumber = "";
        this.waitlistedEvents = new ArrayList<>();
        this.registeredEvents = new HashMap<>();
        this.createdEvents = new ArrayList<>();
        this.isAdmin = false;
        this.notifications = true;
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
        this.createdEvents = new ArrayList<>();
        this.notifications = true;
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
        this.createdEvents = new ArrayList<>();
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

    /** Getter for isAdmin() */
    public boolean isAdmin() {
        return isAdmin;
    }

    /** Setter for isAdmin() */
    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    /**
     * Getter for notifications
     * */
    public boolean getNotifications() {
        return notifications;
    }

    /**
     * Setter for notifications
     * @param notifications boolean for notifications
     * */
    public void setNotifications(boolean notifications) {
        this.notifications = notifications;
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
     * Returns the list of event IDs created by this organizer.
     * @return A list of created event IDs.
     */
    public ArrayList<String> getCreatedEvents() {
        return createdEvents;
    }

    /**
     * Sets the list of created events for this organizer.
     * @param createdEvents A list of event IDs to associate with this organizer.
     */
    public void setCreatedEvents(ArrayList<String> createdEvents) {
        this.createdEvents = createdEvents;
    }

    /**
     * Adds a new event to the list of created events.
     * @param newEvent The event ID of the newly created event.
     */
    public void createEvent(String newEvent) {
        createdEvents.add(newEvent);
    }

    /**
     * Removes an event from the list of created events.
     * @param eventToRemove The event ID to remove from the list.
     */
    public void removeEvent(String eventToRemove) {
        createdEvents.remove(eventToRemove);
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

    // Helper function to get check status
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


    public boolean canReceiveNotifications() {
        return fcmToken != null && !fcmToken.isEmpty();
    }

    public boolean isCreationBan() {
        return creationBan;
    }

    public void setCreationBan(boolean creationBan) {
        this.creationBan = creationBan;
    }
}
