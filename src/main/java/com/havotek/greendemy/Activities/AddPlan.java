package com.naijaunik.kuteb.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
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

public class AddPlan extends AppCompatActivity {

    private Toolbar toolbar;
    MaterialButton btn_update_plan;
    TextInputEditText input_plan_amount,input_duration_in_days,input_plan_duration_name,input_plan_name;

    String planAmount, planDurationInDays, planName, planDurationName;
    Utilities utilities;
    Communicator communicator;
    private ProgressDialog dialog;
    private ServerResponse serverResponse;
    private boolean isExecuted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_plan);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Add Plan");

        initFields();
    }


    private void initFields() {

        utilities = Utilities.getInstance(this);
        communicator = new Communicator();

        dialog = utilities.showLoadingDialog("Adding plan...","");

        btn_update_plan = findViewById(R.id.btn_update_plan);
        input_plan_amount = findViewById(R.id.input_plan_amount);
        input_duration_in_days = findViewById(R.id.input_duration_in_days);
        input_plan_duration_name = findViewById(R.id.input_plan_duration_name);
        input_plan_name = findViewById(R.id.input_plan_name);


        btn_update_plan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                planAmount = input_plan_amount.getText().toString().trim();
                planDurationInDays = input_duration_in_days.getText().toString().trim();
                planName = input_plan_name.getText().toString().trim();
                planDurationName = input_plan_duration_name.getText().toString().trim();

                if(planAmount.isEmpty() || planDurationInDays.isEmpty() || planName.isEmpty() || planDurationName.isEmpty()){

                    utilities.dialogError(AddPlan.this,"All fields are required!");

                }else{

                    dialog.show();

                    isExecuted = true;

                    communicator.updatePlan("addplan",planName,planAmount,planDurationName,planDurationInDays,
                            "");
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

                    utilities.dialogError(AddPlan.this, getString((R.string.upload_successful)));

                } else {

                    //show login error dialog
                    utilities.dialogError(AddPlan.this, getString(R.string.error_occurred));
                }
            } else {

                //show login error dialog
                utilities.dialogError(AddPlan.this, getString(R.string.error_occurred));
            }

            //dismiss dialog loader
            dialog.dismiss();
        }
    }

    @Subscribe
    public void onErrorEvent(ErrorEvent errorEvent){

        Log.e("AddPlan: ",errorEvent.getErrorMsg());

        dialog.dismiss();

        if(isExecuted){

            utilities.dialogError(AddPlan.this,getString(R.string.error_occurred));
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