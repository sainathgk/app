package com.education.service.schoolapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Base64;
import android.util.Log;

import com.education.connection.schoolapp.JSONUtility;
import com.education.connection.schoolapp.NetworkConnectionUtility;
import com.education.connection.schoolapp.NetworkConstants;
import com.education.database.schoolapp.SchoolDataUtility;
import com.education.schoolapp.HomeMainActivity;
import com.education.schoolapp.R;
import com.google.android.gms.gcm.GcmListenerService;
import com.google.common.base.Joiner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Sainath on 08-11-2015.
 */
public class GcmService extends GcmListenerService {

    private static final String TAG = GcmService.class.toString();
    private NetworkConnectionUtility networkConn;

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

        if (data != null && data.getBundle("message_id") != null) {
            String msg_id = data.getBundle("message_id").getString("$oid");
            Log.d(TAG, "Message Id : " + msg_id);

            networkConn = new NetworkConnectionUtility();

            NetworkResp networkResp = new NetworkResp();
            networkConn.setNetworkListener(networkResp);

            networkConn.getMessage(msg_id);
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
                    String[] messageProjection = {"subject","body", "sender_id", /*"start_date", "end_date", */"message_type"};
                    JSONUtility jsonUtility = new JSONUtility();
                    jsonUtility.setColumsList(messageProjection);

                    ContentValues msgValues = jsonUtility.fromJSON(messageObj);
                    msgValues.put("message_id", messageObj.getJSONObject("_id").getString("$oid"));

                    JSONArray membersArray = messageObj.getJSONArray("member_ids");
                    String[] memberIds = new String[membersArray.length()];
                    String[] memberNames = new String[membersArray.length()];
                    HashMap<String, String> mStudentsMap = new SchoolDataUtility().getClassStudents(getApplicationContext());
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
                    }

                    String memberIdString = Joiner.on(",").skipNulls().join(memberIds);
                    msgValues.put("member_ids", memberIdString);
                    msgValues.put("member_names", Joiner.on(",").skipNulls().join(memberNames));
                    msgValues.put("sender_profile_image", Base64.decode(messageObj.getString("sender_profile_image"), 0));
                    //TODO : To be fixed, get the sender name from the Message Response only
                    //msgValues.put("sender_name", new SchoolDataUtility().getTeacherNameforStudent(getApplicationContext(), LoginName));
                    msgValues.put("sender_name", messageObj.getString("sender_id"));

                    getContentResolver().insert(Uri.parse("content://com.education.schoolapp/received_messages_all"), msgValues);
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
