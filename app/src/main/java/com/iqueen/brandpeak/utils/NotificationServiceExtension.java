package com.sgdigitalposter.app.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.sgdigitalposter.app.Config;
import com.sgdigitalposter.app.R;
import com.sgdigitalposter.app.ui.activities.SplashyActivity;
import com.onesignal.notifications.IDisplayableNotification;
import com.onesignal.notifications.INotificationReceivedEvent;
import com.onesignal.notifications.INotificationServiceExtension;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

@SuppressWarnings("unused")
public class NotificationServiceExtension implements INotificationServiceExtension {

    public String id = "";
    public String type = "";
    public String name = "";
    public String link = "";
    public boolean video = false;
    public Context context;
    public static final String CHANNEL_ID = "iqueen_channel";
    public static final int NOTIFICATION_ID = 1;
    PrefManager prefManager;

    @Override
    public void onNotificationReceived(@NonNull INotificationReceivedEvent iNotificationReceivedEvent) {
        this.context = iNotificationReceivedEvent.getContext();
        prefManager = new PrefManager(context);

        if (prefManager.getBoolean(Constant.NOTIFICATION_ENABLED)) {
            IDisplayableNotification notification = iNotificationReceivedEvent.getNotification();
            Util.showLog("NOTIFICATION: " + notification.toString());

            JSONObject data = notification.getAdditionalData();

            Util.showLog(data.toString());
            try {
                type = data.getString("type");
                video = data.getBoolean("video");
                if (type.equals(Constant.CATEGORY)) {

                    id = data.getString("id");
                    name = data.getString("name");

                } else if (type.equals(Constant.FESTIVAL)) {

                    id = data.getString("id");
                    name = data.getString("festival");

                } else if (type.equals(Constant.SUBS_PLAN)) {

                    id = data.getString("id");
                    name = data.getString("subscriptionPlan");

                } else if (type.equals(Constant.CUSTOM_FEATURE)) {

                    id = data.getString("id");
                    name = data.getString("custom");

                } else if (type.equals(Constant.EXTERNAL)) {
                    link = data.getString("externalLink");
                    // intent = new Intent(Intent.ACTION_VIEW, Uri.parse(data.getString("externalLink")));
                }

            } catch (JSONException e) {
                Util.showErrorLog(e.getMessage(), e);
            }
            prefManager.setBoolean(Constant.IS_NOT, true);
            prefManager.setString(Constant.PRF_ID, id);
            prefManager.setString(Constant.PRF_NAME, name);
            prefManager.setString(Constant.PRF_TYPE, type);
            prefManager.setString(Constant.PRF_LINK, link);
            prefManager.setBoolean(Constant.INTENT_VIDEO, video);

//            osNotificationReceivedEvent.complete(mutableNotification);
//            osNotificationReceivedEvent.notify();

            sendNotification(notification);
            iNotificationReceivedEvent.preventDefault(false);
//            osNotificationReceivedEvent.complete(null);
        }
    }

    private void sendNotification(IDisplayableNotification notification) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = null;

