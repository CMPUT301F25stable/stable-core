package com.example.eventlottery.events;

import com.example.eventlottery.events.NotificationSystem;
import com.example.eventlottery.users.User;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for organizer-specific operations
 * US 02.05.01: Handles sending invitations to chosen entrants
 */
public class OrganizerService {

    private NotificationSystem notificationSystem;

    public OrganizerService(NotificationSystem notificationSystem) {
        this.notificationSystem = notificationSystem;
    }

    /**
     * US 02.05.01: Send invitation notifications to chosen entrants
     *
     * @param entrants List of users to invite
     * @param eventName Name of the event
     * @param eventId ID of the event
     * @param customMessage Optional custom message from organizer
     * @return Number of notifications successfully sent
     */
    public int sendInvitationsToEntrants(List<User> entrants, String eventName,
                                         String eventId, String customMessage) {
        if (entrants == null || entrants.isEmpty()) {
            return 0;
        }

        // Filter out users who have notifications disabled
        List<User> eligibleEntrants = new ArrayList<>();
        for (User entrant : entrants) {
            if (entrant.canReceiveNotifications()) {
                eligibleEntrants.add(entrant);
            }
        }


        // Send notifications to eligible entrants
        notificationSystem.notifyInvitedEntrants(eligibleEntrants, eventName, eventId, customMessage);

        return eligibleEntrants.size();
    }

    /**
     * US 02.05.01: Send invitation to a single entrant
     *
     * @param entrant The user to invite
     * @param eventName Name of the event
     * @param eventId ID of the event
     * @param customMessage Optional custom message
     * @return true if notification was sent, false otherwise
     */
    public boolean sendInvitationToEntrant(User entrant, String eventName,
                                           String eventId, String customMessage) {
        if (entrant == null) {
            return false;
        }

        // Check if user can receive notifications
        if (!entrant.canReceiveNotifications()) {
            return false;
        }

        notificationSystem.notifyInvitedEntrant(entrant, eventName, eventId, customMessage);
        return true;
    }
}