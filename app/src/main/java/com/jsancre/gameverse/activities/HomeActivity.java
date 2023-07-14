package com.jsancre.gameverse.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.jsancre.gameverse.R;
import com.jsancre.gameverse.fragments.ChatFragment;
import com.jsancre.gameverse.fragments.FilterFragment;
import com.jsancre.gameverse.fragments.HomeFragment;
import com.jsancre.gameverse.fragments.ProfileFragment;
import com.jsancre.gameverse.providers.AuthProvider;
import com.jsancre.gameverse.providers.TokenProvider;
import com.jsancre.gameverse.providers.UsersProvider;
import com.jsancre.gameverse.utils.ViewedMessageHelper;

/**
 * Clase que gestiona los Fragments (La interfaz de la aplicación) y la inicializa al abrir la aplicación
 */
public class HomeActivity extends AppCompatActivity {
    //--------------------
    //     ATRIBUTOS
    //--------------------
    BottomNavigationView mBottomNavigation;
    TokenProvider mTokenProvider;
    AuthProvider mAuthProvider;
    UsersProvider mUserProvider;
    String mExtraIdUser;

    //--------------------
    //    CONSTRUCTOR
    //--------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mBottomNavigation = findViewById(R.id.bottom_navigation);
        mBottomNavigation.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

        mTokenProvider = new TokenProvider();
        mAuthProvider = new AuthProvider();
        mUserProvider = new UsersProvider();
        getIntent().getStringExtra("userId");

        openFragment(new HomeFragment());
        createToken();
    }

    //--------------------
    //      MÉTODOS
    //--------------------
    @Override
    public void onBackPressed() {
        if (isTaskRoot()) {
            // Si la actividad actual es la raíz de la tarea (no hay actividad anterior)
            // Cerrar la aplicación
            finishAffinity();
        } else {
            // Si hay una actividad anterior en la pila de actividades
            // Volver a la actividad anterior
            super.onBackPressed();
        }
    }

    //Este metodo se ejecuta cada vez que se cambia de activity o se cierra la aplicacion
    @Override
    protected void onPause() {
        super.onPause();
        ViewedMessageHelper.updateOnline(false, HomeActivity.this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        ViewedMessageHelper.updateOnline(false, HomeActivity.this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ViewedMessageHelper.updateOnline(false, HomeActivity.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ViewedMessageHelper.updateOnline(true, HomeActivity.this);
    }

    //y este cuando se inicializa la aplicacion
    @Override
    protected void onStart() {
        super.onStart();
        ViewedMessageHelper.updateOnline(true, HomeActivity.this);
    }

    //Método complementario para gestiona todas las aperturas de los fragments anteriormente mencionados
    public void openFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
    //Aqui llamamos al metodo anterior y llamamos a todos los fragments que deseamos mostrar
    //Los fragments son las 4 pantallas con las que va a interactuar el usuario principalmentem estas pantallas tienen dentro otras subpantallas con otras interfaces, pero esta es la general
    BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    if (item.getItemId() == R.id.itemHome) {
                        // FRAGMENT HOME
                        openFragment(new HomeFragment());
                    }
                    else if (item.getItemId() == R.id.itemFilter) {
                        //FRAGMENT FILTER
                        openFragment(new FilterFragment());
                    }
                    else if (item.getItemId() == R.id.itemChat) {
                        //FRAGMENT CHAT
                        openFragment(new ChatFragment());
                    }
                    else if (item.getItemId() == R.id.itemProfile) {
                        //FRAGMENT PROFILE
                        openFragment(new ProfileFragment());
                    }

                    return true;
                }
            };

    private void createToken(){
        mTokenProvider.create(mAuthProvider.getUid());
    }


}