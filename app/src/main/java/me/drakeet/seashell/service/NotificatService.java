package me.drakeet.seashell.service;

import java.util.Date;
import java.util.Map;
import java.util.Random;

import com.google.gson.Gson;

import me.drakeet.seashell.api.Api;
import me.drakeet.seashell.ui.MainActivity;
import me.drakeet.seashell.utils.HttpDownloader;
import me.drakeet.seashell.utils.MySharedpreference;
import me.drakeet.seashell.R;
import me.drakeet.seashell.model.Word;
import me.drakeet.seashell.utils.NotificationUtils;
import me.drakeet.seashell.utils.TaskUtils;
import me.drakeet.seashell.utils.ToastUtils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
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

    public static boolean isRun      = false;
    public static boolean hasNewWord = true;

    private volatile boolean stopRequested;
    private boolean isFirstTime = true;

    private static long firstTime;

    private Thread thread;

    private String mTodayGsonString;
    private String mYesterdayGsonString;

    private MySharedpreference mSharedpreference;

    private LocalBinder localBinder = new LocalBinder();

    private int mChangeWordTime;

    @Override
    public void onCreate() {
        super.onCreate();
        mSharedpreference = new MySharedpreference(this);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return localBinder;
    }

    public class LocalBinder extends Binder {
        public NotificatService getService() {
            return NotificatService.this;
        }

        @Override
        protected boolean onTransact(int code, final Parcel data, final Parcel reply,
                                     int flags) throws RemoteException {
            //表示从activity中获取数值
            TaskUtils.executeAsyncTask(
                    new AsyncTask<Object, Object, Object>() {
                        boolean change;

                        @Override
                        protected Object doInBackground(Object... params) {
                            mChangeWordTime = data.readInt();
                            Bundle bundle = data.readBundle();
                            change = bundle.getBoolean("change");
                            startNotification();
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Object o) {
                            super.onPostExecute(o);
                            if (mChangeWordTime > 0) {
                                if (change)
                                    ToastUtils.showLong("更换成功:)");
                                else
                                    ToastUtils.showShort("刷新成功:)");
                            } else {
                                ToastUtils.showShort("请不要短时间内一直更换");
                            }
                            reply.writeInt(200);
                        }
                    }
            );
            return super.onTransact(code, data, reply, flags);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // startNotification();
        thread = new Thread(
                new Runnable() {

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
                                Thread.sleep(238 * 1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
        );
        thread.start();

        return super.onStartCommand(intent, flags, startId);
    }

    public void changeNewAndOldWord() {
        Map map = mSharedpreference.getWordJson();
        // 如果和最新的单词是一样的，就取消更新
        if (((String) map.get("today_json")).equals(mTodayGsonString)
                || mTodayGsonString == null
                || mTodayGsonString.isEmpty()) {
            return;
        }
        if (mWord != null) {
            mWord.save();// save the new word to wordlist.db
        }

        Map map2 = mSharedpreference.getInfo();
        int honor = (Integer) map2.get("honor");
        honor++;
        mSharedpreference.saveHonor(honor);

        mYesterdayGsonString = (String) map.get("today_json"); // 将今天的存至昨天的
        mSharedpreference.saveYesterdayJson(mYesterdayGsonString);
        mSharedpreference.saveTodayJson(mTodayGsonString);
        hasNewWord = true;
    }

    private int getCurrentWordId() {
        return mSharedpreference.getCurrentWordId();
    }

    private String getDownloadString() {
        HttpDownloader httpDownloader = new HttpDownloader();
        return httpDownloader.download(String.format(Api.GET_WORD, getCurrentWordId()));
//        return httpDownloader.download(getString(R.string.api));
    }

    public void startNotification() {
        mTodayGsonString = getDownloadString();
        if (mTodayGsonString == null || mTodayGsonString.isEmpty()) {
            try {
                Thread.sleep(100 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mTodayGsonString = getDownloadString();
        }
        try {
            mWord = new Word();
            Gson gson = new Gson();
            mWord = gson.fromJson(mTodayGsonString, Word.class);
        } catch (Exception e) {
            int id = getCurrentWordId();
            id -= 3;
            if (id < 6)
                id = 6;
            mSharedpreference.setCurrentWordId(id);
        }

        NotificationUtils.showWordInNotificationBar(this, mWord);

        if (mWord != null) {
            Message message = Message.obtain();
            message.obj = mWord;
            if (MainActivity.mUpdateTodayWordHandler != null)
                MainActivity.mUpdateTodayWordHandler.sendMessage(message);
        }


        changeNewAndOldWord();// 更换单词

        if (MainActivity.mTodayWord != null)
            MainActivity.mTodayWord = mWord;
    }

    @Override
    public void onDestroy() {
        ToastUtils.showShort("service destroyed");
        stopRequested = true;
        thread.interrupt();
        isRun = false;
        super.onDestroy();
    }
}
