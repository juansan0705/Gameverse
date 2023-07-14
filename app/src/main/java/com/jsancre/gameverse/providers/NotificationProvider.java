package com.jsancre.gameverse.providers;

import com.jsancre.gameverse.models.FCMBody;
import com.jsancre.gameverse.models.FCMResponse;
import com.jsancre.gameverse.retrofit.IFCMApi;
import com.jsancre.gameverse.retrofit.RetrofitClient;

import retrofit2.Call;

public class NotificationProvider {
    private String url = "https://fcm.googleapis.com";

    public NotificationProvider() {
    }
    public Call<FCMResponse> sendNotification(FCMBody body){
        return RetrofitClient.getClient(url).create(IFCMApi.class).send(body);
    }
}
