package me.drakeet.seashell;

import android.app.Application;
import android.content.Context;

import org.litepal.LitePalApplication;

/**
 * Created by drakeet on 9/27/14.
 */
public class App extends LitePalApplication {

    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();
    }

    public static Context getContext() {
        return sContext;
    }
}
