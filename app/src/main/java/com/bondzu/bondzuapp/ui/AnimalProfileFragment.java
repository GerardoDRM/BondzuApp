package com.bondzu.bondzuapp.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bondzu.bondzuapp.R;
import com.bondzu.bondzuapp.utils.GeneralConstants;
import com.bondzu.bondzuapp.utils.ParseConstants;
import com.bondzu.bondzuapp.youtube.FullScreenActivity;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 */
public class AnimalProfileFragment extends Fragment {

    @Bind(R.id.photo_animal_profile)
    ImageView photo;
    @Bind(R.id.animal_title)
    TextView animalName;
    @Bind(R.id.text_about_animal)
    TextView textAbout;
    @Bind(R.id.characteristics_layout_container)
    LinearLayout mLayoutCharacteristics;

    // Events
    @Bind(R.id.event_title)
    TextView eventTitle;
    @Bind(R.id.event_date)
    TextView eventDate;
    @Bind(R.id.event_description)
    TextView eventDescription;
    @Bind(R.id.img_event)
    ImageView imgEvent;
    @Bind(R.id.wrapper_events)
    LinearLayout mWrapperEvent;
    @Bind(R.id.no_events_label)
    TextView mNotEvents;
    @Bind(R.id.animal_event)
    CardView mEventCard;

    // Counter
    @Bind(R.id.label_counter)
    TextView labelCounter;
    @Bind(R.id.counter_adopters)
    TextView mCounter;

    // Keeper
    @Bind(R.id.keepers_layout_container)
    LinearLayout mLayoutKeeper;
    // Go live
    @Bind(R.id.go_live)
    TextView mGoLive;
    // Adoption button
    @Bind(R.id.float_btn_paw)
    FloatingActionButton mAdoption;
    @Bind(R.id.float_btn_bio_car)
    FloatingActionButton mDonation;


