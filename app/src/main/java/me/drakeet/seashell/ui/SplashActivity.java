package me.drakeet.seashell.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.TextView;

import org.litepal.tablemanager.Connector;

import me.drakeet.seashell.BuildConfig;
import me.drakeet.seashell.R;
import me.drakeet.seashell.api.Api;
import me.drakeet.seashell.model.Version;
import me.drakeet.seashell.utils.HttpDownloader;
import me.drakeet.seashell.utils.TaskUtils;
import me.drakeet.seashell.widget.PullScrollView;

/**
 *
 * Created by drak11t on 7/12/2014.
 */
public class SplashActivity extends Activity {

    @Override
    public void onCreate(Bundle icicle) {
        SQLiteDatabase db = Connector.getDatabase();
        super.onCreate(icicle);
        getWindow().setFormat(PixelFormat.RGBA_8888);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);

        setContentView(R.layout.activity_splash);

        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        PullScrollView.mWidth = (int) (metric.widthPixels / 2); // 获取屏幕宽度（像素）

        //Display the current version number
        TextView versionNumber = (TextView) findViewById(R.id.versionNumber);
        versionNumber.setText("Version " + BuildConfig.VERSION_NAME);

        TaskUtils.executeAsyncTask(
                new AsyncTask<Object, Object, Object>() {
                    String note;
                    Version version;
                    @Override
                    protected Object doInBackground(Object... params) {
                        HttpDownloader httpDownloader = new HttpDownloader();
                        note = httpDownloader.download(Api.GET_NOTE);
                        //version = httpDownloader.download(Api.)
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Object o) {
                        super.onPostExecute(o);

                        new Handler().postDelayed(
                                new Runnable() {
                                    public void run() {
                                        Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
                                        mainIntent.putExtra(MainActivity.NOTE, note);
                                        SplashActivity.this.startActivity(mainIntent);
                                        SplashActivity.this.finish();
                                    }
                                }, 680
                        ); //time for release
                    }
                }
        );
    }
}
