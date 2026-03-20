package com.sgdigitalposter.app.editor;

import static android.view.View.DRAWING_CACHE_QUALITY_HIGH;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.sgdigitalposter.app.utils.Constant.AVL_REWARD;
import static com.sgdigitalposter.app.utils.Constant.CURRENT_REWARD;
import static com.sgdigitalposter.app.utils.Constant.REWARD_AD_LIMIT;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.ColorUtils;
import androidx.core.graphics.Insets;
import androidx.core.util.Pair;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.DefaultRenderersFactory;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.ReturnCode;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.ump.ConsentInformation;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sgdigitalposter.app.Ads.GDPRChecker;
import com.sgdigitalposter.app.Ads.InterstitialAdManager;
import com.sgdigitalposter.app.BuildConfig;
import com.sgdigitalposter.app.Config;
import com.sgdigitalposter.app.MyApplication;
import com.sgdigitalposter.app.R;
import com.sgdigitalposter.app.adapters.ColorsAdapter;
import com.sgdigitalposter.app.adapters.DynamicFrameAdapter;
import com.sgdigitalposter.app.adapters.FontAdapter;
import com.sgdigitalposter.app.adapters.GradientAdapter;
import com.sgdigitalposter.app.adapters.LayersAdapter;
import com.sgdigitalposter.app.adapters.NewDragDrop;
import com.sgdigitalposter.app.adapters.RecyclerBorderAdapter;
import com.sgdigitalposter.app.adapters.RecyclerOverLayAdapter;
import com.sgdigitalposter.app.adapters.StickerAdapter;
import com.sgdigitalposter.app.adapters.StickerCategoryAdapter;
import com.sgdigitalposter.app.adapters.StickerViewPagerAdapter;
import com.sgdigitalposter.app.api.common.common.Status;
import com.sgdigitalposter.app.bg_remove.BGConfig;
import com.sgdigitalposter.app.bg_remove.MLCropAsyncTask;
import com.sgdigitalposter.app.bg_remove.MLOnCropTaskCompleted;
import com.sgdigitalposter.app.binding.GlideBinding;
import com.sgdigitalposter.app.databinding.ActivityPosterBinding;
import com.sgdigitalposter.app.items.BusinessItem;
import com.sgdigitalposter.app.items.DynamicFrameItem;
import com.sgdigitalposter.app.items.FrameCategoryItem;
import com.sgdigitalposter.app.items.ItemGradient;
import com.sgdigitalposter.app.items.PostItem;
import com.sgdigitalposter.app.items.UserFrame;
import com.sgdigitalposter.app.items.UserItem;
import com.sgdigitalposter.app.listener.ClickListener;
import com.sgdigitalposter.app.listener.StartDragListener;
import com.sgdigitalposter.app.ui.activities.EraserActivity;
import com.sgdigitalposter.app.ui.activities.SubsPlanActivity;
import com.sgdigitalposter.app.ui.dialog.DialogMsg;
import com.sgdigitalposter.app.ui.stickers.ElementInfo;
import com.sgdigitalposter.app.ui.stickers.RelStickerView;
import com.sgdigitalposter.app.ui.stickers.Sticker_info;
import com.sgdigitalposter.app.ui.stickers.TextInfo;
import com.sgdigitalposter.app.ui.stickers.ViewIdGenerator;
import com.sgdigitalposter.app.ui.stickers.text.AutofitTextRel;
import com.sgdigitalposter.app.ui.stickers.text.TextSTRInfo;
import com.sgdigitalposter.app.ui.view.AutoFitEditText;
import com.sgdigitalposter.app.utils.Constant;
import com.sgdigitalposter.app.utils.ImageUtils;
import com.sgdigitalposter.app.utils.PrefManager;
import com.sgdigitalposter.app.utils.RepeatListener;
import com.sgdigitalposter.app.utils.ShapesImage;
import com.sgdigitalposter.app.utils.StorageUtils;
import com.sgdigitalposter.app.utils.Util;
import com.sgdigitalposter.app.viewmodel.PostViewModel;
import com.sgdigitalposter.app.viewmodel.UserViewModel;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.woxthebox.draglistview.DragItem;
import com.woxthebox.draglistview.DragListView;
import com.yalantis.ucrop.UCrop;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class PosterActivity extends AppCompatActivity implements RelStickerView.TouchEventListener, AutofitTextRel.TouchEventListener, InterstitialAdManager.Listener, RecyclerBorderAdapter.OnBorderSelected, RecyclerOverLayAdapter.OnOverlaySelected {

    ActivityPosterBinding binding;
    private Activity activity;
    private float screenWidth, screenHeight;

    private String ratio;
    private ProgressDialog prgDialog;
    ShapesImage selectedShapesImage;
    private PostItem intentPostItem;
    private BusinessItem businessItem;
    private Bitmap bit;
    private float wr = 1.0f;
    private float hr = 1.0f;
    RecyclerBorderAdapter adaptorBorder;
    RecyclerOverLayAdapter adaptorOverlay;
    private DynamicFrameAdapter frameAdapter;
    RelativeLayout shapeRel;
    public String realX;
    public String realY;
    public String calcWidth = "";
    public String calcHeight = "";
    public JSONObject bgObj;
    private ArrayList<String> fontUrls = new ArrayList<>();
    private ArrayList<String> fontNames = new ArrayList<>();
    List<DynamicFrameItem> frameItemList = new ArrayList<>();
    List<DynamicFrameItem> ori_frameItemList = new ArrayList<>();
    List<UserFrame> userFrames = new ArrayList<>();
    public List<Sticker_info> stickerInfoArrayList, strPostList;
    public List<TextSTRInfo> textInfoArrayList, textPostList;
    private HashMap<Integer, Object> txtShapeList = new HashMap<>();
    int sizeFull = 0;

    String fontName = "";
    int tAlpha = 100;
    int tColor = -1;
    int bgAlpha = 0;
    int bgColor = ViewCompat.MEASURED_STATE_MASK;
    String bgDrawable = "0";
    int outerColor = 0;
    int outerSize = 0;
    int leftRightShadow = 0;
    int topBottomShadow = 0;
    int shadowColor = ViewCompat.MEASURED_STATE_MASK;
    int shadowProg = 0;
    float rotation = 0.0f;

    boolean checkTouchContinue = false;
    Handler mHandler;
    int mInterval = 50;
    Runnable mStatusChecker;
    View focusedView;
    private Animation animSlideDown, animSlideUp;

    Map<String, View> viewMap = new HashMap<>();
    Map<String, View> linViewMap = new HashMap<>();

    TextInfo textInfo;
    ElementInfo stickerInfo;

    private int CURRENT_ID;
    FontAdapter fontAdapter;
    ColorsAdapter colorsAdapter;
    ColorsAdapter patternAdapter;
    GradientAdapter gradientAdapter;

    private String[] directionsMenu;
    private String gradientType;
    private String gradientDirection;
    private boolean isTextBG = false;
    private boolean isEditMode = false;

    private boolean frameLoaded = false;

    private String[] PERMISSIONS = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};

    Uri uri;
    private static final int SELECT_PICTURE_CAMERA = 805;
    private static final int SELECT_PICTURE_GALLERY = 807;
    private boolean isChangeMode = false;
    ArrayList<Integer> selectedRatio = new ArrayList<Integer>();
    LayersAdapter layersAdapter;
    public ArrayList<Pair<Long, View>> mItemArray;

    ExoPlayer absPlayerInternal;
    ExoPlayer sharePlayer;

    boolean isVideo = false;
    ProgressDialog progress, progressDD, progressLoading;
    String VideoPath;
    String FOLDER_NAME = "";
    int alpha = 80;
    int transAlpha = 80;

    StickerCategoryAdapter stickerCategoryAdapter;
    StickerViewPagerAdapter strAdapter;

    String fileName = "";
    boolean isImage = false;
    UserItem userItem;
    PrefManager prefManager;
    DialogMsg dialogMsg;

    private PostViewModel postViewModel;
    UserViewModel userViewModel;

    public static Bitmap eraserResultBmp;
    public Bitmap selectedBit, cutBit;

    int totalMemory = 0;

    int permissionsCount = 0;

    int CURRENT_CLICK = 0;

    String frameCategory = "All";
    String background = "";

    String strPathForThumbNail;

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
                                } else if (!hasPermission(PosterActivity.this, PERMISSIONS[i])) {
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
                                //All permissions granted. Do your stuff 🤞
                                Util.showLog("All permissions granted. Do your stuff \uD83E\uDD1E");
                                if (CURRENT_CLICK == 1) {
                                    GrandSave();
                                }
                                if (CURRENT_CLICK == 2) {
                                    onGalleryButtonClick();
                                }
                                if (CURRENT_CLICK == 3) {
                                    onCameraButtonClick();
                                }
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


    private float getNewHeight(int i, int i2, float f, float f2) {
        return (((float) i2) * f) / ((float) i);
    }

    private float getNewWidth(int i, int i2, float f, float f2) {
        return (((float) i) * f2) / ((float) i2);
    }

    public int getNewSTRWidth(float f, float f2) {
        return (int) ((((float) binding.mainRel.getWidth()) * (f2 - f)) / 100.0f);
    }

    public int getNewSTRHeight(float f, float f2) {
        return (int) ((((float) binding.mainRel.getHeight()) * (f2 - f)) / 100.0f);
    }

    public float getXpos(float f) {
        return (((float) binding.mainRel.getWidth()) * f) / 100.0f;
    }

    public float getYpos(float f) {
        return (((float) binding.mainRel.getHeight()) * f) / 100.0f;
    }

    RewardedAd rewardedAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPosterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Util.applyStatusBarPadding(binding.rlMain);

//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

//        Log.e("SB", "onCreate PosterActivity := ");
        activity = this;

//        Util.showLog("BackGround : " +  getIntent().getStringExtra("BACKGROUND"));
//        Util.showLog("StrPostList : " + (List<Sticker_info>) getIntent().getSerializableExtra("STR_ARRAY"));
//        Util.showLog("TextPostList : " + (List<TextSTRInfo>) getIntent().getSerializableExtra("TXT_ARRAY"));
//        Util.showLog("businessItem : " + (BusinessItem) getIntent().getSerializableExtra(Constant.INTENT_BUSINESS_ITEM));
//        Util.showLog("IntentPostItem : " + (PostItem) getIntent().getSerializableExtra(Constant.INTENT_POST_ITEM));

        prefManager = new PrefManager(this);
        if (prefManager.getString(Constant.FOLDER_NAME).equals("")) {
            FOLDER_NAME = "video_function";
            prefManager.setString(Constant.FOLDER_NAME, FOLDER_NAME);
        } else {
            FOLDER_NAME = prefManager.getString(Constant.FOLDER_NAME);
        }

        InterstitialAdManager.Interstitial(PosterActivity.this, this);
        if (rewardedAd == null) {
            LoadRewardAds();
        }

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = (float) (displayMetrics.widthPixels - ImageUtils.dpToPx(this, 30));
        screenHeight = (float) (displayMetrics.heightPixels - ImageUtils.dpToPx(this, 105));


        stickerInfoArrayList = new ArrayList<>();
        textInfoArrayList = new ArrayList<>();
        mItemArray = new ArrayList<>();

        dialogMsg = new DialogMsg(this, false);
        prgDialog = new ProgressDialog(this);
        progress = new ProgressDialog(this);
        progressDD = new ProgressDialog(this);
        prgDialog.setTitle("Please Wait");
        prgDialog.setMessage("Resource Downloading....");
        prgDialog.setCancelable(false);
        initBorderRecycler();
        initOverlayRecycler();

        if (android.os.Build.VERSION.SDK_INT > 31) {
            PERMISSIONS = new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES};
        }

        if (getIntent().getExtras() != null && Constant.isCustom45Mode == false && Constant.isCustom11Mode == false) {
            intentPostItem = (PostItem) getIntent().getSerializableExtra(Constant.INTENT_POST_ITEM);
            Util.showLog("IntentPostItem : " + intentPostItem);
            businessItem = (BusinessItem) getIntent().getSerializableExtra(Constant.INTENT_BUSINESS_ITEM);
            Util.showLog("businessItem : " + businessItem);
            if (intentPostItem.is_video) {
                ratio = "1:1";
            } else {
                ratio = intentPostItem.aspectRatio;
                Log.e("SB", "onCreate: " + intentPostItem.aspectRatio);
                Log.e("SB", "onCreate : " + ratio);

            }
            if (getIntent().getStringExtra(Constant.INTENT_TYPE).equals(Constant.CUSTOM)) {
                isImage = true;
            } else if (getIntent().getStringExtra(Constant.INTENT_TYPE).equals(Constant.CUSTOM_EDITABLE)) {
                background = getIntent().getStringExtra("BACKGROUND");
                strPostList = (List<Sticker_info>) getIntent().getSerializableExtra("STR_ARRAY");
                textPostList = (List<TextSTRInfo>) getIntent().getSerializableExtra("TXT_ARRAY");

                Util.showLog("BackGround : " + background);
                Util.showLog("StrPostList : " + strPostList);
                Util.showLog("TextPostList : " + textPostList);
            }
            setBackground(background != null ? background : "", intentPostItem, intentPostItem.aspectRatio);
        } else {
            businessItem = (BusinessItem) getIntent().getSerializableExtra(Constant.INTENT_BUSINESS_ITEM);
            if (Constant.isCustom11Mode) {
                Drawable drawable = getDrawable(R.drawable.poster_4_5);
                Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                ratio = "1:1";
                bitmapRatio("1:1", bitmap);
            }
            if (Constant.isCustom45Mode) {
                Drawable drawable = getDrawable(R.drawable.poster_4_5);
                Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                ratio = "4:5";
                bitmapRatio("4:5", bitmap);
            }
        }

        postViewModel = new ViewModelProvider(this).get(PostViewModel.class);

        mHandler = new Handler();

        mStatusChecker = new Runnable() {
            public void run() {
                mHandler.postDelayed(this, mInterval);
            }
        };

        setViews();
        animSlideUp = Util.getAnimUp(this);
        animSlideDown = Util.getAnimDown(this);
        setUpViewModel();
        loadSticker();
        totalMemory = Util.getTotalMemory(this);

    }

    private void setUpViewModel() {
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        userViewModel.getDbUserData(prefManager.getString(Constant.USER_ID)).observe(this, result -> {
            if (result != null) {
                userItem = result.user;
                Gson gson = new Gson();
                gson.toJson(userItem);
//                Log.e("SB", "setUpViewModel: "+userItem.country );
//                Log.e("SB", "setUpViewModel: "+ gson.toJson(userItem));
            }
        });
    }

    @Override
    public void onBackPressed() {
        removeView();
        DialogMsg dialogMsg = new DialogMsg(this, false);
        dialogMsg.showConfirmDialog(getString(R.string.menu_exit), getString(R.string.do_you_want_to_exit), getString(R.string.yes), getString(R.string.no));
        dialogMsg.show();
        dialogMsg.okBtn.setOnClickListener(v -> {
            Constant.REWARD_GRANTED = false;
            dialogMsg.cancel();
            super.onBackPressed();
            finish();
        });
    }

    private void setViews() {
        viewMap.put("CG_IMAGE", binding.cgImage);
        viewMap.put("CG_ADJUST", binding.cgAdjust);
        viewMap.put("CG_3D", binding.cg3DRotation);
        viewMap.put("CG_TEXT", binding.cg);
        viewMap.put("CG_POST_BG", binding.cgPosterBackground);
        viewMap.put("CG_TEXT_COLOR", binding.cgTextColor);
        viewMap.put("CG_BG", binding.cgBackground);
        viewMap.put("CG_STROKE", binding.cgStroke);
        viewMap.put("CG_SHADOW", binding.cgShadow);

        linViewMap.put("LL_EDIT", binding.llEdit);
        linViewMap.put("LL_TV_EDIT", binding.llTvEdit);
        linViewMap.put("LL_FLIP", binding.llFlip);
        linViewMap.put("LL_CROP", binding.llCrop);
        linViewMap.put("RV_FONT", binding.rvFonts);
        linViewMap.put("SB", binding.sb);
        linViewMap.put("LL_SHADOW_A", binding.llShadowAngle);
        linViewMap.put("LL_STROKE", binding.llStrokeStyle);
        linViewMap.put("LL_FRAME", binding.rlFrame);
        linViewMap.put("HS_STYLE", binding.hsTVStyle);
        linViewMap.put("LL_GRADIENT", binding.llGradient);
        linViewMap.put("LL_TXT_SIZE", binding.tvSize);

        directionsMenu = new String[]{"Linear", "Radial", "Sweep"};

        gradientType = directionsMenu[0];
        gradientDirection = "Horizontal";

        binding.tvAddText.setOnClickListener(v -> {
            binding.ll.setVisibility(GONE);
            binding.rlSticker.setVisibility(GONE);
            binding.rlBusiness.setVisibility(GONE);
            binding.llBorder.setVisibility(GONE);
            binding.layEffects.setVisibility(GONE);
            isEditMode = false;
            addTextDialog(null);
        });

        binding.tvAspectCrop.setOnClickListener(v -> {
            isChangeMode = true;

            RelStickerView relStickerView = (RelStickerView) getCurrentSTR_info();

            Uri fromFile = Uri.fromFile(new File(getCacheDir(),
                    "SampleCropImage" + System.currentTimeMillis() + ".png"));
            UCrop.Options options2 = new UCrop.Options();
            options2.setToolbarColor(getResources().getColor(R.color.primary_dark_color));
            options2.setStatusBarColor(getResources().getColor(R.color.primary_dark_color));
            options2.setToolbarWidgetColor(getResources().getColor(R.color.white));
            options2.setActiveControlsWidgetColor(getResources().getColor(R.color.primary_color));
            options2.setToolbarTitle(getString(R.string.ucrop_label_edit_photo));
            options2.setFreeStyleCropEnabled(true);
            UCrop.of(Uri.fromFile(new File(saveBitmap(relStickerView.getMainImageBitmap()))),
                    fromFile).withOptions(options2).start(PosterActivity.this);
        });

        binding.tvErase.setOnClickListener(v -> {
            isChangeMode = true;
            RelStickerView view = (RelStickerView) getCurrentSTR_info();
            EraserActivity.b = view.getMainImageBitmap();
            Intent intent = new Intent(PosterActivity.this, EraserActivity.class);
            intent.putExtra(Constant.KEY_OPEN_FROM, Constant.OPEN_FROM_POSTER);
            startActivityForResult(intent, 1024);
        });

        binding.tvAddSticker.setOnClickListener(v -> {
            binding.ll.setVisibility(GONE);
            binding.rlSticker.setVisibility(VISIBLE);
            binding.rlBusiness.setVisibility(GONE);
            binding.llBorder.setVisibility(GONE);
            binding.layEffects.setVisibility(GONE);
        });

        binding.ivCancel.setOnClickListener(v -> {
            binding.rlSticker.setVisibility(GONE);

        });

        binding.ivMainBack.setOnClickListener(v -> {
            binding.ll.setVisibility(GONE);
            binding.rlSticker.setVisibility(GONE);
            binding.llBorder.setVisibility(GONE);
            binding.layEffects.setVisibility(GONE);
            if (absPlayerInternal != null) {
                absPlayerInternal.setPlayWhenReady(false);
                absPlayerInternal.stop();
                absPlayerInternal.seekTo(0);
            }
            if (sharePlayer != null) {
                sharePlayer.setPlayWhenReady(false);
                sharePlayer.stop();
                sharePlayer.seekTo(0);
            }
            onBackPressed();
        });

        binding.ivCloseFrame.setOnClickListener(v -> {
            binding.ll.setVisibility(GONE);
            binding.rlFrame.setVisibility(GONE);
        });
        binding.ivCloseBorder.setOnClickListener(v -> {
            binding.ll.setVisibility(GONE);
            binding.llBorder.setVisibility(GONE);
        });
        binding.ivCloseEffect.setOnClickListener(v -> {
            binding.ll.setVisibility(GONE);
            binding.layEffects.setVisibility(GONE);
        });
        binding.tvAddBorder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.ll.setVisibility(GONE);


                binding.rlSticker.setVisibility(GONE);
                binding.rlBusiness.setVisibility(GONE);

                binding.llBorder.setVisibility(VISIBLE);
                binding.layEffects.setVisibility(GONE);
                binding.ll.setVisibility(VISIBLE);

            }
        });
        binding.tvAddEffect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.ll.setVisibility(GONE);


                binding.rlSticker.setVisibility(GONE);
                binding.rlBusiness.setVisibility(GONE);

                binding.llBorder.setVisibility(GONE);
                binding.layEffects.setVisibility(VISIBLE);
                binding.ll.setVisibility(VISIBLE);

            }
        });
        binding.seekBorder.setMax(30);
        binding.seekBorder.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                alpha = progress;
                binding.shapeRel.setPadding(progress, progress, progress, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        binding.seek.setMax(255);
        binding.seek.setProgress(80);
        binding.transImg.setImageAlpha(transAlpha);
        binding.seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                transAlpha = progress;
                if (Build.VERSION.SDK_INT >= 16) {
                    binding.transImg.setImageAlpha(transAlpha);

                } else {
                    binding.transImg.setAlpha(transAlpha);

                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        binding.tvAddFame.setOnClickListener(v -> {


            binding.ll.setVisibility(GONE);


            binding.rlSticker.setVisibility(GONE);
            binding.rlBusiness.setVisibility(GONE);
            binding.llBorder.setVisibility(GONE);
            binding.layEffects.setVisibility(GONE);
            if (frameAdapter != null && frameAdapter.getItemCount() > 0) {

                Chip chip2 = (Chip) binding.cgFrameCate.getChildAt(0);
                chip2.setChecked(true);
                if (userFrames.size() > 0) {
                    frameCategory = "All";
                    frameItemList.clear();
                    for (int i = 0; i < ori_frameItemList.size(); i++) {
                        frameItemList.add(ori_frameItemList.get(i));
                    }
                    for (UserFrame frame : userFrames) {
                        frameItemList.add(new DynamicFrameItem("USER", "USER",
                                frame.imageUrl, "USER", true, "custom"));
                    }
                    frameAdapter.setFrameItemList(frameItemList);
                } else {
                    int childCount = binding.cgFrameCate.getChildCount();
                    for (int i = 0; i < childCount; i++) {
                        Chip chip = (Chip) binding.cgFrameCate.getChildAt(i);
                        if (chip.getId() == 9999) {
                            chip.setVisibility(GONE);
                            break;
                        }

                    }
                }

                removeView();
                binding.rlFrame.setVisibility(VISIBLE);
                binding.ll.setVisibility(VISIBLE);
            } else {
                Util.showToast(activity, "No Frame For This Image Size");
            }
        });

        binding.ivCloseBusiness.setOnClickListener(v -> {
            binding.ll.setVisibility(GONE);
            binding.rlBusiness.setVisibility(GONE);
        });
        binding.tvBusiness.setOnClickListener(v -> {
            binding.rlSticker.setVisibility(GONE);
            binding.llBorder.setVisibility(GONE);
            binding.layEffects.setVisibility(GONE);
            getLayoutChild(true);
            if (mItemArray.size() > 0) {

                checkBusiness(binding.cbName, true);
                checkBusiness(binding.cbWebsite, true);
                checkBusiness(binding.cbLogo, true);
                checkBusiness(binding.cbPhone, true);
                checkBusiness(binding.cbAdress, true);
                checkBusiness(binding.cbEmail, true);

                removeView();
                binding.rlBusiness.setVisibility(VISIBLE);
                binding.ll.setVisibility(VISIBLE);
            } else {
                Util.showToast(activity, "No Any Business Item");
            }
        });

        binding.cbName.setOnClickListener(v -> {
            checkBusiness(binding.cbName, false);
        });

        binding.cbAdress.setOnClickListener(v -> {
            checkBusiness(binding.cbAdress, false);
        });

        binding.cbEmail.setOnClickListener(v -> {
            checkBusiness(binding.cbEmail, false);
        });

        binding.cbPhone.setOnClickListener(v -> {
            checkBusiness(binding.cbPhone, false);
        });

        binding.cbLogo.setOnClickListener(v -> {
            checkBusiness(binding.cbLogo, false);
        });

        binding.cbWebsite.setOnClickListener(v -> {
            checkBusiness(binding.cbWebsite, false);
        });

        binding.tvEdit.setOnClickListener(v -> {
            isEditMode = true;
            getCurrentSTR_info();
            addTextDialog(textInfo);
        });

        binding.ivViewOrder.setOnClickListener(v -> {

            if (mItemArray != null) {
                binding.rlSticker.setVisibility(GONE);
                showLayers();
                binding.llLayer.setVisibility(VISIBLE);
            } else {
                Util.showToast(activity, "No Item Available");
            }
        });

        binding.ivUndo.setOnClickListener(v -> {

        });
        binding.ivClose.setOnClickListener(v -> {
            binding.llLayer.setVisibility(GONE);
        });

        binding.ivCopy.setOnClickListener(v -> {
            binding.rlSticker.setVisibility(GONE);
            View view = getCurrentSTR_info();
            if (view != null) {

                if (view instanceof AutofitTextRel) {
                    AutofitTextRel autofitTextRel2 = new AutofitTextRel(activity);
                    binding.txtStkrRel.addView(autofitTextRel2);
                    removeViewControl();
                    autofitTextRel2.setTextInfo(((AutofitTextRel) view).getTextInfo(), false);
                    autofitTextRel2.setId(ViewIdGenerator.generateViewId());
                    autofitTextRel2.setOnTouchCallbackListener(PosterActivity.this);
                    autofitTextRel2.setBorderVisibility(true);
                }

                if (view instanceof RelStickerView) {
                    RelStickerView relStickerView2 = new RelStickerView(activity, false);
                    relStickerView2.setComponentInfo(((RelStickerView) view).getComponentInfo());
                    relStickerView2.setId(ViewIdGenerator.generateViewId());
                    relStickerView2.setAlphaProg(250);
                    relStickerView2.setMainLayoutWH((float) binding.mainRel.getWidth(), (float) binding.mainRel.getHeight());
                    binding.txtStkrRel.addView(relStickerView2);
                    removeViewControl();
                    relStickerView2.setOnTouchCallbackListener(PosterActivity.this);
                    relStickerView2.setBorderVisibility(true);
                }

            } else {
                Util.showToast(activity, "Please Select One Item");
            }
        });

        binding.cg.setOnCheckedStateChangeListener(new ChipGroup.OnCheckedStateChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull ChipGroup group, @NonNull List<Integer> checkedIds) {
                for (Map.Entry<String, View> entry : linViewMap.entrySet()) {
                    entry.getValue().setVisibility(GONE);
                }
                switch (group.getCheckedChipId()) {
                    case R.id.chipEditTv:
                        binding.llTvEdit.setVisibility(VISIBLE);
                        break;
                    case R.id.tvRotation:
                        getCurrentSTR_info();
                        binding.cg.setVisibility(GONE);
                        binding.ivZRotation.setChecked(true);
                        binding.sb.setVisibility(VISIBLE);
                        CURRENT_ID = R.id.ivZRotation;
                        binding.cg3DRotation.setVisibility(VISIBLE);
                        break;
                    case R.id.tvStyle:
                        binding.hsTVStyle.setVisibility(VISIBLE);
                        break;
                    case R.id.tvFont:
                        binding.rvFonts.setAdapter(fontAdapter);
                        binding.rvFonts.setVisibility(VISIBLE);
                        break;
                    case R.id.chipColor:
                        CURRENT_ID = R.id.chipColor;
                        binding.cgTextColor.setVisibility(VISIBLE);
                        binding.tvTextColor.setChecked(true);
                        binding.rvFonts.setVisibility(VISIBLE);
                        binding.cg.setVisibility(GONE);
                        break;
                    case R.id.chipStroke:
                        CURRENT_ID = R.id.chipStroke;
                        binding.cgStroke.setVisibility(VISIBLE);
                        binding.tvStrokeColor.setChecked(true);
                        binding.rvFonts.setVisibility(VISIBLE);
                        binding.cg.setVisibility(GONE);
                        break;
                    case R.id.chipShadow:
                        binding.cgShadow.setVisibility(VISIBLE);
                        binding.tvShadowColor.setChecked(true);
                        binding.cg.setVisibility(GONE);
                        break;
                    case R.id.chipBackground:
                        binding.cgBackground.setVisibility(VISIBLE);
                        binding.tvBgColor.setChecked(true);
                        binding.rvFonts.setAdapter(colorsAdapter);
                        binding.rvFonts.setVisibility(VISIBLE);
                        binding.cg.setVisibility(GONE);
                        break;
                    case R.id.tvOpacity:
                        CURRENT_ID = R.id.tvOpacity;
                        AutofitTextRel autofitTextRel = (AutofitTextRel) getCurrentSTR_info();
                        binding.sb.setProgress(autofitTextRel.getTextAlpha());
                        binding.sb.setVisibility(VISIBLE);
                        break;
                    case R.id.tvLetterSpacing:
                        CURRENT_ID = R.id.tvLetterSpacing;
                        AutofitTextRel autofitTextRel2 = (AutofitTextRel) getCurrentSTR_info();
                        binding.sb.setProgress((int) autofitTextRel2.getLetterSpacing());
                        binding.sb.setVisibility(VISIBLE);
                        break;
                    case R.id.tvLineSpacing:
                        CURRENT_ID = R.id.tvLineSpacing;
                        AutofitTextRel autofitTextRel3 = (AutofitTextRel) getCurrentSTR_info();
                        binding.sb.setProgress((int) autofitTextRel3.getLineSpacing());
                        binding.sb.setVisibility(VISIBLE);
                        break;
                    case R.id.tvTheme:
                        break;
                    case R.id.tvFontSize:
                        AutofitTextRel autofitTextRel4 = (AutofitTextRel) getCurrentSTR_info();
                        binding.tvSizeText.setText("" + autofitTextRel4.getTextSize());
                        Util.showLog("SIZE: " + autofitTextRel4.getTextSize());
                        binding.tvSize.setVisibility(VISIBLE);
                        break;
                }

            }
        });

        binding.cg3DRotation.setOnCheckedStateChangeListener(new ChipGroup.OnCheckedStateChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull ChipGroup group, @NonNull List<Integer> checkedIds) {
                binding.llFlip.setVisibility(GONE);
                View view = getCurrentSTR_info();
                switch (group.getCheckedChipId()) {
                    case R.id.ivZRotation:
                        CURRENT_ID = R.id.ivZRotation;
                        if (view instanceof AutofitTextRel) {
                            binding.sb.setProgress(textInfo.getZRotateProg());
                        }
                        if (view instanceof RelStickerView) {
                            binding.sb.setProgress(stickerInfo.getZRotateProg());
                        }
                        binding.sb.setVisibility(VISIBLE);
                        break;
                    case R.id.ivXRotation:
                        CURRENT_ID = R.id.ivXRotation;
                        if (view instanceof AutofitTextRel) {
                            binding.sb.setProgress(textInfo.getXRotateProg());
                        }
                        if (view instanceof RelStickerView) {
                            binding.sb.setProgress(stickerInfo.getXRotateProg());
                        }
                        binding.sb.setVisibility(VISIBLE);
                        break;
                    case R.id.ivYRotation:
                        CURRENT_ID = R.id.ivYRotation;
                        if (view instanceof AutofitTextRel) {
                            binding.sb.setProgress(textInfo.getYRotateProg());
                        }
                        if (view instanceof RelStickerView) {
                            binding.sb.setProgress(stickerInfo.getYRotateProg());
                        }
                        binding.sb.setVisibility(VISIBLE);
                        break;
                    case R.id.ivFlip:
                        binding.sb.setVisibility(GONE);
                        binding.llFlip.setVisibility(VISIBLE);
                        break;
                }
            }
        });

        binding.cgStroke.setOnCheckedStateChangeListener(new ChipGroup.OnCheckedStateChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull ChipGroup group, @NonNull List<Integer> checkedIds) {
                for (Map.Entry<String, View> entry : linViewMap.entrySet()) {
                    entry.getValue().setVisibility(GONE);
                }
                getCurrentSTR_info();
                switch (group.getCheckedChipId()) {
                    case R.id.tvStrokeOff:
                        binding.cgStroke.setVisibility(GONE);
                        AutofitTextRel autofitTextRel = (AutofitTextRel) getCurrentSTR_info();
                        autofitTextRel.setTextOutlLine(0);
                        autofitTextRel.setTextOutlineColor(Color.TRANSPARENT);
                        binding.cg.setVisibility(VISIBLE);
                        binding.chipEditTv.setChecked(true);
                        binding.llTvEdit.setVisibility(VISIBLE);
                        break;
                    case R.id.tvStrokeColor:
                        CURRENT_ID = R.id.tvStrokeColor;
                        binding.rvFonts.setAdapter(colorsAdapter);
                        binding.rvFonts.setVisibility(VISIBLE);
                        break;
                    case R.id.tvStrokeStyle:

                        break;
                    case R.id.tvStrokeWidth:
                        CURRENT_ID = R.id.tvStrokeWidth;
                        binding.sb.setProgress(textInfo.getOutLineSize());
                        binding.sb.setVisibility(VISIBLE);
                        break;
                    case R.id.tvStrokeOpacity:
                        CURRENT_ID = R.id.tvStrokeOpacity;
                        binding.sb.setProgress(textInfo.getOutLineOpacity() / 2);
                        binding.sb.setVisibility(VISIBLE);
                        break;
                }
            }
        });

        binding.cgShadow.setOnCheckedStateChangeListener(new ChipGroup.OnCheckedStateChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull ChipGroup group, @NonNull List<Integer> checkedIds) {
                for (Map.Entry<String, View> entry : linViewMap.entrySet()) {
                    entry.getValue().setVisibility(GONE);
                }
                getCurrentSTR_info();
                switch (group.getCheckedChipId()) {
                    case R.id.tvShadowColor:
                        CURRENT_ID = R.id.tvShadowColor;
                        binding.rvFonts.setAdapter(colorsAdapter);
                        binding.rvFonts.setVisibility(VISIBLE);
                        break;
                    case R.id.tvShadowAngle:
                        binding.llShadowAngle.setVisibility(VISIBLE);
                        break;
                    case R.id.tvShadowOpacity:
                        CURRENT_ID = R.id.tvShadowOpacity;
                        AutofitTextRel autofitTextRel = (AutofitTextRel) getCurrentSTR_info();
                        binding.sb.setProgress(autofitTextRel.getShadowOpacity());
                        binding.sb.setVisibility(VISIBLE);
                        break;
                    case R.id.tvShadowRadius:
                        CURRENT_ID = R.id.tvShadowRadius;
                        binding.sb.setProgress(textInfo.getSHADOW_PROG());
                        binding.sb.setVisibility(VISIBLE);
                        break;
                }
            }
        });

        binding.ivUp.setOnTouchListener(new RepeatListener(200, 200, v -> {
            updateShadow("UP");
        }));
        binding.ivDown.setOnTouchListener(new RepeatListener(200, 200, v -> {
            updateShadow("DOWN");
        }));
        binding.ivLeft.setOnTouchListener(new RepeatListener(200, 200, v -> {
            updateShadow("LEFT");
        }));
        binding.ivRight.setOnTouchListener(new RepeatListener(200, 200, v -> {
            updateShadow("RIGHT");
        }));

        binding.ivUpTv.setOnTouchListener(new RepeatListener(200, 200, v -> {
            updateTextPos("UP");
        }));
        binding.ivDownTv.setOnTouchListener(new RepeatListener(200, 200, v -> {
            updateTextPos("DOWN");
        }));
        binding.ivLeftTv.setOnTouchListener(new RepeatListener(200, 200, v -> {
            updateTextPos("LEFT");
        }));
        binding.ivRightTv.setOnTouchListener(new RepeatListener(200, 200, v -> {
            updateTextPos("RIGHT");
        }));

        binding.ivUpImg.setOnTouchListener(new RepeatListener(200, 200, v -> {
            updateImgPos("UP");
        }));
        binding.ivDownImg.setOnTouchListener(new RepeatListener(200, 200, v -> {
            updateImgPos("DOWN");
        }));
        binding.ivLeftImg.setOnTouchListener(new RepeatListener(200, 200, v -> {
            updateImgPos("LEFT");
        }));
        binding.ivRightImg.setOnTouchListener(new RepeatListener(200, 200, v -> {
            updateImgPos("RIGHT");
        }));

        binding.cgBackground.setOnCheckedStateChangeListener(new ChipGroup.OnCheckedStateChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull ChipGroup group, @NonNull List<Integer> checkedIds) {
                for (Map.Entry<String, View> entry : linViewMap.entrySet()) {
                    entry.getValue().setVisibility(GONE);
                }
                getCurrentSTR_info();
                switch (group.getCheckedChipId()) {
                    case R.id.tvBgColor:
                        CURRENT_ID = R.id.tvBgColor;
                        binding.rvFonts.setVisibility(VISIBLE);
                        binding.rvFonts.setAdapter(colorsAdapter);
                        break;
                    case R.id.tvBgGradientColor:
                        CURRENT_ID = R.id.tvBgGradientColor;
                        binding.llGradient.setVisibility(VISIBLE);
                        break;
                    case R.id.tvBgPattern:
                        CURRENT_ID = R.id.tvBgPattern;
                        isTextBG = true;
                        binding.rvFonts.setVisibility(VISIBLE);
                        binding.rvFonts.setAdapter(patternAdapter);
                        break;
                    case R.id.tvBgOpacity:
                        CURRENT_ID = R.id.tvBgOpacity;
                        AutofitTextRel autofitTextRel = (AutofitTextRel) getCurrentSTR_info();
                        binding.sb.setProgress(autofitTextRel.getBgAlpha());
                        binding.sb.setVisibility(VISIBLE);
                        break;
                }
            }
        });

        binding.back3DRotation.setOnClickListener(v -> {
            binding.cg3DRotation.setVisibility(GONE);
            if (getCurrentSTR_info() instanceof AutofitTextRel) {
                binding.cg.setVisibility(VISIBLE);
                for (Map.Entry<String, View> entry : linViewMap.entrySet()) {
                    entry.getValue().setVisibility(GONE);
                }
                binding.chipEditTv.setChecked(true);
                binding.llTvEdit.setVisibility(VISIBLE);
            } else {
                binding.cgImage.setVisibility(VISIBLE);
                binding.chipEditIv.setChecked(true);
                for (Map.Entry<String, View> entry : linViewMap.entrySet()) {
                    entry.getValue().setVisibility(GONE);
                }
                binding.llEdit.setVisibility(VISIBLE);
            }

        });

        binding.backFromTxtColor.setOnClickListener(v -> {
            binding.cgTextColor.setVisibility(GONE);
            binding.llGradient.setVisibility(GONE);
            binding.cg.setVisibility(VISIBLE);
            binding.chipEditTv.setChecked(true);
            binding.llTvEdit.setVisibility(VISIBLE);
        });

        binding.backFromTxtStroke.setOnClickListener(v -> {
            binding.cgStroke.setVisibility(GONE);
            binding.cg.setVisibility(VISIBLE);
            binding.chipEditTv.setChecked(true);
            binding.llTvEdit.setVisibility(VISIBLE);
        });

        binding.backFromShadow.setOnClickListener(v -> {
            binding.cgShadow.setVisibility(GONE);
            binding.cg.setVisibility(VISIBLE);
            binding.chipEditTv.setChecked(true);
            binding.llTvEdit.setVisibility(VISIBLE);
        });

        binding.backFromTxtBg.setOnClickListener(v -> {
            binding.cgBackground.setVisibility(GONE);
            binding.cg.setVisibility(VISIBLE);
            binding.chipEditTv.setChecked(true);
            binding.llTvEdit.setVisibility(VISIBLE);
        });

        binding.sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                switch (CURRENT_ID) {
                    case R.id.ivZRotation:
                        setRotateProg("Z", progress * 2);
                        break;
                    case R.id.ivXRotation:
                        setRotateProg("X", progress * 2);
                        break;
                    case R.id.ivYRotation:
                        setRotateProg("Y", progress * 2);
                        break;
                    case R.id.tvStrokeWidth:
                        AutofitTextRel autofitTextRel = (AutofitTextRel) getCurrentSTR_info();
                        autofitTextRel.setTextOutlLine(progress);
                        break;
                    case R.id.tvStrokeOpacity:
                        AutofitTextRel autofitTextRel1 = (AutofitTextRel) getCurrentSTR_info();
                        autofitTextRel1.setOutLineOpacity(progress * 2);
                        autofitTextRel1.setTextOutlineColor(ColorUtils.setAlphaComponent(textInfo.getOutLineColor(),
                                (int) Util.normalize(progress, 0, 100, 0.0f, 255.0f)));
                        break;
                    case R.id.tvShadowOpacity:
                        AutofitTextRel autofitTextRel2 = (AutofitTextRel) getCurrentSTR_info();
                        autofitTextRel2.setTextShadowOpacity((int) Util.normalize(progress, 0, 100, 0.0f, 255.0f));
                        break;
                    case R.id.tvShadowRadius:
                        AutofitTextRel autofitTextRel3 = (AutofitTextRel) getCurrentSTR_info();
                        autofitTextRel3.setTextShadowProg(progress);
                        break;
                    case R.id.tvBgOpacity:
                        AutofitTextRel autofitTextRel4 = (AutofitTextRel) getCurrentSTR_info();
                        autofitTextRel4.setBgAlpha(progress);
                        break;
                    case R.id.tvOpacity:
                        AutofitTextRel autofitTextRel5 = (AutofitTextRel) getCurrentSTR_info();
                        autofitTextRel5.setTextAlpha(progress);
                        break;
                    case R.id.tvLineSpacing:
                        AutofitTextRel autofitTextRel6 = (AutofitTextRel) getCurrentSTR_info();
                        autofitTextRel6.applyLineSpacing(progress);
                        break;
                    case R.id.tvLetterSpacing:
                        AutofitTextRel autofitTextRel7 = (AutofitTextRel) getCurrentSTR_info();
                        autofitTextRel7.applyLetterSpacing(progress);
                        break;
                    case R.id.chipIfvColor:
                        RelStickerView relStickerView = (RelStickerView) getCurrentSTR_info();
                        relStickerView.setHueProg(progress);
                        break;
                    case R.id.chipOpacity:
                        RelStickerView relStickerView2 = (RelStickerView) getCurrentSTR_info();
                        relStickerView2.setAlphaProg(progress * 2);
                        break;
                }


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        binding.tvHFlip.setOnClickListener(v -> {
            flipView("Y");
        });
        binding.tvVFlip.setOnClickListener(v -> {
            flipView("X");
        });

        binding.alignLeft.setOnClickListener(v -> {
            setAlignLeft();
        });

        binding.alignCenter.setOnClickListener(v -> {
            setAlignCenter();
        });

        binding.alignRight.setOnClickListener(v -> {
            setAlignRight();
        });

        binding.fontCap.setOnClickListener(v -> {
            setAlignAllCap();
        });

        binding.fontSmall.setOnClickListener(v -> {
            setLowerFont();
        });
        binding.fontFirstCap.setOnClickListener(v -> {
            setFistCapFont();
        });
        binding.fontUnderline.setOnClickListener(v -> {
            setUnderLineFont();
        });
        binding.fontBold.setOnClickListener(v -> {
            setBoldFont();
        });
        binding.fontItalic.setOnClickListener(v -> {
            setItalicFont();
        });
        binding.fontCenterLine.setOnClickListener(v -> {
            setCenterLineFont();
        });

        fontAdapter = new FontAdapter(this, fontList(), fontName -> {
            setTextFonts(fontName);
        });

        binding.ivPlus.setOnClickListener(v -> {
            AutofitTextRel autofitTextRel6 = (AutofitTextRel) getCurrentSTR_info();
            binding.tvSizeText.setText("" + autofitTextRel6.getTextSize());
            Util.showLog("SIZE: " + autofitTextRel6.getTextSize());
            autofitTextRel6.plusSize();
        });

        binding.ivMinus.setOnClickListener(v -> {
            AutofitTextRel autofitTextRel6 = (AutofitTextRel) getCurrentSTR_info();
            if (autofitTextRel6.getTextSize() > 15) {
                binding.tvSizeText.setText("" + autofitTextRel6.getTextSize());
                Util.showLog("SIZE: " + autofitTextRel6.getTextSize());
                autofitTextRel6.minusSize();
            } else {
                Util.showToast(this, "Minimum Size Limit Exits");
            }
        });

        String jsonFileString = Util.getJsonFromAssets(getApplicationContext(), "colors.json");

        Gson gson = new Gson();
        Type listUserType = new TypeToken<List<String>>() {
        }.getType();

        List<String> colors = gson.fromJson(jsonFileString, listUserType);
        if (colors != null && colors.size() > 0) {
            colorsAdapter = new ColorsAdapter(this, colors, false, color -> {
                View view = getCurrentSTR_info();
                if (view instanceof AutofitTextRel) {
                    AutofitTextRel autofitTextRel = (AutofitTextRel) view;
                    if (CURRENT_ID == R.id.tvTextColor) {
                        autofitTextRel.setTextColor(Color.parseColor(color));
                    }
                    if (CURRENT_ID == R.id.tvStrokeColor) {
                        autofitTextRel.setTextOutlineColor(Color.parseColor(color));
                    }
                    if (CURRENT_ID == R.id.tvShadowColor) {
                        autofitTextRel.setTextShadowColor(Color.parseColor(color));
                    }
                    if (CURRENT_ID == R.id.tvBgColor) {
                        autofitTextRel.setBgAlpha(100);
                        autofitTextRel.setBgColor(Color.parseColor(color));
                    }

                }
                if (view instanceof RelStickerView) {
                    RelStickerView relStickerView = (RelStickerView) view;
                    if (CURRENT_ID == R.id.chipIfvColor) {
//                        relStickerView.setColorFilter(Color.parseColor(color));
                    }
                }
            });
        }

        String jsonFileString2 = Util.getJsonFromAssets(getApplicationContext(), "gradientColors.json");

        Gson gson2 = new Gson();
        Type listUserType2 = new TypeToken<List<ItemGradient>>() {
        }.getType();

        gradientAdapter = new GradientAdapter(this, gradientColor -> {
            View view = getCurrentSTR_info();
            if (view instanceof AutofitTextRel) {
                AutofitTextRel autofitTextRel = (AutofitTextRel) view;
                if (CURRENT_ID == R.id.tvBgGradientColor) {
                    autofitTextRel.setBGGradient(gradientColor, gradientType, gradientDirection);
                } else {
                    autofitTextRel.setGradientColors(gradientColor, gradientType, gradientDirection);
                }
            }
            if (view instanceof RelStickerView) {

            }
        });
        binding.rvGradients.setAdapter(gradientAdapter);

        List<ItemGradient> gradientColors = gson2.fromJson(jsonFileString2, listUserType2);

        if (gradientColors != null && gradientColors.size() > 0) {
            gradientAdapter.setData(gradientColors, gradientType, gradientDirection);
        }

        String[] typeList = new String[]{"Linear", "Radial", "Sweep"};

        binding.gdOrientation.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, typeList
        ));
        binding.gdOrientation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                gradientType = typeList[position];
                gradientAdapter.setData(gradientColors, gradientType, gradientDirection);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        patternAdapter = new ColorsAdapter(this, Util.listAssetFiles(getApplicationContext(), "pattern"), true,
                new ClickListener<String>() {
                    @Override
                    public void onClick(String pattern) {

                        View view = getCurrentSTR_info();
                        if (view instanceof AutofitTextRel) {
                            AutofitTextRel autofitTextRel = (AutofitTextRel) view;
                            try {
                                if (!isTextBG) {
                                    autofitTextRel.setTextPattern(Util.getBitmapFromAssets(getApplicationContext(),
                                            "pattern/" + pattern));
                                } else {
                                    autofitTextRel.setBGPattern(Util.getBitmapFromAssets(getApplicationContext(),
                                            "pattern/" + pattern));
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                });

        binding.ivGradientH.setOnClickListener(v -> {
            gradientDirection = "Horizontal";
            gradientAdapter.setData(gradientColors, gradientType, gradientDirection);
        });

        binding.ivGradientV.setOnClickListener(v -> {
            gradientDirection = "Vertical";
            gradientAdapter.setData(gradientColors, gradientType, gradientDirection);
        });

        binding.cgTextColor.setOnCheckedStateChangeListener(new ChipGroup.OnCheckedStateChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull ChipGroup group, @NonNull List<Integer> checkedIds) {
                for (Map.Entry<String, View> entry : linViewMap.entrySet()) {
                    entry.getValue().setVisibility(GONE);
                }
                switch (group.getCheckedChipId()) {
                    case R.id.tvTextColor:
                        CURRENT_ID = R.id.tvTextColor;
                        binding.rvFonts.setAdapter(colorsAdapter);
                        binding.rvFonts.setVisibility(VISIBLE);
                        break;
                    case R.id.tvTxtGradient:
                        binding.llGradient.setVisibility(VISIBLE);
                        break;
                    case R.id.tvTxtPattern:
                        isTextBG = false;
                        binding.rvFonts.setAdapter(patternAdapter);
                        binding.rvFonts.setVisibility(VISIBLE);
                        break;
                }
            }
        });

        binding.bgFrameLayoutHolder.setOnClickListener(v -> {
            Log.e("SB", " Click  = bgFrameLayoutHolder: ");
            binding.ll.setVisibility(GONE);
            removeViewControl();
            removeView();
            binding.llLayer.setVisibility(GONE);
            binding.rlBusiness.setVisibility(GONE);
        });
        binding.backFromCg.setOnClickListener(v -> {
            Log.e("SB", " Click  = backFromCg: ");
            binding.ll.setVisibility(GONE);
            removeViewControl();
            removeView();
        });
        binding.backFromCgImg.setOnClickListener(v -> {
            Log.e("SB", " Click  = backFromCgImg: ");
            binding.ll.setVisibility(GONE);
            removeViewControl();
            removeView();
        });

        binding.cgImage.setOnCheckedStateChangeListener(new ChipGroup.OnCheckedStateChangeListener() {

            @Override
            public void onCheckedChanged(@NonNull ChipGroup group, @NonNull List<Integer> checkedIds) {
                Log.e("SB", " Click  = cgImage: ");
                for (Map.Entry<String, View> entry : linViewMap.entrySet()) {
                    entry.getValue().setVisibility(GONE);
                }
                switch (group.getCheckedChipId()) {
                    case R.id.chipEditIv:
                        binding.llEdit.setVisibility(VISIBLE);
                        break;
                    case R.id.chip3DRotationIV:
                        binding.cgImage.setVisibility(GONE);
                        binding.ivZRotation.setChecked(true);
                        binding.cg3DRotation.setVisibility(VISIBLE);
                        binding.sb.setVisibility(VISIBLE);
                        break;
                    case R.id.chipIfvColor:
                        CURRENT_ID = R.id.chipIfvColor;
                        binding.sb.setProgress(stickerInfo.getSTC_HUE());
                        binding.sb.setVisibility(VISIBLE);
                        break;
                    case R.id.chipCrop:
                        binding.llCrop.setVisibility(VISIBLE);
                        break;
                    case R.id.chipOpacity:
                        CURRENT_ID = R.id.chipOpacity;
                        binding.sb.setProgress(stickerInfo.getSTC_OPACITY());
                        binding.sb.setVisibility(VISIBLE);
                        break;
                }
            }
        });

        binding.changeImage.setOnClickListener(v -> {
            Log.e("SB", " Click  = changeImage: ");
            RelStickerView relStickerView = (RelStickerView) getCurrentSTR_info();
            selectedRatio.clear();
            selectedRatio = Util.convertDecimalToFraction((float) relStickerView.getMeasuredWidth(),
                    (float) relStickerView.getMeasuredHeight());
            isChangeMode = true;
            addImageDialog();
        });

        binding.tvAddImg.setOnClickListener(v -> {
            Log.e("SB", "Click = tvAddImg: ");
            binding.ll.setVisibility(GONE);
            binding.rlSticker.setVisibility(GONE);
            binding.rlBusiness.setVisibility(GONE);
            isChangeMode = false;
            addImageDialog();
        });

        binding.ivPlayVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                loadVideo(VideoPath);

            }
        });
        binding.btnSave.setOnClickListener(v -> {
            binding.btnSave.setEnabled(false);
            binding.ll.setVisibility(GONE);
            binding.rlBusiness.setVisibility(GONE);
            binding.rlSticker.setVisibility(GONE);
            setBackImage();
            removeViewControl();
            CURRENT_CLICK = 1;
            Dexter.withContext(this).withPermissions(PERMISSIONS).withListener(new MultiplePermissionsListener() {
                public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                    if (multiplePermissionsReport.areAllPermissionsGranted()) {
                        GrandSave();
                    }
                    if (multiplePermissionsReport.isAnyPermissionPermanentlyDenied()) {
                        for (int i = 0; i < multiplePermissionsReport.getDeniedPermissionResponses().size(); i++) {
                            Log.e("TAG", "permis" + multiplePermissionsReport.getDeniedPermissionResponses().get(i).getPermissionName());
                        }
                        showSettingsDialog();
                    }
                }

                public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                    permissionToken.continuePermissionRequest();
                }
            }).withErrorListener(new PermissionRequestErrorListener() {
                public void onError(DexterError dexterError) {
//                    Toast.makeText(PosterActivity.this, "Error occurred! ", Toast.LENGTH_SHORT).show();
                    permissionsList = new ArrayList<>();
                    permissionsList.addAll(Arrays.asList(PERMISSIONS));
                    askForPermissions(permissionsList);
                }
            }).onSameThread().check();
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                binding.btnSave.setEnabled(true);
            }, 500);
        });

    }

    private void GrandSave() {
        if (absPlayerInternal != null) {
            absPlayerInternal.setPlayWhenReady(false);
            absPlayerInternal.stop();
            absPlayerInternal.seekTo(0);
        }
        if (isVideo) {
            Util.showLog("Video Url : " + intentPostItem.image_url);
            DownloadTask downloadTask = new DownloadTask(PosterActivity.this);
            downloadTask.execute(intentPostItem.image_url);
        } else {
            //  Util.showLog("image Url : " + intentPostItem.image_url);
            fileName = System.currentTimeMillis() + ".jpeg";
            Util.showLog("File Name : " + fileName);
            new LoadSaveImage().execute();
        }
    }

    private void checkBusiness(CheckBox checkBox, boolean isClick) {

        if (mItemArray.size() > 0) {

            for (int i = 0; i < mItemArray.size(); i++) {

                View view = (View) ((Pair) this.mItemArray.get(i)).second;

                if (view instanceof AutofitTextRel) {

                    AutofitTextRel autofitTextRel = (AutofitTextRel) view;

                    if (autofitTextRel.getName().equals("name") && checkBox.getText().equals(getString(R.string.name))) {
                        if (isClick) {
                            checkBox.setChecked(autofitTextRel.getVisibility() == VISIBLE ? true : false);
                        } else {
                            autofitTextRel.setVisibility(autofitTextRel.getVisibility() == VISIBLE ? GONE : VISIBLE);
                            checkBox.setChecked(autofitTextRel.getVisibility() == VISIBLE ? true : false);
                        }
                    }

                    if (autofitTextRel.getName().equals("address") && checkBox.getText().equals(getString(R.string.address))) {
                        if (isClick) {
                            checkBox.setChecked(autofitTextRel.getVisibility() == VISIBLE ? true : false);
                        } else {
                            autofitTextRel.setVisibility(autofitTextRel.getVisibility() == VISIBLE ? GONE : VISIBLE);
                            checkBox.setChecked(autofitTextRel.getVisibility() == VISIBLE ? true : false);
                        }
                    }

                    if (autofitTextRel.getName().equals("mobile") && checkBox.getText().equals(getString(R.string.phone))) {
                        if (isClick) {
                            checkBox.setChecked(autofitTextRel.getVisibility() == VISIBLE ? true : false);
                        } else {
                            autofitTextRel.setVisibility(autofitTextRel.getVisibility() == VISIBLE ? GONE : VISIBLE);
                            checkBox.setChecked(autofitTextRel.getVisibility() == VISIBLE ? true : false);
                        }
                    }

                    if (autofitTextRel.getName().equals("website") && checkBox.getText().equals(getString(R.string.website))) {
                        if (isClick) {
                            checkBox.setChecked(autofitTextRel.getVisibility() == VISIBLE ? true : false);
                        } else {
                            autofitTextRel.setVisibility(autofitTextRel.getVisibility() == VISIBLE ? GONE : VISIBLE);
                            checkBox.setChecked(autofitTextRel.getVisibility() == VISIBLE ? true : false);
                        }
                    }

                    if (autofitTextRel.getName().equals("email") && checkBox.getText().equals(getString(R.string.email))) {
                        if (isClick) {
                            checkBox.setChecked(autofitTextRel.getVisibility() == VISIBLE ? true : false);
                        } else {
                            autofitTextRel.setVisibility(autofitTextRel.getVisibility() == VISIBLE ? GONE : VISIBLE);
                            checkBox.setChecked(autofitTextRel.getVisibility() == VISIBLE ? true : false);
                        }
                    }

                }

                if (view instanceof RelStickerView) {

                    RelStickerView relStickerView = (RelStickerView) view;

                    if (relStickerView.getName().equals("logo") && checkBox.getText().equals(getString(R.string.logo))) {
                        if (isClick) {
                            checkBox.setChecked(relStickerView.getVisibility() == VISIBLE ? true : false);
                        } else {
                            relStickerView.setVisibility(relStickerView.getVisibility() == VISIBLE ? GONE : VISIBLE);
                            checkBox.setChecked(relStickerView.getVisibility() == VISIBLE ? true : false);
                        }
                    }


                }

            }

        }

    }

    @Override
    public void onAdFailedToLoad() {

    }

    @Override
    public void onAdDismissed() {
        showPreviewDialog();
    }

    public void LoadRewardAds() {
        if (prefManager.getBoolean(Constant.AVL_REWARD) && MyApplication.prefManager().getBoolean(Constant.REWARD_AD_ENABLE)
                && MyApplication.prefManager().getBoolean(Constant.ADS_ENABLE)
                && !Constant.IS_SUBSCRIBED) {
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
                    RewardedAd.load(this, MyApplication.prefManager().getString(Constant.REWARD_AD_ID), builder.build(),
                            new RewardedAdLoadCallback() {
                                @Override
                                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                                    // Handle the error.
                                    Util.showLog("Load Error" + loadAdError.toString());
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
                    Util.showLog("Ad failed to show");
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
                        Util.showLog("REWARDED: " + rewardItem);
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
                Constant.REWARD_GRANTED = true;
                if (isVideo) {
                    DownloadTask downloadTask = new DownloadTask(PosterActivity.this);
                    downloadTask.execute(intentPostItem.image_url);
                } else {
                    new LoadSaveImage().execute();
                }
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (!InterstitialAdManager.isLoaded()) {
            InterstitialAdManager.LoadAds();
        }
        if (!isLoaded()) {
            LoadRewardAds();
        }
    }

    private void loadSticker() {

        stickerCategoryAdapter = new StickerCategoryAdapter(this, position -> {
            binding.vpStickers.setCurrentItem(position);
        });
        binding.rvStickerCategory.setAdapter(stickerCategoryAdapter);

        strAdapter = new StickerViewPagerAdapter(this, data -> {

            prgDialog.show();
            LoadSticker loadLogo = new LoadSticker(data.stickerImage);
            loadLogo.execute();
        });

        binding.vpStickers.setAdapter(strAdapter);
        binding.vpStickers.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                stickerCategoryAdapter.setSelected(position);
                binding.rvStickerCategory.smoothScrollToPosition(position);

            }
        });

        postViewModel.getStickers().observe(this, listResource -> {
            if (listResource != null) {

                if (listResource.data != null) {

                    stickerCategoryAdapter.setCategories(listResource.data.strCategories);
                    strAdapter.setData(listResource.data.strModelList);

                }
            }
        });

        binding.ivSearch.setOnClickListener(v -> {
            if (binding.etSearchStickers.getText().toString().equals("")) {
                Util.showToast(PosterActivity.this, "Please Enter Keyword");
                return;
            }
            loadSearchStickers();
        });

        binding.etSearchStickers.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_SEARCH) {
                    loadSearchStickers();
                    return true;
                }
                return false;
            }
        });
        binding.etSearchStickers.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0) {
                    binding.vpStickers.setVisibility(VISIBLE);
                    binding.rvStickerCategory.setVisibility(VISIBLE);
                    binding.rvSticker.setVisibility(GONE);
                    binding.etSearchStickers.clearFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        /*binding.etSearchStickers.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_SEARCH) {
                    loadSearchStickers();
                }
                return false;
            }
        });*/


    }

    public void loadSearchStickers() {
        binding.vpStickers.setVisibility(GONE);
        binding.rvStickerCategory.setVisibility(GONE);

        binding.rvSticker.setVisibility(VISIBLE);

        postViewModel.getStickersByKeyword(binding.etSearchStickers.getText().toString()).observe(this,
                listResource -> {
                    if (listResource != null) {

                        if (listResource.data != null && listResource.data.size() > 0) {

                            StickerAdapter strAdapter = new StickerAdapter(this, listResource.data, data -> {
                                binding.rvSticker.setVisibility(GONE);
                                binding.rlSticker.setVisibility(GONE);
                                binding.vpStickers.setVisibility(VISIBLE);
                                binding.rvStickerCategory.setVisibility(VISIBLE);
                                prgDialog.show();
                                LoadSticker loadLogo = new LoadSticker(data.stickerImage);
                                loadLogo.execute();
                            });
                            binding.rvSticker.setAdapter(strAdapter);
                            strAdapter.notifyDataSetChanged();

                        }

                    } else {
                        if (listResource.status == Status.ERROR)
                            Toast.makeText(activity, "No Sticker available for " + binding.etSearchStickers.getText().toString(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void selectedBorder(int drawable) {
        binding.borderImg.setImageDrawable(getResources().getDrawable(drawable));
    }

    @Override
    public void selectedImage(int drawable) {
        setBitmapOverlay(drawable);
    }

    public void setBitmapOverlay(int i) {
        binding.layFilter.setVisibility(View.VISIBLE);
        binding.transImg.setVisibility(View.VISIBLE);
        try {
            binding.transImg.setImageBitmap(BitmapFactory.decodeResource(getResources(), i));
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            BitmapFactory.Options options2 = new BitmapFactory.Options();
            options2.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(getResources(), i, options2);
            BitmapFactory.Options options3 = new BitmapFactory.Options();
            options3.inSampleSize = ImageUtils.getClosestResampleSize(options2.outWidth, options2.outHeight, binding.mainRel.getWidth() < binding.mainRel.getHeight() ? binding.mainRel.getWidth() : binding.mainRel.getHeight());
            options2.inJustDecodeBounds = false;
            binding.transImg.setImageBitmap(BitmapFactory.decodeResource(getResources(), i, options3));
        }
    }

    class LoadSticker extends AsyncTask<String, String, String> {

        private String message = "", verifyStatus = "0", urls;
        Drawable drawable;
        Bitmap bitmap;

        public LoadSticker(String urls) {
            this.urls = urls;
        }

        @Override
        protected String doInBackground(String... strings) {
            bitmap = null;
            Log.e("URL", "Sticker Url := " + urls);
            InputStream inputStream;
            try {
                inputStream = new java.net.URL(urls).openStream();
                bitmap = BitmapFactory.decodeStream(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }

            drawable = new BitmapDrawable(Resources.getSystem(), bitmap);
            return "0";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            prgDialog.dismiss();
            binding.rlSticker.setVisibility(GONE);
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            addSticker(bitmap);
        }
    }

    class LoadSaveImage extends AsyncTask<String, Boolean, Boolean> {

        @Override
        protected void onPreExecute() {
            prgDialog.setMessage("Please Wait...");
            prgDialog.setCancelable(false);
            prgDialog.show();
            if (!userItem.isSubscribed && !Constant.REWARD_GRANTED) {
                binding.ivFrameWatermark.setVisibility(VISIBLE);
            } else {
                binding.ivFrameWatermark.setVisibility(GONE);
            }
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... strings) {

            try {
                Bitmap bitmap = viewToBitmap(binding.mainRel);
                float multiplier = 0;
                if (totalMemory <= 2000) {
                    multiplier = 1f;
                } else if (totalMemory <= 3200) {
                    multiplier = 1.2f;
                } else if (totalMemory <= 3500) {
                    multiplier = 1.6f;
                } else if (totalMemory <= 4500) {
                    multiplier = 2f;
                } else if (totalMemory <= 6000) {
                    multiplier = 2.5f;
                } else if (totalMemory <= 9000) {
                    multiplier = 3f;
                } else if (totalMemory <= 10000) {
                    multiplier = 3.5f;
                } else {
                    multiplier = 2f;
                }

//                int multiplier = (int) getResources().getDimension(com.intuit.ssp.R.dimen._1ssp);
                Util.showLog("Multiplier: " + multiplier + "TotalM: " + totalMemory);
                bitmap = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * multiplier),
                        (int) (bitmap.getHeight() * multiplier), true);
                Constant.bitmap = bitmap;

                prgDialog.dismiss();
                return Constant.bitmap != null;
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean s) {
            if (s) {
                binding.ivFrameWatermark.setVisibility(GONE);
                if (InterstitialAdManager.isLoaded() && prefManager.getInt(Constant.CLICK) >= prefManager.getInt(Constant.INTERSTITIAL_AD_CLICK)) {
                    prefManager.setInt(Constant.CLICK, 0);
                    InterstitialAdManager.showAds();
                } else {
                    prefManager.setInt(Constant.CLICK, prefManager.getInt(Constant.CLICK) + 1);
                    showPreviewDialog();

                }
            } else {
                Toast.makeText(PosterActivity.this, getString(R.string.err_creating_image), Toast.LENGTH_SHORT).show();
            }
            prgDialog.dismiss();
            super.onPostExecute(s);
        }
    }

    //BottomSheet Open
    @OptIn(markerClass = UnstableApi.class)
    private void showPreviewDialog() {
        int screenWidth;
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(R.layout.download_dialog);
        dialog.setCancelable(false);
        screenWidth = MyApplication.getColumnWidth(1, getResources().getDimension(com.intuit.ssp.R.dimen._10ssp));

        CardView cv_base = dialog.findViewById(R.id.cv_base);

        String[] split = ratio.split(":");
        float wrRat = Float.parseFloat(split[0]) / Float.parseFloat(split[1]);
        Util.showLog("tesrRATIO: " + wrRat + " " + ratio);

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) cv_base.getLayoutParams();
        params.width = screenWidth;
        params.height = (int) (screenWidth / wrRat);

        cv_base.setLayoutParams(params);

        TextView title = dialog.findViewById(R.id.save_title);
        ImageView iv_cancel = dialog.findViewById(R.id.iv_close);
        ImageView iv_preview = dialog.findViewById(R.id.iv_save_image);
        ImageView iv_download = dialog.findViewById(R.id.iv_download);
        ImageView iv_whatsapp = dialog.findViewById(R.id.ic_whatsapp);
        ImageView iv_facebook = dialog.findViewById(R.id.ic_facebook);
        ImageView iv_instagram = dialog.findViewById(R.id.ic_instagram);
        ImageView iv_twitter = dialog.findViewById(R.id.ic_twitter);
        ImageView iv_share = dialog.findViewById(R.id.ic_share);
        PlayerView videoPlayer = dialog.findViewById(R.id.videoPlayer);
        ImageView ivPlayVideo = dialog.findViewById(R.id.iv_play_video);
        ImageView icThumb = dialog.findViewById(R.id.icThumb);
        RelativeLayout relative_ads = dialog.findViewById(R.id.relative_ads);


        if (absPlayerInternal != null) {
            absPlayerInternal.setPlayWhenReady(false);
            absPlayerInternal.stop();
            absPlayerInternal.seekTo(0);
            binding.ivPlayVideo.setVisibility(View.VISIBLE);
            binding.icThumb.setVisibility(View.INVISIBLE);
        }


        DefaultRenderersFactory rf = new DefaultRenderersFactory(this).setEnableDecoderFallback(true);
        if (sharePlayer != null)
            sharePlayer.release();

        sharePlayer = new ExoPlayer.Builder(this, rf).build();

        if (!userItem.isSubscribed && MyApplication.prefManager().getBoolean(Constant.ADS_ENABLE)
                && MyApplication.prefManager().getBoolean(Constant.REWARD_AD_ENABLE) && !Constant.REWARD_GRANTED) {
            relative_ads.setVisibility(VISIBLE);
        } else {
            relative_ads.setVisibility(GONE);
        }
        relative_ads.setOnClickListener(v -> {
            if (prefManager.getBoolean(AVL_REWARD)) {
                if (isLoaded()) {
                    dialog.dismiss();
                    showAd();
                } else {
                    Util.showToast(PosterActivity.this, "Rewards not loaded");
                }
            } else {
                com.sgdigitalposter.app.utils.Util.showToast(PosterActivity.this, "Your Daily Reward Limit is Over!");
            }
        });

        ivPlayVideo.setOnClickListener(v -> {
            ivPlayVideo.setVisibility(GONE);
            if (sharePlayer != null) {
                sharePlayer.seekTo(0);
                sharePlayer.setPlayWhenReady(true);
                icThumb.setVisibility(View.INVISIBLE);
            }
        });

        GlideBinding.setTextSize(title, "font_title_size");

        if (isVideo) {
            if (sharePlayer != null) {
                sharePlayer.setPlayWhenReady(false);
                sharePlayer.stop();
                sharePlayer.seekTo(0);
            }


            videoPlayer.setShutterBackgroundColor(ContextCompat.getColor(this, R.color.yellow_50));
            videoPlayer.setDefaultArtwork(ContextCompat.getDrawable(this, R.drawable.spaceholder));

            videoPlayer.setUseController(false);
            videoPlayer.setControllerHideOnTouch(true);
            videoPlayer.setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS);

            MediaItem mediaItem = MediaItem.fromUri(Uri.parse(VideoPath));
            sharePlayer.setMediaItem(mediaItem);
            sharePlayer.prepare();

            Glide.with(this)
                    .load(strPathForThumbNail)
                    .thumbnail(Glide.with(this).load(strPathForThumbNail))
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(icThumb);

            icThumb.setVisibility(VISIBLE);
            ivPlayVideo.setVisibility(VISIBLE);
            videoPlayer.setVisibility(VISIBLE);

           /* sharePlayer.setPlayWhenReady(false); // start loading video and play it at the moment a chunk of it is available offline
            sharePlayer.play();*/

            videoPlayer.setPlayer(sharePlayer);

            sharePlayer.addListener(new Player.Listener() {
                @Override
                public void onEvents(Player player, Player.Events events) {
                    Player.Listener.super.onEvents(player, events);
                }

                @Override
                public void onPlaybackStateChanged(int playbackState) {
                    Player.Listener.super.onPlaybackStateChanged(playbackState);
                    switch (playbackState) {
                        case ExoPlayer.STATE_ENDED:
                            ivPlayVideo.setVisibility(View.VISIBLE);
                            icThumb.setVisibility(View.INVISIBLE);

                            break;
                        case ExoPlayer.STATE_READY:
                            binding.icThumb.setVisibility(View.INVISIBLE);
                            videoPlayer.setVisibility(VISIBLE);
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

        } else {
            iv_preview.setVisibility(VISIBLE);
            Glide.with(this)
                    .load(Constant.bitmap)
                    .into(iv_preview);
        }
        iv_cancel.setOnClickListener(v -> {
            if (sharePlayer != null) {
                sharePlayer.setPlayWhenReady(false);
                sharePlayer.stop();
                sharePlayer.seekTo(0);
                sharePlayer.release();
                ivPlayVideo.setVisibility(VISIBLE);
            }
            dialog.dismiss();
        });
        iv_download.setOnClickListener(v -> {
            if (sharePlayer != null) {
                sharePlayer.setPlayWhenReady(false);
                sharePlayer.stop();
                sharePlayer.seekTo(0);
                ivPlayVideo.setVisibility(VISIBLE);
            }
            new LoadDownloadImage("download").execute();
            dialog.dismiss();
        });
        iv_whatsapp.setOnClickListener(v -> {
            if (sharePlayer != null) {
                sharePlayer.setPlayWhenReady(false);
                sharePlayer.stop();
                sharePlayer.seekTo(0);
                ivPlayVideo.setVisibility(VISIBLE);
            }
            new LoadDownloadImage("whtsapp").execute();
            dialog.dismiss();
        });
        iv_facebook.setOnClickListener(v -> {
            if (sharePlayer != null) {
                sharePlayer.setPlayWhenReady(false);
                sharePlayer.stop();
                sharePlayer.seekTo(0);
                ivPlayVideo.setVisibility(VISIBLE);
            }
            new LoadDownloadImage("fb").execute();
            dialog.dismiss();
        });
        iv_instagram.setOnClickListener(v -> {
            if (sharePlayer != null) {
                sharePlayer.setPlayWhenReady(false);
                sharePlayer.stop();
                sharePlayer.seekTo(0);
                ivPlayVideo.setVisibility(VISIBLE);
            }
            new LoadDownloadImage("insta").execute();
            dialog.dismiss();
        });
        iv_twitter.setOnClickListener(v -> {
            if (sharePlayer != null) {
                sharePlayer.setPlayWhenReady(false);
                sharePlayer.stop();
                sharePlayer.seekTo(0);
                ivPlayVideo.setVisibility(VISIBLE);
            }
            new LoadDownloadImage("twter").execute();
            dialog.dismiss();
        });
        iv_share.setOnClickListener(v -> {
            if (sharePlayer != null) {
                sharePlayer.setPlayWhenReady(false);
                sharePlayer.stop();
                sharePlayer.seekTo(0);
                ivPlayVideo.setVisibility(VISIBLE);
            }
            new LoadDownloadImage("Share Via").execute();
            dialog.dismiss();
        });
        dialog.show();
    }

    class LoadDownloadImage extends AsyncTask<String, Boolean, Boolean> {
        String type = "";
        String filePath;
        boolean checkMemory;

        LoadDownloadImage(String type) {
            this.type = type;
        }

        @Override
        protected void onPreExecute() {
            prgDialog.setMessage("Please Wait...");
            prgDialog.setCancelable(false);
            prgDialog.show();
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... strings) {

            if (isVideo) {
                fileName = getString(R.string.app_name) + "_" + System.currentTimeMillis() + ".mp4";
                filePath = Environment.getExternalStorageDirectory() + File.separator
                        + Environment.DIRECTORY_PICTURES + File.separator + getResources().getString(R.string.app_name)
                        + File.separator + fileName;

                File sourceLocation = new File(VideoPath);

                boolean success = false;

                if (!new File(filePath).exists()) {
                    try {
                        File file = new File(Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_PICTURES
                        ), "/" + getResources().getString(R.string.app_name));
                        if (!file.exists()) {
                            if (!file.mkdirs()) {
                                Util.showLog("Can't create directory to save image.");
                                Toast.makeText(getApplicationContext(),
                                        getResources().getString(R.string.don_t_create),
                                        Toast.LENGTH_LONG).show();
                                success = false;
                            }
                        }

                        if (sourceLocation.exists()) {

                            InputStream in = new FileInputStream(sourceLocation);
                            OutputStream out = new FileOutputStream(filePath);

                            // Copy the bits from instream to outstream
                            byte[] buf = new byte[1024];
                            int len;

                            while ((len = in.read(buf)) > 0) {
                                out.write(buf, 0, len);
                            }

                            in.close();
                            out.close();
                            success = true;

                        } else {
                            Util.showLog("Copy file failed. Source file missing.");
                        }

                    } catch (Exception e) {

                    }
                }
                return success;
            } else {

                filePath = Environment.getExternalStorageDirectory() + File.separator
                        + Environment.DIRECTORY_PICTURES + File.separator + getResources().getString(R.string.app_name)
                        + File.separator + fileName;

                boolean success = false;

                if (!new File(filePath).exists()) {
                    try {
                        File file = new File(Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_PICTURES
                        ), "/" + getResources().getString(R.string.app_name));
                        if (!file.exists()) {
                            if (!file.mkdirs()) {
                                Util.showLog("Can't create directory to save image.");
                                Toast.makeText(getApplicationContext(),
                                        getResources().getString(R.string.don_t_create),
                                        Toast.LENGTH_LONG).show();
                                success = false;
                            }
                        }
                        File file2 = new File(file.getAbsolutePath() + "/" + fileName);
                        if (file2.exists()) {
                            file2.delete();
                        }
                        Bitmap bitmap = Constant.bitmap;
                        try {
                            FileOutputStream fileOutputStream = new FileOutputStream(file2);
                            Bitmap createBitmap = Bitmap.createBitmap(bitmap.getWidth(),
                                    bitmap.getHeight(), bitmap.getConfig());
                            Canvas canvas = new Canvas(createBitmap);
                            canvas.drawColor(-1);
                            canvas.drawBitmap(bitmap, 0.0f, 0.0f, (Paint) null);
                            checkMemory = createBitmap.compress(Bitmap.CompressFormat.PNG,
                                    100, fileOutputStream);
                            createBitmap.recycle();
                            fileOutputStream.flush();
                            fileOutputStream.close();

                            MediaScannerConnection.scanFile(PosterActivity.this, new String[]{file2.getAbsolutePath()},
                                    (String[]) null, new MediaScannerConnection.OnScanCompletedListener() {
                                        public void onScanCompleted(String str, Uri uri) {
                                            Util.showLog("ExternalStorage " + "Scanned " + str + ":");
                                            StringBuilder sb = new StringBuilder();
                                            sb.append("-> uri=");
                                            sb.append(uri);
                                            sb.append("-> FILE=");
                                            sb.append(file2.getAbsolutePath());
                                            Uri muri = Uri.fromFile(file2);
                                        }
                                    });
                            success = true;
                        } catch (Exception e) {
                            e.printStackTrace();
                            success = false;
                        }

                        prgDialog.dismiss();
                    } catch (Exception unused2) {
                    }
                }
                return success;
            }
        }

        @Override
        protected void onPostExecute(Boolean s) {
//                progressDialog.dismiss();
            if (s) {
                if (type.equals("download")) {
                    if (isVideo) {
                        Util.showToast(PosterActivity.this, getString(R.string.video_saved));
                    } else {
                        Toast.makeText(PosterActivity.this, getString(R.string.image_saved), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    shareFileImageUri(getImageContentUri(new File(filePath)), "", type, isVideo);
                }
            } else {
                Toast.makeText(PosterActivity.this, getString(R.string.err_save_image), Toast.LENGTH_SHORT).show();
            }
            prgDialog.dismiss();
            super.onPostExecute(s);
        }
    }

    public class DownloadTask extends AsyncTask<String, Integer, String> {
        private Context context;

        public DownloadTask(Context context) {
            this.context = context;
            progress.setMessage("Downloading...");
            progress.setCancelable(false);
            progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            try {
                progress.setIndeterminate(true);
            } catch (Exception e) {

            }
            progress.setProgress(0);
            progress.show();

        }

        @Override
        protected String doInBackground(String... sUrl) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                java.net.URL url = new URL(sUrl[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode() + " " + connection.getResponseMessage();
                }

                int fileLength = connection.getContentLength();

                input = connection.getInputStream();

                String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/" + "." + FOLDER_NAME + "/";
                File dir = new File(path);
                if (!dir.exists()) {
                    boolean dirCreated = dir.mkdirs();
                    if (!dirCreated) {
                        Log.e("DownloadTask", "Failed to create directory: " + dir.getAbsolutePath());
                        return "Failed to create download directory";
                    }
                } else {
                    // Clean existing files
                    File[] existingFiles = dir.listFiles();
                    if (existingFiles != null) {
                        for (File file : existingFiles) {
                            if (file != null && file.isFile()) {
                                boolean deleted = file.delete();
                                if (!deleted) {
                                    Log.w("DownloadTask", "Failed to delete existing file: " + file.getAbsolutePath());
                                }
                            }
                        }
                    }
                }

                File filename = new File(dir, "video.mp4");

                if (filename.exists()) {
                    // Use file.delete() instead of ContentResolver.delete() for file URIs
                    boolean deleted = filename.delete();
                    if (!deleted) {
                        Log.w("DownloadTask", "Failed to delete existing file: " + filename.getAbsolutePath());
                    }
                }

                try {
                    output = new FileOutputStream(filename);
                } catch (IOException e) {
                    Log.e("DownloadTask", "Failed to create output stream for file: " + filename.getAbsolutePath(), e);
                    return "Failed to create output file: " + e.getMessage();
                }

                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    if (fileLength > 0)
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }

                // Ensure all data is written to disk
                output.flush();
                output.close();
                input.close();

                // Verify the file was created and has content
                if (!filename.exists() || filename.length() == 0) {
                    return "Failed to create video file or file is empty";
                }

                Log.d("DownloadTask", "Video downloaded successfully. Size: " + filename.length() + " bytes");

            } catch (Exception e) {
                Log.e("DownloadTask", "Download error: " + e.getMessage(), e);
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }
                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... progres) {
            super.onProgressUpdate(progres);
            progress.setIndeterminate(false);
            progress.setMax(100);
            progress.setProgress(progres[0]);
        }

        @Override
        protected void onPostExecute(String result) {

            String videoFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/" +
                    "." + FOLDER_NAME + "/" + "video.mp4";

            MediaScannerConnection.scanFile(PosterActivity.this,
                    new String[]{videoFilePath}, null,

                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String newpath, Uri newuri) {
                            Util.showLog("ExternalStorage: Scanned " + newpath + ":");
                            Util.showLog("ExternalStorage: -> uri=" + newuri);
                            Log.d("FFmpegError", "onScanCompleted: " + newuri);
                            Log.d("FFmpegError", "onScanCompleted: " + newpath);

                            progress.dismiss();

                            // Check for download errors first
                            if (result != null) {
                                Log.e("DownloadTask", "Download failed: " + result);
                                Util.showToast(PosterActivity.this, "Download failed: " + result);
                                updateFolderName();
                                return;
                            }

                            // Use the original video file path instead of scanner path
                            File videoFile = new File(videoFilePath);
                            if (videoFile.exists() && videoFile.length() > 0) {
                                Log.d("DownloadTask", "Video file verified: " + videoFile.length() + " bytes");
                                startCreating();
                            } else {
                                // Wait a bit and try again
                                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (videoFile.exists() && videoFile.length() > 0) {
                                            Log.d("DownloadTask", "Video file verified after delay: " + videoFile.length() + " bytes");
                                            startCreating();
                                        } else {
                                            Log.e("DownloadTask", "Video file not found or empty: " + videoFile.getAbsolutePath());
                                            Util.showToast(PosterActivity.this, "Error: Video file not found or empty");
                                            updateFolderName();
                                        }
                                    }
                                }, 1000); // Wait 1 second
                            }
                        }
                    });
        }
    }

    private void startCreating() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!userItem.isSubscribed && !Constant.REWARD_GRANTED) {
                    binding.ivFrameWatermark.setVisibility(VISIBLE);
                } else {
                    binding.ivFrameWatermark.setVisibility(GONE);
                }

                String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/"
                        + FOLDER_NAME + "BrandPeakVideos.mp4";
                File dir = new File(path);
                if (dir.exists()) {
                    dir.delete();
                }

                Bitmap bitmap = Bitmap.createScaledBitmap(viewToBitmap(binding.mainRel), 1080,
                        1080, true);
                String strPath = saveImage(bitmap);
                strPathForThumbNail = strPath;

                StringBuilder outputDir = new StringBuilder();
                File file4 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                outputDir.append(file4.getAbsolutePath());
                outputDir.append("/" + "." + FOLDER_NAME + "/");
                outputDir.append("BrandPeakVideos.mp4");

                StringBuilder inputDir = new StringBuilder();
                File file5 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                inputDir.append(file5.getAbsolutePath());
                inputDir.append("/" + "." + FOLDER_NAME + "/");
                inputDir.append("video.mp4");

                try {
                    long duration = getVideoDuration(inputDir.toString());
                    processVideo(inputDir.toString(), strPath, outputDir.toString(), duration);
                } catch (IOException e) {
                    Log.e("FFmpegError", "Error getting video duration: " + e.getMessage(), e);
                    Util.showToast(PosterActivity.this, "Error processing video: " + e.getMessage());
                    updateFolderName();
                    return;
                }

                progressDD.setMessage("Creating...");
                progressDD.setCancelable(false);
                progressDD.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDD.setIndeterminate(true);
                progressDD.setProgress(0);
                progressDD.show();
            }
        });
    }

    private void processVideo(String inputPath, String strPath, String outputPath, long duration) {
        String command =
                "-i " + inputPath +
                        " -i " + strPath +
                        " -filter_complex " + " overlay " +
                        " -r " + " 150 " +
                        " -b:v " + " 20M " + // Corrected from "-vb" to "-b:v"
                        " -y " + outputPath;

        // Log the command for debugging
        Log.d("FFmpegLog", command.toString());

        // Execute the FFmpeg command correctly
        FFmpegKit.executeAsync(command, session -> {
            FFmpegKit.cancel(session.getSessionId());
            progressDD.dismiss();
            binding.ivFrameWatermark.setVisibility(GONE);
            if (InterstitialAdManager.isLoaded() && prefManager.getInt(Constant.CLICK) >= prefManager.getInt(Constant.INTERSTITIAL_AD_CLICK)) {
                prefManager.setInt(Constant.CLICK, 0);
                InterstitialAdManager.showAds();
            } else {
                prefManager.setInt(Constant.CLICK, prefManager.getInt(Constant.CLICK) + 1);
                VideoPath = outputPath.toString();
                runOnUiThread(() -> {
                    showPreviewDialog();

                });

            }


        }, log -> {
            Log.d("FFmpegLog", log.getMessage());
        }, statistics -> {
            float progressFinal = (float) statistics.getTime() / duration * 100;
            progressDD.setIndeterminate(false);
            progressDD.setMax(100);
            progressDD.setProgress((int) progressFinal);
        });
    }

    private long getVideoDuration(String videoPath) throws IOException {
        File videoFile = new File(videoPath);
        if (!videoFile.exists() || videoFile.length() == 0) {
            throw new IOException("Video file does not exist or is empty: " + videoPath);
        }

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(videoPath);
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            if (time == null) {
                throw new IOException("Could not extract duration from video file");
            }
            long duration = Long.parseLong(time);
            return duration; // Duration in milliseconds
        } catch (Exception e) {
            throw new IOException("Error reading video duration: " + e.getMessage(), e);
        } finally {
            try {
                retriever.release();
            } catch (Exception e) {
                // Ignore cleanup errors
            }
        }
    }


    private void updateFolderName() {
        if (FOLDER_NAME.equals("video_function")) {
            FOLDER_NAME = "video_" + System.currentTimeMillis();
        } else if (FOLDER_NAME.equals("video_function_a")) {
            FOLDER_NAME = "video_function_b";
        } else if (FOLDER_NAME.equals("video_function_b")) {
            FOLDER_NAME = "video_function_c";
        } else if (FOLDER_NAME.equals("video_function_c")) {
            FOLDER_NAME = "video_function_d";
        } else if (FOLDER_NAME.equals("video_function_d")) {
            FOLDER_NAME = "video_function_e";
        } else if (FOLDER_NAME.equals("video_function_e")) {
            FOLDER_NAME = "video_function_f";
        } else if (FOLDER_NAME.equals("video_function_f")) {
            FOLDER_NAME = "video_function_g";
        } else if (FOLDER_NAME.equals("video_function_g")) {
            FOLDER_NAME = "video_function_h";
        } else if (FOLDER_NAME.equals("video_function_h")) {
            FOLDER_NAME = "video_function_i";
        }
        prefManager.setString(Constant.FOLDER_NAME, FOLDER_NAME);
    }

    private void handleSuccess() {
        if (InterstitialAdManager.isLoaded() && prefManager.getInt(Constant.CLICK) >= prefManager.getInt(Constant.INTERSTITIAL_AD_CLICK)) {
            prefManager.setInt(Constant.CLICK, 0);
            InterstitialAdManager.showAds();
            showPreviewDialog();
        } else {
            prefManager.setInt(Constant.CLICK, prefManager.getInt(Constant.CLICK) + 1);
            showPreviewDialog();
        }
    }

    private void handleFailure(ReturnCode returnCode) {
        progressDD.dismiss();
        Util.showToast(PosterActivity.this, "Try Again!!");
        updateFolderName();
        Log.e("FFmpegError", "Command execution failed with rc=" + returnCode.getValue());
    }

    private String saveImage(Bitmap paramBitmap) {
        File directory = new File(Environment.getExternalStorageDirectory().toString()
                + File.separator + Environment.DIRECTORY_DOWNLOADS + File.separator + "." + FOLDER_NAME + File.separator);

        if (!directory.exists()) {
            directory.mkdirs();
        }
        File file = new File(directory, "Image-Bitmap.png");
        if (file.exists()) {
            file.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            paramBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
            this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return file.getAbsolutePath();

//        File directory = new File(Environment.getExternalStorageDirectory().toString()
//                + File.separator + Environment.DIRECTORY_DOWNLOADS + File.separator + "." + FOLDER_NAME + File.separator);
//
//        if (!directory.exists()) {
//            directory.mkdirs();
//        }
//        File file = new File(directory, "Image-Bitmap.png");
//        if (file.exists()) {
//            file.delete();
//        }
//        try {
//            OutputStream outputStream = new FileOutputStream(file);
//            paramBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
//            outputStream.close();
//
//            ContentValues values = new ContentValues();
//            values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
//            this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
//            return file.getAbsolutePath();
//        } catch (Exception e) {
//            return "";
//        }
    }

    private String saveBitmap(Bitmap paramBitmap) {

        String directory = new StorageUtils(activity).getPackageStorageDir("/." + "crop" + "/").getAbsolutePath();

        File file = new File(directory + "/" + "ImgCrop" + ".png");

        if (file.exists()) {
            file.delete();
        }
        try {
            OutputStream outputStream = new FileOutputStream(file);
            paramBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.close();

            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
            this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            return file.getAbsolutePath();
        } catch (Exception e) {
            return "";
        }
    }

    public Uri getImageContentUri(File imageFile) {
        if (isVideo) {
            return Uri.parse(imageFile.getAbsolutePath());
        } else {
            return Uri.parse(imageFile.getAbsolutePath());
            /*String filePath = imageFile.getAbsolutePath();
            Cursor cursor = this.getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Images.Media._ID},
                    MediaStore.Images.Media.DATA + "=? ",
                    new String[]{filePath}, null);
            if (cursor != null && cursor.moveToFirst()) {
                @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
                cursor.close();
                return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + id);
            }*/
        }
//        return null;
    }

    public void shareFileImageUri(Uri path, String name, String shareTo, boolean isVideo) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);

        switch (shareTo) {
            case "whtsapp":
                shareIntent.setPackage("com.whatsapp");
                break;
            case "fb":
                shareIntent.setPackage("com.facebook.katana");
                break;
            case "insta":
                shareIntent.setPackage("com.instagram.android");
                break;
            case "twter":
                shareIntent.setPackage("com.twitter.android");
                break;
        }

        if (isVideo) {
            shareIntent.setDataAndType(path, "video/*");
        } else {
            shareIntent.setDataAndType(path, "image/*");
        }
        shareIntent.putExtra(Intent.EXTRA_STREAM, path);
        if (!name.equals("")) {
            shareIntent.putExtra(Intent.EXTRA_TEXT, name);
        }
        startActivity(Intent.createChooser(shareIntent, MyApplication.context.getString(R.string.share_via)));
    }

    private Bitmap viewToBitmap(View view) {
        Bitmap createBitmap = null;
        view.setDrawingCacheEnabled(true);
        view.setDrawingCacheQuality(DRAWING_CACHE_QUALITY_HIGH);
        try {
            createBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
            view.draw(new Canvas(createBitmap));
            return createBitmap;
        } catch (Exception e) {
            Util.showErrorLog(e.getMessage(), e);
            return createBitmap;
        } finally {
            view.destroyDrawingCache();
        }
    }

    private void updateShadow(String direction) {
        AutofitTextRel autofitTextRel = (AutofitTextRel) getCurrentSTR_info();
        if (direction.equals("UP")) {
            autofitTextRel.setTopBottomShadow(textInfo.getTopBottomShadow() - 2.0f);
        }
        if (direction.equals("DOWN")) {
            autofitTextRel.setTopBottomShadow(textInfo.getTopBottomShadow() + 2.0f);
        }
        if (direction.equals("LEFT")) {
            autofitTextRel.setLeftRightShadow(textInfo.getLeftRighShadow() - 2.0f);
        }
        if (direction.equals("RIGHT")) {
            autofitTextRel.setLeftRightShadow(textInfo.getLeftRighShadow() + 2.0f);
        }
    }

    private void updateTextPos(String direction) {
        AutofitTextRel autofitTextRel = (AutofitTextRel) getCurrentSTR_info();
        if (direction.equals("UP")) {
            autofitTextRel.decY();
        }
        if (direction.equals("DOWN")) {
            autofitTextRel.incrY();
        }
        if (direction.equals("LEFT")) {
            autofitTextRel.decX();
        }
        if (direction.equals("RIGHT")) {
            autofitTextRel.incrX();
        }
    }

    private void updateImgPos(String direction) {
        RelStickerView relStickerView = (RelStickerView) getCurrentSTR_info();
        if (direction.equals("UP")) {
            relStickerView.decY();
        }
        if (direction.equals("DOWN")) {
            relStickerView.incrY();
        }
        if (direction.equals("LEFT")) {
            relStickerView.decX();
        }
        if (direction.equals("RIGHT")) {
            relStickerView.incrX();
        }
    }

    private void setRotateProg(String axis, int progress) {
        int childCount = binding.txtStkrRel.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = binding.txtStkrRel.getChildAt(i);
            if (childAt instanceof AutofitTextRel) {
                AutofitTextRel autofitTextRel = (AutofitTextRel) childAt;
                if (autofitTextRel.getBorderVisibility()) {
                    autofitTextRel.setRotateProg(axis, progress);
                }
            }
            if (childAt instanceof RelStickerView) {
                RelStickerView relStickerView = (RelStickerView) childAt;
                if (relStickerView.getBorderVisbilty()) {
                    relStickerView.setRotateProg(axis, progress);
                }
            }
        }
    }

    private void flipView(String axis) {
        int childCount = binding.txtStkrRel.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = binding.txtStkrRel.getChildAt(i);
            if (childAt instanceof AutofitTextRel) {
                AutofitTextRel autofitTextRel = (AutofitTextRel) childAt;
                if (autofitTextRel.getBorderVisibility()) {
                    autofitTextRel.flipView(axis);
                }
            }
            if (childAt instanceof RelStickerView) {
                RelStickerView relStickerView = (RelStickerView) childAt;
                if (relStickerView.getBorderVisbilty()) {
                    relStickerView.flipView(axis);
                }
            }
        }
    }

    private void setBackground(String background, PostItem intentPostItem, String aspectRatio) {
        if (intentPostItem.is_video) {
            isVideo = true;
            Drawable drawable = getDrawable(R.drawable.frame_02);
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            bitmapRatio("1:1", bitmap);
        } else {
            isVideo = false;
            RequestBuilder<Bitmap> asBitmap = Glide.with(getApplicationContext()).asBitmap();
            RequestOptions requestOptions = new RequestOptions().skipMemoryCache(true);
            float f = this.screenWidth;
            float f2 = this.screenHeight;
            if (f <= f2) {
                f = f2;
            }

            if (!background.isEmpty()) {
                asBitmap.apply(requestOptions.override((int) (this.screenWidth > this.screenHeight
                                ? this.screenWidth : this.screenHeight))).load(background)
                        .placeholder(R.drawable.spaceholder)
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                bitmapRatio(aspectRatio, resource);
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {

                            }
                        });
            } else {
                asBitmap.apply(requestOptions.override((int) (this.screenWidth > this.screenHeight
                                ? this.screenWidth : this.screenHeight))).load(intentPostItem.image_url)
                        .placeholder(R.drawable.spaceholder)
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                bitmapRatio(aspectRatio, resource);
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {

                            }
                        });
            }
        }
    }

    private void bitmapRatio(String aspectRatio, Bitmap bitmap) {
        if (aspectRatio != null) {

            String[] split = aspectRatio.split(":");

            if (!aspectRatio.equals("")) {
                if (aspectRatio.equals("1:1")) {
                    bitmap = cropInRatio(bitmap, 1, 1);
                } else if (aspectRatio.equals("16:9")) {
                    bitmap = cropInRatio(bitmap, 16, 9);
                } else if (aspectRatio.equals("9:16")) {
                    bitmap = cropInRatio(bitmap, 9, 16);
                } else if (aspectRatio.equals("4:3")) {
                    bitmap = cropInRatio(bitmap, 4, 3);
                } else if (aspectRatio.equals("4:5")) {
                    bitmap = cropInRatio(bitmap, 4, 5);
                } else if (aspectRatio.equals("3:4")) {
                    bitmap = cropInRatio(bitmap, 3, 4);
                } else {
                    bitmap = cropInRatio(bitmap, Integer.parseInt(split[0]), Integer.parseInt(split[1]));
                }
            }
            Bitmap resizeBitmap = PosterUtils.newResizeBitmap(bitmap, (int) this.screenWidth, (int) this.screenHeight);
            setImageBitmapToLayout(resizeBitmap);
        } else {
            Util.showToast(activity, "Layout Ratio Error");
        }
    }

    private void setImageBitmapToLayout(Bitmap resizeBitmap) {
        Util.showLog("FIN " + "W: " + resizeBitmap.getWidth() + "H: " + resizeBitmap.getHeight());
        binding.mainRel.getLayoutParams().width = resizeBitmap.getWidth();
        binding.mainRel.getLayoutParams().height = resizeBitmap.getHeight();
        binding.mainRel.postInvalidate();
        binding.mainRel.requestLayout();
        if (isVideo) {
            binding.videoPlayer.setVisibility(VISIBLE);
            binding.videoPlayer.setLayoutParams(new RelativeLayout.LayoutParams(resizeBitmap.getWidth(), resizeBitmap.getHeight()));
            if (absPlayerInternal != null) {
                absPlayerInternal.setPlayWhenReady(false);
                absPlayerInternal.stop();
                absPlayerInternal.seekTo(0);
            }
            binding.centerRel.getLayoutParams().width = resizeBitmap.getWidth();
            binding.centerRel.getLayoutParams().height = resizeBitmap.getHeight();
            binding.backgroundImg.setVisibility(GONE);
            loadVideo(intentPostItem.image_url);
        }
        binding.backgroundImg.setImageBitmap(resizeBitmap);
        bit = resizeBitmap;
        binding.mainRel.post(() -> {
            try {
                float ow = (float) bit.getWidth();
                float oh = (float) bit.getHeight();
//                bit = ImageUtils.resizeBitmap(bit,
//                        binding.centerRel.getWidth(),
//                        binding.centerRel.getHeight());
                float nh = (float) bit.getHeight();
                wr = ((float) bit.getWidth()) / ow;
                hr = nh / oh;
                setCustomData();
                loadFrame();
            } catch (Exception e) {
                e.printStackTrace();
                Util.showErrorLog(e.getMessage(), e);
            }
        });
    }

    @OptIn(markerClass = UnstableApi.class)
    private void loadVideo(String videoURL) {
        Glide.with(this)
                .load(videoURL)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.icThumb);

        binding.icThumb.setVisibility(VISIBLE);
        binding.videoPlayer.setVisibility(VISIBLE);
        binding.videoPlayer.setShutterBackgroundColor(ContextCompat.getColor(this, R.color.yellow_50));
        binding.videoPlayer.setDefaultArtwork(ContextCompat.getDrawable(this, R.drawable.spaceholder));

        binding.videoPlayer.setVisibility(VISIBLE);
        binding.videoPlayer.setUseController(false);
        binding.videoPlayer.setControllerHideOnTouch(true);
        binding.videoPlayer.setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS);

        absPlayerInternal = new ExoPlayer.Builder(this).build(); //creating a player instance
        MediaItem mediaItem = MediaItem.fromUri(Uri.parse(videoURL));
        absPlayerInternal.setMediaItem(mediaItem);
        absPlayerInternal.prepare();
        absPlayerInternal.setPlayWhenReady(true); // start loading video and play it at the moment a chunk of it is available offline
        absPlayerInternal.play();

        binding.videoPlayer.setPlayer(absPlayerInternal);

        absPlayerInternal.addListener(
                new Player.Listener() {

                    @Override
                    public void onPlaybackStateChanged(int playbackState) {
                        Player.Listener.super.onPlaybackStateChanged(playbackState);
                        switch (playbackState) {
                            case ExoPlayer.STATE_ENDED:
                                absPlayerInternal.seekTo(0);
                                absPlayerInternal.setPlayWhenReady(true);
                                break;
                            case ExoPlayer.STATE_READY:
                                binding.icThumb.setVisibility(View.INVISIBLE);
                                binding.videoPlayer.setVisibility(VISIBLE);
                                binding.ivPlayVideo.setVisibility(View.INVISIBLE);
                        }
                    }
                });

    }

    public Bitmap cropInRatio(Bitmap bitmap, int rX, int rY) {
        Bitmap bit = null;
        float Width = (float) bitmap.getWidth();
        float Height = (float) bitmap.getHeight();
        float newHeight = getNewHeight(rX, rY, Width, Height);
        float newWidth = getNewWidth(rX, rY, Width, Height);
        if (newWidth <= Width && newWidth < Width) {
            bit = Bitmap.createBitmap(bitmap, (int) ((Width - newWidth) / 2.0f), 0, (int) newWidth, (int) Height);
        }
        if (newHeight <= Height && newHeight < Height) {
            bit = Bitmap.createBitmap(bitmap, 0, (int) ((Height - newHeight) / 2.0f), (int) Width, (int) newHeight);
        }
        return (newWidth == Width && newHeight == Height) ? bitmap : bit;
    }

    private void loadFrame() {
        userViewModel.getFrameData(prefManager.getString(Constant.USER_ID)).observe(this, result -> {
            if (result != null) {
                switch (result.status) {
                    case LOADING:
                        // Loading State
                        // Data are from Local DB
                        if (result.data != null) {
                            Util.showErrorLog("SSS", "Frame Data Loading State : " + result.data.toString());
                            userFrames.clear();
                            for (UserFrame userFrame : result.data) {
                                userFrames.add(userFrame);
                            }
                        }
                        break;
                    case SUCCESS:
                        // Success State
                        // Data are from Server
                        if (result.data != null) {
                            userFrames.clear();
                            Util.showErrorLog("SSS", "Frame Data Success State : " + result.data.toString());
                            for (UserFrame userFrame : result.data) {
                                userFrames.add(userFrame);
                            }
                        }

                        break;
                    case ERROR:
                        Util.showErrorLog("SSS", "Frame Data Error State  : ERROR ");
//                        prgDialog.dismiss();
                        // Error State
                        break;
                    default:
                        // Default

                        break;
                }
            }
        });

        postViewModel.getFrameDB().observe(this, frameData -> {
            if (frameData != null && frameData.size() > 0) {
                binding.textNotFrame.setVisibility(GONE);
                binding.rvFrame.setVisibility(VISIBLE);
                setFrameData(frameData);
            } else {
                binding.textNotFrame.setVisibility(VISIBLE);
                binding.rvFrame.setVisibility(GONE);
            }
        });

        postViewModel.getDBFrameCategoryData().observe(this, resource -> {
            if (resource != null && resource.size() > 0) {
                setFrameCategory(resource);
            }
        });

    }

    private void setFrameCategory(List<FrameCategoryItem> resource) {
        Chip firstChip = new Chip(this);
        firstChip.setText("All");
        firstChip.setId(10000);
        firstChip.setChipBackgroundColorResource(R.color.bg_chip);
        firstChip.setCheckable(true);
        firstChip.setTextColor(getColor(R.color.tc_chip));
        firstChip.setTextAppearance(R.style.chitText);
        firstChip.setCheckedIconVisible(false);
        firstChip.setChecked(true);

        binding.cgFrameCate.addView(firstChip);

        for (FrameCategoryItem item : resource) {

            Chip chip = new Chip(this);
            chip.setId(item.frameCategoryId);
            chip.setText(item.frameCategoryName);
            chip.setChipBackgroundColorResource(R.color.bg_chip);
            chip.setCheckable(true);
            chip.setTextColor(getColor(R.color.tc_chip));
            chip.setTextAppearance(R.style.chitText);
            chip.setCheckedIconVisible(false);
            chip.setTag(item.frameCategoryId);

            binding.cgFrameCate.addView(chip);

        }

        Chip lastChip = new Chip(this);
        lastChip.setText("My Frame");
        lastChip.setId(9999);
        lastChip.setChipBackgroundColorResource(R.color.bg_chip);
        lastChip.setCheckable(true);
        lastChip.setTextColor(getColor(R.color.tc_chip));
        lastChip.setTextAppearance(R.style.chitText);
        lastChip.setCheckedIconVisible(false);

        binding.cgFrameCate.addView(lastChip);

        postViewModel.setFrameObj(ratio, "All");

        binding.cgFrameCate.setOnCheckedStateChangeListener(new ChipGroup.OnCheckedStateChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull ChipGroup group, @NonNull List<Integer> checkedIds) {
                frameCategory = getChipName(group.getCheckedChipId());
                if (group.getCheckedChipId() == 9999) {
                    if (userFrames.size() > 0) {
                        binding.textNotFrame.setVisibility(GONE);
                        binding.rvFrame.setVisibility(VISIBLE);
                        frameItemList.clear();
                        for (int i = 0; i < userFrames.size(); i++) {
                            frameItemList.add(new DynamicFrameItem("USER", "USER",
                                    userFrames.get(i).imageUrl, "USER", true, "custom"));
                        }
                        frameAdapter.setFrameItemList(frameItemList);
                    }

                } else {
                    postViewModel.setFrameObj(ratio, frameCategory);
                }
            }
        });
    }

    public String getChipName(int id) {
        String name = "";
        int childCount = binding.cgFrameCate.getChildCount();
        for (int i = 0; i < childCount; i++) {
            Chip chip = (Chip) binding.cgFrameCate.getChildAt(i);
            if (chip.getId() == id) {
                name = chip.getText().toString();
                break;
            }

        }
        return name;
    }

    public static <T> List<T> removeDuplicates(List<T> list) {
        // Create a new ArrayList
        List<T> newList = new ArrayList<T>();
        // Traverse through the first list
        for (T element : list) {
            // If this element is not present in newList
            // then add it
            boolean isPresent = false;
            for (T item : newList) {
                if (((DynamicFrameItem) element).thumbnail == ((DynamicFrameItem) item).thumbnail) {
                    newList.remove(item);
                    isPresent = true;
                }
            }
            if (!isPresent)
                newList.add(element);
        }
        // return the new list
        return newList;
    }

    private void setFrameData(List<DynamicFrameItem> data) {
        String[] split = ratio.split(":");
        float wrRat = Float.parseFloat(split[0]) / Float.parseFloat(split[1]);
        if (data != null && data.size() > 0) {
            frameAdapter = new DynamicFrameAdapter(this, new ClickListener<DynamicFrameItem>() {
                @Override
                public void onClick(DynamicFrameItem data) {
                    if (!userItem.isSubscribed && data.is_paid) {
                        dialogMsg.showWarningDialog(getString(R.string.premium), getString(R.string.please_subscribe_frame), getString(R.string.subscribe),
                                true);
                        dialogMsg.show();
                        dialogMsg.okBtn.setOnClickListener(view -> {
                            dialogMsg.cancel();
                            startActivity(new Intent(PosterActivity.this, SubsPlanActivity.class));
                        });
                        return;
                    }
                    int childCount = binding.txtStkrRel.getChildCount();

                    for (int i = 0; i < childCount; i++) {
                        View childAt = binding.txtStkrRel.getChildAt(i);
                        binding.txtStkrRel.removeView(childAt);
                    }
                    binding.txtStkrRel.removeAllViews();
                    prgDialog.show();
                    setCustomData();
                    setUpFrameData(data);
                }
            }, wrRat);
            frameItemList.clear();
            ori_frameItemList.clear();
            ori_frameItemList.addAll(data);
            for (int i = 0; i < data.size(); i++) {
                frameItemList.add(data.get(i));
            }
            frameAdapter.setFrameItemList(frameItemList);

            if (frameItemList.size() > 0) {
                if (!frameLoaded) {
                    frameLoaded = true;
                    if (getIntent().getStringExtra(Constant.INTENT_TYPE).equals(Constant.CUSTOM_EDITABLE)) {

                    } else {
                        setUpFrameData(frameItemList.get(0));
                    }
                }
            }
            binding.rvFrame.setAdapter(frameAdapter);
        }
    }

    private void setUpFrameData(DynamicFrameItem dynamicFrameItem) {
        Log.e("SB", "setUpFrameData dynamicFrameItem :" + dynamicFrameItem);
        prgDialog.show();
        if (dynamicFrameItem != null) {

            if (dynamicFrameItem.name.equals("USER") && dynamicFrameItem.aspectRatio.equals("USER")) {
                stickerInfoArrayList.clear();
                mItemArray.clear();
                textInfoArrayList.clear();

                binding.ivFrame.setVisibility(VISIBLE);
                Log.e("SB", "setUpFrameData thumbnail:" + dynamicFrameItem.thumbnail);
                GlideBinding.bindImage(binding.ivFrame, dynamicFrameItem.thumbnail);
                prgDialog.dismiss();
            } else {
                binding.ivFrame.setVisibility(GONE);
                try {
                    JSONObject json = new JSONObject(dynamicFrameItem.frameData);
                    JSONArray layers = json.getJSONArray("layers");

                    // Create a new JSONArray without null values
                    JSONArray cleanedLayers = new JSONArray();

                    for (int i = 0; i < layers.length(); i++) {
                        Object layerObject = layers.opt(i);
                        if (layerObject != null && !JSONObject.NULL.equals(layerObject)) {
                            cleanedLayers.put(layerObject);
                        }
                    }
                    stickerInfoArrayList.clear();
                    mItemArray.clear();
                    textInfoArrayList.clear();

                    for (int i = 0; i < cleanedLayers.length(); i++) {
                        Object layerObject = cleanedLayers.get(i);

                        Log.d("test", "setUpFrameData: " + layerObject);

                        if (layerObject instanceof JSONObject) {
                            JSONObject jsonObject1 = (JSONObject) layerObject;
                            processJson(i, jsonObject1, dynamicFrameItem.name);
                        } else {
                            Log.w("test", "setUpFrameData: Unexpected type at index " + i);
                        }
                    }

                    LoadStickersAsync loadStickersAsync = new LoadStickersAsync();
                    loadStickersAsync.execute();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setCustomData() {
        if (getIntent().hasExtra(Constant.INTENT_TYPE)) {
            if (getIntent().getStringExtra(Constant.INTENT_TYPE).equals(Constant.CUSTOM_EDITABLE)) {
                prgDialog.show();
                LoadCustomAsync loadCustomAsync = new LoadCustomAsync();
                loadCustomAsync.execute();
            }
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

        } else {

            realX = String.valueOf((Float.parseFloat(x) * 100)
                    / Float.parseFloat(bgObj.getString("width")));
            realY = String.valueOf((Float.parseFloat(y) * 100)
                    / Float.parseFloat(bgObj.getString("height")));

            calcWidth = String.valueOf(Float.parseFloat(width) * 100
                    / templateRealWidth + Float.parseFloat(realX));
            calcHeight = String.valueOf(Float.parseFloat(height) * 100
                    / templateRealHeight + Float.parseFloat(realY));

        }

        if (type != null) {

            if (type.contains("image")) {


                String stickerUrl = "uploads/template/" + name + ""
                        + jsonObject1.getString("src").replace("..", "");

                if (jsonObject1.getString("name").equals("logo")) {
                    String str = saveSticker(name, businessItem.name, businessItem.logo);
                } else if (jsonObject1.getString("name").equals("candidate")) {
                    String userImage = prefManager.getString(Constant.USER_IMAGE);
                    if (userImage != null && !userImage.isEmpty()) {
                        // Delete cached default candidate.png so user's photo is downloaded
                        String dir = new StorageUtils(activity).getPackageStorageDir("/."
                                + name + "/").getAbsolutePath();
                        File cachedFile = new File(dir + "/candidate.png");
                        if (cachedFile.exists()) {
                            cachedFile.delete();
                        }
                        String str = saveSticker(name, "candidate", userImage);
                    }
                } else {
                    if (prefManager.getString(Constant.DIGITAL_ENABLE).equals(Config.ONE)) {
                        String str = saveSticker(name, jsonObject1.getString("name"),
                                prefManager.getString(Constant.DIGITAL_END_URL) + stickerUrl);
                    } else {
                        String str = saveSticker(name, jsonObject1.getString("name"),
                                Config.APP_API_URL + stickerUrl);
                    }
                }
                String directory = new StorageUtils(activity).getPackageStorageDir("/." + name + "/").getAbsolutePath();

                String stickerPath;
                if (jsonObject1.getString("name").equals("logo")) {
                    stickerPath = directory + "/" + businessItem.name + ".png";
                } else if (jsonObject1.getString("name").equals("candidate")) {
                    stickerPath = directory + "/candidate.png";
                } else {
                    stickerPath = directory + "/" + jsonObject1.getString("name") + ".png";
                }
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
                if (layerName.equals("name")) {
                    text = businessItem.name;
                } else if (layerName.equals("mobile")) {
                    text = businessItem.phone;
                } else if (layerName.equals("email")) {
                    text = businessItem.email;
                } else if (layerName.equals("website") && !businessItem.website.equals("DEMO")) {
                    text = businessItem.website;
                } else if (layerName.equals("address")) {
                    text = businessItem.address;
                }

//                if (businessItem.website.equals("DEMO")) {
//                    binding.cbWebsite.setEnabled(false);
//                    binding.cbWebsite.setChecked(false);
//                } else {
//                    binding.cbWebsite.setEnabled(true);
//                    binding.cbWebsite.setChecked(true);
//                }

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


                String directory = new StorageUtils(activity).getPackageStorageDir("/."
                        + "font" + "/").getAbsolutePath();

                File file = new File(directory + "/" + font + ".ttf");
                Util.showLog("FILE: " + file.exists() + " FILE : " + file.getName());
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

    }

    private String saveSticker(String templateName, String name, String stickerUrl) {
        String directory = new StorageUtils(activity).getPackageStorageDir("/." + templateName + "/").getAbsolutePath();

        File file = new File(directory + "/" + name + ".png");
        String[] newPath = {""};
        Util.showLog("FILE: " + file.exists() + " FILE : " + file.getName());
        if (!file.exists()) {
            LoadLogo loadLogo = new LoadLogo(stickerUrl, directory, name, new SaveListener() {
                @Override
                public void onSave(String path) {
                    newPath[0] = path;
                }
            });
            loadLogo.execute();
            return newPath[0];
        } else {
            return file.getAbsolutePath();
        }
    }

    class LoadLogo extends AsyncTask<String, String, String> {

        private String message = "", verifyStatus = "0", urls, directory, name;
        Drawable drawable;
        Bitmap bitmap2;
        SaveListener saveListener;

        public LoadLogo(String urls, String directory, String name, SaveListener listener) {
            this.urls = urls;
            this.directory = directory;
            this.name = name;
            this.saveListener = listener;
        }

        @Override
        protected String doInBackground(String... strings) {
            Util.showLog("URL: " + "Sticker Image Url" + urls);
            bitmap2 = null;
            InputStream inputStream;
            try {
                inputStream = new java.net.URL(urls).openStream();
                bitmap2 = BitmapFactory.decodeStream(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
                prgDialog.dismiss();
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
                saveListener.onSave(filePath);
            } catch (Exception e) {
                e.printStackTrace();
                Util.showErrorLog(e.getMessage(), e);
                prgDialog.dismiss();
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

            Util.showLog("URL: " + font_url);

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
            font_url = prefManager.getString(Constant.DIGITAL_END_URL) + "uploads/template/" + mZipName + "/fonts/"
                    + mName + ext;
        } else {
            font_url = Config.APP_API_URL + "uploads/template/" + mZipName + "/fonts/" + mName + ext;
        }
        return font_url;
    }

    public interface SaveListener {
        void onSave(String path);
    }

    private class LoadStickersAsync extends AsyncTask<String, String, Boolean> {
        private LoadStickersAsync() {
        }

        @Override
        public void onPreExecute() {
            super.onPreExecute();
        }

        public Boolean doInBackground(String... strArr) {
            ArrayList<ElementInfo> stickerArrayList;
            ArrayList<TextInfo> newtextInfoArrayList;
            String str;

            newtextInfoArrayList = new ArrayList<>();
            stickerArrayList = new ArrayList<>();


            for (int i = 0; i < stickerInfoArrayList.size(); i++) {
                int newWidht = getNewSTRWidth(Float.valueOf(stickerInfoArrayList.get(i).getSt_x_pos()).floatValue(),
                        Float.valueOf(stickerInfoArrayList.get(i).getSt_width()).floatValue());
                int newHeight = getNewSTRHeight(Float.valueOf(stickerInfoArrayList.get(i).getSt_y_pos()).floatValue(),
                        Float.valueOf(stickerInfoArrayList.get(i).getSt_height()).floatValue());
//                int i2 = newWidht < 10 ? 20 : (newWidht <= 10 || newWidht > 20) ? newWidht : 35;
//                int i3 = newHeight < 10 ? 20 : (newHeight <= 10 || newHeight > 20) ? newHeight : 35;
                int i2 = newWidht;
                int i3 = newHeight;
                if (stickerInfoArrayList.get(i).getSt_field2() != null) {
                    str = stickerInfoArrayList.get(i).getSt_field2();
                } else {
                    str = "";
                }
                float parseInt = (stickerInfoArrayList.get(i).getSt_rotation() == null ||
                        stickerInfoArrayList.get(i).getSt_rotation().equals("")) ? 0.0f :
                        (float) Integer.parseInt(stickerInfoArrayList.get(i).getSt_rotation());

                float xpos = getXpos(Float.valueOf(stickerInfoArrayList.get(i).getSt_x_pos()).floatValue());
                float ypos = getYpos(Float.valueOf(stickerInfoArrayList.get(i).getSt_y_pos()).floatValue());
                ElementInfo el = new ElementInfo(stickerInfoArrayList.get(i).getName(), 0, xpos,
                        ypos,
                        i2,
                        i3,
                        parseInt, 0.0f, "",
                        "STICKER",
                        Integer.parseInt(stickerInfoArrayList.get(i).getSt_order()),
                        0, 255, 0, 0, 0, 0,
                        stickerInfoArrayList.get(i).getSt_image(), "colored",
                        1, 0, str, "", "", null, null);

                stickerArrayList.add(el);
            }
            for (int i5 = 0; i5 < textInfoArrayList.size(); i5++) {

                TextSTRInfo tsInfo = textInfoArrayList.get(i5);

                String text = textInfoArrayList.get(i5).getText();


                String font_family = textInfoArrayList.get(i5).getFont_family();


                int parseColor = Color.parseColor(textInfoArrayList.get(i5).getTxt_color());
                float xpos2 = getXpos(Float.valueOf(textInfoArrayList.get(i5).getTxt_x_pos()).floatValue());
                float ypos = getYpos(Float.valueOf(textInfoArrayList.get(i5).getTxt_y_pos()).floatValue());
                int newWidht2 = getNewSTRWidth(Float.valueOf(textInfoArrayList.get(i5).getTxt_x_pos()).floatValue(), Float.valueOf(textInfoArrayList.get(i5).getTxt_width()).floatValue());
                int newHeit2 = getNewSTRHeight(Float.valueOf(textInfoArrayList.get(i5).getTxt_y_pos()).floatValue(),
                        Float.valueOf(textInfoArrayList.get(i5).getTxt_height()).floatValue());

                boolean isBorder = false;
                if (textInfoArrayList.get(i5).getLineSize() != 0) {
                    isBorder = true;
                }

                boolean isShadow = false;
                if (textInfoArrayList.get(i5).getSdDistance() != -11111) {
                    isShadow = true;
                }

                TextInfo ti = new TextInfo(textInfoArrayList.get(i5).getName(), 0, text, font_family, parseColor, 100,
                        isShadow ? Color.parseColor(tsInfo.getSdColor()) : 0,
                        isShadow ? tsInfo.getSdBlur() : 0,
                        "0",
                        ViewCompat.MEASURED_STATE_MASK, 0,
                        xpos2,
                        ypos,
                        newWidht2,
                        newHeit2,
                        Float.parseFloat(textInfoArrayList.get(i5).getTxt_rotation()),
                        "TEXT", Integer.parseInt(textInfoArrayList.get(i5).getTxt_order()),
                        0, 0, 0, 0, 0, "",
                        "", "",
                        isShadow ? Util.getXDistance(tsInfo.getSdDistance(), tsInfo.getSdAngle()) : 0.0f,
                        isShadow ? Util.getYDistance(tsInfo.getSdDistance(), tsInfo.getSdAngle()) : 0.0f,
                        isBorder ? tsInfo.getLineSize() : 0,
                        isBorder ? Color.parseColor(tsInfo.getLineColor()) : 0,
                        isBorder ? tsInfo.getLineOpacity() : 100,
                        textInfoArrayList.get(i5).getJustification(), textInfoArrayList.get(i5).getUppercase());
                newtextInfoArrayList.add(ti);
            }


            txtShapeList.clear();
            Iterator<TextInfo> it = newtextInfoArrayList.iterator();
            while (it.hasNext()) {
                TextInfo next = it.next();
                txtShapeList.put(Integer.valueOf(next.getORDER()), next);
            }
            Iterator<ElementInfo> it2 = stickerArrayList.iterator();
            while (it2.hasNext()) {
                ElementInfo next2 = it2.next();
                Log.d("next2", "doInBackground: " + next2);
                txtShapeList.put(Integer.valueOf(next2.getORDER()), next2);
            }

            ArrayList arrayList = new ArrayList(txtShapeList.keySet());
            Collections.sort(arrayList);
            int size = arrayList.size();

            for (int i = 0; i < size; i++) {
                Object obj = txtShapeList.get(arrayList.get(i));

                if (obj instanceof ElementInfo) {
                    ElementInfo elementInfo = (ElementInfo) obj;
                    String stkr_path = elementInfo.getSTKR_PATH();
                    if (stkr_path.equals("")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                RelStickerView stickerView = new RelStickerView(activity, false);
                                binding.txtStkrRel.addView(stickerView);
                                stickerView.optimizeScreen(screenWidth, screenHeight);
                                stickerView.setMainLayoutWH((float) binding.mainRel.getWidth(), (float) binding.mainRel.getHeight());
                                stickerView.setComponentInfo(elementInfo);
                                stickerView.setId(ViewIdGenerator.generateViewId());
                                stickerView.optimize(wr, hr);
                                stickerView.setOnTouchCallbackListener(PosterActivity.this);
                                stickerView.setBorderVisibility(false);
                                if (elementInfo.getName().contains("frame")) {
                                    stickerView.isMultiTouchEnabled = stickerView.setDefaultTouchListener(false);
                                }
                                sizeFull++;
                            }
                        });
                    } else {
                        File file2 = new File(stkr_path);
                        if (file2.exists()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    RelStickerView stickerView2 = new RelStickerView(activity, false);
                                    binding.txtStkrRel.addView(stickerView2);
                                    stickerView2.optimizeScreen(screenWidth, screenHeight);
                                    stickerView2.setMainLayoutWH((float) binding.mainRel.getWidth(), (float) binding.mainRel.getHeight());
                                    stickerView2.setComponentInfo(elementInfo);
                                    stickerView2.setId(ViewIdGenerator.generateViewId());
                                    stickerView2.optimize(wr, hr);
                                    stickerView2.setOnTouchCallbackListener(PosterActivity.this);
                                    stickerView2.setBorderVisibility(false);
                                    if (elementInfo.getName().contains("frame")) {
                                        stickerView2.isMultiTouchEnabled = stickerView2.setDefaultTouchListener(false);
                                    }
                                    sizeFull++;
                                }
                            });
                        } else if (file2.getName().replace(".png", "").length() < 7) {
//                                dialogShow = false;
//                                new SaveStickersAsync(obj).execute(stkr_path);
                        } else {
                            sizeFull++;
                        }
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AutofitTextRel autofitTextRel = new AutofitTextRel(activity);
                            binding.txtStkrRel.addView(autofitTextRel);
                            TextInfo textInfo = (TextInfo) obj;


                            if (textInfo.getFONT_NAME() != null) {

                                setTextFonts(textInfo.getFONT_NAME());

                            }

                            if (!textInfo.getJustification().equals("")) {
                                if (textInfo.getJustification().equals("center")) {
                                    autofitTextRel.setCenterAlignMent();
                                } else if (textInfo.getJustification().equals("left")) {
                                    autofitTextRel.setLeftAlignMent();
                                } else if (textInfo.getJustification().equals("right")) {
                                    autofitTextRel.setRightAlignMent();
                                }
                            }

                            if (!textInfo.getUPPERCASE().equals("")) {
                                if (textInfo.getUPPERCASE().equals("1")) {
                                    autofitTextRel.setCapitalFont();
                                } else if (textInfo.getUPPERCASE().equals("0")) {
                                    autofitTextRel.setLowerFont();
                                }
                            }

                            autofitTextRel.setTextInfo(textInfo, false);
                            autofitTextRel.setId(ViewIdGenerator.generateViewId());
                            autofitTextRel.optimize(wr, hr);
                            autofitTextRel.setOnTouchCallbackListener(PosterActivity.this);
                            autofitTextRel.setBorderVisibility(false);
                            fontName = textInfo.getFONT_NAME();
                            tColor = textInfo.getTEXT_COLOR();
                            shadowColor = textInfo.getSHADOW_COLOR();
                            shadowProg = textInfo.getSHADOW_PROG();
                            tAlpha = textInfo.getTEXT_ALPHA();
                            bgDrawable = textInfo.getBG_DRAWABLE();
                            bgAlpha = textInfo.getBG_ALPHA();
                            rotation = textInfo.getROTATION();
                            bgColor = textInfo.getBG_COLOR();
                            outerColor = textInfo.getOutLineColor();
                            outerSize = textInfo.getOutLineSize();
                            leftRightShadow = (int) textInfo.getLeftRighShadow();
                            topBottomShadow = (int) textInfo.getTopBottomShadow();
                            topBottomShadow = (int) textInfo.getTopBottomShadow();
                            sizeFull++;
                        }
                    });
                }
            }

            if (txtShapeList.size() == sizeFull) {
                try {
//                    dialogIs.dismiss();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }

            return true;
        }

        @Override
        public void onPostExecute(Boolean bool) {
            super.onPostExecute(bool);
//            saveBitmapUndu();
            prgDialog.dismiss();
        }
    }

    private class LoadCustomAsync extends AsyncTask<String, String, Boolean> {
        private LoadCustomAsync() {
        }

        @Override
        public void onPreExecute() {
            super.onPreExecute();
        }

        public Boolean doInBackground(String... strArr) {
            ArrayList<ElementInfo> stickerArrayList;
            ArrayList<TextInfo> newtextInfoArrayList;
            String str;

            newtextInfoArrayList = new ArrayList<>();
            stickerArrayList = new ArrayList<>();

            for (int i = 0; i < strPostList.size(); i++) {
                int newWidht = getNewSTRWidth(Float.valueOf(strPostList.get(i).getSt_x_pos()).floatValue(),
                        Float.valueOf(strPostList.get(i).getSt_width()).floatValue());
                int newHeight = getNewSTRHeight(Float.valueOf(strPostList.get(i).getSt_y_pos()).floatValue(),
                        Float.valueOf(strPostList.get(i).getSt_height()).floatValue());
                int i2 = newWidht;
                int i3 = newHeight;
                if (strPostList.get(i).getSt_field2() != null) {
                    str = strPostList.get(i).getSt_field2();
                } else {
                    str = "";
                }
                float parseInt = (strPostList.get(i).getSt_rotation() == null ||
                        strPostList.get(i).getSt_rotation().equals("")) ? 0.0f :
                        (float) Integer.parseInt(strPostList.get(i).getSt_rotation());

                float xpos = getXpos(Float.valueOf(strPostList.get(i).getSt_x_pos()).floatValue());
                float ypos = getYpos(Float.valueOf(strPostList.get(i).getSt_y_pos()).floatValue());
                ElementInfo el = new ElementInfo(strPostList.get(i).getName(), 0, xpos,
                        ypos,
                        i2,
                        i3,
                        parseInt, 0.0f, "",
                        "STICKER",
                        Integer.parseInt(strPostList.get(i).getSt_order()),
                        0, 255, 0, 0, 0, 0,
                        strPostList.get(i).getSt_image(), "colored",
                        1, 0, str, "", "", null, null);

                stickerArrayList.add(el);
            }
            for (int i5 = 0; i5 < textPostList.size(); i5++) {

                TextSTRInfo tsInfo = textPostList.get(i5);

                String text = textPostList.get(i5).getText();


                String font_family = textPostList.get(i5).getFont_family();


                int parseColor = Color.parseColor(textPostList.get(i5).getTxt_color());
                float xpos2 = getXpos(Float.valueOf(textPostList.get(i5).getTxt_x_pos()).floatValue());
                float ypos = getYpos(Float.valueOf(textPostList.get(i5).getTxt_y_pos()).floatValue());
                int newWidht2 = getNewSTRWidth(Float.valueOf(textPostList.get(i5).getTxt_x_pos()).floatValue(),
                        Float.valueOf(textPostList.get(i5).getTxt_width()).floatValue());
                int newHeit2 = getNewSTRHeight(Float.valueOf(textPostList.get(i5).getTxt_y_pos()).floatValue(),
                        Float.valueOf(textPostList.get(i5).getTxt_height()).floatValue());

                boolean isBorder = false;
                if (textPostList.get(i5).getLineSize() != 0) {
                    isBorder = true;
                }

                boolean isShadow = false;
                if (textPostList.get(i5).getSdDistance() != -11111) {
                    isShadow = true;
                }

                TextInfo ti = new TextInfo(textPostList.get(i5).getName(), 0, text, font_family, parseColor, 100,
                        isShadow ? Color.parseColor(tsInfo.getSdColor()) : 0,
                        isShadow ? tsInfo.getSdBlur() : 0,
                        "0",
                        ViewCompat.MEASURED_STATE_MASK, 0,
                        xpos2,
                        ypos,
                        newWidht2,
                        newHeit2,
                        Float.parseFloat(textPostList.get(i5).getTxt_rotation()),
                        "TEXT", Integer.parseInt(textPostList.get(i5).getTxt_order()),
                        0, 0, 0, 0, 0, "",
                        "", "",
                        isShadow ? Util.getXDistance(tsInfo.getSdDistance(), tsInfo.getSdAngle()) : 0.0f,
                        isShadow ? Util.getYDistance(tsInfo.getSdDistance(), tsInfo.getSdAngle()) : 0.0f,
                        isBorder ? tsInfo.getLineSize() : 0,
                        isBorder ? Color.parseColor(tsInfo.getLineColor()) : 0,
                        isBorder ? tsInfo.getLineOpacity() : 100,
                        textPostList.get(i5).getJustification(), textPostList.get(i5).getUppercase());
                newtextInfoArrayList.add(ti);
                Util.showLog("Text: " + ti.toString());
            }

            txtShapeList.clear();
            Iterator<TextInfo> it = newtextInfoArrayList.iterator();
            while (it.hasNext()) {
                TextInfo next = it.next();
                txtShapeList.put(Integer.valueOf(next.getORDER()), next);
            }
            Iterator<ElementInfo> it2 = stickerArrayList.iterator();
            while (it2.hasNext()) {
                ElementInfo next2 = it2.next();
                txtShapeList.put(Integer.valueOf(next2.getORDER()), next2);
            }

            ArrayList arrayList = new ArrayList(txtShapeList.keySet());
            Collections.sort(arrayList);
            int size = arrayList.size();


            for (int i = 0; i < size; i++) {
                Object obj = txtShapeList.get(arrayList.get(i));
                if (obj instanceof ElementInfo) {
                    ElementInfo elementInfo = (ElementInfo) obj;
                    String stkr_path = elementInfo.getSTKR_PATH();
                    if (stkr_path.equals("")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                RelStickerView stickerView = new RelStickerView(activity, false);
                                binding.txtStkrRel.addView(stickerView);
                                stickerView.optimizeScreen(screenWidth, screenHeight);
                                stickerView.setMainLayoutWH((float) binding.mainRel.getWidth(), (float) binding.mainRel.getHeight());
                                stickerView.setComponentInfo(elementInfo);
                                stickerView.setId(ViewIdGenerator.generateViewId());
                                stickerView.optimize(wr, hr);
                                stickerView.setOnTouchCallbackListener(PosterActivity.this);
                                stickerView.setBorderVisibility(false);
                                if (elementInfo.getName().contains("frame")) {
                                    stickerView.isMultiTouchEnabled = stickerView.setDefaultTouchListener(false);
                                }
                                sizeFull++;
                            }
                        });
                    } else {
                        File file2 = new File(stkr_path);
                        if (file2.exists()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    RelStickerView stickerView2 = new RelStickerView(activity, false);
                                    binding.txtStkrRel.addView(stickerView2);
                                    stickerView2.optimizeScreen(screenWidth, screenHeight);
                                    stickerView2.setMainLayoutWH((float) binding.mainRel.getWidth(), (float) binding.mainRel.getHeight());
                                    stickerView2.setComponentInfo(elementInfo);
                                    stickerView2.setId(ViewIdGenerator.generateViewId());
                                    stickerView2.optimize(wr, hr);
                                    stickerView2.setOnTouchCallbackListener(PosterActivity.this);
                                    stickerView2.setBorderVisibility(false);
                                    if (elementInfo.getName().contains("frame")) {
                                        stickerView2.isMultiTouchEnabled = stickerView2.setDefaultTouchListener(false);
                                    }
                                    sizeFull++;
                                }
                            });
                        } else if (file2.getName().replace(".png", "").length() < 7) {
//                                dialogShow = false;
//                                new SaveStickersAsync(obj).execute(stkr_path);
                        } else {
                            sizeFull++;
                        }
                    }
                } else {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AutofitTextRel autofitTextRel = new AutofitTextRel(activity);
                            binding.txtStkrRel.addView(autofitTextRel);
                            TextInfo textInfo = (TextInfo) obj;


                            if (textInfo.getFONT_NAME() != null) {

                                setTextFonts(textInfo.getFONT_NAME());

                            }

                            if (!textInfo.getJustification().equals("")) {
                                if (textInfo.getJustification().equals("center")) {
                                    autofitTextRel.setCenterAlignMent();
                                } else if (textInfo.getJustification().equals("left")) {
                                    autofitTextRel.setLeftAlignMent();
                                } else if (textInfo.getJustification().equals("right")) {
                                    autofitTextRel.setRightAlignMent();
                                }
                            }

                            if (!textInfo.getUPPERCASE().equals("")) {
                                if (textInfo.getUPPERCASE().equals("1")) {
                                    autofitTextRel.setCapitalFont();
                                } else if (textInfo.getUPPERCASE().equals("0")) {
                                    autofitTextRel.setLowerFont();
                                }
                            }

                            autofitTextRel.setTextInfo(textInfo, false);
                            autofitTextRel.setId(ViewIdGenerator.generateViewId());
                            autofitTextRel.optimize(wr, hr);
                            autofitTextRel.setOnTouchCallbackListener(PosterActivity.this);
                            autofitTextRel.setBorderVisibility(false);
                            fontName = textInfo.getFONT_NAME();
                            tColor = textInfo.getTEXT_COLOR();
                            shadowColor = textInfo.getSHADOW_COLOR();
                            shadowProg = textInfo.getSHADOW_PROG();
                            tAlpha = textInfo.getTEXT_ALPHA();
                            bgDrawable = textInfo.getBG_DRAWABLE();
                            bgAlpha = textInfo.getBG_ALPHA();
                            rotation = textInfo.getROTATION();
                            bgColor = textInfo.getBG_COLOR();
                            outerColor = textInfo.getOutLineColor();
                            outerSize = textInfo.getOutLineSize();
                            leftRightShadow = (int) textInfo.getLeftRighShadow();
                            topBottomShadow = (int) textInfo.getTopBottomShadow();
                            sizeFull++;
                        }
                    });
                }
            }

            if (txtShapeList.size() == sizeFull) {
                try {
//                    dialogIs.dismiss();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }

            return true;
        }

        @Override
        public void onPostExecute(Boolean bool) {
            super.onPostExecute(bool);
//            saveBitmapUndu();
            prgDialog.dismiss();
        }
    }

    private void showLayers() {
        getLayoutChild(true);

        Log.d("testMItem", "showLayers: " + mItemArray);
        /*layersAdapter = new LayersAdapter(mItemArray, new StartDragListener() {
            @Override
            public void requestDrag(RecyclerView.ViewHolder viewHolder) {
                if (viewHolder instanceof LayersAdapter.MyViewHolder) {
                    LayersAdapter.MyViewHolder myViewHolder =
                            (LayersAdapter.MyViewHolder) viewHolder;
                    Util.showLog("SS" + myViewHolder.mTitle.getText().toString());
                }
            }

            @Override
            public void onDragEnd(RecyclerView.ViewHolder viewHolder) {
                for (int size = mItemArray.size() - 1; size >= 0; size--) {
                    ((View) ((Pair) mItemArray.get(size)).second).bringToFront();
                }
                binding.txtStkrRel.requestLayout();
                binding.txtStkrRel.postInvalidate();
            }

            @Override
            public void onDelete(int position) {
                for (int size = mItemArray.size() - 1; size >= 0; size--) {
                    if (size == position) {
                        binding.txtStkrRel.removeView(((View) ((Pair) mItemArray.get(size)).second));
                        break;
                    }
                }
                binding.txtStkrRel.requestLayout();
                binding.txtStkrRel.postInvalidate();
                layersAdapter.notifyDataSetChanged();
                binding.llLayer.setVisibility(GONE);
            }
        });
        ItemTouchHelper.Callback callback =
                new ItemMoveCallback(layersAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(binding.rvViewOrder);*/
        binding.dragListView.getRecyclerView().setVerticalScrollBarEnabled(true);
        binding.dragListView.setDragListListener(new DragListView.DragListListenerAdapter() {
            public void onItemDragStarted(int i) {
            }

            public void onItemDragEnded(int i, int i2) {
                if (i != i2) {
                    for (int size = mItemArray.size() - 1; size >= 0; size--) {
                        ((View) ((Pair) mItemArray.get(size)).second).bringToFront();
                    }
                    binding.txtStkrRel.requestLayout();
                    binding.txtStkrRel.postInvalidate();
                }
            }
        });
        binding.dragListView.setLayoutManager(new LinearLayoutManager(this));
        NewDragDrop itemAdapter = new NewDragDrop(this, mItemArray,
                R.layout.list_item, R.id.ivDrag, false, new StartDragListener() {
            @Override
            public void requestDrag(RecyclerView.ViewHolder viewHolder) {

            }

            @Override
            public void onDragEnd(RecyclerView.ViewHolder viewHolder) {

            }

            @Override
            public void onDelete(int position) {
                for (int size = mItemArray.size() - 1; size >= 0; size--) {
                    if (size == position) {
                        binding.txtStkrRel.removeView(((View) ((Pair) mItemArray.get(size)).second));
                        break;
                    }
                }
                binding.txtStkrRel.requestLayout();
                binding.txtStkrRel.postInvalidate();
                binding.llLayer.setVisibility(GONE);
            }
        });
        binding.dragListView.setAdapter(itemAdapter, true);
        binding.dragListView.setCanDragHorizontally(false);
        binding.dragListView.setCustomDragItem(new MyDragItem(this, R.layout.list_item));

        if (mItemArray.size() > 0) {

        } else {
            Util.showToast(activity, "No Any Items");
            binding.llLayer.setVisibility(GONE);
        }
    }

    private static class MyDragItem extends DragItem {
        MyDragItem(Context context, int i) {
            super(context, i);
        }

        public void onBindDragView(View view, View view2) {
//            ((LinearLayout) view2.findViewById(R.id.ll)).setBackgroundColor(createBitmap);
        }
    }

    public void getLayoutChild(boolean z) {
        if (z) {
            this.mItemArray.clear();
        }
        int i = 0;
        if (binding.txtStkrRel.getChildCount() != 0) {
            if (z) {
//                this.lay_Notext.setVisibility(View.GONE);
            }
            for (int childCount2 = binding.txtStkrRel.getChildCount() - 1; childCount2 >= 0; childCount2--) {
                if (z) {
                    this.mItemArray.add(new Pair(Long.valueOf((long) childCount2),
                            binding.txtStkrRel.getChildAt(childCount2)));
                }
            }
        } else {
//            this.lay_Notext.setVisibility(View.VISIBLE);
        }
    }

    public void removeViewControl() {
        Log.e("SB", "removeViewControl := ");
        int childCount = binding.txtStkrRel.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = binding.txtStkrRel.getChildAt(i);
            if (childAt instanceof AutofitTextRel) {
                ((AutofitTextRel) childAt).setBorderVisibility(false);
            }
            if (childAt instanceof RelStickerView) {
                ((RelStickerView) childAt).setBorderVisibility(false);
            }
        }
    }

    public void setTextFonts(String str) {
        this.fontName = str;
        int childCount = binding.txtStkrRel.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = binding.txtStkrRel.getChildAt(i);
            if (childAt instanceof AutofitTextRel) {
                AutofitTextRel autofitTextRel = (AutofitTextRel) childAt;
                if (autofitTextRel.getBorderVisibility()) {

                    autofitTextRel.setTextFont(str);

                }
            }
        }
    }

    public View getCurrentSTR_info() {
        int childCount = binding.txtStkrRel.getChildCount();
        View view = null;
        for (int i = 0; i < childCount; i++) {
            View childAt = binding.txtStkrRel.getChildAt(i);
            if (childAt instanceof AutofitTextRel) {
                AutofitTextRel autofitTextRel = (AutofitTextRel) childAt;
                if (autofitTextRel.getBorderVisibility()) {
                    textInfo = autofitTextRel.getTextInfo();
                    view = autofitTextRel;
                    break;
                }
            }
            if (childAt instanceof RelStickerView) {
                RelStickerView relStickerView = (RelStickerView) childAt;
                if (relStickerView.getBorderVisbilty()) {
                    stickerInfo = ((RelStickerView) relStickerView).getComponentInfo();
                    view = relStickerView;
                    break;
                }
            }
        }
        if (isImage) {
            for (int i = 0; i < binding.shapeRel.getChildCount(); i++) {
                View childAt = binding.shapeRel.getChildAt(i);
                if (childAt instanceof RelStickerView) {
                    RelStickerView relStickerView = (RelStickerView) childAt;
                    if (relStickerView.getBorderVisbilty() && relStickerView.getIsImage()) {
                        stickerInfo = ((RelStickerView) relStickerView).getComponentInfo();
                        view = relStickerView;
                        break;
                    }
                }
            }
        }
        return view;
    }

    private void setAlignCenter() {
        AutofitTextRel autofitTextRel = (AutofitTextRel) getCurrentSTR_info();
        autofitTextRel.setCenterAlignMent();
    }

    private void setAlignLeft() {
        AutofitTextRel autofitTextRel = (AutofitTextRel) getCurrentSTR_info();
        autofitTextRel.setLeftAlignMent();
    }

    private void setAlignRight() {
        AutofitTextRel autofitTextRel = (AutofitTextRel) getCurrentSTR_info();
        autofitTextRel.setRightAlignMent();
    }

    private void setAlignAllCap() {
        AutofitTextRel autofitTextRel = (AutofitTextRel) getCurrentSTR_info();
        autofitTextRel.setCapitalFont();
    }

    private void setLowerFont() {
        AutofitTextRel autofitTextRel = (AutofitTextRel) getCurrentSTR_info();
        autofitTextRel.setLowerFont();
    }

    private void setFistCapFont() {
        AutofitTextRel autofitTextRel = (AutofitTextRel) getCurrentSTR_info();
        autofitTextRel.setFirstLetterCap();
    }

    private void setUnderLineFont() {
        AutofitTextRel autofitTextRel = (AutofitTextRel) getCurrentSTR_info();
        autofitTextRel.setUnderLineFont();
    }

    private void setBoldFont() {
        AutofitTextRel autofitTextRel = (AutofitTextRel) getCurrentSTR_info();
        autofitTextRel.setBoldFont();
    }

    private void setItalicFont() {
        AutofitTextRel autofitTextRel = (AutofitTextRel) getCurrentSTR_info();
        autofitTextRel.setItalicFont();
    }

    private void setCenterLineFont() {
        AutofitTextRel autofitTextRel = (AutofitTextRel) getCurrentSTR_info();
        autofitTextRel.setCenterLineFont();
    }

    private List<String> fontList() {
        String[] list;
        List<String> fonts_array = new ArrayList<>();
        list = getResources().getStringArray(R.array.fonts);
        if (list != null && list.length > 0) {
            // This is a folder
            fonts_array.clear();
            for (String file : list) {
                if (file.endsWith(".ttf") || file.endsWith(".otf")) {
                    fonts_array.add(file);
                }
            }
        }

        File directory = new File(new StorageUtils(this).getPackageStorageDir("/." + "font" + "/")
                .getAbsolutePath());

        for (File file : directory.listFiles()) {
            if (file.getName().endsWith(".ttf") || file.getName().endsWith(".TTF") || file.getName().endsWith(".otf")) {
                fonts_array.add(file.getName());
            }
        }

        Collections.reverse(fonts_array);

        return fonts_array;
    }

    public void addTextDialog(TextInfo originTextInfo) {
        final Dialog dialog = new Dialog(this, R.style.ThemeWithCorners);
        dialog.setContentView(R.layout.add_auto_text_dialog);
        dialog.setCancelable(false);
        AutoFitEditText autoFitEditText = (AutoFitEditText) dialog.findViewById(R.id.auto_fit_edit_text);
        Button button = (Button) dialog.findViewById(R.id.btnCancelDialog);
        Button button2 = (Button) dialog.findViewById(R.id.btnAddTextSDialog);
        if (originTextInfo != null) {
            autoFitEditText.setText(originTextInfo.getTEXT());
        } else {
            autoFitEditText.setText("");
        }
        button.setOnClickListener(view -> dialog.dismiss());
        button2.setOnClickListener(view -> {
            if (autoFitEditText.getText().toString().trim().length() > 0) {
                String replace = autoFitEditText.getText().toString().replace("\n", " ");
                if (isEditMode) {
                    AutofitTextRel autofitTextRel = (AutofitTextRel) getCurrentSTR_info();
                    autofitTextRel.setText(replace);
                } else {
                    TextInfo textInfo = new TextInfo();
                    textInfo.setTEXT("Text_" + new Random().nextInt(10));
                    textInfo.setTEXT(replace);
                    textInfo.setFONT_NAME(this.fontName);
                    textInfo.setTEXT_COLOR(ViewCompat.MEASURED_STATE_MASK);
                    textInfo.setTEXT_ALPHA(100);
                    textInfo.setSHADOW_COLOR(ViewCompat.MEASURED_STATE_MASK);
                    textInfo.setSHADOW_PROG(0);
                    textInfo.setBG_COLOR(ViewCompat.MEASURED_STATE_MASK);
                    textInfo.setBG_DRAWABLE("0");
                    textInfo.setBG_ALPHA(0);
                    textInfo.setROTATION(0.0f);
                    textInfo.setFIELD_TWO("");
                    textInfo.setPOS_X((float) ((binding.txtStkrRel.getWidth() / 2) - ImageUtils.dpToPx(this, 100)));
                    textInfo.setPOS_Y((float) ((binding.txtStkrRel.getHeight() / 2) - ImageUtils.dpToPx(this, 100)));
                    textInfo.setWIDTH(ImageUtils.dpToPx(this, 200));
                    textInfo.setHEIGHT(ImageUtils.dpToPx(this, 200));
                    try {
                        AutofitTextRel autofitTextRel2 = new AutofitTextRel(this);
                        binding.txtStkrRel.addView(autofitTextRel2);
                        autofitTextRel2.setTextInfo(textInfo, false);
                        autofitTextRel2.setId(ViewIdGenerator.generateViewId());
                        autofitTextRel2.setOnTouchCallbackListener(this);
                        autofitTextRel2.setBorderVisibility(true);
                    } catch (ArrayIndexOutOfBoundsException e2) {
                        e2.printStackTrace();
                    }
                }
                dialog.dismiss();
                return;
            }
            Toast.makeText(this, "Please enter text here.", Toast.LENGTH_SHORT).show();
        });
        dialog.show();
    }

    private void addImageDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.add_image_dialog);
        dialog.setCancelable(false);
        TextView textView = (TextView) dialog.findViewById(R.id.txtTitle);
        TextView fitEditText = (TextView) dialog.findViewById(R.id.auto_fit_edit_text);
        LinearLayout btnGallery = (LinearLayout) dialog.findViewById(R.id.btnGallery);
        LinearLayout btnCamera = (LinearLayout) dialog.findViewById(R.id.btnCamera);
        View btnCancel = dialog.findViewById(R.id.btnCancel);

        ImageView ivClose = dialog.findViewById(R.id.ivClose);

        GlideBinding.setTextSize(textView, "font_title_size");
        GlideBinding.setTextSize(fitEditText, "font_body_size");

        btnCamera.setOnClickListener(v -> {
            dialog.dismiss();
            CURRENT_CLICK = 3;
            Dexter.withContext(this).withPermissions(PERMISSIONS).withListener(new MultiplePermissionsListener() {
                public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                    if (multiplePermissionsReport.areAllPermissionsGranted()) {
                        onCameraButtonClick();
                    }
                    if (multiplePermissionsReport.isAnyPermissionPermanentlyDenied()) {
                        showSettingsDialog();
                    }
                }

                public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                    permissionToken.continuePermissionRequest();
                }
            }).withErrorListener(new PermissionRequestErrorListener() {
                public void onError(DexterError dexterError) {
//                    Toast.makeText(activity, "Error occurred! ", Toast.LENGTH_SHORT).show();
                    permissionsList = new ArrayList<>();
                    permissionsList.addAll(Arrays.asList(PERMISSIONS));
                    askForPermissions(permissionsList);
                }
            }).onSameThread().check();
        });

        btnGallery.setOnClickListener(v -> {
            dialog.dismiss();
            CURRENT_CLICK = 2;
            Dexter.withContext(this).withPermissions(PERMISSIONS).withListener(new MultiplePermissionsListener() {
                public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                    if (multiplePermissionsReport.areAllPermissionsGranted()) {
                        onGalleryButtonClick();
                    }
                    if (multiplePermissionsReport.isAnyPermissionPermanentlyDenied()) {
                        showSettingsDialog();
                    }
                }

                public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                    permissionToken.continuePermissionRequest();
                }
            }).withErrorListener(new PermissionRequestErrorListener() {
                public void onError(DexterError dexterError) {
//                    Toast.makeText(activity, "Error occurred! ", Toast.LENGTH_SHORT).show();
                    permissionsList = new ArrayList<>();
                    permissionsList.addAll(Arrays.asList(PERMISSIONS));
                    askForPermissions(permissionsList);
                }
            }).onSameThread().check();
        });

        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
        });

        ivClose.setOnClickListener(view -> {
            dialog.dismiss();
        });

        dialog.show();

    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Need Permissions");
        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.");
        builder.setPositiveButton("GOTO SETTINGS", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
                Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
                intent.setData(Uri.fromParts("package", getPackageName(), (String) null));
                startActivityForResult(intent, 101);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.show();
    }

    private void onGalleryButtonClick() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction("android.intent.action.GET_CONTENT");
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_image).toString()), SELECT_PICTURE_GALLERY);
    }

    private void onCameraButtonClick() {
        uri = FileProvider.getUriForFile(
                getApplicationContext(),
                BuildConfig.APPLICATION_ID + "." + "provider",
                createCameraFile()
        );
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra("output", uri);
        startActivityForResult(intent, SELECT_PICTURE_CAMERA);
    }

    private File createCameraFile() {
        File image = null;
        String dateTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMG_" + dateTime + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        try {
            image = File.createTempFile(imageFileName, ".jpg", storageDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        int i3 = requestCode;
        int i4 = resultCode;
        if (i4 == -1) {
            if (data != null || i3 == SELECT_PICTURE_CAMERA) {
                if (i3 == SELECT_PICTURE_GALLERY) {
                    try {
                        Uri fromFile = Uri.fromFile(new File(getCacheDir(),
                                "SampleCropImage" + System.currentTimeMillis() + ".png"));
                        if (isChangeMode) {
                            UCrop.Options options = new UCrop.Options();
                            options.withAspectRatio((float) selectedRatio.get(0), (float) selectedRatio.get(1));
                            options.setToolbarColor(getResources().getColor(R.color.primary_dark_color));
                            options.setStatusBarColor(getResources().getColor(R.color.primary_dark_color));
                            options.setToolbarWidgetColor(getResources().getColor(R.color.white));
                            options.setActiveControlsWidgetColor(getResources().getColor(R.color.primary_color));
                            options.setToolbarTitle(getString(R.string.ucrop_label_edit_photo));
                            options.setFreeStyleCropEnabled(false);
                            UCrop.of(data.getData(), fromFile).withOptions(options).start(PosterActivity.this);
                        } else {
                            UCrop.Options options2 = new UCrop.Options();
                            options2.setToolbarColor(getResources().getColor(R.color.primary_dark_color));
                            options2.setStatusBarColor(getResources().getColor(R.color.primary_dark_color));
                            options2.setToolbarWidgetColor(getResources().getColor(R.color.white));
                            options2.setActiveControlsWidgetColor(getResources().getColor(R.color.primary_color));
                            options2.setToolbarTitle(getString(R.string.ucrop_label_edit_photo));
                            options2.setFreeStyleCropEnabled(true);
                            UCrop.of(data.getData(), fromFile).withOptions(options2).start(PosterActivity.this);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (i3 == SELECT_PICTURE_CAMERA) {
                    try {
                        Uri fromFile2 = Uri.fromFile(new File(MyApplication.context.getCacheDir(),
                                "SampleCropImage" + System.currentTimeMillis() + ".png"));
                        UCrop.Options options3 = new UCrop.Options();
                        if (isChangeMode) {
                            UCrop.Options options = new UCrop.Options();
                            options.withAspectRatio((float) selectedRatio.get(0), (float) selectedRatio.get(1));
                            options.setToolbarColor(getResources().getColor(R.color.primary_dark_color));
                            options.setStatusBarColor(getResources().getColor(R.color.primary_dark_color));
                            options.setToolbarWidgetColor(getResources().getColor(R.color.white));
                            options.setActiveControlsWidgetColor(getResources().getColor(R.color.primary_color));
                            options.setToolbarTitle(getString(R.string.ucrop_label_edit_photo));
                            options.setFreeStyleCropEnabled(false);
                            UCrop.of(data.getData(), fromFile2).withOptions(options).start(PosterActivity.this);
                        } else {
                            options3.setToolbarColor(getResources().getColor(R.color.primary_dark_color));
                            options3.setStatusBarColor(getResources().getColor(R.color.primary_dark_color));
                            options3.setToolbarWidgetColor(getResources().getColor(R.color.white));
                            options3.setActiveControlsWidgetColor(getResources().getColor(R.color.primary_color));
                            options3.setToolbarTitle(getString(R.string.ucrop_label_edit_photo));
                            options3.setFreeStyleCropEnabled(true);
                            UCrop.of(uri, fromFile2).withOptions(options3).start(PosterActivity.this);
                        }
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
                if (i4 == -1 && i3 == 69) {
                    handleCropResult(data);
                } else if (i4 == 96) {
                    UCrop.getError(data);
                }
            }
        }

        if (resultCode == RESULT_OK && requestCode == 1024) {
            if (eraserResultBmp != null && isChangeMode) {
                dialogMsg.cancel();
                RelStickerView relStickerView = (RelStickerView) getCurrentSTR_info();
                relStickerView.setMainImageBitmap(eraserResultBmp);
            } else {
                if (eraserResultBmp != null) {
                    dialogMsg.cancel();
                    addSticker(eraserResultBmp);
                }
            }
        }
    }

    private void handleCropResult(Intent data) {

        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), UCrop.getOutput(data));
        } catch (IOException e) {
            e.printStackTrace();
        }

        BGConfig.currentBit = bitmap;
        selectedBit = bitmap;

        selectedShapesImage = dialogMsg.normalPreviewImage;
        dialogMsg.showRemoveBGDialog();
        dialogMsg.show();
        GlideBinding.bindImage(dialogMsg.normalPreviewImage, UCrop.getOutput(data).getPath());
        dialogMsg.llShape.setVisibility(VISIBLE);
        dialogMsg.normalShape.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogMsg.normalShape.setBackground(getDrawable(R.drawable.black_border));
                dialogMsg.ovalShape.setBackground(null);
                dialogMsg.diamondShape.setBackground(null);
                dialogMsg.squareShape.setBackground(null);
                GlideBinding.bindImage(dialogMsg.normalPreviewImage, UCrop.getOutput(data).getPath());
                selectedShapesImage(dialogMsg.normalPreviewImage);
                dialogMsg.btnUseThis.setVisibility(GONE);
                selectedShapesImage = dialogMsg.normalPreviewImage;
            }
        });
        dialogMsg.ovalShape.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogMsg.normalShape.setBackground(null);
                dialogMsg.ovalShape.setBackground(getDrawable(R.drawable.black_border));
                dialogMsg.diamondShape.setBackground(null);
                dialogMsg.squareShape.setBackground(null);
                GlideBinding.bindImage(dialogMsg.ovalPreviewImage, UCrop.getOutput(data).getPath());
                selectedShapesImage(dialogMsg.ovalPreviewImage);
                dialogMsg.btnUseThis.setVisibility(VISIBLE);
                selectedShapesImage = dialogMsg.ovalPreviewImage;
            }
        });
        dialogMsg.diamondShape.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogMsg.normalShape.setBackground(null);
                dialogMsg.ovalShape.setBackground(null);
                dialogMsg.diamondShape.setBackground(getDrawable(R.drawable.black_border));
                dialogMsg.squareShape.setBackground(null);
                GlideBinding.bindImage(dialogMsg.diamondPreviewImage, UCrop.getOutput(data).getPath());
                selectedShapesImage(dialogMsg.diamondPreviewImage);
                dialogMsg.btnUseThis.setVisibility(VISIBLE);
                selectedShapesImage = dialogMsg.diamondPreviewImage;
            }
        });
        dialogMsg.squareShape.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogMsg.normalShape.setBackground(null);
                dialogMsg.ovalShape.setBackground(null);
                dialogMsg.diamondShape.setBackground(null);
                dialogMsg.squareShape.setBackground(getDrawable(R.drawable.black_border));
                GlideBinding.bindImage(dialogMsg.squarePreviewImage, UCrop.getOutput(data).getPath());
                selectedShapesImage(dialogMsg.squarePreviewImage);
                dialogMsg.btnUseThis.setVisibility(VISIBLE);
                selectedShapesImage = dialogMsg.squarePreviewImage;
            }
        });
        dialogMsg.btnUseThis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedBit = selectedShapesImage.getBitmap();
                dialogMsg.cancel();
                if (isChangeMode) {
                    RelStickerView relStickerView = (RelStickerView) getCurrentSTR_info();
                    relStickerView.setMainImageBitmap(selectedBit);
                } else {
                    addSticker(selectedBit);
                }

            }
        });
        dialogMsg.btnReal.setOnClickListener(v -> {

            if (dialogMsg.lvRemove.getVisibility() == View.VISIBLE) {

                dialogMsg.cancel();
                if (isChangeMode) {
                    RelStickerView relStickerView = (RelStickerView) getCurrentSTR_info();
                    relStickerView.setMainImageBitmap(selectedBit);
                } else {
                    addSticker(selectedBit);
                }

            } else {
                dialogMsg.scanAnimation.setVisibility(View.VISIBLE);
                dialogMsg.btnReal.setEnabled(false);
                dialogMsg.btnNo.setEnabled(false);

                new MLCropAsyncTask(new MLOnCropTaskCompleted() {
                    public void onTaskCompleted(Bitmap bitmap, Bitmap bitmap2, int left, int top) {
                        /*int[] iArr = {0, 0, selectedBit.getWidth(), selectedBit.getHeight()};
                        int width = selectedBit.getWidth();
                        int height = selectedBit.getHeight();
                        int i = width * height;
                        selectedBit.getPixels(new int[i], 0, width, 0, 0, width, height);
                        int[] iArr2 = new int[i];
                        Bitmap createBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                        createBitmap.setPixels(iArr2, 0, width, 0, 0, width, height);
                        cutBit = ImageUtils.getMask(AddBusinessActivity.this, selectedBit, createBitmap, width, height);
                       */
                        Bitmap resizedBitmap = Bitmap.createScaledBitmap(
                                bitmap, selectedBit.getWidth(), selectedBit.getHeight(), false);
                        cutBit = resizedBitmap;

                        runOnUiThread(new Runnable() {
                            public void run() {
                                Palette p = Palette.from(cutBit).generate();
                                if (p.getDominantSwatch() == null) {
                                    Toast.makeText(PosterActivity.this, "OKK", Toast.LENGTH_SHORT).show();
                                }
                                Util.showLog("BG COMPLETE");
                                dialogMsg.ivRemove.setImageBitmap(resizedBitmap);
                                dialogMsg.lvRemove.setVisibility(View.VISIBLE);
                                dialogMsg.btnReal.setEnabled(true);
                                dialogMsg.btnNo.setEnabled(true);
                                dialogMsg.btnReal.setText("Use This");
                                dialogMsg.btnNo.setText("Change Image");
                                dialogMsg.scanAnimation.setVisibility(View.GONE);
                            }
                        });


                    }
                }, PosterActivity.this).execute(new Void[0]);
            }

        });
        dialogMsg.btnRemove.setOnClickListener(v -> {
            dialogMsg.cancel();
            if (isChangeMode) {
                RelStickerView relStickerView = (RelStickerView) getCurrentSTR_info();
                relStickerView.setMainImageBitmap(cutBit);
            } else {
                addSticker(cutBit);
            }
        });
        dialogMsg.btnNo.setOnClickListener(v -> {
            if (dialogMsg.lvRemove.getVisibility() == View.VISIBLE) {
                dialogMsg.cancel();
                addImageDialog();
            } else {
                dialogMsg.cancel();
                if (isChangeMode) {
                    RelStickerView relStickerView = (RelStickerView) getCurrentSTR_info();
                    relStickerView.setMainImageBitmap(selectedBit);
                } else {
                    addSticker(selectedBit);
                }
            }
        });

        dialogMsg.btnManual.setOnClickListener(v -> {
            EraserActivity.b = selectedBit;
            Intent intent = new Intent(PosterActivity.this, EraserActivity.class);
            intent.putExtra(Constant.KEY_OPEN_FROM, Constant.OPEN_FROM_POSTER);
            startActivityForResult(intent, 1024);
        });
    }

    private void selectedShapesImage(ShapesImage img) {
        int imgId = img.getId();
        if (imgId == R.id.normalPreviewImage) {
            dialogMsg.normalPreviewImage.setVisibility(View.VISIBLE);
            dialogMsg.ovalPreviewImage.setVisibility(View.GONE);
            dialogMsg.diamondPreviewImage.setVisibility(View.GONE);
            dialogMsg.squarePreviewImage.setVisibility(View.GONE);
        } else if (imgId == R.id.ovalPreviewImage) {
            dialogMsg.normalPreviewImage.setVisibility(View.GONE);
            dialogMsg.ovalPreviewImage.setVisibility(View.VISIBLE);
            dialogMsg.diamondPreviewImage.setVisibility(View.GONE);
            dialogMsg.squarePreviewImage.setVisibility(View.GONE);
        } else if (imgId == R.id.diamondPreviewImage) {
            dialogMsg.normalPreviewImage.setVisibility(View.GONE);
            dialogMsg.ovalPreviewImage.setVisibility(View.GONE);
            dialogMsg.diamondPreviewImage.setVisibility(View.VISIBLE);
            dialogMsg.squarePreviewImage.setVisibility(View.GONE);
        } else if (imgId == R.id.squarePreviewImage) {
            dialogMsg.normalPreviewImage.setVisibility(View.GONE);
            dialogMsg.ovalPreviewImage.setVisibility(View.GONE);
            dialogMsg.diamondPreviewImage.setVisibility(View.GONE);
            dialogMsg.squarePreviewImage.setVisibility(View.VISIBLE);
        }
    }

    private void addSticker(Bitmap bitmap) {
        ElementInfo elementInfo = new ElementInfo();
        elementInfo.setName("Image_" + new Random().nextInt(10));
        elementInfo.setPOS_X((float) ((binding.mainRel.getWidth() / 2)));
        elementInfo.setPOS_Y((float) ((binding.mainRel.getHeight() / 2)));
        elementInfo.setWIDTH(ImageUtils.dpToPx(MyApplication.context, 140));
        elementInfo.setHEIGHT(ImageUtils.dpToPx(MyApplication.context, 140));
        elementInfo.setROTATION(0.0f);
        elementInfo.setRES_ID("");
        elementInfo.setBITMAP(bitmap);
        elementInfo.setCOLORTYPE("colored");
        elementInfo.setTYPE("STICKER");
        elementInfo.setSTC_OPACITY(255);
        elementInfo.setSTC_COLOR(0);
        elementInfo.setSTKR_PATH("");
        elementInfo.setSTC_HUE(1);
        elementInfo.setFIELD_TWO("0,0");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                RelStickerView relStickerView = new RelStickerView(activity, isImage);
                binding.txtStkrRel.addView(relStickerView);
                relStickerView.optimizeScreen(screenWidth, screenHeight);
                relStickerView.setMainLayoutWH((float) binding.mainRel.getWidth(), (float) binding.mainRel.getHeight());
                relStickerView.setComponentInfo(elementInfo);
                relStickerView.setId(ViewIdGenerator.generateViewId());
                relStickerView.optimize(wr, hr);
                relStickerView.setOnTouchCallbackListener(PosterActivity.this);
                relStickerView.setBorderVisibility(true);
            }
        });
    }

    @Override
    public void onDelete() {
        binding.ll.setVisibility(GONE);
        removeView();
    }

    @Override
    public void onDoubleTap() {
        View view = getCurrentSTR_info();
        if (view instanceof AutofitTextRel) {
            isEditMode = true;
            addTextDialog(textInfo);
        }
    }

    @Override
    public void onEdit(View view, Uri uri) {

    }

    @Override
    public void onRotateDown(View view) {

    }

    @Override
    public void onRotateMove(View view) {

    }

    @Override
    public void onRotateUp(View view) {

    }

    @Override
    public void onScaleDown(View view) {

    }

    @Override
    public void onScaleMove(View view) {

    }

    @Override
    public void onScaleUp(View view) {

    }

    @Override
    public void onTouchDown(View view) {

    }

    @Override
    public void onTouchMove(View view) {
        if (binding.ll.getVisibility() == VISIBLE) {
            binding.ll.setVisibility(GONE);
        }
    }

    @Override
    public void onTouchMoveUpClick(View view) {

    }

    @Override
    public void onTouchUp(View view) {
        touchUp(view, "hideboder");
    }

    @Override
    public void onMainClick(View view) {
        Log.e("TOUCH", "MAIN TOUCH");
        removeView();
        setBackImage();
    }

    private void touchUp(View view, String str) {
        binding.rlBusiness.setVisibility(GONE);
        removeView();
        if (str.equals("hideboder")) {
            removeViewControl();
        }
        binding.rlFrame.setVisibility(GONE);
        if (view instanceof RelStickerView) {
            viewMap.get("CG_IMAGE").setVisibility(VISIBLE);
            viewMap.get("CG_TEXT").setVisibility(GONE);
            linViewMap.get("LL_EDIT").setVisibility(VISIBLE);
            linViewMap.get("LL_TV_EDIT").setVisibility(GONE);
            binding.chipEditIv.setChecked(true);
            binding.ll.setVisibility(VISIBLE);
            binding.llBorder.setVisibility(GONE);
            binding.layEffects.setVisibility(GONE);
            binding.ll.startAnimation(animSlideUp);
            stickerInfo = ((RelStickerView) view).getComponentInfo();
            ((RelStickerView) view).setBorderVisibility(true);
            if (isImage && view.getParent() == binding.shapeRel) {
                binding.shapeRel.removeView(view);
                binding.txtStkrRel.addView(view);
                ((RelStickerView) view).setOnTouchCallbackListener(PosterActivity.this);
            }
        }
        if (view instanceof AutofitTextRel) {
            viewMap.get("CG_TEXT").setVisibility(VISIBLE);
            viewMap.get("CG_IMAGE").setVisibility(GONE);
            linViewMap.get("LL_TV_EDIT").setVisibility(VISIBLE);
            linViewMap.get("LL_EDIT").setVisibility(GONE);
            binding.chipEditTv.setChecked(true);
            binding.ll.setVisibility(VISIBLE);
            binding.ll.startAnimation(animSlideUp);
            binding.llBorder.setVisibility(GONE);
            binding.layEffects.setVisibility(GONE);

            textInfo = ((AutofitTextRel) view).getTextInfo();
            ((AutofitTextRel) view).setBorderVisibility(true);
        }

    }

    RelStickerView.TouchEventListener newtouchlistener = new RelStickerView.TouchEventListener() {
        @Override
        public void onDelete() {
            removeView();
        }

        @Override
        public void onEdit(View view, Uri uri) {

        }

        @Override
        public void onRotateDown(View view) {

        }

        @Override
        public void onRotateMove(View view) {

        }

        @Override
        public void onRotateUp(View view) {

        }

        @Override
        public void onScaleDown(View view) {

        }

        @Override
        public void onScaleMove(View view) {

        }

        @Override
        public void onScaleUp(View view) {

        }

        @Override
        public void onTouchDown(View view) {

        }

        @Override
        public void onTouchMove(View view) {
            if (binding.ll.getVisibility() == VISIBLE) {
                binding.ll.setVisibility(GONE);
            }
        }

        @Override
        public void onTouchUp(View view) {
            touchUp(view, "hideboder");
        }

        @Override
        public void onMainClick(View view) {
            Log.e("TOUCH", "MAIN TOUCH");
            removeView();
            setBackImage();
        }
    };

    public void setBackImage() {
        int childCount = binding.txtStkrRel.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = binding.txtStkrRel.getChildAt(i);
            if (childAt instanceof RelStickerView) {
                Util.showLog("LOCK: " + ((RelStickerView) childAt).getIsImage());
                if (((RelStickerView) childAt).getBorderVisbilty() && ((RelStickerView) childAt).getIsImage()) {
                    binding.txtStkrRel.removeView(childAt);
                    binding.shapeRel.addView(childAt);
                    ((RelStickerView) childAt).setOnTouchCallbackListener(newtouchlistener);
                    ((RelStickerView) childAt).setBorderVisibility(false);
                }
            }
        }
    }

    public void removeView() {
        Log.e("SB", "removeView := ");
        for (Map.Entry<String, View> entry : linViewMap.entrySet()) {
            entry.getValue().setVisibility(GONE);
//            entry.getValue().setAnimation(animSlideDown);
        }
        for (Map.Entry<String, View> entry : viewMap.entrySet()) {
            entry.getValue().setVisibility(GONE);
//            entry.getValue().setAnimation(animSlideDown);
        }
    }

    private void initBorderRecycler() {
        adaptorBorder = new RecyclerBorderAdapter(this, Constant.borderArr, this);
        RecyclerView recyclerView = findViewById(R.id.border_recylr);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(this.adaptorBorder);
    }

    private void initOverlayRecycler() {
        adaptorOverlay = new RecyclerOverLayAdapter(this, Constant.overlayArr, this);
        RecyclerView recyclerView = findViewById(R.id.overlay_recylr);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(this.adaptorOverlay);

    }

}
