package com.jsancre.gameverse.fragments;

import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;
import com.jsancre.gameverse.R;
import com.jsancre.gameverse.adapters.ChatAdapter;
import com.jsancre.gameverse.adapters.PostAdapter;
import com.jsancre.gameverse.models.Chat;
import com.jsancre.gameverse.models.Post;
import com.jsancre.gameverse.providers.AuthProvider;
import com.jsancre.gameverse.providers.ChatProvider;

/**
 * Los 'fragments' son utilizados para crear las vistas de las pantallas
 */
public class ChatFragment extends Fragment {
    ChatAdapter mAdapter;
    RecyclerView mRecyclerView;
    View mView;
    ChatProvider mChatProvider;
    AuthProvider mAuthProvider;
    Toolbar mToolbar;

    //--------------------
    //     CONSTRUCTOR
    //--------------------
    public ChatFragment() {
    }

    //--------------------
    //   MÉTODO ONCREATE
    //--------------------
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_chat, container, false);
        mRecyclerView = mView.findViewById(R.id.recyclerViewChats);
        mToolbar = mView.findViewById(R.id.toolbar);

        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Chats");
        mToolbar.setTitleTextColor(Color.WHITE);

        // Este linear layout nos mostrara los post de forma vertical uno debajo de otro...
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager((getContext()));
        mRecyclerView.setLayoutManager(linearLayoutManager);

        mChatProvider = new ChatProvider();
        mAuthProvider = new AuthProvider();

        return mView;
    }
    //este código establece la configuración inicial para mostrar los chats en un RecyclerView.
    // Crea una consulta para obtener los chats del usuario actual, crea un adaptador con las opciones de
    // configuración y el contexto adecuados, y luego vincula el adaptador con el RecyclerView para mostrar los chats.
    // Finalmente, inicia la escucha de cambios en los datos para mantener el adaptador actualizado en tiempo real.
    @Override
    public void onStart() {
        super.onStart();
        Query query = mChatProvider.getAll(mAuthProvider.getUid());
        FirestoreRecyclerOptions<Chat> options =
                new FirestoreRecyclerOptions.Builder<Chat>()
                    .setQuery(query, Chat.class)
                    .build();
        mAdapter = new ChatAdapter(options,getContext());
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAdapter.getListener() != null) {
            mAdapter.getListener().remove();
        }
        if (mAdapter.getListenerLastMessage() != null) {
            mAdapter.getListenerLastMessage().remove();
        }
    }
}