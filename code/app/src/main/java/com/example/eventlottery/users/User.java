package com.example.eventlottery.users;

import androidx.annotation.NonNull;
import com.example.eventlottery.events.Event;

import java.io.Serializable;

public class User implements Serializable {
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

    public void setEntrant(Entrant entrant) {
        this.entrant = entrant;
    }

    /** USER STORY 01.05.02 - Accept invitation
     * @param event accept the given event
     * */
    public void acceptInvitation(Event event) {
        entrant.acceptInvitation(event);
    }

    /** USER STORY 01.05.03 - Decline invitation
     * @param event decline the given event
     * */
    public void declineInvitation(Event event) {
        entrant.declineInvitation(event);
    }

    @NonNull
    @Override
    public String toString() {
        return "ID: " + getId() + "\nName: " + getName() + "\nEmail Address: " + getEmailAddress() + "\nPhone Number: " + getPhoneNumber();
    }
}