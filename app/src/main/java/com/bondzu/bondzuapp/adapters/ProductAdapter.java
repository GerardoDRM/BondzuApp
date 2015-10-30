package com.bondzu.bondzuapp.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseObject;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.List;

import com.bondzu.bondzuapp.R;
import com.bondzu.bondzuapp.utils.ParseConstants;

/**
 * Created by gerardo on 12/08/15.
 */
public class ProductAdapter extends ArrayAdapter<ParseObject> {

    private Context mContext;
    private List<ParseObject> mProducts;

    public ProductAdapter(Context context,List<ParseObject> products) {
        super(context, R.layout.products_item, products);
        this.mContext = context;
        this.mProducts = products;
    }


    @Override
    public ParseObject getItem(int position) {
        return mProducts.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.products_item, null);
            holder = new ViewHolder();
            holder.cardView = (CardView) convertView.findViewById(R.id.product_card);
            holder.productPhoto = (ImageView) convertView.findViewById(R.id.product_card_photo);
            holder.productName = (TextView) convertView.findViewById(R.id.product_card_name);
            holder.productAmount = (TextView) convertView.findViewById(R.id.product_card_amount);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }


        ParseObject product = mProducts.get(position);


        // Get the image url and we use picasso to load the image
        String urlProduct = product.getParseFile(ParseConstants.KEY_PRODUCT_PHOTO).getUrl();
        Picasso.with(mContext).load(urlProduct).fit().centerCrop().tag(mContext).into(holder.productPhoto);

        // Get product name
        String name = product.getString(ParseConstants.getKeyProductName(mContext));
        holder.productName.setText(name);
        //  Get product amount
        DecimalFormat df = new DecimalFormat("#.00");
        Double price = product.getDouble(ParseConstants.KEY_PRODUCT_PRICE);
        holder.productAmount.setText("$"+price.toString());


        return convertView;
    }

    private static class ViewHolder {
        CardView cardView;
        ImageView productPhoto;
        TextView productName;
        TextView productAmount;
    }

    public void refill(List<ParseObject> products) {
        mProducts.clear();
        mProducts.addAll(products);
        notifyDataSetChanged();
    }
}
