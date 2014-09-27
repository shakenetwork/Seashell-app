package me.drakeet.seashell.ui;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.MenuItem;


/**
 * Created by drakeet on 8/17/2014.
 */
public class BaseActivity extends FragmentActivity {

    protected ActionBar mActionBar;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActionBar();
    }

    private void initActionBar() {
        mActionBar = getActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);             // 给左上角图标的左边加上一个返回的图标"《" 。对应ActionBar.DISPLAY_HOME_AS_UP
        mActionBar.setDisplayShowHomeEnabled(true);             //使左上角图标可点击，对应id为android.R.id.home，对应ActionBar.DISPLAY_SHOW_HOME false 则图标无法点击
        mActionBar.setHomeButtonEnabled(true);                  // false 就无法点击
    }

    public void setTitle(int resId) {
        mActionBar.setTitle(resId);
    }

    public void setTitle(CharSequence text) {
        mActionBar.setTitle(text);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}