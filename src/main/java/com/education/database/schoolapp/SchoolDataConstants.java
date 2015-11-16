package com.education.database.schoolapp;

/**
 * Created by Sainath on 23-10-2015.
 */
public class SchoolDataConstants {
    public final String CONTENT_URI = "content://com.education.schoolapp/";

    /* TABLES in school app */
    public final static String IDENTITY_TABLE = "identity";
    public final static String USER_PROFILE = "user_profile";
    public final static String STUDENT_ATTENDANCE = "student_attendance";
    public final static String RECEIVED_MESSAGES = "received_messages";
    public final static String SENT_MESSAGES = "sent_messages";
    public final static String RECEIVED_NOTIFICATIONS = "received_notifications";
    public final static String SENT_NOTIFICATIONS = "sent_notifications";
    public final static String ALBUM_DETAILS = "album_details";
    public final static String ALBUM_DATA = "album_data";
    public final static String SERVER_MESSAGE_IDS = "server_message_ids";
    public final static String CLASS_STUDENTS = "class_students";
    public final static String SENT_MESSAGES_ALL = "sent_messages_all";
    public final static String RECEIVED_MESSAGES_ALL = "received_messages_all";

    private final static String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS ";

    /* String format for table creations for school app */
    public final static String CREATE_IDENTITY_TABLE = CREATE_TABLE + IDENTITY_TABLE + " (_ID INTEGER UNIQUE DEFAULT 1, loginid TEXT UNIQUE NOT NULL, password TEXT NOT NULL, " +
            "user_type TEXT NOT NULL, gcmid TEXT); ";

    public final static String CREATE_USER_PROFILE = CREATE_TABLE + USER_PROFILE + " (oid TEXT UNIQUE NOT NULL, member_name TEXT NOT NULL, " +
            "age INTEGER, dob TEXT, blood_group TEXT, standards TEXT NOT NULL, section TEXT, profile_pic BLOB, " +
            "father_name TEXT NOT NULL, father_contact_num TEXT NOT NULL, father_email TEXT, " +
            "mother_name TEXT NOT NULL, mother_contact_num TEXT NOT NULL, mother_email TEXT, " +
            "guardian_name TEXT, guardian_contact_num TEXT, guardian_email TEXT, mentor_name TEXT, mentor_contact_num TEXT, mentor_email TEXT," +
            "loginid TEXT NOT NULL, gcmid TEXT NOT NULL, role INTEGER, subjects TEXT ); ";

    public final static String CREATE_ATTENDANCE_TABLE = CREATE_TABLE + STUDENT_ATTENDANCE + " (user_id TEXT NOT NULL, name TEXT NOT NULL, " +
            "present_days INTEGER NOT NULL, absent_days INTEGER NOT NULL); ";

    public final static String CREATE_RECEIVED_MESSAGES = CREATE_TABLE + RECEIVED_MESSAGES + " (msg_id INTEGER NOT NULL, from_name TEXT NOT NULL, from_image BLOB, " +
            "msg_title TEXT NOT NULL, msg_description TEXT NOT NULL, msg_date INTEGER NOT NULL, msg_attachment_path TEXT, to_name TEXT, " +
            "to_be_replied INTEGER DEFAULT 0, saved INTEGER DEFAULT 0, read_status INTEGER DEFAULT 1); ";

    public final static String CREATE_SENT_MESSAGES = CREATE_TABLE + SENT_MESSAGES + " (msg_id INTEGER NOT NULL, to_name TEXT NOT NULL, " +
            "msg_title TEXT NOT NULL, msg_description TEXT NOT NULL, msg_date TEXT NOT NULL, msg_attachment_path TEXT, " +
            "from_name TEXT, from_image BLOB, need_reply INTEGER DEFAULT 0); ";

    public final static String CREATE_RECEIVED_NOTIFICATIONS = CREATE_TABLE + RECEIVED_NOTIFICATIONS + " (msg_id INTEGER NOT NULL, from_name TEXT NOT NULL, from_image BLOB, " +
            "noti_title TEXT NOT NULL, noti_description TEXT NOT NULL, noti_date INTEGER NOT NULL, to_name TEXT, " +
            "read_status INTEGER DEFAULT 1); ";

    public final static String CREATE_SENT_NOTIFICATIONS = CREATE_TABLE + SENT_NOTIFICATIONS + " (msg_id INTEGER NOT NULL, to_name TEXT NOT NULL, " +
            "noti_title TEXT NOT NULL, noti_description TEXT NOT NULL, noti_date TEXT NOT NULL, from_name TEXT, from_image BLOB); ";

    public final static String CREATE_SENT_MESSAGES_ALL = CREATE_TABLE + SENT_MESSAGES_ALL + " (subject TEXT NOT NULL, body TEXT NOT NULL, " +
            "sender_id TEXT NOT NULL, sender_name TEXT NOT NULL, sender_profile_image BLOB, member_ids TEXT NOT NULL, member_names TEXT NOT NULL, message_type INTEGER, start_date TEXT, end_date TEXT, " +
            "message_id TEXT, local_msg_id INTEGER, status INTEGER DEFAULT 0); ";

    public final static String CREATE_RECEIVED_MESSAGES_ALL = CREATE_TABLE + RECEIVED_MESSAGES_ALL + " (subject TEXT NOT NULL, body TEXT NOT NULL, " +
            "sender_id TEXT NOT NULL, sender_name TEXT, sender_profile_image BLOB, member_ids TEXT NOT NULL, member_names TEXT, message_type INTEGER, start_date TEXT, end_date TEXT, " +
            "message_id TEXT UNIQUE NOT NULL, saved INTEGER DEFAULT 0, read_status INTEGER DEFAULT 1); " ;

    public final static String CREATE_ALBUM_DETAILS = CREATE_TABLE + ALBUM_DETAILS + " (album_name TEXT NOT NULL, album_date TEXT NOT NULL, " +
            "image_count INTEGER NOT NULL); ";

    public final static String CREATE_ALBUM_DATA = CREATE_TABLE + ALBUM_DATA + " (image_id TEXT NOT NULL, " +
            "thumbnail BLOB, image_url TEXT NOT NULL, image_download_path TEXT); ";

    /** Message Status
     *  PENDING - 0
     *  RECEIVED - 1
     *  FAILED - 2
     */
    /** Message Type
     * 1 - Plain Message
     * 2 - Notification
     * 3 - Multimedia
     */
    public final static String CREATE_SERVER_MESSAGE_IDS = CREATE_TABLE + SERVER_MESSAGE_IDS + " (message_id TEXT NOT NULL, " +
            "message_type INTEGER NOT NULL, status INTEGER DEFAULT 0); ";

    public final static String CREATE_CLASS_STUDENTS = CREATE_TABLE + CLASS_STUDENTS + " (student_id TEXT UNIQUE NOT NULL, " +
            "student_name TEXT, class TEXT); ";
}
