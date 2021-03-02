package com.naijaunik.kuteb.Activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ct7ct7ct7.androidvimeoplayer.listeners.VimeoPlayerErrorListener;
import com.ct7ct7ct7.androidvimeoplayer.listeners.VimeoPlayerStateListener;
import com.ct7ct7ct7.androidvimeoplayer.view.VimeoPlayerView;
import com.example.jean.jcplayer.model.JcAudio;
import com.example.jean.jcplayer.view.JcPlayerView;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.naijaunik.kuteb.Api.BusProvider;
import com.naijaunik.kuteb.Api.Communicator;
import com.naijaunik.kuteb.Api.ErrorEvent;
import com.naijaunik.kuteb.Api.ServerEvent;
import com.naijaunik.kuteb.Api.ServerResponse;
import com.naijaunik.kuteb.Model.ExamQuestionsModel;
import com.naijaunik.kuteb.R;
import com.naijaunik.kuteb.Utils.AppSession;
import com.naijaunik.kuteb.Utils.Utilities;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import tcking.github.com.giraffeplayer2.GiraffePlayer;
import tcking.github.com.giraffeplayer2.PlayerListener;
import tcking.github.com.giraffeplayer2.VideoInfo;
import tcking.github.com.giraffeplayer2.VideoView;
import tv.danmaku.ijk.media.player.IjkTimedText;

public class ViewAndTakeExam extends AppCompatActivity {

    private Utilities utilities;
    private Communicator communicator;
    private Toolbar toolbar;
    private JsonObject userObj;
    private AppSession appSession;
    private ShimmerFrameLayout shimmer_view_container;
    private LinearLayout no_data_found_layout,error_layout,difficulty_level_selection_layout,
            question_layout,difficulty_button_layout,single_question_layout,result_layout,
            select_course_layout;

    private ArrayList<ExamQuestionsModel> examsList, selectedLevelListItems;
    private String examId;
    private ServerResponse serverResponse;
    private ProgressDialog dialog;
    private List<String> difficultyList;

    private RelativeLayout question_navigation_container;
    private MaterialButton previous_btn,next_btn,restart_exam;
    private int questionCounter = 0;
    private int totalLevelQuestions;
    private boolean isAdmin;
    private ArrayList<String> questionOptions;
    private boolean isAnswered = false;
    private int point = 0;
    private TextView exam_result;
    private ImageView result_img;
    private RelativeLayout container;
    private MediaPlayer mp;
    private boolean isSubjectWise;

    private List<String> subjectsList;
    private LinearLayout subjects_button_layout;
    private String currentSubject;
    private boolean isRestart = false;

