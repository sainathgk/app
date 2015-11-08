package com.education.service.schoolapp;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.education.database.schoolapp.MessageItem;
import com.education.database.schoolapp.SchoolDataUtility;

import java.util.ArrayList;

/**
 * Created by Sainath on 24-10-2015.
 */
public class SchoolDataContentObserver extends ContentObserver {
    private final Context mContext;

    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */
    public SchoolDataContentObserver(Handler handler, Context ctx) {
        super(handler);
        mContext = ctx;
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        if (uri.equals(Uri.parse("content://com.education.schoolapp/received_notifications"))) {
            Log.i("Sainath", "Change in Notifications Table");

            ArrayList<MessageItem> notifications = new SchoolDataUtility("",false).getAllNotifications(mContext);
            MessageItem notiItem = notifications.get(notifications.size()-1);

            NotificationService notiService = new NotificationService(mContext);
            notiService.setNotificationMsg(notiItem.msgTitle, notiItem.msgDescription);

            //notiService.buildAndNotify();
        }
        super.onChange(selfChange, uri);
    }
}
