package com.bondzu.bondzuapp.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.List;

import com.bondzu.bondzuapp.R;
import com.bondzu.bondzuapp.adapters.ReplyAdapter;
import com.bondzu.bondzuapp.utils.CircleTransform;
import com.bondzu.bondzuapp.utils.DividerItemDecoration;
import com.bondzu.bondzuapp.utils.GeneralConstants;
import com.bondzu.bondzuapp.utils.ParseConstants;
import butterknife.Bind;
import butterknife.ButterKnife;

public class IndependentMessage extends AppCompatActivity {

    @Bind(R.id.user_comment_replyF)
    ImageView mUserPhoto;
    @Bind(R.id.label_user_replyF)
    TextView mUserName;
    @Bind(R.id.message_replyF)
    TextView mMessage;
    @Bind(R.id.timeF)
    TextView mTime;
    // Comments list
    @Bind(R.id.recyclerViewReply)
    RecyclerView mRecyclerComments;

    // Reply
    @Bind(R.id.send_reply)
    ImageView mSendReply;
    @Bind(R.id.send_inactive)
    ImageView mSendInactive;

    @Bind(R.id.edit_reply)
    EditText mEditReply;

    @Bind(R.id.layout_first_comment)
    LinearLayout mFirstComment;
    @Bind(R.id.photo_first)
    ImageButton mPhotoOK;

    // Update comments
    @Bind(R.id.swipeRefreshIndependent)
    SwipeRefreshLayout mSwipe;

    private ParseObject postObject;
    private String messageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_independent_message);
        ButterKnife.bind(this);

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
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerComments.setLayoutManager(layoutManager);
        mRecyclerComments.setHasFixedSize(true);
        mRecyclerComments.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        // Adding material design toolbar
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setNavigationIcon(ContextCompat.getDrawable(this, R.mipmap.ic_close_white_24dp));
        setSupportActionBar(mToolbar);
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        // Create local instance of animal object
        messageReference = getIntent().getStringExtra(ParseConstants.KEY_COMMENTS_MESSAGE);

    }

    private void sendActions() {
        /**
         * Change any change on the edit box
         */
        mEditReply.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // If the edit box is empty disappear send button
                if (mEditReply.getText().toString().isEmpty() || mEditReply.getText().toString().length() < 4) {
                    mSendInactive.setVisibility(View.VISIBLE);
                    mSendReply.setVisibility(View.GONE);
                }
                // Else the send button appears
                else if(GeneralConstants.checkLong(mEditReply.getText().toString(), 4)){
                    mSendReply.setVisibility(View.VISIBLE);
                    mSendInactive.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        /**
         *  If the user wants to post a reply this buttons will be clicked
         */
        mSendReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseObject comment = new ParseObject(ParseConstants.CLASS_COMMENT);
                comment.put(ParseConstants.KEY_COMMENTS_PARENT, postObject);
                comment.put(ParseConstants.KEY_COMMENTS_USER, ParseUser.getCurrentUser());
                comment.put(ParseConstants.KEY_COMMENTS_MESSAGE, mEditReply.getText().toString());
                comment.saveInBackground();
                mEditReply.setText("");
                // Update UI with the new comments
                getComments();
            }
        });
    }

    /**
     * This method get the main post
     */
    private void getPost() {
        // Query to get Post
        ParseQuery<ParseObject> query = ParseQuery.getQuery(ParseConstants.CLASS_MESSAGES);
        query.whereEqualTo(ParseConstants.KEY_GENERAL_ID, messageReference);
        query.include(ParseConstants.KEY_COMMENTS_USER);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            public void done(final ParseObject message, ParseException e) {
                if (message != null) {
                    // Get the image url and we use picasso to load the image
                    String userPhoto = message.getParseObject(ParseConstants.KEY_COMMENTS_USER).getString(ParseConstants.KEY_USER_PHOTO);
                    Picasso.with(IndependentMessage.this).load(userPhoto).transform(new CircleTransform()).into(mUserPhoto);
                    mUserName.setText(message.getParseObject(ParseConstants.KEY_COMMENTS_USER).getString(ParseConstants.KEY_USER_NAME));
                    mMessage.setText(message.getString(ParseConstants.KEY_COMMENTS_MESSAGE));
                    Date createdAt = message.getCreatedAt();
                    long now = new Date().getTime();
                    String convertedDate = DateUtils.getRelativeTimeSpanString(
                            createdAt.getTime(),
                            now,
                            DateUtils.DAY_IN_MILLIS).toString();
                    mTime.setText(convertedDate);

                    // Check if there is an image
                    if (message.getParseFile(ParseConstants.KEY_COMMENTS_PHOTO) != null) {
                        Picasso.with(IndependentMessage.this).load(R.drawable.ic_photo_library_grey_24dp).into(mPhotoOK);
                        mPhotoOK.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ParseFile file = message.getParseFile(ParseConstants.KEY_COMMENTS_PHOTO);
                                Uri fileUri = Uri.parse(file.getUrl());
                                // View the image
                                Intent i = new Intent(IndependentMessage.this, ViewImageActivity.class);
                                i.setData(fileUri);
                                startActivity(i);
                            }
                        });
                    }

                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!GeneralConstants.checkNetwork(this))
            GeneralConstants.showMessageConnection(this);
        else {
            // Get main post
            getPost();
            // Query to get comments
            postObject = ParseObject.createWithoutData(ParseConstants.CLASS_MESSAGES, messageReference);
            getComments();
            // Activate edittext and send button actions
            sendActions();
        }
    }


    /**
     * This method will get all replies for
     * the message received
     */
    private void getComments() {
        ParseQuery<ParseObject> queryComments = ParseQuery.getQuery(ParseConstants.CLASS_COMMENT);
        queryComments.addDescendingOrder(ParseConstants.KEY_GENERAL_CREATED);
        queryComments.whereEqualTo(ParseConstants.KEY_COMMENTS_PARENT, postObject);
        queryComments.include(ParseConstants.KEY_COMMENTS_USER);
        queryComments.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> messages, ParseException e) {
                // Hide swipe from UI
                if (mSwipe.isRefreshing()) {
                    mSwipe.setRefreshing(false);
                }
                // If there aren't any errors the comments adapter needs to be created
                if (e == null) {
                    // Check if we need to create the recycler view adapter
                    if(mRecyclerComments.getAdapter() == null) {
                        ReplyAdapter comments = new ReplyAdapter(IndependentMessage.this, messages);
                        mRecyclerComments.setAdapter(comments);
                    }
                    // Else just update list
                    else {
                        ((ReplyAdapter) mRecyclerComments.getAdapter()).refill(messages);
                    }
                }
                // Else a message will be displayed
                else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(IndependentMessage.this);
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
