package com.naijaunik.kuteb.Fragments;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.naijaunik.kuteb.Activities.CourseLessonViewer;
import com.naijaunik.kuteb.Activities.Login;
import com.naijaunik.kuteb.Activities.UpgradePlans;
import com.naijaunik.kuteb.Adapters.VideosAdapter;
import com.naijaunik.kuteb.Api.BusProvider;
import com.naijaunik.kuteb.Api.Communicator;
import com.naijaunik.kuteb.Api.ErrorEvent;
import com.naijaunik.kuteb.Api.ServerEvent;
import com.naijaunik.kuteb.Api.ServerResponse;
import com.naijaunik.kuteb.Model.VideosModel;
import com.naijaunik.kuteb.R;
import com.naijaunik.kuteb.Utils.AppSession;
import com.naijaunik.kuteb.Utils.Utilities;
import com.squareup.otto.Subscribe;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import static com.naijaunik.kuteb.Utils.AppConstants.DATE_TIME_FORMAT;

public class VideosFragment extends Fragment implements VideosAdapter.VideosAdapterListener {

    private Utilities utilities;
    private AppSession appSession;
    private Communicator communicator;
    private JsonObject userObj;
    private Toolbar toolbar;
    RecyclerView videos_recyclerview;
    LinearLayout error_layout,no_data_found_layout;
    ShimmerFrameLayout shimmer_view_container;
    private boolean isAdmin;
    private ArrayList<VideosModel> videoList;
    private ServerResponse serverResponse;
    private VideosAdapter videosAdapter;
    TextView error_layout_text;
    MaterialButton upgrade_btn;
    private SearchView searchView;

    public VideosFragment() {
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
        View videosFragmentView =  inflater.inflate(R.layout.fragment_videos, container, false);
        setHasOptionsMenu(true);

        toolbar = videosFragmentView.findViewById(R.id.toolbar);
        ((AppCompatActivity) Objects.requireNonNull(getActivity())).setSupportActionBar(toolbar);
        Objects.requireNonNull(((AppCompatActivity) getActivity()).getSupportActionBar()).setTitle("Videos");


        //init fields
        initFields(videosFragmentView);

        return  videosFragmentView;
    }

