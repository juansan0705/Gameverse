package com.jsancre.gameverse.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jsancre.gameverse.models.User;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Las clases 'Provider' las utilizo para la gestión de información con la base de datos de FireStore
 */
public class UsersProvider {
    //--------------------
    //     ATRIBUTOS
    //--------------------
    private CollectionReference mCollection;

    //--------------------
    //    CONSTRUCTOR
    //--------------------
    public UsersProvider() {
        mCollection = FirebaseFirestore.getInstance().collection("Users");
    }
    
    //--------------------
    //      MÉTODOS
    //--------------------
    //Metodo qye devuelve el usuario de la sesión
    public Task<DocumentSnapshot> getUser(String id) {
        return mCollection.document(id).get();
    }
    public DocumentReference getUserRealtime(String id) {
        return mCollection.document(id);
    }

    //Método para crer un archivo 'User' en FireStore y poder almacenar sus datos posteriormente
    public Task<Void> create(User user) {
        return mCollection.document(user.getId()).set(user);
    }

    //metodo para actualizar datos de los archivos 'User' de firestore
    public Task<Void> update(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("username", user.getUsername());
        map.put("phone", user.getPhone());
        map.put("timestamp", new Date().getTime());
        map.put("image_profile", user.getImage_profile());
        map.put("image_cover", user.getImage_cover());

        return mCollection.document(user.getId()).update(map);
    }

    public Task<Void> updateOnline(String idUser, boolean status) {
        Map<String, Object> map = new HashMap<>();
        map.put("online", status);
        map.put("lastConnect", new Date().getTime());

        return mCollection.document(idUser).update(map);
    }

}
