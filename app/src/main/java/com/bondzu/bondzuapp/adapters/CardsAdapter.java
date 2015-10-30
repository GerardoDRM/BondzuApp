package com.bondzu.bondzuapp.adapters;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;
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

import com.bondzu.bondzuapp.R;
import com.bondzu.bondzuapp.ui.PaymentActivity;
import com.bondzu.bondzuapp.utils.ParseConstants;
import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by gerardo on 7/09/15.
 */
public class CardsAdapter extends RecyclerView.Adapter<CardsAdapter.ViewHolder> {
private Context mContext;
private List<ExternalAccount> mList;

    public CardsAdapter(Context context, List<ExternalAccount> cards) {
            this.mContext = context;
            this.mList = cards;
    }

    @Override
    public CardsAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.card_items, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CardsAdapter.ViewHolder holder, int position) {
        holder.bind((Card) mList.get(position));
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.card_last_numbers)
        TextView mNumbers;
        @Bind(R.id.card_date)
        TextView mDate;
        @Bind(R.id.btn_delete_Card)
        TextView mDelete;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
        public void bind(final Card item) {
            mNumbers.setText("************" + item.getLast4());
            mDate.setText(item.getExpMonth() + "/" + item.getExpYear());
            mDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new DeleteCard(item).execute();
                }
            });
        }
    }

    // We use an async task in order to configure stripe account
    class DeleteCard extends AsyncTask<String, String, String> {
        boolean empty = false;
        private Card mCard;
        String mFingerPrint;
        public DeleteCard(Card item) {
            this.mCard = item;
        }

        @Override
        protected String doInBackground(String... params) {
            // Retrieve Account data
            try {
                mFingerPrint = mCard.getFingerprint();
                mCard.delete();
            } catch (AuthenticationException | APIException | APIConnectionException | InvalidRequestException | CardException e) {
                e.printStackTrace();
            }
            return null;

        }// end doInBackground

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            ((PaymentActivity)mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ParseUser currentUser = ParseUser.getCurrentUser();
                    ArrayList<String> fingerprints = getFingerPrints(currentUser);
                    fingerprints.remove(mFingerPrint);
                    currentUser.put(ParseConstants.KEY_USER_STRIPE_FINGERPRINT, fingerprints);
                    currentUser.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            new UpdateView().execute();
                        }
                    });

                }
            });
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
    }

    // We use an async task in order to configure stripe account
    public class UpdateView extends AsyncTask<String, String, String> {
        CardsAdapter adapter;

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
                if (data != null) {
                    List<ExternalAccount> mCardsList = data.getData();
                    mList.clear();
                    mList.addAll(mCardsList);
                }
            } catch (AuthenticationException | InvalidRequestException | CardException | APIConnectionException | APIException e) {
                e.printStackTrace();
            }
            return null;
        }// end doInBackground

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            ((PaymentActivity)mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    notifyDataSetChanged();
                }
            });
        }
    }// end SendCharge
}
