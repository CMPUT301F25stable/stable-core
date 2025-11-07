package com.example.eventlottery.view;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eventlottery.R;
import com.example.eventlottery.events.DBConnector;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Objects;

/**
 * EditUserInfoActivity class is used to display the users personal
 * info when added/updated.
 */
public class EditUserInfoActivity extends AppCompatActivity {
    private static final String TAG = "EditUserInfoActivity";
    ImageButton backBtn, confirmBtn;
    Button deleteBtn;
    EditText nameEditText, emailEditText, phoneNumEditText;
    DBConnector db;
    String id;

    /**
     * onCreate method for this activity
     * @param savedInstanceState: Bundle for activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_info);

        // connection to db
        db = new DBConnector(EditUserInfoActivity.this);

        // For testing
        String mockID = getIntent().getStringExtra("mockID");
        if (mockID == null) {
            id = db.getUserId();
        } else {
            id = mockID;
        }


        nameEditText = findViewById(R.id.name_edit_text);
        emailEditText = findViewById(R.id.email_edit_text);
        phoneNumEditText = findViewById(R.id.phoneNum_edit_text);

        // load a users info
        db.loadUserInfo(id, EditUserInfoActivity.this::loadInfo);

        // saves a users updated info
        confirmBtn = findViewById(R.id.confirm_btn);
        confirmBtn.setOnClickListener(v -> {
            saveInfo();
        });

        // back to profile page
        backBtn = findViewById(R.id.back_btn);
        backBtn.setOnClickListener(v -> {
            finish();
        });

        // deletes User
        AlertDialog.Builder builder = new AlertDialog.Builder(EditUserInfoActivity.this);
        deleteBtn = findViewById(R.id.delete_account_btn);
        deleteBtn.setOnClickListener(v -> {
            builder.setTitle("This Will Delete Your Account")
                    .setMessage("Are You Sure?")
                    .setNegativeButton("No", null)
                    .setPositiveButton("Yes", (dialogInterface, i) -> {
                                db.deleteUserAcc(id, this::deleteUser);
                            })
                    .show();
        });
    }

    /**
     * Saves the user info
     */
    public void saveInfo() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String phoneNum = phoneNumEditText.getText().toString().trim();

        // name and email must be filled out
        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(EditUserInfoActivity.this, "Name and Email are Required", Toast.LENGTH_SHORT).show();
            return;
        }

        // save info
        db.saveUserInfo(id, name, email, phoneNum, task -> {
            if (task.isSuccessful()) {
                Toast.makeText(EditUserInfoActivity.this, "Saved Info", Toast.LENGTH_SHORT).show();
            } else {
                Log.d(TAG, "Failed saving user info:" + id);
            }
        });
    }

    /**
     * Loads the user info
     * @param task: firestore get data request
     */
    public void loadInfo(Task<DocumentSnapshot> task) {
        if (task.isSuccessful()) {
            DocumentSnapshot snapshot = task.getResult();
            if (snapshot.exists()) {
                String name = snapshot.getString("name");
                String email = snapshot.getString("emailAddress");
                String phoneNum = "";

                // phone number might not be filled out
                if (!Objects.requireNonNull(snapshot.getString("phoneNumber")).isEmpty()) {
                    phoneNum = snapshot.getString("phoneNumber");
                }
                nameEditText.setText(name);
                emailEditText.setText(email);
                phoneNumEditText.setText(phoneNum);

            } else {
                Log.d(TAG, "Snapshot DNE:" + id);
                }
        } else {
            Log.d(TAG, "Failed loading user" + id);
            }
    }

    /**
     * Deletes User
     * @param task: firestore get data request
     */
    public void deleteUser(Task<Void> task) {
        if (task.isSuccessful()) {
            Toast.makeText(EditUserInfoActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
        } else {
            Log.d(TAG, "Failed deleting user:" + id);
        }
    }
}