package it.hci2020.smartlock;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Objects;

public class MyFirebaseMessagingService extends FirebaseMessagingService
{
    @Override
    public void onMessageReceived(RemoteMessage pRemoteMessage)
    {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        sendNotification(pRemoteMessage.getData().get("title"), pRemoteMessage.getData().get("body"));
    }

    private void sendNotification(String messageTitle, String messageBody)
    {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("menuFragment", "statusFragment");
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT);

        String channelId = "SmartLock";

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.notification)
                        .setContentTitle(messageTitle)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel channel = new NotificationChannel(channelId,"SmartLock", NotificationManager.IMPORTANCE_DEFAULT);

            Objects.requireNonNull(notificationManager).createNotificationChannel(channel);
        }

        Objects.requireNonNull(notificationManager).notify(0 /* ID of notification */, notificationBuilder.build());
    }
}