package com.bondzu.bondzuapp.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bondzu.bondzuapp.BondzuApp;
import com.bondzu.bondzuapp.R;
import com.bondzu.bondzuapp.utils.CircleTransform;
import com.bondzu.bondzuapp.utils.FileHelper;
import com.bondzu.bondzuapp.utils.GeneralConstants;
import com.bondzu.bondzuapp.utils.ParseConstants;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import com.squareup.picasso.Picasso;
import com.stripe.exception.APIConnectionException;
import com.stripe.exception.APIException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.model.Customer;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SignUp extends AppCompatActivity {

    @Bind(R.id.username)
    EditText mUsername;
    @Bind(R.id.password)
    EditText mPassword;
    @Bind(R.id.emailField)
    EditText mEmail;
    @Bind(R.id.signupBtn)
    Button mSignUpBtn;
    @Bind(R.id.facebook)
    Button mFacebook;
    @Bind(R.id.img_register)
    ImageView mImgProfile;
    @Bind(R.id.input_lastname)
    EditText mLastName;
    @Bind(R.id.go_login)
    TextView mGoLogin;

    public static final int PICK_PHOTO_REQUEST = 2;
    private ParseFile file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_athentication);
        ButterKnife.bind(this);

        // Check if the user is logged
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            // do stuff with the user
            Intent home = new Intent(this, Home.class);
            home.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(home);
        }

        mGoLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SignUp.this, Login.class);
                startActivity(i);
            }
        });

        // If the user wants an image profile
        mImgProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open Intent in order to get a picture from gallery
                Intent choosePhoto = new Intent(Intent.ACTION_GET_CONTENT);
                choosePhoto.setType(GeneralConstants.KEY_IMG_PATH);
                startActivityForResult(choosePhoto, PICK_PHOTO_REQUEST);
            }
        });

        // Sign up with facebook
        mFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Validate Terms and policies
                AlertDialog.Builder builder = new AlertDialog.Builder(SignUp.this);
                builder.setMessage(getString(R.string.privacy_content))
                        .setTitle(getString(R.string.title_privacy))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Permission list facebook
                                List<String> permissions = Arrays.asList(GeneralConstants.KEY_FACEBOOK_PERMISSION, ParseConstants.KEY_USER_EMAIL);
                                ParseFacebookUtils.logInWithReadPermissionsInBackground(SignUp.this, permissions, new LogInCallback() {
                                    @Override
                                    public void done(ParseUser user, ParseException err) {
                                        if (user == null) {
                                            String message = getString(R.string.facebook_cancel);
                                            errorSignUp(message);
                                        } else if (user.isNew()) {
                                            makeMeRequest();
                                        } else {
                                            acceptUser();
                                        }

                                    }
                                });
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null);
                AlertDialog dialog = builder.create();
                dialog.show();

            }
        });

        // Sign up using Bondzu service
        mSignUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = mUsername.getText().toString().trim();
                final String lastName = mLastName.getText().toString().trim();
                final String password = mPassword.getText().toString().trim();
                final String email = mEmail.getText().toString().trim();

                // Validate Terms and policies
                AlertDialog.Builder builder = new AlertDialog.Builder(SignUp.this);
                builder.setMessage(R.string.privacy_content)
                        .setTitle(R.string.title_privacy)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Validate data
                        if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(SignUp.this);
                            builder.setMessage(R.string.sign_up_error_message)
                                    .setTitle(R.string.sign_up_error_title)
                                    .setPositiveButton(android.R.string.ok, null);
                            AlertDialog dialogSign = builder.create();
                            dialogSign.show();
                        } else if (!GeneralConstants.checkLong(password, 5)) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(SignUp.this);
                            builder.setMessage(R.string.sign_up_password)
                                    .setTitle(R.string.sign_up_error_title)
                                    .setPositiveButton(android.R.string.ok, null);
                            AlertDialog dialogSign = builder.create();
                            dialogSign.show();
                        } else {

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    final String id = createAccount(email);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            // create the new user
                                            ParseUser user = new ParseUser();
                                            user.put(ParseConstants.KEY_USER_NAME, username);
                                            user.put(ParseConstants.KEY_USER_LASTNAME, lastName);
                                            user.setUsername(email);
                                            user.setPassword(password);
                                            user.setEmail(email);
                                            user.put(ParseConstants.KEY_USER_STRIPE, id);

                                            // Check if the user add a photo
                                            if (file != null) {
                                                user.put(ParseConstants.KEY_USER_PHOTO_FILE, file);
                                            } else {
                                                // Create a file with default image
                                                user.put(ParseConstants.KEY_USER_PHOTO, GeneralConstants.KEY_DEFAULT_PHOTO_URL);
                                            }

                                            // Use parse sign up to store user
                                            user.signUpInBackground(new SignUpCallback() {
                                                public void done(ParseException e) {
                                                    setProgressBarIndeterminateVisibility(false);
                                                    if (e == null) {
                                                        // Hooray! Let them use the app now.
                                                        acceptUser();
                                                    } else {
                                                        // Sign up didn't succeed. Look at the ParseException
                                                        // to figure out what went wrong
                                                        errorSignUp(e.getMessage());

                                                    }
                                                }
                                            });
                                        }
                                    });
                                }
                            }).start();

                        }

                    }
                })
                        .setNegativeButton(android.R.string.cancel, null);
                AlertDialog dialog = builder.create();
                dialog.show();


            } // end OnClick
        });// end Listener

    }

    private void acceptUser() {
        // Hooray! Let them use the app now.
        BondzuApp.updateParseInstallation(
                ParseUser.getCurrentUser()
        );
        Intent i = new Intent(SignUp.this, Home.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }

    // If there is an error report error
    private void errorSignUp(String e) {
        AlertDialog.Builder builder = new AlertDialog.Builder(SignUp.this);
        builder.setMessage(e)
                .setTitle(R.string.sign_up_error_title)
                .setPositiveButton(android.R.string.ok, null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * This function create a new account
     * on Stripe
     *
     * @return String
     */
    private String createAccount(String email) {
        Customer mNewCustomer = null;
        Map<String, Object> customerParams = new HashMap<String, Object>();
        customerParams.put("description", "Customer for " + email);
        customerParams.put("email", email);
        try {
            mNewCustomer = Customer.create(customerParams);
        } catch (AuthenticationException | APIException | APIConnectionException | InvalidRequestException | CardException e) {
            e.printStackTrace();
        }
        assert mNewCustomer != null;
        return mNewCustomer.getId();
    }

    // Get picture from gallery
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            // Success add it to the gallery
            if (requestCode == PICK_PHOTO_REQUEST) {
                if (data == null) {
                    Toast.makeText(this, getString(R.string.simple_error_message), Toast.LENGTH_LONG).show();
                }
                else {
                    // Get image from gallery and create a file
                    Uri mMediaUri = data.getData();
                    byte[] fileBytes = FileHelper.getByteArrayFromFile(this, mMediaUri);
                    fileBytes = FileHelper.reduceImageForUpload(fileBytes);
                    String fileName = FileHelper.getFileName(this, mMediaUri, GeneralConstants.KEY_IMG);
                    file = new ParseFile(fileName, fileBytes);
                    file.saveInBackground();
                    Picasso.with(this).load(mMediaUri).transform(new CircleTransform()).into(mImgProfile);

                }
            }
        }
    }

    // This function request to open graph facebook in order to get profile data
    private void makeMeRequest() {
        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject jsonObject, GraphResponse graphResponse) {
                        boolean error = false;
                        String message = "";
                        if (jsonObject != null) {
                            // Save the user profile info in a user property
                            ParseUser currentUser = ParseUser.getCurrentUser();
                            try {
                                String name[] = new String[2];
                                name = parseString(jsonObject.getString(ParseConstants.KEY_USER_NAME));
                                currentUser.put(ParseConstants.KEY_USER_NAME, name[0]);
                                currentUser.put(ParseConstants.KEY_USER_LASTNAME, name[1]);

                                if (jsonObject.getString(ParseConstants.KEY_USER_EMAIL) != null) {
                                    currentUser.put(ParseConstants.KEY_USER_EMAIL, jsonObject.getString(ParseConstants.KEY_USER_EMAIL));
                                    String id = createAccount(jsonObject.getString(ParseConstants.KEY_USER_EMAIL));
                                    currentUser.put("stripeId",id);
                                }
                                else {
                                    String id = createAccount(name[0] + "@bondzu_cliente.com" );
                                    currentUser.put("stripeId", id);
                                }

                                JSONObject photo = jsonObject.getJSONObject(GeneralConstants.KEY_PHOTO_FACEBOOK).getJSONObject(GeneralConstants.KEY_PHOTO_DATA_FACEBOOK);

                                currentUser.put(ParseConstants.KEY_USER_PHOTO, photo.getString(GeneralConstants.KEY_PHOTO_URL_FACEBOOK));
                                currentUser.saveInBackground();

                                // Hooray! Let them use the app now.
                                acceptUser();

                            } catch (JSONException e) {
                                message = getString(R.string.error_parsing_facebook) + e;
                                error = true;
                            }
                        } else if (graphResponse.getError() != null) {
                            error = true;
                            switch (graphResponse.getError().getCategory()) {
                                case LOGIN_RECOVERABLE:
                                    message = getString(R.string.authentication_error) + graphResponse.getError();
                                    break;

                                case TRANSIENT:
                                    message = getString(R.string.try_again_facebook) + graphResponse.getError();
                                    break;

                                case OTHER:
                                    message = getString(R.string.other_facebook_error) + graphResponse.getError();
                                    break;
                            }
                        }
                        if (error) {
                            errorSignUp(message);
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,email,gender,name, picture.type(large)");
        request.setParameters(parameters);
        request.executeAsync();
    }

    // This function is a simple string slicer to get the name and last name from facebook
    private String[] parseString(String name) {
        String completeName[]= new String[2];
        String[] parts = name.split(" ");
        String lastname= "";
        if(parts.length > 2) {
            for(int i=1; i<parts.length; i++) {
                lastname += parts[i] + " ";
                completeName[1] = lastname;
            }
        }
        else {
            completeName[1] = parts[1];
        }
        completeName[0] = parts[0];

        return completeName;
    }

}
