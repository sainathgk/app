package com.education.schoolapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.education.database.schoolapp.MessageItem;
import com.education.database.schoolapp.SchoolDataUtility;

import java.util.ArrayList;

/**
 * Created by Sainath on 05-12-2015.
 */
public class MessageChatViewAdapter extends RecyclerView.Adapter<MessageChatViewAdapter.ChatViewHolder> {

    private SchoolDataUtility mSchoolDB;
    private ArrayList<MessageItem> msgData;
    private SharedPreferences sharePrefs;

    private static final String APP_SHARED_PREFS = "school_preferences";
    private static final String SHARED_LOGIN_NAME = "schoolUserLoginName";
    private String loginName;

    public class ChatViewHolder extends RecyclerView.ViewHolder {
        public ImageView mSenderImage;
        public TextView mMsgFrom;
        public TextView mMsgTitle;
        public TextView mMsgTimeStamp;
        public ImageView mAttachmentView;
        public View mMsgItemView;

        public ChatViewHolder(View itemView) {
            super(itemView);
            mMsgItemView = itemView;
        }
    }

    public void updateData(Context mContext, String senderId) {
        sharePrefs = mContext.getSharedPreferences(APP_SHARED_PREFS, Context.MODE_PRIVATE);

        loginName = sharePrefs.getString(SHARED_LOGIN_NAME, "");
        mSchoolDB = new SchoolDataUtility(loginName, false);
        msgData = mSchoolDB.getChatMessages(mContext, senderId);
    }

    @Override
    public int getItemViewType(int position) {
        if (msgData.get(position).msgFromId.equalsIgnoreCase(loginName)) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        if (viewType == 1) {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_chat_right_bubble, parent, false);
        } else {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_chat_bubble, parent, false);
        }

        ChatViewHolder cvh = new ChatViewHolder(itemView);

        cvh.mSenderImage = (ImageView) itemView.findViewById(R.id.person_image);
        cvh.mMsgFrom = (TextView) itemView.findViewById(R.id.textUser);
        cvh.mMsgTitle = (TextView) itemView.findViewById(R.id.textMessage);
        cvh.mMsgTimeStamp = (TextView) itemView.findViewById(R.id.textTime);

        return cvh;
    }

    @Override
    public void onBindViewHolder(ChatViewHolder holder, int position) {

        holder.mMsgFrom.setText(msgData.get(position).msgFrom);
        holder.mMsgTitle.setText(msgData.get(position).msgTitle);
        holder.mMsgTimeStamp.setText(msgData.get(position).msgDate);
        byte[] senderImageBlob = msgData.get(position).msgFromImage;
        if (senderImageBlob != null) {
            holder.mSenderImage.setImageBitmap(HomeMainActivity.GetBitmapClippedCircle(BitmapFactory.decodeByteArray(senderImageBlob, 0, senderImageBlob.length)));
        }
    }

    @Override
    public int getItemCount() {
        if (msgData != null)
            return msgData.size();

        return 0;
    }
}
