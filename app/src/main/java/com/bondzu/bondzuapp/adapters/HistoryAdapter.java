package com.bondzu.bondzuapp.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseObject;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.List;

import com.bondzu.bondzuapp.R;
import com.bondzu.bondzuapp.utils.CircleTransform;
import com.bondzu.bondzuapp.utils.ParseConstants;
import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by gerardo on 6/09/15.
 */
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
private Context mContext;
private List<ParseObject> mList;

    public HistoryAdapter(Context context, List<ParseObject> transactions) {
            this.mContext = context;
            this.mList = transactions;
            }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.history_item, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(mList.get(position));
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void refill(List<ParseObject> list) {
        mList.clear();
        mList.addAll(list);
        notifyDataSetChanged();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.transaction_product_photo)
        ImageView mPhoto;
        @Bind(R.id.transaction_product_name)
        TextView mTitle;
        @Bind(R.id.transaction_product_description)
        TextView mDescription;
        @Bind(R.id.card_date)
        TextView mDate;
        @Bind(R.id.transaction_amount)
        TextView mAmount;


        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(ParseObject item) {
            // Adding event photo
            Picasso.with(mContext).load(item.getParseObject(ParseConstants.KEY_TRANSACTION_PRODUCT_ID)
                    .getParseFile(ParseConstants.KEY_PRODUCT_PHOTO).getUrl()).transform(new CircleTransform())
                    .fit().centerCrop().tag(mContext).into(mPhoto);
            // Adding event title
            mTitle.setText(item.getParseObject(ParseConstants.KEY_TRANSACTION_PRODUCT_ID)
                    .getString(ParseConstants.getKeyProductName(mContext)));
            // Adding event description
            mDescription.setText(item.getString(ParseConstants.KEY_TRANSACTION_DESCRIPTION));
            // Adding amount
            mAmount.setText("$"+ String.valueOf(item.getInt(ParseConstants.KEY_TRANSACTION_AMOUNT)));
            // Adding Date
            // Format the date to Strings
            SimpleDateFormat format = new SimpleDateFormat(mContext.getString(R.string.date_format));
            String date = format.format(item.getCreatedAt());
            mDate.setText(date);
        }
    }
    }
