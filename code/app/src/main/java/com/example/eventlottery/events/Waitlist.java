package com.example.eventlottery.events;

import com.example.eventlottery.users.User;

import java.io.Serializable;
import java.util.ArrayList;

public class Waitlist implements Serializable {
    private ArrayList<User> waitlistedUsers;
    private int maxSize;  // -1 = no limit

    public Waitlist() {
        waitlistedUsers = new ArrayList<>();
        maxSize = -1;
    }

    public Waitlist(ArrayList<User> waitlistedUsers) {
        this.waitlistedUsers = waitlistedUsers;
    }

    public ArrayList<User> getWaitlistedUsers() {
        return waitlistedUsers;
    }

    public void setWaitlistedUsers(ArrayList<User> waitlistedUsers) {
        this.waitlistedUsers = waitlistedUsers;
    }

    public void addUser(User newUser) {
        waitlistedUsers.add(newUser);
    }

    public void removeUser(User userToRemove) {
        waitlistedUsers.remove(userToRemove);
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }
}