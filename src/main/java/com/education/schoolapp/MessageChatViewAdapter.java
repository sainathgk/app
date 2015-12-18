package com.education.schoolapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.education.database.schoolapp.MessageItem;
import com.education.database.schoolapp.SchoolDataUtility;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.utils.StorageUtils;

import java.io.File;
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

    private final int MESSAGE_RECEIVED = 0;
    private final int MESSAGE_SENT = 1;
    private final int IMAGE_RECEIVED = 2;
    private final int IMAGE_SENT = 3;
    private ImageLoader imageLoader;
    private Context mContext;

    public class ChatViewHolder extends RecyclerView.ViewHolder {
        public ImageView mSenderImage;
        public TextView mMsgFrom;
        public TextView mMsgTitle;
        public TextView mMsgTimeStamp;
        public ImageView mAttachmentView;
        public View mMsgItemView;
        public ImageView mMsgImage;

        public ChatViewHolder(View itemView) {
            super(itemView);
            mMsgItemView = itemView;
        }
    }

    private void initImageLoader(Context ctx) {
        mContext = ctx;
        try {
            String CACHE_DIR = Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + "/.temp_tmp";
            new File(CACHE_DIR).mkdirs();

            File cacheDir = StorageUtils.getOwnCacheDirectory(ctx,
                    CACHE_DIR);

            DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                    .cacheOnDisc(true).imageScaleType(ImageScaleType.EXACTLY)
                    .bitmapConfig(Bitmap.Config.RGB_565).build();
            ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(
                    ctx)
                    .defaultDisplayImageOptions(defaultOptions)
                    .discCache(new UnlimitedDiscCache(cacheDir))
                    .memoryCache(new WeakMemoryCache());

            ImageLoaderConfiguration config = builder.build();
            imageLoader = ImageLoader.getInstance();
            imageLoader.init(config);

        } catch (Exception e) {

        }
    }

    public void updateData(Context mContext, String senderId) {
        initImageLoader(mContext);
        sharePrefs = mContext.getSharedPreferences(APP_SHARED_PREFS, Context.MODE_PRIVATE);

        loginName = sharePrefs.getString(SHARED_LOGIN_NAME, "");
        mSchoolDB = new SchoolDataUtility(loginName, false);
        msgData = mSchoolDB.getChatMessages(mContext, senderId);
    }

    @Override
    public int getItemViewType(int position) {
        int messageType = msgData.get(position).msgType;

        if (msgData.get(position).msgFromId.equalsIgnoreCase(loginName)) {
            if (messageType == 3 || messageType == 4) {
                return IMAGE_SENT;
            } else if (messageType == 1) {
                return MESSAGE_SENT;
            }
        } else {
            if (messageType == 3 || messageType == 4) {
                return IMAGE_RECEIVED;
            } else if (messageType == 1) {
                return MESSAGE_RECEIVED;
            }
        }
        return -1;
    }

    @Override
    public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        if (viewType == MESSAGE_SENT) {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_chat_right_bubble, parent, false);
        } else if (viewType == MESSAGE_RECEIVED) {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_chat_bubble, parent, false);
        }

        switch (viewType) {
            case MESSAGE_SENT:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_chat_right_bubble, parent, false);
                break;

            case MESSAGE_RECEIVED:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_chat_bubble, parent, false);
                break;

            case IMAGE_SENT:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_chat_right_bubble, parent, false);
                break;

            case IMAGE_RECEIVED:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_chat_bubble, parent, false);
                break;

            default:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_chat_right_bubble, parent, false);
                break;
        }

        ChatViewHolder cvh = new ChatViewHolder(itemView);

        cvh.mSenderImage = (ImageView) itemView.findViewById(R.id.person_image);
        cvh.mMsgFrom = (TextView) itemView.findViewById(R.id.textUser);
        cvh.mMsgTitle = (TextView) itemView.findViewById(R.id.textMessage);
        cvh.mMsgTimeStamp = (TextView) itemView.findViewById(R.id.textTime);

        if (viewType == MESSAGE_SENT || viewType == MESSAGE_RECEIVED) {
            cvh.mMsgTitle = (TextView) itemView.findViewById(R.id.textMessage);
        } else if (viewType == IMAGE_SENT || viewType == IMAGE_RECEIVED) {
            cvh.mMsgImage = (ImageView) itemView.findViewById(R.id.imageChat);
            cvh.mMsgTitle = (TextView) itemView.findViewById(R.id.imageTitle);
            cvh.mMsgImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String imagePath = "file://" + (String) v.getTag();

                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(imagePath), "image/*");

                    mContext.startActivity(intent);
                }
            });
        }

        return cvh;
    }

    @Override
    public void onBindViewHolder(final ChatViewHolder holder, int position) {
        int messageType = getItemViewType(position);

        holder.mMsgFrom.setText(msgData.get(position).msgFrom);
        holder.mMsgTitle.setText(msgData.get(position).msgTitle);
        holder.mMsgTimeStamp.setText(HomeMainActivity.getDateString(msgData.get(position).msgDate));
        byte[] senderImageBlob = msgData.get(position).msgFromImage;
        if (senderImageBlob != null) {
            holder.mSenderImage.setImageBitmap(HomeMainActivity.GetBitmapClippedCircle(BitmapFactory.decodeByteArray(senderImageBlob, 0, senderImageBlob.length)));
        }

        if (messageType == MESSAGE_SENT || messageType == MESSAGE_RECEIVED) {
            holder.mMsgTitle.setText(msgData.get(position).msgTitle);
        } else if (messageType == IMAGE_SENT || messageType == IMAGE_RECEIVED) {
            holder.mMsgTitle.setText(msgData.get(position).msgImageName);
            imageLoader.displayImage("file://" + msgData.get(position).msgImagePath,
                    holder.mMsgImage, new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingStarted(String imageUri, View view) {
                            holder.mMsgImage
                                    .setImageResource(R.drawable.no_media);
                            super.onLoadingStarted(imageUri, view);
                        }
                    });
            holder.mMsgImage.setTag(msgData.get(position).msgImagePath);
        }
    }

    @Override
    public int getItemCount() {
        if (msgData != null)
            return msgData.size();

        return 0;
    }
}