    private void initFields(View videosFragmentView) {

        isAdmin = utilities.cleanData(userObj.get("status")).equals("admin");

        videos_recyclerview = videosFragmentView.findViewById(R.id.videos_recyclerview);
        error_layout = videosFragmentView.findViewById(R.id.error_layout);
        no_data_found_layout = videosFragmentView.findViewById(R.id.no_data_found_layout);
        shimmer_view_container = videosFragmentView.findViewById(R.id.shimmer_view_container);
        error_layout_text = videosFragmentView.findViewById(R.id.error_layout_text);
        upgrade_btn = videosFragmentView.findViewById(R.id.upgrade_btn);

        videos_recyclerview.setHasFixedSize(true);
        LinearLayoutManager coursesLayoutManager = new LinearLayoutManager(getContext());
        videos_recyclerview.setLayoutManager(coursesLayoutManager);
        videos_recyclerview.setItemAnimator(new DefaultItemAnimator());

        //fill list screen
        videoList = new ArrayList<>();

        if(!isAdmin && appSession.isUserPlanExpired()) {

                videos_recyclerview.setVisibility(View.GONE);
                shimmer_view_container.setVisibility(View.GONE);
                error_layout.setVisibility(View.VISIBLE);
                upgrade_btn.setVisibility(View.VISIBLE);

                error_layout_text.setText(R.string.upgrade_your_plan_text);


        }else{

            showVideos();
        }

        upgrade_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(getContext(),UpgradePlans.class));
                getActivity().overridePendingTransition(R.anim.right_in,R.anim.left_out);
            }
        });

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

            } else if (serverResponse.getType().equals("videos")) {

                if (serverResponse.getResult().equals("success")) {

                    JsonArray videosData = serverResponse.getData();

                    if (videosData != null) {

                        appSession.storeVideos(videosData);

                        if (!isAdmin && appSession.isUserPlanExpired()) {

                            videos_recyclerview.setVisibility(View.GONE);
                            shimmer_view_container.setVisibility(View.GONE);
                            error_layout.setVisibility(View.VISIBLE);
                            upgrade_btn.setVisibility(View.VISIBLE);

                            error_layout_text.setText(R.string.upgrade_your_plan_text);


                        } else {

                            showVideos();
                        }

                    } else if (appSession.getVideos() == null) {

                        videos_recyclerview.setVisibility(View.GONE);
                        error_layout.setVisibility(View.GONE);
                        no_data_found_layout.setVisibility(View.VISIBLE);
                    }

                } else {

                    if (appSession.getVideos() != null) {

                        videos_recyclerview.setVisibility(View.VISIBLE);

                        if (!isAdmin && appSession.isUserPlanExpired()) {

                            videos_recyclerview.setVisibility(View.GONE);
                            shimmer_view_container.setVisibility(View.GONE);
                            error_layout.setVisibility(View.VISIBLE);
                            upgrade_btn.setVisibility(View.VISIBLE);

                            error_layout_text.setText(R.string.upgrade_your_plan_text);


                        } else {

                            showVideos();

                        }

                    } else {

                        //show error layout
                        error_layout.setVisibility(View.VISIBLE);
                        no_data_found_layout.setVisibility(View.GONE);

                    }
                }

            }

        }

    }

    private void showVideos() {

        shimmer_view_container.setVisibility(View.GONE);

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

                //add data to list
                videoList.add(new VideosModel(id,course_id,course_title,title,content,video_url,
                        video_from,external_link,attachment,attachment_extension));
            }


            if(videoList.size() == 0){

                videos_recyclerview.setVisibility(View.GONE);
                error_layout.setVisibility(View.GONE);
                no_data_found_layout.setVisibility(View.VISIBLE);
            }


            videosAdapter = new VideosAdapter(getContext(), videoList,this,getLifecycle());

            videos_recyclerview.setAdapter(videosAdapter);

        }catch (Exception e){

            e.printStackTrace();

            no_data_found_layout.setVisibility(View.VISIBLE);
            error_layout.setVisibility(View.GONE);
            videos_recyclerview.setVisibility(View.GONE);

        }

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

        inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.search_and_add_menu, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        MenuItem addExam = menu.findItem(R.id.action_add);


        if(isAdmin){

            addExam.setVisible(false);
            addExam.setTitle("Add Video");
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

                    videosAdapter.getFilter().filter(query);

                }catch (Exception e){
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                try{

                    videosAdapter.getFilter().filter(newText);

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
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onVideoSelected(VideosModel course, int position) {

        if(!isAdmin){

            if(appSession.isUserPlanExpired()){

                //user's plan has expired so take them to Upgrade Plan screen
                startActivity(new Intent(getContext(), UpgradePlans.class));
                getActivity().overridePendingTransition(R.anim.right_in, R.anim.left_out);


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
                getActivity().overridePendingTransition(R.anim.right_in, R.anim.left_out);

            }

        }else {

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
            getActivity().overridePendingTransition(R.anim.right_in, R.anim.left_out);
        }

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

        Log.e("VideosFragment: ",errorEvent.getErrorMsg());

        shimmer_view_container.setVisibility(View.GONE);

        try {

            if(appSession.getVideos() != null){

                videos_recyclerview.setVisibility(View.VISIBLE);

                if(!isAdmin && appSession.isUserPlanExpired()) {

                    videos_recyclerview.setVisibility(View.GONE);
                    shimmer_view_container.setVisibility(View.GONE);
                    error_layout.setVisibility(View.VISIBLE);
                    upgrade_btn.setVisibility(View.VISIBLE);

                    error_layout_text.setText(R.string.upgrade_your_plan_text);


                }else{

                    showVideos();

                }

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

    private void getVideos() {

        communicator.nonParametrizedCall("getvideos",getContext());
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
        getVideos();

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