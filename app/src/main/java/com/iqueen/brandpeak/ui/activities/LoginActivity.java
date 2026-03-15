package com.sgdigitalposter.app.ui.activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import com.sgdigitalposter.app.BuildConfig;
import com.sgdigitalposter.app.databinding.ActivityLoginBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.auth.UserInfo;
import com.sgdigitalposter.app.Config;
import com.sgdigitalposter.app.R;
import com.sgdigitalposter.app.api.ApiStatus;
import com.sgdigitalposter.app.api.common.common.Resource;
import com.sgdigitalposter.app.databinding.DialogEnterDetailBinding;
import com.sgdigitalposter.app.databinding.VerifyDialogBinding;
import com.sgdigitalposter.app.items.UserItem;
import com.sgdigitalposter.app.ui.dialog.DialogMsg;
import com.sgdigitalposter.app.utils.Connectivity;
import com.sgdigitalposter.app.utils.Constant;
import com.sgdigitalposter.app.utils.PrefManager;
import com.sgdigitalposter.app.utils.Util;
import com.sgdigitalposter.app.viewmodel.BusinessViewModel;
import com.sgdigitalposter.app.viewmodel.UserViewModel;
import com.onesignal.Continue;
import com.onesignal.OneSignal;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    ActivityLoginBinding binding;
    UserViewModel userViewModel;
    BusinessViewModel businessViewModel;
    boolean loginScene = true;


    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    FirebaseUser user;
    private FirebaseAuth.AuthStateListener mAuthListener;
    DialogMsg dialogMsg;
    String phoneNum;
    String userEmail = "";
    ProgressDialog prgDialog;
    PrefManager prefManager;
    Connectivity connectivity;
    String verificationId;

    String userName = "", usEmail = "";

    boolean isGoogleLogin = false;
    public String gEmail = "", gName = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //One Signal
        OneSignal.getNotifications().requestPermission(false, Continue.none());

        prefManager = new PrefManager(this);
        dialogMsg = new DialogMsg(this, false);
        prgDialog = new ProgressDialog(this);
        connectivity = new Connectivity(this);
        prgDialog.setMessage(getResources().getString(R.string.login_loading));
        prgDialog.setCancelable(false);

        mAuth = FirebaseAuth.getInstance();

        if (BuildConfig.DEBUG){
            mAuth.getFirebaseAuthSettings().setAppVerificationDisabledForTesting(true);
        }


        mAuthListener = firebaseAuth -> {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {}
        };


        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("827209419123-i9v7au9ft3ok168329eft3h5u9rtuej3.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        businessViewModel = new ViewModelProvider(this).get(BusinessViewModel.class);

        setUpScene(loginScene);
        initUi();
        initData();

        createLink(binding.checkboxAgree, "I have read the Privacy Policy and agree to this policy.", "Privacy Policy", clickableSpan);
        createLink(binding.checkboxAgreeSignUp, "I have read the Privacy Policy and agree to this policy.", "Privacy Policy", clickableSpan);

        // Sign Check Box
        binding.checkboxPrivacyPolicySignUp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                binding.checkboxPrivacyPolicy.setChecked(isChecked);
            }
        });

        // Login Check Box
        binding.checkboxPrivacyPolicy.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                binding.checkboxPrivacyPolicySignUp.setChecked(isChecked);
            }
        });
    }

    ClickableSpan clickableSpan = new ClickableSpan() {
        @Override
        public void onClick(View widget) {
            openPrivacyPolicy();

        }

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            // this is where you set link color, underline, typeface etc.
            int linkColor = ContextCompat.getColor(LoginActivity.this, R.color.primary_color);
            ds.setColor(linkColor);
            ds.setUnderlineText(true);
        }
    };
    private void openPrivacyPolicy() {
        String privacyPolicyUrl = prefManager.getString(Constant.PRIVACY_POLICY);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyUrl));
        startActivity(intent);
    }

    public static TextView createLink(TextView targetTextView, String completeString,
                                      String partToClick, ClickableSpan clickableAction) {

        SpannableString spannableString = new SpannableString(completeString);

        // make sure the String is exist, if it doesn't exist
        // it will throw IndexOutOfBoundException
        int startPosition = completeString.indexOf(partToClick);
        int endPosition = completeString.lastIndexOf(partToClick) + partToClick.length();

        spannableString.setSpan(clickableAction, startPosition, endPosition, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

        targetTextView.setText(spannableString);
        targetTextView.setMovementMethod(LinkMovementMethod.getInstance());

        return targetTextView;
    }



    private void initData() {
        userViewModel.getLoadingState().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean loadingState) {

                if (loadingState != null && loadingState) {
                    prgDialog.show();
                } else {
                    prgDialog.cancel();
                }
                updateRegisterBtnStatus();
                updateLoginBtnStatus();

            }
        });

        userViewModel.getUserLoginStatus().observe(this, listResource -> {

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
                                Log.e("GOOD", listResource.data.toString());

                                if (listResource.data.user.is_email_verify) {
                                    prefManager.setBoolean(Constant.IS_LOGIN, true);
                                    prefManager.setString(Constant.USER_EMAIL, binding.etEmail.getText().toString().trim());
                                    prefManager.setString(Constant.USER_PASSWORD, binding.etPassword.getText().toString().trim());
                                    prefManager.setString(Constant.USER_IMAGE, listResource.data.user.userImage);
                                    prefManager.setString(Constant.USER_NAME, listResource.data.user.userName);
                                    prefManager.setString(Constant.USER_PHONE, listResource.data.user.phone);
                                    prefManager.setString(Constant.USER_ID, listResource.data.user_id);
                                    prefManager.setString(Constant.LOGIN_TYPE, Constant.NORMAL);
                                    Constant.IS_SUBSCRIBED = listResource.data.user.isSubscribed;
                                    prefManager.setString(Constant.REFER_CODE_BY, listResource.data.user.referralCode);
                                    loadBusiness();
                                } else {
                                    prgDialog.cancel();
                                    showVerifyDialog(listResource.data.user, true);
                                }

                            } catch (NullPointerException ne) {
                                Util.showErrorLog("Null Pointer Exception.", ne);
                            } catch (Exception e) {
                                Util.showErrorLog("Error in getting notification flag data.", e);
                            }

                        }

                        break;
                    case ERROR:
                        // Error State
                        prefManager.setBoolean(Constant.IS_LOGIN, false);
                        dialogMsg.showErrorDialog(listResource.message, getString(R.string.ok));
                        dialogMsg.show();

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
                prefManager.setBoolean(Constant.IS_LOGIN, false);

            }

        });


        userViewModel.getRegisterUser().observe(this, listResource -> {
            if (listResource != null) {

                Util.showLog("Got Data "
                        + listResource.message +
                        listResource.toString());

                switch (listResource.status) {
                    case LOADING:
                        // Loading State
                        // Data are from Local DB

                        prgDialog.show();

                        break;
                    case SUCCESS:
                        // Success State
                        // Data are from Server

                        if (listResource.data != null) {
                            userViewModel.setLoadingState(false);
                            prgDialog.cancel();
                            showVerifyDialog(listResource.data, false);
                        }

                        break;
                    case ERROR:
                        // Error State

                        Util.showLog("Error: " + listResource.message);

                        dialogMsg.showErrorDialog(listResource.message, getString(R.string.ok));
                        dialogMsg.show();
                        binding.btnSingUp.setText(getResources().getString(R.string.login_sign_up));

                        userViewModel.setLoadingState(false);
                        prgDialog.cancel();

                        break;
                    default:
                        // Default
                        userViewModel.isLoading = false;
                        prgDialog.cancel();
                        break;
                }

            } else {

                // Init Object or Empty Data
                Util.showLog("Empty Data");

            }
        });

        userViewModel.getGoogleLoginData().observe(this, listResource -> {

            if (listResource != null) {

                Util.showLog("Got Data " + listResource.message + listResource.toString());

                switch (listResource.status) {
                    case LOADING:
                        // Loading State
                        // Data are from Local DB

                        prgDialog.show();

                        break;
                    case SUCCESS:
                        // Success State
                        // Data are from Server
//                        Log.e("SB", "initData:" + listResource.data);

                        if (listResource.data != null) {

                            try {

                                Log.e("TAG", "initData listResource.data.user.userName := " + listResource.data );
                                if (listResource.data.user.userName.equals("Brand_User_Google")) {
                                    prgDialog.dismiss();
                                    prefManager.setString(Constant.USER_ID, listResource.data.user.userId);
                                    prefManager.setString(Constant.USER_EMAIL, listResource.data.user.email);
                                    prefManager.setString(Constant.USER_NAME, listResource.data.user.userName);
                                    prefManager.setString(Constant.USER_IMAGE, listResource.data.user.userImage);
                                    prefManager.setString(Constant.USER_PHONE, listResource.data.user.phone);
                                    prefManager.setString(Constant.LOGIN_TYPE, Constant.GOOGLE);
                                    isGoogleLogin = true;
                                    prefManager.setString(Constant.REFER_CODE_BY, listResource.data.user.referralCode);
                                    showEnterDetailDialog();
                                } else {
                                    Constant.IS_SUBSCRIBED = listResource.data.user.isSubscribed;
                                    prefManager.setBoolean(Constant.IS_LOGIN, true);
                                    prefManager.setString(Constant.USER_EMAIL, listResource.data.user.email);
                                    prefManager.setString(Constant.USER_NAME, listResource.data.user.userName);
                                    prefManager.setString(Constant.USER_IMAGE, listResource.data.user.userImage);
                                    prefManager.setString(Constant.USER_ID, listResource.data.user_id);
                                    prefManager.setString(Constant.LOGIN_TYPE, Constant.GOOGLE);
                                    prefManager.setString(Constant.REFER_CODE_BY, listResource.data.user.referralCode);
                                    loadBusiness();
                                }

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

                        userViewModel.isLoading = false;
                        prgDialog.cancel();

                        dialogMsg.showErrorDialog(listResource.message, getString(R.string.ok));
                        dialogMsg.show();

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

        userViewModel.getPhoneLoginData().observe(this, listResource -> {

            if (listResource != null) {

                Util.showLog("Got Data " + listResource.message + listResource.toString());

                switch (listResource.status) {
                    case LOADING:
                        // Loading State
                        // Data are from Local DB

                        prgDialog.show();

                        break;
                    case SUCCESS:
                        // Success State
                        // Data are from Server

                        if (listResource.data != null) {

                            try {
                                Log.e("GOOD", listResource.data.toString());

                                if (listResource.data.user.userName.equals("Brand_User")) {
                                    prgDialog.dismiss();
                                    prefManager.setString(Constant.USER_ID, listResource.data.user.userId);
                                    prefManager.setString(Constant.USER_PHONE, listResource.data.user.phone);
                                    prefManager.setString(Constant.REFER_CODE_BY, listResource.data.user.referralCode);
                                    showEnterDetailDialog();
                                } else {
                                    Constant.IS_SUBSCRIBED = listResource.data.user.isSubscribed;
                                    prefManager.setBoolean(Constant.IS_LOGIN, true);
                                    prefManager.setString(Constant.USER_EMAIL, listResource.data.user.email);
                                    prefManager.setString(Constant.USER_NAME, listResource.data.user.userName);
                                    prefManager.setString(Constant.USER_IMAGE, listResource.data.user.userImage);
                                    prefManager.setString(Constant.USER_PHONE, listResource.data.user.phone);
                                    prefManager.setString(Constant.USER_ID, listResource.data.user_id);
                                    prefManager.setString(Constant.LOGIN_TYPE, Constant.MOBILE);
                                    prefManager.setString(Constant.REFER_CODE_BY, listResource.data.user.referralCode);
                                    loadBusiness();
                                }

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

                        userViewModel.isLoading = false;
                        prgDialog.cancel();

                        dialogMsg.showErrorDialog(listResource.message, getString(R.string.ok));
                        dialogMsg.show();

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

        //For resent code
        userViewModel.getResentVerifyCodeData().observe(this, result -> {

            if (result != null) {
                switch (result.status) {
                    case SUCCESS:
                        //add offer text
                        Util.showToast(this, "Successfully Send Code");
                        break;

                    case ERROR:
                        Util.showToast(this, "Fail resent code again");
                        break;
                }
            }
        });

    }

    private void showEnterDetailDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        DialogEnterDetailBinding binding = DialogEnterDetailBinding.inflate(getLayoutInflater());
        dialog.setContentView(binding.getRoot());
        if (dialog.getWindow() != null) {
            dialog.getWindow().setAttributes(getLayoutParams(dialog));

            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.setCancelable(false);

        if (isGoogleLogin) {
            binding.etEmail.setText(gEmail);
            binding.etEmail.setEnabled(false);
            binding.etName.setText(gName);
        }
        if (!prefManager.getBoolean(Constant.REFER_SYSTEM_ENABLE)) {
            binding.etReferral.setVisibility(View.VISIBLE);
            binding.textView11.setVisibility(View.GONE);
        }

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
            String referralCode = "";
            if (prefManager.getBoolean(Constant.REFER_SYSTEM_ENABLE)) {
                referralCode = binding.etReferral.getText().toString();
            }
            userName = binding.etName.getText().toString().trim();
            usEmail = binding.etEmail.getText().toString().trim();

            prgDialog.show();
            userViewModel.uploadImage(this, null, null, prefManager.getString(Constant.USER_ID),
                    userName,
                    usEmail,
                    prefManager.getString(Constant.USER_PHONE),
                    getContentResolver(), referralCode,"").observe(this, listResource -> {
                if (listResource != null && listResource.data != null) {
                    Util.showLog("Got Data" + listResource.message + listResource.toString());
                    prefManager.setString(Constant.REFER_CODE_BY, listResource.data.referralCode);
                    prefManager.setString(Constant.USER_IMAGE, listResource.data.userImage);
                    prefManager.setString(Constant.USER_NAME, listResource.data.userName);
                    prefManager.setString(Constant.USER_PHONE, listResource.data.phone);
                    prgDialog.cancel();
                    dialog.dismiss();
                    dialogMsg.showSuccessDialog(getString(R.string.success_register), getString(R.string.ok));
                    dialogMsg.show();
                    dialogMsg.okBtn.setOnClickListener(view -> {

                        dialogMsg.cancel();
                        prefManager.setBoolean(Constant.IS_LOGIN, true);
                        gotoMainActivity();

                    });

                } else if (listResource != null && listResource.message != null) {
                    Util.showLog("Message from server.");

                    dialogMsg.showErrorDialog(listResource.message, getString(R.string.ok));
                    dialogMsg.show();
                    prgDialog.cancel();
                } else {

                    Util.showLog("Empty Data");

                }
            });

        });
        dialog.show();
    }

    private void loadBusiness() {

        prgDialog.show();

        businessViewModel.setBusinessObj(prefManager.getString(Constant.USER_ID));

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
                        prgDialog.dismiss();
                        gotoMainActivity();
                        break;
                    case ERROR:
                        // Error State
                        gotoMainActivity();
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

    private void showVerifyDialog(UserItem userItem, boolean isLogin) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        VerifyDialogBinding binding = VerifyDialogBinding.inflate(getLayoutInflater());
        dialog.setContentView(binding.getRoot());
        if (dialog.getWindow() != null) {
            dialog.getWindow().setAttributes(getLayoutParams(dialog));
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.setContentView(binding.getRoot());
        binding.emailTextView.setText(userItem.email);

        binding.submitButton.setOnClickListener(v -> {
            if (!binding.otpEditTextLoginActivity.getText().toString().equals("") && binding.otpEditTextLoginActivity.getText().toString().length() == 6) {
                binding.submitButton.setEnabled(false);
                binding.submitButton.setText(getResources().getString(R.string.login_loading));
                userViewModel.setEmailVerificationUser(userItem.userId, binding.otpEditTextLoginActivity.getText().toString());
            } else {
                Util.showToast(this, getString(R.string.verify_email__enter_code));
            }
        });

        if (isLogin) {
            userViewModel.setResentVerifyCodeObj(userItem.userId);
        }

        binding.resentCodeButton.setOnClickListener(v -> userViewModel.setResentVerifyCodeObj(userItem.userId));

        binding.changeEmailButton.setOnClickListener(v -> {
            userViewModel.setLoadingState(false);
            dialog.dismiss();
        });

        dialog.show();

        LiveData<Resource<ApiStatus>> itemList = userViewModel.getEmailVerificationUser();

        if (itemList != null) {

            itemList.observe(this, listResource -> {
                if (listResource != null) {

                    binding.submitButton.setEnabled(true);
                    binding.submitButton.setText(getResources().getString(R.string.verify_email__submit));
                    switch (listResource.status) {
                        case LOADING:

                            break;

                        case SUCCESS:

                            if (listResource.data != null) {

                                try {
                                    if (isLogin) {

                                        dialogMsg.showSuccessDialog(getString(R.string.success_verify), getString(R.string.ok));
                                        dialogMsg.show();
                                        dialog.dismiss();
                                        dialogMsg.okBtn.setOnClickListener(v -> {
                                            prefManager.setBoolean(Constant.IS_LOGIN, true);
                                            prefManager.setString(Constant.USER_EMAIL, userItem.email);
                                            prefManager.setString(Constant.USER_PASSWORD, this.binding.etPassword.getText().toString().trim());
                                            prefManager.setString(Constant.USER_ID, userItem.userId);
                                            prefManager.setString(Constant.LOGIN_TYPE, Constant.NORMAL);
                                            gotoMainActivity();
                                        });

                                    } else {
                                        dialogMsg.showSuccessDialog(getString(R.string.success_register), getString(R.string.ok));
                                        dialogMsg.show();
                                        dialog.dismiss();
                                        dialogMsg.okBtn.setOnClickListener(v -> {
                                            Constant.IS_SUBSCRIBED = userItem.isSubscribed;
                                            prefManager.setBoolean(Constant.IS_LOGIN, true);
                                            prefManager.setString(Constant.USER_EMAIL, this.binding.etEmailSi.getText().toString().trim());
                                            prefManager.setString(Constant.USER_PASSWORD, this.binding.etPasswordSi.getText().toString().trim());
                                            prefManager.setString(Constant.USER_ID, userItem.userId);
                                            prefManager.setString(Constant.LOGIN_TYPE, Constant.NORMAL);

                                            dialogMsg.cancel();

                                            loadBusiness();
                                            userEmail = "";
                                            setEmptyText();
                                            setUpScene(true);
                                        });
                                    }

                                } catch (NullPointerException ne) {
                                    Util.showErrorLog("Null Pointer Exception.", ne);
                                } catch (Exception e) {
                                    Util.showErrorLog("Error in getting notification flag data.", e);
                                }

                            }

                            break;

                        case ERROR:
                            // Error State
                            dialogMsg.showErrorDialog(listResource.message, getString(R.string.ok));
                            dialogMsg.show();
//                            dialog.dismiss();

                            break;
                        default:
                            // Default

                            break;
                    }

                }

            });
        }
    }

    private void gotoMainActivity() {
        userViewModel.setLoadingState(false);
        prgDialog.cancel();
        Constant.FOR_ADD_BUSINESS = false;
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }

    private void initUi() {
        binding.txtTabLogin.setOnClickListener(v -> {
            if (!loginScene) {
                setUpScene(true);
            }
        });

        binding.txtTabSignUp.setOnClickListener(v -> {
            if (loginScene) {
                setUpScene(false);
            }
        });

        binding.txtSingBottom.setOnClickListener(v -> {
            if (loginScene) {
                setUpScene(false);
            }
        });

        binding.txtLoginBottom.setOnClickListener(v -> {
            if (!loginScene) {
                setUpScene(true);
            }
        });

        binding.btnLogin.setOnClickListener(v -> {
            if (validate()) {
                if (!connectivity.isConnected()) {
                    Util.showToast(this, getString(R.string.error_message__no_internet));
                    return;
                }
                if (binding.checkboxPrivacyPolicy.isChecked()) {
                    if (!userViewModel.isLoading) {
                        prgDialog.show();

                        userEmail = binding.etEmail.getText().toString().trim();

                        userViewModel.isLoading = false;
                        updateLoginBtnStatus();
                        Util.showLog("Sign in with email and password");
//                    signInWithEmailAndPassword(userEmail, userEmail + "sdg");

                        signInWithEmailAndPassword(userEmail, binding.etPassword.getText().toString());
                    }
                } else {
                    Util.showToast(this , getResources().getString(R.string.accept_privacy_policy));
                }
            }
        });

        binding.relativeLayoutGoogleLogin.setOnClickListener(v -> {
            if (!connectivity.isConnected()) {
                Util.showToast(this, getString(R.string.error_message__no_internet));
                return;
            }

            signIn();


        });

        binding.relativeLayoutPhoneLogin.setOnClickListener(v -> {
            if (!connectivity.isConnected()) {
                Util.showToast(this, getString(R.string.error_message__no_internet));
                return;
            }

            phoneLogin();


        });

        binding.btnSingUp.setOnClickListener(v -> {
            if (registerValid()) {
                if (!connectivity.isConnected()) {
                    Util.showToast(this, getString(R.string.error_message__no_internet));
                    return;
                }
                userEmail = binding.etEmailSi.getText().toString().trim();
                userViewModel.isLoading = true;
                prgDialog.show();
                updateRegisterBtnStatus();

                // userEmail and userEmail is correct
                // This is needed for firebase login
                // no need to change to password
                createRegisterUserWithEmailAndPassword(userEmail, binding.etPasswordSi.getText().toString());
            }
        });
        binding.btnSkip.setOnClickListener(v -> {
            Constant.FOR_ADD_BUSINESS = false;
            startActivity(new Intent(this, MainActivity.class));
        });

        binding.txtForgotPass.setOnClickListener(v -> {
            startActivity(new Intent(this, ForgotPassActivity.class));
        });

        if (prefManager.getBoolean(Constant.WHATSAPP_AUTH_ENABLE)) {
            binding.txtLbPhone.setText("Login With Whatsapp Number");
        }
    }

    private void phoneLogin() {
        dialogMsg.showEnterNumberDialog();
        dialogMsg.confirm_phone_number.setOnClickListener(v -> {
            if (dialogMsg.etPhone.getText().toString().equals("")) {
                Util.showToast(LoginActivity.this, "Please Enter Phone Number");
                return;
            }


            phoneNum = "+" + dialogMsg.countryCodePicker.getSelectedCountryCode().toString() + dialogMsg.etPhone.getText().toString();
            new AlertDialog.Builder(this)
                    .setTitle("We will be verifying the phone number:")
                    .setMessage(" \n" + phoneNum + " \n\n Is this OK,or would you like to edit the number ?")
                    .setPositiveButton("Confirm",
                            (dialog, which) -> {
                                dialogMsg.dismiss();
                                //Do Something Here
                                dialog.dismiss();
                                if (!prefManager.getBoolean(Constant.WHATSAPP_AUTH_ENABLE)) {
                                    whatsappLogin(phoneNum);
                                } else {
                                    loginWithPhone();
                                }
                            })
                    .setNegativeButton("Edit",
                            (dialog, which) -> {
                                dialogMsg.dismiss();
                                dialog.dismiss();
                            }).show();
        });
        dialogMsg.show();
    }

    private void whatsappLogin(String phoneNumber) {

        new AlertDialog.Builder(this)
                .setTitle("Alert")
                .setMessage("This functionality is not active in Demo app,")
                .setPositiveButton("OK",
                        (dialog, which) -> {
                            //Do Something Here
                            dialog.dismiss();

                        }).show();

        return;

        //TODO
        // ONCE YOU HAVE WHATSAPI KEY ENABLED, REMOVE ABOVE DIALOG AND UNCOMMENT BELOW CODE
        // AS WHATSAPP API IS NEED ONCE YOU SUBSCRIBE OR PURCHASE IT WILL CONTINUE TO WORK


       /* prgDialog.show();
        userViewModel.whatsappLogin(phoneNumber).observe(this, listResource->{
            if (listResource != null) {

                Util.showLog("Got Data " + listResource.message + listResource.toString());

                switch (listResource.status) {
                    case LOADING:
                        // Loading State
                        // Data are from Local DB

                        prgDialog.show();

                        break;
                    case SUCCESS:
                        // Success State
                        // Data are from Server

                        if (listResource.data != null) {

                            verificationId = listResource.data.otp;
                            prgDialog.cancel();
                            showOTPVerificationDialog();
                        }

                        break;
                    case ERROR:
                        // Error State

                        prgDialog.cancel();

                        dialogMsg.showErrorDialog(listResource.message, getString(R.string.ok));
                        dialogMsg.show();

                        break;
                    default:
                        // Default

                        break;
                }

            } else {

                // Init Object or Empty Data
                Util.showLog("Empty Data");

            }
        });*/
    }

    private void loginWithPhone() {
        prgDialog.show();

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNum,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallBack);
        /* PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNum)            // Phone number to verify
                        .setTimeout(30L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallBack)           // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options); */
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks

            // initializing our callbacks for on
            // verification callback method.
            mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        // below method is used when
        // OTP is sent from Firebase
        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            // when we receive the OTP it
            // contains a unique id which
            // we are storing in our string
            // which we have already created.
            verificationId = s;
            prgDialog.cancel();
            showOTPVerificationDialog();
        }

        // this method is called when user
        // receive OTP from Firebase.
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            // below line is used for getting OTP code
            // which is sent in phone auth credentials.
            final String code = phoneAuthCredential.getSmsCode();
            // checking if the code
            // is null or not.
            Util.showLog("OTP: " + phoneAuthCredential.getSmsCode() + phoneAuthCredential.getSignInMethod() + phoneAuthCredential.getSignInMethod());
            if (code != null) {

            }
        }

        // this method is called when firebase doesn't
        // sends our OTP code due to any error or issue.
        @Override
        public void onVerificationFailed(FirebaseException e) {
            // displaying error message with firebase exception.
            prgDialog.cancel();
            dialogMsg.cancel();
            dialogMsg.showErrorDialog(e.getMessage(), getString(R.string.ok));
            dialogMsg.show();
        }
    };

    private void showOTPVerificationDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        VerifyDialogBinding binding = VerifyDialogBinding.inflate(getLayoutInflater());
        dialog.setContentView(binding.getRoot());
        if (dialog.getWindow() != null) {
            dialog.getWindow().setAttributes(getLayoutParams(dialog));
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.setCancelable(false);
        binding.emailTextView.setText(phoneNum);

        binding.submitButton.setOnClickListener(v -> {

            if (!binding.otpEditTextLoginActivity.getText().toString().equals("") && binding.otpEditTextLoginActivity.getText().toString().trim().length() == 6) {
                dialogMsg.cancel();
                prgDialog.show();
                String OTP = binding.otpEditTextLoginActivity.getText().toString().trim();
                if (prefManager.getBoolean(Constant.WHATSAPP_AUTH_ENABLE)) {
                    if (OTP.equals(verificationId)) {
                        userViewModel.setRegisterPhoneUser("Brand_User",
                                "BrandUser@gmail.com", phoneNum);
                    } else {
                        prgDialog.dismiss();
                        Util.showToast(LoginActivity.this, getString(R.string.invalid_enter_code));
                    }
                } else {
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, OTP);
                    mAuth.signInWithCredential(credential)
                            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Util.showLog("signInWithCredential:success");

                                        FirebaseUser user = task.getResult().getUser();
                                        // Update UI
                                        binding.submitButton.setEnabled(false);
                                        binding.submitButton.setText(getResources().getString(R.string.login_loading));
                                        dialog.dismiss();
                                        userViewModel.setRegisterPhoneUser("Brand_User", "BrandUser@gmail.com", phoneNum);
                                    } else {
                                        // Sign in failed, display a message and update the UI
                                        Util.showLog("signInWithCredential:failure " + task.getException());
                                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                            // The verification code entered was invalid
                                        }
                                        prgDialog.dismiss();
                                        Util.showToast(LoginActivity.this, getString(R.string.invalid_enter_code));
                                    }
                                }
                            });
                }
            } else {
                Util.showToast(this, getString(R.string.verify_email__enter_code));
            }
        });
        binding.changeEmailButton.setOnClickListener(v -> {
            dialogMsg.show();
            dialog.dismiss();
        });

        new CountDownTimer(60000, 1000) {

            public void onTick(long millisUntilFinished) {
                binding.resentCodeButton.setText("00 : " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                binding.resentCodeButton.setText("Resend Code");
            }
        }.start();

        binding.resentCodeButton.setOnClickListener(v -> {

            if (binding.resentCodeButton.getText().toString().equals("Resend Code")) {
                dialog.dismiss();
                dialogMsg.cancel();
                loginWithPhone();
            }
        });
        dialog.show();
    }


    private WindowManager.LayoutParams getLayoutParams(@NonNull Dialog dialog) {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        dialog.getWindow().setGravity(Gravity.getAbsoluteGravity(Gravity.CENTER,Gravity.FILL_VERTICAL));
        if (dialog.getWindow() != null) {
            layoutParams.copyFrom(dialog.getWindow().getAttributes());
        }
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        return layoutParams;
    }

    private boolean     registerValid() {
        if (binding.etNameSi.getText().toString().trim().isEmpty()) {
//            binding.etEmailSi.setError(getResources().getString(R.string.hint_business_name));
            binding.etNameSi.setError(getResources().getString(R.string.enter_name));
            binding.etNameSi.requestFocus();
            return false;
        }  else if (!isEmailValid(binding.etEmailSi.getText().toString())) {
            binding.etEmailSi.setError(getString(R.string.invalid_email));
            binding.etEmailSi.requestFocus();
            return false;
        }
        else if (binding.etPasswordSi.getText().toString().trim().isEmpty()) {
//            binding.etPasswordSi.setError(getResources().getString(R.string.hint_business_email));
            binding.etPasswordSi.setError(getResources().getString(R.string.enter_password));
            binding.etPasswordSi.requestFocus();
            return false;
        }else if (binding.etPasswordSi.getText().toString().endsWith(" ")) {
//            binding.etPasswordSi.setError(getResources().getString(R.string.space_not_allowed));
            binding.etPasswordSi.setError(getResources().getString(R.string.space_not_allowed));
            binding.etPasswordSi.requestFocus();
            return false;
        } else if (binding.etConfPasswordSi.getText().toString().isEmpty()) {
            binding.etConfPasswordSi.setError(getResources().getString(R.string.enter_confirm_password));
            binding.etConfPasswordSi.requestFocus();
            return false;
        } else if (!binding.etPasswordSi.getText().toString().equals(binding.etConfPasswordSi.getText().toString())) {
            binding.etConfPasswordSi.setError(getResources().getString(R.string.pass_not_match));
            binding.etConfPasswordSi.requestFocus();
            return false;
        } else if (binding.etNumberSi.getText().toString().trim().isEmpty() && binding.etNumberSi.getText().toString().length() > 13) {
            binding.etNumberSi.setError(getResources().getString(R.string.hint_phone_number));
            binding.etNumberSi.requestFocus();
            return false;
        }else if(!binding.checkboxPrivacyPolicySignUp.isChecked()){
            Util.showToast(LoginActivity.this , getResources().getString(R.string.accept_privacy_policy));
            return false;
        }
        return true;
    }

    private FirebaseUser createRegisterUserWithEmailAndPassword(String email, String password) {
        try {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Util.showLog("createUserWithEmail:success");
                                user = mAuth.getCurrentUser();
                                registerUser();

                            } else {
                                registerWithEmailAndPassword(email, password);
                            }
                        }
                    });
        } catch (Exception exception) {
            Util.showLog("***** Error Exception: " + exception);

            dialogMsg.showErrorDialog(getString(R.string.login__exception_error), getString(R.string.ok));
            dialogMsg.show();

        }
        return user;
    }

    private FirebaseUser registerWithEmailAndPassword(String email, String password) {
        try {
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Util.showLog("signInWithEmail:success");
                        user = mAuth.getCurrentUser();
                        registerUser();
                    } else {
                        Util.showLog("Fail");

                        if (!email.equals(Constant.DEFAULTEMAIL)) {

                            createRegisterUserWithEmailAndPassword(Constant.DEFAULTEMAIL, Constant.DEFAULTPASSWORD);
                        } else {
                            // Error
                            handleRegisterFirebaseAuthError(binding.etEmailSi.getText().toString().trim());
                        }

                    }

                }
            });
        } catch (Exception e) {
            Util.showLog("signInWithEmail:failure");
            dialogMsg.showErrorDialog(getString(R.string.login__failure_error), getString(R.string.ok));
            dialogMsg.show();

        }
