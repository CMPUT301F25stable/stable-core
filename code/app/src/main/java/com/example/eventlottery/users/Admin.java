package com.example.eventlottery.users;

public class Admin extends Organizer {
    public Admin(String id, String name, String emailAddress) {
        super(id, name, emailAddress);
    }

    public Admin(String id, String name, String emailAddress, String phoneNumber) {
        super(id, name, emailAddress, phoneNumber);
    }

    @Override
    public String getUserType() {
        return "Admin";
    }
}