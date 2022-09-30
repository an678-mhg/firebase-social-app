package com.example.android_firebase.api;

import com.example.android_firebase.models.BodyNotification;
import com.example.android_firebase.models.FCMResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface NotificationServices {
    Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();

    NotificationServices notificationServices = new Retrofit.Builder()
            .baseUrl("https://fcm.googleapis.com/fcm/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(NotificationServices.class);

    @Headers({
            "Authorization: key=AAAAs860bjw:APA91bFBn05QM1mYU6TU9Ormh8j05UKuZatCKw746pqtE5BhYf2ojGv2LVpQMKKr_VWEbM9W1NOBQf6GBBzQ05h21rcmC0rxbWgBi3FcwcUOp2qxWRq0EvQxE6ETvg-FfvZYWB2iLo-T",
            "Content-Type: application/json"
    })
    @POST("send")
    Call<FCMResponse> sendNotification(@Body BodyNotification body);
}
