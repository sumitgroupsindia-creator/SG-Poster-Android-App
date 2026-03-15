package com.sgdigitalposter.app.ui.activities;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.sgdigitalposter.app.utils.Constant.AVL_REWARD;
import static com.sgdigitalposter.app.utils.Constant.CURRENT_REWARD;
import static com.sgdigitalposter.app.utils.Constant.REWARD_AD_LIMIT;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.material.tabs.TabLayout;
import com.google.android.ump.ConsentInformation;
import com.sgdigitalposter.app.Ads.BannerAdManager;
import com.sgdigitalposter.app.Ads.GDPRChecker;
import com.sgdigitalposter.app.Config;
import com.sgdigitalposter.app.MyApplication;
import com.sgdigitalposter.app.R;
import com.sgdigitalposter.app.adapters.DetailAdapter;
import com.sgdigitalposter.app.adapters.DynamicFrameAdapter;
import com.sgdigitalposter.app.adapters.ImageSliderAdapter;
import com.sgdigitalposter.app.adapters.LanguageAdapter;
import com.sgdigitalposter.app.binding.GlideApp;
import com.sgdigitalposter.app.binding.GlideBinding;
import com.sgdigitalposter.app.databinding.ActivityDetailBinding;
import com.sgdigitalposter.app.editor.PosterActivity;
import com.sgdigitalposter.app.items.BusinessItem;
import com.sgdigitalposter.app.items.BusinessSubCategoryItem;
import com.sgdigitalposter.app.items.DynamicFrameItem;
import com.sgdigitalposter.app.items.LanguageItem;
import com.sgdigitalposter.app.items.PostItem;
import com.sgdigitalposter.app.items.UserItem;
import com.sgdigitalposter.app.listener.ClickListener;
import com.sgdigitalposter.app.ui.dialog.DialogMsg;
import com.sgdigitalposter.app.ui.dialog.LanguageDialog;
import com.sgdigitalposter.app.ui.stickers.Sticker_info;
import com.sgdigitalposter.app.ui.stickers.text.TextSTRInfo;
import com.sgdigitalposter.app.utils.Connectivity;
import com.sgdigitalposter.app.utils.Constant;
import com.sgdigitalposter.app.utils.PrefManager;
import com.sgdigitalposter.app.utils.StorageUtils;
import com.sgdigitalposter.app.utils.Util;
import com.sgdigitalposter.app.viewmodel.BusinessViewModel;
import com.sgdigitalposter.app.viewmodel.CategoryViewModel;
import com.sgdigitalposter.app.viewmodel.LanguageViewModel;
import com.sgdigitalposter.app.viewmodel.PostViewModel;
import com.sgdigitalposter.app.viewmodel.UserViewModel;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class DetailActivity extends AppCompatActivity {

    ActivityDetailBinding binding;
    int screenWidth;
    String type;

    DetailAdapter adapter;
    List<DynamicFrameItem> frameItemList = new ArrayList<>();
    List<DynamicFrameItem> ori_frameItemList = new ArrayList<>();
    LanguageAdapter languageAdapter;
    LanguageAdapter subcategoryAdapter;

    PostViewModel postViewModel;

    PrefManager prefManager;

    List<PostItem> postItemList;
    int position = 0;

    UserViewModel userViewModel;
    BusinessViewModel businessViewModel;
    CategoryViewModel categoryViewModel;

    LanguageViewModel languageViewModel;
    UserItem userItem;
    BusinessItem businessItem;
    Connectivity connectivity;
    DialogMsg dialogMsg;

    ExoPlayer absPlayerInternal;
    public boolean isVideo = false;
    public boolean showVideo = false;
    public String videoUrl = "";
    public boolean hasImage = false;

    PostItem postItem = null;

    List<LanguageItem> subCategoryList;
    private String[] PERMISSIONS = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};

    private int pastVisibleItems, visibleItemCount, totalItemCount;
    private int[] firstVisibleItems = null;
    int page = 0;


    int permissionsCount = 0;

    String subCategory = "";
    String language = "";

    public List<Sticker_info> stickerInfoArrayList;
    public List<TextSTRInfo> textInfoArrayList;

    public String realX;
    public String realY;
    public String calcWidth = "";
    public String calcHeight = "";
    public JSONObject bgObj;
    private String ratio;
    private String background;
    ProgressDialog prgDialog;
    JSONArray layers;
    RewardedAd rewardedAd;
    ActivityResultLauncher<String[]> permissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onActivityResult(Map<String, Boolean> result) {
                    ArrayList<Boolean> list = new ArrayList<>(result.values());
                    permissionsList = new ArrayList<>();
                    permissionsCount = 0;
                    for (int i = 0; i < list.size(); i++) {
                        if (shouldShowRequestPermissionRationale(PERMISSIONS[i])) {
                            permissionsList.add(PERMISSIONS[i]);
                        } else if (!hasPermission(DetailActivity.this, PERMISSIONS[i])) {
                            permissionsCount++;
                        }
                    }
                    if (permissionsList.size() > 0) {
                        //Some permissions are denied and can be asked again.
                        askForPermissions(permissionsList);
                    } else if (permissionsCount > 0) {
                        //Show alert dialog
                        showSettingsDialog("Permission Camera and Storage");

                    } else {
                        //All permissions granted. Do your stuff 🤞
                        com.sgdigitalposter.app.utils.Util.showLog("All permissions granted. Do your stuff \uD83E\uDD1E");
                        startPost();
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

    androidx.appcompat.app.AlertDialog alertDialog;

    private void showPermissionDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Permission required").setMessage("Some permissions are needed to be allowed to use this app without any problems.").setPositiveButton("Ok", (dialog, which) -> {
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
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Util.applyStatusBarPadding(binding.clMain);

        absPlayerInternal = new ExoPlayer.Builder(this).build(); //creating a player instance

//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        if (android.os.Build.VERSION.SDK_INT >= 33) {
            PERMISSIONS = new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES};
        }

        prgDialog = new ProgressDialog(this);
        prgDialog.setTitle("Please Wait");
        prgDialog.setMessage("Resource Downloading....");
        prgDialog.setCancelable(false);
        screenWidth = MyApplication.getColumnWidth(1, getResources().getDimension(com.intuit.ssp.R.dimen._10ssp));
        prefManager = new PrefManager(this);
        connectivity = new Connectivity(this);
        dialogMsg = new DialogMsg(this, false);

        Display defaultDisplay = MyApplication.getDefaultDisplay();
        Point point = new Point();
        defaultDisplay.getSize(point);
//        screenWidth = point.x;
        int i = screenWidth;
