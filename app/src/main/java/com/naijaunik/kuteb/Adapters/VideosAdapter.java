package com.naijaunik.kuteb.Adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.RecyclerView;

import com.ct7ct7ct7.androidvimeoplayer.listeners.VimeoPlayerErrorListener;
import com.ct7ct7ct7.androidvimeoplayer.listeners.VimeoPlayerStateListener;
import com.ct7ct7ct7.androidvimeoplayer.view.VimeoPlayerView;
import com.google.android.material.button.MaterialButton;
import com.naijaunik.kuteb.Activities.YoutubePlayerActivity;
import com.naijaunik.kuteb.Model.VideosModel;
import com.naijaunik.kuteb.R;
import com.naijaunik.kuteb.Utils.Utilities;

import java.util.ArrayList;
import java.util.List;

import tcking.github.com.giraffeplayer2.GiraffePlayer;
import tcking.github.com.giraffeplayer2.PlayerListener;
import tcking.github.com.giraffeplayer2.VideoInfo;
import tcking.github.com.giraffeplayer2.VideoView;
import tv.danmaku.ijk.media.player.IjkTimedText;

public class VideosAdapter extends RecyclerView.Adapter<VideosAdapter.VideosAdapterViewHolder>
        implements Filterable {

    private Context mContext;
    private List<VideosModel> videosList;
    private List<VideosModel> videosListFiltered;
    private VideosAdapterListener listener;
    View view;
    private String video_from;
    private String video_url;
    private Utilities utilities;
    Lifecycle lifecycle;

    public VideosAdapter(Context mContext, List<VideosModel> videosList, VideosAdapterListener listener,
                         Lifecycle lifecycle) {
        this.mContext = mContext;
        this.videosList = videosList;
        this.listener = listener;
        this.videosListFiltered = videosList;
        this.lifecycle = lifecycle;
    }

    @NonNull
    @Override
    public VideosAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        view = inflater.inflate(R.layout.videos_layout, null);

        return new VideosAdapterViewHolder(view);
    }


    @SuppressLint("SimpleDateFormat")
    @Override
    public void onBindViewHolder(@NonNull final VideosAdapterViewHolder holder, final int position) {

        utilities = Utilities.getInstance(mContext);

        holder.course_title.setText(videosList.get(position).getCourse_title());
        holder.lesson_title.setText(videosList.get(position).getSection_title());

        video_from = videosList.get(position).getVideo_from();
        video_url = videosList.get(position).getVideo_url();

        if(video_from.equals("server")){

            renderServerVideo(holder,videosList);

        }else if(video_from.equals("youtube")){

            renderYouTubeVideo(holder,videosList);

        }else if (video_from.equals("vimeo")){

            renderVimeovideo(holder,videosList);

        }


        holder.view_lesson_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // send selected book in callback
                try {

                    listener.onVideoSelected(videosListFiltered.get(position),position);

                }catch (Exception e){

                    e.printStackTrace();

                }
            }
        });
    }

    private void renderServerVideo(VideosAdapterViewHolder holder, List<VideosModel> videosList) {

        holder.video_view.setVisibility(View.VISIBLE);

        holder.video_view.getVideoInfo().setBgColor(Color.GRAY).setAspectRatio(VideoInfo.AR_MATCH_PARENT);//config player
        try{
            holder.video_view.setVideoPath(video_url).getPlayer();

            holder.video_view.setPlayerListener(new PlayerListener() {
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

                    utilities.dialogError(mContext,message);
                }
            });


        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void renderYouTubeVideo(VideosAdapterViewHolder holder, List<VideosModel> videosList){

        holder.youtube_video_image_placeholder.setVisibility(View.VISIBLE);

        holder.youtube_video_image_placeholder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Activity activity = (Activity) mContext;

                Intent videoIntent = new Intent(mContext, YoutubePlayerActivity.class);
                videoIntent.putExtra("videocode",video_url);
                activity.startActivity(videoIntent);
                activity.overridePendingTransition(R.anim.right_in, R.anim.left_out);

            }
        });
    }

    private void renderVimeovideo(VideosAdapterViewHolder holder, List<VideosModel> videosList) {

        holder.vimeoPlayer.setVisibility(View.VISIBLE);

        lifecycle.addObserver(holder.vimeoPlayer);

        try {

            holder.vimeoPlayer.initialize(Integer.parseInt(video_url)); // samplevideoId is 458049458

        }catch (Exception e){

            e.printStackTrace();

            utilities.dialogError(mContext, e.getMessage());

        }

        holder.vimeoPlayer.addStateListener(new VimeoPlayerStateListener() {
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
                utilities.dialogError(mContext,"Failed to initialize video player!");
            }
        });

        holder.vimeoPlayer.addErrorListener(new VimeoPlayerErrorListener() {
            @Override
            public void onError(String message, String method, String name) {
                utilities.dialogError(mContext,message);
            }
        });

    }

    @Override
    public int getItemCount() {
        return videosListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    videosListFiltered = videosList;
                } else {
                    List<VideosModel> filteredList = new ArrayList<>();
                    for (VideosModel row : videosList) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for title, description or date match
                        if (row.getSection_title().toLowerCase().contains(charString.toLowerCase())) {

                            filteredList.add(row);
                        }
                    }

                    videosListFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = videosListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                videosListFiltered = (ArrayList<VideosModel>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public interface VideosAdapterListener {
        void onVideoSelected(VideosModel course, int position);
    }


    static class VideosAdapterViewHolder extends RecyclerView.ViewHolder{


        TextView lesson_title,course_title;
        MaterialButton view_lesson_btn;
        ImageView youtube_video_image_placeholder;
        VideoView video_view;
        VimeoPlayerView vimeoPlayer;


        public VideosAdapterViewHolder(@NonNull View itemView) {
            super(itemView);

            lesson_title = itemView.findViewById(R.id.lesson_title);
            course_title = itemView.findViewById(R.id.course_title);
            view_lesson_btn = itemView.findViewById(R.id.view_lesson_btn);
            youtube_video_image_placeholder = itemView.findViewById(R.id.youtube_video_image_placeholder);
            video_view = itemView.findViewById(R.id.video_view);
            vimeoPlayer = itemView.findViewById(R.id.vimeoPlayer);

        }
    }

}