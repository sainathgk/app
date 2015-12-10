package com.education.schoolapp;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.education.connection.schoolapp.JSONUtility;
import com.education.connection.schoolapp.NetworkConnectionUtility;
import com.education.connection.schoolapp.NetworkConstants;
import com.education.database.schoolapp.SchoolDataConstants;
import com.education.service.schoolapp.QuickstartPreferences;
import com.education.service.schoolapp.RegistrationIntentService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.common.base.Joiner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText mPasswordView;
    private EditText mUserNameView;
    private RadioGroup mRadioLayout;

    private static final String APP_SHARED_PREFS = "school_preferences";
    private static final String SHARED_LOGIN_KEY = "schoolUserLoginState";
    private static final String SHARED_LOGIN_TYPE = "schoolUserLoginType";
    private static final String SHARED_LOGIN_NAME = "schoolUserLoginName";
    private String mLoginType;

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = LoginActivity.class.toString();

    private BroadcastReceiver mRegistrationReceiver;
    private ProgressDialog progress;
    private NetworkConnectionUtility networkConn;
    private String mOid;
    private String mClassName = "";
    private String mSectionName = "";
    private int mMessageArrayLength;
    private int mCurrentMsg = 1;
    private int mMessageFinalCount;
    private String[] toStringArray;
    private String[] receivedMsgs;
    private int mClassesLength;
    private HashMap<String, Integer> mReceivedMsgsMap;
    private String[] receivedNots;
    private String[] receivedAlbum;
    private ArrayList<String> mAlbumIds = new ArrayList<String>();
    private int mAlbumIdx = 0;
    private String[] toSectionArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        mUserNameView = (EditText) findViewById(R.id.user_id);
        mPasswordView = (EditText) findViewById(R.id.password);

        Button mLoginButton = (Button) findViewById(R.id.login_button);
        mLoginButton.setOnClickListener(this);

        mRadioLayout = (RadioGroup) findViewById(R.id.radio_layout);

        mRegistrationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences
                        .getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);

                if (sentToken) {
                    String[] projection = {"loginid", "password", "gcmid"};
                    Cursor userCursor = getContentResolver().query(Uri.parse("content://com.education.schoolapp/identity"), projection, null, null, null);
                    JSONUtility jsonUtility = new JSONUtility();
                    String loginDetails = "";
                    jsonUtility.setColumsList(projection);
                    if (userCursor != null && userCursor.getCount() > 0) {
                        userCursor.moveToFirst();

                        try {
                            JSONObject jsonObject = jsonUtility.toJSON(userCursor);
                            loginDetails = new JSONObject().put("member", jsonObject).toString();
                            Log.i(TAG, loginDetails);
                            userCursor.close();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    networkConn.loginUser(loginDetails);
                }
                //launhHomeActivity();
            }
        };

        progress = new ProgressDialog(this);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.setTitle(R.string.login_authenticating);

        networkConn = new NetworkConnectionUtility();

        NetworkResp networkResp = new NetworkResp();
        networkConn.setNetworkListener(networkResp);
    }

    private class NetworkResp implements NetworkConnectionUtility.NetworkResponseListener {
        @Override
        public void onResponse(String urlString, String networkResult) {
            if (urlString.equalsIgnoreCase(NetworkConstants.AUTHENTICATE)) {
                if (networkResult == null) {
                    progress.setTitle(R.string.login_failed);
                    progress.dismiss();
                    return;
                }
                int userType = -1;
                JSONUtility jsonUtility = new JSONUtility();
                String[] loginColumns = {"member_name", "dob", "age", "blood_group",
                        "father_name", "father_contact_num", "father_email", "mother_name", "mother_contact_num", "mother_email",
                        "guardian_name", "guardian_contact_num", "guardian_email", "mentor_name", "mentor_contact_num", "mentor_email",
                        "loginid", "gcmid", "role", "subjects"};

                jsonUtility.setColumsList(loginColumns);
                ContentValues userValues = null;
                try {
                    JSONObject userJsonObj = new JSONObject(networkResult);
                    userValues = jsonUtility.fromJSON(userJsonObj);
                    mOid = userJsonObj.getJSONObject("_id").getString("$oid");
                    userValues.put("oid", mOid);
                    userValues.put("profile_pic", Base64.decode(userJsonObj.getString("profile_pic"), 0));
                    userType = userJsonObj.getInt("role");

                    JSONArray standardsArray = userJsonObj.getJSONArray("standards");
                    int standardsLength = standardsArray.length();
                    for (int i = 0; i < standardsLength; i++) {
                        JSONArray sectionArray = standardsArray.getJSONObject(i).getJSONArray("section");
                        for (int j = 0; j < sectionArray.length(); j++) {
                            mClassName = mClassName.concat(standardsArray.getJSONObject(i).getString("standard_class"));
                            mSectionName = mSectionName.concat(sectionArray.getString(j));
                            if (standardsLength > 1 && i + 1 < standardsLength) {
                                mClassName = mClassName.concat(",");
                                mSectionName = mSectionName.concat(",");
                            }
                        }
                    }

                    if (mClassName != null) {
                        userValues.put("standards", mClassName);
                        userValues.put("section", mSectionName);
                        toStringArray = (String[]) Arrays.asList(mClassName.split(",")).toArray();
                        toSectionArray = (String[]) Arrays.asList(mSectionName.split(",")).toArray();
                        mClassesLength = toStringArray.length;

                        getContentResolver().insert(Uri.parse("content://com.education.schoolapp/user_profile"), userValues);

                        for (int clas = 0; clas < mClassesLength; clas++)
                            networkConn.getStudents(toStringArray[clas], toSectionArray[clas]);
                    }
                    JSONArray messageArray = userJsonObj.getJSONArray("messages");
                    int msgArrayLength = messageArray.length();
                    mMessageFinalCount = mMessageArrayLength = msgArrayLength;
                    if (msgArrayLength > 0) {
                        ContentValues[] msgIdValues = new ContentValues[msgArrayLength];
                        receivedMsgs = new String[msgArrayLength];
                        for (int msgId = 0; msgId < msgArrayLength; msgId++) {
                            JSONObject msgIdObj = messageArray.getJSONObject(msgId);
                            msgIdValues[msgId] = new ContentValues();
                            String messageId = msgIdObj.getJSONObject("_id").getString("$oid");
                            msgIdValues[msgId].put("message_id", messageId);
                            receivedMsgs[msgId] = messageId;
                            msgIdValues[msgId].put("message_type", msgIdObj.getString("message_type"));
                        }
                        getContentResolver().bulkInsert(Uri.parse("content://com.education.schoolapp/server_message_ids"), msgIdValues);
                    } else {
                        launhHomeActivity();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    progress.setTitle("Failed");
                }

                SharedPreferences sharePrefs = getApplicationContext().getSharedPreferences(APP_SHARED_PREFS, Context.MODE_PRIVATE);

                SharedPreferences.Editor editor = sharePrefs.edit();
                editor.putBoolean(SHARED_LOGIN_KEY, true);
                switch (userType) {
                    case 0:
                        mLoginType = "Admin";
                        break;

                    case 1:
                        mLoginType = "Parent";
                        break;

                    case 2:
                        mLoginType = "Teacher";
                        break;
                }
                editor.putString(SHARED_LOGIN_TYPE, mLoginType);
                editor.putString(SHARED_LOGIN_NAME, mOid);
                editor.apply();

            } else if (urlString.startsWith(NetworkConstants.GET_CLASS_STUDENTS)) {
                mClassesLength--;
                if (networkResult == null) {
                    Log.i("Network", "Get Class Students API");
                } else {
                    try {
                        JSONArray studentsArray = new JSONArray(networkResult);
                        int stuArrayLength = studentsArray.length();
                        if (stuArrayLength > 0) {
                            ContentValues[] studentValues = new ContentValues[stuArrayLength];

                            for (int student = 0; student < stuArrayLength; student++) {
                                studentValues[student] = new ContentValues();
                                studentValues[student].put("student_id", studentsArray.getJSONObject(student).getString("id"));
                                studentValues[student].put("student_name", studentsArray.getJSONObject(student).getString("student_name"));
                                int classLength = NetworkConstants.GET_CLASS_STUDENTS.length();
                                int endUrlLength = urlString.lastIndexOf(".json");
                                studentValues[student].put("class", urlString.substring(classLength, endUrlLength));
                            }

                            getContentResolver().bulkInsert(Uri.parse("content://com.education.schoolapp/class_students"), studentValues);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if (mClassesLength == 0) {
                    //receivedMsgs = new SchoolDataUtility().getPendingMessages(getApplicationContext());
                    if (receivedMsgs != null) {
                        mMessageFinalCount = mMessageArrayLength = receivedMsgs.length;
                        if (mMessageFinalCount > 0) {
                            for (int msg = 0; msg < mMessageFinalCount; msg++) {
                                networkConn.getMessage(receivedMsgs[msg]);
                            }
                        }
                    }
                }
            } else if (urlString.startsWith(NetworkConstants.GET_MESSAGE)) {
                progress.setTitle(getString(R.string.message_fetching_data) + mCurrentMsg + "/" + mMessageFinalCount);
                mMessageArrayLength--;
                mCurrentMsg++;
                if (networkResult == null) {
                    Log.i("Network", "Get Message API");
                    return;
                }
                try {
                    JSONObject messageObj = new JSONObject(networkResult);
                    String[] messageProjection = {"subject", "body", "sender_id", "group_id", "start_date", "end_date", "message_type"};
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
                            memberIds[i] = membersArray.getJSONObject(i).getJSONObject("_id").getString("$oid");
                            memberNames[i] = membersArray.getJSONObject(i).getString("member_name");
                            *//*for (Map.Entry<String, String> entry : mStudentsMap.entrySet()) {
                                if (entry.getValue().equals(memberIds[i])) {
                                    System.out.println(entry.getKey());
                                    memberNames[i] = entry.getKey();
                                }
                            }*//*
                        }
                    }*/

                    String memberIdString = Joiner.on(",").skipNulls().join(memberIds);
                    msgValues.put("member_ids", memberIdString);
                    msgValues.put("member_names", Joiner.on(",").skipNulls().join(memberNames));

                    if (messageObj.getString("album_ids") != null && !messageObj.getString("album_ids").equalsIgnoreCase("null")) {
                        JSONArray albumArray = messageObj.getJSONArray("album_ids");
                        if (albumArray != null) {
                            String[] albumIds = new String[albumArray.length()];
                            for (int i = 0; i < albumIds.length; i++) {
                                albumIds[i] = albumArray.getString(i);
                                mAlbumIds.add(mAlbumIdx, albumArray.getString(i));
                                mAlbumIdx++;
                            }
                            msgValues.put("album_ids", Joiner.on(",").skipNulls().join(albumIds));
                        }
                    }
                    msgValues.put("sender_profile_image", Base64.decode(messageObj.getString("sender_profile_image"), 0));
                    //msgValues.put("sender_name", new SchoolDataUtility().getTeacherNameforStudent(getApplicationContext(), mOid));
                    msgValues.put("sender_name", messageObj.getString("sender_name"));

                    getContentResolver().insert(Uri.parse("content://com.education.schoolapp/received_messages_all"), msgValues);

                    ContentValues msgIdUpdate = new ContentValues();
                    String selection = " message_id like '" + messageObj.getJSONObject("_id").getString("$oid") + "'";
                    msgIdUpdate.put("status", 1);

                    getContentResolver().update(Uri.parse("content://com.education.schoolapp/server_message_ids"), msgIdUpdate, selection, null);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (mMessageArrayLength == 0) {
                    if (mAlbumIds != null) {
                        mMessageFinalCount = mMessageArrayLength = mAlbumIds.size();
                        if (mMessageArrayLength > 0) {
                            mCurrentMsg = 1;
                            for (int msg = 0; msg < mMessageArrayLength; msg++) {
                                networkConn.getAlbum(mAlbumIds.get(msg));
                            }
                        } else {
                            launhHomeActivity();
                        }
                    } else {
                        launhHomeActivity();
                    }
                    //receivedAlbum = new SchoolDataUtility().getPendingAlbum(getApplicationContext());
                    /*if (receivedAlbum != null) {
                        mMessageFinalCount = mMessageArrayLength = receivedAlbum.length;
                        if (mMessageArrayLength > 0) {
                            for (int msg = 0; msg < mMessageArrayLength; msg++) {
                                networkConn.getAlbum(receivedAlbum[msg]);
                            }
                        } else {
                            launhHomeActivity();
                        }
                    } else {
                        launhHomeActivity();
                    }*/
                }
            } else if (urlString.startsWith(NetworkConstants.GET_ALBUM)) {

                progress.setTitle(getString(R.string.album_fetching_data) + mCurrentMsg + "/" + mMessageFinalCount);
                mMessageArrayLength--;
                mCurrentMsg++;

                if (networkResult == null || networkResult.equalsIgnoreCase("Bad Request")) {
                    Log.i("Network", "Get Album API");
                    return;
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
                                albumValues[albIdx].put("album_id", albumObj.getJSONObject("_id").getString("$oid"));
                                albumValues[albIdx].put("image_id", albumArray.getJSONObject(albIdx).getJSONObject("_id").getString("$oid"));
                            }
                            getContentResolver().bulkInsert(Uri.parse(SchoolDataConstants.CONTENT_URI + SchoolDataConstants.ALBUM_IMAGES), albumValues);

                            /*albumMsgUpdate.put("read_status", 0);
                            String selection = " album_name like '" + albumName + "'";

                            getContentResolver().update(Uri.parse(SchoolDataConstants.CONTENT_URI + SchoolDataConstants.RECEIVED_MESSAGES_ALL),
                                    albumMsgUpdate, selection, null);*/
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (mMessageArrayLength == 0) {
                    launhHomeActivity();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationReceiver, new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));
    }


    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationReceiver);
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        String userNameText = mUserNameView.getText().toString();
        String passWordText = mPasswordView.getText().toString();
        if (userNameText.isEmpty()) {
            mUserNameView.setFocusable(true);
            Toast.makeText(this, "Please enter the User ID", Toast.LENGTH_SHORT).show();
            return;
        }
        if (passWordText.isEmpty()) {
            mPasswordView.setFocusable(true);
            Toast.makeText(this, "Please enter the Password", Toast.LENGTH_SHORT).show();
            return;
        }

        progress.setTitle(R.string.login_authenticating);
        progress.show();

        int mSelectedRadioId = mRadioLayout.getCheckedRadioButtonId();
        switch (mSelectedRadioId) {
            case R.id.parent_radioButton:
                mLoginType = "Parent";
                //addStudentDetails(userNameText);
                break;

            case R.id.teacher_radioButton2:
                mLoginType = "Teacher";
                //userNameText = getResources().getString(R.string.teacher_name);
                break;
        }

        /*SharedPreferences sharePrefs = getApplicationContext().getSharedPreferences(APP_SHARED_PREFS, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharePrefs.edit();
        editor.putBoolean(SHARED_LOGIN_KEY, true);
        editor.putString(SHARED_LOGIN_TYPE, mLoginType);
        editor.putString(SHARED_LOGIN_NAME, userNameText.toLowerCase());
        editor.apply();*/

        //DB insert of all these Values
        ContentValues loginValues = new ContentValues();
        loginValues.put("loginid", userNameText);
        loginValues.put("password", passWordText);
        loginValues.put("user_type", mLoginType);

        getContentResolver().insert(Uri.parse("content://com.education.schoolapp/identity"), loginValues);

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
    }

    private void launhHomeActivity() {
        progress.dismiss();

        Intent homeIntent = new Intent(this, HomeMainActivity.class);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        homeIntent.putExtra("class", mClassName);
        startActivity(homeIntent);
        finish();
    }

    private void addStudentDetails(String loginName) {
        ContentValues studentValues = new ContentValues();

        studentValues.put("user_id", loginName);
        studentValues.put("age", 5);
        studentValues.put("class", "UKG");

        if (loginName.equalsIgnoreCase("mazher")) {
            studentValues.put("name", "Mohammed Ashraf");
            studentValues.put("blood_group", "A+ve");
            studentValues.put("section", "B");
            studentValues.put("father_name", "Mazher");
            studentValues.put("father_contact", "9902546856");
            studentValues.put("mother_name", "Nazia");
            studentValues.put("mother_contact", "8576425891");
            Bitmap proBitmap = null;
            if (((BitmapDrawable) getResources().getDrawable(R.drawable.student)) != null) {
                proBitmap = ((BitmapDrawable) getResources().getDrawable(R.drawable.student)).getBitmap();
            }
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            proBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            studentValues.put("user_image", outputStream.toByteArray());
        } else {
            studentValues.put("name", "Rehan");
            studentValues.put("blood_group", "B-ve");
            studentValues.put("section", "D");
            studentValues.put("father_name", "Amir");
            studentValues.put("father_contact", "9902546875");
            studentValues.put("mother_name", "Nazneen");
            studentValues.put("mother_contact", "85764525891");
            Bitmap proBitmap = null;
            if (((BitmapDrawable) getResources().getDrawable(R.drawable.student)) != null) {
                proBitmap = ((BitmapDrawable) getResources().getDrawable(R.drawable.student)).getBitmap();
            }
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            proBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            studentValues.put("user_image", outputStream.toByteArray());
        }
        studentValues.put("guardian_name", "Rehman");
        studentValues.put("guardian_contact", "125486954");

        getContentResolver().insert(Uri.parse("content://com.education.schoolapp/user_profile"), studentValues);

    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }
}
