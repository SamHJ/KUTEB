package com.naijaunik.kuteb.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Objects;

public class AppSession {
    //the constants
    private static final String SHARED_PREF_NAME = "appsessionpref";
    private static final String ADMIN_SETTINGS_PREF_NAME = "adminsettings";
    private static final String HOME_SLIDES_PREF_NAME = "homeslides";
    private static final String NOTIFICATION__PREF_NAME = "notifications";
    private static final String PLAN_EXPIRATION_STATUS__PREF_NAME = "planexpirationstatuspref";
    private static final String COURSES_PREF_NAME = "coursespref";
    private static final String VIDEOS_PREF_NAME = "videospref";
    private static final String PLANS_PREF_NAME = "planspref";
    private static final String EXAMS_PREF_NAME = "examspref";
    private static final String EXAM_QUESTIONS_PREF_NAME = "examquestionspref";

    private static AppSession appSession;
    private static Context mCtx;

    private AppSession(Context context) {
        mCtx = context;
    }

    public static synchronized AppSession getInstance(Context context) {
        if (appSession == null) {
            appSession = new AppSession(context);
        }
        return appSession;
    }

    public void storeAdminSettings(JsonObject object){

        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(ADMIN_SETTINGS_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("admin_settings",object.toString());
        editor.apply();
    }

    public JsonObject getAdminSettings(){

        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(ADMIN_SETTINGS_PREF_NAME, Context.MODE_PRIVATE);

        return new JsonParser().parse(Objects.requireNonNull(sharedPreferences.getString("admin_settings", null)))
                .getAsJsonObject();

    }

    //method to let the user login
    //this method will store the user data in shared preferences
    public void userLogin(JsonObject object) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("user_data",object.toString());
        editor.apply();
    }

    //this method will checker whether user is already logged in or not
    public boolean isLoggedIn() {
        //check if the phone data exists in the user_data object

        try{
            if(getUser() != null) {

                return !Utilities.cleanData(getUser().get("phone")).isEmpty();

            }else{

                return false;
            }
        }catch (Exception e){

            e.printStackTrace();

            return  false;
        }
    }

    //this method will get the logged in user
    public JsonObject getUser() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);

        return new JsonParser().parse(Objects.requireNonNull(sharedPreferences.getString("user_data", null)))
                .getAsJsonObject();
    }

    //this method will logout the user
    public void logout() {

        //clear everything on pref
        PreferenceManager.getDefaultSharedPreferences(mCtx).edit().clear().apply();

        //clear user data for logout
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    //this method will store the home sliders data shared preferences
    public void storeHomeSliders(JsonArray object) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(HOME_SLIDES_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("home_slider",object.toString());
        editor.apply();
    }

    //this method will get the home sliders object
    public JsonArray getHomeSliders() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(HOME_SLIDES_PREF_NAME, Context.MODE_PRIVATE);

        String data = sharedPreferences.getString("home_slider", "");

        if (data.isEmpty()){

            return null;

        }else{

            return new JsonParser().parse(Objects.requireNonNull(data))
                    .getAsJsonArray();
        }
    }

    //this method will store the notifications data to shared pref
    public void storeNotifications(JsonArray object){

        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(NOTIFICATION__PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("notifs",object.toString());
        editor.apply();
    }

    //this method will get the notifications object
    public JsonArray getNotifications(){

        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(NOTIFICATION__PREF_NAME, Context.MODE_PRIVATE);

        return new JsonParser().parse(Objects.requireNonNull(sharedPreferences.getString("notifs", null)))
                .getAsJsonArray();
    }

    //this method will set the user's plan expiration status
    public void setUserPlanExpired(boolean param){

        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(PLAN_EXPIRATION_STATUS__PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("is_expired", param ? "true" : "false");
        editor.apply();

    }

    public boolean isUserPlanExpired(){

        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(NOTIFICATION__PREF_NAME, Context.MODE_PRIVATE);

        String prefVal =  sharedPreferences.getString("is_expired", "");

        return !prefVal.isEmpty() && !prefVal.equals("false");
    }

    public void storeCourses(JsonArray object){

        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(COURSES_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("courses",object.toString());
        editor.apply();
    }

    //this method will get the notifications object
    public JsonArray getCourses(){

        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(COURSES_PREF_NAME, Context.MODE_PRIVATE);

        String data = sharedPreferences.getString("courses", "");

        if (data.isEmpty()){

            return null;

        }else{

            return new JsonParser().parse(Objects.requireNonNull(data))
                    .getAsJsonArray();
        }

    }

    public void storeVideos(JsonArray object){

        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(VIDEOS_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("videos",object.toString());
        editor.apply();
    }

    public JsonArray getVideos(){

        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(VIDEOS_PREF_NAME, Context.MODE_PRIVATE);

        String data = sharedPreferences.getString("videos", "");

        if (data.isEmpty()){

            return null;

        }else{

            return new JsonParser().parse(Objects.requireNonNull(data))
                    .getAsJsonArray();
        }

    }

    public void storePlans(JsonArray plansData) {

        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(PLANS_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("plans",plansData.toString());
        editor.apply();

    }

    public JsonArray getPlans(){

        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(PLANS_PREF_NAME, Context.MODE_PRIVATE);

        return new JsonParser().parse(Objects.requireNonNull(sharedPreferences.getString("plans", null)))
                .getAsJsonArray();

    }

    public void  storeExams(JsonArray examsData){

        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(EXAMS_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("exams",examsData.toString());
        editor.apply();

    }

    public JsonArray getExams(){

        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(EXAMS_PREF_NAME, Context.MODE_PRIVATE);

        return new JsonParser().parse(Objects.requireNonNull(sharedPreferences.getString("exams", null)))
                .getAsJsonArray();

    }

    public void storeExamQuestions(JsonArray examsQuestionsData){


        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(EXAM_QUESTIONS_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("examsquestions",examsQuestionsData.toString());
        editor.apply();

    }

    public JsonArray getExamQuestions(){

        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(EXAM_QUESTIONS_PREF_NAME, Context.MODE_PRIVATE);

        return new JsonParser().parse(Objects.requireNonNull(sharedPreferences.getString("examsquestions", null)))
                .getAsJsonArray();

    }
}
