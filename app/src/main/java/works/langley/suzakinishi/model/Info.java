package works.langley.suzakinishi.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Info implements Parcelable {

    private String mTitle;
    private String mAuthor;
    private String mUrl;

    public Info(String title, String author, String url) {
        mTitle = title;
        mAuthor = author;
        mUrl = url;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public void setAuthor(String author) {
        mAuthor = author;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mTitle);
        dest.writeString(this.mAuthor);
        dest.writeString(this.mUrl);
    }

    public Info() {
    }

    protected Info(Parcel in) {
        this.mTitle = in.readString();
        this.mAuthor = in.readString();
        this.mUrl = in.readString();
    }

    public static final Creator<Info> CREATOR = new Creator<Info>() {
        public Info createFromParcel(Parcel source) {
            return new Info(source);
        }

        public Info[] newArray(int size) {
            return new Info[size];
        }
    };
}