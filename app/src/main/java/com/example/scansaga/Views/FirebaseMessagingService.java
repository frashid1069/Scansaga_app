package com.example.scansaga.Views;
import static com.example.scansaga.Model.MainActivity.CHANNEL_ID;
import static com.example.scansaga.Model.MainActivity.notificationID;
import static com.example.scansaga.Views.AddEvent.deviceId;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.util.Log;
import android.window.SplashScreen;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.scansaga.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Objects;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

        db.collection("events").whereArrayContains("signedUpAttendees", deviceId)
                .addSnapshotListener((querySnapshots, error) -> {
                    if (error != null) {
                        Log.e("TOKEN", "Firestore error in FirebaseMessagingService: ", error);
                        return;
                    }
                    if (querySnapshots != null) {
                        for (QueryDocumentSnapshot doc : querySnapshots) {
                            DocumentReference eventRef = db
                                    .collection("events")
                                    .document(String.valueOf(doc));
                            eventRef.update("signedUpAttendeeTokens", FieldValue.arrayUnion(token))
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("TOKEN", "Token successfully added in FirebaseMessagingService");
                                    });
                        }
                    }
                });
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        handleNotification(remoteMessage);
    }

    private void handleNotification(RemoteMessage remoteMessage) {
        // Play audio and vibration
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
        r.play();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            r.setLooping(false);
        }

        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {100, 300, 300, 300};
        v.vibrate(pattern, -1);


        int resourceImage = getResources().getIdentifier(Objects.requireNonNull(remoteMessage.getNotification()).getIcon(), "drawable", getPackageName());

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setSmallIcon(resourceImage);
        } else {
            builder.setSmallIcon(resourceImage);
        }

        Intent resultIntent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            resultIntent = new Intent(this, SplashScreen.class);
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentTitle(remoteMessage.getNotification().getTitle());
        builder.setContentText(remoteMessage.getNotification().getBody());
        builder.setContentIntent(pendingIntent);
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(remoteMessage.getNotification().getBody()));
        builder.setAutoCancel(true);
        builder.setPriority(Notification.PRIORITY_MAX);

        NotificationManager alertManager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.channel_description),
                    NotificationManager.IMPORTANCE_HIGH);
            alertManager.createNotificationChannel(channel);
            builder.setChannelId(CHANNEL_ID);
        }
        alertManager.notify(notificationID, builder.build());
    }
}

