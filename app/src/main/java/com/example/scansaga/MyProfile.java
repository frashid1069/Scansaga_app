package com.example.scansaga;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.scansaga.EditUserFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class MyProfile extends AppCompatActivity {

    private TextView firstNameTextView, lastNameTextView, emailTextView, phoneNumberTextView;
    private String deviceId;
    private User currentUser;
    private ListenerRegistration userDataListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_display);

        // Initialize TextViews
        firstNameTextView = findViewById(R.id.first_name_text_view);
        lastNameTextView = findViewById(R.id.last_name_text_view);
        emailTextView = findViewById(R.id.email_text_view);
        phoneNumberTextView = findViewById(R.id.phone_number_text_view);

        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Fetch user data from Firestore
        fetchUserDataFromFirestore(deviceId);

        FloatingActionButton fab = findViewById(R.id.edit_profile_button);
        fab.setOnClickListener(v -> {
            // Pass the deviceId and currentUser to the EditUserFragment
            EditUserFragment editUserFragment = EditUserFragment.newInstance(deviceId, currentUser);
            editUserFragment.show(getSupportFragmentManager(), "Edit User");
        });

    }

    private void fetchUserDataFromFirestore(String deviceId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        userDataListener = db.collection("users")
                .whereEqualTo("DeviceId", deviceId)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        // Handle errors
                        return;
                    }

                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            // Update UI with fetched data
                            String retrievedFirstName = documentSnapshot.getString("Firstname");
                            String lastName = documentSnapshot.getString("Lastname");
                            String email = documentSnapshot.getString("Email");
                            String retrievedPhone = documentSnapshot.getString("PhoneNumber");

                            firstNameTextView.setText(retrievedFirstName);
                            lastNameTextView.setText(lastName);
                            emailTextView.setText(email);
                            phoneNumberTextView.setText(retrievedPhone);
                            currentUser = new User(retrievedFirstName,lastName,email,retrievedPhone);
                        }
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove the listener when the activity is destroyed to avoid memory leaks
        if (userDataListener != null) {
            userDataListener.remove();
        }
    }
}