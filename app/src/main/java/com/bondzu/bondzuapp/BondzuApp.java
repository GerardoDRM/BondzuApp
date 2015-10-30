package com.bondzu.bondzuapp;

import android.app.Application;

import com.facebook.FacebookSdk;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.stripe.Stripe;

import com.bondzu.bondzuapp.utils.ParseConstants;

/**
 * Created by gerardo on 5/08/15.
 */
public class BondzuApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        Parse.initialize(this, "7aGqZRDKBITfaIRAXq2oKoBkuWkhNqJZJWmf318I", "lwOEFDvVC8SsM4Nl86YBzrkDOlOw8WHCyqu4UpBe");

        ParseInstallation.getCurrentInstallation().saveInBackground();

        // Using stripe
        Stripe.apiKey = "sk_live_EZQjYpgz0EIdX86EGLhkfvG4";

        ParseFacebookUtils.initialize(this);
        FacebookSdk.sdkInitialize(getApplicationContext());

    }

    public static void updateParseInstallation(ParseUser user) {
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.put(ParseConstants.KEY_USER_ID, user.getObjectId());
    }

}
