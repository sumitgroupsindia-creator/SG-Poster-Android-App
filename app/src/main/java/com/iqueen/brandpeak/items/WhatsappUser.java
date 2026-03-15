package com.sgdigitalposter.app.items;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class WhatsappUser {

    @SerializedName("waNumber")
    @Expose
    public String waNumber;
    @SerializedName("waName")
    @Expose
    public String waName;
    @SerializedName("timestamp")
    @Expose
    public String timestamp;

    public WhatsappUser(String waNumber, String waName, String timestamp) {
        this.waNumber = waNumber;
        this.waName = waName;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "WhatsappUser{" +
                "waNumber='" + waNumber + '\'' +
                ", waName='" + waName + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
