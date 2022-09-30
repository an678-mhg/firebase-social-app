package com.example.android_firebase.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.android_firebase.activity.ChatActivity;
import com.example.android_firebase.models.User;
import com.example.android_firebase.R;

import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {
    private Context context;
    private ArrayList<User> userArrayList;
    private ArrayList<User> userArrayListOld;

    public UserAdapter(Context context, ArrayList<User> userArrayList) {
        this.context = context;
        this.userArrayList = userArrayList;
        this.userArrayListOld = userArrayList;
    }

    public void setUserArrayList(ArrayList<User> userArrayList) {
        this.userArrayList = userArrayList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        User user = userArrayList.get(position);
        if(user == null) return;
        UserViewHolder userViewHolder = (UserViewHolder) holder;
        userViewHolder.userName.setText(user.getDisplayName());
        userViewHolder.userSubName.setText("@" + user.getDisplayName());
        Glide.with(context).load(user.getPhotoURL()).into(userViewHolder.userAvatar);
        userViewHolder.userItem.setOnClickListener(v -> startChat(user));
    }

    public void startChat(User user) {
        Intent intent = new Intent(context, ChatActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("chatUserId", user.getId());
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        if(userArrayList != null) {
            return userArrayList.size();
        }
        return 0;
    }

    public class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView userAvatar;
        TextView userName, userSubName;
        LinearLayout userItem;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userAvatar = itemView.findViewById(R.id.userAvatar);
            userName = itemView.findViewById(R.id.userName);
            userSubName = itemView.findViewById(R.id.userSubName);
            userItem = itemView.findViewById(R.id.userItem);
        }
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String keyWord = charSequence.toString();
                if(keyWord.isEmpty()) {
                    userArrayList = userArrayListOld;
                } else {
                    ArrayList<User> users = new ArrayList<>();
                    for(User user : userArrayListOld) {
                        if(user.getDisplayName().toLowerCase().contains(keyWord.toLowerCase())) {
                            users.add(user);
                        }
                    }
                    userArrayList = users;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = userArrayList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                userArrayList = (ArrayList<User>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public void release() {
        context = null;
    }
}
