package com.jsancre.gameverse.activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.UploadTask;
import com.jsancre.gameverse.R;
import com.jsancre.gameverse.models.Games;
import com.jsancre.gameverse.models.Post;
import com.jsancre.gameverse.models.User;
import com.jsancre.gameverse.providers.AuthProvider;
import com.jsancre.gameverse.providers.GamesProvider;
import com.jsancre.gameverse.providers.ImageProvider;
import com.jsancre.gameverse.providers.PostProvider;
import com.jsancre.gameverse.providers.TitleProvider;
import com.jsancre.gameverse.utils.FileUtil;
import com.jsancre.gameverse.utils.ViewedMessageHelper;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;

/**
 * Clase que gestiona toda la funcionalidad de subir Posts
 */
public class PostActivity extends AppCompatActivity {
    //--------------------
    //     ATRIBUTOS
    //--------------------
    ImageView mImageViewPost1;
    ImageView mImageViewPost2;
    File mImageFile;
    File mImageFile2;
    Button mButtonPost;
    ImageProvider mImageProvider;
    PostProvider mPostProvider;
    AuthProvider mAuthProvider;
    GamesProvider mGamesProvider;
    ArrayAdapter<String> autoCompleteAdapter;
    Spinner mTitlePost;
    List<String> titleList; // Lista de títulos para el ComboBox
    TextInputEditText mTextInputDescription;
    ImageView mImageViewPC;
    ImageView mImageViewPS4;
    ImageView mImageViewXBOX;
    ImageView mImageViewNINTENDO;
    CircleImageView mCircleImageBack;
    TextView mTextViewCategory;
    String mCategory = "";
    String mTitle = "";
    String mDescription = "";
    AlertDialog mDialog;
    ImageView adminButton;
    String mExtraIdUser;

    //SUBIR IMAGEN DESDE LA CÁMARA
    AlertDialog.Builder mBuilderSelector;
    CharSequence options[];
    private final int GALLERY_REQUEST_CODE = 1;
    private final int GALLERY_REQUEST_CODE_2 = 2;
    private final int PHOTO_REQUEST_CODE = 3;
    private final int PHOTO_REQUEST_CODE_2 = 4;

    // FOTO 1
    String mAbsolutePhotoPath;
    String mPhotoPath;
    File mPhotoFile;

    // FOTO 2
    String mAbsolutePhotoPath2;
    String mPhotoPath2;
    File mPhotoFile2;

    //--------------------
    //    CONSTRUCTOR
    //--------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mImageProvider = new ImageProvider();
        mPostProvider = new PostProvider();
        mAuthProvider = new AuthProvider();
        mGamesProvider = new GamesProvider();

        mDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Espere un momento")
                .setCancelable(false).build();

        mBuilderSelector = new AlertDialog.Builder(this);
        mBuilderSelector.setTitle("Selecciona una opcion");
        options = new CharSequence[]{"Imagen de galeria", "Abrir cámara"};

        mImageViewPost1 = findViewById(R.id.imageViewPost1);
        mImageViewPost2 = findViewById(R.id.imageViewPost2);
        mButtonPost = findViewById(R.id.btnPost);
        mTitlePost = findViewById(R.id.spinnerTitle);
        mTextInputDescription = findViewById(R.id.textInputDescription);
        mImageViewPC = findViewById(R.id.imageViewPc);
        mImageViewPS4 = findViewById(R.id.imageViewPs4);
        mImageViewXBOX = findViewById(R.id.imageViewXbox);
        mImageViewNINTENDO = findViewById(R.id.imageViewNintendo);
        mTextViewCategory = findViewById(R.id.textViewCategory);
        mCircleImageBack = findViewById(R.id.circleImageBack);
        adminButton = findViewById(R.id.addItemsAdmin);
        mExtraIdUser = getIntent().getStringExtra("userId");

