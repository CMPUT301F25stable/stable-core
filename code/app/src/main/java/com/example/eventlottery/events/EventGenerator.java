package com.example.eventlottery.events;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EventGenerator {
    private FirebaseFirestore db;

    public EventGenerator() {
        db = FirebaseFirestore.getInstance();
    }

    public void createTestEvents() {
        // Create a calendar instance for dates
        Calendar calendar = Calendar.getInstance();

        // Test Event 1
        calendar.set(2025, Calendar.NOVEMBER, 5, 9, 0); // Start: Nov 5, 2025 at 9:00 AM
        Date startTime1 = calendar.getTime();
        calendar.set(2025, Calendar.NOVEMBER, 5, 19, 0); // End: Nov 5, 2025 at 7:00 PM
        Date endTime1 = calendar.getTime();

        Event event1 = new Event(
                "Demon Slayer: Infinity Castle – The Final Battle Begins",
                "Enter the Infinity Castle — the ever-shifting fortress where Tanjiro Kamado and the Hashira face their greatest challenge yet.",
                "Edmonton Cineplex Westmount",
                "Anime Alberta",
                "https://storage.googleapis.com/cmput-301-stable-21008.firebasestorage.app/anime.webp",
                startTime1,
                endTime1
        );

        // Test Event 2
        calendar.set(2025, Calendar.NOVEMBER, 10, 14, 0); // Start: Nov 10, 2025 at 2:00 PM
        Date startTime2 = calendar.getTime();
        calendar.set(2025, Calendar.NOVEMBER, 10, 18, 30); // End: Nov 10, 2025 at 6:30 PM
        Date endTime2 = calendar.getTime();

        Event event2 = new Event(
                "City League Hocket Night",
                "Weekly rec league double-header.",
                "Terwillegar Rec Centre",
                "YEG Sports",
                "https://storage.googleapis.com/cmput-301-stable-21008.firebasestorage.app/hockey.webp",
                startTime2,
                endTime2
        );

        // Test Event 3
        calendar.set(2025, Calendar.NOVEMBER, 15, 19, 0); // Start: Nov 15, 2025 at 7:00 PM
        Date startTime3 = calendar.getTime();
        calendar.set(2025, Calendar.NOVEMBER, 15, 21, 0); // End: Nov 15, 2025 at 9:00 PM
        Date endTime3 = calendar.getTime();

        Event event3 = new Event(
                "Winter Dance Showcase",
                "Contemporary + hip-hop student performances.",
                "U of A Timms Centre",
                "Dance Society",
                "https://storage.googleapis.com/cmput-301-stable-21008.firebasestorage.app/dance.jpg",
                startTime3,
                endTime3
        );

        // Store events in Firebase
        storeEvent(event1);
        storeEvent(event2);
        storeEvent(event3);
    }

    private void storeEvent(Event event) {
        if (event.getId() == null || event.getId().isEmpty()) {
            event.setId(UUID.randomUUID().toString());
        }

        db.collection("event")
                .document(event.getId())
                .set(event)
                .addOnSuccessListener(aVoid -> {
                    System.out.println("Event " + event.getName() + " added successfully with ID: " + event.getId());
                })
                .addOnFailureListener(e -> {
                    System.out.println("Error adding event " + event.getName() + ": " + e.getMessage());
                });
    }

    // If you need to create an event with a specific ID (for pre-existing events):
    public Event createPreExistingEvent(String id, String name, String description,
                                        String location, String organizer, String image,
                                        Date startTime, Date endTime) {
        return new Event(id, name, description, location, organizer, image, startTime, endTime);
    }
}
