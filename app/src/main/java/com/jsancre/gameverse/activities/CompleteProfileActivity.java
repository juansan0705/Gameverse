package com.jsancre.gameverse.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jsancre.gameverse.R;
import com.jsancre.gameverse.models.User;
import com.jsancre.gameverse.providers.AuthProvider;
import com.jsancre.gameverse.providers.UsersProvider;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;

/**
 * Clase necesaria para la funcionalidad de inicio de sesion con google, este inicio de sesión no completa el nombre de usuario,
 * por lo tanto es necesario que el usuario lo complete
 */
public class CompleteProfileActivity extends AppCompatActivity {

    //--------------------
    //     ATRIBUTOS
    //--------------------
    TextInputEditText mTextInputUsername;
    TextInputEditText mTextInputPhone;
    Button mButtonComfirm;
    AuthProvider mAuthProvider;
    UsersProvider mUserProvider;
    AlertDialog mDialog;

    //--------------------
    //    CONSTRUCTOR
    //--------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_profile);

        mTextInputUsername = findViewById(R.id.textInputUsername);
        mTextInputPhone = findViewById(R.id.textInputPhone);
        mButtonComfirm = findViewById(R.id.btnConfirm);

        mAuthProvider = new AuthProvider();
        mUserProvider = new UsersProvider();
        mDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Espere un momento")
                .setCancelable(false).build();

        mButtonComfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });

    }

    //--------------------
    //      MÉTODOS
    //--------------------
    //Gestionamos la pantalla de completar informacion con este metodo
    private void register(){
        String username = mTextInputUsername.getText().toString();
        String phone = mTextInputPhone.getText().toString();

        if (!username.isEmpty()){
            updateUser(username, phone);
        }
        else {
            Toast.makeText(this, "Debes insertar un nombre de usuario", Toast.LENGTH_SHORT).show();
        }
    }

    //Aqui llamamos a un metodo de la clase 'UserProvider' que gestiona todas las consultas de este modelo en la base de datos,
    // y llamamos al metodo update para actualizar el nombre de usuario que le pedimos al nuevo usuario
    private void updateUser(final String username, final String phone){
        String id = mAuthProvider.getUid();
        User user = new User();
        user.setUsername(username);
        user.setId(id);
        user.setPhone(phone);
        user.setTimestamp(new Date().getTime());

        mDialog.show();
        mUserProvider.update(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                mDialog.dismiss();
                if (task.isSuccessful()){
                    Intent intent = new Intent(CompleteProfileActivity.this, HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                else{
                    Toast.makeText(CompleteProfileActivity.this, "No se pudo actualizar los datos", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

}