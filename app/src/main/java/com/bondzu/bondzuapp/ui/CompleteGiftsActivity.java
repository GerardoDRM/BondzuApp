package com.bondzu.bondzuapp.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

import com.bondzu.bondzuapp.R;
import com.bondzu.bondzuapp.adapters.ProductAdapter;
import com.bondzu.bondzuapp.utils.GeneralConstants;
import com.bondzu.bondzuapp.utils.ParseConstants;

public class CompleteGiftsActivity extends AppCompatActivity {
    GridView grid;
    String mCategory, mAnimal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_gifts);

        mCategory = getIntent().getExtras().getString(ParseConstants.getKeyProductCategory(this));
        mAnimal = getIntent().getExtras().getString(ParseConstants.KEY_PRODUCT_ANIMAL_ID);

        // Adding material design toolbar
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setNavigationIcon(ContextCompat.getDrawable(this, R.mipmap.ic_close_white_24dp));
        mToolbar.setTitle(mCategory);
        setSupportActionBar(mToolbar);
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        grid = (GridView) findViewById(R.id.custom_products_grid);


    }

    private void getGifts(String mCategory, String mAnimal) {
        ParseObject mPointer = ParseObject.createWithoutData(ParseConstants.CLASS_ANIMALV2, mAnimal);
        // Query to get Products of specific category
        ParseQuery<ParseObject> query = ParseQuery.getQuery(ParseConstants.CLASS_PRODUCT);
        query.whereEqualTo(ParseConstants.KEY_PRODUCT_ANIMAL_ID, mPointer);
        query.whereEqualTo(ParseConstants.getKeyProductCategory(this), mCategory);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> gifts, ParseException e) {
                if (e == null) {
                    if (grid.getAdapter() == null) {
                        // Adding functionality to items on grid views
                        ProductAdapter adapter = new ProductAdapter(CompleteGiftsActivity.this, gifts);
                        grid.setAdapter(adapter);
                        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                ParseObject product = (ParseObject) grid.getItemAtPosition(position);
                                Intent i = new Intent(CompleteGiftsActivity.this, ProductDescription.class);
                                i.putExtra(ParseConstants.KEY_PRODUCT_ID, product.getObjectId());
                                startActivity(i);
                            }
                        });
                    } else {
                        ((ProductAdapter) grid.getAdapter()).refill(gifts);
                    }


                } else {
                    if(!isFinishing()){
                    AlertDialog.Builder builder = new AlertDialog.Builder(CompleteGiftsActivity.this);
                    builder.setMessage(e.getMessage())
                            .setTitle(R.string.simple_error_title)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    }
                }
            }

        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check Network
        if (!GeneralConstants.checkNetwork(this))
            GeneralConstants.showMessageConnection(this);
        else {
            // Get gifts
            getGifts(mCategory, mAnimal);
        }
    }
}