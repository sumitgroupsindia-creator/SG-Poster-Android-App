package com.sgdigitalposter.app.ui.fragments;

import static android.view.View.DRAWING_CACHE_QUALITY_HIGH;

import static androidx.core.content.ContextCompat.registerReceiver;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;
import android.widget.Toast;

import com.sgdigitalposter.app.Config;
import com.sgdigitalposter.app.MyApplication;
import com.sgdigitalposter.app.R;
import com.sgdigitalposter.app.adapters.AdapterPersonal;
import com.sgdigitalposter.app.adapters.BusinessCategoryAdapter;
import com.sgdigitalposter.app.adapters.CategoryAdapter;
import com.sgdigitalposter.app.adapters.FeatureAdapter;
import com.sgdigitalposter.app.adapters.FestivalAdapter;
import com.sgdigitalposter.app.adapters.HomeBannerAdapter;
import com.sgdigitalposter.app.adapters.NewsAdapter;
import com.sgdigitalposter.app.adapters.PersonalDetailAdapter;
import com.sgdigitalposter.app.adapters.TrendingAdapter;
import com.sgdigitalposter.app.binding.GlideBinding;
import com.sgdigitalposter.app.databinding.FragmentHomeBinding;
import com.sgdigitalposter.app.items.BusinessCategoryItem;
import com.sgdigitalposter.app.items.FeatureItem;
import com.sgdigitalposter.app.items.HomeItem;
import com.sgdigitalposter.app.items.PersonalItem;
import com.sgdigitalposter.app.items.PostItem;
import com.sgdigitalposter.app.items.UserItem;
import com.sgdigitalposter.app.ui.activities.BusinessCategoryActivity;
import com.sgdigitalposter.app.ui.activities.CategoryActivity;
import com.sgdigitalposter.app.ui.activities.DetailActivity;
import com.sgdigitalposter.app.ui.activities.FestivalActivity;
import com.sgdigitalposter.app.ui.activities.LoginActivity;
import com.sgdigitalposter.app.ui.activities.NewsDetailActivity;
import com.sgdigitalposter.app.ui.activities.ProfileEditActivity;
import com.sgdigitalposter.app.ui.activities.SubsPlanActivity;
import com.sgdigitalposter.app.ui.dialog.DialogMsg;
import com.sgdigitalposter.app.utils.Connectivity;
import com.sgdigitalposter.app.utils.Constant;
import com.sgdigitalposter.app.utils.PrefManager;
import com.sgdigitalposter.app.utils.Util;
import com.sgdigitalposter.app.viewmodel.HomeViewModel;
import com.sgdigitalposter.app.viewmodel.NewsViewModel;
import com.sgdigitalposter.app.viewmodel.PostViewModel;
import com.sgdigitalposter.app.viewmodel.UserViewModel;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class HomeFragment extends Fragment {

    FragmentHomeBinding binding;
    boolean isLoadingPersonal = false;

    
    private String[] PERMISSIONS = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE
    };
    int permissionsCount = 0;

    BroadcastReceiver updateData  = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (intent.getStringExtra("SendBroadCast").equals("Success")){
                    personalDetailAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getActivity(),"UnSuccess",Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
//                Toast.makeText(getActivity(),"Not Work",Toast.LENGTH_LONG).show();
            }
        }
    };
    ActivityResultLauncher<String[]> permissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                    new ActivityResultCallback<Map<String, Boolean>>() {
                        @RequiresApi(api = Build.VERSION_CODES.M)
                        @Override
                        public void onActivityResult(Map<String, Boolean> result) {
                            ArrayList<Boolean> list = new ArrayList<>(result.values());
                            permissionsList = new ArrayList<>();
                            permissionsCount = 0;

                            if (Build.VERSION_CODES.TIRAMISU <= Build.VERSION.SDK_INT){
                                String postNotifications = Manifest.permission.POST_NOTIFICATIONS;
                                permissionsList.add(postNotifications);
                            }

                            for (int i = 0; i < list.size(); i++) {
                                if (shouldShowRequestPermissionRationale(PERMISSIONS[i])) {
                                    permissionsList.add(PERMISSIONS[i]);
                                } else if (!hasPermission(getContext(), PERMISSIONS[i])) {
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
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
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

    BusinessCategoryItem businessItem;

    public HomeFragment() {
    }

    HomeViewModel homeViewModel;

//    StoryAdapter storyAdapter;

    // Adapter
    HomeBannerAdapter homeBannerAdapter;

    FestivalAdapter festivalAdapter;

    CategoryAdapter categoryAdapter;

    FeatureAdapter featureAdapter;

    BusinessCategoryAdapter businessCategoryAdapter;

    NewsAdapter newsAdapter;

    AdapterPersonal personalAdapter;
    PersonalDetailAdapter personalDetailAdapter;
    // Adapter

    PersonalItem personalItem;

    boolean isLoading = false;

    TrendingAdapter busDetailAdapter;
    NewsViewModel newsViewModel;
    PostViewModel postViewModel;

    List<PostItem> postItemList;

    PrefManager prefManager;

    boolean isPersonal = false;
    DialogMsg dialogMsg;
    Connectivity connectivity;
    UserItem userItem;
    UserViewModel userViewModel;

    PostItem currentItem;
    View currentView;
    private ProgressDialog prgDialog;
    String fileName = "";
    String filePath;
    boolean checkMemory;
    Bitmap bitmap;
    int totalMemory = 0;
    boolean isWhatsapp = false;
    int page = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(getLayoutInflater());
        Util.fadeIn(binding.getRoot(), getContext());

        connectivity = new Connectivity(getContext());
        prefManager = new PrefManager(getActivity());
        dialogMsg = new DialogMsg(getActivity(), false);
        prgDialog = new ProgressDialog(getContext());

        if (android.os.Build.VERSION.SDK_INT >= 33) {
            PERMISSIONS = new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES,
                    };
        }

        initViewModel();
        setupUi();
        setData();


        Integer receiverFlags  = AppCompatActivity.RECEIVER_EXPORTED;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            receiverFlags =  AppCompatActivity.RECEIVER_EXPORTED;
        } else {
            receiverFlags = AppCompatActivity.RECEIVER_NOT_EXPORTED;
        }

        IntentFilter intentFilter = new IntentFilter("EditUpdateName");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getActivity().registerReceiver(updateData,intentFilter,receiverFlags );
        } else {
            getActivity().registerReceiver(updateData,intentFilter);
        }
        return binding.getRoot();
    }

    private void initViewModel() {
        homeViewModel = new ViewModelProvider(getActivity()).get(HomeViewModel.class);
        newsViewModel = new ViewModelProvider(getActivity()).get(NewsViewModel.class);
        postViewModel = new ViewModelProvider(getActivity()).get(PostViewModel.class);
        userViewModel = new ViewModelProvider(getActivity()).get(UserViewModel.class);

        homeViewModel.getLoadingState().observe(getActivity(), loadingState -> {

            if (loadingState != null && loadingState) {
                binding.swipeRefresh.setRefreshing(true);
                binding.shimmerViewContainer.startShimmer();
                binding.shimmerViewContainer.setVisibility(View.VISIBLE);
                binding.mainContainer.setVisibility(View.GONE);
            } else {
                binding.swipeRefresh.setRefreshing(false);
                binding.shimmerViewContainer.stopShimmer();
                binding.shimmerViewContainer.setVisibility(View.GONE);
                binding.mainContainer.setVisibility(View.VISIBLE);
            }

        });

        postViewModel.getFrameData("New").observe(getActivity(), resource -> {
            if (resource != null) {

//                Util.showLog("Got Data" + resource.message + resource.toString());

                switch (resource.status) {
                    case LOADING:
                        // Loading State
                        // Data are from Local DB

                        break;
                    case SUCCESS:
                        // Success State
                        // Data are from Server
                        Log.e("SB", "initViewModel POST dATA: " + resource.data);

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

        postViewModel.getFrameCategoryData().observe(getActivity(), resource -> {
            if (resource != null) {

//                Util.showLog("Got Data" + resource.message + resource.toString());

                switch (resource.status) {
                    case LOADING:
                        // Loading State
                        // Data are from Local DB

                        break;
                    case SUCCESS:
                        // Success State
                        // Data are from Server

//                        Log.e("SB", "initViewModel POST dATA 2: " + resource.data);
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

        userViewModel.getDbUserData(prefManager.getString(Constant.USER_ID)).observe((LifecycleOwner) getContext(),
                resource -> {
                    if (resource != null) {
                        userItem = resource.user;
                    }
                });

        busDetailAdapter = new TrendingAdapter(getContext(), data -> {
            if (businessItem != null) {
                Intent intent = new Intent(getContext(), DetailActivity.class);
                intent.putExtra(Constant.INTENT_TYPE, data.type);
                intent.putExtra(Constant.INTENT_FEST_ID, data.fest_id);
                intent.putExtra(Constant.INTENT_FEST_NAME, businessItem.businessCategoryName);
                intent.putExtra(Constant.INTENT_POST_IMAGE, data.image_url);
                intent.putExtra(Constant.INTENT_POST_ITEM, data);
                intent.putExtra(Constant.INTENT_VIDEO, businessItem.video);
                startActivity(intent);
            }
        });
        binding.rvFeature.setAdapter(busDetailAdapter);

        binding.txtViewTrending.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), DetailActivity.class);
            intent.putExtra(Constant.INTENT_TYPE, Constant.BUSINESS);
            intent.putExtra(Constant.INTENT_FEST_ID, businessItem.businessCategoryId);
            intent.putExtra(Constant.INTENT_FEST_NAME, businessItem.businessCategoryName);
            intent.putExtra(Constant.INTENT_POST_IMAGE, "");
            intent.putExtra(Constant.INTENT_VIDEO, businessItem.video);
            startActivity(intent);
        });

        postViewModel.getById().observe((LifecycleOwner) getContext(), resource -> {

            if (resource != null) {

                if (resource.data != null && resource.data.size() > 0) {
                    Log.e("SB", " HomeFragment initViewModel GetById :" + resource.data );
                    busDetailAdapter.setTrending(resource.data);
                    binding.businessCat.setVisibility(View.VISIBLE);

                }
            }

        });

        postViewModel.getPersonalById().observe((LifecycleOwner) getContext(), resource -> {

            if (resource != null) {

                Util.showLog("Got Data" + resource.message + resource.toString());

                switch (resource.status) {
                    case LOADING:
                        // Loading State
                        // Data are from Local DB
                        if (resource.data != null) {
//                            Log.e("SB", "loadPersonal getPersonalById : " + resource.data);
                            setPersonalDetail(resource.data);
                        }
                        break;
                    case SUCCESS:
                        // Success State
                        // Data are from Server

                        if (resource.data != null && resource.data.size() > 0) {
                            setPersonalDetail(resource.data);
                        }

                        break;
                    case ERROR:
                        // Error State
//                        binding.swipeRefresh.setRefreshing(false);
//                        binding.mainContainer.setVisibility(View.GONE);
//                        binding.shimmerViewContainer.stopShimmer();
//                        binding.shimmerViewContainer.setVisibility(View.GONE);
//                        binding.clList.setVisibility(View.VISIBLE);
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

        userViewModel.getDefaultBusiness().observe((LifecycleOwner) getContext(), resource -> {
            if (resource != null) {
                if (resource.category != null) {

                    businessItem = resource.category;
                    if (getContext() != null) {
                        homeViewModel.getBusinessCategory().observe((LifecycleOwner) getContext(), resources -> {
                            if (resources != null && resources.size() > 0) {

                                for (BusinessCategoryItem item : resources) {
                                    if (resource.category.businessCategoryName.equals(item.businessCategoryName)) {
                                        businessItem = item;
                                        break;
                                    }
                                }

                            }
                        });
                    }
                    binding.tvFeature.setText(resource.category.businessCategoryName + " (My Business)");

                    postViewModel.setPostByIdObj(resource.category.businessCategoryId,
                            Constant.BUSINESS, "", false, "");
                }
            }
        });
    }

    @SuppressLint("ResourceAsColor")
    private void setupUi() {

        if (Config.PERSONAL_SECTION) {
            binding.clPersonal.setVisibility(View.VISIBLE);
        } else {
            binding.clPersonal.setVisibility(View.GONE);
        }
        // Story Region
       /* storyAdapter = new StoryAdapter(getContext(), item -> {
            if (item.type.equals("externalLink")) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.externalLink));
                startActivity(intent);
            } else if (item.type.equals(SUBS_PLAN)) {
                Intent intent = new Intent(getActivity(), SubsPlanActivity.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra(Constant.INTENT_TYPE, item.type);
                intent.putExtra(Constant.INTENT_FEST_ID, item.festivalId);
                intent.putExtra(Constant.INTENT_FEST_NAME, item.title);
                intent.putExtra(Constant.INTENT_POST_IMAGE, "");
                intent.putExtra(Constant.INTENT_VIDEO, item.video);
                startActivity(intent);
            }
        });
        binding.rvStory.setAdapter(storyAdapter);*/

        //Festival Region
        festivalAdapter = new FestivalAdapter(getContext(), item -> {
            if (!item.isActive) {
                DialogMsg dialogMsg = new DialogMsg(getActivity(), true);
                dialogMsg.showWarningDialog(getContext().getString(R.string.no_festival_image), getContext().getString(R.string.festival_image_create),
                        getContext().getString(R.string.ok), false);
                dialogMsg.show();
                return;
            }
            Intent intent = new Intent(getActivity(), DetailActivity.class);
            intent.putExtra(Constant.INTENT_TYPE, Constant.FESTIVAL);
            intent.putExtra(Constant.INTENT_FEST_ID, item.id);
            intent.putExtra(Constant.INTENT_FEST_NAME, item.name);
            intent.putExtra(Constant.INTENT_POST_IMAGE, "");
            intent.putExtra(Constant.INTENT_VIDEO, item.video);
            startActivity(intent);
        }, true);
        binding.rvFestival.setAdapter(festivalAdapter);

        //Category Region
        categoryAdapter = new CategoryAdapter(getContext(), item -> {
            Intent intent = new Intent(getActivity(), DetailActivity.class);
            intent.putExtra(Constant.INTENT_TYPE, Constant.CATEGORY);
            intent.putExtra(Constant.INTENT_FEST_ID, item.id);
            intent.putExtra(Constant.INTENT_FEST_NAME, item.name);
            intent.putExtra(Constant.INTENT_POST_IMAGE, "");
            intent.putExtra(Constant.INTENT_VIDEO, item.video);
            startActivity(intent);
        }, true);
        binding.rvCategory.setAdapter(categoryAdapter);
        binding.rvCategory.setNestedScrollingEnabled(false);
        binding.rvCategory.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            }
        });

        //Custom Category
        businessCategoryAdapter = new BusinessCategoryAdapter(getContext(), item -> {

            Intent intent = new Intent(getActivity(), DetailActivity.class);
            intent.putExtra(Constant.INTENT_TYPE, Constant.BUSINESS);
            intent.putExtra(Constant.INTENT_FEST_ID, item.businessCategoryId);
            intent.putExtra(Constant.INTENT_FEST_NAME, item.businessCategoryName);
            intent.putExtra(Constant.INTENT_POST_IMAGE, "");
            intent.putExtra(Constant.INTENT_VIDEO, item.video);
            startActivity(intent);

        }, true, false);
        binding.rvBusinessCategory.setAdapter(businessCategoryAdapter);

        featureAdapter = new FeatureAdapter(getContext());
        binding.rvHomeFeature.setAdapter(featureAdapter);
        binding.rvHomeFeature.setNestedScrollingEnabled(false);
        binding.rvHomeFeature.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            }
        });

        binding.swipeRefresh.setOnRefreshListener(() -> {
            if (isPersonal) {
                loadPersonal();
            } else {
                homeViewModel.setHomeObj("New");
                newsViewModel.setNewsObj(String.valueOf(0));
            }
        });

        newsAdapter = new NewsAdapter(getContext(), data -> {
            Intent intent = new Intent(getActivity(), NewsDetailActivity.class);
            intent.putExtra(Constant.INTENT_NEWS_ITEM, data);
            getContext().startActivity(intent);
        });
        binding.rvNews.setAdapter(newsAdapter);

        binding.txtViewFestival.setOnClickListener(v -> {
            getContext().startActivity(new Intent(getActivity(), FestivalActivity.class));
        });

        binding.txtViewCategory.setOnClickListener(v -> {
            getContext().startActivity(new Intent(getActivity(), CategoryActivity.class));
        });

        binding.txtViewBusiness.setOnClickListener(v -> {
            getContext().startActivity(new Intent(getActivity(), BusinessCategoryActivity.class));
        });

        if (Config.offerItem != null && !Config.offerItem.banner.equals("")) {
            GlideBinding.bindImage(binding.ivOffer, Config.offerItem.banner);
            binding.cvOffer.setVisibility(View.VISIBLE);
            ViewPropertyAnimator animate = binding.cvOffer.animate();
            animate.scaleY(0.9f);
            animate.scaleX(0.9f);
            animate.setDuration(1000);
            animate.start();
            animate.setUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    animation.setRepeatMode(ValueAnimator.REVERSE);
                    animation.setRepeatCount(Animation.INFINITE);
                }
            });

            binding.cvOffer.setOnClickListener(v -> {
//                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://brandpeak.app/festival-pack/"));
                startActivity(new Intent(getActivity(), SubsPlanActivity.class));
            });
        }


        binding.btnPersonal.setOnClickListener(v -> {
            if (!isLoadingPersonal){
                binding.btnPersonal.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.active_color)));
                binding.btnBusiness.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.black)));
                binding.btnPersonal.setTextAppearance(R.style.ActiveButtonStyle);
                binding.btnBusiness.setTextAppearance(R.style.ButtonStyle);
                binding.btnBusiness.setIconTint(ColorStateList.valueOf(getResources().getColor(prefManager.getString(Constant.CHECKED_ITEM).equals("yes") ? R.color.white : R.color.white)));
                binding.btnPersonal.setIconTint(ColorStateList.valueOf(getResources().getColor(prefManager.getString(Constant.CHECKED_ITEM).equals("yes") ? R.color.black : R.color.white)));
                isPersonal = true;
                isLoadingPersonal = true;
                loadPersonal();
            }
        });

        binding.btnBusiness.setOnClickListener(v -> {
            if (businessItem != null) {
                postViewModel.setPostByIdObj(businessItem.businessCategoryId,
                        Constant.BUSINESS, "", false, "");
            }
            binding.swipeRefresh.setRefreshing(false);
            binding.shimmerViewContainer.setVisibility(View.GONE);
            binding.btnPersonal.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.black)));
            binding.btnBusiness.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.active_color)));
            binding.btnBusiness.setTextAppearance(R.style.ActiveButtonStyle);
            binding.btnPersonal.setTextAppearance(R.style.ButtonStyle);
            binding.btnPersonal.setIconTint(ColorStateList.valueOf(getResources().getColor(prefManager.getString(Constant.CHECKED_ITEM).equals("yes") ? R.color.white : R.color.white)));
            binding.btnBusiness.setIconTint(ColorStateList.valueOf(getResources().getColor(prefManager.getString(Constant.CHECKED_ITEM).equals("yes") ? R.color.black : R.color.white)));
            isPersonal = false;
            isLoadingPersonal = false;
            binding.mainContainer.setVisibility(View.VISIBLE);
            binding.clList.setVisibility(View.GONE);
        });

