package me.drakeet.seashell.ui;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.RectF;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.drakeet.materialdialog.MaterialDialog;
import me.drakeet.seashell.R;
import me.drakeet.seashell.model.FavoriteWord;
import me.drakeet.seashell.model.Word;
import me.drakeet.seashell.ui.notboringactionbar.AlphaForegroundColorSpan;
import me.drakeet.seashell.ui.notboringactionbar.KenBurnsView;
import me.drakeet.seashell.utils.ToastUtils;

/**
 * Changed by drakeet on 9/18/2014.
 */
public class WordListActivity extends BaseActivity {

    private static final String TAG = "WordListActivity";
    private int                              mActionBarTitleColor;
    private int                              mActionBarHeight;
    private int                              mHeaderHeight;
    private int                              mMinHeaderTranslation;
    private ListView                         mListView;
    private KenBurnsView                     mHeaderPicture;
    private ImageView                        mHeaderLogo;
    private View                             mHeader;
    private View                             mPlaceHolderView;
    private AccelerateDecelerateInterpolator mSmoothInterpolator;

    private RectF mRect1 = new RectF();
    private RectF mRect2 = new RectF();

    private AlphaForegroundColorSpan mAlphaForegroundColorSpan;
    private SpannableString          mSpannableString;

    private TypedValue mTypedValue = new TypedValue();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSmoothInterpolator = new AccelerateDecelerateInterpolator();
        mHeaderHeight = getResources().getDimensionPixelSize(R.dimen.header_height);
        mMinHeaderTranslation = -mHeaderHeight + getActionBarHeight();

        setContentView(R.layout.activity_wordlist);

        mListView = (ListView) findViewById(R.id.listview);
        mHeader = findViewById(R.id.header);
        mHeaderPicture = (KenBurnsView) findViewById(R.id.header_picture);
        mHeaderPicture.setResourceIds(R.drawable.picture0, R.drawable.picture1);
        mHeaderLogo = (ImageView) findViewById(R.id.header_logo);

        mActionBarTitleColor = getResources().getColor(R.color.actionbar_title_color);

        mSpannableString = new SpannableString(getString(R.string.noboringactionbar_title));
        mAlphaForegroundColorSpan = new AlphaForegroundColorSpan(mActionBarTitleColor);

