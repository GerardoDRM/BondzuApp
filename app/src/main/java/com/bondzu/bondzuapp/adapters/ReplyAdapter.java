package com.bondzu.bondzuapp.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseObject;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.List;

import com.bondzu.bondzuapp.R;
import com.bondzu.bondzuapp.utils.CircleTransform;
import com.bondzu.bondzuapp.utils.ParseConstants;
import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by gerardo on 1/09/15.
 */
public class ReplyAdapter extends RecyclerView.Adapter<ReplyAdapter.ViewHolder> {
    private Context mContext;
    private List<ParseObject> mComment;

    public ReplyAdapter(Context context, List<ParseObject> comment) {
        this.mContext = context;
        this.mComment = comment;
    }

    @Override
    public ReplyAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.comment_items, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bindMessage(mComment.get(position));
    }


    @Override
    public int getItemCount() {
        return mComment.size();
    }

    public void refill(List<ParseObject> messages) {
        mComment.clear();
        mComment.addAll(messages);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.user_comment_reply)
        ImageView mUserPhoto;
        @Bind(R.id.label_user_reply)
        TextView mUserName;
        @Bind(R.id.message_reply)
        TextView mMessage;
        @Bind(R.id.time_reply)
        TextView mTime;


        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bindMessage(final ParseObject message) {
            // Get the image url and we use picasso to load the image
            String userPhoto = message.getParseObject(ParseConstants.KEY_COMMENTS_USER).getString(ParseConstants.KEY_USER_PHOTO);
            Picasso.with(mContext).load(userPhoto).transform(new CircleTransform()).fit().centerCrop().tag(mContext).into(mUserPhoto);
            mUserName.setText(message.getParseObject(ParseConstants.KEY_COMMENTS_USER).getString(ParseConstants.KEY_USER_NAME));
            mMessage.setText(message.getString(ParseConstants.KEY_COMMENTS_MESSAGE));
            Date createdAt = message.getCreatedAt();
            long now = new Date().getTime();
            String convertedDate = DateUtils.getRelativeTimeSpanString(
                    createdAt.getTime(),
                    now,
                    DateUtils.DAY_IN_MILLIS).toString();
            mTime.setText(convertedDate);

        }

    }
}