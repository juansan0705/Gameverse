package com.jsancre.gameverse.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.jsancre.gameverse.activities.PostDetailActivity;
import com.jsancre.gameverse.models.Like;
import com.jsancre.gameverse.models.Post;
import com.jsancre.gameverse.providers.AuthProvider;
import com.jsancre.gameverse.providers.LikesProvider;
import com.jsancre.gameverse.providers.PostProvider;
import com.jsancre.gameverse.providers.UsersProvider;
import com.squareup.picasso.Picasso;

import java.util.Date;

/**
 * Esta clase adapta los posts de los usuarios a la pantalla de 'Home' y gestiona la extraccion de información necesaria para los 'Post' con la base de datos
 */
public class PostAdapter extends FirestoreRecyclerAdapter<Post, PostAdapter.ViewHolder> {

    //---------------------
    //     ATRIBUTOS
    //---------------------
    Context context;
    UsersProvider mUsersProvider;

    LikesProvider mLikesProvider;
    TextView mTextViewNumberFilter;

    AuthProvider mAuthProvider;
    ListenerRegistration mListener;

    //---------------------
    //     CONSTRUCTOR
    //---------------------
    public PostAdapter(FirestoreRecyclerOptions<Post> options, Context context) {
        super(options);
        this.context = context;
        mUsersProvider = new UsersProvider();
        mLikesProvider = new LikesProvider();
        mAuthProvider = new AuthProvider();
    }

    public PostAdapter(FirestoreRecyclerOptions<Post> options, Context context, TextView textView) {
        super(options);
        this.context = context;
        mUsersProvider = new UsersProvider();
        mLikesProvider = new LikesProvider();
        mAuthProvider = new AuthProvider();
        this.mTextViewNumberFilter = textView;
    }

    //---------------------
    //     MÉTODOS
    //---------------------

    //Con este metodo cogemos la plantilla del 'cardView_post' y la llamamos cada vez que se crea una publicación
    @Override
    protected void onBindViewHolder(@NonNull final ViewHolder holder, int position, @NonNull final Post post) {

        DocumentSnapshot document = getSnapshots().getSnapshot(position);
        final String postId = document.getId();

        if (mTextViewNumberFilter != null){
            int numberFilter = getSnapshots().size();
            mTextViewNumberFilter.setText(String.valueOf(numberFilter));
        }

        holder.textViewTitle.setText(post.getTitle().toUpperCase());
        holder.textViewDescription.setText(post.getDescription());

        //validamos que el post tenga una imagen antes de establecerla para que no suponga un error
        if (post.getImage1() != null){
            if (!post.getImage1().isEmpty()) {
                Picasso.with(context).load(post.getImage1()).into(holder.imageViewPost);
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
        //cuando le damos me gusta en la publicacion llamamos a un onClickListener y cambiamos la imagen y aumentamos el contador
        holder.imageViewLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Like like = new Like();
                like.setIdUser(mAuthProvider.getUid());
                like.setIdPost(postId);
                like.setTimestamp(new Date().getTime());
                like(like, holder);
            }
        });
        //Llamamos a los metodos creados más abajo que gestionan los likes de los post y los autores de estos
        getUserInfo(post.getIdUser(), holder);
        getNumberLikesByPost(postId, holder);
        checkIfExistsLike(postId, mAuthProvider.getUid(), holder);
    }

    //este método recupera de la base de datos con una consulta que esta echa en la clase 'LikesProvider' los likes de la publicación
    private void getNumberLikesByPost(String idPost,final ViewHolder holder){
        mListener = mLikesProvider.getLikesByPost(idPost).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    // Manejar el error de Firestore aquí
                    return;
                }
                if (value != null) {
                    int numberLikes = value.size();
                    if (value.size() == 1){
                        holder.textViewLikes.setText(String.valueOf(numberLikes));
                    }else {
                        holder.textViewLikes.setText(String.valueOf(numberLikes));
                    }
                } else {
                    // Manejar la situación cuando no hay documentos que coincidan con la consulta
                }
            }
        });
    }



    //Este metodo gestiona la funcionalidad de el like, cambiando las imagenes entre sí y eliminado el like de la base de datos cuando sea así
    private void like(final Like like, final ViewHolder holder) {
        mLikesProvider.getLikeByPostAndUser(like.getIdPost(), mAuthProvider.getUid()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                int numberDocuments = queryDocumentSnapshots.size();
                if (numberDocuments > 0) {
                    String idLike = queryDocumentSnapshots.getDocuments().get(0).getId();
                    holder.imageViewLike.setImageResource(R.drawable.unlike);
                    mLikesProvider.delete(idLike);
                }
                else {
                    holder.imageViewLike.setImageResource(R.drawable.like);
                    mLikesProvider.create(like);
                }
            }
        });

    }

    //este metodo checkea cuando se incia sesion si ese usuario ha pulsado like anteriormente a una publicacion para que salga marcado de antemano
    private void checkIfExistsLike(String idPost,String idUser, final ViewHolder holder) {
        mLikesProvider.getLikeByPostAndUser(idPost, idUser).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                int numberDocuments = queryDocumentSnapshots.size();
                if (numberDocuments > 0) {
                    holder.imageViewLike.setImageResource(R.drawable.like);
                }
                else {
                    holder.imageViewLike.setImageResource(R.drawable.unlike);
                }
            }
        });

    }

    //Aqui recuperamos de la base de datos el autor del post y lo mostramos en las publicaciones del 'Home'
    private void getUserInfo(String idUser, ViewHolder holder) {
        mUsersProvider.getUser(idUser).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){
                    if (documentSnapshot.contains("username")){
                        String username = documentSnapshot.getString("username");
                        holder.textViewUsername.setText(username.toUpperCase());
                    }
                }
            }
        });
    }

    public ListenerRegistration getListener(){
        return  mListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_post, parent, false);
        return new ViewHolder(view);
    }

    // la clase ViewHolder se utiliza para mantener referencias a los elementos de la vista de un comentario en un RecyclerView,
    // lo cual permite acceder y actualizar fácilmente los datos de ese comentario.
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle;
        TextView textViewDescription;
        TextView textViewUsername;
        TextView textViewLikes;
        ImageView imageViewPost;
        ImageView imageViewLike;
        View viewHolder;

        public ViewHolder(View view){
            super(view);
            textViewTitle = view.findViewById(R.id.textViewTitlePostCard);
            textViewDescription = view.findViewById(R.id.textViewDescriptionPostCard);
            textViewUsername = view.findViewById(R.id.textViewUsernamePostCard);
            imageViewPost = view.findViewById(R.id.imageViewPostCard);
            imageViewLike = view.findViewById(R.id.imageViewLike);
            textViewLikes = view.findViewById(R.id.textViewLikes);
            viewHolder = view;
        }
    }
}
