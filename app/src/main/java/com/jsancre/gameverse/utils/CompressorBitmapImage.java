package com.jsancre.gameverse.utils;

import android.content.Context;
import android.graphics.Bitmap;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import id.zelory.compressor.Compressor;

/**
 * Esta clase es necesaria para la compresión de imagenes en la base de datos, gestiona todas
 * las imagenes que se suben en la aplicacion y las comprime a una resolución determinada para que no
 * haya posibles errores de memoria o del sistema de android
 */
public class CompressorBitmapImage {

    /*
     * Metodo que permite comprimir imagenes y transformarlas a bitmap
     */
    public static byte[] getImage(Context ctx, String path, int width, int height) {
        final File file_thumb_path = new File(path);
        Bitmap thumb_bitmap = null;

        try {
            thumb_bitmap = new Compressor(ctx)
                    .setMaxWidth(width)
                    .setMaxHeight(height)
                    .setQuality(75)
                    .compressToBitmap(file_thumb_path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        thumb_bitmap.compress(Bitmap.CompressFormat.JPEG,80,baos);
        byte[] thumb_byte = baos.toByteArray();
        return  thumb_byte;
    }
}
