package com.bondzu.bondzuapp.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bondzu.bondzuapp.BondzuApp;
import com.bondzu.bondzuapp.R;
import com.bondzu.bondzuapp.adapters.CardsAdapter;
import com.bondzu.bondzuapp.utils.DividerItemDecoration;
import com.bondzu.bondzuapp.utils.GeneralConstants;
import com.bondzu.bondzuapp.utils.ParseConstants;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.stripe.exception.APIConnectionException;
import com.stripe.exception.APIException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.model.Card;
import com.stripe.model.Customer;
import com.stripe.model.ExternalAccount;
import com.stripe.model.ExternalAccountCollection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

public class PaymentActivity extends AppCompatActivity {

    @Bind(R.id.recyclerView)
    RecyclerView mList;
    @Bind(R.id.empty_cards)
    TextView mEmpty;
    @Bind(R.id.add_card)
    Button mAdd;
    @Bind(R.id.progressBar)
    ProgressBar progressBar;
    @Bind(R.id.progress_layout)
    RelativeLayout progressLayout;

    // View Extra dialog
    View view;
    EditText firstName, lastName, cardNumber, month, year, cvc;
    // Card Variables
    String completeName, card, exp_month, exp_year, cv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        ButterKnife.bind(this);

        mAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Adding parameters to create dialog view
                LayoutInflater inflater = PaymentActivity.this.getLayoutInflater();
                // Inflate and set the layout for the dialog
                // Pass null as the parent view because its going in the dialog layout
                view = inflater.inflate(R.layout.dialog_card, null);
                // Get all dialog components
                firstName = (EditText) view.findViewById(R.id.card_first_name);
                lastName = (EditText) view.findViewById(R.id.card_last_name);
                cardNumber = (EditText) view.findViewById(R.id.card_number);
                month = (EditText) view.findViewById(R.id.month_exp_date);
                year = (EditText) view.findViewById(R.id.year_exp_date);
                cvc = (EditText) view.findViewById(R.id.card_cv);


