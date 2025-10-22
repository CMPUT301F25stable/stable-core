package com.example.eventlottery.users;

import android.util.Log;

public class User {
    private String id; // Device identifier
    private String name;
    private String emailAddress;
    private String phoneNumber;
    private Entrant entrant;

    public User(String id, String name, String emailAddress) {
        this.id = id;
        this.name = name;
        this.emailAddress = emailAddress;
        this.phoneNumber = "";
        entrant = new Entrant();
    }

    public User(String id, String name, String emailAddress, String phoneNumber) {
        this.id = id;
        this.name = name;
        this.emailAddress = emailAddress;
        this.phoneNumber = phoneNumber;
        entrant = new Entrant();
    }

    public String getUserType() {
        return "User";
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Entrant getEntrant() {
        return entrant;
    }

    public void print() {
        Log.d("User", id);
        Log.d("User", name);
        Log.d("User", emailAddress);
        Log.d("User", phoneNumber);
    }
}