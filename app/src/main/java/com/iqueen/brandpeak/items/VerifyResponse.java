package com.sgdigitalposter.app.items;

import com.google.gson.annotations.SerializedName;

public class VerifyResponse {

    @SerializedName("response")
    public String response;

    public VerifyResponse(String response) {
        this.response = response;
    }

    @Override
    public String toString() {
        return "VerifyResponse{" +
                "response='" + response + '\'' +
                '}';
    }
}
