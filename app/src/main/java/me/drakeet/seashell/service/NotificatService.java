package me.drakeet.seashell.service;

import java.util.Date;
import java.util.Map;
import java.util.Random;

import com.google.gson.Gson;

import me.drakeet.seashell.ui.MainActivity;
import me.drakeet.seashell.utils.HttpDownloader;
import me.drakeet.seashell.utils.MySharedpreference;
import me.drakeet.seashell.R;
import me.drakeet.seashell.model.Word;
import me.drakeet.seashell.utils.TaskUtils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

/**
 * Changed by drakeet on 9/18/2014.
 */
public class NotificatService extends Service {

    private Word mWord;

    public static boolean isRun = false;
    static long firstTime;
    Thread thread;
    private volatile boolean stopRequested;
    boolean isFirstTime = true;
    private String mTodayGsonString;
    private String mYesterdayGsonString;
    private LocalBinder localBinder = new LocalBinder();
    public static boolean hasNewWord = true;

    @Override
    public IBinder onBind(Intent arg0) {
        return localBinder;
    }

    public class LocalBinder extends Binder {
        public NotificatService getService() {
            return NotificatService.this;
        }

        @Override
        protected boolean onTransact(int code, Parcel data, final Parcel reply,
                                     int flags) throws RemoteException {
            //表示从activity中获取数值
            if (data.readInt() == 199) {
                TaskUtils.executeAsyncTask(new AsyncTask<Object, Object, Object>() {
                    @Override
                    protected Object doInBackground(Object... params) {
                        startNotification();
                        return null;
                    }

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        Toast.makeText(getApplicationContext(), "更新完成", Toast.LENGTH_SHORT).show();
                        reply.writeInt(200);
                    }
                });
            }
            return super.onTransact(code, data, reply, flags);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        thread = new Thread(new Runnable() {

            @Override
            public void run() {
                Date date = new Date();

                while (stopRequested == false) {
                    if (isFirstTime) {
                        firstTime = date.getDate();
                        startNotification();
                        isFirstTime = false;
                    }
                    date = new Date();
                    int currentTime = date.getDate();
                    if (currentTime != firstTime) {
                        startNotification();
                        firstTime = currentTime;
                    }

                    try {
                        Log.i("Seashell-->", "onStartCommand is runing");
                        Thread.sleep(120 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();

        return super.onStartCommand(intent, flags, startId);
    }

    public void changeNewAndOldWord() {
        Context context = getApplicationContext();
        MySharedpreference sharedpreference = new MySharedpreference(context);
        Map map = sharedpreference.getWordJson();
        // 如果和最新的单词是一样的，就取消更新
        if (((String) map.get("today_json")).equals(mTodayGsonString)
                || mTodayGsonString == null
                || mTodayGsonString.isEmpty()) {
            return;
        }
        if (mWord != null) {
            mWord.save();// save the new word to wordlist.db
        }
        mYesterdayGsonString = (String) map.get("today_json"); // 将今天的存至昨天的
        sharedpreference.saveYesterdayJson(mYesterdayGsonString);
        sharedpreference.saveTodayJson(mTodayGsonString);
        hasNewWord = true;
    }

    public void startNotification() {
        HttpDownloader httpDownloader = new HttpDownloader();
        mTodayGsonString = httpDownloader.download(getString(R.string.api));
        if (mTodayGsonString == null || mTodayGsonString.isEmpty()) {
            try {
                Thread.sleep(10 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mTodayGsonString = httpDownloader.download(getString(R.string.api));
        }
        mWord = new Word();
        Gson gson = new Gson();
        mWord = gson.fromJson(mTodayGsonString, Word.class);
        showWordInNotificationBar();

        if (mWord != null) {
            Message message = Message.obtain();
            message.obj = mWord;
            if (MainActivity.mUpdateTodayWordHandler != null)
                MainActivity.mUpdateTodayWordHandler.sendMessage(message);
        }

        Context context = getApplicationContext();
        MySharedpreference sharedpreference = new MySharedpreference(context);
        Map map = sharedpreference.getInfo();
        int honor = (Integer) map.get("honor");
        honor++;
        sharedpreference.saveHonor(honor);
        changeNewAndOldWord();// 更换单词
        sharedpreference.saveTodayJson(mTodayGsonString);

        if (MainActivity.mTodayWord != null)
            MainActivity.mTodayWord = mWord;
    }

    private void showWordInNotificationBar() {
        Random random = new Random();
        int i = random.nextInt((int) SystemClock.uptimeMillis());

        NotificationCompat.Builder notifyBuilder;
        notifyBuilder = new NotificationCompat.Builder(
                this);
        notifyBuilder.setSmallIcon(R.drawable.ic_launcher);
        // 初始化
        notifyBuilder.setContentTitle("未联网");
        notifyBuilder.setContentText("请尝试联网后重启程序...");
        if (mWord != null) {
            notifyBuilder.setContentTitle(mWord.getWord());
            notifyBuilder.setContentText(mWord.getSpeech() + " "
                    + mWord.getExplanation());
        }
        // 这里用来显示右下角的数字
        notifyBuilder.setWhen(System.currentTimeMillis());
        Intent notifyIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(notifyIntent);
        // 给notification设置一个独一无二的requestCode
        int requestCode = (int) SystemClock.uptimeMillis();
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                requestCode, PendingIntent.FLAG_UPDATE_CURRENT);
        notifyBuilder.setContentIntent(resultPendingIntent);
        notifyBuilder.setPriority(NotificationCompat.PRIORITY_MIN);
        notifyBuilder.setOngoing(true);
        long[] vibrate = {0, 50, 0, 0};
        notifyBuilder.setVibrate(vibrate);

        Notification notification = notifyBuilder.build();
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int NOTIFY_ID = 524947901;
        mNotificationManager.notify(NOTIFY_ID, notification);
    }

    @Override
    public void onDestroy() {
        stopRequested = true;
        thread.interrupt();
        isRun = false;
        super.onDestroy();
    }
}
