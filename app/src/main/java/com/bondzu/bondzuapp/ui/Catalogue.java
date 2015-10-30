package com.bondzu.bondzuapp.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.bondzu.bondzuapp.R;
import com.bondzu.bondzuapp.adapters.CatalogueAdapter;
import com.bondzu.bondzuapp.utils.GeneralConstants;
import com.bondzu.bondzuapp.utils.ParseConstants;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class Catalogue extends Fragment {

    GridView catalogGrid;
    Context mContext;
    CatalogueAdapter adapter;
    private String grid_currentQuery = null; // holds the current query...


    final private SearchView.OnQueryTextListener queryListener = new SearchView.OnQueryTextListener() {

        @Override
        public boolean onQueryTextChange(String newText) {
            if (TextUtils.isEmpty(newText)) {
                grid_currentQuery = null;
            } else {
                grid_currentQuery = newText;
                adapter.getFilter().filter(newText);


            }
            return false;
        }

        @Override
        public boolean onQueryTextSubmit(String query) {
            adapter.getFilter().filter(query);
            return false;
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.activity_catalogue, container, false);

        catalogGrid = (GridView) rootView.findViewById(R.id.catalogue_grid);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(getActivity() == null) return;
        // Check Network
        if (!GeneralConstants.checkNetwork(getActivity()))
            GeneralConstants.showMessageConnection(getActivity());
        else {
            // Get animals
            getAnimals();
        }


    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_catalogue, menu);
        SearchView searchView = (SearchView)menu.findItem(R.id.search).getActionView();
        searchView.setOnQueryTextListener(queryListener);

    }



    private void getAnimals() {
        // Query to get animal cameras
        ParseQuery<ParseObject> query = ParseQuery.getQuery(ParseConstants.CLASS_ANIMALV2);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> animals, ParseException e) {

                if (e == null) {
                    if(getActivity() == null) return;
                    if (catalogGrid.getAdapter() == null) {
                        // Adding functionality to items on grid views
                        adapter = new CatalogueAdapter(getActivity(), animals);
                        catalogGrid.setAdapter(adapter);
                        catalogGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                ParseObject animal = (ParseObject) catalogGrid.getItemAtPosition(position);
                                Intent i = new Intent(getActivity(), AnimalProfile.class);
                                i.putExtra(ParseConstants.KEY_ANIMAL_ID, animal.getObjectId());
                                startActivity(i);
                            }
                        });
                    }
                    else {
                        ((CatalogueAdapter)catalogGrid.getAdapter()).refill(animals);
                    }


                } else {
                    if(getActivity() == null) return;
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage("Bad connection")
                            .setTitle(R.string.simple_error_title)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }

        });
    }


}
