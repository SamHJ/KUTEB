package com.naijaunik.kuteb.Activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonObject;
import com.naijaunik.kuteb.Api.BusProvider;
import com.naijaunik.kuteb.Api.Communicator;
import com.naijaunik.kuteb.Api.ErrorEvent;
import com.naijaunik.kuteb.Api.ServerEvent;
import com.naijaunik.kuteb.Api.ServerResponse;
import com.naijaunik.kuteb.R;
import com.naijaunik.kuteb.Utils.AppSession;
import com.naijaunik.kuteb.Utils.Utilities;
import com.squareup.otto.Subscribe;

import java.util.Objects;

public class AdminSettings extends AppCompatActivity implements View.OnClickListener {

    private Utilities utilities;
    private AppSession appSession;
    private JsonObject userObj;
    private Communicator communicator;
    private boolean isAdmin;
    private Toolbar toolbar;

    String appName,trialPeriod,aboutApp,privacyPolicy,terms,contactPhone,queryEmail,paymentOption,version,currencySymbol,
            currencyName,countryCode;

    TextInputEditText input_payment_option,input_query_email,input_contact_telephone,input_terms_and_conditions,
            input_privacy_policy,input_about_app,input_trial_period,input_app_name,input_app_version,input_currency_symbol,
            input_currency_name,input_country_code,question_difficulties;
    MaterialButton btn_update_admin_settings;
    private ProgressDialog dialog;
    private ServerResponse serverResponse;
    private JsonObject adminSettings;
    private boolean isExecuted = false;
    private String questionDifficulties;

    private ChipGroup chip_group;

    String[] difficultyOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        utilities = Utilities.getInstance(this);
        appSession = appSession.getInstance(this);
        userObj = appSession.getUser();
        communicator = new Communicator();

        adminSettings = appSession.getAdminSettings();

        isAdmin = utilities.cleanData(userObj.get("status")).equals("admin");

        setContentView(R.layout.activity_admin_settings);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Admin Settings");

