package com.example.android_firebase.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.android_firebase.models.User;
import com.example.android_firebase.R;

import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private ArrayList<User> userArrayList;

    public UserAdapter(Context context, ArrayList<User> userArrayList) {
        this.context = context;
        this.userArrayList = userArrayList;
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

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userAvatar = itemView.findViewById(R.id.userAvatar);
            userName = itemView.findViewById(R.id.userName);
            userSubName = itemView.findViewById(R.id.userSubName);
        }
    }
}
