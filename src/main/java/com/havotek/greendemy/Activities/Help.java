package com.naijaunik.kuteb.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import com.google.android.material.button.MaterialButton;
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

public class Help extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_PHONE_CALL_PERMISSION = 219;
    TextInputEditText input_subject,input_message;
    MaterialButton send_btn,tel_btn;
    private Toolbar toolbar;
    private Utilities utilities;
    private AppSession appSession;
    private JsonObject userObj;
    private String contactPhone;
    private String subject,message;
    private Communicator communicator;
    private ProgressDialog dialog;
    private ServerResponse serverResponse;
    private String senderEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        utilities = Utilities.getInstance(this);
        appSession = appSession.getInstance(this);
        userObj = appSession.getUser();
        communicator = new Communicator();

        if(!utilities.cleanData(userObj.get("status")).equals("admin")){

            //prevent screen capture
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        }

        setContentView(R.layout.activity_help);


        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Help");

        //init fields
        initFields();
    }

    @SuppressLint("SetTextI18n")
    private void initFields() {

        tel_btn = findViewById(R.id.tel_btn);
        send_btn = findViewById(R.id.send_btn);
        input_message = findViewById(R.id.input_message);
        input_subject = findViewById(R.id.input_subject);

        senderEmail = utilities.cleanData(userObj.get("email"));

        input_message.setText("Hi! My name is "+utilities.cleanData(userObj.get("first_name")) + " " +
                utilities.cleanData(userObj.get("last_name")) +" and my phone number is "+
                utilities.cleanData(userObj.get("phone")) +". \n I need help on...");

        dialog = utilities.showLoadingDialog("Sending email...","a moment please");

        contactPhone = utilities.cleanData(appSession.getAdminSettings().get("contact_telephone"));

        tel_btn.setText(contactPhone);



        tel_btn.setOnClickListener(this);
        send_btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        if(view.getId() == R.id.send_btn){

            //send message
            subject = input_subject.getText().toString().trim();
            message = input_message.getText().toString().trim();

            if(subject.isEmpty() || message.isEmpty()){

                utilities.dialogError(this,"All fields are required!");

            }else{

               dialog.show();

               try{
                   communicator.sendHelpEmail("sendhelpemail",subject,message,
                           utilities.cleanData(appSession.getAdminSettings().get("query_email")),
                           senderEmail,
                           utilities.cleanData(appSession.getAdminSettings().get("app_name")));

               }catch (Exception e){

                   e.printStackTrace();
               }
            }

        }else if(view.getId() == R.id.tel_btn){

            //init calling
           requestPermissionsFromUser();

        }
    }

    private void requestPermissionsFromUser() {

        if(PackageManager.PERMISSION_GRANTED !=
                ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                || PackageManager.PERMISSION_GRANTED !=
                ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)) {

            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CALL_PHONE)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE},
                        REQUEST_PHONE_CALL_PERMISSION);
            }else {
                //Yeah! I want both block to do the same thing, you can write your own logic, but this works for me.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE},
                        REQUEST_PHONE_CALL_PERMISSION);
            }
        }else {

            //Permission Granted, lets make a call
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + contactPhone));
            startActivity(intent);
        }
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

    @SuppressLint("SetTextI18n")
    @Subscribe
    public void onServerEvent(ServerEvent serverEvent){

        serverResponse = serverEvent.getServerResponse();

        if(serverResponse != null){

            if(serverResponse.getResult().equals("success")){

                utilities.dialogError(Help.this,"Help query sent successfully!");

                input_message.setText("Hi! My name is "+utilities.cleanData(userObj.get("first_name")) + " " +
                        utilities.cleanData(userObj.get("last_name")) +" and my phone number is "+
                        utilities.cleanData(userObj.get("phone")) +". \n I need help on...");
                input_subject.setText("");

            }else{

                //show login error dialog
                utilities.dialogError(Help.this,getString(R.string.error_occurred));
            }
        }else {

            //show login error dialog
            utilities.dialogError(Help.this,getString(R.string.error_occurred));
        }

        //dismiss dialog loader
        dialog.dismiss();
    }

    @Subscribe
    public void onErrorEvent(ErrorEvent errorEvent){

        Log.e("Help: ",errorEvent.getErrorMsg());

        dialog.dismiss();

        utilities.dialogError(Help.this,getString(R.string.error_occurred));
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