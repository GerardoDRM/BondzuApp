package com.bondzu.bondzuapp.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.bondzu.bondzuapp.ui.AnimalProfileFragment;
import com.bondzu.bondzuapp.ui.CommunityFragment;
import com.bondzu.bondzuapp.ui.GalleryFragment;
import com.bondzu.bondzuapp.ui.GiftsFragment;

/**
 * Created by gerardo on 7/08/15.
 */
public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    CharSequence Titles[]; // This will Store the Titles of the Tabs which are Going to be passed when ViewPagerAdapter is created
    int NumbOfTabs; // Store the number of tabs, this will also be passed when the ViewPagerAdapter is created


    // Build a Constructor and assign the passed Values to appropriate values in the class
    public ViewPagerAdapter(FragmentManager fm, CharSequence mTitles[], int mNumbOfTabsumb) {
        super(fm);

        this.Titles = mTitles;
        this.NumbOfTabs = mNumbOfTabsumb;

    }

    //This method return the fragment for the every position in the View Pager
    @Override
    public Fragment getItem(int position) {

        if (position == 0) // if the position is 0 we are returning the First tab
        {
            return new AnimalProfileFragment();
        } else if (position == 1) {
            return new CommunityFragment();
        } else if (position == 2) {
            return new GalleryFragment();
        } else//
        {
            return new GiftsFragment();
        }


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