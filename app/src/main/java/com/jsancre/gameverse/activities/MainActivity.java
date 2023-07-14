  package com.jsancre.gameverse.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jsancre.gameverse.R;
import com.jsancre.gameverse.models.User;
import com.jsancre.gameverse.providers.AuthProvider;
import com.jsancre.gameverse.providers.UsersProvider;

import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;

  public class MainActivity extends AppCompatActivity {

      //--------------------
      //     ATRIBUTOS
      //--------------------
      TextView mTextViewRegister;
      TextInputEditText mTextInputEmail;
      TextInputEditText mTextInputPassword;
      Button mButtonLogin;
      AlertDialog mDialog;

      //INICIAR SESIÓN CON GOOGLE
      private static final String TAG = "MainActivity";
      private static final int REQUEST_CODE_GOOGLE = 9001;
      private GoogleSignInClient mGoogleSignInClient;
      AuthProvider mAuthProvider;
      SignInButton mButtonGoogle;
      UsersProvider mUsersProvider;

      //--------------------
      //     CONSTRUCTOR
      //--------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextViewRegister = findViewById(R.id.textViewRegister);
        mTextInputEmail = findViewById(R.id.TextInputEmail);
        mTextInputPassword = findViewById(R.id.TextInputPassword);
        mButtonLogin = findViewById(R.id.btnLogin);
        mButtonGoogle = findViewById(R.id.btnLoginGoogle);

        mAuthProvider = new AuthProvider();
        mDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Espere un momento")
                .setCancelable(false).build();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mUsersProvider = new UsersProvider();

        mButtonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        mButtonGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        mTextViewRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

    }

      //--------------------
      //      MÉTODOS
      //--------------------
      @Override
      public void onActivityResult(int requestCode, int resultCode, Intent data) {
          super.onActivityResult(requestCode, resultCode, data);

          if (requestCode == REQUEST_CODE_GOOGLE) {
              Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
              try {
                  GoogleSignInAccount account = task.getResult(ApiException.class);
                  firebaseAuthWithGoogle(account);
              } catch (ApiException e) {
                  Log.w("ERROR: ", "¡Iniciar sesión con Google fallo!", e);
              }
          }
      }



      //Metodo que integra los metodos que comprueban si el usuario existe (Para registrarlo en la base de datos o no)
      //y el metodo para iniciar sesion con google
      private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        mDialog.show();
        mAuthProvider.googleLogin(acct).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                      @Override
                      public void onComplete(@NonNull Task<AuthResult> task) {
                          if (task.isSuccessful()) {
                              String id = mAuthProvider.getUid();
                              chekUserExist(id);

                          } else {
                              mDialog.dismiss();
                              Log.w("ERROR", "Iniciar sesion con google fallo", task.getException());
                              Toast.makeText(MainActivity.this, "No se pudo iniciar sesión con google", Toast.LENGTH_SHORT).show();
                          }
                      }
                  });
      }

      //Comprobamos si el usuario existe al iniciar sesion con google, si no existe lo registrara.
      private void chekUserExist(final String id) {
         mUsersProvider.getUser(id).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
             @Override
             public void onSuccess(DocumentSnapshot documentSnapshot) {
                 //Si ya existe el usuario se mete dentro de este if y simplemente entra dentro del 'HomeActivity'
                 if (documentSnapshot.exists()){
                     mDialog.dismiss();
                     Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                     intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                     startActivity(intent);
                 }
                 //Sí no existe, registrara el correo de ese usuario en la base de datos de Firebase
                 else {
                     String email = mAuthProvider.getEmail();

                     User user = new User();
                     user.setEmail(email);
                     user.setId(id);

                     mUsersProvider.create(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                         @Override
                         public void onComplete(@NonNull Task<Void> task) {
                             mDialog.dismiss();
                             if (task.isSuccessful()){
                                 Intent intent = new Intent(MainActivity.this, CompleteProfileActivity.class);
                                 startActivity(intent);
                             }
                             //Si tiene algun error al registrar el usuario lanzara este mensaje
                             else {
                                 Toast.makeText(MainActivity.this, "No se puedo registrar al usuario", Toast.LENGTH_SHORT).show();
                             }
                         }
                     });
                 }
             }
         });
      }

      //Para mantener la sesion iniciada comprobamos si habia un registro anteriormente pata no tener que pedirles las credenciales nuevamente al usuario
      @Override
      protected void onStart() {
          super.onStart();
          if (mAuthProvider.getUserSession() != null) {
              Intent intent = new Intent(MainActivity.this, HomeActivity.class);
              intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
              startActivity(intent);
          }
      }

      //Método para iniciar sesion con Google
      private void signIn() {
          Intent signInIntent = mGoogleSignInClient.getSignInIntent();
          startActivityForResult(signInIntent, REQUEST_CODE_GOOGLE);
      }

      //Método que comprueba que las credenciales de el usuario sean correctas.
      private void login(){
          String email = mTextInputEmail.getText().toString();
          String password = mTextInputPassword.getText().toString();
          if (email.isEmpty() || password.isEmpty()) {
              Toast.makeText(MainActivity.this, "Por favor, ingresa tu correo electrónico y contraseña", Toast.LENGTH_LONG).show();
              return;
          }
          mDialog.show();
          mAuthProvider.login(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
              @Override
              public void onComplete(@NonNull Task<AuthResult> task) {
                  mDialog.dismiss();
                  if (task.isSuccessful()){
                      Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                      intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                      startActivity(intent);
                  }
                  else {
                      Toast.makeText(MainActivity.this, "Las credenciales son incorrectas, intentalo de nuevo", Toast.LENGTH_LONG).show();
                  }
              }
          });
    }
}