    private ProgressBar question_loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_and_take_exam);

        utilities = Utilities.getInstance(this);
        appSession = AppSession.getInstance(this);
        communicator = new Communicator();

        examId  = getIntent().getStringExtra("id");

        userObj = appSession.getUser();

        isAdmin = utilities.cleanData(userObj.get("status")).equals("admin");

        isSubjectWise = getIntent().getStringExtra("is_subject_wise").equals("1");

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getIntent().getStringExtra("exam_title") +" Exam");

        initFields();
    }

    private void initFields() {

        shimmer_view_container = findViewById(R.id.shimmer_view_container);
        no_data_found_layout = findViewById(R.id.no_data_found_layout);
        error_layout = findViewById(R.id.error_layout);
        difficulty_level_selection_layout = findViewById(R.id.difficulty_level_selection_layout);
        question_layout = findViewById(R.id.question_layout);
        difficulty_button_layout = findViewById(R.id.difficulty_button_layout);
        single_question_layout = findViewById(R.id.single_question_layout);
        question_navigation_container = findViewById(R.id.question_navigation_container);
        previous_btn = findViewById(R.id.previous_btn);
        next_btn = findViewById(R.id.next_btn);
        result_layout = findViewById(R.id.result_layout);
        restart_exam = findViewById(R.id.restart_exam);
        exam_result = findViewById(R.id.exam_result);
        result_img = findViewById(R.id.result_img);
        container = findViewById(R.id.container);
        select_course_layout = findViewById(R.id.select_course_layout);
        subjects_button_layout = findViewById(R.id.subjects_button_layout);
        question_loader = findViewById(R.id.question_loader);

        difficulty_button_layout.setVisibility(View.GONE);
        subjects_button_layout.setVisibility(View.GONE);

        dialog = utilities.showLoadingDialog("Deleting question","a moment please...");

        examsList = new ArrayList<>();
        selectedLevelListItems = new ArrayList<>();
        difficultyList = new ArrayList<>();
        questionOptions = new ArrayList<>();
        subjectsList = new ArrayList<>();

//        showExamQuestions(examsQuestionsData);
        getExamQuestions();

        restart_exam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                isRestart = true;
                point = 0;
                questionCounter = 0;

                question_loader.setVisibility(View.VISIBLE);

                if(isSubjectWise){

                    difficulty_level_selection_layout.setVisibility(View.GONE);
                    result_layout.setVisibility(View.GONE);
                    question_layout.setVisibility(View.GONE);

                    select_course_layout.setVisibility(View.VISIBLE);

//                    showExamQuestions(examsQuestionsData);
                    getExamQuestions();

                }else{

                    result_layout.setVisibility(View.GONE);
                    question_layout.setVisibility(View.GONE);
                    select_course_layout.setVisibility(View.GONE);
                    difficulty_level_selection_layout.setVisibility(View.VISIBLE);

//                    showExamQuestions(examsQuestionsData);
                    getExamQuestions();

                }
            }
        });

    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_and_add_menu, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        MenuItem addExamQuestion = menu.findItem(R.id.action_add);

        if(utilities.cleanData(userObj.get("status")).equals("admin")){

            addExamQuestion.setVisible(true);
            addExamQuestion.setTitle("Add Exam Question");
        }

        item.setVisible(false); //hide searchview for now


        return  super.onCreateOptionsMenu(menu);

    }

    @SuppressLint("NewApi")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
            overridePendingTransition(R.anim.left_in, R.anim.right_out);

        }else if(item.getItemId() == R.id.action_add){
            Intent intent = new Intent(ViewAndTakeExam.this, AddExamQuestion.class);
            intent.putExtra("examid",examId);
            intent.putExtra("is_subject_wise",getIntent().getStringExtra("is_subject_wise"));
            startActivity(intent);
            overridePendingTransition(R.anim.right_in, R.anim.left_out);

        }

        return super.onOptionsItemSelected(item);
    }

    @Subscribe
    public void onServerEvent(ServerEvent serverEvent){

        serverResponse = serverEvent.getServerResponse();

        if(serverResponse != null) {

          if (serverResponse.getType().equals("get_exam_questions")) {

                if (serverResponse.getResult().equals("success")) {

                    JsonArray examsQuestionsData = serverResponse.getData();

                    if (examsQuestionsData != null) {

                        appSession.storeExamQuestions(examsQuestionsData);

                        showExamQuestions(examsQuestionsData);


                    }
//                    else if (appSession.getExamQuestions() == null) {
                    else {

                        error_layout.setVisibility(View.GONE);
                        no_data_found_layout.setVisibility(View.VISIBLE);
                    }

                } else {

//                    if (appSession.getExamQuestions() != null) {
//
//
//                        showExamQuestions();
//
//                    } else {

                        //show error layout
                        error_layout.setVisibility(View.VISIBLE);
                        no_data_found_layout.setVisibility(View.GONE);

//                    }
                }

            }else if(serverResponse.getType().equals("delete_exam_question")){

                if (serverResponse.getResult().equals("success")) {

                    utilities.dialogError(this, getString(R.string.exam_question_deleted_successfully));

                    shimmer_view_container.setVisibility(View.VISIBLE);
                    difficulty_level_selection_layout.setVisibility(View.GONE);
                    question_layout.setVisibility(View.GONE);
                    select_course_layout.setVisibility(View.GONE);

                    getExamQuestions();

                } else {

                    utilities.dialogError(this, getString(R.string.error_occurred));
                }

                //dismiss dialog loader
                dialog.dismiss();

            }

        }

    }

    @Subscribe
    public void onErrorEvent(ErrorEvent errorEvent){

        Log.e("ViewAndTakeExam: ",errorEvent.getErrorMsg());


        shimmer_view_container.setVisibility(View.GONE);

//        try {
//
//            if(appSession.getExamQuestions() != null){
//
//                showExamQuestions(examsQuestionsData);
//
//            }else{

                //show error layout
                error_layout.setVisibility(View.VISIBLE);
                no_data_found_layout.setVisibility(View.GONE);

//            }
//        }catch (Exception e){
//
//            e.printStackTrace();
//        }

    }

    private void showExamQuestions(JsonArray examsQuestionsData) {

        shimmer_view_container.setVisibility(View.GONE);

        if(!isRestart) {

            //clear book array list to avoid repeatition of data
            examsList.clear();
            difficultyList.clear();

            try {

                for (JsonElement element : examsQuestionsData) {
                    String id = Utilities.cleanData(((JsonObject) element).get("id"));
                    String exam_id = Utilities.cleanData(((JsonObject) element).get("exam_id"));
                    String subject = Utilities.cleanData(((JsonObject) element).get("subject"));
                    String question = Utilities.cleanData(((JsonObject) element).get("question"));
                    String question_file = Utilities.cleanData(((JsonObject) element).get("question_file"));
                    String question_file_type = Utilities.cleanData(((JsonObject) element).get("question_file_type"));
                    String question_option = Utilities.cleanData(((JsonObject) element).get("question_options"));
                    String correct_answer = Utilities.cleanData(((JsonObject) element).get("correct_answer"));
                    String question_difficulty = Utilities.cleanData(((JsonObject) element).get("question_difficulty")).trim();


                    //add data to list
                    examsList.add(new ExamQuestionsModel(id, exam_id, subject, question, question_file, question_file_type,
                            question_option, correct_answer, question_difficulty));

                    //add difficulty to difficulty list

                    if (!difficultyList.contains(question_difficulty)) {

                        difficultyList.add(question_difficulty);

                    }

                    //add subjects to list
                    if (!subjectsList.contains(subject)) {

                        subjectsList.add(subject);

                    }

                    questionOptions = new ArrayList<>(Arrays.asList(question_option.split("\\s*,\\s*")));

                }
                if (examsList.size() == 0) {

                    error_layout.setVisibility(View.GONE);
                    no_data_found_layout.setVisibility(View.VISIBLE);
                    question_layout.setVisibility(View.GONE);
                    difficulty_level_selection_layout.setVisibility(View.GONE);
                    question_loader.setVisibility(View.GONE);

                } else {

                    // check if the exam is subject wise and show the respective layout first
                    if (isSubjectWise) {
                        //show course selection
                        select_course_layout.setVisibility(View.VISIBLE);

                        //render subject selection
                        renderSubjectSelection(subjectsList);

                    } else {

                        //show difficulty level selection view and hide other views
                        difficulty_level_selection_layout.setVisibility(View.VISIBLE);

                        //render difficulty level buttons
                        renderDifficultyLevelButtons(difficultyList);
                    }

                    error_layout.setVisibility(View.GONE);
                    no_data_found_layout.setVisibility(View.GONE);
                    question_layout.setVisibility(View.GONE);

                }


            } catch (Exception e) {

                e.printStackTrace();

                question_loader.setVisibility(View.GONE);
                no_data_found_layout.setVisibility(View.VISIBLE);
                error_layout.setVisibility(View.GONE);

            }

        }else{

            difficulty_button_layout.setVisibility(View.VISIBLE);
            question_loader.setVisibility(View.GONE);

        }

    }

    private void renderSubjectSelection(final List<String> subjectsList) {

        MaterialButton[] diffButton = new MaterialButton[subjectsList.size()];

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);

        params.setMargins(20, 10, 20, 10); //substitute parameters for left, top, right, bottom

        subjects_button_layout.removeAllViews();

        for (int i = 0; i < subjectsList.size(); i++){

            diffButton[i] = new MaterialButton(this); //initialize the button here
            diffButton[i].setText(subjectsList.get(i));
            diffButton[i].setTextColor(getResources().getColor(R.color.white));
            diffButton[i].setAllCaps(false);
            diffButton[i].setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            diffButton[i].setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
            diffButton[i].setLayoutParams(params);

            final int finalI = i;

            diffButton[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    currentSubject = subjectsList.get(finalI);

                    select_course_layout.setVisibility(View.GONE);

                    //clear difficulty list
                    difficultyList.clear();

                    for (ExamQuestionsModel element : examsList) {

                        if(element.getSubject().equals(currentSubject)){

                            if(!difficultyList.contains(element.getQuestion_difficulty())){

                                difficultyList.add(element.getQuestion_difficulty());

                            }

                        }

                    }

                    if(difficultyList.size() != 0){

                        //render difficulty level buttons
                        renderDifficultyLevelButtons(difficultyList);

                        difficulty_level_selection_layout.setVisibility(View.VISIBLE);

                    }else{

                        difficulty_level_selection_layout.setVisibility(View.GONE);
                        error_layout.setVisibility(View.GONE);
                        question_layout.setVisibility(View.GONE);
                        result_layout.setVisibility(View.GONE);
                        difficulty_level_selection_layout.setVisibility(View.GONE);
                        no_data_found_layout.setVisibility(View.VISIBLE);

                    }

                }
            });

            subjects_button_layout.addView(diffButton[i]);
        }

        subjects_button_layout.setVisibility(View.VISIBLE);

        question_loader.setVisibility(View.GONE);

    }

    private void renderDifficultyLevelButtons(final List<String> diffList) {

        MaterialButton[] diffButton = new MaterialButton[diffList.size()];

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);

        params.setMargins(20, 10, 20, 10); //substitute parameters for left, top, right, bottom


        difficulty_button_layout.removeAllViews();

        for (int i = 0; i < diffList.size(); i++){

            diffButton[i] = new MaterialButton(this); //initialize the button here
            diffButton[i].setText(diffList.get(i));
            diffButton[i].setTextColor(getResources().getColor(R.color.white));
            diffButton[i].setAllCaps(false);
            diffButton[i].setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            diffButton[i].setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
            diffButton[i].setLayoutParams(params);

            final int finalI = i;

            diffButton[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //filter questions based on the level selected

                    //but first clear the selectedLevelListItems to avoid duplication and to ensure the correct level question
                    //gets added correctly
                    selectedLevelListItems.clear();

                    for (ExamQuestionsModel element : examsList) {

                        if(currentSubject != null && !currentSubject.isEmpty()){

                            if (element.getQuestion_difficulty().equals(diffList.get(finalI)) &&
                            element.getSubject().equals(currentSubject)) {

                                selectedLevelListItems.add(element);

                            }

                        }else {

                            if (element.getQuestion_difficulty().equals(diffList.get(finalI))) {

                                selectedLevelListItems.add(element);

                            }

                        }

                    }

                    //start showing the questions

                    totalLevelQuestions = selectedLevelListItems.size();

                    difficulty_level_selection_layout.setVisibility(View.GONE);

                    difficulty_button_layout.setVisibility(View.GONE);//hide the difficulty level selection container

                    question_layout.setVisibility(View.VISIBLE); //show the questions layout container

                    renderQuestionItems();//render the question items
                }
            });

            difficulty_button_layout.addView(diffButton[i]);
        }

        difficulty_button_layout.setVisibility(View.VISIBLE);

        question_loader.setVisibility(View.GONE);

    }

    private void renderQuestionItems(){


        //render single question in single_question_layout

        inflateQuestion(questionCounter);

        question_navigation_container.setVisibility(View.VISIBLE);

        hideShowNavigators();

        previous_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                inflateQuestion(questionCounter - 1);

                questionCounter -= 1;//decrement the counter to current question position

                hideShowNavigators();//render the navigation buttons accordingly

            }
        });

        next_btn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {

               if(isAnswered){

                   //release the media player object
                   if(mp != null){

                       mp.release();
                   }

                   if(selectedLevelListItems.size() != 1){

                       inflateQuestion(questionCounter + 1);

                       questionCounter += 1;//increment the counter to current question position

                   }else{

                       //show user their result

                       //release the media player object
                       if(mp != null){

                           mp.release();
                       }

                       question_layout.setVisibility(View.GONE);
                       difficulty_level_selection_layout.setVisibility(View.GONE);
                       result_layout.setVisibility(View.VISIBLE);

                       container.setBackgroundColor(getResources().getColor(R.color.white));

                       int resultPercentage = (point * 100) / selectedLevelListItems.size();

                       exam_result.setText("Result: " + point + "/" + selectedLevelListItems.size() +
                               "\n \n Overall percentage performance: " + resultPercentage + "%");

                       if(resultPercentage < 10){

                           result_img.setImageDrawable(getResources().getDrawable(R.drawable.failed));

                           exam_result.setTextColor(getResources().getColor(R.color.red));

                       }else if(resultPercentage < 50 ){

                           result_img.setImageDrawable(getResources().getDrawable(R.drawable.thumb_ups));

                           exam_result.setTextColor(getResources().getColor(R.color.red));

                       }else if(resultPercentage < 70){

                           result_img.setImageDrawable(getResources().getDrawable(R.drawable.runner_up));

                           exam_result.setTextColor(getResources().getColor(R.color.orange));

                       }else{

                           result_img.setImageDrawable(getResources().getDrawable(R.drawable.exam_acer));

                           exam_result.setTextColor(getResources().getColor(R.color.gradient_start_color));

                       }

                   }

                   hideShowNavigators();//render the navigation buttons accordingly
               }

            }
        });

    }

    @SuppressLint("SetTextI18n")
    private void hideShowNavigators() {

        //show or hide the previous btn accordingly
        if(questionCounter == 0){

            previous_btn.setVisibility(View.GONE);

        }else{
            previous_btn.setVisibility(View.VISIBLE);
        }

        //show or hide the next btn accordingly
        if(questionCounter == selectedLevelListItems.size()-1){

            next_btn.setVisibility(View.VISIBLE);
            next_btn.setText("Finish");

            next_btn.setOnClickListener(null);

            next_btn.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("UseCompatLoadingForDrawables")
                @Override
                public void onClick(View view) {
                    //show user their result

                    //release the media player object
                    if(mp != null){

                        mp.release();
                    }

                    question_layout.setVisibility(View.GONE);
                    difficulty_level_selection_layout.setVisibility(View.GONE);
                    result_layout.setVisibility(View.VISIBLE);

                    container.setBackgroundColor(getResources().getColor(R.color.white));

                    int resultPercentage = (point * 100) / selectedLevelListItems.size();

                    exam_result.setText("Result: " + point + "/" + selectedLevelListItems.size() +
                            "\n \n Overall percentage performance: " + resultPercentage + "%");

                   if(resultPercentage < 10){

                       result_img.setImageDrawable(getResources().getDrawable(R.drawable.failed));

                       exam_result.setTextColor(getResources().getColor(R.color.red));

                   }else if(resultPercentage < 50 ){

                       result_img.setImageDrawable(getResources().getDrawable(R.drawable.thumb_ups));

                        exam_result.setTextColor(getResources().getColor(R.color.red));

                    }else if(resultPercentage < 70){

                       result_img.setImageDrawable(getResources().getDrawable(R.drawable.runner_up));

                        exam_result.setTextColor(getResources().getColor(R.color.orange));

                    }else{

                        result_img.setImageDrawable(getResources().getDrawable(R.drawable.exam_acer));

                        exam_result.setTextColor(getResources().getColor(R.color.gradient_start_color));

                    }
                }
            });

        }else{

            next_btn.setVisibility(View.VISIBLE);
        }

        previous_btn.setVisibility(View.GONE);

    }

    private void inflateQuestion(final int questionCounter){

        isAnswered = false;

        next_btn.setEnabled(false);
        next_btn.setClickable(false);
        next_btn.setPressed(true);
        next_btn.setBackgroundColor(getResources().getColor(R.color.MPD_pickerItemTextColorUnSelected));

       final ExamQuestionsModel questionItem = selectedLevelListItems.get(questionCounter);

       String fileType = questionItem.getQuestion_file_type();
       boolean fileIsNotEmpty = !questionItem.getQuestion_file().isEmpty();

        //first remove previous view(s) currently available
        single_question_layout.removeAllViews();

        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.single_question_template, single_question_layout);

        TextView question_text = itemView.findViewById(R.id.question_text);
        ImageView question_img = itemView.findViewById(R.id.question_img);
        ImageView youtube_video_image_placeholder = itemView.findViewById(R.id.youtube_video_image_placeholder);
        ImageView pdf_document_image_placeholder = itemView.findViewById(R.id.pdf_document_image_placeholder);
        VimeoPlayerView vimeoPlayer = itemView.findViewById(R.id.vimeoPlayer);
        VideoView video_view = itemView.findViewById(R.id.video_view);
        JcPlayerView audio_player_view = itemView.findViewById(R.id.audio_player_view);
        LinearLayout options_layout = itemView.findViewById(R.id.options_layout);
        MaterialButton btn_delete = itemView.findViewById(R.id.btn_delete);
        MaterialButton btn_update = itemView.findViewById(R.id.btn_update);

        if(isAdmin){

            btn_delete.setVisibility(View.VISIBLE);
            btn_update.setVisibility(View.VISIBLE);
        }

        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //take to question update page

                Intent intent = new Intent(ViewAndTakeExam.this, UpdateExamQuestion.class);
                intent.putExtra("question_id",questionItem.getId());
                intent.putExtra("subject",questionItem.getSubject());
                intent.putExtra("question",questionItem.getQuestion());
                intent.putExtra("question_file",questionItem.getQuestion_file());
                intent.putExtra("question_file_type",questionItem.getQuestion_file_type());
                intent.putExtra("question_options",questionItem.getQuestion_options());
                intent.putExtra("correct_answer",questionItem.getCorrect_answer());
                intent.putExtra("question_difficulty",questionItem.getQuestion_difficulty());
                intent.putExtra("is_subject_wise",getIntent().getStringExtra("is_subject_wise"));
                startActivity(intent);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
            }
        });

        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(ViewAndTakeExam.this);
                builder.setMessage("Are you sure you want to delete this question? This process cannot be reversed!");
                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        deleteExamQuestion(questionItem.getId());
                    }
                });
                final AlertDialog alertDialog = builder.create();

                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
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
        });

        question_text.setText(Html.fromHtml(questionItem.getQuestion()));

        if(fileType.equals("image") && fileIsNotEmpty){
            renderQuestionImage(question_img,questionItem);
        }
        if(fileType.equals("document") && fileIsNotEmpty){
            renderQuestionDocument(pdf_document_image_placeholder,questionItem);
        }
        if(fileType.equals("youtube") && fileIsNotEmpty){
            renderQuestionYouTube(youtube_video_image_placeholder,questionItem);
        }
        if(fileType.equals("vimeo") && fileIsNotEmpty){
            renderQuestionVimeo(vimeoPlayer,questionItem);
        }
        if(fileType.equals("servervideo") && fileIsNotEmpty){
            renderQuestionServerVideo(video_view,questionItem);
        }
        if(fileType.equals("audio") && fileIsNotEmpty){
            renderQuestionAudio(audio_player_view,questionItem);
        }

        //lets render question options

        final MaterialButton[] diffButton = new MaterialButton[questionOptions.size()];

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);

        params.setMargins(20, 10, 20, 10); //substitute parameters for left, top, right, bottom


        options_layout.removeAllViews();

        for (int i = 0; i < questionOptions.size(); i++) {

            diffButton[i] = new MaterialButton(this); //initialize the button here
            diffButton[i].setText(questionOptions.get(i));
            diffButton[i].setTextColor(getResources().getColor(R.color.white));
            diffButton[i].setAllCaps(false);
            diffButton[i].setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            diffButton[i].setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
            diffButton[i].setLayoutParams(params);

            final int finalI = i;

            diffButton[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    isAnswered = true;

                    next_btn.setEnabled(true);
                    next_btn.setClickable(true);
                    next_btn.setPressed(false);
                    next_btn.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));

                    //detect the correct answer and compare
                    //get first char in the option and compare with the right answer
                    String firstChar = String.valueOf(questionOptions.get(finalI).charAt(0)).trim().toLowerCase();

                    if(firstChar.equals(questionItem.getCorrect_answer().trim().toLowerCase())){

                        point += 1;

                        diffButton[finalI].setBackgroundColor(getResources().getColor(R.color.gradient_start_color));

                        //disable all the buttons
                        disableAllButtons(diffButton,questionOptions,finalI,getResources().getColor(R.color.gradient_start_color));

                        //play success sound
                        mp = MediaPlayer.create(ViewAndTakeExam.this, R.raw.success);
                        mp.start();

                    }else{

                        diffButton[finalI].setBackgroundColor(getResources().getColor(R.color.red));

                        //disable all the buttons
                        disableAllButtons(diffButton,questionOptions,finalI,getResources().getColor(R.color.red));

                        //play success sound
                        mp = MediaPlayer.create(ViewAndTakeExam.this, R.raw.failure);
                        mp.start();

                    }

                }
            });

            options_layout.addView(diffButton[i]);

        }

    }

    private void disableAllButtons(MaterialButton[] diffButton, ArrayList<String> questionOptions,
                                   int currentPosition, int color) {

        for (int i = 0; i < questionOptions.size(); i++) {

            diffButton[i].setEnabled(false);
            diffButton[i].setClickable(false);
            diffButton[i].setPressed(true);
            diffButton[i].setBackgroundColor(getResources().getColor(R.color.MPD_pickerItemTextColorUnSelected));
        }

        diffButton[currentPosition].setBackgroundColor(color);
    }

    private void renderQuestionAudio(JcPlayerView audio_player_view, ExamQuestionsModel questionItem) {

        ArrayList<JcAudio> jcAudios = new ArrayList<>();
        jcAudios.add(JcAudio.createFromURL(Objects.requireNonNull(getIntent().getStringExtra("exam_title")),
                questionItem.getQuestion_file()));
//             jcAudios.add(JcAudio.createFromAssets("Asset audio", "audio.mp3"));
//             jcAudios.add(JcAudio.createFromRaw("Raw audio", R.raw.audio));


        audio_player_view.initPlaylist(jcAudios, null);
        audio_player_view.createNotification(); // default icon
//             jcplayerView.createNotification(R.drawable.myIcon); // Your icon resource

        audio_player_view.setVisibility(View.VISIBLE);
    }

    private void renderQuestionServerVideo(VideoView video_view, ExamQuestionsModel questionItem) {

        video_view.getVideoInfo().setBgColor(Color.GRAY).setAspectRatio(VideoInfo.AR_MATCH_PARENT);//config player
        try{
            video_view.setVideoPath(questionItem.getQuestion_file()).getPlayer();

            video_view.setPlayerListener(new PlayerListener() {
                @Override
                public void onPrepared(GiraffePlayer giraffePlayer) {

                }

                @Override
                public void onBufferingUpdate(GiraffePlayer giraffePlayer, int percent) {

                }

                @Override
                public boolean onInfo(GiraffePlayer giraffePlayer, int what, int extra) {
                    return false;
                }

                @Override
                public void onCompletion(GiraffePlayer giraffePlayer) {

                }

                @Override
                public void onSeekComplete(GiraffePlayer giraffePlayer) {

                }

                @Override
                public boolean onError(GiraffePlayer giraffePlayer, int what, int extra) {
                    return false;
                }

                @Override
                public void onPause(GiraffePlayer giraffePlayer) {

                }

                @Override
                public void onRelease(GiraffePlayer giraffePlayer) {

                }

                @Override
                public void onStart(GiraffePlayer giraffePlayer) {

                }

                @Override
                public void onTargetStateChange(int oldState, int newState) {

                }

                @Override
                public void onCurrentStateChange(int oldState, int newState) {

                }

                @Override
                public void onDisplayModelChange(int oldModel, int newModel) {

                }

                @Override
                public void onPreparing(GiraffePlayer giraffePlayer) {

                }

                @Override
                public void onTimedText(GiraffePlayer giraffePlayer, IjkTimedText text) {

                }

                @Override
                public void onLazyLoadProgress(GiraffePlayer giraffePlayer, int progress) {

                }

                @Override
                public void onLazyLoadError(GiraffePlayer giraffePlayer, String message) {

                    utilities.dialogError(ViewAndTakeExam.this,message);
                }
            });


        }catch (Exception e){
            e.printStackTrace();
        }

        video_view.setVisibility(View.VISIBLE);
    }

    private void renderQuestionVimeo(VimeoPlayerView vimeoPlayer, ExamQuestionsModel questionItem) {

        getLifecycle().addObserver(vimeoPlayer);

        try {

            vimeoPlayer.initialize(Integer.parseInt(questionItem.getQuestion_file())); // samplevideoId is 458049458

        }catch (Exception e){

            e.printStackTrace();

            utilities.dialogError(this, e.getMessage());

        }

        vimeoPlayer.addStateListener(new VimeoPlayerStateListener() {
            @Override
            public void onLoaded(int videoId) {
            }

            @Override
            public void onPlaying(float duration) {
            }

            @Override
            public void onPaused(float seconds) {
            }

            @Override
            public void onEnded(float duration) {
            }

            @Override
            public void onInitFailed() {
                utilities.dialogError(ViewAndTakeExam.this,"Failed to initialize video player!");
            }
        });

        vimeoPlayer.addErrorListener(new VimeoPlayerErrorListener() {
            @Override
            public void onError(String message, String method, String name) {
                utilities.dialogError(ViewAndTakeExam.this,message);
            }
        });

        vimeoPlayer.setVisibility(View.VISIBLE);

    }

    private void renderQuestionYouTube(ImageView youtube_video_image_placeholder, final ExamQuestionsModel questionItem) {

        youtube_video_image_placeholder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity activity = (Activity) ViewAndTakeExam.this;
                Intent viewIntent = new Intent(ViewAndTakeExam.this, YoutubePlayerActivity.class);
                viewIntent.putExtra("videocode",questionItem.getQuestion_file());
                activity.startActivity(viewIntent);
                activity.overridePendingTransition(R.anim.right_in, R.anim.left_out);
            }
        });
        youtube_video_image_placeholder.setVisibility(View.VISIBLE);

    }

    private void renderQuestionDocument(ImageView pdf_document_image_placeholder, final ExamQuestionsModel questionItem) {

        pdf_document_image_placeholder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity activity = (Activity) ViewAndTakeExam.this;
                Intent viewIntent = new Intent(ViewAndTakeExam.this, WebViewer.class);
                viewIntent.putExtra("type","pdf");
                viewIntent.putExtra("pdf_url",questionItem.getQuestion_file());
                activity.startActivity(viewIntent);
                activity.overridePendingTransition(R.anim.right_in, R.anim.left_out);
            }
        });
        pdf_document_image_placeholder.setVisibility(View.VISIBLE);
    }

    private void renderQuestionImage(final ImageView question_img, final ExamQuestionsModel questionItem) {

        try{
            Picasso.get().load(questionItem.getQuestion_file()).networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.default_profile)
                    .error(R.drawable.default_profile).into(question_img,
                    new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(questionItem.getQuestion_file())
                                    .placeholder(R.drawable.default_profile)
                                    .error(R.drawable.default_profile).into(question_img);
                        }

                    });

            question_img.setVisibility(View.VISIBLE);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void getExamQuestions() {

        communicator.singleParametrizedCall(examId,"get_exam_questions");
    }

    private void deleteExamQuestion(String questionId){

        dialog.show();

        communicator.singleParametrizedCall(questionId,"delete_exam_question");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        try {
            BusProvider.getInstance().unregister(this);
        }catch (Exception e){
            e.printStackTrace();
        }

        //release the media player
        if(mp != null){

            mp.release();
        }

        finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }

    public void onResume(){
        super.onResume();
        shimmer_view_container.startShimmer();
        try {
            BusProvider.getInstance().register(this);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        shimmer_view_container.stopShimmer();
        try {
            BusProvider.getInstance().unregister(this);
        }catch (Exception e){
            e.printStackTrace();
        }

        //release the media player
        if(mp != null){

            mp.release();
        }
    }
}