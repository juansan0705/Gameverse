package com.jsancre.gameverse.providers;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.jsancre.gameverse.models.Comment;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Las clases 'Provider' las utilizo para la gestión de información con la base de datos de FireStore
 */
public class CommentProvider {
    CollectionReference mCollection;
    private AuthProvider mAuthProvider;

    //Metodo que recupera la tabla 'Comments' de FireStore
    public CommentProvider() {
        mCollection = FirebaseFirestore.getInstance().collection("Comments");
        mAuthProvider = new AuthProvider();
    }
    public CollectionReference getCollection() {
        return mCollection;
    }

    // método para crear un comentario, llama a la clase model 'Comment' y lo almacena en la tabla correspondiente en FireStore
    public Task<Void> create (Comment comment) {
        return mCollection.document().set(comment);
    }

    // Metodo que te devuelve los comentarios de un post, haciendo una consula a la base de datos y pasandole como parametro el id de el post
    public Query getCommentsByPost(String idPost){
        return mCollection.whereEqualTo("idPost", idPost);
    }

    public Query getCommentsByPostOrderByRating(String idPost){
        return mCollection.whereEqualTo("idPost", idPost).orderBy("rating", Query.Direction.DESCENDING);
    }

    public Task<Void> likeComment(String commentId) {
        String userId = mAuthProvider.getUid();
        return mCollection.document(commentId).get().continueWithTask(task -> {
            if (task.isSuccessful()) {
                Comment comment = task.getResult().toObject(Comment.class);
                if (comment != null) {
                    if (comment.getIdLike().contains(userId)) {
                        // El usuario ya había dado like, así que se elimina el like
                        comment.getIdLike().remove(userId);
                    } else {
                        // El usuario no había dado like, se agrega el like y se elimina el dislike si existe
                        comment.getIdLike().add(userId);
                        comment.getIdDislike().remove(userId);
                    }
                    comment.setRating(comment.getIdLike().size() - comment.getIdDislike().size());
                    return mCollection.document(commentId).set(comment);
                }
            }
            return null;
        });
    }

    public Task<Void> dislikeComment(String commentId) {
        String userId = mAuthProvider.getUid();
        return mCollection.document(commentId).get().continueWithTask(task -> {
            if (task.isSuccessful()) {
                Comment comment = task.getResult().toObject(Comment.class);
                if (comment != null) {
                    if (comment.getIdDislike().contains(userId)) {
                        // El usuario ya había dado dislike, así que se elimina el dislike
                        comment.getIdDislike().remove(userId);
                    } else {
                        // El usuario no había dado dislike, se agrega el dislike y se elimina el like si existe
                        comment.getIdDislike().add(userId);
                        comment.getIdLike().remove(userId);
                    }
                    comment.setRating(comment.getIdLike().size() - comment.getIdDislike().size());
                    return mCollection.document(commentId).set(comment);
                }
            }
            return null;
        });
    }



}
