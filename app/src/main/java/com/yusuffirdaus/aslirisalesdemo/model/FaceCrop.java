package com.yusuffirdaus.aslirisalesdemo.model;

public class FaceCrop {

    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("errors")
    private Errors errors;
    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("status")
    private int status;
    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("timestamp")
    private int timestamp;
    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("data")
    private DataCrop data;

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

    public DataCrop getData() {
        return data;
    }

    public void setData(DataCrop data) {
        this.data = data;
    }
}
