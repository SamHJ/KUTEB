package com.naijaunik.kuteb.Activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonArray;
import com.naijaunik.kuteb.Api.BusProvider;
import com.naijaunik.kuteb.Api.Communicator;
import com.naijaunik.kuteb.Api.ErrorEvent;
import com.naijaunik.kuteb.Api.ServerEvent;
import com.naijaunik.kuteb.Api.ServerResponse;
import com.naijaunik.kuteb.R;
import com.naijaunik.kuteb.Utils.AppSession;
import com.naijaunik.kuteb.Utils.Utilities;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import static com.naijaunik.kuteb.Utils.AppConstants.AUDIO_GALLERY_PICK;
import static com.naijaunik.kuteb.Utils.AppConstants.DOCUMENT_GALLERY_PICK;
import static com.naijaunik.kuteb.Utils.AppConstants.IMAGE_GALLERY_PICK;
import static com.naijaunik.kuteb.Utils.AppConstants.REQUEST_STORAGE_PERMISSION;
import static com.naijaunik.kuteb.Utils.AppConstants.VIDEO_GALLERY_PICK;

public class UpdateExamQuestion extends AppCompatActivity implements View.OnClickListener {

    private Communicator communicator;
    private Utilities utilities;
    private Toolbar toolbar;

    private TextInputEditText question_subject,question,question_file,question_file_type,question_difficulty,
            correct_answer,question_option;
    private boolean isSubjectWise;
    private String fileType;
    private Uri fileUri;
    private String filePath;
    private AppSession appSession;
    private TextInputLayout subject_input_layout;
    private ChipGroup chip_group;
    private MaterialButton btn_add_question;

    ArrayList<String> questionOptions;

    String examOptions;
    private String subject,examQuestion,correctAnswer,questionDifficulty;
    private ProgressDialog dialog;
    private boolean isExecuted;
    private ServerResponse serverResponse;
    
    private String qOptions;