        intent = new Intent(context, SplashyActivity.class);
        intent.putExtra(Constant.INTENT_TYPE, type);
        intent.putExtra(Constant.INTENT_IS_FROM_NOTIFICATION, true);
        intent.putExtra(Constant.INTENT_FEST_ID, id);
        intent.putExtra(Constant.INTENT_FEST_NAME, name);
        intent.putExtra(Constant.INTENT_POST_IMAGE, "");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        Config.isFromNotifications = true;

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);


        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationChannel mChannel;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "QuotesPush";// The user-visible name of the channel.
            int importance = NotificationManager.IMPORTANCE_HIGH;
            mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mNotificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setAutoCancel(true)
                .setSound(uri)
                .setAutoCancel(true)
                .setLights(Color.RED, 800, 800)
                .setContentText(notification.getBody())
                .setChannelId(CHANNEL_ID);

        mBuilder.setSmallIcon(getNotificationIcon(mBuilder));
        try {
            mBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher));
        } catch (Exception e) {
            Toast.makeText(context.getApplicationContext(), "errror large- " + e.getMessage(), Toast.LENGTH_LONG).show();
            prefManager.setBoolean(Constant.IS_NOT, false);
        }

        if (notification.getTitle().trim().isEmpty()) {
            mBuilder.setContentTitle(context.getString(R.string.app_name));
            mBuilder.setTicker(context.getString(R.string.app_name));
        } else {
            mBuilder.setContentTitle(notification.getTitle());
            mBuilder.setTicker(notification.getTitle());
        }

        if (notification.getBigPicture() != null) {
            mBuilder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(getBitmapFromURL(notification.getBigPicture())));
        }

        mBuilder.setContentIntent(pendingIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());

    }

    private int getNotificationIcon(NotificationCompat.Builder notificationBuilder) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setColor(getColour());
            return R.mipmap.ic_launcher;
        } else {
            return R.mipmap.ic_launcher;
        }
    }

    private int getColour() {
        return 0x8b5630;
    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            InputStream input;
            if (src.contains("https://")) {
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                input = connection.getInputStream();
            } else {
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                input = connection.getInputStream();
            }
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            return null;
        }
    }
}

//public class NotificationServiceExtension implements OneSignal. {
//    @Override
//    public void remoteNotificationReceived(Context context, OSNotificationReceivedEvent osNotificationReceivedEvent) {
//        this.context = context;
//        prefManager = new PrefManager(context);
//
//        if(prefManager.getBoolean(Constant.NOTIFICATION_ENABLED)) {
//            OSNotification notification = osNotificationReceivedEvent.getNotification();
//            Util.showLog("NOTIFICATION: " + notification.toString());


            /*OSMutableNotification mutableNotification = notification.mutableCopy();
            mutableNotification.setExtender(builder -> {

                builder.setColor(new BigInteger("FF00FF00", 16).intValue());
                // Sets the notification Title to Red
                builder.setLights(Color.RED, 800, 800);
            */
            /*Spannable spannableTitle = new SpannableString(notification.getTitle());
            spannableTitle.setSpan(new ForegroundColorSpan(Color.RED), 0, notification.getTitle().length(), 0);
            builder.setContentTitle(spannableTitle);
            // Sets the notification Body to Blue
            Spannable spannableBody = new SpannableString(notification.getBody());
            spannableBody.setSpan(new ForegroundColorSpan(Color.BLUE), 0, notification.getBody().length(), 0);
            builder.setContentText(spannableBody);*/
            /*

                builder.setPriority(NotificationManager.IMPORTANCE_MAX);

                builder.setSmallIcon(getNotificationIcon(builder));
                try {
                    builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher));
                } catch (Exception e) {
                    Toast.makeText(context.getApplicationContext(), "errror large- " + e.getMessage(), Toast.LENGTH_LONG).show();
                    prefManager.setBoolean(Constant.IS_NOT, false);
                }

                if (notification.getTitle().trim().isEmpty()) {
                    builder.setContentTitle(context.getString(R.string.app_name));
                    builder.setTicker(context.getString(R.string.app_name));
                } else {
                    builder.setContentTitle(notification.getTitle());
                    builder.setTicker(notification.getTitle());
                }
                return builder;
            });*/
            // If complete isn't call within a time period of 25 seconds, OneSignal internal logic will show the original notification
            // To omit displaying a notification, pass `null` to complete()
//            JSONObject data = notification.getAdditionalData();

