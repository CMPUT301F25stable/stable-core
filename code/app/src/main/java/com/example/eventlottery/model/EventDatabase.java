package com.example.eventlottery.model;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ListView;

import com.example.eventlottery.events.Event;
import com.example.eventlottery.users.Organizer;
import com.example.eventlottery.view.EventAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

/**
 * Provides an interface for interacting with the Firestore database
 * containing Event objects.
 * <p>
 * This class is primarily for handling basic CRUD operations such as retrieving, inserting,
 * and updating events, as well as fetching all events created by a particular organizer.
 * </p>
 */
public class EventDatabase {
    /** The Firebase Firestore instance used to access the database. */
    private FirebaseFirestore db;
    /** Reference to the "event" collection in Firestore. */
    private CollectionReference eventsRef;
    /**
     * Default constructor initializes the Firestore instance and
     * sets the reference to the "event" collection.
     */
    public EventDatabase() {
        this.db = FirebaseFirestore.getInstance();
        this.eventsRef = db.collection("event");
    }
    /** @return the FirebaseFirestore instance used by this database */
    public FirebaseFirestore getDb() {
        return db;
    }
    /** @param db sets the FirebaseFirestore instance to use */
    public void setDb(FirebaseFirestore db) {
        this.db = db;
    }
    /** @return the CollectionReference for the "event" collection */
    public CollectionReference getEventsRef() {
        return eventsRef;
    }
    /** @param eventsRef sets the CollectionReference for the "event" collection */
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
     * Retrieves all events associated with a particular organizer.
     * <p>
     * Each event is added to the provided data list, and the adapter is notified
     * to refresh the UI.
     * </p>
     *
     * @param organizer the {@link Organizer} whose events are to be retrieved
     * @param data      the {@link ArrayList} to store the retrieved events
     * @param adapter   the {@link EventAdapter} to notify when data changes
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
            eventRef = eventsRef.document(eventID);
            eventRef.get().addOnSuccessListener(snapshot -> {
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
     * TODO: Right now it just updates the waitlistMax
     * @param event The event to update.
     */
    public void organizerUpdateEvent(Event event) {
        DocumentReference eventRef = eventsRef.document(event.getId());
        int newMaxSize = event.getWaitlistMax();
        eventRef.update("waitlist.maxSize", newMaxSize);
    }
}
