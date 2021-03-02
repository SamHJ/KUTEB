package com.naijaunik.kuteb.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.naijaunik.kuteb.Adapters.TransactionsHistoryAdapter;
import com.naijaunik.kuteb.Api.BusProvider;
import com.naijaunik.kuteb.Api.Communicator;
import com.naijaunik.kuteb.Api.ErrorEvent;
import com.naijaunik.kuteb.Api.ServerEvent;
import com.naijaunik.kuteb.Api.ServerResponse;
import com.naijaunik.kuteb.Model.TransactionHistoryModel;
import com.naijaunik.kuteb.R;
import com.naijaunik.kuteb.Utils.AppSession;
import com.naijaunik.kuteb.Utils.Utilities;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TransactionsHistory extends AppCompatActivity implements TransactionsHistoryAdapter.HistoryAdapterListener {
    
    RecyclerView transactions_recycler_view;
    private Toolbar toolbar;
    ShimmerFrameLayout shimmer_view_container;
    LinearLayout error_layout,no_data_found_layout;
    private Utilities utilities;
    private Object userObj;
    private AppSession appSession;
    private Communicator communicator;
    private List<TransactionHistoryModel> transactionsHistoryList;
    private TransactionsHistoryAdapter adapter;
    private ServerResponse serverResponse;
    private SearchView searchView;
    private TextView total_earnings_text;

    private boolean isExecuted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        utilities = Utilities.getInstance(this);
        appSession = appSession.getInstance(this);
        userObj = appSession.getUser();
        communicator = new Communicator();

        
        setContentView(R.layout.activity_transactions_history);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Transactions History");


        //init fields
        initFields();
    }

    private void initFields() {

        transactions_recycler_view = findViewById(R.id.transactions_recycler_view);
        shimmer_view_container = findViewById(R.id.shimmer_view_container);
        error_layout = findViewById(R.id.error_layout);
        no_data_found_layout = findViewById(R.id.no_data_found_layout);
        total_earnings_text = findViewById(R.id.total_earnings_text);

        transactions_recycler_view.setHasFixedSize(true);
        LinearLayoutManager notifsLayoutManager = new LinearLayoutManager(this);
        transactions_recycler_view.setLayoutManager(notifsLayoutManager);
        transactions_recycler_view.setItemAnimator(new DefaultItemAnimator());

        transactionsHistoryList = new ArrayList<>();

        isExecuted = true;

        communicator.nonParametrizedCall("gettransactionhistory",this);
        
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

        }

        return super.onOptionsItemSelected(item);
    }

    @Subscribe
    public void onServerEvent(ServerEvent serverEvent){

        if(isExecuted) {

            serverResponse = serverEvent.getServerResponse();

            if (serverResponse != null) {

                if (serverResponse.getType().equals("gettransactionhistory")) {

                    if (serverResponse.getResult().equals("success")) {

                        JsonArray sectionData = serverResponse.getData();
                        String totalEarnings = serverResponse.getTotalearnings();

                        if (sectionData != null) {

                            //storing the user in shared preferences

                            displayTransactionHistory(sectionData);

                            total_earnings_text.setVisibility(View.VISIBLE);

                            total_earnings_text.setText("Total Subscription Purchases: " + totalEarnings);

                        } else {

                            //show error layout
                            error_layout.setVisibility(View.VISIBLE);
                            no_data_found_layout.setVisibility(View.GONE);
                            transactions_recycler_view.setVisibility(View.GONE);
                        }

                    } else {

                        //show error layout
                        error_layout.setVisibility(View.VISIBLE);
                        no_data_found_layout.setVisibility(View.GONE);
                        transactions_recycler_view.setVisibility(View.GONE);

                    }

                }
            }
        }
    }

    @Subscribe
    public void onErrorEvent(ErrorEvent errorEvent){

        Log.e("TransHistory: ",errorEvent.getErrorMsg());

        shimmer_view_container.setVisibility(View.GONE);

            error_layout.setVisibility(View.VISIBLE);
            no_data_found_layout.setVisibility(View.GONE);
            transactions_recycler_view.setVisibility(View.GONE);

    }

    private void displayTransactionHistory(JsonArray sectionData) {

        shimmer_view_container.setVisibility(View.GONE);

        //clear book array list to avoid repeatition of data
        transactionsHistoryList.clear();

        try {

            for (JsonElement element : sectionData) {
                String id = Utilities.cleanData(((JsonObject) element).get("id"));
                String plan_name = Utilities.cleanData(((JsonObject) element).get("plan_name"));
                String plan_amount = Utilities.cleanData(((JsonObject) element).get("plan_amount"));
                String plan_duration_name = Utilities.cleanData(((JsonObject) element).get("plan_duration_name"));
                String plan_duration_in_days = Utilities.cleanData(((JsonObject) element).get("plan_duration_in_days"));
                String date_paid = Utilities.cleanData(((JsonObject) element).get("date_paid"));
                String user_name = Utilities.cleanData(((JsonObject) element).get("user_name"));
                String user_phone = Utilities.cleanData(((JsonObject) element).get("phone"));

                //add data to list
                transactionsHistoryList.add(new TransactionHistoryModel(id,plan_name,plan_amount,
                        plan_duration_name,plan_duration_in_days,date_paid,user_name,user_phone));
            }


            if(transactionsHistoryList.size() == 0){

                transactions_recycler_view.setVisibility(View.GONE);
                error_layout.setVisibility(View.GONE);
                no_data_found_layout.setVisibility(View.VISIBLE);
            }


            adapter = new TransactionsHistoryAdapter(TransactionsHistory.this,
                    transactionsHistoryList,this);

            transactions_recycler_view.setAdapter(adapter);

        }catch (Exception e){

            e.printStackTrace();

            no_data_found_layout.setVisibility(View.VISIBLE);
            error_layout.setVisibility(View.GONE);
            transactions_recycler_view.setVisibility(View.GONE);

        }
    }

    @Override
    public void onHistorySelected(TransactionHistoryModel history, int position) {

    }
    
    @Override
    public void onResume(){
        super.onResume();
        shimmer_view_container.startShimmer();

        //fetch course sections

        isExecuted = true;

        communicator.nonParametrizedCall("gettransactionhistory",this);

        try {
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