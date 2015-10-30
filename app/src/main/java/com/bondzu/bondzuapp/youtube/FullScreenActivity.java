package com.bondzu.bondzuapp.youtube;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.bondzu.bondzuapp.R;
import com.bondzu.bondzuapp.ui.Home;
import com.bondzu.bondzuapp.ui.NativeCamera;
import com.bondzu.bondzuapp.utils.GeneralConstants;
import com.bondzu.bondzuapp.utils.ParseConstants;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class FullScreenActivity extends YouTubeFailureRecoveryActivity implements
        YouTubePlayer.OnFullscreenListener, YouTubePlayer.PlayerStateChangeListener {

    private LinearLayout baseLayout;
    private YouTubePlayerView playerView;
    private YouTubePlayer player;

    private boolean fullscreen;
    String youtube;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_full_screen);
        baseLayout = (LinearLayout) findViewById(R.id.layout);
        playerView = (YouTubePlayerView) findViewById(R.id.player);

        youtube = getIntent().getExtras().getString(ParseConstants.KEY_VIDEO_YOUTUBE);


        playerView.initialize(GeneralConstants.DEVELOPER_KEY, this);
        fullscreen = true;
        doLayout();
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player,
                                        boolean wasRestored) {
        this.player = player;
        player.setFullscreen(true);
        player.setPlayerStateChangeListener(this);
        if (!wasRestored) {
            player.cueVideo(youtube);
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult result) {
        this.player = null;
    }

    @Override
    protected YouTubePlayer.Provider getYouTubePlayerProvider() {
        return playerView;
    }

    @Override
    protected void onDestroy() {
        if (player != null) {
            player.release();
        }
        super.onDestroy();
    }

    private void doLayout() {
        LinearLayout.LayoutParams playerParams =
                (LinearLayout.LayoutParams) playerView.getLayoutParams();
        if (fullscreen) {
            // When in fullscreen, the visibility of all other views than the player should be set to
            // GONE and the player should be laid out across the whole screen.
            playerParams.width = LayoutParams.MATCH_PARENT;
            playerParams.height = LayoutParams.MATCH_PARENT;

        }
    }

    @Override
    public void onFullscreen(boolean isFullscreen) {
        fullscreen = isFullscreen;
        doLayout();
    }

    @Override
    public void onLoading() {
    }

    @Override
    public void onLoaded(String s) {

    }

    @Override
    public void onAdStarted() {
    }

    @Override
    public void onVideoStarted() {

    }

    @Override
    public void onVideoEnded() {
        // Query to get video object
        // and compare unique title for human waste
        ParseQuery<ParseObject> query = ParseQuery.getQuery(ParseConstants.CLASS_VIDEO);
        query.include(ParseConstants.KEY_ANIMAL_ID);
        query.whereEqualTo(ParseConstants.KEY_VIDEO_YOUTUBE, youtube);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject video, ParseException e) {
                if(e == null) {
                    ParseObject animal = video.getParseObject(ParseConstants.KEY_ANIMAL_ID);
                    if(animal.getObjectId().equals(ParseConstants.KEY_PREDATOR)) {
                        Intent i = new Intent(FullScreenActivity.this, NativeCamera.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                        finish();
                    }
                    else {
                        Intent i = new Intent(FullScreenActivity.this, Home.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                        finish();
                    }
                }

            }
        });
    }

    @Override
    public void onError(YouTubePlayer.ErrorReason errorReason) {
    }

    @Override
    public void onBackPressed() {
        // do nothing.
        Intent i = new Intent(FullScreenActivity.this, Home.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

}
