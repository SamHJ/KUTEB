package com.naijaunik.kuteb.Activities;

import android.annotation.SuppressLint;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.google.gson.JsonObject;
import com.naijaunik.kuteb.R;
import com.naijaunik.kuteb.Utils.AppSession;
import com.naijaunik.kuteb.Utils.Utilities;

import java.util.Objects;

public class WebViewer extends AppCompatActivity {

    ProgressBar webview_loader;
    WebView webview;
    AppSession appSession;
    private Utilities utilities;
    JsonObject userObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        appSession = AppSession.getInstance(this);
        utilities = Utilities.getInstance(this);

        userObj = appSession.getUser();

        if(!utilities.cleanData(userObj.get("status")).equals("admin")){

            //prevent screen capture
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        }

        setContentView(R.layout.activity_pdfviewer);

        initFields();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initFields() {
        webview = findViewById(R.id.webview);
        webview_loader = findViewById(R.id.webview_loader);

        webview.setWebChromeClient(new WebChromeClient(){

            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    webview_loader.setVisibility(View.GONE);
                } else {
                    webview_loader.setVisibility(View.VISIBLE);
                    webview_loader.setProgress(newProgress);
                }
            }
        });
        webview.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                webview_loader.setVisibility(View.GONE);
            }
        });
        webview.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webview.getSettings().setLoadWithOverviewMode(true);
        webview.getSettings().setBuiltInZoomControls(true);
        webview.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webview.clearCache(true);
        webview.clearHistory();
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        if(Objects.equals(getIntent().getStringExtra("type"), "pdf")){
            webview.loadUrl("https://drive.google.com/viewerng/viewer?embedded=true&url=" +
                    getIntent().getStringExtra("pdf_url"));
        }else {
            webview.loadUrl(Objects.requireNonNull(getIntent().getStringExtra("slider_go_to_url")));
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
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


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }
}
