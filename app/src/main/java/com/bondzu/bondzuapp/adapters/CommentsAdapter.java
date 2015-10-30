package com.bondzu.bondzuapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.bondzu.bondzuapp.R;
import com.bondzu.bondzuapp.ui.IndependentMessage;
import com.bondzu.bondzuapp.utils.CircleTransform;
import com.bondzu.bondzuapp.utils.ParseConstants;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by gerardo on 27/08/15.
 */
public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {
    private Context mContext;
    private List<ParseObject> mMessages;
    private String mUserId;

    public CommentsAdapter(Context context, List<ParseObject> messages, String userId) {
        this.mContext = context;
        this.mMessages = messages;
        this.mUserId = userId;
    }

    @Override
    public CommentsAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.messages_items, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CommentsAdapter.ViewHolder holder, int position) {
        holder.bindMessage(mMessages.get(position), position);
    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }


    public void refill(List<ParseObject> messages) {
        mMessages.clear();
        mMessages.addAll(messages);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        @Bind(R.id.user_comment_reply)
        ImageView mUserPhoto;
        @Bind(R.id.label_user_reply)
        TextView mUserName;
        @Bind(R.id.message_reply)
        TextView mMessage;
        @Bind(R.id.time_reply)
        TextView mTime;
        @Bind(R.id.comments_likes)
        TextView mLikes;
        @Bind(R.id.reply_btn)
        TextView mReply;
        @Bind(R.id.report_btn)
        TextView mReport;
        @Bind(R.id.toggle_like)
        ToggleButton mLove;
        @Bind(R.id.img_photo_attached)
        ImageView mAttach;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bindMessage(final ParseObject message, int position) {
            // Get the image url and we use picasso to load the image
            if(message.getParseObject(ParseConstants.KEY_COMMENTS_USER) == null)
                Log.d("ERROr", message.getParseObject(ParseConstants.KEY_COMMENTS_USER)+"");
            else {
            String userPhoto = message.getParseObject(ParseConstants.KEY_COMMENTS_USER).getString(ParseConstants.KEY_USER_PHOTO);
            Picasso.with(mContext).load(userPhoto).transform(new CircleTransform()).fit().centerCrop().tag(mContext).into(mUserPhoto);
            mUserName.setText(message.getParseObject(ParseConstants.KEY_COMMENTS_USER).getString(ParseConstants.KEY_USER_NAME));
            mMessage.setText(message.getString(ParseConstants.KEY_COMMENTS_MESSAGE));}
            Date createdAt = message.getCreatedAt();
            long now = new Date().getTime();
            String convertedDate = DateUtils.getRelativeTimeSpanString(
                    createdAt.getTime(),
                    now,
                    DateUtils.DAY_IN_MILLIS).toString();
            mTime.setText(convertedDate);
            // Give me love
            ArrayList<String> likesIds = getLikesIds(message);
            mLikes.setText(String.valueOf(likesIds.size()));

            // Check if the use has given a like
            boolean hasLikes = checkOwnLikes(likesIds);
            // Update toogle
            if(hasLikes) {
                mLove.setChecked(true);
            }
            else {
                mLove.setChecked(false);
            }
            mLove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Update value like
                    ParseQuery<ParseObject> query = ParseQuery.getQuery(ParseConstants.CLASS_MESSAGES);
                    // Retrieve the object by id
                    query.getInBackground(message.getObjectId(), new GetCallback<ParseObject>() {
                        public void done(final ParseObject comment, ParseException e) {
                            if (e == null) {
                                ArrayList<String> likesIds = getLikesIds(comment);
                                int likes = likesIds.size();
                                if (mLove.isChecked()){
                                    likes += 1;
                                    likesIds.add(mUserId);
                                }
                                else{
                                    likes -= 1;
                                    likesIds.remove(mUserId);
                                }
                                comment.put(ParseConstants.KEY_COMMENTS_LIKES_RELATION, likesIds);
                                comment.saveInBackground();
                                mLikes.setText(String.valueOf(likes));
                            }
                        }
                    });
                }
            });
            // Report a message
            mReport.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("message/rfc822");
                    i.putExtra(Intent.EXTRA_EMAIL, new String[]{"reports@bondzu.com"});
                    i.putExtra(Intent.EXTRA_SUBJECT, "Report");
                    i.putExtra(Intent.EXTRA_TEXT, "Message report with ID " + message.getObjectId());
                    try {
                        mContext.startActivity(Intent.createChooser(i, "Send mail..."));
                    } catch (android.content.ActivityNotFoundException ex) {
                        Toast.makeText(mContext, R.string.no_email_clients, Toast.LENGTH_SHORT).show();
                    }
                }
            });
            // Replay this message
            mReply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent independetMessage = new Intent(mContext, IndependentMessage.class);
                    independetMessage.putExtra(ParseConstants.KEY_COMMENTS_MESSAGE, message.getObjectId());
                    mContext.startActivity(independetMessage);
                }
            });

            // Check if an image is attached
            if (message.getParseFile(ParseConstants.KEY_COMMENTS_PHOTO) != null) {
                Picasso.with(mContext).load(R.drawable.ic_photo_library_grey_24dp).into(mAttach);
            }
            else {
                Picasso.with(mContext).load(R.drawable.null_object).into(mAttach);
            }
        }

        private boolean checkOwnLikes(ArrayList<String> likesIds) {
            for(String id : likesIds) {
                if(id.equals(mUserId)) {
                    return true;
                }
            }
            return false;
        }

        public ArrayList<String> getLikesIds(ParseObject comment) {
            List<String> likes = comment.getList(ParseConstants.KEY_COMMENTS_LIKES_RELATION);
            if (likes != null) {
                return (ArrayList<String>) likes;
            }
            else {
                return new ArrayList<String>();
            }
        }
    }
}
