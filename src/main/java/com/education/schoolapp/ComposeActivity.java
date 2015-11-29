package com.education.schoolapp;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.RadioGroup;
import android.widget.TimePicker;
import android.widget.Toast;

import com.education.connection.schoolapp.JSONUtility;
import com.education.connection.schoolapp.NetworkConnectionUtility;
import com.education.connection.schoolapp.NetworkConstants;
import com.education.database.schoolapp.SchoolDataUtility;
import com.google.common.base.Joiner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ComposeActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = ComposeActivity.class.toString();
    private EditText mTitleView;
    private EditText mDescView;
    private MultiAutoCompleteTextView mToView;
    private LinearLayout mDateTimeLayout;
    private int mType;
    private int mYear;
    private int mMonth;
    private int mDay;
    private int mHour;
    private int mMinute;
    private Button mDatePick;
    private Button mTimePick;
    private SharedPreferences sharePrefs;
    private String mLoginName;

    private static final String APP_SHARED_PREFS = "school_preferences";
    private static final String SHARED_LOGIN_NAME = "schoolUserLoginName";
    private static final String SHARED_LOGIN_TYPE = "schoolUserLoginType";
    private String mLoginType;
    private boolean mIsTeacher = false;
    final Calendar c = Calendar.getInstance();
    private int mSelectedYear;
    private int mSelectedMonth;
    private int mSelectedDay;
    private int mSelectedHour;
    private int mSelectedMinute;
    private RadioGroup mRadioCompose;

    private static final int MIN_NUM = 1;
    private static final int MAX_NUM = 1000;
    private NetworkConnectionUtility networkConn;
    private int mComposeMessageId;
    private HashMap<String, String> mStudentsMap;
    private ArrayAdapter<String> adapter;
    private String[] mStudentIdArray;
    private String mTitle;
    private String mDescription;
    private String mToText;
    private String mFromText;
    private String mComposeType;
    private String mAlbumId;
    private String mTeacherId = "";
    private boolean isNotReply = true;
    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        /*getActionBar().setHomeButtonEnabled(true);
        getActionBar().setIcon(R.drawable.school_logo);*/
        setContentView(R.layout.compose_view);

        mToView = (MultiAutoCompleteTextView) findViewById(R.id.msg_to);
        mTitleView = (EditText) findViewById(R.id.msg_title);
        mDescView = (EditText) findViewById(R.id.msg_description);

        mDateTimeLayout = (LinearLayout) findViewById(R.id.date_time_layout);
        mDatePick = (Button) findViewById(R.id.date_button);
        mTimePick = (Button) findViewById(R.id.time_button);

        mType = getIntent().getIntExtra("Type", -1);
        if (mType == 2) {
            mDateTimeLayout.setVisibility(View.VISIBLE);
        } else if (mType == 4) {
            mAlbumId = getIntent().getStringExtra("albumId");
        }

        mRadioCompose = (RadioGroup) findViewById(R.id.radio_compose);

        String msgBox = getIntent().getStringExtra("msg_box");
        String msgId = getIntent().getStringExtra("msg_id");
        mComposeType = getIntent().getStringExtra("compose_type");

        sharePrefs = getApplicationContext().getSharedPreferences(APP_SHARED_PREFS, Context.MODE_PRIVATE);
        mLoginName = sharePrefs.getString(SHARED_LOGIN_NAME, "");
        mLoginType = sharePrefs.getString(SHARED_LOGIN_TYPE, "");
        if (!mLoginType.isEmpty()) {
            mIsTeacher = mLoginType.equalsIgnoreCase("Teacher");
        }

        if (msgBox != null && msgId != null) {

            String msgItem = new SchoolDataUtility().getMessage(this.getApplicationContext(), msgBox, mType, msgId);
            if (msgItem == null) {
                Toast.makeText(getApplicationContext(), "Message is null", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            mRadioCompose.setVisibility(View.GONE);

            try {
                JSONObject msgJsonObj = new JSONObject(msgItem);
                if (!msgJsonObj.optString("subject").isEmpty()) {
                    mTitle = msgJsonObj.getString("subject");
                }
                mTitleView.setText(mTitle);
                if (!msgJsonObj.optString("body").isEmpty()) {
                    mDescription = msgJsonObj.getString("body");
                }

                mDescView.setText(mDescription);
                mToText = msgJsonObj.getString("member_names");
                mFromText = msgJsonObj.getString("sender_name");

                mTeacherId = msgJsonObj.getString("sender_id");

                if (mComposeType.equalsIgnoreCase("reply")) {
                    isNotReply = false;
                    mToView.setText(mFromText);
                } else if (mComposeType.equalsIgnoreCase("replyAll")) {
                    isNotReply = false;
                    mToView.setText(mFromText + ", " + mToText);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this, "JSON Exception", Toast.LENGTH_SHORT).show();
            }

        }

        mDatePick.setOnClickListener(this);
        mTimePick.setOnClickListener(this);

        mToView.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

        mStudentsMap = new SchoolDataUtility().getClassStudents(this);
        if (mStudentsMap != null) {
            mStudentIdArray = new String[mStudentsMap.size()];
            int idx = 0;

            for (Map.Entry<String, String> mapEntry : mStudentsMap.entrySet()) {
                mStudentIdArray[idx] = mapEntry.getKey();
                idx++;
            }

            adapter =
                    new ArrayAdapter<String>(ComposeActivity.this,
                            android.R.layout.simple_spinner_dropdown_item, mStudentIdArray);

            adapter.setDropDownViewResource(android.R.layout.simple_expandable_list_item_1);
        }

        mRadioCompose.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.individual_radioButton2:
                        mToView.setVisibility(View.VISIBLE);
                        break;

                    case R.id.broadcast_radioButton:
                        mToView.setVisibility(View.GONE);
                        break;
                }
            }
        });

        if (!mIsTeacher) {
            mRadioCompose.setVisibility(View.GONE);
            //mToView.setText("Teacher");
        } else {
            mToView.setThreshold(1);
            mToView.setAdapter(adapter);
        }

        progress = new ProgressDialog(this);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.setTitle(R.string.compose_send_progress);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.compose_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_send) {
            handleSendMessage();
        }
        return super.onOptionsItemSelected(item);
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

    private void handleSendMessage() {
        String textTo = Joiner.on(",").skipNulls().join(mStudentIdArray);

        if (mRadioCompose.getCheckedRadioButtonId() == R.id.individual_radioButton2) {
            textTo = mToView.getText().toString();
            if (textTo.isEmpty()) {
                mToView.setFocusable(true);
                return;
            }
        }
        String textTitle = mTitleView.getText().toString();
        if (textTitle.isEmpty()) {
            mTitleView.setFocusable(true);
            return;
        }
        String textDesc = mDescView.getText().toString();
        if (textDesc.isEmpty()) {
            mDescView.setFocusable(true);
            return;
        }

        if (mType == 2) {
            String textDate = mDatePick.getText().toString();
            String textTime = mTimePick.getText().toString();
            if (textDate.equalsIgnoreCase("Set Date") || textTime.equalsIgnoreCase("Set Time")) {
                Toast.makeText(getApplicationContext(), "Please select the Date & Time", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        progress.show();

        networkConn = new NetworkConnectionUtility();

        NetworkResp networkResp = new NetworkResp();
        networkConn.setNetworkListener(networkResp);

        String[] toStringArray = (String[]) Arrays.asList(textTo.split(",")).toArray();
        JSONArray toSenderIds = new JSONArray();
        int toSenderIdsLength = toStringArray.length;
        if (isNotReply) {
            toSenderIdsLength--;
        }
        for (int i = 0; i < toSenderIdsLength; i++) {
            toSenderIds.put(mStudentsMap.get(toStringArray[i]));
        }

        if (!mTeacherId.isEmpty()) {
            toSenderIds.put(mTeacherId);
        }
        c.set(mSelectedYear, mSelectedMonth, mSelectedDay, mSelectedHour, mSelectedMinute);

        JSONObject compJsonObj = new JSONObject();
        JSONObject msgJsonObj = new JSONObject();
        try {
            compJsonObj.put("subject", textTitle);
            compJsonObj.put("body", textDesc);
            compJsonObj.put("message_type", mType);
            compJsonObj.put("sender_id", mLoginName);
            compJsonObj.put("sender_name", new SchoolDataUtility(mLoginName, true).getStudentName(getApplicationContext())[0]);
            compJsonObj.put("sender_profile_image", Base64.encodeToString(new SchoolDataUtility(mLoginName, true).getMemberProfilePic(this), 0));
            if (mType == 2) {
                compJsonObj.put("start_date", HomeMainActivity.getDateString(c.getTimeInMillis()));
                compJsonObj.put("end_date", HomeMainActivity.getDateString(c.getTimeInMillis() + 10000));
            } else if (mType == 1) {
                compJsonObj.put("start_date", HomeMainActivity.getDateString(System.currentTimeMillis()));
            } else if (mType == 4) {
                compJsonObj.put("album_ids", new JSONArray().put(mAlbumId));
            }
            compJsonObj.put("member_ids", toSenderIds);
            msgJsonObj.put("message", compJsonObj);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        networkConn.postMessage(msgJsonObj.toString());

        JSONUtility jsonUtility = new JSONUtility();
        String[] composeColumns = {"subject", "body", "message_type", "sender_id", "start_date", "end_date", "member_ids"};
        jsonUtility.setColumsList(composeColumns);

        try {
            ContentValues msgValues = jsonUtility.fromJSON(compJsonObj);
            mComposeMessageId = randInt();
            msgValues.remove("sender_profile_image");
            msgValues.put("sender_profile_image", new SchoolDataUtility(mLoginName, true).getMemberProfilePic(this));
            msgValues.put("local_msg_id", mComposeMessageId);
            msgValues.put("member_names", textTo);
            msgValues.put("sender_name", new SchoolDataUtility(mLoginName, true).getStudentName(this)[0]);

            getContentResolver().insert(Uri.parse("content://com.education.schoolapp/sent_messages_all"), msgValues);
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
                getContentResolver().update(Uri.parse("content://com.education.schoolapp/sent_messages_all"), msgRespValues, selection, null);
                progress.dismiss();
                finish();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.date_button:
                // Process to get Current Date
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);
                // Launch Date Picker Dialog
                DatePickerDialog dpd = new DatePickerDialog(this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                // Display Selected date in textbox
                                String date = dayOfMonth + " - "
                                        + (monthOfYear + 1) + " - " + year;
                                mDatePick.setText(date);
                                mSelectedYear = year;
                                mSelectedMonth = monthOfYear;
                                mSelectedDay = dayOfMonth;
                            }
                        }, mYear, mMonth, mDay);
                dpd.show();
                break;

            case R.id.time_button:
                // Process to get Current Time
                mHour = c.get(Calendar.HOUR_OF_DAY);
                mMinute = c.get(Calendar.MINUTE);
                // Launch Time Picker Dialog
                TimePickerDialog tpd = new TimePickerDialog(this,
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                  int minute) {
                                // Display Selected time in textbox
                                int displayHour = hourOfDay;
                                String ampm = "AM";
                                if (hourOfDay > 12) {
                                    displayHour = hourOfDay - 12;
                                    ampm = "PM";
                                }
                                mTimePick.setText(displayHour + " : " + minute + " " + ampm);
                                mSelectedHour = hourOfDay;
                                mSelectedMinute = minute;
                            }
                        }, mHour, mMinute, false);
                tpd.show();
                break;
        }
    }
}