    private ParseUser mCurrentUser;
    private ParseRelation<ParseObject> mAdopter;
    private String animalRef;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_animal_profile, container, false);
        ButterKnife.bind(this, rootView);

        // Get data from animal
        // Restore value to get data
        animalRef = getActivity().getIntent().getExtras().getString(ParseConstants.KEY_ANIMAL_ID);

        // Add donation action
        mDonation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), CardDetailsActivity.class);
                i.putExtra(ParseConstants.KEY_PRODUCT_ID, GeneralConstants.KEY_ID_DONATION);
                i.putExtra(ParseConstants.getKeyProductName(getActivity()), GeneralConstants.KEY_CONSTANT_DONATION);
                i.putExtra(GeneralConstants.KEY_ACTIVITY_TRANSACTION, GeneralConstants.KEY_ACTIVITY_DONATION);
                getActivity().startActivity(i);
            }
        });


        // Set Go Live
        mGoLive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!animalRef.equals(ParseConstants.KEY_PREDATOR)) {
                    Intent i = new Intent(getActivity(), LiveStreamingActivity.class);
                    i.putExtra(ParseConstants.KEY_ANIMAL_ID, animalRef);
                    startActivity(i);
                }
                else {
                    // Query to get predator video
                    // Human has a video instead of live cam
                    ParseObject animal = ParseObject.createWithoutData(ParseConstants.CLASS_ANIMALV2, ParseConstants.KEY_PREDATOR);
                    ParseQuery<ParseObject> query = ParseQuery.getQuery(ParseConstants.CLASS_VIDEO);
                    query.whereEqualTo(ParseConstants.KEY_VIDEO_ANIMAL, animal);
                    query.getFirstInBackground(new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject video, ParseException e) {
                            if (e == null) {
                                String youtube = (String) video.getList(ParseConstants.KEY_VIDEO_YOUTUBE).get(0);
                                Intent i = new Intent(getActivity(), FullScreenActivity.class);
                                i.putExtra(ParseConstants.KEY_VIDEO_YOUTUBE, youtube);
                                startActivity(i);
                            }
                        }
                    });
                }
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Check Network
        if (getActivity() == null) return;
        if (!GeneralConstants.checkNetwork(getActivity()))
            GeneralConstants.showMessageConnection(getActivity());
        else {
            // Get current user
            mCurrentUser = ParseUser.getCurrentUser();

            if(!animalRef.equals(ParseConstants.KEY_PREDATOR)) {
                // Get adopters relation
                mAdopter = mCurrentUser.getRelation(ParseConstants.KEY_USER_ADOPTER_RELATION);
                ParseQuery mAdopterQuery = mAdopter.getQuery();
                mAdopterQuery.whereEqualTo(ParseConstants.KEY_GENERAL_ID, animalRef);
                // If animal is adopted by current user change button visibility
                mAdopterQuery.getFirstInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject animal, ParseException e) {
                        if (animal != null) {
                            mDonation.setVisibility(View.VISIBLE);
                        } else {
                            mAdoption.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
            // Get animal Info
            getAnimalInfo();

        }
    }

    private void getAnimalInfo() {
        // Query to get animal object
        ParseQuery<ParseObject> query = ParseQuery.getQuery(ParseConstants.CLASS_ANIMALV2);
        query.include(ParseConstants.KEY_ANIMAL_EVENTS);
        query.include(ParseConstants.KEY_ANIMAL_KEEPERS);
        query.include(ParseConstants.KEY_ANIMAL_KEEPERS + "." + ParseConstants.KEY_KEEPER_ZOO);
        query.include(ParseConstants.KEY_ANIMAL_KEEPERS + "." + ParseConstants.KEY_KEEPER_USER);
        query.whereEqualTo(ParseConstants.KEY_GENERAL_ID, animalRef);
        query.getFirstInBackground(new GetCallback<ParseObject>() {


            @Override
            public void done(final ParseObject animalInfo, ParseException e) {
                if (animalInfo == null || e != null) {
                    if (getActivity() == null) return;
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.simple_error_title)
                            .setMessage(R.string.simple_error_message)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    if(!animalRef.equals(ParseConstants.KEY_PREDATOR)) {
                        // Add adoption button action
                        mAdoption.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // Query to get animal object
                                mCurrentUser = ParseUser.getCurrentUser();
                                final ParseObject animal = animalInfo;
                                mAdopter.add(animal);
                                mCurrentUser.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e != null) {
                                            if (getActivity() == null) return;
                                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                            builder.setTitle(R.string.simple_error_title)
                                                    .setMessage(R.string.simple_error_message)
                                                    .setPositiveButton(android.R.string.ok, null);
                                            AlertDialog dialog = builder.create();
                                            dialog.show();
                                        } else {

                                            animal.put(ParseConstants.KEY_ANIMAL_ADOPTERS, animal.getInt(ParseConstants.KEY_ANIMAL_ADOPTERS) + 1);
                                            animal.saveInBackground(new SaveCallback() {
                                                @Override
                                                public void done(ParseException e) {
                                                    if (e == null) {
                                                        refreshAdopters();
                                                    }
                                                }
                                            });


                                            if (getActivity() == null) return;
                                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                            builder.setTitle(R.string.thanks)
                                                    .setMessage(getActivity().getString(R.string.adoption_message) + animal.getString(ParseConstants.getKeyAnimalv2Name(getActivity())))
                                                    .setPositiveButton(android.R.string.ok, null);
                                            AlertDialog dialog = builder.create();
                                            dialog.show();
                                            mAdoption.setVisibility(View.INVISIBLE);
                                            mDonation.setVisibility(View.VISIBLE);

                                        }
                                    }
                                });
                            }
                        });
                    }
                    if (getActivity() == null) return;
                    // Change toolbar title to species name
                    getActivity().setTitle(animalInfo.getString(ParseConstants.getKeyAnimalSpecies(getActivity())));


                    // Get the image url and we use picasso to load the image
                    if (animalInfo.getParseFile(ParseConstants.KEY_ANIMAL_PHOTO) != null) {
                        String urlProduct = animalInfo.getParseFile(ParseConstants.KEY_ANIMAL_PHOTO).getUrl();
                        Picasso.with(getActivity()).load(urlProduct).into(photo);
                    }
                    // Get animal name
                    String name = animalInfo.getString(ParseConstants.getKeyAnimalv2Name(getActivity()));
                    animalName.setText(name);

                    // Get about text
                    String about = animalInfo.getString(ParseConstants.getKeyAnimalAbout(getActivity()));
                    textAbout.setText(about);

                    // Get characteristics
                    Map p = (HashMap) animalInfo.get(ParseConstants.getKeyAnimalCharacteristics(getActivity()));
                    getAnimalCharacteristics(p);

                    // Get Keepers
                    List<ParseObject> keepers = animalInfo.getList(ParseConstants.KEY_ANIMAL_KEEPERS);
                    if(keepers != null) getAnimalsKeeper(keepers);


                    // Get events
                    getAnimalEvents();


                    // Get adopters number
                    if(!animalRef.equals(ParseConstants.KEY_PREDATOR))
                        mCounter.setText(String.valueOf(animalInfo.getInt(ParseConstants.KEY_ANIMAL_ADOPTERS)));
                    else {
                        labelCounter.setText(getString(R.string.population));
                        mCounter.setText(String.valueOf(animalInfo.getInt(ParseConstants.KEY_ANIMAL_ADOPTERS)) + "M");

                    }
                }
            }
        });
    }

    private void getAnimalEvents() {
        Calendar c = Calendar.getInstance();
        Date d = c.getTime();
        ParseObject animalReference = ParseObject.createWithoutData(ParseConstants.CLASS_ANIMALV2, animalRef);
        ParseQuery<ParseObject> query = ParseQuery.getQuery(ParseConstants.CLASS_EVENTS);
        query.addDescendingOrder(ParseConstants.KEY_EVENT_START_DATE);
        query.whereEqualTo(ParseConstants.KEY_EVENT_ANIMAL_ID, animalReference);
        query.whereGreaterThan(ParseConstants.KEY_EVENT_START_DATE, d);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject event, ParseException e) {
                if (event == null) {
                    mWrapperEvent.setVisibility(View.INVISIBLE);
                    mNotEvents.setVisibility(View.VISIBLE);
                } else {
                    mWrapperEvent.setVisibility(View.VISIBLE);
                    mNotEvents.setVisibility(View.INVISIBLE);
                    eventTitle.setText(event.get(ParseConstants.getKeyEventTitle(getActivity())).toString());
                    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                    // Format the date to Strings
                    String start = format.format(event.getDate(ParseConstants.KEY_EVENT_START_DATE));
                    String end = format.format(event.getDate(ParseConstants.KEY_EVENT_END_DATE));
                    eventDate.setText(start + " - " + end);
                    eventDescription.setText(event.get(ParseConstants.getKeyEventDescription(getActivity())).toString());
                    // Get the image url and we use picasso to load the image
                    if (event.getParseFile(ParseConstants.KEY_EVENT_PHOTO) != null) {
                        String urlEvent = event.getParseFile(ParseConstants.KEY_EVENT_PHOTO).getUrl();
                        Picasso.with(getActivity()).load(urlEvent).into(imgEvent);
                    }


                    mEventCard.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent event = new Intent(getActivity(), EventsActivity.class);
                            event.putExtra(ParseConstants.KEY_EVENT_ANIMAL_ID, animalRef);
                            getActivity().startActivity(event);
                        }
                    });
                }
            }
        });
    }


    private void refreshAdopters() {
        // Query to get animal object
        ParseQuery<ParseObject> query = ParseQuery.getQuery(ParseConstants.CLASS_ANIMALV2);
        query.whereEqualTo(ParseConstants.KEY_GENERAL_ID, animalRef);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject animal, ParseException e) {
                if (e == null) {
                    mCounter.setText(String.valueOf(animal.getInt(ParseConstants.KEY_ANIMAL_ADOPTERS)));
                }
            }
        });
    }

    /**
     * This method gets all the keepers information in a List,
     * at the same time the structure is created programmatically
     *
     * @params List <ParseObject>
     */
    private void getAnimalsKeeper(List<ParseObject> keepers) {
        if (keepers == null) return;
        // Clean
        mLayoutKeeper.removeAllViews();
        // Iterate over keepers list in order to get their information
        for (ParseObject keeper : keepers) {
            // Parent layout
            LinearLayout innerContainer = new LinearLayout(getActivity());
            LinearLayout.LayoutParams LLParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            LLParams.setMargins(0, 20, 20, 5);
            innerContainer.setLayoutParams(LLParams);
            innerContainer.setOrientation(LinearLayout.HORIZONTAL);
            innerContainer.setWeightSum(100);


            // Create container for Image View
            LinearLayout.LayoutParams mWrapperImage = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    350, 70);
            // Create Image View for Keepers photo
            ImageView mImageKeeper = new ImageView(getActivity());
            mImageKeeper.setLayoutParams(mWrapperImage);
            mImageKeeper.setScaleType(ImageView.ScaleType.CENTER_CROP);
            innerContainer.addView(mImageKeeper);

            // Layout keeper description
            RelativeLayout.LayoutParams mWrapperBottom = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);
            mWrapperBottom.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            RelativeLayout bottom = new RelativeLayout(getActivity());
            bottom.setLayoutParams(mWrapperBottom);

            LinearLayout innerContainerDescription = new LinearLayout(getActivity());
            LinearLayout.LayoutParams mWrapDescription = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            innerContainerDescription.setVerticalGravity(Gravity.BOTTOM);
            innerContainerDescription.setLayoutParams(mWrapDescription);
            innerContainerDescription.setOrientation(LinearLayout.VERTICAL);
            float scale = getResources().getDisplayMetrics().density;
            int dpAsPixels = (int) (10*scale + 0.5f);
            innerContainerDescription.setPadding(dpAsPixels, 0,0,0);

            // Create a template container witht MATCH_PARENT and WRAP_CONTENT
            LinearLayout.LayoutParams mWrapContent = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);

            // Create container for Keepers title
            LinearLayout.LayoutParams mWraperTitle = mWrapContent;
            TextView mKeeperTitle = new TextView(getActivity());
            mKeeperTitle.setLayoutParams(mWraperTitle);
            innerContainerDescription.addView(mKeeperTitle);

            // Create container for Keepers zoo
            TextView mKeeperZoo = new TextView(getActivity());
            mKeeperZoo.setLayoutParams(mWrapContent);
            innerContainerDescription.addView(mKeeperZoo);

            // Create container for Keepers name
            LinearLayout.LayoutParams mWraperName = mWrapContent;
            TextView mKeeperName = new TextView(getActivity());
            mKeeperName.setLayoutParams(mWraperName);
            innerContainerDescription.addView(mKeeperName);
            bottom.addView(innerContainerDescription);

            innerContainer.addView(bottom);

            // Add everything to parent container
            mLayoutKeeper.addView(innerContainer);


            // Once the containers are created we can fill boxes
            // Get the image url and we use picasso to load the image
            String urlKeeper = keeper.getParseObject(ParseConstants.KEY_KEEPER_USER).getString(ParseConstants.KEY_USER_PHOTO);
            Picasso.with(getActivity()).load(urlKeeper).into(mImageKeeper);
            // Add zoo name
            mKeeperZoo.setText(keeper.getParseObject(ParseConstants.KEY_KEEPER_ZOO).getString(ParseConstants.KEY_ZOO_NAME));
            mKeeperZoo.setTextSize(15);
            mKeeperZoo.setTypeface(Typeface.create(GeneralConstants.KEY_ROBOTO_NORMAL, Typeface.NORMAL));

            // Add keeper name
            String completeName = keeper.getParseObject(ParseConstants.KEY_KEEPER_USER).getString(ParseConstants.KEY_USER_NAME) +
                    " " + keeper.getParseObject(ParseConstants.KEY_KEEPER_USER).getString(ParseConstants.KEY_USER_LASTNAME);
            mKeeperName.setText(completeName);
            mKeeperName.setTextSize(22);
            mKeeperName.setTypeface(Typeface.create(GeneralConstants.KEY_ROBOTO_NORMAL, Typeface.NORMAL));

            // Add keeper title
            mKeeperTitle.setText(R.string.keeper_label);
            mKeeperTitle.setTextSize(15);
            mKeeperTitle.setTypeface(Typeface.create(GeneralConstants.KEY_ROBOTO_NORMAL, Typeface.NORMAL));
        }
    }

    /**
     * This method gets all the characteristics from Map,
     * at the same time the structure is created programmatically
     *
     * @params Mam p
     */

    private void getAnimalCharacteristics(Map p) {
        if (p == null) return;
        // Clean
        mLayoutCharacteristics.removeAllViews();
        // Iterate over map in order to get all animal characteristics
        for (Object key : p.keySet()) {

            // Create the main container
            LinearLayout innerContainer = new LinearLayout(getActivity());
            LinearLayout.LayoutParams LLParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            innerContainer.setLayoutParams(LLParams);
            innerContainer.setOrientation(LinearLayout.HORIZONTAL);
            innerContainer.setWeightSum(100);

            // Create title container
            LinearLayout.LayoutParams paramTitle = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 70);
            paramTitle.setMargins(10, 10, 2, 10);

            // Create title TextView
            TextView title = new TextView(getActivity());
            title.setLayoutParams(paramTitle);
            title.setText(key.toString());
            title.setTextSize(12);

            // Create Description container
            LinearLayout.LayoutParams paramDescription = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 30);
            paramDescription.setMargins(10, 10, 0, 10);

            // Create description TextView
            TextView description = new TextView(getActivity());
            description.setLayoutParams(paramDescription);
            description.setText(p.get(key).toString());
            description.setTypeface(Typeface.create(GeneralConstants.KEY_ROBOTO_LIGHT, Typeface.NORMAL));
            description.setTextSize(12);

            innerContainer.addView(title);
            innerContainer.addView(description);

            // Add everything to parent container
            mLayoutCharacteristics.addView(innerContainer);

        }
    }


}
