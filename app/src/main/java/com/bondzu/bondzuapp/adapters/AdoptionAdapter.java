package com.bondzu.bondzuapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseObject;
import com.squareup.picasso.Picasso;

import java.util.List;

import com.bondzu.bondzuapp.R;
import com.bondzu.bondzuapp.ui.AnimalProfile;
import com.bondzu.bondzuapp.utils.CircleTransform;
import com.bondzu.bondzuapp.utils.ParseConstants;
import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by gerardo on 6/09/15.
 */
public class AdoptionAdapter extends RecyclerView.Adapter<AdoptionAdapter.ViewHolder> {
    private Context mContext;
    private List<ParseObject> mAnimals;

    public AdoptionAdapter(Context context, List<ParseObject> animals) {
        this.mContext = context;
        this.mAnimals = animals;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.adoption_item, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(mAnimals.get(position));
    }

    @Override
    public int getItemCount() {
        return mAnimals.size();
    }


    public void refill(List<ParseObject> list) {
        mAnimals.clear();
        mAnimals.addAll(list);
        notifyDataSetChanged();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        @Bind(R.id.animal_adoption_photo)
        ImageView mPhoto;
        @Bind(R.id.animal_adoption_tile)
        TextView mTitle;
        @Bind(R.id.animal_adoption_description)
        TextView mDescription;

        String mAnimal;


        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        public void bind(ParseObject event) {
            mAnimal = event.getObjectId();
            // Adding event photo
            Picasso.with(mContext).load(event.getParseFile(ParseConstants.KEY_ANIMAL_PHOTO).getUrl())
                    .transform(new CircleTransform()).fit().centerCrop().tag(mContext).into(mPhoto);
            // Adding event title
            mTitle.setText(event.getString(ParseConstants.getKeyAnimalv2Name(mContext)));
            // Adding event description
            mDescription.setText(event.getString(ParseConstants.getKeyAnimalSpecies(mContext)));

        }

        @Override
        public void onClick(View v) {
            Intent animal = new Intent(mContext, AnimalProfile.class);
            animal.putExtra(ParseConstants.KEY_ANIMAL_ID, mAnimal);
            mContext.startActivity(animal);
        }
    }
}
