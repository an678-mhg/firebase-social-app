package com.example.android_firebase.activity;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.android_firebase.R;
import com.example.android_firebase.api.UploadService;
import com.example.android_firebase.models.CloudinaryResponse;
import com.example.android_firebase.models.RealPathUtil;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddPostActivity extends AppCompatActivity {
    private static final int MY_REQUESTED = 10;
    ImageView actionImage, backButton;
    EditText editTextTitle;
    Button selectFile, addPost;
    LinearLayout linearLayoutWrapImage;
    ProgressDialog progressDialog;
    Uri mUri;
    FirebaseFirestore db;

    private ActivityResultLauncher<Intent> mIntentActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode() == Activity.RESULT_OK) {
                        Intent intent = result.getData();
                        if(intent == null) {
                            return;
                        }
                        Uri uri = intent.getData();
                        mUri = uri;
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                            linearLayoutWrapImage.setVisibility(View.VISIBLE);
                            actionImage.setImageBitmap(bitmap);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);
        init();
        listenEvent();
    }

    void init() {
        editTextTitle = findViewById(R.id.editTextTitle);
        selectFile = findViewById(R.id.button_select_file);
        addPost = findViewById(R.id.button_add_post);
        actionImage = findViewById(R.id.action_image);
        linearLayoutWrapImage = findViewById(R.id.wrap_image);
        backButton = findViewById(R.id.backButton);
        db = FirebaseFirestore.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait....");
    }

    void listenEvent() {
        selectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickRequestPermission();
            }
        });

        addPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleAddPost();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void onClickRequestPermission() {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            openGallery();
            return;
        }

        if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            String [] permission = { Manifest.permission.READ_EXTERNAL_STORAGE };
            requestPermissions(permission, MY_REQUESTED);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == MY_REQUESTED) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            }
        }
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(intent.ACTION_GET_CONTENT);
        mIntentActivityResultLauncher.launch(Intent.createChooser(intent, "Select Picture"));
    }

    private void handleAddPost() {
        if(validateDataBeforePost()) {
            handleUploadFile(mUri);
        }
    }

    private boolean validateDataBeforePost() {
        if(editTextTitle.getText().toString().equals("")) {
            editTextTitle.setError("Title is required!");
            return false;
        }
        if(mUri == null) {
            Toast.makeText(this, "Image is required", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void handleAddPostToFireStore(String title, String imageUri) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        Map<String, Object> post = new HashMap<>();
        post.put("title", title);
        post.put("imageUri", imageUri);
        post.put("likes", new ArrayList<String>());
        post.put("userId", firebaseAuth.getCurrentUser().getUid());
        post.put("createAt", Timestamp.now());
        db.collection("posts").add(post)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        progressDialog.dismiss();
                        startActivity(new Intent(AddPostActivity.this, MainActivity.class));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddPostActivity.this, "Add post failed", Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }
                });
    }

    private void handleUploadFile(Uri uri) {
        progressDialog.show();
        String upload_presets = "xhkmjqak";
        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), upload_presets);
        String strRealPath = RealPathUtil.getRealPath(this, uri);
        File mFile = new File(strRealPath);
        RequestBody file = RequestBody.create(MediaType.parse("multipart/form-data"), mFile);
        MultipartBody.Part fileUpload = MultipartBody.Part.createFormData("file", mFile.getName(), file);
        UploadService.uploadService.uploadImg(fileUpload, requestBody).enqueue(new Callback<CloudinaryResponse>() {
            @Override
            public void onResponse(Call<CloudinaryResponse> call, Response<CloudinaryResponse> response) {
                CloudinaryResponse cloudinaryResponse = response.body();
                if(cloudinaryResponse != null) {
                    handleAddPostToFireStore(
                            editTextTitle.getText().toString(),
                            cloudinaryResponse.getUrl()
                    );
                }
                Toast.makeText(AddPostActivity.this, "Add post success", Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<CloudinaryResponse> call, Throwable t) {
                Toast.makeText(AddPostActivity.this, "Add post failed", Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
            }
        });
    }
}