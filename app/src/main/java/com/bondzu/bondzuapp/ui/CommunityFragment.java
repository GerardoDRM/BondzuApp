package com.bondzu.bondzuapp.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

import com.bondzu.bondzuapp.R;
import com.bondzu.bondzuapp.adapters.CommentsAdapter;
import com.bondzu.bondzuapp.utils.DividerItemDecoration;
import com.bondzu.bondzuapp.utils.FileHelper;
import com.bondzu.bondzuapp.utils.GeneralConstants;
import com.bondzu.bondzuapp.utils.ParseConstants;
import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class CommunityFragment extends Fragment {
    @Bind(R.id.recyclerViewComments)
    RecyclerView mRecyclerComments;
    @Bind(R.id.float_btn_comment)
    FloatingActionButton mFloatingComment;
    @Bind(R.id.float_btn_write)
    FloatingActionButton mCommentWrite;
    @Bind(R.id.float_btn_photo)
    FloatingActionButton mCommentPhoto;
    @Bind(R.id.send_container)
    LinearLayout mSendContainer;

    // Comment container
    @Bind(R.id.close_coment)
    ImageView mCloseComment;
    @Bind(R.id.send_comment)
    ImageView mSendComment;
    @Bind(R.id.edit_comment)
    EditText mEditComment;
    @Bind(R.id.photo_attached)
    ImageView photo;

    boolean expandible = false;
    public static final int PICK_PHOTO_REQUEST = 2;
    ParseObject animalReference;
    private String animalRef;
    ParseFile file;
    // Update comments
    @Bind(R.id.swipeRefreshCommunity)
    SwipeRefreshLayout mSwipe;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_community, container, false);
        ButterKnife.bind(this, rootView);

        // Use a swipe in order to update UI
        mSwipe.setColorSchemeColors(R.color.colorPrimary, R.color.colorPrimaryDark,
                R.color.colorAccent, android.R.color.white);
        mSwipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // When the UI is updated we get new comments
                getComments();
            }
        });
        // Recycler view data
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerComments.setLayoutManager(layoutManager);
        mRecyclerComments.setHasFixedSize(true);
        mRecyclerComments.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));

        // Get data from animal
        // Restore value to get data
        animalRef = getActivity().getIntent().getExtras().getString(ParseConstants.KEY_ANIMAL_ID);
        // Create local instance of animal object
        animalReference = ParseObject.createWithoutData(ParseConstants.CLASS_ANIMALV2, animalRef);


        return rootView;
    }

    private void buttonsFunction() {
        /**
         *  Send a post, we get all data from UI and
         *  we use parse.com to post info on the database
         */
        mSendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseObject comment = new ParseObject(ParseConstants.CLASS_MESSAGES);
                comment.put(ParseConstants.KEY_COMMENTS_ANIMAL, animalReference);
                comment.put(ParseConstants.KEY_COMMENTS_USER, ParseUser.getCurrentUser());
                comment.put(ParseConstants.KEY_COMMENTS_MESSAGE, mEditComment.getText().toString());
                // Check if an image is attached
                if(file != null) {
                    comment.put(ParseConstants.KEY_COMMENTS_PHOTO, file);
                }
                comment.saveInBackground();
                // Visible floating button
                mFloatingComment.setVisibility(View.VISIBLE);
                // Input for comment is hidden
                mSendContainer.setVisibility(View.INVISIBLE);
                // Reset edit comment container
                mEditComment.setText("");
                // Photo attached needs to disappear
                photo.setVisibility(View.GONE);
                // Clean file
                file = null;
                // Change image from floating button
                collapseFab();
                // Update UI to get new comment
                getComments();
            }
        });

        /**
         * When the user is writing on the edit box
         * the cancel and send options needs to be updated
         * on the UI
         */
        mEditComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // If the edit box is empty the "X" appears
                if (mEditComment.getText().toString().isEmpty()) {
                    mCloseComment.setVisibility(View.VISIBLE);
                    mSendComment.setVisibility(View.GONE);
                }
                // Else the send button appears
                else if(GeneralConstants.checkLong(mEditComment.getText().toString(),3)){
                    mSendComment.setVisibility(View.VISIBLE);
                    mCloseComment.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        /**
         * When "X" button is clicked the comment layout is
         * hidden
         */
        mCloseComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Floating button is visible
                mFloatingComment.setVisibility(View.VISIBLE);
                // Edit box is hidden
                mSendContainer.setVisibility(View.INVISIBLE);
                // Reset photo and photo icon
                if(photo.getVisibility() == View.VISIBLE)
                    photo.setVisibility(View.GONE);
                file = null;
                // Callapse extra options
                collapseFab();
            }
        });

        /**
         * When the main floating button is clicked
         * the tow more options needs to appear on the UI
         */
        mFloatingComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Expands and show tow more options
                if (!expandible) {
                    mCommentPhoto.setVisibility(View.VISIBLE);
                    mCommentWrite.setVisibility(View.VISIBLE);
                    expandFab();
                    expandible = true;

                }
                // Collapse and tow extra options disappear
                else {
                    mCommentPhoto.setVisibility(View.INVISIBLE);
                    mCommentWrite.setVisibility(View.INVISIBLE);
                    collapseFab();
                    expandible = false;

                }

            }
        });


        /**
         *  If the extra option button for simple post message
         *  is clicked an edit box will appear on the UI
         */
        mCommentWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Edit box appears
                mSendContainer.setVisibility(View.VISIBLE);
                // Floating button is hidden
                mFloatingComment.setVisibility(View.INVISIBLE);
                // Photo attached button is hidden
                mCommentPhoto.setVisibility(View.INVISIBLE);
                // The simple post button is hidden
                mCommentWrite.setVisibility(View.INVISIBLE);
                // The options collapse
                expandible = false;
            }
        });

        /**
         * If the user wants to send a photo with text
         * this button will be clicked
         */
        mCommentPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get image from gallery
                Intent choosePhoto = new Intent(Intent.ACTION_GET_CONTENT);
                choosePhoto.setType("image/*");
                startActivityForResult(choosePhoto, PICK_PHOTO_REQUEST);
                expandible = false;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if(getActivity() == null) return;
        // Check Network
        if (!GeneralConstants.checkNetwork(getActivity()))
            GeneralConstants.showMessageConnection(getActivity());
        else {
            // Get all comments at the beginning
            buttonsFunction();
            getComments();
        }
    }

    private void getComments() {
        // Query to get all post from this animal
        ParseQuery<ParseObject> query = ParseQuery.getQuery(ParseConstants.CLASS_MESSAGES);
        query.addDescendingOrder(ParseConstants.KEY_GENERAL_CREATED);
        query.whereEqualTo(ParseConstants.KEY_COMMENTS_ANIMAL, animalReference);
        query.include(ParseConstants.KEY_COMMENTS_USER);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> messages, ParseException e) {
                // Hide swipe from UI
                if (mSwipe.isRefreshing()) {
                    mSwipe.setRefreshing(false);
                }
                // If there aren't errors
                if (e == null) {
                    if(getActivity() == null) return;
                    // Check if we need to create the recycler view adapter
                    if(mRecyclerComments.getAdapter() == null) {
                        CommentsAdapter comments = new CommentsAdapter(getActivity(), messages, ParseUser.getCurrentUser().getObjectId());
                        mRecyclerComments.setAdapter(comments);
                    }
                    // Else just update list
                    else {
                        ((CommentsAdapter) mRecyclerComments.getAdapter()).refill(messages);
                    }

                }
                // If there are any errors a message will be displayed
                else {
                    if(getActivity() == null) return;
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

    /**
     * If an image is attached the activity
     * get the data and the this data will be
     * attached to a parse file
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            // Success add it to the gallery
            if (requestCode == PICK_PHOTO_REQUEST) {
                if (data == null) {
                    Toast.makeText(getActivity(), getString(R.string.simple_error_message), Toast.LENGTH_LONG).show();
                }
                else {
                    // Get image data and the image is stored in a ParseFile object
                    Uri mMediaUri = data.getData();
                    byte[] fileBytes = FileHelper.getByteArrayFromFile(getActivity(), mMediaUri);
                    fileBytes = FileHelper.reduceImageForUpload(fileBytes);
                    String fileName = FileHelper.getFileName(getActivity(), mMediaUri, "img");
                    file = new ParseFile(fileName, fileBytes);
                    // The photo icon is visible
                    photo.setVisibility(View.VISIBLE);
                    // The send container is visible
                    mSendContainer.setVisibility(View.VISIBLE);
                    // The floating buttons needs to be hidden
                    mFloatingComment.setVisibility(View.INVISIBLE);
                    // The two extra buttons need to be hidden
                    mCommentPhoto.setVisibility(View.INVISIBLE);
                    mCommentWrite.setVisibility(View.INVISIBLE);
                    collapseFab();
                    expandible = false;
                }
        }
        else if(resultCode != Activity.RESULT_CANCELED) {
            Toast.makeText(getActivity(), R.string.simple_error_message, Toast.LENGTH_LONG).show();
        }
    }
    }

    private void collapseFab() {
        mFloatingComment.setImageResource(R.drawable.ic_add_white_24dp);
        animateFab();
    }

    private void expandFab() {
        mFloatingComment.setImageResource(R.mipmap.ic_close_white_24dp);
        animateFab();
    }


    private void animateFab() {
        Drawable drawable = mFloatingComment.getDrawable();
        if (drawable instanceof Animatable) {
            ((Animatable) drawable).start();
        }
    }
}
