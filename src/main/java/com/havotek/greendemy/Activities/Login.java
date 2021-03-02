package com.naijaunik.kuteb.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.TaskExecutors;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.gson.JsonObject;
import com.naijaunik.kuteb.Api.BusProvider;
import com.naijaunik.kuteb.Api.Communicator;
import com.naijaunik.kuteb.Api.ErrorEvent;
import com.naijaunik.kuteb.Api.ServerEvent;
import com.naijaunik.kuteb.Api.ServerResponse;
import com.naijaunik.kuteb.R;
import com.naijaunik.kuteb.Utils.AppSession;
import com.naijaunik.kuteb.Utils.Utilities;

import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.squareup.otto.Subscribe;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

import static com.naijaunik.kuteb.Utils.AppConstants.COUNTRY_PHONE_CODE;

public class Login extends AppCompatActivity  implements View.OnClickListener {

    MaterialButton send_otp_btn, verify_otp_btn, btn_change_phone,get_started_btn;
    TextInputEditText login_input_phone, otp_input;
    LinearLayout phone_input_layout, phone_verification_layout;
    private String userPhone, userOTP,mVerificationId;
    Utilities utilities;
    PhoneAuthProvider.ForceResendingToken mResendToken;
    private ProgressDialog dialog;
    Communicator communicator;
    private ServerResponse serverResponse;
    ImageButton btn_resend_otp_code;
    TextView verify_info_text,get_started_text,input_get_started_text;
    ImageView success_img,logo_img;
    JsonObject userdata;
    JsonObject adminsettings;
    RelativeLayout resend_layout;
    TextInputLayout otp_input_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //init fields
        initFields();
    }

    private void initFields() {

        utilities = Utilities.getInstance(this);

        communicator = new Communicator();

        dialog = new ProgressDialog(this);


        phone_input_layout = findViewById(R.id.phone_input_layout);
        phone_verification_layout = findViewById(R.id.phone_verification_layout);

        verify_info_text = findViewById(R.id.verify_info_text);
        get_started_text = findViewById(R.id.get_started_text);
        success_img = findViewById(R.id.success_img);
        input_get_started_text = findViewById(R.id.input_get_started_text);

        login_input_phone = findViewById(R.id.login_input_phone);
        send_otp_btn = findViewById(R.id.send_otp_btn);
        resend_layout = findViewById(R.id.resend_layout);
        logo_img = findViewById(R.id.logo_img);

        otp_input_layout = findViewById(R.id.otp_input_layout);

        otp_input = findViewById(R.id.otp_input);
        verify_otp_btn = findViewById(R.id.verify_otp_btn);

        btn_change_phone = findViewById(R.id.btn_change_phone);
        btn_resend_otp_code = findViewById(R.id.btn_resend_otp_code);

        get_started_btn = findViewById(R.id.get_started_btn);

        send_otp_btn.setOnClickListener(this);
        verify_otp_btn.setOnClickListener(this);
        btn_change_phone.setOnClickListener(this);
        btn_resend_otp_code.setOnClickListener(this);
        get_started_btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.send_otp_btn:

                userPhone = login_input_phone.getText().toString().trim();

                if (isPhoneValid(userPhone)) {

                    sendFireBaseOTP();

                } else {

                    utilities.dialogError(this,
                            getResources().getString(R.string.invalid_phone));
                }
                break;
            case R.id.verify_otp_btn:

                userOTP = otp_input.getText().toString().trim();

                if (isOTPValid(userOTP)) {

                    verifyVerificationCode(userOTP);

                } else {

                    utilities.dialogError(this,
                            getResources().getString(R.string.invalid_otp));
                }
                break;
            case R.id.btn_change_phone:
                phone_verification_layout.setVisibility(View.GONE);
                phone_input_layout.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_resend_otp_code:
                //resend OTP
                sendFireBaseOTP();
                break;

            case R.id.get_started_btn:
                //take the user to the dashboard section
                navigateToDashboard(userdata, adminsettings);
                break;
        }
    }

    private boolean isOTPValid(String userOTP) {

        return !userOTP.isEmpty();
    }

    private boolean isPhoneValid(String phonNumber) {

        return phonNumber.length() >= 10;
    }

    private void sendFireBaseOTP() {

        dialog = utilities.showLoadingDialog(getString(R.string.verifying_phone),getString(R.string.a_moment));

        dialog.show();

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
               userPhone, //currently this app is not country specific (i.e available to all countries)!
                // If you do not want this then change the "userPhone" to "COUNTRY_PHONE_CODE + userPhone"
//                COUNTRY_PHONE_CODE + userPhone,
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallbacks);
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override
                public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                    //Getting the code sent by SMS
                    String code = phoneAuthCredential.getSmsCode();

                    //sometime the code is not detected automatically
                    //in this case the code will be null
                    //so user has to manually enter the code
                    dialog.dismiss();
                    if (code != null) {
                        otp_input.setText(code);
                        //verifying the code
                        verifyVerificationCode(code);
                    }
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {

                    dialog.dismiss();

                    utilities.dialogError(Login.this,e.getMessage());
                }

                @Override
                public void onCodeSent(@NotNull String s, @NotNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                    super.onCodeSent(s, forceResendingToken);
                    mVerificationId = s;
                    mResendToken = forceResendingToken;

                    //hide the phone input layout and show the verification layout
                    dialog.dismiss();
                    phone_input_layout.setVisibility(View.GONE);
                    phone_verification_layout.setVisibility(View.VISIBLE);
                }
            };

    private void verifyVerificationCode(String code) {

        dialog = utilities.showLoadingDialog(getString(R.string.verifying_code),getString(R.string.a_moment));

        dialog.show();

        //creating the credential
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);

        //signing the user
        signInWithPhoneAuthCredential(credential);

    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {

        if(credential != null){

            verify_info_text.setText(R.string.verfication_success);
            logo_img.setVisibility(View.GONE);
            success_img.setVisibility(View.VISIBLE);

            //sign in the user
            communicator.singleParametrizedCall(userPhone,"login");

        }else {

            dialog.dismiss();

            utilities.dialogError(this,getString(R.string.could_not_verify_phone));
        }

    }

    @Subscribe
    public void onServerEvent(ServerEvent serverEvent){

        serverResponse = serverEvent.getServerResponse();

        if(serverResponse != null){

            if(serverResponse.getResult().equals("success")){

                userdata = serverResponse.getUserdata();
                adminsettings = serverResponse.getAdmin_settings();

                if(userdata != null) {

                    //check if the user has been blocked
                    if (Integer.parseInt(Utilities.cleanData(userdata.get("is_blocked"))) == 1) {

                        //user has been blocked
                        phone_input_layout.setVisibility(View.VISIBLE);
                        phone_verification_layout.setVisibility(View.GONE);
                        utilities.dialogError(Login.this, getString(R.string.account_blocked));

                    } else {

                        get_started_btn.setVisibility(View.VISIBLE);
                        btn_change_phone.setVisibility(View.GONE);
                        verify_otp_btn.setVisibility(View.GONE);
                        resend_layout.setVisibility(View.GONE);
                        otp_input_layout.setVisibility(View.GONE);
                        get_started_text.setVisibility(View.GONE);
                        logo_img.setVisibility(View.GONE);


                    }

                }else{

                    //show login error dialog
                    utilities.dialogError(Login.this,getString(R.string.no_record_found));
                }

            }else{

                //show login error dialog
                utilities.dialogError(Login.this,getString(R.string.login_error_occurred));
            }

        }else{

            //show login error dialog
            utilities.dialogError(Login.this,getString(R.string.login_error_occurred));
        }

        //dismiss dialog loader
        dialog.dismiss();

    }

    private void navigateToDashboard(JsonObject userdata, JsonObject adminsettings) {

        //storing the user in shared preferences
        AppSession.getInstance(getApplicationContext()).userLogin(userdata);
        //store the admin settings in preferences
        AppSession.getInstance(getApplicationContext()).storeAdminSettings(adminsettings);
        //take user to dashboard
        startActivity(new Intent(getApplicationContext(), AppContainer.class));
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        finish();

    }

    @Subscribe
    public void onErrorEvent(ErrorEvent errorEvent){

        Log.e("HomeFragment: ",errorEvent.getErrorMsg());

        dialog.dismiss();

        utilities.dialogError(Login.this,getString(R.string.login_error_occurred));
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
    }

}
