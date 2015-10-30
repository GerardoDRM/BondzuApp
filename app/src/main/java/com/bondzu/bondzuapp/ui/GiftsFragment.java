package com.bondzu.bondzuapp.ui;


import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

import com.bondzu.bondzuapp.R;
import com.bondzu.bondzuapp.adapters.ProductsAdapater;
import com.bondzu.bondzuapp.utils.GeneralConstants;
import com.bondzu.bondzuapp.utils.ParseConstants;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class GiftsFragment extends Fragment {

    private ArrayList<String> mCategories;
    RecyclerView mReciclyeView;
    ParseObject animalReference;
    private String animalRef;

    public GiftsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_gifts, container, false);
        ButterKnife.bind(getActivity());

        mCategories = new ArrayList<String>();

        mReciclyeView = (RecyclerView) rootView.findViewById(R.id.recyclerViewProducts);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mReciclyeView.setLayoutManager(layoutManager);
        mReciclyeView.setHasFixedSize(true);


        // Get data from animal
        // Restore value to get data
        animalRef = getActivity().getIntent().getExtras().getString(ParseConstants.KEY_ANIMAL_ID);
        // Create local instance of animal object
        animalReference = ParseObject.createWithoutData(ParseConstants.CLASS_ANIMALV2, animalRef);


        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Check Network
        if(getActivity() == null) return;
        if (!GeneralConstants.checkNetwork(getActivity()))
            GeneralConstants.showMessageConnection(getActivity());
        else {
            // Get all gifts
            getGifts();
        }
    }

    private void getGifts() {
        // Query to get animal cameras
        ParseQuery<ParseObject> query = ParseQuery.getQuery(ParseConstants.CLASS_PRODUCT);
        query.whereEqualTo(ParseConstants.KEY_PRODUCT_ANIMAL_ID, animalReference);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {

                if (e == null) {
                    if (getActivity() == null) return;
                    for (ParseObject p : list) {
                        if (!p.getString(ParseConstants.getKeyProductName(getActivity())).equals(ParseConstants.KEY_PRODUCT_FIELD_DONATION)
                                && !mCategories.contains(p.getString(ParseConstants.getKeyProductCategory(getActivity()))))
                            mCategories.add(p.getString(ParseConstants.getKeyProductCategory(getActivity())));
                    }

                    // Create an adapter for recycler view
                    ProductsAdapater adapter = new ProductsAdapater(getActivity(), list, mCategories);
                    mReciclyeView.setAdapter(adapter);


                } else {
                    if (getActivity() == null) return;
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage(e.getMessage())
                            .setTitle(R.string.simple_error_title)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }

        });
    }
}
