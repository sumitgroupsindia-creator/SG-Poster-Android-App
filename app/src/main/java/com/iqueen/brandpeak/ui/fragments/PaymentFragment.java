package com.sgdigitalposter.app.ui.fragments;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.sgdigitalposter.app.utils.Constant.RESULT_OK;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cashfree.pg.api.CFPaymentGatewayService;
import com.cashfree.pg.core.api.CFSession;
import com.cashfree.pg.core.api.CFTheme;
import com.cashfree.pg.core.api.callback.CFCheckoutResponseCallback;
import com.cashfree.pg.core.api.exception.CFException;
import com.cashfree.pg.core.api.utils.CFErrorResponse;
import com.cashfree.pg.ui.api.CFDropCheckoutPayment;
import com.cashfree.pg.ui.api.CFPaymentComponent;
import com.sgdigitalposter.app.Config;
import com.sgdigitalposter.app.MyApplication;
import com.sgdigitalposter.app.R;
import com.sgdigitalposter.app.api.ApiClient;
import com.sgdigitalposter.app.api.ApiResponse;
import com.sgdigitalposter.app.binding.GlideBinding;
import com.sgdigitalposter.app.databinding.DialogEnquiryBinding;
import com.sgdigitalposter.app.items.CashFreeOrder;
import com.sgdigitalposter.app.items.CouponItem;
import com.sgdigitalposter.app.items.PaytmResponse;
import com.sgdigitalposter.app.items.PhonePeResponseDTO;
import com.sgdigitalposter.app.items.StripeResponse;
import com.sgdigitalposter.app.items.SubsPlanItem;
import com.sgdigitalposter.app.items.UserItem;
import com.sgdigitalposter.app.ui.dialog.DialogMsg;
import com.sgdigitalposter.app.utils.Connectivity;
import com.sgdigitalposter.app.utils.Constant;
import com.sgdigitalposter.app.utils.PrefManager;
import com.sgdigitalposter.app.utils.Util;
import com.sgdigitalposter.app.viewmodel.SubsPlanViewModel;
import com.sgdigitalposter.app.viewmodel.UserViewModel;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;
import com.paytm.pgsdk.TransactionManager;
import com.phonepe.intent.sdk.api.B2BPGRequest;
import com.phonepe.intent.sdk.api.B2BPGRequestBuilder;
import com.phonepe.intent.sdk.api.PhonePe;
import com.phonepe.intent.sdk.api.PhonePeInitException;
import com.razorpay.Checkout;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.Stripe;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Response;

public class PaymentFragment extends DialogFragment implements CFCheckoutResponseCallback {
//    <!--    ,"razorpayKeyId":"rzp_test_UBTNSKFSEi3qL0","razorpayKeySecret":"2vpJgAW39vDsFHkQ9XnB240z"-->

    SubsPlanItem item;
//    public Activity activity;

//    public PaymentFragment(Activity activity, SubsPlanItem subsPlanItem) {
//        // Required empty public constructor
//        this.item = subsPlanItem;
//        this.activity = activity;
//    }



    public void setRequestCode(int request_code) {
        this.request_code = request_code;
    }

    public CallbackResult callbackResult;

    public void setOnCallbackResult(final CallbackResult callbackResult) {
        this.callbackResult = callbackResult;
    }

    private int request_code = 0;
    SubsPlanViewModel subPlanViewModel;
    Connectivity connectivity;
    UserViewModel userViewModel;
    PrefManager prefManager;
    UserItem userItem;

    DialogMsg dialogMsg;
    String amount;

