package com.example.eventlottery.events;

import com.example.eventlottery.users.User;

import java.io.Serializable;
import java.util.ArrayList;

public class Waitlist implements Serializable {
    private ArrayList<User> waitlistedUsers;

    public Waitlist() {
        waitlistedUsers = new ArrayList<>();
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
}