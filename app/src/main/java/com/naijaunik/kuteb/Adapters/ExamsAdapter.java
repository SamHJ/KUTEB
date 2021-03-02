package com.naijaunik.kuteb.Adapters;

import android.annotation.SuppressLint;
        import android.content.Context;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.Filter;
        import android.widget.Filterable;
        import android.widget.TextView;

        import androidx.annotation.NonNull;
        import androidx.cardview.widget.CardView;
        import androidx.recyclerview.widget.RecyclerView;

import com.naijaunik.kuteb.Model.ExamsModel;
        import com.naijaunik.kuteb.R;

        import java.util.ArrayList;
        import java.util.List;

public class ExamsAdapter extends RecyclerView.Adapter<ExamsAdapter.ExamsAdapterViewHolder>
        implements Filterable {

    private Context mContext;
    private List<ExamsModel> examsList;
    private List<ExamsModel> examsListFiltered;
    private ExamsAdapterListener listener;
    View view;

    public ExamsAdapter(Context mContext, List<ExamsModel> examsList, ExamsAdapterListener listener) {
        this.mContext = mContext;
        this.examsList = examsList;
        this.listener = listener;
        this.examsListFiltered = examsList;
    }

    @NonNull
    @Override
    public ExamsAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        view = inflater.inflate(R.layout.exams_layout, null);

        return new ExamsAdapterViewHolder(view);
    }


    @SuppressLint({"SimpleDateFormat", "SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull final ExamsAdapterViewHolder holder, final int position) {

        holder.exams_title.setText(examsList.get(position).getExam_title());
        holder.no_of_questions.setText("("+examsList.get(position).getNo_of_questions()+" Questions )");

//        if(examsList.get(position).getIs_subject_wise().equals("1")){
//
//            holder.no_of_questions.setVisibility(View.GONE);
//        }

        holder.exams_cardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // send selected book in callback
                listener.onExamSelected(examsListFiltered.get(position), position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return examsListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    examsListFiltered = examsList;
                } else {
                    List<ExamsModel> filteredList = new ArrayList<>();
                    for (ExamsModel row : examsList) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for title, description or date match
                        if (row.getExam_title().toLowerCase().contains(charString.toLowerCase())
                                || row.getNo_of_questions().contains(charSequence)) {

                            filteredList.add(row);
                        }
                    }

                    examsListFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = examsListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                examsListFiltered = (ArrayList<ExamsModel>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public interface ExamsAdapterListener {
        void onExamSelected(ExamsModel exam, int position);
    }

    class ExamsAdapterViewHolder extends RecyclerView.ViewHolder {


        TextView exams_title,no_of_questions;
        CardView exams_cardview;


        public ExamsAdapterViewHolder(@NonNull View itemView) {
            super(itemView);

            exams_title = itemView.findViewById(R.id.exams_title);
            no_of_questions = itemView.findViewById(R.id.no_of_questions);
            exams_cardview = itemView.findViewById(R.id.exams_cardview);

        }
    }

}