package com.jsancre.gameverse.retrofit;

import com.jsancre.gameverse.models.FCMBody;
import com.jsancre.gameverse.models.FCMResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMApi {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAZuHA6wU:APA91bFfekw7GQa2lE5XmCpMYi5ht3FR89-R8v88q3mEinRMctvruEI4zECZm8PNS27tGLuqrCpb4Bzgoji3fCULhn2ITpb3aHweKli50AGT-xU3yPKPFyuyFFTWaL9Y6gTKbS48R6Wh"
    })
    @POST("fcm/send")
    Call<FCMResponse> send(@Body FCMBody body);
}
