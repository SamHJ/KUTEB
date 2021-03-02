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
import com.naijaunik.kuteb.Activities.AddExam;
import com.naijaunik.kuteb.Activities.Login;
import com.naijaunik.kuteb.Activities.UpdateExam;
import com.naijaunik.kuteb.Activities.UpgradePlans;
import com.naijaunik.kuteb.Activities.ViewAndTakeExam;
import com.naijaunik.kuteb.Adapters.ExamsAdapter;
import com.naijaunik.kuteb.Api.BusProvider;
import com.naijaunik.kuteb.Api.Communicator;
import com.naijaunik.kuteb.Api.ErrorEvent;
import com.naijaunik.kuteb.Api.ServerEvent;
import com.naijaunik.kuteb.Api.ServerResponse;
import com.naijaunik.kuteb.Model.ExamsModel;
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

public class ExamsFragment extends Fragment implements ExamsAdapter.ExamsAdapterListener {


    private Utilities utilities;
    private AppSession appSession;
    private Communicator communicator;
    private JsonObject userObj;
    private Toolbar toolbar;
    private ShimmerFrameLayout shimmer_view_container;
    private LinearLayout no_data_found_layout,error_layout;
    private RecyclerView exams_recyclerview;
    private MaterialButton upgrade_btn;
    private TextView error_layout_text;
    private boolean isAdmin;
    private ArrayList<ExamsModel> examsList;
    private ExamsAdapter adapter;
    private ServerResponse serverResponse;
    private ProgressDialog dialog;
    private SearchView searchView;

    public ExamsFragment() {
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
        View examsFragmentView = inflater.inflate(R.layout.fragment_exams, container, false);
        setHasOptionsMenu(true);

        toolbar = examsFragmentView.findViewById(R.id.toolbar);
        ((AppCompatActivity) Objects.requireNonNull(getActivity())).setSupportActionBar(toolbar);
        Objects.requireNonNull(((AppCompatActivity) getActivity()).getSupportActionBar()).setTitle("Exams");

        initFields(examsFragmentView);

        return examsFragmentView;
    }

