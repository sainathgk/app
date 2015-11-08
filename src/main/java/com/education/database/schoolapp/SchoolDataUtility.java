package com.education.database.schoolapp;

import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.util.ArrayList;

/**
 * Created by Sainath on 24-10-2015.
 */
public class SchoolDataUtility {
    ArrayList<MessageItem> msgArray = null;
    Cursor msgCursor = null;
    String mLoginName = null;
    boolean mIsTeacher = false;

    public SchoolDataUtility(String loginName, boolean isTeacher) {
        mLoginName = loginName;
        mIsTeacher = isTeacher;
    }

    public ArrayList<MessageItem> getAllMessages(Context context) {
        String[] messageProjection = {"from_name", "from_image", "msg_title", "msg_description",
                "msg_date", "msg_attachment_path", "read_status"};
        String selection = null;

        if (mIsTeacher) {
            selection = " to_name like '" + mLoginName + "' ";
        } else {
            selection = " to_name like '" + mLoginName + "' or to_name like 'all' or to_name like 'All' ";
        }

        msgCursor = context.getContentResolver().query(Uri.parse("content://com.education.schoolapp/received_messages"),
                messageProjection, selection, null, "msg_date DESC");
        if (msgCursor != null && msgCursor.getCount() > 0) {
            msgArray = new ArrayList<>(msgCursor.getCount());

            while (msgCursor.moveToNext()) {
                MessageItem msgItem = new MessageItem();
                msgItem.msgFromTo = msgCursor.getString(msgCursor.getColumnIndex("from_name")).substring(0, 1);
                msgItem.msgTitle = msgCursor.getString(msgCursor.getColumnIndex("msg_title"));
                msgItem.msgDescription = msgCursor.getString(msgCursor.getColumnIndex("msg_description"));
                msgItem.msgDate = msgCursor.getString(msgCursor.getColumnIndex("msg_date"));
                msgItem.msgAttachment = (msgCursor.getString(msgCursor.getColumnIndex("msg_attachment_path")) != null) ? true : false;
                msgItem.msgReadStatus = msgCursor.getShort(msgCursor.getColumnIndex("read_status"));
                msgItem.msgFromImage = msgCursor.getBlob(msgCursor.getColumnIndex("from_image"));

                msgArray.add(msgItem);
            }
        }
        if (msgCursor != null) {
            msgCursor.close();
        }

        return msgArray;
    }

    public ArrayList<MessageItem> getSavedMessages(Context context) {
        String[] messageProjection = {"from_name", "from_image", "msg_title", "msg_description",
                "msg_date", "msg_attachment_path", "read_status"};
        Cursor msgCursor = null;
        String selection = null;

        if (mIsTeacher) {
            selection = " to_name like '" + mLoginName + "' ";
        } else {
            selection = " ( to_name like '" + mLoginName + "' or to_name like 'all' or to_name like 'All' ) ";
        }

        msgCursor = context.getContentResolver().query(Uri.parse("content://com.education.schoolapp/received_messages"),
                messageProjection, " saved = 1 and " + selection, null, "msg_date DESC");
        if (msgCursor != null && msgCursor.getCount() > 0) {
            msgArray = new ArrayList<>(msgCursor.getCount());

            while (msgCursor.moveToNext()) {
                MessageItem msgItem = new MessageItem();
                msgItem.msgFromTo = msgCursor.getString(msgCursor.getColumnIndex("from_name")).substring(0, 1);
                msgItem.msgTitle = msgCursor.getString(msgCursor.getColumnIndex("msg_title"));
                msgItem.msgDescription = msgCursor.getString(msgCursor.getColumnIndex("msg_description"));
                msgItem.msgDate = msgCursor.getString(msgCursor.getColumnIndex("msg_date"));
                msgItem.msgAttachment = (msgCursor.getString(msgCursor.getColumnIndex("msg_attachment_path")) != null) ? true : false;
                msgItem.msgReadStatus = msgCursor.getShort(msgCursor.getColumnIndex("read_status"));
                msgItem.msgFromImage = msgCursor.getBlob(msgCursor.getColumnIndex("from_image"));

                msgArray.add(msgItem);
            }
        }
        if (msgCursor != null) {
            msgCursor.close();
        }

        return msgArray;
    }

    public ArrayList<MessageItem> getOutboxMessages(Context context) {
        String[] messageProjection = {"to_name", "msg_title", "msg_description",
                "msg_date", "msg_attachment_path", "from_image"};
        Cursor msgCursor = null;
        String selection = null;

        selection = " from_name like '" + mLoginName + "' ";

        msgCursor = context.getContentResolver().query(Uri.parse("content://com.education.schoolapp/sent_messages"),
                messageProjection, selection, null, "msg_date DESC");
        if (msgCursor != null && msgCursor.getCount() > 0) {
            msgArray = new ArrayList<>(msgCursor.getCount());

            while (msgCursor.moveToNext()) {
                MessageItem msgItem = new MessageItem();
                msgItem.msgFromTo = msgCursor.getString(msgCursor.getColumnIndex("to_name")).substring(0, 1);
                msgItem.msgTitle = msgCursor.getString(msgCursor.getColumnIndex("msg_title"));
                msgItem.msgDescription = msgCursor.getString(msgCursor.getColumnIndex("msg_description"));
                msgItem.msgDate = msgCursor.getString(msgCursor.getColumnIndex("msg_date"));
                msgItem.msgAttachment = (msgCursor.getString(msgCursor.getColumnIndex("msg_attachment_path")) != null) ? true : false;
                msgItem.msgFromImage = msgCursor.getBlob(msgCursor.getColumnIndex("from_image"));

                msgArray.add(msgItem);
            }
        }
        if (msgCursor != null) {
            msgCursor.close();
        }

        return msgArray;
    }

