package com.sgdigitalposter.app.ui.activities;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.sgdigitalposter.app.databinding.ActivityMainBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationBarView;
import com.sgdigitalposter.app.Ads.BannerAdManager;
import com.sgdigitalposter.app.Ads.GDPRChecker;
import com.sgdigitalposter.app.BuildConfig;
import com.sgdigitalposter.app.Config;
import com.sgdigitalposter.app.MyApplication;
import com.sgdigitalposter.app.R;
import com.sgdigitalposter.app.binding.GlideBinding;
import com.sgdigitalposter.app.items.UserItem;
import com.sgdigitalposter.app.ui.dialog.DialogMsg;
import com.sgdigitalposter.app.ui.dialog.LanguageDialog;
import com.sgdigitalposter.app.ui.fragments.BusinessFragment;
import com.sgdigitalposter.app.ui.fragments.CustomFragment;
import com.sgdigitalposter.app.ui.fragments.NewsFragment;
import com.sgdigitalposter.app.ui.fragments.DownloadFragment;
import com.sgdigitalposter.app.ui.fragments.HomeFragment;
import com.sgdigitalposter.app.utils.Connectivity;
import com.sgdigitalposter.app.utils.Constant;
import com.sgdigitalposter.app.utils.PrefManager;
import com.sgdigitalposter.app.utils.Util;
import com.sgdigitalposter.app.viewmodel.BusinessViewModel;
import com.sgdigitalposter.app.viewmodel.UserViewModel;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.onesignal.Continue;
import com.onesignal.OneSignal;

import java.security.Permission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    PrefManager prefManager;
    private String[] PERMISSIONS = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_MEDIA_IMAGES};

    UserViewModel userViewModel;
    BusinessViewModel businessViewModel;
    UserItem userItem = null;
    DialogMsg dialogMsg;
    Connectivity connectivity;

    boolean isLoadingHomePage;
    boolean isLoadingNewPage;
    boolean isLoadingCustomPage;
    boolean isLoadingBusinessPage;

    boolean isLoadingDownloadPage;


    int permissionsCount = 0;
    ActivityResultLauncher<String[]> permissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                    new ActivityResultCallback<Map<String, Boolean>>() {
                        @RequiresApi(api = Build.VERSION_CODES.M)
                        @Override
                        public void onActivityResult(Map<String, Boolean> result) {
                            ArrayList<Boolean> list = new ArrayList<>(result.values());
                            permissionsList = new ArrayList<>();
                            permissionsCount = 0;
                            for (int i = 0; i < list.size(); i++) {
                                if (shouldShowRequestPermissionRationale(PERMISSIONS[i])) {
                                    permissionsList.add(PERMISSIONS[i]);
                                } else if (!hasPermission(MainActivity.this, PERMISSIONS[i])) {
                                    permissionsCount++;
                                }
                            }
                            if (permissionsList.size() > 0) {
                                //Some permissions are denied and can be asked again.
                                askForPermissions(permissionsList);
                            } else if (permissionsCount > 0) {
                                //Show alert dialog
                                showPermissionDialog();
                            } else {
                                //One Signal
                                OneSignal.getNotifications().requestPermission(false, Continue.none());
                                //All permissions granted. Do your stuff 🤞
                                Util.showLog("All permissions granted. Do your stuff \uD83E\uDD1E");
                            }
                        }
                    });

    private void askForPermissions(ArrayList<String> permissionsList) {
        String[] newPermissionStr = new String[permissionsList.size()];
        for (int i = 0; i < newPermissionStr.length; i++) {
            newPermissionStr[i] = permissionsList.get(i);
        }
        if (newPermissionStr.length > 0) {
            permissionsLauncher.launch(newPermissionStr);
        } else {
            showPermissionDialog();
        }
    }

    AlertDialog alertDialog;

    private void showPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permission required")
                .setMessage("Some permissions are needed to be allowed to use this app without any problems.")
                .setPositiveButton("Ok", (dialog, which) -> {
                    dialog.dismiss();
                });
        if (alertDialog == null) {
            alertDialog = builder.create();
            if (!alertDialog.isShowing()) {
                alertDialog.show();
            }
        }
    }


    private boolean hasPermission(Context context, String permissionStr) {
        return ContextCompat.checkSelfPermission(context, permissionStr) == PackageManager.PERMISSION_GRANTED;
    }

    ArrayList<String> permissionsList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Util.applyStatusBarPadding(binding.contentView);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        //One Signal
        OneSignal.getNotifications().requestPermission(false, Continue.none());

        connectivity = new Connectivity(this);
        prefManager = new PrefManager(this);
        dialogMsg = new DialogMsg(this, false);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        businessViewModel = new ViewModelProvider(this).get(BusinessViewModel.class);

        if (!Constant.FOR_ADD_BUSINESS) {
            binding.toolbar.ibSearch.setVisibility(View.VISIBLE);
            setupFragment(new HomeFragment());
            binding.bottomNavigationView.setSelectedItemId(R.id.home_menu);
        } else {
            binding.toolbar.ibSearch.setVisibility(View.GONE);
            binding.toolbar.toolName.setText(getResources().getString(R.string.menu_business));
            setupFragment(new BusinessFragment());
            binding.bottomNavigationView.setSelectedItemId(R.id.business_menu);
        }

        if (!prefManager.getBoolean(Constant.NOTIFICATION_FIRST)) {
            prefManager.setBoolean(Constant.NOTIFICATION_ENABLED, true);
            prefManager.setBoolean(Constant.NOTIFICATION_FIRST, true);
            //OneSignal.promptForPushNotifications();
        }
