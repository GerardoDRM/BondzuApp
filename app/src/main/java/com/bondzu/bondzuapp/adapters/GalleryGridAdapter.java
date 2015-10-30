package com.bondzu.bondzuapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.parse.ParseObject;
import com.squareup.picasso.Picasso;

import java.util.List;

import com.bondzu.bondzuapp.R;
import com.bondzu.bondzuapp.utils.ParseConstants;

/**
 * Created by Gerardo de la Rosa on 11/09/15.
 */
public class GalleryGridAdapter extends ArrayAdapter<ParseObject> {
    private Context mContext;
    private List<ParseObject> mItem;

    public GalleryGridAdapter(Context context, List<ParseObject> item) {
        super(context, R.layout.gallery_grid_item, item);
        this.mContext = context;
        this.mItem = item;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.gallery_grid_item, null);
            holder = new ViewHolder();
            holder.animalPhoto = (ImageView) convertView.findViewById(R.id.imageView);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();

        }
        ParseObject gallery = mItem.get(position);

        // Get the image url and we use picasso to load the image
        String urlProduct = gallery.getParseFile(ParseConstants.KEY_GALLERY_FILE).getUrl();
        Picasso.with(mContext).load(urlProduct).fit().centerCrop().tag(mContext).into(holder.animalPhoto);

        return convertView;

    }

    private static class ViewHolder {
        ImageView animalPhoto;
    }

    public void refill(List<ParseObject> animals) {
        mItem.clear();
        mItem.addAll(animals);
        notifyDataSetChanged();
    }
}