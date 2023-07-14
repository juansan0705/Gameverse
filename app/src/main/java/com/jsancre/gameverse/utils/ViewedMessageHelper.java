package com.jsancre.gameverse.utils;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;

import com.jsancre.gameverse.providers.AuthProvider;
import com.jsancre.gameverse.providers.UsersProvider;

import java.util.List;

//Esta clase es para manejar los estados de conexion, comprueba si el usuario a cerrado la ventana
// de la aplicacion pero no termino su proceso, para que muestre como estado desconectado a los demás usuarios
public class ViewedMessageHelper {

    public static void updateOnline(boolean status, final Context context){
        UsersProvider mUserProvider = new UsersProvider();
        AuthProvider mAuthprovider = new AuthProvider();
        if (mAuthprovider.getUid() != null){
            if (isAplicationSendToBackground(context)){
                mUserProvider.updateOnline(mAuthprovider.getUid(), status);
            }
            else if (status){
                mUserProvider.updateOnline(mAuthprovider.getUid(), status);
            }
        }
    }

    public static boolean isAplicationSendToBackground(final Context context){
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(1);
        if (!tasks.isEmpty()){
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(context.getPackageName())){
                return true;
            }
        }
        return false;
    }

}
