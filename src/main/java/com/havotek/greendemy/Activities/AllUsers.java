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
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.naijaunik.kuteb.Adapters.AllUsersAdapter;
import com.naijaunik.kuteb.Api.BusProvider;
import com.naijaunik.kuteb.Api.Communicator;
import com.naijaunik.kuteb.Api.ErrorEvent;
import com.naijaunik.kuteb.Api.ServerEvent;
import com.naijaunik.kuteb.Api.ServerResponse;
import com.naijaunik.kuteb.Model.UserModel;
import com.naijaunik.kuteb.R;
import com.naijaunik.kuteb.Utils.AppSession;
import com.naijaunik.kuteb.Utils.Utilities;
import com.squareup.otto.Subscribe;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.naijaunik.kuteb.Utils.AppConstants.DATE_TIME_FORMAT;

public class AllUsers extends AppCompatActivity implements AllUsersAdapter.UsersAdapterListener {

    RecyclerView all_users_recycler_view;
    LinearLayout error_layout,no_data_found_layout;
    ShimmerFrameLayout shimmer_view_container;
    private SearchView searchView;
    private Communicator communicator;
    private ArrayList<UserModel> usersList;
    private ProgressDialog dialog;
    private Utilities utilities;
    private AppSession appSession;
    private JsonObject userObj;
    private ServerResponse serverResponse;
    private AllUsersAdapter allUsersAdapter;
    private Toolbar toolbar;
    private String setNumberOfDays;
    private UserModel selectedUserObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);

        utilities = Utilities.getInstance(this);
        appSession = appSession.getInstance(this);
        userObj = appSession.getUser();
        communicator = new Communicator();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("All Users");

        initFields();
    }

    private void initFields() {

        dialog = utilities.showLoadingDialog("Deleting User...","");

        all_users_recycler_view = findViewById(R.id.all_users_recycler_view);
        shimmer_view_container = findViewById(R.id.shimmer_view_container);
        error_layout = findViewById(R.id.error_layout);
        no_data_found_layout = findViewById(R.id.no_data_found_layout);

        all_users_recycler_view.setHasFixedSize(true);
        LinearLayoutManager notifsLayoutManager = new LinearLayoutManager(this);
        all_users_recycler_view.setLayoutManager(notifsLayoutManager);
        all_users_recycler_view.setItemAnimator(new DefaultItemAnimator());

        usersList = new ArrayList<>();

       getAllUsers();
       getPlans();
    }

    private void getAllUsers() {

        communicator.nonParametrizedCall("getallusers",this);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_and_add_menu, menu);

        MenuItem item = menu.findItem(R.id.action_search);


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

                    allUsersAdapter.getFilter().filter(query);

                }catch (Exception e){
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                try{

                    allUsersAdapter.getFilter().filter(newText);

                }catch (Exception e){
                    e.printStackTrace();
                }
                return false;
            }
        });

        return  super.onCreateOptionsMenu(menu);

    }

    @Subscribe
    public void onServerEvent(ServerEvent serverEvent){

        serverResponse = serverEvent.getServerResponse();

        if(serverResponse != null) {

            if (serverResponse.getType().equals("getallusers")) {

                if (serverResponse.getResult().equals("success")) {

                    JsonArray usersData = serverResponse.getData();

                    if (usersData != null) {

                        //storing the user in shared preferences

                        displayUsers(usersData);

                    } else{

                        //show error layout
                        error_layout.setVisibility(View.VISIBLE);
                        no_data_found_layout.setVisibility(View.GONE);
                        all_users_recycler_view.setVisibility(View.GONE);
                    }

                } else {

                    //show error layout
                    error_layout.setVisibility(View.VISIBLE);
                    no_data_found_layout.setVisibility(View.GONE);
                    all_users_recycler_view.setVisibility(View.GONE);

                }

                //dismiss dialog loader
                dialog.dismiss();

            }else if(serverResponse.getType().equals("deleteuser")){

                if (serverResponse.getResult().equals("success")) {

                    utilities.dialogError(this, getString(R.string.user_deleted_successfully));

                }else{

                    utilities.dialogError(this,getString(R.string.error_occurred));
                }

                //dismiss dialog loader
                dialog.dismiss();

            }else if(serverResponse.getType().equals("changeuserstatus")){

                if (serverResponse.getResult().equals("success")) {

                    utilities.dialogError(this, getString(R.string.status_updated_successfully));

                }else{

                    utilities.dialogError(this,getString(R.string.error_occurred));
                }

                //dismiss dialog loader
                dialog.dismiss();

            }else if(serverResponse.getType().equals("blockunblockuser")){

                if (serverResponse.getResult().equals("success")) {

                    utilities.dialogError(this, getString(R.string.accessbility_status_updated_successfully));

                }else{

                    utilities.dialogError(this,getString(R.string.error_occurred));
                }

                //dismiss dialog loader
                dialog.dismiss();

            }else if(serverResponse.getType().equals("awardfreesub")){

                if (serverResponse.getResult().equals("success")) {

                    utilities.dialogError(this, "Free subscription awarded to " +
                            selectedUserObj.getFirst_name() + " "+ selectedUserObj.getLast_name() +
                            " sucessfully!");

                }else{

                    utilities.dialogError(this,getString(R.string.error_occurred));
                }

                //dismiss dialog loader
                dialog.dismiss();

            }else if (serverResponse.getType().equals("getplans")) {

                if (serverResponse.getResult().equals("success")) {

                    JsonArray plansData = serverResponse.getData();

                    if (plansData != null) {

                        //store plans to pref
                        appSession.storePlans(plansData);

                    }
                }

            }
        }
    }

    @Subscribe
    public void onErrorEvent(ErrorEvent errorEvent){

        Log.e("CourseSection: ",errorEvent.getErrorMsg());

        shimmer_view_container.setVisibility(View.GONE);

        error_layout.setVisibility(View.VISIBLE);
        no_data_found_layout.setVisibility(View.GONE);
        all_users_recycler_view.setVisibility(View.GONE);

        dialog.dismiss();

    }

    private void displayUsers(JsonArray sectionData) {

        shimmer_view_container.setVisibility(View.GONE);

        //clear book array list to avoid repeatition of data
        usersList.clear();

        try {

            for (JsonElement element : sectionData) {
                String id = Utilities.cleanData(((JsonObject) element).get("id"));
                String phone = Utilities.cleanData(((JsonObject) element).get("phone"));
                String is_blocked = Utilities.cleanData(((JsonObject) element).get("is_blocked"));
                String first_name = Utilities.cleanData(((JsonObject) element).get("first_name"));
                String last_name = Utilities.cleanData(((JsonObject) element).get("last_name"));
                String is_trial = Utilities.cleanData(((JsonObject) element).get("is_trial"));
                String is_paid = Utilities.cleanData(((JsonObject) element).get("is_paid"));
                String current_expiration_date = Utilities.cleanData(((JsonObject) element).get("current_expiration_date"));
                String date_registered = Utilities.cleanData(((JsonObject) element).get("date_registered"));
                String email = Utilities.cleanData(((JsonObject) element).get("email"));
                String status = Utilities.cleanData(((JsonObject) element).get("status"));
                String userimage = Utilities.cleanData(((JsonObject) element).get("userimage"));
                String gender = Utilities.cleanData(((JsonObject) element).get("gender"));

                //add data to list
                usersList.add(new UserModel(id,phone,is_blocked,first_name,
                        last_name,is_trial,is_paid,current_expiration_date,date_registered,email,
                        status,userimage,gender));
            }


            if(usersList.size() == 0){

                all_users_recycler_view.setVisibility(View.GONE);
                error_layout.setVisibility(View.GONE);
                no_data_found_layout.setVisibility(View.VISIBLE);
            }


            allUsersAdapter = new AllUsersAdapter(this,
                    usersList,this);

            all_users_recycler_view.setAdapter(allUsersAdapter);

        }catch (Exception e){

            e.printStackTrace();

            no_data_found_layout.setVisibility(View.VISIBLE);
            error_layout.setVisibility(View.GONE);
            all_users_recycler_view.setVisibility(View.GONE);

        }
    }

    @Override
    public void onUserSelected(final UserModel user, final int position) {

        selectedUserObj = user;

        if(!user.getPhone().equals(utilities.cleanData(userObj.get("phone")))) {

            final boolean isAdmin = user.getStatus().equals("admin");

            final String newStatus = isAdmin ? "Student" : "Admin";

            final boolean isBlocked = Integer.parseInt(user.getIs_blocked()) == 1;
            final String blockStatus = isBlocked ? "Unblock" : "Block";

            CharSequence options_image[] = new CharSequence[]{
                    "Make " + newStatus,
                    blockStatus,
                    "Award Free Subscription",
                    "Delete",
                    "Cancel"
            };
            AlertDialog.Builder builderoptions_image = new AlertDialog.Builder(this);
            builderoptions_image.setTitle("User Options");

            builderoptions_image.setItems(options_image, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    if (which == 0) {

                            AlertDialog.Builder builder = new AlertDialog.Builder(AllUsers.this);
                            builder.setMessage("Are you sure you want to make this user " + newStatus + "?");
                            builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    changeUserStatus(user.getPhone(), newStatus.toLowerCase(), position);
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


                    } else if (which == 1) {

                            AlertDialog.Builder builder = new AlertDialog.Builder(AllUsers.this);
                            builder.setMessage("Are you sure you want to " + blockStatus + " this user?");
                            builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    blockUnblockUser(user.getPhone(), isBlocked ? 0 : 1, position);
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


                    } else if(which == 2){

                        CharSequence options_image[] = new CharSequence[]{
                                "From Current Plans",
                                "With Number Of Days",
                                "Cancel"
                        };
                        AlertDialog.Builder free_sub_dialog = new AlertDialog.Builder(AllUsers.this);
                        free_sub_dialog.setTitle("Subscription Options");

                        free_sub_dialog.setItems(options_image, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which_free_sub) {

                                if(which_free_sub == 0){

                                    //show select dialog of all the current plans

                                    JsonArray plansObj = appSession.getPlans();

                                    ArrayList<String> stringArrayList = new ArrayList<String>();

                                    for (int i = 0; i<plansObj.size(); i++) {

                                        String planName = Utilities.cleanData(plansObj.get(i).getAsJsonObject().get("plan_name"));
                                        String planDurationInDays = Utilities.cleanData(plansObj.
                                                get(i).getAsJsonObject().get("plan_duration_in_days"));

                                        stringArrayList.add(planName+" ("+planDurationInDays+" days"+")");

                                    }

                                    final String [] options_plans = stringArrayList.toArray(new String[stringArrayList.size()]);


                                    AlertDialog.Builder planssub_dialog = new AlertDialog.Builder(AllUsers.this);
                                    planssub_dialog.setTitle("Plans Options");

                                    planssub_dialog.setItems(options_plans, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which_free_sub) {

                                           //extract no of days from selected plan
                                            String planSelected = options_plans[which_free_sub];

                                            String textInBracket = planSelected.substring(planSelected.indexOf("(")+1,
                                                    planSelected.indexOf(")"));

                                            final String noOfDays = utilities.substringBetween(textInBracket,"","days").trim();

                                            AlertDialog.Builder builder = new AlertDialog.Builder(AllUsers.this);
                                            builder.setMessage("Are you sure you want to award " +
                                                    noOfDays + " free days to this user?");
                                            builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            }).setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                    //get remaining number of days before user expiration
                                                    String currentUserExpirationDate =
                                                            utilities.cleanString(user.getCurrent_expiration_date());

                                                    SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_FORMAT);
                                                    Date strDate;

                                                    try {

                                                        strDate = sdf.parse(currentUserExpirationDate);

                                                        long oldNumberOfDaysRemaining =
                                                                getExpirationDateDifferenc(strDate.getTime());

                                                        long newNoOfDays = oldNumberOfDaysRemaining +
                                                                Long.parseLong(noOfDays);

                                                        awardFreeSub(user.getPhone(),
                                                                position,newNoOfDays);

                                                    }catch (Exception e){

                                                        e.printStackTrace();
                                                    }

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

                                    });

                                    planssub_dialog.show();


                                }else{

                                    //prompt the admin to enter the number of days to award to this user

                                    new MaterialDialog.Builder(AllUsers.this)
                                            .title(R.string.enter_number_of_days)
                                            .content(R.string.award_free_sub_content)
                                            .inputType(InputType.TYPE_CLASS_NUMBER)
                                            .inputRangeRes(1, 10, R.color.red)
                                            .input(null, null, new MaterialDialog.InputCallback() {
                                                @Override
                                                public void onInput(MaterialDialog dialog, CharSequence input) {
                                                    // Do something
                                                    setNumberOfDays = input.toString().trim();
                                                }
                                            })
                                            .showListener(new DialogInterface.OnShowListener() {
                                                @Override
                                                public void onShow(DialogInterface dialog) {

                                                    setNumberOfDays = "";
                                                }
                                            })
                                            .cancelListener(new DialogInterface.OnCancelListener() {
                                                @Override
                                                public void onCancel(DialogInterface dialog) {

                                                    setNumberOfDays = "";

                                                }
                                            })
                                            .dismissListener(new DialogInterface.OnDismissListener() {
                                                @Override
                                                public void onDismiss(DialogInterface dialog) {

                                                    if(!setNumberOfDays.isEmpty()){


                                                        AlertDialog.Builder builder = new AlertDialog.Builder(AllUsers.this);
                                                        builder.setMessage("Are you sure you want to award " +
                                                                setNumberOfDays + " free days to this user?");
                                                        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                dialog.dismiss();
                                                            }
                                                        }).setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {

                                                                //get remaining number of days before user expiration
                                                                String currentUserExpirationDate =
                                                                        utilities.cleanString(user.getCurrent_expiration_date());

                                                                SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_FORMAT);
                                                                Date strDate;

                                                                try {

                                                                    strDate = sdf.parse(currentUserExpirationDate);

                                                                    long oldNumberOfDaysRemaining =
                                                                            getExpirationDateDifferenc(strDate.getTime());

                                                                    long newNoOfDays = oldNumberOfDaysRemaining +
                                                                            Long.parseLong(setNumberOfDays);

                                                                    awardFreeSub(user.getPhone(),
                                                                            position,newNoOfDays);

                                                                }catch (Exception e){

                                                                    e.printStackTrace();
                                                                }

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
                                            })
                                            .show();

                                }

                            }
                        });

                        free_sub_dialog.show();

                    }else if (which == 3) {

                            AlertDialog.Builder builder = new AlertDialog.Builder(AllUsers.this);
                            builder.setMessage("Are you sure you want to delete this user?");
                            builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    DeleteUser(user.getPhone(), position);
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

    private void awardFreeSub(String phone, int position, long newNoOfDays) {

        dialog = Utilities.showLoadingDialog("Awarding free subscription"," a moment please");

        dialog.show();

        communicator.changeUserStatus(phone, String.valueOf(newNoOfDays),"awardfreesub");

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

    private void blockUnblockUser(String phone, int newBlockStatus, int position) {

        dialog = utilities.showLoadingDialog(newBlockStatus == 1 ? "Unblocking" : "Blocking" + " user","");

        dialog.show();

        communicator.changeUserStatus(phone, String.valueOf(newBlockStatus),"blockunblockuser");

        usersList.get(position).setIs_blocked(String.valueOf(newBlockStatus));
        allUsersAdapter.notifyItemChanged(position);

    }

    private void changeUserStatus(String phone, String newStatus, int position) {

        dialog = utilities.showLoadingDialog("Changing user's status","");

        dialog.show();

        communicator.changeUserStatus(phone, newStatus,"changeuserstatus");

        usersList.get(position).setStatus(newStatus);
        allUsersAdapter.notifyItemChanged(position);
    }

    private void DeleteUser(String phone, int position) {

        dialog.show();

        communicator.singleParametrizedCall(phone,"deleteuser");

        usersList.remove(position);
        allUsersAdapter.notifyItemRemoved(position);
    }

    @SuppressLint("NewApi")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
            overridePendingTransition(R.anim.left_in, R.anim.right_out);

        }

        return super.onOptionsItemSelected(item);
    }

    private void getPlans() {

        communicator.nonParametrizedCall("getplans",AllUsers.this);
    }

    @Override
    public void onResume(){
        super.onResume();
        getAllUsers();
        getPlans();
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

        finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }

}