package com.bondzu.bondzuapp.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bondzu.bondzuapp.R;
import com.bondzu.bondzuapp.utils.GeneralConstants;
import com.bondzu.bondzuapp.utils.ParseConstants;
import com.bondzu.bondzuapp.youtube.FullScreenActivity;
import com.google.android.youtube.player.YouTubeApiServiceUtil;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeThumbnailLoader;
import com.google.android.youtube.player.YouTubeThumbnailView;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A fragment representing a list of Items.
 */
public class VideoCapsuleFragment extends ListFragment {
    private static List<VideoEntry> VIDEO_LIST;
    private static Map<String,Integer> month_pos = new HashMap<String, Integer>();

    private PageAdapter adapter;

    /** The request code when calling startActivityForResult to recover from an API service error. */
    private static final int RECOVERY_DIALOG_REQUEST = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public void onResume() {
        super.onResume();
            checkYouTubeApi();
            getVideos();
    }

    private void checkYouTubeApi() {
        YouTubeInitializationResult errorReason =
                YouTubeApiServiceUtil.isYouTubeApiServiceAvailable(getActivity());
        if (errorReason.isUserRecoverableError()) {
            errorReason.getErrorDialog(getActivity(), RECOVERY_DIALOG_REQUEST).show();
        } else if (errorReason != YouTubeInitializationResult.SUCCESS) {
            String errorMessage =
                    String.format(getString(R.string.error_player), errorReason.toString());
            Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Intent i = new Intent(getActivity(), FullScreenActivity.class);
        i.putExtra(ParseConstants.KEY_VIDEO_YOUTUBE, VIDEO_LIST.get(position).videoId);
        startActivity(i);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(adapter != null)
            adapter.releaseLoaders();
    }


    /**
     * This method will get video capsules
     * by date
     */
    private void getVideos() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(ParseConstants.CLASS_VIDEO);
        query.addDescendingOrder(ParseConstants.KEY_GENERAL_CREATED);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> videos, ParseException e) {
                // If there aren't any errors the comments adapter needs to be created
                if (e == null && videos != null) {
                    // Create the Video List
                    List<VideoEntry> list = new ArrayList<VideoEntry>();
                    int mont_counter = 0;
                    for (ParseObject video : videos) {
                        List<String> title = video.getList(ParseConstants.getKeyVideoTitles(getActivity()));
                        List<String> description = video.getList(ParseConstants.getKeyVideoDescriptions(getActivity()));
                        List<String> youtube = video.getList(ParseConstants.KEY_VIDEO_YOUTUBE);
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(video.getCreatedAt());
                        int month = cal.get(Calendar.MONTH);

                        if(month_pos.isEmpty()) {
                            month_pos.put(getMonthForInt(month), 0);
                        }
                        else if(!month_pos.containsKey(getMonthForInt(month))) {
                            month_pos.put(getMonthForInt(month), mont_counter);
                        }

                        for (int i = 0; i < title.size(); i++) {
                            list.add(new VideoEntry(title.get(i), youtube.get(i), description.get(i), getMonthForInt(month)));
                        }
                        mont_counter++;
                    }
                    VIDEO_LIST = Collections.unmodifiableList(list);

                    // Check if we need to create the recycler view adapter
                    if (getListAdapter() == null) {
                        adapter = new PageAdapter(getActivity(), VIDEO_LIST);
                        setListAdapter(adapter);
                    }
                    // Else just update list
                    else {
                        ((PageAdapter) getListAdapter()).refill(VIDEO_LIST);
                    }
                }
                // Else a message will be displayed
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

    String getMonthForInt(int num) {
        String month = "wrong";
        DateFormatSymbols dfs = new DateFormatSymbols();
        String[] months = dfs.getMonths();
        if (num >= 0 && num <= 11 ) {
            month = months[num];
        }
        return month;
    }


/**
 * Adapter for the video list. Manages a set of YouTubeThumbnailViews, including initializing each
 * of them only once and keeping track of the loader of each one. When the ListFragment gets
 * destroyed it releases all the loaders.
 */
private static final class PageAdapter extends BaseAdapter {

    private final List<VideoEntry> entries;
    private final Map<YouTubeThumbnailView, YouTubeThumbnailLoader> thumbnailViewToLoaderMap;
    private final LayoutInflater inflater;
    private final ThumbnailListener thumbnailListener;

    private boolean labelsVisible;

    public PageAdapter(Context context, List<VideoEntry> entries) {
        this.entries = entries;

        thumbnailViewToLoaderMap = new HashMap<YouTubeThumbnailView, YouTubeThumbnailLoader>();
        inflater = LayoutInflater.from(context);
        thumbnailListener = new ThumbnailListener();

        labelsVisible = true;
    }

    public void releaseLoaders() {
        for (YouTubeThumbnailLoader loader : thumbnailViewToLoaderMap.values()) {
            loader.release();
        }
    }



    @Override
    public int getCount() {
        return entries.size();
    }

    @Override
    public VideoEntry getItem(int position) {
        return entries.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        VideoEntry entry = entries.get(position);

        // There are three cases here
        if (view == null) {
            // 1) The view has not yet been created - we need to initialize the YouTubeThumbnailView.
            view = inflater.inflate(R.layout.video_list_item, parent, false);
            YouTubeThumbnailView thumbnail = (YouTubeThumbnailView) view.findViewById(R.id.thumbnail);
            thumbnail.setTag(entry.videoId);
            thumbnail.initialize(GeneralConstants.DEVELOPER_KEY, thumbnailListener);
        } else {
            YouTubeThumbnailView thumbnail = (YouTubeThumbnailView) view.findViewById(R.id.thumbnail);
            YouTubeThumbnailLoader loader = thumbnailViewToLoaderMap.get(thumbnail);
            if (loader == null) {
                // 2) The view is already created, and is currently being initialized. We store the
                //    current videoId in the tag.
                thumbnail.setTag(entry.videoId);
            } else {
                // 3) The view is already created and already initialized. Simply set the right videoId
                //    on the loader.
                thumbnail.setImageResource(R.drawable.no_disponible);
                loader.setVideo(entry.videoId);
            }
        }
        TextView label = ((TextView) view.findViewById(R.id.text));
        label.setText(entry.title);
        TextView desc = ((TextView) view.findViewById(R.id.description));
        desc.setText(entry.description);
        label.setVisibility(labelsVisible ? View.VISIBLE : View.GONE);
        TextView month = ((TextView) view.findViewById(R.id.month));
        month.setVisibility(View.VISIBLE);
        month.setText(entry.month);
        return view;
    }

    public void refill(List<VideoEntry> videos) {
        notifyDataSetChanged();
    }

    private final class ThumbnailListener implements
            YouTubeThumbnailView.OnInitializedListener,
            YouTubeThumbnailLoader.OnThumbnailLoadedListener {

        @Override
        public void onInitializationSuccess(
                YouTubeThumbnailView view, YouTubeThumbnailLoader loader) {
            loader.setOnThumbnailLoadedListener(this);
            thumbnailViewToLoaderMap.put(view, loader);
            view.setImageResource(R.drawable.no_disponible);
            String videoId = (String) view.getTag();
            loader.setVideo(videoId);
        }

        @Override
        public void onInitializationFailure(
                YouTubeThumbnailView view, YouTubeInitializationResult loader) {
            view.setImageResource(R.drawable.no_disponible);
        }

        @Override
        public void onThumbnailLoaded(YouTubeThumbnailView view, String videoId) {
        }

        @Override
        public void onThumbnailError(YouTubeThumbnailView view, YouTubeThumbnailLoader.ErrorReason errorReason) {
            view.setImageResource(R.drawable.no_disponible);
        }
    }

}

private static final class VideoEntry {
    private final String title;
    private final String description;
    private final String videoId;
    private final String month;

    public VideoEntry(String text, String videoId, String description, String month) {
        this.title = text;
        this.videoId = videoId;
        this.description = description;
        this.month = month;
    }
}
}