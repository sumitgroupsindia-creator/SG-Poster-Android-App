package com.sgdigitalposter.app.items;

import androidx.annotation.NonNull;
import androidx.room.Embedded;
import androidx.room.Entity;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "app_info", primaryKeys = "id")
public class AppInfo {

    @SerializedName("id")
    public int id;

    @SerializedName("appTitle")
    public String appTitle;

    @SerializedName("author")
    public String author;

    @SerializedName("description")
    public String description;

    @SerializedName("contact")
    public String contact;

    @SerializedName("email")
    public String email;

    @SerializedName("website")
    public String website;

    @SerializedName("developedBy")
    public String developedBy;

    @SerializedName("appLogo")
    public String appLogo;

    @SerializedName("currency")
    public String currency;

    @SerializedName("privacyPolicy")
    public String privacyPolicy;

    @SerializedName("refundPolicy")
    public String refundPolicy;

    @SerializedName("termsCondition")
    public String termsCondition;

    @Embedded(prefix = "version_")
    @SerializedName("appUpdate")
    public AppVersion appVersion;

    public String privacyPolicy_link;

    @SerializedName("adsEnable")
    public String adsEnabled;

    @SerializedName("adsNetwork")
    public String ad_network;

    @SerializedName("publisherId")
    public String publisher_id;

    @SerializedName("bannerAdsId")
    public String banner_ad_id;

    @SerializedName("appOpenAdsId")
    public String open_ad_id;

    @SerializedName("interstitialAdsId")
    public String interstitial_ad_id;

    @SerializedName("interstitialAdsClick")
    public String interstitial_ad_click;

    @SerializedName("nativeAdsId")
    public String native_ad_id;

    @SerializedName("bannerAdsEnable")
    public String banner_ad;

    @SerializedName("interstitialAdsEnable")
    public String interstitial_ad;

    @SerializedName("nativeAdsEnable")
    public String native_ad;

    @SerializedName("appOpensAdsEnable")
    public String open_ad;

    @Embedded(prefix = "offer_")
    @SerializedName("offer")
    public OfferItem offerItem;

    @SerializedName("productEnable")
    public String productEnable = "0";

    @SerializedName("razorpayKeyId")
    public String razorpayKeyId;

    @SerializedName("razorpayKeySecret")
    public String razorpayKeySecret;

    @SerializedName("stripePublishableKey")
    public String stripePublishableKey;

    @SerializedName("stripeSecretKey")
    public String stripeSecretKey;

    @SerializedName("paytmMerchantId")
    public String paytmMerchantId;

    @SerializedName("paytmMerchantKey")
    public String paytmMerchantKey;

    @SerializedName("cashfreeKeyId")
    public String cashfreeKeyId = "";

    @SerializedName("cashfreeKeySecret")
    public String cashfreeKeySecret = "";

    @SerializedName("razorpayEnable")
    public String razorpayEnable = "";

    @SerializedName("cashfreeEnable")
    public String cashfreeEnable = "";

    @SerializedName("phonepeEnable")
    public String phonepeEnable = "";

    @SerializedName("phonepeMerchantId")
    public String phonepeMerchantId = "";

    @SerializedName("phonepeSaltKey")
    public String phonepeSaltKey = "";

    @SerializedName("offlineEnable")
    public String offlineEnable = "";

    @SerializedName("stripeEnable")
    public String stripeEnable = "";

    @SerializedName("paytmEnable")
    public String paytmEnable = "";

    @SerializedName("offlinePaymentDetails")
    public String offlinePaymentDetails = "";

    @SerializedName("referralSystemEnable")
    public String referralSystemEnable = "0";

    @SerializedName("registerPoint")
    public String registerPoint = "";

    @SerializedName("subscriptionPoint")
    public String subscriptionPoint = "";

    @SerializedName("withdrawalLimit")
    public String withdrawalLimit = "";

    @SerializedName("whatsappContactEnable")
    public String whatsappContactEnable;

    @SerializedName("whatsappNumber")
    public String whatsappNumber;

    @SerializedName("digitalOcean")
    public String digitalOcean;

    @SerializedName("digitalOceanEndpoint")
    public String digitalOceanEndpoint;

    @SerializedName("cashfreeType")
    public String cashfreeType;

