package com.example.eventlottery;

import static org.junit.Assert.assertEquals;

import com.example.eventlottery.events.LotterySystem;
import com.example.eventlottery.users.User;

import org.junit.Test;

import java.util.ArrayList;
import java.util.UUID;

public class EventStructuresTest {
    private ArrayList<User> generateUsers(int amount) {
        ArrayList<User> users = new ArrayList<>();

        for (int i = 0; i < amount; i++) {
            String randomId = String.valueOf(UUID.randomUUID());
            users.add(new User(randomId, randomId.concat(" Name"), randomId.concat("@example.com"), randomId.concat(" Number")));
        }
        return users;
    }

    @Test
    public void winnerSelectionTest() {
        LotterySystem lotterySystem = new LotterySystem(generateUsers(20));
        assertEquals(20, lotterySystem.getWaitlist().getWaitlistedUsers().size());

        ArrayList<User> winners = lotterySystem.selectWinners(10);
        int iterateNum = 0;
        for (User user : winners) {
            iterateNum++;
            System.out.println(user.toString());
            System.out.println("--------------------------------");
        }
        assertEquals(lotterySystem.getWinners().size(), iterateNum);
    }
}