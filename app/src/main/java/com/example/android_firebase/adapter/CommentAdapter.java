package com.example.android_firebase.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.android_firebase.models.Comment;
import com.example.android_firebase.R;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class CommentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private ArrayList<Comment> commentArrayList;

    public CommentAdapter(Context context, ArrayList<Comment> commentArrayList) {
        this.context = context;
        this.commentArrayList = commentArrayList;
    }

    public void setCommentArrayList(ArrayList<Comment> commentArrayList) {
        this.commentArrayList = commentArrayList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_item, parent, false);
        return new CommentViewHolder(view);
    }

    public void release() {
        context = null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Comment comment = commentArrayList.get(position);
        if(comment == null) return;
        CommentViewHolder commentViewHolder = (CommentViewHolder) holder;
        commentViewHolder.commentText.setText(comment.getText());
        commentViewHolder.commentUserSubName.setText(convertTimeStamp(comment.getCreatedAt()));
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(comment.getUserId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()) {
                    commentViewHolder.commentUserName.setText(documentSnapshot.getString("displayName"));
                    Glide.with(context).load(documentSnapshot.getString("photoURL")).into(commentViewHolder.commentUserAvatar);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        commentViewHolder.commentLayoutLoading.setVisibility(View.GONE);
        commentViewHolder.commentLayoutInfo.setVisibility(View.VISIBLE);
    }

    private String convertTimeStamp(Timestamp timestamp) {
        SimpleDateFormat sfd = new SimpleDateFormat("dd/MM/yyyy");
        return sfd.format(timestamp.toDate());
    }

    @Override
    public int getItemCount() {
        if(commentArrayList != null) {
            return commentArrayList.size();
        }
        return 0;
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder {
        ImageView commentUserAvatar;
        TextView commentUserName, commentUserSubName, commentText;
        FlexboxLayout commentLayoutLoading, commentLayoutInfo;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            commentUserAvatar = itemView.findViewById(R.id.commentUserAvatar);
            commentUserSubName = itemView.findViewById(R.id.commentUserSubName);
            commentUserName = itemView.findViewById(R.id.commentUserName);
            commentText = itemView.findViewById(R.id.commentText);
            commentLayoutLoading = itemView.findViewById(R.id.commentLayoutLoading);
            commentLayoutInfo = itemView.findViewById(R.id.commentLayoutInfo);
        }
    }
}
