package com.naijaunik.kuteb.Activities;

import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import com.github.appintro.AppIntro2;
import com.github.appintro.AppIntroFragment;
import com.naijaunik.kuteb.R;

import org.jetbrains.annotations.Nullable;

public class Onboarding extends AppIntro2 {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //as this activity is about to be launched, check if the user has already viewed the intro slide
        if(restorePrefData()){

            Intent i = new Intent(Onboarding.this, SplashScreen.class);
            startActivity(i);
            finish();
        }

        // Call addSlide passing your Fragments.
        // You can use AppIntroFragment to use a pre-built fragment
        addSlide(AppIntroFragment.newInstance(
                "Welcome To Kuteb E-Learning",
                "Enroll in quality eduaction from a wide range of courses to choose from",
                R.drawable.onlinecourses,
                Color.WHITE, //background color
                R.color.colorPrimary, //title color
                R.color.colorPrimary //description color
        ));
        addSlide(AppIntroFragment.newInstance(
                "Quality E-learning",
                "Our courses are well taught and modeled to suite your needs!",
                R.drawable.e_learning,
                Color.WHITE, //background color
                R.color.colorPrimary, //title color
                R.color.colorPrimary //description color
        ));

        addSlide(AppIntroFragment.newInstance(
                "Test Your Capabilities",
                "Kuteb E-Learning allows you to enroll in different tasks related to the course you studied",
                R.drawable.enroll_in_tasks,
                Color.WHITE, //background color
                R.color.colorPrimary, //title color
                R.color.colorPrimary //description color
        ));

        addSlide(AppIntroFragment.newInstance(
                "Get Started",
                "It's time to learn with Kuteb E-Learning! Let's get started",
                R.drawable.time_to_learn,
                Color.WHITE, //background color
                R.color.colorPrimary, //title color
                R.color.colorPrimary //description color
        ));

        setImmersiveMode();
        isColorTransitionsEnabled();

        // Change Indicator Color
        setIndicatorColor(
                R.color.colorPrimary,
                R.color.lightPrimary
        );

    }

    public void takeToNextScreen(){

        Intent i = new Intent(Onboarding.this, Login.class);
        startActivity(i);
        overridePendingTransition(R.anim.right_in,R.anim.left_out);
        finish();
    }

    private boolean restorePrefData() {
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("intro_slide_pref", MODE_PRIVATE);
        return preferences.getBoolean("isIntroOpened", false);
    }

    private void savePrefsData() {
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("intro_slide_pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isIntroOpened", true);
        editor.apply();
    }


    @Override
    protected void onSkipPressed(@Nullable Fragment currentFragment) {

        //use sharedpreferences to prevent the intro slide from showing up again
        savePrefsData();

        takeToNextScreen();

        super.onSkipPressed(currentFragment);
    }

    @Override
    protected void onDonePressed(@Nullable Fragment currentFragment) {

        //use sharedpreferences to prevent the intro slide from showing up again
        savePrefsData();

        takeToNextScreen();

        super.onDonePressed(currentFragment);
    }
}