package me.drakeet.seashell.ui;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
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
import android.text.Html;
import android.text.InputFilter;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.lurencun.cfuture09.androidkit.utils.ui.ExitDoubleClick;

import net.simonvt.menudrawer.MenuDrawer;
import net.simonvt.menudrawer.Position;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import me.drakeet.materialdialog.MaterialDialog;
import me.drakeet.seashell.model.FavoriteWord;
import me.drakeet.seashell.model.Word;
import me.drakeet.seashell.service.NotificatService;
import me.drakeet.seashell.ui.menu.MyMenuDrawer;
import me.drakeet.seashell.ui.menu.Item;
import me.drakeet.seashell.utils.MySharedpreference;
import me.drakeet.seashell.utils.ToastUtils;
import me.drakeet.seashell.widget.PullScrollView;

import me.drakeet.seashell.R;

/**
 * Created by drakeet on 9/14/14.
 */
public class MainActivity extends MyMenuDrawer implements PullScrollView.OnTurnListener, View.OnClickListener, View.OnLongClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String NOTE = "NOTE";

    public static boolean mIsPause = false;
    public static final int YESTERDAY = 0, TODAY = 1;

    public static Handler mUpdateTodayWordHandler;

    public static Word mTodayWord;
    private static Word mYesterdayWord;

    private TextView mUseTimesTextView;
    private Intent serviceIntent;
    private WordViewHoder mYesterdayWordViewHoder;
    private WordViewHoder mTodayWordViewHoder;
    private PullScrollView mScrollView;
    private ImageView mHeadImg;
    private ViewPager mMainViewPager;
    private PagerTitleStrip mPagerTitleStrip;
    private ProgressBar mYesterdayProgressBar;
    private ProgressBar mTodayProgressBar;

    private List<View> mViewList;
    private List<String> mTitleList;

    private String mTimesSting;
    private String mNoteString;
    private boolean mIsBound;
    private NotificatService.LocalBinder mLocalBinder;
    private NotificatService mNotificatService;

    private TextView mUserNameTextView;
    private String mUserName;
    private MySharedpreference mSharedpreference;
    private View mWordView;

    private int mChangeWordTimes = 3;

    // bind activity and service
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
        mSharedpreference = new MySharedpreference(this);
        initWord();

        mUpdateTodayWordHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                mTodayWord = (Word) msg.obj;
                if (mTodayWord != null) {
                    setWordViewContent(mTodayWordViewHoder, mTodayWord);
                }
            }
        };
        serviceIntent = new Intent(this, NotificatService.class);
        startService(serviceIntent);
        bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        mNoteString = getIntent().getStringExtra(NOTE);
    }

    private void updataLookCount(Boolean isPlus) {
        TextView textView = (TextView) findViewById(R.id.user_book);
        int lookCount = mSharedpreference.getInt("look_count");
        String s = "详细浏览次数：" + lookCount + " 次";
        textView.setText(s);
        if (isPlus)
            mSharedpreference.saveInt("look_count", ++lookCount);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Boolean isFromNotification = getIntent().getBooleanExtra("is_from_notification", false);
        // reset menudrawer position
        initView();
        if (isFromNotification) {
            ToastUtils.showShort("^ ^浏览次数+1");
            updataLookCount(true);
            getIntent().putExtra("is_from_notification", false);
        }
        mChangeWordTimes = 3;
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

    /**
     * init View and ViewHoder
     */
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
        mUserNameTextView = (TextView) findViewById(R.id.user_name);
        TextView noteTextView = (TextView) findViewById(R.id.tv_note);
        if (mNoteString != null) {
            noteTextView.setText(mNoteString);
            noteTextView.setSelected(true);
        }
        mUserNameTextView.setOnClickListener(this);
        mUserName = mSharedpreference.getString("username");
        if (mUserName != null) {
            mUserNameTextView.setText(mUserName);
        }
        updataLookCount(false);

        final View viewYesterday = LayoutInflater.from(MainActivity.this).inflate(R.layout.view_main, null);
        final View viewToday = LayoutInflater.from(MainActivity.this).inflate(R.layout.view_main, null);
        viewToday.setTag("today");
        viewYesterday.setTag("yesterday");
        viewToday.setOnLongClickListener(this);
        viewYesterday.setOnLongClickListener(this);
        mYesterdayProgressBar = (ProgressBar) viewYesterday.findViewById(R.id.content_progressbar);
        mTodayProgressBar = (ProgressBar) viewToday.findViewById(R.id.content_progressbar);

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
        mYesterdayWordViewHoder.setId(YESTERDAY);
        mTodayWordViewHoder.setId(TODAY);

        mMainViewPager.setOnPageChangeListener(
                new ViewPager.OnPageChangeListener() {

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
                        if (position == 0 && mYesterdayWord != null) {
                            setWordViewContent(mYesterdayWordViewHoder, mYesterdayWord);
                        } else if (position == 1 && mTodayWord != null) {
                            setWordViewContent(mTodayWordViewHoder, mTodayWord);
                        }
                    }
                }
        );

        mMainViewPager.requestFocus();
        mMainViewPager.setFocusableInTouchMode(true);
        if (mTodayWord != null) {
            setWordViewContent(mTodayWordViewHoder, mTodayWord);
        }
    }

    /**
     * set the content of the WordView
     *
     * @param wordViewHoder
     * @param word
     */
    public void setWordViewContent(WordViewHoder wordViewHoder, Word word) {
        initWord();
        try {
            wordViewHoder.wordTextView.setText(word.getWord());
            wordViewHoder.phoneticTextView.setText(word.getPhonetic());
            wordViewHoder.speechTextView.setText(word.getSpeech());
            wordViewHoder.explanationTextView.setText(word.getExplanation());

            wordViewHoder.exampleTextView.setText(Html.fromHtml(boldWordParser(word.getWord().trim(), word.getExample().trim())));
            if (wordViewHoder.getId() == YESTERDAY)
                mYesterdayProgressBar.setVisibility(View.INVISIBLE);
            else
                mTodayProgressBar.setVisibility(View.INVISIBLE);
        } catch (NullPointerException e) {
            ToastUtils.showShort("ID为" + mSharedpreference.getCurrentWordId() + "单词获取失败");
            onChangeOneClick(null);
        }
    }

    private String boldWordParser(String word, String example) {
        String string = "";
        int index_of_ln = example.indexOf("\n");
        for (int i = 0; i < index_of_ln; i++) {
            string += example.charAt(i) + "";
        }
        string += "<br>";
        for (int i = index_of_ln + "\n".length(); i < example.length(); i++) {
            string += example.charAt(i) + "";
        }
        example = string;
        string = "";
        int index = example.indexOf(word);
        for (int i = 0; i < index; i++) {
            string += example.charAt(i) + "";
        }
        string = string + "<b>" + word + "</b>";
        for (int i = index + word.length(); i < example.length(); i++) {
            string += example.charAt(i) + "";
        }
        return string;
    }

    /**
     * init the ViewHoder
     *
     * @param view the View that contain Word views.
     * @return WordViewHoder
     */
    private WordViewHoder getView(View view) {
        WordViewHoder wordViewHoder = new WordViewHoder();
        wordViewHoder.wordTextView = (TextView) view.findViewById(R.id.content_word);
        wordViewHoder.phoneticTextView = (TextView) view.findViewById(R.id.content_phonetic);
        wordViewHoder.speechTextView = (TextView) view.findViewById(R.id.content_speech);
        wordViewHoder.explanationTextView = (TextView) view.findViewById(R.id.content_explanation);
        wordViewHoder.exampleTextView = (TextView) view.findViewById(R.id.content_example);
        return wordViewHoder;
    }

    /**
     * init the word data from sharedpreference
     */
    private void initWord() {
        try {
            Map<String, String> map;
            Gson gson = new Gson();
            map = mSharedpreference.getWordJson();
            //取出
            String todayGsonString = map.get("today_json");
            String yesterdayGsonString = map.get("yesterday_json");
            mTodayWord = gson.fromJson(todayGsonString, Word.class);
            mYesterdayWord = gson.fromJson(yesterdayGsonString, Word.class);
            //TODO
            Map<String, Object> map2;
            map2 = mSharedpreference.getInfo();
            mTimesSting = "已更新 " + map2.get("honor") + " 次单词";
        } catch (Exception e) {
            e.printStackTrace();
            mSharedpreference.saveTodayJson("");
            mSharedpreference.saveYesterdayJson("");
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.user_name:
                updataUserName();
                break;
        }
    }

    private void updataUserName() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText editText = new EditText(this);
        builder.setTitle("修改用户名");
        editText.setHint("输入新用户名");
        editText.setPadding(30, 80, 16, 30);
        InputFilter[] filters = {new InputFilter.LengthFilter(10)};
        editText.setFilters(filters);
        builder.setView(editText);
        builder.setPositiveButton(
                "确定~", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mSharedpreference.saveString("username", editText.getText().toString());
                        mUserNameTextView.setText(editText.getText().toString());
                    }
                }
        );
        builder.setNegativeButton("取消", null);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public boolean onLongClick(View v) {
        Word word = null;
        String tag = (String) v.getTag();
        if (tag.equals("today")) {
            word = mTodayWord;
        } else if (tag.equals("yesterday")) {
            word = mYesterdayWord;
        }
        // 判断word是否收藏
        List<FavoriteWord> words = DataSupport.where("word = ?", word.getWord()).find(FavoriteWord.class);
        if (!words.isEmpty()) {
            ToastUtils.showShort("已在我的收藏，请不要重复添加");
            return true;
        }

        final MaterialDialog materialDialog = new MaterialDialog(this);
        materialDialog.setTitle("加入收藏?");
        materialDialog.setMessage("点击确定可把此单词加入『我的收藏』");

        if (tag.equals("today")) {
            materialDialog.setNegativeButton(
                    "取消",
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            materialDialog.dismiss();
                        }
                    }
            );
            materialDialog.setPositiveButton(
                    "确定",
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            saveWord(materialDialog, mTodayWord);
                        }
                    }
            );
            materialDialog.show();
        } else if (tag.equals("yesterday")) {
            materialDialog.setNegativeButton(
                    "取消",
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            materialDialog.dismiss();
                        }
                    }
            );
            materialDialog.setPositiveButton(
                    "确定",
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            saveWord(materialDialog, mYesterdayWord);
                        }
                    }
            );
            materialDialog.show();
        }
        return false;
    }

    // 收藏单词
    private void saveWord(MaterialDialog materialDialog, Word word) {
        ToastUtils.showShort("已加入收藏");
        materialDialog.dismiss();
        FavoriteWord favoriteWord = new FavoriteWord(word);
        favoriteWord.save();
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

    private void onRefresh(boolean change) {
        // 往Service中传递值的对象，到Service中去处理
        mTodayProgressBar.setVisibility(View.VISIBLE);
        Parcel data = Parcel.obtain();
        data.writeInt(mChangeWordTimes);
        Bundle bundle = new Bundle();
        bundle.putBoolean("change", change);
        data.writeBundle(bundle);
        Parcel reply = Parcel.obtain();

        try {
            mLocalBinder.transact(
                    IBinder.LAST_CALL_TRANSACTION, data,
                    reply, 0
            );
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onTurn() {
        ToastUtils.showShort("刷新...");
        onRefresh(false);
    }

    /**
     * on change one button click.
     *
     * @param view
     */
    public void onChangeOneClick(View view) {
        if (mChangeWordTimes > 0) {
            mSharedpreference.updateWordId();
        }
        onRefresh(true);
        mChangeWordTimes--;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_MENU) {
            mMenuDrawer.toggleMenu();
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            mMenuDrawer.toggleMenu();
            ExitDoubleClick.getInstance(this).doDoubleClick(1500, getString(R.string.double_click_exit));
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * on share item click
     */
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
            Intent intent = new Intent(MainActivity.this, WordListActivity.class);
            intent.putExtra("title", "已背单词");
            startActivity(intent);
        } else if (title.equals("我的收藏")) {
            Intent intent = new Intent(MainActivity.this, WordListActivity.class);
            intent.putExtra("title", "我的收藏");
            startActivity(intent);
        } else if (title.equals("设置")) {
            startActivity(new Intent(MainActivity.this, SettingActivity.class));
        } else if (title.equals(mUserName) || title.equals("注册/登录")) {
            updataUserName();
            ToastUtils.showLong("更改之后，侧滑菜单项目将在下一次启动后更新^ ^");
        } else if (title.equals("退出")) {
            mMenuDrawer.closeMenu();
            //回到桌面
            Intent i = new Intent(Intent.ACTION_MAIN);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.addCategory(Intent.CATEGORY_HOME);
            startActivity(i);
        } else {
            Toast.makeText(getApplicationContext(), item.mTitle + " 敬请期待^ ^", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected int getDragMode() {
        return MenuDrawer.MENU_DRAG_CONTENT;
    }

    @Override
    protected Position getDrawerPosition() {
        if (mSharedpreference != null && mSharedpreference.getBoolean("hand_switch")) {
            return Position.START;
        }
        return Position.END;
    }

    class WordViewHoder {
        int id;
        TextView wordTextView;
        TextView phoneticTextView;
        TextView speechTextView;
        TextView explanationTextView;
        TextView exampleTextView;

        public void setId(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }
}
