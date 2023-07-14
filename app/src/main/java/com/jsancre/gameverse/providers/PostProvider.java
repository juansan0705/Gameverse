package com.jsancre.gameverse.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.jsancre.gameverse.models.Post;

/**
 * Las clases 'Provider' las utilizo para la gestión de información con la base de datos de FireStore
 */
public class PostProvider {
    //--------------------
    //     ATRIBUTOS
    //--------------------
    CollectionReference mCollection;

    //--------------------
    //     CONSTRUCTOR
    //--------------------
    public PostProvider() {
        mCollection = FirebaseFirestore.getInstance().collection("Posts");
    }

    //--------------------
    //      MÉTODOS
    //--------------------
    //Metodo para almacenar un archivo post en FireStore
    public Task<Void> save(Post post){
        return mCollection.document().set(post);
    }

    //Metodo que nos devuelve todos los posts ordenados por titulo
    public Query getAll() {
        return mCollection.orderBy("timestamp", Query.Direction.DESCENDING);
    }

    //Método para filtrar los posts por consola y fecha de subida.
    public Query getPostByCategoryAndTimestamp(String category) {
        return mCollection.whereEqualTo("category", category).orderBy("timestamp", Query.Direction.DESCENDING);
    }

    public Query getPostByTitle(String title) {
        //la ultima sentencia es para incluir en la busqueda cualquier coincidencia que incluya el titulo
        return mCollection.orderBy("title").startAt(title).endAt(title+'\uf8ff');
    }

    // Metodo que obtiene todos los posts de un usuario en concreto
    public Query getPostByUser(String id){
        return mCollection.whereEqualTo("idUser", id);

    }
    // metodo que devuelve los posts filtrados por 'id'
    public Task<DocumentSnapshot> getPostById(String id){
        return mCollection.document(id).get();
    }

    public Task<Void> delete(String id){
        return mCollection.document(id).delete();
    }

    public Task<String> getPostTitleById(String postId) {
        return mCollection.document(postId)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Post post = document.toObject(Post.class);
                            return post.getTitle();
                        }
                    }
                    return null;
                });
    }

}
