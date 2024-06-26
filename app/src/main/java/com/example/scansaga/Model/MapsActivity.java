package com.example.scansaga.Model;

import androidx.fragment.app.FragmentActivity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;

import com.example.scansaga.Controllers.CustomInfoWindowAdapter;
import com.example.scansaga.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.scansaga.databinding.ActivityMapsBinding;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;

/**
 * MapsActivity is responsible for displaying a Google Map and placing markers on it
 * based on the location data fetched from Firestore. This activity uses a custom info window
 * adapter to display personalized information about each marker.
 */
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final String EXTRA_EVENT_NAME_DATE = "extra_event_name_date"; // use a consistent key

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Callback that is triggered when the map is ready to be used.
     * This method sets up the map and fetches location data based on the event name and date.
     *
     * @param googleMap The GoogleMap that is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(this));
        mMap.getUiSettings().setZoomControlsEnabled(true);
        String eventNameDate = getIntent().getStringExtra(EXTRA_EVENT_NAME_DATE);

        if (eventNameDate != null) {
            Log.d("SeeMap", eventNameDate);
        } else {
            Log.d("SeeMap", "eventNameDate is null");
        }

        if (eventNameDate != null) {
            // Fetch the location data from Firestore
            db.collection("events")
                    .document(eventNameDate)
                    .collection("locationOfCheckedInUsers")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("SeeMap", document +"");
                                addLocationMarker(document);
                            }
                        } else {

                        }
                    });
        }
    }

    /**
     * Adds a marker on the map for a given location fetched from Firestore.
     * This method also fetches the user's name and last name to display in the marker's info window.
     *
     * @param locationDocument The Firestore document containing the location data.
     */
    private void addLocationMarker(QueryDocumentSnapshot locationDocument) {
        double latitude = locationDocument.getDouble("latitude");
        double longitude = locationDocument.getDouble("longitude");
        String userId = locationDocument.getString("deviceId");

        // Fetch the user data to get name and last name
        db.collection("users").document(userId).get().addOnCompleteListener(userTask -> {
            if (userTask.isSuccessful()) {
                DocumentSnapshot userDocument = userTask.getResult();
                String name = userDocument.getString("Firstname");
                name = name.substring(0, 1).toUpperCase() + name.substring(1);
                String lastName = userDocument.getString("Lastname");
                lastName = lastName.substring(0, 1).toUpperCase() + lastName.substring(1);
                LatLng location = new LatLng(latitude, longitude);

                Drawable profilePicture = generateUniqueProfilePicture(name, lastName);

                // Convert Drawable to Bitmap for the marker
                Bitmap bitmap = drawableToBitmap(profilePicture);

                // Create a marker and set the user's full name as the title
                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(location)
                        .title(name + " " + lastName)
                        .icon(BitmapDescriptorFactory.fromBitmap(bitmap))); // Set the icon

                // Move the camera to the new marker
                mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
            } else {
                // still need to implement on case of fail
            }
        });
    }

    private class UserMarkerInfo {
        final String firstName;
        final String lastName;

        UserMarkerInfo(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
        }

    }
    /**
     * Generates a unique profile picture based on the user's first name and last name.
     * This method creates a bitmap with the user's initials on a colored background.
     *
     * @param firstName The user's first name.
     * @param lastName The user's last name.
     * @return A drawable representing the generated profile picture.
     */
    private Drawable generateUniqueProfilePicture(String firstName, String lastName) {
        int color = generateBackgroundColor(firstName, lastName);
        char firstLetter = firstName.toLowerCase().charAt(0);
        int resourceId = getResources().getIdentifier(String.valueOf(firstLetter), "drawable", getPackageName());
        Drawable letterDrawable = getResources().getDrawable(resourceId);

        Bitmap result = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        paint.setColor(color);
        canvas.drawRect(0F, 0F, (float) result.getWidth(), (float) result.getHeight(), paint);
        letterDrawable.setBounds(0, 0, result.getWidth(), result.getHeight());
        letterDrawable.draw(canvas);

        return new BitmapDrawable(getResources(), result);
    }

    /**
     * Generates a background color for the profile picture based on the user's name.
     *
     * @param firstName The user's first name.
     * @param lastName The user's last name.
     * @return An integer representing the generated color.
     */

    private int generateBackgroundColor(String firstName, String lastName) {
        int hash = (firstName + lastName).hashCode();
        // Ensuring the alpha value is set to 255 (fully opaque)
        return Color.argb(255, Math.abs(hash) % 256, (Math.abs(hash) / 256) % 256, (Math.abs(hash) / (256 * 256)) % 256);
    }

    /**
     * Converts a Drawable to a Bitmap.
     * This method is used to create a bitmap from the generated drawable profile pictures.
     *
     * @param drawable The drawable to convert.
     * @return A bitmap representation of the drawable.
     */
    private Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }
}