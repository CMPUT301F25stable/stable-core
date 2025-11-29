package com.example.eventlottery.events;

import androidx.annotation.NonNull;

import com.example.eventlottery.users.User;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Represents an event in the Event Lottery system.
 * <p>
 * Each {@code Event} contains identifying information, timing details, an associated QR code,
 * and a {@link Waitlist} for managing users who sign up for participation.
 * </p>
 */
public class Event implements Serializable {
    /** Unique identifier for the event (UUID as a string). */
    private String id; // UUID as a string
    /** Name of the event. */
    private String name;
    /** Description of the event. */
    private String description;
    /** Location of the event. */
    private String location; // Could also be a HashMap so we can easily grab individual location info (street, city, etc..)
    /** Organizer of the event. */
    private String organizer;
    /** Download path of event image. */
    private String image;
    /** Storage path of event image */
    private String storagePath = "";
    /** Start time of the event. */
    private Date startTime;
    /** End time of the event. */
    private Date endTime;
    /** Formatted start date string (e.g., "March 5, 2025"). */
    private String formattedStartDate;
    /** Formatted start time string (e.g., "2:30 PM"). */
    private String formattedStartTime;
    /** Formatted end date string. */
    private String formattedEndDate;
    /** Formatted end time string. */
    private String formattedEndTime;
    /** Waitlist containing users who have registered or are waiting to participate. */
    private Waitlist waitlist;
    /** Finalized list of users who have been selected as winners. */
    private java.util.List<String> filterTags = new java.util.ArrayList<>();
    private FinalizedList finalizedList;
    /** List of users who have been selected as winners. */
    private List<User> chosenEntrants = new ArrayList<>();
    private List<String> selectedIds = new ArrayList<>();
    /** Lottery system used for selecting winners. */
    private transient LotterySystem lotteryEngine;
    /** Determines if geolocation is on (true) or not (false) */
    private boolean geolocation;
    /** An array of all user locations for an event. The map stores the user ID, latitude, & longitude. */
    private ArrayList<Map<String, Object>> userLocations;
    /** List of user IDs who were notified but declined the invitation*/
    private List<String> cancelledEntrants = new ArrayList<>();

    /**
     * Generates a new universally unique identifier (UUID) for an event.
     *
     * @return a randomly generated UUID as a string
     */
    @NonNull
    private String generateUUID() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    /**
     * Formats the start and end dates/times of the event into readable strings.
     * <p>
     * Uses the {@code Locale.CANADA} date format: "MMMM d, yyyy - h:mm a".
     * Populates formatted date and time fields for display purposes.
     * </p>
     */
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

    /**
     * Empty constructor required by Firestore for object deserialization.
     * Initializes a non-null {@link Waitlist} to prevent crashes.
     */
    public Event() {
        this.waitlist = new Waitlist();  // ensure it's non-null to stop crash
        this.filterTags = new java.util.ArrayList<>();
        this.userLocations = new ArrayList<>();
        this.chosenEntrants = new ArrayList<>();
        this.selectedIds = new ArrayList<>();
        this.cancelledEntrants = new ArrayList<>();
    }

    public Event(String name, String description, String location, String organizer, String image, Date startTime, Date endTime, List<String> filterTags, boolean geolocation) { // For new events (not yet in database)
        this.id = generateUUID();
        this.name = name;
        this.description = description;
        this.location = location;
        this.organizer = organizer;
        this.image = image;
        this.startTime = startTime;
        this.endTime = endTime;
        this.waitlist = new Waitlist();
        this.filterTags = filterTags != null ? filterTags : new ArrayList<>();
        this.geolocation = geolocation;
        this.userLocations = new ArrayList<>();
        this.chosenEntrants = new ArrayList<>();
        this.selectedIds = new ArrayList<>();
        this.cancelledEntrants = new ArrayList<>();
        formatDates();
    }

