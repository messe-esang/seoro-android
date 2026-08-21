package com.messe.seoro.service;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import com.appsflyer.AppsFlyerLib;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.messe.seoro.R;
import com.messe.seoro.kit.Kit;
import com.messe.seoro.kit.PrefKit;
import com.messe.seoro.kit.TelKit;
import com.messe.seoro.ui.MainActivity;
import com.messe.seoro.ui.SplashActivity;

import java.io.BufferedInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final int ID_NOTIFICATION_DEFAULT = 0;
    private String TAG = getClass().getSimpleName();

    @Override
    public void onRegistered(@NonNull String installationId) {
        super.onRegistered(installationId);
        Log.e("MyFirebaseMessaging", "Refreshed token: " + installationId);
    }

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.e("MyFirebaseMessaging", "Refreshed token: " + token);
        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        PrefKit.setPushToken(getApplicationContext(), token);
        String mem_idx = PrefKit.getMemberIdx(getApplicationContext());
        Log.e("MyFirebaseMessaging", "mem_idx : " + mem_idx);
        if (Kit.isNotNullNotEmpty(mem_idx)) {
            Log.e("MyFirebaseMessaging", "Refreshed token2: " + token);
            TelKit.tokenRegistrationToServer(this, mem_idx, token);
        }

        //# Appsflyer Start
        // Sending new token to AppsFlyer
        AppsFlyerLib.getInstance().updateServerUninstallToken(getApplicationContext(), token);
        //# Appsflyer End
// TODO: Implement this method to send any registration to your app's servers.
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO(developer): Handle FCM messages here.
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.

        //# Appsflyer Start
        if (remoteMessage.getData().containsKey("af-uinstall-tracking")) {
            return;
        }
        //# Appsflyer End

        Log.e(TAG, "remoteMessage.toString() = " + remoteMessage.toString());
        String from = remoteMessage.getFrom();
        Map<String, String> data = remoteMessage.getData();
        Log.e(TAG, "From: " + from);
        Log.e(TAG, "Notification Data: " + data);
        if (data != null) {
            //푸시 data
            //bbs_id => notice/event
            //bbs_ix => 게시물 IDX
            //title => 제목
            //content => 내용
            String title = data.get("title");
            String body = data.get("body");
            String link = data.get("link");
            String img_url = data.get("img_url");
            Log.e(TAG, "title = " + title);
            Log.e(TAG, "body = " + body);
            Log.e(TAG, "link = " + link);
            Log.e(TAG, "img_url = [" + img_url + "]");

            int notif_id = ID_NOTIFICATION_DEFAULT;
            showNotification(notif_id, title, body, link, img_url);
        }
    }

    public void showNotification(int notif_id, String title, String body, String link, String img_url) {
        if (notif_id < 0)
            notif_id = (int) Calendar.getInstance().getTimeInMillis();

        Intent intent = null;
        if (isAppIsInBackground(this)) {
            intent = new Intent(this, SplashActivity.class);    // launcher 가 SplashActivity 이므로 SplashActivity 실행 후 열림
        } else {
            intent = new Intent(this, MainActivity.class);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("url", link);

        // look up the notification manager service
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // The PendingIntent to launch our activity if the user selects this
        // notification.  Note the use of FLAG_CANCEL_CURRENT so that, if there
        // is already an active matching pending intent, cancel it and replace
        // it with the new array of Intents.
        // Java
        PendingIntent contentIntent = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        } else {
            contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

//        PendingIntent contentIntent = PendingIntent.getActivity(
//                this,
//                0 /* Request code */,
//                intent,
//                // PendingIntent.FLAG_UPDATE_CURRENT 설정해줘야 Extra 값이 갱신됨
//                PendingIntent.FLAG_UPDATE_CURRENT);
        long when = System.currentTimeMillis();
        boolean useWhiteIcon = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
        int smallIcon = useWhiteIcon ? R.drawable.ic_notification : R.drawable.ic_notification;
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Notification.BigTextStyle bigTextStyle = new Notification.BigTextStyle();
        bigTextStyle.bigText(body);

        Notification.Builder builder;

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Notification
            final String channel_id = getResources().getString(R.string.notification_channel_id);
            final String channel_name = getResources().getString(R.string.notification_channel_name);
            final String channel_description = getResources().getString(R.string.notification_channel_description);
//            final int importance = NotificationManager.IMPORTANCE_DEFAULT;
            // 헤드업 알림을 트리거할 수 있는 조건
            // - 사용자 액티비티가 전체 화면 모드이거나(앱이 fullScreenIntent를 사용할 경우)
            // - 알림의 우선 순위가 높고 벨소리나 진동을 사용할 경우
            final int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(channel_id, channel_name, importance);
            channel.setDescription(channel_description);
//            channel.enableLights(true);
//            channel.setLightColor(getResources().getColor(R.color.color_primary));
            channel.enableVibration(true);
//            channel.setVibrationPattern(new long[]{100, 200, 100, 200});
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

            builder = new Notification.Builder(this, channel.getId());
        } else {
            builder = new Notification.Builder(this);
        }

        if (Kit.isNotNullNotEmpty(img_url)) {
            try {
                URL url = new URL(img_url);
                URLConnection conn = url.openConnection();
                conn.connect();

                BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
                Bitmap img = BitmapFactory.decodeStream(bis);

                Notification.BigPictureStyle style = new Notification.BigPictureStyle(builder);
                style.bigPicture(img).setBigContentTitle(title);
                builder.setStyle(style);
                builder.setLargeIcon(img);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            builder.setStyle(bigTextStyle);
        }

        builder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(when)
                .setSmallIcon(smallIcon)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_notification))
                .setSound(defaultSoundUri)
                .setTicker(title)
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(contentIntent);
//                .setContentInfo(title);       //  the large text at the right-hand side of the notification

        if (useWhiteIcon) {
            builder.setColor(getResources().getColor(R.color.white, null));
        }

        Notification notif = builder.build();

        // 알림 메시지 터치시 자동 삭제
        notif.flags = Notification.FLAG_AUTO_CANCEL;

        // Note that we use R.layout.incoming_message_panel as the ID for
        // the notification.  It could be any integer you want, but we use
        // the convention of using a resource id for a string related to
        // the notification.  It will always be a unique number within your
        // application.
        nm.notify(notif_id, notif);
    }

    private boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        }

        return isInBackground;
    }
}
