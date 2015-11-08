package com.education.schoolapp;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.education.database.schoolapp.MessageItem;
import com.education.database.schoolapp.SchoolDataUtility;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Sainath on 18-10-2015.
 */
public class MessagesListAdapter extends RecyclerView.Adapter<MessagesListAdapter.ViewHolder> {

    private SchoolDataUtility mSchoolDB;
    private String mDataset;
    private ArrayList<MessageItem> msgData = null;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView mSenderImage;
        public TextView mMsgFrom;
        public TextView mMsgTitle;
        public TextView mMsgDescription;
        public TextView mMsgTimeStamp;
        public ImageView mAttachmentView;

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    public MessagesListAdapter(Context ctx, String dataset, String loginName, boolean isTeacher) {
        this.mDataset = dataset;
        mSchoolDB = new SchoolDataUtility(loginName, isTeacher);

        switch (dataset) {
            case "Messages":
                msgData = mSchoolDB.getAllMessages(ctx);
                break;

            case "Inbox":
                if (isTeacher) {
                    msgData = mSchoolDB.getSentNotifications(ctx);
                } else {
                    msgData = mSchoolDB.getSavedMessages(ctx);
                }
                break;

            case "Outbox":
                msgData = mSchoolDB.getOutboxMessages(ctx);
                break;

            case "Notifications":
                msgData = mSchoolDB.getAllNotifications(ctx);
                break;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.msg_card_layout, parent, false);

        ViewHolder vh = new ViewHolder(itemView);

        vh.mMsgFrom = ((TextView) itemView.findViewById(R.id.sender_profile_text));
        vh.mMsgTitle = ((TextView) itemView.findViewById(R.id.msg_title));
        vh.mMsgDescription = ((TextView) itemView.findViewById(R.id.msg_description));
        vh.mMsgTimeStamp = ((TextView) itemView.findViewById(R.id.msg_time));
        vh.mAttachmentView = (ImageView) itemView.findViewById(R.id.attachment_icon);
        vh.mSenderImage = (ImageView) itemView.findViewById(R.id.sender_profile_image);

        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        holder.mMsgTitle.setText(msgData.get(position).msgTitle);
        holder.mMsgDescription.setText(msgData.get(position).msgDescription);
        holder.mMsgTimeStamp.setText(getDateString(msgData.get(position).msgDate));
        if (msgData.get(position).msgReadStatus == 1) {
            holder.mMsgTitle.setTypeface(null, Typeface.BOLD);
            holder.mMsgDescription.setTypeface(null, Typeface.BOLD);
        } else {
            holder.mMsgTitle.setTypeface(null, Typeface.NORMAL);
            holder.mMsgDescription.setTypeface(null, Typeface.NORMAL);
        }
        if (msgData.get(position).msgAttachment) {
            holder.mAttachmentView.setVisibility(View.VISIBLE);
        } else {
            holder.mAttachmentView.setVisibility(View.GONE);
        }
        byte[] senderImageBlob = msgData.get(position).msgFromImage;
        if (senderImageBlob != null) {
            holder.mSenderImage.setImageBitmap(HomeMainActivity.GetBitmapClippedCircle(BitmapFactory.decodeByteArray(senderImageBlob, 0, senderImageBlob.length)));
            holder.mMsgFrom.setVisibility(View.GONE);
            holder.mSenderImage.setVisibility(View.VISIBLE);
        } else {
            holder.mMsgFrom.setText(msgData.get(position).msgFromTo);
            holder.mMsgFrom.setVisibility(View.VISIBLE);
            holder.mSenderImage.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        if (msgData != null)
            return msgData.size();

        return 0;
    }

    private String getDateString(String timeMilliSeconds) {
        if (timeMilliSeconds == null)
            return "";

        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MMM-yyyy hh:mm a");

        Calendar calendar = Calendar.getInstance();
        Long mLongTime = Long.parseLong(timeMilliSeconds);
        calendar.setTimeInMillis(mLongTime);

        return dateFormatter.format(calendar.getTime());
    }


}
