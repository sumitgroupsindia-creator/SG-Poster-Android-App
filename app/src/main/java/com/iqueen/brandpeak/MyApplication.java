package com.sgdigitalposter.app;

import static android.webkit.ConsoleMessage.MessageLevel.LOG;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.AppCheckToken;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.sgdigitalposter.app.Ads.AppOpenManager;
import com.sgdigitalposter.app.ui.activities.SplashyActivity;
import com.sgdigitalposter.app.utils.Connectivity;
import com.sgdigitalposter.app.utils.Constant;
import com.sgdigitalposter.app.utils.PrefManager;
import com.sgdigitalposter.app.utils.Util;
import com.onesignal.OneSignal;
import com.onesignal.debug.LogLevel;
import com.onesignal.notifications.IDisplayableNotification;
import com.onesignal.notifications.INotificationLifecycleListener;
import com.onesignal.notifications.INotificationWillDisplayEvent;

import org.json.JSONObject;

public class MyApplication extends Application {

    Connectivity connectivity;

    PrefManager prefManager;
    public static Context context;

    private static final String ONESIGNAL_APP_ID = "cc91206a-406f-4148-8a99-d07d6b136d40";

    public static AppOpenManager appOpenAdManager;
    public static MyApplication myApplication;

    @Override
    public void onCreate() {
        super.onCreate();

        myApplication = this;

        connectivity = new Connectivity(this);
        prefManager = new PrefManager(this);

        context = this;
        if (connectivity.isConnected()) {
            Config.IS_CONNECTED = true;
        } else {
            Config.IS_CONNECTED = false;
        }

//        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);
          OneSignal.getDebug().setLogLevel(LogLevel.VERBOSE);

//        // OneSignal Initialization
//        OneSignal.initWithContext(this);
//        OneSignal.setAppId(ONESIGNAL_APP_ID);

        // OneSignal Initialization
        OneSignal.initWithContext(this, ONESIGNAL_APP_ID);

        FirebaseApp.initializeApp(/*context=*/ this);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
                firebaseAppCheck.installAppCheckProviderFactory(
                        PlayIntegrityAppCheckProviderFactory.getInstance());
                firebaseAppCheck.addAppCheckListener(new FirebaseAppCheck.AppCheckListener() {
                    @Override
                    public void onAppCheckTokenChanged(@NonNull AppCheckToken token) {
                        Util.showLog("Token: " + token.getToken() + " " + token.getExpireTimeMillis());
                    }
                });

            }
        }, 1000);
        MobileAds.initialize(
                this,
                new OnInitializationCompleteListener() {
                    @Override
                    public void onInitializationComplete(InitializationStatus initializationStatus) {
                    }
                });

        OneSignal.getNotifications().addForegroundLifecycleListener(new INotificationLifecycleListener() {
            @Override
            public void onWillDisplay(@NonNull INotificationWillDisplayEvent event) {
//                IDisplayableNotification notification = event.getNotification();
//                Config.isFromNotifications = true;
//                Intent intent = new Intent(context, SplashyActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);
            }
        });
        OneSignal.getNotifications().addForegroundLifecycleListener(new INotificationLifecycleListener() {
            @Override
            public void onWillDisplay(@NonNull INotificationWillDisplayEvent event) {

        IDisplayableNotification notification = event.getNotification();
        JSONObject data = notification.getAdditionalData();

        event.preventDefault();
        Runnable r = () -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ignored) {
            }

            notification.display();
        };

        Thread t = new Thread(r);
        t.start();
            }
        });
    }



    public static int getScreenWidth() {
        int columnWidth;
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        final Point point = new Point();

        point.x = display.getWidth();
        point.y = display.getHeight();

        columnWidth = point.x;
        return columnWidth;
    }

    public static Display getDefaultDisplay() {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        return display;
    }

    public static int getScreenHeight() {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        final Point point = new Point();
        point.y = display.getHeight();

        return point.y;
    }

    public static int getColumnWidth(int column, float grid_padding) {
        Resources r = context.getResources();
        float padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, grid_padding, r.getDisplayMetrics());
        return (int) ((getScreenWidth() - ((column + 1) * padding)) / column);
    }

    public static void ShowOpenAds() {
        try {
            if(prefManager().getBoolean(Constant.OPEN_AD_ENABLE) && prefManager().getBoolean(Constant.ADS_ENABLE) && !Constant.IS_SUBSCRIBED) {
                appOpenAdManager = new AppOpenManager(myApplication);
            }
        } catch (Exception e) {
            Util.showErrorLog(e.getMessage(), e);
        }
    }

    public static PrefManager prefManager() {
        return new PrefManager(myApplication);
    }
}
