package com.bondzu.bondzuapp.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.stripe.exception.APIConnectionException;
import com.stripe.exception.APIException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.model.Card;
import com.stripe.model.Charge;
import com.stripe.model.Customer;
import com.stripe.model.ExternalAccount;
import com.stripe.model.ExternalAccountCollection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bondzu.bondzuapp.BondzuApp;
import com.bondzu.bondzuapp.R;
import com.bondzu.bondzuapp.utils.GeneralConstants;
import com.bondzu.bondzuapp.utils.ParseConstants;
import butterknife.Bind;
import butterknife.ButterKnife;

public class CardDetailsActivity extends AppCompatActivity {

    static Map<String, Object> cardParams = new HashMap<>();
    static Map<String, Object> chargeParams = new HashMap<>();

    @Bind(R.id.card_first_name)
    TextView firstName;
    @Bind(R.id.card_last_name)
    TextView lastName;
    @Bind(R.id.card_number)
    TextView cardNumber;
    @Bind(R.id.month_exp_date)
    TextView monthExp;
    @Bind(R.id.year_exp_date)
    TextView yearExp;
    @Bind(R.id.card_cv)
    TextView cvc;
    @Bind(R.id.card_amount)
    TextView amount;
    @Bind(R.id.card_charge_btn)
    FloatingActionButton chargeBtn;
    @Bind(R.id.fixed_card_amount)
    TextView fixedCardAmount;


    @Bind(R.id.il_card_number)
    TextInputLayout il_cardNumber;
    @Bind(R.id.il_card_amount)
    TextInputLayout ilCardAmount;
    @Bind(R.id.il_card_cv)
    TextInputLayout ilCvc;
    @Bind(R.id.group_fixed_card_amount)
    LinearLayout layoutFixedAmount;


    private int PESOS;
    private String productRef;
    private String productName;
    private String idCharge;
    private String description;
    private Integer completeAmount;

    private ParseUser mUser;
    String accountId = null;
    private boolean hasCard = false;
    private Card mCardInfo;

    @Bind(R.id.progressBar)
    ProgressBar progressBar;

    public CardDetailsActivity() {
        PESOS = 100;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_details);
        ButterKnife.bind(this);
        // Get current User
        mUser = ParseUser.getCurrentUser();

        // Adding material design toolbar
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setNavigationIcon(ContextCompat.getDrawable(this, R.mipmap.ic_close_white_24dp));
        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get data from productsetNavigationIcon
        productRef = getIntent().getExtras().getString(ParseConstants.KEY_PRODUCT_ID);
        productName = getIntent().getExtras().getString(ParseConstants.getKeyProductName(this));
        String activityRef = getIntent().getExtras().getString(GeneralConstants.KEY_ACTIVITY_TRANSACTION);

        assert activityRef != null;
        if (activityRef.equals(GeneralConstants.KEY_ACTIVITY_DONATION)) {
            ilCardAmount.setVisibility(View.VISIBLE);
        } else if (activityRef.equals(GeneralConstants.KEY_ACTIVITY_PURCHASE)) {
            Integer fixedAmount = Integer.parseInt(getIntent().getExtras().getString(ParseConstants.KEY_PRODUCT_PRICE));
            fixedCardAmount.setText(fixedAmount.toString());
            layoutFixedAmount.setVisibility(View.VISIBLE);
        }


