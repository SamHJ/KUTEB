package com.naijaunik.kuteb.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.etebarian.meowbottomnavigation.MeowBottomNavigation;
import com.naijaunik.kuteb.R;
import com.naijaunik.kuteb.Fragments.AboutFragment;
import com.naijaunik.kuteb.Fragments.CoursesFragment;
import com.naijaunik.kuteb.Fragments.ExamsFragment;
import com.naijaunik.kuteb.Fragments.HomeFragment;
import com.naijaunik.kuteb.Fragments.VideosFragment;
import com.naijaunik.kuteb.Utils.AppSession;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;


public class AppContainer extends AppCompatActivity {

    MeowBottomNavigation meow;
    private  static  final  int ID_COURSES = 1;
    private  static  final  int ID_VIDEOS = 2;
    private  static  final  int ID_HOME = 3;
    private  static  final  int ID_EXAMS = 4;
    private  static  final  int ID_ABOUT = 5;
    private AppSession appSession;
    private BroadcastReceiver mBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_container);

        appSession = AppSession.getInstance(this);


        meow = findViewById(R.id.bottomNavigation);
        meow.add(new MeowBottomNavigation.Model(1, R.drawable.ic_courses));
        meow.add(new MeowBottomNavigation.Model(2, R.drawable.ic_videos));
        meow.add(new MeowBottomNavigation.Model(3, R.drawable.ic_home));
        meow.add(new MeowBottomNavigation.Model(4, R.drawable.ic_exams));
        meow.add(new MeowBottomNavigation.Model(5, R.drawable.ic_about));

        meow.show(ID_HOME,true);


        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();

        meow.setOnClickMenuListener(new Function1<MeowBottomNavigation.Model, Unit>() {
            @Override
            public Unit invoke(MeowBottomNavigation.Model model) {
                // YOUR CODES
                return null;
            }
        });

        meow.setOnShowListener(new Function1<MeowBottomNavigation.Model, Unit>() {
            @Override
            public Unit invoke(MeowBottomNavigation.Model model) {
                // YOUR CODES
                Fragment selectedFragment = null;

                switch (model.getId()){

                    case ID_COURSES:
                        selectedFragment = new CoursesFragment();
                        break;
                    case ID_VIDEOS:
                        selectedFragment = new VideosFragment();
                        break;
                    case ID_HOME:
                        selectedFragment = new HomeFragment();
                        break;
                    case ID_EXAMS:
                        selectedFragment = new ExamsFragment();
                        break;
                    case ID_ABOUT:
                        selectedFragment = new AboutFragment();
                        break;
                }

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();

                return null;
            }
        });
    }
    

    @Override
    protected void onStart() {
        super.onStart();
        if(!appSession.isLoggedIn()){
            startActivity(new Intent(getApplicationContext(), Login.class));
            supportFinishAfterTransition();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBroadcastReceiver = new BroadcastReceiver(){

            @Override
            public void onReceive(Context context, Intent intent){

                String mess = intent.getStringExtra("message");

                if(mess.equals("show_courses_tab")){

                    meow.show(ID_COURSES,true);

                }else if(mess.equals("show_videos_tab")){

                    meow.show(ID_VIDEOS, true);
                }
            }

        };

        IntentFilter filter = new IntentFilter("msg");
        registerReceiver(mBroadcastReceiver,filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mBroadcastReceiver);
    }
}