//        screenWidth = (i / 2);

        postViewModel = new ViewModelProvider(this).get(PostViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        businessViewModel = new ViewModelProvider(this).get(BusinessViewModel.class);
        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
        languageViewModel = new ViewModelProvider(this).get(LanguageViewModel.class);

        if (rewardedAd == null) {
            LoadRewardAds();
        }

        userViewModel.getDbUserData(prefManager.getString(Constant.USER_ID)).observe(this, resource -> {
            if (resource != null) {
                userItem = resource.user;
            }
        });

        businessViewModel.getBusiness().observe(this, resource -> {

            if (resource != null) {

                if (resource.data != null) {
                    for (BusinessItem item : resource.data) {
                        if (item.isDefault) {
                            businessItem = item;
                            break;
                        }
                    }
                }

            }

        });
        ratio = "1:1";
        postViewModel.setFrameObj(ratio, "All");
//        postViewModel.getFrameDB().observe(this, frameData -> {
//            if (frameData != null && frameData.size() > 0) {
//                binding.imageSlider.setVisibility(VISIBLE);
//                setFrameData(frameData);
//
//            } else {
//                binding.imageSlider.setVisibility(GONE);
//            }
//        });

//        userViewModel.getDefaultBusiness().observe(this, item -> {
//            if (item != null) {
//                businessItem = item;
//            } else {
//                businessItem = businessViewModel.getDefaultBusiness() != null ? businessViewModel.getDefaultBusiness() : null;
//            }
//        });

        if (prefManager.getBoolean(Constant.IS_LOGIN)) {
            businessViewModel.setBusinessObj(prefManager.getString(Constant.USER_ID));
        }

        postItemList = new ArrayList<>();

        BannerAdManager.showBannerAds(this, binding.llAdview);
//        binding.tabLayout.selectTab(binding.tabImage.);

        setShimmerEffect();

        setUpUi();
        loadImages();
    }

    private void loadImages() {
        showVideo = false;
        if (type.equals(Constant.FESTIVAL)) {
            loadFestival(false);
            if (!getIntent().getStringExtra(Constant.INTENT_POST_IMAGE).equals("")) {
                GlideBinding.bindImage(binding.ivShow, getIntent().getStringExtra(Constant.INTENT_POST_IMAGE));
            }
        } else if (type.equals(Constant.CATEGORY)) {
            loadCategory(false);
            if (!getIntent().getStringExtra(Constant.INTENT_POST_IMAGE).equals("")) {
                GlideBinding.bindImage(binding.ivShow, getIntent().getStringExtra(Constant.INTENT_POST_IMAGE));
            }
        } else if (type.equals(Constant.BUSINESS)) {
            loadBusinessCategory(false);
            loadSubcategory();
            if (!getIntent().getStringExtra(Constant.INTENT_POST_IMAGE).equals("")) {
                GlideBinding.bindImage(binding.ivShow, getIntent().getStringExtra(Constant.INTENT_POST_IMAGE));
            }
        } else if (type.equals(Constant.CUSTOM) || type.equals(Constant.CUSTOM_EDITABLE) || type.equals(Constant.CUSTOM_FEATURE)) {
            binding.tabLayout.setVisibility(GONE);
            loadCustom(false, type);
            if (!getIntent().getStringExtra(Constant.INTENT_POST_IMAGE).equals("")) {
                GlideBinding.bindImage(binding.ivShow, getIntent().getStringExtra(Constant.INTENT_POST_IMAGE));
            }
        }

        com.sgdigitalposter.app.utils.Util.showLog("Type: " + type + " \n" + "Image: " + getIntent().getStringExtra(Constant.INTENT_POST_IMAGE) + "DD: " + getIntent().getSerializableExtra(Constant.INTENT_POST_ITEM));
    }

    public void loadVideos() {
        showVideo = true;
        if (getIntent() != null) {
            type = getIntent().getStringExtra(Constant.INTENT_TYPE);
        }
        if (type.equals(Constant.FESTIVAL)) {
            loadFestival(true);
        } else if (type.equals(Constant.CATEGORY)) {
            loadCategory(true);
        } else if (type.equals(Constant.BUSINESS)) {
            loadBusinessCategory(true);
        } else if (type.equals(Constant.CUSTOM)) {

        }

    }

    private void loadSubcategory() {
        categoryViewModel.getSubCategory().observe(this, resource -> {
            if (resource != null) {

                com.sgdigitalposter.app.utils.Util.showLog("Got Data" + resource.message + resource.toString());

                switch (resource.status) {
                    case LOADING:
                        // Loading State
                        // Data are from Local DB
                        if (resource.data != null && resource.data.size() > 0) {
                            setSubData(resource.data);
                        }
                        break;
                    case SUCCESS:
                        // Success State
                        // Data are from Server

                        if (resource.data != null && resource.data.size() > 0) {
                            setSubData(resource.data);
                        }

                        break;
                    case ERROR:
                        // Error State
                        subCategoryList = null;
                        binding.hvCat.setVisibility(GONE);
                        break;
                    default:
                        // Default

                        break;
                }

            }
        });
        categoryViewModel.setSubCategory(getIntent().getStringExtra(Constant.INTENT_FEST_ID));
    }

    @SuppressLint("ResourceType")
    private void setSubData(List<BusinessSubCategoryItem> data) {
        binding.hvCat.setVisibility(VISIBLE);
        subCategoryList = new ArrayList<>();
        subCategoryList.add(new LanguageItem("All", "All", "All", true));
        for (BusinessSubCategoryItem item : data) {
            subCategoryList.add(new LanguageItem(item.businessCategoryId, item.businessCategoryIcon, item.businessCategoryName, false));
        }

        subcategoryAdapter.setLanguageItemList(subCategoryList);

        /*binding.cgImage.removeAllViews();

        Chip firstChip = new Chip(this);
        firstChip.setText("All");
        firstChip.setId(10000);
        firstChip.setChipBackgroundColorResource(R.color.bg_chip);
        firstChip.setCheckable(true);
        firstChip.setTextColor(getColor(R.color.tc_chip));
        firstChip.setTextAppearance(R.style.chitText);
        firstChip.setCheckedIconVisible(false);
        firstChip.setChecked(true);

        binding.cgImage.addView(firstChip);

        for (BusinessSubCategoryItem item : data) {

            Chip chip = new Chip(this);
            chip.setId(Integer.parseInt(item.businessCategoryId));
            chip.setText(item.businessCategoryName);
            chip.setChipBackgroundColorResource(R.color.bg_chip);
            chip.setCheckable(true);
            chip.setTextColor(getColor(R.color.tc_chip));
            chip.setTextAppearance(R.style.chitText);
            chip.setCheckedIconVisible(false);
            chip.setTag(item.businessCategoryId);

            binding.cgImage.addView(chip);

        }

        binding.cgImage.setOnCheckedStateChangeListener(new ChipGroup.OnCheckedStateChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull ChipGroup group, @NonNull List<Integer> checkedIds) {

                Util.showLog("CheckedID: " + getChipName(group.getCheckedChipId()));
                subCategory = getChipName(group.getCheckedChipId());
                page = 0;
                if (subCategory.equals("All")) {
                    loadBusinessCategory(false);
                } else {
                    postViewModel.setPostByIdObj(getIntent().getStringExtra(Constant.INTENT_FEST_ID),
                            BUSINESS, "", false, subCategory);
                }
            }
        });*/
    }

    /*public String getChipName(int id) {
        String name = "";
        int childCount = binding.cgImage.getChildCount();
        for (int i = 0; i < childCount; i++) {
            Chip chip = (Chip) binding.cgImage.getChildAt(i);
            if (chip.getId() == id) {
                name = chip.getText().toString();
                break;
            }

        }
        return name;
    }
*/
    private void setUpUi() {

        if (getIntent() != null) {
            type = getIntent().getStringExtra(Constant.INTENT_TYPE);
            isVideo = getIntent().getBooleanExtra(Constant.INTENT_VIDEO, false);
            if (getIntent().getSerializableExtra(Constant.INTENT_POST_ITEM) != null) {
                postItem = (PostItem) getIntent().getSerializableExtra(Constant.INTENT_POST_ITEM);
            }
        }

        binding.toolbar.toolbarIvMenu.setBackground(getDrawable(R.drawable.ic_back));
        binding.toolbar.toolbarIvMenu.setOnClickListener(v -> {
            if (absPlayerInternal != null) {
                absPlayerInternal.setPlayWhenReady(false);
                absPlayerInternal.stop();
                absPlayerInternal.seekTo(4);
            }
            onBackPressed();
        });

        binding.toolbar.txtEdit.setVisibility(VISIBLE);

        binding.toolbar.toolbarIvLanguage.setOnClickListener(v -> {
            LanguageDialog dialog = new LanguageDialog(this, data -> {
                if (data.equals("")) {
                    page = 0;
                }
                postViewModel.setPostByIdObj(getIntent().getStringExtra(Constant.INTENT_FEST_ID), type, prefManager.getString(Constant.USER_LANGUAGE), showVideo, type.equals(Constant.BUSINESS) ? subCategory.equals("All") ? "" : subCategory : "");
            });
            dialog.showDialog();
        });

        binding.toolbar.txtEdit.setOnClickListener(v -> {
            Dexter.withContext(DetailActivity.this).withPermissions(PERMISSIONS).withListener(new MultiplePermissionsListener() {
                public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                    if (multiplePermissionsReport.areAllPermissionsGranted()) {
                        startPost();
                    }
                    if (multiplePermissionsReport.isAnyPermissionPermanentlyDenied()) {
                        String data = "";
                        for (int i = 0; i < multiplePermissionsReport.getDeniedPermissionResponses().size(); i++) {
                            data = data + " , " + multiplePermissionsReport.getDeniedPermissionResponses().get(i).getPermissionName();
                        }
                        showSettingsDialog(data);
                    }
                }

                public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                    permissionToken.continuePermissionRequest();
                }
            }).withErrorListener(new PermissionRequestErrorListener() {
                public void onError(DexterError dexterError) {
//                    Toast.makeText(DetailActivity.this, "Error occurred! Please Give Permission", Toast.LENGTH_SHORT).show();
                    permissionsList = new ArrayList<>();
                    permissionsList.addAll(Arrays.asList(PERMISSIONS));
                    askForPermissions(permissionsList);
                }
            }).onSameThread().check();

        });

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) binding.ivShow.getLayoutParams();
        params.width = screenWidth;
        params.height = screenWidth;

        binding.ivShow.setLayoutParams(params);

        ConstraintLayout.LayoutParams params2 = (ConstraintLayout.LayoutParams) binding.videoPlayer.getLayoutParams();
        params2.width = screenWidth;
        params2.height = screenWidth;

        binding.videoPlayer.setLayoutParams(params2);

        languageAdapter = new LanguageAdapter(this, item -> {
            language = item.title;
            page = 0;
            if (language.equals("All")) {
                language = "";
            }
            postViewModel.setPostByIdObj(getIntent().getStringExtra(Constant.INTENT_FEST_ID), type, language, showVideo, type.equals(Constant.BUSINESS) ? subCategory.equals("All") ? "" : subCategory : "");
        });
        binding.hvLang.setAdapter(languageAdapter);

        if (type.equals(Constant.BUSINESS)) {

        } else {
            languageViewModel.getLanguages().observe(this, result -> {
                if (result.data != null) {
                    switch (result.status) {
                        case LOADING:
                            if (result.data.size() > 0) {
                                showLanguage(result.data);
                            }
                            break;
                        case SUCCESS:
                            if (result.data.size() > 0) {
                                showLanguage(result.data);
                            }
                            break;
                        case ERROR:
                            break;
                        default:
                            break;
                    }
                }
            });
            languageViewModel.setLanguageObj();
        }

        adapter = new DetailAdapter(this, new ClickListener<Integer>() {
            @Override
            public void onClick(Integer data) {
//                Log.e("SB", "onClick: " +data);
                if (postItemList != null) {
                    position = data;
                    setImageShow(postItemList.get(data));
                }
            }
        }, 3, getResources().getDimension(com.intuit.ssp.R.dimen._2ssp));
        binding.rvPost.setAdapter(adapter);

        binding.rvPost.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                StaggeredGridLayoutManager mLayoutManager = (StaggeredGridLayoutManager) recyclerView.getLayoutManager();

                if (mLayoutManager != null) {

                    visibleItemCount = mLayoutManager.getChildCount();
                    totalItemCount = mLayoutManager.getItemCount();

                    firstVisibleItems = mLayoutManager.findFirstVisibleItemPositions(firstVisibleItems);

                    if (firstVisibleItems != null && firstVisibleItems.length > 0) {
                        pastVisibleItems = firstVisibleItems[0];
                    }

                    if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                        page++;
                        postViewModel.getNewPost(page, getIntent().getStringExtra(Constant.INTENT_FEST_ID), showVideo, type);
                    }
                }
            }
        });

        postViewModel.getById().observe(this, resource -> {

            if (resource != null) {

                com.sgdigitalposter.app.utils.Util.showLog("Got Data" + resource.message + resource.toString());

                switch (resource.status) {
                    case LOADING:
                        // Loading State
                        // Data are from Local DB
                        if (resource.data != null) {

                            if (resource.data.size() > 0) {
                                setData(resource.data);
                                binding.executePendingBindings();
                            } else {
                                showError();
                            }

                        }
                        break;
                    case SUCCESS:
                        // Success State
                        // Data are from Server

                        if (resource.data != null && resource.data.size() > 0) {
                            setData(resource.data);
                            binding.executePendingBindings();
                        } else {
                            showError();
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
                com.sgdigitalposter.app.utils.Util.showLog("Empty Data");

            }
        });

        binding.ivPlayVideo.setOnClickListener(v -> {
            binding.ivPlayVideo.setVisibility(GONE);
            absPlayerInternal.seekTo(0);
            absPlayerInternal.setPlayWhenReady(true);
        });

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                binding.tabLayout.selectTab(tab);
                com.sgdigitalposter.app.utils.Util.showLog("" + tab.getId() + tab.getText());
                if (tab.getText() == getString(R.string.image)) {
                    page = 0;
                    showLoading();
                    loadImages();

                    binding.ivPlayVideo.setVisibility(GONE);
                    if (subCategoryList != null && subCategoryList.size() > 0) {
                        binding.hvCat.setVisibility(VISIBLE);
                    }

                    if (absPlayerInternal != null) {
                        absPlayerInternal.setPlayWhenReady(false);
                        absPlayerInternal.stop();
                        absPlayerInternal.seekTo(4);
                    }
                } else {
                    binding.hvCat.setVisibility(View.GONE);
                    page = 0;
                    showLoading();
                    loadVideos();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        subcategoryAdapter = new LanguageAdapter(this, item -> {
            subCategory = item.title;
            page = 0;
            if (subCategory.equals("All")) {
                loadBusinessCategory(false);
            } else {
                postViewModel.setPostByIdObj(getIntent().getStringExtra(Constant.INTENT_FEST_ID), Constant.BUSINESS, "", false, subCategory);
            }
        });
        binding.hvCat.setAdapter(subcategoryAdapter);
    }

    private void startPost() {
        if (postItemList != null && postItemList.size() > 0) {
            if (!connectivity.isConnected()) {
                dialogMsg.showErrorDialog(getString(R.string.error_message__no_internet), getString(R.string.ok));
                dialogMsg.show();
                return;
            }

            if (!prefManager.getBoolean(Constant.IS_LOGIN)) {
                dialogMsg.showWarningDialog(getString(R.string.please_login), getString(R.string.login_first_login), getString(R.string.login_login), false);
                dialogMsg.show();
                dialogMsg.okBtn.setOnClickListener(view -> {
                    dialogMsg.cancel();
                    startActivity(new Intent(DetailActivity.this, LoginActivity.class));
                });
                return;
            }

            if (businessItem == null) {

                if (userItem.businessLimit <= businessViewModel.getBusinessCount()) {

                    dialogMsg.showWarningDialog(getString(R.string.upgrade), getString(R.string.your_business_limit), getString(R.string.upgrade), true);
                    dialogMsg.show();
                    dialogMsg.okBtn.setOnClickListener(view -> {
                        dialogMsg.cancel();
                        startActivity(new Intent(DetailActivity.this, SubsPlanActivity.class));
                    });
                    return;
                } else {
                    dialogMsg.showWarningDialog(getString(R.string.add_business_titles), getString(R.string.please_add_business_titles), getString(R.string.add_business), true);
                    dialogMsg.show();
                    dialogMsg.okBtn.setOnClickListener(view -> {
                        dialogMsg.cancel();
                        Constant.FOR_ADD_BUSINESS = true;
                        startActivity(new Intent(DetailActivity.this, MainActivity.class));
                    });
                    return;
                }
            }

            if (!userItem.isSubscribed && postItem.is_premium) {
                dialogMsg.showUnlockDialog(getString(R.string.premium), getString(R.string.please_subscribe), getString(R.string.subscribe), true);
                dialogMsg.show();
                dialogMsg.okBtn.setOnClickListener(view -> {
                    dialogMsg.cancel();
                    startActivity(new Intent(DetailActivity.this, SubsPlanActivity.class));
                });
                dialogMsg.btnUnlock.setOnClickListener(view -> {
                    if (prefManager.getBoolean(AVL_REWARD)) {
                        if (isLoaded()) {
                            dialogMsg.cancel();
                            prefManager.setInt(CURRENT_REWARD, prefManager.getInt(CURRENT_REWARD) + 1);
                            showAd();
                        } else {
                            com.sgdigitalposter.app.utils.Util.showToast(DetailActivity.this, "Rewards not loaded");
                        }
                    } else {
                        com.sgdigitalposter.app.utils.Util.showToast(DetailActivity.this, "Your Daily Reward Limit is Over!");
                    }
                });
                return;
            }

            if (absPlayerInternal != null) {
                absPlayerInternal.setPlayWhenReady(false);
                absPlayerInternal.stop();
                absPlayerInternal.seekTo(4);
            }
            Constant.isCustom11Mode = false;
            Constant.isCustom45Mode = false;

            if (postItem.type.equals(Constant.CUSTOM_EDITABLE)) {
                posterProcess(postItem, businessItem, position);
            } else {
                Intent intent = new Intent(DetailActivity.this, PosterActivity.class);
                intent.putExtra(Constant.INTENT_POST_ITEM, postItem);
                intent.putExtra(Constant.INTENT_BUSINESS_ITEM, businessItem);
                intent.putExtra(Constant.INTENT_POS, position);
                intent.putExtra(Constant.INTENT_TYPE, postItem.type);
                startActivity(intent);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isLoaded()) {
            LoadRewardAds();
        }
        if (!videoUrl.isEmpty() && absPlayerInternal != null) {
            loadVideo(videoUrl);
        }

    }

    private void showSettingsDialog(String data) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Need Permissions");
        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings. \n " + data);
        builder.setPositiveButton("GOTO SETTINGS", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
                Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
                intent.setData(Uri.fromParts("package", getPackageName(), (String) null));
                startActivityForResult(intent, 101);
            }
        });
        builder.setNegativeButton("Cancel", (dialogInterface, i) -> {
            dialogInterface.cancel();
            showPermissionDialog();
        });
        builder.show();
    }

    private void setFrameData(List<DynamicFrameItem> data) {
        String[] split = ratio.split(":");
        float wrRat = Float.parseFloat(split[0]) / Float.parseFloat(split[1]);
        if (data != null && data.size() > 0) {


//            frameAdapter = new DynamicFrameAdapter(this, new ClickListener<DynamicFrameItem>() {
//                @Override
//                public void onClick(DynamicFrameItem data) {
//                    if (!userItem.isSubscribed && data.is_paid) {
//                        dialogMsg.showWarningDialog(getString(R.string.premium), getString(R.string.please_subscribe_frame), getString(R.string.subscribe),
//                                true);
//                        dialogMsg.show();
//                        dialogMsg.okBtn.setOnClickListener(view -> {
//                            dialogMsg.cancel();
//                            startActivity(new Intent(PosterActivity.this, SubsPlanActivity.class));
//                        });
//                        return;
//                    }
//                    int childCount = binding.txtStkrRel.getChildCount();
//
//                    for (int i = 0; i < childCount; i++) {
//                        View childAt = binding.txtStkrRel.getChildAt(i);
//                        binding.txtStkrRel.removeView(childAt);
//                    }
//                    binding.txtStkrRel.removeAllViews();
//                    prgDialog.show();
//                    setCustomData();
//                    setUpFrameData(data);
//                }
//            }, wrRat);
            frameItemList.clear();
            ori_frameItemList.clear();
            ori_frameItemList.addAll(data);
            for (int i = 0; i < data.size(); i++) {
                frameItemList.add(data.get(i));
            }
//            ImageSliderAdapter adapter = new ImageSliderAdapter(DetailActivity.this,frameItemList);
//            binding.imageSlider.setAdapter(adapter);

//            if (frameItemList.size() > 0) {
//                if (!frameLoaded) {
//                    frameLoaded = true;
//                    if (getIntent().getStringExtra(Constant.INTENT_TYPE).equals(Constant.CUSTOM_EDITABLE)) {
//
//                    } else {
//                        setUpFrameData(frameItemList.get(0));
//                    }
//                }
//            }
//            binding.rvFrame.setAdapter(frameAdapter);
        }
    }

    //    private void setUpFrameData(DynamicFrameItem dynamicFrameItem) {