//        setupFragment(new HomeFragment());
//        binding.bottomNavigationView.setSelectedItemId(R.id.home_menu);
        setUi();
        initData();
        if (prefManager.getString(Constant.api_key).equals(Constant.api_key)) {
            Util.loadFirebase(this);
        }
        if (prefManager.getBoolean(Constant.IS_LOGIN)) {
            changeData();
        } else {
            setUserData(null, false);
        }
        showOfferDialog();
    }

    private void showOfferDialog() {
        if (Config.offerItem != null && !Config.offerItem.image.equals("")) {
            dialogMsg.showOfferDialog(Config.offerItem.image);

           /* LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) dialogMsg.cvOffer.getLayoutParams();

            int width = MyApplication.getColumnWidth(1, getResources().getDimension(com.intuit.ssp.R.dimen._10ssp));

            params.width = width;
            params.height = (int) (width);*/

            //   dialogMsg.cvOffer.setLayoutParams(params);

            dialogMsg.show();
            dialogMsg.ivOffer.setOnClickListener(v -> {
                dialogMsg.cancel();
                startActivity(new Intent(MainActivity.this, SubsPlanActivity.class));
            });
            dialogMsg.ivCancel.setOnClickListener(v -> {
                dialogMsg.cancel();
            });
        }
    }

    private void changeData() {
        userViewModel.getUserDataById().observe(this, listResource -> {
            if (listResource != null) {
                Util.showLog("Got Data "
                        + listResource.message +
                        listResource.toString());

                switch (listResource.status) {
                    case LOADING:
                        // Loading State
                        // Data are from Local DB

                        if (listResource.data != null) {
                            userItem = listResource.data;
                            setUserData(listResource.data, true);
                        }

                        break;
                    case SUCCESS:
                        // Success State
                        // Data are from Server

                        if (listResource.data != null) {
                            userItem = listResource.data;
                            setUserData(listResource.data, true);
                        }

                        break;
                    case ERROR:
                        // Error State

                        Util.showLog("Error: " + listResource.message);

                        break;
                    default:
                        // Default
                        break;
                }

            } else {

                // Init Object or Empty Data
                Util.showLog("Empty Data");

            }
        });
        userViewModel.setUserById(prefManager.getString(Constant.USER_ID));
        loadBusiness();
    }

    private void loadBusiness() {
        businessViewModel.getBusiness().observe(this, resource -> {
            if (resource != null) {

                Util.showLog("Got Data" + resource.message + resource.toString());

                switch (resource.status) {
                    case LOADING:
                        // Loading State
                        // Data are from Local DB

                        break;
                    case SUCCESS:
                        // Success State
                        // Data are from Server
                        break;
                    case ERROR:
                        // Error State
                        break;
                    default:
                        // Default

                        break;
                }

            } else {

                // Init Object or Empty Data
                Util.showLog("Empty Data");

            }
        });
        businessViewModel.setBusinessObj(prefManager.getString(Constant.USER_ID));
    }

    private void setUserData(UserItem data, boolean bool) {
        if (bool) {
            prefManager.setString(Constant.REFER_CODE_BY, data.referralCode);
            Constant.IS_SUBSCRIBED = data.isSubscribed;
            GlideBinding.bindImage(binding.header.cvProfileImage, data.userImage);
            prefManager.setString(Constant.USER_IMAGE, data.userImage);
            binding.header.txtHeaderName.setText(data.userName);
            binding.header.txtHeaderEmail.setText(data.email);
            binding.header.liHeader.setOnClickListener(v -> {
                closeDrawer();
                startActivity(new Intent(this, ProfileActivity.class));
            });
            binding.navLogin.setVisibility(View.GONE);
            binding.navLogout.setVisibility(View.VISIBLE);
            if (prefManager.getBoolean(Constant.REFER_SYSTEM_ENABLE)) {
                binding.navRefer.setVisibility(View.VISIBLE);
            }
        } else {
            binding.navLogout.setVisibility(View.GONE);
            binding.navLogin.setVisibility(View.VISIBLE);
            binding.navProfile.setVisibility(View.GONE);
            binding.header.cvProfileImage.setImageDrawable(getDrawable(R.drawable.ic_profile));
            binding.header.txtHeaderName.setText(getString(R.string.click_here));
            binding.header.txtHeaderEmail.setText(getString(R.string.login_first_login));
            binding.header.liHeader.setOnClickListener(v -> {
                closeDrawer();
                startActivity(new Intent(this, LoginActivity.class));
            });
            binding.navRefer.setVisibility(View.GONE);
        }
    }

    private void initData() {
        userViewModel.getAppInfo().observe(this, listResource -> {
            if (listResource != null) {

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

                                //Native Ads Id Change
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

                                prefManager.setString(Constant.CURRENCY, listResource.data.currency);

                                prefManager.setString(Constant.CASHFREE_TYPE, listResource.data.cashfreeType);
                                prefManager.setBoolean(Constant.REWARD_AD_ENABLE, listResource.data.rewardedAdsEnable.equals(Config.ONE) ? true : false);
                                prefManager.setString(Constant.REWARD_AD_ID, listResource.data.rewardedAdsId);

                                if (prefManager.getBoolean(Constant.ADS_ENABLE)) {
                                    initializeAds();
                                }

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

                        break;
                    default:
                        // Default

                        break;
                }

            } else {

                // Init Object or Empty Data
                Util.showLog("Empty Data");
//                prefManager.setBoolean(Constant.IS_LOGIN, false);

            }

        });
        userViewModel.setAppInfo("good");
    }

    private void initializeAds() {
        switch (prefManager.getString(Constant.AD_NETWORK)) {
            case Constant.ADMOB:
                new GDPRChecker()
                        .withContext(MainActivity.this)
                        .withPrivacyUrl(prefManager.getString(Constant.PRIVACY_POLICY_LINK))
                        .withPublisherIds(prefManager.getString(Constant.PUBLISHER_ID))
                        .check();
                MyApplication.ShowOpenAds();
                BannerAdManager.showBannerAds(this, binding.llAdview);
                break;
            case Constant.UNITY:
                break;
            case Constant.FACEBOOK:
                break;
        }
    }

    private void setUi() {
        isLoadingHomePage = true;
        isLoadingNewPage = true;
        isLoadingCustomPage = true;
        isLoadingBusinessPage = true;
        isLoadingDownloadPage = true;
        checkPer();
        binding.drawerLayout.setScrimColor(Color.TRANSPARENT);
        binding.drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
                                                   @Override
                                                   public void onDrawerSlide(View drawer, float slideOffset) {

                                                       binding.contentView.setX(binding.navigationView.getWidth() * slideOffset);
                                                       DrawerLayout.LayoutParams lp =
                                                               (DrawerLayout.LayoutParams) binding.contentView.getLayoutParams();
                                                       lp.height = drawer.getHeight() -
                                                               (int) (drawer.getHeight() * slideOffset * 0.3f);
                                                       lp.topMargin = (drawer.getHeight() - lp.height) / 2;
                                                       binding.contentView.setLayoutParams(lp);
                                                   }

                                                   @Override
                                                   public void onDrawerOpened(View drawerView) {
                                                       binding.toolbar.toolbarIvMenu.setBackground(getDrawable(R.drawable.ic_back));
                                                   }

                                                   @Override
                                                   public void onDrawerClosed(View drawerView) {
                                                       binding.toolbar.toolbarIvMenu.setBackground(getDrawable(R.drawable.ic_menu_icon));
                                                   }
                                               }
        );

        binding.bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home_menu:
                        if (isLoadingHomePage) {
                            binding.toolbar.ibSearch.setVisibility(View.VISIBLE);
                            binding.toolbar.toolName.setText(getResources().getString(R.string.app_name));
                            setupFragment(new HomeFragment());
                            isLoadingHomePage = false;
                            isLoadingNewPage = true;
                            isLoadingCustomPage = true;
                            isLoadingBusinessPage = true;
                            isLoadingDownloadPage = true;
                            return true;
                        } else {
                            return false;
                        }
                    case R.id.news_menu:
                        if (isLoadingNewPage) {
                            binding.toolbar.ibSearch.setVisibility(View.GONE);
                            binding.toolbar.toolName.setText(getResources().getString(R.string.menu_news));
                            setupFragment(new NewsFragment());
                            isLoadingHomePage = true;
                            isLoadingNewPage = false;
                            isLoadingCustomPage = true;
                            isLoadingBusinessPage = true;
                            isLoadingDownloadPage = true;
                            return true;
                        } else {
                            return false;
                        }
                    case R.id.custom_menu:
                        if (isLoadingCustomPage) {
                            binding.toolbar.ibSearch.setVisibility(View.VISIBLE);
                            binding.toolbar.toolName.setText(getResources().getString(R.string.menu_custom));
                            setupFragment(new CustomFragment());
                            isLoadingHomePage = true;
                            isLoadingNewPage = true;
                            isLoadingCustomPage = false;
                            isLoadingBusinessPage = true;
                            isLoadingDownloadPage = true;
                            return true;
                        } else {
                            return false;
                        }
                    case R.id.business_menu:
                        if (isLoadingBusinessPage) {
                            binding.toolbar.ibSearch.setVisibility(View.GONE);
                            binding.toolbar.toolName.setText(getResources().getString(R.string.menu_business));
                            setupFragment(new BusinessFragment());
                            isLoadingHomePage = true;
                            isLoadingNewPage = true;
                            isLoadingCustomPage = true;
                            isLoadingBusinessPage = false;
                            isLoadingDownloadPage = true;
                            return true;
                        } else {
                            return false;
                        }
                    case R.id.download_menu:
                        if (isLoadingDownloadPage) {
                            binding.toolbar.ibSearch.setVisibility(View.GONE);
                            binding.toolbar.toolName.setText(getResources().getString(R.string.menu_download));
                            setupFragment(new DownloadFragment());
                            isLoadingHomePage = true;
                            isLoadingNewPage = true;
                            isLoadingCustomPage = true;
                            isLoadingBusinessPage = true;
                            isLoadingDownloadPage = false;
                            return true;
                        } else {
                            return false;
                        }
                    default:
                        return false;
                }
            }
        });

        binding.toolbar.toolbarIvMenu.setOnClickListener(v -> {
            if (binding.drawerLayout.isOpen()) {
                binding.drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                binding.drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        binding.toolbar.ibSearch.setOnClickListener(v -> {
            startActivity(new Intent(this, SearchActivity.class));
        });

        binding.navProducts.setOnClickListener(v -> {
//            closeDrawer();
            startActivity(new Intent(this, ProductActivity.class));
            closeDrawerUsingHandler();

        });

        binding.navLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
        });

        binding.navLanguage.setOnClickListener(v -> {
//            closeDrawer();
            closeDrawerUsingHandler();
            LanguageDialog dialog = new LanguageDialog(this, languages -> {
                prefManager.setString(Constant.USER_LANGUAGE, languages);
            });
            dialog.showDialog();
        });
        binding.navCategory.setOnClickListener(v -> {
//            closeDrawer();
            startActivity(new Intent(this, CategoryActivity.class));
            closeDrawerUsingHandler();

        });

        binding.navBusinessCard.setOnClickListener(v -> {
//            closeDrawer();
            startActivity(new Intent(this, VCardActivity.class));
            closeDrawerUsingHandler();

        });

        binding.navHome.setOnClickListener(v -> {
            closeDrawer();
            binding.bottomNavigationView.setSelectedItemId(R.id.home_menu);
//            closeDrawerUsingHandler();

        });

//        String secureId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
//        Util.showLog("AndroidId: " + secureId);
        binding.navSubscribe.setOnClickListener(v -> {
//            closeDrawer();
            startActivity(new Intent(this, SubsPlanActivity.class));
            closeDrawerUsingHandler();

        });
        binding.navSetting.setOnClickListener(v -> {
//            closeDrawer();
            startActivity(new Intent(this, SettingActivity.class));
            closeDrawerUsingHandler();

        });

        binding.navContact.setOnClickListener(v -> {
//            closeDrawer();
            startActivity(new Intent(this, ContactUsActivity.class));
            closeDrawerUsingHandler();

        });
        binding.navProfile.setOnClickListener(v -> {
//            closeDrawer();
            startActivity(new Intent(this, ProfileActivity.class));
            closeDrawerUsingHandler();

        });
        binding.navAboutUs.setOnClickListener(v -> {
//            closeDrawer();
            startActivity(new Intent(this, AboutUsActivity.class));
            closeDrawerUsingHandler();

        });
        binding.navRate.setOnClickListener(v -> {
//            closeDrawer();
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName()));
            startActivity(intent);
            closeDrawerUsingHandler();

        });

        binding.navLogout.setOnClickListener(v -> {
            if (Util.doubleClick()) {
            } else {
                DialogMsg dialogMsg = new DialogMsg(this, false);
                dialogMsg.showConfirmDialog(getString(R.string.menu_logout), getString(R.string.message__want_to_logout),
                        getString(R.string.message__logout),
                        getString(R.string.message__cancel_close));
                dialogMsg.show();

                dialogMsg.okBtn.setOnClickListener(view -> {

                    dialogMsg.cancel();
                    if (!connectivity.isConnected()) {
                        Util.showToast(this, getString(R.string.error_message__no_internet));
                        return;
                    }

                    if (userItem != null) {
                        userViewModel.deleteUserLogin(userItem).observe(this, status -> {
                            if (status != null) {

                                Util.showLog("User is Status : " + status);

                                prefManager.setBoolean(Constant.IS_LOGIN, false);
                                prefManager.remove(Constant.USER_ID);
                                prefManager.remove(Constant.USER_EMAIL);
                                prefManager.remove(Constant.USER_PASSWORD);
                                prefManager.remove(Constant.PERSONAL_NAME);
                                prefManager.remove(Constant.PERSONAL_IMAGE);
                                prefManager.remove(Constant.PERSONAL_NUMBER);
                                prefManager.remove(Constant.USER_IMAGE);
                                prefManager.setString("UPI_ID", "");

                                userItem = null;

                                setUserData(null, false);

                                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                        .requestEmail()
                                        .build();
                                GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
                                googleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                    }
                                });

                                Util.showToast(MainActivity.this, getString(R.string.success_logout));
                            }
                        });

                        Util.showLog("nav_logout_login");
                    }
                });

                dialogMsg.cancelBtn.setOnClickListener(view -> dialogMsg.cancel());
