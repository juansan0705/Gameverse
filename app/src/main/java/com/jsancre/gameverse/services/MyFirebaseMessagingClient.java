package com.jsancre.gameverse.services;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.RemoteInput;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.jsancre.gameverse.R;
import com.jsancre.gameverse.channel.NotificationHelper;
import com.jsancre.gameverse.models.Message;
import com.jsancre.gameverse.receivers.MessageReceiver;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.Map;
import java.util.Random;

public class MyFirebaseMessagingClient extends FirebaseMessagingService {
    //---------------------
    //      ATRIBUTOS
    //---------------------
    public static final String NOTIFICATION_REPLY = "NotificationReply";

    //---------------------
    //      MÉTODOS
    //---------------------
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        Map<String,String> data = message.getData();
        String title = data.get("title");
        String body = data.get("body");
        if (title != null) {
            if (title.equals("NUEVO MENSAJE")){
                showNotificationMessage(data);
            }else {
                showNotification(title, body);
            }
        }
    }

    private void showNotification(String title, String body){
        NotificationHelper notificationHelper = new NotificationHelper(getBaseContext());
        NotificationCompat.Builder builder = notificationHelper.getNotification(title, body);
        Random random = new Random();
        int n = random.nextInt(10000);
        notificationHelper.getManager().notify(n,builder.build());
    }

    private void showNotificationMessage(Map<String, String> data) {
        final String imageSender = data.get("imageSender");
        final String imageReceiver = data.get("imageReceiver");

        getImageSender(data, imageSender, imageReceiver);

    }
    private void notifyMessage(Map<String, String> data, Bitmap bitmapSender,Bitmap bitmapReceiver){
        final String usernameSender = data.get("usernameSender");
        final String usernameReceiver = data.get("usernameReceiver");
        final String lastMessage = data.get("lastMessage");
        String messagesJSON = data.get("messages");
        final String imageSender = data.get("imageSender");
        final String imageReceiver = data.get("imageReceiver");

        final String idSender = data.get("idSender");
        final String idReceiver = data.get("idReceiver");
        final String idChat = data.get("idChat");
        final int idNotification = Integer.parseInt(data.get("idNotification"));

        Intent intent = new Intent(this, MessageReceiver.class);
        intent.putExtra("idSender", idSender);
        intent.putExtra("idReceiver", idReceiver);
        intent.putExtra("idChat", idChat);
        intent.putExtra("idNotification", idNotification);
        intent.putExtra("usernameSender", usernameSender);
        intent.putExtra("usernameReceiver", usernameReceiver);
        intent.putExtra("imageReceiver", imageReceiver);
        intent.putExtra("imageSender", imageSender);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteInput remoteInput = new RemoteInput.Builder(NOTIFICATION_REPLY).setLabel("Tu mensaje...").build();

        final NotificationCompat.Action action = new NotificationCompat.Action.Builder(
                R.mipmap.ic_launcher,
                "Responder",
                pendingIntent)
                .addRemoteInput(remoteInput)
                .build();

        Gson gson = new Gson();
        final Message[] messages = gson.fromJson(messagesJSON, Message[].class);

        NotificationHelper notificationHelper = new NotificationHelper(getBaseContext());
        NotificationCompat.Builder builder =
                notificationHelper.getNotificationMessage(
                        messages,
                        usernameSender,
                        usernameReceiver,
                        lastMessage,
                        bitmapSender,
                        bitmapReceiver,
                        action
                );
        notificationHelper.getManager().notify(idNotification, builder.build());
    }

    private void getImageSender(final Map<String, String> data, final String imageSender, final String imageReceiver) {
        new Handler(Looper.getMainLooper())
                .post(new Runnable() {
                    @Override
                    public void run() {
                        Picasso.with(getApplicationContext())
                                .load("IMAGEN_NO_VALIDA") //Aqui debería ir imageSender pero no me la recupera correctamente
                                .into(new Target() {
                                    @Override
                                    public void onBitmapLoaded(final Bitmap bitmapSender, Picasso.LoadedFrom from) {
                                        getImageReceiver(data, imageReceiver, bitmapSender);
                                    }

                                    @Override
                                    public void onBitmapFailed(Drawable errorDrawable) {
                                        getImageReceiver(data, imageReceiver,null);
                                    }

                                    @Override
                                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                                    }
                                });
                    }
                });
    }

    private void getImageReceiver(final Map<String, String> data, final String imageReceiver, Bitmap bitmapSender){
        Picasso.with(getApplicationContext())
                .load("IMAGEN_NO_VALIDA") //Aqui debería ir imageReceiver pero no me la recupera correctamente
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmapReceiver, Picasso.LoadedFrom from) {
                        notifyMessage(data, bitmapSender, bitmapReceiver);
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {
                        notifyMessage(data, bitmapSender, null);
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });
    }



}
