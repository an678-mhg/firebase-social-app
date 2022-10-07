package com.example.android_firebase.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.android_firebase.activity.ChatActivity;
import com.example.android_firebase.models.Conventions;
import com.example.android_firebase.models.User;
import com.google.android.flexbox.FlexboxLayout;
import com.example.android_firebase.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class ConventionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Context context;
    ArrayList<Conventions> conventionsArrayList;
    String userId;

    public ConventionAdapter(Context context, ArrayList<Conventions> conventionsArrayList, String userId) {
        this.context = context;
        this.conventionsArrayList = conventionsArrayList;
        this.userId = userId;
    }

    public void setConventionsArrayList(ArrayList<Conventions> conventionsArrayList) {
        this.conventionsArrayList = conventionsArrayList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.convention_item, parent, false);
        return new ConventionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Conventions convention = conventionsArrayList.get(position);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if(convention == null) return;
        ConventionViewHolder conventionViewHolder = (ConventionViewHolder) holder;
        conventionViewHolder.layoutConventionLoading.setVisibility(View.VISIBLE);
        conventionViewHolder.lastMessage.setEllipsize(TextUtils.TruncateAt.END);
        conventionViewHolder.lastMessage.setMaxLines(1);
        if(convention.getLastMessage().equals("")) {
            conventionViewHolder.lastMessage.setText("No recently messages");
        } else {
            conventionViewHolder.lastMessage.setText(convention.getLastMessage());
        }
        for(String member : convention.getMembers()) {
            if(!member.equals(userId)) {
                db.collection("users").document(member).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Glide.with(context).load(documentSnapshot.getString("photoURL")).into(conventionViewHolder.userAvatar);
                        conventionViewHolder.userName.setText(documentSnapshot.getString("displayName"));
                        conventionViewHolder.layoutConvention.setOnClickListener(v -> startChat(new User(
                                documentSnapshot.getId(),
                                documentSnapshot.getString("photoURL"),
                                documentSnapshot.getString("displayName"),
                                documentSnapshot.getString("email")
                                )
                        ));
                        conventionViewHolder.layoutConventionLoading.setVisibility(View.GONE);
                        conventionViewHolder.layoutConvention.setVisibility(View.VISIBLE);
                    }
                });
            }
        }
    }

    public void startChat(User user) {
        Intent intent = new Intent(context, ChatActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("chatUserId", user.getId());
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    public void release() {
        context = null;
    }

    @Override
    public int getItemCount() {
        if(conventionsArrayList != null) {
            return conventionsArrayList.size();
        }
        return 0;
    }

    public class ConventionViewHolder extends RecyclerView.ViewHolder {
        FlexboxLayout layoutConventionLoading;
        FlexboxLayout layoutConvention;
        ImageView userAvatar;
        TextView userName, lastMessage;

        public ConventionViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutConventionLoading = itemView.findViewById(R.id.layoutConventionLoading);
            layoutConvention = itemView.findViewById(R.id.layoutConvention);
            userAvatar = itemView.findViewById(R.id.userAvatar);
            userName = itemView.findViewById(R.id.userName);
            lastMessage = itemView.findViewById(R.id.userSubName);
        }
    }
}
