package com.bondzu.bondzuapp.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

import com.bondzu.bondzuapp.R;
import com.bondzu.bondzuapp.adapters.GalleryGridAdapter;
import com.bondzu.bondzuapp.utils.GeneralConstants;
import com.bondzu.bondzuapp.utils.ParseConstants;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 */
public class GalleryFragment extends Fragment {

    GridView mGrid;
    ParseObject animalObject;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_gallery, container, false);

        // Recycler view data
        mGrid = (GridView) rootView.findViewById(R.id.grid);

        // Get animal reference
        final String animalRef = getActivity().getIntent().getExtras().getString(ParseConstants.KEY_ANIMAL_ID);
        animalObject = ParseObject.createWithoutData(ParseConstants.CLASS_ANIMALV2, animalRef);


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
            // Get all gallery photos
            getGallery(animalObject);
        }
    }

    private void getGallery(ParseObject animalObject) {
        // Query to get animal cameras
        ParseQuery<ParseObject> query = ParseQuery.getQuery(ParseConstants.CLASS_GALLERY);
        query.whereEqualTo(ParseConstants.KEY_GALLERY_ANIMAL, animalObject);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(final List<ParseObject> photo, ParseException e) {

                if (e == null) {
                    if (mGrid.getAdapter() == null) {
                        if(getActivity() == null) return;
                        // Adding functionality to items on grid views
                        GalleryGridAdapter adapter = new GalleryGridAdapter(getActivity(), photo);
                        mGrid.setAdapter(adapter);
                        mGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                ArrayList<String> photos_url = new ArrayList<String>();
                                for (ParseObject p : photo) {
                                    photos_url.add(p.getParseFile(ParseConstants.KEY_GALLERY_FILE).getUrl());
                                }
                                Intent i = new Intent(getActivity(), SingleViewActivity.class);
                                i.putExtra(GeneralConstants.KEY_POS, position);
                                i.putStringArrayListExtra(GeneralConstants.KEY_LIST, photos_url);
                                startActivity(i);
                            }
                        });
                    } else {
                        ((GalleryGridAdapter) mGrid.getAdapter()).refill(photo);
                    }
                } else {
                    if (getActivity() == null) return;
                    String message = "Bad Internet Connection";
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    message = e.getMessage();
                    builder.setMessage(message)
                            .setTitle(R.string.simple_error_title)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }

        });
    }
}
