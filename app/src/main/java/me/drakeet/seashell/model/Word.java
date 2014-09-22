package me.drakeet.seashell.model;

import org.litepal.crud.DataSupport;

/**
 * Changed by drakeet on 9/18/2014.
 */
public class Word extends DataSupport {

    private String word;
    private String phonetic;
    private String speech;
    private String explanation;
    private String example;

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

    @Override
    public String toString() {
        return "Word{" +
                "word='" + word + '\'' +
                ", phonetic='" + phonetic + '\'' +
                ", speech='" + speech + '\'' +
                ", explanation='" + explanation + '\'' +
                ", example='" + example + '\'' +
                '}';
    }
}