//        if (Config.whatsAvailable && !Config.whatsappNumber.equals("")) {
            showHelp();
//        }
    }

    private void showHelp() {
        binding.ciHelp.setVisibility(View.VISIBLE);
        AnimatorSet mAnimatorSet = new AnimatorSet();
        mAnimatorSet.playTogether(
                animate("scaleX", 0.65f, 1),
                animate("scaleY", 0.65f, 1),
                animate("alpha", 0, 1)
        );
        mAnimatorSet.start();

        binding.ciHelp.setOnClickListener(v -> {
            Intent whatsapp = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://api.whatsapp.com/send?phone=" + "+91" + Config.whatsappNumber + "&text=" + "Hello I Want to Help"));
            startActivity(whatsapp);
        });

    }

    private ObjectAnimator animate(String style, float... values) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(binding.ciHelp, style, values).setDuration(1200);
        objectAnimator.setInterpolator(new BounceInterpolator());
        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                animation.setStartDelay(1000);
                animation.start();
            }
        });
        return objectAnimator;
    }


    private void setData() {

        homeViewModel.setLoadingState(true);

        homeViewModel.setHomeObj("home");
        homeViewModel.getHomeData().observe(getActivity(), resource -> {
            if (resource != null) {

                Util.showLog("Got Data" + resource.message + resource.toString());

                switch (resource.status) {
                    case LOADING:
                        // Loading State
                        // Data are from Local DB

                        if (resource.data != null) {

                            setHomeData(resource.data);

                        }
                        break;
                    case SUCCESS:
                        // Success State
                        // Data are from Server

                        if (resource.data != null) {
                            setHomeData(resource.data);
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

            }
        });

        newsViewModel.setNewsObj(String.valueOf(1));
        newsViewModel.getNews().observe(getActivity(), resource -> {
            if (resource != null) {

                Util.showLog("Got Data" + resource.message + resource.toString());

                switch (resource.status) {
                    case LOADING:
                        // Loading State
                        // Data are from Local DB

                        if (resource.data != null) {

                            if (resource.data.size() > 0) {
                                newsAdapter.setData(resource.data);
                                binding.executePendingBindings();
                            }

                        }
                        break;
                    case SUCCESS:
                        // Success State
                        // Data are from Server

                        if (resource.data != null && resource.data.size() > 0) {

                            newsAdapter.setData(resource.data);
                            binding.executePendingBindings();
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

            }
        });
    }
    //Story Data Set
    private void setHomeData(HomeItem data) {

        binding.swipeRefresh.setRefreshing(false);
        binding.shimmerViewContainer.stopShimmer();
        binding.shimmerViewContainer.setVisibility(View.GONE);
        binding.mainContainer.setVisibility(View.VISIBLE);

        homeViewModel.setLoadingState(false);

//        storyAdapter.setItemList(data.storyItemList);

        homeBannerAdapter = new HomeBannerAdapter(getContext(), data.storyItemList, item -> {
            if (item.type.equals("externalLink")) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.externalLink));
                startActivity(intent);
            } else if (item.type.equals(Constant.SUBS_PLAN)) {
                Intent intent = new Intent(getActivity(), SubsPlanActivity.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra(Constant.INTENT_TYPE, item.type);
                intent.putExtra(Constant.INTENT_FEST_ID, item.festivalId);
                intent.putExtra(Constant.INTENT_FEST_NAME, item.title);
                intent.putExtra(Constant.INTENT_POST_IMAGE, "");
                intent.putExtra(Constant.INTENT_VIDEO, item.video);
                startActivity(intent);
            }
        });

//        BannerTransformer bannerTransformer = new BannerTransformer(binding.viewPagerHome, homeBannerAdapter);

        ViewPager.PageTransformer compositePageTransformer = new ViewPager.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                float r = 1 - Math.abs(position);
                page.setScaleY(0.85f + r * 0.15f);
                page.setScaleX(0.85f + r * 0.15f);
            }
        };

        binding.viewPagerHome.setPageTransformer(false, compositePageTransformer);

        int itemWidth = MyApplication.getScreenWidth();
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) binding.viewPagerHome.getLayoutParams();
        params.width = itemWidth;
        params.height = (int) (itemWidth * 0.55f);
        binding.viewPagerHome.setLayoutParams(params);
        binding.viewPagerHome.setAdapter(homeBannerAdapter);


        festivalAdapter.setFestData(data.festivalItemList);

        categoryAdapter.setCategories(data.categoryList);

       List<FeatureItem> featureItemList = data.featureItemList.stream().filter(featureItem -> featureItem.postItemList.size() > 0).collect(Collectors.toList());
        featureAdapter.setFeatureItemList(featureItemList);

        businessCategoryAdapter.setCategories(data.businessCategoryList);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
