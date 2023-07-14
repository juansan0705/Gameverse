package com.jsancre.gameverse.receivers;

import static com.jsancre.gameverse.services.MyFirebaseMessagingClient.NOTIFICATION_REPLY;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.RemoteInput;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.jsancre.gameverse.activities.ChatActivity;
import com.jsancre.gameverse.models.FCMBody;
import com.jsancre.gameverse.models.FCMResponse;
import com.jsancre.gameverse.models.Message;
import com.jsancre.gameverse.providers.MessagesProvider;
import com.jsancre.gameverse.providers.NotificationProvider;
import com.jsancre.gameverse.providers.TokenProvider;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageReceiver extends BroadcastReceiver {

    String mExtraIdSender;
    String mExtraIdReceiver;
    String mExtraIdChat;
    String mExtraUsernameSender;
    String mExtraUsernameReceiver;
    String mExtraImageSender;
    String mExtraImageReceiver;
    int mExtraIdNotification;
    TokenProvider mTokenProvider;
    NotificationProvider mNotificationProvider;

    @Override
    public void onReceive(Context context, Intent intent) {
        mExtraIdSender = intent.getExtras().getString("idSender");
        mExtraIdReceiver = intent.getExtras().getString("idReceiver");
        mExtraIdChat = intent.getExtras().getString("idChat");
        mExtraUsernameSender = intent.getExtras().getString("usernameSender");
        mExtraUsernameReceiver = intent.getExtras().getString("usernameReceiver");
        mExtraImageSender = intent.getExtras().getString("imageSender");
        mExtraImageReceiver = intent.getExtras().getString("imageReceiver");

        mExtraIdNotification = intent.getExtras().getInt("idNotification");

        mTokenProvider = new TokenProvider();
        mNotificationProvider = new NotificationProvider();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(mExtraIdNotification);

        String message = getMessageText(intent).toString();

        sendMessage(message);

    }

    private void sendMessage(String textMessage) {
        final Message message = new Message();
        message.setIdChat(mExtraIdChat);
        message.setIdSender(mExtraIdReceiver);
        message.setIdReceiver(mExtraIdSender);
        message.setTimestamp(new Date().getTime());
        message.setViewed(false);
        message.setIdChat(mExtraIdChat);
        message.setMessage(textMessage);

        MessagesProvider messageProvider = new MessagesProvider();

        messageProvider.create(message).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    getToken(message);
                }
            }
        });
    }

    private void getToken(Message message){
        mTokenProvider.getToken(mExtraIdSender).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()) {
                    if (documentSnapshot.contains("token")){
                        String token = documentSnapshot.getString("token");
                        Gson gson = new Gson();
                        ArrayList<Message> arrayMessages = new ArrayList<>();
                        arrayMessages.add(message);
                        String messages = gson.toJson(arrayMessages);
                        sendNotification(token, messages, message);
                    }
                }
            }
        });
    }
    private void sendNotification(final String token, String messages, Message message){
        final Map<String, String> data = new HashMap<>();
        data.put("title", "NUEVO MENSAJE");
        data.put("body", message.getMessage());
        data.put("idNotification", String.valueOf(mExtraIdNotification));
        data.put("messages", messages);
        data.put("usernameSender", mExtraUsernameReceiver);
        data.put("usernameReceiver", mExtraUsernameSender);
        data.put("idSender", message.getIdSender());
        data.put("idReceiver", message.getIdReceiver());
        data.put("idChat", message.getIdChat());
        data.put("imageSender", mExtraImageReceiver);
        data.put("imageReceiver", mExtraImageSender);


        FCMBody body = new FCMBody(token, "high", "4500s", data);
        mNotificationProvider.sendNotification(body).enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {

            }

            @Override
            public void onFailure(Call<FCMResponse> call, Throwable t) {
                Log.d("ERROR", "El error fue: " + t.getMessage());
            }
        });

    }

    private CharSequence getMessageText(Intent intent){
        Bundle removeInput = RemoteInput.getResultsFromIntent(intent);
        if (removeInput != null) {
            return removeInput.getCharSequence(NOTIFICATION_REPLY);
        }
        return null;
    }

}
