package com.bondzu.bondzuapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.bondzu.bondzuapp.R;
import com.bondzu.bondzuapp.adapters.ViewPageAdapterHome;
import com.bondzu.bondzuapp.view.FragmentDrawer;
import com.bondzu.bondzuapp.view.SlidingTabLayout;
import com.parse.ParseUser;

public class Home extends AppCompatActivity implements FragmentDrawer.FragmentDrawerListener {

    ViewPager pager;
    CharSequence Titles[]={"ANIMALIA", "VIDEO"};
    int Numboftabs = 2;
    SlidingTabLayout tabs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FragmentDrawer drawerFragment = (FragmentDrawer) getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), mToolbar);
        drawerFragment.setDrawerListener(this);

        // Creating The ViewPagerAdapter and Passing Fragment Manager, Titles fot the Tabs and Number Of Tabs.
        ViewPageAdapterHome adapter = new ViewPageAdapterHome(getSupportFragmentManager(), Titles, Numboftabs);

        // Assigning ViewPager View and setting the adapter
        pager = (ViewPager) findViewById(R.id.pager_home);
        pager.setAdapter(adapter);

        // Adding the Sliding Tab Layout View
        tabs = (SlidingTabLayout) findViewById(R.id.tabs_home);
        tabs.setDistributeEvenly(true); // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available width

        // Setting Custom Color for the Scroll bar indicator of the Tab View
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabsScrollColor);
            }
        });

        // Setting the ViewPager For the SlidingTabsLayout
        tabs.setViewPager(pager);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDrawerItemSelected(View view, int position) {
        switch (position) {
            case 0:
                Intent home = new Intent(this, Home.class);
                home.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(home);
                break;
            case 1:
                Intent adoptions = new Intent(this, AdoptionsActivity.class);
                startActivity(adoptions);
                break;
            case 2:
                Intent history = new Intent(this, PaymentHistoryAcvity.class);
                startActivity(history);
                break;
            case 3:
                Intent payments = new Intent(this, PaymentActivity.class);
                startActivity(payments);
                break;
            case 4:
                ParseUser.logOut();
                Intent logout = new Intent(this, SignUp.class);
                logout.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                logout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(logout);
                break;
            default:
                break;
    }
    }
}
