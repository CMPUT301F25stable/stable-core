package com.example.eventlottery;
import static org.junit.Assert.fail;

import androidx.test.core.app.ApplicationProvider;
import android.content.Context;

import com.example.eventlottery.events.DBConnector;
import com.example.eventlottery.users.Organizer;
import com.google.firebase.firestore.DocumentSnapshot;

import org.junit.Test;

/**
 * Instrumented tests for the DBConnector class.
 * References: https://stackoverflow.com/questions/2095695/android-unit-tests-requiring-context
 */
public class DBConnectorInstrumentedTest {

    /**
     * US 01.07.01
     * Tests saving a user to the database, with a mock ID.
     */
    @Test
    public void testSaveNewUser() throws InterruptedException {
        // Get context, initialize connector & set the ID to a mock ID.
        Context context = ApplicationProvider.getApplicationContext();
        DBConnector connector = new DBConnector(context);
        String mockUserID = "mockID";
        connector.setId(mockUserID);

        // Test the method
        connector.saveNewUser(context);

        // Wait 2 seconds to wait for operation to complete
        Thread.sleep(2000);

        // Check if it exists in firebase
        connector.loadUserInfo(mockUserID, task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Organizer mockUser = document.toObject(Organizer.class);
                    if (mockUser != null && mockUser.getId().equals(mockUserID)) {
                        System.out.println("Test passes");
                    } else {
                        fail("Document exists but ID doesn't match or is null");
                    }
                } else {
                    fail("User document not found in Firestore");
                }
            } else {
                fail("Firestore task failed: " + task.getException());
            }
        });
    }
}
