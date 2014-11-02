package me.drakeet.seashell.ui;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import me.drakeet.seashell.BuildConfig;
import me.drakeet.seashell.R;

/**
 *
 * Created by drakeet on 9/14/14.
 */
public class AboutActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        //Display the current version number
        TextView versionNumber = (TextView) findViewById(R.id.versionNumber);
        versionNumber.setText("Version " + BuildConfig.VERSION_NAME);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.about, menu);
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
}
