package com.naijaunik.kuteb.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.naijaunik.kuteb.Model.HomeSlider;
import com.naijaunik.kuteb.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class SliderAdapter extends RecyclerView.Adapter<SliderAdapter.SlidesAdapterViewHolder>
        implements Filterable {

    private Context mContext;
    private List<HomeSlider> sliderList;
    private List<HomeSlider> sliderListFiltered;
    private SliderAdapterListener listener;
    View view;

    public SliderAdapter(Context mContext, List<HomeSlider> sliderList, SliderAdapterListener listener) {
        this.mContext = mContext;
        this.sliderList = sliderList;
        this.listener = listener;
        this.sliderListFiltered = sliderList;
    }

    @NonNull
    @Override
    public SlidesAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        view = inflater.inflate(R.layout.image_slider_layout_item, null);

        return new SlidesAdapterViewHolder(view);
    }


    @SuppressLint("SimpleDateFormat")
    @Override
    public void onBindViewHolder(@NonNull final SlidesAdapterViewHolder holder, final int position) {

        holder.slider_title.setText(sliderList.get(position).getTitle());
        holder.slider_sub_title.setText(sliderList.get(position).getSubtitle());

        try {
            Picasso.get().load(sliderList.get(position).getImage()).networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.greendemy_logo)
                    .error(R.drawable.greendemy_logo).into(holder.iv_auto_image_slider,
                    new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(sliderList.get(position).getImage())
                                    .placeholder(R.drawable.greendemy_logo)
                                    .error(R.drawable.greendemy_logo).into(holder.iv_auto_image_slider);
                        }

                    });

        } catch (Exception e) {
            e.printStackTrace();
        }

        holder.slider_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // send selected book in callback
                listener.onSlideSelected(sliderListFiltered.get(position), position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return sliderListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    sliderListFiltered = sliderList;
                } else {
                    List<HomeSlider> filteredList = new ArrayList<>();
                    for (HomeSlider row : sliderList) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for title, description or date match
                        if (row.getTitle().toLowerCase().contains(charString.toLowerCase())
                                || row.getTitle().contains(charSequence)
                                || row.getSubtitle().contains(charSequence)) {

                            filteredList.add(row);
                        }
                    }

                    sliderListFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = sliderListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                sliderListFiltered = (ArrayList<HomeSlider>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public interface SliderAdapterListener {
        void onSlideSelected(HomeSlider course, int position);
    }

    class SlidesAdapterViewHolder extends RecyclerView.ViewHolder {


        TextView slider_title,slider_sub_title;
        ImageView iv_auto_image_slider;
        FrameLayout slider_container;


        public SlidesAdapterViewHolder(@NonNull View itemView) {
            super(itemView);

            slider_title = itemView.findViewById(R.id.tv_auto_image_slider);
            slider_sub_title = itemView.findViewById(R.id.sb_auto_image_slider);
            iv_auto_image_slider = itemView.findViewById(R.id.iv_auto_image_slider);
            slider_container = itemView.findViewById(R.id.slider_container);

        }
    }

}