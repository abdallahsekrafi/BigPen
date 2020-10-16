package com.zwir.bigpen.data;

import android.os.Parcel;
import android.os.Parcelable;

public class BookPage implements Parcelable{
    private String pageUrl;
    private boolean lockState;
    private int pageSize;
    public BookPage() {
    }

    public BookPage( String pageUrl, boolean lockState) {
        this.pageUrl = pageUrl;
        this.lockState = lockState;
    }
    public String getPageUrl() {
        return pageUrl;
    }
    public void setPageUrl(String pageUrl) {
        this.pageUrl = pageUrl;
    }
    public boolean isLockState() {
        return lockState;
    }
    public void setLockState(boolean lockState) {
        this.lockState = lockState;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.pageUrl);
        dest.writeByte((byte) (lockState ? 1 : 0));
    }
    protected BookPage(Parcel in) {
        this.pageUrl = in.readString();
        this.lockState = in.readByte() != 0;
    }
    public static final Parcelable.Creator<BookPage> CREATOR = new Parcelable.Creator<BookPage>() {
        @Override
        public BookPage createFromParcel(Parcel source) {
            return new BookPage(source);
        }

        @Override
        public BookPage[] newArray(int size) {
            return new BookPage[size];
        }
    };

}
