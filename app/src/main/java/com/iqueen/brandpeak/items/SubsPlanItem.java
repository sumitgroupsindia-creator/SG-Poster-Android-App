package com.sgdigitalposter.app.items;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.TypeConverters;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

@Entity(tableName = "subs_plan", primaryKeys = "id")
public class SubsPlanItem implements Serializable {

    @NonNull
    @SerializedName("id")
    public String id;
    @SerializedName("planName")
    public String planName;
    public String planImage;
    @SerializedName("planPrice")
    public String planPrice;
    @SerializedName("discountPrice")
    public String planDiscount;
    @SerializedName("duration")
    public String planDuration;
    @SerializedName("googleProductEnable")
    public String googleProductEnable;
    @SerializedName("googleProductId")
    public String googleProductId;

    @TypeConverters
    @SerializedName("planDetail")
    public List<String> pointItemList;

    public String gPrice;

    public SubsPlanItem(@NonNull String id, String planName, String planImage, String planPrice, String planDiscount, String planDuration, String googleProductEnable, String googleProductId, List<String> pointItemList, String gPrice) {
        this.id = id;
        this.planName = planName;
        this.planImage = planImage;
        this.planPrice = planPrice;
        this.planDiscount = planDiscount;
        this.planDuration = planDuration;
        this.googleProductEnable = googleProductEnable;
        this.googleProductId = googleProductId;
        this.pointItemList = pointItemList;
        this.gPrice = gPrice;
    }
}
