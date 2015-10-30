package com.bondzu.bondzuapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.bondzu.bondzuapp.R;
import com.bondzu.bondzuapp.utils.ParseConstants;
import com.parse.ParseObject;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by gerardo on 2/09/15.
 */
public class CatalogueAdapter extends ArrayAdapter<ParseObject> implements Filterable{
    private Context mContext;
    private List<ParseObject> mAnimals;
    private ParseObject dataTest [];

    Filter myFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            ArrayList<ParseObject> tempList=new ArrayList<ParseObject>();
            //constraint is the result from text you want to filter against.
            //objects is your data set you will filter from
            if(constraint != null && dataTest!=null) {

                for(ParseObject animal : dataTest) {

                    Pattern pattern = Pattern.compile(constraint.toString().toLowerCase());
                    Matcher matcherName = pattern.matcher(animal.getString(ParseConstants.getKeyAnimalv2Name(mContext)).toLowerCase());
                    Matcher matcherSpecie = pattern.matcher(animal.getString(ParseConstants.getKeyAnimalSpecies(mContext)).toLowerCase());

                    if (matcherSpecie.find() || matcherName.find()) {
                        tempList.add(animal);
                    }

                }
                //following two lines is very important
                //as publish result can only take FilterResults objects
                filterResults.values = tempList;
                filterResults.count = tempList.size();
            }

            return filterResults;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence contraint, FilterResults results) {
            mAnimals.clear();
            mAnimals.addAll((ArrayList<ParseObject>) results.values);

            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    };


    public CatalogueAdapter(Context context, List<ParseObject> animals) {
        super(context, R.layout.catalogue_item, animals);
        this.mContext = context;
        this.mAnimals = animals;
        dataTest = new ParseObject[animals.size()];
        for(int i=0; i<animals.size(); i++) {
            this.dataTest[i]= animals.get(i);
        }
        Picasso picasso = Picasso.with(mContext);

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.catalogue_item, null);
            holder = new ViewHolder();
            holder.animalPhoto = (ImageView) convertView.findViewById(R.id.img_animal_catalogue);
            holder.animalName = (TextView) convertView.findViewById(R.id.animal_name_catalogue);
            holder.animalSpecie = (TextView) convertView.findViewById(R.id.specie_name_catalogue);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();

        }
        if(mAnimals.size() > 0) {
            ParseObject animal = mAnimals.get(position);

            // Get the image url and we use picasso to load the image
            if(animal.getParseFile(ParseConstants.KEY_ANIMAL_PHOTO) != null) {
                String urlProduct = animal.getParseFile(ParseConstants.KEY_ANIMAL_PHOTO).getUrl();
                Picasso.with(mContext).load(urlProduct).fit().centerCrop().tag(mContext).into(holder.animalPhoto);
            }
            holder.animalName.setText(animal.getString(ParseConstants.getKeyAnimalv2Name(mContext)));
            holder.animalSpecie.setText(animal.getString(ParseConstants.getKeyAnimalSpecies(mContext)));

        }

        return convertView;

    }

    private static class ViewHolder {
        ImageView animalPhoto;
        TextView animalName;
        TextView animalSpecie;
    }

    public void refill(List<ParseObject> animals) {
        mAnimals.clear();
        mAnimals.addAll(animals);
        notifyDataSetChanged();
    }


    @Override
    public Filter getFilter() {
        return myFilter;
    }

}
