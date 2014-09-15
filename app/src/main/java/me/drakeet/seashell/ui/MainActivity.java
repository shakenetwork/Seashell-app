package me.drakeet.seashell.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.lurencun.cfuture09.androidkit.utils.ui.ExitDoubleClick;

import net.simonvt.menudrawer.MenuDrawer;
import net.simonvt.menudrawer.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import me.drakeet.seashell.model.Word;
import me.drakeet.seashell.service.NotificatService;
import me.drakeet.seashell.ui.notboringactionbar.NoBoringActionBarActivity;
import me.drakeet.seashell.utils.MySharedpreference;
import me.drakeet.seashell.widget.PullScrollView;

import me.drakeet.seashell.R;

/**
 * Created by drakeet on 9/14/14.
 */
public class MainActivity extends BaseListSample implements PullScrollView.OnTurnListener {

    public static boolean mIsPause = false;
    //private MenuDrawer mMenuDrawer;
    private TextView mYerterdayTitleTextView;
    private TextView mUseTimesTextView;
    private int MENU_SETTING;
    private Intent serviceIntent;
    public static Word mTodayWord;
    private static Word mYesterdayWord;
    private WordViewHoder mYesterdayWordViewHoder;
    private WordViewHoder mTodayWordViewHoder;
    private PullScrollView mScrollView;
    private ImageView mHeadImg;
    private TableLayout mMainLayout;
    private ViewPager mMainViewPager;
    private PagerTitleStrip mPagerTitleStrip;

    private List<View> mViewList;
    private List<String> mTitleList;

