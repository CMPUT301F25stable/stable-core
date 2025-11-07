package com.example.eventlottery.model;

/**
 * Represents the data for an event in a format suitable for displaying
 * in a list or user interface component, such as a RecyclerView.
 * <p>
 * This class holds basic information about an event, including its name,
 * description, date, time, location, organizer, and image.
 * </p>
 */
public class EventListData {
    /** The name of the event. */
    String eventName;
    /** A short description of the event. */
    String eventDescription;
    /** The date of the event. */
    String eventDate;
    /** The time of the event. */
    String eventTime;
    /** The location of the event. */
    String eventLocation;
    /** The organizer of the event. */
    String eventOrganizer;
    /** The URL of the image associated with the event. */
    String eventImage;

    /**
     * Constructs a new {@code EventListData} object with all event details.
     *
     * @param eventName        the name of the event
     * @param eventImage       the URL or resource path for the event's image
     * @param eventDescription a short description of the event
     * @param eventDate        the formatted date of the event
     * @param eventTime        the formatted time of the event
     * @param eventLocation    the location of the event
     * @param eventOrganizer   the name of the event organizer
     */
    public EventListData(String eventName, String eventImage, String eventDescription, String eventDate, String eventTime, String eventLocation, String eventOrganizer) {
        this.eventName = eventName;
        this.eventImage = eventImage;
        this.eventDescription = eventDescription;
        this.eventDate = eventDate;
        this.eventTime = eventTime;
        this.eventLocation = eventLocation;
        this.eventOrganizer = eventOrganizer;
    }
    /** @return the name of the event */
    public String getEventName() {
        return eventName;
    }
    /** @param eventName the name of the event to set */
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }
    /** @return the URL or path of the event image */
    public String getEventImage() {
        return eventImage;
    }
    /** @param eventImage the URL or path of the event image to set */
    public void setEventImage(String eventImage) {
        this.eventImage = eventImage;
    }
    /** @return a short description of the event */
    public String getEventDescription() {
        return eventDescription;
    }
    /** @param eventDescription the description of the event to set */
    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }
    /** @return the formatted date of the event */
    public String getEventDate() {
        return eventDate;
    }
    /** @param eventDate the formatted date of the event to set */
    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }
    /** @return the formatted time of the event */
    public String getEventTime() {
        return eventTime;
    }
    /** @param eventTime the formatted time of the event to set */
    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }
    /** @return the location of the event */
    public String getEventLocation() {
        return eventLocation;
    }
    /** @param eventLocation the location of the event to set */
    public void setEventLocation(String eventLocation) {
        this.eventLocation = eventLocation;
    }
    /** @return the name of the event organizer */
    public String getEventOrganizer() {
        return eventOrganizer;
    }
    /** @param eventOrganizer the name of the event organizer to set */
    public void setEventOrganizer(String eventOrganizer) {
        this.eventOrganizer = eventOrganizer;
    }

}