    private TextInputLayout question_file_2_layout,question_file_layout;
    private TextInputEditText question_file_2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_exam_question);

        utilities = Utilities.getInstance(this);
        appSession = AppSession.getInstance(this);
        communicator = new Communicator();

        isSubjectWise = getIntent().getStringExtra("is_subject_wise").equals("1");

        dialog = utilities.showLoadingDialog("Updating Question","a moment please...");

        qOptions = getIntent().getStringExtra("question_options") ;

        fileType = getIntent().getStringExtra("question_file_type");

        //set initial options syntax
        questionOptions = new ArrayList<>(Arrays.asList(qOptions.split("\\s*,\\s*")));

        //set initial exam options value
        examOptions = android.text.TextUtils.join(",", questionOptions);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Update Question");

        initFields();
    }



    @SuppressLint("SetTextI18n")
    private void initFields() {

        question_subject = findViewById(R.id.question_subject);
        question = findViewById(R.id.question);
        question_file = findViewById(R.id.question_file);
        question_file_type = findViewById(R.id.question_file_type);
        subject_input_layout = findViewById(R.id.subject_input_layout);
        question_difficulty = findViewById(R.id.question_difficulty);
        correct_answer = findViewById(R.id.correct_answer);
        chip_group = findViewById(R.id.chip_group);
        question_option = findViewById(R.id.question_option);
        btn_add_question = findViewById(R.id.btn_add_question);
        question_file_2_layout = findViewById(R.id.question_file_2_layout);
        question_file_layout = findViewById(R.id.question_file_layout);
        question_file_2 = findViewById(R.id.question_file_2);
        
        subject = getIntent().getStringExtra("subject");
        question_subject.setText(getIntent().getStringExtra("subject"));
        
        examQuestion = getIntent().getStringExtra("question");
        question.setText(getIntent().getStringExtra("question"));
        
        question_file.setText(getIntent().getStringExtra("question_file"));
        question_file_2.setText(getIntent().getStringExtra("question_file"));

        if(fileType.equals("audio")){

            question_file_type.setText("Audio");

            question_file_2.setHint("Select Audio");

            question_file_2_layout.setVisibility(View.VISIBLE);
            question_file_layout.setVisibility(View.GONE);


        }else if(fileType.equals("document")){

            question_file_type.setText("Document (PDF)");

            question_file_2.setHint("Select Document");

            question_file_2_layout.setVisibility(View.VISIBLE);
            question_file_layout.setVisibility(View.GONE);

        }else if(fileType.equals("image")){

            question_file_type.setText("Image");

            question_file_2.setHint("Select Image");

            question_file_2_layout.setVisibility(View.VISIBLE);
            question_file_layout.setVisibility(View.GONE);

        }else if(fileType.equals("servervideo")){

            question_file_type.setText("Video");

            question_file_2.setHint("Select Video");

            question_file_2_layout.setVisibility(View.VISIBLE);
            question_file_layout.setVisibility(View.GONE);

        }else if(fileType.equals("youtube")){

            question_file_type.setText("Video");

            question_file.setHint("Enter YouTube video link/url");

            question_file_2_layout.setVisibility(View.GONE);
            question_file_layout.setVisibility(View.VISIBLE);

        }else{

            question_file_type.setText("Video");

            question_file.setHint("Enter Vimeo video ID");

            question_file_2_layout.setVisibility(View.GONE);
            question_file_layout.setVisibility(View.VISIBLE);

        }

        
        questionDifficulty = getIntent().getStringExtra("question_difficulty");
        question_difficulty.setText(getIntent().getStringExtra("question_difficulty"));
        
        correct_answer.setText(getIntent().getStringExtra("correct_answer"));
        correctAnswer = getIntent().getStringExtra("correct_answer");

        //set initial options syntax
        renderChip();

        question_option.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

                if (count > 0) {
                    String s1 = "" + charSequence.charAt(start);
                    if (s1.equals(",")) {
                        String option = charSequence.toString().substring(0, charSequence.length() - 1);

                        questionOptions.add(option);
                        //reset chip_group views
                        chip_group.removeAllViews();
                        //re-render chips
                        renderChip();
                        //reset option input field
                        question_option.setText("");

                        //convert array items to comma separated items
                        examOptions = android.text.TextUtils.join(",", questionOptions); // a,b


                        return;
                    }
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        if(isSubjectWise){
            subject_input_layout.setVisibility(View.VISIBLE);
        }

        question_file_type.setOnClickListener(this);
        question_subject.setOnClickListener(this);
        question_file_2.setOnClickListener(this);
        question_difficulty.setOnClickListener(this);
        btn_add_question.setOnClickListener(this);
    }

    private void renderChip() {

        for(final String genre : questionOptions) {

            final Chip chip = new Chip(this);

            chip.setText(genre);

            chip.isCloseIconVisible();

            chip.setCloseIconVisible(true);


            chip.setOnCloseIconClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Handle the click on the close icon.

                    chip_group.removeView(view);
                    questionOptions.remove(genre);

                    //update the examoptions string to the new value after removal of an option from the list of options
                    examOptions = android.text.TextUtils.join(",", questionOptions);

                }
            });

            chip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //copy text to clip board
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("Copied Text", genre);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(UpdateExamQuestion.this, "Copied!", Toast.LENGTH_SHORT).show();
                }
            });


            chip_group.addView(chip);

        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){

            case R.id.question_file_2:

                    requestPermissionsFromUser();

                break;

            case R.id.question_file_type:
                //show question file/attachment options
                showQuestionFileOptions();
                break;

            case R.id.question_subject:
                //show subject selection options
                showSubjectSelectOptions();
                break;

            case R.id.question_difficulty:
                //show question difficulty level options
                showSelectQuestionDifficulty();
                break;

            case R.id.btn_add_question:
                validateAndUploadExamQuestion();
                break;
        }

    }

    private void validateAndUploadExamQuestion() {

        examQuestion = question.getText().toString().trim();
        correctAnswer = correct_answer.getText().toString().trim();
        questionDifficulty = question_difficulty.getText().toString().trim();

        if(isSubjectWise){
            //validate based on subject wise

            subject = question_subject.getText().toString().trim();


            if(subject.isEmpty() || examQuestion.isEmpty() || examOptions.isEmpty() || correctAnswer.isEmpty() ||
                    questionDifficulty.isEmpty()){

                utilities.dialogError(this,getResources().getString(R.string.all_fields_required));

            }else{

                isExecuted = true;

                //upload question

                dialog.show();

                if(fileUri != null){

                    communicator.uploadUpdateExamQuestion("update_question","yes",
                            subject,examQuestion,filePath,fileUri,fileType,
                            examOptions,correctAnswer,questionDifficulty,this,
                            getIntent().getStringExtra("question_id"),question_file.getText().toString().trim());
                }else{

                    communicator.uploadUpdateExamQuestion("update_question","no",
                            subject,examQuestion,null,null,fileType,
                            examOptions,correctAnswer,questionDifficulty,this,
                            getIntent().getStringExtra("question_id"),question_file.getText().toString().trim());
                }
            }

        }else{
            //validate based on non-subject wise

            if(examQuestion.isEmpty() || examOptions.isEmpty() || correctAnswer.isEmpty() ||
                    questionDifficulty.isEmpty()){

                utilities.dialogError(this,getResources().getString(R.string.all_fields_required));

            }else{

                isExecuted = true;

                //upload question

                dialog.show();

                if(fileUri != null){

                    communicator.uploadUpdateExamQuestion("update_question","yes","",
                            examQuestion,filePath,fileUri,fileType,
                            examOptions,correctAnswer,questionDifficulty,this,
                            getIntent().getStringExtra("question_id"),question_file.getText().toString().trim());

                }else{

                    communicator.uploadUpdateExamQuestion("update_question","no","",
                            examQuestion,null,null,fileType,
                            examOptions,correctAnswer,questionDifficulty,this,
                            getIntent().getStringExtra("question_id"),question_file.getText().toString().trim());
                }
            }

        }
    }

    private void showSelectQuestionDifficulty() {

        //show difficulty level options

        String questionDifficulties = utilities.cleanData(appSession.getAdminSettings().get("question_difficulties"));

        final String [] difficulty_options = questionDifficulties.split("\\s*,\\s*");


        AlertDialog.Builder difficulty_dialog = new AlertDialog.Builder(UpdateExamQuestion.this);
        difficulty_dialog.setTitle("Difficulty Level Options");

        difficulty_dialog.setItems(difficulty_options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which_free_sub) {

                //get selected subject
                String difficultySelected = difficulty_options[which_free_sub];

                question_difficulty.setText(difficultySelected);

            }

        });

        difficulty_dialog.show();
    }

    private void showSubjectSelectOptions() {

        //show select dialog of all the current plans

        JsonArray plansObj = appSession.getCourses();

        ArrayList<String> stringArrayList = new ArrayList<String>();

        for (int i = 0; i<plansObj.size(); i++) {

            String subjectName = Utilities.cleanData(plansObj.get(i).getAsJsonObject().get("title"));

            stringArrayList.add(subjectName);

        }

        final String [] options_plans = stringArrayList.toArray(new String[stringArrayList.size()]);


        AlertDialog.Builder subjects_dialog = new AlertDialog.Builder(UpdateExamQuestion.this);
        subjects_dialog.setTitle("Subject Options");

        subjects_dialog.setItems(options_plans, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which_free_sub) {

                //get selected subject
                String subjectSelected = options_plans[which_free_sub];

                question_subject.setText(subjectSelected);

            }

        });

        subjects_dialog.show();
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

            if(fileType.equals("image")) {
                //Permission Granted, lets go pick photo
                selectImageFromGallery();

            }else if(fileType.equals("audio")){

                selectAudioFromGallery();

            }else if(fileType.equals("document")){

                selectDocumentFromGallery();

            }else if(fileType.equals("servervideo")){

                selectVideoFromGallery();

            }
        }
    }

    private void selectVideoFromGallery() {

        Intent gallery_intent = new Intent();
        gallery_intent.setAction(Intent.ACTION_GET_CONTENT);
        gallery_intent.setType("video/*");
        startActivityForResult(Intent.createChooser(gallery_intent, "Select video"), VIDEO_GALLERY_PICK);
    }

    private void selectDocumentFromGallery() {

        Intent gallery_intent = new Intent();
        gallery_intent.setAction(Intent.ACTION_GET_CONTENT);
        gallery_intent.setType("application/pdf");
        startActivityForResult(Intent.createChooser(gallery_intent, "Select a document (PDF)"), DOCUMENT_GALLERY_PICK);

    }

    private void selectAudioFromGallery() {

        Intent gallery_intent = new Intent();
        gallery_intent.setAction(Intent.ACTION_GET_CONTENT);
        gallery_intent.setType("audio/*");
        startActivityForResult(Intent.createChooser(gallery_intent, "Select audio"), AUDIO_GALLERY_PICK);
    }

    private void selectImageFromGallery() {

        Intent gallery_intent = new Intent();
        gallery_intent.setAction(Intent.ACTION_GET_CONTENT);
        gallery_intent.setType("image/*");
        startActivityForResult(Intent.createChooser(gallery_intent, "Select image"), IMAGE_GALLERY_PICK);

    }

    private void showQuestionFileOptions() {

        //show options
        CharSequence options_image[] = new CharSequence[]{
                "Image",
                "Audio",
                "Document (PDF)",
                "Video",
                "Cancel"
        };
        AlertDialog.Builder builderoptions_image = new AlertDialog.Builder(UpdateExamQuestion.this);
        builderoptions_image.setTitle("Attachment Options");

        builderoptions_image.setItems(options_image, new DialogInterface.OnClickListener() {

            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(which == 0){

                    //select image

                    question_file_2.setHint("Select Image");
                    question_file_2.setText("");

                    question_file_2_layout.setVisibility(View.VISIBLE);
                    question_file_layout.setVisibility(View.GONE);

                    question_file_type.setText("Image");

                    fileType = "image";

                }else if (which == 1) {

                    //select audio

                    question_file_2.setHint("Select Audio");
                    question_file_2.setText("");

                    question_file_2_layout.setVisibility(View.VISIBLE);
                    question_file_layout.setVisibility(View.GONE);

                    question_file_type.setText("Audio");

                    fileType = "audio";

                } else if (which == 2) {

                    //select document

                    question_file_2.setHint("Select Document");
                    question_file_2.setText("");

                    question_file_2_layout.setVisibility(View.VISIBLE);
                    question_file_layout.setVisibility(View.GONE);

                    question_file_type.setText("Document (PDF)");

                    fileType = "document";

                }else if(which == 3){

                    dialog.dismiss();

                    //select video
                    //show options
                    CharSequence options_video[] = new CharSequence[]{
                            "Server",
                            "YouTube",
                            "Vimeo",
                            "Cancel"
                    };
                    AlertDialog.Builder videoBuilderOptions = new AlertDialog.Builder(UpdateExamQuestion.this);
                    videoBuilderOptions.setTitle("Video Options");

                    videoBuilderOptions.setItems(options_video, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            if(which == 0){

                                //select image

                                question_file_2.setHint("Select Video");
                                question_file_2.setText("");

                                question_file_2_layout.setVisibility(View.VISIBLE);
                                question_file_layout.setVisibility(View.GONE);

                                question_file_type.setText("Video");

                                fileType = "servervideo";

                            }else if (which == 1) {

                                //select audio

                                question_file.setHint("Enter YouTube video link/url");
                                question_file.setText("");

                                question_file_2_layout.setVisibility(View.GONE);
                                question_file_layout.setVisibility(View.VISIBLE);

                                question_file_type.setText("Video");

                                fileType = "youtube";

                            } else if (which == 2) {

                                //select document

                                question_file.setHint("Enter Vimeo video ID");
                                question_file.setText("");

                                question_file_2_layout.setVisibility(View.GONE);
                                question_file_layout.setVisibility(View.VISIBLE);

                                question_file_type.setText("Video");

                                fileType = "vimeo";

                            }
                        }
                    });

                    videoBuilderOptions.show();
                }
            }
        });

        builderoptions_image.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == VIDEO_GALLERY_PICK && resultCode == RESULT_OK &&
                data != null) {

            fileUri = data.getData();
            filePath = utilities.getAbsolutePath(fileUri);

            question_file.setText(filePath);

            question_file_2.setText(filePath);

        }else if(requestCode == IMAGE_GALLERY_PICK && resultCode == RESULT_OK
                && data != null){

            fileUri = data.getData();
            filePath = utilities.getAbsolutePath(fileUri);

            question_file.setText(filePath);

            question_file_2.setText(filePath);

        }else if(requestCode == AUDIO_GALLERY_PICK && resultCode == RESULT_OK
                && data != null){

            fileUri = data.getData();
            filePath = utilities.getAbsolutePath(fileUri);

            question_file.setText(filePath);

            question_file_2.setText(filePath);

        }else if(requestCode == DOCUMENT_GALLERY_PICK && resultCode == RESULT_OK
                && data != null){

            fileUri = data.getData();
            filePath = utilities.getAbsolutePath(fileUri);

            question_file.setText(filePath);

            question_file_2.setText(filePath);

        }
    }

    @Subscribe
    public void onServerEvent(ServerEvent serverEvent){

        if(isExecuted) {

            serverResponse = serverEvent.getServerResponse();

            if (serverResponse != null) {

                if (serverResponse.getResult().equals("success")) {


                    utilities.dialogError(this, getString((R.string.update_successful)));

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

        Log.e("UpdateExamQuestion: ",errorEvent.getErrorMsg());

        dialog.dismiss();

        if(isExecuted){

            utilities.dialogError(this,getString(R.string.error_occurred));
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
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