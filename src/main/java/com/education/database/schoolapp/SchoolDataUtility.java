package com.education.database.schoolapp;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;

import com.education.connection.schoolapp.JSONUtility;
import com.education.schoolapp.CustomGallery;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Sainath on 24-10-2015.
 */
public class SchoolDataUtility {

    String mLoginName = null;
    boolean mIsTeacher = false;

    public SchoolDataUtility(String loginName, boolean isTeacher) {
        mLoginName = loginName;
        mIsTeacher = isTeacher;
    }

    public SchoolDataUtility() {
    }

    public ArrayList<MessageItem> getAllMessages(Context context) {
        String[] messageProjection = {"message_id", "member_ids", "subject", "body", "start_date", "sender_id",
                "sender_profile_image", "read_status"};
        ArrayList<MessageItem> msgArray = null;
        Cursor msgCursor = null;
        /*String[] messageProjection = {"msg_id", "from_name", "from_image", "msg_title", "msg_description",
                "msg_date", "msg_attachment_path", "read_status"};*/
        /*String[] messageProjection = {"msg_id", "sender_id", "sender_profile_image", "subject", "body",
                "member_ids", "msg_attachment_path", "read_status"};*/
/*
        String selection = null;

        if (mIsTeacher) {
            selection = " to_name like '" + mLoginName + "' ";
        } else {
            selection = " to_name like '" + mLoginName + "' or to_name like 'all' or to_name like 'All' ";
        }
*/

        String selection = " member_ids like '%" + mLoginName + "%' and message_type = 1";


        msgCursor = context.getContentResolver().query(Uri.parse("content://com.education.schoolapp/received_messages_all"),
                messageProjection, selection, null, "start_date DESC");
        if (msgCursor != null && msgCursor.getCount() > 0) {
            msgArray = new ArrayList<>(msgCursor.getCount());

            while (msgCursor.moveToNext()) {
                MessageItem msgItem = new MessageItem();
                msgItem.msgId = msgCursor.getString(msgCursor.getColumnIndex("message_id"));
                msgItem.msgFrom = msgCursor.getString(msgCursor.getColumnIndex("sender_id"));
                msgItem.msgTo = msgCursor.getString(msgCursor.getColumnIndex("member_ids"));
                msgItem.msgTitle = msgCursor.getString(msgCursor.getColumnIndex("subject"));
                msgItem.msgDescription = msgCursor.getString(msgCursor.getColumnIndex("body"));
                msgItem.msgDate = msgCursor.getString(msgCursor.getColumnIndex("start_date"));
                /*msgItem.msgAttachment = (msgCursor.getString(msgCursor.getColumnIndex("msg_attachment_path")) != null) ? true : false;*/
                msgItem.msgReadStatus = msgCursor.getShort(msgCursor.getColumnIndex("read_status"));
                msgItem.msgFromImage = msgCursor.getBlob(msgCursor.getColumnIndex("sender_profile_image"));

                msgArray.add(msgItem);
            }
        }
        if (msgCursor != null) {
            msgCursor.close();
        }

        return msgArray;
    }

