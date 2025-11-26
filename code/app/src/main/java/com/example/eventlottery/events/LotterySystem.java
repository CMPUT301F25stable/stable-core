package com.example.eventlottery.events;

import com.example.eventlottery.users.User;

import java.util.ArrayList;
import java.util.Random;

/**
 * Represents a lottery system for selecting winners from a waitlist of users.
 * <p>
 * This system maintains a list of winners and a waitlist of eligible users.
 * It provides functionality to randomly select winners, handle declined spots,
 * and manage the current waitlist.
 * </p>
 */
public class LotterySystem {
    /** The list of users who have been selected as winners. */
    private ArrayList<User> winners;
    /** The waitlist of users eligible for selection. */
    private Waitlist waitlist;


    /**
     * Default constructor that initializes an empty list of winners
     * and an empty waitlist.
     */
    public LotterySystem() {
        winners = new ArrayList<>();
        waitlist = new Waitlist();
    }

    /**
     * Constructs a {@code LotterySystem} with a predefined list of
     * waitlisted users.
     *
     * @param waitlistedUsers the list of users to initialize the waitlist with
     */
    public LotterySystem(ArrayList<User> waitlistedUsers) {
        winners = new ArrayList<>();
        waitlist = new Waitlist(waitlistedUsers);
    }

    /**
     * Randomly selects a user from the waitlisted users who has not
     * already been selected as a winner.
     * <p>
     * If a duplicate is found, the selection process repeats until
     * a unique user is chosen.
     *
     * @param selectedUsers   the list of users already selected as winners
     * @param waitlistedUsers the list of users currently on the waitlist
     * @return a randomly selected {@code User} from the waitlist
     */
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
     * Selects a number of winners from the waitlist based on the event capacity.
     * The winners are removed from the waitlist once selected.
     *
     * @param eventCapacity the maximum number of winners to select
     * @return a list of {@code User} objects representing the selected winners
     */
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

    /**
     * Returns the list of currently selected winners.
     * @return an {@code ArrayList} of {@code User} objects representing the winners
     */
    public ArrayList<User> getWinners() {
        return winners;
    }

    /**
     * Returns the current waitlist used in this lottery system.
     * @return the {@code Waitlist} object associated with this system
     */
    public Waitlist getWaitlist() {
        return waitlist;
    }
}