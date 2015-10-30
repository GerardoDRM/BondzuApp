package com.bondzu.bondzuapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.provider.CalendarContract;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseObject;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import com.bondzu.bondzuapp.R;
import com.bondzu.bondzuapp.utils.ParseConstants;
import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by gerardo on 6/09/15.
 */
public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.ViewHolder> {
    private Context mContext;
    private List<ParseObject> mEvents;

    public EventsAdapter(Context context, List<ParseObject> events) {
        this.mContext = context;
        this.mEvents = events;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.events_items, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bindEvents(mEvents.get(position));
    }


    public void refill(List<ParseObject> events) {
        mEvents.clear();
        mEvents.addAll(events);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mEvents.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.event_card_photo)
        ImageView mPhoto;
        @Bind(R.id.event_card_title)
        TextView mTitle;
        @Bind(R.id.event_card_description)
        TextView mDescription;
        @Bind(R.id.event_card_start)
        TextView mStartDate;
        @Bind(R.id.add_calendar)
        TextView mCalendar;

        Date mDate;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bindEvents(ParseObject event) {
            // Adding event photo
            Picasso.with(mContext).load(event.getParseFile(ParseConstants.KEY_EVENT_PHOTO).getUrl())
                    .fit().centerCrop().tag(mContext).into(mPhoto);
            // Adding event title
            mTitle.setText(event.getString(ParseConstants.getKeyEventTitle(mContext)));
            // Adding event description
            mDescription.setText(event.getString(ParseConstants.getKeyEventDescription(mContext)));
            // Adding event Date
            // Format the date to Strings
            SimpleDateFormat format = new SimpleDateFormat(mContext.getString(R.string.date_format));
            String start = format.format(event.getDate(ParseConstants.KEY_EVENT_START_DATE));
            String end = format.format(event.getDate(ParseConstants.KEY_EVENT_END_DATE));
            mStartDate.setText(start + " - " + end);
            mDate = event.getDate(ParseConstants.KEY_EVENT_START_DATE);

            mCalendar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent calIntent = new Intent(Intent.ACTION_INSERT);
                    calIntent.setType("vnd.android.cursor.item/event");
                    calIntent.putExtra(CalendarContract.Events.TITLE, mTitle.getText().toString());
                    calIntent.putExtra(CalendarContract.Events.DESCRIPTION, mDescription.getText().toString());

                    Calendar cal = Calendar.getInstance();
                    cal.setTime(mDate);

                    GregorianCalendar calDate = new GregorianCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
                    calIntent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true);
                    calIntent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                            calDate.getTimeInMillis());
                    calIntent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME,
                            calDate.getTimeInMillis());
                    mContext.startActivity(calIntent);
                }
            });

        }
    }
}
