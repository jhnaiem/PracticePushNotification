package com.example.practicepushnotification;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.example.practicepushnotification.data.model.Contact;
import com.example.practicepushnotification.ui.view.MainActivity;
import com.example.practicepushnotification.ui.viewModel.MainActivityViewModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FirebaseMessagingServic";

    private FirebaseFirestore firebaseDatabase = FirebaseFirestore.getInstance();

    private static final int REQUEST_CODE = 101;

    private MainActivityViewModel mainActivityViewModel  = new MainActivityViewModel();


    private String IMEINumber;

    public MyFirebaseMessagingService() {
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        getIMEI();



        Map<String, String> data = remoteMessage.getData();


        String title = remoteMessage.getData().get("title");
        String message = remoteMessage.getData().get("body");

        String id = remoteMessage.getData().get("id");
        String name = remoteMessage.getData().get("name");
        String phoneNumber = remoteMessage.getData().get("number");




        Log.d(TAG, "From: " + remoteMessage.getFrom());

        Log.d(TAG, "onMessageReceived: Message Received: \n" +
                "Title: " + title + "\n" +
                "Message: " + message);

        if (remoteMessage.getData() != null) {
            passTORealm(id,name,phoneNumber);
            sendNotification(title, message);
        }

//        if (remoteMessage.getNotification() != null) {
//            Log.e(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
//        }
    }

    //pass the new contact received by push notification
    private void passTORealm(String id, String name, String phoneNumber) {

        Contact newContact = new Contact();
        newContact.setId(id);
        newContact.setName(name);
        newContact.addPhoneNumber(phoneNumber);
        List<Contact> passContacts = new ArrayList<>();
        passContacts.add(newContact);
        mainActivityViewModel.storeinRealm(passContacts);
        mainActivityViewModel.writeinFirebase(firebaseDatabase,IMEINumber);

    }


    //Get IMEI number
    @SuppressLint("HardwareIds")
    private void getIMEI() {
        Context mContext = getApplicationContext();

        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) mContext, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_CODE);
            return;
        }
        IMEINumber = telephonyManager.getDeviceId();


    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }


    private void sendNotification(String title, String messageBody) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = getString(R.string.default_notification_channel_id);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,channelId)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }



        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());

    }




}
