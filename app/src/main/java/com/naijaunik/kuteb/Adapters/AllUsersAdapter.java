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

        import com.naijaunik.kuteb.Model.UserModel;
        import com.naijaunik.kuteb.R;
        import com.squareup.picasso.Callback;
        import com.squareup.picasso.NetworkPolicy;
        import com.squareup.picasso.Picasso;

        import java.util.ArrayList;
        import java.util.List;

        import de.hdodenhof.circleimageview.CircleImageView;

public class AllUsersAdapter extends RecyclerView.Adapter<AllUsersAdapter.UsersAdapterViewHolder>
        implements Filterable {

    private Context mContext;
    private List<UserModel> usersList;
    private List<UserModel> usersListFiltered;
    private UsersAdapterListener listener;
    View view;

    public AllUsersAdapter(Context mContext, List<UserModel> usersList, UsersAdapterListener listener) {
        this.mContext = mContext;
        this.usersList = usersList;
        this.listener = listener;
        this.usersListFiltered = usersList;
    }

    @NonNull
    @Override
    public UsersAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        view = inflater.inflate(R.layout.users_layout, null);

        return new UsersAdapterViewHolder(view);
    }


    @SuppressLint({"SimpleDateFormat", "SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull final UsersAdapterViewHolder holder, final int position) {

        holder.user_name.setText(usersList.get(position).getFirst_name() + " "+ usersList.get(position).getLast_name());
        holder.user_phone.setText(usersList.get(position).getPhone());

        String status = usersList.get(position).getStatus();

        holder.user_status.setText(status.equals("admin") ? "Admin" : "Student");

        boolean isBlocked = Integer.parseInt(usersList.get(position).getIs_blocked()) == 1;
        String accessibilityStatus =  isBlocked ? "Blocked" : "Active";
        holder.user_accessiblity_status.setText(accessibilityStatus);

        if(isBlocked){

            holder.user_accessiblity_status.setTextColor(mContext.getResources().getColor(R.color.red));

        }else{

            holder.user_accessiblity_status.setTextColor(mContext.getResources().getColor(R.color.gradient_start_color));
        }

        if(status.equals("admin")){

            holder.user_status.setTextColor(mContext.getResources().getColor(R.color.gradient_start_color));

        }else{

            holder.user_status.setTextColor(mContext.getResources().getColor(R.color.orange));
        }

        try{
            Picasso.get().load(usersList.get(position).getUserimage()).networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.default_profile)
                    .error(R.drawable.default_profile).into(holder.user_image,
                    new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(usersList.get(position).getUserimage())
                                    .placeholder(R.drawable.default_profile)
                                    .error(R.drawable.default_profile).into(holder.user_image);
                        }

                    });

        }catch (Exception e){
            e.printStackTrace();
        }

        holder.user_cardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // send selected book in callback
                listener.onUserSelected(usersListFiltered.get(position),position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return usersListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    usersListFiltered = usersList;
                } else {
                    List<UserModel> filteredList = new ArrayList<>();
                    for (UserModel row : usersList) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for title, description or date match
                        if (row.getFirst_name().toLowerCase().contains(charString.toLowerCase())
                                || row.getLast_name().contains(charSequence)
                                || row.getStatus().contains(charSequence)
                                || row.getEmail().contains(charSequence)
                                || row.getPhone().contains(charSequence)) {

                            filteredList.add(row);
                        }
                    }

                    usersListFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = usersListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                usersListFiltered = (ArrayList<UserModel>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public interface UsersAdapterListener {
        void onUserSelected(UserModel user, int position);
    }


    class UsersAdapterViewHolder extends RecyclerView.ViewHolder{


        TextView user_name,user_phone,user_status,user_accessiblity_status;
        CircleImageView user_image;
        CardView user_cardview;


        public UsersAdapterViewHolder(@NonNull View itemView) {
            super(itemView);

            user_name = itemView.findViewById(R.id.user_name);
            user_status = itemView.findViewById(R.id.user_status);
            user_phone = itemView.findViewById(R.id.user_phone);
            user_image = itemView.findViewById(R.id.user_image);
            user_cardview = itemView.findViewById(R.id.user_cardview);
            user_accessiblity_status = itemView.findViewById(R.id.user_accessiblity_status);

        }
    }

}