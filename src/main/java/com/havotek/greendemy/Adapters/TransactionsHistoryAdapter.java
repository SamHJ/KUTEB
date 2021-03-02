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

        import com.naijaunik.kuteb.Model.TransactionHistoryModel;
        import com.naijaunik.kuteb.R;

        import java.util.ArrayList;
        import java.util.List;

public class TransactionsHistoryAdapter extends RecyclerView.Adapter<TransactionsHistoryAdapter.HistoryAdapterViewHolder>
        implements Filterable {

    private Context mContext;
    private List<TransactionHistoryModel> historyList;
    private List<TransactionHistoryModel> historyListFiltered;
    private HistoryAdapterListener listener;
    View view;

    public TransactionsHistoryAdapter(Context mContext, List<TransactionHistoryModel> historyList, 
                                      HistoryAdapterListener listener) {
        this.mContext = mContext;
        this.historyList = historyList;
        this.listener = listener;
        this.historyListFiltered = historyList;
    }

    @NonNull
    @Override
    public HistoryAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        view = inflater.inflate(R.layout.transactions_history_layout, null);

        return new HistoryAdapterViewHolder(view);
    }


    @SuppressLint({"SimpleDateFormat", "SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull final HistoryAdapterViewHolder holder, final int position) {

        holder.plan_name.setText("Plan name: "+historyList.get(position).getPlan_name());
        holder.plan_amount.setText("Plan amount: "+historyList.get(position).getPlan_amount());
        holder.plan_duration_name.setText("Plan duration: "+historyList.get(position).getPlan_duration_name());
        holder.date_paid.setText("Date paid: "+historyList.get(position).getDate_paid());
        holder.user_phone.setText("Phone: "+historyList.get(position).getUser_phone());
        holder.user_name.setText("Paid by: "+historyList.get(position).getUser_name());


        holder.history_cardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // send selected book in callback
                try {

                    listener.onHistorySelected(historyListFiltered.get(position),position);

                }catch (Exception e){

                    e.printStackTrace();

                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return historyListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    historyListFiltered = historyList;
                } else {
                    List<TransactionHistoryModel> filteredList = new ArrayList<>();
                    for (TransactionHistoryModel row : historyList) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for title, description or date match
                        if (row.getPlan_name().toLowerCase().contains(charString.toLowerCase())
                        || row.getDate_paid().toLowerCase().contains(charString.toLowerCase())
                        || row.getPlan_amount().toLowerCase().contains(charString.toLowerCase())
                        || row.getPlan_duration_in_days().toLowerCase().contains(charString.toLowerCase())
                        || row.getPlan_duration_name().toLowerCase().contains(charString.toLowerCase())
                        || row.getUser_name().toLowerCase().contains(charString.toLowerCase())
                        || row.getUser_phone().toLowerCase().contains(charString.toLowerCase())) {

                            filteredList.add(row);
                        }
                    }

                    historyListFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = historyListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                historyListFiltered = (ArrayList<TransactionHistoryModel>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public interface HistoryAdapterListener {
        void onHistorySelected(TransactionHistoryModel history, int position);
    }


    class HistoryAdapterViewHolder extends RecyclerView.ViewHolder{


        TextView plan_name,plan_amount,plan_duration_name,date_paid,user_name,user_phone;
        CardView history_cardview;


        public HistoryAdapterViewHolder(@NonNull View itemView) {
            super(itemView);

            plan_name = itemView.findViewById(R.id.plan_name);
            history_cardview = itemView.findViewById(R.id.history_cardview);
            plan_amount = itemView.findViewById(R.id.plan_amount);
            plan_duration_name = itemView.findViewById(R.id.plan_duration_name);
            date_paid = itemView.findViewById(R.id.date_paid);
            user_name = itemView.findViewById(R.id.user_name);
            user_phone = itemView.findViewById(R.id.user_phone);

        }
    }

}