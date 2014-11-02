package me.drakeet.seashell.utils;


import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

import android.content.Context;
import android.content.SharedPreferences;

import me.drakeet.seashell.R;
import me.drakeet.seashell.model.Word;

public class MySharedpreference {

    private Context           context;
    private SharedPreferences sharedPreferences;

    public MySharedpreference(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(
                "userinfo", Context.MODE_PRIVATE
        );
    }

    public int getCurrentWordId() {
        return sharedPreferences.getInt(context.getString(R.string.current_word_id), 6);
    }

    public boolean updateWordId() {
        int currentWordId = getCurrentWordId();
        currentWordId++;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(context.getString(R.string.current_word_id), currentWordId);
        return editor.commit();
    }

    public boolean setCurrentWordId(int currentWordId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(context.getString(R.string.current_word_id), currentWordId);
        return editor.commit();
    }

    public boolean saveString(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        return editor.commit();
    }

    public boolean saveInt(String key, int value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        return editor.commit();
    }

    public boolean saveBoolean(String key, Boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        return editor.commit();
    }

    public String getString(String key) {
        String s = sharedPreferences.getString(key, null);
        return s;
    }

    public int getInt(String key) {
        int i = sharedPreferences.getInt(key, 1);
        return i;
    }

    public Boolean getBoolean(String key) {
        Boolean b = sharedPreferences.getBoolean(key, false);
        return b;
    }

    /**
     * Set the honor, it is the number of notify count.
     *
     * @param honor count
     *
     * @return true is successful
     */
    public boolean saveHonor(int honor) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("honor", honor);
        return editor.commit();
    }

    public boolean saveYesterdayJson(String string) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("yesterday_json", string);
        return editor.commit();
    }

    public boolean saveTodayJson(String string) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("today_json", string);
        return editor.commit();
    }

    public Map<String, Object> getInfo() {
        Map<String, Object> map = new HashMap<String, Object>();
        int honor = sharedPreferences.getInt("honor", 0);
        boolean adStatus = sharedPreferences.getBoolean("ad_status", true);
        map.put("honor", honor);
        map.put("ad_status", adStatus);
        return map;
    }

    public Map<String, String> getWordJson() {

        Map<String, String> map = new HashMap<String, String>();
        //init
        Word word = new Word();
        word.setWord("seashell");
        word.setPhonetic("[ˈsi:ʃel]");
        word.setSpeech("n.");
        word.setExplanation("海中软体动物的壳，贝壳。");
        word.setExample("eg. With your ear to a seashell.");

        String yesterdayJson = sharedPreferences.getString("yesterday_json", new Gson().toJson(word));
        String todayJson = sharedPreferences.getString("today_json", new Gson().toJson(word));
        map.put("yesterday_json", yesterdayJson);
        map.put("today_json", todayJson);
        return map;
    }
}
