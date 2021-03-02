package com.naijaunik.kuteb.Adapters;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.naijaunik.kuteb.Activities.WebViewer;
import com.naijaunik.kuteb.Model.HomeSlider;
import com.naijaunik.kuteb.R;
import com.naijaunik.kuteb.Utils.Utilities;
import com.smarteist.autoimageslider.SliderViewAdapter;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;


public class HomeSliderAdapter extends SliderViewAdapter<HomeSliderAdapter.SliderAdapterVH> {

    private Context context;
    List<HomeSlider> sliderList;

    public HomeSliderAdapter(Context context, List<HomeSlider> sliderList) {
        this.context = context;
        this.sliderList = sliderList;
    }

    @Override
    public SliderAdapterVH onCreateViewHolder(ViewGroup parent) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_slider_layout_item, null);
        return new SliderAdapterVH(inflate);
    }

    @Override
    public void onBindViewHolder(final SliderAdapterVH viewHolder, final int position) {

        for(int i = 0; i<sliderList.size(); i++){

            viewHolder.textViewDescription.setText(sliderList.get(position).getTitle());
            viewHolder.textViewSubtitle.setText(sliderList.get(position).getSubtitle());

            try{
                Picasso.get().load(sliderList.get(position).getImage()).networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.greendemy_logo)
                        .error(R.drawable.greendemy_logo).into(viewHolder.imageViewBackground,
                        new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError(Exception e) {
                                Picasso.get().load(sliderList.get(position).getImage())
                                        .placeholder(R.drawable.greendemy_logo)
                                        .error(R.drawable.greendemy_logo).into(viewHolder.imageViewBackground);
                            }

                        });
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!Utilities.cleanString(sliderList.get(position).getGo_to_url()).isEmpty() &&
                        sliderList.get(position).getGo_to_url() != null){

                    //view the slider item in the webViewLoader (i.e WebViewer.class)
                    Activity activity = (Activity) viewHolder.itemView.getContext();
                    Intent intent = new Intent(viewHolder.itemView.getContext(), WebViewer.class);
                    intent.putExtra("slider_go_to_url", sliderList
                            .get(position).getGo_to_url());
                    intent.putExtra("title", sliderList.get(position).getTitle());
                    intent.putExtra("type","url");
                    viewHolder.itemView.getContext().startActivity(intent);
                    activity.overridePendingTransition(R.anim.right_in, R.anim.left_out);
                }
            }
        });


    }

    @Override
    public int getCount() {
        //slider view count could be dynamic size
        return sliderList.size();
    }

    class SliderAdapterVH extends SliderViewAdapter.ViewHolder {

        View itemView;
        ImageView imageViewBackground;
        TextView textViewDescription,textViewSubtitle;

        public SliderAdapterVH(View itemView) {
            super(itemView);
            imageViewBackground = itemView.findViewById(R.id.iv_auto_image_slider);
            textViewDescription = itemView.findViewById(R.id.tv_auto_image_slider);
            textViewSubtitle = itemView.findViewById(R.id.sb_auto_image_slider);
            this.itemView = itemView;
        }
    }
}
