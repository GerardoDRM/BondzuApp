package com.bondzu.bondzuapp.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import com.bondzu.bondzuapp.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 */
public class SingleViewFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_single_view, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        String resource = getArguments().getString("image");

        ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
        //imageView.setImageResource(imageAdapter.mThumbIds[position]);
        Picasso.with(getActivity()).load(resource).into(imageView);
    }
}