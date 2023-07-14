package com.jsancre.gameverse.providers;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.jsancre.gameverse.models.Token;
import com.jsancre.gameverse.services.MyInstanceIdResult;

public class TokenProvider {

    CollectionReference mCollection;

    public TokenProvider(){
        mCollection = FirebaseFirestore.getInstance().collection("Tokens");
    }

    public void create(String idUser){
        if (idUser == null){
            return;
        }
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()) {
                    return;
                }
                String tokenStr = task.getResult();
                MyInstanceIdResult instanceIdResult = new MyInstanceIdResult(tokenStr);

                Token token = new Token(instanceIdResult.getToken());
                mCollection.document(idUser).set(token);
            }
        });
    }

    public Task<DocumentSnapshot> getToken(String idUser) {
        return  mCollection.document(idUser).get();
    }

}
