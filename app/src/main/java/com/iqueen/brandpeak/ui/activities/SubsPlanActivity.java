package com.sgdigitalposter.app.ui.activities;

import static android.view.View.GONE;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.sgdigitalposter.app.Ads.BannerAdManager;
import com.sgdigitalposter.app.R;
import com.sgdigitalposter.app.adapters.SubsPlanAdapter;
import com.sgdigitalposter.app.databinding.ActivitySubsPlanBinding;
import com.sgdigitalposter.app.items.SubsPlanItem;
import com.sgdigitalposter.app.items.UserItem;
import com.sgdigitalposter.app.ui.dialog.DialogMsg;
import com.sgdigitalposter.app.ui.fragments.PaymentFragment;
import com.sgdigitalposter.app.utils.Connectivity;
import com.sgdigitalposter.app.utils.Constant;
import com.sgdigitalposter.app.utils.PrefManager;
import com.sgdigitalposter.app.utils.Util;
import com.sgdigitalposter.app.viewmodel.SubsPlanViewModel;
import com.sgdigitalposter.app.viewmodel.UserViewModel;
import com.razorpay.PaymentResultListener;

import java.io.Serializable;
import java.util.List;

public class SubsPlanActivity extends AppCompatActivity implements PaymentResultListener {

    ActivitySubsPlanBinding binding;
    SubsPlanViewModel subPlanViewModel;
    SubsPlanAdapter subsPlanAdapter;
    DialogMsg dialogMsg;
    Connectivity connectivity;
    UserViewModel userViewModel;
    PrefManager prefManager;
    UserItem userItem;
    String planId = "";
    SubsPlanItem planItem;
    String planName = "";
    String planPrice = "";
    String couponCode = "";
    String payuTrans = "";
    PaymentFragment paymentFragment;
    ProgressDialog prgDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySubsPlanBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Util.applyStatusBarPadding(binding.clMain);

        dialogMsg = new DialogMsg(this, false);
        connectivity = new Connectivity(this);
        prefManager = new PrefManager(this);

        prgDialog = new ProgressDialog(this);
        prgDialog.setMessage("Loading...");
        prgDialog.setCancelable(false);

        BannerAdManager.showBannerAds(this, binding.llAdview);
        setUpUi();
        setUpViewModel();

//        Toast.makeText(SubsPlanActivity.this, "result", Toast.LENGTH_LONG).show();
//        Log.e("SB", "onCreate SubPlan Activity: " );

    }

    private void setUpViewModel() {
        subPlanViewModel = new ViewModelProvider(this).get(SubsPlanViewModel.class);
        subPlanViewModel.getSubsPlanItems(this).observe(this, listResource -> {
            if (listResource != null) {

                Util.showLog("Got Data" + listResource.message + listResource.toString());

                switch (listResource.status) {
                    case LOADING:
                        // Loading State
                        // Data are from Local DB

                        if (listResource.data != null) {
                            setData(listResource.data);
                        }
                        break;
                    case SUCCESS:
                        // Success State
                        // Data are from Server

                        if (listResource.data != null) {

                            setData(listResource.data);
//                                        updateForgotBtnStatus();
                        }

                        break;
                    case ERROR:
                        // Error State

                        dialogMsg.showErrorDialog(listResource.message, getString(R.string.ok));
                        dialogMsg.show();

                        break;
                    default:

                        break;
                }

            } else {

                // Init Object or Empty Data
                Util.showLog("Empty Data");

            }
        });

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        userViewModel.getDbUserData(prefManager.getString(Constant.USER_ID)).observe(this, item -> {
            if (item != null) {
                userItem = item.user;
            }
        });

    }

    private void setData(List<SubsPlanItem> data) {
        subsPlanAdapter.subsPlanItemList(data);
        binding.shimmerViewContainer.stopShimmer();
        binding.shimmerViewContainer.setVisibility(GONE);
        binding.rvSubsplan.setVisibility(View.VISIBLE);
    }

    private void setUpUi() {
        binding.toolbar.toolName.setText(getResources().getString(R.string.subscribe));
        binding.toolbar.toolbarIvMenu.setBackground(getResources().getDrawable(R.drawable.ic_back));
        binding.toolbar.toolbarIvMenu.setOnClickListener(v -> {
            onBackPressed();
        });
        subsPlanAdapter = new SubsPlanAdapter(this, item -> {

            planItem = item;
            planId = item.id;
            planPrice = item.planPrice;
            planName = item.planName;
            if (!connectivity.isConnected()) {
                Util.showToast(SubsPlanActivity.this, getResources().getString(R.string.error_message__no_internet));
                return;
            }

            if (!prefManager.getBoolean(Constant.IS_LOGIN)) {
                dialogMsg.showWarningDialog(getString(R.string.login_login), getString(R.string.login_first_login), getString(R.string.login_login), false);
                dialogMsg.show();
                dialogMsg.okBtn.setOnClickListener(v -> {
                    startActivity(new Intent(SubsPlanActivity.this, LoginActivity.class));
                });
                return;
            }

            startPay();
        });
        binding.rvSubsplan.setAdapter(subsPlanAdapter);
    }

    private void startPay() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        paymentFragment = new PaymentFragment();
        //34 Support Change
        Bundle bundle = new Bundle();
        bundle.putSerializable("planItem", planItem);

        paymentFragment.setArguments(bundle);
        paymentFragment.setRequestCode(300);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(android.R.id.content, paymentFragment).addToBackStack(null).commit();
        paymentFragment.setOnCallbackResult(new PaymentFragment.CallbackResult() {
            @Override
            public void sendResult(int requestCode, Object obj) {
                if (requestCode == 300) {
                    loadData();
                }
            }
        });
    }

    private void loadData() {
        userViewModel.getUserDataById().observe(SubsPlanActivity.this, listResource -> {
            if (listResource != null) {
                Util.showLog("Got Data "
                        + listResource.message +
                        listResource.toString());

                switch (listResource.status) {
                    case LOADING:
                        // Loading State
                        // Data are from Local DB

                        break;
                    case SUCCESS:
                        // Success State
                        // Data are from Server

                        prgDialog.dismiss();
                        if (listResource.data != null) {
                            userItem = listResource.data;
                            Constant.IS_SUBSCRIBED = listResource.data.isSubscribed;
                            prefManager.setString(Constant.REFER_CODE_BY,
                                    listResource.data.referralCode);
                            onBackPressed();
                        }

                        break;
                    case ERROR:
                        // Error State
                        prgDialog.dismiss();
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
    }


    @Override
    public void onPaymentSuccess(String s) {
        paymentFragment.onPaymentSuccess(s);
    }

    @Override
    public void onPaymentError(int i, String s) {
        paymentFragment.onPaymentError(i, s);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 50000 && data != null) {
            Toast.makeText(this, data.getStringExtra("nativeSdkForMerchantMessage")
                    + data.getStringExtra("response"), Toast.LENGTH_SHORT).show();
        }
    }

}