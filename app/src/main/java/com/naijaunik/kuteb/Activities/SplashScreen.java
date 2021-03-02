package com.naijaunik.kuteb.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import com.naijaunik.kuteb.R;
import com.naijaunik.kuteb.Utils.AppSession;

public class SplashScreen extends AppCompatActivity {

    ImageView splash_header_img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        splash_header_img = findViewById(R.id.splash_header_img);

        splash_header_img.setClipToOutline(true);


        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {

                // This method will be executed once the timer is over

                // check if the user is currently logged in

                if(AppSession.getInstance(SplashScreen.this).isLoggedIn()){

                    Intent i = new Intent(SplashScreen.this, AppContainer.class);
                    startActivity(i);
                    overridePendingTransition(R.anim.right_in,R.anim.left_out);

                }else{

                    Intent i = new Intent(SplashScreen.this, Login.class);
                    startActivity(i);
                    overridePendingTransition(R.anim.right_in,R.anim.left_out);
                }

                // close this activity
                finish();
            }
        }, 3000);
    }
}