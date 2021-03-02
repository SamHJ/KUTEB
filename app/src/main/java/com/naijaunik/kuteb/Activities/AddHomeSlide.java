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
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.naijaunik.kuteb.Api.BusProvider;
import com.naijaunik.kuteb.Api.Communicator;
import com.naijaunik.kuteb.Api.ErrorEvent;
import com.naijaunik.kuteb.Api.ServerEvent;
import com.naijaunik.kuteb.Api.ServerResponse;
import com.naijaunik.kuteb.R;
import com.naijaunik.kuteb.Utils.Utilities;
import com.squareup.otto.Subscribe;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

public class AddHomeSlide extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_STORAGE_PERMISSION = 3623;
    ImageView slide_img;
    TextInputEditText input_title,input_subtitle,input_go_to_url;
    MaterialButton btn_update_slide;
    private String commpressedImage;
    private int gallery_Pic = 3753;
    private Uri resultUri;

    Utilities utilities;
    private ProgressDialog dialog;
    Communicator communicator;
    String title, subtitle, goToUrl;
    private ServerResponse serverResponse;
    private Toolbar toolbar;
    private boolean isExecuted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_home_slide);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Add Slide");


        initFields();
    }

    private void initFields() {

        utilities = Utilities.getInstance(this);
        communicator = new Communicator();
        dialog = utilities.showLoadingDialog("Adding Slide Item...","");;

        slide_img = findViewById(R.id.slide_img);
        input_title = findViewById(R.id.input_title);
        input_subtitle = findViewById(R.id.input_subtitle);
        input_go_to_url = findViewById(R.id.input_go_to_url);

        btn_update_slide = findViewById(R.id.btn_update_slide);

        btn_update_slide.setOnClickListener(this);
        slide_img.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        if(view.getId() == R.id.btn_update_slide){

            title = input_title.getText().toString().trim();
            subtitle = input_subtitle.getText().toString().trim();
            goToUrl = input_go_to_url.getText().toString().trim();

            if(commpressedImage == null){

                utilities.dialogError(this,"Please select an image first!");

            }else{

                dialog.show();

                isExecuted = true;

                communicator.updateSlideItem("addslideitem",title,subtitle,goToUrl,"","",
                        resultUri,utilities.getAbsolutePath(resultUri), this,"no");

            }

        }else if(view.getId() == R.id.slide_img){

            requestUserPermission();
        }

    }

    private void requestUserPermission() {

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
            selectSlideImageFromGallery();
        }
    }


    private void selectSlideImageFromGallery() {
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
        slide_img.setImageBitmap(bmp);
        //upload image to server
        commpressedImage = encodedImage;
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

    @SuppressLint("UseCompatLoadingForDrawables")
    @Subscribe
    public void onServerEvent(ServerEvent serverEvent){

        if(isExecuted) {

            serverResponse = serverEvent.getServerResponse();

            if (serverResponse != null) {

                if (serverResponse.getResult().equals("success")) {

                    utilities.dialogError(this, getString(R.string.upload_successful));
                    input_go_to_url.setText("");
                    input_subtitle.setText("");
                    input_title.setText("");
                    slide_img.setImageDrawable(getResources().getDrawable(R.drawable.greendemy_logo));

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

        Log.e("AddHomeSlide: ",errorEvent.getErrorMsg());

        dialog.dismiss();

        if(isExecuted){

            utilities.dialogError(this,getString(R.string.error_occurred));
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