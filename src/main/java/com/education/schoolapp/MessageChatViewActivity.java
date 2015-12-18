package com.education.schoolapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.education.connection.schoolapp.JSONUtility;
import com.education.connection.schoolapp.NetworkConnectionUtility;
import com.education.connection.schoolapp.NetworkConstants;
import com.education.database.schoolapp.SchoolDataConstants;
import com.education.database.schoolapp.SchoolDataUtility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

public class MessageChatViewActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MessageChatViewActivity.class.toString();
    private RecyclerView mChatList;
    private MessageChatViewAdapter mChatListAdapter;
    private ImageButton mComposeSendBtn;
    private EditText mComposeEdit;
    private NetworkConnectionUtility networkConn;
    private ProgressDialog progress;

    private static final int MIN_NUM = 1;
    private static final int MAX_NUM = 100000;
    int REQUEST_CAMERA = 0, SELECT_FILE = 100;

    private static final String APP_SHARED_PREFS = "school_preferences";
    private static final String SHARED_LOGIN_NAME = "schoolUserLoginName";
    private static final String SHARED_LOGIN_TYPE = "schoolUserLoginType";
    private static final String SHARED_MSG_VIEW = "schoolChatMsgView";
    private SharedPreferences sharePrefs;
    private String mLoginName;
    private int mComposeMessageId;
    private HashMap<String, String> mStudentsMap;
    private String mTextTo;
    private String mFromName;
    private boolean mIsNewGroup;
    private SharedPreferences.Editor editor;
    private String mLoginType = "";
    private SchoolDataUtility mSchoolDataUtility;
    private String currUserName;
    private File mAppDir;
    private String mAlbumId;
    private ImageButton mAttachBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_chat_view);
        mFromName = getIntent().getStringExtra("msg_title");
        mTextTo = getIntent().getStringExtra("msg_members");
        mIsNewGroup = getIntent().getBooleanExtra("new_group", false);

        mChatList = (RecyclerView) findViewById(R.id.chat_messages_list);
        mChatList.setHasFixedSize(false);

        mChatList.setLayoutManager(new LinearLayoutManager(this));
        mChatList.setItemAnimator(new DefaultItemAnimator());

        mChatListAdapter = new MessageChatViewAdapter();

        sharePrefs = getApplicationContext().getSharedPreferences(APP_SHARED_PREFS, Context.MODE_PRIVATE);
        mLoginName = sharePrefs.getString(SHARED_LOGIN_NAME, "");
        mLoginType = sharePrefs.getString(SHARED_LOGIN_TYPE, "");

        mSchoolDataUtility = new SchoolDataUtility(mLoginName, mLoginType.equalsIgnoreCase("Teacher"));

        if (mTextTo != null && !mTextTo.isEmpty()) {
            mChatListAdapter.updateData(this, mTextTo);
            mFromName = mSchoolDataUtility.getGroupMembersNames(this, mTextTo);
        } else {
            mFromName = mFromName.concat(",You");
        }

        mChatList.setAdapter(mChatListAdapter);

        mChatList.scrollToPosition(mChatListAdapter.getItemCount() - 1);

        mComposeSendBtn = (ImageButton) findViewById(R.id.sendMessageButton);
        mComposeSendBtn.setOnClickListener(this);

        mAttachBtn = (ImageButton) findViewById(R.id.attachmentButton);
        mAttachBtn.setOnClickListener(this);

        mComposeEdit = (EditText) findViewById(R.id.message);

        progress = new ProgressDialog(this);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.setTitle(R.string.compose_send_progress);

        mStudentsMap = new SchoolDataUtility().getClassStudents(this);
        currUserName = mSchoolDataUtility.getStudentName(getApplicationContext())[0];

        setTitle(mFromName);

        getContentResolver().registerContentObserver(Uri.parse(SchoolDataConstants.CONTENT_URI + SchoolDataConstants.RECEIVED_MESSAGES_ALL), true, new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                if (mChatListAdapter != null) {
                    mChatListAdapter.updateData(getApplicationContext(), mTextTo);
                    mChatListAdapter.notifyDataSetChanged();
                }
                if (mChatList != null) {
                    mChatList.invalidate();
                }
            }
        });

        ContentValues msgUpdate = new ContentValues();
        String selection = "group_id like '" + mTextTo + "'";

        msgUpdate.put("read_status", 0);

        getContentResolver().update(Uri.parse("content://com.education.schoolapp/received_messages_all"), msgUpdate, selection, null);
        SharedPreferences sharePrefs = getApplicationContext().getSharedPreferences(APP_SHARED_PREFS, Context.MODE_PRIVATE);

        editor = sharePrefs.edit();

        // Find the SD Card path
        File filepath = Environment.getExternalStorageDirectory();

        // Create a new folder in SD Card
        mAppDir = new File(filepath.getAbsolutePath() + "/" + getResources().getString(R.string.app_name));
        if (!mAppDir.exists()) {
            mAppDir.mkdirs();
        }

        networkConn = new NetworkConnectionUtility();

        NetworkResp networkResp = new NetworkResp();
        networkConn.setNetworkListener(networkResp);
    }

    @Override
    protected void onResume() {
        super.onResume();

        editor.putBoolean(SHARED_MSG_VIEW, true);
        editor.apply();

        mChatList.scrollToPosition(mChatListAdapter.getItemCount() - 1);
    }

    @Override
    protected void onPause() {
        super.onPause();
        editor.putBoolean(SHARED_MSG_VIEW, false);
        editor.apply();
    }

    @Override
    protected void onDestroy() {
        editor.putBoolean(SHARED_MSG_VIEW, false);
        editor.apply();
        super.onDestroy();
    }

    /*@Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mChatList != null) {
            mChatList.invalidate();
            mChatList.scrollToPosition(mChatListAdapter.getItemCount() - 1);
        }
    }*/

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.sendMessageButton) {
            String composeText = mComposeEdit.getText().toString();

            if (composeText.isEmpty()) {
                return;
            }

            handleSendMessage(composeText, null, 1);
        } else if (v.getId() == R.id.attachmentButton) {
            selectImage();
        }
    }

    private void handleSendMessage(String message, String album_id, int messageType) {
        progress.show();

        JSONArray toSenderIds;
        if (mTextTo != null && !mTextTo.isEmpty()) {
            toSenderIds = mSchoolDataUtility.getGroupMembersIds(this, mTextTo);
        } else {
            String[] toStringArray = (String[]) Arrays.asList(mFromName.split(",")).toArray();
            int toSenderIdsLength = toStringArray.length;
            toSenderIds = new JSONArray();
            for (int i = 0; i < toSenderIdsLength; i++) {
                if (!toStringArray[i].equalsIgnoreCase("You"))
                    toSenderIds.put(mStudentsMap.get(toStringArray[i]));
                //toSenderIds.put(toStringArray[i]);
            }

            /*for (int j = 0; j < toSenderIds.length(); j++) {
                try {
                    if (toSenderIds.getString(j).equalsIgnoreCase(mLoginName)) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            toSenderIds.remove(j);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }*/
        }
        JSONObject compJsonObj = new JSONObject();
        JSONObject msgJsonObj = new JSONObject();
        try {
            compJsonObj.put("subject", message);
            compJsonObj.put("body", message);
            compJsonObj.put("message_type", messageType);
            compJsonObj.put("sender_id", mLoginName);
            compJsonObj.put("sender_name", currUserName);
            compJsonObj.put("sender_profile_image", Base64.encodeToString(mSchoolDataUtility.getMemberProfilePic(this), 0));
            compJsonObj.put("start_date", HomeMainActivity.getDateString(System.currentTimeMillis()));
            compJsonObj.put("member_ids", toSenderIds);
            if (mIsNewGroup) {
                compJsonObj.put("group_status", 1);
            } else {
                compJsonObj.put("group_status", 2);
                compJsonObj.put("group_id", mTextTo);
            }
            msgJsonObj.put("message", compJsonObj);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        networkConn.postMessage(msgJsonObj.toString());

        JSONUtility jsonUtility = new JSONUtility();
        String[] composeColumns = {"subject", "body", "message_type", "sender_id", "sender_name", "start_date", "end_date", "group_id"};
        jsonUtility.setColumsList(composeColumns);

        try {
            ContentValues msgValues = jsonUtility.fromJSON(compJsonObj);
            mComposeMessageId = randInt();
            //msgValues.remove("sender_profile_image");
            msgValues.put("sender_profile_image", new SchoolDataUtility(mLoginName, true).getMemberProfilePic(this));
            String memberString = toSenderIds.toString();
            msgValues.put("member_ids", memberString.substring(2, memberString.length() - 2));
            msgValues.put("members_count", toSenderIds.length());
            msgValues.put("local_msg_id", mComposeMessageId);

            /*String[] toStringArray = (String[]) Arrays.asList(mFromName.split(",")).toArray();
            String[] memberNames = new String[toStringArray.length];

            for (int j = 0, i = 0; j < toStringArray.length; j++) {
                if (!toStringArray[j].equalsIgnoreCase(currUserName)) {
                    memberNames[i] = toStringArray[j];
                    i++;
                }
            }*/

            ArrayList<String> memberStringArray = new ArrayList<String>(Arrays.asList(mFromName.split(",")));
            memberStringArray.remove("You");

            msgValues.put("member_names", TextUtils.join(",", memberStringArray));

            if (messageType == 1) {
                getContentResolver().insert(Uri.parse("content://com.education.schoolapp/received_messages_all"), msgValues);
            } else if (messageType == 4) {
                String selection = "album_id like '" + album_id + "'";
                getContentResolver().update(Uri.parse("content://com.education.schoolapp/received_messages_all"), msgValues, selection, null);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private class NetworkResp implements NetworkConnectionUtility.NetworkResponseListener {
        @Override
        public void onResponse(String urlString, String networkResult) {
            if (urlString.equalsIgnoreCase(NetworkConstants.POST_MESSAGE)) {
                ContentValues msgRespValues = new ContentValues();
                String selection = " local_msg_id = " + mComposeMessageId;
                if (networkResult == null || networkResult.equalsIgnoreCase("Bad Request")) {
                    msgRespValues.put("status", 2);
                } else {
                    Log.i(TAG, "Post message is successfully done");
                    try {
                        JSONObject respObj = new JSONObject(networkResult);

                        msgRespValues.put("message_id", respObj.getJSONObject("_id").getString("$oid"));
                        //msgRespValues.put("message_id", respObj.getString("$oid"));
                        if (mTextTo.isEmpty()) {
                            mTextTo = respObj.getString("group_id");
                        }
                        msgRespValues.put("group_id", respObj.getString("group_id"));
                        //msgRespValues.put("group_id", respObj.getString("$oid"));
                        msgRespValues.put("status", 1);
                        msgRespValues.put("read_status", 0);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                getContentResolver().update(Uri.parse("content://com.education.schoolapp/received_messages_all"), msgRespValues, selection, null);
                progress.dismiss();

                mComposeEdit.setText("");
                if (mChatListAdapter != null) {
                    mChatListAdapter.updateData(getApplicationContext(), mTextTo);
                    mChatListAdapter.notifyDataSetChanged();
                }
                if (mChatList != null) {
                    mChatList.invalidate();
                    mChatList.scrollToPosition(mChatListAdapter.getItemCount() - 1);
                }
            } else if (urlString.equalsIgnoreCase(NetworkConstants.CREATE_ALBUM)) {
                if (networkResult == null) {
                    progress.dismiss();
                    return;
                }

                try {
                    JSONObject albumResp = new JSONObject(networkResult);

                    JSONArray imageArray = albumResp.getJSONArray("multimediums");
                    for (int i = 0; i < imageArray.length(); i++) {
                        ContentValues albumValues = new ContentValues();
                        String selection = " image_name like '" + imageArray.getJSONObject(i).getString("name") + "'";

                        albumValues.put("album_name", albumResp.getString("name"));
                        mAlbumId = albumResp.getJSONObject("_id").getString("$oid");
                        albumValues.put("album_id", mAlbumId);
                        albumValues.put("status", 1);
                        albumValues.put("image_id", imageArray.getJSONObject(i).getJSONObject("_id").getString("$oid"));

                        getContentResolver().update(Uri.parse(SchoolDataConstants.CONTENT_URI + SchoolDataConstants.ALBUM_IMAGES),
                                albumValues, selection, null);

                        getContentResolver().update(Uri.parse(SchoolDataConstants.CONTENT_URI + SchoolDataConstants.RECEIVED_MESSAGES_ALL),
                                albumValues, selection, null);
                    }

                    handleSendMessage("Image", mAlbumId, 4);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                progress.dismiss();

            }
        }
    }

    private int randInt() {

        // NOTE: Usually this should be a field rather than a method
        // variable so that it is not re-seeded every call.
        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((MAX_NUM - MIN_NUM) + 1) + MIN_NUM;

        return randomNum;
    }

    private void selectImage() {
        final CharSequence[] items = {getString(R.string.popup_take_photos), getString(R.string.popup_choose_gallery)};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.popup_title);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals(getString(R.string.popup_take_photos))) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, REQUEST_CAMERA);
                } else if (items[item].equals(getString(R.string.popup_choose_gallery))) {
                    Intent intent = new Intent(
                            Action.ACTION_MULTIPLE_PICK);
                    intent.setClass(getApplicationContext(), CustomGalleryActivity.class);
                    startActivityForResult(intent,
                            SELECT_FILE);
                }
            }
        });
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE) {
                onSelectFromGalleryResult(data);
            } else if (requestCode == REQUEST_CAMERA) {
                onCaptureImageResult(data);
            }
        }
    }

    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

        String fileName = System.currentTimeMillis() + ".jpg";
        File destination = new File(mAppDir, fileName);

        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ContentValues imgValues = new ContentValues();
        imgValues.put("image_local_path", destination.toString());
        Long timeInMS = System.currentTimeMillis();
        imgValues.put("image_date", HomeMainActivity.getDateInString(timeInMS));
        imgValues.put("image_time", HomeMainActivity.getTimeInString(timeInMS));
        imgValues.put("image_name", fileName);
        imgValues.put("type", "sent");

        getContentResolver().insert(Uri.parse(SchoolDataConstants.CONTENT_URI + SchoolDataConstants.ALBUM_IMAGES), imgValues);

        getContentResolver().insert(Uri.parse(SchoolDataConstants.CONTENT_URI + SchoolDataConstants.RECEIVED_MESSAGES_ALL), imgValues);

        createAlbumInServer(randomString(6));
    }

    private void onSelectFromGalleryResult(Intent data) {
        createAlbumInServer(randomString(6));
    }

    static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    static Random rnd = new Random();

    private String randomString(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        return sb.toString();
    }

    private void createAlbumInServer(String albumName) {
        JSONObject albumJsonObj = new JSONObject();
        JSONObject albumObj = new JSONObject();
        progress.setTitle(R.string.album_upload_progress);
        progress.show();
        try {
            albumJsonObj.put("name", albumName);
            albumJsonObj.put("date", HomeMainActivity.getDateString(System.currentTimeMillis()));
            albumJsonObj.put("image_data_arr", new SchoolDataUtility().getPendingImagesForAlbum(this));
            albumJsonObj.put("member_id", mLoginName);

            albumObj.put("album", albumJsonObj);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        networkConn.createAlbum(albumObj.toString());
    }
}
