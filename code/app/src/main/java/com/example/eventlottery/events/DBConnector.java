package com.example.eventlottery.events;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.util.Log;

import com.example.eventlottery.users.Organizer;
import com.example.eventlottery.users.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
     * Gets the UUID of the device, to identify the user.
     * @param context: the context of the application
     * @return the UUID of the user
     */
    protected String getUUID(Context context) {
        String uuid = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        return uuid;
    }

    /**
     * Gets the DocumentReference from UUID
     * @param id the UUID of the user
     * @return DocumentReference from UUID
     */
    public DocumentReference getUserDoc(String id) {
        return this.db.collection("users").document(id);
    }

    /**
     * Saves a new user to the database, if the user doesn't already exist.
     * TODO: Right now it makes every user an organizer! Probably should change later
     * @param context The activity it's called in.
     */
    public void saveNewUser(Context context) {
        CollectionReference usersRef = db.collection("users");
        DocumentReference userRef = usersRef.document(this.id);
        // Asynchronous so added onCompleteListener
        userRef.get().addOnCompleteListener(task -> {
            DocumentSnapshot document = task.getResult();
            // If user doesn't already exist, create & set
            if (!document.exists()) {
                // TODO: Change this from organizer to user later.
                // I did it this way for now so that they don't crash when they enter OrganizerPanel - John
                Organizer organizer = new Organizer(context);
                userRef.set(organizer);
            }
        });
    }

    /**
     * Saved the personal info of the user
     *
     * @param id:       the UUID of the user
     * @param name:     the name of the user
     * @param email:    the email of the user
     * @param phoneNum: the phone number of the user
     * @param listener: listener called when saving user
     * @return
     */
    public void saveUserInfo(String id, String name, String email,
                             String phoneNum, OnCompleteListener<Void> listener) {

        Map<String, Object> items = new HashMap<>();
        items.put("name", name);
        items.put("emailAddress", email);
        items.put("phoneNumber", phoneNum);
        getUserDoc(id)
                .set(items, SetOptions.merge())
                .addOnCompleteListener(listener)
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed save info" + id, e);
                });
    }

    public void updateOrganizerCreatedEvents(Organizer organizer) {
        CollectionReference users = db.collection("users");
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

    /**
     * Gets the UUID of the user
     * @return UUID of the user
     */
    public String getUserId() { return this.id; }
}