//                closeDrawerUsingHandler();
                closeDrawer();
            }
        });


        binding.navPrivacyPolicy.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, PrivacyActivity.class);
            intent.putExtra("type", Constant.PRIVACY_POLICY);
            startActivity(intent);
            closeDrawerUsingHandler();
        });

        binding.navShare.setOnClickListener(v -> {
            if (Util.doubleClick()) {
            } else {
                try {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                    String shareMessage = getString(R.string.share_msg);
                    shareMessage = shareMessage + " " + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID + "\n\n";
                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                    startActivity(Intent.createChooser(shareIntent, "choose one"));
                } catch (Exception e) {
                    //e.toString();
                }
            }
        });

        binding.navRefer.setOnClickListener(v -> {
//            closeDrawer();
            if (!connectivity.isConnected()) {
                Util.showToast(MainActivity.this, getResources().getString(R.string.error_message__no_internet));
                return;
            }

            if (!prefManager.getBoolean(Constant.IS_LOGIN)) {
                dialogMsg.showWarningDialog(getString(R.string.login_login), getString(R.string.login_first_login), getString(R.string.login_login), false);
                dialogMsg.show();
                dialogMsg.okBtn.setOnClickListener(v2 -> {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                });
                return;
            }
            startActivity(new Intent(MainActivity.this, ReferralActivity.class));
            closeDrawerUsingHandler();
        });

        if (!prefManager.getBoolean(Constant.PRODUCT_ENABLE)) {
            binding.navProducts.setVisibility(View.GONE);
        }
        if (!prefManager.getBoolean(Constant.REFER_SYSTEM_ENABLE) || !prefManager.getBoolean(Constant.IS_LOGIN)) {
            binding.navRefer.setVisibility(View.GONE);
        }

    }

    void closeDrawerUsingHandler() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                closeDrawer();
            }
        }, 1000);
    }

    private void checkPer() {
        Dexter.withContext(this)
                .withPermissions(
                        PERMISSIONS
                ).withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
//                        Log.e("SB", "onPermissionsChecked: Permission Check MainActivity  : ");
                        /* ...*/
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
//                        Log.e("SB", "onPermissionRationaleShouldBeShown: Permission Check MainActivity  : ");
                        /* ... */
                    }
                }).withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError dexterError) {
//                        Log.e("SB", "onError: Permission Check MainActivity  : ");
                        permissionsList = new ArrayList<>();
                        permissionsList.addAll(Arrays.asList(PERMISSIONS));
                        askForPermissions(permissionsList);
                    }
                }).check();


