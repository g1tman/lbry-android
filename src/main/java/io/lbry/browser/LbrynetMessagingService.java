package io.lbry.browser;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import io.lbry.browser.reactmodules.UtilityModule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LbrynetMessagingService extends FirebaseMessagingService {

    private static final String TAG = "LbrynetMessagingService";

    private static final String NOTIFICATION_CHANNEL_ID = "io.lbry.browser.LBRY_ENGAGEMENT_CHANNEL";

    private static final String TYPE_SUBSCRIPTION = "subscription";

    private static final String TYPE_REWARD = "reward";

    private static final String TYPE_INTERESTS = "interests";

    private static final String TYPE_CREATOR = "creator";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        Map<String, String> payload = remoteMessage.getData();
        String type = null;
        String url = null;
        if (payload != null) {
            type = payload.get("type");
            url = payload.get("target");
        }

        if (type != null && getEnabledTypes().indexOf(type) > -1) {
            // Check if message contains a notification payload.
            RemoteMessage.Notification remoteNotification = remoteMessage.getNotification();
            if (remoteNotification != null) {
                Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
                sendNotification(remoteNotification.getTitle(), remoteNotification.getBody(), type, url);
            }
        }
    }

    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token);
    }

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        // TODO: Implement this method to send token to your app server.
    }

     /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String title, String messageBody, String type, String url) {
        //Intent intent = new Intent(this, MainActivity.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (url == null) {
            if (TYPE_REWARD.equals(type)) {
                url = "lbry://?rewards";
            } else {
                // default to home page
                url = "lbry://?discover";
            }
        }

        Intent launchIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        launchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                        .setColor(ContextCompat.getColor(this, R.color.lbryGreen))
                        .setSmallIcon(R.drawable.ic_lbry)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                NOTIFICATION_CHANNEL_ID, "LBRY Engagement", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(9898, notificationBuilder.build());
    }

    public List<String> getEnabledTypes() {
        SharedPreferences sp = getSharedPreferences(MainActivity.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        List<String> enabledTypes = new ArrayList<String>();

        if (sp.getBoolean(UtilityModule.RECEIVE_SUBSCRIPTION_NOTIFICATIONS, true)) {
            enabledTypes.add(TYPE_SUBSCRIPTION);
        }
        if (sp.getBoolean(UtilityModule.RECEIVE_REWARD_NOTIFICATIONS, true)) {
            enabledTypes.add(TYPE_REWARD);
        }
        if (sp.getBoolean(UtilityModule.RECEIVE_INTERESTS_NOTIFICATIONS, true)) {
            enabledTypes.add(TYPE_INTERESTS);
        }
        if (sp.getBoolean(UtilityModule.RECEIVE_CREATOR_NOTIFICATIONS, true)) {
            enabledTypes.add(TYPE_CREATOR);
        }

        return enabledTypes;
    }
}