    @SerializedName("rewardedAdsEnable")
    public String rewardedAdsEnable;

    @SerializedName("rewardedAdsId")
    public String rewardedAdsId;
    @SerializedName("whatsappAuthEnable")
    public String whatsappAuthEnable;
    @SerializedName("dailyLimitRewarded")
    public String dailyLimitRewarded;

    public AppInfo(int id, String appTitle, String author, String description,
                   String contact, String email, String website, String developedBy,
                   String appLogo, String currency, String privacyPolicy, String refundPolicy,
                   String termsCondition, AppVersion appVersion, String privacyPolicy_link, String adsEnabled,
                   String ad_network, String publisher_id, String banner_ad_id, String open_ad_id, String interstitial_ad_id,
                   String interstitial_ad_click, String native_ad_id, String banner_ad, String interstitial_ad,
                   String native_ad, String open_ad, OfferItem offerItem, String productEnable,
                   String razorpayKeyId, String razorpayKeySecret, String stripePublishableKey, String stripeSecretKey,
                   String paytmMerchantId, String paytmMerchantKey, String cashfreeKeyId, String cashfreeKeySecret,
                   String razorpayEnable, String cashfreeEnable, String offlineEnable, String stripeEnable,
                   String paytmEnable,
                   String phonepeEnable,
                   String phonepeMerchantId,
                   String phonepeSaltKey,
                   String offlinePaymentDetails, String referralSystemEnable, String registerPoint, String subscriptionPoint, String withdrawalLimit, String whatsappContactEnable, String whatsappNumber, String digitalOcean, String digitalOceanEndpoint, String cashfreeType, String rewardedAdsEnable, String rewardedAdsId, String whatsappAuthEnable, String dailyLimitRewarded) {
        this.id = id;
        this.appTitle = appTitle;
        this.author = author;
        this.description = description;
        this.contact = contact;
        this.email = email;
        this.website = website;
        this.developedBy = developedBy;
        this.appLogo = appLogo;
        this.currency = currency;
        this.privacyPolicy = privacyPolicy;
        this.refundPolicy = refundPolicy;
        this.termsCondition = termsCondition;
        this.appVersion = appVersion;
        this.privacyPolicy_link = privacyPolicy_link;
        this.adsEnabled = adsEnabled;
        this.ad_network = ad_network;
        this.publisher_id = publisher_id;
        this.banner_ad_id = banner_ad_id;
        this.open_ad_id = open_ad_id;
        this.interstitial_ad_id = interstitial_ad_id;
        this.interstitial_ad_click = interstitial_ad_click;
        this.native_ad_id = native_ad_id;
        this.banner_ad = banner_ad;
        this.interstitial_ad = interstitial_ad;
        this.native_ad = native_ad;
        this.open_ad = open_ad;
        this.offerItem = offerItem;
        this.productEnable = productEnable;
        this.razorpayKeyId = razorpayKeyId;
        this.razorpayKeySecret = razorpayKeySecret;
        this.stripePublishableKey = stripePublishableKey;
        this.stripeSecretKey = stripeSecretKey;
        this.paytmMerchantId = paytmMerchantId;
        this.paytmMerchantKey = paytmMerchantKey;
        this.cashfreeKeyId = cashfreeKeyId;
        this.cashfreeKeySecret = cashfreeKeySecret;
        this.razorpayEnable = razorpayEnable;
        this.cashfreeEnable = cashfreeEnable;
        this.offlineEnable = offlineEnable;
        this.stripeEnable = stripeEnable;
        this.paytmEnable = paytmEnable;
        this.phonepeEnable = phonepeEnable;
        this.phonepeMerchantId = phonepeMerchantId;
        this.phonepeSaltKey = phonepeSaltKey;

        this.offlinePaymentDetails = offlinePaymentDetails;
        this.referralSystemEnable = referralSystemEnable;
        this.registerPoint = registerPoint;
        this.subscriptionPoint = subscriptionPoint;
        this.withdrawalLimit = withdrawalLimit;
        this.whatsappContactEnable = whatsappContactEnable;
        this.whatsappNumber = whatsappNumber;
        this.digitalOcean = digitalOcean;
        this.digitalOceanEndpoint = digitalOceanEndpoint;
        this.cashfreeType = cashfreeType;
        this.rewardedAdsEnable = rewardedAdsEnable;
        this.rewardedAdsId = rewardedAdsId;
        this.whatsappAuthEnable = whatsappAuthEnable;
        this.dailyLimitRewarded = dailyLimitRewarded;
    }

