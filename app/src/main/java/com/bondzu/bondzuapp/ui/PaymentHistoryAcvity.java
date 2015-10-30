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
import com.bondzu.bondzuapp.adapters.HistoryAdapter;
import com.bondzu.bondzuapp.utils.DividerItemDecoration;
import com.bondzu.bondzuapp.utils.GeneralConstants;
import com.bondzu.bondzuapp.utils.ParseConstants;
import butterknife.Bind;
import butterknife.ButterKnife;

public class PaymentHistoryAcvity extends AppCompatActivity {
    @Bind(R.id.recyclerView)
    RecyclerView mHistoryList;
    @Bind(R.id.empty_transactions)
    TextView mEmpty;
    private ParseRelation<ParseObject> mRelation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_history_acvity);
        ButterKnife.bind(this);

        // Recycler view data
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mHistoryList.setLayoutManager(layoutManager);
        mHistoryList.setHasFixedSize(true);
        mHistoryList.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        // Adding material design toolbar
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!GeneralConstants.checkNetwork(this))
            GeneralConstants.showMessageConnection(this);
        else {
            // Get user id in order to check his transactions
            ParseUser mUser = ParseUser.getCurrentUser();
            mRelation = mUser.getRelation(ParseConstants.KEY_USER_TRANSACTIONS);
            // Get transactions
            getTransactions();
        }
    }

    /**
     * This method will get all user adoptions
     *
     */
    private void getTransactions() {
        ParseQuery<ParseObject> query = mRelation.getQuery();
        query.include(ParseConstants.KEY_TRANSACTION_PRODUCT_ID);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> transactions, ParseException e) {
                // If there aren't any errors the adapter needs to be created
                if (e == null) {
                    if(transactions == null ||transactions.isEmpty()){
                        mHistoryList.setVisibility(View.GONE);
                        mEmpty.setVisibility(View.VISIBLE);
                    }
                    else {
                        // Check if we need to create the recycler view adapter
                        if (mHistoryList.getAdapter() == null) {
                            HistoryAdapter adapter = new HistoryAdapter(PaymentHistoryAcvity.this, transactions);
                            mHistoryList.setAdapter(adapter);
                        }
                        // Else just update list
                        else {
                            ((HistoryAdapter) mHistoryList.getAdapter()).refill(transactions);
                        }
                    }
                }
                // Else a message will be displayed
                else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(PaymentHistoryAcvity.this);
                    builder.setMessage(e.getMessage())
                            .setTitle(R.string.simple_error_title)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    mHistoryList.setVisibility(View.GONE);
                    mEmpty.setVisibility(View.VISIBLE);
                }
            }

        });
    }
}
