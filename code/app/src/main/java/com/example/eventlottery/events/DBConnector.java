package com.example.eventlottery.events;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import com.example.eventlottery.users.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * Connects to FirebaseFirestore database
 */
public class DBConnector {
    private static final String TAG = "DBConnector";

    private FirebaseFirestore db;
    String id;


    /**
     * Gets the DocumentReference from UUID
     * @param context: context of the application
     */
    public DBConnector(Context context) {
        this.db = FirebaseFirestore.getInstance();
        this.id = getUUID(context);
    }

    /**
     * Gets the database connection.
     * @return the database connection.
     */
    public FirebaseFirestore getDb() {
        return db;
    }

    /**
     * Sets the database connection.
     * @param db the new database connection.
     */
    public void setDb(FirebaseFirestore db) {
        this.db = db;
    }

    /**
     * Gets the user id.
     * @return the user id.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the user id.
     * @param id the new user id.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the UUID of the device, to identify the user.
     * @param context: the context of the application
     * @return the UUID of the user
     */
    @SuppressLint("HardwareIds")
    protected String getUUID(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    /**
     * Gets the DocumentReference from UUID
     * @param id the UUID of the user
     * @return DocumentReference from UUID
     */
    public DocumentReference getUserDoc(String id) {
        return this.db.collection("users-p4").document(id);
    }

    /**
     * US 01.07.01
     * Saves a new user to the database, if the user doesn't already exist.
     * @param context The activity it's called in.
     */
    public void saveNewUser(Context context) {
        CollectionReference usersRef = db.collection("users-p4");
        DocumentReference userRef = usersRef.document(this.id);
        // Asynchronous so added onCompleteListener
        userRef.get().addOnCompleteListener(task -> {
            DocumentSnapshot document = task.getResult();
            // If user doesn't already exist, create & set
            if (!document.exists()) {
                User organizer = new User(context);
                userRef.set(organizer);
            }
        });
    }

    /**
     * Saved the personal info of the user, if the user deleted their account, a new account
     * will be made
     * @param id:       the UUID of the user
     * @param name:     the name of the user
     * @param email:    the email of the user
     * @param phoneNum: the phone number of the user
     * @param context:  the context of the application
     * @param listener: listener called when saving user
     * @return
     */
    public void saveUserInfo(String id, String name, String email,
                             String phoneNum, Context context, OnCompleteListener<Void> listener) {

        Map<String, Object> items = new HashMap<>();
        items.put("name", name);
        items.put("emailAddress", email);
        items.put("phoneNumber", phoneNum);

        getUserDoc(id).get().addOnCompleteListener(task -> {
            DocumentSnapshot document = task.getResult();
            if (document == null || !document.exists()) {
                User user = new User(context);
                user.setName(name);
                user.setEmailAddress(email);
                user.setPhoneNumber(phoneNum);
                getUserDoc(id).set(user);
            } else {
                getUserDoc(id)
                        .set(items, SetOptions.merge())
                        .addOnCompleteListener(listener)
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Failed save info" + id, e);
                        });
            }
        });
    }

    /**
     * Updates the organizer's created events (i.e, adds to createdEvents array)
     * @param organizer The organizer who we are updating created events for
     */
    public void updateOrganizerCreatedEvents(User organizer) {
        CollectionReference users = db.collection("users-p4");
        DocumentReference user = users.document(organizer.getId());
        user.update("createdEvents", organizer.getCreatedEvents());
    }

    /**
     * Loads the personal info of the user
     * @param id: the UUID of the user
     * @param listener: listener called when loading user
     */
    public void loadUserInfo(String id, OnCompleteListener<DocumentSnapshot> listener) {
        getUserDoc(id)
                .get()
                .addOnCompleteListener(listener)
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load info" + id, e);
                });
    }

    /**
     * deletes a user
     * @param id: the UUID of the user
     * @param listener: listener called when loading user
     */
    public void deleteUserAcc(String id, OnCompleteListener<Void> listener) {
        getUserDoc(id).delete()
                .addOnCompleteListener(listener)
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to delete user" + id, e);
                });
    }

    public void updateOrganizerPerms(String id, boolean organizerStatus, OnCompleteListener<Void> listener) {
        Map<String, Object> items = new HashMap<>();
        items.put("creationBan", organizerStatus);

        getUserDoc(id).get().addOnCompleteListener(task -> {
            DocumentSnapshot userData = task.getResult();
            if (userData != null) {
                getUserDoc(id)
                        .set(items, SetOptions.merge())
                        .addOnCompleteListener(listener);
            } else {
                Log.e(TAG, "Failed to retrieve user data or user doesn't exist in the database.");
            }
        });
    }

    /**
     * Gets the UUID of the user
     * @return UUID of the user
     */
    public String getUserId() { return this.id; }
}