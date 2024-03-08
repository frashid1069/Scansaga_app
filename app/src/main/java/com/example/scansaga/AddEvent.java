package com.example.scansaga;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.ArrayList;
import java.util.HashMap;


public class AddEvent extends AppCompatActivity implements AddEventFragment.AddEventDialogListener {
    ListView eventList;
    ArrayList<Event> eventDataList;
    EventArrayAdapter eventArrayAdapter;
    private FirebaseFirestore db;
    private CollectionReference eventsRef;

    @Override
    public void addNewEvent(Event event) {
        eventDataList.add(event);
        eventArrayAdapter.notifyDataSetChanged();
        addEventToFirestore(event);
    }

    @Override
    public void deleteEvent(Event event) {
        eventDataList.remove(event);
        eventArrayAdapter.notifyDataSetChanged();
    }

    @Override
    public void editEvent(Event event) {
        eventArrayAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_event);

        eventList = findViewById(R.id.event_list);
        eventDataList = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("events");

        eventArrayAdapter = new EventArrayAdapter(this, eventDataList);
        eventList.setAdapter(eventArrayAdapter);

        FloatingActionButton fab = findViewById(R.id.add_event_button);
        fab.setOnClickListener(v -> new AddEventFragment().show(getSupportFragmentManager(), "Add Event"));

    }

    // Method to add a new event to Firestore
    private void addEventToFirestore(Event event) {
        // Add the event to the database
        HashMap<String, String> data = new HashMap<>();
        data.put("Date", event.getDate());
        data.put("Venue", event.getVenue());
        data.put("Name", event.getName());
        //data.put("QR", event.getQrCodeBitmap().toString());
        // Add more fields as needed

        eventsRef.document(event.getName()).set(data)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Event added successfully!"))
                .addOnFailureListener(e -> Log.e("Firestore", "Error adding event", e));
    }

}