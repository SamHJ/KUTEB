package com.naijaunik.kuteb.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.google.gson.JsonObject;
import com.naijaunik.kuteb.R;
import com.naijaunik.kuteb.Utils.AppSession;
import com.naijaunik.kuteb.Utils.Utilities;

import static com.naijaunik.kuteb.Utils.AppConstants.YOUTUBE_API_KEY;

public class YoutubePlayerActivity extends YouTubeBaseActivity implements
        YouTubePlayer.OnInitializedListener, View.OnClickListener {

    YouTubePlayerView youtube_view;
    private static final int RECOVERY_REQUEST = 1;
    private AppSession appSession;

    private static final String TAG = "CustomPlayerControlActivity";

    //https://www.youtube.com/watch?v=<VIDEO_ID>
    public static final String VIDEO_ID = "u9qbba3gPms";

    private YouTubePlayer mPlayer;

    private View mPlayButtonLayout;
    private TextView mPlayTimeTextView;

    private Handler mHandler = null;
    private SeekBar mSeekBar;
    ImageButton play_video, pause_video;
    private JsonObject userObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        appSession = AppSession.getInstance(this);
        userObj = appSession.getUser();

        if(!userObj.get("status").equals("admin")){

            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        }

        setContentView(R.layout.activity_youtube_player);

        youtube_view = findViewById(R.id.youtube_view);
        youtube_view.initialize(YOUTUBE_API_KEY, this);

        //Add play button to explicitly play video in YouTubePlayerView
        mPlayButtonLayout = findViewById(R.id.video_control);
        play_video = findViewById(R.id.play_video);
        pause_video = findViewById(R.id.pause_video);
        findViewById(R.id.pause_video).setOnClickListener(this);

        play_video.setOnClickListener(this);
        pause_video.setOnClickListener(this);

        mPlayTimeTextView = (TextView) findViewById(R.id.play_time);
        mSeekBar = (SeekBar) findViewById(R.id.video_seekbar);
        mSeekBar.setOnSeekBarChangeListener(mVideoSeekBarChangeListener);

        mHandler = new Handler();

    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean wasRestored) {
        if (!wasRestored) {

            if (null == player) return;
            mPlayer = player;

            displayCurrentTime();

            String youtubeUrl = getIntent().getStringExtra("videocode");

            String videoID = youtubeUrl.replace("https://www.youtube.com/watch?v=","");

            // Start buffering
            player.cueVideo(videoID); // Plays https://www.youtube.com/watch?v=fhWaJi1Hsfo

            player.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS);
            mPlayButtonLayout.setVisibility(View.VISIBLE);

            // Add listeners to YouTubePlayer instance
            player.setPlayerStateChangeListener(mPlayerStateChangeListener);
            player.setPlaybackEventListener(mPlaybackEventListener);


        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult errorReason) {
        if (errorReason.isUserRecoverableError()) {
            errorReason.getErrorDialog(this, RECOVERY_REQUEST).show();
        } else {
            String error = String.format("%s %s", getString(R.string.player_error), errorReason.toString());
            Utilities.getInstance(this).dialogError(this,error);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RECOVERY_REQUEST) {
            // Retry initialization if user performed a recovery action
            getYouTubePlayerProvider().initialize(YOUTUBE_API_KEY, this);
        }
    }

    protected YouTubePlayer.Provider getYouTubePlayerProvider() {
        return youtube_view;
    }


    YouTubePlayer.PlaybackEventListener mPlaybackEventListener = new YouTubePlayer.PlaybackEventListener() {
        @Override
        public void onBuffering(boolean arg0) {
        }

        @Override
        public void onPaused() {

            //hide play/pause controls
            play_video.setVisibility(View.VISIBLE);
            pause_video.setVisibility(View.GONE);

            mHandler.removeCallbacks(runnable);

        }

        @Override
        public void onPlaying() {

            //hide play/pause controls
            play_video.setVisibility(View.GONE);
            pause_video.setVisibility(View.VISIBLE);

            mHandler.postDelayed(runnable, 100);
            displayCurrentTime();
        }

        @Override
        public void onSeekTo(int arg0) {

            //hide play/pause controls
            play_video.setVisibility(View.VISIBLE);
            pause_video.setVisibility(View.GONE);

            mHandler.postDelayed(runnable, 100);
        }

        @Override
        public void onStopped() {

            //hide play/pause controls
            play_video.setVisibility(View.VISIBLE);
            pause_video.setVisibility(View.GONE);

            mHandler.removeCallbacks(runnable);
        }
    };

    YouTubePlayer.PlayerStateChangeListener mPlayerStateChangeListener = new YouTubePlayer.PlayerStateChangeListener() {
        @Override
        public void onAdStarted() {
        }

        @Override
        public void onError(YouTubePlayer.ErrorReason arg0) {
        }

        @Override
        public void onLoaded(String arg0) {
        }

        @Override
        public void onLoading() {
        }

        @Override
        public void onVideoEnded() {
        }

        @Override
        public void onVideoStarted() {
            displayCurrentTime();
        }
    };

    SeekBar.OnSeekBarChangeListener mVideoSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            long lengthPlayed = (mPlayer.getDurationMillis() * progress) / 100;
            mPlayer.seekToMillis((int) lengthPlayed);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            displayCurrentTime();
            mHandler.postDelayed(this, 100);
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.play_video:
                if (null != mPlayer && !mPlayer.isPlaying())
                    mPlayer.play();
                break;
            case R.id.pause_video:
                if (null != mPlayer && mPlayer.isPlaying())
                    mPlayer.pause();
                break;
        }
    }

    private void displayCurrentTime() {
        if (null == mPlayer) return;
        String formattedTime = formatTime(mPlayer.getDurationMillis() - mPlayer.getCurrentTimeMillis());
        mPlayTimeTextView.setText(formattedTime);
    }

    private String formatTime(int millis) {
        int seconds = millis / 1000;
        int minutes = seconds / 60;
        int hours = minutes / 60;

        return (hours == 0 ? "--:" : hours + ":") + String.format("%02d:%02d", minutes % 60, seconds % 60);
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
