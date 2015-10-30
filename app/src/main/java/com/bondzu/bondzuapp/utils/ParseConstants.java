package com.bondzu.bondzuapp.utils;

import android.content.Context;

import com.bondzu.bondzuapp.R;

/**
 * Created by gerardo on 5/08/15.
 */
public class ParseConstants {
    // Class name
    public static final String CLASS_USER = "_User";
    public static final String CLASS_CAMERA = "Camera";
    public static final String CLASS_ANIMALV2 = "AnimalV2";
    public static final String CLASS_PRODUCT = "Productos";
    public static final String CLASS_TRANSACTIONS = "Transacciones";
    public static final String CLASS_EVENTS = "Calendar";
    public static final String CLASS_KEEPERS = "Keeper";
    public static final String CLASS_MESSAGES = "Messages";
    public static final String CLASS_COMMENT = "Comment";
    public static final String CLASS_GALLERY = "Gallery";
    public static final String CLASS_VIDEO = "Video";


    public static final String KEY_GENERAL_ID = "objectId";
    public static final String KEY_GENERAL_CREATED = "createdAt";

    // Filed animaslV2
    public static String getKeyAnimalv2Name(Context context) {
        return context.getString(R.string.KEY_ANIMALV2_NAME);
    }

    public static String getKeyAnimalCharacteristics(Context context) {
        return context.getString(R.string.KEY_ANIMAL_CHARACTERISTICS);
    }

    public static String getKeyAnimalAbout(Context context) {
        return context.getString(R.string.KEY_ANIMAL_ABOUT);
    }

    public static String getKeyAnimalSpecies(Context context) {
        return context.getString(R.string.KEY_ANIMAL_SPECIES);
    }
    public static final String KEY_ANIMAL_PHOTO = "profilePhoto";
    public static final String KEY_ANIMAL_KEEPERS = "keepers";
    public static final String KEY_ANIMAL_EVENTS = "events";
    public static final String KEY_ANIMAL_ADOPTERS = "adopters";

    // Fields animals
    public static final String KEY_ANIMAL_ID = "animal_id";

    // Fields cameras
    public static final String KEY_CAMERA_ANIMAL_ID = "animal_Id";
    public static final String KEY_CAMERA_DESCRIPTION = "description";
    public static final String KEY_CAMERA_URL = "url";
    public static final String KEY_CAMERA_AVAILABILITY = "funcionando";

    // Fields products
    public static final String KEY_PRODUCT_ID= "objectId";
    public static final String KEY_PRODUCT_PHOTO= "photo";
    public static final String KEY_PRODUCT_PRICE= "precio2";
    public static final String KEY_PRODUCT_ANIMAL_ID= "animal_Id";
    public static final String KEY_PRODUCT_FIELD_DONATION= "Donation";
    public static String getKeyProductName(Context context) {
        return context.getString(R.string.KEY_PRODUCT_NAME);
    }

    public static String getKeyProductDescription(Context context) {
        return context.getString(R.string.KEY_PRODUCT_DESCRIPTION);
    }

    public static String getKeyProductInfo(Context context) {
        return context.getString(R.string.KEY_PRODUCT_INFO);
    }

    public static String getKeyProductInfoAmount(Context context) {
        return context.getString(R.string.KEY_PRODUCT_INFO_AMOUNT);
    }

    public static String getKeyProductCategory(Context context) {
        return context.getString(R.string.KEY_PRODUCT_CATEGORY);
    }


    // Fields transactions
    public static final String KEY_TRANSACTION_USER_ID= "userid";
    public static final String KEY_TRANSACTION_PRODUCT_ID= "productoid";
    public static final String KEY_TRANSACTION_ID= "transaccionid";
    public static final String KEY_TRANSACTION_DESCRIPTION= "descripcion";
    public static final String KEY_TRANSACTION_AMOUNT= "precio";


    // Fields Keepers
    public static final String KEY_KEEPER_USER= "user";
    public static final String KEY_KEEPER_ZOO= "zoo";

    // Fields Zoo
    public static final String KEY_ZOO_NAME= "name";

    //Fields Event
    public static final String KEY_EVENT_START_DATE= "start_date";
    public static final String KEY_EVENT_END_DATE= "end_date";
    public static final String KEY_EVENT_PHOTO= "event_photo";
    public static final String KEY_EVENT_ANIMAL_ID= "id_animal";
    public static String getKeyEventTitle(Context context) {
        return context.getString(R.string.KEY_EVENT_TITLE);
    }

    public static String getKeyEventDescription(Context context) {
        return context.getString(R.string.KEY_EVENT_DESCRIPTION);
    }

    // Fields User
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_USER_NAME= "name";
    public static final String KEY_USER_LASTNAME= "lastname";
    public static final String KEY_USER_PHOTO= "photo";
    public static final String KEY_USER_PHOTO_FILE= "photoFile";
    public static final String KEY_USER_EMAIL= "email";
    public static final String KEY_USER_ADOPTER_RELATION= "adoptersRelation";
    public static final String KEY_USER_TRANSACTIONS= "Compras";
    public static final String KEY_USER_STRIPE = "stripeId";
    public static final String KEY_USER_STRIPE_FINGERPRINT = "stripeFingerPrint";


    // Fields Comments
    public static final String KEY_COMMENTS_ANIMAL = "animal_Id";
    public static final String KEY_COMMENTS_USER= "id_user";
    public static final String KEY_COMMENTS_MESSAGE= "message";
    public static final String KEY_COMMENTS_PARENT= "parent";
    public static final String KEY_COMMENTS_PHOTO= "photo_message";
    public static final String KEY_COMMENTS_LIKES_RELATION= "likesRelation";

    // Fields Gallery
    public static final String KEY_GALLERY_ANIMAL = "animal_id";
    public static final String KEY_GALLERY_FILE= "image";

    // Fields Video
    public static final String KEY_VIDEO_ANIMAL = "animal_id";
    public static final String KEY_VIDEO_YOUTUBE= "youtube_ids";
    public static String getKeyVideoTitles(Context context) {
        return context.getString(R.string.KEY_VIDEO_TITLES);
    }

    public static String getKeyVideoDescriptions(Context context) {
        return context.getString(R.string.KEY_VIDEO_DESCRIPTIONS);
    }

    // Fields predator
    public static final String KEY_PREDATOR = "661WX90t3V";

}
