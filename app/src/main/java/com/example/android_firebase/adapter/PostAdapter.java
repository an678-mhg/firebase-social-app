package com.example.android_firebase.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.android_firebase.R;
import com.example.android_firebase.activity.EditPostActivity;
import com.example.android_firebase.activity.PostDetailActivity;
import com.example.android_firebase.models.Post;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<Post> postArrayList;
    private String userId;
    private Context context;

    public PostAdapter(ArrayList<Post> postArrayList, String userId, Context context) {
        this.postArrayList = postArrayList;
        this.userId = userId;
        this.context = context;
    }

    public void setPosts(ArrayList<Post> postArrayList) {
        this.postArrayList = postArrayList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item, parent, false);
        return new PostViewViewHolder(view);
    }

    public void release() {
        context = null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Post post = postArrayList.get(position);
        if(post == null) return;
        PostViewViewHolder postViewViewHolder = (PostViewViewHolder) holder;
        postViewViewHolder.postTitle.setEllipsize(TextUtils.TruncateAt.END);
        postViewViewHolder.postTitle.setMaxLines(1);
        postViewViewHolder.postTitle.setText(post.getTitle());
        Glide.with(context).load(post.getImageUri()).into(postViewViewHolder.postImageContent);
        postViewViewHolder.userSubName.setText(convertTimeStamp(post.getCreatedAt()));

        if(userId.equals(post.getUserId())) {
            postViewViewHolder.layoutEdit.setVisibility(View.VISIBLE);
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(post.getUserId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot != null) {
                    String photoURL = documentSnapshot.getString("photoURL");
                    String displayName = documentSnapshot.getString("displayName");
                    postViewViewHolder.userName.setText(displayName);
                    if(context != null) {
                        Glide.with(context).load(photoURL).into(postViewViewHolder.userAvatar);
                    }
                }
                postViewViewHolder.boxUserInfoLoading.setVisibility(View.GONE);
                postViewViewHolder.boxUserInfo.setVisibility(View.VISIBLE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Get post failed!", Toast.LENGTH_SHORT).show();
            }
        });

        if(post.getLikes() != null) {
            postViewViewHolder.textLikeCount.setText(String.valueOf(post.getLikes().size()));
            if(post.getLikes().contains(userId)) {
                postViewViewHolder.iconLike.setImageResource(R.drawable.ic_baseline_thumb_up_24_blue);
            } else {
                postViewViewHolder.iconLike.setImageResource(R.drawable.ic_baseline_thumb_up_24);
            }
        }

        postViewViewHolder.postItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickDetail(post);
            }
        });

        postViewViewHolder.buttonLikePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleLikePost(post);
            }
        });

        postViewViewHolder.buttonRemove.setOnClickListener(v -> handleDeletePost(post.getId(), position));

        postViewViewHolder.buttonEdit.setOnClickListener(v -> handleClickButtonEdit(post.getId(), post.getTitle()));
    }

    private String convertTimeStamp(Timestamp timestamp) {
        SimpleDateFormat sfd = new SimpleDateFormat("dd/MM/yyyy");
        return sfd.format(timestamp.toDate());
    }

    private void onClickDetail(Post post) {
        Intent intent = new Intent(context, PostDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("postId", post.getId());
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    private void handleLikePost(Post post) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        HashMap<String, Object> newData = new HashMap<>();
        if(post.getLikes().contains(userId)) {
            post.getLikes().remove(userId);
        } else {
            post.getLikes().add(userId);
        }
        newData.put("likes", post.getLikes());
        notifyDataSetChanged();
        db.collection("posts").document(post.getId()).update(newData);
    }

    private void handleDeletePost(String postId, int index) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle("Do you want to delete this post?");
        alertDialog.setIcon(R.drawable.ic_outline_info_24);

        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                postArrayList.remove(index);
                notifyDataSetChanged();
                db.collection("posts").document(postId).delete();
            }
        });
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        alertDialog.show();
    }

    private void handleClickButtonEdit(String postId, String postTitle) {
        Intent intent = new Intent(context, EditPostActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("postId", postId);
        bundle.putString("postTitle", postTitle);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        if(postArrayList != null) {
            return postArrayList.size();
        }
        return 0;
    }

    public class PostViewViewHolder extends RecyclerView.ViewHolder {
        TextView userName, userSubName, postTitle, textLikeCount;
        CircleImageView userAvatar;
        ImageView postImageContent;
        CardView postItem;
        FlexboxLayout buttonLikePost;
        ImageView iconLike;
        FlexboxLayout boxUserInfo;
        FlexboxLayout boxUserInfoLoading;
        FlexboxLayout layoutEdit;
        ImageView buttonEdit;
        ImageView buttonRemove;

        public PostViewViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.userName);
            userSubName = itemView.findViewById(R.id.userSubName);
            postTitle = itemView.findViewById(R.id.postTitle);
            userAvatar = itemView.findViewById(R.id.userAvatar);
            postImageContent = itemView.findViewById(R.id.postImageContent);
            postItem = itemView.findViewById(R.id.postItem);
            buttonLikePost = itemView.findViewById(R.id.buttonLikePost);
            iconLike = itemView.findViewById(R.id.iconLike);
            textLikeCount = itemView.findViewById(R.id.textLikeCount);
            boxUserInfo = itemView.findViewById(R.id.box_info_user);
            boxUserInfoLoading = itemView.findViewById(R.id.box_info_user_loading);
            layoutEdit = itemView.findViewById(R.id.layoutEdit);
            buttonEdit = itemView.findViewById(R.id.buttonEdit);
            buttonRemove = itemView.findViewById(R.id.buttonRemove);
        }
    }
}