        // Executing a charge
        chargeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get card details
                if (hasCard) {
                    boolean error;
                    error = addingDefaultDetails();
                    if (!error)
                        new SendCharge().execute();
                } else {
                    boolean error;
                    error = addingCardDetails();
                    if (!error)
                        new AddCard().execute();
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
            // Check if this user has an account
            checkForUser();
        }
    }

    private boolean addingDefaultDetails() {

        // Add charge params
        // Cents to pesos conversion
        String money = checkAmount();
        if(money.isEmpty() || money.equals("")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.card_details_verification)
                    .setTitle(R.string.card_error_title
                    )
                    .setPositiveButton(android.R.string.ok, null);
            AlertDialog dialog = builder.create();
            dialog.show();
            return true;
        }
        completeAmount = PESOS * Integer.parseInt(money);
        chargeParams.put("amount", completeAmount);
        chargeParams.put("currency", "mxn");
        chargeParams.put("customer", accountId);
        description = getString(R.string.charge_description) + productName + " - Boundzu";
        chargeParams.put("description", description);
        return false;
    }

    /**
     * This function checks if a user has an account
     * on Stripe and return stripe id
     *
     * @return String
     */
    private void checkForUser() {
        String stripe = mUser.getString(ParseConstants.KEY_USER_STRIPE);
        accountId = stripe;
        checkForCards();
    }

    /**
     *  This method get all cards from user
     *  and display each card on UI,
     *  then the user can choice one option
     */
    private void checkForCards() {
        final List<String> cards = mUser.getList(ParseConstants.KEY_USER_STRIPE_FINGERPRINT);
        if(cards != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final CharSequence[] cards_name = new CharSequence[cards.size()];
                    Customer currentCustomer = null;
                    try {
                        currentCustomer = Customer.retrieve(accountId);
                        // Check Register Cards
                        Map<String, Object> cardParams = new HashMap<String, Object>();
                        cardParams.put("object", "card");
                        ExternalAccountCollection data_cards = currentCustomer.getSources().all(cardParams);
                        final List<ExternalAccount> cards_list = data_cards.getData();
                        // Get cameras info
                        int i= 0;
                        for (ExternalAccount card: cards_list) {
                            Card c = (Card) card;
                            cards_name[i] = "************" + c.getLast4() + " " + c.getBrand();
                            i++;
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                final int[] position = {0};
                                AlertDialog.Builder builder = new AlertDialog.Builder(CardDetailsActivity.this);
                                builder.setTitle(R.string.select_card)
                                        .setSingleChoiceItems(cards_name, 0, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                position[0] = which;
                                            }
                                        })
                                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                finish();
                                            }
                                        })
                                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                for (ExternalAccount card: cards_list) {
                                                    if(((Card) card).getFingerprint().equals(((Card) cards_list.get(position[0])).getFingerprint())){
                                                        mCardInfo = (Card) card;
                                                        uploadCardDetails((Card)card);
                                                        hasCard = true;
                                                    }
                                                }
                                            }
                                        });
                                AlertDialog dialog = builder.create();
                                dialog.setCanceledOnTouchOutside(false);
                                dialog.setCancelable(false);
                                dialog.show();
                            }
                        });

                    } catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException | APIException e) {
                        e.printStackTrace();
                    }

                }
            }).start();
        }

    }

    /**
     * This method will upload the UI with
     * all card data
     * @param card
     */
    private void uploadCardDetails(Card card) {
        // Auto fill all edit text
        // enable false because we don't want changes
        String[] completeName = parseString(card.getName());
        firstName.setText(completeName[0]);
        firstName.setEnabled(false);
        lastName.setText(completeName[1]);
        lastName.setEnabled(false);
        cardNumber.setText("************" + card.getLast4());
        cardNumber.setEnabled(false);
        monthExp.setText(card.getExpMonth().toString());
        monthExp.setEnabled(false);
        String year = card.getExpYear().toString();
        yearExp.setText(year.length() > 2 ? year.substring(year.length() - 2) : year);
        yearExp.setEnabled(false);
        ilCvc.setVisibility(View.INVISIBLE);
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



    private boolean addingCardDetails() {
        String firstN = firstName.getText().toString().trim();
        String lastN = lastName.getText().toString().trim();
        String cardNum = cardNumber.getText().toString().trim();
        String month = monthExp.getText().toString();
        String year = yearExp.getText().toString().trim();
        String cv = cvc.getText().toString();
        String money = checkAmount();

        description = "Gifts / Donations - " + productName + " - Boundzu";

        if (firstN.isEmpty() || lastN.isEmpty() || cardNum.isEmpty() || month.isEmpty() ||
                year.isEmpty() || cv.isEmpty() || money.isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.card_details_verification)
                    .setTitle(R.string.card_error_title
                    )
                    .setPositiveButton(android.R.string.ok, null);
            AlertDialog dialog = builder.create();
            dialog.show();
            return true;
        } else {
            String completeName = firstN + " " + lastN;
            // Adding card data
            cardParams.put("name", completeName);
            cardParams.put("number", cardNum);
            cardParams.put("exp_month", month);
            cardParams.put("exp_year", Integer.parseInt("20" + year));
            cardParams.put("cvc", cv);
            // Charge data
            // Cents to pesos conversion
            completeAmount = PESOS * Integer.parseInt(money);
            chargeParams.put("amount", completeAmount);
            chargeParams.put("currency", "mxn");
            chargeParams.put("description", description);
            chargeParams.put("customer", accountId);
            return false;
        }
    }

    public ArrayList<String> getFingerPrints(ParseUser user) {
        List<String> likes = user.getList(ParseConstants.KEY_USER_STRIPE_FINGERPRINT);
        if (likes != null) {
            return (ArrayList<String>) likes;
        }
        else {
            return new ArrayList<String>();
        }
    }

    private String checkAmount() {
        String money;
        if (ilCardAmount.getVisibility() == View.VISIBLE)
            money = amount.getText().toString().trim();
        else
            money = fixedCardAmount.getText().toString().trim();

        return money;
    }

    // We use an async task in order to configure stripe account
    class AddCard extends AsyncTask<String, String, String> {
        boolean error = false;
        String message;

        @Override
        protected String doInBackground(String... params) {
            try {
                Customer cu  = Customer.retrieve(accountId);
                cardParams.put("source", accountId);
                Map<String, Object> creationParams = new HashMap<String, Object>();
                creationParams.put("card", cardParams);
                mCardInfo = cu.createCard(creationParams);

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
                        il_cardNumber.setError(message);
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
                                                if (mCardInfo.getFingerprint().equals(data)) {
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
                                                fingerprints.add(mCardInfo.getFingerprint());
                                                currentUser.put(ParseConstants.KEY_USER_STRIPE_FINGERPRINT, fingerprints);
                                                currentUser.saveInBackground();
                                                BondzuApp.updateParseInstallation(currentUser);

                                                new SendCharge().execute();
                                            }
                                        }).start();
                                    } else {
                                        new Thread(new Runnable() {
                                            public void run() {
                                                try {
                                                    mCardInfo.delete();
                                                    il_cardNumber.setError(message);
                                                    // Display message and finishing activity
                                                } catch (AuthenticationException | InvalidRequestException | CardException | APIConnectionException | APIException e1) {
                                                    e1.printStackTrace();
                                                }
                                            }
                                        }).start();

                                    }// end else
                                }
                            }
                        });
                    }
                }
            });
        }
    }// end SendCharge




    // We use an async task in order to send the charge to stripe
    class SendCharge extends AsyncTask<String, String, String> {
        Charge charge;
        String message;
        boolean error = false;

        @Override
        protected String doInBackground(String... params) {
            try {
                Customer customer = Customer.retrieve(accountId);
                Map<String, Object> updateParams = new HashMap<String, Object>();
                updateParams.put("default_source", mCardInfo.getId());
                customer.update(updateParams);

                charge = Charge.create(chargeParams);

                if (charge.getStatus().equals("succeeded")) {
                    message = getString(R.string.thanks_for_helping);
                    idCharge = charge.getId();
                    saveTransaction();
                }// end if
                else {
                    error = true;
                    message = getString(R.string.incomplete_charge);
                }

            } catch (AuthenticationException | InvalidRequestException | APIConnectionException | APIException | CardException e) {
                error = true;
                message = e.getMessage();
            }
            return null;
        }// end doInBackground

        protected void onPostExecute(String file_url) {
            runOnUiThread(new Runnable() {
                public void run() {
                    if (error)
                        il_cardNumber.setError(message);
                    else {
                        // Display message and finishing activity
                        AlertDialog.Builder builder = new AlertDialog.Builder(CardDetailsActivity.this);
                        builder.setMessage(message)
                                .setTitle(R.string.success)
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                });
                        AlertDialog dialog = builder.create();
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.show();
                    }
                }
            });

        } // end onPostExecute

    }// end SendCharge

    /*
     * This method save the transaction object on Parse
     */
    private void saveTransaction() {
        // Create an transaction object
        final ParseObject transaction = new ParseObject(ParseConstants.CLASS_TRANSACTIONS);
        transaction.put(ParseConstants.KEY_TRANSACTION_ID, idCharge);
        transaction.put(ParseConstants.KEY_TRANSACTION_USER_ID, mUser);
        ParseObject productId = ParseObject.createWithoutData(ParseConstants.CLASS_PRODUCT, productRef);
        transaction.put(ParseConstants.KEY_TRANSACTION_PRODUCT_ID, productId);
        transaction.put(ParseConstants.KEY_TRANSACTION_DESCRIPTION, description);
        transaction.put(ParseConstants.KEY_TRANSACTION_AMOUNT, completeAmount / 100);
        transaction.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    ParseRelation<ParseObject>  relation = ParseUser.getCurrentUser().getRelation(ParseConstants.KEY_USER_TRANSACTIONS);
                    relation.add(transaction);
                    ParseUser.getCurrentUser().saveInBackground();
                    BondzuApp.updateParseInstallation(ParseUser.getCurrentUser());
                }
            }
        });



    } // end saveTransaction

}
