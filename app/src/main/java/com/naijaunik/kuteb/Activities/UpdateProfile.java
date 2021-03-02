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
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

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
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

import adil.dev.lib.materialnumberpicker.dialog.GenderPickerDialog;
import de.hdodenhof.circleimageview.CircleImageView;

public class UpdateProfile extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_STORAGE_PERMISSION = 457;
    private static final int gallery_Pic = 401;

    Toolbar toolbar;
    CircleImageView profile_image;
    TextInputEditText input_first_name,input_last_name,input_phone,input_email,input_gender;
    MaterialButton btn_update_profile;
    private String firstname,lastname,gender,email,phone;
    AppSession appSession;
    JsonObject userObj;
    Utilities utilities;
    private String commpressedImage;
    Communicator communicator;
    String userImage;
    private ProgressDialog dialog;
    private ServerResponse serverResponse;
    private Uri resultUri;
    private boolean isExecuted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        communicator = new Communicator();

        utilities = Utilities.getInstance(this);
        appSession = AppSession.getInstance(this);
        userObj = appSession.getUser();

        if(!utilities.cleanData(userObj.get("status")).equals("admin")){

            //prevent screen capture
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        }

        setContentView(R.layout.activity_update_profile);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Update Profile");

        //init fields
        initFields();
    }

    private void initFields() {

        dialog = utilities.showLoadingDialog(getString(R.string.updating_profile),getString(R.string.a_moment));

        profile_image = findViewById(R.id.profile_image);
        input_first_name = findViewById(R.id.input_first_name);
        input_last_name = findViewById(R.id.input_last_name);
        input_phone = findViewById(R.id.input_phone);
        input_email = findViewById(R.id.input_email);
        btn_update_profile = findViewById(R.id.btn_update_profile);
        input_gender = findViewById(R.id.input_gender);

        input_first_name.setText(utilities.cleanData(userObj.get("first_name")));
        input_last_name.setText(utilities.cleanData(userObj.get("last_name")));
        input_phone.setText(utilities.cleanData(userObj.get("phone")));
        input_email.setText(utilities.cleanData(userObj.get("email")));
        input_gender.setText(utilities.cleanData(userObj.get("gender")));

        userImage = utilities.cleanData(userObj.get("userimage"));

        try{
            Picasso.get().load(userImage).networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.default_profile)
                    .error(R.drawable.default_profile).into(profile_image,
                    new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(userImage)
                                    .placeholder(R.drawable.default_profile)
                                    .error(R.drawable.default_profile).into(profile_image);
                        }

                    });
        }catch (Exception e){
            e.printStackTrace();
        }

        btn_update_profile.setOnClickListener(this);
        profile_image.setOnClickListener(this);
        input_gender.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.btn_update_profile){

            firstname = input_first_name.getText().toString().trim();
            lastname = input_last_name.getText().toString().trim();
            phone = input_phone.getText().toString().trim();
            email = input_email.getText().toString().trim();
            gender = input_gender.getText().toString().trim();

            updateProfile();

        }else if (view.getId() == R.id.profile_image){

            requestPermissionsFromUser();

        }else if(view.getId() == R.id.input_gender){

            GenderPickerDialog dialog=new GenderPickerDialog(this);
            dialog.setOnSelectingGender(new GenderPickerDialog.OnGenderSelectListener() {
                @Override
                public void onSelectingGender(String value) {
                    input_gender.setText(value);
                }
            });
            dialog.show();
        }
    }

    private void updateProfile() {

        dialog.show();

        isExecuted = true;

        if(commpressedImage != null){

            //user selected an image

            //no image was selected
            communicator.updateUserProfile(firstname, lastname,
                    phone,email,gender,utilities.getAbsolutePath(resultUri),"no",
                    "updateaccount",resultUri, UpdateProfile.this);

        }else{

            //no image was selected
            communicator.updateUserProfile(firstname, lastname,
                    phone,email,gender,userImage,"yes",
                    "updateaccount",null, UpdateProfile.this);
        }

    }

    private void requestPermissionsFromUser() {

        if(PackageManager.PERMISSION_GRANTED !=
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        || PackageManager.PERMISSION_GRANTED !=
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_STORAGE_PERMISSION);
            }else {
                //Yeah! I want both block to do the same thing, you can write your own logic, but this works for me.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_STORAGE_PERMISSION);
            }
        }else {
            //Permission Granted, lets go pick photo
            selectProfileImageFromGallery();
        }
    }


    private void selectProfileImageFromGallery() {
        Intent gallery_intent = new Intent();
        gallery_intent.setAction(Intent.ACTION_GET_CONTENT);
        gallery_intent.setType("image/*");
        startActivityForResult(Intent.createChooser(gallery_intent, "Select image"), gallery_Pic);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == gallery_Pic && resultCode == RESULT_OK &&
                data != null) {
            resultUri = data.getData();
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), resultUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            toStringImage(bitmap);

        }else{
            Toast.makeText(this, "An error occurred!", Toast.LENGTH_SHORT).show();
        }
    }

    public void toStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        profile_image.setImageBitmap(bmp);
        //upload image to server
        commpressedImage = encodedImage;
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

                    JsonObject userdata = serverResponse.getUserdata();
                    JsonObject adminsettings = serverResponse.getAdmin_settings();

                    if (userdata != null) {

                        //check if the user has been blocked
                        if (Integer.parseInt(Utilities.cleanData(userdata.get("is_blocked"))) == 1) {

                            //user has been blocked so we log them out!
                            appSession.logout();
                            startActivity(new Intent(this, Login.class));
                            overridePendingTransition(R.anim.right_in, R.anim.left_out);
                            finish();

                            utilities.dialogError(this, getString(R.string.account_blocked));

                        } else {

                            //storing the user in shared preferences
                            AppSession.getInstance(this).userLogin(userdata);

                            //store the admin settings in preferences
                            AppSession.getInstance(this).storeAdminSettings(adminsettings);

                            utilities.dialogError(this, getString(R.string.update_successful));
                        }
                    }

                } else {

                    //show login error dialog
                    utilities.dialogError(UpdateProfile.this, getString(R.string.error_occurred));
                }
            }

            //dismiss dialog loader
            dialog.dismiss();

        }
    }

    @Subscribe
    public void onErrorEvent(ErrorEvent errorEvent){

        Log.e("Profile: ",errorEvent.getErrorMsg());

        dialog.dismiss();

        if(isExecuted) {
            utilities.dialogError(UpdateProfile.this, getString(R.string.error_occurred));
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