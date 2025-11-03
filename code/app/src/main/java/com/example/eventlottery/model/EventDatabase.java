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
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

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

        // Else, iterate through every event from the organizer's created events & add to 'data'.
        DocumentReference eventRef;
        // Note: This code is basically identical to what they have in lab 5
        eventsRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("EventDatabase", error.toString());;
            }
            if (value != null && !value.isEmpty()) {
                data.clear();
                for (QueryDocumentSnapshot snapshot: value) {
                    Event event = snapshot.toObject(Event.class);
                    data.add(event);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }
}