    public ArrayList<MessageItem> getSavedMessages(Context context) {
        String[] messageProjection = {"message_id", "member_ids", "subject", "body", "start_date", "sender_id",
                "sender_profile_image", "read_status"};
        ArrayList<MessageItem> msgArray = null;
        Cursor msgCursor = null;
        String selection = " member_ids like '%" + mLoginName + "%' and message_type = 1 and saved = 1";

        msgCursor = context.getContentResolver().query(Uri.parse("content://com.education.schoolapp/received_messages_all"),
                messageProjection, selection, null, "start_date DESC");
        if (msgCursor != null && msgCursor.getCount() > 0) {
            msgArray = new ArrayList<>(msgCursor.getCount());

            while (msgCursor.moveToNext()) {
                MessageItem msgItem = new MessageItem();
                msgItem.msgId = msgCursor.getString(msgCursor.getColumnIndex("message_id"));
                msgItem.msgFrom = msgCursor.getString(msgCursor.getColumnIndex("sender_id"));
                msgItem.msgTo = msgCursor.getString(msgCursor.getColumnIndex("member_ids"));
                msgItem.msgTitle = msgCursor.getString(msgCursor.getColumnIndex("subject"));
                msgItem.msgDescription = msgCursor.getString(msgCursor.getColumnIndex("body"));
                msgItem.msgDate = msgCursor.getString(msgCursor.getColumnIndex("start_date"));
                /*msgItem.msgAttachment = (msgCursor.getString(msgCursor.getColumnIndex("msg_attachment_path")) != null) ? true : false;*/
                msgItem.msgReadStatus = msgCursor.getShort(msgCursor.getColumnIndex("read_status"));
                msgItem.msgFromImage = msgCursor.getBlob(msgCursor.getColumnIndex("sender_profile_image"));

                msgArray.add(msgItem);
            }
        }
        if (msgCursor != null) {
            msgCursor.close();
        }

        return msgArray;
    }/*{
        String[] messageProjection = {"msg_id", "from_name", "from_image", "msg_title", "msg_description",
                "msg_date", "msg_attachment_path", "read_status", "to_name"};
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
                msgItem.msgId = msgCursor.getString(msgCursor.getColumnIndex("msg_id"));
                msgItem.msgFrom = msgCursor.getString(msgCursor.getColumnIndex("from_name"));
                msgItem.msgTo = msgCursor.getString(msgCursor.getColumnIndex("to_name"));
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
    }*/

    public ArrayList<MessageItem> getOutboxMessages(Context context) {
        String[] messageProjection = {"message_id", "member_ids", "subject", "body", "start_date", "sender_id", "sender_profile_image"};
        ArrayList<MessageItem> msgArray = null;
        Cursor msgCursor = null;
        String selection = " sender_id like '" + mLoginName + "' and message_type = 1";

        msgCursor = context.getContentResolver().query(Uri.parse("content://com.education.schoolapp/sent_messages_all"),
                messageProjection, selection, null, "start_date DESC");
        if (msgCursor != null && msgCursor.getCount() > 0) {
            msgArray = new ArrayList<>(msgCursor.getCount());

            while (msgCursor.moveToNext()) {
                MessageItem msgItem = new MessageItem();
                msgItem.msgId = msgCursor.getString(msgCursor.getColumnIndex("message_id"));
                msgItem.msgTo = msgCursor.getString(msgCursor.getColumnIndex("member_ids"));
                msgItem.msgFrom = msgCursor.getString(msgCursor.getColumnIndex("sender_id"));
                msgItem.msgTitle = msgCursor.getString(msgCursor.getColumnIndex("subject"));
                msgItem.msgDescription = msgCursor.getString(msgCursor.getColumnIndex("body"));
                msgItem.msgDate = msgCursor.getString(msgCursor.getColumnIndex("start_date"));
                msgItem.msgFromImage = msgCursor.getBlob(msgCursor.getColumnIndex("sender_profile_image"));

                msgArray.add(msgItem);
            }
        }
        if (msgCursor != null) {
            msgCursor.close();
        }

        return msgArray;
    }

    public ArrayList<MessageItem> getAllNotifications(Context context) {
        String[] messageProjection = {"message_id", "member_ids", "subject", "body", "start_date", "sender_id",
                "sender_profile_image", "read_status"};
        ArrayList<MessageItem> msgArray = null;
        Cursor msgCursor = null;
        String selection = " member_ids like '%" + mLoginName + "%' and message_type = 2";

        msgCursor = context.getContentResolver().query(Uri.parse("content://com.education.schoolapp/received_messages_all"),
                messageProjection, selection, null, "start_date DESC");
        if (msgCursor != null && msgCursor.getCount() > 0) {
            msgArray = new ArrayList<>(msgCursor.getCount());

            while (msgCursor.moveToNext()) {
                MessageItem msgItem = new MessageItem();
                msgItem.msgId = msgCursor.getString(msgCursor.getColumnIndex("message_id"));
                msgItem.msgFrom = msgCursor.getString(msgCursor.getColumnIndex("sender_id"));
                msgItem.msgTo = msgCursor.getString(msgCursor.getColumnIndex("member_ids"));
                msgItem.msgTitle = msgCursor.getString(msgCursor.getColumnIndex("subject"));
                msgItem.msgDescription = msgCursor.getString(msgCursor.getColumnIndex("body"));
                msgItem.msgDate = msgCursor.getString(msgCursor.getColumnIndex("start_date"));
                msgItem.msgReadStatus = msgCursor.getShort(msgCursor.getColumnIndex("read_status"));
                msgItem.msgFromImage = msgCursor.getBlob(msgCursor.getColumnIndex("sender_profile_image"));

                msgArray.add(msgItem);
            }
        }
        if (msgCursor != null) {
            msgCursor.close();
        }

        return msgArray;
        /*String[] messageProjection = {"msg_id", "from_name", "from_image", "noti_title", "noti_description",
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
                msgItem.msgId = msgCursor.getString(msgCursor.getColumnIndex("msg_id"));
                msgItem.msgFrom = msgCursor.getString(msgCursor.getColumnIndex("from_name"));
                msgItem.msgTo = msgCursor.getString(msgCursor.getColumnIndex("to_name"));
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

        return msgArray;*/
    }

