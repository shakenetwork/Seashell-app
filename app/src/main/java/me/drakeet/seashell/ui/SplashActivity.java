package me.drakeet.seashell.ui;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import org.litepal.tablemanager.Connector;

import java.io.File;

import me.drakeet.materialdialog.MaterialDialog;
import me.drakeet.seashell.R;
import me.drakeet.seashell.utils.HttpDownloader;
import me.drakeet.seashell.widget.PullScrollView;

/**
 *
 * 这个文件有BUG，MaterialDialog 显示存在问题！！！
 * Created by drak11t on 7/12/2014.
 */
public class SplashActivity extends Activity {

    private long mDownloadReference;

    private DownloadManager downloadManager;

    private IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);

    private BroadcastReceiver receiver;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    //需要更新 显示对话框
                    showDialog();
                    //downloadNewVersion();
                    break;
            }
        }
    };
    private MaterialDialog mMaterialDialog;

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
        PackageManager pm = getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo("me.drakeet.seashell", 0);

            TextView versionNumber = (TextView) findViewById(R.id.versionNumber);
            versionNumber.setText("Version " + pi.versionName);

            //初始化下载完成的广播接收器
            initBroadcast();
            //检查是否需要更新
            checkUpdate(pi.versionName);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        // gotoMainUI();
    }

    private void initBroadcast() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long reference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (mDownloadReference == reference) {
                    //对下载的文件进行操作
                    File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "seashell.apk");
                    if (file.exists()) {
                        installApk(file);
                    } else {
                        Toast.makeText(SplashActivity.this, "自动安装失败, 请手动安装", Toast.LENGTH_LONG).show();
                    }
                }
            }
        };

        registerReceiver(receiver, filter);
    }

    /**
     * 检查是否要更新
     *
     * @param currentVersion 当前版本号
     */
    private void checkUpdate(final String currentVersion) {
        Thread thread = new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        HttpDownloader httpDownloader = new HttpDownloader();
                        String newVersion = httpDownloader.download("http://test.drakeet.me/?key=s_check_update");

                        if (!currentVersion.equals(newVersion)) {
                            handler.sendEmptyMessage(1);
                        } else {
                            //TODO 我觉得应该在这里做跳转到主页面的操作
                            gotoMainUI();
                        }
                    }
                }
        );
        thread.start();
    }

    /**
     * 显示对话框 提示用户是否需要下载新的版本
     */
    private void showDialog() {
        mMaterialDialog = new MaterialDialog(this);
        mMaterialDialog.setTitle("有爱的小提示");
        mMaterialDialog.setMessage("贝壳单词有新版本啦！你要下载咩？");

        mMaterialDialog.setPositiveButton(
                "确定", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mMaterialDialog.dismiss();
                        Toast.makeText(SplashActivity.this, "下载即将开始", Toast.LENGTH_LONG).show();
                        downloadNewVersion();
                        gotoMainUI();
                    }
                }
        );

        mMaterialDialog.setNegativeButton(
                "取消", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mMaterialDialog.dismiss();
                        //TODO 这里做跳转到主界面的操作
                        gotoMainUI();
                    }
                }
        );
        mMaterialDialog.setCanceledOnTouchOutside(false);
        mMaterialDialog.show();
    }

    /**
     * 下载新的版本
     */
    private void downloadNewVersion() {
        downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse("http://drakeet.me/seashell.apk");
        DownloadManager.Request request = new DownloadManager.Request(uri);
        //wifi下可下载 不需要的话可注释掉
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        //指定下载位置 外部存储器根目录
        request.setDestinationUri(Uri.fromFile(Environment.getExternalStorageDirectory()));
        request.setTitle("贝壳单词");
        //下载中 和 下载完成会一直显示 直到被选择或者取消
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        mDownloadReference = downloadManager.enqueue(request);
    }

    /*
     * 自动跳转到安装界面
     *
     * 哎...不知道会不会成功
     */
    private void installApk(File file) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        startActivity(intent);
        this.finish();
    }

    /**
     * 跳转到主界面
     */
    private void gotoMainUI() {
        new Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
                        SplashActivity.this.startActivity(mainIntent);
                        SplashActivity.this.finish();
                    }
                }, 618
        ); //618 for release
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }
}