        setupActionBar();
        setupListView();
    }

    List<FavoriteWord> wordList = null;

    private void setupListView() {
        String title = getIntent().getStringExtra("title");
        System.out.println("-->" + title);
        ArrayList<String> tData = new ArrayList<String>();
        if (title.equals("已背单词")) {
            List<Word> wordList0 = DataSupport.findAll(Word.class);
            for (Word word : wordList0) {
                tData.add(word.getWord() + " " + word.getSpeech() + " " + word.getExplanation());
            }
            Collections.reverse(tData);
        } else if (title.equals("我的收藏")) {
            wordList = DataSupport.findAll(FavoriteWord.class);
            for (FavoriteWord word : wordList) {
                tData.add(word.getWord() + " " + word.getSpeech() + " " + word.getExplanation());
            }
            Collections.reverse(tData);
        }

        mPlaceHolderView = getLayoutInflater().inflate(R.layout.view_header_placeholder, mListView, false);
        mListView.addHeaderView(mPlaceHolderView);
        final MyAdapter ma = new MyAdapter(this, wordList);
        if (title.equals("已背单词")) {
            mListView.setAdapter(new ArrayAdapter<String>(this, R.layout.item_wordlist, tData));
        } else {
            Collections.reverse(wordList);
            mListView.setAdapter(ma);
        }
        final List<FavoriteWord> finalWordList = wordList;
        if (title.equals("我的收藏"))
            mListView.setOnItemLongClickListener(
                    new AdapterView.OnItemLongClickListener() {
                        @Override
                        public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {


                            final MaterialDialog m = new MaterialDialog(WordListActivity.this);
                            m.setTitle("删除 " + finalWordList.get(position - 1).getWord() + "?");
                            m.setMessage("点击确定将删除此单词");

                            m.setNegativeButton(
                                    "取消",
                                    new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            m.dismiss();
                                        }
                                    }
                            );
                            m.setPositiveButton(
                                    "确定",
                                    new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            ToastUtils.showShort("已删除");
                                            DataSupport.delete(FavoriteWord.class, finalWordList.get(position - 1).getId());
                                            wordList.remove(position - 1);
                                            ma.notifyDataSetChanged();
                                            m.dismiss();
                                        }
                                    }
                            );
                            m.show();
                            return false;
                        }
                    }
            );
        mListView.setOnScrollListener(
                new AbsListView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(AbsListView view, int scrollState) {
                    }

                    @Override
                    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                        int scrollY = getScrollY();
                        //sticky actionbar
                        mHeader.setTranslationY(Math.max(-scrollY, mMinHeaderTranslation));
                        //header_logo --> actionbar icon
                        float ratio = clamp(mHeader.getTranslationY() / mMinHeaderTranslation, 0.0f, 1.0f);
                        interpolate(mHeaderLogo, getActionBarIconView(), mSmoothInterpolator.getInterpolation(ratio));
                        //actionbar title alpha
                        //getActionBarTitleView().setAlpha(clamp(5.0F * ratio - 4.0F, 0.0F, 1.0F));
                        //---------------------------------
                        //better way thanks to @cyrilmottier
                        setTitleAlpha(clamp(5.0F * ratio - 4.0F, 0.0F, 1.0F));
                    }
                }
        );
    }

    private void setTitleAlpha(float alpha) {
        mAlphaForegroundColorSpan.setAlpha(alpha);
        mSpannableString.setSpan(mAlphaForegroundColorSpan, 0, mSpannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        //getActionBar().setTitle(mSpannableString);
        String title = getIntent().getStringExtra("title");
        getActionBar().setTitle(title);
        //TextView titleTextView = (TextView) findViewById(R.id.tv_title);
        //titleTextView.setText(title);
    }

    public static float clamp(float value, float max, float min) {
        return Math.max(Math.min(value, min), max);
    }

    private void interpolate(View view1, View view2, float interpolation) {
        getOnScreenRect(mRect1, view1);
        getOnScreenRect(mRect2, view2);

        float scaleX = 1.0F + interpolation * (mRect2.width() / mRect1.width() - 1.0F);
        float scaleY = 1.0F + interpolation * (mRect2.height() / mRect1.height() - 1.0F);
        float translationX = 0.5F * (interpolation * (mRect2.left + mRect2.right - mRect1.left - mRect1.right));
        float translationY = 0.5F * (interpolation * (mRect2.top + mRect2.bottom - mRect1.top - mRect1.bottom));

        view1.setTranslationX(translationX);
        view1.setTranslationY(translationY - mHeader.getTranslationY());
        view1.setScaleX(scaleX);
        view1.setScaleY(scaleY);
    }

    private RectF getOnScreenRect(RectF rect, View view) {
        rect.set(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
        return rect;
    }

    public int getScrollY() {
        View c = mListView.getChildAt(0);
        if (c == null) {
            return 0;
        }

        int firstVisiblePosition = mListView.getFirstVisiblePosition();
        int top = c.getTop();

        int headerHeight = 0;
        if (firstVisiblePosition >= 1) {
            headerHeight = mPlaceHolderView.getHeight();
        }

        return -top + firstVisiblePosition * c.getHeight() + headerHeight;
    }

    private void setupActionBar() {
        ActionBar actionBar = getActionBar();

        actionBar.setIcon(R.drawable.ic_transparent);

        //getActionBarTitleView().setAlpha(0f);
    }

    private ImageView getActionBarIconView() {
        return (ImageView) findViewById(android.R.id.home);
    }

    /*private TextView getActionBarTitleView() {
        int id = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
        return (TextView) findViewById(id);
    }*/

    public int getActionBarHeight() {
        if (mActionBarHeight != 0) {
            return mActionBarHeight;
        }
        getTheme().resolveAttribute(android.R.attr.actionBarSize, mTypedValue, true);
        mActionBarHeight = TypedValue.complexToDimensionPixelSize(mTypedValue.data, getResources().getDisplayMetrics());
        return mActionBarHeight;
    }

    private class MyAdapter extends BaseAdapter {

        Context            mContext;
        List<FavoriteWord> mList;

        public MyAdapter(Context context, List<FavoriteWord> wordList) {
            this.mContext = context;
            this.mList = wordList;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public FavoriteWord getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            FavoriteWord fw = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_wordlist, null);
                TextView tv = (TextView) convertView;
                tv.setText((fw.getWord() + " " + fw.getSpeech() + " " + fw.getExplanation()));
                convertView.setTag(tv);
            } else {
                TextView tv = (TextView) convertView.getTag();
                tv.setText((fw.getWord() + " " + fw.getSpeech() + " " + fw.getExplanation()));
            }
            return convertView;
        }
    }
}
