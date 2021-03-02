package com.naijaunik.kuteb.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.ct7ct7ct7.androidvimeoplayer.listeners.VimeoPlayerErrorListener;
import com.ct7ct7ct7.androidvimeoplayer.listeners.VimeoPlayerStateListener;
import com.ct7ct7ct7.androidvimeoplayer.view.VimeoPlayerView;
import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonObject;
import com.naijaunik.kuteb.Api.Communicator;
import com.naijaunik.kuteb.R;
import com.naijaunik.kuteb.Utils.AppSession;
import com.naijaunik.kuteb.Utils.Utilities;

import java.util.Objects;

import tcking.github.com.giraffeplayer2.GiraffePlayer;
import tcking.github.com.giraffeplayer2.PlayerListener;
import tcking.github.com.giraffeplayer2.VideoInfo;
import tcking.github.com.giraffeplayer2.VideoView;
import tv.danmaku.ijk.media.player.IjkTimedText;

public class CourseLessonViewer extends AppCompatActivity implements View.OnClickListener {

    TextView course_content;
    private Utilities utilities;
    private AppSession appSession;
    private JsonObject userObj;
    private Communicator communicator;
    private boolean isAdmin;
    private Toolbar toolbar;
    MaterialButton attached_file_btn,external_link_btn;
    private String lessonTitle,external_link,attachment,video_url,video_from;
    CardView video_cardview;
    VimeoPlayerView vimeoPlayer;
    VideoView video_view;
    ImageView youtube_video_image_placeholder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        utilities = Utilities.getInstance(this);
        appSession = appSession.getInstance(this);
        userObj = appSession.getUser();
        communicator = new Communicator();

        isAdmin = utilities.cleanData(userObj.get("status")).equals("admin");

        if(!isAdmin){

            //prevent screen capture
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        }

        setContentView(R.layout.activity_course_lesson_viewer);

        lessonTitle = getIntent().getStringExtra("title");
        external_link = getIntent().getStringExtra("external_link");
        attachment = getIntent().getStringExtra("attachment");
        video_url = getIntent().getStringExtra("video_url");
        video_from = getIntent().getStringExtra("video_from");

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(lessonTitle);

        //init Fields
        initFields();
    }

    private void initFields() {

        course_content = findViewById(R.id.course_content);
        attached_file_btn = findViewById(R.id.attached_file_btn);
        external_link_btn = findViewById(R.id.external_link_btn);
        video_cardview = findViewById(R.id.video_cardview);

        course_content.setText(Html.fromHtml(getIntent().getStringExtra("content")));

        if(attachment.isEmpty()){

            attached_file_btn.setVisibility(View.GONE);
        }

        if(external_link.isEmpty()){

            external_link_btn.setVisibility(View.GONE);

        }

        //hide the cardview if all the options in it have empty values
        if(external_link.isEmpty() && video_url.isEmpty() && attachment.isEmpty()){

            video_cardview.setVisibility(View.GONE);
        }

        if(video_from.equals("server")){

            renderServerVideo();
            
        }else if(video_from.equals("youtube")){
            
            renderYouTubeVideo();
            
        }else if (video_from.equals("vimeo")){
            
            renderVimeovideo();
            
        }

        external_link_btn.setOnClickListener(this);
        attached_file_btn.setOnClickListener(this);

    }

    private void renderServerVideo() {

        video_view = findViewById(R.id.video_view);

        video_view.setVisibility(View.VISIBLE);

        video_view.getVideoInfo().setBgColor(Color.GRAY).setAspectRatio(VideoInfo.AR_MATCH_PARENT);//config player
        try{
            video_view.setVideoPath(video_url).getPlayer();

            video_view.setPlayerListener(new PlayerListener() {
                @Override
                public void onPrepared(GiraffePlayer giraffePlayer) {

                }

                @Override
                public void onBufferingUpdate(GiraffePlayer giraffePlayer, int percent) {

                }

                @Override
                public boolean onInfo(GiraffePlayer giraffePlayer, int what, int extra) {
                    return false;
                }

                @Override
                public void onCompletion(GiraffePlayer giraffePlayer) {

                }

                @Override
                public void onSeekComplete(GiraffePlayer giraffePlayer) {

                }

                @Override
                public boolean onError(GiraffePlayer giraffePlayer, int what, int extra) {
                    return false;
                }

                @Override
                public void onPause(GiraffePlayer giraffePlayer) {

                }

                @Override
                public void onRelease(GiraffePlayer giraffePlayer) {

                }

                @Override
                public void onStart(GiraffePlayer giraffePlayer) {

                }

                @Override
                public void onTargetStateChange(int oldState, int newState) {

                }

                @Override
                public void onCurrentStateChange(int oldState, int newState) {

                }

                @Override
                public void onDisplayModelChange(int oldModel, int newModel) {

                }

                @Override
                public void onPreparing(GiraffePlayer giraffePlayer) {

                }

                @Override
                public void onTimedText(GiraffePlayer giraffePlayer, IjkTimedText text) {

                }

                @Override
                public void onLazyLoadProgress(GiraffePlayer giraffePlayer, int progress) {

                }

                @Override
                public void onLazyLoadError(GiraffePlayer giraffePlayer, String message) {

                    utilities.dialogError(CourseLessonViewer.this,message);
                }
            });


        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void renderYouTubeVideo(){

        youtube_video_image_placeholder = findViewById(R.id.youtube_video_image_placeholder);
        youtube_video_image_placeholder.setVisibility(View.VISIBLE);

        youtube_video_image_placeholder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent videoIntent = new Intent(CourseLessonViewer.this, YoutubePlayerActivity.class);
                videoIntent.putExtra("videocode",video_url);
                startActivity(videoIntent);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);

            }
        });
    }

    private void renderVimeovideo() {

        vimeoPlayer = findViewById(R.id.vimeoPlayer);

        vimeoPlayer.setVisibility(View.VISIBLE);

        getLifecycle().addObserver(vimeoPlayer);

       try {

           vimeoPlayer.initialize(Integer.parseInt(video_url)); // samplevideoId is 458049458

       }catch (Exception e){

           e.printStackTrace();

           utilities.dialogError(this, e.getMessage());

       }

        vimeoPlayer.addStateListener(new VimeoPlayerStateListener() {
            @Override
            public void onLoaded(int videoId) {
            }

            @Override
            public void onPlaying(float duration) {
            }

            @Override
            public void onPaused(float seconds) {
            }

            @Override
            public void onEnded(float duration) {
            }

            @Override
            public void onInitFailed() {
                utilities.dialogError(CourseLessonViewer.this,"Failed to initialize video player!");
            }
        });

        vimeoPlayer.addErrorListener(new VimeoPlayerErrorListener() {
            @Override
            public void onError(String message, String method, String name) {
                utilities.dialogError(CourseLessonViewer.this,message);
            }
        });

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){

            case R.id.attached_file_btn:
                  downloadAttachedFile();
                break;

            case R.id.external_link_btn:
                  naviageToExternalLink();
                break;
        }
    }

    private void naviageToExternalLink() {

        String link = !external_link.startsWith("http://") && !external_link.startsWith("https://")
                ? "http://" + external_link : external_link;

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        startActivity(browserIntent);
    }

    private void downloadAttachedFile() {

        String fileExtension = getIntent().getStringExtra("attachment_extension");

        utilities.downloadFileToPhone(attachment,
                lessonTitle,fileExtension,
                utilities.folderName(fileExtension));
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