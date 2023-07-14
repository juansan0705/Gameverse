package com.jsancre.gameverse.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.jsancre.gameverse.R;
import com.jsancre.gameverse.adapters.MyPostAdapter;
import com.jsancre.gameverse.models.Post;
import com.jsancre.gameverse.providers.AuthProvider;
import com.jsancre.gameverse.providers.PostProvider;
import com.jsancre.gameverse.providers.UsersProvider;
import com.jsancre.gameverse.utils.ViewedMessageHelper;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Clase que gestiona todas las funcionalidades de la pantalla 'Profile'
 */
public class UserProfileActivity extends AppCompatActivity {
    //--------------------
    //     ATRIBUTOS
    //--------------------
    TextView mTextViewUsername;
    TextView mTextViewEmail;
    TextView mTextViewPostNumber;
    TextView mTextViewPostExists;
    ImageView mImageViewCover;
    CircleImageView mCircleImageViewProfile;
    RecyclerView mRecyclerView;

    Toolbar mToolBar;

    UsersProvider mUsersProvider;
    AuthProvider mAuthProvider;
    PostProvider mPostProvider;
    MyPostAdapter mAdapter;
    String mExtraIdUser;
    FloatingActionButton mFabChat;
    ListenerRegistration mListener;

    //-------------------
    //    CONSTRUCTOR
    //-------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        mTextViewEmail = findViewById(R.id.textViewEmail);
        mTextViewPostNumber = findViewById(R.id.textViewPostNumber);
        mTextViewUsername = findViewById(R.id.textViewUsername);
        mTextViewPostExists = findViewById(R.id.textViewPostExist);
        mCircleImageViewProfile = findViewById(R.id.circleImageProfile);
        mImageViewCover = findViewById(R.id.imageViewCover);
        mToolBar = findViewById(R.id.toolbar);
        mFabChat = findViewById(R.id.fabChat);

        // Establecemos el toolbar de la pantalla de vista del 'Profile Activity',
        // este toolbar gestiona la flecha para volver al 'Post Detail Activity'
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mRecyclerView = findViewById(R.id.recyclerViewMyPost);

        //Este linear layout nos mostrara los post de forma vertical uno debajo de otro...
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager((UserProfileActivity.this));
        mRecyclerView.setLayoutManager(linearLayoutManager);

        mUsersProvider = new UsersProvider();
        mAuthProvider = new AuthProvider();
        mPostProvider = new PostProvider();
        mExtraIdUser = getIntent().getStringExtra("idUser");

        //Aqui validamos que el boton de chat no le salga en su propio perfil a el usuario de la sesión.
        if (mAuthProvider.getUid().equals(mExtraIdUser)){
            mFabChat.setVisibility(View.GONE);
        }

        mFabChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToChatActivity();
            }
        });

        getUser();
        getPostNumber();
        checkIfExistPost();
    }



    //-------------------
    //      MÉTODOS
    //-------------------
    //Aqui mediante una consulta a la base de datos gestionada por la clase 'PostProvider', obtiene el numero total de posts de el usuario.
    //Las clases 'Provider' se encargan de todas las consultas a la base de datos
    private void getPostNumber(){
        mPostProvider.getPostByUser(mExtraIdUser).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                int numberPost = queryDocumentSnapshots.size();
                mTextViewPostNumber.setText(String.valueOf(numberPost));
            }
        });
    }

    private void goToChatActivity() {
        Intent intent = new Intent(UserProfileActivity.this, ChatActivity.class);
        intent.putExtra("idUser1", mAuthProvider.getUid());
        intent.putExtra("idUser2", mExtraIdUser);
        //intent.putExtra("idChat", chatId);
        startActivity(intent);
    }

    //Este método consulta en la base de datos todos los posts almacenados y los muestra,
    // lo uso para mostrar las publicaciones de el usuario en un 'Recycler View' (Lista vertical)
    @Override
    public void onStart() {
        super.onStart();
        Query query = mPostProvider.getPostByUser(mExtraIdUser);
        FirestoreRecyclerOptions<Post> options = new FirestoreRecyclerOptions.Builder<Post>().setQuery(query, Post.class).build();
        mAdapter = new MyPostAdapter(options,UserProfileActivity.this );
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.startListening();
        ViewedMessageHelper.updateOnline(true, UserProfileActivity.this);
    }

    //Este metodo se ejecuta cada vez que se cambia de activity o se cierra la aplicacion
    @Override
    protected void onPause() {
        super.onPause();
        ViewedMessageHelper.updateOnline(false, UserProfileActivity.this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mListener != null){
            mListener.remove();
        }
        ViewedMessageHelper.updateOnline(false, UserProfileActivity.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ViewedMessageHelper.updateOnline(true, UserProfileActivity.this);
    }

    @Override
    public void onStop() {
        super.onStop();
        mAdapter.stopListening();
        ViewedMessageHelper.updateOnline(false, UserProfileActivity.this);
    }

    //En este metodos consultamos si el usuario tiene alguna post publicado, para mostrarlo en el titulo de su perfil
    private void checkIfExistPost() {
        mListener = mPostProvider.getPostByUser(mExtraIdUser).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (value != null){
                    int num_posts = value.size();
                    if (num_posts > 0) {
                        mTextViewPostExists.setText("Publicaciones");
                        mTextViewPostExists.setTextColor(Color.BLUE);
                    }
                    else {
                        mTextViewPostExists.setText("No hay publicaciones");
                        mTextViewPostExists.setTextColor(Color.GRAY);
                    }
                }
                //esta variable recoge el numero de posts de el usuario
                if (error != null) {
                    // Con este condicional manejo un posible error de 'NullPointerException'
                    return;
                }
            }
        });
    }

    //Aqui obtenemos la sesión del usuario actual y recuperamos los datos de FireStore para mostrarlos en la pantalla
    private void getUser(){
        mUsersProvider.getUser(mExtraIdUser).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){
                    if (documentSnapshot.contains("email")) {
                        String email = documentSnapshot.getString("email");
                        mTextViewEmail.setText(email);
                    }
                    if (documentSnapshot.contains("username")) {
                        String username = documentSnapshot.getString("username");
                        mTextViewUsername.setText(username);
                    }
                    if (documentSnapshot.contains("image_profile")) {
                        String imageprofile = documentSnapshot.getString("image_profile");
                        if (imageprofile != null){
                            if (!imageprofile.isEmpty()){
                                Picasso.with(UserProfileActivity.this).load(imageprofile).into(mCircleImageViewProfile);
                            }
                        }
                    }
                    if (documentSnapshot.contains("image_cover")) {
                        String imagecover = documentSnapshot.getString("image_cover");
                        if (imagecover != null){
                            if (!imagecover.isEmpty()){
                                Picasso.with(UserProfileActivity.this).load(imagecover).into(mImageViewCover);
                            }
                        }
                    }
                }
            }
        });
    }

    // Método que sobrescribe una clase de android para solucionar un error con la id de el usuario
    // el error es que al pulsas la flecha hacia atras le pasa un id nulo y salta una excepción
    // poniendo finish lo solucionamos.
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return true;
    }
}