//            OneSignal.setDisableGMSMissingPrompt(true);
        OneSignal.getDisableGMSMissingPrompt();
//        OneSignal.promptForPushNotifications(false, new OneSignal.PromptForPushNotificationPermissionResponseHandler() {
//            @Override
//            public void response(boolean b) {
//                Util.showLog("NOTO: " + b);
//            }
//        });

    }

    private void closeDrawer() {
        binding.drawerLayout.closeDrawer(GravityCompat.START);
    }

    public void setupFragment(Fragment fragment) {
        try {
            this.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fl_main, fragment)
                    .commitAllowingStateLoss();
        } catch (Exception e) {
            Util.showLog("Error! Can't replace fragment.");
        }
    }

    private void launchIntro() {
        startActivity(new Intent(this, IntroActivity.class));
    }

    @Override
    public void onBackPressed() {

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fl_main);

        if (fragment != null) {
            if (fragment instanceof HomeFragment) {

                DialogMsg dialogMsg = new DialogMsg(this, false);
                dialogMsg.showConfirmDialog(getString(R.string.menu_exit), getString(R.string.do_you_want_to_exit), getString(R.string.yes), getString(R.string.no));
                dialogMsg.show();
                dialogMsg.okBtn.setOnClickListener(v -> {
                    dialogMsg.cancel();
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addCategory(Intent.CATEGORY_HOME);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    super.onBackPressed();
                    finish();
                    System.exit(0);
                });
            } else {
                binding.toolbar.ibSearch.setVisibility(View.GONE);
                binding.toolbar.toolName.setText(getResources().getString(R.string.app_name));
                setupFragment(new HomeFragment());
                binding.bottomNavigationView.setSelectedItemId(R.id.home_menu);
            }
        }

    }
}