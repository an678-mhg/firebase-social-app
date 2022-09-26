package com.example.android_firebase.api;
import com.example.android_firebase.models.CloudinaryResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface UploadService {

    Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();

    UploadService uploadService = new Retrofit.Builder()
            .baseUrl("https://api.cloudinary.com/v1_1/annnn/image/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(UploadService.class);

    @Multipart
    @POST("upload")
    Call<CloudinaryResponse> uploadImg(@Part MultipartBody.Part file,
                                       @Part("upload_preset") RequestBody upload_preset);
}
