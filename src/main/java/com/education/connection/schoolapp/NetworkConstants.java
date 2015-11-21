package com.education.connection.schoolapp;

/**
 * Created by Sainath on 23-10-2015.
 */
public class NetworkConstants {
    public static final boolean isServerON = true;
    private static final String SERVER_ADDRESS = "52.88.98.39";
    private static final String HOST_URL = "http://" + SERVER_ADDRESS;

    public final static String AUTHENTICATE = HOST_URL + "/members/login.json";

    //GET - http://<server_ip>/getStudentProfile?parentId=758x584d6
    public final static String GET_USER_DETAILS = HOST_URL + "/getProfile.json?id=";

    //GET - http://<server_ip>getAttendance?studentId=254862014
    public final static String GET_STUDENT_ATTENDANCE = HOST_URL + "getAttendance?studentId=";

    //GET - http://<server_ip>/getTeacherProfile?id=758x58sef
    public final static String GET_TEACHER_ATTENDANCE = HOST_URL + "getTeacherProfile?id=";

    //GET -  All the class students used to post Message
    public final static String GET_CLASS_STUDENTS = HOST_URL + "/getStudents/";

    //GET - http://<server_ip>/getAllMessages?Class=LKG&id=758x584d6
    public final static String GET_ALL_MESSAGES = HOST_URL + "getAllMessages?Class=";

    //GET - http://<server_ip>/getMessage/<message_id>.json
    public final static String GET_MESSAGE = HOST_URL + "/getMessage/";

    //POST - http://<server_ip>/postMessage
    public final static String POST_MESSAGE = HOST_URL + "/createMessage.json";

    //POST - http://<server_ip>/createAlbum.json
    public final static String CREATE_ALBUM = HOST_URL + "/createAlbum.json";

    //POST - http://<server_ip>/createMultimedia.json
    public final static String CREATE_MULTIMEDIA = HOST_URL + "/createMultimedia.json";

    //http://<server_id>/getAlbum/<album_id>.json
    public final static String GET_ALBUM = HOST_URL + "/getAlbum/";

    //http://<server id>/getMultimedia/<Multimedia_id>.json
    public final static  String GET_MULTIMEDIA = HOST_URL + "/getMultimedia/";

    //GET - http://<server_ip>/getAllNotifications?Class=LKG&id=758x584d6
    public final static String GET_ALL_NOTIFICATIONS = HOST_URL + "getAllNotifications?Class=";

    //POST - http://<server_ip>/postMessage
    public final static String POST_NOTIFICATION = HOST_URL + "postNotification";

    //GET - http://<server_ip>/getImageList?Class=LKG
    public final static String GET_IMAGES_LIST = HOST_URL + "getImageList?Class=";

    //POST - http://<server_ip>postImages
    public final static String POST_IMAGES = HOST_URL + "postImages";

}
