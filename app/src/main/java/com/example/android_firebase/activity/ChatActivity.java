package com.example.android_firebase.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.android_firebase.R;
import com.example.android_firebase.adapter.ChatAdapter;
import com.example.android_firebase.api.NotificationServices;
import com.example.android_firebase.models.BodyNotification;
import com.example.android_firebase.models.Chat;

import com.example.android_firebase.models.Data;
import com.example.android_firebase.models.FCMResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends BaseActivity {
    private String chatUserId;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ImageView backButton, userAvatar;
    private TextView userName, textIsUserOnline, textNoRecentlyMessages;
    private EditText editTextChat;
    private FrameLayout sendButton;
    private ProgressBar progressBar;
    private String currentRoomId;
    private ArrayList<Chat> chatArrayList;
    private ChatAdapter chatAdapter;
    private LinearLayoutManager linearLayoutManager;
    private RecyclerView listMessage;
    private View bottomView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        init();
        listenEvent();
    }

    private void init() {
        backButton = findViewById(R.id.backButton);
        userAvatar = findViewById(R.id.userAvatar);
        userName = findViewById(R.id.userName);
        editTextChat = findViewById(R.id.editTextChat);
        sendButton = findViewById(R.id.buttonSendMess);
        progressBar = findViewById(R.id.progressBar);
        listMessage = findViewById(R.id.listMessage);
        textIsUserOnline = findViewById(R.id.textIsOnlineUser);
        textNoRecentlyMessages = findViewById(R.id.no_recent_messages);
        bottomView = findViewById(R.id.bottomView);

        Bundle bundle = getIntent().getExtras();
        if(bundle == null) return;
        chatUserId = (String) bundle.get("chatUserId");
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        handleCreateRoomChat();

        chatArrayList = new ArrayList<>();
        chatAdapter = new ChatAdapter(ChatActivity.this, chatArrayList, auth.getCurrentUser().getUid(), chatUserId);
        linearLayoutManager = new LinearLayoutManager(ChatActivity.this);
        listMessage.setLayoutManager(linearLayoutManager);
        chatAdapter.setChatArrayList(chatArrayList);
        listMessage.setAdapter(chatAdapter);
    }

    private void updateLastMessage() {
        HashMap<String, Object> updated = new HashMap<>();
        updated.put("lastMessage", editTextChat.getText().toString());
        DocumentReference roomRef = db.collection("rooms").document(currentRoomId);
        roomRef.update(updated);
    }

    private void listenUserOnline(String receiveId) {
        db.collection("users").document(receiveId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error != null) {
                    return;
                }

                if(value.getLong("isOnline") != null) {
                    if(value.getLong("isOnline") == 1) {
                        textIsUserOnline.setVisibility(View.VISIBLE);
                    }
                    else {
                        textIsUserOnline.setVisibility(View.GONE);
                    }
                } else {
                    textIsUserOnline.setVisibility(View.GONE);
                }
            }
        });
    }

    private void sendNotification(String senderName, String userId, String text, String to) {
        Log.e("Start push notification", "start");
        BodyNotification bodyNotification = new BodyNotification(new Data(senderName, userId, text), to);
        NotificationServices.notificationServices.sendNotification(bodyNotification).enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                Log.e("ResponseFCM", String.valueOf(response.body().getFailure()));
            }

            @Override
            public void onFailure(Call<FCMResponse> call, Throwable t) {
                Log.e("ResponseFCM", t.getMessage());
            }
        });
    }

    private void handleLoadMessage(String roomId) {
        db.collection("messages")
                .whereEqualTo("roomId", roomId)
                .orderBy("createAt", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Toast.makeText(ChatActivity.this, "failed to load message", Toast.LENGTH_SHORT).show();
                            Log.e("error query", e.getMessage());
                            progressBar.setVisibility(View.GONE);
                            return;
                        }
                        for (DocumentChange doc : value.getDocumentChanges()) {
                            if(doc.getType() == DocumentChange.Type.ADDED) {
                                String text = doc.getDocument().getString("text");
                                String senderId = doc.getDocument().getString("senderId");
                                String roomId = doc.getDocument().getString("roomId");
                                Timestamp createAt = doc.getDocument().getTimestamp("createAt");
                                Chat chat = new Chat(roomId, text, senderId, createAt);
                                if(chat != null) {
                                    chatArrayList.add(chat);
                                }
                            }
                        }
                        if(chatArrayList.size() > 0) {
                            chatAdapter.notifyDataSetChanged();
                            listMessage.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                            textNoRecentlyMessages.setVisibility(View.GONE);
                            listMessage.smoothScrollToPosition(chatArrayList.size() - 1);
                        } else {
                            progressBar.setVisibility(View.GONE);
                            textNoRecentlyMessages.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    private void handleLoadRoom(String roomId) {
        currentRoomId = roomId;
        db.collection("rooms").document(roomId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()) {
                    ArrayList<String> members = (ArrayList<String>) documentSnapshot.get("members");
                    for(String member: members) {
                        if(!member.equalsIgnoreCase(auth.getCurrentUser().getUid())) {
                            listenUserOnline(member);
                            db.collection("users").document(member).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    if(documentSnapshot.exists()) {
                                        Glide.with(ChatActivity.this).load(documentSnapshot.getString("photoURL")).into(userAvatar);
                                        userName.setText(documentSnapshot.getString("displayName"));
                                    }
                                }
                            });
                            handleLoadMessage(roomId);
                        }
                    }
                }
            }
        });
    }

    private void listenEvent() {
        sendButton.setOnClickListener(v -> createNewMessage());
        backButton.setOnClickListener(v -> onBackPressed());
    }

    private void createNewMessage() {
        if(editTextChat.getText().toString().equals("")) return;
        if(currentRoomId == null) return;
        HashMap<String, Object> message = new HashMap<>();
        message.put("text", editTextChat.getText().toString().trim());
        message.put("senderId", auth.getCurrentUser().getUid());
        message.put("createAt", Timestamp.now());
        message.put("roomId", currentRoomId);
        db.collection("messages").add(message);
        updateLastMessage();
        db.collection("users").document(chatUserId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.getString("token") != null) {
                    if(documentSnapshot.getLong("isOnline") != null && documentSnapshot.getLong("isOnline") == 0) {
                        sendNotification(
                                auth.getCurrentUser().getDisplayName(),
                                auth.getCurrentUser().getUid(),
                                editTextChat.getText().toString(),
                                documentSnapshot.getString("token")
                        );
                    }
                }
                editTextChat.setText("");
            }
        });
    }

    private void handleCreateRoomChat() {
        FirebaseUser user = auth.getCurrentUser();
        ArrayList<String> memberToRoom = new ArrayList<>();
        memberToRoom.add(chatUserId);
        memberToRoom.add(user.getUid());

        ArrayList<String> memberToRoomReverse = new ArrayList<>();
        memberToRoomReverse.add(user.getUid());
        memberToRoomReverse.add(chatUserId);

        db.collection("rooms").whereEqualTo("members", memberToRoom).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    if(task.getResult().getDocuments().size() > 0) {
                        handleLoadRoom(task.getResult().getDocuments().get(0).getId());
                    } else {
                        handleFindRoom(memberToRoomReverse);
                    }
                }
            }
        });
    }

    private void handleFindRoom(ArrayList<String> members) {
        db.collection("rooms").whereEqualTo("members", members).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    if(task.getResult().getDocuments().size() > 0) {
                        handleLoadRoom(task.getResult().getDocuments().get(0).getId());
                    } else {
                        handleCreateNewRoom(members);
                    }
                }
            }
        });
    }

    private void handleCreateNewRoom(ArrayList<String> members) {
        HashMap<String, Object> rooms = new HashMap<>();
        rooms.put("members", members);
        rooms.put("createAt", Timestamp.now());
        rooms.put("lastMessage", "");
        db.collection("rooms").add(rooms).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                if(documentReference.getId() != null) {
                    handleLoadRoom(documentReference.getId());
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(chatAdapter != null) {
            chatAdapter.release();
        }
    }
}