    public ArrayList<MessageItem> getAllNotifications(Context context) {
        String[] messageProjection = {"from_name", "from_image", "noti_title", "noti_description",
                "noti_date", "read_status"};
        Cursor msgCursor = null;
        String selection = null;

        selection = " to_name like '" + mLoginName + "' or to_name like 'all' or to_name like 'All' ";

        msgCursor = context.getContentResolver().query(Uri.parse("content://com.education.schoolapp/received_notifications"),
                messageProjection, selection, null, "noti_date DESC");
        if (msgCursor != null && msgCursor.getCount() > 0) {
            msgArray = new ArrayList<>(msgCursor.getCount());

            while (msgCursor.moveToNext()) {
                MessageItem msgItem = new MessageItem();
                msgItem.msgFromTo = msgCursor.getString(msgCursor.getColumnIndex("from_name")).substring(0, 1);
                msgItem.msgTitle = msgCursor.getString(msgCursor.getColumnIndex("noti_title"));
                msgItem.msgDescription = msgCursor.getString(msgCursor.getColumnIndex("noti_description"));
                msgItem.msgDate = msgCursor.getString(msgCursor.getColumnIndex("noti_date"));
                msgItem.msgReadStatus = msgCursor.getShort(msgCursor.getColumnIndex("read_status"));
                msgItem.msgFromImage = msgCursor.getBlob(msgCursor.getColumnIndex("from_image"));

                msgArray.add(msgItem);
            }
        }
        if (msgCursor != null) {
            msgCursor.close();
        }

        return msgArray;
    }

    public ArrayList<MessageItem> getSentNotifications(Context context) {
        String[] messageProjection = {"to_name", "noti_title", "noti_description", "noti_date", "from_image"};
        Cursor msgCursor = null;
        String selection = null;

        selection = " from_name like '" + mLoginName + "' ";

        msgCursor = context.getContentResolver().query(Uri.parse("content://com.education.schoolapp/sent_notifications"),
                messageProjection, selection, null, "noti_date DESC");
        if (msgCursor != null && msgCursor.getCount() > 0) {
            msgArray = new ArrayList<>(msgCursor.getCount());

            while (msgCursor.moveToNext()) {
                MessageItem msgItem = new MessageItem();
                msgItem.msgFromTo = msgCursor.getString(msgCursor.getColumnIndex("to_name")).substring(0, 1);
                msgItem.msgTitle = msgCursor.getString(msgCursor.getColumnIndex("noti_title"));
                msgItem.msgDescription = msgCursor.getString(msgCursor.getColumnIndex("noti_description"));
                msgItem.msgDate = msgCursor.getString(msgCursor.getColumnIndex("noti_date"));
                msgItem.msgFromImage = msgCursor.getBlob(msgCursor.getColumnIndex("from_image"));

                msgArray.add(msgItem);
            }
        }
        if (msgCursor != null) {
            msgCursor.close();
        }

        return msgArray;
    }

    public ProfileItem getStudentProfileDetails(Context context) {
        Cursor proCursor = null;

        String selection = " user_id like '" + mLoginName + "' ";
        proCursor = context.getContentResolver().query(Uri.parse("content://com.education.schoolapp/user_profile"), null, selection, null, null);

        if (proCursor != null && proCursor.getCount() > 0) {
            ProfileItem proItem = new ProfileItem();

            proCursor.moveToFirst();

            proItem.studentName = proCursor.getString(proCursor.getColumnIndex("name"));
            proItem.studentClass = proCursor.getString(proCursor.getColumnIndex("class"));
            proItem.studentSection = proCursor.getString(proCursor.getColumnIndex("section"));
            proItem.studentBloodGroup = proCursor.getString(proCursor.getColumnIndex("blood_group"));
            proItem.studentFatherName = proCursor.getString(proCursor.getColumnIndex("father_name"));
            proItem.studentFatherContact = proCursor.getString(proCursor.getColumnIndex("father_contact"));
            proItem.studentFatherEmail = proCursor.getString(proCursor.getColumnIndex("father_email"));
            proItem.studentMotherName = proCursor.getString(proCursor.getColumnIndex("mother_name"));
            proItem.studentMotherContact = proCursor.getString(proCursor.getColumnIndex("mother_contact"));
            proItem.studentMotherEmail = proCursor.getString(proCursor.getColumnIndex("mother_email"));
            proItem.studentGuardianName = proCursor.getString(proCursor.getColumnIndex("guardian_name"));
            proItem.studentGuardianContact = proCursor.getString(proCursor.getColumnIndex("guardian_contact"));
            proItem.studentGuardianEmail = proCursor.getString(proCursor.getColumnIndex("guardian_email"));
            byte[] studentBlob = proCursor.getBlob(proCursor.getColumnIndex("user_image"));
            if (studentBlob != null) {
                proItem.studentImage = BitmapFactory.decodeByteArray(studentBlob, 0, studentBlob.length);
            }
            return proItem;
        }
        return null;
    }

    public String[] getStudentName(Context context) {
        Cursor proCursor = null;
        String selection = " user_id like '" + mLoginName + "' ";
        String[] studentName = new String[]{"", ""};

        proCursor = context.getContentResolver().query(Uri.parse("content://com.education.schoolapp/user_profile"), null, selection, null, null);

        if (proCursor != null && proCursor.getCount() > 0) {
            proCursor.moveToFirst();

            studentName[0] = proCursor.getString(proCursor.getColumnIndex("name"));
            studentName[1] = proCursor.getString(proCursor.getColumnIndex("class"));

            return studentName;
        }
        return null;
    }

}
