package com.naijaunik.kuteb.Fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.naijaunik.kuteb.Activities.CourseLessonViewer;
import com.naijaunik.kuteb.Activities.CourseSections;
import com.naijaunik.kuteb.Activities.HomeSlides;
import com.naijaunik.kuteb.Activities.Login;
import com.naijaunik.kuteb.Activities.Notifications;
import com.naijaunik.kuteb.Activities.Profile;
import com.naijaunik.kuteb.Activities.UpgradePlans;
import com.naijaunik.kuteb.Adapters.CoursesAdapter;
import com.naijaunik.kuteb.Adapters.HomeSliderAdapter;
import com.naijaunik.kuteb.Adapters.VideosAdapter;
import com.naijaunik.kuteb.Api.BusProvider;
import com.naijaunik.kuteb.Api.Communicator;
import com.naijaunik.kuteb.Api.ErrorEvent;
import com.naijaunik.kuteb.Api.ServerEvent;
import com.naijaunik.kuteb.Api.ServerResponse;
import com.naijaunik.kuteb.BuildConfig;
import com.naijaunik.kuteb.Model.CoursesModel;
import com.naijaunik.kuteb.Model.HomeSlider;
import com.naijaunik.kuteb.Model.VideosModel;
import com.naijaunik.kuteb.R;
import com.naijaunik.kuteb.Utils.AppSession;
import com.naijaunik.kuteb.Utils.Utilities;
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.naijaunik.kuteb.Utils.AppConstants.DATE_TIME_FORMAT;

