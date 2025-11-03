package com.example.eventlottery.events;

import com.example.eventlottery.users.User;
import com.example.eventlottery.events.NotificationService;
import java.util.ArrayList;
import java.util.Random;

public class LotterySystem {
    private ArrayList<User> winners;
    private Waitlist waitlist;

    // US 01.04.01 & 01.04.02: Add notification support
    private NotificationService notificationService;
    private String eventName;

    public LotterySystem() {
        winners = new ArrayList<>();
        waitlist = new Waitlist();
    }

    public LotterySystem(ArrayList<User> waitlistedUsers) {
        winners = new ArrayList<>();
        waitlist = new Waitlist(waitlistedUsers);
    }

    /**
     * Sets the notification service for the lottery system
     */
    public void setNotificationService(NotificationService service) {
        this.notificationService = service;
    }

    /**
     * Set event name for notifications
     */
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    private User randomSelection(ArrayList<User> selectedUsers, ArrayList<User> waitlistedUsers) {
        Random random = new Random();
        int userIndex = random.nextInt(waitlistedUsers.size());
        User selectedUser = waitlistedUsers.get(userIndex);
        if (selectedUsers.contains(selectedUser)) {
            return randomSelection(selectedUsers, waitlistedUsers);
        } else {
            return selectedUser;
        }
    }

    /**
     * Select winners from waitlist and losers
     */
    public ArrayList<User> selectWinners(int eventCapacity) {
        ArrayList<User> selectedUsers = new ArrayList<>();
        ArrayList<User> waitlistedUsers = waitlist.getWaitlistedUsers();

        // Keep track of original waitlist before removing winners
        ArrayList<User> originalWaitlist = new ArrayList<>(waitlistedUsers);

        for (int i = 0; i < eventCapacity; i++) {
            User selectedUser = randomSelection(selectedUsers, waitlistedUsers);
            selectedUsers.add(selectedUser);
        }

        for (User user : selectedUsers) {
            waitlistedUsers.remove(user);
        }

        winners = selectedUsers;

        // US 01.04.01: Send notifications to winners
        if (notificationService != null && eventName != null) {
            for (User winner : winners) {
                notificationService.notifyWinner(winner, eventName);
            }

            // US 01.04.02: Send notifications to losers (users still in waitlist)
            for (User loser : waitlistedUsers) {
                notificationService.notifyLoser(loser, eventName);
            }
        }

        return selectedUsers;
    }

    /**
     * Replace declined user with new one from waitlist Sends notification to replacement winner
     */
    public User handleDecline(User declinedUser) {
        if (winners.contains(declinedUser)) {
            winners.remove(declinedUser);
        }

        // Select the next user from waitlist
        ArrayList<User> waitlistedUsers = waitlist.getWaitlistedUsers();
        User replacement = randomSelection(winners, waitlistedUsers);

        // If replacement is valid, add the new winner, and remove the replacement from the waitlist
        if (replacement != null) {
            winners.add(replacement);
            waitlistedUsers.remove(replacement);

            //Send notification to replacement winner
            if (notificationService != null && eventName != null) {
                notificationService.notifyWinner(replacement, eventName);
            }

            return replacement;
        }
        return null;
    }

    public ArrayList<User> getWinners() {
        return winners;
    }

    public Waitlist getWaitlist() {
        return waitlist;
    }
}