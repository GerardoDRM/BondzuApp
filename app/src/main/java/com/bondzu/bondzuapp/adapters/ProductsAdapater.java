package com.bondzu.bondzuapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.List;

import com.bondzu.bondzuapp.R;
import com.bondzu.bondzuapp.ui.CardDetailsActivity;
import com.bondzu.bondzuapp.ui.CompleteGiftsActivity;
import com.bondzu.bondzuapp.ui.ProductDescription;
import com.bondzu.bondzuapp.utils.GeneralConstants;
import com.bondzu.bondzuapp.utils.ParseConstants;

/**
 * Created by gerardo on 12/08/15.
 */
public class ProductsAdapater extends RecyclerView.Adapter<ProductsAdapater.ViewHolder> {

    private Context mContext;
    private List<ParseObject> mProducts;
    private ArrayList<String> mCategories;

    public ProductsAdapater(Context context, List<ParseObject> products, ArrayList<String> categories) {
        this.mContext = context;
        this.mProducts = products;
        this.mCategories = categories;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.products_grid, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // We bind the views with the data
        holder.bindProducts(mCategories.get(position), position);
    }

    @Override
    public int getItemCount() {
        return mCategories.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView categoryName;
        TextView categoryPhrase;
        GridView categoryGrid;
        Button btnMore;
        Button btnDonation;

        public ViewHolder(View itemView) {
            super(itemView);

            categoryName = (TextView) itemView.findViewById(R.id.category_title);
            categoryPhrase = (TextView) itemView.findViewById(R.id.category_phrase);
            categoryGrid = (GridView) itemView.findViewById(R.id.custom_products_grid);
            btnMore = (Button) itemView.findViewById(R.id.btn_more);
            btnDonation = (Button) itemView.findViewById(R.id.btn_donations);
        }

        public void bindProducts(final String category, int place) {

            // Add donation button
            if (place == 0) {
                btnDonation.setVisibility(View.VISIBLE);
                btnDonation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(mContext, CardDetailsActivity.class);
                        i.putExtra(ParseConstants.KEY_PRODUCT_ID, "1EhtID3J6X");
                        i.putExtra(ParseConstants.getKeyProductName(mContext), "Donacion");
                        i.putExtra(GeneralConstants.KEY_ACTIVITY_TRANSACTION, GeneralConstants.KEY_ACTIVITY_DONATION);
                        mContext.startActivity(i);
                    }
                });
            }

            // Change the category name and phrase in our UI
            categoryName.setText(category);
            categoryPhrase.setText("");

            // Get products
            ArrayList<ParseObject> products_list = new ArrayList<ParseObject>();
            int i = 0;
            for (final ParseObject p : mProducts) {
                // Check if a product match in the category
                if (p.getString(ParseConstants.getKeyProductCategory(mContext)).equals(category) && i < 5) {
                    products_list.add(p);
                    i++;
                }
                // If there are more than 5 products we add a more button
                if (i == 5) {
                    btnMore.setVisibility(View.VISIBLE);
                    btnMore.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(mContext, CompleteGiftsActivity.class);
                            i.putExtra(ParseConstants.getKeyProductCategory(mContext), category);
                            i.putExtra(ParseConstants.KEY_PRODUCT_ANIMAL_ID, p.getParseObject(ParseConstants.KEY_PRODUCT_ANIMAL_ID).getObjectId());
                            mContext.startActivity(i);
                        }
                    });
                }

            }// end for products

            if (categoryGrid.getAdapter() == null) {
                // Adding functionality to items on grid views
                ProductAdapter p = new ProductAdapter(mContext, products_list);
                categoryGrid.setAdapter(p);
                categoryGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        ParseObject product = (ParseObject) categoryGrid.getItemAtPosition(position);
                        Intent i = new Intent(mContext, ProductDescription.class);
                        i.putExtra(ParseConstants.KEY_PRODUCT_ID, product.getObjectId());
                        mContext.startActivity(i);
                    }
                });
            }
            else {
                ((ProductAdapter)categoryGrid.getAdapter()).refill(products_list);
            }

        }
    }
}
