package com.naijaunik.kuteb.Fragments;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.naijaunik.kuteb.Activities.AddCourse;
import com.naijaunik.kuteb.Activities.CourseSections;
import com.naijaunik.kuteb.Activities.Login;
import com.naijaunik.kuteb.Activities.UpdateCourse;
import com.naijaunik.kuteb.Activities.UpgradePlans;
import com.naijaunik.kuteb.Adapters.CoursesAdapter;
import com.naijaunik.kuteb.Api.BusProvider;
import com.naijaunik.kuteb.Api.Communicator;
import com.naijaunik.kuteb.Api.ErrorEvent;
import com.naijaunik.kuteb.Api.ServerEvent;
import com.naijaunik.kuteb.Api.ServerResponse;
import com.naijaunik.kuteb.Model.CoursesModel;
import com.naijaunik.kuteb.R;
import com.naijaunik.kuteb.Utils.AppSession;
import com.naijaunik.kuteb.Utils.Utilities;
import com.squareup.otto.Subscribe;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.naijaunik.kuteb.Utils.AppConstants.DATE_TIME_FORMAT;

public class CoursesFragment extends Fragment implements CoursesAdapter.CoursesAdapterListener {


    private Utilities utilities;
    private AppSession appSession;
    private JsonObject userObj;
    private Communicator communicator;
    private ServerResponse serverResponse;
    private RecyclerView courses_recycler_view;
    LinearLayout error_layout,no_data_found_layout;
    ShimmerFrameLayout shimmer_view_container;
    List<CoursesModel> courseList;
    private CoursesAdapter coursesAdapter;
    private Toolbar toolbar;

    boolean isAdmin;
    private ProgressDialog dialog;
    private SearchView searchView;

    public CoursesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        utilities = Utilities.getInstance(getContext());
        appSession = AppSession.getInstance(getContext());
        communicator = new Communicator();

        userObj = appSession.getUser();

        if(!utilities.cleanData(userObj.get("status")).equals("admin")){

            //prevent screen capture
            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        }

        // Inflate the layout for this fragment
        View coursesFragmentView =  inflater.inflate(R.layout.fragment_courses, container, false);
        setHasOptionsMenu(true);

        toolbar = coursesFragmentView.findViewById(R.id.toolbar);
        ((AppCompatActivity) Objects.requireNonNull(getActivity())).setSupportActionBar(toolbar);
        Objects.requireNonNull(((AppCompatActivity) getActivity()).getSupportActionBar()).setTitle("Courses");


        //init fields
        initFields(coursesFragmentView);

        showCourses();

