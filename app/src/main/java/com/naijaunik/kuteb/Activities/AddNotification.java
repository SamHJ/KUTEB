package com.naijaunik.kuteb.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class AddNotification extends AppCompatActivity implements View.OnClickListener {

    MaterialButton btn_update_notif;
    TextInputEditText input_date,input_description,input_title;
    private String title,description,date;
    Communicator communicator;
    private ProgressDialog dialog;
    private Utilities utilities;
    private ServerResponse serverResponse;
    private Toolbar toolbar;

    private Calendar calendar;

    DatePickerDialog.OnDateSetListener dateSetListener;
    boolean isFetch = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_notification);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Add Notification");

        //init fields
        initFields();
    }

    private void initFields() {

        utilities = Utilities.getInstance(this);

        communicator = new Communicator();


        //init dialog
        dialog = utilities.showLoadingDialog("Adding Notification...","");

        btn_update_notif = findViewById(R.id.btn_update_notif);
        input_date = findViewById(R.id.input_date);
        input_description = findViewById(R.id.input_description);
        input_title = findViewById(R.id.input_title);

        calendar = Calendar.getInstance();

         dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                new TimePickerDialog(AddNotification.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);

                        updateLabel();
                    }
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();

            }
        };

        input_date.setOnClickListener(this);

        btn_update_notif.setOnClickListener(this);
    }

    @SuppressLint("NewApi")
    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.btn_update_notif){

            title = input_title.getText().toString().trim();
            description = input_description.getText().toString().trim();
            date = input_date.getText().toString().trim();

            if(title.isEmpty() || description.isEmpty()){

                utilities.dialogError(this,"Title and Description must not be empty!");

            }else{

                AddNotification();
            }
        }else if(view.getId() == R.id.input_date){

            new DatePickerDialog(this, dateSetListener, calendar
                    .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)).show();
        }

    }


    public void updateLabel(){
        String myFormat = "dd MMM yyyy";
        String myFormat2 = "HH:mm:ss";
        SimpleDateFormat format = new SimpleDateFormat(myFormat2, Locale.US);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(myFormat, Locale.US);
        input_date.setText(String.format("On %s, at %s", simpleDateFormat.format(calendar.getTime()),
                format.format(calendar.getTime())));
    }


    private void AddNotification() {

        dialog.show();

        isFetch = true;

        communicator.fiveParametrizedCall("add_notif",title, description, date,"empty");

    }

    @Subscribe
    public void onServerEvent(ServerEvent serverEvent){

        if(isFetch) {

            serverResponse = serverEvent.getServerResponse();

            if (serverResponse != null) {

                if (serverResponse.getResult().equals("success")) {

                    utilities.dialogError(this, getString(R.string.upload_successful));

                    //reset fields
                    input_date.setText("");
                    input_description.setText("");
                    input_title.setText("");

                } else {

                    //show login error dialog
                    utilities.dialogError(AddNotification.this, getString(R.string.error_occurred));
                }
            }

            //dismiss dialog loader
            dialog.dismiss();

        }
    }

    @Subscribe
    public void onErrorEvent(ErrorEvent errorEvent){

        Log.e("AddNotification: ",errorEvent.getErrorMsg());

        dialog.dismiss();

        if(isFetch){

            utilities.dialogError(AddNotification.this,getString(R.string.error_occurred));
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