    // Old method: I'm keeping this so it doesn't break every instance of an event being created - John
    public Event(String name, String description, String location, String organizer, String image, Date startTime, Date endTime, List<String> filterTags) {
        this.id = generateUUID();
        this.name = name;
        this.description = description;
        this.location = location;
        this.organizer = organizer;
        this.image = image;
        this.startTime = startTime;
        this.endTime = endTime;
        this.waitlist = new Waitlist();
        this.filterTags = filterTags != null ? filterTags : new ArrayList<>();
        this.geolocation = false;
        this.userLocations = new ArrayList<>();
        this.chosenEntrants = new ArrayList<>();
        this.selectedIds = new ArrayList<>();
        this.cancelledEntrants = new ArrayList<>();
        formatDates();
    }

    public Event(String id, String name, String description, String location, String organizer, String image, Date startTime, Date endTime, List<String> filterTags) { // For pre-existing events
        this.finalizedList = new FinalizedList();
        formatDates();
    }

    /**
     * Constructs a new {@code Event} with given details.
     * <p>
     * This constructor generates a new UUID for the event.
     * </p>
     *
     * @param name        the name of the event
     * @param description a description of the event
     * @param location    the location of the event
     * @param organizer   the name or ID of the event organizer
     * @param image       the image URL or path
     * @param startTime   the start time of the event
     * @param endTime     the end time of the event
     */
    public Event(String id, String name, String description, String location, String organizer, String image, Date startTime, Date endTime) { // For pre-existing events
        this.id = id;
        this.name = name;
        this.description = description;
        this.location = location;
        this.organizer = organizer;
        this.image = image;
        this.startTime = startTime;
        this.endTime = endTime;
        this.waitlist = new Waitlist();
        this.filterTags = filterTags != null ? filterTags : new ArrayList<>();
        this.finalizedList = new FinalizedList();
        formatDates();
    }

    public void setId(String id) { this.id = id; }

    /** @return the unique ID of the event */
    public String getId() {
        return id;
    }

    /** @return the name of the event */
    public String getName() {
        return name;
    }

    /** @param name the new name of the event */
    public void setName(String name) {
        this.name = name;
    }

    /** @return the event description */
    public String getDescription() {
        return description;
    }

    /** @param description the new event description */
    public void setDescription(String description) {
        this.description = description;
    }

    /** @return the event location */
    public String getLocation() {
        return location;
    }

    /** @param location the new event location */
    public void setLocation(String location) {
        this.location = location;
    }

    /** @return the event organizer */
    public String getOrganizer() {
        return organizer;
    }

    /** @param organizer the new event organizer */
    public void setOrganizer(String organizer) {
        this.organizer = organizer;
    }

    /** @return the image URL or path of the event */
    public String getImage() {
        return image;
    }

    /** @param image the new image URL or path */
    public void setImage(String image) {
        this.image = image;
    }

    /** Getter for storage path */
    public String getStoragePath() {
        return storagePath;
    }

    /** Setter for storage path */
    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    /** @return the event start time */
    public Date getStartTime() {
        return startTime;
    }

    /** @param startTime the new event start time */
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    /** @return the event end time */
    public Date getEndTime() {
        return endTime;
    }

    /** @param endTime the new event end time */
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    /** @return geolocation status (true or false) */
    public boolean getGeolocation() {
        return geolocation;
    }

    /** @param geolocation the new geolocation status */
    public void setGeolocation(boolean geolocation) {
        this.geolocation = geolocation;
    }

    public List<String> getFilterTags() {
        return filterTags;
    }

    public void setFilterTags(List<String> filterTags) {
        this.filterTags = filterTags;
    }

    public void addFilterTag(String tag) {
        if (this.filterTags == null) {
            this.filterTags = new ArrayList<>();
        }
        this.filterTags.add(tag);
    }

    public void removeFilterTag(String tag) {
        if (this.filterTags != null) {
            this.filterTags.remove(tag);
        }
    }

    /** @return the formatted start date string */
    public String getFormattedStartDate() {
        return formattedStartDate;
    }

    /** @param formattedStartDate the formatted start date string */
    public void setFormattedStartDate(String formattedStartDate) {
        this.formattedStartDate = formattedStartDate;
    }