//        getActivity().unregisterReceiver(updateData);
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        getActivity().unregisterReceiver(updateData);
        super.onDestroyView();
    }

    public void loadPersonal() {
        page = 0;
        binding.swipeRefresh.setRefreshing(true);
        binding.mainContainer.setVisibility(View.GONE);
        binding.shimmerViewContainer.startShimmer();
        binding.shimmerViewContainer.setVisibility(View.VISIBLE);
        binding.clList.setVisibility(View.GONE);

        personalAdapter = new AdapterPersonal(getActivity(), item -> {
            binding.pbDetail.setVisibility(View.VISIBLE);
            page = 0;
            personalItem = item;
            loadPersonalDetail(item);
        });
        binding.rvPersonal.setAdapter(personalAdapter);
        personalDetailAdapter = new PersonalDetailAdapter(getActivity(), new PersonalDetailAdapter.ClickEvent() {
            @Override
            public void onWhatsappClick(View view, PostItem item) {

                isWhatsapp = true;
                currentItem = item;
                currentView = view;

                Dexter.withContext(getContext()).withPermissions(PERMISSIONS).withListener(new MultiplePermissionsListener() {
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {

                        if (multiplePermissionsReport.areAllPermissionsGranted()) {
//                            Log.e("SB", "onPermissionsChecked HomeFragment Granted : if  " );
                            startPost();
                        }
                        if (multiplePermissionsReport.isAnyPermissionPermanentlyDenied()) {
//                            Log.e("SB", "onPermissionsChecked  homeFragment Denied: if  " );
                            String data = "";
                            for (int i = 0; i < multiplePermissionsReport.getDeniedPermissionResponses().size(); i++) {
                                data = data + " , " + multiplePermissionsReport.getDeniedPermissionResponses().get(i).getPermissionName();
                            }
                            showSettingsDialog(data);
                        }
                    }

                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
//                        Log.e("SB", "onPermissionRationaleShouldBeShown : HomeFragment  " );
                        permissionToken.continuePermissionRequest();
                    }
                }).withErrorListener(new PermissionRequestErrorListener() {
                    public void onError(DexterError dexterError) {
//                    Toast.makeText(DetailActivity.this, "Error occurred! Please Give Permission", Toast.LENGTH_SHORT).show();
//                        Log.e("SB", "onError :HomeFragment   " );
                        permissionsList = new ArrayList<>();
                        permissionsList.addAll(Arrays.asList(PERMISSIONS));
                        askForPermissions(permissionsList);
                    }
                }).onSameThread().check();

            }


            @Override
            public void onDownloadClick(View view, PostItem item) {
                isWhatsapp = false;
                currentItem = item;
                currentView = view;

                Dexter.withContext(getContext()).withPermissions(PERMISSIONS).withListener(new MultiplePermissionsListener() {
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        if (multiplePermissionsReport.areAllPermissionsGranted()) {
//                            Log.e("SB", "onPermissionsChecked: Granted  hOMEFRAGMENT if 2  " );
                            startPost();
                        }
                        if (multiplePermissionsReport.isAnyPermissionPermanentlyDenied()) {
//                            Log.e("SB", "onPermissionsChecked:  Denied HomeFragment if 2  " );
                            String data = "";
                            for (int i = 0; i < multiplePermissionsReport.getDeniedPermissionResponses().size(); i++) {
                                data = data + " , " + multiplePermissionsReport.getDeniedPermissionResponses().get(i).getPermissionName();
                            }
                            showSettingsDialog(data);
                        }
                    }

                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
//                        Log.e("SB", "onPermissionRationaleShouldBeShown:   HomeFragment if 2  " );
                        permissionToken.continuePermissionRequest();
                    }
                }).withErrorListener(new PermissionRequestErrorListener() {
                    public void onError(DexterError dexterError) {
//                    Toast.makeText(DetailActivity.this, "Error occurred! Please Give Permission", Toast.LENGTH_SHORT).show();
//                        Log.e("SB", "onError:   HomeFragment if 2  " );
                        permissionsList = new ArrayList<>();
                        permissionsList.addAll(Arrays.asList(PERMISSIONS));
                        askForPermissions(permissionsList);
                    }
                }).onSameThread().check();
            }

            @Override
            public void onEditClick() {

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
                        startActivity(new Intent(getActivity(), LoginActivity.class));
                    });
                    return;
                }
                Constant.FROM_PERSONAL = true;
                startActivity(new Intent(getActivity(), ProfileEditActivity.class));

            }
        }, 1, getContext().getResources().getDimension(com.intuit.ssp.R.dimen._5ssp));
        binding.rvDetail.setAdapter(personalDetailAdapter);
        binding.rvDetail.setHasFixedSize(true);

        binding.rvDetail.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                if (!isLoading) {
                    if (postItemList != null && linearLayoutManager != null &&
                            linearLayoutManager.findLastCompletelyVisibleItemPosition() ==
                                    postItemList.size() - 1) {
                        //bottom of list!
                        page++;
                        loadMore();
                        isLoading = true;
                    }
                }
            }
        });

        homeViewModel.getPersonalData().observe((LifecycleOwner) getContext(), resource -> {
            if (resource != null) {

                Util.showLog("Got Data" + resource.message + resource.toString());

                switch (resource.status) {
                    case LOADING:
                        // Loading State
                        // Data are from Local DB
                        if (resource.data != null && resource.data.size() > 0) {
                            personalAdapter.setData(resource.data);
                        }
                        break;
                    case SUCCESS:
                        // Success State
                        // Data are from Server

                        if (resource.data != null && resource.data.size() > 0) {
                            personalItem = resource.data.get(0);
                            personalAdapter.setData(resource.data);
                            loadPersonalDetail(resource.data.get(0));
                        }

                        break;
                    case ERROR:
                        // Error State
                        break;
                    default:
                        // Default

                        break;
                }

            }
        });


    }

    private void loadMore() {
      postViewModel.getNewPost(page, personalItem.id, false, personalItem.type);
    }

    private void startPost() {
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
                startActivity(new Intent(getActivity(), LoginActivity.class));
            });
            return;
        }

        if (!userItem.isSubscribed && currentItem.is_premium) {
            dialogMsg.showWarningDialog(getString(R.string.premium), getString(R.string.please_subscribe), getString(R.string.subscribe),
                    true);
            dialogMsg.show();
            dialogMsg.okBtn.setOnClickListener(view -> {
                dialogMsg.cancel();
                startActivity(new Intent(getContext(), SubsPlanActivity.class));
            });
            return;

        }
        if (!isWhatsapp)
            DownloadImage("download");
        else
            DownloadImage("whtsapp");
    }

    private void DownloadImage(String type) {

        prgDialog.setMessage("Please Wait...");
        prgDialog.setCancelable(false);
        prgDialog.show();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ImageView iv_watermark = currentView.findViewById(R.id.iv_frame_watermark);

                try {
                    if (userItem.isSubscribed) {
                        iv_watermark.setVisibility(View.GONE);
                    }
                    ImageView iv_prime = currentView.findViewById(R.id.iv_prime);
                    iv_prime.setVisibility(View.GONE);

                    bitmap = viewToBitmap(currentView);
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
                    prgDialog.dismiss();

                    iv_prime.setVisibility(currentItem.is_premium ? View.VISIBLE : View.GONE);

                } catch (Exception e) {
                    Util.showErrorLog(e.getMessage(), e);
                }

                fileName = System.currentTimeMillis() + ".jpeg";

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
                                Toast.makeText(getContext(),
                                        getResources().getString(R.string.don_t_create),
                                        Toast.LENGTH_LONG).show();
                                success = false;
                            }
                        }
                        File file2 = new File(file.getAbsolutePath() + "/" + fileName);
                        if (file2.exists()) {
                            file2.delete();
                        }
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

                            MediaScannerConnection.scanFile(getContext(), new String[]{file2.getAbsolutePath()},
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
                            Util.showErrorLog(e.getMessage(), e);
                            success = false;
                        }

                        prgDialog.dismiss();
                    } catch (Exception e) {
                        Util.showErrorLog(e.getMessage(), e);
                    }

                    if (success) {
                        if (type.equals("download")) {
                            Toast.makeText(getContext(), getString(R.string.image_saved), Toast.LENGTH_SHORT).show();
                        } else {
                            shareFileImageUri(getImageContentUri(new File(filePath)), "", type);
                        }
                    } else {
                        Toast.makeText(getContext(), getString(R.string.err_save_image), Toast.LENGTH_SHORT).show();
                    }
                    prgDialog.dismiss();
                }

            }
        });
    }

    private void showSettingsDialog(String data) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Need Permissions");
        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings. \n " + data);
        builder.setPositiveButton("GOTO SETTINGS", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
                Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
                intent.setData(Uri.fromParts("package", getContext().getPackageName(), (String) null));
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

    private void loadPersonalDetail(PersonalItem personalItem) {

        postViewModel.setPersonalByIdObj(personalItem.id, personalItem.type, "", false, "");

    }

    private void setPersonalDetail(List<PostItem> data) {
        if (isPersonal) {
            binding.pbDetail.setVisibility(View.GONE);
            postItemList = new ArrayList<>();
            postItemList.clear();
            postItemList.addAll(data);
            binding.swipeRefresh.setRefreshing(false);
            binding.mainContainer.setVisibility(View.GONE);
            binding.shimmerViewContainer.stopShimmer();
            binding.shimmerViewContainer.setVisibility(View.GONE);
            personalDetailAdapter.setData(postItemList);
            binding.clList.setVisibility(View.VISIBLE);
        }
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

    public void shareFileImageUri(Uri path, String name, String shareTo) {
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
        shareIntent.setDataAndType(path, "image/*");
        shareIntent.putExtra(Intent.EXTRA_STREAM, path);
        if (!name.equals("")) {
            shareIntent.putExtra(Intent.EXTRA_TEXT, name);
        }
        startActivity(Intent.createChooser(shareIntent, MyApplication.context.getString(R.string.share_via)));
    }

    public Uri getImageContentUri(File imageFile) {
        return Uri.parse(imageFile.getAbsolutePath());
    }


}