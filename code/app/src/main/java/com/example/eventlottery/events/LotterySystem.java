package com.example.eventlottery.events;

import com.example.eventlottery.users.User;

import java.util.ArrayList;
import java.util.Random;

public class LotterySystem {
    private ArrayList<User> winners;
    private Waitlist waitlist;

    public LotterySystem() {
        winners = new ArrayList<>();
        waitlist = new Waitlist();
    }

    public LotterySystem(ArrayList<User> waitlistedUsers) {
        winners = new ArrayList<>();
        waitlist = new Waitlist(waitlistedUsers);
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

    public ArrayList<User> selectWinners(int eventCapacity) {
        ArrayList<User> selectedUsers = new ArrayList<>();
        ArrayList<User> waitlistedUsers = waitlist.getWaitlistedUsers();

        for (int i = 0; i < eventCapacity; i++) {
            User selectedUser = randomSelection(selectedUsers, waitlistedUsers);
            selectedUsers.add(selectedUser);
        }

        for (User user : selectedUsers) {
            waitlistedUsers.remove(user);
        }
        winners = selectedUsers;
        return selectedUsers;
    }

    public ArrayList<User> getWinners() {
        return winners;
    }

    public Waitlist getWaitlist() {
        return waitlist;
    }
}