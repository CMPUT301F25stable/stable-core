package com.example.eventlottery.model;

import android.util.Log;

import com.example.eventlottery.events.Event;
import com.example.eventlottery.users.Organizer;
import com.example.eventlottery.view.EventAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Connects to the Firestore database for events
 */
public class EventDatabase {
    private FirebaseFirestore db;
    private CollectionReference eventsRef;

    public EventDatabase() {
        this.db = FirebaseFirestore.getInstance();
        this.eventsRef = db.collection("event");
    }

    public FirebaseFirestore getDb() {
        return db;
    }

    public void setDb(FirebaseFirestore db) {
        this.db = db;
    }

    public CollectionReference getEventsRef() {
        return eventsRef;
    }

    public void setEventsRef(CollectionReference eventsRef) {
        this.eventsRef = eventsRef;
    }

    /**
     * Retrieves an event from the database.
     * @param eventId The event id to look for in the database.
     * @param listener (Description to be completed)
     */
    public void get(String eventId, OnCompleteListener<DocumentSnapshot> listener) {
        eventsRef.document(eventId)
                .get()
                .addOnCompleteListener(listener)
                .addOnFailureListener(e -> {
                    Log.e("EventDatabase", "Failed to load event: " + eventId);
                    Log.e("EventDatabase", e.toString());
                });
    }

    /**
     * Inserts an event into the database.
     * Note: This is basically identical to lab 5 code.
     * @param event The event to insert.
     */
    public void insert(Event event) {
        DocumentReference docRef = eventsRef.document(event.getId());
        docRef.set(event);
    }

    /**
     * Gets all of the events associated with a particular organizer.
     */
    public void organizerGetEvents(Organizer organizer, ArrayList<Event> data, EventAdapter adapter) {
        // Get eventIDs. Return prematurely if there are no events
        ArrayList<String> eventIDs = organizer.getCreatedEvents();
        if (eventIDs.isEmpty()) {
            return;
        }

        // Iterate through every eventID & add it from firestore, to the list.
        DocumentReference eventRef;
        for (String eventID : eventIDs) {
            // Get reference to the event
            eventRef = eventsRef.document(eventID);
            eventRef.get().addOnSuccessListener(snapshot -> {
                // If successfully gotten, & it exists, add to 'data'
                if (snapshot.exists()) {
                    Event event = snapshot.toObject(Event.class);
                    data.add(event);
                    adapter.notifyDataSetChanged();
                }
            });
        }
    }


    /**
     * Updates the event in firestore, according to what an organizer changes.
     * TODO: Right now it just updates the waitlistMax & start + end date.
     * @param event The event to update.
     */
    public void organizerUpdateEvent(Event event) {
        // Get reference to event & relevant variables to update
        DocumentReference eventRef = eventsRef.document(event.getId());
        int newMaxSize = event.getWaitlistMax();
        Date startTime = event.getStartTime();
        Date endTime = event.getEndTime();
        ArrayList<String> formattedDates = this.formatDates(startTime, endTime);
        String formattedStartDate = formattedDates.get(0);
        String formattedStartTime = formattedDates.get(1);
        String formattedEndDate = formattedDates.get(2);
        String formattedEndTime = formattedDates.get(3);

        // Update event in firestore
        eventRef.update(
                "waitlist.maxSize", newMaxSize,
                "startTime", startTime,
                "endTime", endTime,
                "formattedStartTime", formattedStartTime,
                "formattedEndTime", formattedEndTime,
                "formattedStartDate", formattedStartDate,
                "formattedEndDate", formattedEndDate
        );
    }

    /**
     * A helper function for updating events.
     * Formats the start time & end time of an event.
     * Returns an arraylist of its parts in the order:
     * 0. formatted start date
     * 1. formatted start time
     * 2. formatted end date
     * 3. formatted end time
     * @param startTime The start time.
     * @param endTime The end time.
     * @return
     */
    private ArrayList<String> formatDates(Date startTime, Date endTime) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy'-'h:mm a", Locale.CANADA);
        String formattedStart = dateFormat.format(startTime);
        String formattedEnd = dateFormat.format(endTime);

        String[] startParts = formattedStart.split("-");
        String[] endParts = formattedEnd.split("-");

        // Store results in this array
        ArrayList<String> formattedParts = new ArrayList<>();

        for (String startPart : startParts) {
            formattedParts.add(startPart);
        }

        for (String endPart: endParts) {
            formattedParts.add(endPart);
        }

        return formattedParts;
    }
}
