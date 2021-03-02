package com.naijaunik.kuteb.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.naijaunik.kuteb.Model.CourseSectionModel;
import com.naijaunik.kuteb.R;

import java.util.ArrayList;
import java.util.List;

public class CourseSectionAdapter extends RecyclerView.Adapter<CourseSectionAdapter.CourseSectionAdapterViewHolder>
        implements Filterable {

    private Context mContext;
    private List<CourseSectionModel> courseSection;
    private List<CourseSectionModel> courseSectionFiltered;
    private CourseSectionAdapterListener listener;
    View view;

    public CourseSectionAdapter(Context mContext, List<CourseSectionModel> courseSection, CourseSectionAdapterListener listener) {
        this.mContext = mContext;
        this.courseSection = courseSection;
        this.listener = listener;
        this.courseSectionFiltered = courseSection;
    }

    @NonNull
    @Override
    public CourseSectionAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        view = inflater.inflate(R.layout.course_lesson_layout, null);

        return new CourseSectionAdapterViewHolder(view);
    }


    @SuppressLint("SimpleDateFormat")
    @Override
    public void onBindViewHolder(@NonNull final CourseSectionAdapterViewHolder holder, final int position) {

        holder.section_title.setText(courseSection.get(position).getSection_title());


        holder.section_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // send selected book in callback
                try {

                    listener.onCourseSectionSelected(courseSectionFiltered.get(position),position);

                }catch (Exception e){

                    e.printStackTrace();

                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return courseSectionFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    courseSectionFiltered = courseSection;
                } else {
                    List<CourseSectionModel> filteredList = new ArrayList<>();
                    for (CourseSectionModel row : courseSection) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for title, description or date match
                        if (row.getSection_title().toLowerCase().contains(charString.toLowerCase())) {

                            filteredList.add(row);
                        }
                    }

                    courseSectionFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = courseSectionFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                courseSectionFiltered = (ArrayList<CourseSectionModel>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public interface CourseSectionAdapterListener {
        void onCourseSectionSelected(CourseSectionModel course, int position);
    }


    class CourseSectionAdapterViewHolder extends RecyclerView.ViewHolder{


        TextView section_title;
        RelativeLayout section_layout;


        public CourseSectionAdapterViewHolder(@NonNull View itemView) {
            super(itemView);

            section_title = itemView.findViewById(R.id.section_title);
            section_layout = itemView.findViewById(R.id.section_layout);

        }
    }

}