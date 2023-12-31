package com.jsancre.gameverse.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.jsancre.gameverse.R;
import com.jsancre.gameverse.adapters.MessageAdapter;
import com.jsancre.gameverse.models.Chat;
import com.jsancre.gameverse.models.FCMBody;
import com.jsancre.gameverse.models.FCMResponse;
import com.jsancre.gameverse.models.Message;
import com.jsancre.gameverse.providers.AuthProvider;
import com.jsancre.gameverse.providers.ChatProvider;
import com.jsancre.gameverse.providers.MessagesProvider;
import com.jsancre.gameverse.providers.NotificationProvider;
import com.jsancre.gameverse.providers.TokenProvider;
import com.jsancre.gameverse.providers.UsersProvider;
import com.jsancre.gameverse.utils.RelativeTime;
import com.jsancre.gameverse.utils.ViewedMessageHelper;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {

    String mExtraIdUser1;
    String mExtraIdUser2;
    String mExtraIdChat;
    String mMyUsername;
    String mUsernameChat;
    String mImageReceiver = "";
    String mImageSender = "";
    long idNotificationChat;

    CircleImageView mCircleImageProfile;
    TextView mTextViewUsername;
    TextView mTextViewRelativeTime;
    ImageView mImageViewBack;
    EditText mEditTextMessage;
    ImageView mImageViewSendMessage;
    View mActionBarView;

    ChatProvider mChatProvider;
    MessagesProvider mMessageProvider;
    AuthProvider mAuthProvider;
    UsersProvider mUserProvider;
    NotificationProvider mNotificationProvider;
    TokenProvider mTokenProvider;

    RecyclerView mRecyclerViewMessage;
    MessageAdapter mAdapter;
    LinearLayoutManager mLinearLayoutManager;
    ListenerRegistration mListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mChatProvider = new ChatProvider();
        mMessageProvider = new MessagesProvider();
        mAuthProvider = new AuthProvider();
        mUserProvider = new UsersProvider();
        mNotificationProvider = new NotificationProvider();
        mTokenProvider = new TokenProvider();

        mEditTextMessage = findViewById(R.id.editTextMessage);
        mImageViewSendMessage = findViewById(R.id.imageViewSendMessage);
        mRecyclerViewMessage = findViewById(R.id.recyclerViewMessages);

        //Este linear layout nos mostrara los post de forma vertical uno debajo de otro...
        mLinearLayoutManager = new LinearLayoutManager((ChatActivity.this));
        //Con esta propiedad establecemos que al entrar en el chat me muestre la pantalla desde el ultimo mensaje que se envio
        mLinearLayoutManager.setStackFromEnd(true);
        mRecyclerViewMessage.setLayoutManager(mLinearLayoutManager);

        mExtraIdUser1 = getIntent().getStringExtra("idUser1");
        mExtraIdUser2 = getIntent().getStringExtra("idUser2");
        mExtraIdChat = getIntent().getStringExtra("idChat");

        showCustomToolbar(R.layout.custom_chat_toolbar);
        getMyInfoUser();

        mImageViewSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        checkIfChatExist();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mAdapter != null) {
            mAdapter.startListening();
        }
        ViewedMessageHelper.updateOnline(true, ChatActivity.this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ViewedMessageHelper.updateOnline(false, ChatActivity.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ViewedMessageHelper.updateOnline(true, ChatActivity.this);
    }

    @Override
    public void onStop() {
        super.onStop();
        mAdapter.stopListening();
        ViewedMessageHelper.updateOnline(false, ChatActivity.this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mListener != null) {
            mListener.remove();
        }
        ViewedMessageHelper.updateOnline(false, ChatActivity.this);
    }

    private void getMessageChat() {
        Query query = mMessageProvider.getMessageByChat(mExtraIdChat);
        FirestoreRecyclerOptions<Message> options =
                new FirestoreRecyclerOptions.Builder<Message>()
                        .setQuery(query, Message.class)
                        .build();
        mAdapter = new MessageAdapter(options, ChatActivity.this);
        mRecyclerViewMessage.setAdapter(mAdapter);
        mAdapter.startListening();
        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                updateViewed();
                int numberMessage = mAdapter.getItemCount();
                int lastMessagePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                //Aqui cuando hay un mensaje nuevo la pantalla se escrollea automaticamente
                if (lastMessagePosition == -1 || (positionStart >= (numberMessage -1) && lastMessagePosition == (positionStart - 1))) {
                    mRecyclerViewMessage.scrollToPosition(positionStart);
                }
            }
        });
    }

    //Aqui capturamos el mensaje que ha escrito el usuario en el EditText y gestionamos el envio al otro usuario
    private void sendMessage() {
        String textMessage = mEditTextMessage.getText().toString();
        if (!textMessage.isEmpty()) {
            final Message message = new Message();
            message.setIdChat(mExtraIdChat);
            if (mAuthProvider.getUid().equals(mExtraIdUser1)) {
                message.setIdSender(mExtraIdUser1);
                message.setIdReceiver(mExtraIdUser2);
            }
            else {
                message.setIdSender(mExtraIdUser2);
                message.setIdReceiver(mExtraIdUser1);
            }
            message.setTimestamp(new Date().getTime());
            message.setViewed(false);
            message.setIdChat(mExtraIdChat);
            message.setMessage(textMessage);

            mMessageProvider.create(message).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        mEditTextMessage.setText("");
                        mAdapter.notifyDataSetChanged();
                        getToken(message);

                    }
                    else {
                        Toast.makeText(ChatActivity.this, "El mensaje no se pudo crear", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void showCustomToolbar(int resource) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mActionBarView = inflater.inflate(resource, null);
        actionBar.setCustomView(mActionBarView);
        mCircleImageProfile = mActionBarView.findViewById(R.id.circleImageProfile);
        mTextViewUsername = mActionBarView.findViewById(R.id.textViewUsername);
        mTextViewRelativeTime = mActionBarView.findViewById(R.id.textViewRelativeTime);
        mImageViewBack = mActionBarView.findViewById(R.id.imageViewBack);

        mImageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        getUserInfo();

    }

    private void getUserInfo() {
        String idUserInfo = "";
        if (mAuthProvider.getUid().equals(mExtraIdUser1)) {
            idUserInfo = mExtraIdUser2;
        }
        else {
            idUserInfo = mExtraIdUser1;
        }

        mListener = mUserProvider.getUserRealtime(idUserInfo).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot.exists()) {
                    if (documentSnapshot.contains("username")) {
                        mUsernameChat = documentSnapshot.getString("username");
                        mTextViewUsername.setText(mUsernameChat);
                    }
                    if (documentSnapshot.contains("online")) {
                        boolean online = documentSnapshot.getBoolean("online");
                        if (online) {
                            mTextViewRelativeTime.setText("En linea");
                        }
                        else if (documentSnapshot.contains("lastConnect")) {
                            long lastConnect = documentSnapshot.getLong("lastConnect");
                            String relativeTime = RelativeTime.getTimeAgo(lastConnect, ChatActivity.this);
                            mTextViewRelativeTime.setText(relativeTime);
                        }
                    }
                    if (documentSnapshot.contains("image_profile")) {
                        mImageReceiver = documentSnapshot.getString("image_profile");
                        if (mImageReceiver != null) {
                            if (!mImageReceiver.equals("")) {
                                Picasso.with(ChatActivity.this).load(mImageReceiver).into(mCircleImageProfile);
                            }
                        }
                    }
                }
            }
        });
    }

    private void checkIfChatExist() {
        mChatProvider.getChatByUser1AndUser2(mExtraIdUser1, mExtraIdUser2).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                int size = queryDocumentSnapshots.size();
                if (size == 0) {
                    createChat();
                }
                else {
                    mExtraIdChat = queryDocumentSnapshots.getDocuments().get(0).getId();
                    idNotificationChat = queryDocumentSnapshots.getDocuments().get(0).getLong("idNotification");
                    getMessageChat();
                    updateViewed();
                }
            }
        });
    }

    private void updateViewed() {
        String idSender = "";

        if (mAuthProvider.getUid().equals(mExtraIdUser1)){
            idSender = mExtraIdUser2;
        } else {
            idSender = mExtraIdUser1;
        }

        //Aqui cambiamos el estado de visto
        mMessageProvider.getMessageByChatAndSender(mExtraIdChat, idSender).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()){
                    mMessageProvider.updateViewed(document.getId(), true);
                }
            }
        });
    }

    //Metodo para crear un chat, recoge los parametros de id de los dos usuarios que van a interactuar, los recoge desde la pantalla UserProfile
    private void createChat() {
        Chat chat = new Chat();
        chat.setIdUser1(mExtraIdUser1);
        chat.setIdUser2(mExtraIdUser2);
        chat.setWritting(false);
        chat.setTimestamp(new Date().getTime());
        chat.setId(mExtraIdUser1 + mExtraIdUser2);
        Random random = new Random();
        int n = random.nextInt(1000000);
        chat.setIdNotification(n);
        idNotificationChat = n;

        ArrayList<String> ids = new ArrayList<>();
        ids.add(mExtraIdUser1);
        ids.add(mExtraIdUser2);
        chat.setIds(ids);
        mChatProvider.create(chat);
        mExtraIdChat = chat.getId();
        getMessageChat();
    }
    private void getToken(final Message message) {
        String idUser = "";
        if (mAuthProvider.getUid().equals(mExtraIdUser1)){
            idUser = mExtraIdUser2;
        } else {
            idUser = mExtraIdUser1;
        }
        mTokenProvider.getToken(idUser).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){
                    if (documentSnapshot.contains("token")){
                        String token = documentSnapshot.getString("token");
                        getLastThreeMessages(message, token);
                    }
                }
                else {
                    Toast.makeText(ChatActivity.this, "El token de notificaciones de el usuario no existe", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getLastThreeMessages(Message message,final String token) {
        mMessageProvider.getLastThreeMessagesByChatAndSender(mExtraIdChat, mAuthProvider.getUid()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                ArrayList <Message> messageArrayList = new ArrayList<>();

                for (DocumentSnapshot d : queryDocumentSnapshots.getDocuments()) {
                    if (d.exists()){
                        Message message = d.toObject(Message.class);
                        messageArrayList.add(message);
                    }
                }

                if (messageArrayList.size() == 0){
                    messageArrayList.add(message);
                }
                //Esta funcion me pone en reverso el arraylist para que en la vista de las notificaciones se vean por orden de llegada
                Collections.reverse(messageArrayList);

                Gson gson = new Gson();
                String messages = gson.toJson(messageArrayList);

                sendNotification(token, messages, message);
            }
        });
    }

    private void sendNotification(final String token, String messages, Message message){
        final Map<String, String> data = new HashMap<>();
        data.put("title", "NUEVO MENSAJE");
        data.put("body", message.getMessage());
        data.put("idNotification", String.valueOf(idNotificationChat));
        data.put("messages", messages);
        data.put("usernameSender", mMyUsername);
        data.put("usernameReceiver", mUsernameChat);
        data.put("idSender", message.getIdSender());
        data.put("idReceiver", message.getIdReceiver());
        data.put("idChat", message.getIdChat());

        if (mImageSender == null || mImageSender.equals("")){
            mImageSender = "IMAGEN_NO_VALIDA";
        }
        if (mImageReceiver == null || mImageReceiver.equals("")){
            mImageReceiver = "IMAGEN_NO_VALIDA";
        }

        data.put("imageSender", mImageSender);
        data.put("imageReceiver", mImageReceiver);

        String idSender = "";
        if (mAuthProvider.getUid().equals(mExtraIdUser1)){
            idSender = mExtraIdUser2;
        } else {
            idSender = mExtraIdUser1;

        }

        mMessageProvider.getLastMessageSender(mExtraIdChat, idSender).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                int size = queryDocumentSnapshots.size();
                String lastMessage = "";
                if (size > 0){
                    lastMessage = queryDocumentSnapshots.getDocuments().get(0).getString("message");
                    data.put("lastMessage", lastMessage);
                }
                FCMBody body = new FCMBody(token, "high", "4500s", data);
                mNotificationProvider.sendNotification(body).enqueue(new Callback<FCMResponse>() {
                    @Override
                    public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                        if (response.body() != null){
                            if (response.body().getSuccess() == 1){
                            }
                            else {
                                Toast.makeText(ChatActivity.this, "La notificación no se pudo enviar", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else {
                            Toast.makeText(ChatActivity.this, "La notificación no se pudo enviar", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<FCMResponse> call, Throwable t) {

                    }
                });
            }
        });
    }

    private void getMyInfoUser() {
        mUserProvider.getUser(mAuthProvider.getUid()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    if (documentSnapshot.contains("username")) {
                        mMyUsername = documentSnapshot.getString("username");
                    }
                    if (documentSnapshot.contains("image_profile")) {
                        mImageSender = documentSnapshot.getString("image_profile");
                    }
                }
            }
        });
    }

}