        //init fields
        initFields();

    }

    @SuppressLint("SetTextI18n")
    private void initFields() {

        dialog = utilities.showLoadingDialog("Updating Settings","a moment please");

        input_payment_option = findViewById(R.id.input_payment_option);
        input_query_email = findViewById(R.id.input_query_email);
        input_contact_telephone = findViewById(R.id.input_contact_telephone);
        input_terms_and_conditions = findViewById(R.id.input_terms_and_conditions);
        input_privacy_policy = findViewById(R.id.input_privacy_policy);
        input_about_app = findViewById(R.id.input_about_app);
        input_trial_period = findViewById(R.id.input_trial_period);
        input_app_name = findViewById(R.id.input_app_name);
        btn_update_admin_settings = findViewById(R.id.btn_update_admin_settings);
        input_app_version = findViewById(R.id.input_app_version);
        input_currency_symbol = findViewById(R.id.input_currency_symbol);
        input_currency_name = findViewById(R.id.input_currency_name);
        input_country_code = findViewById(R.id.input_country_code);
        question_difficulties = findViewById(R.id.question_difficulties);

        input_payment_option.setText("flutterwave");
        input_app_version.setText("1.0");

        input_payment_option.setText(utilities.cleanData(adminSettings.get("payment_option")));
        input_query_email.setText(utilities.cleanData(adminSettings.get("query_email")));
        input_contact_telephone.setText(utilities.cleanData(adminSettings.get("contact_telephone")));
        input_terms_and_conditions.setText(utilities.cleanData(adminSettings.get("terms_and_conditions")));
        input_privacy_policy.setText(utilities.cleanData(adminSettings.get("privacy_policy")));
        input_about_app.setText(utilities.cleanData(adminSettings.get("about_app")));
        input_trial_period.setText(utilities.cleanData(adminSettings.get("trial_period")));
        input_app_name.setText(utilities.cleanData(adminSettings.get("app_name")));
        input_app_version.setText(utilities.cleanData(adminSettings.get("new_app_version")));
        input_currency_symbol.setText(utilities.cleanData(adminSettings.get("currency_symbol")));
        input_currency_name.setText(utilities.cleanData(adminSettings.get("currency_name")));
        input_country_code.setText(utilities.cleanData(adminSettings.get("country_code")));

        String questionDiffs  = utilities.cleanData(adminSettings.get("question_difficulties"));

        question_difficulties.setText(questionDiffs);


        chip_group = findViewById(R.id.chip_group);

        difficultyOptions = questionDiffs.split("\\s*,\\s*");

        //set initial options syntax
        for(String genre : difficultyOptions) {
            Chip chip = new Chip(this);
            chip.setText(genre);
            chip_group.addView(chip);
        }


        btn_update_admin_settings.setOnClickListener(this);

        input_payment_option.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){

            case R.id.btn_update_admin_settings:
                validateAndUpdateSettings();
                break;

            case R.id.input_payment_option:

                CharSequence options_image[] = new CharSequence[]{
//                        "payumoney",
//                        "paypal",
//                        "paystack",
                        "flutterwave",
                        "Cancel"
                };
                AlertDialog.Builder builderoptions_image = new AlertDialog.Builder(this);
                builderoptions_image.setTitle("Select Payment Option");

                builderoptions_image.setItems(options_image, new DialogInterface.OnClickListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

//                        if(which == 0){
//
//                           input_payment_option.setText("payumoney");
//                            dialog.dismiss();
//
//                        }
//                        else if (which == 1) {
//
//                            input_payment_option.setText("paypal");
//                            dialog.dismiss();
//
//                        }else if (which == 2) {
//
//                           input_payment_option.setText("paystack");
//                            dialog.dismiss();
//
//                        }else if (which == 3) {
                        if(which == 0){

                            input_payment_option.setText("flutterwave");
                            dialog.dismiss();

                        }
                    }
                });

                builderoptions_image.show();

                break;
        }
    }

    private void validateAndUpdateSettings() {

        appName = input_app_name.getText().toString().trim();
        trialPeriod = input_trial_period.getText().toString().trim();
        aboutApp = input_about_app.getText().toString().trim();
        privacyPolicy = input_privacy_policy.getText().toString().trim();
        terms = input_terms_and_conditions.getText().toString().trim();
        contactPhone = input_contact_telephone.getText().toString().trim();
        queryEmail = input_query_email.getText().toString().trim();
        paymentOption = input_payment_option.getText().toString().trim();
        version = input_app_version.getText().toString().trim();
        currencySymbol = input_currency_symbol.getText().toString().trim();
        currencyName = input_currency_name.getText().toString().trim();
        countryCode = input_country_code.getText().toString().trim();
        questionDifficulties = question_difficulties.getText().toString().trim();

        if(isNotEmpty()){

            dialog.show();

            isExecuted = true;

            communicator.updateAdminSettings("updateadminsettings",
                    appName,trialPeriod,aboutApp,privacyPolicy,terms,contactPhone,
                    queryEmail,paymentOption,version,currencySymbol,currencyName,countryCode,
                    questionDifficulties);


        }else{

            utilities.dialogError(this,"All fields are required!");

        }

    }

    private boolean isNotEmpty() {

        return !appName.isEmpty() && !trialPeriod.isEmpty() && !aboutApp.isEmpty() && !privacyPolicy.isEmpty() &&
                !terms.isEmpty() && !contactPhone.isEmpty() && !queryEmail.isEmpty() && !paymentOption.isEmpty()
                && !version.isEmpty() && !currencySymbol.isEmpty() && !currencyName.isEmpty()
                && !countryCode.isEmpty();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return  super.onCreateOptionsMenu(menu);

    }

    @SuppressLint("NewApi")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
            overridePendingTransition(R.anim.left_in, R.anim.right_out);

        }

        return super.onOptionsItemSelected(item);
    }

    @Subscribe
    public void onServerEvent(ServerEvent serverEvent){

        if(isExecuted) {

            serverResponse = serverEvent.getServerResponse();

            if (serverResponse != null) {

                if (serverResponse.getResult().equals("success")) {

                    utilities.dialogError(this, getString(R.string.update_successful));

                } else {

                    //show login error dialog
                    utilities.dialogError(this, getString(R.string.error_occurred));
                }
            }

            //dismiss dialog loader
            dialog.dismiss();

        }
    }

    @Subscribe
    public void onErrorEvent(ErrorEvent errorEvent){

        Log.e("AdminSettings: ",errorEvent.getErrorMsg());

        dialog.dismiss();

        if(isExecuted){

            utilities.dialogError(AdminSettings.this,getString(R.string.error_occurred));
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        try {
            BusProvider.getInstance().register(this);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        try {
            BusProvider.getInstance().unregister(this);
        }catch (Exception e){
            e.printStackTrace();
        }

        finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }

}