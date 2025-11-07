package com.example.eventlottery.users;

import android.content.Context;

import com.example.eventlottery.events.Event;

import java.util.ArrayList;

/**
 * Represents an organizer user within the Event Lottery application.
 */
public class Organizer extends User {
    /** List of event IDs created by this organizer. */
    private ArrayList<String> createdEvents;

    /**Default no-argument constructor required by Firestore for object deserialization.*/
    public Organizer() {};

    /**
     * Method for creating new organizers
     * @param context
     */
    public Organizer(Context context) {
        super(context);
        this.createdEvents = new ArrayList<>();
    }

    /**
     * Constructs an organizer with the given ID, name, and email address.
     *
     * @param id The organizer’s unique identifier.
     * @param name The organizer’s display name.
     * @param emailAddress The organizer’s email address.
     */
    public Organizer(String id, String name, String emailAddress) {
        super(id, name, emailAddress);
        createdEvents = new ArrayList<>();
    }

    /**
     * Constructs an organizer with the given ID, name, email address, and phone number.
     *
     * @param id The organizer’s unique identifier.
     * @param name The organizer’s display name.
     * @param emailAddress The organizer’s email address.
     * @param phoneNumber The organizer’s phone number.
     */
    public Organizer(String id, String name, String emailAddress, String phoneNumber) {
        super(id, name, emailAddress, phoneNumber);
        createdEvents = new ArrayList<>();
    }

    /**
     * Returns the user type for this object.
     * @return The string {@code "Organizer"} to identify this user’s role.
     */
    @Override
    public String getUserType() {
        return "Organizer";
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
}