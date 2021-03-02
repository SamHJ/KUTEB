package com.naijaunik.kuteb.Activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import android.widget.ImageView;
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

public class UpdateCourse extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_STORAGE_PERMISSION = 9948;
    private Communicator communicator;
    private Utilities utilities;
    private AppSession appSession;
    private JsonObject userObj;
    String title,description,noOfStudents,status;

    ImageView course_image;
    TextInputEditText input_title,input_description,input_no_of_students,input_status;
    private String courseImage;
    MaterialButton btn_update_course;
    ProgressDialog dialog;
    private int gallery_Pic = 572;
    private String commpressedImage;
    private Uri resultUri;
    private ServerResponse serverResponse;
    private Toolbar toolbar;
    private boolean isExecuted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_course);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Update "+getIntent().getStringExtra("title"));

        //init fields
        initFields();
    }

    private void initFields() {

        communicator = new Communicator();

        utilities = Utilities.getInstance(this);
        appSession = AppSession.getInstance(this);
        userObj = appSession.getUser();

        dialog = utilities.showLoadingDialog("Updating Course...","");

        course_image = findViewById(R.id.course_image);
        input_title = findViewById(R.id.input_title);
        input_description = findViewById(R.id.input_description);
        input_no_of_students = findViewById(R.id.input_no_of_students);
        input_status = findViewById(R.id.input_status);

        btn_update_course = findViewById(R.id.btn_update_course);

        input_title.setText(getIntent().getStringExtra("title"));
        input_description.setText(getIntent().getStringExtra("description"));
        input_no_of_students.setText(getIntent().getStringExtra("no_of_students_enrolled"));
        input_status.setText(getIntent().getStringExtra("status"));

        courseImage = getIntent().getStringExtra("course_icon");

        try{
            Picasso.get().load(courseImage).networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.course_placeholder)
                    .error(R.drawable.course_placeholder).into(course_image,
                    new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(courseImage)
                                    .placeholder(R.drawable.course_placeholder)
                                    .error(R.drawable.course_placeholder).into(course_image);
                        }

                    });
        }catch (Exception e){
            e.printStackTrace();
        }

        btn_update_course.setOnClickListener(this);
        input_status.setOnClickListener(this);
        course_image.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.input_status){

            CharSequence options_image[] = new CharSequence[]{
                    "available",
                    "pending",
                    "cancelled",
                    "Cancel"
            };
            AlertDialog.Builder builderoptions_image = new AlertDialog.Builder(this);
            builderoptions_image.setTitle("Status Options");

            builderoptions_image.setItems(options_image, new DialogInterface.OnClickListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    if(which == 0){

                        input_status.setText("available");
                        dialog.dismiss();

                    }else if (which == 1) {

                        input_status.setText("pending");
                        dialog.dismiss();

                    } else if (which == 2) {

                        input_status.setText("cancelled");
                        dialog.dismiss();

                    }
                }
            });

            builderoptions_image.show();


        }else if(view.getId() == R.id.btn_update_course){

            title = input_title.getText().toString().trim();
            description = input_description.getText().toString().trim();
            status = input_status.getText().toString().trim();
            noOfStudents = input_no_of_students.getText().toString().trim();

            if(title.isEmpty()){

                utilities.dialogError(this,"Please enter a title first!");

            }else{

                updateCourse();

            }
        }else if(view.getId() == R.id.course_image){

            requestPermissionsFromUser();
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
            selectImageFromGallery();
        }
    }


    private void selectImageFromGallery() {
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
        course_image.setImageBitmap(bmp);
        //upload image to server
        commpressedImage = encodedImage;
    }

    private void updateCourse() {

        dialog.show();

        isExecuted = true;

        if(commpressedImage != null){

            //user selected an image

            //no image was selected
            communicator.updateCourse(getIntent().getStringExtra("id"),title, description,
                    noOfStudents,status,utilities.getAbsolutePath(resultUri),"no",
                    "updatecourse",resultUri, UpdateCourse.this);

        }else{

            //no image was selected
            communicator.updateCourse(getIntent().getStringExtra("id"),title, description,
                    noOfStudents,status,getIntent().getStringExtra("course_icon"),"yes",
                    "updatecourse",null, UpdateCourse.this);
        }

    }

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

                    utilities.dialogError(UpdateCourse.this, getString((R.string.update_successful)));

                } else {

                    //show login error dialog
                    utilities.dialogError(UpdateCourse.this, getString(R.string.error_occurred));
                }
            } else {

                //show login error dialog
                utilities.dialogError(UpdateCourse.this, getString(R.string.error_occurred));
            }

            //dismiss dialog loader
            dialog.dismiss();

        }
    }

    @Subscribe
    public void onErrorEvent(ErrorEvent errorEvent){

        Log.e("UpdateCourse: ",errorEvent.getErrorMsg());

        dialog.dismiss();

       if(isExecuted){

           utilities.dialogError(UpdateCourse.this,getString(R.string.error_occurred));

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