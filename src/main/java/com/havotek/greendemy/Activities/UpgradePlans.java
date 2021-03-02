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
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.flutterwave.raveandroid.RavePayActivity;
import com.flutterwave.raveandroid.RaveUiManager;
import com.flutterwave.raveandroid.rave_java_commons.RaveConstants;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.naijaunik.kuteb.Adapters.PlansAdapter;
import com.naijaunik.kuteb.Api.BusProvider;
import com.naijaunik.kuteb.Api.Communicator;
import com.naijaunik.kuteb.Api.ErrorEvent;
import com.naijaunik.kuteb.Api.ServerEvent;
import com.naijaunik.kuteb.Api.ServerResponse;
import com.naijaunik.kuteb.Model.PlansModel;
import com.naijaunik.kuteb.R;
import com.naijaunik.kuteb.Utils.AppSession;
import com.naijaunik.kuteb.Utils.Utilities;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.payumoney.core.PayUmoneyConfig;
import com.payumoney.core.PayUmoneySdkInitializer;
import com.payumoney.core.entity.TransactionResponse;
import com.payumoney.sdkui.ui.utils.PayUmoneyFlowManager;
import com.payumoney.sdkui.ui.utils.ResultModel;
import com.squareup.otto.Subscribe;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import co.paystack.android.Paystack;
import co.paystack.android.PaystackSdk;
import co.paystack.android.Transaction;
import co.paystack.android.exceptions.ExpiredAccessCodeException;
import co.paystack.android.model.Card;
import co.paystack.android.model.Charge;

import static com.naijaunik.kuteb.Utils.AppConstants.DATE_TIME_FORMAT;
import static com.naijaunik.kuteb.Utils.AppConstants.DEBUG;
import static com.naijaunik.kuteb.Utils.AppConstants.ERROR_MSG;
import static com.naijaunik.kuteb.Utils.AppConstants.FLUTTERWAVE_ENCRYPTION_KEY;
import static com.naijaunik.kuteb.Utils.AppConstants.FLUTTERWAVE_PAYMENT_VERIFIER;
import static com.naijaunik.kuteb.Utils.AppConstants.FLUTTERWAVE_PUBLIC_KEY;
import static com.naijaunik.kuteb.Utils.AppConstants.FURL;
import static com.naijaunik.kuteb.Utils.AppConstants.MERCHANT_ID;
import static com.naijaunik.kuteb.Utils.AppConstants.MERCHANT_KEY;
import static com.naijaunik.kuteb.Utils.AppConstants.MONEY_HASH;
import static com.naijaunik.kuteb.Utils.AppConstants.PAYMENT_VERIFIER;
import static com.naijaunik.kuteb.Utils.AppConstants.PAYPAL_CLIENT_ID;
import static com.naijaunik.kuteb.Utils.AppConstants.PAYPAL_CONFIG_ENV;
import static com.naijaunik.kuteb.Utils.AppConstants.SURL;

public class UpgradePlans extends AppCompatActivity implements PlansAdapter.PlansAdapterListener {

    AppSession appSession;
    Utilities utilities;
    JsonObject userObj;
    private Toolbar toolbar;
    private ProgressDialog dialog;
    private ServerResponse serverResponse;
    private SearchView searchView;
    private PlansAdapter planAdapter;
    ShimmerFrameLayout shimmer_view_container;
    LinearLayout no_data_found_layout,error_layout;
    RecyclerView plans_recycler_view;
    Communicator communicator;
    private ArrayList<PlansModel> plansList;
    private boolean isAdmin;
    private JsonObject adminSettings;
    LinearLayout layout_payment_cards_input,layout_upgrade_plans;

    TextInputEditText card_number,card_month,card_year,card_cvc;
    MaterialButton btn_pay_with_paystack;
    private static final String ALLOWED_CHARACTERS ="0123456789qwertyuiopasdfghjklzxcvbnm";
    TextView paystack_success_msg;