        List<String> titleList = mGamesProvider.getGameTitles(PostActivity.this);
        titleList.add(0, getString(R.string.select_title_prompt));

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, titleList) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0;
            }

            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (position == 0) {
                    // Establecer el texto de indicación en el primer elemento
                    TextView textView = (TextView) view.findViewById(android.R.id.text1);
                    textView.setTextColor(getResources().getColor(R.color.blue));  // Cambia el color del texto de indicación si es necesario
                }
                return view;
            }
        };



        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mTitlePost.setAdapter(adapter);

        adminButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogComment();
            }
        });

        mCircleImageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mButtonPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickPost();
            }
        });

        mImageViewPost1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectOptionImage(1);
            }
        });

        mImageViewPost2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectOptionImage(2);
            }
        });

        mImageViewPC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCategory = "PC";
                mTextViewCategory.setText(mCategory);
            }
        });

        mImageViewPS4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCategory = "PS4";
                mTextViewCategory.setText(mCategory);
            }
        });

        mImageViewXBOX.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCategory = "XBOX";
                mTextViewCategory.setText(mCategory);
            }
        });

        mImageViewNINTENDO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCategory = "NINTENDO";
                mTextViewCategory.setText(mCategory);
            }
        });

    }

    //--------------------
    //      MÉTODOS
    //--------------------
    //Este metodo se ejecuta cada vez que se cambia de activity o se cierra la aplicacion
    @Override
    protected void onPause() {
        super.onPause();
        ViewedMessageHelper.updateOnline(false, PostActivity.this);
    }

    //y este cuando se inicializa la aplicacion
    @Override
    protected void onStart() {
        super.onStart();
        ViewedMessageHelper.updateOnline(true, PostActivity.this);
        mGamesProvider.startListening();
        showAdminButton();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGamesProvider.stopListening();
        ViewedMessageHelper.updateOnline(false, PostActivity.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ViewedMessageHelper.updateOnline(true, PostActivity.this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ViewedMessageHelper.updateOnline(false, PostActivity.this);
    }

    private void showDialogComment() {
        AlertDialog.Builder alert = new AlertDialog.Builder(PostActivity.this);
        alert.setTitle("AÑADIR NUEVO TITULO");
        alert.setMessage("Escribe el nuevo titulo que desea añadir");

        EditText editText = new EditText(PostActivity.this);
        editText.setHint("Texto");


        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        params.setMargins(36,0,36,36);
        editText.setLayoutParams(params);

        RelativeLayout container = new RelativeLayout(PostActivity.this);
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
                    Games game = new Games();
                    game.setTitle(value);
                    mGamesProvider.create(game);

                    Toast.makeText(PostActivity.this, "¡Se ha añadido el titulo correctamente!", Toast.LENGTH_SHORT).show();

                    // Actualiza la lista de títulos en el adaptador del Spinner
                    List<String> titleList = mGamesProvider.getGameTitles(PostActivity.this);
                    titleList.add(0, getString(R.string.select_title_prompt));
                    ArrayAdapter<String> adapter = (ArrayAdapter<String>) mTitlePost.getAdapter();
                    adapter.clear();
                    adapter.addAll(titleList);
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(PostActivity.this, "Debe ingresar un titulo", Toast.LENGTH_SHORT).show();
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

    private void showAdminButton() {
        // Obtener el ID del usuario actual de la sesión
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DocumentReference userRef = FirebaseFirestore.getInstance().collection("Users").document(userId);

        userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    Boolean adminPrivilege = documentSnapshot.getBoolean("admin");
                    if (adminPrivilege != null && adminPrivilege) {
                        // El usuario tiene privilegios de administrador
                        adminButton.setVisibility(View.VISIBLE);
                    } else {
                        // El usuario no tiene privilegios de administrador
                        adminButton.setVisibility(View.GONE);
                    }
                } else {
                    // No se encontró el documento del usuario
                    adminButton.setVisibility(View.GONE);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Error al obtener los datos del usuario
                adminButton.setVisibility(View.GONE);
                Toast.makeText(PostActivity.this, "Error al obtener los datos del usuario", Toast.LENGTH_SHORT).show();
            }
        });
    }




    private void selectOptionImage(final int numberImage) {
        mBuilderSelector.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                if (i == 0){
                    if (numberImage == 1) {
                        openGallery(GALLERY_REQUEST_CODE);
                    }
                    else if (numberImage == 2){
                        openGallery(GALLERY_REQUEST_CODE_2);
                    }
                }
                else if (i == 1) {
                    if (numberImage == 1) {
                        takePhoto(PHOTO_REQUEST_CODE);
                    }
                    else if (numberImage == 2){
                        takePhoto(PHOTO_REQUEST_CODE_2);
                    }
                }
            }
        });

        mBuilderSelector.show();

    }

    //Metodo para abrir la cámara del teléfono y hacer una foto
    private void takePhoto(int requestCode){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try{
                photoFile = createPhotoFile(requestCode);
            }catch (Exception e) {
                Toast.makeText(this, "Hubo un error abriendo la cámara: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            if (photoFile != null){
                Uri photoUri = FileProvider.getUriForFile(PostActivity.this, "com.jsancre.gameverse", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, requestCode);
            }
        }
    }

    //metodo que gestiona los id de las fotos realizadas con la cámara
    private File createPhotoFile(int requestCode) throws IOException {
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File photoFile = File.createTempFile(
                new Date() + "_photo",
                ".jpg",
                storageDir
        );
        if (requestCode == PHOTO_REQUEST_CODE){
            mPhotoPath = "file:" + photoFile.getAbsolutePath();
            mAbsolutePhotoPath = photoFile.getAbsolutePath();
        }
        else if (requestCode == PHOTO_REQUEST_CODE_2){
            mPhotoPath2 = "file:" + photoFile.getAbsolutePath();
            mAbsolutePhotoPath2 = photoFile.getAbsolutePath();
        }
        return photoFile;
    }

    // metodo que realiza todas las validaciones a la hora de subir las fotos del post
    private void clickPost() {
        mTitle = mTitlePost.getSelectedItem().toString();
        mDescription = mTextInputDescription.getText().toString();
        //AQUI COMPROBAMOS SI EL USUARIO ESTA SUBIENDO LA FOTO DESDE GALERIA O CAMARA
        if (!mTitle.isEmpty() && !mDescription.isEmpty() && !mCategory.isEmpty()){
            //SELECCIONO AMBAS IMAGENES DESDE GALERIA
            if (mImageFile != null && mImageFile2 != null){
                saveImage(mImageFile, mImageFile2);
            }
            //SELECCIONO AMBAS IMAGENES DESDE LA CÁMARA
            else if (mPhotoFile != null && mPhotoFile2 != null){
                saveImage(mPhotoFile, mPhotoFile2);
            }
            //SELECCIONO LA PRIMERA IMAGEN DESDE GALERIA Y LA SEGUNDA DESDE CÁMARA
            else if (mImageFile != null && mPhotoFile2 != null){
                saveImage(mImageFile, mPhotoFile2);
            }
            //SELECCIONO LA PRIMERA IMAGEN DESD CÁMARA Y LA SEGUNDA DESDE GALERIA
            else if (mPhotoFile != null && mImageFile2 != null){
                saveImage(mPhotoFile, mImageFile2);
            }
            else if (mPhotoFile != null){
                saveOneImage(mPhotoFile);
            }
            else if (mImageFile != null){
                saveOneImage(mImageFile);
            }
            else {
                Toast.makeText(this, "Debes seleccionar dos imagenes", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(this, "Completa todos los campos para publicar", Toast.LENGTH_SHORT).show();
        }
    }

    //Metodo que crea el post y almacena los datos
    private void saveImage(File imageFile1, File imageFile2) {
        mDialog.show();
        mPostProvider.getPostByTitle(mTitle.toLowerCase()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult() != null && !task.getResult().isEmpty()) {
                        // El título ya existe en una publicación existente
                        mDialog.dismiss();
                        Toast.makeText(PostActivity.this, "El título ya está en uso. Por favor, elija otro título.", Toast.LENGTH_SHORT).show();
                    } else {
                        // El título no existe, guardar la información de la publicación
                        mImageProvider.save(PostActivity.this, imageFile1).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> taskImage1) {
                                if (taskImage1.isSuccessful()) {
                                    mImageProvider.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri1) {
                                            final String url1 = uri1.toString();

                                            mImageProvider.save(PostActivity.this, imageFile2).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> taskImage2) {
                                                    if (taskImage2.isSuccessful()) {
                                                        mImageProvider.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                            @Override
                                                            public void onSuccess(Uri uri2) {
                                                                String url2 = uri2.toString();
                                                                Post post = new Post();
                                                                post.setImage1(url1);
                                                                post.setImage2(url2);
                                                                post.setTitle(mTitle.toLowerCase());
                                                                post.setDescription(mDescription);
                                                                post.setCategory(mCategory);
                                                                post.setIdUser(mAuthProvider.getUid());
                                                                post.setTimestamp(new Date().getTime());
                                                                mPostProvider.save(post).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> taskSave) {
                                                                        mDialog.dismiss();
                                                                        if (taskSave.isSuccessful()){
                                                                            Intent intent = new Intent(PostActivity.this, HomeActivity.class);
                                                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                            clearForm();
                                                                            Toast.makeText(PostActivity.this, "El post se ha subido correctamente!", Toast.LENGTH_SHORT).show();
                                                                            startActivity(intent);
                                                                        }else {
                                                                            Toast.makeText(PostActivity.this, "No se pudo almacenar la información", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    }
                                                                });
                                                            }
                                                        });
                                                    } else {
                                                        mDialog.dismiss();
                                                        Toast.makeText(PostActivity.this, "La imagen número 2 no se pudo guardar", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        }
                                    });
                                } else {
                                    mDialog.dismiss();
                                    Toast.makeText(PostActivity.this, "La imagen número 1 no se pudo guardar", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                } else {
                    // Error al consultar la base de datos
                    mDialog.dismiss();
                    Toast.makeText(PostActivity.this, "Hubo un error al consultar la base de datos", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveOneImage(File imageFile) {
        mDialog.show();
        mPostProvider.getPostByTitle(mTitle.toLowerCase()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult() != null && !task.getResult().isEmpty()) {
                        // El título ya existe en una publicación existente
                        mDialog.dismiss();
                        Toast.makeText(PostActivity.this, "El título ya está en uso. Por favor, elija otro título.", Toast.LENGTH_SHORT).show();
                    } else {
                        // El título no existe, guardar la información de la publicación
                        mImageProvider.save(PostActivity.this, imageFile).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> taskImage) {
                                if (taskImage.isSuccessful()) {
                                    mImageProvider.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            final String url = uri.toString();
                                            Post post = new Post();
                                            post.setImage1(url);
                                            post.setImage2(url);
                                            post.setTitle(mTitle.toLowerCase());
                                            post.setDescription(mDescription);
                                            post.setCategory(mCategory);
                                            post.setIdUser(mAuthProvider.getUid());
                                            post.setTimestamp(new Date().getTime());
                                            mPostProvider.save(post).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> taskSave) {
                                                    mDialog.dismiss();
                                                    if (taskSave.isSuccessful()) {
                                                        Intent intent = new Intent(PostActivity.this, HomeActivity.class);
                                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                        clearForm();
                                                        Toast.makeText(PostActivity.this, "El post se ha subido correctamente!", Toast.LENGTH_SHORT).show();
                                                        startActivity(intent);
                                                    } else {
                                                        Toast.makeText(PostActivity.this, "No se pudo almacenar la información", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        }
                                    });
                                } else {
                                    mDialog.dismiss();
                                    Toast.makeText(PostActivity.this, "La imagen no se pudo guardar", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                } else {
                    // Error al consultar la base de datos
                    mDialog.dismiss();
                    Toast.makeText(PostActivity.this, "Hubo un error al consultar la base de datos", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    //Método para limpiar todos los datos una vez el post se haya subido
    private void clearForm() {
        mTitlePost.setSelection(0);
        mTextInputDescription.setText("");
        mTextViewCategory.setText("CATEGORIAS");
        mImageViewPost1.setImageResource(R.drawable.upload_image_new);
        mImageViewPost2.setImageResource(R.drawable.upload_image_new);
        mTitle = "";
        mDescription = "";
        mCategory = "";
        mImageFile = null;
        mImageFile2= null;
    }

    //Metodo que sustitye a 'StartActivityForResult' que esta deprecated...
    ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK){
                        try {
                            //Hace una conversion en la imagen seleccionada para el post y la establece en la pantalla
                            mImageFile = FileUtil.from(PostActivity.this, result.getData().getData());
                            mImageViewPost1.setImageBitmap(BitmapFactory.decodeFile(mImageFile.getAbsolutePath()));
                        }
                        catch(Exception ex){
                            Log.d("ERROR", "Se produjo un error: " + ex.getMessage());
                            Toast.makeText(PostActivity.this, "Se produjo un error " + ex.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
    );

    //metodo para abrir la galeria del telefono
    private void openGallery(int requestCode) {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, requestCode);
        //galleryLauncher.launch(galleryIntent);
    }

    //metodo que gestiona y comprime las imagenes selccionadas pasandolas a traves de la clase 'CompressorBitmapImage' y 'FileUtil'
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /**
         * SELECCIÓN DE IMAGEN CON GALERÍA
         */
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK) {
            try {
                mPhotoFile = null;
                mImageFile = FileUtil.from(this, data.getData());
                mImageViewPost1.setImageBitmap(BitmapFactory.decodeFile(mImageFile.getAbsolutePath()));
            } catch (Exception e) {
                Log.d("ERROR", "Se produjo un error" + e.getMessage());
                Toast.makeText(this, "Se produjo un error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        if (requestCode == GALLERY_REQUEST_CODE_2 && resultCode == RESULT_OK) {
            try {
                mPhotoFile2 = null;
                mImageFile2 = FileUtil.from(this, data.getData());
                mImageViewPost2.setImageBitmap(BitmapFactory.decodeFile(mImageFile2.getAbsolutePath()));
            } catch (Exception e) {
                Log.d("ERROR", "Se produjo un error" + e.getMessage());
                Toast.makeText(this, "Se produjo un error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        /**
         * SELECCIÓN DE IMAGEN CON CÁMARA
         */
        if (requestCode == PHOTO_REQUEST_CODE && resultCode == RESULT_OK){
            mImageFile = null;
            mPhotoFile = new File(mAbsolutePhotoPath);
            Picasso.with(PostActivity.this).load(mPhotoPath).into(mImageViewPost1);
        }

        if (requestCode == PHOTO_REQUEST_CODE_2 && resultCode == RESULT_OK){
            mImageFile2 = null;
            mPhotoFile2 = new File(mAbsolutePhotoPath2);
            Picasso.with(PostActivity.this).load(mPhotoPath2).into(mImageViewPost2);
        }

    }
}