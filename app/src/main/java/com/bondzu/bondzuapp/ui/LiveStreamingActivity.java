package com.bondzu.bondzuapp.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;

import com.google.android.exoplayer.AspectRatioFrameLayout;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.drm.UnsupportedDrmException;
import com.google.android.exoplayer.util.Util;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.List;

import com.bondzu.bondzuapp.R;
import com.bondzu.bondzuapp.player.DemoPlayer;
import com.bondzu.bondzuapp.player.DemoPlayer.RendererBuilder;
import com.bondzu.bondzuapp.player.HlsRendererBuilder;
import com.bondzu.bondzuapp.utils.GeneralConstants;
import com.bondzu.bondzuapp.utils.ParseConstants;
import com.bondzu.bondzuapp.youtube.FullScreenActivity;

public class LiveStreamingActivity extends AppCompatActivity implements SurfaceHolder.Callback,
        DemoPlayer.Listener {

    // List Camera Objects
    protected List<ParseObject> mCameras;
    ImageView opt_cameras;
    // List items cameras
    CharSequence cameras_url[];
    CharSequence cameras_name[];

    private String animalRef;

    // Live stream params
    private static final CookieManager defaultCookieManager;
    static {
        defaultCookieManager = new CookieManager();
        defaultCookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }

    private MediaController mediaController;
    private View shutterView;
    private AspectRatioFrameLayout videoFrame;
    private SurfaceView surfaceView;

    private DemoPlayer player;
    private boolean playerNeedsPrepare;
    private long playerPosition;

    // Error Counter
    private int errorCounter= 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        final View mDecorView = this.getWindow().getDecorView();
        mDecorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);

        setContentView(R.layout.activity_live_streaming);
        // Live stream Explore
        View root = findViewById(R.id.root);
        root.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(player!=null) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    toggleControlsVisibility();
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    view.performClick();
                }
                }
                return true;
            }
        });
        root.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return !(keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU) && mediaController.dispatchKeyEvent(event);
            }
        });

        shutterView = findViewById(R.id.shutter);
        videoFrame = (AspectRatioFrameLayout) findViewById(R.id.video_frame);
        surfaceView = (SurfaceView) findViewById(R.id.surface_view);
        surfaceView.getHolder().addCallback(this);
        mediaController = new MediaController(this);
        mediaController.setAnchorView(root);

        CookieHandler currentHandler = CookieHandler.getDefault();
        if (currentHandler != defaultCookieManager) {
            CookieHandler.setDefault(defaultCookieManager);
        }

        // Camera button for switching
        opt_cameras = (ImageView) findViewById(R.id.btn_opt_cameras);
        opt_cameras.setVisibility(View.INVISIBLE);

        // Get data from animal
        animalRef = getIntent().getExtras().getString(ParseConstants.KEY_ANIMAL_ID);

    }

    private void getCameras(ParseObject animal) {
        // Query to get animal cameras
        ParseQuery<ParseObject> query = ParseQuery.getQuery(ParseConstants.CLASS_CAMERA);
        query.include(ParseConstants.KEY_CAMERA_ANIMAL_ID);
        query.whereEqualTo(ParseConstants.KEY_CAMERA_AVAILABILITY, true);
        query.whereEqualTo(ParseConstants.KEY_CAMERA_ANIMAL_ID, animal);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> cameras, ParseException e) {

                if (e == null) {

                    // We found the cameras
                    mCameras = cameras;

                    // Check for errors
                    if (mCameras.size() == 0) {
                        errorCounter += 1;
                        if(errorCounter == 1) {
                            errorCam();
                        }
                    }
                    // If we get just one camera
                    else {
                        // We play the first
                        String url = mCameras.get(0).getString(ParseConstants.KEY_CAMERA_URL);
                        preparePlayer(true, url);

                        // If we get many cameras
                        if (mCameras.size() > 1) {
                            int i = 0;
                            cameras_url = new CharSequence[mCameras.size()];
                            cameras_name = new CharSequence[mCameras.size()];
                            // Get cameras info
                            for (ParseObject cam : mCameras) {
                                cameras_url[i] = cam.getString(ParseConstants.KEY_CAMERA_URL);
                                cameras_name[i] = cam.getString(ParseConstants.KEY_CAMERA_DESCRIPTION);
                                i++;
                            }
                            opt_cameras.setVisibility(View.VISIBLE);
                            // List item cameras
                            opt_cameras.setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View v) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(LiveStreamingActivity.this);
                                    builder.setItems(cameras_name, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int position) {
                                            releasePlayer();
                                            preparePlayer(true, cameras_url[position].toString());

                                        }
                                    })
                                            .setTitle(R.string.camera_title);
                                    AlertDialog dialog = builder.create();
                                    dialog.show();
                                }
                            });
                        }
                    }// end else
                }// end if
                else {
                    errorCam();
                }
            }// end done
        });// end findInBackground
    }


    private void toggleControlsVisibility()  {
        if (mediaController.isShowing()) {
            mediaController.hide();
        } else {
            showControls();
        }
    }

    private void showControls() {
        mediaController.show(0);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (!GeneralConstants.checkNetwork(this))
            GeneralConstants.showMessageConnection(this);
        else {
            if (!GeneralConstants.checkWIFI(this))
                GeneralConstants.showMessageWIFI(this);
            // Create local instance of animal object
            ParseObject animal = ParseObject.createWithoutData(ParseConstants.CLASS_ANIMALV2, animalRef);
            if (player == null) {
                getCameras(animal);
            } else {
                player.setBackgrounded(false);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (player != null) {
            player.setBackgrounded(true);
            shutterView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (player!=null) {
            releasePlayer();
        }
    }

    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putInt("savedText", errorCounter);
        outState.putString("animal", animalRef);

    }
    protected void onRestoreInstanceState(Bundle savedState)
    {
        errorCounter = savedState.getInt("savedText");
        animalRef = savedState.getString("animal");
        if(errorCounter == 1) {
            ImageView image = (ImageView) findViewById(R.id.offline);
            image.setImageResource(R.mipmap.offline);
        }
    }


    // If we have an error, the screen will display an offline image
    private void errorCam() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        opt_cameras.setVisibility(View.GONE);


        new Handler().postDelayed(new Runnable() {
            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
            */
            @Override
            public void run() {
                if (errorCounter == 1) {
                    errorCounter += 1;
                    // Create local instance of animal object
                    ParseObject animal = ParseObject.createWithoutData(ParseConstants.CLASS_ANIMALV2, animalRef);
                    // Query to get animal cameras
                    ParseQuery<ParseObject> query = ParseQuery.getQuery(ParseConstants.CLASS_VIDEO);
                    query.whereEqualTo(ParseConstants.KEY_VIDEO_ANIMAL, animal);
                    query.getFirstInBackground(new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject video, ParseException e) {
                            if (e == null) {
                                List<String> videos = video.getList(ParseConstants.KEY_VIDEO_YOUTUBE);
                                Intent i = new Intent(LiveStreamingActivity.this, FullScreenActivity.class);
                                i.putExtra(ParseConstants.KEY_VIDEO_YOUTUBE, videos.get(0));
                                startActivity(i);
                                finish();
                            }
                            else {
                                finish();
                            }
                        }
                    });
                }
            }
        }, 3000);

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (player != null) {
            player.setSurface(holder.getSurface());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // Do nothing
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (player != null) {
            player.blockingClearSurface();
        }
    }



    private RendererBuilder getRendererBuilder(String url) {
        String userAgent = Util.getUserAgent(this, "ExoPlayerDemo");
        return new HlsRendererBuilder(this, userAgent, url);
    }

    private void preparePlayer(boolean playWhenReady, String url) {

        if (player == null) {
            player = new DemoPlayer(getRendererBuilder(url));
            player.addListener(this);
            player.seekTo(playerPosition);
            playerNeedsPrepare = true;
            mediaController.setMediaPlayer(player.getPlayerControl());
            mediaController.setEnabled(true);
        }
        if (playerNeedsPrepare) {
            player.prepare();
            playerNeedsPrepare = false;
        }
        player.setSurface(surfaceView.getHolder().getSurface());
        player.setPlayWhenReady(playWhenReady);
    }

    private void releasePlayer() {
        if (player != null) {
            playerPosition = player.getCurrentPosition();
            player.release();
            player = null;
        }
    }


    @Override
    public void onStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == ExoPlayer.STATE_ENDED) {
            showControls();
        }
    }

    @Override
    public void onError(Exception e) {
        if (e instanceof UnsupportedDrmException) {
            errorCam();
        }
        playerNeedsPrepare = true;
        showControls();
    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
        shutterView.setVisibility(View.GONE);
    }

}
