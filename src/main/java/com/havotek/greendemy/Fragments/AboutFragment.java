package com.naijaunik.kuteb.Fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonObject;
import com.naijaunik.kuteb.Activities.AdminSettings;
import com.naijaunik.kuteb.Activities.AllUsers;
import com.naijaunik.kuteb.Activities.Help;
import com.naijaunik.kuteb.Activities.TransactionsHistory;
import com.naijaunik.kuteb.Api.Communicator;
import com.naijaunik.kuteb.R;
import com.naijaunik.kuteb.Utils.AppSession;
import com.naijaunik.kuteb.Utils.Utilities;

import java.util.Objects;

public class AboutFragment extends Fragment implements View.OnClickListener {

    private Utilities utilities;
    private AppSession appSession;
    private Communicator communicator;
    private JsonObject userObj;
    private Toolbar toolbar;
    MaterialButton about_app_btn,privacy_policy_btn,terms_and_condition_btn,transactions_history_btn;
    private boolean isAdmin;

    public AboutFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        utilities = Utilities.getInstance(getContext());
        appSession = AppSession.getInstance(getContext());
        communicator = new Communicator();

        userObj = appSession.getUser();

        isAdmin = utilities.cleanData(userObj.get("status")).equals("admin");

        if(!utilities.cleanData(userObj.get("status")).equals("admin")){

            //prevent screen capture
            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        }

        // Inflate the layout for this fragment
        View aboutFragmentView =  inflater.inflate(R.layout.fragment_about, container, false);
        setHasOptionsMenu(true);

        toolbar = aboutFragmentView.findViewById(R.id.toolbar);
        ((AppCompatActivity) Objects.requireNonNull(getActivity())).setSupportActionBar(toolbar);
        Objects.requireNonNull(((AppCompatActivity) getActivity()).getSupportActionBar()).setTitle("About");


        //init fields
        initFields(aboutFragmentView);

        return  aboutFragmentView;
    }

    private void initFields(View aboutFragmentView) {

        about_app_btn = aboutFragmentView.findViewById(R.id.about_app_btn);
        privacy_policy_btn = aboutFragmentView.findViewById(R.id.privacy_policy_btn);
        terms_and_condition_btn = aboutFragmentView.findViewById(R.id.terms_and_condition_btn);
        transactions_history_btn = aboutFragmentView.findViewById(R.id.transactions_history_btn);

        if(isAdmin){

            transactions_history_btn.setVisibility(View.VISIBLE);
        }

        about_app_btn.setOnClickListener(this);
        privacy_policy_btn.setOnClickListener(this);
        terms_and_condition_btn.setOnClickListener(this);
        transactions_history_btn.setOnClickListener(this);

    }


    @Override
    public void onClick(View view) {

        switch (view.getId()){

            case R.id.about_app_btn:

                new MaterialDialog.Builder(getContext())
                        .content(Html.fromHtml(utilities.cleanData(appSession.getAdminSettings().get("about_app"))))
                        .positiveText(R.string.ok)
                        .show();

                break;

            case R.id.privacy_policy_btn:

                new MaterialDialog.Builder(getContext())
                        .content(Html.fromHtml(utilities.cleanData(appSession.getAdminSettings().get("privacy_policy"))))
                        .positiveText(R.string.ok)
                        .show();

                break;

            case R.id.terms_and_condition_btn:

                new MaterialDialog.Builder(getContext())
                        .content(Html.fromHtml(utilities.cleanData(appSession.getAdminSettings().get("terms_and_conditions"))))
                        .positiveText(R.string.ok)
                        .show();

                break;

            case R.id.transactions_history_btn:
                startActivity(new Intent(getContext(), TransactionsHistory.class));
                getActivity().overridePendingTransition(R.anim.right_in,R.anim.left_out);
                break;
        }
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

        inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.about_app_menu, menu);

        MenuItem item = menu.findItem(R.id.action_settings);
        MenuItem allUsersItem = menu.findItem(R.id.all_users);


        if(utilities.cleanData(userObj.get("status")).equals("admin")){

           item.setVisible(true);
           allUsersItem.setVisible(true);
        }

        super.onCreateOptionsMenu(menu, inflater);

    }

    @SuppressLint("NewApi")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.action_settings){

            startActivity(new Intent(getContext(), AdminSettings.class));
            getActivity().overridePendingTransition(R.anim.right_in, R.anim.left_out);

        }else if(item.getItemId() == R.id.action_help){

            startActivity(new Intent(getContext(), Help.class));
            getActivity().overridePendingTransition(R.anim.right_in, R.anim.left_out);

        }else if(item.getItemId() == R.id.all_users){

            startActivity(new Intent(getContext(), AllUsers.class));
            getActivity().overridePendingTransition(R.anim.right_in, R.anim.left_out);
        }

        return super.onOptionsItemSelected(item);
    }


}