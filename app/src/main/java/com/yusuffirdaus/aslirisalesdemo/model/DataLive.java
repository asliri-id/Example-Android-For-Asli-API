package com.yusuffirdaus.aslirisalesdemo.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DataLive {
    @Expose
    @SerializedName("passed")
    private String passed;

    public String getPassed() {
        return passed;
    }

    public void setPassed(String passed) {
        this.passed = passed;
    }
}