//        FirebaseUser firebaseUser = user;
        return user;
    }

    private void registerUser() {
        userViewModel.isLoading = true;
        updateRegisterBtnStatus();
        String selectedCountry = binding.CountryCodePicker.getSelectedCountryCode().toString();
        String country = selectedCountry.isEmpty() ? binding.CountryCodePicker.getDefaultCountryCode().toString() : selectedCountry;
        Log.e("TAG", "registerUser: " + country);
        userViewModel.setRegisterUserData(new UserItem("",
                binding.etNameSi.getText().toString().trim(),
                "",
                binding.etEmailSi.getText().toString().trim(),
                binding.etPasswordSi.getText().toString().trim(),
                binding.etNumberSi.getText().toString().trim(),
                "",
                true,
                "",
                "",
                "",
                "",
                false,
                1, "",country
                ));
    }

    private void setEmptyText() {
        binding.etNameSi.setText(Config.EMPTY_STRING);
        binding.etPasswordSi.setText(Config.EMPTY_STRING);
        binding.etConfPasswordSi.setText(Config.EMPTY_STRING);
        binding.etNumberSi.setText(Config.EMPTY_STRING);
        binding.etEmailSi.setText(Config.EMPTY_STRING);

        binding.etEmail.setText(Config.EMPTY_STRING);
        binding.etPassword.setText(Config.EMPTY_STRING);
    }

    private void handleRegisterFirebaseAuthError(String email) {
        mAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
            @Override
            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                if (task.isSuccessful()) {
                    SignInMethodQueryResult result = task.getResult();
                    List<String> signInMethod = result.getSignInMethods();

                    Util.showLog("SignInMethod  =" + signInMethod);
                    if (signInMethod.contains(Constant.EMAILAUTH)) {
                        dialogMsg.showErrorDialog("[" + email + "]" + getString(R.string.login__auth_email), getString(R.string.ok));
                        dialogMsg.show();
                    } else if (signInMethod.contains(Constant.GOOGLEAUTH)) {
                        dialogMsg.showErrorDialog("[" + email + "]" + getString(R.string.login__auth_google), getString(R.string.ok));
                        dialogMsg.show();
                    }
                }
            }
        });
    }

    private void updateRegisterBtnStatus() {
        if (userViewModel.isLoading) {
            binding.btnSingUp.setEnabled(false);
            binding.btnSingUp.setText(getResources().getString(R.string.login_loading));
        } else {
            binding.btnSingUp.setEnabled(true);
            binding.btnSingUp.setText(getResources().getString(R.string.login_sign_up));
        }
    }

    private FirebaseUser signInWithEmailAndPassword(String email, String password) {

        Util.showLog(email + " " + password);
        try {
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        try {
                            //Success
                            Util.showLog("signInWithEmail:success" + email);
                            user = mAuth.getCurrentUser();
                            Util.showLog("doSubmit Sign");
                            doSubmit(userEmail, binding.etPassword.getText().toString());
                        } catch (Exception e) {
                            Util.showLog("" + e);
                        }
                    } else {
                        // Fail
                        Log.d("test", "onComplete: "+task.getException());
                        Util.showLog("Fail");
                        if (!email.equals(Constant.DEFAULTEMAIL)) {
                            createUserWithEmailAndPassword(email, email);
                        } else {
                            userViewModel.isLoading = false;
                            updateLoginBtnStatus();
                            // Error Handling
                            Util.showLog("handleFirebaseAuthError");
                            handleFirebaseAuthError(binding.etEmail.getText().toString().trim());
                        }

                    }

                }

            });
        } catch (Exception e) {
            Util.showLog("signInWithEmail:failure");
            dialogMsg.showErrorDialog(getString(R.string.login__failure_error), getString(R.string.ok));
            dialogMsg.show();
        }

        return user;
    }

    public FirebaseUser createUserWithEmailAndPassword(String email, String password) {
        try {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Util.showLog("createUserWithEmail:success");
                                user = mAuth.getCurrentUser();
                                Util.showLog("doSubmit Create");
                                doSubmit(email, binding.etPassword.getText().toString());
                            } else {
                                //fail
                                if (!email.equals(Constant.DEFAULTEMAIL)) {
                                    createUserWithEmailAndPassword(Constant.DEFAULTEMAIL, Constant.DEFAULTPASSWORD);
//                                    createUserWithEmailAndPassword(email, password);

                                } else {
                                    //fail
                                    signInWithEmailAndPassword(Constant.DEFAULTEMAIL, Constant.DEFAULTPASSWORD);
                                }
                            }
                        }

                    });
        } catch (Exception exception) {

            //If sign in fails, display a message to the user.
            Util.showLog("createUserWithEmail:failure");

            dialogMsg.showErrorDialog(getString(R.string.login__exception_error), getString(R.string.ok));
            dialogMsg.show();

        }

        return user;
    }

    private void doSubmit(String email, String password) {
        userViewModel.setUserLogin(new UserItem(
                "",
                "",
                "",
                email,
                password,
                "",
                "",
                false,
                "",
                "",
                "",
                "",
                true,
                1, "",""
        ));
        userViewModel.isLoading = true;
    }

    private boolean validate() {
        if (binding.etEmail.getText().toString().trim().isEmpty()) {
//            binding.etEmail.setError(getResources().getString(R.string.hint_business_name));
            binding.etEmail.setError(getResources().getString(R.string.enter_email));
            binding.etEmail.requestFocus();
            return false;
        } else if (binding.etPassword.getText().toString().trim().isEmpty()) {
            binding.etPassword.setError(getResources().getString(R.string.enter_password));
            binding.etPassword.requestFocus();
            return false;
        } else if (!isEmailValid(binding.etEmail.getText().toString())) {
            binding.etEmail.setError(getString(R.string.invalid_email));
            binding.etEmail.requestFocus();
            return false;
        }
        return true;
    }

    private boolean isEmailValid(String email) {
        return email.contains("@") && !email.contains(" ");
    }

    private void updateLoginBtnStatus() {
        if (userViewModel.isLoading) {
            binding.btnLogin.setEnabled(false);
            binding.btnLogin.setText(getResources().getString(R.string.login_loading));
        } else {
            binding.btnLogin.setEnabled(true);
            binding.btnLogin.setText(getResources().getString(R.string.login_login));
        }
    }

    private void setUpScene(boolean loginScene) {
        this.loginScene = loginScene;
        if (loginScene) {
            binding.txtTabLogin.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.active_color)));
            binding.viewTabLogin.setBackgroundColor(getResources().getColor(R.color.active_color));
            binding.txtTabSignUp.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.blue_grey_200)));
            binding.viewTabSingUp.setBackgroundColor(getResources().getColor(R.color.transparent_color));
            binding.llSingUp.setVisibility(View.GONE);
            binding.llLogin.setVisibility(View.VISIBLE);
            clearSignUp();
        } else {
            binding.txtTabLogin.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.blue_grey_200)));
            binding.viewTabLogin.setBackgroundColor(getResources().getColor(R.color.transparent_color));
            binding.txtTabSignUp.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.active_color)));
            binding.viewTabSingUp.setBackgroundColor(getResources().getColor(R.color.active_color));
            binding.llSingUp.setVisibility(View.VISIBLE);
            binding.llLogin.setVisibility(View.GONE);
            clearLogin();
        }

    }

    private void clearLogin() {
        binding.etEmail.setText(Config.EMPTY_STRING);
        binding.etPassword.setText(Config.EMPTY_STRING);
        binding.checkboxPrivacyPolicy.setChecked(false);
        binding.etEmail.setError(null);
        binding.etPassword.setError(null);
        binding.etEmail.setError(null);
        binding.etPassword.setError(null);
    }

    private void clearSignUp() {
        binding.etNameSi.setText(Config.EMPTY_STRING);
        binding.etEmailSi.setText(Config.EMPTY_STRING);
        binding.etPasswordSi.setText(Config.EMPTY_STRING);
        binding.etConfPasswordSi.setText(Config.EMPTY_STRING);
        binding.etNumberSi.setText(Config.EMPTY_STRING);
        binding.checkboxPrivacyPolicySignUp.setChecked(false);
        binding.etNameSi.setError(null);
        binding.etEmailSi.setError(null);
        binding.etPasswordSi.setError(null);
        binding.etConfPasswordSi.setError(null);
        binding.etNumberSi.setError(null);
        binding.etNameSi.clearFocus();
        binding.etEmailSi.clearFocus();
        binding.etPasswordSi.clearFocus();
        binding.etConfPasswordSi.clearFocus();
        binding.etNumberSi.clearFocus();

    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        someActivityResultLauncher.launch(signInIntent);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mAuth != null) {
            mAuth.addAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.addAuthStateListener(mAuthListener);
        }
    }

    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // Here, no request code

                        if (result.getData() != null) {
                            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());

                            try {

                                // Google Sign In was successful, authenticate with Firebase
                                GoogleSignInAccount account = task.getResult(ApiException.class);
                                if (account != null) {
                                    prgDialog.show();
                                    firebaseAuthWithGoogle(account);
                                    Util.showLog("Google sign in success ");
                                }
                            } catch (ApiException e) {
                                // Google Sign In failed, update UI appropriately
                                Util.showLog("Google sign in failed: " + e);
                                // ...
                            }
                        }
                    }else if(result.getResultCode() == RESULT_CANCELED){
                        if(result.getData() != null){
                            Log.e("GOOGLE LOGIN ", "onActivityResult: "   + result.getData());
                            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                            Log.e("GOOGLE LOGIN ", "onActivityResult: "   + task.getException() );
                        }
                        Log.e("GOOGLE LOGIN ", "onActivityResult: RESULT_CANCELED");
                    }
                    else if(result.getResultCode() == RESULT_FIRST_USER){
                        Log.e("GOOGLE LOGIN ", "onActivityResult: RESULT_FIRST_USER");
                    } else {
                        Log.e("GOOGLE LOGIN ", "onActivityResult: Google sign in failed:");
                        Util.showLog("Google sign in failed: ");
                    }
                }
            });

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Util.showLog("firebaseAuthWithGoogle: " + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        user = mAuth.getCurrentUser();
                        if (user != null) {
                            List<? extends UserInfo> userInfoList = user.getProviderData();

                            String email = "";
                            String uid = "";
                            String displayName = "";
                            String photoUrl = "";
                            for (int i = 0; i < userInfoList.size(); i++) {

                                email = userInfoList.get(i).getEmail();

                                if (email != null && !email.equals("")) {
                                    uid = userInfoList.get(i).getUid();
                                    displayName = userInfoList.get(i).getDisplayName();
                                    photoUrl = String.valueOf(userInfoList.get(i).getPhotoUrl());

                                    gEmail = email;
                                    gName = displayName;

                                    break;
                                }
                            }

                            userViewModel.setRegisterGoogleUser(displayName, email, photoUrl);

                        } else {
                            // Error Message
                            Toast.makeText(this, getString(R.string.login__fail_account), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Log.d("test", "firebaseAuthWithGoogle: "+task.getException());
                        Toast.makeText(this, getString(R.string.login__fail), Toast.LENGTH_LONG).show();
                        String email = user.getEmail();
                        handleFirebaseAuthError(email);
                    }
                });
    }

    private void handleFirebaseAuthError(String email) {
        mAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
            @Override
            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                if (task.isSuccessful()) {
                    SignInMethodQueryResult result = task.getResult();
                    List<String> signInMethod = result.getSignInMethods();

                    Util.showLog("SignInMethod  =" + signInMethod);
                    if (signInMethod.contains(Constant.EMAILAUTH)) {
                        dialogMsg.showErrorDialog("[" + email + "]" + getString(R.string.login__auth_email), getString(R.string.ok));
                        dialogMsg.show();
                    } else if (signInMethod.contains(Constant.GOOGLEAUTH)) {
                        dialogMsg.showErrorDialog("[" + email + "]" + getString(R.string.login__auth_google), getString(R.string.ok));
                        dialogMsg.show();
                    }
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        return;
    }

    @Override
    protected void onDestroy() {
        if (mAuth != null)
            mAuth.signOut();
        if (mGoogleSignInClient != null)
            mGoogleSignInClient.signOut();
        super.onDestroy();
    }
}