package com.andsomore.sosinfosante.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.andsomore.sosinfosante.MainActivity;
import com.andsomore.sosinfosante.MainSecouristeActivity;
import com.andsomore.sosinfosante.R;
import com.andsomore.sosinfosante.WelcomeActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Random;

public class MyFirebaseInstanceService extends FirebaseMessagingService {
    public MyFirebaseInstanceService() {
        super();
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if(!remoteMessage.getData().isEmpty())
            showNotification(remoteMessage.getData());
        Log.d("Message:",remoteMessage.getData().toString());

    }

    private void showNotification(Map<String, String> data) {
        String title = data.get("title").toString();
        String body = data.get("body").toString();
        String click_action = data.get("click_action").toString();
        NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "adsm.firestore.ntotifications";
        Intent notificationIntent;
        if(click_action.equals("MainActivity")){
            String idIntervention = data.get("idIntervention").toString();
            String idIncident= data.get("idIncident").toString();
            String codeSecouriste= data.get("codeSecouriste").toString();
            notificationIntent  = new Intent(this, MainActivity.class);
            notificationIntent.putExtra("idIncident",idIncident);
            notificationIntent.putExtra("idIntervention",idIntervention);
            notificationIntent.putExtra("code",codeSecouriste);


        }else{
            String idIncident = data.get("idIncident").toString();
            String quartier = data.get("quartier").toString();
            String description = data.get("description").toString();
            notificationIntent  = new Intent(this, MainSecouristeActivity.class);
            notificationIntent.putExtra("idIncident",idIncident);
            notificationIntent.putExtra("quartier",quartier);
            notificationIntent.putExtra("description",description);


        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,"Notification",NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("ADSM Channel");
            channel.enableLights(true);
            channel.setLightColor(R.color.colorRed1);
            channel.setVibrationPattern(new long[]{0,1000});
            manager.createNotificationChannel(channel);

        }
            try {
                final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                        PendingIntent.FLAG_ONE_SHOT);
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
                notificationBuilder.setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setWhen(System.currentTimeMillis())
                        .setSmallIcon(R.drawable.ic_sos)
                        .setColor(getColor(R.color.colorRed))
                        .setContentTitle(title)
                        .setContentText(body)
                        .setChannelId(NOTIFICATION_CHANNEL_ID)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                        .setVibrate(new long[]{1000})
                        .setContentIntent(pendingIntent)
                        .setContentInfo("Info");
                manager.notify(new Random().nextInt(), notificationBuilder.build());
            } catch (Exception e) {
                Log.e("Message error:", e.getMessage());
            }
        }



    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Log.d("TOKEN:", s);
    }


}
