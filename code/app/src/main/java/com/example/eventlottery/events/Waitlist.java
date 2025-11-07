package com.example.eventlottery.events;

import com.example.eventlottery.users.User;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Represents a waitlist of users for an event in the Event Lottery system.
 * <p>
 * The {@code Waitlist} class manages users who have registered for an event
 * but have not yet been selected as winners. It supports adding and removing
 * users, retrieving the waitlist, and setting a maximum capacity.
 * </p>
 */
public class Waitlist implements Serializable {
    /** The list of users currently on the waitlist. */
    private ArrayList<User> waitlistedUsers;
    /** The maximum number of users allowed on the waitlist. */
    private int maxSize;  // Integer.MAX_VALUE == no limit (highest number you can set it to anyway)

    /**
     * Default constructor that initializes an empty waitlist with
     * no maximum size restriction.
     */
    public Waitlist() {
        waitlistedUsers = new ArrayList<>();
        maxSize = Integer.MAX_VALUE;
    }

    /**
     * Constructs a {@code Waitlist} with an existing list of users.
     * @param waitlistedUsers the list of users to initialize the waitlist with
     */
    public Waitlist(ArrayList<User> waitlistedUsers) {
        this.waitlistedUsers = waitlistedUsers;
    }

    /**
     * Returns the list of users currently on the waitlist.
     * @return an {@link ArrayList} of {@link User} objects
     */
    public ArrayList<User> getWaitlistedUsers() {
        return waitlistedUsers;
    }


    /**
     * Replaces the current waitlist with a new list of users.
     *
     * @param waitlistedUsers the new list of {@link User} objects
     */
    public void setWaitlistedUsers(ArrayList<User> waitlistedUsers) {
        this.waitlistedUsers = waitlistedUsers;
    }

    /**
     * Adds a user to the waitlist.
     *
     * @param newUser the {@link User} to add
     */
    public void addUser(User newUser) {
        waitlistedUsers.add(newUser);
    }


    /**
     * Removes a user from the waitlist.
     * @param userToRemove the {@link User} to remove
     */
    public void removeUser(User userToRemove) {
        waitlistedUsers.remove(userToRemove);
    }

    /**
     * Returns the maximum number of users allowed on the waitlist.
     * @return the maximum waitlist size
     */
    public int getMaxSize() {
        return maxSize;
    }


    /**
     * Sets the maximum number of users allowed on the waitlist.
     * Use {@code Integer.MAX_VALUE} to indicate no limit.
     * @param maxSize the new maximum size of the waitlist
     */
    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }
}