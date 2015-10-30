package com.bondzu.bondzuapp.ui;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;

import com.bondzu.bondzuapp.R;
import com.bondzu.bondzuapp.adapters.ImagePageAdapter;
import com.bondzu.bondzuapp.utils.GeneralConstants;

public class SingleViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_view);
        // Selected image id
        int position = getIntent().getIntExtra(GeneralConstants.KEY_POS, 0);
        ArrayList<String> list = getIntent().getStringArrayListExtra(GeneralConstants.KEY_LIST);

        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(new ImagePageAdapter(getSupportFragmentManager(), this, list));
        pager.setCurrentItem(position);
    }

}
