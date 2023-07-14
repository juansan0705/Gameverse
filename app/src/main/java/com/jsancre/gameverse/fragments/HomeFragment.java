package com.jsancre.gameverse.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.Query;
import com.jsancre.gameverse.R;
import com.jsancre.gameverse.activities.HomeActivity;
import com.jsancre.gameverse.activities.MainActivity;
import com.jsancre.gameverse.activities.PostActivity;
import com.jsancre.gameverse.adapters.PostAdapter;
import com.jsancre.gameverse.models.Post;
import com.jsancre.gameverse.providers.AuthProvider;
import com.jsancre.gameverse.providers.PostProvider;


/**
 * Los 'fragments' son utilizados para crear las vistas de las pantallas
 */
public class HomeFragment extends Fragment{
    //--------------------
    //     ATRIBUTOS
    //--------------------
    View mView;
    FloatingActionButton mFab;
    AuthProvider mAuthProvider;
    RecyclerView mRecyclerView;
    PostProvider mPostProvider;
    PostAdapter mPostAdapter;
    PostAdapter mPostAdapterSearch;

    SearchView mSearchView;
    ImageView mImageViewCloseIcon;
    ImageView mImageViewSearchIcon;

    //--------------------
    //    CONSTRUCTOR
    //--------------------
    public HomeFragment() {
    }

    //--------------------
    //      MÉTODOS
    //--------------------

    //Metodo para establecer toda la vista de la pantalla 'Home'
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.fragment_home, container, false);
        mFab = mView.findViewById(R.id.fab);
        mAuthProvider = new AuthProvider();
        mRecyclerView = mView.findViewById(R.id.recyclerViewHome);
        mPostProvider = new PostProvider();

        mSearchView = mView.findViewById(R.id.searchView);

        int closeIconId = mSearchView.getContext().getResources().getIdentifier("android:id/search_close_btn", null, null);
        mImageViewCloseIcon = mSearchView.findViewById(closeIconId);
        if (mImageViewCloseIcon != null) {
            mSearchView.setIconifiedByDefault(false);
            mSearchView.setIconified(false);
            mImageViewCloseIcon.setColorFilter(ContextCompat.getColor(getContext(), R.color.white));
        }

        int searchIconId = mSearchView.getContext().getResources().getIdentifier("android:id/search_mag_icon", null, null);
        mImageViewSearchIcon = mSearchView.findViewById(searchIconId);
        if (mImageViewSearchIcon != null) {
            mImageViewSearchIcon.setColorFilter(ContextCompat.getColor(getContext(), R.color.white));
        }

        mSearchView.setQueryHint("Buscar por título");
        mSearchView.setIconified(false); // Mostrar el SearchView expandido
        mSearchView.clearFocus(); // Evitar que se abra el teclado automáticamente

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Acciones a realizar cuando se envía la búsqueda
                SearchByTitle(query.toLowerCase());
                mSearchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Acciones a realizar cuando el texto de búsqueda cambia
                if (newText.isEmpty()) {
                    // Si el texto de búsqueda está vacío, muestra el ícono de búsqueda
                    mImageViewCloseIcon.setVisibility(View.GONE);
                } else {
                    // Si hay texto de búsqueda, muestra el ícono de borrar
                    mImageViewSearchIcon.setImageResource(R.drawable.baseline_arrow_back_24);
                    mImageViewSearchIcon.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getContext(), HomeActivity.class);
                            startActivity(intent);
                        }
                    });
                    mImageViewCloseIcon.setVisibility(View.VISIBLE);
                    mImageViewCloseIcon.setColorFilter(ContextCompat.getColor(getContext(), R.color.white));
                }
                return true;
            }
        });

        //Este linear layout nos mostrara los post de forma vertical uno debajo de otro...
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager((getContext()));
        mRecyclerView.setLayoutManager(linearLayoutManager);

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToPost();
            }
        });

        return mView;

    }

    private void SearchByTitle(String titulo){
        Query query = mPostProvider.getPostByTitle(titulo);
        FirestoreRecyclerOptions<Post> options = new FirestoreRecyclerOptions.Builder<Post>().setQuery(query, Post.class).build();
        mPostAdapterSearch = new PostAdapter(options,getContext());
        mPostAdapterSearch.notifyDataSetChanged();
        mRecyclerView.setAdapter(mPostAdapterSearch);
        mPostAdapterSearch.startListening();

    }

    //Metodo para acceder a la pantalla 'Post'
    private void goToPost() {
        Intent intent = new Intent(getContext(), PostActivity.class);
        intent.putExtra("idUser", mAuthProvider.getUid());
        startActivity(intent);
    }


    //Este método consulta en la base de datos todos los posts almacenados y los muestra
    @Override
    public void onStart() {
        super.onStart();
        getAllPost();
    }

    private void getAllPost(){
        Query query = mPostProvider.getAll();
        FirestoreRecyclerOptions<Post> options = new FirestoreRecyclerOptions.Builder<Post>().setQuery(query, Post.class).build();
        mPostAdapter = new PostAdapter(options,getContext());
        mPostAdapter.notifyDataSetChanged();
        mRecyclerView.setAdapter(mPostAdapter);
        mPostAdapter.startListening();

    }

    @Override
    public void onStop() {
        super.onStop();
        mPostAdapter.stopListening();
        if (mPostAdapterSearch != null){
            mPostAdapterSearch.stopListening();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPostAdapter != null && mPostAdapter.getListener() != null) {
            mPostAdapter.getListener().remove();
        }
    }



}