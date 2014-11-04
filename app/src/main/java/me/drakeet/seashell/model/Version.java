package me.drakeet.seashell.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;

import java.sql.SQLOutput;

/**
 * Created by drakeet on 11/3/14.
 */
public class Version implements Parcelable {
    String version;
    String describe;

    public Version() {}

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public String toGson() {
        return new Gson().toJson(this);
    }

    @Override
    public String toString() {
        return toGson();
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.version);
        dest.writeString(this.describe);
    }

    private Version(Parcel in) {
        this.version = in.readString();
        this.describe = in.readString();
    }

    public static final Parcelable.Creator<Version> CREATOR = new Parcelable.Creator<Version>() {
        public Version createFromParcel(Parcel source) {return new Version(source);}

        public Version[] newArray(int size) {return new Version[size];}
    };

    public static void main(String[] a) {
        Version version1 = new Version();
        version1.version = "1.4.1";
        version1.describe = "1. 『刷新』变成『换一个』\n"
                + "2. 新增 词库选择";
        System.out.println(new Gson().toJson(version1, Version.class));
    }
}