    PaymentSheet paymentSheet;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //34 Support Change
        assert getArguments() != null;
        item = (SubsPlanItem) getArguments().getSerializable("planItem");
        Log.e("SB", "onCreate item := " + item.toString() );
    }

    View dialog;
    public EditText etCode;
    public TextView tv_plan_selected, tv_plan_name, tv_have_p, tv_error, tv_applied_code, tv_code, tv_code_dec, tv_total_payable,
            tv_price, tv_currency, tv_currency1, tv_original_price, card_rz, card_cf, card_ba, card_pt, card_st, card_PhonePe;
    public Button btn_apply;
    public RelativeLayout rlOpen;
    public ConstraintLayout csApplied;
    public int FINAL_PRICE;
    public CheckBox checkRazor, checkCashF, checkBank, checkPaytm, checkStripe, checkPhonepe;

    public LinearLayout btn_skip, lv_bank_detail;
    public RelativeLayout cbRazorPay, cbCashFree, cbBank, cbPaytm, cbStripe, cvPhonePe;
    public TextView tv_bankDetail;
    public ProgressBar pbPayment;
    public ImageView ivPayCancel;

    public String planPrice;

    String filePath;
    Uri imageUri;
    private String[] PERMISSIONS = {
            Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};

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
                                } else if (!hasPermission(requireActivity(), PERMISSIONS[i])) {
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
                                Intent i = new Intent(
                                        Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                                someActivityResultLauncher.launch(i);
                            }
                        }
                    });

    ActivityResultLauncher<Intent>   phonePeResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        // Handle the result in onActivityResult method
        Log.e("CALLED phonePeResultLauncher", ":  =========== "+ result.getResultCode() );
        if (result.getResultCode() == RESULT_OK) {
            Intent data = result.getData();
            // Handle the data as needed

            checkStatus();

        }else if(result.getResultCode() == 0 ){
            pbPayment.setVisibility(GONE);
            btn_skip.setVisibility(VISIBLE);
            Util.showLog("Canceled");
            dialogMsg.cancel();
            dialogMsg.showErrorDialog("Payment Canceled By User", "Ok");
            dialogMsg.show();
        }
    });

    String apiEndPoint = "/pg/v1/pay";

    String MERCHANT_TID = System.currentTimeMillis()+"";
    String BASE_URL = "https://api-preprod.phonepe.com/";
    private void checkStatus() {

        String xVerify = sha256("/pg/v1/status/"+prefManager.getString(Constant.PHONE_PE_MERCHANT_ID)+"/"+MERCHANT_TID + prefManager.getString(Constant.PHONE_PE_SALT_KEY) ) + "###1";

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-VERIFY", xVerify);
        headers.put("X-MERCHANT-ID", prefManager.getString(Constant.PHONE_PE_MERCHANT_ID));

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            //Background work here
            try {
                // Call the API Service
                Response<PhonePeResponseDTO> response = ApiClient.getApiServicePhonePe().checkStatus(prefManager.getString(Constant.PHONE_PE_MERCHANT_ID), MERCHANT_TID, headers).execute();

                // Wrap with APIResponse Class
                ApiResponse<PhonePeResponseDTO> apiResponse = new ApiResponse<>(response);

                // If response is successful
                if (apiResponse.isSuccessful()) {

                    Util.showLog("" + apiResponse.body + " " + apiResponse.body.toString());

                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            pbPayment.setVisibility(GONE);
                            btn_skip.setVisibility(VISIBLE);

                            if (apiResponse.body != null && apiResponse.body.isSuccess()) {
                                Log.d("phonepe", "onCreate: success");
                                Toast.makeText(requireActivity(), apiResponse.body.getMessage(), Toast.LENGTH_SHORT).show();
                                postPayment(apiResponse.body.getData().getTransactionId(), "phonepe");
                            }
                        }
                    });

                } else {
                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pbPayment.setVisibility(GONE);
                            btn_skip.setVisibility(VISIBLE);
                            dialogMsg.cancel();
                            dialogMsg.showErrorDialog(apiResponse.errorMessage, "Ok");
                            dialogMsg.show();
                        }
                    });
                }

            } catch (IOException e) {
                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }
            handler.post(() -> {
                //UI Thread work here

            });
        });

    }

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
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireActivity());
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

    String stripeOrderID = "";
    String paytmOrderID = "";
    String customerID = "";
    String publisherKey = "";
    String client_secret = "";
    Stripe stripe;
    String paymentIntentClientSecret;
    PaymentSheet.CustomerConfiguration customerConfig;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        dialog = inflater.inflate(R.layout.fragment_payment, container, false);

        if (android.os.Build.VERSION.SDK_INT > 31) {
            PERMISSIONS = new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES};
        }

        PhonePe.init(requireActivity());


        dialogMsg = new DialogMsg(requireActivity(), false);
        prefManager = new PrefManager(requireActivity());

        if (prefManager.getString(Constant.RazorPay).equals(Config.ONE)) {
            Checkout.preload(requireActivity());
        }
        if (prefManager.getString(Constant.CashFree).equals(Config.ONE)) {
            try {
                CFPaymentGatewayService.getInstance().setCheckoutCallback(this);
            } catch (CFException e) {
                e.printStackTrace();
            }
        }
        if (prefManager.getString(Constant.Stripe).equals(Config.ONE)) {
            paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);
        }

        setUpUi();
        initViewModel();
        return dialog;
    }

    private void initViewModel() {
        subPlanViewModel = new ViewModelProvider(this).get(SubsPlanViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        userViewModel.getDbUserData(prefManager.getString(Constant.USER_ID)).observe(this, item -> {
            if (item != null) {
                userItem = item.user;
            }
        });
    }

    private void setUpUi() {
        cbRazorPay = dialog.findViewById(R.id.cv_pay_razorpay);
        cbCashFree = dialog.findViewById(R.id.cv_pay_cashfree);
        cbBank = dialog.findViewById(R.id.cv_pay_bank);
        cbPaytm = dialog.findViewById(R.id.cv_pay_paytm);
        cbStripe = dialog.findViewById(R.id.cv_pay_stripe);
        cvPhonePe = dialog.findViewById(R.id.cv_pay_phonePe);
        pbPayment = dialog.findViewById(R.id.pb_payment);
        ivPayCancel = dialog.findViewById(R.id.iv_cancel);
        TextView title = (TextView) dialog.findViewById(R.id.tv1);
        card_rz = (TextView) dialog.findViewById(R.id.txt_card);
        card_cf = (TextView) dialog.findViewById(R.id.txt_cashf_card);
        card_ba = (TextView) dialog.findViewById(R.id.txt_bank);
        card_pt = (TextView) dialog.findViewById(R.id.txt_card_paytm);
        card_st = (TextView) dialog.findViewById(R.id.txt_card_stripe);
        card_PhonePe = (TextView) dialog.findViewById(R.id.txt_bank_PhonePe);
        tv_plan_selected = (TextView) dialog.findViewById(R.id.tv_plan_selected);
        tv_plan_name = (TextView) dialog.findViewById(R.id.tv_plan_name);
        tv_have_p = (TextView) dialog.findViewById(R.id.tv_have_p);
        tv_error = (TextView) dialog.findViewById(R.id.tv_error);
        tv_applied_code = (TextView) dialog.findViewById(R.id.tv_applied_code);
        tv_code = (TextView) dialog.findViewById(R.id.tv_code);
        tv_code_dec = (TextView) dialog.findViewById(R.id.tv_code_dec);
        tv_total_payable = (TextView) dialog.findViewById(R.id.tv_total_payable);
        tv_price = (TextView) dialog.findViewById(R.id.tv_price);
        tv_currency = (TextView) dialog.findViewById(R.id.tv_currency);
        tv_currency1 = (TextView) dialog.findViewById(R.id.tv_currency_1);
        tv_original_price = (TextView) dialog.findViewById(R.id.tv_ori_price);
        tv_bankDetail = (TextView) dialog.findViewById(R.id.tv_detail);
        etCode = (EditText) dialog.findViewById(R.id.et_promo);
        btn_apply = (Button) dialog.findViewById(R.id.btn_apply);
        btn_skip = (LinearLayout) dialog.findViewById(R.id.btn_skip);
        lv_bank_detail = (LinearLayout) dialog.findViewById(R.id.lv_bank_detail);

        checkRazor = dialog.findViewById(R.id.cb_razprpay);
        checkBank = dialog.findViewById(R.id.cb_bank);
        checkCashF = dialog.findViewById(R.id.cb_cashfree);
        checkStripe = dialog.findViewById(R.id.cb_stripe);
        checkPaytm = dialog.findViewById(R.id.cb_paytm);
        checkPhonepe = dialog.findViewById(R.id.cb_phonepe);

        if (prefManager.getString(Constant.CURRENCY).equals("USD")) {
            tv_currency.setText("$");
            tv_currency1.setText("$");
        }

        if (prefManager.getString(Constant.RazorPay).equals("1")) {

            cbRazorPay.setBackground(requireActivity().getDrawable(R.drawable.bg_phone_btn));
            cbRazorPay.setBackgroundTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.primary_color)));
            checkRazor.setButtonTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.white_1000)));
            card_rz.setTextColor(requireActivity().getColor(R.color.white_1000));
            checkRazor.setChecked(true);
        } else if (prefManager.getString(Constant.Paytm).equals("1")) {

            cbPaytm.setBackground(requireActivity().getDrawable(R.drawable.bg_phone_btn));
            cbPaytm.setBackgroundTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.primary_color)));
            checkPaytm.setButtonTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.white_1000)));
            card_pt.setTextColor(requireActivity().getColor(R.color.white_1000));
            checkPaytm.setChecked(true);
        } else if (prefManager.getString(Constant.Stripe).equals("1")) {

            cbStripe.setBackground(requireActivity().getDrawable(R.drawable.bg_phone_btn));
            cbStripe.setBackgroundTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.primary_color)));
            checkStripe.setButtonTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.white_1000)));
            card_st.setTextColor(requireActivity().getColor(R.color.white_1000));
            checkStripe.setChecked(true);
        } else if (prefManager.getString(Constant.CashFree).equals("1")) {

            cbCashFree.setBackground(requireActivity().getDrawable(R.drawable.bg_phone_btn));
            cbCashFree.setBackgroundTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.primary_color)));
            checkCashF.setButtonTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.white_1000)));
            card_cf.setTextColor(requireActivity().getColor(R.color.white_1000));
            checkCashF.setChecked(true);
        } else if (prefManager.getString(Constant.Offline).equals("1")) {

            cbBank.setBackground(requireActivity().getDrawable(R.drawable.bg_phone_btn));
            cbBank.setBackgroundTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.primary_color)));
            checkBank.setButtonTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.white_1000)));
            card_ba.setTextColor(requireActivity().getColor(R.color.white_1000));
            checkBank.setChecked(true);
            tv_bankDetail.setText(prefManager.getString(Constant.OFFLINE_DETAIL));
        } else if (prefManager.getString(Constant.phonePe).equals("1")) {

            cvPhonePe.setBackground(requireActivity().getDrawable(R.drawable.bg_phone_btn));
            cvPhonePe.setBackgroundTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.primary_color)));
            checkPhonepe.setButtonTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.white_1000)));
            card_PhonePe.setTextColor(requireActivity().getColor(R.color.white_1000));
            checkPhonepe.setChecked(true);
        }
        /*TODO PHONE PE*/



        if (prefManager.getString(Constant.CashFree).equals("1")) {
            cbCashFree.setVisibility(VISIBLE);
        }
        if (prefManager.getString(Constant.Offline).equals("1")) {
            cbBank.setVisibility(VISIBLE);
        }
        if (prefManager.getString(Constant.RazorPay).equals("1")) {
            cbRazorPay.setVisibility(VISIBLE);
        }
        if (prefManager.getString(Constant.Paytm).equals("1")) {
            cbPaytm.setVisibility(VISIBLE);
        }
        if (prefManager.getString(Constant.Stripe).equals("1")) {
            cbStripe.setVisibility(VISIBLE);
        }

        if (prefManager.getString(Constant.phonePe).equals("1")) {
            cvPhonePe.setVisibility(VISIBLE);
        }

        checkRazor.setOnClickListener(v -> {
            setButtonStyle("RAZORPAY");
        });

        checkCashF.setOnClickListener(v -> {
            setButtonStyle("CASH");
        });

        checkBank.setOnClickListener(v -> {
            setButtonStyle("BANK");
        });

        cbRazorPay.setOnClickListener(v -> {
            setButtonStyle("RAZORPAY");
        });

        cbBank.setOnClickListener(v -> {
            setButtonStyle("BANK");
        });
        cbCashFree.setOnClickListener(v -> {
            setButtonStyle("CASH");
        });
        cbStripe.setOnClickListener(v -> {
            setButtonStyle("STRIPE");
        });
        cbPaytm.setOnClickListener(v -> {
            setButtonStyle("PAYTM");
        });

        /*TODO PHONE PE*/

        cvPhonePe.setOnClickListener(v -> {
            setButtonStyle("PHONEPE");
        });

        LinearLayout lvAdd = (LinearLayout) dialog.findViewById(R.id.lv_add);
        lvAdd.setOnClickListener(v -> {
            Dexter.withContext(requireActivity()).withPermissions(PERMISSIONS).withListener(new MultiplePermissionsListener() {
                public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                    if (multiplePermissionsReport.areAllPermissionsGranted()) {
                        Intent i = new Intent(
                                Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                        someActivityResultLauncher.launch(i);
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
//                    Toast.makeText(AddBusinessActivity.this, "Error occurred! ", Toast.LENGTH_SHORT).show();
                    permissionsList = new ArrayList<>();
                    permissionsList.addAll(Arrays.asList(PERMISSIONS));
                    askForPermissions(permissionsList);
                }
            }).onSameThread().check();
        });

        rlOpen = (RelativeLayout) dialog.findViewById(R.id.rl_open);
        csApplied = (ConstraintLayout) dialog.findViewById(R.id.cs_applied);

        GlideBinding.setTextSize(etCode, "edit_text");
        GlideBinding.setTextSize(btn_apply, "button_text_12");

        GlideBinding.setTextSize(tv_plan_selected, "font_body_size");
        GlideBinding.setTextSize(tv_plan_name, "font_title_size");
        GlideBinding.setTextSize(tv_have_p, "font_body_size");
        GlideBinding.setTextSize(tv_error, "font_body_s_size");
        GlideBinding.setTextSize(tv_applied_code, "font_body_size");
        GlideBinding.setTextSize(tv_code, "font_body_size");
        GlideBinding.setTextSize(tv_code_dec, "font_body_size");
        GlideBinding.setTextSize(tv_total_payable, "font_h7_size");
        GlideBinding.setTextSize(tv_price, "font_h6_size");
        GlideBinding.setTextSize(tv_original_price, "font_h7_size");
        GlideBinding.setTextSize(tv_currency, "font_h6_size");
        GlideBinding.setTextSize(tv_currency1, "font_title_size");
        GlideBinding.setTextSize(card_rz, "font_body_s_size");
        GlideBinding.setTextSize(card_cf, "font_body_s_size");
        GlideBinding.setTextSize(card_ba, "font_body_s_size");
        GlideBinding.setTextSize(title, "font_title_size");

        GlideBinding.setFont(etCode, "bold");
        GlideBinding.setFont(btn_apply, "medium");

        GlideBinding.setFont(tv_plan_selected, "normal");
        GlideBinding.setFont(tv_plan_name, "extra_bold");
        GlideBinding.setFont(tv_have_p, "bold");
        GlideBinding.setFont(tv_error, "normal");
        GlideBinding.setFont(tv_applied_code, "normal");
        GlideBinding.setFont(tv_code, "extra_bold");
        GlideBinding.setFont(tv_code_dec, "normal");
        GlideBinding.setFont(tv_total_payable, "extra_bold");
        GlideBinding.setFont(tv_price, "normal");
        GlideBinding.setFont(tv_original_price, "normal");
        GlideBinding.setFont(tv_currency, "normal");
        GlideBinding.setFont(tv_currency1, "normal");
        GlideBinding.setFont(card_rz, "extra_bold");
        GlideBinding.setFont(card_cf, "extra_bold");
        GlideBinding.setFont(card_ba, "extra_bold");
        GlideBinding.setFont(title, "extra_bold");

        tv_plan_name.setText(item.planName);
        tv_original_price.setText(item.planPrice);
        tv_price.setText(item.planPrice);

        double price = Double.parseDouble(item.planPrice);

        FINAL_PRICE = (int) price;

        rlOpen.setVisibility(VISIBLE);

        btn_apply.setOnClickListener(v -> {

            if (etCode.getText().toString().equals("")) {
                Util.showToast(requireActivity(), "Enter Code");
                return;
            }

            btn_apply.setEnabled(false);
            btn_apply.setText("Checking...");

            checkCoupon(userItem.userId, etCode.getText().toString());
        });

        btn_skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pbPayment.setVisibility(VISIBLE);
                btn_skip.setVisibility(GONE);

                if (FINAL_PRICE == 0) {
                    postPayment("100%_coupon_use", "Free");
                } else {
                    if (checkRazor.isChecked()) {
                        startPayment(FINAL_PRICE, prefManager.getString(Constant.RAZORPAY_KEY_ID));
                    } else if (checkCashF.isChecked()) {
                        createOrderCashfree(FINAL_PRICE);
                    } else if (checkBank.isChecked()) {
                        checkOffline();
                    } else if (checkPaytm.isChecked()) {
                        createPaytm();
                    } else if (checkStripe.isChecked()) {
                        createStripePayment();
                    } else if (checkPhonepe.isChecked()) {
                        createPhonePePayment();
                    }
                }
            }
        });

        ivPayCancel.setOnClickListener(v -> {
            dismiss();
        });

    }


    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle("Need Permissions");
        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.");
        builder.setPositiveButton("GOTO SETTINGS", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
                Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
                intent.setData(Uri.fromParts("package", requireActivity().getPackageName(), (String) null));
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

    private void checkOffline() {
        if (filePath == null || imageUri == null) {
            Util.showToast(requireActivity(), "Please Attach a Receipt");
            pbPayment.setVisibility(GONE);
            btn_skip.setVisibility(VISIBLE);
            return;
        }

        btn_skip.setVisibility(GONE);
        subPlanViewModel.offlinePayment(filePath, imageUri, prefManager.getString(Constant.USER_ID), item.id,
                String.valueOf(FINAL_PRICE), tv_code.getText().toString(),
                prefManager.getString(Constant.REFER_CODE_BY)).observe(this,
                result -> {
                    if (result != null) {
                        switch (result.status) {
                            case SUCCESS:
                                pbPayment.setVisibility(GONE);
                                btn_skip.setVisibility(VISIBLE);
                                dialogMsg.cancel();

                                dialogMsg.showSuccessDialog("Thanks for Payment\nWe will active your plan in your account within 5 hours",
                                        getString(R.string.ok));
                                dialogMsg.show();
                                dialogMsg.okBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialogMsg.cancel();
                                        callbackResult.sendResult(request_code, true);
                                    }
                                });
                                break;

                            case ERROR:
                                btn_skip.setVisibility(VISIBLE);
                                pbPayment.setVisibility(GONE);
                                dialogMsg.cancel();

                                dialogMsg.showErrorDialog(result.message, getString(R.string.ok));
                                dialogMsg.show();
                                break;
                        }
                    }

                });
    }

    private void startPayment(int planPrice, String key) {
        /**
         * Instantiate Checkout
         */
        Checkout checkout = new Checkout();
        checkout.setKeyID(key);
        /**
         * Set your logo here
         */
        checkout.setImage(R.drawable.login_logo);
        /**
         * Reference to current requireActivity()
         */
        /**
         * Pass your payment options to the Razorpay Checkout as a JSONObject
         */
        try {
            JSONObject options = new JSONObject();

            options.put("name", userItem.userName);
            options.put("description", "Charge Of Plan");
            options.put("theme.color", "#1565C0");
            options.put("send_sms_hash", true);
            options.put("allow_rotation", true);
            options.put("currency", prefManager.getString(Constant.CURRENCY));
            options.put("amount", (float) planPrice * 100);//pass amount in currency subunits
            options.put("prefill.email", userItem.email);
            if (userItem.phone != null && !userItem.phone.equals("")) {
                options.put("prefill.contact", userItem.phone);
            }
            checkout.open(requireActivity(), options);

        } catch (Exception e) {
            Util.showErrorLog("Error in starting Razorpay Checkout", e);
        }
    }

    private void createOrderCashfree(int final_price) {
        if (userItem.phone == null || userItem.phone.equals("") || userItem.phone.length() > 10) {
            Dialog dialog = new Dialog(requireActivity());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            DialogEnquiryBinding binding = DialogEnquiryBinding.inflate(getLayoutInflater());
            dialog.setContentView(binding.getRoot());
            if (dialog.getWindow() != null) {
                dialog.getWindow().setAttributes(getLayoutParams(dialog));

                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }
            dialog.setCancelable(true);

            if (MyApplication.prefManager().getBoolean(Constant.IS_LOGIN)) {
                binding.etName.setText(MyApplication.prefManager().getString(Constant.USER_NAME));
                binding.etEmail.setText(MyApplication.prefManager().getString(Constant.USER_EMAIL));
                binding.etEmail.setEnabled(MyApplication.prefManager().getString(Constant.USER_EMAIL).equals("") ? true : false);
                binding.etMobile.setText(MyApplication.prefManager().getString(Constant.USER_PHONE));
            }
            binding.etDetails.setVisibility(GONE);
            binding.textViewi.setVisibility(GONE);

            binding.btnSave.setOnClickListener(v -> {

                if (binding.etEmail.getText().toString().trim().isEmpty()) {
                    binding.etEmail.setError(getResources().getString(R.string.enter_email));
                    binding.etEmail.requestFocus();
                    return;
                }
                if (binding.etName.getText().toString().trim().isEmpty()) {
                    binding.etName.setError(getResources().getString(R.string.enter_name));
                    binding.etName.requestFocus();
                    return;
                }
                if (!isEmailValid(binding.etEmail.getText().toString())) {
                    binding.etEmail.setError(getString(R.string.invalid_email));
                    binding.etEmail.requestFocus();
                    return;
                }
                if (binding.etMobile.getText().toString().trim().isEmpty()) {
                    binding.etMobile.setError(getResources().getString(R.string.please_enter_valid_mobile));
                    binding.etMobile.requestFocus();
                    return;
                }
                if (binding.etMobile.getText().toString().length() > 10) {
                    binding.etMobile.setError("Please Enter 10 Digit Mobile");
                    binding.etMobile.requestFocus();
                    return;
                }
//                if (binding.etDetails.getText().toString().trim().isEmpty()) {
//                    binding.etDetails.setError(getResources().getString(R.string.enter_details));
//                    binding.etDetails.requestFocus();
//                    return;
//                }

                String name = binding.etName.getText().toString().trim();
                String email = binding.etEmail.getText().toString().trim();
                String mobile = binding.etMobile.getText().toString().trim();

                dialog.dismiss();

                continueOrder(final_price, name, email, mobile);


            });
            binding.ivCancel.setVisibility(GONE);
            binding.ivCancel.setOnClickListener(v -> {
                dialog.cancel();
            });
            dialog.show();
        } else {

            continueOrder(final_price, userItem.userName, userItem.email, userItem.phone);

        }
    }

    private void continueOrder(int final_price, String name, String email, String mobile) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            //Background work here
            try {

                // Call the API Service
                Response<CashFreeOrder> response = ApiClient.getApiService().createOrderCashFree(prefManager.getString(Constant.api_key),
                        prefManager.getString(Constant.USER_ID),
                        final_price,
                        name,
                        email,
                        mobile).execute();


                // Wrap with APIResponse Class
                ApiResponse<CashFreeOrder> apiResponse = new ApiResponse<>(response);

                // If response is successful
                if (apiResponse.isSuccessful()) {

                    Util.showLog("" + apiResponse.body);

                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            doDropCheckoutPayment(apiResponse.body.order_id, apiResponse.body.payment_session_id);
                        }
                    });

                } else {
                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Util.showLog("EEE: " + apiResponse.errorMessage);
                            pbPayment.setVisibility(GONE);
                            btn_skip.setVisibility(VISIBLE);
                            dialogMsg.cancel();
                            dialogMsg.showErrorDialog(apiResponse.errorMessage, getString(R.string.ok));
                            dialogMsg.show();
                        }
                    });
                }

            } catch (IOException e) {
                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Util.showLog("EEE: " + "Coupon Code Not Valid");
                        pbPayment.setVisibility(GONE);
                        btn_skip.setVisibility(VISIBLE);
                        dialogMsg.cancel();
                        dialogMsg.showErrorDialog("Error while Creating Order", getString(R.string.ok));
                        dialogMsg.show();
                    }
                });
            }
            handler.post(() -> {
                //UI Thread work here

            });
        });
    }

    private WindowManager.LayoutParams getLayoutParams(@NonNull Dialog dialog) {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        if (dialog.getWindow() != null) {
            layoutParams.copyFrom(dialog.getWindow().getAttributes());
        }
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        return layoutParams;
    }

    private boolean isEmailValid(String email) {
        return email.contains("@") && !email.contains(" ");
    }

    public void doDropCheckoutPayment(String orderId, String token) {
        try {
            CFSession cfSession;
            if (prefManager.getString(Constant.CASHFREE_TYPE).equals("Test")) {
                cfSession = new CFSession.CFSessionBuilder()
                        .setEnvironment(CFSession.Environment.SANDBOX)
                        .setPaymentSessionID(token)
                        .setOrderId(orderId)
                        .build();
            } else {
                cfSession = new CFSession.CFSessionBuilder()
                        .setEnvironment(CFSession.Environment.PRODUCTION)
                        .setPaymentSessionID(token)
                        .setOrderId(orderId)
                        .build();
            }
            CFPaymentComponent cfPaymentComponent =
                    new CFPaymentComponent.CFPaymentComponentBuilder()
                            // Shows only Card and UPI modes
                            .add(CFPaymentComponent.CFPaymentModes.CARD)
                            .add(CFPaymentComponent.CFPaymentModes.UPI)
                            .build();
            // Replace with your application's theme colors
            CFTheme cfTheme = new CFTheme.CFThemeBuilder()
                    .setNavigationBarBackgroundColor("#fc2678")
                    .setNavigationBarTextColor("#ffffff")
                    .setButtonBackgroundColor("#fc2678")
                    .setButtonTextColor("#ffffff")
                    .setPrimaryTextColor("#000000")
                    .setSecondaryTextColor("#000000")
                    .build();
            CFDropCheckoutPayment cfDropCheckoutPayment = new CFDropCheckoutPayment.CFDropCheckoutPaymentBuilder()
                    .setSession(cfSession)
                    .setCFUIPaymentModes(cfPaymentComponent)
                    .setCFNativeCheckoutUITheme(cfTheme)
                    .build();
            CFPaymentGatewayService gatewayService = CFPaymentGatewayService.getInstance();
            gatewayService.doPayment(requireActivity(), cfDropCheckoutPayment);
        } catch (CFException exception) {
            exception.printStackTrace();
        }
    }


    @Override
    public void onPaymentVerify(String s) {
        pbPayment.setVisibility(GONE);
        btn_skip.setVisibility(VISIBLE);
        postPayment(s, "CashFree");
    }

    @Override
    public void onPaymentFailure(CFErrorResponse cfErrorResponse, String s) {
        pbPayment.setVisibility(GONE);
        btn_skip.setVisibility(VISIBLE);
        Util.showLog("Er: " + cfErrorResponse.getMessage() + " " + cfErrorResponse.toJSON().toString());
        dialogMsg.cancel();

        dialogMsg.showErrorDialog(cfErrorResponse.getDescription(), getString(R.string.ok));
        dialogMsg.show();
    }

    public void onPaymentSuccess(String paymentId) {
        pbPayment.setVisibility(GONE);
        btn_skip.setVisibility(VISIBLE);
        postPayment(paymentId, "RazorPay");
    }

    public void onPaymentError(int i, String s) {
        String message = "";
        if (i == Checkout.PAYMENT_CANCELED) {
            message = "The user canceled the payment.";
        } else if (i == Checkout.NETWORK_ERROR) {
            message = "There was a network error, for iqueen, loss of internet connectivity.";
        } else if (i == Checkout.INVALID_OPTIONS) {
            message = "An issue with options passed in checkout.open .";
        } else if (i == Checkout.TLS_ERROR) {
            message = "The device does not support TLS v1.1 or TLS v1.2.";
        } else {
            message = "Unknown Error";
        }
        pbPayment.setVisibility(GONE);
        btn_skip.setVisibility(VISIBLE);
        dialogMsg.cancel();
        Util.showLog(i + " " + s);
        dialogMsg.showErrorDialog(message, getString(R.string.ok));
        dialogMsg.show();
    }

    private void postPayment(String paymentId, String type) {
        Util.showLog("ORDERID: " + paymentId);
        subPlanViewModel.loadPayment(prefManager.getString(Constant.USER_ID), item.id, paymentId,
                String.valueOf(FINAL_PRICE), tv_code.getText().toString(),
                prefManager.getString(Constant.REFER_CODE_BY), type).observe(this,
                result -> {
                    if (result != null) {
                        switch (result.status) {
                            case SUCCESS:
                                pbPayment.setVisibility(GONE);
                                btn_skip.setVisibility(VISIBLE);
                                dialogMsg.cancel();

                                dialogMsg.showSuccessDialog(result.data.message, getString(R.string.ok));
                                dialogMsg.show();
                                dialogMsg.okBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialogMsg.cancel();
                                        callbackResult.sendResult(request_code, true);
                                    }
                                });
                                break;

                            case ERROR:

                                pbPayment.setVisibility(GONE);
                                btn_skip.setVisibility(VISIBLE);
                                dialogMsg.cancel();

                                dialogMsg.showErrorDialog(result.message, getString(R.string.ok));
                                dialogMsg.show();
                                break;
                        }
                    }
                });
    }

    public interface CallbackResult {
        void sendResult(int requestCode, Object obj);
    }

    public void setButtonStyle(String type) {
        if (type.equals("RAZORPAY")) {
            cbRazorPay.setBackground(requireActivity().getDrawable(R.drawable.bg_phone_btn));
            cbRazorPay.setBackgroundTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.primary_color)));
            checkRazor.setButtonTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.white_1000)));

            card_rz.setTextColor(requireActivity().getColor(R.color.white_1000));
            card_cf.setTextColor(requireActivity().getColor(R.color.acc_gray));
            card_ba.setTextColor(requireActivity().getColor(R.color.acc_gray));
            card_st.setTextColor(requireActivity().getColor(R.color.acc_gray));
            card_pt.setTextColor(requireActivity().getColor(R.color.acc_gray));

            checkRazor.setChecked(true);
            lv_bank_detail.setVisibility(View.GONE);

            cbCashFree.setBackground(requireActivity().getDrawable(R.drawable.rounded_border2x));
            cbCashFree.setBackgroundTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.gray)));
            checkCashF.setButtonTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.tg_check)));
            checkCashF.setChecked(false);

            cbBank.setBackground(requireActivity().getDrawable(R.drawable.rounded_border2x));
            cbBank.setBackgroundTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.gray)));
            checkBank.setButtonTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.tg_check)));
            checkBank.setChecked(false);

            cbPaytm.setBackground(requireActivity().getDrawable(R.drawable.rounded_border2x));
            cbPaytm.setBackgroundTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.gray)));
            checkPaytm.setButtonTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.tg_check)));
            checkPaytm.setChecked(false);

            cbStripe.setBackground(requireActivity().getDrawable(R.drawable.rounded_border2x));
            cbStripe.setBackgroundTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.gray)));
            checkStripe.setButtonTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.tg_check)));
            checkStripe.setChecked(false);

            /*TODO PHONE PE*/

            cvPhonePe.setBackground(requireActivity().getDrawable(R.drawable.rounded_border2x));
            cvPhonePe.setBackgroundTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.gray)));
            checkPhonepe.setButtonTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.tg_check)));
            checkPhonepe.setChecked(false);

        } else if (type.equals("CASH")) {
            cbCashFree.setBackground(requireActivity().getDrawable(R.drawable.bg_phone_btn));
            cbCashFree.setBackgroundTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.primary_color)));
            checkCashF.setButtonTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.white_1000)));
            checkCashF.setChecked(true);
            lv_bank_detail.setVisibility(View.GONE);

            card_rz.setTextColor(requireActivity().getColor(R.color.acc_gray));
            card_cf.setTextColor(requireActivity().getColor(R.color.white_1000));
            card_ba.setTextColor(requireActivity().getColor(R.color.acc_gray));
            card_st.setTextColor(requireActivity().getColor(R.color.acc_gray));
            card_pt.setTextColor(requireActivity().getColor(R.color.acc_gray));
            card_pt.setTextColor(requireActivity().getColor(R.color.acc_gray));
            card_PhonePe.setTextColor(requireActivity().getColor(R.color.acc_gray));

            cbRazorPay.setBackground(requireActivity().getDrawable(R.drawable.rounded_border2x));
            cbRazorPay.setBackgroundTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.gray)));
            checkRazor.setButtonTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.tg_check)));
            checkRazor.setChecked(false);

            cbBank.setBackground(requireActivity().getDrawable(R.drawable.rounded_border2x));
            cbBank.setBackgroundTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.gray)));
            checkBank.setButtonTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.tg_check)));
            checkBank.setChecked(false);

            cbPaytm.setBackground(requireActivity().getDrawable(R.drawable.rounded_border2x));
            cbPaytm.setBackgroundTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.gray)));
            checkPaytm.setButtonTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.tg_check)));
            checkPaytm.setChecked(false);

            cbStripe.setBackground(requireActivity().getDrawable(R.drawable.rounded_border2x));
            cbStripe.setBackgroundTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.gray)));
            checkStripe.setButtonTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.tg_check)));
            checkStripe.setChecked(false);

            /*TODO PHONE PE*/

            cvPhonePe.setBackground(requireActivity().getDrawable(R.drawable.rounded_border2x));
            cvPhonePe.setBackgroundTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.gray)));
            checkPhonepe.setButtonTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.tg_check)));
            checkPhonepe.setChecked(false);


        } else if (type.equals("BANK")) {
            cbBank.setBackground(requireActivity().getDrawable(R.drawable.bg_phone_btn));
            cbBank.setBackgroundTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.primary_color)));
            checkBank.setButtonTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.white_1000)));
            checkBank.setChecked(true);

            card_rz.setTextColor(requireActivity().getColor(R.color.acc_gray));
            card_cf.setTextColor(requireActivity().getColor(R.color.acc_gray));
            card_ba.setTextColor(requireActivity().getColor(R.color.white_1000));
            card_st.setTextColor(requireActivity().getColor(R.color.acc_gray));
            card_pt.setTextColor(requireActivity().getColor(R.color.acc_gray));
            card_PhonePe.setTextColor(requireActivity().getColor(R.color.acc_gray));

            lv_bank_detail.setVisibility(VISIBLE);
            tv_bankDetail.setText(prefManager.getString(Constant.OFFLINE_DETAIL));

            cbRazorPay.setBackground(requireActivity().getDrawable(R.drawable.rounded_border2x));
            cbRazorPay.setBackgroundTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.gray)));
            checkRazor.setButtonTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.tg_check)));
            checkRazor.setChecked(false);

            cbCashFree.setBackground(requireActivity().getDrawable(R.drawable.rounded_border2x));
            cbCashFree.setBackgroundTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.gray)));
            checkCashF.setButtonTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.tg_check)));
            checkCashF.setChecked(false);

            cbPaytm.setBackground(requireActivity().getDrawable(R.drawable.rounded_border2x));
            cbPaytm.setBackgroundTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.gray)));
            checkPaytm.setButtonTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.tg_check)));
            checkPaytm.setChecked(false);

            cbStripe.setBackground(requireActivity().getDrawable(R.drawable.rounded_border2x));
            cbStripe.setBackgroundTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.gray)));
            checkStripe.setButtonTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.tg_check)));
            checkStripe.setChecked(false);

            /*TODO PHONE PE*/

            cvPhonePe.setBackground(requireActivity().getDrawable(R.drawable.rounded_border2x));
            cvPhonePe.setBackgroundTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.gray)));
            checkPhonepe.setButtonTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.tg_check)));
            checkPhonepe.setChecked(false);

        } else if (type.equals("PAYTM")) {
            cbPaytm.setBackground(requireActivity().getDrawable(R.drawable.bg_phone_btn));
            cbPaytm.setBackgroundTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.primary_color)));
            checkPaytm.setButtonTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.white_1000)));
            checkPaytm.setChecked(true);

            lv_bank_detail.setVisibility(View.GONE);

            card_rz.setTextColor(requireActivity().getColor(R.color.acc_gray));
            card_cf.setTextColor(requireActivity().getColor(R.color.acc_gray));
            card_ba.setTextColor(requireActivity().getColor(R.color.acc_gray));
            card_st.setTextColor(requireActivity().getColor(R.color.acc_gray));
            card_pt.setTextColor(requireActivity().getColor(R.color.white_1000));
            card_PhonePe.setTextColor(requireActivity().getColor(R.color.acc_gray));

            cbRazorPay.setBackground(requireActivity().getDrawable(R.drawable.rounded_border2x));
            cbRazorPay.setBackgroundTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.gray)));
            checkRazor.setButtonTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.tg_check)));
            checkRazor.setChecked(false);

            cbCashFree.setBackground(requireActivity().getDrawable(R.drawable.rounded_border2x));
            cbCashFree.setBackgroundTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.gray)));
            checkCashF.setButtonTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.tg_check)));
            checkCashF.setChecked(false);

            cbBank.setBackground(requireActivity().getDrawable(R.drawable.rounded_border2x));
            cbBank.setBackgroundTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.gray)));
            checkBank.setButtonTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.tg_check)));
            checkBank.setChecked(false);

            cbStripe.setBackground(requireActivity().getDrawable(R.drawable.rounded_border2x));
            cbStripe.setBackgroundTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.gray)));
            checkStripe.setButtonTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.tg_check)));
            checkStripe.setChecked(false);

            /* TODO PHONE PE */

            cvPhonePe.setBackground(requireActivity().getDrawable(R.drawable.rounded_border2x));
            cvPhonePe.setBackgroundTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.gray)));
            checkPhonepe.setButtonTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.tg_check)));
            checkPhonepe.setChecked(false);

        } else if (type.equals("STRIPE")) {
            cbStripe.setBackground(requireActivity().getDrawable(R.drawable.bg_phone_btn));
            cbStripe.setBackgroundTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.primary_color)));
            checkStripe.setButtonTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.white_1000)));
            checkStripe.setChecked(true);

            lv_bank_detail.setVisibility(View.GONE);

            card_rz.setTextColor(requireActivity().getColor(R.color.acc_gray));
            card_cf.setTextColor(requireActivity().getColor(R.color.acc_gray));
            card_ba.setTextColor(requireActivity().getColor(R.color.acc_gray));
            card_st.setTextColor(requireActivity().getColor(R.color.white_1000));
            card_pt.setTextColor(requireActivity().getColor(R.color.acc_gray));
            card_PhonePe.setTextColor(requireActivity().getColor(R.color.acc_gray));

            cbRazorPay.setBackground(requireActivity().getDrawable(R.drawable.rounded_border2x));
            cbRazorPay.setBackgroundTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.gray)));
            checkRazor.setButtonTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.tg_check)));
            checkRazor.setChecked(false);

            cbCashFree.setBackground(requireActivity().getDrawable(R.drawable.rounded_border2x));
            cbCashFree.setBackgroundTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.gray)));
            checkCashF.setButtonTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.tg_check)));
            checkCashF.setChecked(false);

            cbBank.setBackground(requireActivity().getDrawable(R.drawable.rounded_border2x));
            cbBank.setBackgroundTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.gray)));
            checkBank.setButtonTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.tg_check)));
            checkBank.setChecked(false);

            cbPaytm.setBackground(requireActivity().getDrawable(R.drawable.rounded_border2x));
            cbPaytm.setBackgroundTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.gray)));
            checkPaytm.setButtonTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.tg_check)));
            checkPaytm.setChecked(false);


            /*TODO PHONE PE*/
            cvPhonePe.setBackground(requireActivity().getDrawable(R.drawable.rounded_border2x));
            cvPhonePe.setBackgroundTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.gray)));
            checkPhonepe.setButtonTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.tg_check)));
            checkPhonepe.setChecked(false);

        }
        /*TODO PHONE PE*/
        else if (type.equals("PHONEPE")) {

            cvPhonePe.setBackground(requireActivity().getDrawable(R.drawable.bg_phone_btn));
            cvPhonePe.setBackgroundTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.primary_color)));
            checkPhonepe.setButtonTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.white_1000)));
            card_PhonePe.setTextColor(requireActivity().getColor(R.color.white_1000));
            checkPhonepe.setChecked(true);


            lv_bank_detail.setVisibility(View.GONE);

            card_rz.setTextColor(requireActivity().getColor(R.color.acc_gray));
            card_cf.setTextColor(requireActivity().getColor(R.color.acc_gray));
            card_ba.setTextColor(requireActivity().getColor(R.color.acc_gray));
            card_st.setTextColor(requireActivity().getColor(R.color.acc_gray));
            card_pt.setTextColor(requireActivity().getColor(R.color.acc_gray));
            card_PhonePe.setTextColor(requireActivity().getColor(R.color.white_1000));


            cbRazorPay.setBackground(requireActivity().getDrawable(R.drawable.rounded_border2x));
            cbRazorPay.setBackgroundTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.gray)));
            checkRazor.setButtonTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.tg_check)));
            checkRazor.setChecked(false);

            cbCashFree.setBackground(requireActivity().getDrawable(R.drawable.rounded_border2x));
            cbCashFree.setBackgroundTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.gray)));
            checkCashF.setButtonTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.tg_check)));
            checkCashF.setChecked(false);

            cbBank.setBackground(requireActivity().getDrawable(R.drawable.rounded_border2x));
            cbBank.setBackgroundTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.gray)));
            checkBank.setButtonTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.tg_check)));
            checkBank.setChecked(false);

            cbStripe.setBackground(requireActivity().getDrawable(R.drawable.rounded_border2x));
            cbStripe.setBackgroundTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.gray)));
            checkStripe.setButtonTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.tg_check)));
            checkStripe.setChecked(false);


        }
    }

    private void checkCoupon(String userId, String couponCode) {

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            //Background work here
            try {

                // Call the API Service
                Response<CouponItem> response = ApiClient.getApiService().checkCoupon(prefManager.getString(Constant.api_key),
                        userId,
                        couponCode).execute();


                // Wrap with APIResponse Class
                ApiResponse<CouponItem> apiResponse = new ApiResponse<>(response);

                // If response is successful
                if (apiResponse.isSuccessful()) {

                    Util.showLog("" + apiResponse.body);

                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            double discountPrice = Double.parseDouble(apiResponse.body.discount) * FINAL_PRICE / 100;

                            double price = FINAL_PRICE - discountPrice;

                            tv_price.setText("" + new DecimalFormat("##.##").format(price));

                            FINAL_PRICE = (int) price;

                            planPrice = String.valueOf(FINAL_PRICE);

                            rlOpen.setVisibility(GONE);
                            csApplied.setVisibility(VISIBLE);

                            tv_code.setText(etCode.getText().toString());
                            tv_code_dec.setText(apiResponse.body.discount + "% " + getString(R.string.discount_on) + " " + tv_plan_name.getText());

                            if(price == 0.0){
                                cbRazorPay.setVisibility(GONE);
                                cbCashFree.setVisibility(GONE);
                                cbBank.setVisibility(GONE);
                                cbPaytm.setVisibility(GONE);
                                cbStripe.setVisibility(GONE);
                                cvPhonePe.setVisibility(GONE);

                            }else{
                                btn_apply.setEnabled(true);
                                cbRazorPay.setEnabled(true);
                            }




                        }
                    });

                } else {
                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            Util.showLog("EEE: " + apiResponse.errorMessage);
                            tv_error.setText(apiResponse.errorMessage);
                            tv_error.setVisibility(VISIBLE);

                            btn_apply.setEnabled(true);
                            cbRazorPay.setEnabled(true);
                            btn_apply.setText(getString(R.string.apply));
                        }
                    });
                }

            } catch (IOException e) {
                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Util.showLog("EEE: " + "Coupon Code Not Valid");
                        tv_error.setText("Coupon Code Not Valid");
                        tv_error.setVisibility(VISIBLE);

                        btn_apply.setEnabled(true);
                        cbRazorPay.setEnabled(true);
                        btn_apply.setText(getString(R.string.apply));
                    }
                });
            }
            handler.post(() -> {
                //UI Thread work here

            });
        });

    }

    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // Here, no request code
                        if (result.getData() != null) {
                            Uri selectedImage = result.getData().getData();
                            String[] filePathColumn = {MediaStore.Images.Media.DATA};

                            if (selectedImage != null) {
                                Cursor cursor = requireActivity().getContentResolver().query(selectedImage,
                                        null, null, null, null);

                                if (cursor != null) {
                                    cursor.moveToFirst();

                                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                    filePath = cursor.getString(columnIndex);
                                    cursor.close();

                                    imageUri = selectedImage;
                                    GlideBinding.bindImage((ImageView) dialog.findViewById(R.id.iv_image), filePath);
                                }
                            }
                        }
                    }
                }
            });

    //**** Stripe

    private void createStripePayment() {
        stripeOrderID = "STRIPE_" + System.currentTimeMillis();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            //Background work here
            try {

                // Call the API Service
                Response<StripeResponse> response = ApiClient.getApiService().createStripePayment(
                        prefManager.getString(Constant.api_key),
                        String.valueOf(FINAL_PRICE)).execute();


                // Wrap with APIResponse Class
                ApiResponse<StripeResponse> apiResponse = new ApiResponse<>(response);

                // If response is successful
                if (apiResponse.isSuccessful()) {

                    Util.showLog("" + apiResponse.body + " " + apiResponse.body.toString());

                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            publisherKey = response.body().publishableKey;

                            Util.showLog("KEY: " + publisherKey + " " + response.body().customer + " "
                                    + response.body().ephemeralKey);

                            customerConfig = new PaymentSheet.CustomerConfiguration(
                                    response.body().customer,
                                    response.body().ephemeralKey
                            );
                            paymentIntentClientSecret = response.body().paymentIntent;
                            PaymentConfiguration.init(requireActivity().getApplicationContext(), publisherKey);
                            presentPaymentSheet();
                        }
                    });

                } else {
                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pbPayment.setVisibility(GONE);
                            btn_skip.setVisibility(VISIBLE);
                            dialogMsg.cancel();
                            dialogMsg.showErrorDialog(apiResponse.errorMessage, "Ok");
                            dialogMsg.show();
                        }
                    });
                }

            } catch (IOException e) {
                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }
            handler.post(() -> {
                //UI Thread work here

            });
        });
    }

    private void presentPaymentSheet() {
        final PaymentSheet.Configuration configuration = new PaymentSheet.Configuration.Builder(getString(R.string.app_name))
                .customer(customerConfig)
                // Set `allowsDelayedPaymentMethods` to true if your business can handle payment methods
                // that complete payment after a delay, like SEPA Debit and Sofort.
                .allowsDelayedPaymentMethods(true)
                .build();
        paymentSheet.presentWithPaymentIntent(
                paymentIntentClientSecret,
                configuration
        );
    }

    void onPaymentSheetResult(final PaymentSheetResult paymentSheetResult) {
        // implemented in the next steps
        if (paymentSheetResult instanceof PaymentSheetResult.Canceled) {
            pbPayment.setVisibility(GONE);
            btn_skip.setVisibility(VISIBLE);
            Util.showLog("Canceled");
            dialogMsg.cancel();
            dialogMsg.showErrorDialog("Payment Canceled By User", "Ok");
            dialogMsg.show();
        } else if (paymentSheetResult instanceof PaymentSheetResult.Failed) {
            pbPayment.setVisibility(GONE);
            btn_skip.setVisibility(VISIBLE);
            Util.showLog("Got error: " + ((PaymentSheetResult.Failed) paymentSheetResult).getError());
            dialogMsg.cancel();
            dialogMsg.showErrorDialog("" + ((PaymentSheetResult.Failed) paymentSheetResult).getError(), "Ok");
            dialogMsg.show();
        } else if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            // Display for iqueen, an order confirmation screen
            Util.showLog("Completed");
            postPayment(stripeOrderID, "Stripe");
        }
    }


    //PHone PE

    private void createPhonePePayment() {

        JSONObject data =new    JSONObject();
        try {
            data.put("merchantTransactionId", MERCHANT_TID);//String. Mandatory

        data.put("merchantUserId",System.currentTimeMillis());
        data.put("merchantId" , prefManager.getString(Constant.PHONE_PE_MERCHANT_ID)) ;//String. Mandatory

        data.put("amount", FINAL_PRICE*100 );//Long. Mandatory

        data.put("mobileNumber", prefManager.getString(Constant.USER_PHONE)); //String. Optional

        data.put("callbackUrl", Config.APP_API_URL+"phonepe-callback"); //String. Mandatory

        JSONObject paymentInstrument =new JSONObject();
        paymentInstrument.put("type", "PAY_PAGE");

        data.put("paymentInstrument", paymentInstrument );//OBJECT. Mandatory


        JSONObject deviceContext =new JSONObject();
        deviceContext.put("deviceOS", "ANDROID");
        data.put("deviceContext", deviceContext);

        String payloadBase64 = Base64.encodeToString(data.toString().getBytes(StandardCharsets.UTF_8), android.util.Base64.NO_WRAP);
        String checksum = sha256(payloadBase64 + apiEndPoint + prefManager.getString(Constant.PHONE_PE_SALT_KEY)) + "###1";

        B2BPGRequest b2BPGRequest =new B2BPGRequestBuilder()
                .setData(payloadBase64)
                .setChecksum(checksum)
                .setUrl(apiEndPoint)
                .build();

        try {
            Intent intent =   PhonePe.getImplicitIntent(requireActivity(),b2BPGRequest,"");

            phonePeResultLauncher.launch(intent);
        } catch (PhonePeInitException e) {
            Log.e("EXCEPTION ",e.getMessage());
        }

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

    }
    public static String sha256(String input) {
        try {
            byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(bytes);

            StringBuilder result = new StringBuilder();
            for (byte b : digest) {
                result.append(String.format("%02x", b));
            }

            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace(); // Handle the exception according to your needs
            return null;
        }
    }

    //Paytm
    private void createPaytm() {
        paytmOrderID = Util.randomAlphaNumeric(20);
        customerID = Util.randomAlphaNumeric(10);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            //Background work here
            try {

                // Call the API Service
                Response<PaytmResponse> response = ApiClient.getApiService().createPaytmPayment(
                        prefManager.getString(Constant.api_key),
                        String.valueOf(FINAL_PRICE),
                        paytmOrderID,
                        customerID).execute();


                // Wrap with APIResponse Class
                ApiResponse<PaytmResponse> apiResponse = new ApiResponse<>(response);

                // If response is successful
                if (apiResponse.isSuccessful()) {

                    Util.showLog("" + apiResponse.body + " " + apiResponse.body.toString());

                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            startPaytmPayment(response.body().txnToken, response.body().callback_url);
                        }
                    });

                } else {
                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pbPayment.setVisibility(GONE);
                            btn_skip.setVisibility(VISIBLE);
                            dialogMsg.cancel();
                            dialogMsg.showErrorDialog(apiResponse.errorMessage, "Ok");
                            dialogMsg.show();
                        }
                    });
                }

            } catch (IOException e) {
                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }
            handler.post(() -> {
                //UI Thread work here

            });
        });
    }

    private void startPaytmPayment(String txnToken, String callback_url) {
        PaytmOrder paytmOrder = new PaytmOrder(paytmOrderID, prefManager.getString(Constant.PAYTM_ID),
                txnToken, String.valueOf(FINAL_PRICE), callback_url);

        TransactionManager transactionManager = new TransactionManager(paytmOrder, new PaytmPaymentTransactionCallback() {
            @Override
            public void onTransactionResponse(@Nullable Bundle bundle) {
                Util.showLog("SS: " + bundle.toString());
                if (bundle.getString("STATUS").equals("TXN_FAILURE")) {
                    showError(bundle.getString("RESPMSG"));
                } else if (bundle.getString("STATUS").equals("TXN_SUCCESS")) {
                    postPayment(paytmOrderID, "Paytm");
                }
            }

            @Override
            public void networkNotAvailable() {
                showError("Internet is not available");
            }

            @Override
            public void onErrorProceed(String s) {
                showError(s);
            }

            @Override
            public void clientAuthenticationFailed(String s) {
                showError("Transaction Failed");
            }

            @Override
            public void someUIErrorOccurred(String s) {
                showError("Transaction Error");
            }

            @Override
            public void onErrorLoadingWebPage(int i, String s, String s1) {
                showError("Transaction Error");
            }

            @Override
            public void onBackPressedCancelTransaction() {
                showError("Transaction cancel");
            }

            @Override
            public void onTransactionCancel(String s, Bundle bundle) {
                showError("Transaction cancel");
            }
        });
        transactionManager.setAppInvokeEnabled(false);
//        transactionManager.setShowPaymentUrl("https://securegw-stage.paytm.in/theia/api/v1/showPaymentPage");
        transactionManager.startTransaction(requireActivity(), 50000);
    }

    private void showError(String detail) {
        pbPayment.setVisibility(GONE);
        btn_skip.setVisibility(VISIBLE);
        dialogMsg.cancel();
        dialogMsg.showErrorDialog(detail, "Ok");
        dialogMsg.show();
    }

}