package com.jsancre.gameverse.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.jsancre.gameverse.R;
import com.jsancre.gameverse.activities.ChatActivity;
import com.jsancre.gameverse.models.Chat;
import com.jsancre.gameverse.providers.AuthProvider;
import com.jsancre.gameverse.providers.ChatProvider;
import com.jsancre.gameverse.providers.MessagesProvider;
import com.jsancre.gameverse.providers.UsersProvider;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Esta clase adapta los chats de los usuarios
 */
public class ChatAdapter extends FirestoreRecyclerAdapter<Chat, ChatAdapter.ViewHolder> {

    //---------------------
    //     ATRIBUTOS
    //---------------------
    Context context;
    UsersProvider mUsersProvider;
    AuthProvider mAuthProvider;
    ChatProvider mChatProvider;
    MessagesProvider mMessageProvider;
    ListenerRegistration mListener;
    ListenerRegistration mListenerLastMessage;

    //---------------------
    //     CONSTRUCTOR
    //---------------------
    public ChatAdapter(FirestoreRecyclerOptions<Chat> options, Context context) {
        super(options);
        this.context = context;
        mUsersProvider = new UsersProvider();
        mAuthProvider = new AuthProvider();
        mChatProvider = new ChatProvider();
        mMessageProvider = new MessagesProvider();
    }

    //---------------------
    //     MÉTODOS
    //---------------------

    //Con este metodo cogemos la plantilla del 'cardView_comment' y la llamamos cada vez que se comenta en una publicación
    // Y le insertamos los datos de ese post
    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Chat chat) {
        DocumentSnapshot document = getSnapshots().getSnapshot(position);
        final String chatId = document.getId();
        //id de la persona con la que estamos chateando
        if (mAuthProvider.getUid().equals(chat.getIdUser1())){
            getUserInfo(chat.getIdUser2(), holder);
        }else{
            getUserInfo(chat.getIdUser1(), holder);
        }

        holder.viewHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToChatAcivity(chatId, chat.getIdUser1(), chat.getIdUser2());
            }
        });
        getLastMessage(chatId, holder.textViewLastMessage);
        String idSender = "";
        if (mAuthProvider.getUid().equals(chat.getIdUser1())){
            idSender = chat.getIdUser2();
        }else {
            idSender = chat.getIdUser1();
        }
        getMessageNotRead(chatId, idSender, holder.textViewMessageNotRead, holder.frameLayoutMessageNotRead);
        
    }

    private void getMessageNotRead(String chatId, String idSender, TextView textViewMessageNotRead, FrameLayout frameLayoutMessageNotRead) {

        mListener = mMessageProvider.getMessageByChatAndSender(chatId, idSender).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (value != null) {
                    int size = value.size();
                    if (size > 0) {
                        frameLayoutMessageNotRead.setVisibility(View.VISIBLE);
                        textViewMessageNotRead.setText(String.valueOf(size));
                    } else {
                        frameLayoutMessageNotRead.setVisibility(View.GONE);
                    }
                }
            }
        });

    }

    public ListenerRegistration getListener(){
        return mListener;
    }

    public ListenerRegistration getListenerLastMessage(){
        return mListenerLastMessage;
    }

    private void getLastMessage(String chatId, TextView textViewLastMessage) {
        mListenerLastMessage = mMessageProvider.getLastMessage(chatId).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (value != null){
                    int size = value.size();
                    if (size > 0 ){
                        //Aqui obtenemos en una variable el ultimo mensaje en el chat.
                        String lastMessage = value.getDocuments().get(0).getString("message");
                        textViewLastMessage.setText(lastMessage);
                    }
                }
            }
        });
    }


    private void goToChatAcivity(String chatId, String idUser1, String idUser2) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra("idChat", chatId);
        intent.putExtra("idUser1", idUser1);
        intent.putExtra("idUser2", idUser2);
        context.startActivity(intent);
    }

    //En este metodo llamamos a la clase 'UsersProvider' que contiene todas las consultas a la base de datos
    // para extraer el nombre de usuario y la imagen de perfil y establecerla en el 'Post Detail'
    private void getUserInfo(String idUser, final ViewHolder holder) {
        mUsersProvider.getUser(idUser).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    if (documentSnapshot.contains("username")) {
                        String username = documentSnapshot.getString("username");
                        holder.textViewUsername.setText(username);
                    }
                    if (documentSnapshot.contains("image_profile")) {
                        String imageProfile = documentSnapshot.getString("image_profile");
                        if (imageProfile != null) {
                            if (!imageProfile.isEmpty()) {
                                Picasso.with(context).load(imageProfile).into(holder.circleImageChat);
                            }
                        }
                    }
                }
            }
        });
    }

    // Este metodo que estoy sobrescribiendo crea y devuelve una instancia de ViewHolder para una vista de comentario en un RecyclerView,
    // que se utilizará para mantener referencias a los elementos de la vista y facilitar el acceso y la actualización
    // de los datos del comentario.
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_chat, parent, false);
        return new ViewHolder(view);
    }

    // la clase ViewHolder se utiliza para mantener referencias a los elementos de la vista de un comentario en un RecyclerView,
    // lo cual permite acceder y actualizar fácilmente los datos de ese comentario.
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewUsername;
        TextView textViewLastMessage;
        TextView textViewMessageNotRead;
        FrameLayout frameLayoutMessageNotRead;
        CircleImageView circleImageChat;
        View viewHolder;

        public ViewHolder(View view){
            super(view);
            textViewUsername = view.findViewById(R.id.textViewUsernameChat);
            textViewLastMessage = view.findViewById(R.id.textViewLastMessageChat);
            textViewMessageNotRead = view.findViewById(R.id.textViewMessageNotRead);
            circleImageChat = view.findViewById(R.id.circleImageChat);
            frameLayoutMessageNotRead = view.findViewById(R.id.frameLayoutMessageNotRead);
            viewHolder = view;
        }
    }
}
