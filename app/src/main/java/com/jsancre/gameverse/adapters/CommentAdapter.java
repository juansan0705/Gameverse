package com.jsancre.gameverse.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.jsancre.gameverse.R;
import com.jsancre.gameverse.activities.PostDetailActivity;
import com.jsancre.gameverse.models.Comment;
import com.jsancre.gameverse.models.Post;
import com.jsancre.gameverse.providers.AuthProvider;
import com.jsancre.gameverse.providers.CommentProvider;
import com.jsancre.gameverse.providers.UsersProvider;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Esta clase adapta los comentarios de los usuarios a la pantalla de 'Post Detail Activity'
 */
public class CommentAdapter extends FirestoreRecyclerAdapter<Comment, CommentAdapter.ViewHolder> {

    //---------------------
    //     ATRIBUTOS
    //---------------------
    Context context;
    UsersProvider mUsersProvider;
    CommentProvider mCommentsProvider;
    AuthProvider mAuthProvider;
    private String mExtraPostId;


    //---------------------
    //     CONSTRUCTOR
    //---------------------
    public CommentAdapter(FirestoreRecyclerOptions<Comment> options, Context context, String mExtraPostId) {
        super(options);
        this.context = context;
        mUsersProvider = new UsersProvider();
        mCommentsProvider = new CommentProvider();
        mAuthProvider = new AuthProvider();
        this.mExtraPostId = mExtraPostId;
    }

    //---------------------
    //     MÉTODOS
    //---------------------

    //Con este metodo cogemos la plantilla del 'cardView_comment' y la llamamos cada vez que se comenta en una publicación
    // Y le insertamos los datos de ese post
    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Comment comment) {
        DocumentSnapshot document = getSnapshots().getSnapshot(position);
        final String commentId = document.getId();
        String idUser = document.getString("idUser");

        holder.textViewComment.setText(comment.getComment());
        getUserInfo(idUser, holder);

        // Establecer el recurso de imagen inicial según el estado del like
        if (comment.getIdLike().contains(mAuthProvider.getUid())) {
            holder.mImageViewLike.setImageResource(R.drawable.up_dark);
        } else {
            holder.mImageViewLike.setImageResource(R.drawable.up);
        }

        // Establecer el recurso de imagen inicial según el estado del dislike
        if (comment.getIdDislike().contains(mAuthProvider.getUid())) {
            holder.mImageViewDislike.setImageResource(R.drawable.down_dark);
        } else {
            holder.mImageViewDislike.setImageResource(R.drawable.down);
        }

        holder.mImageViewLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userId = mAuthProvider.getUid();
                if (comment.getIdLike().contains(userId)) {
                    mCommentsProvider.likeComment(commentId).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            notifyDataSetChanged();
                            updateLikesCounter(holder.likeCounter, comment.getRating());
                        }
                    });
                } else {
                    mCommentsProvider.likeComment(commentId).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            notifyDataSetChanged();
                            updateLikesCounter(holder.likeCounter, comment.getRating());
                        }
                    });
                }
            }
        });

        holder.mImageViewDislike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userId = mAuthProvider.getUid();
                if (comment.getIdDislike().contains(userId)) {
                    mCommentsProvider.dislikeComment(commentId).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            notifyDataSetChanged();
                            updateLikesCounter(holder.likeCounter, comment.getRating());
                        }
                    });
                } else {
                    mCommentsProvider.dislikeComment(commentId).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            notifyDataSetChanged();
                            updateLikesCounter(holder.likeCounter, comment.getRating());
                        }
                    });
                }
            }
        });

        updateLikesCounter(holder.likeCounter, comment.getRating());
    }



    // Método para actualizar el contador de likes en el TextView
    private void updateLikesCounter(TextView textView, int likes) {
        textView.setText(String.valueOf(likes));
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
                                Picasso.with(context).load(imageProfile).into(holder.circleImageViewComment);
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_comment, parent, false);
        return new ViewHolder(view);
    }

    // la clase ViewHolder se utiliza para mantener referencias a los elementos de la vista de un comentario en un RecyclerView,
    // lo cual permite acceder y actualizar fácilmente los datos de ese comentario.
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewUsername;
        TextView textViewComment;
        TextView likeCounter;
        CircleImageView circleImageViewComment;
        ImageView mImageViewLike;
        ImageView mImageViewDislike;

        ImageView mImageViewLikeDark;
        ImageView mImageViewDislikeDark;
        View viewHolder;

        public ViewHolder(View view){
            super(view);
            textViewUsername = view.findViewById(R.id.textViewUsername);
            likeCounter = view.findViewById(R.id.likesCounter);
            textViewComment = view.findViewById(R.id.textViewComment);
            circleImageViewComment = view.findViewById(R.id.circleImageComment);
            mImageViewLike = view.findViewById(R.id.imageViewLike);
            mImageViewDislike = view.findViewById(R.id.imageViewDislike);
            viewHolder = view;
        }
    }
}
