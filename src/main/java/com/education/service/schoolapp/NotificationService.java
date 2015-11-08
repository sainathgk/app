package com.education.service.schoolapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.education.schoolapp.HomeMainActivity;
import com.education.schoolapp.R;

/**
 * Created by Sainath on 23-10-2015.
 */
public class NotificationService {
    private Context mContext = null;
    private String mNotificationTitle;
    private String mNotificationDesc;
    private Drawable mNotificationSmallIcon;
    private Bitmap mNotificationBigIcon;
    private Class mIntentClass = HomeMainActivity.class;
    private NotificationManager mNotiManager;
    private Notification.Builder mNotiBuilder;

    public NotificationService(Context mContext) {
        this.mContext = mContext;
        mNotiManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotiBuilder = new Notification.Builder(mContext);
    }

    public void setNotificationMsg(String title, String desc) {
        this.mNotificationTitle = title;
        this.mNotificationDesc = desc;
    }

    public void setNotificationIcons(Drawable smallIcon, Bitmap bigIcon) {
        this.mNotificationSmallIcon = smallIcon;
        this.mNotificationBigIcon = bigIcon;
    }

    public void buildAndNotify() {
        Intent notiIntent = new Intent(mContext, mIntentClass);

        PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, notiIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Notification noti = mNotiBuilder.setContentTitle(mNotificationTitle)
                .setContentText(mNotificationDesc)
                .setSmallIcon(R.drawable.ic_person_black_36dp)
                .setLargeIcon(mNotificationBigIcon)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .build();

        mNotiManager.notify(10, noti);
    }
}
