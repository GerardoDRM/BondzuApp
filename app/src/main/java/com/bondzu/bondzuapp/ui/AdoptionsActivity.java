package com.bondzu.bondzuapp.ui;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.List;

import com.bondzu.bondzuapp.R;
import com.bondzu.bondzuapp.adapters.AdoptionAdapter;
import com.bondzu.bondzuapp.utils.DividerItemDecoration;
import com.bondzu.bondzuapp.utils.GeneralConstants;
import com.bondzu.bondzuapp.utils.ParseConstants;
import butterknife.Bind;
import butterknife.ButterKnife;

public class AdoptionsActivity extends AppCompatActivity {

    @Bind(R.id.recyclerViewAdoptions)
    RecyclerView mAdoptionList;
    @Bind(R.id.empty_adoptions)
    TextView mEmpty;
    private ParseRelation<ParseObject> mRelation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adoptions);
        ButterKnife.bind(this);

        // Adding material design toolbar
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        // Recycler view data
        mAdoptionList.setAdapter(null);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mAdoptionList.setLayoutManager(layoutManager);
        mAdoptionList.setHasFixedSize(true);
        mAdoptionList.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check Network
        if(!GeneralConstants.checkNetwork(this))
            GeneralConstants.showMessageConnection(this);
        else {
            // Get user
            ParseUser mUser = ParseUser.getCurrentUser();
            mRelation = mUser.getRelation(ParseConstants.KEY_USER_ADOPTER_RELATION);
            // Get adoptions
            getAdoptions();
        }
    }

    /**
     * This method will get all user adoptions
     *
     */
    private void getAdoptions() {
        ParseQuery<ParseObject> query = mRelation.getQuery();
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> animals, ParseException e) {
                // If there aren't any errors the comments adapter needs to be created
                if (e == null) {

                    if(animals == null ||animals.isEmpty()){
                        mAdoptionList.setVisibility(View.GONE);
                        mEmpty.setVisibility(View.VISIBLE);
                    }
                    else{
                        // Check if we need to create the recycler view adapter
                        if(mAdoptionList.getAdapter() == null) {
                            AdoptionAdapter adapter = new AdoptionAdapter(AdoptionsActivity.this, animals);
                            mAdoptionList.setAdapter(adapter);
                        }
                        // Else just update list
                        else {
                            ((AdoptionAdapter) mAdoptionList.getAdapter()).refill(animals);
                        }
                    }
                }
                // Else a message will be displayed
                else {
                    if(!isFinishing()) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(AdoptionsActivity.this);
                        builder.setMessage(e.getMessage())
                                .setTitle(R.string.simple_error_title)
                                .setPositiveButton(android.R.string.ok, null);
                        AlertDialog dialog = builder.create();
                        dialog.show();
                        mAdoptionList.setVisibility(View.GONE);
                        mEmpty.setVisibility(View.VISIBLE);
                    }
                }
            }

        });
    }
}
