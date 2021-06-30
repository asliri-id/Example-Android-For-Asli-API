package com.yusuffirdaus.aslirisalesdemo.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
public class Liveness {


    @Expose
    @SerializedName("errors")
    private Errors errors;
    @Expose
    @SerializedName("status")
    private int status;
    @Expose
    @SerializedName("timestamp")
    private int timestamp;
    @Expose
    @SerializedName("data")
    private DataLive data;

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

    public DataLive getData() {
        return data;
    }

    public void setData(DataLive data) {
        this.data = data;
    }
}