                if (!isFinishing()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(PaymentActivity.this);
                    builder.setView(view)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String firstN = firstName.getText().toString();
                                    String lastN = lastName.getText().toString();
                                    completeName = firstN + " " + lastN;
                                    card = cardNumber.getText().toString();
                                    exp_month = month.getText().toString();
                                    exp_year = year.getText().toString();
                                    cv = cvc.getText().toString();
                                    if (firstN.isEmpty() || lastN.isEmpty() || card.isEmpty() || exp_month.isEmpty() ||
                                            exp_year.isEmpty() || cv.isEmpty()) {
                                        Toast.makeText(PaymentActivity.this, R.string.card_details_verification, Toast.LENGTH_LONG).show();
                                    } else {
                                        new AddCard().execute();
                                    }
                                }
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });

        // Recycler view data
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mList.setLayoutManager(layoutManager);
        mList.setHasFixedSize(true);
        mList.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
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
            // Check account
            new CheckAccount().execute();
        }
    }

    /**
     * This function checks if a user has an account
     * on Stripe and return stripe id
     *
     * @return String
     */
    private String checkForUser() {
        String stripe = ParseUser.getCurrentUser().getString(ParseConstants.KEY_USER_STRIPE);
        return stripe;
    }


    public ArrayList<String> getFingerPrints(ParseUser user) {
        List<String> likes = user.getList(ParseConstants.KEY_USER_STRIPE_FINGERPRINT);
        if (likes != null) {
            return (ArrayList<String>) likes;
        } else {
            return new ArrayList<String>();
        }
    }

    // We use an async task in order to configure stripe account
    public class CheckAccount extends AsyncTask<String, String, String> {
        boolean empty = false;
        CardsAdapter adapter;
        String message = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressLayout.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            String idAccount = ParseUser.getCurrentUser().getString(ParseConstants.KEY_USER_STRIPE);
            // Retrieve Account data
            try {
                Customer currentCustomer = Customer.retrieve(idAccount);
                // Check Register Cards
                Map<String, Object> cardParams = new HashMap<String, Object>();
                cardParams.put("object", "card");
                ExternalAccountCollection data = currentCustomer.getSources().all(cardParams);
                if (data == null) {
                    empty = true;
                } else {
                    List<ExternalAccount> mCardsList = data.getData();
                    adapter = new CardsAdapter(PaymentActivity.this, mCardsList);
                }


            } catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException | APIException e) {
                e.printStackTrace();
            }
            return null;
        }// end doInBackground

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (empty) {
                        mEmpty.setVisibility(View.VISIBLE);
                        mEmpty.setText(message);
                        mList.setVisibility(View.INVISIBLE);
                    } else {
                        mList.setAdapter(adapter);
                    }
                    progressBar.setVisibility(View.INVISIBLE);
                    progressLayout.setVisibility(View.GONE);
                }
            });
        }
    }// end SendCharge


    // We use an async task in order to configure stripe account
    class AddCard extends AsyncTask<String, String, String> {
        boolean error = false;
        String message;
        Customer cu = null;
        Card addedCard;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressLayout.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            // Adding card
            String idAccount = checkForUser();
            try {

                cu = Customer.retrieve(idAccount);
                Map<String, Object> cardParams = new HashMap<String, Object>();
                cardParams.put("name", completeName);
                cardParams.put("number", card);
                cardParams.put("exp_month", exp_month);
                cardParams.put("exp_year", Integer.parseInt("20" + exp_year));
                cardParams.put("cvc", cv);
                cardParams.put("source", idAccount);
                Map<String, Object> creationParams = new HashMap<String, Object>();
                creationParams.put("card", cardParams);

                addedCard = cu.createCard(creationParams);


            } catch (AuthenticationException | InvalidRequestException | APIConnectionException | APIException e) {
                error = true;
                message = e.getMessage();
            } catch (CardException e) {
                error = true;
                String code = e.getCode();
                switch (code) {
                    case "incorrect_number":
                        message = getString(R.string.incorrect_number);
                        break;
                    case "invalid_number":
                        message = getString(R.string.invalid_number);
                        break;
                    case "invalid_expiry_month":
                        message = getString(R.string.invalid_expiry_month);
                        break;
                    case "invalid_expiry_year":
                        message = getString(R.string.invalid_expiry_year);
                        break;
                    case "invalid_cvc":
                        message = getString(R.string.invalid_cvc);
                        break;
                    case "expired_card":
                        message = getString(R.string.expired_card);
                        break;
                    case "incorrect_cvc":
                        message = getString(R.string.incorrect_cvc);
                        break;
                    case "card_declined":
                        message = getString(R.string.card_declined);
                        break;
                    case "processing_error":
                        message = getString(R.string.processing_error);
                        break;
                    default:
                        message = getString(R.string.default_card_error);
                        break;
                }

            }
            return null;
        }// end doInBackground

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (error) {
                        // Display message and finishing activity
                        Toast.makeText(PaymentActivity.this, message, Toast.LENGTH_LONG).show();
                    } else {
                        ParseQuery<ParseUser> query = ParseQuery.getQuery(ParseConstants.CLASS_USER);
                        query.setLimit(1000);
                        query.findInBackground(new FindCallback<ParseUser>() {
                            public void done(List<ParseUser> users, ParseException e) {
                                if (e == null) {
                                    // The query was successful.
                                    boolean dataErrorStripe = false;
                                    for (ParseUser mUser : users) {
                                        List<String> stripe = mUser.getList(ParseConstants.KEY_USER_STRIPE_FINGERPRINT);

                                        if (stripe != null) {
                                            for (String data : stripe) {
                                                if (addedCard.getFingerprint().equals(data)) {
                                                    dataErrorStripe = true;
                                                    error = true;
                                                    message = getString(R.string.registered_card);
                                                    break;
                                                }
                                            }// end for
                                        }
                                    }// end for
                                    if (!dataErrorStripe) {
                                        new Thread(new Runnable() {
                                            public void run() {
                                                ParseUser currentUser = ParseUser.getCurrentUser();
                                                ArrayList<String> fingerprints = getFingerPrints(currentUser);
                                                fingerprints.add(addedCard.getFingerprint());
                                                currentUser.put(ParseConstants.KEY_USER_STRIPE_FINGERPRINT, fingerprints);
                                                currentUser.saveInBackground();
                                                BondzuApp.updateParseInstallation(currentUser);
                                            }
                                        }).start();
                                    } else {
                                        new Thread(new Runnable() {
                                            public void run() {
                                                try {
                                                    addedCard.delete();
                                                    // Display message and finishing activity
                                                } catch (AuthenticationException | InvalidRequestException | CardException | APIConnectionException | APIException e1) {
                                                    e1.printStackTrace();
                                                }
                                            }
                                        }).start();

                                    }// end else
                                    Toast.makeText(PaymentActivity.this, message, Toast.LENGTH_LONG).show();
                                    progressBar.setVisibility(View.INVISIBLE);
                                    progressLayout.setVisibility(View.GONE);
                                    new CheckAccount().execute();
                                }
                            }
                        });
                    }


                }
            });
        }
    }// end SendCharge


}
