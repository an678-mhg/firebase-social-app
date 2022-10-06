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
import com.example.android_firebase.R;
import com.example.android_firebase.models.Chat;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    int TYPE_SENDER = 0;
    int TYPE_RECEIVE = 1;

    private Context context;
    private ArrayList<Chat> chatArrayList;
    private String userId;
    private String receiveId;

    public ChatAdapter(Context context, ArrayList<Chat> chatArrayList, String userId, String receiveId) {
        this.context = context;
        this.chatArrayList = chatArrayList;
        this.userId = userId;
        this.receiveId = receiveId;
    }

    public void setChatArrayList(ArrayList<Chat> chatArrayList) {
        this.chatArrayList = chatArrayList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View viewSender = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_type_sender, parent, false);
        View viewReceive = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_type_receive, parent, false);
        if(viewType == TYPE_SENDER) {
            return new SenderViewHolder(viewSender);
        }
        return new ReceiveViewHolder(viewReceive);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Chat chat = chatArrayList.get(position);
        if(chat == null) return;
        if(TYPE_SENDER == holder.getItemViewType()) {
            SenderViewHolder senderViewHolder = (SenderViewHolder) holder;
            senderViewHolder.textMessage.setText(chat.getText());
            senderViewHolder.textDate.setText(convertTimeStamp(chat.getCreateAt()));
        } else {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            ReceiveViewHolder receiveViewHolder = (ReceiveViewHolder) holder;
            receiveViewHolder.textMessage.setText(chat.getText());
            receiveViewHolder.textDate.setText(convertTimeStamp(chat.getCreateAt()));
            db.collection("users").document(receiveId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if(documentSnapshot.exists()) {
                        Glide.with(context).load(documentSnapshot.getString("photoURL")).into(receiveViewHolder.imageReceive);
                    }
                }
            });
        }
    }

    private String convertTimeStamp(Timestamp timestamp) {
        SimpleDateFormat sfd = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        return sfd.format(timestamp.toDate());
    }

    @Override
    public int getItemCount() {
        if(chatArrayList != null) {
            return chatArrayList.size();
        }
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        Chat chat = chatArrayList.get(position);
        if(chat.getSenderId().equals(userId)) {
            return TYPE_SENDER;
        }
        return TYPE_RECEIVE;
    }

    public class SenderViewHolder extends RecyclerView.ViewHolder {
        TextView textMessage, textDate;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            textMessage = itemView.findViewById(R.id.textMessage);
            textDate = itemView.findViewById(R.id.textDateTime);
        }
    }

    public void release() {
        context = null;
    }

    public class ReceiveViewHolder extends RecyclerView.ViewHolder {
        TextView textMessage, textDate;
        ImageView imageReceive;

        public ReceiveViewHolder(@NonNull View itemView) {
            super(itemView);
            textMessage = itemView.findViewById(R.id.textMessage);
            textDate = itemView.findViewById(R.id.textDateTime);
            imageReceive = itemView.findViewById(R.id.imageReceive);
        }
    }
}
