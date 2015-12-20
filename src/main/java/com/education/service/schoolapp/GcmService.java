package com.education.service.schoolapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Base64;
import android.util.Log;

import com.education.connection.schoolapp.JSONUtility;
import com.education.connection.schoolapp.NetworkConnectionUtility;
import com.education.connection.schoolapp.NetworkConstants;
import com.education.database.schoolapp.SchoolDataConstants;
import com.education.schoolapp.HomeMainActivity;
import com.education.schoolapp.R;
import com.google.android.gms.gcm.GcmListenerService;
import com.google.common.base.Joiner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by Sainath on 08-11-2015.
 */
public class GcmService extends GcmListenerService {

    private static final String TAG = GcmService.class.toString();
    private NetworkConnectionUtility networkConn;
    private static final String SHARED_MSG_VIEW = "schoolChatMsgView";
    private static final String APP_SHARED_PREFS = "school_preferences";
    private HashMap<String, ContentValues> mAlbumMessageMap = new HashMap<String, ContentValues>();

    public GcmService() {
        super();
    }

    @Override
    public void onMessageReceived(String from, Bundle data) {
        /*String message = data.getString("message");
        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Message: " + message);

        if (from.startsWith("/topics/")) {
            // message received from some topic.
        } else {
            // normal downstream message.
        }

        sendNotification(message);*/
        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Message: " + data);
        //Toast.makeText(getApplicationContext(), "GCM Message Received from "+from, Toast.LENGTH_SHORT).show();

        if (data != null && data.getString("message_id") != null) {
            try {
                JSONObject jsonObject = new JSONObject(data.getString("message_id"));
                String msg_id = jsonObject.getString("$oid");
                Log.d(TAG, "Message Id : " + msg_id);

                networkConn = new NetworkConnectionUtility();

                NetworkResp networkResp = new NetworkResp();
                networkConn.setNetworkListener(networkResp);

                networkConn.getMessage(msg_id);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private class NetworkResp implements NetworkConnectionUtility.NetworkResponseListener {
        @Override
        public void onResponse(String urlString, String networkResult) {
            if (urlString.startsWith(NetworkConstants.GET_MESSAGE)) {
                if (networkResult == null) {
                    Log.i("Network", "Get Message API");
                    return;
                }
                try {
                    JSONObject messageObj = new JSONObject(networkResult);
                    String[] messageProjection = {"subject","body", "sender_id", "group_id", "start_date", "end_date", "message_type"};
                    JSONUtility jsonUtility = new JSONUtility();
                    jsonUtility.setColumsList(messageProjection);

                    ContentValues msgValues = jsonUtility.fromJSON(messageObj);
                    msgValues.put("message_id", messageObj.getJSONObject("_id").getString("$oid"));

                    JSONArray membersArray = messageObj.getJSONArray("members");
                    String[] memberIds = new String[membersArray.length()];
                    String[] memberNames = new String[membersArray.length()];
                    for (int i = 0; i < memberIds.length; i++) {
                        memberIds[i] = membersArray.getJSONObject(i).getJSONObject("_id").getString("$oid");
                        memberNames[i] = membersArray.getJSONObject(i).getString("member_name");
                    }
                    /*HashMap<String, String> mStudentsMap = new SchoolDataUtility().getClassStudents(getApplicationContext());
                    if (mStudentsMap != null) {
                        for (int i = 0; i < memberIds.length; i++) {
                            memberIds[i] = membersArray.getJSONObject(i).getString("$oid");
                            for (Map.Entry<String, String> entry : mStudentsMap.entrySet()) {
                                if (entry.getValue().equals(memberIds[i])) {
                                    System.out.println(entry.getKey());
                                    memberNames[i] = entry.getKey();
                                }
                            }
                        }
                    }*/

                    String memberIdString = Joiner.on(",").skipNulls().join(memberIds);
                    msgValues.put("member_ids", memberIdString);
                    msgValues.put("member_names", Joiner.on(",").skipNulls().join(memberNames));
                    msgValues.put("members_count", membersArray.length());
                    msgValues.put("sender_profile_image", Base64.decode(messageObj.getString("sender_profile_image"), 0));
                    msgValues.put("sender_name", messageObj.getString("sender_name"));

                    if (messageObj.getString("album_ids") != null && !messageObj.getString("album_ids").equalsIgnoreCase("null")) {
                        JSONArray albumArray = messageObj.getJSONArray("album_ids");
                        if (albumArray != null) {
                            String[] albumIds = new String[albumArray.length()];
                            for (int i = 0; i < albumIds.length; i++) {
                                albumIds[i] = albumArray.getString(i);
                                /*mAlbumIds.add( mAlbumIdx, albumArray.getString(i));
                                mAlbumIdx++;*/
                                mAlbumMessageMap.put(albumIds[i], msgValues);

                                networkConn.getAlbum(albumIds[i]);
                            }
                            msgValues.put("album_id", Joiner.on(",").skipNulls().join(albumIds));
                        }
                    }

                    //TODO - album insert to Messages table yet to be done similar to Home main activity.

                    if (!msgValues.containsKey("album_id")) {
                        getContentResolver().insert(Uri.parse("content://com.education.schoolapp/received_messages_all"), msgValues);
                    } else {
                        Log.i("Sainath", "Album id is present");
                    }

                    getContentResolver().notifyChange(Uri.parse("content://com.education.schoolapp/received_messages_all"), null);

                    ContentValues msgIdUpdate = new ContentValues();
                    String selection = " message_id like '" + messageObj.getJSONObject("_id").getString("$oid") + "'";
                    msgIdUpdate.put("status", 1);

                    getContentResolver().update(Uri.parse("content://com.education.schoolapp/server_message_ids"), msgIdUpdate, selection, null);

                    SharedPreferences sharePrefs = getApplicationContext().getSharedPreferences(APP_SHARED_PREFS, Context.MODE_PRIVATE);

                    if (!sharePrefs.getBoolean(SHARED_MSG_VIEW, true)) {
                        Intent notiIntent = new Intent(getApplicationContext(), HomeMainActivity.class);
                        notiIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        notiIntent.putExtra("message_type", msgValues.getAsInteger("message_type"));

                        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notiIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT);
                        NotificationManager mNotiManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                        Notification.Builder mNotiBuilder = new Notification.Builder(getApplicationContext());

                        Notification noti = mNotiBuilder.setContentTitle(msgValues.getAsString("sender_name"))
                                .setContentText(msgValues.getAsString("subject"))
                                .setSmallIcon(R.drawable.ic_school_black_36dp)
                                .setContentIntent(contentIntent)
                                .setAutoCancel(true)
                                .build();
                        noti.defaults |= Notification.DEFAULT_ALL;

                        mNotiManager.notify(10, noti);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (urlString.startsWith(NetworkConstants.GET_ALBUM)) {
                if (networkResult == null || networkResult.equalsIgnoreCase("Bad Request")) {
                    Log.i("Network", "Get Album API");
                }
                try {
                    JSONObject albumObj = new JSONObject(networkResult);
                    JSONArray albumArray = albumObj.getJSONArray("multimediums");
                    if (albumArray != null) {
                        int imageLength = albumArray.length();
                        if (imageLength > 0) {
                            ContentValues[] albumValues = new ContentValues[imageLength];
                            ContentValues albumMsgUpdate = new ContentValues();
                            String albumName = "";
                            for (int albIdx = 0; albIdx < imageLength; albIdx++) {
                                albumValues[albIdx] = new ContentValues();

                                albumValues[albIdx].put("type", "Received");
                                albumName = albumObj.getString("name");
                                albumValues[albIdx].put("album_name", albumName);
                                String albumId = albumObj.getJSONObject("_id").getString("$oid");
                                albumValues[albIdx].put("album_id", albumId);
                                albumValues[albIdx].put("image_id", albumArray.getJSONObject(albIdx).getJSONObject("_id").getString("$oid"));

                                ContentValues msgValues = mAlbumMessageMap.get(albumId);

                                msgValues.remove("message_id");
                                albumValues[albIdx].putAll(msgValues);
                            }
                            //TODO - To be checked again
                            //getContentResolver().bulkInsert(Uri.parse(SchoolDataConstants.CONTENT_URI + SchoolDataConstants.ALBUM_IMAGES), albumValues);

                            getContentResolver().bulkInsert(Uri.parse(SchoolDataConstants.CONTENT_URI + SchoolDataConstants.RECEIVED_MESSAGES_ALL), albumValues);

                            /*albumMsgUpdate.put("read_status", 0);
                            String selection = " album_name like '" + albumName + "'";

                            getContentResolver().update(Uri.parse(SchoolDataConstants.CONTENT_URI + SchoolDataConstants.RECEIVED_MESSAGES_ALL),
                                    albumMsgUpdate, selection, null);*/
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
            @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }

    @Override
    public void onMessageSent(String msgId) {
        super.onMessageSent(msgId);
        Log.i(TAG, "Sent message to " + msgId);
        //Toast.makeText(getApplicationContext(), "GCM Message Sent to "+msgId, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSendError(String msgId, String error) {
        super.onSendError(msgId, error);
    }

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String message) {
        Intent intent = new Intent(this, HomeMainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_ic_notification)
                .setContentTitle("GCM Message")
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

}
