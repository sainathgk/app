package com.education.schoolapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.education.connection.schoolapp.JSONUtility;
import com.education.connection.schoolapp.NetworkConnectionUtility;
import com.education.connection.schoolapp.NetworkConstants;
import com.education.database.schoolapp.SchoolDataUtility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    private SharedPreferences sharePrefs;
    private String mLoginName;
    private int mComposeMessageId;
    private HashMap<String, String> mStudentsMap;
    private String mTextTo;
    private String mFromName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_chat_view);
        mFromName = getIntent().getStringExtra("msg_title");
        setTitle(mFromName);
        mTextTo = getIntent().getStringExtra("msg_members");

        mChatList = (RecyclerView) findViewById(R.id.chat_messages_list);
        mChatList.setHasFixedSize(false);

        mChatList.setLayoutManager(new LinearLayoutManager(this));
        mChatList.setItemAnimator(new DefaultItemAnimator());

        mChatListAdapter = new MessageChatViewAdapter();
        mChatListAdapter.updateData(this, mTextTo);
        mChatList.setAdapter(mChatListAdapter);

        mChatList.scrollToPosition(mChatListAdapter.getItemCount() - 1);

        mComposeSendBtn = (ImageButton) findViewById(R.id.sendMessageButton);
        mComposeSendBtn.setOnClickListener(this);

        mComposeEdit = (EditText) findViewById(R.id.message);

        progress = new ProgressDialog(this);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.setTitle(R.string.compose_send_progress);

        sharePrefs = getApplicationContext().getSharedPreferences(APP_SHARED_PREFS, Context.MODE_PRIVATE);
        mLoginName = sharePrefs.getString(SHARED_LOGIN_NAME, "");

        mStudentsMap = new SchoolDataUtility().getClassStudents(this);
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.sendMessageButton) {
            String composeText = mComposeEdit.getText().toString();

            if (composeText.isEmpty()) {
                return;
            }

            handleSendMessage(composeText);
        } else if (v.getId() == R.id.attachmentButton) {
            selectImage();
        }
    }

    private void handleSendMessage(String message) {
        progress.show();

        networkConn = new NetworkConnectionUtility();

        NetworkResp networkResp = new NetworkResp();
        networkConn.setNetworkListener(networkResp);

        String[] toStringArray = (String[]) Arrays.asList(mTextTo.split(",")).toArray();
        JSONArray toSenderIds = new JSONArray();
        int toSenderIdsLength = toStringArray.length;

        for (int i = 0; i < toSenderIdsLength; i++) {
            //toSenderIds.put(mStudentsMap.get(toStringArray[i]));
            toSenderIds.put(toStringArray[i]);
        }

        JSONObject compJsonObj = new JSONObject();
        JSONObject msgJsonObj = new JSONObject();
        try {
            compJsonObj.put("subject", message);
            compJsonObj.put("body", message);
            compJsonObj.put("message_type", 1);
            compJsonObj.put("sender_id", mLoginName);
            compJsonObj.put("sender_name", new SchoolDataUtility(mLoginName, true).getStudentName(getApplicationContext())[0]);
            compJsonObj.put("sender_profile_image", Base64.encodeToString(new SchoolDataUtility(mLoginName, true).getMemberProfilePic(this), 0));
            compJsonObj.put("start_date", HomeMainActivity.getDateString(System.currentTimeMillis()));
            compJsonObj.put("member_ids", toSenderIds);
            msgJsonObj.put("message", compJsonObj);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        networkConn.postMessage(msgJsonObj.toString());

        JSONUtility jsonUtility = new JSONUtility();
        String[] composeColumns = {"subject", "body", "message_type", "sender_id", "sender_name", "start_date", "end_date", "member_ids"};
        jsonUtility.setColumsList(composeColumns);

        try {
            ContentValues msgValues = jsonUtility.fromJSON(compJsonObj);
            mComposeMessageId = randInt();
            msgValues.remove("sender_profile_image");
            msgValues.put("sender_profile_image", new SchoolDataUtility(mLoginName, true).getMemberProfilePic(this));
            msgValues.put("local_msg_id", mComposeMessageId);
            msgValues.put("member_names", mTextTo);

            getContentResolver().insert(Uri.parse("content://com.education.schoolapp/received_messages_all"), msgValues);
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

                        msgRespValues.put("message_id", respObj.getString("$oid"));
                        msgRespValues.put("status", 1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                getContentResolver().update(Uri.parse("content://com.education.schoolapp/received_messages_all"), msgRespValues, selection, null);
                progress.dismiss();

                mComposeEdit.setText("");
                if (mChatListAdapter != null) {
                    mChatListAdapter.updateData(getApplicationContext(), mFromName);
                    mChatListAdapter.notifyDataSetChanged();
                }
                if (mChatList != null) {
                    mChatList.invalidate();
                }
                //finish();
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
                //onSelectFromGalleryResult(data);
            }
            else if (requestCode == REQUEST_CAMERA) {
                //onCaptureImageResult(data);
            }
        }
    }
}
