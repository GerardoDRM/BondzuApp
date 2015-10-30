package com.bondzu.bondzuapp.ui;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;

import com.bondzu.bondzuapp.R;
import com.bondzu.bondzuapp.utils.GeneralConstants;
import com.bondzu.bondzuapp.utils.ParseConstants;
import butterknife.Bind;
import butterknife.ButterKnife;

public class ProductDescription extends AppCompatActivity {

    @Bind(R.id.photo_description)
    ImageView photo;
    @Bind(R.id.product_title)
    TextView title;
    @Bind(R.id.amount)
    TextView amount;
    @Bind(R.id.product_desc)
    TextView description;
    @Bind(R.id.float_btn_car)
    FloatingActionButton car;
    private String productRef;


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.status_bar_product_desc));
        }

        setContentView(R.layout.activity_product_description);
        ButterKnife.bind(this);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setNavigationIcon(ContextCompat.getDrawable(this, R.mipmap.ic_close_white_24dp));
        mToolbar.setBackgroundColor(Color.TRANSPARENT);
        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        productRef = getIntent().getExtras().getString(ParseConstants.KEY_PRODUCT_ID);

    }

    private void getProductDescription() {
        // Query to get product object
        ParseQuery<ParseObject> query = ParseQuery.getQuery(ParseConstants.CLASS_PRODUCT);
        query.whereEqualTo(ParseConstants.KEY_PRODUCT_ID, productRef);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            public void done(final ParseObject product, ParseException e) {
                if (product == null || e != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ProductDescription.this);
                    builder.setTitle(R.string.simple_error_title)
                            .setMessage(R.string.products_error_message)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                else {
                    // Get the image url and we use picasso to load the image
                    String urlProduct = product.getParseFile(ParseConstants.KEY_PRODUCT_PHOTO).getUrl();
                    Picasso.with(ProductDescription.this).load(urlProduct).into(photo);

                    // Get product name and price
                    String name = product.getString(ParseConstants.getKeyProductName(ProductDescription.this));
                    title.setText(name);
                    DecimalFormat df = new DecimalFormat("#.00");
                    Double price = product.getDouble(ParseConstants.KEY_PRODUCT_PRICE);
                    amount.setText("$" + df.format(price).toString() + " *");

                    // Get all product description
                    String desc = "";
                    desc += product.getString(ParseConstants.getKeyProductDescription(ProductDescription.this)) + "\n\n";
                    desc += product.getString(ParseConstants.getKeyProductInfo(ProductDescription.this)) + "\n\n";
                    desc += product.getString(ParseConstants.getKeyProductInfoAmount(ProductDescription.this));

                    description.setText(desc);

                    // Add car functionality
                    car.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String value = String.format("%d", (long) product.getDouble(ParseConstants.KEY_PRODUCT_PRICE));
                            Intent i = new Intent(ProductDescription.this, CardDetailsActivity.class);
                            i.putExtra(ParseConstants.KEY_PRODUCT_ID, product.getObjectId());
                            i.putExtra(ParseConstants.getKeyProductName(ProductDescription.this), product.getString(ParseConstants.getKeyProductName(ProductDescription.this)));
                            i.putExtra(ParseConstants.KEY_PRODUCT_PRICE, value );
                            i.putExtra(GeneralConstants.KEY_ACTIVITY_TRANSACTION, GeneralConstants.KEY_ACTIVITY_PURCHASE);
                            startActivity(i);
                        }
                    });

                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!GeneralConstants.checkNetwork(this))
            GeneralConstants.showMessageConnection(this);
        else {
            // Get product description
            getProductDescription();
        }
    }
}