public class HomeFragment extends Fragment implements View.OnClickListener, CoursesAdapter.CoursesAdapterListener,
        VideosAdapter.VideosAdapterListener {

    Toolbar toolbar;
    private SliderView imageSlider;

    private Communicator communicator;
    private final static String TAG = "HomeFragment";
    public static Bus bus;
    ServerResponse serverResponse;
    ProgressBar sliderLoader;
    Utilities utilities;
    ImageView placeholder_img;
    RelativeLayout trial_warning_section,courses_layout,videos_section;
    MaterialButton upgrade_btn, view_all_btn,view_all_videos_btn;
    TextView trial_warning_text;
    AppSession appSession;
    JsonObject userObj;
    RecyclerView courses_recycler_view,videos_recycler_view;
    private List<CoursesModel> courseList;
    private boolean isAdmin;
    private CoursesAdapter coursesAdapter;
    private ArrayList<VideosModel> videoList;
    private VideosAdapter videosAdapter;
    private JsonObject adminSettings;
    private float newAppVersion;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        utilities = Utilities.getInstance(getContext());
        appSession = AppSession.getInstance(getContext());
        userObj = appSession.getUser();
        adminSettings = appSession.getAdminSettings();
        communicator = new Communicator();

        if(!utilities.cleanData(userObj.get("status")).equals("admin")){

            //prevent screen capture
            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        }
        // Inflate the layout for this fragment
        View homeFragmentView = inflater.inflate(R.layout.fragment_home, container, false);
        setHasOptionsMenu(true);

        toolbar = homeFragmentView.findViewById(R.id.toolbar);
        ((AppCompatActivity) Objects.requireNonNull(getActivity())).setSupportActionBar(toolbar);
        Objects.requireNonNull(((AppCompatActivity) getActivity()).getSupportActionBar()).setTitle("Home");
        Objects.requireNonNull(((AppCompatActivity) getActivity()).getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_person_pin);
        Objects.requireNonNull(((AppCompatActivity) getActivity()).getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(((AppCompatActivity) getActivity()).getSupportActionBar()).setDisplayShowCustomEnabled(true);

        initFields(homeFragmentView);

        return  homeFragmentView;
    }

    private void initFields(View homeFragmentView) {
        
        courseList = new ArrayList<>();

        videoList = new ArrayList<>();

        isAdmin = utilities.cleanData(userObj.get("status")).equals("admin");

        newAppVersion = Float.parseFloat(utilities.cleanData(adminSettings.get("new_app_version")));

        imageSlider = homeFragmentView.findViewById(R.id.imageSlider);
        sliderLoader = homeFragmentView.findViewById(R.id.sliderLoader);
        placeholder_img = homeFragmentView.findViewById(R.id.placeholder_img);

        trial_warning_section = homeFragmentView.findViewById(R.id.trial_warning_section);
        upgrade_btn = homeFragmentView.findViewById(R.id.upgrade_btn);
        trial_warning_text = homeFragmentView.findViewById(R.id.trial_warning_text);

        view_all_btn = homeFragmentView.findViewById(R.id.view_all_btn);
        view_all_videos_btn = homeFragmentView.findViewById(R.id.view_all_videos_btn);

        courses_recycler_view = homeFragmentView.findViewById(R.id.courses_recycler_view);

        courses_layout = homeFragmentView.findViewById(R.id.courses_section);

        videos_section = homeFragmentView.findViewById(R.id.videos_section);
        videos_recycler_view = homeFragmentView.findViewById(R.id.videos_recycler_view);

        //check if the user needs to upgrade
        checkExpirationDate();

        if(!isAdmin && appSession.isUserPlanExpired()){

            videos_section.setVisibility(View.GONE);
        }

        if(isAdmin){

            getPlans();
        }


        //fetch sliders
        initSlider();

        //set click listeners
        upgrade_btn.setOnClickListener(this);
        view_all_btn.setOnClickListener(this);
        view_all_videos_btn.setOnClickListener(this);

        courses_recycler_view.setHasFixedSize(true);
        LinearLayoutManager coursesLayoutManager
                = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        courses_recycler_view.setLayoutManager(coursesLayoutManager);
        courses_recycler_view.setItemAnimator(new DefaultItemAnimator());

        LinearLayoutManager videosLayoutManager
                = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        videos_recycler_view.setLayoutManager(videosLayoutManager);
        videos_recycler_view.setItemAnimator(new DefaultItemAnimator());


        showCourses();

        showVideos();

        getExams();

        int versionCode = BuildConfig.VERSION_CODE;

        if(newAppVersion > versionCode){

            showUpdateNotification();

        }
    }

    @SuppressLint("SetTextI18n")
    private void showUpdateNotification() {

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.update_layout,null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setView(dialogView);

        final AlertDialog alertDialog = builder.create();

        TextView update_text = dialogView.findViewById(R.id.update_text);

        update_text.setText("A newer version of "+ utilities.getApplicationName(getContext()) +
                ", version "+ newAppVersion + " app, is now available on app stores!");

        MaterialButton btn_update_app  = dialogView.findViewById(R.id.btn_update_app);
        MaterialButton close_dialog_btn = dialogView.findViewById(R.id.close_dialog_btn);

        close_dialog_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                alertDialog.dismiss();
            }
        });

        btn_update_app.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String packageName = getContext().getPackageName();
                try {
                    //open playstore app if installed
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));

                }catch (android.content.ActivityNotFoundException anfe){

                    //open playstore in browser if playstore app is not installed
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)));
                }
            }
        });

        alertDialog.show();



    }

    private void showVideos() {

        //clear book array list to avoid repeatition of data
        videoList.clear();

        try {

            for (JsonElement element : appSession.getVideos()) {
                String id = Utilities.cleanData(((JsonObject) element).get("id"));
                String course_id = Utilities.cleanData(((JsonObject) element).get("course_id"));
                String course_title = Utilities.cleanData(((JsonObject) element).get("course_title"));
                String title = Utilities.cleanData(((JsonObject) element).get("title"));
                String content = Utilities.cleanData(((JsonObject) element).get("content"));
                String video_url = Utilities.cleanData(((JsonObject) element).get("video_url"));
                String video_from = Utilities.cleanData(((JsonObject) element).get("video_from"));
                String external_link = Utilities.cleanData(((JsonObject) element).get("external_link"));
                String attachment = Utilities.cleanData(((JsonObject) element).get("attachment"));
                String attachment_extension = Utilities.cleanData(((JsonObject) element).get("attachment_extension"));

               if(videoList.size() <= 5){

                   //add data to list
                   videoList.add(new VideosModel(id,course_id,course_title,title,content,video_url,
                           video_from,external_link,attachment,attachment_extension));

               }
            }


            if(videoList.size() == 0){

                videos_section.setVisibility(View.GONE);
            }


            videosAdapter = new VideosAdapter(getContext(), videoList,this,getLifecycle());

            videos_recycler_view.setAdapter(videosAdapter);

        }catch (Exception e){

            e.printStackTrace();

            videos_section.setVisibility(View.VISIBLE);

        }

    }

    @Override
    public void onVideoSelected(VideosModel course, int position) {

        if(!isAdmin && appSession.isUserPlanExpired()){

                //user's plan has expired so take them to Upgrade Plan screen
                startActivity(new Intent(getContext(), UpgradePlans.class));


            }else {

                //user's plan is still valid

                //view course sections
                Intent viewCourseIntent = new Intent(getContext(), CourseLessonViewer.class);
                viewCourseIntent.putExtra("id", course.getId());
                viewCourseIntent.putExtra("course_id",course.getCourse_id());
                viewCourseIntent.putExtra("title",course.getSection_title());
                viewCourseIntent.putExtra("content",course.getContent());
                viewCourseIntent.putExtra("video_url",course.getVideo_url());
                viewCourseIntent.putExtra("video_from",course.getVideo_from());
                viewCourseIntent.putExtra("external_link",course.getExternal_link());
                viewCourseIntent.putExtra("attachment",course.getAttachment());
                viewCourseIntent.putExtra("attachment_extension",course.getAttachment_extension());
                startActivity(viewCourseIntent);

            }

        getActivity().overridePendingTransition(R.anim.right_in, R.anim.left_out);


    }

    private void showCourses() {


        //clear book array list to avoid repeatition of data
        courseList.clear();

        try {

            for (JsonElement element : appSession.getCourses()) {
                String id = Utilities.cleanData(((JsonObject) element).get("id"));
                String title = Utilities.cleanData(((JsonObject) element).get("title"));
                String description = Utilities.cleanData(((JsonObject) element).get("description"));
                String no_of_students_enrolled = Utilities.cleanData(((JsonObject) element).get("no_of_students_enrolled"));
                String status = Utilities.cleanData(((JsonObject) element).get("status"));
                String course_icon = Utilities.cleanData(((JsonObject) element).get("course_icon"));



                if(courseList.size() <= 5){

                    if(isAdmin){

                        //add data to list
                        courseList.add(new CoursesModel(id,title, description,no_of_students_enrolled,status,course_icon));

                    }else{

                        if(status.equals("available")){

                            //add data to list
                            courseList.add(new CoursesModel(id,title, description,no_of_students_enrolled,status,course_icon));
                        }
                    }

                }
            }


            if(courseList.size() == 0){
                
                courses_layout.setVisibility(View.GONE);
            }


            coursesAdapter = new CoursesAdapter(getContext(), courseList,this);

            courses_recycler_view.setAdapter(coursesAdapter);

        }catch (Exception e){

            e.printStackTrace();

            courses_layout.setVisibility(View.GONE);

        }

    }

    @Override
    public void onCourseSelected(final CoursesModel course, final int position) {

        if(!isAdmin && appSession.isUserPlanExpired()){

                //user's plan has expired so take them to Upgrade Plan screen
                startActivity(new Intent(getContext(), UpgradePlans.class));


        }else {

                //user's plan is still valid

                //view course sections
                Intent viewCourseIntent = new Intent(getContext(), CourseSections.class);
                viewCourseIntent.putExtra("id", course.getId());
                viewCourseIntent.putExtra("title",course.getTitle());
                startActivity(viewCourseIntent);

        }
        getActivity().overridePendingTransition(R.anim.right_in, R.anim.left_out);

    }

    @SuppressLint({"SimpleDateFormat", "DefaultLocale"})
    private void checkExpirationDate() {

        String currentUserExpirationDate = utilities.cleanData(appSession.getUser().get("current_expiration_date"));

        SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_FORMAT);
        Date strDate;

        int isTrial = Integer.parseInt(utilities.cleanData(appSession.getUser().get("is_trial")));

        if(!currentUserExpirationDate.trim().isEmpty()){

            try {

                strDate = sdf.parse(currentUserExpirationDate);
                boolean is_plan_expired =  System.currentTimeMillis() > strDate.getTime();

                if(is_plan_expired){

                    //set user plan as expired
                    appSession.setUserPlanExpired(true);

                    if(!utilities.cleanData(userObj.get("status")).equals("admin")) {

                        trial_warning_section.setVisibility(View.VISIBLE);

                    }

                    if(isTrial == 1){

                        trial_warning_text.setText(getContext().getResources().getString(R.string.trial_expired));

                    }else{

                        trial_warning_text.setText(getContext().getResources().getString(R.string.plan_expired));
                    }

                }else{

                    //show upgrade warning 7 days ahead of expiration date
                    if(getExpirationDateDifferenc(strDate.getTime()) <= 7){

                        if(!utilities.cleanData(userObj.get("status")).equals("admin")) {

                            trial_warning_section.setVisibility(View.VISIBLE);

                        }

                        if(isTrial == 1) {

                            trial_warning_text.setText(String.format(getString(R.string.trial_expires_in),
                                    getExpirationDateDifferenc(strDate.getTime())));

                        }else{

                            trial_warning_text.setText(String.format(getString(R.string.plan_expires_in),
                                    getExpirationDateDifferenc(strDate.getTime())));
                        }

                    }else {

                        trial_warning_section.setVisibility(View.GONE);
                    }
                }

            } catch (ParseException e) {

                e.printStackTrace();

            }

        }
    }

    @SuppressLint("SimpleDateFormat")
    private long getExpirationDateDifferenc(long expirationDate) {

        SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_FORMAT);

        String currentDateandTime = sdf.format(new Date());

        Date formattedDate;
        try {

            formattedDate = sdf.parse(currentDateandTime);

            long diff = expirationDate - formattedDate.getTime();


            return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return 0;

    }

    private void initSlider() {

       try {
           //check if there's home slider in appsession
           if(appSession.getHomeSliders() != null)
           {
               imageSlider.setVisibility(View.VISIBLE);
               sliderLoader.setVisibility(View.GONE);
               placeholder_img.setVisibility(View.GONE);
           }
       }catch (Exception e){
           e.printStackTrace();
       }

       getHomeSliders();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

        inflater = Objects.requireNonNull(getActivity()).getMenuInflater();
        inflater.inflate(R.menu.home_menu, menu);

        MenuItem upgradePlansItem = menu.findItem(R.id.home_upgrade_plans);
        MenuItem homeSlidesItem = menu.findItem(R.id.home_slides);

        if(isAdmin){

            upgradePlansItem.setVisible(true);
            homeSlidesItem.setVisible(true);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.home_notif_icon) {

            startActivity(new Intent(getContext(), Notifications.class));
            Objects.requireNonNull(getActivity()).overridePendingTransition(R.anim.right_in, R.anim.left_out);

        }else if(item.getItemId() == R.id.home_slides){

            startActivity(new Intent(getContext(), HomeSlides.class));
            Objects.requireNonNull(getActivity()).overridePendingTransition(R.anim.right_in, R.anim.left_out);

        }else if (item.getItemId() == R.id.home_upgrade_plans){

            startActivity(new Intent(getContext(), UpgradePlans.class));
            Objects.requireNonNull(getActivity()).overridePendingTransition(R.anim.right_in, R.anim.left_out);

        }else {

            startActivity(new Intent(getContext(), Profile.class));
            Objects.requireNonNull(getActivity()).overridePendingTransition(R.anim.right_in, R.anim.left_out);

        }

        return super.onOptionsItemSelected(item);
    }

    @Subscribe
    public void onServerEvent(ServerEvent serverEvent){

        serverResponse = serverEvent.getServerResponse();

        if(serverResponse != null) {

            //handle different kinds of request responses

            if (serverResponse.getType().equals("homeslider")) {

                if (serverResponse.getResult().equals("success")) {

                    //populate and show the slider items
                    sliderLoader.setVisibility(View.GONE);
                    imageSlider.setVisibility(View.VISIBLE);
                    placeholder_img.setVisibility(View.GONE);

                    JsonArray slidesDataArray = serverResponse.getData();

                    //store home slides to prefs

                    appSession.storeHomeSliders(slidesDataArray);

                    displaySlideItems();

                } else {

                    //show default image instead of slider
                    sliderLoader.setVisibility(View.GONE);
                    placeholder_img.setVisibility(View.VISIBLE);
                    imageSlider.setVisibility(View.GONE);
                }

            }else if(serverResponse.getType().equals("login")){

                if(serverResponse.getResult().equals("success")){

                    JsonObject userdata = serverResponse.getUserdata();
                    JsonObject adminsettings = serverResponse.getAdmin_settings();

                    if(userdata != null) {

                        //check if the user has been blocked
                        if (Integer.parseInt(utilities.cleanData(userdata.get("is_blocked"))) == 1) {

                            //user has been blocked so we log them out!
                            appSession.logout();
                            startActivity(new Intent(getContext(), Login.class));
                            getActivity().overridePendingTransition(R.anim.right_in, R.anim.left_out);
                            getActivity().finish();

                            utilities.dialogError(getContext(), getString(R.string.account_blocked));

                        } else {

                            //storing the user in shared preferences
                            appSession.userLogin(userdata);

                            //store the admin settings in preferences
                            appSession.storeAdminSettings(adminsettings);
                        }
                    }

                }

            }else if(serverResponse.getType().equals("getcourses")){

                if(serverResponse.getResult().equals("success")){

                    appSession.storeCourses(serverResponse.getData());
                }

            }else if(serverResponse.getType().equals("videos")){

                if(serverResponse.getResult().equals("success")){

                    appSession.storeVideos(serverResponse.getData());
                }

            }else  if (serverResponse.getType().equals("getplans")) {

                if (serverResponse.getResult().equals("success")) {

                    JsonArray plansData = serverResponse.getData();

                    if (plansData != null) {

                        //store plans to pref
                        appSession.storePlans(plansData);

                    }
                }

            }else  if (serverResponse.getType().equals("getexams")) {

                if (serverResponse.getResult().equals("success")) {

                    JsonArray examsData = serverResponse.getData();

                    if (examsData != null) {

                        //store plans to pref
                        appSession.storeExams(examsData);

                    }
                }

            }

        }
    }

    private void displaySlideItems() {


            //fill list screen
            final List<HomeSlider> sliderList = new ArrayList<>();
            //clear book array list to avoid repeatition of data
            sliderList.clear();

            for (JsonElement element : appSession.getHomeSliders()) {
                int id = Integer.parseInt(Utilities.cleanData(((JsonObject) element).get("id")));
                String image = Utilities.cleanData(((JsonObject) element).get("image"));
                String title = Utilities.cleanData(((JsonObject) element).get("title"));
                String subtitle = Utilities.cleanData(((JsonObject) element).get("subtitle"));
                String go_to_url = Utilities.cleanData(((JsonObject) element).get("go_to_url"));
                //add data to list
                sliderList.add(new HomeSlider(id,image, title,subtitle,go_to_url));
            }


            HomeSliderAdapter adapter = new HomeSliderAdapter(getContext(), sliderList);

            imageSlider.setSliderAdapter(adapter);
            imageSlider.setIndicatorAnimation(IndicatorAnimationType.WORM); //set indicator animation by using SliderLayout.IndicatorAnimations. :WORM or THIN_WORM or COLOR or DROP or FILL or NONE or SCALE or SCALE_DOWN or SLIDE and SWAP!!
            imageSlider.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
            imageSlider.setScrollTimeInSec(4); //set scroll delay in seconds :
            imageSlider.startAutoCycle();
            adapter.notifyDataSetChanged();

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){

            case R.id.upgrade_btn:
                //upgrade user
                startActivity(new Intent(getContext(), UpgradePlans.class));
                Objects.requireNonNull(getActivity()).overridePendingTransition(R.anim.right_in, R.anim.left_out);
                break;
            case R.id.view_all_btn:
                //take to courses screen
                Intent coursesIntent = new Intent("msg");    //action: "msg"
                coursesIntent.setPackage(getActivity().getPackageName());
                coursesIntent.putExtra("message", "show_courses_tab");
                getActivity().getApplicationContext().sendBroadcast(coursesIntent);
                break;
            case R.id.view_all_videos_btn:
                //take to courses screen
                Intent videoIntent = new Intent("msg");    //action: "msg"
                videoIntent.setPackage(getActivity().getPackageName());
                videoIntent.putExtra("message", "show_videos_tab");
                getActivity().getApplicationContext().sendBroadcast(videoIntent);
                break;
        }
    }

    @Subscribe
    public void onErrorEvent(ErrorEvent errorEvent){

        Log.e("HomeFragment: ",errorEvent.getErrorMsg());

    }

    private void getPlans() {

        communicator.nonParametrizedCall("getplans",getContext());
    }

    private void getUserInfo() {

        //sign in the user
        communicator.singleParametrizedCall(utilities.cleanData(userObj.get("phone")),"login");
    }

    private void getCourses() {

        communicator.nonParametrizedCall("getcourses",getContext());
    }

    private void getVideos() {

        communicator.nonParametrizedCall("getvideos",getContext());
    }

    private void getExams() {

        communicator.nonParametrizedCall("get_exams",getContext());
    }

    public void onResume(){
        super.onResume();

        try {
            BusProvider.getInstance().register(this);
        }catch (Exception e){
            e.printStackTrace();
        }

        getUserInfo();
        getCourses();
        getVideos();
        getHomeSliders();
        getExams();

        if(isAdmin){

            getPlans();
        }

        //check user's subscription
        checkExpirationDate();
    }

    private void getHomeSliders() {

        communicator.nonParametrizedCall("get_home_slides",getContext());
    }

    @Override
    public void onPause() {
        super.onPause();

        try {
            BusProvider.getInstance().unregister(this);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}