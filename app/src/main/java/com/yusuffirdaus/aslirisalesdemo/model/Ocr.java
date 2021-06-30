package com.yusuffirdaus.aslirisalesdemo.model;

import java.util.Map;

public  class Ocr {


    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("data")
    private Map data;
    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("errors")
    private Errors errors;
    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("status")
    private int status;
    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("timestamp")
    private int timestamp;

    public Map getData() {
        return data;
    }

    public void setData(Map data) {
        this.data = data;
    }

    public Errors getErrors() {
        return errors;
    }

    public void setErrors(Errors errors) {
        this.errors = errors;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }
}
