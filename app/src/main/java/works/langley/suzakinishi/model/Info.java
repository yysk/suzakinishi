package works.langley.suzakinishi.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Info implements Parcelable {

    public String title;
    public String author;
    public String url;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.title);
        dest.writeString(this.author);
        dest.writeString(this.url);
    }

    public Info() {
    }

    protected Info(Parcel in) {
        this.title = in.readString();
        this.author = in.readString();
        this.url = in.readString();
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