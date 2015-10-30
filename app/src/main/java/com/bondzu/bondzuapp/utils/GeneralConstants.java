package com.bondzu.bondzuapp.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;

import com.bondzu.bondzuapp.R;

/**
 * Created by gerardo on 18/08/15.
 */
public class GeneralConstants {

    public static final String KEY_ACTIVITY_TRANSACTION = "payment";
    public static final String KEY_ACTIVITY_DONATION = "donation";
    public static final String KEY_ACTIVITY_PURCHASE = "purchase";
    public static final String KEY_ROBOTO_NORMAL = "sans-serif";
    public static final String KEY_ROBOTO_LIGHT = "sans-serif-light";
    public static final String KEY_CONSTANT_DONATION = "Donacion";
    public static final String KEY_ID_DONATION = "1EhtID3J6X";
    public static final String KEY_IMG = "img";
    public static final String KEY_FACEBOOK_PERMISSION = "public_profile";
    public static final String KEY_IMG_PATH = "image/*";
    public static final String KEY_DEFAULT = "deafult";
    public static final String KEY_DEFAULT_PHOTO_URL = "http://files.parsetfss.com/a82729a1-e6e9-43f0-acae-a060f3153c6d/tfss-3d88ecd0-d988-4ab3-a297-cf44a2682864-perfil3.jpg";
    public static final String KEY_PHOTO_FACEBOOK = "picture";
    public static final String KEY_PHOTO_DATA_FACEBOOK = "data";
    public static final String KEY_PHOTO_URL_FACEBOOK = "url";

    public static final String KEY_POS = "position";
    public static final String KEY_LIST = "list";

    // Youtube developer key
    public static final String DEVELOPER_KEY = "AIzaSyBJ8l08aMVz66aGQNnYaNpdcarA5wlw7UQ";

    // Connection

    /**
     *
     * @param context
     * @return a boolean variable
     */
    public static boolean checkNetwork(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        return isConnected;
    }

    /**
     *
     * @param context
     * @return
     */
    public static boolean checkWIFI(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;

        return isWiFi;
    }

    public static void showMessageWIFI(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(context.getString(R.string.no_wifi))
                .setTitle(R.string.simple_error_title)
                .setPositiveButton(android.R.string.ok, null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public static void showMessageConnection(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(context.getString(R.string.no_internet))
                .setTitle(R.string.simple_error_title)
                .setPositiveButton(android.R.string.ok, null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public static boolean checkLong(String message, int longitude){
        if(message.length() < longitude) {
            return false;
        }
        return true;
    }
}
