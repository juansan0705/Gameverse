package com.jsancre.gameverse.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.jsancre.gameverse.R;
import com.jsancre.gameverse.activities.PostDetailActivity;
import com.jsancre.gameverse.models.Like;
import com.jsancre.gameverse.models.Post;
import com.jsancre.gameverse.providers.AuthProvider;
import com.jsancre.gameverse.providers.LikesProvider;
import com.jsancre.gameverse.providers.PostProvider;
import com.jsancre.gameverse.providers.UsersProvider;
import com.jsancre.gameverse.utils.RelativeTime;
import com.squareup.picasso.Picasso;

import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Esta clase adapta los posts de los usuarios a la pantalla de 'Home' y gestiona la extraccion de información necesaria para los 'Post' con la base de datos
 */
public class MyPostAdapter extends FirestoreRecyclerAdapter<Post, MyPostAdapter.ViewHolder> {

    //---------------------
    //     ATRIBUTOS
    //---------------------
    Context context;
    UsersProvider mUsersProvider;
    LikesProvider mLikesProvider;
    AuthProvider mAuthProvider;
    PostProvider mPostProvider;

    //---------------------
    //     CONSTRUCTOR
    //---------------------
    public MyPostAdapter(FirestoreRecyclerOptions<Post> options, Context context) {
        super(options);
        this.context = context;
        mUsersProvider = new UsersProvider();
        mLikesProvider = new LikesProvider();
        mAuthProvider = new AuthProvider();
        mPostProvider = new PostProvider();
    }

    //---------------------
    //     MÉTODOS
    //---------------------

    //Con este metodo cogemos la plantilla del 'cardView_post' y la llamamos cada vez que se crea una publicación
    @Override
    protected void onBindViewHolder(@NonNull final ViewHolder holder, int position, @NonNull final Post post) {

        DocumentSnapshot document = getSnapshots().getSnapshot(position);
        final String postId = document.getId();
        String relativeTime = RelativeTime.getTimeAgo(post.getTimestamp(), context);
        holder.textViewRelativeTime.setText(relativeTime);
        holder.textViewTitle.setText(post.getTitle().toUpperCase());

        //Aquí ocultamos el boton de borrar publicacion de el perfil siempre y cuando no sea el usuario creador de dicha publicación
        if (post.getIdUser().equals(mAuthProvider.getUid())){
            holder.imageViewDelete.setVisibility(View.VISIBLE);
        }
        else{
            holder.imageViewDelete.setVisibility(View.GONE);
        }

        //validamos que el post tenga una imagen antes de establecerla para que no suponga un error
        if (post.getImage1() != null){
            if (!post.getImage1().isEmpty()) {
                Picasso.with(context).load(post.getImage1()).into(holder.circleImagePost);
            }
        }
        //aqui cuando pulsamos en cualquier parte de el post se abre la pantalla de 'Post Detail' para mostrar completamente el post,
        //le pasamos el id de este para poder recuperarlo desde la otra clase
        holder.viewHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra("id", postId);
                context.startActivity(intent);
            }
        });

        holder.imageViewDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmDelete(postId);
            }
        });

    }

    //Este método creara una ventana modal que preguntara al usuario si esta seguro de borrar la publicación
    private void showConfirmDelete(String idPost){
        new AlertDialog.Builder(context)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Eliminar publicación")
                .setMessage("¿Estas seguro de realizar esta acción?")
                .setPositiveButton("SI", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deletePost(idPost);
                    }
                })
                .setNegativeButton("NO", null)
                .show();

    }

    private void deletePost(String postId) {
        mPostProvider.delete(postId).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(context, "El post se elimino correctamente", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(context, "Hubo un error eliminando el post, inténtelo de nuevo más tarde.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_my_post, parent, false);
        return new ViewHolder(view);
    }

    // la clase ViewHolder se utiliza para mantener referencias a los elementos de la vista de un comentario en un RecyclerView,
    // lo cual permite acceder y actualizar fácilmente los datos de ese comentario.
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle;
        TextView textViewRelativeTime;
        CircleImageView circleImagePost;
        ImageView imageViewDelete;
        View viewHolder;

        public ViewHolder(View view){
            super(view);
            textViewTitle = view.findViewById(R.id.textViewTitleMyPost);
            textViewRelativeTime = view.findViewById(R.id.textViewRelativeTimeMyPost);
            circleImagePost = view.findViewById(R.id.circleImageViewMyPost);
            imageViewDelete = view.findViewById(R.id.imageViewDeleteMyPost);
            viewHolder = view;
        }
    }
}