    public static final int PAYPAL_REQUEST_CODE = 7273;

    private static PayPalConfiguration config = new PayPalConfiguration()
            .environment(PAYPAL_CONFIG_ENV)
            .clientId(PAYPAL_CLIENT_ID);
    private PlansModel userSelectedPlan;
    private boolean isExecuted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        appSession = AppSession.getInstance(this);
        utilities = Utilities.getInstance(this);
        userObj = appSession.getUser();
        communicator = new Communicator();
        adminSettings = appSession.getAdminSettings();

        if(!utilities.cleanData(userObj.get("status")).equals("admin")){

            //prevent screen capture
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        }

        setContentView(R.layout.activity_upgrade_plans);

        //init paystack
        PaystackSdk.initialize(getApplicationContext());

        //start PayPal service
        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION,config);
        startService(intent);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Upgrade Plans");
        
        initFields();
    }

    private void initFields() {

        dialog = utilities.showLoadingDialog("Deleting Plan...","");
        isAdmin = utilities.cleanData(userObj.get("status")).equals("admin");

        shimmer_view_container = findViewById(R.id.shimmer_view_container);
        no_data_found_layout = findViewById(R.id.no_data_found_layout);
        error_layout = findViewById(R.id.error_layout);
        plans_recycler_view = findViewById(R.id.plans_recycler_view);

        layout_payment_cards_input = findViewById(R.id.layout_payment_cards_input);
        layout_upgrade_plans = findViewById(R.id.layout_upgrade_plans);
        paystack_success_msg = findViewById(R.id.paystack_success_msg);
        btn_pay_with_paystack = findViewById(R.id.btn_pay_with_paystack);
        card_number = findViewById(R.id.card_number);
        card_month = findViewById(R.id.card_month);
        card_year = findViewById(R.id.card_year);
        card_cvc = findViewById(R.id.card_cvc);

        plans_recycler_view.setHasFixedSize(true);
        LinearLayoutManager notifsLayoutManager = new LinearLayoutManager(this);
        plans_recycler_view.setLayoutManager(notifsLayoutManager);
        plans_recycler_view.setItemAnimator(new DefaultItemAnimator());


        btn_pay_with_paystack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isValid()){

                    if(utilities.cleanData(userObj.get("email")).isEmpty()){

                        utilities.dialogError(UpgradePlans.this,
                                "Please add your valid email address to your profile!");

                    }else{

                        PayWithPaystack();
                    }

                }else{

                   utilities.dialogError(UpgradePlans.this, "All fields are required!");

                }
            }
        });


        plansList = new ArrayList<>();

       getPlans();
    }

    public void PayWithPaystack() {
        int expiryMonth = 0;
        int expiryYear = 0;

        String cardNumber = card_number.getText().toString().trim();
        if(!card_month.getText().toString().trim().isEmpty()) {
            expiryMonth = Integer.parseInt(card_month.getText().toString().trim());
        }
        if(!card_year.getText().toString().trim().isEmpty()) {
            expiryYear = Integer.parseInt(card_year.getText().toString().trim());
        }
        String cvv = card_cvc.getText().toString().trim();

        Card card = new Card(cardNumber, expiryMonth, expiryYear, cvv);
        if (card.isValid()) {
            Charge charge = new Charge();
            charge.setCard(card);

            dialog = new ProgressDialog(UpgradePlans.this);
            dialog.setMessage("Performing transaction... please wait");
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            dialog.show();

            int realAmount = (int)(double)convertToDouble(userSelectedPlan.getAmount()) * 100;// * 100 because amount is in Kobo
            charge.setAmount(realAmount);
            charge.setEmail(utilities.cleanData(userObj.get("email")));
            charge.setReference(getRandomString(7));
            try {
                charge.putCustomField("Fee Title", userSelectedPlan.getTitle());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            chargeCard(charge);
        }
        else {
            Toast.makeText(UpgradePlans.this, "Invalid card details", Toast.LENGTH_LONG).show();
        }
    }

    private void chargeCard(final Charge charge) {

        PaystackSdk.chargeCard(UpgradePlans.this, charge, new Paystack.TransactionCallback() {
            // This is called only after transaction is successful
            @Override
            public void onSuccess(Transaction transaction) {
                dialog.dismiss();

                dialog = new ProgressDialog(UpgradePlans.this);
                dialog.setMessage("Verifying transaction... please wait");
                dialog.setCanceledOnTouchOutside(false);
                dialog.setCancelable(false);
                dialog.show();

                paystack_success_msg.setVisibility(View.VISIBLE);
                paystack_success_msg.setText(String.format("Payment successfull with transaction reference %s", transaction.getReference()));

                new verifyOnServer().execute(transaction.getReference());
            }

            // This is called only before requesting OTP
            // Save reference so you may send to server if
            // error occurs with OTP
            // No need to dismiss dialog
            @Override
            public void beforeValidate(Transaction transaction) {
                paystack_success_msg.setVisibility(View.VISIBLE);
                paystack_success_msg.setText(String.format("Transaction reference %s", transaction.getReference()));
            }

            @Override
            public void onError(Throwable error, Transaction transaction) {
                // If an access code has expired, simply ask your server for a new one
                // and restart the charge instead of displaying error
                if (error instanceof ExpiredAccessCodeException) {
                    chargeCard(charge);
                    return;
                }

                if (transaction.getReference() != null) {

                    if(error.getMessage().equals(ERROR_MSG)){
                        dialog.dismiss();
                        paystack_success_msg.setVisibility(View.VISIBLE);
                        paystack_success_msg.setText(String.format("%s concluded with error: %s", transaction.getReference(), error.getMessage()));
                    }else{
                        dialog.dismiss();
                        dialog = new ProgressDialog(UpgradePlans.this);
                        dialog.setMessage("Verifying transaction... please wait");
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.setCancelable(false);
                        dialog.show();
                        paystack_success_msg.setVisibility(View.VISIBLE);
                        paystack_success_msg.setText(String.format("%s concluded with error: %s", transaction.getReference(), error.getMessage()));
                        new verifyOnServer().execute(transaction.getReference());
                    }
                } else {
                    Toast.makeText(UpgradePlans.this, error.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    private Double convertToDouble(String str){
        return  Double.parseDouble(str);
    }

    private class verifyOnServer extends AsyncTask<String, Void, String> {
        private String reference;
        private String error;


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null && result.equals("success")) {
                dialog.dismiss();
                Toast.makeText(UpgradePlans.this, "Payment Verified!", Toast.LENGTH_SHORT).show();

                upgradeUserPlan();

            } else {
                Toast.makeText(UpgradePlans.this, String.format("There was a problem verifying %s on the backend: %s ",
                        this.reference, error), Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        }

        @Override
        protected String doInBackground(String... reference) {
            try {
                this.reference = reference[0];
                String url1 = PAYMENT_VERIFIER + this.reference;
                URL url = new URL(url1);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(
                                url.openStream()));

                String inputLine;
                inputLine = in.readLine();
                in.close();
                return inputLine;
            } catch (Exception e) {
                error = e.getClass().getSimpleName() + ": " + e.getMessage();
            }
            return null;
        }
    }

    @SuppressLint("SimpleDateFormat")
    private void upgradeUserPlan() {

        isExecuted = true;

        dialog = new ProgressDialog(this);

        dialog.setTitle("Upgrading");
        dialog.setMessage("applying changes to your account...");
        dialog.show();

        String currentUserExpirationDate = utilities.cleanData(userObj.get("current_expiration_date"));

        SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_FORMAT);
        Date strDate;

        try {
            strDate = sdf.parse(currentUserExpirationDate);

            long newNumberOfDays  = Integer.parseInt(userSelectedPlan.getPlan_duration_in_days());
            long oldNumberOfDaysRemaining = getExpirationDateDifferenc(strDate.getTime());

            long newNumberOfDaysBeforeExpiration = newNumberOfDays + oldNumberOfDaysRemaining;

            communicator.upgradeUserPlan("upgradeuser",utilities.cleanData(userObj.get("phone")),
                    newNumberOfDaysBeforeExpiration,userSelectedPlan.getId());

        } catch (ParseException e) {

            e.printStackTrace();

            utilities.dialogError(this,"Could not upgrade your account due to time conversion issues!");
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

    private boolean isValid(){
        return !card_number.getText().toString().trim().isEmpty() && !card_cvc.getText().toString().trim().isEmpty() &&
                !card_year.getText().toString().trim().isEmpty() && !card_month.getText().toString().trim().isEmpty();
    }

    private static String getRandomString(final int sizeOfRandomString)
    {
        final Random random=new Random();
        final StringBuilder sb=new StringBuilder(sizeOfRandomString);
        for(int i=0;i<sizeOfRandomString;++i)
            sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        return sb.toString();
    }

    private void getPlans() {

        communicator.nonParametrizedCall("getplans",this);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_and_add_menu, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        MenuItem addPlans = menu.findItem(R.id.action_add);

        if(utilities.cleanData(userObj.get("status")).equals("admin")){

            addPlans.setVisible(true);
            addPlans.setTitle("Add Plan");
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

                    planAdapter.getFilter().filter(query);

                }catch (Exception e){
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                try{

                    planAdapter.getFilter().filter(newText);

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
            Intent intent = new Intent(this, AddPlan.class);
            startActivity(intent);
            overridePendingTransition(R.anim.right_in, R.anim.left_out);

        }

        return super.onOptionsItemSelected(item);
    }

    @Subscribe
    public void onServerEvent(ServerEvent serverEvent){

        serverResponse = serverEvent.getServerResponse();

        if(serverResponse != null) {

            if (serverResponse.getType().equals("getplans")) {

                if (serverResponse.getResult().equals("success")) {

                    JsonArray plansData = serverResponse.getData();

                    if (plansData != null) {

                        //storing the user in shared preferences

                        plans_recycler_view.setVisibility(View.VISIBLE);

                        //store plans to pref
                        appSession.storePlans(plansData);

                        displayPlans(plansData);

                    } else{

                        //show error layout
                        error_layout.setVisibility(View.VISIBLE);
                        no_data_found_layout.setVisibility(View.GONE);
                        plans_recycler_view.setVisibility(View.GONE); }

                } else {

                    //show error layout
                    error_layout.setVisibility(View.VISIBLE);
                    no_data_found_layout.setVisibility(View.GONE);
                    plans_recycler_view.setVisibility(View.GONE);

                }

                //dismiss dialog loader
                dialog.dismiss();

            }else if(serverResponse.getType().equals("deleteplan")){

                if (serverResponse.getResult().equals("success")) {

                    utilities.dialogError(this, getString(R.string.plan_deleted));

                }else{

                    utilities.dialogError(this,getString(R.string.error_occurred));
                }

                //dismiss dialog loader
                dialog.dismiss();

            }else if(serverResponse.getType().equals("upgradeplan")){

                if (serverResponse.getResult().equals("success")) {

                    JsonObject userdata = serverResponse.getUserdata();
                    JsonObject adminsettings = serverResponse.getAdmin_settings();

                    if(userdata != null) {

                        //check if the user has been blocked
                        if (Integer.parseInt(utilities.cleanData(userdata.get("is_blocked"))) == 1) {

                            //user has been blocked so we log them out!
                            appSession.logout();
                            startActivity(new Intent(this, Login.class));
                            overridePendingTransition(R.anim.right_in, R.anim.left_out);
                            finish();

                            utilities.dialogError(this, getString(R.string.account_blocked));

                        } else {

                            //storing the user in shared preferences
                            appSession.userLogin(userdata);

                            //store the admin settings in preferences
                            appSession.storeAdminSettings(adminsettings);


                            utilities.dialogError(this, getString(R.string.plan_upgraded));

                            Toast.makeText(this, "Upgrade Successful!!!", Toast.LENGTH_LONG).show();

                            finish();
                            overridePendingTransition(R.anim.left_in, R.anim.right_out);
                        }
                    }

                }else{

                    utilities.dialogError(this,getString(R.string.could_not_upgrade));
                }

                //dismiss dialog loader
                dialog.dismiss();

            }
        }

        shimmer_view_container.setVisibility(View.GONE);

    }

    private void displayPlans(JsonArray sectionData) {

        shimmer_view_container.setVisibility(View.GONE);

        //clear book array list to avoid repeatition of data
        plansList.clear();

        try {

            for (JsonElement element : sectionData) {
                String id = Utilities.cleanData(((JsonObject) element).get("id"));
                String plan_name = Utilities.cleanData(((JsonObject) element).get("plan_name"));
                String plan_amount = Utilities.cleanData(((JsonObject) element).get("plan_amount"));
                String plan_duration_name = Utilities.cleanData(((JsonObject) element).get("plan_duration_name"));
                String plan_duration_in_days = Utilities.cleanData(((JsonObject) element).get("plan_duration_in_days"));

                //add data to list
                plansList.add(new PlansModel(id,plan_name,plan_amount,plan_duration_name,plan_duration_in_days));
            }


            if(plansList.size() == 0){

                plans_recycler_view.setVisibility(View.GONE);
                error_layout.setVisibility(View.GONE);
                no_data_found_layout.setVisibility(View.VISIBLE);
            }


            planAdapter = new PlansAdapter(this,
                    plansList,this,utilities.cleanData(appSession.getAdminSettings().get("currency_symbol")));

            plans_recycler_view.setAdapter(planAdapter);

        }catch (Exception e){

            e.printStackTrace();

            no_data_found_layout.setVisibility(View.VISIBLE);
            error_layout.setVisibility(View.GONE);
            plans_recycler_view.setVisibility(View.GONE);

        }


    }

    @Override
    public void onPlanSelected(final PlansModel plan, final int position) {

        userSelectedPlan = plan;

        if(!isAdmin){

            if(utilities.cleanData(userObj.get("first_name")).isEmpty() ||
                    utilities.cleanData(userObj.get("email")).isEmpty()){

                utilities.dialogError(this,"Please update your profile email and name(s) to continue");

            }else {


                //detect current payment gateway set by the admin
                String gateway = utilities.cleanData(adminSettings.get("payment_option"));

                switch (gateway) {

                    case "payumoney":
                        renderPayUMoneyGateWay();
                        break;
                    case "paypal":
                        renderPayPalGateWay();
                        break;
                    case "paystack":
                        renderPaystackGateWay();
                        break;
                    case "flutterwave":
                        renderFlutterWaveGateWay();
                        break;
                }

            }

        }else{

                CharSequence options_image[] = new CharSequence[]{
                        "Update Plan",
                        "Delete Plan",
                        "Cancel"
                };
                AlertDialog.Builder builderoptions_image = new AlertDialog.Builder(this);
                builderoptions_image.setTitle("Plan Options");

                builderoptions_image.setItems(options_image, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (which == 0) {

                            Intent intent = new Intent(UpgradePlans.this, UpdatePlan.class);
                            intent.putExtra("id", String.valueOf(plan.getId()));
                            intent.putExtra("plan_name", plan.getTitle());
                            intent.putExtra("plan_duration_name", plan.getPlan_duration_name());
                            intent.putExtra("plan_amount", plan.getAmount());
                            intent.putExtra("duration_in_days", String.valueOf(plan.getPlan_duration_in_days()));
                            startActivity(intent);
                            overridePendingTransition(R.anim.right_in, R.anim.left_out);

                        } else if (which == 1) {

                            AlertDialog.Builder builder = new AlertDialog.Builder(UpgradePlans.this);
                            builder.setMessage("Are you sure you want to delete this plan?");
                            builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    DeletePlan(plan.getId(), position);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        /*
         *  We advise you to do a further verification of transaction's details on your server to be
         *  sure everything checks out before providing service or goods.
         */
        if (requestCode == RaveConstants.RAVE_REQUEST_CODE && data != null) {
            String message = data.getStringExtra("response");
            if (resultCode == RavePayActivity.RESULT_SUCCESS) {

                JsonObject responsJson = new JsonParser().parse(message)
                        .getAsJsonObject();

                if(utilities.cleanData(responsJson.get("status")).equals("success")){

                    JsonObject dataObj = new JsonParser().parse(responsJson.get("data").toString())
                            .getAsJsonObject();

                    dialog = new ProgressDialog(this);

                    dialog.setTitle("Verifying Transaction");
                    dialog.setMessage("a moment please");
                    dialog.show();

                    verifyFlutterWaveTransaction(utilities.cleanData(dataObj.get("id")));


                }else{

                    utilities.dialogError(this, "Payment was not successful!");
                }

            }
            else if (resultCode == RavePayActivity.RESULT_ERROR) {
                Toast.makeText(this, "ERROR " + message, Toast.LENGTH_SHORT).show();
            }
            else if (resultCode == RavePayActivity.RESULT_CANCELLED) {
                Toast.makeText(this, "PAYMENT WINDOW CANCELLED ", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }

        if (requestCode == PayUmoneyFlowManager.REQUEST_CODE_PAYMENT && resultCode == RESULT_OK && data != null) {

            TransactionResponse transactionResponse = data.getParcelableExtra(PayUmoneyFlowManager.INTENT_EXTRA_TRANSACTION_RESPONSE);
            ResultModel resultModel = data.getParcelableExtra(PayUmoneyFlowManager.ARG_RESULT);

            if (transactionResponse != null && transactionResponse.getPayuResponse() != null) {

                if (transactionResponse.getTransactionStatus().equals(TransactionResponse.TransactionStatus.SUCCESSFUL)) {
                    Toast.makeText(this, "Payment Successful!", Toast.LENGTH_LONG).show();
                    upgradeUserPlan();
                } else if (transactionResponse.getTransactionStatus().equals(TransactionResponse.TransactionStatus.CANCELLED)) {
                    Toast.makeText(this, "Payment Cancelled", Toast.LENGTH_SHORT).show();
                } else if (transactionResponse.getTransactionStatus().equals(TransactionResponse.TransactionStatus.FAILED)) {
                    Toast.makeText(this, "Payment Failed", Toast.LENGTH_SHORT).show();
                }

            } else if (resultModel != null && resultModel.getError() != null) {
                Toast.makeText(this, "Error check log", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Both objects are null", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == PayUmoneyFlowManager.REQUEST_CODE_PAYMENT && resultCode == RESULT_CANCELED) {
            Toast.makeText(this, "Payment Cancelled", Toast.LENGTH_SHORT).show();
        }
        else if (requestCode == PAYPAL_REQUEST_CODE){
            //handle paypal response
            if(resultCode == RESULT_OK) {
                PaymentConfirmation confirmation = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirmation != null) {
                    try {
                        String paymentDetails = confirmation.toJSONObject().toString(4);
                        JSONObject jsonObject = new JSONObject(paymentDetails);
                        //the values in the json object are id and state as given below
                        String id = jsonObject.getJSONObject("response").getString("id");
                        String state = jsonObject.getJSONObject("response").getString("state");

                        //update payment record
                        upgradeUserPlan();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }else if (resultCode == Activity.RESULT_CANCELED){
                Toast.makeText(this, "Payment Canceled!", Toast.LENGTH_SHORT).show();
            }
        }else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID){
            Toast.makeText(this, "Invalid Payment Credentials!", Toast.LENGTH_SHORT).show();
        }
    }

    private void renderFlutterWaveGateWay() {

        new RaveUiManager(this).setAmount(Double.parseDouble(userSelectedPlan.getAmount()))
                .setCurrency(utilities.cleanData(adminSettings.get("currency_name")))
                .setEmail(utilities.cleanData(userObj.get("email")))
                .setfName(utilities.cleanData(userObj.get("first_name")))
                .setlName(utilities.cleanData(userObj.get("last_name")))
                .setNarration("Payment for "+userSelectedPlan.getTitle() + " plan, which costs " +
                        adminSettings.get("currency_symbol") + userSelectedPlan.getAmount() + " with a duration of " +
                        userSelectedPlan.getPlan_duration_name())
                .setPublicKey(FLUTTERWAVE_PUBLIC_KEY)
                .setEncryptionKey(FLUTTERWAVE_ENCRYPTION_KEY)
                .setTxRef(getRandomString(7))
                .setPhoneNumber(utilities.cleanData(userObj.get("phone")))
                    .acceptAccountPayments(true)
                    .acceptCardPayments(true)
                    .acceptMpesaPayments(true)
                    .acceptAchPayments(true)
                    .acceptGHMobileMoneyPayments(true)
                    .acceptUgMobileMoneyPayments(true)
                    .acceptZmMobileMoneyPayments(true)
                    .acceptRwfMobileMoneyPayments(true)
                    .acceptSaBankPayments(true)
                    .acceptUkPayments(true)
                    .acceptBankTransferPayments(true)
                    .acceptUssdPayments(true)
                    .acceptBarterPayments(true)
                    .acceptFrancMobileMoneyPayments(true)
                    .allowSaveCardFeature(true)
                    .onStagingEnv(false)
                .withTheme(R.style.MyCustomTheme)
                .isPreAuth(false)
                .shouldDisplayFee(true)
                    .showStagingLabel(false)
                    .initialize();

    }

    private void renderPaystackGateWay() {

        layout_payment_cards_input.setVisibility(View.VISIBLE);
        layout_upgrade_plans.setVisibility(View.GONE);
    }

    private void renderPayPalGateWay() {

        PayPalPayment payPalPayment = new PayPalPayment(new
                BigDecimal(String.valueOf(convertToDouble(userSelectedPlan.getAmount()))),"USD","Pay "+
                userSelectedPlan.getTitle(),
                PayPalPayment.PAYMENT_INTENT_SALE);
        Intent paypalIntent = new Intent(UpgradePlans.this, PaymentActivity.class);
        paypalIntent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION,config);
        paypalIntent.putExtra(PaymentActivity.EXTRA_PAYMENT,payPalPayment);
        startActivityForResult(paypalIntent,PAYPAL_REQUEST_CODE);

    }

    private void renderPayUMoneyGateWay() {

        launchPaymentFlow(UpgradePlans.this,convertToDouble(userSelectedPlan.getAmount()),
                userSelectedPlan.getTitle(),utilities.cleanData(adminSettings.get("currency_symbol")));
    }

    private void verifyFlutterWaveTransaction(String tRef){

        dialog = new ProgressDialog(this);

        dialog.setTitle("Verifying Transaction");
        dialog.setMessage("a moment please");
        dialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, FLUTTERWAVE_PAYMENT_VERIFIER + tRef,

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        dialog.dismiss();

                        if(response.equals("success")){

                            dialog = new ProgressDialog(UpgradePlans.this);

                            dialog.setTitle("Upgrading");
                            dialog.setMessage("applying changes to your account...");
                            dialog.show();

                            //verfication successful so we upgrade the user plan
                            upgradeUserPlan();

                        }else{

                            utilities.dialogError(UpgradePlans.this,
                                    "An error occurred! Could not verify transaction!");
                        }

                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (error instanceof NoConnectionError) {
                            Toast.makeText(UpgradePlans.this, "Internet/Connection Error!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(UpgradePlans.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        dialog.dismiss();
                    }
                });

        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);

        requestQueue.addRequestFinishedListener(new RequestQueue.RequestFinishedListener<Object>() {
            @Override
            public void onRequestFinished(Request<Object> request) {
                requestQueue.getCache().clear();
            }
        });

    }

    private void launchPaymentFlow(Context mContext, Double feeamount, String feeTitle, String currencySymbol) {
        dialog.show();

        PayUmoneyConfig payUmoneyConfig = PayUmoneyConfig.getInstance();
        payUmoneyConfig.setPayUmoneyActivityTitle("Pay" + feeTitle);
        payUmoneyConfig.setDoneButtonText("Pay " + currencySymbol + feeamount);

        PayUmoneySdkInitializer.PaymentParam.Builder builder = new PayUmoneySdkInitializer.PaymentParam.Builder();
        builder.setAmount(String.valueOf(feeamount))
                .setTxnId(System.currentTimeMillis() + "")
                .setPhone(utilities.cleanData(userObj.get("phone")))
                .setProductName(feeTitle)
                .setFirstName(utilities.cleanData(userObj.get("first_name")) + " " +
                        utilities.cleanData(userObj.get("last_name")))
                .setEmail(utilities.cleanData(userObj.get("email")))
                .setsUrl(SURL)
                .setfUrl(FURL)
                .setUdf1("")
                .setUdf2("")
                .setUdf3("")
                .setUdf4("")
                .setUdf5("")
                .setUdf6("")
                .setUdf7("")
                .setUdf8("")
                .setUdf9("")
                .setUdf10("")
                .setIsDebug(DEBUG)
                .setKey(MERCHANT_KEY)
                .setMerchantId(MERCHANT_ID);

        try {
            PayUmoneySdkInitializer.PaymentParam mPaymentParams = builder.build();
            calculateHashInServer(mPaymentParams);
        } catch (Exception e) {
            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
            dialog.dismiss();
        }
    }

    private void calculateHashInServer(final PayUmoneySdkInitializer.PaymentParam mPaymentParams) {
        dialog.dismiss();
        dialog.setTitle("Fetching keys");
        dialog.setMessage("a moment please");
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();

        String url = MONEY_HASH;
        StringRequest request = new StringRequest(Request.Method.POST, url,

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        String merchantHash = "";

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            merchantHash = jsonObject.getString("result");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        dialog.dismiss();

                        if (merchantHash.isEmpty() || merchantHash.equals("")) {
                            Toast.makeText(UpgradePlans.this, "Could not generate hash", Toast.LENGTH_SHORT).show();
                        } else {
                            mPaymentParams.setMerchantHash(merchantHash);
                            PayUmoneyFlowManager.startPayUMoneyFlow(mPaymentParams, UpgradePlans.this
                                    , R.style.PayUMoney, true);
                        }
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (error instanceof NoConnectionError) {
                            Toast.makeText(UpgradePlans.this, "Internet/Connection Error!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(UpgradePlans.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        dialog.dismiss();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                return mPaymentParams.getParams();
            }
        };
        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);

        requestQueue.addRequestFinishedListener(new RequestQueue.RequestFinishedListener<Object>() {
            @Override
            public void onRequestFinished(Request<Object> request) {
                requestQueue.getCache().clear();
            }
        });
    }

    private void DeletePlan(String plan_id, int position) {

        dialog.show();

        isExecuted = true;

        communicator.singleParametrizedCall(plan_id,"deleteplanitem");

        plansList.remove(position);
        planAdapter.notifyItemRemoved(position);
    }

    @Subscribe
    public void onErrorEvent(ErrorEvent errorEvent){

        Log.e("UpgradePlans: ",errorEvent.getErrorMsg());

        dialog.dismiss();

        if(isExecuted) {
            utilities.dialogError(this, getString(R.string.error_occurred));
        }
    }

    @Override
    public void onResume(){
        super.onResume();

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