package com.jsancre.gameverse.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.jsancre.gameverse.R;
import com.jsancre.gameverse.adapters.CommentAdapter;
import com.jsancre.gameverse.adapters.PostAdapter;
import com.jsancre.gameverse.adapters.SliderAdapter;
import com.jsancre.gameverse.models.Comment;
import com.jsancre.gameverse.models.FCMBody;
import com.jsancre.gameverse.models.FCMResponse;
import com.jsancre.gameverse.models.Post;
import com.jsancre.gameverse.models.SliderItem;
import com.jsancre.gameverse.providers.AuthProvider;
import com.jsancre.gameverse.providers.CommentProvider;
import com.jsancre.gameverse.providers.LikesProvider;
import com.jsancre.gameverse.providers.NotificationProvider;
import com.jsancre.gameverse.providers.PostProvider;
import com.jsancre.gameverse.providers.TokenProvider;
import com.jsancre.gameverse.providers.UsersProvider;
import com.jsancre.gameverse.utils.RelativeTime;
import com.jsancre.gameverse.utils.ViewedMessageHelper;
import com.smarteist.autoimageslider.IndicatorAnimations;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Esta clase gestiona toda la funcionalidad de la pantalla 'PostDetailActivity' que es la pantalla que se muestra al pulsar sobre un 'Post'
 * Nos muestra dicho Post mas al detalle y con toda su información
 */
public class PostDetailActivity extends AppCompatActivity {
    //---------------------
    //     ATRIBUTOS
    //---------------------
    SliderView mSliderView;
    SliderAdapter mSliderAdapter;
    List<SliderItem> mSliderItems = new ArrayList<>();

    PostProvider mPostProvider;
    UsersProvider mUsersProvider;
    CommentProvider mCommentProvider;
    AuthProvider mAuthProvider;
    LikesProvider mLikesProvider;
    NotificationProvider mNotificationProvider;
    TokenProvider mTokenProvider;
    CommentAdapter mAdapter;

    String mExtraPostId;
    TextView mTextViewTitle;
    TextView mTextViewDescription;
    TextView mTextViewUsername;
    TextView mTextViewEmail;
    TextView mTextViewNameCategory;
    TextView mTextViewRelativeTime;
    TextView mTextViewLikes;
    ImageView mImageViewCategory;
    CircleImageView mCircleImageViewProfile;
    Button mButtonShowProfile;
    String mIdUser = "";
    FloatingActionButton mFabComment;
    RecyclerView mRecyclerView;
    Toolbar mToolBar;
    ListenerRegistration mListener;

    //---------------------
    //     CONSTRUCTOR
    //---------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        mSliderView = findViewById(R.id.imageSlider);
        mTextViewTitle = findViewById(R.id.textViewTitle);
        mTextViewDescription = findViewById(R.id.textViewDescription);
        mTextViewUsername = findViewById(R.id.textViewUsername);
        mTextViewEmail = findViewById(R.id.textViewEmail);
        mTextViewNameCategory = findViewById(R.id.textViewNameCategory);
        mTextViewRelativeTime = findViewById(R.id.textViewRelativeTime);
        mTextViewLikes = findViewById(R.id.textViewLikes);
        mImageViewCategory = findViewById(R.id.imageViewCategory);
        mCircleImageViewProfile = findViewById(R.id.circleImageProfile);
        mButtonShowProfile = findViewById(R.id.btnShowProfile);
        mFabComment = findViewById(R.id.fabComment);
        mRecyclerView = findViewById(R.id.recyclerViewComments);
        mToolBar = findViewById(R.id.toolbar);

        // Establecemos el toolbar de la pantalla de vista del 'Post Detail Activity',
        // este toolbar gestiona la flecha para volver al 'Home Activity'
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Este linear layout nos mostrara los comentarios del post de forma vertical uno debajo de otro...
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(PostDetailActivity.this);
        mRecyclerView.setLayoutManager(linearLayoutManager);


        mPostProvider = new PostProvider();
        mUsersProvider = new UsersProvider();
        mCommentProvider = new CommentProvider();
        mAuthProvider = new AuthProvider();
        mLikesProvider = new LikesProvider();
        mNotificationProvider = new NotificationProvider();
        mTokenProvider = new TokenProvider();

        mExtraPostId = getIntent().getStringExtra("id");

        mFabComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogComment();
            }
        });



        mButtonShowProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToShowProfile();
            }
        });

        getPost();
        getNumberLikes();
    }

    //---------------------
    //     MÉTODOS
    //---------------------
    //Metodo que sobrescribe el 'onStart' de android para cargar todos los comentarios de un post.
    @Override
    protected void onStart() {
        super.onStart();
        Query query = mCommentProvider.getCommentsByPost(mExtraPostId);
        FirestoreRecyclerOptions<Comment> options = new FirestoreRecyclerOptions.Builder<Comment>().setQuery(query, Comment.class).build();
        mAdapter = new CommentAdapter(options,PostDetailActivity.this, mExtraPostId);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.startListening();
        ViewedMessageHelper.updateOnline(true, PostDetailActivity.this);

    }

    //Este metodo se ejecuta cada vez que se cambia de activity o se cierra la aplicacion
    @Override
    protected void onPause() {
        super.onPause();
        ViewedMessageHelper.updateOnline(false, PostDetailActivity.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ViewedMessageHelper.updateOnline(true, PostDetailActivity.this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mListener != null){
            mListener.remove();
        }
        ViewedMessageHelper.updateOnline(false, PostDetailActivity.this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAdapter.stopListening();
        ViewedMessageHelper.updateOnline(false, PostDetailActivity.this);

    }

    //Metodo para la ventana modal que se abrira para introducir los comentarios a un post
    private void showDialogComment() {
        AlertDialog.Builder alert = new AlertDialog.Builder(PostDetailActivity.this);
        alert.setTitle("¡DEJA TU COMENTARIO!");
        alert.setMessage("Escribe una reseña personal");

        EditText editText = new EditText(PostDetailActivity.this);
        editText.setHint("Texto");


        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        params.setMargins(36,0,36,36);
        editText.setLayoutParams(params);

        RelativeLayout container = new RelativeLayout(PostDetailActivity.this);
        RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );

        container.setLayoutParams(relativeParams);
        container.addView(editText);

        alert.setView(container);

        //Esta funcion es para cuando se pulse el 'OK' llame al metodo 'createComment' que creara el comentario en dicho post
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String value = editText.getText().toString();
                if (!value.isEmpty()){
                    createComment(value);
                } else {
                    Toast.makeText(PostDetailActivity.this, "Debe ingresar el comentario", Toast.LENGTH_SHORT).show();
                }
            }
        });
        alert.setNegativeButton("CANCELAR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //No introducimos nada simplemente para que se cierre la ventana modal.
            }
        });

        alert.show();
    }

    //Con este método creamos el comentario en el post seleccionado
    private void createComment(final String value) {
        // Generar un ID único para el comentario
        String commentId = mCommentProvider.getCollection().document().getId();
        mCommentProvider.getCommentsByPostOrderByRating(mExtraPostId);

        // Crear una lista vacía de likes y dislikes
        ArrayList<String> idLike = new ArrayList<>();
        ArrayList<String> idDislike = new ArrayList<>();

        //le pasamos como parametros los atributos de el usuario que esta haciendo dicho comentario para dejarlos reflejados en la base de datos
        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setComment(value);
        comment.setIdPost(mExtraPostId);
        comment.setIdLike(idLike);
        comment.setIdDislike(idDislike);
        comment.setRating(0);
        comment.setIdUser(mAuthProvider.getUid());
        comment.setTimestamp(new Date().getTime());
        mCommentProvider.create(comment).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    sendNotification(value);

                }
                else {
                    Toast.makeText(PostDetailActivity.this, "No se pudo crear el comentario en la publicacion", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    //Metodo para crear una notificacion de alerta al propietario de un Post cuando le agregen un comentario
    private void sendNotification(final String value) {
        if (mIdUser == null){
            return;
        }

        mTokenProvider.getToken(mIdUser).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){
                    if (documentSnapshot.contains("token")){
                        String token = documentSnapshot.getString("token");
                        Map<String, String> data = new HashMap<>();
                        data.put("title", "NUEVO COMENTARIO" );
                        data.put("body", value);
                        FCMBody body = new FCMBody(token, "high", "4500s", data);
                        mNotificationProvider.sendNotification(body).enqueue(new Callback<FCMResponse>() {
                            @Override
                            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                                if (response.body() != null){
                                    if (response.body().getSuccess() == 1){
                                    }
                                    else {
                                        Toast.makeText(PostDetailActivity.this, "La notificación no se pudo enviar", Toast.LENGTH_SHORT).show();
                                    }
                                }
                                else {
                                    Toast.makeText(PostDetailActivity.this, "La notificación no se pudo enviar", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<FCMResponse> call, Throwable t) {

                            }
                        });
                    }
                }
                else {
                    Toast.makeText(PostDetailActivity.this, "El token de notificaciones de el usuario no existe", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //El método instanceSlider() se utiliza para configurar y activar un componente de deslizador (slider) en la actividad PostDetailActivity.
    //Se encarga de inicializar y configurar un componente de deslizador con sus respectivas animaciones y opciones de configuración
    private void instanceSlider() {
        mSliderAdapter = new SliderAdapter(PostDetailActivity.this, mSliderItems);
        mSliderView.setSliderAdapter(mSliderAdapter);
        mSliderView.setIndicatorAnimation(IndicatorAnimations.THIN_WORM);
        mSliderView.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
        mSliderView.setAutoCycleDirection(SliderView.AUTO_CYCLE_DIRECTION_RIGHT);
        mSliderView.setIndicatorSelectedColor(Color.WHITE);
        mSliderView.setIndicatorUnselectedColor(Color.GRAY);
        mSliderView.setScrollTimeInSec(3);
        mSliderView.setAutoCycle(true);
        mSliderView.startAutoCycle();
    }

    //Metodo para el boton de 'VER PERFIL' que abrira dicha pantalla de el usuario que ha creado ese Post
    private void goToShowProfile() {
        if (!mIdUser.equals("")){
            Intent intent = new Intent(PostDetailActivity.this, UserProfileActivity.class);
            intent.putExtra("idUser", mIdUser);
            startActivity(intent);
        }
        else{
            Toast.makeText(this, "Hubo un error cargando el usuario.", Toast.LENGTH_SHORT).show();
        }
    }

    // Método paa contar los likes y mostrarlos en la vista de 'Post Detail Activity'
    // Para hacerlo llamamos al metodo de la clase 'Like Provider' que gestiona las consultas a la base de datos
    // y obtenemos los likes totales de la publicación, una vez obtenidos filtramos la salida de datos para mostrarlos.
    private void getNumberLikes() {
        mListener = mLikesProvider.getLikesByPost(mExtraPostId).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (value != null){
                    int numberLikes = value.size();
                    if (numberLikes == 1){
                        mTextViewLikes.setText(numberLikes + " Me gusta");
                    }
                    else {
                        mTextViewLikes.setText(numberLikes + " Me gustas");
                    }
                }
            }
        });
    }


    //El método getPost() se utiliza para obtener los detalles de una publicación (post) específica y mostrar esos detalles
    //en la interfaz de usuario de la actividad actual.
    private void getPost() {
        mPostProvider.getPostById(mExtraPostId).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {

                    if (documentSnapshot.contains("image1")) {
                        String image1 = documentSnapshot.getString("image1");
                        SliderItem item = new SliderItem();
                        item.setImageUrl(image1);
                        mSliderItems.add(item);
                    }
                    if (documentSnapshot.contains("image2")) {
                        String image2 = documentSnapshot.getString("image2");
                        SliderItem item = new SliderItem();
                        item.setImageUrl(image2);
                        mSliderItems.add(item);
                    }
                    if (documentSnapshot.contains("title")){
                        String title = documentSnapshot.getString("title");
                        mTextViewTitle.setText(title.toUpperCase());
                    }
                    if (documentSnapshot.contains("description")){
                        String description = documentSnapshot.getString("description");
                        mTextViewDescription.setText(description);
                    }
                    if (documentSnapshot.contains("category")){
                        String category = documentSnapshot.getString("category");
                        mTextViewNameCategory.setText(category);

                        if (category.equals("PS4")){
                            mImageViewCategory.setImageResource(R.drawable.icon_ps4_rs);
                        }
                        else if (category.equals("XBOX")){
                            mImageViewCategory.setImageResource(R.drawable.icon_xbox_rs);
                        }
                        else if (category.equals("PC")){
                            mImageViewCategory.setImageResource(R.drawable.icon_pc_rs);
                        }
                        else if (category.equals("NINTENDO")){
                            mImageViewCategory.setImageResource(R.drawable.icon_nintendo_rs);
                        }
                    }
                    if (documentSnapshot.contains("idUser")){
                        mIdUser = documentSnapshot.getString("idUser");
                        getUserInfo(mIdUser);
                    }
                    if (documentSnapshot.contains("timestamp")){
                        Long timestamp = documentSnapshot.getLong("timestamp");
                        String relativeTime = RelativeTime.getTimeAgo(timestamp, PostDetailActivity.this);
                        mTextViewRelativeTime.setText(relativeTime);
                    }

                    instanceSlider();
                }
            }
        });
    }

    //Este metodo se utiliza para obtener la información del usuario correspondiente de la subida del post
    // y mostrar esa información en la interfaz de usuario de la actividad actual.
    private void getUserInfo(String idUser) {
        mUsersProvider.getUser(idUser).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){
                    if (documentSnapshot.contains("username")) {
                        String username = documentSnapshot.getString("username");
                        mTextViewUsername.setText(username);
                    }
                    if (documentSnapshot.contains("email")) {
                        String email = documentSnapshot.getString("email");
                        mTextViewEmail.setText(email);
                    }
                    if (documentSnapshot.contains("image_profile")) {
                        String image_profile = documentSnapshot.getString("image_profile");
                        Picasso.with(PostDetailActivity.this).load(image_profile).into(mCircleImageViewProfile);
                    }
                }
            }
        });

    }


}



