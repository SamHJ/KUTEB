package com.naijaunik.kuteb.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.naijaunik.kuteb.Model.CoursesModel;
import com.naijaunik.kuteb.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class CoursesAdapter extends RecyclerView.Adapter<CoursesAdapter.CoursesAdapterViewHolder>
        implements Filterable {

    private Context mContext;
    private List<CoursesModel> coursesList;
    private List<CoursesModel> coursesListFiltered;
    private CoursesAdapterListener listener;
    View view;

    public CoursesAdapter(Context mContext, List<CoursesModel> coursesList, CoursesAdapterListener listener) {
        this.mContext = mContext;
        this.coursesList = coursesList;
        this.listener = listener;
        this.coursesListFiltered = coursesList;
    }

    @NonNull
    @Override
    public CoursesAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        view = inflater.inflate(R.layout.courses_layout, null);

        return new CoursesAdapterViewHolder(view);
    }


    @SuppressLint("SimpleDateFormat")
    @Override
    public void onBindViewHolder(@NonNull final CoursesAdapterViewHolder holder, final int position) {

        holder.course_title.setText(coursesList.get(position).getTitle());

        try{
            Picasso.get().load(coursesList.get(position).getCourse_icon()).networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.course_placeholder)
                    .error(R.drawable.course_placeholder).into(holder.course_image,
                    new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(coursesList.get(position).getCourse_icon())
                                    .placeholder(R.drawable.course_placeholder)
                                    .error(R.drawable.course_placeholder).into(holder.course_image);
                        }

                    });

        }catch (Exception e){
            e.printStackTrace();
        }

        holder.course_cardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // send selected book in callback
                listener.onCourseSelected(coursesListFiltered.get(position),position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return coursesListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    coursesListFiltered = coursesList;
                } else {
                    List<CoursesModel> filteredList = new ArrayList<>();
                    for (CoursesModel row : coursesList) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for title, description or date match
                        if (row.getTitle().toLowerCase().contains(charString.toLowerCase())
                                || row.getDescription().contains(charSequence)
                                || row.getNo_of_students_enrolled().contains(charSequence)) {

                            filteredList.add(row);
                        }
                    }

                    coursesListFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = coursesListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                coursesListFiltered = (ArrayList<CoursesModel>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public interface CoursesAdapterListener {
        void onCourseSelected(CoursesModel course, int position);
    }


    class CoursesAdapterViewHolder extends RecyclerView.ViewHolder{


        TextView course_title;
        ImageView course_image;
        CardView course_cardview;


        public CoursesAdapterViewHolder(@NonNull View itemView) {
            super(itemView);

            course_title = itemView.findViewById(R.id.course_title);
            course_image = itemView.findViewById(R.id.course_image);
            course_cardview = itemView.findViewById(R.id.course_cardview);

        }
    }

}