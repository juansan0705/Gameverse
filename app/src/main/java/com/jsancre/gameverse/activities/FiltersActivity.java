package com.jsancre.gameverse.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;
import com.jsancre.gameverse.R;
import com.jsancre.gameverse.adapters.PostAdapter;
import com.jsancre.gameverse.models.Post;
import com.jsancre.gameverse.providers.AuthProvider;
import com.jsancre.gameverse.providers.PostProvider;
import com.jsancre.gameverse.utils.ViewedMessageHelper;

public class FiltersActivity extends AppCompatActivity {

    String mExtraCategory;

    AuthProvider mAuthProvider;
    RecyclerView mRecyclerView;
    PostProvider mPostProvider;
    PostAdapter mPostAdapter;

    TextView mTextViewNumberFilter;
    Toolbar mToolBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filters);
        mRecyclerView = findViewById(R.id.recyclerViewFilter);
        mToolBar = findViewById(R.id.toolbar);
        mTextViewNumberFilter = findViewById(R.id.textViewNumberFilter);

        // Establecemos el toolbar de la pantalla de vista del 'Filter Activity',
        // este toolbar gestiona la flecha para salir de la busqueda de publicaciones con filtro.
        mToolBar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("Filtros");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Este linear layout nos mostrara los post de forma vertical uno debajo de otro...
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(FiltersActivity.this);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        //mRecyclerView.setLayoutManager(new GridLayoutManager(FiltersActivity.this, 2));

        mExtraCategory = getIntent().getStringExtra("category");

        mAuthProvider = new AuthProvider();
        mPostProvider = new PostProvider();
    }

    //Este método consulta en la base de datos todos los posts almacenados y los filtra por consola
    @Override
    public void onStart() {
        super.onStart();
        Query query = mPostProvider.getPostByCategoryAndTimestamp(mExtraCategory);
        FirestoreRecyclerOptions<Post> options = new FirestoreRecyclerOptions.Builder<Post>().setQuery(query, Post.class).build();
        mPostAdapter = new PostAdapter(options,FiltersActivity.this, mTextViewNumberFilter);
        mRecyclerView.setAdapter(mPostAdapter);
        mPostAdapter.startListening();
        ViewedMessageHelper.updateOnline(true, FiltersActivity.this);

    }
    //Este metodo se ejecuta cada vez que se cambia de activity o se cierra la aplicacion
    @Override
    protected void onPause() {
        super.onPause();
        ViewedMessageHelper.updateOnline(false, FiltersActivity.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ViewedMessageHelper.updateOnline(true, FiltersActivity.this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ViewedMessageHelper.updateOnline(false, FiltersActivity.this);
    }

    @Override
    public void onStop() {
        super.onStop();
        mPostAdapter.stopListening();
        ViewedMessageHelper.updateOnline(false, FiltersActivity.this);
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