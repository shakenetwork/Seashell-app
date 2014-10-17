package me.drakeet.seashell.ui;

import android.app.ActionBar;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.google.gson.Gson;

import java.util.Map;

import me.drakeet.seashell.R;
import me.drakeet.seashell.model.Word;
import me.drakeet.seashell.utils.MySharedpreference;
import me.drakeet.seashell.utils.NotificationUtils;
import me.drakeet.seashell.utils.ToastUtils;

public class SettingActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener,
        Preference.OnPreferenceClickListener {

    protected ActionBar          mActionBar;
    private   String             mHandSwitch;
    private   String             mPhonetickey;
    private   CheckBoxPreference mHandSwitchCheckPref;
    private   CheckBoxPreference mPhoneticSwitchCheckPref;
    private   MySharedpreference mSharedpreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        mSharedpreference = new MySharedpreference(this);
        initActionBar();
        setTitle("设置");
        mHandSwitch = getResources().getString(R.string.hand_switch);
        mPhonetickey = getResources().getString(R.string.notify_with_phonetic);
        mHandSwitchCheckPref = (CheckBoxPreference) findPreference(mHandSwitch);
        mPhoneticSwitchCheckPref = (CheckBoxPreference) findPreference(mPhonetickey);

        mHandSwitchCheckPref.setOnPreferenceChangeListener(this);
        Boolean hand = mSharedpreference.getBoolean("hand_switch");
        mHandSwitchCheckPref.setChecked(hand);

        mPhoneticSwitchCheckPref.setOnPreferenceChangeListener(this);
        Boolean isShowPhonetic = mSharedpreference.getBoolean(mPhonetickey);
        mPhoneticSwitchCheckPref.setChecked(isShowPhonetic);
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
        getMenuInflater().inflate(R.menu.setting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_back) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void toogleHandSwitch() {

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference.getKey().equals(mHandSwitch)) {
            mSharedpreference.saveBoolean("hand_switch", !mHandSwitchCheckPref.isChecked());
        } else if (preference.getKey().equals(mPhonetickey)) {
            mSharedpreference.saveBoolean(mPhonetickey, !mPhoneticSwitchCheckPref.isChecked());
            if (!mPhoneticSwitchCheckPref.isChecked()) {

                ToastUtils.showLong("已开启通知栏显示音标设置！\n但为了保持简洁性，建议是关闭^ ^");
            }
            Map<String, String> map = mSharedpreference.getWordJson();
            String jsonString = map.get("today_json");
            Word word = new Gson().fromJson(jsonString, Word.class);
            NotificationUtils.showWordInNotificationBar(this, word);
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
