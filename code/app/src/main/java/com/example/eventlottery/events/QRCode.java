package com.example.eventlottery.events;

import java.io.Serializable;

/**
 * Represents a QR code associated with a specific event in the Event Lottery system.
 * <p>
 * Each {@code QRCode} stores a reference to the event it belongs to via the event's unique ID.
 * </p>
 */
public class QRCode implements Serializable { // To be implemented
    /** The unique ID of the event associated with this QR code. */
    private String eventId;
    /**
     * Default constructor required for serialization and deserialization.
     * <p>
     * Primarily used by Firestore or other frameworks that need to reconstruct
     * objects automatically.
     * </p>
     */
    public QRCode() {}
    /**
     * Constructs a new {@code QRCode} linked to a specific event.
     *
     * @param eventId the unique identifier of the event this QR code represents
     */
    public QRCode(String eventId) {
        this.eventId = eventId;
    }
    /**
     * Returns the unique ID of the event associated with this QR code.
     *
     * @return the event ID
     */
    public String getEventId() {
        return eventId;
    }
    /**
     * Sets or updates the event ID associated with this QR code.
     *
     * @param eventId the new event ID to link with this QR code
     */
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
}