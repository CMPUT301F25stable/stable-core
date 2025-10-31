package com.example.eventlottery.events;

import java.io.Serializable;

public class QRCode implements Serializable { // To be implemented
    private String eventId;

    public QRCode(String eventId) {
        this.eventId = eventId;
    }
}