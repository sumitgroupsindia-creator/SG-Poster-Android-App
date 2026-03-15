package com.sgdigitalposter.app.ui.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.sgdigitalposter.app.R;
import com.sgdigitalposter.app.databinding.ActivityContactUsBinding;
import com.sgdigitalposter.app.items.SubjectItem;
import com.sgdigitalposter.app.ui.dialog.DialogMsg;
import com.sgdigitalposter.app.utils.Connectivity;
import com.sgdigitalposter.app.utils.Constant;
import com.sgdigitalposter.app.utils.PrefManager;
import com.sgdigitalposter.app.utils.Util;
import com.sgdigitalposter.app.viewmodel.UserViewModel;

import java.util.ArrayList;
import java.util.List;

public class ContactUsActivity extends AppCompatActivity {

    ActivityContactUsBinding binding;
    PrefManager prefManager;
    UserViewModel userViewModel;
    ArrayList<String> arrayList_name = new ArrayList<>();
    ArrayList<SubjectItem> arrayList_subject = new ArrayList<>();
    DialogMsg dialogMsg;
    ProgressDialog prgDialog;
    Connectivity connectivity;
    int pos = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityContactUsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Util.applyStatusBarPadding(binding.clMain);

        prefManager = new PrefManager(this);
        connectivity = new Connectivity(this);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        dialogMsg = new DialogMsg(this, false);
        prgDialog = new ProgressDialog(this);
        prgDialog.setMessage(getString(R.string.login_loading));
        prgDialog.setCancelable(false);

        setUpUi();
        setUpViewModel();
    }

    private void setUpViewModel() {
        userViewModel.getSubjects().observe(this, result -> {
            if (result != null) {
                if (result.data != null && result.data.size() > 0) {
                    setSpinner(result.data);
                }
            }
        });

    }

    private void setSpinner(List<SubjectItem> data) {
        arrayList_name.clear();
        arrayList_subject.clear();
        for (SubjectItem subjectItem : data) {
            arrayList_name.add(subjectItem.title);
            arrayList_subject.add(subjectItem);
        }
        binding.dropdown2.setAdapter(new ArrayAdapter(ContactUsActivity.this, android.R.layout.simple_list_item_1,
                arrayList_name));
        binding.dropdown2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                pos = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void setUpUi() {
        binding.toolbar.toolName.setText(getResources().getString(R.string.menu_contact));
        binding.toolbar.toolbarIvMenu.setBackground(getResources().getDrawable(R.drawable.ic_back));
        binding.toolbar.toolbarIvMenu.setOnClickListener(v -> {
            onBackPressed();
        });

        if (prefManager.getBoolean(Constant.IS_LOGIN)) {
            userViewModel.getDbUserData(prefManager.getString(Constant.USER_ID)).observe(this, result -> {
                if (result != null) {
                    binding.etName.setText(result.user.userName);
                    binding.etEmail.setText(result.user.email);
                    binding.etNumber.setText(result.user.phone);
                }
            });
        }

        binding.btnSkip.setOnClickListener(v -> {
            if (validate()) {

                if (!connectivity.isConnected()) {
                    Util.showToast(ContactUsActivity.this, getString(R.string.error_message__no_internet));
                    return;
                }

                if (binding.etNumber.getText().toString().length() < 10) {
                    Util.showToast(ContactUsActivity.this, getString(R.string.please_enter_valid_mobile));
                    return;
                }

                String name = binding.etName.getText().toString().trim();
                String email = binding.etEmail.getText().toString().trim();
                String number = binding.etNumber.getText().toString().trim();
                String massage = binding.etDetails.getText().toString().trim();
                String subjectId = arrayList_subject.get(pos).id;
                prgDialog.show();
                userViewModel.sendContact(name, email, number, massage, subjectId).observe(this, result -> {

                    if (result != null) {
                        switch (result.status) {
                            case SUCCESS:

                                prgDialog.cancel();
                                dialogMsg.showSuccessDialog(getString(R.string.message_contact), getString(R.string.ok));
                                dialogMsg.show();
                                dialogMsg.okBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialogMsg.cancel();
                                        onBackPressed();
                                    }
                                });
                                break;

                            case ERROR:
                                prgDialog.cancel();
                                dialogMsg.showErrorDialog(getString(R.string.fail_message_contact), getString(R.string.ok));
                                dialogMsg.show();
                                break;
                        }
                    }
                });
            }
        });

    }

    private Boolean validate() {
        if (binding.etName.getText().toString().trim().isEmpty()) {
            binding.etName.setError(getResources().getString(R.string.hint_name));
            binding.etName.requestFocus();
            return false;
        } else if (binding.etEmail.getText().toString().trim().isEmpty()) {
            binding.etEmail.setError(getResources().getString(R.string.email));
            binding.etEmail.requestFocus();
            return false;
        } else if (!isEmailValid(binding.etEmail.getText().toString())) {
            binding.etEmail.setError(getString(R.string.invalid_email));
            binding.etEmail.requestFocus();
            return false;
        } else if (binding.etDetails.getText().toString().isEmpty()) {
            binding.etDetails.setError(getResources().getString(R.string.enter_details));
            binding.etDetails.requestFocus();
            return false;
        } else if (binding.etNumber.getText().toString().isEmpty()) {
            binding.etNumber.setError(getResources().getString(R.string.hint_phone_number));
            binding.etNumber.requestFocus();
            return false;
        }else if (arrayList_subject == null){
            Toast.makeText(ContactUsActivity.this, "Please Select Subject", Toast.LENGTH_SHORT).show();
            return false;
        }else if(arrayList_subject.size() == 0){
            Toast.makeText(ContactUsActivity.this, "Please Select Subject", Toast.LENGTH_SHORT).show();
            return false;
        }else if(arrayList_subject.get(pos) == null){
            Toast.makeText(ContactUsActivity.this, "Please Select Subject", Toast.LENGTH_SHORT).show();
            return false;
        }
        /*else if (!isEdit && imageUri == null) {
            Toast.makeText(AddBusinessActivity.this, getString(R.string.err_add_image), Toast.LENGTH_SHORT).show();
            return false;
        }*/
        else {
            return true;
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@") && !email.contains(" ");
    }

}