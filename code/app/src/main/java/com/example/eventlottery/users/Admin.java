package com.example.eventlottery.users;

/**
 * Represents an administrator user within the Event Lottery application.
 */
public class Admin extends Organizer {
    /**
     * Constructs a new {@code Admin} with the given ID, name, and email address.
     *
     * @param id The admin’s unique identifier.
     * @param name The admin’s display name.
     * @param emailAddress The admin’s email address.
     */
    public Admin(String id, String name, String emailAddress) {
        super(id, name, emailAddress);
    }
    /**
     * Constructs a new {@code Admin} with the given ID, name, email address, and phone number.
     *
     * @param id The admin’s unique identifier.
     * @param name The admin’s display name.
     * @param emailAddress The admin’s email address.
     * @param phoneNumber The admin’s phone number.
     */
    public Admin(String id, String name, String emailAddress, String phoneNumber) {
        super(id, name, emailAddress, phoneNumber);
    }
    /**
     * Returns the user type for this object.
     * @return The string {@code "Admin"} to identify this user’s role.
     */
    @Override
    public String getUserType() {
        return "Admin";
    }
}