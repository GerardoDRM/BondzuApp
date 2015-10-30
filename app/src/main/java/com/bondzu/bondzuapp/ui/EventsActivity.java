package com.bondzu.bondzuapp.ui;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.bondzu.bondzuapp.R;
import com.bondzu.bondzuapp.adapters.EventsAdapter;
import com.bondzu.bondzuapp.utils.GeneralConstants;
import com.bondzu.bondzuapp.utils.ParseConstants;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class EventsActivity extends AppCompatActivity {

    @Bind(R.id.recyclerViewEvents)
    RecyclerView mEventList;
    @Bind(R.id.swipeRefreshEvents)
    SwipeRefreshLayout mSwipe;

    private ParseObject animalReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);
        ButterKnife.bind(this);

        // Use a swipe in order to update UI
        mSwipe.setColorSchemeColors(R.color.colorPrimary, R.color.colorPrimaryDark,
                R.color.colorAccent, android.R.color.white);
        mSwipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // When the UI is updated we get new comments
                getEvents();
            }
        });
        // Recycler view data
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mEventList.setLayoutManager(layoutManager);
        mEventList.setHasFixedSize(true);

        // Adding material design toolbar
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setNavigationIcon(ContextCompat.getDrawable(this, R.mipmap.ic_close_white_24dp));
        setSupportActionBar(mToolbar);
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        // Get animal Id in order to get all its events
        String animalId = getIntent().getStringExtra(ParseConstants.KEY_EVENT_ANIMAL_ID);
        animalReference = ParseObject.createWithoutData(ParseConstants.CLASS_ANIMALV2, animalId);
        getEvents();
    }


    /**
     * This method will get all replies for
     * the message received
     */
    private void getEvents() {
        Calendar c = Calendar.getInstance();
        Date d = c.getTime();
        ParseQuery<ParseObject> query = ParseQuery.getQuery(ParseConstants.CLASS_EVENTS);
        query.addDescendingOrder(ParseConstants.KEY_EVENT_START_DATE);
        query.whereEqualTo(ParseConstants.KEY_EVENT_ANIMAL_ID, animalReference);
        query.whereGreaterThan(ParseConstants.KEY_EVENT_START_DATE, d);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> events, ParseException e) {
                // Hide swipe from UI
                if (mSwipe.isRefreshing()) {
                    mSwipe.setRefreshing(false);
                }
                // If there aren't any errors the comments adapter needs to be created
                if (e == null) {

                    // Check if we need to create the recycler view adapter
                    if(mEventList.getAdapter() == null) {
                        EventsAdapter comments = new EventsAdapter(EventsActivity.this, events);
                        mEventList.setAdapter(comments);
                    }
                    // Else just update list
                    else {
                        ((EventsAdapter) mEventList.getAdapter()).refill(events);
                    }
                }
                // Else a message will be displayed
                else {
                    if(!isFinishing()) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(EventsActivity.this);
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
            // Get all events
            getEvents();
        }
    }
}