//        Log.e("SB", "setUpFrameData dynamicFrameItem :" + dynamicFrameItem);
//        prgDialog.show();
//        if (dynamicFrameItem != null) {
//
//            if (dynamicFrameItem.name.equals("USER") && dynamicFrameItem.aspectRatio.equals("USER")) {
//                stickerInfoArrayList.clear();
//                mItemArray.clear();
//                textInfoArrayList.clear();
//
//                binding.ivFrame.setVisibility(VISIBLE);
//                Log.e("SB", "setUpFrameData thumbnail:" + dynamicFrameItem.thumbnail);
//                GlideBinding.bindImage(binding.ivFrame, dynamicFrameItem.thumbnail);
//                prgDialog.dismiss();
//            } else {
//                binding.ivFrame.setVisibility(GONE);
//                try {
//                    JSONObject json = new JSONObject(dynamicFrameItem.frameData);
//                    JSONArray layers = json.getJSONArray("layers");
//
//                    // Create a new JSONArray without null values
//                    JSONArray cleanedLayers = new JSONArray();
//
//                    for (int i = 0; i < layers.length(); i++) {
//                        Object layerObject = layers.opt(i);
//                        if (layerObject != null && !JSONObject.NULL.equals(layerObject)) {
//                            cleanedLayers.put(layerObject);
//                        }
//                    }
//                    stickerInfoArrayList.clear();
//                    mItemArray.clear();
//                    textInfoArrayList.clear();
//
//                    for (int i = 0; i < cleanedLayers.length(); i++) {
//                        Object layerObject = cleanedLayers.get(i);
//
//                        Log.d("test", "setUpFrameData: " + layerObject);
//
//                        if (layerObject instanceof JSONObject) {
//                            JSONObject jsonObject1 = (JSONObject) layerObject;
//                            processJson(i, jsonObject1, dynamicFrameItem.name);
//                        } else {
//                            Log.w("test", "setUpFrameData: Unexpected type at index " + i);
//                        }
//                    }
//
//                    PosterActivity.LoadStickersAsync loadStickersAsync = new PosterActivity.LoadStickersAsync();
//                    loadStickersAsync.execute();
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }
//    private void setCustomData() {
//        if (getIntent().hasExtra(Constant.INTENT_TYPE)) {
//            if (getIntent().getStringExtra(Constant.INTENT_TYPE).equals(Constant.CUSTOM_EDITABLE)) {
//                prgDialog.show();
//                LoadCustomAsync loadCustomAsync = new PosterActivity.LoadCustomAsync();
//                loadCustomAsync.execute();
//            }
//        }
//
//    }
    private void loadFestival(boolean isVideo) {
        binding.toolbar.toolName.setText(getIntent().getStringExtra(Constant.INTENT_FEST_NAME));

        postViewModel.setPostByIdObj(getIntent().getStringExtra(Constant.INTENT_FEST_ID), Constant.FESTIVAL, "", isVideo, "");

    }

    private void loadBusinessCategory(boolean isVideo) {
        binding.toolbar.toolName.setText(getIntent().getStringExtra(Constant.INTENT_FEST_NAME));
        postViewModel.setPostByIdObj(getIntent().getStringExtra(Constant.INTENT_FEST_ID), Constant.BUSINESS, "", isVideo, "");
    }

    private void loadCategory(boolean isVideo) {
        binding.toolbar.toolName.setText(getIntent().getStringExtra(Constant.INTENT_FEST_NAME));
        postViewModel.setPostByIdObj(getIntent().getStringExtra(Constant.INTENT_FEST_ID), Constant.CATEGORY, "", isVideo, "");

    }

    private void loadCustom(boolean isVideo, String type) {
        binding.toolbar.toolName.setText(getIntent().getStringExtra(Constant.INTENT_FEST_NAME));
        postViewModel.setPostByIdObj(getIntent().getStringExtra(Constant.INTENT_FEST_ID), type, "", isVideo, "");
    }

    private void showError() {
        binding.animationView.setVisibility(VISIBLE);
        binding.shimmerViewContainer.setVisibility(GONE);
        binding.videoPlayer.setVisibility(GONE);
        binding.tabLayout.setVisibility(GONE);
        binding.ivShow.setVisibility(View.GONE);
        binding.rvPost.setVisibility(GONE);
    }

    public void showLoading() {
        binding.animationView.setVisibility(GONE);
        binding.shimmerViewContainer.setVisibility(VISIBLE);
        binding.tabLayout.setVisibility(GONE);
        binding.videoPlayer.setVisibility(GONE);
        binding.ivShow.setVisibility(View.GONE);
        binding.rvPost.setVisibility(GONE);
    }

    private void setData(List<PostItem> data) {
        if (!type.equals(Constant.BUSINESS)) {
            binding.hvLang.setVisibility(VISIBLE);
        }
        com.sgdigitalposter.app.utils.Util.showLog("Video: " + isVideo);
        if (!isVideo) {
            binding.tabLayout.setVisibility(GONE);
        } else {
            binding.tabLayout.setVisibility(VISIBLE);
        }
        postItemList.clear();
        postItemList.addAll(data);
        binding.ivShow.setVisibility(VISIBLE);
        binding.rvPost.setVisibility(VISIBLE);
        binding.shimmerViewContainer.setVisibility(GONE);
        binding.animationView.setVisibility(View.GONE);

        adapter.setData(data);

        if (type.equals(Constant.FESTIVAL) || type.equals(Constant.CATEGORY) ||
                type.equals(Constant.CUSTOM) || type.equals(Constant.CUSTOM_EDITABLE) ||
                type.equals(Constant.CUSTOM_FEATURE) || type.equals(Constant.BUSINESS)) {
            if (getIntent().getStringExtra(Constant.INTENT_POST_IMAGE).equals("")) {
                position = new Random().nextInt(data.size());
                setImageShow(data.get(position));
            }
            if (showVideo) {
                position = new Random().nextInt(data.size());
                setImageShow(data.get(position));
            }
        }
    }

    private void showLanguage(List<LanguageItem> data) {

        List<LanguageItem> itemList = new ArrayList<>();
        itemList.clear();
        itemList.add(new LanguageItem("All", "All", "All", true));
        itemList.addAll(data);

        languageAdapter.setLanguageItemList(itemList);
    }

    private void setImageShow(PostItem postItem) {
        this.postItem = postItem;
        if (postItem.is_video) {
            binding.ivCrossVideo.setVisibility(postItem.is_premium == true ? VISIBLE : GONE);
            if (userItem != null && userItem.isSubscribed) {
                binding.ivCrossVideo.setVisibility(GONE);
            }
            if (absPlayerInternal != null) {
                absPlayerInternal.setPlayWhenReady(false);
                absPlayerInternal.stop();
                absPlayerInternal.seekTo(4);
            }
            loadVideo(postItem.image_url);
        } else {
            videoUrl = "";
        /*    float f = 1.0f;
            String width = postItemList.get(position).postWidth;
            String height = postItemList.get(position).postHeight;

            f = Float.parseFloat(height) / Float.parseFloat(width);

            int i2 = screenWidth;
            int i3 = Math.round((1.0f / f) * ((float) screenWidth));

            ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(i2, (int) (((float) i2) * f));

            binding.ivShow.requestLayout();
            binding.ivShow.setLayoutParams(params);
*/
            binding.videoPlayer.setVisibility(GONE);
            binding.ivCross.setVisibility(postItem.is_premium == true ? VISIBLE : GONE);
            if (userItem != null && userItem.isSubscribed) {
                binding.ivCross.setVisibility(GONE);
            }
            binding.ivShow.setAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
            GlideBinding.bindImage(binding.ivShow, postItem.image_url);
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    private void loadVideo(String videoURL) {
        this.videoUrl = videoURL;
        GlideApp.with(this)
                .load(videoURL)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.icThumb);

        binding.icThumb.setVisibility(VISIBLE);
        binding.videoPlayer.setVisibility(VISIBLE);
        binding.videoPlayer.setShutterBackgroundColor(ContextCompat.getColor(this, R.color.yellow_50));
        binding.videoPlayer.setDefaultArtwork(ContextCompat.getDrawable(this, R.drawable.spaceholder));
        binding.videoPlayer.setUseController(false);
        binding.videoPlayer.setControllerHideOnTouch(true);
        binding.videoPlayer.setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS);

        MediaItem mediaItem = MediaItem.fromUri(Uri.parse(videoURL));
        absPlayerInternal.setMediaItem(mediaItem);
        absPlayerInternal.prepare();
        absPlayerInternal.setPlayWhenReady(true); // start loading video and play it at the moment a chunk of it is available offline
        absPlayerInternal.play();

        binding.videoPlayer.setPlayer(absPlayerInternal);

        absPlayerInternal.addListener(new Player.Listener() {
            @Override
            public void onEvents(Player player, Player.Events events) {
                Player.Listener.super.onEvents(player, events);

            }

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                Player.Listener.super.onPlaybackStateChanged(playbackState);
                switch (playbackState) {
                    case ExoPlayer.STATE_ENDED:
                        binding.ivPlayVideo.setVisibility(View.VISIBLE);
                        binding.icThumb.setVisibility(View.INVISIBLE);
                        absPlayerInternal.seekTo(0);
                        absPlayerInternal.setPlayWhenReady(true);
                        break;
                    case ExoPlayer.STATE_READY:
                        binding.icThumb.setVisibility(View.INVISIBLE);
                        binding.videoPlayer.setVisibility(VISIBLE);
                        binding.ivPlayVideo.setVisibility(View.INVISIBLE);


                }
            }

            @Override
            public void onPlayWhenReadyChanged(boolean playWhenReady, int reason) {
                Player.Listener.super.onPlayWhenReadyChanged(playWhenReady, reason);
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                Player.Listener.super.onIsPlayingChanged(isPlaying);
            }

            @Override
            public void onPlayerError(PlaybackException error) {
                Player.Listener.super.onPlayerError(error);
            }
        });


    }

    private void setShimmerEffect() {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) binding.place.imageView2.getLayoutParams();
        lp.width = screenWidth;
        lp.height = screenWidth;

        binding.place.imageView2.setLayoutParams(lp);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (absPlayerInternal != null) {
            absPlayerInternal.setPlayWhenReady(false);
            absPlayerInternal.stop();
            absPlayerInternal.seekTo(4);

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (absPlayerInternal != null) {
            absPlayerInternal.setPlayWhenReady(false);
            absPlayerInternal.stop();
            absPlayerInternal.seekTo(4);
        }
    }

    public void posterProcess(PostItem postItem, BusinessItem businessItem, int position) {
        this.ratio = postItem.aspectRatio;
        prgDialog.show();
        try {
            JSONObject json = new JSONObject(postItem.json);
            layers = json.getJSONArray("layers");

            stickerInfoArrayList = new ArrayList<>();
            textInfoArrayList = new ArrayList<>();

            stickerInfoArrayList.clear();
            textInfoArrayList.clear();

            for (int i = 0; i < layers.length(); i++) {

                JSONObject jsonObject1 = layers.getJSONObject(i);
                processJson(i, jsonObject1, postItem.zipName);

            }

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                prgDialog.dismiss();
                Intent intent = new Intent(DetailActivity.this, PosterActivity.class);
                intent.putExtra(Constant.INTENT_POST_ITEM, postItem);
                intent.putExtra(Constant.INTENT_BUSINESS_ITEM, businessItem);
                intent.putExtra(Constant.INTENT_POS, position);
                intent.putExtra(Constant.INTENT_TYPE, postItem.type);
                intent.putExtra("BACKGROUND", background);
                intent.putExtra("STR_ARRAY", (Serializable) stickerInfoArrayList);
                intent.putExtra("TXT_ARRAY", (Serializable) textInfoArrayList);
                startActivity(intent);
            }, 1000);


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void processJson(int i, JSONObject jsonObject1, String name) throws JSONException {

        String type = jsonObject1.getString("type");
        String lName = jsonObject1.getString("name");
        String width = jsonObject1.getString("width");
        String height = jsonObject1.getString("height");

        String x = jsonObject1.getString("x");
        String y = jsonObject1.getString("y");

        realX = x;
        realY = y;

        calcWidth = "";
        calcHeight = "";

        float templateRealWidth = Float.parseFloat(ratio.split(":")[0]);
        float templateRealHeight = Float.parseFloat(ratio.split(":")[1]);

        if (bgObj != null) {
            templateRealWidth = Float.parseFloat(bgObj.getString("width"));
            templateRealHeight = Float.parseFloat(bgObj.getString("height"));
        }

        if (i == 0) {
            calcWidth = width;
            calcHeight = height;
            bgObj = jsonObject1;

            String stickerUrl = "uploads/template/" + name + "" + jsonObject1.getString("src").replace("..", "");

            saveSticker(name, jsonObject1.getString("name"), Config.APP_API_URL + stickerUrl);

            String directory = new StorageUtils(this).getPackageStorageDir("/." + name + "/").getAbsolutePath();

            background = directory + "/" + jsonObject1.getString("name") + ".png";

        } else {

            realX = String.valueOf((Float.parseFloat(x) * 100) / Float.parseFloat(bgObj.getString("width")));
            realY = String.valueOf((Float.parseFloat(y) * 100) / Float.parseFloat(bgObj.getString("height")));

            calcWidth = String.valueOf(Float.parseFloat(width) * 100 / templateRealWidth + Float.parseFloat(realX));
            calcHeight = String.valueOf(Float.parseFloat(height) * 100 / templateRealHeight + Float.parseFloat(realY));

        }

        if (type != null) {

            if (type.contains("image")) {


                String stickerUrl = "uploads/template/" + name + "" + jsonObject1.getString("src").replace("..", "");

                if (MyApplication.prefManager().getString(Constant.DIGITAL_ENABLE).equals(Config.ONE)) {
                    String str = saveSticker(name, jsonObject1.getString("name"), MyApplication.prefManager().getString(Constant.DIGITAL_END_URL) + stickerUrl);
                } else {
                    String str = saveSticker(name, jsonObject1.getString("name"), Config.APP_API_URL + stickerUrl);
                }

                String directory = new StorageUtils(this).getPackageStorageDir("/." + name + "/").getAbsolutePath();

                String stickerPath = directory + "/" + jsonObject1.getString("name") + ".png";

                String rotation = jsonObject1.has("rotation") ? jsonObject1.getString("rotation") : "0";

                // Bg
                if (i == 0) {

//                    bgUrl = stickerUrl;

                } else {
                    Sticker_info info = new Sticker_info();
                    info.setName(lName);
                    info.setSticker_id(String.valueOf(i));
                    info.setSt_image(stickerPath);
                    info.setSt_order(String.valueOf(i));
                    info.setSt_height(calcHeight);
                    info.setSt_width(calcWidth);
                    info.setSt_x_pos(realX);
                    info.setSt_y_pos(realY);
                    info.setSt_rotation(rotation);
                    stickerInfoArrayList.add(info);
                }
            } else if (type.contains("text")) {

                String color = jsonObject1.getString("color");
                String font = jsonObject1.getString("font");

                String layerName = jsonObject1.getString("name");
                String text = jsonObject1.getString("text");

                String size = jsonObject1.getString("size");

                if (!jsonObject1.has("rotation")) {

                    jsonObject1.put("size", Integer.parseInt(size) + 15);
                    jsonObject1.put("y", Integer.parseInt(y) + 5);
                    y = jsonObject1.getString("y");

                    size = jsonObject1.getString("size");

                    String calSizeHeight = String.valueOf(Float.parseFloat(size) - Float.parseFloat(height)).replace("-", "");

                    String calRealY = String.valueOf(Float.parseFloat(y) - Float.parseFloat(calSizeHeight));

                    realY = String.valueOf((Float.parseFloat(calRealY) * 100) / templateRealHeight);

                    calcHeight = String.valueOf(Float.parseFloat(size) * 100 / templateRealHeight + Float.parseFloat(realY));
                }

                String directory = new StorageUtils(this).getPackageStorageDir("/." + "font" + "/").getAbsolutePath();

                File file = new File(directory + "/" + font + ".ttf");
                com.sgdigitalposter.app.utils.Util.showLog("FILE: " + file.exists() + " FILE : " + file.getName());
                if (!file.exists()) {
                    file = new File(directory + "/" + font + ".TTF");
                    if (!file.exists()) {
                        file = new File(directory + "/" + font + ".otf");
                        if (!file.exists()) {
                            LoadFonts loadFonts = new LoadFonts(name, directory, font);
                            loadFonts.execute();
                        }
                    }
                } else {

                }

                String rotation = "0";
                if (jsonObject1.has("rotation")) {
                    rotation = jsonObject1.getString("rotation");
                }

                String justification = "";
                if (jsonObject1.has("justification")) {
                    justification = jsonObject1.getString("justification");
                }

                String upperCase = "0";
                if (jsonObject1.has("uppercase")) {
                    boolean upperCaseb = jsonObject1.getBoolean("uppercase");
                    upperCase = upperCaseb ? "1" : "0";
                }
                int lineSize = 0;
                String lineColor = "0xffffff";
                int lineOpacity = 100;
                if (jsonObject1.has("effects")) {
                    JSONObject effect = jsonObject1.getJSONObject("effects");
                    if (effect.has("frameFX")) {
                        if (effect.getJSONObject("frameFX").has("lineSize")) {
                            lineSize = effect.getJSONObject("frameFX").getInt("lineSize");
                        }
                        if (effect.getJSONObject("frameFX").has("color")) {
                            lineColor = effect.getJSONObject("frameFX").getString("color");
                        }
                        if (effect.getJSONObject("frameFX").has("alpha")) {
                            lineOpacity = effect.getJSONObject("frameFX").getInt("alpha");
                        }
                    }
                }

                int distance = -11111;
                int angle = 0;
                int blurX = 0;
                int alpha = 100;
                String colorSD = "0xffffff";

                if (jsonObject1.has("effects")) {
                    JSONObject effect = jsonObject1.getJSONObject("effects");
                    if (effect.has("dropShadow")) {
                        if (effect.getJSONObject("dropShadow").has("distance")) {
                            distance = effect.getJSONObject("dropShadow").getInt("distance");
                        }
                        if (effect.getJSONObject("dropShadow").has("color")) {
                            colorSD = effect.getJSONObject("dropShadow").getString("color");
                        }
                        if (effect.getJSONObject("dropShadow").has("angle")) {
                            angle = effect.getJSONObject("dropShadow").getInt("angle");
                        }
                        if (effect.getJSONObject("dropShadow").has("alpha")) {
                            alpha = effect.getJSONObject("dropShadow").getInt("alpha");
                        }
                        if (effect.getJSONObject("dropShadow").has("blurX")) {
                            blurX = effect.getJSONObject("dropShadow").getInt("blurX");
                        }
                    }
                }

                TextSTRInfo textInfo = new TextSTRInfo();

                textInfo.setText_id(String.valueOf(i));
                textInfo.setName(lName);
                textInfo.setText(text);
                textInfo.setTxt_height(calcHeight);
                textInfo.setTxt_width(calcWidth);
                textInfo.setTxt_x_pos(realX);
                textInfo.setJustification(justification);
                textInfo.setTxt_y_pos(realY);
                textInfo.setUppercase(upperCase);
                textInfo.setTxt_rotation(rotation);
                textInfo.setTxt_color(color.replace("0x", "#"));
                textInfo.setTxt_order("" + i);
                textInfo.setFont_family(font);
                textInfo.setLineSize(lineSize);
                textInfo.setLineColor(lineColor.replace("0x", "#"));
                textInfo.setLineOpacity(lineOpacity);
                textInfo.setSdDistance(distance);
                textInfo.setSdAngle(angle);
                textInfo.setSdBlur(blurX);
                textInfo.setSdOpacity(alpha);
                textInfo.setSdColor(colorSD.replace("0x", "#"));
                textInfoArrayList.add(textInfo);

            }

        }

        if (i == layers.length()) {
            prgDialog.dismiss();
        }

    }

    private String saveSticker(String templateName, String name, String stickerUrl) {
        String directory = new StorageUtils(this).getPackageStorageDir("/." + templateName + "/").getAbsolutePath();

        File file = new File(directory + "/" + name + ".png");
        if (!file.exists()) {
            LoadLogo loadLogo = new LoadLogo(stickerUrl, directory, name);
            loadLogo.execute();
            return file.getAbsolutePath();
        } else {
            return file.getAbsolutePath();
        }
    }

    public void LoadRewardAds() {
        if (prefManager.getBoolean(Constant.AVL_REWARD) && MyApplication.prefManager().getBoolean(Constant.REWARD_AD_ENABLE) && MyApplication.prefManager().getBoolean(Constant.ADS_ENABLE) && !Constant.IS_SUBSCRIBED) {
            switch (MyApplication.prefManager().getString(Constant.AD_NETWORK)) {
                case Constant.ADMOB:
                    AdRequest.Builder builder = new AdRequest.Builder();
                    int request = GDPRChecker.getStatus();
                    if (request == ConsentInformation.ConsentStatus.NOT_REQUIRED) {
                        // load non Personalized ads
                        Bundle extras = new Bundle();
                        extras.putString("npa", "1");
                        builder.addNetworkExtrasBundle(AdMobAdapter.class, extras);
                    } // else do nothing , it will load PERSONALIZED ads
                    RewardedAd.load(this, MyApplication.prefManager().getString(Constant.REWARD_AD_ID), builder.build(), new RewardedAdLoadCallback() {
                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            // Handle the error.
                            com.sgdigitalposter.app.utils.Util.showLog("Load Error" + loadAdError.toString());
                            rewardedAd = null;

                        }

                        @Override
                        public void onAdLoaded(@NonNull RewardedAd ad) {
                            rewardedAd = ad;
                        }
                    });
                    break;
                case Constant.FACEBOOK:

                    break;
                case Constant.UNITY:

                    break;
            }
        } else {
            rewardedAd = null;
        }
    }

    public void showAd() {
        if (rewardedAd != null) {
            rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdClicked() {
                    // Called when a click is recorded for an ad.
                }

                @Override
                public void onAdDismissedFullScreenContent() {
                    // Called when ad is dismissed.
                    // Set the ad reference to null so you don't show the ad a second time.
                    rewardedAd = null;
                    onRewardAdDismissed();
                }

                @Override
                public void onAdFailedToShowFullScreenContent(AdError adError) {
                    com.sgdigitalposter.app.utils.Util.showLog("Ad failed to show");
                    // Called when ad fails to show.
                    rewardedAd = null;
                }

                @Override
                public void onAdImpression() {
                    // Called when an impression is recorded for an ad.
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    // Called when ad is shown.
                }
            });

            if (rewardedAd != null) {
                rewardedAd.show(this, new OnUserEarnedRewardListener() {
                    @Override
                    public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                        com.sgdigitalposter.app.utils.Util.showLog("REWARDED: " + rewardItem);
                    }
                });
            }
        }
    }

    public boolean isLoaded() {
        return rewardedAd == null ? false : true;
    }

    public void onRewardAdDismissed() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (prefManager.getInt(CURRENT_REWARD) >= Integer.valueOf(prefManager.getString(REWARD_AD_LIMIT))) {
                    prefManager.setBoolean(AVL_REWARD, false);
                } else {
                    prefManager.setBoolean(AVL_REWARD, true);
                }
                com.sgdigitalposter.app.utils.Util.showLog("Success");
                Constant.REWARD_GRANTED = true;
                if (absPlayerInternal != null) {
                    absPlayerInternal.setPlayWhenReady(false);
                    absPlayerInternal.stop();
                    absPlayerInternal.seekTo(4);
                }
                Constant.isCustom11Mode = false;
                Constant.isCustom45Mode = false;

                if (postItem.type.equals(Constant.CUSTOM_EDITABLE)) {
                    posterProcess(postItem, businessItem, position);
                } else {
                    Intent intent = new Intent(DetailActivity.this, PosterActivity.class);
                    intent.putExtra(Constant.INTENT_POST_ITEM, postItem);
                    intent.putExtra(Constant.INTENT_BUSINESS_ITEM, businessItem);
                    intent.putExtra(Constant.INTENT_POS, position);
                    intent.putExtra(Constant.INTENT_TYPE, postItem.type);
                    startActivity(intent);
                }

            }
        });
    }

    private class LoadLogo extends AsyncTask<String, String, String> {

        private String urls, directory, name;
        Drawable drawable;
        Bitmap bitmap2;

        public LoadLogo(String urls, String directory, String name) {
            this.urls = urls;
            this.directory = directory;
            this.name = name;
        }

        @Override
        protected String doInBackground(String... strings) {
            com.sgdigitalposter.app.utils.Util.showLog("URL: " + urls);
            bitmap2 = null;
            InputStream inputStream;
            try {
                inputStream = new java.net.URL(urls).openStream();
                bitmap2 = BitmapFactory.decodeStream(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }

            drawable = new BitmapDrawable(Resources.getSystem(), bitmap2);
            return "0";
        }

        @SuppressLint("WrongThread")
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            File file = new File(directory);

            if (!file.exists()) {
                file.mkdirs();
            }
            try {
                String filePath = directory + "/" + name + ".png";
                FileOutputStream fileOutputStream = new FileOutputStream(filePath);
                bitmap2.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                fileOutputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
                com.sgdigitalposter.app.utils.Util.showErrorLog(e.getMessage(), e);
            }
        }
    }

    class LoadFonts extends AsyncTask<String, String, String> {

        private String message = "", verifyStatus = "0", zipName, directory, name;
        InputStream inputStream;

        public LoadFonts(String zipName, String directory, String name) {
            this.zipName = zipName;
            this.directory = directory;
            this.name = name;
        }

        @Override
        protected String doInBackground(String... strings) {


            String font_url = getFontUrl(zipName, name, ".ttf");

            com.sgdigitalposter.app.utils.Util.showLog("URL: " + font_url);

            File file = new File(directory);

            if (!file.exists()) {
                file.mkdirs();
            }
            try {
                inputStream = new java.net.URL(font_url).openStream();
                FileOutputStream fileOutputStream = new FileOutputStream(directory + "/" + name + ".ttf");
                int length;
                byte[] buffer = new byte[1024];
                while ((length = inputStream.read(buffer)) > -1) {
                    fileOutputStream.write(buffer, 0, length);
                }
                fileOutputStream.close();
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    inputStream = new java.net.URL(getFontUrl(zipName, name, ".TTF")).openStream();
                    FileOutputStream fileOutputStream = new FileOutputStream(directory + "/" + name + ".TTF");
                    int length;
                    byte[] buffer = new byte[1024];
                    while ((length = inputStream.read(buffer)) > -1) {
                        fileOutputStream.write(buffer, 0, length);
                    }
                    fileOutputStream.close();
                    inputStream.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                    try {
                        inputStream = new java.net.URL(getFontUrl(zipName, name, ".otf")).openStream();
                        FileOutputStream fileOutputStream = new FileOutputStream(directory + "/" + name + ".otf");
                        int length;
                        byte[] buffer = new byte[1024];
                        while ((length = inputStream.read(buffer)) > -1) {
                            fileOutputStream.write(buffer, 0, length);
                        }
                        fileOutputStream.close();
                        inputStream.close();
                    } catch (IOException e3) {
                        e3.printStackTrace();
                        prgDialog.dismiss();
                    }
                }
            }

            return "0";
        }

        @SuppressLint("WrongThread")
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }

    private String getFontUrl(String mZipName, String mName, String ext) {
        String font_url = "";
        if (prefManager.getString(Constant.DIGITAL_ENABLE).equals(Config.ONE)) {
            font_url = prefManager.getString(Constant.DIGITAL_END_URL) + "uploads/template/" + mZipName + "/fonts/" + mName + ext;
        } else {
            font_url = Config.APP_API_URL + "uploads/template/" + mZipName + "/fonts/" + mName + ext;
        }
        return font_url;
    }

}