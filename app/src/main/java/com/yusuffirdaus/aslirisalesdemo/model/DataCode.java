package com.yusuffirdaus.aslirisalesdemo.model;


import android.os.Parcel;
import android.os.Parcelable;


public class DataCode implements Parcelable {
    private String code,name;

    public DataCode(String code, String name) {
        this.code = code;
        this.name = name;


    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.code);

    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DataCode(){
    }

    protected DataCode(Parcel in ){
        this.code = in.readString();
        this.name = in.readString();
    }

    public static final Creator<DataCode> CREATOR = new Creator<DataCode>(){

        @Override
        public DataCode createFromParcel(Parcel source) {
            return new DataCode(source);
        }

        @Override
        public DataCode[] newArray(int size) {
            return new DataCode[size];
        }
    };
}