package com.jsancre.gameverse.providers;

import android.content.Context;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jsancre.gameverse.utils.CompressorBitmapImage;

import java.io.File;
import java.util.Date;

/**
 * Las clases 'Provider' las utilizo para la gestión de información con la base de datos de FireStore
 */
public class ImageProvider {
    //--------------------
    //     ATRIBUTOS
    //--------------------
    StorageReference mStorage;

    //--------------------
    //    CONSTRUCTOR
    //--------------------
    public ImageProvider() {
        mStorage = FirebaseStorage.getInstance().getReference();
    }

    //--------------------
    //      MÉTODOS
    //--------------------
    //Método que almacena la imagen en la base de datos pasandola antes por el CompressorBitMapImage,
    // que es una clase interna que he creado que gestiona la compresion de imagenes a una resolución determinada
    public UploadTask save(Context context, File file){
        byte[] imageByte = CompressorBitmapImage.getImage(context, file.getPath(), 500, 500);
        StorageReference storage = FirebaseStorage.getInstance().getReference().child(new Date() + ".jpg");
        mStorage = storage;
        UploadTask task = storage.putBytes(imageByte);
        return task;
    }

    public StorageReference getStorage() {
        return mStorage;
    }


}
