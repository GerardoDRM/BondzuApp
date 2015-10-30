package com.bondzu.bondzuapp.adapters;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.List;

import com.bondzu.bondzuapp.R;
import com.bondzu.bondzuapp.view.NavDrawerItem;
import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * Created by gerardo on 7/08/15.
 */
public class NavigationDrawerAdapter extends RecyclerView.Adapter<NavigationDrawerAdapter.MyViewHolder> {
    List<NavDrawerItem> data = Collections.emptyList();
    private LayoutInflater inflater;
    private Context mContext;
    private TypedArray mImages;


    public NavigationDrawerAdapter(Context context, List<NavDrawerItem> data, TypedArray imgs) {
        this.mContext = context;
        inflater = LayoutInflater.from(context);
        this.data = data;
        this.mImages = imgs;
    }

    public void delete(int position) {
        data.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.nav_drawer_row, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        NavDrawerItem current = data.get(position);
        holder.title.setText(current.getTitle());
        Picasso.with(mContext).load(mImages.getResourceId(position, -1)).tag(mContext).into(holder.mIcon);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.title)
        TextView title;
        @Bind(R.id.icon_drawer)
        ImageView mIcon;

        public MyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}