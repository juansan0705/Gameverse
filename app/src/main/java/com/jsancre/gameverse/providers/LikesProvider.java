package com.jsancre.gameverse.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.jsancre.gameverse.models.Like;

/**
 * Las clases 'Provider' las utilizo para la gestión de información con la base de datos de FireStore
 */
public class LikesProvider {

    CollectionReference mCollection;

    public LikesProvider() {
        mCollection = FirebaseFirestore.getInstance().collection("Likes");
    }

    //Método que crea un nuevo archivo en la tabla de Like de FireStore
    public Task<Void> create (Like like) {
        DocumentReference document = mCollection.document();
        String id = document.getId();
        like.setId(id);
        return document.set(like);
    }

    // Metodo que mediante una consulta a la tabla de Like, devuelve la cantidad de likes que tiene un post.
    // para hacer eso consulta en la tabla cuantos archivos de like hay que contengan el mismo idPost y hace el recuento.
    public Query getLikesByPost(String idPost) {
        return mCollection.whereEqualTo("idPost", idPost);
    }

    //Este metodo es usado para saber a que posts le han dado like los usuarios
    public Query getLikeByPostAndUser(String idPost, String idUser){
        return mCollection.whereEqualTo("idPost", idPost).whereEqualTo("idUser", idUser);
    }

    //metodo para borrar el like
    public Task<Void> delete(String id){
        return mCollection.document(id).delete();
    }

}
