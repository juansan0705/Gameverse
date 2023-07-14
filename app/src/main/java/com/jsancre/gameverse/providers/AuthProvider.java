package com.jsancre.gameverse.providers;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

/**
 * Las clases 'Provider' las utilizo para la gestión de información con la base de datos de FireStore
 */
public class AuthProvider {

    //--------------------
    //     ATRIBUTOS
    //--------------------
    private FirebaseAuth mAuth;

    //--------------------
    //    CONSTRUCTOR
    //--------------------
    public AuthProvider(){
        mAuth = FirebaseAuth.getInstance();
    }

    //--------------------
    //      MÉTODOS
    //--------------------
    // Metodo que usa una clase de Firebase para registrar a un usuario
    public Task<AuthResult> register(String email, String password){
        return mAuth.createUserWithEmailAndPassword(email, password);
    }

    // Metodo que usa una clase de Firebase para hacer login a un usuario, esta clase se encarga de comprobarlo en la base de datos
    public Task<AuthResult> login(String email, String password){
        return mAuth.signInWithEmailAndPassword(email,password);
    }

    // // Metodo que usa una clase de Firebase para hacer login a un usuario con google
    public Task<AuthResult> googleLogin(GoogleSignInAccount googleSignInAccount){
        AuthCredential credential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);
        return mAuth.signInWithCredential(credential);
    }

    // metodo que te devuelve el email de el usuario actual
    public String getEmail(){
        if (mAuth.getCurrentUser() != null) {
            return mAuth.getCurrentUser().getEmail();
        }
        else{
            return null;
        }
    }

    // metodo que te devuelve el id de el usuario actual
    public String getUid(){
        if (mAuth.getCurrentUser() != null) {
            return mAuth.getCurrentUser().getUid();
        }
        else{
            return null;
        }
    }

    //método que te devuelve la sesión de el usuario actual
    public FirebaseUser getUserSession(){
        if (mAuth.getCurrentUser() != null) {
            return mAuth.getCurrentUser();
        }
        else{
            return null;
        }
    }

    // metodo para cerrar sesión
    public void logout() {
        if (mAuth != null) {
            mAuth.signOut();
        }
    }

}
