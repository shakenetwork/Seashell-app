package me.drakeet.seashell.ui;

import android.app.ActionBar;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuItem;

import me.drakeet.seashell.R;
import me.drakeet.seashell.utils.MySharedpreference;
import me.drakeet.seashell.utils.TaskUtils;
import me.drakeet.seashell.utils.ToastUtils;

public class SettingActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener,
        Preference.OnPreferenceClickListener {

    protected ActionBar mActionBar;
    private String mHandSwitch;
    private CheckBoxPreference mHandSwitchCheckPref;
    private MySharedpreference mSharedpreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        mSharedpreference = new MySharedpreference(this);
        initActionBar();
        mHandSwitch = getResources().getString(R.string.hand_switch);
        mHandSwitchCheckPref = (CheckBoxPreference) findPreference(mHandSwitch);
        mHandSwitchCheckPref.setOnPreferenceChangeListener(this);
        Boolean hand = mSharedpreference.getBoolean("hand_switch");
        mHandSwitchCheckPref.setChecked(hand);
        //mHandSwitchCheckPref.setOnPreferenceClickListener(this);
    }

    private void initActionBar() {
        mActionBar = getActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);// 给左上角图标的左边加上一个返回的图标"《" 。对应ActionBar.DISPLAY_HOME_AS_UP
        mActionBar.setDisplayShowHomeEnabled(true); //使左上角图标可点击，对应id为android.R.id.home，对应ActionBar.DISPLAY_SHOW_HOME false 则图标无法点击
        mActionBar.setHomeButtonEnabled(true); // false 就无法点击
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.setting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void toogleHandSwitch() {

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference.getKey().equals(mHandSwitch)) {
            mSharedpreference.saveBoolean("hand_switch", !mHandSwitchCheckPref.isChecked());
        } else {
            return false;
        }
        return true;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals(mHandSwitch)) {

        } else {
            return false;
        }
        return true;
    }
}
