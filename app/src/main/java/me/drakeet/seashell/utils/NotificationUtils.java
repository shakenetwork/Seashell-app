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

        NotificationCompat.Builder notifyBuilder;
        notifyBuilder = new NotificationCompat.Builder(
                context
        );
        notifyBuilder.setSmallIcon(R.drawable.ic_launcher);
        // 初始化
        notifyBuilder.setContentTitle("未联网");
        notifyBuilder.setContentText("请尝试联网后重启程序...");
        MySharedpreference mySharedpreference = new MySharedpreference(context);
        boolean isWithPhonetic = mySharedpreference.getBoolean(context.getString(R.string.notify_with_phonetic));
        if (word != null) {
            if (isWithPhonetic)
                notifyBuilder.setContentTitle(word.getWord() + " " + word.getPhonetic());
            else
                notifyBuilder.setContentTitle(word.getWord());
            notifyBuilder.setContentText(word.getSpeech() + " " + word.getExplanation());
        }
        // 这里用来显示右下角的数字
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
