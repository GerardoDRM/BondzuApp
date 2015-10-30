package com.bondzu.bondzuapp.adapters;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.bondzu.bondzuapp.view.SingleViewFragment;

import java.util.ArrayList;

/**
 * Created by Gerardo de la Rosa on 11/09/15.
 */
public class ImagePageAdapter extends FragmentStatePagerAdapter {

    private Context mContext;
    private ArrayList<String> mList;

    public ImagePageAdapter(FragmentManager fm, Context context, ArrayList<String> list) {
        super(fm);
        this.mContext = context;
        this.mList = list;
    }


    @Override
    public Fragment getItem(int i) {
        Bundle args = new Bundle();
        args.putString("image", mList.get(i));
        SingleViewFragment fragment = new SingleViewFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public int getCount() {
        return mList.size();
    }
}
