package com.bondzu.bondzuapp.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import com.bondzu.bondzuapp.ui.Catalogue;
import com.bondzu.bondzuapp.ui.VideoCapsuleFragment;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gerardo on 3/09/15.
 */
public class ViewPageAdapterHome extends FragmentStatePagerAdapter {

    CharSequence Titles[]; // This will Store the Titles of the Tabs which are Going to be passed when ViewPagerAdapter is created
    int NumbOfTabs; // Store the number of tabs, this will also be passed when the ViewPagerAdapter is created
    private Map<Integer, Fragment> mPageReferenceMap = new HashMap<Integer, Fragment>();


    // Build a Constructor and assign the passed Values to appropriate values in the class
    public ViewPageAdapterHome(FragmentManager fm, CharSequence mTitles[], int mNumbOfTabsumb) {
        super(fm);

        this.Titles = mTitles;
        this.NumbOfTabs = mNumbOfTabsumb;

    }

    //This method return the fragment for the every position in the View Pager
    @Override
    public Fragment getItem(int position) {

        if (position == 0) // if the position is 0 we are returning the First tab
        {   Catalogue catalogue = new Catalogue();
            mPageReferenceMap.put(0, catalogue);
            return catalogue;
        } else {
            VideoCapsuleFragment videoCapsuleFragment = new VideoCapsuleFragment();
            mPageReferenceMap.put(1, videoCapsuleFragment);
            return videoCapsuleFragment;
        }
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
        mPageReferenceMap.remove(position);
    }

    // This method return the titles for the Tabs in the Tab Strip

    @Override
    public CharSequence getPageTitle(int position) {
        return Titles[position];
    }

    // This method return the Number of tabs for the tabs Strip

    @Override
    public int getCount() {
        return NumbOfTabs;
    }

}