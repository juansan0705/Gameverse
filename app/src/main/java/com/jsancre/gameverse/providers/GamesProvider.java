package com.jsancre.gameverse.providers;

import android.content.Context;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.jsancre.gameverse.R;
import com.jsancre.gameverse.models.Chat;
import com.jsancre.gameverse.models.Games;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GamesProvider {
    CollectionReference mCollection;
    private ListenerRegistration mListener;


    public GamesProvider() {
        mCollection = FirebaseFirestore.getInstance().collection("Games");
    }

    public void create(Games game){
        mCollection.document().set(game);

    }

    public void startListening() {
        mListener = mCollection.addSnapshotListener((snapshot, error) -> {
            if (error != null) {
                // Manejar el error de snapshot
                return;
            }

            for (DocumentChange dc : snapshot.getDocumentChanges()) {
                DocumentSnapshot document = dc.getDocument();
                String title = document.getString("title");

                switch (dc.getType()) {
                    case ADDED:
                        // Se agregó un nuevo juego
                        Games game = new Games(title);
                        // Llamar a un método en tu clase que gestione el nuevo juego creado
                        handleNewGame(game);
                        break;
                    case MODIFIED:
                        // Se modificó un juego existente (opcional)
                        // Actualizar los datos si es necesario
                        break;
                    case REMOVED:
                        // Se eliminó un juego existente (opcional)
                        // Actualizar los datos si es necesario
                        break;
                }
            }

            // Notificar a la interfaz de usuario sobre los cambios en los títulos
            // (por ejemplo, a través de un EventBus, callback, etc.)
        });
    }

    public void stopListening() {
        if (mListener != null) {
            mListener.remove();
            mListener = null;
        }
    }

    private void handleNewGame(Games game) {
        // Implementa la lógica para manejar el nuevo juego creado
        // Por ejemplo, puedes enviar una notificación, mostrarlo en la interfaz de usuario, etc.
        // Aquí solo se imprime por simplicidad
        System.out.println("Nuevo juego: " + game.getTitle());
    }
    public List<String> getGameTitles(Context context) {
        String selectTitlePrompt = context.getString(R.string.select_title_prompt);
        List<String> titles = new ArrayList<>();
        // Obtener la lista de documentos de la colección "Games" en Firestore
        mCollection.get().addOnSuccessListener(querySnapshot -> {
            for (DocumentSnapshot documentSnapshot : querySnapshot) {
                // Obtener el título del juego de cada documento
                String title = documentSnapshot.getString("title");
                titles.add(title);
            }
            // Ordenar los títulos alfabéticamente, excluyendo "select_title_prompt"
            titles.remove(selectTitlePrompt);
            Collections.sort(titles);
            titles.add(0, selectTitlePrompt);
        }).addOnFailureListener(e -> {
            // Error al obtener la lista de juegos
            // Aquí puedes manejar el error o mostrar un mensaje al usuario
        });
        return titles;
    }






}