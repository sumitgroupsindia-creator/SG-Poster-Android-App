package com.sgdigitalposter.app.Ads;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.ump.ConsentInformation;
import com.sgdigitalposter.app.utils.Constant;
import com.sgdigitalposter.app.utils.Util;
import com.sgdigitalposter.app.MyApplication;

public class BannerAdManager {
    public static void showBannerAds(Context context, LinearLayout mAdViewLayout) {
        Util.showLog("SHOW_BANNER: " + MyApplication.prefManager().getString(Constant.AD_NETWORK));
        if (MyApplication.prefManager().getBoolean(Constant.BANNER_AD_ENABLE) && MyApplication.prefManager().getBoolean(Constant.ADS_ENABLE) && !Constant.IS_SUBSCRIBED) {
            switch (MyApplication.prefManager().getString(Constant.AD_NETWORK)) {
                case Constant.ADMOB:
                    AdView mAdView = new AdView(context);
                    mAdView.setAdSize(AdSize.BANNER);
                    mAdView.setAdUnitId(MyApplication.prefManager().getString(Constant.BANNER_AD_ID));
                    AdRequest.Builder builder = new AdRequest.Builder();
                    int request = GDPRChecker.getStatus();
                    if (request == ConsentInformation.ConsentStatus.NOT_REQUIRED) {
                        // load non Personalized ads
                        Bundle extras = new Bundle();
                        extras.putString("npa", "1");
                        builder.addNetworkExtrasBundle(AdMobAdapter.class, extras);
                    } // else do nothing , it will load PERSONALIZED ads
                    mAdView.loadAd(builder.build());
                    mAdViewLayout.removeAllViews();
                    mAdViewLayout.addView(mAdView);
                    mAdViewLayout.setGravity(Gravity.CENTER);
                    break;
                case Constant.FACEBOOK:

                    break;
                case Constant.UNITY:

                    break;
            }
        } else {
            mAdViewLayout.setVisibility(View.GONE);
        }
    }
}
