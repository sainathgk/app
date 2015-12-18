package com.education.schoolapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.education.database.schoolapp.MessageItem;
import com.education.database.schoolapp.SchoolDataUtility;

import java.util.ArrayList;

/**
 * Created by Sainath on 18-10-2015.
 */
public class MessagesListAdapter extends RecyclerView.Adapter<MessagesListAdapter.ViewHolder> {

    private final boolean mIsTeacher;
    private String mMsgBox = "Inbox";
    private int mMsgType = 1;
    private SchoolDataUtility mSchoolDB;
    private String mDataset;
    private ArrayList<MessageItem> msgData = null;
    private Context mContext;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView mSenderImage;
        public TextView mMsgFrom;
        public TextView mMsgTitle;
        public TextView mMsgDescription;
        public TextView mMsgTimeStamp;
        public ImageView mAttachmentView;
        public View mMsgItemView;

        public ViewHolder(View itemView) {
            super(itemView);
            mMsgItemView = itemView;
        }
    }

    public MessagesListAdapter(Context ctx, String dataset, String loginName, boolean isTeacher) {
        this.mDataset = dataset;
        mContext = ctx;
        mIsTeacher = isTeacher;
        mSchoolDB = new SchoolDataUtility(loginName, isTeacher);

        switch (dataset) {
            case "Messages":
                msgData = mSchoolDB.getAllMessages(ctx);
                break;

            case "Inbox":
                if (isTeacher) {
                    msgData = mSchoolDB.getSentNotifications(ctx);
                    mMsgType = 2;
                    mMsgBox = "Outbox";
                } else {
                    msgData = mSchoolDB.getSavedMessages(ctx);
                    mMsgBox = "Saved";
                }
                break;

            case "Outbox":
                msgData = mSchoolDB.getOutboxMessages(ctx);
                mMsgBox = "Outbox";
                break;

            case "Notifications":
                msgData = mSchoolDB.getAllNotifications(ctx);
                mMsgType = 2;
                break;
        }
    }

    public void updateData() {
        switch (mDataset) {
            case "Messages":
                msgData = mSchoolDB.getAllMessages(mContext);
                break;

            case "Inbox":
                if (mIsTeacher) {
                    msgData = mSchoolDB.getSentNotifications(mContext);
                    mMsgType = 2;
                    mMsgBox = "Outbox";
                } else {
                    msgData = mSchoolDB.getSavedMessages(mContext);
                    mMsgBox = "Saved";
                }
                break;

            case "Outbox":
                msgData = mSchoolDB.getOutboxMessages(mContext);
                mMsgBox = "Outbox";
                break;

            case "Notifications":
                msgData = mSchoolDB.getAllNotifications(mContext);
                mMsgType = 2;
                break;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.msg_card_layout, parent, false);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((String) v.getTag()) == null) {
                    Log.i("Sainath", "Message Tag is empty");
                    Toast.makeText(mContext, "Message Tag is empty", Toast.LENGTH_SHORT).show();
                }

                Intent messageViewIntent = new Intent();
                messageViewIntent.putExtra("msg_id", (String) v.getTag());
                messageViewIntent.putExtra("msg_box", mMsgBox);
                messageViewIntent.putExtra("msg_type", mMsgType);
                messageViewIntent.putExtra("msg_title", ((TextView) v.findViewById(R.id.msg_title)).getText());
                messageViewIntent.putExtra("msg_members", (String) v.getTag());
                if (mMsgType == 1) {
                    messageViewIntent.setClass(mContext, MessageChatViewActivity.class);
                } else if (mMsgType == 2) {
                    messageViewIntent.setClass(mContext, MessageViewActivity.class);
                }

                mContext.startActivity(messageViewIntent);
            }
        });

        ViewHolder vh = new ViewHolder(itemView);

        //vh.mMsgFrom = ((TextView) itemView.findViewById(R.id.sender_profile_text));
        vh.mMsgTitle = ((TextView) itemView.findViewById(R.id.msg_title));
        vh.mMsgDescription = ((TextView) itemView.findViewById(R.id.msg_description));
        vh.mMsgTimeStamp = ((TextView) itemView.findViewById(R.id.msg_time));
        vh.mAttachmentView = (ImageView) itemView.findViewById(R.id.attachment_icon);
        vh.mSenderImage = (ImageView) itemView.findViewById(R.id.sender_profile_image);

        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        holder.mMsgTitle.setText(msgData.get(position).msgFrom);
        holder.mMsgItemView.setTag(msgData.get(position).msgGroupId);
/*
        if (mMsgType == 1) {
            holder.mMsgItemView.setTag(msgData.get(position).msgGroupId);
        } else if (mMsgType == 2) {
            holder.mMsgItemView.setTag(msgData.get(position).msgId);
        }
*/
        holder.mMsgDescription.setText(msgData.get(position).msgTitle);
        holder.mMsgTimeStamp.setText(HomeMainActivity.getDateString(msgData.get(position).msgDate));
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
//            holder.mMsgFrom.setVisibility(View.GONE);
            holder.mSenderImage.setVisibility(View.VISIBLE);
        } else {
  /*          String senderText = "";
            if (mMsgBox.equalsIgnoreCase("Inbox")) {
                senderText = msgData.get(position).msgFrom.substring(0,1);
            } else {
                senderText = msgData.get(position).msgTo.substring(0,1);
            }
            holder.mMsgFrom.setText(senderText);*/
            //holder.mMsgFrom.setVisibility(View.VISIBLE);
            holder.mSenderImage.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        if (msgData != null)
            return msgData.size();

        return 0;
    }

}
