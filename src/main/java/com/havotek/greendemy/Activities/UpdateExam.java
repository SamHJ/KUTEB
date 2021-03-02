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
import com.google.android.material.textfield.TextInputEditText;
import com.naijaunik.kuteb.Api.BusProvider;
import com.naijaunik.kuteb.Api.Communicator;
import com.naijaunik.kuteb.Api.ErrorEvent;
import com.naijaunik.kuteb.Api.ServerEvent;
import com.naijaunik.kuteb.Api.ServerResponse;
import com.naijaunik.kuteb.R;
import com.naijaunik.kuteb.Utils.Utilities;
import com.squareup.otto.Subscribe;

import java.util.Objects;

public class UpdateExam extends AppCompatActivity {

    private Toolbar toolbar;
    private ProgressDialog dialog;
    private Utilities utilities;
    private boolean isExecuted;
    private ServerResponse serverResponse;
    private Communicator communicator;
    private TextInputEditText input_exam_title,input_exam_subject_wise;
    private String examTitle,isExamSubjectWise;
    private MaterialButton btn_update_exam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_exam);

        utilities = Utilities.getInstance(this);
        communicator = new Communicator();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Update "+getIntent().getStringExtra("exam_title") +" Exam");

        initFields();
    }

    private void initFields() {

        dialog = utilities.showLoadingDialog("Updating Exam","a moment please...");

        input_exam_title = findViewById(R.id.input_exam_title);
        input_exam_subject_wise = findViewById(R.id.input_exam_subject_wise);
        btn_update_exam = findViewById(R.id.btn_update_exam);

        input_exam_title.setText(getIntent().getStringExtra("exam_title"));

        input_exam_subject_wise.setText(getIntent().getStringExtra("is_subject_wise").equals("1") ? "Yes" : "No");

        input_exam_subject_wise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                CharSequence options_image[] = new CharSequence[]{
                        "Yes",
                        "No",
                        "Cancel"
                };
                AlertDialog.Builder builderoptions_image = new AlertDialog.Builder(UpdateExam.this);
                builderoptions_image.setTitle("Is Exam Subject Wise?");

                builderoptions_image.setItems(options_image, new DialogInterface.OnClickListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if(which == 0){

                            input_exam_subject_wise.setText("Yes");
                            dialog.dismiss();

                        }else if (which == 1) {

                            input_exam_subject_wise.setText("No");
                            dialog.dismiss();

                        }
                    }
                });

                builderoptions_image.show();

            }
        });

        btn_update_exam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                examTitle = input_exam_title.getText().toString().trim();
                isExamSubjectWise = String.valueOf(input_exam_subject_wise.getText().toString().trim().equals("Yes") ? 1 : 0);

                if(examTitle.isEmpty()){

                    utilities.dialogError(UpdateExam.this,"Please enter exam title!");

                }else{

                    dialog.show();

                    isExecuted = true;

                    communicator.updateExam("update_exam",examTitle,isExamSubjectWise,
                            getIntent().getStringExtra("id"));
                }

            }
        });
    }

    @Subscribe
    public void onServerEvent(ServerEvent serverEvent){

        if(isExecuted) {

            serverResponse = serverEvent.getServerResponse();

            if (serverResponse != null) {

                if (serverResponse.getResult().equals("success")) {

                    utilities.dialogError(UpdateExam.this, getString((R.string.update_successful)));

                } else {

                    //show login error dialog
                    utilities.dialogError(UpdateExam.this, getString(R.string.error_occurred));
                }
            } else {

                //show login error dialog
                utilities.dialogError(UpdateExam.this, getString(R.string.error_occurred));
            }

        }

        //dismiss dialog loader
        dialog.dismiss();
    }

    @Subscribe
    public void onErrorEvent(ErrorEvent errorEvent){

        Log.e("UpdateExam: ",errorEvent.getErrorMsg());

        dialog.dismiss();

        if(isExecuted) {
            utilities.dialogError(UpdateExam.this, getString(R.string.error_occurred));
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