    public ArrayList<MessageItem> getSentNotifications(Context context) {
        /*String[] messageProjection = {"msg_id", "to_name", "noti_title", "noti_description", "noti_date", "from_image", "from_name"};*/
        String[] messageProjection = {"message_id", "member_ids", "subject", "body", "start_date", "sender_id", "sender_profile_image"};
        ArrayList<MessageItem> msgArray = null;
        Cursor msgCursor = null;
        String selection = " sender_id like '" + mLoginName + "' and message_type = 2";

        msgCursor = context.getContentResolver().query(Uri.parse("content://com.education.schoolapp/sent_messages_all"),
                messageProjection, selection, null, "start_date DESC");
        if (msgCursor != null && msgCursor.getCount() > 0) {
            msgArray = new ArrayList<>(msgCursor.getCount());

            while (msgCursor.moveToNext()) {
                MessageItem msgItem = new MessageItem();
                msgItem.msgId = msgCursor.getString(msgCursor.getColumnIndex("message_id"));
                msgItem.msgTo = msgCursor.getString(msgCursor.getColumnIndex("member_ids"));
                msgItem.msgFrom = msgCursor.getString(msgCursor.getColumnIndex("sender_id"));
                msgItem.msgTitle = msgCursor.getString(msgCursor.getColumnIndex("subject"));
                msgItem.msgDescription = msgCursor.getString(msgCursor.getColumnIndex("body"));
                msgItem.msgDate = msgCursor.getString(msgCursor.getColumnIndex("start_date"));
                msgItem.msgFromImage = msgCursor.getBlob(msgCursor.getColumnIndex("sender_profile_image"));

                msgArray.add(msgItem);
            }
        }
        if (msgCursor != null) {
            msgCursor.close();
        }

        return msgArray;
    }

    public String[] getAllAlbumMessages(Context context) {
        String[] messageProjection = {"album_ids"};
        String[] msgArray = null;
        Cursor msgCursor = null;
        String selection = " member_ids like '%" + mLoginName + "%' and message_type = 4";

        msgCursor = context.getContentResolver().query(Uri.parse("content://com.education.schoolapp/received_messages_all"),
                messageProjection, selection, null, null);
        if (msgCursor != null && msgCursor.getCount() > 0) {
            msgArray = new String[msgCursor.getCount()];
            int i = 0;

            while (msgCursor.moveToNext()) {
                msgArray[i] = msgCursor.getString(msgCursor.getColumnIndex("album_ids"));
                i++;
            }
        }
        if (msgCursor != null) {
            msgCursor.close();
        }

        return msgArray;
    }


    public String[] getPendingMessages(Context context) {
        String[] pendingMsgs = null;
        String[] projection = {"message_id"};
        String selection = " status = 0 and message_type == 1 or message_type == 2 ";
        Cursor msgCursor = null;

        msgCursor = context.getContentResolver().query(Uri.parse("content://com.education.schoolapp/server_message_ids"), projection, selection, null, null);
        if (msgCursor != null && msgCursor.getCount() > 0) {
            pendingMsgs = new String[msgCursor.getCount()];
            int idx = 0;
            while (msgCursor.moveToNext()) {
                pendingMsgs[idx] = msgCursor.getString(msgCursor.getColumnIndex("message_id"));
                idx++;
            }
            msgCursor.close();
        }

        return pendingMsgs;
    }

