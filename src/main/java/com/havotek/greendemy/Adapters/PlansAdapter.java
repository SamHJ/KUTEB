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

import com.naijaunik.kuteb.Model.PlansModel;
import com.naijaunik.kuteb.R;

import java.util.ArrayList;
import java.util.List;

public class PlansAdapter extends RecyclerView.Adapter<PlansAdapter.PlansAdapterViewHolder>
        implements Filterable {

    private Context mContext;
    private List<PlansModel> plansList;
    private List<PlansModel> plansListFiltered;
    private PlansAdapterListener listener;
    String currencySymbol;
    View view;

    public PlansAdapter(Context mContext, List<PlansModel> plansList, PlansAdapterListener listener,
                        String currencySymbol) {
        this.mContext = mContext;
        this.plansList = plansList;
        this.listener = listener;
        this.plansListFiltered = plansList;
        this.currencySymbol = currencySymbol;
    }

    @NonNull
    @Override
    public PlansAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        view = inflater.inflate(R.layout.plans_layout, null);

        return new PlansAdapterViewHolder(view);
    }


    @SuppressLint({"SimpleDateFormat", "SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull final PlansAdapterViewHolder holder, final int position) {

        holder.plan_name.setText(plansList.get(position).getTitle());
        holder.plan_amount.setText(currencySymbol + " " + plansList.get(position).getAmount());
        holder.plan_duration.setText("Plan Duration: " + plansList.get(position).getPlan_duration_name());

        holder.plan_cardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // send selected book in callback
                listener.onPlanSelected(plansListFiltered.get(position), position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return plansListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    plansListFiltered = plansList;
                } else {
                    List<PlansModel> filteredList = new ArrayList<>();
                    for (PlansModel row : plansList) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for title, description or date match
                        if (row.getTitle().toLowerCase().contains(charString.toLowerCase())
                                || row.getTitle().contains(charSequence)
                                || row.getAmount().contains(charSequence)) {

                            filteredList.add(row);
                        }
                    }

                    plansListFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = plansListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                plansListFiltered = (ArrayList<PlansModel>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public interface PlansAdapterListener {
        void onPlanSelected(PlansModel plan, int position);
    }

    class PlansAdapterViewHolder extends RecyclerView.ViewHolder {


        TextView plan_amount,plan_name,plan_duration;
        CardView plan_cardview;


        public PlansAdapterViewHolder(@NonNull View itemView) {
            super(itemView);

            plan_amount = itemView.findViewById(R.id.plan_amount);
            plan_name = itemView.findViewById(R.id.plan_name);
            plan_cardview = itemView.findViewById(R.id.plan_cardview);
            plan_duration = itemView.findViewById(R.id.plan_duration);

        }
    }

}