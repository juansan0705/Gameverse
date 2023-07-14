package com.jsancre.gameverse.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.jsancre.gameverse.models.Chat;

import java.util.ArrayList;

public class ChatProvider {
    CollectionReference mCollection;

    public ChatProvider() {
        //Aquí establezco el nombre de la colleción de la base de datos de FireStore (Nombre de la tabla)
        mCollection = FirebaseFirestore.getInstance().collection("Chats");
    }

    //En este metodo creamos una colección dentro de la colección (Chat), que registrara los usuarios que estan chateando
    // esto se tiene que hacer porque estoy trabajando con una base de datos no relacional y por lo tanto solo puedo obtener esta información así
    public void create(Chat chat){
        mCollection.document(chat.getIdUser1() + chat.getIdUser2()).set(chat);
    }

    public Query getAll(String idUser){
        return mCollection.whereArrayContains("ids", idUser);
    }

    public Query getChatByUser1AndUser2(String user1, String user2){
        ArrayList<String> ids = new ArrayList<>();
        ids.add(user1 + user2);
        ids.add(user2 + user1);
        return mCollection.whereIn("id", ids);
    }

}