        return  coursesFragmentView;
    }

    private void initFields(View coursesFragmentView) {

        isAdmin = utilities.cleanData(userObj.get("status")).equals("admin");

        dialog = utilities.showLoadingDialog("Deleting course...","");

        courses_recycler_view = coursesFragmentView.findViewById(R.id.courses_recycler_view);
        error_layout = coursesFragmentView.findViewById(R.id.error_layout);
        no_data_found_layout = coursesFragmentView.findViewById(R.id.no_data_found_layout);
        shimmer_view_container = coursesFragmentView.findViewById(R.id.shimmer_view_container);

        courses_recycler_view.setHasFixedSize(true);
        GridLayoutManager coursesLayoutManager = new GridLayoutManager(getContext(), 2);
        courses_recycler_view.setLayoutManager(coursesLayoutManager);
        courses_recycler_view.setItemAnimator(new DefaultItemAnimator());

        //fill list screen
        courseList = new ArrayList<>();

    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

        inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.search_and_add_menu, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        MenuItem addCourses = menu.findItem(R.id.action_add);


        if(utilities.cleanData(userObj.get("status")).equals("admin")){

            addCourses.setVisible(true);
            addCourses.setTitle("Add Course");
        }

        searchView = (SearchView) MenuItemCompat.getActionView(item);

        item.setVisible(false); //hide searchview for now

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                /* prevent crashing of app
                 * when the user tries to search for something
                 * and there's little to no network
                 * because courseAdapter would be null by then */
                try{

                    coursesAdapter.getFilter().filter(query);

                }catch (Exception e){
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                try{

                    coursesAdapter.getFilter().filter(newText);

                }catch (Exception e){
                    e.printStackTrace();
                }
                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);

    }

    @SuppressLint("NewApi")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

       if(item.getItemId() == R.id.action_add){

            startActivity(new Intent(getContext(), AddCourse.class));
            getActivity().overridePendingTransition(R.anim.right_in, R.anim.left_out);
        }

        return super.onOptionsItemSelected(item);
    }

    @Subscribe
    public void onServerEvent(ServerEvent serverEvent){

        serverResponse = serverEvent.getServerResponse();

        if(serverResponse != null) {

            if (serverResponse.getType().equals("login")) {

                if (serverResponse.getResult().equals("success")) {

                    JsonObject userdata = serverResponse.getUserdata();
                    JsonObject adminsettings = serverResponse.getAdmin_settings();

                    if (userdata != null) {

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

            } else if (serverResponse.getType().equals("getcourses")) {

                if (serverResponse.getResult().equals("success")) {

                    JsonArray courseData = serverResponse.getData();

                    if (courseData != null) {

                        appSession.storeCourses(courseData);

                        showCourses();

                    } else if (appSession.getCourses() == null) {

                        courses_recycler_view.setVisibility(View.GONE);
                        error_layout.setVisibility(View.GONE);
                        no_data_found_layout.setVisibility(View.VISIBLE);
                    }

                } else {

                    if (appSession.getCourses() != null) {

                        courses_recycler_view.setVisibility(View.VISIBLE);

                        showCourses();

                    } else {

                        //show error layout
                        error_layout.setVisibility(View.VISIBLE);
                        no_data_found_layout.setVisibility(View.GONE);

                    }
                }

            } else if (serverResponse.getType().equals("deletecourse")) {

                if (serverResponse.getResult().equals("success")) {

                    utilities.dialogError(getContext(), getString(R.string.course_deleted_successfully));

                } else {

                    utilities.dialogError(getContext(), getString(R.string.error_occurred));
                }

                //dismiss dialog loader
                dialog.dismiss();
            }

        }

    }

    private void showCourses() {

        shimmer_view_container.setVisibility(View.GONE);

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


        if(courseList.size() == 0){

            courses_recycler_view.setVisibility(View.GONE);
            error_layout.setVisibility(View.GONE);
            no_data_found_layout.setVisibility(View.VISIBLE);
        }


        coursesAdapter = new CoursesAdapter(getContext(), courseList,this);

        courses_recycler_view.setAdapter(coursesAdapter);

    }catch (Exception e){

        e.printStackTrace();

        no_data_found_layout.setVisibility(View.VISIBLE);
        error_layout.setVisibility(View.GONE);
        courses_recycler_view.setVisibility(View.GONE);

    }

    }

    @Override
    public void onCourseSelected(final CoursesModel course, final int position) {

        if(!isAdmin){

            if(appSession.isUserPlanExpired()){

                //user's plan has expired so take them to Upgrade Plan screen
                startActivity(new Intent(getContext(), UpgradePlans.class));
                getActivity().overridePendingTransition(R.anim.right_in, R.anim.left_out);


            }else {

                //user's plan is still valid

                //view course sections
                Intent viewCourseIntent = new Intent(getContext(), CourseSections.class);
                viewCourseIntent.putExtra("id", course.getId());
                viewCourseIntent.putExtra("title",course.getTitle());
                viewCourseIntent.putExtra("description",course.getDescription());
                viewCourseIntent.putExtra("studentscount",course.getNo_of_students_enrolled());
                startActivity(viewCourseIntent);
                getActivity().overridePendingTransition(R.anim.right_in, R.anim.left_out);

            }

        }else {

            CharSequence options_image[] = new CharSequence[]{
                    "View Course",
                    "Update",
                    "Delete",
                    "Cancel"
            };
            AlertDialog.Builder builderoptions_image = new AlertDialog.Builder(getContext());
            builderoptions_image.setTitle("Course Options");

            builderoptions_image.setItems(options_image, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                   if(which == 0){

                       //view course sections
                       Intent viewCourseIntent = new Intent(getContext(), CourseSections.class);
                       viewCourseIntent.putExtra("id", course.getId());
                       viewCourseIntent.putExtra("title",course.getTitle());
                       viewCourseIntent.putExtra("description",course.getDescription());
                       viewCourseIntent.putExtra("studentscount",course.getNo_of_students_enrolled());
                       startActivity(viewCourseIntent);
                       getActivity().overridePendingTransition(R.anim.right_in, R.anim.left_out);

                   }else if (which == 1) {

                        Intent intent = new Intent(getContext(), UpdateCourse.class);
                        intent.putExtra("title", course.getTitle());
                        intent.putExtra("description", course.getDescription());
                        intent.putExtra("course_icon",course.getCourse_icon());
                        intent.putExtra("status",course.getStatus());
                        intent.putExtra("no_of_students_enrolled",course.getNo_of_students_enrolled());
                        intent.putExtra("id", course.getId());
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.right_in, R.anim.left_out);

                    } else if (which == 2) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setMessage("Are you sure you want to delete this course? All of it's contents would be deleted also!");
                        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                DeleteCourse(course.getId(), position);
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

    private void DeleteCourse(String id, int position) {

        dialog.show();

        communicator.singleParametrizedCall(id,"deletecourse");

        courseList.remove(position);
        coursesAdapter.notifyItemRemoved(position);

    }

    @SuppressLint({"SimpleDateFormat", "DefaultLocale"})
    private void checkExpirationDate() {

        String currentUserExpirationDate = utilities.cleanData(appSession.getUser().get("current_expiration_date"));

        SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_FORMAT);
        Date strDate;

        if(!currentUserExpirationDate.trim().isEmpty()){

            try {

                strDate = sdf.parse(currentUserExpirationDate);
                boolean is_plan_expired =  System.currentTimeMillis() > strDate.getTime();

                if(is_plan_expired){

                    //set user plan as expired
                    appSession.setUserPlanExpired(true);

                }

            } catch (ParseException e) {

                e.printStackTrace();

            }

        }
    }

    @Subscribe
    public void onErrorEvent(ErrorEvent errorEvent){

        Log.e("CoursesFragment: ",errorEvent.getErrorMsg());

        shimmer_view_container.setVisibility(View.GONE);

        try {

            if(appSession.getCourses() != null){

                courses_recycler_view.setVisibility(View.VISIBLE);
                showCourses();

            }else{

                //show error layout
                error_layout.setVisibility(View.VISIBLE);
                no_data_found_layout.setVisibility(View.GONE);

            }
        }catch (Exception e){

            e.printStackTrace();
        }

    }

    private void getUserInfo() {

        //sign in the user
        communicator.singleParametrizedCall(utilities.cleanData(userObj.get("phone")),"login");
    }

    private void getCourses() {

        communicator.nonParametrizedCall("getcourses",getContext());
    }

    public void onResume(){
        super.onResume();
        shimmer_view_container.startShimmer();
        try {
            BusProvider.getInstance().register(this);
        }catch (Exception e){
            e.printStackTrace();
        }

        getUserInfo();
        getCourses();

        //check user's subscription
        checkExpirationDate();
    }

    @Override
    public void onPause(){
        super.onPause();
        shimmer_view_container.stopShimmer();
        try {
            BusProvider.getInstance().unregister(this);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}