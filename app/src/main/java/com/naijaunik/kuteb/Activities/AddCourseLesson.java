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
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonObject;
import com.naijaunik.kuteb.Api.BusProvider;
import com.naijaunik.kuteb.Api.Communicator;
import com.naijaunik.kuteb.Api.ErrorEvent;
import com.naijaunik.kuteb.Api.ServerEvent;
import com.naijaunik.kuteb.Api.ServerResponse;
import com.naijaunik.kuteb.R;
import com.naijaunik.kuteb.Utils.AppSession;
import com.naijaunik.kuteb.Utils.FileUtils;
import com.naijaunik.kuteb.Utils.Utilities;
import com.squareup.otto.Subscribe;

import java.util.Objects;

public class AddCourseLesson extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_STORAGE_PERMISSION = 513;
    private Communicator communicator;
    private Utilities utilities;
    private AppSession appSession;
    private JsonObject userObj;
    private ProgressDialog dialog;
    private Toolbar toolbar;
    private ServerResponse serverResponse;
    private MaterialButton btn_update_course_lesson;

    private Uri videoUri,attachmentUri;
    String isVideoEmpty, isAttachmentEmpty;

    TextInputEditText input_title,input_content,input_video_type,input_video,input_external_link,input_attachment;
    private String title,content,video,type,extlink,attachment;
    private int video_gallery_Pic = 1354;
    private int attachment_gallery_Pic = 4859;
    private String videoFilePath;
    private String attachmentFilePath;
    private boolean isExecuted = false;

    private TextInputLayout input_video_layout,input_video_2_layout;
    private TextInputEditText input_video_2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_course_lesson);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Add Lesson");

        //init fields
        initFields();
    }

    @SuppressLint("SetTextI18n")
    private void initFields() {

        communicator = new Communicator();

        utilities = Utilities.getInstance(this);
        appSession = AppSession.getInstance(this);
        userObj = appSession.getUser();

        dialog = utilities.showLoadingDialog("Adding Lesson...","");

        btn_update_course_lesson = findViewById(R.id.btn_update_course_lesson);

        input_title = findViewById(R.id.input_title);
        input_content = findViewById(R.id.input_content);
        input_video_type = findViewById(R.id.input_video_type);
        input_video = findViewById(R.id.input_video);
        input_external_link = findViewById(R.id.input_external_link);
        input_attachment = findViewById(R.id.input_attachment);

        input_video_layout = findViewById(R.id.input_video_layout);
        input_video_2_layout = findViewById(R.id.input_video_2_layout);
        input_video_2 = findViewById(R.id.input_video_2);

        input_video_type.setText("server");
        input_video_2.setHint("Select Video");

        input_video_layout.setVisibility(View.GONE);
        input_video_2_layout.setVisibility(View.VISIBLE);

        input_video_2.setOnClickListener(this);


        btn_update_course_lesson.setOnClickListener(this);
        input_video_type.setOnClickListener(this);
        input_attachment.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {

        if(view.getId() == R.id.btn_update_course_lesson){

            title = input_title.getText().toString().trim();
            content = input_content.getText().toString().trim();
            type = input_video_type.getText().toString().trim();
            video = input_video.getText().toString().trim();
            extlink = input_external_link.getText().toString().trim();
            attachment = input_attachment.getText().toString().trim();

            if (title.isEmpty()){

                utilities.dialogError(this,getString(R.string.non_empty_title));

            }else{

                updateLesson();

            }

        }else if(view.getId() == R.id.input_video_type){

            CharSequence options_image[] = new CharSequence[]{
                    "server",
                    "youtube",
                    "vimeo",
                    "Cancel"
            };
            AlertDialog.Builder builderoptions_image = new AlertDialog.Builder(this);
            builderoptions_image.setTitle("Select Video Type");

            builderoptions_image.setItems(options_image, new DialogInterface.OnClickListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    if(which == 0){

                        input_video_type.setText("server");
                        input_video_2.setHint("Select Video");

                        input_video_layout.setVisibility(View.GONE);
                        input_video_2_layout.setVisibility(View.VISIBLE);

                        dialog.dismiss();

                    }else if (which == 1) {

                        input_video_type.setText("youtube");
                        input_video.setHint("Enter YouTube video link/url");

                        input_video_layout.setVisibility(View.VISIBLE);
                        input_video_2_layout.setVisibility(View.GONE);

                        dialog.dismiss();

                    } else if (which == 2) {

                        input_video_type.setText("vimeo");
                        input_video.setHint("Enter Vimeo video ID");

                        input_video_layout.setVisibility(View.VISIBLE);
                        input_video_2_layout.setVisibility(View.GONE);

                        dialog.dismiss();

                    }
                }
            });

            builderoptions_image.show();

        }else if(view.getId() == R.id.input_video_2){

            requestPermissionsFromUser("video");

        }else if(view.getId() == R.id.input_attachment){

            requestPermissionsFromUser("attachment");
        }
    }

    private void requestPermissionsFromUser(String type) {

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

            if(type.equals("video")) {
                //Permission Granted, lets go pick photo
                selectVideoFromGallery();

            }else {

                selectAttachmentFromGallery();
            }
        }
    }

    private void selectVideoFromGallery() {

        Intent gallery_intent = new Intent();
        gallery_intent.setAction(Intent.ACTION_GET_CONTENT);
        gallery_intent.setType("video/*");
        startActivityForResult(Intent.createChooser(gallery_intent, "Select video"), video_gallery_Pic);
    }

    private void selectAttachmentFromGallery() {

        Intent gallery_intent = new Intent();
        gallery_intent.setAction(Intent.ACTION_GET_CONTENT);
        gallery_intent.setType("*/*");
        startActivityForResult(Intent.createChooser(gallery_intent, "Select a file"), attachment_gallery_Pic);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == video_gallery_Pic && resultCode == RESULT_OK &&
                data != null) {

            videoUri = data.getData();
            videoFilePath = FileUtils.getPath(this,videoUri);//assign it to a string(your choice).

            input_video.setText(videoFilePath);
            input_video_2.setText(videoFilePath);

        }else if(requestCode == attachment_gallery_Pic && resultCode == RESULT_OK
                && data != null){

            attachmentUri = data.getData();
            attachmentFilePath = FileUtils.getPath(this,attachmentUri);//assign it to a string(your choice).

            input_attachment.setText(attachmentFilePath);
        }
    }

    private void updateLesson() {

        dialog.show();

        isExecuted = true;

        String courseID = getIntent().getStringExtra("courseid");

        isVideoEmpty = videoUri != null ? "no" : "yes";
        isAttachmentEmpty = attachmentUri != null ? "no" : "yes";

        communicator.updateLesson(courseID,title,content,type,video,extlink,attachment,"addlesson",
                isVideoEmpty,isAttachmentEmpty,videoUri, attachmentUri,AddCourseLesson.this,
                videoFilePath,attachmentFilePath);
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

                    input_video.setText("");
                    input_video_2.setText("");
                    input_attachment.setText("");
                    input_content.setText("");
                    input_title.setText("");
                    input_external_link.setText("");
                    videoFilePath = null;
                    videoUri = null;

                    utilities.dialogError(this, getString((R.string.upload_successful)));

                } else {

                    //show login error dialog
                    utilities.dialogError(this, getString(R.string.error_occurred));
                }
            } else {

                //show login error dialog
                utilities.dialogError(this, getString(R.string.error_occurred));
            }

            //dismiss dialog loader
            dialog.dismiss();

        }
    }

    @Subscribe
    public void onErrorEvent(ErrorEvent errorEvent){

        Log.e("UpdateLesson: ",errorEvent.getErrorMsg());

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