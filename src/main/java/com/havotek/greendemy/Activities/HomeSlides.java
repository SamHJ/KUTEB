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
import android.widget.LinearLayout;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.naijaunik.kuteb.Adapters.SliderAdapter;
import com.naijaunik.kuteb.Api.BusProvider;
import com.naijaunik.kuteb.Api.Communicator;
import com.naijaunik.kuteb.Api.ErrorEvent;
import com.naijaunik.kuteb.Api.ServerEvent;
import com.naijaunik.kuteb.Api.ServerResponse;
import com.naijaunik.kuteb.Model.HomeSlider;
import com.naijaunik.kuteb.R;
import com.naijaunik.kuteb.Utils.AppSession;
import com.naijaunik.kuteb.Utils.Utilities;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Objects;

public class HomeSlides extends AppCompatActivity implements SliderAdapter.SliderAdapterListener {

    private Toolbar toolbar;
    RecyclerView home_slides_recycler_view;
    LinearLayout error_layout,no_data_found_layout;
    ShimmerFrameLayout shimmer_view_container;
    private Communicator communicator;
    private ArrayList<HomeSlider> sliderList;
    private ServerResponse serverResponse;
    private AppSession appSession;
    private ProgressDialog dialog;
    private Utilities utilities;
    SliderAdapter adapter;
    private SearchView searchView;
    private boolean isExecuted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_slides);

        appSession = AppSession.getInstance(this);
        utilities = Utilities.getInstance(this);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Home Slides");

        communicator = new Communicator();

        //init fields
        initFields();

        isExecuted = true;
        communicator.nonParametrizedCall("get_home_slides",this);

    }

    private void initFields() {

        home_slides_recycler_view = findViewById(R.id.home_slides_recycler_view);
        error_layout = findViewById(R.id.error_layout);
        no_data_found_layout = findViewById(R.id.no_data_found_layout);
        shimmer_view_container = findViewById(R.id.shimmer_view_container);

        dialog = utilities.showLoadingDialog("Deleting Slide...","");

        home_slides_recycler_view.setHasFixedSize(true);
        LinearLayoutManager notifsLayoutManager = new LinearLayoutManager(this);
        home_slides_recycler_view.setLayoutManager(notifsLayoutManager);
        home_slides_recycler_view.setItemAnimator(new DefaultItemAnimator());

        sliderList = new ArrayList<>();
    }

    @Subscribe
    public void onServerEvent(ServerEvent serverEvent){

        if(isExecuted) {

            serverResponse = serverEvent.getServerResponse();

            if (serverResponse != null) {

                //handle different kinds of request responses

                if (serverResponse.getType().equals("homeslider")) {

                    if (serverResponse.getResult().equals("success")) {

                        //populate and show the slider items
                        shimmer_view_container.setVisibility(View.GONE);
                        home_slides_recycler_view.setVisibility(View.VISIBLE);

                        JsonArray slidesDataArray = serverResponse.getData();

                        //store home slides to prefs

                        appSession.storeHomeSliders(slidesDataArray);

                        displaySlideItems();

                    } else {

                        //show default image instead of slider
                        shimmer_view_container.setVisibility(View.GONE);
                        error_layout.setVisibility(View.VISIBLE);
                    }

                } else if (serverResponse.getType().equals("deleteslideitem")) {

                    if (serverResponse.getResult().equals("success")) {

                        utilities.dialogError(this, getString(R.string.item_deleted));

                    } else {

                        utilities.dialogError(this, getString(R.string.error_occurred));
                    }

                    dialog.dismiss();
                }

            }

        }
    }

    private void displaySlideItems() {


        //fill list screen
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


        adapter = new SliderAdapter(this, sliderList,this);

        home_slides_recycler_view.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    @Override
    public void onSlideSelected(final HomeSlider slider, final int position) {

        CharSequence options_image[] = new CharSequence[]{
                "Update Slide",
                "Delete Slide",
                "Cancel"
        };
        AlertDialog.Builder builderoptions_image = new AlertDialog.Builder(this);
        builderoptions_image.setTitle("Slide Options");

        builderoptions_image.setItems(options_image, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(which == 0){
                    Intent updateIntent = new Intent(HomeSlides.this, UpdateHomeSlide.class);
                    updateIntent.putExtra("id",String.valueOf(slider.getId()));
                    updateIntent.putExtra("image",slider.getImage());
                    updateIntent.putExtra("title",slider.getTitle());
                    updateIntent.putExtra("subtitle",slider.getSubtitle());
                    updateIntent.putExtra("gotourl",slider.getGo_to_url());
                    startActivity(updateIntent);
                    overridePendingTransition(R.anim.right_in,R.anim.left_out);

                    dialog.dismiss();

                }else if (which == 1) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(HomeSlides.this);
                    builder.setMessage("Are you sure you want to delete this slide?");
                    builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            deleteSlide(position,String.valueOf(slider.getId()));
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

                    dialog.dismiss();

                }
            }
        });

        builderoptions_image.show();
    }

    private void deleteSlide(int position, String sliderId) {

        dialog.show();

        isExecuted = true;

        communicator.singleParametrizedCall(sliderId,
                "deleteslideitem");

        try {

            sliderList.remove(position);

        }catch (Exception e){

            e.printStackTrace();
        }
        adapter.notifyItemRemoved(position);
    }

    @Subscribe
    public void onErrorEvent(ErrorEvent errorEvent){

        Log.e("HomeSlides: ",errorEvent.getErrorMsg());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_and_add_menu, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        MenuItem addSlideItem = menu.findItem(R.id.action_add);
        
        addSlideItem.setVisible(true);
        addSlideItem.setTitle("Add Slide Item");

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

        return  super.onCreateOptionsMenu(menu);

    }

    @SuppressLint("NewApi")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
            overridePendingTransition(R.anim.left_in, R.anim.right_out);

        }else if(item.getItemId() == R.id.action_add){

            startActivity(new Intent(this,AddHomeSlide.class));
            overridePendingTransition(R.anim.right_in, R.anim.left_out);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume(){
        super.onResume();

        isExecuted = true;

        communicator.nonParametrizedCall("get_home_slides",this);
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