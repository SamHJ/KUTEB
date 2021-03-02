package com.naijaunik.kuteb.Activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.naijaunik.kuteb.Adapters.CourseSectionAdapter;
import com.naijaunik.kuteb.Api.BusProvider;
import com.naijaunik.kuteb.Api.Communicator;
import com.naijaunik.kuteb.Api.ErrorEvent;
import com.naijaunik.kuteb.Api.ServerEvent;
import com.naijaunik.kuteb.Api.ServerResponse;
import com.naijaunik.kuteb.Model.CourseSectionModel;
import com.naijaunik.kuteb.R;
import com.naijaunik.kuteb.Utils.AppSession;
import com.naijaunik.kuteb.Utils.Utilities;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CourseSections extends AppCompatActivity implements CourseSectionAdapter.CourseSectionAdapterListener {

    private Utilities utilities;
    private AppSession appSession;
    private JsonObject userObj;
    private Toolbar toolbar;
    ShimmerFrameLayout shimmer_view_container;
    LinearLayout no_data_found_layout,error_layout;
    RecyclerView course_sections_recycler_view;
    private SearchView searchView;
    private ServerResponse serverResponse;
    private ProgressDialog dialog;
    private CourseSectionAdapter courseSectionAdapter;
    private List<CourseSectionModel> courseSectionList;
    private Communicator communicator;
    private boolean isAdmin;
    private boolean isSectionFetch;
    private String courseTitle;
    private String courseDescription;
    private String enrolledCount;
    private String courseID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        utilities = Utilities.getInstance(this);
        appSession = appSession.getInstance(this);
        userObj = appSession.getUser();
        communicator = new Communicator();

        isAdmin = utilities.cleanData(userObj.get("status")).equals("admin");

        if(!isAdmin){

            //prevent screen capture
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        }

        setContentView(R.layout.activity_course_sections);

        courseID = getIntent().getStringExtra("id");

        courseTitle = getIntent().getStringExtra("title");
        courseDescription = getIntent().getStringExtra("description");
        enrolledCount = getIntent().getStringExtra("studentscount");


        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(courseTitle);

        //init fields
        initFields();

        //fetch course sections
        isSectionFetch = true;
        communicator.singleParametrizedCall(courseID,"get_course_sections");


    }

    private void initFields() {

        courseSectionList = new ArrayList<>();

        dialog = utilities.showLoadingDialog("Deleting Lesson...","");

        shimmer_view_container = findViewById(R.id.shimmer_view_container);
        no_data_found_layout = findViewById(R.id.no_data_found_layout);
        error_layout = findViewById(R.id.error_layout);
        course_sections_recycler_view = findViewById(R.id.course_sections_recycler_view);

        course_sections_recycler_view.setHasFixedSize(true);
        LinearLayoutManager notifsLayoutManager = new LinearLayoutManager(this);
        course_sections_recycler_view.setLayoutManager(notifsLayoutManager);
        course_sections_recycler_view.setItemAnimator(new DefaultItemAnimator());
    }


    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_and_add_menu, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        MenuItem addCourses = menu.findItem(R.id.action_add);
        MenuItem courseInfo = menu.findItem(R.id.action_info);

        courseInfo.setVisible(true);

        if(utilities.cleanData(userObj.get("status")).equals("admin")){

            addCourses.setVisible(true);
            addCourses.setTitle("Add Course Lesson");
        }

        searchView = (SearchView) MenuItemCompat.getActionView(item);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                /* prevent crashing of app
                 * when the user tries to search for something
                 * and there's little to no network
                 * because courseAdapter would be null by then */
                try{

                    courseSectionAdapter.getFilter().filter(query);

                }catch (Exception e){
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                try{

                    courseSectionAdapter.getFilter().filter(newText);

                }catch (Exception e){
                    e.printStackTrace();
                }
                return false;
            }
        });

        return  super.onCreateOptionsMenu(menu);

    }

    @SuppressLint("NewApi")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
            overridePendingTransition(R.anim.left_in, R.anim.right_out);

        }else if(item.getItemId() == R.id.action_add){
            Intent intent = new Intent(CourseSections.this, AddCourseLesson.class);
            intent.putExtra("courseid",courseID);
            startActivity(intent);
            overridePendingTransition(R.anim.right_in, R.anim.left_out);

        }else if(item.getItemId() == R.id.action_info){

            new MaterialDialog.Builder(this)
                    .title(Html.fromHtml("About "+courseTitle))
                    .content(Html.fromHtml(courseDescription))
                    .positiveText(R.string.ok)
                    .show();

        }

        return super.onOptionsItemSelected(item);
    }

    @Subscribe
    public void onServerEvent(ServerEvent serverEvent){

        serverResponse = serverEvent.getServerResponse();

        if(serverResponse != null) {

            if (serverResponse.getType().equals("getcoursesections")) {

                if (serverResponse.getResult().equals("success")) {

                    JsonArray sectionData = serverResponse.getData();

                    if (sectionData != null) {

                        //storing the user in shared preferences

                        displayLessons(sectionData);

                    } else{

                        //show error layout
                        error_layout.setVisibility(View.VISIBLE);
                        no_data_found_layout.setVisibility(View.GONE);
                        course_sections_recycler_view.setVisibility(View.GONE);
                    }

                } else {

                    //show error layout
                    error_layout.setVisibility(View.VISIBLE);
                    no_data_found_layout.setVisibility(View.GONE);
                    course_sections_recycler_view.setVisibility(View.GONE);

                }

            }else if(serverResponse.getType().equals("deletecoursesection")){

                if (serverResponse.getResult().equals("success")) {

                    utilities.dialogError(this, getString(R.string.section_deleted_successfully));

                }else{

                    utilities.dialogError(this,getString(R.string.error_occurred));
                }

                //dismiss dialog loader
                dialog.dismiss();

            }
        }
    }

    @Subscribe
    public void onErrorEvent(ErrorEvent errorEvent){

        Log.e("CourseSection: ",errorEvent.getErrorMsg());

        shimmer_view_container.setVisibility(View.GONE);

        if(isSectionFetch){

            error_layout.setVisibility(View.VISIBLE);
            no_data_found_layout.setVisibility(View.GONE);
            course_sections_recycler_view.setVisibility(View.GONE);
        }

        dialog.dismiss();

    }

    private void displayLessons(JsonArray sectionData) {

        shimmer_view_container.setVisibility(View.GONE);

        //clear book array list to avoid repeatition of data
        courseSectionList.clear();

        try {

            for (JsonElement element : sectionData) {
                String id = Utilities.cleanData(((JsonObject) element).get("id"));
                String course_id = Utilities.cleanData(((JsonObject) element).get("course_id"));
                String title = Utilities.cleanData(((JsonObject) element).get("title"));
                String content = Utilities.cleanData(((JsonObject) element).get("content"));
                String video_url = Utilities.cleanData(((JsonObject) element).get("video_url"));
                String video_from = Utilities.cleanData(((JsonObject) element).get("video_from"));
                String external_link = Utilities.cleanData(((JsonObject) element).get("external_link"));
                String attachment = Utilities.cleanData(((JsonObject) element).get("attachment"));
                String attachment_extension = Utilities.cleanData(((JsonObject) element).get("attachment_extension"));

                //add data to list
                courseSectionList.add(new CourseSectionModel(id,course_id,title,content,video_url,
                        video_from,external_link,attachment,attachment_extension));
            }


            if(courseSectionList.size() == 0){

                course_sections_recycler_view.setVisibility(View.GONE);
                error_layout.setVisibility(View.GONE);
                no_data_found_layout.setVisibility(View.VISIBLE);
            }


            courseSectionAdapter = new CourseSectionAdapter(CourseSections.this,
                    courseSectionList,CourseSections.this);

            course_sections_recycler_view.setAdapter(courseSectionAdapter);

        }catch (Exception e){

            e.printStackTrace();

            no_data_found_layout.setVisibility(View.VISIBLE);
            error_layout.setVisibility(View.GONE);
            course_sections_recycler_view.setVisibility(View.GONE);

        }
    }

    @Override
    public void onCourseSectionSelected(final CourseSectionModel course, final int position) {

        if(!isAdmin){

            if(appSession.isUserPlanExpired()){

                //user's plan has expired so take them to Upgrade Plan screen
                startActivity(new Intent(this, UpgradePlans.class));
                overridePendingTransition(R.anim.right_in, R.anim.left_out);


            }else {

                //user's plan is still valid

                //view course sections
                Intent viewCourseIntent = new Intent(this, CourseLessonViewer.class);
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
                overridePendingTransition(R.anim.right_in, R.anim.left_out);

            }

        }else {

            CharSequence options_image[] = new CharSequence[]{
                    "View Lesson",
                    "Update",
                    "Delete",
                    "Cancel"
            };
            AlertDialog.Builder builderoptions_image = new AlertDialog.Builder(this);
            builderoptions_image.setTitle("Lesson Options");

            builderoptions_image.setItems(options_image, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    if (which == 0) {

                        //view course sections
                        Intent viewCourseIntent = new Intent(CourseSections.this, CourseLessonViewer.class);
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
                        overridePendingTransition(R.anim.right_in, R.anim.left_out);

                    } else if (which == 1) {

                        Intent intent = new Intent(CourseSections.this, UpdateCourseSection.class);
                        intent.putExtra("id", course.getId());
                        intent.putExtra("course_id",course.getCourse_id());
                        intent.putExtra("title",course.getSection_title());
                        intent.putExtra("content",course.getContent());
                        intent.putExtra("video_url",course.getVideo_url());
                        intent.putExtra("video_from",course.getVideo_from());
                        intent.putExtra("external_link",course.getExternal_link());
                        intent.putExtra("attachment",course.getAttachment());
                        intent.putExtra("attachment_extension",course.getAttachment_extension());
                        startActivity(intent);
                        overridePendingTransition(R.anim.right_in, R.anim.left_out);

                    } else if (which == 2) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(CourseSections.this);
                        builder.setMessage("Are you sure you want to delete this lesson?");
                        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                DeleteCourseSection(course.getId(), position);
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
                }
            });

            builderoptions_image.show();
        }

    }

    private void DeleteCourseSection(String section_id, int position) {

        dialog.show();

        isSectionFetch = false;

        communicator.singleParametrizedCall(section_id,"deletecoursesection");

        courseSectionList.remove(position);
        courseSectionAdapter.notifyItemRemoved(position);

    }

    @Override
    public void onResume(){
        super.onResume();

        try {
            shimmer_view_container.startShimmer();

            //fetch course sections
            communicator.singleParametrizedCall(courseID,"get_course_sections");


            BusProvider.getInstance().register(this);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        shimmer_view_container.stopShimmer();
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