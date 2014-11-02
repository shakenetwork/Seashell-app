package me.drakeet.seashell.model;

import com.google.gson.Gson;

import org.litepal.crud.DataSupport;

/**
 * Changed by drakeet on 9/18/2014.
 */
public class Word extends DataSupport {

    private int    id;
    private String word;
    private String phonetic;
    private String speech;
    private String explanation;
    private String example;

    public long getId() {
        return this.getBaseObjId();
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getPhonetic() {
        return phonetic;
    }

    public void setPhonetic(String phonetic) {
        this.phonetic = phonetic;
    }

    public String getSpeech() {
        return speech;
    }

    public void setSpeech(String speech) {
        this.speech = speech;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }

    public String toGson() {
        return new Gson().toJson(this);
    }

    @Override
    public String toString() {
        return toGson();
    }

    public static void main(String[] d) {
        Word word1 = new Gson().fromJson("{\"word\":\"essence\",\"phonetic\":\"[ˈesns] \",\"speech\":\"n. \",\"explanation\":\"本质，实质；精髓\",\"example\":\"eg. The essence of flat, is super curved.\\n平坦的本质，是极致的曲面。 \\n\\n \"}", Word.class);
        word1.setId(1);
        System.out.printf("-->" + new Word().toGson());
    }
}
