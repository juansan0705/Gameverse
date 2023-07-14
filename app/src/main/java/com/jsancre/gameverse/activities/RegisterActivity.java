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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jsancre.gameverse.R;
import com.jsancre.gameverse.models.User;
import com.jsancre.gameverse.providers.AuthProvider;
import com.jsancre.gameverse.providers.UsersProvider;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;

/**
 * Clase que gestiona el registro de usuarios en la plataforma
 */
public class RegisterActivity extends AppCompatActivity {
    //--------------------
    //     ATRIBUTOS
    //--------------------
    CircleImageView mCircleImageViewBack;
    TextInputEditText mTextInputUsername;
    TextInputEditText mTextInputEmail;
    TextInputEditText mTextInputPhone;
    TextInputEditText mTextInputPassword;
    TextInputEditText mTextInputRepeatPassword;
    Button mButtonRegister;
    AuthProvider mAuthProvider;
    UsersProvider mUsersProvider;
    AlertDialog mDialog;

    //--------------------
    //    CONSTRUCTOR
    //--------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mCircleImageViewBack = findViewById(R.id.circleImageBack);
        mTextInputUsername = findViewById(R.id.textInputUsername);
        mTextInputEmail = findViewById(R.id.textInputEmail);
        mTextInputPhone = findViewById(R.id.textInputPhone);
        mTextInputPassword = findViewById(R.id.textInputPassword);
        mTextInputRepeatPassword = findViewById(R.id.textInputRepeatPassword);
        mButtonRegister = findViewById(R.id.btnRegister);

        mAuthProvider = new AuthProvider();
        mUsersProvider = new UsersProvider();
        mDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Espere un momento")
                .setCancelable(false).build();

        mButtonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });

        mCircleImageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    //--------------------
    //      MÉTODOS
    //--------------------
    //Este método recoge toda la información de los textView de la pantala de registros y los almacena en variables para poder trabajar con ellas
    private void register(){
        String username = mTextInputUsername.getText().toString();
        String email = mTextInputEmail.getText().toString();
        String phone = mTextInputPhone.getText().toString();
        String password = mTextInputPassword.getText().toString();
        String repeatPassword = mTextInputRepeatPassword.getText().toString();

        //También gestiona las validaciones de estos campos para que el usuario no se olvide de rellenar correctamente toda la información
        if (!username.isEmpty() && !email.isEmpty() && !password.isEmpty() && !repeatPassword.isEmpty() && !phone.isEmpty()){
            if (isEmailValid(email)){
                if (password.equals(repeatPassword)){
                    if (password.length() >= 6) {
                        createUser(username, email, password, phone);
                    }
                    else {
                        Toast.makeText(this, "La contraseña debe tener al menos 6 carácteres", Toast.LENGTH_LONG).show();
                    }
                }
                else {
                    Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_LONG).show();
                }
            }
            else {
                Toast.makeText(this, "Has insertado todos los campos pero el correo no es válido", Toast.LENGTH_LONG).show();
            }
        }
        else {
            Toast.makeText(this, "Debes rellenar todos los campos", Toast.LENGTH_SHORT).show();
        }
    }

    //Con este metodo gestionamos dicho registro en la base de datos, la consulta es realizada en la clase 'AuthProvider'
    // dicha clase contiene todas las consultas relacionadas con el usuario
    private void createUser(final String username, final String email, final String password, final String phone){
        mDialog.show();
        mAuthProvider.register(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    //El id guarda el numero identificador de el usuario que se registra en la plataforma
                    String id = mAuthProvider.getUid();
                    //La contraseña de el usuario se guarda de forma cifrada en la base de datos de firebase con un servicio que tienen de cifrado especial.
                    User user = new User();
                    user.setId(id);
                    user.setEmail(email);
                    user.setUsername(username);
                    user.setPhone(phone);
                    user.setAdminPrivilege(false);
                    user.setTimestamp(new Date().getTime());

                    mUsersProvider.create(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            mDialog.dismiss();
                            if (task.isSuccessful()){
                                Toast.makeText(RegisterActivity.this, "El usuario se ha registrado correctamente", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                                //El "Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK"
                                //Sirve para cuando el usuario pulsa el boton de ir hacia atras, no le lleve al login
                                //sino que borre el registro de pantallas y cierre la aplicación.
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                            else {
                                Toast.makeText(RegisterActivity.this, "No se pudo registrar el usuario", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
                else {
                    mDialog.dismiss();
                    Toast.makeText(RegisterActivity.this, "El usuario introducido ya existe", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    //Con este método validamos que el correo sea válido y este correctamente estructurado
    public boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }


}