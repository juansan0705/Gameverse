package com.jsancre.gameverse.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.jsancre.gameverse.activities.EditProfileActivity;
import com.jsancre.gameverse.activities.MainActivity;
import com.jsancre.gameverse.adapters.MyPostAdapter;
import com.jsancre.gameverse.models.Post;
import com.jsancre.gameverse.providers.AuthProvider;
import com.jsancre.gameverse.providers.PostProvider;
import com.jsancre.gameverse.providers.UsersProvider;
import com.squareup.picasso.Picasso;

import org.checkerframework.checker.units.qual.A;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Los 'fragments' los utilizo para crear las vistas de las pantallas
 */
public class ProfileFragment extends Fragment {
    //--------------------
    //     ATRIBUTOS
    //--------------------
    FloatingActionButton fab_edit;
    View mView;
    TextView mTextViewUsername;
    TextView mTextViewEmail;
    TextView mTextViewPostNumber;
    TextView mTextViewPostExists;
    ImageView mImageViewCover;
    CircleImageView mCircleImageViewProfile;
    FloatingActionButton fab_close_sesion;
    RecyclerView mRecyclerView;
    MyPostAdapter mAdapter;

    UsersProvider mUsersProvider;
    AuthProvider mAuthProvider;
    PostProvider mPostProvider;

    ListenerRegistration mListener;
    private static final int REQUEST_CODE_EDIT_PROFILE = 1;


    //--------------------
    //    CONSTRUCTOR
    //--------------------
    public ProfileFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_profile, container, false);
        fab_edit = mView.findViewById(R.id.fab_edit);
        mTextViewEmail = mView.findViewById(R.id.textViewEmail);
        mTextViewPostNumber = mView.findViewById(R.id.textViewPostNumber);
        mTextViewUsername = mView.findViewById(R.id.textViewUsername);
        mTextViewPostExists = mView.findViewById(R.id.textViewPostExist);
        fab_close_sesion = mView.findViewById(R.id.fab_close_sesion);

        fab_close_sesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        mCircleImageViewProfile = mView.findViewById(R.id.circleImageProfile);
        mImageViewCover = mView.findViewById(R.id.imageViewCover);
        mRecyclerView = mView.findViewById(R.id.recyclerViewMyPost);

        //Este linear layout nos mostrara los post de forma vertical uno debajo de otro...
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager((getContext()));
        mRecyclerView.setLayoutManager(linearLayoutManager);

        fab_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToEditProfile();
            }
        });

        mUsersProvider = new UsersProvider();
        mAuthProvider = new AuthProvider();
        mPostProvider = new PostProvider();

        getUser();
        getPostNumber();
        
        checkIfExistPost();
        
        return mView;
    }

    //En este metodos consultamos si el usuario tiene alguna post publicado, para mostrarlo en el titulo de su perfil
    private void checkIfExistPost() {
        mListener = mPostProvider.getPostByUser(mAuthProvider.getUid()).addSnapshotListener(new EventListener<QuerySnapshot>() {
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

    //Este método consulta en la base de datos todos los posts almacenados y los muestra,
    // lo uso para mostrar las publicaciones de el usuario en un 'Recycler View' (Lista vertical)
    @Override
    public void onStart() {
        super.onStart();
        Query query = mPostProvider.getPostByUser(mAuthProvider.getUid());
        FirestoreRecyclerOptions<Post> options = new FirestoreRecyclerOptions.Builder<Post>().setQuery(query, Post.class).build();
        mAdapter = new MyPostAdapter(options,getContext());
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.startListening();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mListener != null){
            mListener.remove();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }

    //Metodo para acceder a la pantalla de 'Editar perfil'
    private void goToEditProfile() {
        Intent intent = new Intent(getActivity(), EditProfileActivity.class);
        startActivityForResult(intent, REQUEST_CODE_EDIT_PROFILE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_EDIT_PROFILE && resultCode == Activity.RESULT_OK) {
            getUser();
        }
    }

    //este metodo llama al metodo de PostProvider que realiza una consulta sobre cuantos posts hay con el id del usuario de la sesión.
    private void getPostNumber(){
        mPostProvider.getPostByUser(mAuthProvider.getUid()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                int numberPost = queryDocumentSnapshots.size();
                mTextViewPostNumber.setText(String.valueOf(numberPost));
            }
        });
    }

    //Este metodo llama a los Providers que gestionan la sesión de el usuario que ha iniciado sesión y recuperan la información
    // necesaria en la base de datos de dicho usuario para mostrarlos en la pantalla de 'Perfil'
    private void getUser(){
        mUsersProvider.getUser(mAuthProvider.getUid()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
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
                                Picasso.with(getContext()).load(imageprofile).into(mCircleImageViewProfile);
                            }
                        }
                    }
                    if (documentSnapshot.contains("image_cover")) {
                        String imagecover = documentSnapshot.getString("image_cover");
                        if (imagecover != null){
                            if (!imagecover.isEmpty()){
                                Picasso.with(getContext()).load(imagecover).into(mImageViewCover);
                            }
                        }
                    }
                }
            }
        });
    }
    private void logout() {
        mAuthProvider.logout();
        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}