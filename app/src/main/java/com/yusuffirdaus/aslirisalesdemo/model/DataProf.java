package com.yusuffirdaus.aslirisalesdemo.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DataProf {
    @Expose
    @SerializedName("selfie_photo")
    private double selfie_photo;
    @Expose
    @SerializedName("address")
    private String address;
    @Expose
    @SerializedName("birthplace")
    private boolean birthplace;
    @Expose
    @SerializedName("birthdate")
    private boolean birthdate;
    @Expose
    @SerializedName("name")
    private boolean name;

    public double getSelfie_photo() {
        return selfie_photo;
    }

    public void setSelfie_photo(double selfie_photo) {
        this.selfie_photo = selfie_photo;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean getBirthplace() {
        return birthplace;
    }

    public void setBirthplace(boolean birthplace) {
        this.birthplace = birthplace;
    }

    public boolean getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(boolean birthdate) {
        this.birthdate = birthdate;
    }

    public boolean getName() {
        return name;
    }

    public void setName(boolean name) {
        this.name = name;
    }
}
