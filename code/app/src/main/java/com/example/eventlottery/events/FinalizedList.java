package com.example.eventlottery.events;

import com.example.eventlottery.users.User;

import java.io.Serializable;
import java.util.ArrayList;

public class FinalizedList implements Serializable {
    private ArrayList<User> finalizedUsers;

    public FinalizedList() {
        finalizedUsers = new ArrayList<>();
    }

    public FinalizedList(ArrayList<User> finalizedUsers) {
        this.finalizedUsers = finalizedUsers;
    }

    public void addUser(User newUser) {
        finalizedUsers.add(newUser);
    }

    public void removeUser(User userToRemove) {
        finalizedUsers.remove(userToRemove);
    }

    public ArrayList<User> getFinalizedUsers() {
        return finalizedUsers;
    }

    public void setFinalizedUsers(ArrayList<User> finalizedUsers) {
        this.finalizedUsers = finalizedUsers;
    }
}