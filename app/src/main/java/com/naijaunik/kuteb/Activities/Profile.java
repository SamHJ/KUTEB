package com.naijaunik.kuteb.Activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonArray;
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
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.naijaunik.kuteb.Utils.AppConstants.COUNTRY_PHONE_CODE;
import static com.naijaunik.kuteb.Utils.AppConstants.DATE_TIME_FORMAT;

public class Profile extends AppCompatActivity implements View.OnClickListener {

    CircleImageView profile_image;
    TextView user_name_text,phone_number_text,email_text;
    private Toolbar toolbar;
    private AppSession appSession;
    private Utilities utilities;
    private String userImage;
    JsonObject userObj;
    ImageButton edit_btn;
    MaterialButton logout_btn,upgrade_btn;

    CardView upgrade_cardview;

    TextView subscription_details,status_text;
    private boolean isAdmin;
    private Communicator communicator;
    private ServerResponse serverResponse;
    private boolean isExecuted = false;

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

        setContentView(R.layout.activity_profile);


        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Profile");

        //init fields
        initFields();
    }

    private void initFields() {


        isAdmin = utilities.cleanData(userObj.get("status")).equals("admin");

        profile_image = findViewById(R.id.profile_image);
        user_name_text = findViewById(R.id.user_name_text);
        phone_number_text = findViewById(R.id.phone_number_text);
        email_text = findViewById(R.id.email_text);
        logout_btn = findViewById(R.id.logout_btn);

        status_text = findViewById(R.id.status_text);

        upgrade_cardview = findViewById(R.id.upgrade_cardview);
        upgrade_btn = findViewById(R.id.upgrade_btn);
        subscription_details = findViewById(R.id.subscription_details);

        edit_btn = findViewById(R.id.edit_btn);

        //attach click listeners
        upgrade_btn.setOnClickListener(this);
        edit_btn.setOnClickListener(this);
        logout_btn.setOnClickListener(this);

        try {
            email_text.setText(String.format("Email: %s", utilities.cleanData(userObj.get("email"))));

            phone_number_text.setText(String.format("Ph: %s %s", COUNTRY_PHONE_CODE,
                    utilities.cleanData(userObj.get("phone"))));

            user_name_text.setText(String.format("%s %s", utilities.cleanData(userObj.get("first_name")),
                    utilities.cleanData(userObj.get("last_name"))));

        }catch (Exception e){

            e.printStackTrace();
        }

        userImage = utilities.cleanData(userObj.get("userimage"));

        if(isAdmin){

            status_text.setVisibility(View.VISIBLE);
            status_text.setText(R.string.status_text);

        }

        try{
            Picasso.get().load(userImage).networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.profile_placeholder_white)
                    .error(R.drawable.profile_placeholder_white).into(profile_image,
                    new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(userImage)
                                    .placeholder(R.drawable.profile_placeholder_white)
                                    .error(R.drawable.profile_placeholder_white).into(profile_image);
                        }

                    });
        }catch (Exception e){
            e.printStackTrace();
        }
        
        //check user's subscription
        checkExpirationDate();

    }

    @SuppressLint({"SimpleDateFormat", "DefaultLocale"})
    private void checkExpirationDate() {

        String currentUserExpirationDate = utilities.cleanData(userObj.get("current_expiration_date"));

        SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_FORMAT);
        Date strDate;

        int isTrial = Integer.parseInt(utilities.cleanData(userObj.get("is_trial")));

        if(!currentUserExpirationDate.trim().isEmpty()){

            try {

                strDate = sdf.parse(currentUserExpirationDate);
                boolean is_plan_expired =  System.currentTimeMillis() > strDate.getTime();

                if(is_plan_expired){

                    //set user plan as expired
                    appSession.setUserPlanExpired(true);

                    if(!isAdmin) {

                        upgrade_cardview.setVisibility(View.VISIBLE);
                    }

                    if(isTrial == 1){

                        subscription_details.setText(getResources().getString(R.string.trial_expired_explanation));
                        subscription_details.setTextColor(getResources().getColor(R.color.red));

                    }else{

                        subscription_details.setText(getResources().getString(R.string.plan_expired_explanation));
                        subscription_details.setTextColor(getResources().getColor(R.color.red));
                    }

                }else{

                    String expirationDate = utilities.
                            addNDaystToCurrentDate(getExpirationDateDifferenc(strDate.getTime())).toString();

//                    //show upgrade warning 7 days ahead of expiration date
                    if(getExpirationDateDifferenc(strDate.getTime()) <= 7){

                        if(!isAdmin) {

                            upgrade_cardview.setVisibility(View.VISIBLE);
                        }


                        if(isTrial == 1) {

                            subscription_details.setText(String.format(getString(R.string.trial_warning_text_explained),
                                    expirationDate));

                            subscription_details.setTextColor(getResources().getColor(R.color.red));

                        }else{

                            subscription_details.setText(String.format(getString(R.string.plan_warning_text_explained),
                                    expirationDate));

                            subscription_details.setTextColor(getResources().getColor(R.color.red));
                        }

                    }else {

                        if(isTrial == 1){

                            subscription_details.setText(String.format(getString(R.string.trial_expiration_text_active),
                                    expirationDate));

                        }else{

                            subscription_details.setText(String.format(getString(R.string.plan_expiration_text_active),
                                    expirationDate));
                        }

                    }
                }

            } catch (ParseException e) {

                e.printStackTrace();

            }

        }
    }

    @SuppressLint("SimpleDateFormat")
    private long getExpirationDateDifferenc(long expirationDate) {

        SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_FORMAT);

        String currentDateandTime = sdf.format(new Date());

        Date formattedDate;
        try {

            formattedDate = sdf.parse(currentDateandTime);

            long diff = expirationDate - formattedDate.getTime();


            return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return 0;

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){

            case R.id.edit_btn:

                startActivity(new Intent(Profile.this, UpdateProfile.class));
                overridePendingTransition(R.anim.right_in, R.anim.left_out);

                break;

            case R.id.logout_btn:

                initLogout();

                break;

            case R.id.upgrade_btn:

                startActivity(new Intent(Profile.this, UpgradePlans.class));
                overridePendingTransition(R.anim.right_in, R.anim.left_out);

                break;
        }
    }

    private void initLogout() {

        AlertDialog.Builder builder = new AlertDialog.Builder(Profile.this);
        builder.setMessage("Are you sure you want to logout?");
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                appSession.logout();
                startActivity(new Intent(getApplicationContext(), Login.class));
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                finish();
            }
        });
        final AlertDialog alertDialog = builder.create();

        alertDialog.setOnShowListener( new DialogInterface.OnShowListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onShow(DialogInterface arg0) {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(R.color.colorPrimary);
                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                        .setTextColor(R.color.colorPrimary);
            }
        });
        alertDialog.show();
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

                //handle different kinds of request responses

                if (serverResponse.getType().equals("homeslider")) {

                    if (serverResponse.getResult().equals("success")) {

                        JsonArray slidesDataArray = serverResponse.getData();

                        //store home slides to prefs

                        appSession.storeHomeSliders(slidesDataArray);

                    }

                } else if (serverResponse.getType().equals("login")) {

                    if (serverResponse.getResult().equals("success")) {

                        JsonObject userdata = serverResponse.getUserdata();
                        JsonObject adminsettings = serverResponse.getAdmin_settings();

                        if (userdata != null) {

                            //check if the user has been blocked
                            if (Integer.parseInt(utilities.cleanData(userdata.get("is_blocked"))) == 1) {

                                //user has been blocked so we log them out!
                                appSession.logout();
                                startActivity(new Intent(this, Login.class));
                                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                                finish();

                                utilities.dialogError(this, getString(R.string.account_blocked));

                            } else {

                                //storing the user in shared preferences
                                appSession.userLogin(userdata);

                                //store the admin settings in preferences
                                appSession.storeAdminSettings(adminsettings);
                            }
                        }

                    }

                } else if (serverResponse.getType().equals("getcourses")) {

                    if (serverResponse.getResult().equals("success")) {

                        appSession.storeCourses(serverResponse.getData());
                    }

                } else if (serverResponse.getType().equals("videos")) {

                    if (serverResponse.getResult().equals("success")) {

                        appSession.storeVideos(serverResponse.getData());
                    }

                }

            }

        }
    }

    @Subscribe
    public void onErrorEvent(ErrorEvent errorEvent){

        Log.e("Profile: ",errorEvent.getErrorMsg());

    }

    private void getUserInfo() {

        isExecuted = true;

        //sign in the user
        communicator.singleParametrizedCall(utilities.cleanData(userObj.get("phone")),"login");
    }

    private void getCourses() {

        isExecuted = true;

        communicator.nonParametrizedCall("getcourses",this);
    }

    private void getVideos() {

        isExecuted = true;

        communicator.nonParametrizedCall("getvideos",this);
    }

    public void onResume(){
        super.onResume();
        try {
            BusProvider.getInstance().register(this);
        }catch (Exception e){
            e.printStackTrace();
        }

        getUserInfo();
        getCourses();
        getVideos();
        getHomeSliders();

        //check user's subscription
        checkExpirationDate();
    }

    private void getHomeSliders() {

        isExecuted = true;

        communicator.nonParametrizedCall("get_home_slides",this);
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