//            Util.showLog(data.toString());
//            try {
//                type = data.getString("type");
//                video = data.getBoolean("video");
//                if (type.equals(Constant.CATEGORY)) {
//
//                    id = data.getString("id");
//                    name = data.getString("name");
//
//                } else if (type.equals(Constant.FESTIVAL)) {
//
//                    id = data.getString("id");
//                    name = data.getString("festival");
//
//                } else if (type.equals(Constant.SUBS_PLAN)) {
//
//                    id = data.getString("id");
//                    name = data.getString("subscriptionPlan");
//
//                } else if (type.equals(Constant.CUSTOM_FEATURE)) {
//
//                    id = data.getString("id");
//                    name = data.getString("custom");
//
//                } else if (type.equals(Constant.EXTERNAL)) {
//                    link = data.getString("externalLink");
//                    // intent = new Intent(Intent.ACTION_VIEW, Uri.parse(data.getString("externalLink")));
//                }
//
//            } catch (JSONException e) {
//                Util.showErrorLog(e.getMessage(), e);
//            }
//            prefManager.setBoolean(Constant.IS_NOT, true);
//            prefManager.setString(Constant.PRF_ID, id);
//            prefManager.setString(Constant.PRF_NAME, name);
//            prefManager.setString(Constant.PRF_TYPE, type);
//            prefManager.setString(Constant.PRF_LINK, link);
//            prefManager.setBoolean(Constant.INTENT_VIDEO, video);
//
////            osNotificationReceivedEvent.complete(mutableNotification);
////            osNotificationReceivedEvent.notify();
//
//             sendNotification(notification);
//             osNotificationReceivedEvent.complete(null);
//        }
//    }

//    private void sendNotification(OSNotification notification) {
//        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//
//        Intent intent = null;
//
//        intent = new Intent(context, SplashyActivity.class);
//        intent.putExtra(Constant.INTENT_TYPE, type);
//        intent.putExtra(Constant.INTENT_IS_FROM_NOTIFICATION, true);
//        intent.putExtra(Constant.INTENT_FEST_ID, id);
//        intent.putExtra(Constant.INTENT_FEST_NAME, name);
//        intent.putExtra(Constant.INTENT_POST_IMAGE, "");
//        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//
//        Config.isFromNotifications = true;
//
//        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
//
//
//        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//
//        NotificationChannel mChannel;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            CharSequence name = "QuotesPush";// The user-visible name of the channel.
//            int importance = NotificationManager.IMPORTANCE_HIGH;
//            mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
//            mNotificationManager.createNotificationChannel(mChannel);
//        }
//
//        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
//                .setAutoCancel(true)
//                .setSound(uri)
//                .setAutoCancel(true)
//                .setLights(Color.RED, 800, 800)
//                .setContentText(notification.getBody())
//                .setChannelId(CHANNEL_ID);
//
//        mBuilder.setSmallIcon(getNotificationIcon(mBuilder));
//        try {
//            mBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher));
//        } catch (Exception e) {
//            Toast.makeText(context.getApplicationContext(), "errror large- " + e.getMessage(), Toast.LENGTH_LONG).show();
//            prefManager.setBoolean(Constant.IS_NOT, false);
//        }
//
//        if (notification.getTitle().trim().isEmpty()) {
//            mBuilder.setContentTitle(context.getString(R.string.app_name));
//            mBuilder.setTicker(context.getString(R.string.app_name));
//        } else {
//            mBuilder.setContentTitle(notification.getTitle());
//            mBuilder.setTicker(notification.getTitle());
//        }
//
//        if (notification.getBigPicture() != null) {
//            mBuilder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(getBitmapFromURL(notification.getBigPicture())));
//        }
//
//        mBuilder.setContentIntent(pendingIntent);
//        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
//
//    }
//
//    private int getNotificationIcon(NotificationCompat.Builder notificationBuilder) {
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            notificationBuilder.setColor(getColour());
//            return R.mipmap.ic_launcher;
//        } else {
//            return R.mipmap.ic_launcher;
//        }
//    }
//
//    private int getColour() {
//        return 0x8b5630;
//    }
//
//    public static Bitmap getBitmapFromURL(String src) {
//        try {
//            URL url = new URL(src);
//            InputStream input;
//            if (src.contains("https://")) {
//                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
//                connection.setDoInput(true);
//                connection.connect();
//                input = connection.getInputStream();
//            } else {
//                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//                connection.setDoInput(true);
//                connection.connect();
//                input = connection.getInputStream();
//            }
//            Bitmap myBitmap = BitmapFactory.decodeStream(input);
//            return myBitmap;
//        } catch (IOException e) {
//            return null;
//        }
//    }

//}