    public String[] getPendingAlbum(Context context) {
        String[] pendingMsgs = null;
        String[] projection = {"album_ids"};
        String selection = " read_status = 1 and message_type = 3";
        Cursor msgCursor = null;

        msgCursor = context.getContentResolver().query(Uri.parse("content://com.education.schoolapp/received_messages_all"), projection, selection, null, null);
        if (msgCursor != null && msgCursor.getCount() > 0) {
            pendingMsgs = new String[msgCursor.getCount()];
            int idx = 0;
            while (msgCursor.moveToNext()) {
                pendingMsgs[idx] = msgCursor.getString(msgCursor.getColumnIndex("album_ids"));
                idx++;
            }
            msgCursor.close();
        }

        return pendingMsgs;
    }

    public String getMessage(Context context, String msgBox, int msgType, String msgId) {
        String[] messageProjection = {"message_id", "member_names", "subject", "body", "start_date", "sender_name", "sender_profile_image", "sender_id"};
        String contentString = "content://com.education.schoolapp/";
        Cursor msgCursor = null;
        String selection = " message_id like '" + msgId + "' and message_type = " + msgType;
        JSONUtility jsonUtility = new JSONUtility();
        JSONObject msgJsonObj = null;

        switch (msgBox.toLowerCase()) {
            case "inbox":
            case "saved":
                contentString = contentString.concat("received_messages_all");
                break;

            case "outbox":
                contentString = contentString.concat("sent_messages_all");
                break;
        }
        jsonUtility.setColumsList(messageProjection);

        msgCursor = context.getContentResolver().query(Uri.parse(contentString), null, selection, null, null);
        if (msgCursor != null && msgCursor.getCount() > 0) {
            msgCursor.moveToFirst();

            try {
                msgJsonObj = jsonUtility.toJSON(msgCursor);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            msgCursor.close();
        }

        if (msgJsonObj != null)
            return msgJsonObj.toString();
        else
            return null;
    }

    /*public String getMessage(Context context, String msgBox, String msgType, int msgId) {
        String[] messageProjection = {"to_name", "noti_title", "noti_description", "noti_date",
                "from_name", "from_image", "msg_title", "msg_description",
                "msg_date", "msg_attachment_path", "read_status"};
        String contentString = "content://com.education.schoolapp/";
        Cursor msgCursor = null;
        String selection = " msg_id = " + msgId;
        JSONUtility jsonUtility = new JSONUtility();
        JSONObject msgJsonObj = null;

        switch (msgBox.toLowerCase()) {
            case "inbox":
            case "saved":
                contentString = contentString.concat("received_");
                break;

            case "outbox":
                contentString = contentString.concat("sent_");
                break;
        }

        jsonUtility.setColumsList(messageProjection);
        contentString = contentString.concat(msgType.toLowerCase());

        msgCursor = context.getContentResolver().query(Uri.parse(contentString), null, selection, null, null);
        if (msgCursor != null && msgCursor.getCount() > 0) {
            msgCursor.moveToFirst();

            try {
                msgJsonObj = jsonUtility.toJSON(msgCursor);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            msgCursor.close();
        }

        if (msgJsonObj != null)
            return msgJsonObj.toString();
        else
            return null;
    }*/

    public ProfileItem getStudentProfileDetails(Context context) {
        Cursor proCursor = null;

        String selection = " oid like '" + mLoginName + "' ";
        proCursor = context.getContentResolver().query(Uri.parse("content://com.education.schoolapp/user_profile"), null, selection, null, null);

        if (proCursor != null && proCursor.getCount() > 0) {
            ProfileItem proItem = new ProfileItem();

            proCursor.moveToFirst();

            proItem.studentName = proCursor.getString(proCursor.getColumnIndex("member_name"));
            proItem.studentClass = proCursor.getString(proCursor.getColumnIndex("standards"));
            proItem.studentSection = proCursor.getString(proCursor.getColumnIndex("section"));
            proItem.studentBloodGroup = proCursor.getString(proCursor.getColumnIndex("blood_group"));
            proItem.studentFatherName = proCursor.getString(proCursor.getColumnIndex("father_name"));
            proItem.studentFatherContact = proCursor.getString(proCursor.getColumnIndex("father_contact_num"));
            proItem.studentFatherEmail = proCursor.getString(proCursor.getColumnIndex("father_email"));
            proItem.studentMotherName = proCursor.getString(proCursor.getColumnIndex("mother_name"));
            proItem.studentMotherContact = proCursor.getString(proCursor.getColumnIndex("mother_contact_num"));
            proItem.studentMotherEmail = proCursor.getString(proCursor.getColumnIndex("mother_email"));
            proItem.studentGuardianName = proCursor.getString(proCursor.getColumnIndex("guardian_name"));
            proItem.studentGuardianContact = proCursor.getString(proCursor.getColumnIndex("guardian_contact_num"));
            proItem.studentGuardianEmail = proCursor.getString(proCursor.getColumnIndex("guardian_email"));
            proItem.studentTeacherName = proCursor.getString(proCursor.getColumnIndex("mentor_name"));
            proItem.studentTeacherContact = proCursor.getString(proCursor.getColumnIndex("mentor_contact_num"));
            proItem.studentTeacherEmail = proCursor.getString(proCursor.getColumnIndex("mentor_email"));
            byte[] studentBlob = proCursor.getBlob(proCursor.getColumnIndex("profile_pic"));
            if (studentBlob != null) {
                proItem.studentImage = BitmapFactory.decodeByteArray(studentBlob, 0, studentBlob.length);
            }
            proCursor.close();
            return proItem;
        }
        return null;
    }

    public String[] getStudentName(Context context) {
        Cursor proCursor = null;
        String selection = " oid like '" + mLoginName + "' ";
        String[] studentName = new String[]{"", ""};

        proCursor = context.getContentResolver().query(Uri.parse("content://com.education.schoolapp/user_profile"), null, selection, null, null);

        if (proCursor != null && proCursor.getCount() > 0) {
            proCursor.moveToFirst();

            studentName[0] = proCursor.getString(proCursor.getColumnIndex("member_name"));
            studentName[1] = proCursor.getString(proCursor.getColumnIndex("standards"));
            proCursor.close();

            return studentName;
        }
        return null;
    }

    public byte[] getMemberProfilePic(Context context) {
        Cursor memCursor = null;
        String[] projection = {"profile_pic"};
        String selection = " oid like '" + mLoginName + "'";
        byte[] profile_pic = null;

        memCursor = context.getContentResolver().query(Uri.parse("content://com.education.schoolapp/user_profile"), projection, selection, null, null);
        if (memCursor != null && memCursor.getCount() > 0) {
            memCursor.moveToFirst();

            profile_pic = memCursor.getBlob(memCursor.getColumnIndex("profile_pic"));
            memCursor.close();
        }
        return profile_pic;
    }

    public HashMap<String, String> getClassStudents(Context context) {
        HashMap<String, String> students = null;
        Cursor stuCursor = null;
        String[] projection = {"student_id", "student_name"};

        stuCursor = context.getContentResolver().query(Uri.parse("content://com.education.schoolapp/class_students"), projection, null, null, null);

        if (stuCursor != null && stuCursor.getCount() > 0) {
            students = new HashMap<>(stuCursor.getCount());
            while (stuCursor.moveToNext()) {
                students.put(stuCursor.getString(stuCursor.getColumnIndex("student_name")),
                        stuCursor.getString(stuCursor.getColumnIndex("student_id")));
            }

            stuCursor.close();
        }
        return students;
    }

    public String getTeacherNameforStudent(Context context, String student_Oid) {
        String teacherName = null;
        Cursor teachCursor = null;
        String[] projection = {"mentor_name"};
        String selection = " oid like '" + student_Oid + "'";

        teachCursor = context.getContentResolver().query(Uri.parse("content://com.education.schoolapp/user_profile"), projection, selection, null, null);
        if (teachCursor != null && teachCursor.getCount() > 0) {
            teachCursor.moveToFirst();
            teacherName = teachCursor.getString(teachCursor.getColumnIndex("mentor_name"));
            teachCursor.close();
        }

        return teacherName;
    }

    public ArrayList<CustomGallery> getUpdatedImages(Context context, String displayType) {
        ArrayList<CustomGallery> galleryList = new ArrayList<CustomGallery>();
        Cursor imgCursor = null;
        String[] projection = {"image_local_path", "status"};
        String selection = " type like '" + displayType + "'";

        imgCursor = context.getContentResolver().query(Uri.parse(SchoolDataConstants.CONTENT_URI + SchoolDataConstants.ALBUM_IMAGES),
                projection, selection, null, null);

        if (imgCursor != null && imgCursor.getCount() > 0) {
            while (imgCursor.moveToNext()) {
                CustomGallery item = new CustomGallery();
                item.sdcardPath = imgCursor.getString(imgCursor.getColumnIndex("image_local_path"));
                item.syncState = imgCursor.getInt(imgCursor.getColumnIndex("status"));
                galleryList.add(item);
            }
            imgCursor.close();
        }
        return galleryList;
    }

    public JSONArray getPendingImagesForAlbum(Context context) {
        JSONArray pendImagesArray = new JSONArray();
        Cursor pendImageCursor = null;
        String[] projection = {"image_local_path", "image_name"};
        String selection = " type like 'Sent' and status == 0";

        pendImageCursor = context.getContentResolver().query(Uri.parse(SchoolDataConstants.CONTENT_URI + SchoolDataConstants.ALBUM_IMAGES),
                projection, selection, null, null);

        if (pendImageCursor != null && pendImageCursor.getCount() > 0) {
            while (pendImageCursor.moveToNext()) {
                JSONObject pendImagesObj = new JSONObject();

                try {
                    pendImagesObj.put("name", pendImageCursor.getString(pendImageCursor.getColumnIndex("image_name")));
                    pendImagesObj.put("data", getSelectedImageString(pendImageCursor.getString(
                            pendImageCursor.getColumnIndex("image_local_path"))));

                    pendImagesArray.put(pendImagesObj);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return pendImagesArray;
    }

    public JSONArray getPendingImages(Context context) {
        JSONArray pendImagesArray = new JSONArray();
        Cursor pendImageCursor = null;
        String[] projection = {"image_local_path", "album_id", "image_date"};
        String selection = " type like 'Sent' and status == 0";

        pendImageCursor = context.getContentResolver().query(Uri.parse(SchoolDataConstants.CONTENT_URI + SchoolDataConstants.ALBUM_IMAGES),
                projection, selection, null, null);

        if (pendImageCursor != null && pendImageCursor.getCount() > 0) {
            while (pendImageCursor.moveToNext()) {
                JSONObject pendImagesObj = new JSONObject();
                try {
                    pendImagesObj.put("date", pendImageCursor.getLong(pendImageCursor.getColumnIndex("image_date")));
                    pendImagesObj.put("type", "Image");
                    pendImagesObj.put("album_id", pendImageCursor.getString(pendImageCursor.getColumnIndex("album_id")));
                    pendImagesObj.put("content", getSelectedImageString(pendImageCursor.getString(
                            pendImageCursor.getColumnIndex("image_local_path"))));

                    pendImagesArray.put(new JSONObject().put("multimedium", pendImagesObj));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            pendImageCursor.close();
        }

        return pendImagesArray;
    }

    public JSONArray getPendingImagesToDownload(Context context) {
        JSONArray pendImagesArray = new JSONArray();
        Cursor pendImageCursor = null;
        String[] projection = {"image_id"};
        String selection = " type like 'Received' and status == 0";

        pendImageCursor = context.getContentResolver().query(Uri.parse(SchoolDataConstants.CONTENT_URI + SchoolDataConstants.ALBUM_IMAGES),
                projection, selection, null, null);

        if (pendImageCursor != null && pendImageCursor.getCount() > 0) {
            while (pendImageCursor.moveToNext()) {
                pendImagesArray.put(pendImageCursor.getString(pendImageCursor.getColumnIndex("image_id")));
            }
            pendImageCursor.close();
        }
        return pendImagesArray;
    }

    private String getSelectedImageString(String selectedImagePath) {
        Bitmap bm;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(selectedImagePath, options);
        final int REQUIRED_SIZE = 200;
        int scale = 1;
        while (options.outWidth / scale / 2 >= REQUIRED_SIZE
                && options.outHeight / scale / 2 >= REQUIRED_SIZE)
            scale *= 2;
        options.inSampleSize = scale;
        options.inJustDecodeBounds = false;
        bm = BitmapFactory.decodeFile(selectedImagePath, options);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 70, outputStream);

        return Base64.encodeToString(outputStream.toByteArray(), 0);
    }
}
