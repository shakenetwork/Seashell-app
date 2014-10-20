package me.drakeet.seashell.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import java.util.Random;

import me.drakeet.seashell.R;
import me.drakeet.seashell.model.Word;
import me.drakeet.seashell.ui.MainActivity;

/**
 * this class is separated from {@link me.drakeet.seashell.service.NotificatService}
 * Created by drakeet on 10/17/14.
 */
public class NotificationUtils {

    public static void showWordInNotificationBar(Context context, Word word) {
        Random random = new Random();
        int i = random.nextInt((int) SystemClock.uptimeMillis());

        NotificationCompat.Builder notifyBuilder =
            new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("未联网")
            .setContentText("请尝试联网后重启程序...");

        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        // Moves the big view style object into the notification object.
        notifyBuilder.setStyle(bigTextStyle);

        MySharedpreference mPrefs = new MySharedpreference(context);
        boolean isWithPhonetic = mPrefs.getBoolean(context.getString(R.string.notify_with_phonetic));
        if (word != null) {
            if (isWithPhonetic)
                notifyBuilder.setContentTitle(word.getWord() + " " + word.getPhonetic());
            else
                notifyBuilder.setContentTitle(word.getWord());
            notifyBuilder.setContentText(word.getSpeech() + " " + word.getExplanation());
            // init big view content
            bigTextStyle.bigText(word.getSpeech() + " " + word.getExplanation()
                    + "\n" + word.getExample());
        }
        // 这里用来显示更新时间
        notifyBuilder.setWhen(System.currentTimeMillis());
        Intent notifyIntent = new Intent(context, MainActivity.class);
        notifyIntent.putExtra("is_from_notification", true);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntent(notifyIntent);
        // 给notification设置一个独一无二的requestCode
        int requestCode = (int) SystemClock.uptimeMillis();
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                requestCode, PendingIntent.FLAG_UPDATE_CURRENT
        );
        notifyBuilder.setContentIntent(resultPendingIntent);
        notifyBuilder.setPriority(NotificationCompat.PRIORITY_MIN);
        notifyBuilder.setOngoing(true);
        long[] vibrate = {0, 50, 0, 0};
        notifyBuilder.setVibrate(vibrate);

        Notification notification = notifyBuilder.build();
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        int NOTIFY_ID = 524947901;
        mNotificationManager.notify(NOTIFY_ID, notification);
    }
}