    private String mTimesSting;
    private boolean mIsBound;
    private NotificatService.LocalBinder mLocalBinder;
    private NotificatService mNotificatService;

    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mTodayWord = (Word) msg.obj;
            if (mTodayWord != null) {
                //start math
                setWordViewContent(mTodayWordViewHoder, mTodayWord);
            }
        }
    };

    // 链接activity和service
    public ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mIsBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mLocalBinder = (NotificatService.LocalBinder) service;
            mNotificatService = mLocalBinder.getService();
            mIsBound = true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initWord();
        initView();
        serviceIntent = new Intent(this, NotificatService.class);
        startService(serviceIntent);
        // 绑定service的服务
        bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIsPause = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsPause = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mIsBound) {
            this.unbindService(mServiceConnection);
        }
    }

    protected void initView() {
        mMenuDrawer.setContentView(R.layout.activity_main);
        mScrollView = (PullScrollView) findViewById(R.id.scroll_view);
        mHeadImg = (ImageView) findViewById(R.id.background_img);
        mUseTimesTextView = (TextView) findViewById(R.id.use_times);
        mUseTimesTextView.setText(mTimesSting);
        mScrollView.setOnTurnListener(this);
        mScrollView.init(mHeadImg);
        mMainViewPager = (ViewPager) findViewById(R.id.viewpage_main);
        mPagerTitleStrip = (PagerTitleStrip) findViewById(R.id.pager_title_strip_main);

        View viewYesterday = LayoutInflater.from(MainActivity.this).inflate(R.layout.view_main, null);
        View viewToday = LayoutInflater.from(MainActivity.this).inflate(R.layout.view_main, null);

        mViewList = new ArrayList<View>();
        mViewList.add(viewYesterday);
        mViewList.add(viewToday);
        mTitleList = new ArrayList<String>();
        mTitleList.add("Yesterday");
        mTitleList.add("Today");
        mMainViewPager.setAdapter(new MainViewPagerAdapter());
        mMainViewPager.setCurrentItem(1);

        mYesterdayWordViewHoder = getView(viewYesterday);
        mTodayWordViewHoder = getView(viewToday);

        mMainViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            int position;

            @Override
            public void onPageScrolled(int i, float v, int i2) {

            }

            @Override
            public void onPageSelected(int i) {
                position = i;
            }

            @Override
            public void onPageScrollStateChanged(int i) {
                initWord();
                if (position == 0 && mYesterdayWord != null) {
                    setWordViewContent(mYesterdayWordViewHoder, mYesterdayWord);
                } else if (position == 1 && mTodayWord != null) {
                    setWordViewContent(mTodayWordViewHoder, mTodayWord);
                }
            }
        });

        mMainViewPager.requestFocus();
        mMainViewPager.setFocusableInTouchMode(true);
        if (mTodayWord != null) {
            setWordViewContent(mTodayWordViewHoder, mTodayWord);
        }
    }

    private void setWordViewContent(WordViewHoder wordViewHoder, Word word) {
        wordViewHoder.wordTextView.setText(word.getWord());
        wordViewHoder.phoneticTextView.setText(word.getPhonetic());
        wordViewHoder.speechTextView.setText(word.getSpeech());
        wordViewHoder.explanationTextView.setText(word.getExplanation());
        wordViewHoder.exampleTextView.setText(word.getExample());
    }

    private WordViewHoder getView(View view) {
        WordViewHoder wordViewHoder = new WordViewHoder();
        wordViewHoder.wordTextView = (TextView) view.findViewById(R.id.content_word);
        wordViewHoder.phoneticTextView = (TextView) view.findViewById(R.id.content_phonetic);
        wordViewHoder.speechTextView = (TextView) view.findViewById(R.id.content_speech);
        wordViewHoder.explanationTextView = (TextView) view.findViewById(R.id.content_explanation);
        wordViewHoder.exampleTextView = (TextView) view.findViewById(R.id.content_example);
        return wordViewHoder;
    }

    //更新单词数据
    private void initWord() {
        Map<String, String> map;
        Gson gson = new Gson();
        Context context = getApplicationContext();
        MySharedpreference sharedpreference = new MySharedpreference(context);
        map = sharedpreference.getWordJson();
        //取出
        String todayGsonString = map.get("today_json");
        String yesterdayGsonString = map.get("yesterday_json");
        mTodayWord = gson.fromJson(todayGsonString, Word.class);
        mYesterdayWord = gson.fromJson(yesterdayGsonString, Word.class);
        //TODO
        Map<String, Object> map2;
        map2 = sharedpreference.getInfo();
        mTimesSting = "已更新 " + map2.get("honor") + " 次单词";
    }


    class MainViewPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return mViewList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitleList.get(position);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ((ViewPager) container).addView(mViewList.get(position));
            return mViewList.get(position);
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view == o;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ((ViewPager) container).removeView(mViewList.get(position));
        }
    }

    @Override
    public void onTurn() {

    }

    public void onRefreshClick(View view) {

        // 往Service中传递值的对象，到Service中去处理
        Parcel data = Parcel.obtain();
        data.writeInt(199);
        Parcel reply = Parcel.obtain();
        try {
            mLocalBinder.transact(IBinder.LAST_CALL_TRANSACTION, data,
                    reply, 0);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (reply.readInt() == 200) {
            Toast.makeText(getApplicationContext(), "更新成功", Toast.LENGTH_SHORT).show();
        }
    }

    // 键盘按键响应监听，主要监听了munu按键，用于弹出菜单
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_MENU) {
            mMenuDrawer.toggleMenu();
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            mMenuDrawer.toggleMenu();
            ExitDoubleClick.getInstance(this).doDoubleClick(1500, "再按一次返回键退出");
            return true;
        }
        return super.onKeyDown(keyCode, event); // 最后，一定要做完以后返回
        // true，或者在弹出菜单后返回true，其他键返回super，让其他键默认
    }

    public void onClickShare() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "分享");
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(Intent.createChooser(intent, getTitle()));
    }

    @Override
    protected void onMenuItemClicked(int position, Item item) {
        String title = item.mTitle;
        if (title.equals("分享")) {
            onClickShare();
        } else if (title.equals("关于")) {
            Intent intent = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(intent);
        } else if (title.equals("已背单词")) {
            startActivity(new Intent(MainActivity.this, NoBoringActionBarActivity.class));
        } else if (title.equals("退出")) {
            mMenuDrawer.closeMenu();
            //回到桌面
            Intent i = new Intent(Intent.ACTION_MAIN);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.addCategory(Intent.CATEGORY_HOME);
            startActivity(i);
        } else {
            Toast.makeText(getApplicationContext(), item.mTitle + "暂未完成开发...", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected int getDragMode() {
        return MenuDrawer.MENU_DRAG_CONTENT;
    }

    @Override
    protected Position getDrawerPosition() {
        return Position.END;
    }

    class WordViewHoder {

        TextView wordTextView;
        TextView phoneticTextView;
        TextView speechTextView;
        TextView explanationTextView;
        TextView exampleTextView;
    }
}
