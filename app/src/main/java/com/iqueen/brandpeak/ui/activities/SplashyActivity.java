package com.sgdigitalposter.app.ui.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.sgdigitalposter.app.Config;
import com.sgdigitalposter.app.R;
import com.sgdigitalposter.app.items.AppVersion;
import com.sgdigitalposter.app.ui.dialog.DialogMsg;
import com.sgdigitalposter.app.utils.Constant;
import com.sgdigitalposter.app.utils.PrefManager;
import com.sgdigitalposter.app.utils.Util;
import com.sgdigitalposter.app.viewmodel.UserViewModel;

public class SplashyActivity extends AppCompatActivity {

    PrefManager prefManager;
    UserViewModel userViewModel;
    DialogMsg dialogMsg;

    String id = "";
    String type = "";
    String imgUrl = "";
    String name = "";
    boolean video = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashy);

        prefManager = new PrefManager(this);
        dialogMsg = new DialogMsg(this, false);

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);


        initData();

        if (prefManager.getString(Constant.CHECKED_ITEM).equals("yes")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        try {

            if (Config.isFromNotifications) {
                id = prefManager.getString(Constant.PRF_ID);
                name = prefManager.getString(Constant.PRF_NAME);
                imgUrl = "";
                type = prefManager.getString(Constant.PRF_TYPE);
                video = prefManager.getBoolean(Constant.INTENT_VIDEO);

                Util.showLog("NOT: " + id + ", " + name + ", " + imgUrl + ", " + type + ", " + video);

            }
        } catch (Exception e) {
            Util.showErrorLog(e.getMessage(), e);
        }

        getData();
    }

    public void getData() {
        if (Config.IS_CONNECTED) {
            Util.showLog("Internet connected");

            FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
            FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                    .setMinimumFetchIntervalInSeconds(60)
                    .build();
            mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
            mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);
            mFirebaseRemoteConfig.fetchAndActivate()
                    .addOnCompleteListener(new OnCompleteListener<Boolean>() {
                        @Override
                        public void onComplete(@NonNull Task<Boolean> task) {
                            if (task.isSuccessful()) {
                                boolean updated = task.getResult();
                                Util.showLog("Config params updated: " + updated);

                            } else {

                            }
                            Util.showLog("API_KEY: " + mFirebaseRemoteConfig.getString("apiKey"));
//                            Log.e("SB", " Splash Screen : onComplete:" + mFirebaseRemoteConfig.getString("apiKey"));
//                            prefManager.setString(Constant.api_key, "123456");
                            prefManager.setString(Constant.api_key, mFirebaseRemoteConfig.getString("apiKey"));

                            Config.API_KEY = prefManager.getString(Constant.api_key);
                            prefManager.setString("FIRST", "TRUE");
                            userViewModel.setAppInfo("new");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Util.showErrorLog("Firebase", e);
                            gotoMainActivity();
                        }
                    });

        } else {
            Util.showLog("Internet is not connected");
            gotoMainActivity();
        }
    }

    private void initData() {

        userViewModel.getAppInfo().observe(this, listResource -> {

            if (listResource != null) {

                Util.showLog("Got Data: " + listResource.message + listResource.toString());

                switch (listResource.status) {
                    case LOADING:
                        // Loading State
                        // Data are from Local DB
                        break;
                    case SUCCESS:
                        // Success State
                        // Data are from Server

                        if (listResource.data != null) {
                            try {
                                Util.showLog("APP INFO ========= "+ listResource.data.privacyPolicy );
                                prefManager.setString(Constant.PRIVACY_POLICY, listResource.data.privacyPolicy);
                                prefManager.setString(Constant.TERM_CONDITION, listResource.data.termsCondition);
                                prefManager.setString(Constant.REFUND_POLICY, listResource.data.refundPolicy);

                                prefManager.setString(Constant.PRIVACY_POLICY_LINK, listResource.data.privacyPolicy);


                                prefManager.setBoolean(Constant.ADS_ENABLE, listResource.data.adsEnabled.equals(Config.ONE) ? true : false);

                                prefManager.setString(Constant.AD_NETWORK, listResource.data.ad_network);

                                prefManager.setString(Constant.PUBLISHER_ID, listResource.data.publisher_id);

                                prefManager.setString(Constant.BANNER_AD_ID, listResource.data.banner_ad_id);
                                prefManager.setBoolean(Constant.BANNER_AD_ENABLE, listResource.data.banner_ad.equals(Config.ONE) ? true : false);

                                prefManager.setString(Constant.INTERSTITIAL_AD_ID, listResource.data.interstitial_ad_id);
                                prefManager.setBoolean(Constant.INTERSTITIAL_AD_ENABLE, listResource.data.interstitial_ad.equals(Config.ONE) ? true : false);
                                prefManager.setInt(Constant.INTERSTITIAL_AD_CLICK, Integer.parseInt(listResource.data.interstitial_ad_click));

                                //Native Ads Id
                                prefManager.setString(Constant.NATIVE_AD_ID, listResource.data.native_ad_id);
                                prefManager.setBoolean(Constant.NATIVE_AD_ENABLE, listResource.data.native_ad.equals(Config.ONE) ? true : false);

                                prefManager.setString(Constant.OPEN_AD_ID, listResource.data.open_ad_id);
                                prefManager.setBoolean(Constant.OPEN_AD_ENABLE, listResource.data.open_ad.equals(Config.ONE) ? true : false);

                                prefManager.setBoolean(Constant.PRODUCT_ENABLE, listResource.data.productEnable.equals(Config.ONE) ? true : false);
                                prefManager.setBoolean(Constant.REFER_SYSTEM_ENABLE, listResource.data.referralSystemEnable.equals(Config.ONE) ? true : false);

                                prefManager.setString(Constant.RAZORPAY_KEY_ID, listResource.data.razorpayKeyId);
                                prefManager.setString(Constant.RAZORPAY_KEY_SECRET, listResource.data.razorpayKeySecret);

                                prefManager.setString(Constant.CASHFREE_KEY_ID, listResource.data.cashfreeKeyId);
                                prefManager.setString(Constant.CASHFREE_SECRET_ID, listResource.data.cashfreeKeySecret);

                                prefManager.setString(Constant.OFFLINE_DETAIL, listResource.data.offlinePaymentDetails);

                                prefManager.setString(Constant.PAYTM_ID, listResource.data.paytmMerchantId);
                                prefManager.setString(Constant.PAYTM_KEY, listResource.data.paytmMerchantKey);

                                prefManager.setString(Constant.STRIPE_KEY, listResource.data.stripePublishableKey);
                                prefManager.setString(Constant.STRIPE_SECRET_KEY, listResource.data.stripeSecretKey);

                                prefManager.setString(Constant.RazorPay, listResource.data.razorpayEnable);
                                prefManager.setString(Constant.CashFree, listResource.data.cashfreeEnable);
                                prefManager.setString(Constant.Offline, listResource.data.offlineEnable);
                                prefManager.setString(Constant.Paytm, listResource.data.paytmEnable);
                                prefManager.setString(Constant.Stripe, listResource.data.stripeEnable);

                                prefManager.setString(Constant.phonePe, listResource.data.phonepeEnable);
                                prefManager.setString(Constant.PHONE_PE_MERCHANT_ID, listResource.data.phonepeMerchantId);
                                prefManager.setString(Constant.PHONE_PE_SALT_KEY, listResource.data.phonepeSaltKey);


                                prefManager.setString(Constant.REFER_SUBS_POINT, listResource.data.subscriptionPoint);
                                prefManager.setString(Constant.REFER_LOGIN_POINT, listResource.data.registerPoint);
                                prefManager.setString(Constant.WITHDRAW_POINT, listResource.data.withdrawalLimit);

                                prefManager.setString(Constant.DIGITAL_ENABLE, listResource.data.digitalOcean);
                                prefManager.setString(Constant.DIGITAL_END_URL, listResource.data.digitalOceanEndpoint);
                                Log.e("SB", "initData listResource.data.digitalOcean := "+listResource.data.digitalOcean );
                                Log.e("SB", "initData listResource.data.digitalOceanEndpoint := "+listResource.data.digitalOceanEndpoint );

                                prefManager.setString(Constant.CURRENCY, listResource.data.currency);

                                prefManager.setString(Constant.CASHFREE_TYPE, listResource.data.cashfreeType);
                                prefManager.setBoolean(Constant.REWARD_AD_ENABLE, listResource.data.rewardedAdsEnable.equals(Config.ONE) ? true : false);
                                prefManager.setString(Constant.REWARD_AD_ID, listResource.data.rewardedAdsId);
                                prefManager.setString(Constant.REWARD_AD_LIMIT, listResource.data.dailyLimitRewarded);

                                prefManager.setBoolean(Constant.WHATSAPP_AUTH_ENABLE, listResource.data.whatsappAuthEnable.equals(Config.ONE) ? true : false);

                                if (prefManager.getBoolean(Constant.ADS_ENABLE)) {
                                    initializeAds();

                                    Util.TodayRewardAvl(this);

                                    if (prefManager.getInt(Constant.CURRENT_REWARD) >= Integer.valueOf(prefManager.getString(Constant.REWARD_AD_LIMIT))) {
                                        prefManager.setBoolean(Constant.AVL_REWARD, false);
                                    } else {
                                        prefManager.setBoolean(Constant.AVL_REWARD, true);
                                    }

                                }

                                checkVersionNo(listResource.data.appVersion);

                                Config.whatsAvailable = listResource.data.whatsappContactEnable.equals(Config.ONE) ? true : false;
                                Config.whatsappNumber = listResource.data.whatsappNumber;
                                Config.offerItem = listResource.data.offerItem;

                            } catch (NullPointerException ne) {
                                Util.showErrorLog("Null Pointer Exception.", ne);
                            } catch (Exception e) {
                                Util.showErrorLog("Error in getting notification flag data.", e);
                            }

                            userViewModel.setLoadingState(false);

                        }

                        break;
                    case ERROR:

                        // Error State
                        dialogMsg.showErrorDialog(getString(R.string.click_try_again), getString(R.string.try_again));
                        dialogMsg.show();

                        dialogMsg.okBtn.setOnClickListener(v -> {
                            dialogMsg.cancel();
                            getData();
                        });

                        userViewModel.setLoadingState(false);

                        break;
                    default:
                        // Default

                        userViewModel.setLoadingState(false);

                        break;
                }

            } else {

                // Init Object or Empty Data
                Util.showLog("Empty Data");

            }

        });

    }

    private void initializeAds() {
        switch (prefManager.getString(Constant.AD_NETWORK)) {
            case Constant.ADMOB:
                break;
            case Constant.UNITY:
                break;
            case Constant.FACEBOOK:
                break;
        }
    }

    private void checkVersionNo(AppVersion appVersion) {
        String version;
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            version=Config.APP_VERSION;
        }
        if (appVersion.updatePopupShow.equals(Config.ONE) && !appVersion.newAppVersionCode.equals(version)) {

            dialogMsg.showAppInfoDialog(getString(R.string.force_update__button_update), getString(R.string.app__cancel),
                    getString(R.string.force_update_true), appVersion.versionMessage);
            dialogMsg.show();

            if (appVersion.cancelOption.equals(Config.ZERO)) {
                dialogMsg.cancelBtn.setVisibility(View.GONE);
            }
            dialogMsg.cancelBtn.setOnClickListener(v -> {
                dialogMsg.cancel();
                gotoMainActivity();
            });

            dialogMsg.okBtn.setOnClickListener(v -> {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(appVersion.appLink)));
            });

        } else {
            gotoMainActivity();
        }

    }

    private void gotoMainActivity() {
        Intent intent;
        Util.showLog("NOTI_SS: " + Config.isFromNotifications);
        if (Config.isFromNotifications) {
            if (type.equals(Constant.FESTIVAL) || type.equals(Constant.CATEGORY) || type.equals(Constant.CUSTOM_FEATURE)) {
                intent = new Intent(SplashyActivity.this, DetailActivity.class);
                intent.putExtra(Constant.INTENT_TYPE, type);
                intent.putExtra(Constant.INTENT_FEST_ID, id);
                intent.putExtra(Constant.INTENT_FEST_NAME, name);
                intent.putExtra(Constant.INTENT_POST_IMAGE, "");
                intent.putExtra(Constant.INTENT_VIDEO, video);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            } else if (type.equals(Constant.EXTERNAL)) {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(prefManager.getString(Constant.PRF_LINK)));
            } else {
                intent = new Intent(SplashyActivity.this, SubsPlanActivity.class);
            }
            Config.isFromNotifications = false;
        }
        else {
            Util.showLog("USERID: " + prefManager.getString(Constant.USER_ID));
            if (!prefManager.getBoolean(Constant.IS_FIRST_TIME_LAUNCH)) {
                intent = new Intent(SplashyActivity.this, IntroActivity.class);
            } else {
                Constant.FOR_ADD_BUSINESS = false;
                intent = new Intent(SplashyActivity.this, MainActivity.class);
            }
        }
        startActivity(intent);
        finish();
    }
}