    @Override
    public String toString() {
        return "AppInfo{" +
                "id=" + id +
                ", appTitle='" + appTitle + '\'' +
                ", author='" + author + '\'' +
                ", description='" + description + '\'' +
                ", contact='" + contact + '\'' +
                ", email='" + email + '\'' +
                ", website='" + website + '\'' +
                ", developedBy='" + developedBy + '\'' +
                ", appLogo='" + appLogo + '\'' +
                ", currency='" + currency + '\'' +
                ", privacyPolicy='" + privacyPolicy + '\'' +
                ", refundPolicy='" + refundPolicy + '\'' +
                ", termsCondition='" + termsCondition + '\'' +
                ", appVersion=" + appVersion +
                ", privacyPolicy_link='" + privacyPolicy_link + '\'' +
                ", adsEnabled='" + adsEnabled + '\'' +
                ", ad_network='" + ad_network + '\'' +
                ", publisher_id='" + publisher_id + '\'' +
                ", banner_ad_id='" + banner_ad_id + '\'' +
                ", open_ad_id='" + open_ad_id + '\'' +
                ", interstitial_ad_id='" + interstitial_ad_id + '\'' +
                ", interstitial_ad_click='" + interstitial_ad_click + '\'' +
                ", native_ad_id='" + native_ad_id + '\'' +
                ", banner_ad='" + banner_ad + '\'' +
                ", interstitial_ad='" + interstitial_ad + '\'' +
                ", native_ad='" + native_ad + '\'' +
                ", open_ad='" + open_ad + '\'' +
                ", offerItem=" + offerItem +
                ", productEnable='" + productEnable + '\'' +
                ", razorpayKeyId='" + razorpayKeyId + '\'' +
                ", razorpayKeySecret='" + razorpayKeySecret + '\'' +
                ", stripePublishableKey='" + stripePublishableKey + '\'' +
                ", stripeSecretKey='" + stripeSecretKey + '\'' +
                ", paytmMerchantId='" + paytmMerchantId + '\'' +
                ", paytmMerchantKey='" + paytmMerchantKey + '\'' +
                ", cashfreeKeyId='" + cashfreeKeyId + '\'' +
                ", cashfreeKeySecret='" + cashfreeKeySecret + '\'' +
                ", razorpayEnable='" + razorpayEnable + '\'' +
                ", cashfreeEnable='" + cashfreeEnable + '\'' +
                ", offlineEnable='" + offlineEnable + '\'' +
                ", stripeEnable='" + stripeEnable + '\'' +
                ", paytmEnable='" + paytmEnable + '\'' +
                ", phonepeEnable='" + phonepeEnable + '\'' +
                ", phonepeMerchantId='" + phonepeMerchantId + '\'' +
                ", phonepeSaltKey='" + phonepeSaltKey + '\'' +
                ", offlinePaymentDetails='" + offlinePaymentDetails + '\'' +
                ", referralSystemEnable='" + referralSystemEnable + '\'' +
                ", registerPoint='" + registerPoint + '\'' +
                ", subscriptionPoint='" + subscriptionPoint + '\'' +
                ", withdrawalLimit='" + withdrawalLimit + '\'' +
                ", whatsappContactEnable='" + whatsappContactEnable + '\'' +
                ", whatsappNumber='" + whatsappNumber + '\'' +
                ", digitalOcean='" + digitalOcean + '\'' +
                ", digitalOceanEndpoint='" + digitalOceanEndpoint + '\'' +
                ", cashfreeType='" + cashfreeType + '\'' +
                ", rewardedAdsEnable='" + rewardedAdsEnable + '\'' +
                ", rewardedAdsId='" + rewardedAdsId + '\'' +
                ", whatsappAuthEnable='" + whatsappAuthEnable + '\'' +
                ", dailyLimitRewarded='" + dailyLimitRewarded + '\'' +
                '}';
    }
}
