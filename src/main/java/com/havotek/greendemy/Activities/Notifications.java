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
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.naijaunik.kuteb.Adapters.NotificationsAdapter;
import com.naijaunik.kuteb.Api.BusProvider;
import com.naijaunik.kuteb.Api.Communicator;
import com.naijaunik.kuteb.Api.ErrorEvent;
import com.naijaunik.kuteb.Api.ServerEvent;
import com.naijaunik.kuteb.Api.ServerResponse;
import com.naijaunik.kuteb.Model.NotifsModel;
import com.naijaunik.kuteb.R;
import com.naijaunik.kuteb.Utils.AppSession;
import com.naijaunik.kuteb.Utils.Utilities;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Notifications extends AppCompatActivity implements NotificationsAdapter.NotifsAdapterListener {

    Toolbar toolbar;
    AppSession appSession;
    Utilities utilities;
    private ProgressDialog dialog;
    private ServerResponse serverResponse;
    JsonObject userObj;
    NotificationsAdapter notifsAdapter;
    SearchView searchView;
    RecyclerView notif_recycler_view;
    Communicator communicator;
    ShimmerFrameLayout shimmer_view_container;
    LinearLayout error_layout,no_data_found_layout;
    List<NotifsModel> notifList;
    private boolean  isExecuted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        communicator = new Communicator();

        utilities = Utilities.getInstance(this);
        appSession = AppSession.getInstance(this);
        userObj = appSession.getUser();

        if(!utilities.cleanData(userObj.get("status")).equals("admin")){

            //prevent screen capture
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        }

        setContentView(R.layout.activity_notifications);

        //fill list screen
        notifList = new ArrayList<>();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Notifications");

        //initFields
        initFields();
    }

    private void initFields() {

        dialog = utilities.showLoadingDialog("Deleting notification...","");

        notif_recycler_view = findViewById(R.id.notif_recycler_view);

        notif_recycler_view.setHasFixedSize(true);
        LinearLayoutManager notifsLayoutManager = new LinearLayoutManager(this);
        notif_recycler_view.setLayoutManager(notifsLayoutManager);
        notif_recycler_view.setItemAnimator(new DefaultItemAnimator());
        shimmer_view_container = findViewById(R.id.shimmer_view_container);
        error_layout = findViewById(R.id.error_layout);
        no_data_found_layout = findViewById(R.id.no_data_found_layout);

        shimmer_view_container.startShimmer();

        isExecuted = true;

        //fetch Notifications
        communicator.singleParametrizedCall(utilities.cleanData(userObj.get("id")),"get_notifications");

        //mark current user's notifications as read/is seen
        communicator.singleParametrizedCall(utilities.cleanData(userObj.get("id")),"mark_notif_isseen");
    }


    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_and_add_menu, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        MenuItem addCourses = menu.findItem(R.id.action_add);

        if(utilities.cleanData(userObj.get("status")).equals("admin")){

            addCourses.setVisible(true);
            addCourses.setTitle("Add Notification");
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

                    notifsAdapter.getFilter().filter(query);

                }catch (Exception e){
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                try{

                    notifsAdapter.getFilter().filter(newText);

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

            startActivity(new Intent(Notifications.this, AddNotification.class));
            overridePendingTransition(R.anim.right_in, R.anim.left_out);
        }

        return super.onOptionsItemSelected(item);
    }

    @Subscribe
    public void onServerEvent(ServerEvent serverEvent){

        if(isExecuted) {

            serverResponse = serverEvent.getServerResponse();

            if (serverResponse != null) {

                if (serverResponse.getType().equals("getnotif")) {

                    if (serverResponse.getResult().equals("success")) {

                        JsonArray notifData = serverResponse.getData();

                        if (notifData != null) {

                            //storing the user in shared preferences
                            AppSession.getInstance(this).storeNotifications(notifData);

                            displayNotifications();

                        } else if (appSession.getNotifications() == null) {

                            notif_recycler_view.setVisibility(View.GONE);
                            error_layout.setVisibility(View.GONE);
                            no_data_found_layout.setVisibility(View.VISIBLE);
                        }

                    } else {

                        if (appSession.getNotifications() != null) {

                            notif_recycler_view.setVisibility(View.VISIBLE);
                            displayNotifications();

                        } else {

                            //show error layout
                            error_layout.setVisibility(View.VISIBLE);
                            no_data_found_layout.setVisibility(View.GONE);

                        }
                    }

                } else if (serverResponse.getType().equals("deletenotif")) {

                    if (serverResponse.getResult().equals("success")) {

                        utilities.dialogError(this, getString(R.string.notif_deleted_successfully));

                    } else {

                        utilities.dialogError(this, getString(R.string.error_occurred));
                    }

                    //dismiss dialog loader
                    dialog.dismiss();

                }
            }

        }
    }

    private void displayNotifications() {

        shimmer_view_container.setVisibility(View.GONE);

        //clear book array list to avoid repeatition of data
        notifList.clear();

       try {

           for (JsonElement element : appSession.getNotifications()) {
               String id = Utilities.cleanData(((JsonObject) element).get("id"));
               String title = Utilities.cleanData(((JsonObject) element).get("title"));
               String description = Utilities.cleanData(((JsonObject) element).get("description"));
               String date = Utilities.cleanData(((JsonObject) element).get("date"));
               String user_id = Utilities.cleanData(((JsonObject) element).get("user_id"));
               String is_seen = Utilities.cleanData(((JsonObject) element).get("is_seen"));

               //add data to list
               notifList.add(new NotifsModel(id,title, description,date,user_id,is_seen));
           }


           if(notifList.size() == 0){

               notif_recycler_view.setVisibility(View.GONE);
               error_layout.setVisibility(View.GONE);
               no_data_found_layout.setVisibility(View.VISIBLE);
           }


           notifsAdapter = new NotificationsAdapter(Notifications.this, notifList,Notifications.this);

           notif_recycler_view.setAdapter(notifsAdapter);

       }catch (Exception e){

           e.printStackTrace();

           no_data_found_layout.setVisibility(View.VISIBLE);
           error_layout.setVisibility(View.GONE);
           notif_recycler_view.setVisibility(View.GONE);

       }

    }

    @Override
    public void onNotifSelected(final NotifsModel notif, final int position) {

        if(utilities.cleanData(userObj.get("status")).equals("admin")){

            CharSequence options_image[] = new CharSequence[]{
                    "Update",
                    "Delete",
                    "Cancel"
            };
            AlertDialog.Builder builderoptions_image = new AlertDialog.Builder(this);
            builderoptions_image.setTitle("Notification options");

            builderoptions_image.setItems(options_image, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    if(which == 0){

                        Intent intent = new Intent(Notifications.this, UpdateNotification.class);
                        intent.putExtra("title", notifList.get(position).getTitle());
                        intent.putExtra("description", notifList.get(position).getDescription());
                        intent.putExtra("date",notifList.get(position).getDate());
                        intent.putExtra("id", notifList.get(position).getId());
                        startActivity(intent);
                        overridePendingTransition(R.anim.right_in, R.anim.left_out);

                    }
                    else  if(which == 1){

                        AlertDialog.Builder builder = new AlertDialog.Builder(Notifications.this);
                        builder.setMessage("Are you sure you want to delete this notification?");
                        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                DeleteNotification(notif.getId(),position);
                            }
                        });
                        final AlertDialog alertDialog = builder.create();

                        alertDialog.setOnShowListener( new DialogInterface.OnShowListener() {
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

    private void DeleteNotification(String id, int position) {

        dialog.show();

        isExecuted = true;

        communicator.singleParametrizedCall(id,"deletenotif");

        notifList.remove(position);
        notifsAdapter.notifyItemRemoved(position);


    }

    @Subscribe
    public void onErrorEvent(ErrorEvent errorEvent){

        Log.e("HomeFragment: ",errorEvent.getErrorMsg());

        shimmer_view_container.setVisibility(View.GONE);

       try {

           if(appSession.getNotifications() != null){

               notif_recycler_view.setVisibility(View.VISIBLE);
               displayNotifications();

           }else{

               //show error layout
               error_layout.setVisibility(View.VISIBLE);
               no_data_found_layout.setVisibility(View.GONE);

           }
       }catch (Exception e){

           e.printStackTrace();
       }

        dialog.dismiss();

    }

    @Override
    public void onResume(){
        super.onResume();
        shimmer_view_container.startShimmer();

        isExecuted = true;

        //fetch Notifications
        communicator.singleParametrizedCall(utilities.cleanData(userObj.get("id")),"get_notifications");

        //mark current user's notifications as read/is seen
        communicator.singleParametrizedCall(utilities.cleanData(userObj.get("id")),"mark_notif_isseen");

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