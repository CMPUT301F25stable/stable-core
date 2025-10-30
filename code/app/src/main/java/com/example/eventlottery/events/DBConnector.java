package com.example.eventlottery.events;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
     * This file includes code from [well-fed] (https://github.com/CMPUT301F22T02/well-fed)
     * licensed under the GNU General Public License v3.0.
     * <p>
     * Gets the UUID of the device, to identify the user.
     * Creates a new UUID for the user if they do not already have one.
     * @param context: the context of the application
     * @return the UUID of the user
     */
    protected String getUUID(Context context) {
        SharedPreferences sharedPreferences;
        sharedPreferences = context.getApplicationContext()
                .getSharedPreferences("pref", Context.MODE_PRIVATE);

        String uuid = sharedPreferences.getString("UUID", null);

        // if uuid does not exist, create the uuid and save it locally
        if (uuid == null) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            uuid = UUID.randomUUID().toString();
            editor.putString("UUID", uuid);
            editor.commit();
        }
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