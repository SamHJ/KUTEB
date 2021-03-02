package com.naijaunik.kuteb.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.naijaunik.kuteb.Model.NotifsModel;
import com.naijaunik.kuteb.R;

import java.util.ArrayList;
import java.util.List;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotifsAdapterViewHolder>
        implements Filterable {

    private Context mContext;
    private List<NotifsModel> notifList;
    private List<NotifsModel> notifListFiltered;
    private NotifsAdapterListener listener;
    View view;

    public NotificationsAdapter(Context mContext, List<NotifsModel> notifList, NotifsAdapterListener listener) {
        this.mContext = mContext;
        this.notifList = notifList;
        this.listener = listener;
        this.notifListFiltered = notifList;
    }

    @NonNull
    @Override
    public NotifsAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        view = inflater.inflate(R.layout.notifs_layout, null);

        return new NotifsAdapterViewHolder(view);
    }


    @SuppressLint("SimpleDateFormat")
    @Override
    public void onBindViewHolder(@NonNull final NotifsAdapterViewHolder holder, final int position) {

        holder.notif_title.setText(notifList.get(position).getTitle());
        holder.notif_description.setText(Html.fromHtml(notifList.get(position).getDescription()));
        holder.notif_date.setText(notifList.get(position).getDate());

        holder.notif_cardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // send selected book in callback
                listener.onNotifSelected(notifListFiltered.get(position),position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notifListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    notifListFiltered = notifList;
                } else {
                    List<NotifsModel> filteredList = new ArrayList<>();
                    for (NotifsModel row : notifList) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for title, description or date match
                        if (row.getTitle().toLowerCase().contains(charString.toLowerCase())
                                || row.getDescription().contains(charSequence)
                                || row.getDate().contains(charSequence)) {

                            filteredList.add(row);
                        }
                    }

                    notifListFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = notifListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                notifListFiltered = (ArrayList<NotifsModel>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public interface NotifsAdapterListener {
        void onNotifSelected(NotifsModel course, int position);
    }


    class NotifsAdapterViewHolder extends RecyclerView.ViewHolder{

        
        TextView notif_title, notif_description,notif_date;
        CardView notif_cardview;


        public NotifsAdapterViewHolder(@NonNull View itemView) {
            super(itemView);

            notif_title = itemView.findViewById(R.id.notif_title);
            notif_description = itemView.findViewById(R.id.notif_description);
            notif_date = itemView.findViewById(R.id.notif_date);
            notif_cardview = itemView.findViewById(R.id.notif_cardview);

        }
    }

}