    /** @return the formatted start time string */
    public String getFormattedStartTime() {
        return formattedStartTime;
    }

    /** @param formattedStartTime the formatted start time string */
    public void setFormattedStartTime(String formattedStartTime) {
        this.formattedStartTime = formattedStartTime;
    }

    /** @return the formatted end date string */
    public String getFormattedEndDate() {
        return formattedEndDate;
    }

    /** @param formattedEndDate the formatted end date string */
    public void setFormattedEndDate(String formattedEndDate) {
        this.formattedEndDate = formattedEndDate;
    }

    /** @return the formatted end time string */
    public String getFormattedEndTime() {
        return formattedEndTime;
    }

    /** @param formattedEndTime the formatted end time string */
    public void setFormattedEndTime(String formattedEndTime) {
        this.formattedEndTime = formattedEndTime;
    }

    /** @return the {@link Waitlist} associated with this event */
    public Waitlist getWaitlist() { return waitlist; }
    /** @param waitlist the new {@link Waitlist} to associate with this event */
    public void setWaitlist(Waitlist waitlist) { this.waitlist = waitlist; }

    /** getter for userLocations. */
    public ArrayList<Map<String, Object>> getUserLocations() {
        return userLocations;
    }

    /** setter for userLocations */
    public void setUserLocations(ArrayList<Map<String, Object>> userLocations) {
        this.userLocations = userLocations;
    }

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
     * Returns the current maximum size of the event’s waitlist.
     *
     * @return the waitlist’s maximum capacity
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

    public FinalizedList getFinalizedList() {
        return finalizedList;
    }

    public void setFinalizedList(FinalizedList finalizedList) {
        this.finalizedList = finalizedList;
    }

    /**
     * Adds a user to the finalized list.
     * @param user The user to add.
     */
    public void addToFinalizedList(User user) {
        this.finalizedList.addUser(user);
    }

    /**
     * Runs the lottery for this event and stores the selected winners internally.
     * Any previous winners are cleared before the new draw. Draws chosen entrants
     * and stores their IDs in selectedIds.
     * @param capacity the number of winners to select (must be &ge; 0 and
     *                 not larger than the current wait-list size)
     * @return an unmodifiable view of the newly selected winners; the same
     *         list can also be retrieved later via {@link #getChosenEntrants()}
     */
    public List<User> drawLotteryWinners(int capacity) {
        chosenEntrants.clear();
        selectedIds.clear(); // Clear previous selected IDs

        if (lotteryEngine == null) {
            lotteryEngine = new LotterySystem(waitlist.getWaitlistedUsers());
        }

        chosenEntrants.addAll(lotteryEngine.selectWinners(capacity));

        // Populate selectedIds from chosenEntrants
        for (User user : chosenEntrants) {
            selectedIds.add(user.getId());
        }

        return chosenEntrants;
    }
    public List<User> getChosenEntrants() {
        return new ArrayList<>(chosenEntrants);
    }

    public List<String> getSelectedIds() {
        return new ArrayList<>(selectedIds);
    }

    public void setSelectedIds(List<String> selectedIds) {
        this.selectedIds = selectedIds;
    }

    /**
     * Gets the list of user IDS who declined after being accepted
     * @return List of cancelled entrant IDS
     * */
    public List<String> getCancelledEntrants() {
        return new ArrayList<>(cancelledEntrants);
    }

    /**
     * Sets the list of cancelled entrants
     * @param cancelledEntrants List of user IDs who declined
     * */
    public void setCancelledEntrants(List<String> cancelledEntrants) {
        this.cancelledEntrants = cancelledEntrants != null ?
                new ArrayList<>(cancelledEntrants) : new ArrayList<>();
    }

    /**
     * Adds a user ID to the cancelled entrants list
     * @param userId ID of user who declined
     * */
    public void addCancelledEntrant(String userId) {
        if (this.cancelledEntrants == null) {
            this.cancelledEntrants = new ArrayList<>();
        }
        if (!this.cancelledEntrants.contains(userId)) {
            this.cancelledEntrants.add(userId);
        }
    }
}