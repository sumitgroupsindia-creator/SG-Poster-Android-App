package com.sgdigitalposter.app.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.sgdigitalposter.app.R;
import com.sgdigitalposter.app.utils.Util;
import com.google.android.material.textfield.TextInputEditText;

public class PaymentCardDialog {

    Activity activity;
    public Dialog dialog;
    public TextView card_number;
    public TextView card_expire;
    public TextView card_cvv;
    public TextView card_name;
    public TextView amount_usd;

    public TextInputEditText et_card_number;
    public TextInputEditText et_expire;
    public TextInputEditText et_cvv;
    public TextInputEditText et_name;

    public Button btnContinue;

    InterFacetListener interFacetListener;

    public PaymentCardDialog(Activity activity, InterFacetListener interFacetListener) {
        this.activity = activity;
        this.interFacetListener = interFacetListener;
        this.dialog = new Dialog(activity);
        this.dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
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

    public void showDialog(String amount) {
        if (dialog != null) {
            this.dialog.setContentView(R.layout.layout_card_details);
            card_number = (TextView) dialog.findViewById(R.id.card_number);
            card_expire = (TextView) dialog.findViewById(R.id.card_expire);
            card_cvv = (TextView) dialog.findViewById(R.id.card_cvv);
            card_name = (TextView) dialog.findViewById(R.id.card_name);
            amount_usd = (TextView) dialog.findViewById(R.id.amount_usd);

            et_card_number = (TextInputEditText) dialog.findViewById(R.id.et_card_number);
            et_expire = (TextInputEditText) dialog.findViewById(R.id.et_expire);
            et_cvv = (TextInputEditText) dialog.findViewById(R.id.et_cvv);
            et_name = (TextInputEditText) dialog.findViewById(R.id.et_name);
            btnContinue = (Button) dialog.findViewById(R.id.btn_continue);

            amount_usd.setText(" $ " + amount);
            et_card_number.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int count) {
                    if (charSequence.toString().trim().length() == 0) {
                        card_number.setText("**** **** **** ****");
                    } else {
                        String number = Util.insertPeriodically(charSequence.toString().trim(), " ", 4);
                        card_number.setText(number);
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });

            et_expire.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int count) {
                    if (charSequence.toString().trim().length() == 0) {
                        card_expire.setText("MM/YYYY");
                    } else {
                        card_expire.setText(charSequence);
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });

            et_cvv.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int count) {
                    if (charSequence.toString().trim().length() == 0) {
                        card_cvv.setText("***");
                    } else {
                        card_cvv.setText(charSequence.toString().trim());
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });

            et_name.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int count) {
                    if (charSequence.toString().trim().length() == 0) {
                        card_name.setText("Your Name");
                    } else {
                        card_name.setText(charSequence.toString().trim());
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });

            btnContinue.setOnClickListener(view -> {
                if (checkValidate()) {

                    String cardNumber = et_card_number.getText().toString();
                    String cardName = et_name.getText().toString();
                    String cardCvv = et_cvv.getText().toString();

                    String expiry = et_expire.getText().toString();

                    int month = Integer.parseInt("" + expiry.charAt(0) + expiry.charAt(1));
                    int year = Integer.parseInt("" + expiry.charAt(2) + expiry.charAt(3) + expiry.charAt(4) + expiry.charAt(5));

                    interFacetListener.onComplete(cardNumber, cardCvv, cardName, month, year);
                    dialog.dismiss();
                }
            });

            if (dialog.getWindow() != null) {
                dialog.getWindow().setAttributes(getLayoutParams(dialog));

                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                dialog.setCancelable(true);
            }
            dialog.show();
        }
    }

    private boolean checkValidate() {
        if (et_card_number.getText().toString().isEmpty() || et_card_number.getText().length() > 16) {
            et_card_number.setError("Enter a valid card number");
            et_card_number.requestFocus();
            return false;
        } else if (et_expire.getText().toString().isEmpty() || et_expire.getText().length() > 6) {
            et_expire.setError("Enter a valid date");
            et_expire.requestFocus();
            return false;
        } else if (et_cvv.getText().toString().isEmpty() || et_cvv.getText().length() > 3) {
            et_cvv.setError("Enter a valid cvv");
            et_cvv.requestFocus();
            return false;
        } else if (et_name.getText().toString().isEmpty()) {
            et_name.setError("Enter Card Holder Name");
            et_name.requestFocus();
            return false;
        }
        return true;
    }

    public void cancel() {
        if (dialog != null) {
            dialog.cancel();
        }
    }

    public interface InterFacetListener {
        void onComplete(String cardNumber, String cvv, String name, int month, int year);
    }

}
