package com.bondzu.bondzuapp.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;

import com.bondzu.bondzuapp.BondzuApp;
import com.bondzu.bondzuapp.R;
import butterknife.Bind;
import butterknife.ButterKnife;

public class Login extends AppCompatActivity {
    @Bind(R.id.email_login)
    EditText mUsernameLog;
    @Bind(R.id.password_login)
    EditText mPasswordLog;
    @Bind(R.id.loginBtn)
    Button mLoginBtn;
    @Bind(R.id.forgot_password)
    TextView mForgot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        // Login using Parse
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = mUsernameLog.getText().toString().trim();
                String password = mPasswordLog.getText().toString().trim();

                // Validate email and password
                if (username.isEmpty() || password.isEmpty()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
                    builder.setMessage(R.string.login_error_message)
                            .setTitle(R.string.login_error_title)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {

                    // Login in Background using Parse
                    ParseUser.logInInBackground(username, password, new LogInCallback() {
                        public void done(ParseUser user, ParseException e) {

                            if (user != null) {
                                // Hooray! The user is logged in.
                                // Let them use the app now.
                                BondzuApp.updateParseInstallation(user);
                                Intent i = new Intent(Login.this, Home.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(i);
                            } else {
                                // Sign up failed. Look at the ParseException to see what happened.
                                // Sign up didn't succeed. Look at the ParseException
                                // to figure out what went wrong
                                if(!isFinishing()) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
                                    builder.setMessage(e.getMessage())
                                            .setTitle(R.string.sign_up_error_title)
                                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    Intent i = new Intent(Login.this, SignUp.class);
                                                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(i);
                                                }
                                            });
                                    AlertDialog dialog = builder.create();
                                    dialog.show();
                                }
                            }
                        }
                    });

                }
            }
        });

        mForgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = mUsernameLog.getText().toString().trim();

                // Validate email and password
                if (username.isEmpty()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
                    builder.setMessage(R.string.forgot_error)
                            .setTitle(R.string.login_error_title)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    forgotPassword(username);
                }

            }
        });
    }

    public void forgotPassword(String email) {
        //postEvent(new UserForgotPasswordStartEvent());
        ParseUser.requestPasswordResetInBackground(email, new UserForgotPasswordCallback());
    }

    private class UserForgotPasswordCallback implements RequestPasswordResetCallback {
        public UserForgotPasswordCallback(){
            super();
        }

        @Override
        public void done(ParseException e) {
            if (e == null) {
                Toast.makeText(getApplicationContext(), R.string.forgot_success, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), R.string.forgot_fail, Toast.LENGTH_LONG).show();

            }
        }
    }

}
