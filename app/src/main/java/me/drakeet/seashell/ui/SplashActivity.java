package me.drakeet.seashell.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.TextView;

import org.litepal.tablemanager.Connector;

import me.drakeet.seashell.BuildConfig;
import me.drakeet.seashell.R;
import me.drakeet.seashell.widget.PullScrollView;

/**
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

        TextView versionNumber = (TextView) findViewById(R.id.versionNumber);
        versionNumber.setText(BuildConfig.VERSION_NAME);

        new Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
                        SplashActivity.this.startActivity(mainIntent);
                        SplashActivity.this.finish();
                    }
                }, 900
        ); //time for release
    }
}
