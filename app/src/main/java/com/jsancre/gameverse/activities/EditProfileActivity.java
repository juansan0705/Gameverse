package com.jsancre.gameverse.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.UploadTask;
import com.jsancre.gameverse.R;
import com.jsancre.gameverse.models.User;
import com.jsancre.gameverse.providers.AuthProvider;
import com.jsancre.gameverse.providers.ImageProvider;
import com.jsancre.gameverse.providers.UsersProvider;
import com.jsancre.gameverse.utils.FileUtil;
import com.jsancre.gameverse.utils.ViewedMessageHelper;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;

/**
 * Clase para modificar el Perfil de el usuario
 */
public class EditProfileActivity extends AppCompatActivity {
    //---------------------
    //     ATRIBUTOS
    //---------------------

    CircleImageView mCircleImageViewBack;
    CircleImageView mCircleImageViewProfile;
    ImageView mImageViewCover;
    TextInputEditText mTextInputUsername;
    TextInputEditText mTextInputPhone;
    Button mButtonEditProfile;

    File mImageFile;
    File mImageFile2;

    //SUBIR IMAGEN DESDE LA CÁMARA
    AlertDialog.Builder mBuilderSelector;
    CharSequence options[];
    private final int GALLERY_REQUEST_CODE_PROFILE = 1;
    private final int GALLERY_REQUEST_CODE_COVER = 2;
    private final int PHOTO_REQUEST_CODE_PROFILE = 3;
    private final int PHOTO_REQUEST_CODE_COVER = 4;

    // FOTO 1
    String mAbsolutePhotoPath;
    String mPhotoPath;
    File mPhotoFile;

    // FOTO 2
    String mAbsolutePhotoPath2;
    String mPhotoPath2;
    File mPhotoFile2;

    String mUsername = "";
    String mPhone = "";
    String mImageProfile;
    String mImageCover;

    AlertDialog mDialog;

    ImageProvider mImageProvider;
    UsersProvider mUserProvider;
    AuthProvider mAuthProvider;

    //---------------------
    //     CONSTRUCTOR
    //---------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mCircleImageViewBack = findViewById(R.id.circleImageBack);
        mCircleImageViewProfile = findViewById(R.id.circleImageProfile);
        mImageViewCover = findViewById(R.id.imageViewCover);
        mTextInputUsername = findViewById(R.id.textInputUsername);
        mTextInputPhone = findViewById(R.id.textInputPhone);
        mButtonEditProfile = findViewById(R.id.btnEditProfile);

        mImageProvider = new ImageProvider();
        mUserProvider = new UsersProvider();
        mAuthProvider = new AuthProvider();

        mBuilderSelector = new AlertDialog.Builder(this);
        mBuilderSelector.setTitle("Selecciona una opcion");
        options = new CharSequence[]{"Imagen de galeria", "Abrir cámara"};

        mDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Espere un momento")
                .setCancelable(false).build();

        mButtonEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickEditProfile();
            }
        });

        mCircleImageViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectOptionImage(1);
            }
        });

        mImageViewCover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectOptionImage(2);
            }
        });

        mCircleImageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        getUser();
    }

    //---------------------
    //      MÉTODOS
    //---------------------
    //Metodo que recupera toda la información de el usuario que quiere modificar su perfil para poder,
    // las consultas para ellos estan gestionadas en la clase 'User Provider'
    private void getUser(){
        mUserProvider.getUser(mAuthProvider.getUid()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    if (documentSnapshot.contains("username")){
                        mUsername = documentSnapshot.getString("username");
                        mTextInputUsername.setText(mUsername);
                    }
                    if (documentSnapshot.contains("phone")){
                        mPhone = documentSnapshot.getString("phone");
                        mTextInputPhone.setText(mPhone);
                    }
                    if (documentSnapshot.contains("image_profile")){
                        mImageProfile = documentSnapshot.getString("image_profile");
                        if (mImageProfile != null){
                            if (!mImageProfile.isEmpty()){
                                Picasso.with(EditProfileActivity.this).load(mImageProfile).into(mCircleImageViewProfile);
                            }
                        }
                    }
                    if (documentSnapshot.contains("image_cover")){
                        mImageCover = documentSnapshot.getString("image_cover");
                        if (mImageCover != null){
                            if (!mImageCover.isEmpty()){
                                Picasso.with(EditProfileActivity.this).load(mImageCover).into(mImageViewCover);
                            }
                        }
                    }
                }
            }
        });
    }

    //Este metodo es para hacer ciertas validaciones al seleccionar las imagenes de portada y perfil.
    private void clickEditProfile() {
        mUsername = mTextInputUsername.getText().toString();
        mPhone = mTextInputPhone.getText().toString();

        if (!mUsername.isEmpty() && !mPhone.isEmpty()){
            if (mImageFile != null && mImageFile2 != null){
                saveImageCoverAndProfile(mImageFile, mImageFile2);
            }
            //SELECCIONO AMBAS IMAGENES DESDE LA CÁMARA
            else if (mPhotoFile != null && mPhotoFile2 != null){
                saveImageCoverAndProfile(mPhotoFile, mPhotoFile2);
            }
            //SELECCIONO LA PRIMERA IMAGEN DESDE GALERIA Y LA SEGUNDA DESDE CÁMARA
            else if (mImageFile != null && mPhotoFile2 != null){
                saveImageCoverAndProfile(mImageFile, mPhotoFile2);
            }
            //SELECCIONO LA PRIMERA IMAGEN DESDE CÁMARA Y LA SEGUNDA DESDE GALERÍA
            else if (mPhotoFile != null && mImageFile2 != null){
                saveImageCoverAndProfile(mPhotoFile, mImageFile2);
            }
            //MODIFICO LA FOTO DE PERFIL DESDE CÁMARA
            else if (mPhotoFile != null) {
                saveImage(mPhotoFile, true);
            }
            //MODIFICO LA FOTO DE LA PORTADA DESDE CÁMARA
            else if (mPhotoFile2 != null) {
                saveImage(mPhotoFile2, false);
            }
            //MODIFICO LA FOTO DE PERFIL DESDE GALERÍA
            else if (mImageFile != null) {
                saveImage(mImageFile, true);
            }
            //MODIFICO LA FOTO DE LA PORTADA DESDE GALERÍA
            else if (mImageFile2 != null) {
                saveImage(mImageFile2, false);
            }
            //Aqui actualizo la nueva información
            else {
                User user = new User();
                user.setUsername(mUsername);
                user.setPhone(mPhone);
                user.setImage_profile(mImageProfile);
                user.setImage_cover(mImageCover);
                user.setId(mAuthProvider.getUid());
                updateInfo(user);
            }
        }

        else {
            Toast.makeText(this, "Debes ingresar los nuevos cambios para actualizar", Toast.LENGTH_SHORT).show();
        }
    }

    //Con este metodo actualizamos las imagenes de perfil y de la portada en la base de datos, capturando posibles errores
    private void saveImageCoverAndProfile(File imageFile1, File imageFile2) {
        mDialog.show();
        mImageProvider.save(EditProfileActivity.this, imageFile1).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()){
                    mImageProvider.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        //Aqui almacenamos toda la informacion del post en la base de datos
                        @Override
                        public void onSuccess(Uri uri) {
                            final String urlProfile = uri.toString();

                            mImageProvider.save(EditProfileActivity.this, imageFile2).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                //Si no ha habido ningun error se actualizaran todos los nuevos campos en la base de datos
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> taskImage2) {
                                    if (taskImage2.isSuccessful()) {
                                        mImageProvider.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri2) {
                                                String urlCover = uri2.toString();
                                                User user = new User();
                                                user.setUsername(mUsername);
                                                user.setPhone(mPhone);
                                                user.setImage_profile(urlProfile);
                                                user.setImage_cover(urlCover);
                                                user.setId(mAuthProvider.getUid());
                                                updateInfo(user);
                                            }
                                        });
                                    }
                                    //si hubiera un error se avisaría al usuario de ello con un 'Toast'
                                    else {
                                        mDialog.dismiss();
                                        Toast.makeText(EditProfileActivity.this, "La imagen número 2 no se pudo guardar", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    });
                }
                else{
                    mDialog.dismiss();
                    Toast.makeText(EditProfileActivity.this, "Hubo un error al almacenar la imagen", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    //Metodo complementario para cuando solo se quiera modificar una de las dos imagenes
    private void saveImage(File image, boolean isProfileImage){
        mDialog.show();
        mImageProvider.save(EditProfileActivity.this, image).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()){
                    mImageProvider.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        //Aqui almacenamos toda la informacion del post en la base de datos
                        @Override
                        public void onSuccess(Uri uri) {
                            final String url = uri.toString();
                            User user = new User();
                            user.setUsername(mUsername);
                            user.setPhone(mPhone);
                            if (isProfileImage){
                                user.setImage_profile(url);
                                user.setImage_cover(mImageCover);
                            }else {
                                user.setImage_cover(url);
                                user.setImage_profile(mImageProfile);
                            }
                            user.setId(mAuthProvider.getUid());
                            updateInfo(user);
                        }
                    });
                }
                else{
                    mDialog.dismiss();
                    Toast.makeText(EditProfileActivity.this, "Hubo un error al almacenar la imagen", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    //Método complementario para actualizar la información en la base de datos
    private void updateInfo(User user) {
        if (mDialog.isShowing()){
            mDialog.show();
        }
        mUserProvider.update(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                mDialog.dismiss();
                if (task.isSuccessful()){
                    Toast.makeText(EditProfileActivity.this, "La información se actualizó correctamente", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                }
                else {
                    Toast.makeText(EditProfileActivity.this, "Hubo un error... La información no se actualizó", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    //Método que gestiona la selección de imagenes tanto de galería como de la cámara
    private void selectOptionImage(final int numberImage) {
        mBuilderSelector.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                if (i == 0){
                    if (numberImage == 1) {
                        openGallery(GALLERY_REQUEST_CODE_PROFILE);
                    }
                    else if (numberImage == 2){
                        openGallery(GALLERY_REQUEST_CODE_COVER);
                    }
                }
                else if (i == 1) {
                    if (numberImage == 1) {
                        takePhoto(PHOTO_REQUEST_CODE_PROFILE);
                    }
                    else if (numberImage == 2){
                        takePhoto(PHOTO_REQUEST_CODE_COVER);
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
                Uri photoUri = FileProvider.getUriForFile(EditProfileActivity.this, "com.jsancre.gameverse", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, requestCode);
            }
        }
    }

    //Método necesario para poder abrir la camara del teléfono para hacer una foto
    private File createPhotoFile(int requestCode) throws IOException {
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File photoFile = File.createTempFile(
                new Date() + "_photo",
                ".jpg",
                storageDir
        );
        if (requestCode == PHOTO_REQUEST_CODE_PROFILE){
            mPhotoPath = "file:" + photoFile.getAbsolutePath();
            mAbsolutePhotoPath = photoFile.getAbsolutePath();
        }
        else if (requestCode == PHOTO_REQUEST_CODE_COVER){
            mPhotoPath2 = "file:" + photoFile.getAbsolutePath();
            mAbsolutePhotoPath2 = photoFile.getAbsolutePath();
        }
        return photoFile;
    }

    //Método necesario para poder abrir la galeria de el teléfono
    private void openGallery(int requestCode) {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, requestCode);
        //galleryLauncher.launch(galleryIntent);
    }

    //Gestiona la seleccion de imagenes con galería y cámara
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /**
         * SELECCIÓN DE IMAGEN CON GALERÍA
         */
        if (requestCode == GALLERY_REQUEST_CODE_PROFILE && resultCode == RESULT_OK) {
            try {
                mPhotoFile = null;
                mImageFile = FileUtil.from(this, data.getData());
                mCircleImageViewProfile.setImageBitmap(BitmapFactory.decodeFile(mImageFile.getAbsolutePath()));
            } catch (Exception e) {
                Log.d("ERROR", "Se produjo un error" + e.getMessage());
                Toast.makeText(this, "Se produjo un error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        if (requestCode == GALLERY_REQUEST_CODE_COVER && resultCode == RESULT_OK) {
            try {
                mPhotoFile2 = null;
                mImageFile2 = FileUtil.from(this, data.getData());
                mImageViewCover.setImageBitmap(BitmapFactory.decodeFile(mImageFile2.getAbsolutePath()));
            } catch (Exception e) {
                Log.d("ERROR", "Se produjo un error" + e.getMessage());
                Toast.makeText(this, "Se produjo un error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        /**
         * SELECCIÓN DE IMAGEN CON CÁMARA
         */
        if (requestCode == PHOTO_REQUEST_CODE_PROFILE && resultCode == RESULT_OK){
            mImageFile = null;
            mPhotoFile = new File(mAbsolutePhotoPath);
            Picasso.with(EditProfileActivity.this).load(mPhotoPath).into(mCircleImageViewProfile);
        }

        if (requestCode == PHOTO_REQUEST_CODE_COVER && resultCode == RESULT_OK){
            mImageFile2 = null;
            mPhotoFile2 = new File(mAbsolutePhotoPath2);
            Picasso.with(EditProfileActivity.this).load(mPhotoPath2).into(mImageViewCover);
        }

    }

    //y este cuando se inicializa la aplicacion
    @Override
    protected void onStart() {
        super.onStart();
        ViewedMessageHelper.updateOnline(true, EditProfileActivity.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ViewedMessageHelper.updateOnline(true, EditProfileActivity.this);
    }

    //Este metodo se ejecuta cada vez que se cambia de activity o se cierra la aplicacion
    @Override
    protected void onPause() {
        super.onPause();
        ViewedMessageHelper.updateOnline(false, EditProfileActivity.this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        ViewedMessageHelper.updateOnline(false, EditProfileActivity.this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ViewedMessageHelper.updateOnline(false, EditProfileActivity.this);
    }
}