    private void initFields(View examsFragmentView) {

        isAdmin = utilities.cleanData(userObj.get("status")).equals("admin");

        dialog = utilities.showLoadingDialog("Deleting exam","a moment please...");

        shimmer_view_container = examsFragmentView.findViewById(R.id.shimmer_view_container);
        no_data_found_layout = examsFragmentView.findViewById(R.id.no_data_found_layout);
        error_layout = examsFragmentView.findViewById(R.id.error_layout);
        exams_recyclerview = examsFragmentView.findViewById(R.id.exams_recyclerview);
        upgrade_btn = examsFragmentView.findViewById(R.id.upgrade_btn);
        error_layout_text = examsFragmentView.findViewById(R.id.error_layout_text);

        exams_recyclerview.setHasFixedSize(true);
        LinearLayoutManager coursesLayoutManager = new LinearLayoutManager(getContext());
        exams_recyclerview.setLayoutManager(coursesLayoutManager);
        exams_recyclerview.setItemAnimator(new DefaultItemAnimator());

        //fill list screen
        examsList = new ArrayList<>();

        if(!isAdmin && appSession.isUserPlanExpired()) {

            exams_recyclerview.setVisibility(View.GONE);
            shimmer_view_container.setVisibility(View.GONE);
            error_layout.setVisibility(View.VISIBLE);
            upgrade_btn.setVisibility(View.VISIBLE);

            error_layout_text.setText(R.string.upgrade_your_plan_text);


        }else{

            showExams();
        }

        upgrade_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(getContext(), UpgradePlans.class));
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

            } else if (serverResponse.getType().equals("getexams")) {

                if (serverResponse.getResult().equals("success")) {

                    JsonArray examsData = serverResponse.getData();

                    if (examsData != null) {

                        appSession.storeExams(examsData);

                        if (!isAdmin && appSession.isUserPlanExpired()) {

                            exams_recyclerview.setVisibility(View.GONE);
                            shimmer_view_container.setVisibility(View.GONE);
                            error_layout.setVisibility(View.VISIBLE);
                            upgrade_btn.setVisibility(View.VISIBLE);

                            error_layout_text.setText(R.string.upgrade_your_plan_text);


                        } else {

                            showExams();
                        }

                    } else if (appSession.getExams() == null) {

                        exams_recyclerview.setVisibility(View.GONE);
                        error_layout.setVisibility(View.GONE);
                        no_data_found_layout.setVisibility(View.VISIBLE);
                    }

                } else {

                    if (appSession.getExams() != null) {

                        exams_recyclerview.setVisibility(View.VISIBLE);

                        if (!isAdmin && appSession.isUserPlanExpired()) {

                            exams_recyclerview.setVisibility(View.GONE);
                            shimmer_view_container.setVisibility(View.GONE);
                            error_layout.setVisibility(View.VISIBLE);
                            upgrade_btn.setVisibility(View.VISIBLE);

                            error_layout_text.setText(R.string.upgrade_your_plan_text);


                        } else {

                            showExams();

                        }

                    } else {

                        //show error layout
                        error_layout.setVisibility(View.VISIBLE);
                        no_data_found_layout.setVisibility(View.GONE);

                    }
                }

            }else if(serverResponse.getType().equals("delete_exam")){

                if (serverResponse.getResult().equals("success")) {

                    utilities.dialogError(getContext(), getString(R.string.exam_deleted_successfully));

                } else {

                    utilities.dialogError(getContext(), getString(R.string.error_occurred));
                }

                //dismiss dialog loader
                dialog.dismiss();

            }

        }

    }

    private void showExams() {

        shimmer_view_container.setVisibility(View.GONE);

        //clear book array list to avoid repeatition of data
        examsList.clear();

        try {

            for (JsonElement element : appSession.getExams()) {
                String id = Utilities.cleanData(((JsonObject) element).get("id"));
                String exam_title = Utilities.cleanData(((JsonObject) element).get("exam_title"));
                String no_of_questions = Utilities.cleanData(((JsonObject) element).get("no_of_questions"));
                String is_subject_wise = Utilities.cleanData(((JsonObject) element).get("is_subject_wise"));

                //add data to list
                examsList.add(new ExamsModel(id,exam_title,no_of_questions,is_subject_wise));
            }


            if(examsList.size() == 0){

                exams_recyclerview.setVisibility(View.GONE);
                error_layout.setVisibility(View.GONE);
                no_data_found_layout.setVisibility(View.VISIBLE);
            }


            adapter = new ExamsAdapter(getContext(), examsList,this);

            exams_recyclerview.setAdapter(adapter);

        }catch (Exception e){

            e.printStackTrace();

            no_data_found_layout.setVisibility(View.VISIBLE);
            error_layout.setVisibility(View.GONE);
            exams_recyclerview.setVisibility(View.GONE);

        }

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

        inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.search_and_add_menu, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        MenuItem addExam = menu.findItem(R.id.action_add);


        if(isAdmin){

            addExam.setVisible(true);
            addExam.setTitle("Add Exam");
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

                    adapter.getFilter().filter(query);

                }catch (Exception e){
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                try{

                    adapter.getFilter().filter(newText);

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

            startActivity(new Intent(getContext(), AddExam.class));
            getActivity().overridePendingTransition(R.anim.right_in, R.anim.left_out);
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onExamSelected(final ExamsModel exam, final int position) {

        final boolean isExamSubjectWise = exam.getIs_subject_wise().equals("1");

        if(!isAdmin){

            if(appSession.isUserPlanExpired()){

                //user's plan has expired so take them to Upgrade Plan screen
                startActivity(new Intent(getContext(), UpgradePlans.class));
                getActivity().overridePendingTransition(R.anim.right_in, R.anim.left_out);


            }else {

                //user's plan is still valid

                // view exam sections
                Intent viewCourseIntent;

                viewCourseIntent = new Intent(getContext(), ViewAndTakeExam.class);

                viewCourseIntent.putExtra("id", exam.getId());
                viewCourseIntent.putExtra("exam_title",exam.getExam_title());
                viewCourseIntent.putExtra("is_subject_wise",exam.getIs_subject_wise());
                startActivity(viewCourseIntent);
                getActivity().overridePendingTransition(R.anim.right_in, R.anim.left_out);
            }

        }else {

            //show options
            CharSequence options_image[] = new CharSequence[]{
                    "View Exam",
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

                        // view exam sections
                        Intent viewCourseIntent;

                        viewCourseIntent = new Intent(getContext(), ViewAndTakeExam.class);

                        viewCourseIntent.putExtra("id", exam.getId());
                        viewCourseIntent.putExtra("exam_title",exam.getExam_title());
                        viewCourseIntent.putExtra("is_subject_wise",exam.getIs_subject_wise());
                        startActivity(viewCourseIntent);
                        getActivity().overridePendingTransition(R.anim.right_in, R.anim.left_out);

                    }else if (which == 1) {

                        Intent viewCourseIntent = new Intent(getContext(), UpdateExam.class);
                        viewCourseIntent.putExtra("id", exam.getId());
                        viewCourseIntent.putExtra("exam_title",exam.getExam_title());
                        viewCourseIntent.putExtra("is_subject_wise",exam.getIs_subject_wise());
                        startActivity(viewCourseIntent);
                        getActivity().overridePendingTransition(R.anim.right_in, R.anim.left_out);

                    } else if (which == 2) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setMessage("Are you sure you want to delete this exam? All of it's contents would be deleted also!");
                        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                DeleteExam(exam.getId(), position);
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

    private void DeleteExam(String id, int position) {

        dialog.show();

        communicator.singleParametrizedCall(id,"delete_exam");

        examsList.remove(position);
        adapter.notifyItemRemoved(position);
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

        Log.e("ExamsFragment: ",errorEvent.getErrorMsg());

        shimmer_view_container.setVisibility(View.GONE);

        try {

            if(appSession.getExams() != null){

                exams_recyclerview.setVisibility(View.VISIBLE);

                if(!isAdmin && appSession.isUserPlanExpired()) {

                    exams_recyclerview.setVisibility(View.GONE);
                    shimmer_view_container.setVisibility(View.GONE);
                    error_layout.setVisibility(View.VISIBLE);
                    upgrade_btn.setVisibility(View.VISIBLE);

                    error_layout_text.setText(R.string.upgrade_your_plan_text);


                }else{

                    showExams();

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

    private void getExams() {

        communicator.nonParametrizedCall("get_exams",getContext());
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
        getExams();

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