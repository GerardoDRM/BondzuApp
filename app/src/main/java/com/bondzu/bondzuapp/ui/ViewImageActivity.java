package com.bondzu.bondzuapp.ui;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.Timer;
import java.util.TimerTask;

import com.bondzu.bondzuapp.R;

public class ViewImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);

        ImageView image = (ImageView) findViewById(R.id.image_full_view);
        Uri imageUri = getIntent().getData();

        // Using picasso to get an image from web
        // ,, Pretty cool
        Picasso.with(this).load(imageUri.toString()).into(image);
        Timer time = new Timer();
        time.schedule(new TimerTask() {
            @Override
            public void run() {
                finish();
            }
        }, 10 * 1000);
    }
}
