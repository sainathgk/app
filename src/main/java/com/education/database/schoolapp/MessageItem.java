package com.education.database.schoolapp;

import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Sainath on 24-10-2015.
 */
public class MessageItem {
    public String msgTitle;
    public String msgDescription;
    public String msgFrom;
    public String msgTo;
    public String msgDate;
    public boolean msgAttachment = false;
    public short msgReadStatus = 0;
    public byte[] msgFromImage;
    public String msgId;

    public String toString() {
        JSONObject msgJson = new JSONObject();
        try {
            msgJson.put("msgTitle", msgTitle);
            msgJson.put("msgDescription", msgDescription);
            msgJson.put("msgDate", msgDate);
            msgJson.put("msgSenderImage", Base64.encodeToString(msgFromImage, 0));
            msgJson.put("msgFrom", msgFrom);
            msgJson.put("msgTo",msgTo);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return msgJson.toString();
    }
}
