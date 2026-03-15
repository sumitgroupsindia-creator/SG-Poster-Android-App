package com.sgdigitalposter.app.items;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class WhatsappDetails {

    @SerializedName("ok")
    @Expose
    public Boolean ok;
    @SerializedName("user")
    @Expose
    public WhatsappUser user;

    public WhatsappDetails(Boolean ok, WhatsappUser user) {
        this.ok = ok;
        this.user = user;
    }

    @Override
    public String toString() {
        return "WhatsappDetails{" +
                "ok=" + ok +
                ", user=" + user +
                '}';
    }
}
