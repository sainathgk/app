package com.education.schoolapp;

import android.app.DatePickerDialog;
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
        }

        mDatePick.setOnClickListener(this);
        mTimePick.setOnClickListener(this);

        sharePrefs = getApplicationContext().getSharedPreferences(APP_SHARED_PREFS, Context.MODE_PRIVATE);
        mLoginName = sharePrefs.getString(SHARED_LOGIN_NAME, "");
        mLoginType = sharePrefs.getString(SHARED_LOGIN_TYPE, "");
        if (!mLoginType.isEmpty()) {
            mIsTeacher = mLoginType.equalsIgnoreCase("Teacher");
        }

        mToView.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

        mStudentsMap = new SchoolDataUtility().getClassStudents(this);
        if (mStudentsMap != null) {
            String[] studentArray = new String[mStudentsMap.size()];
            int idx = 0;

            for (Map.Entry<String, String> mapEntry : mStudentsMap.entrySet()) {
                studentArray[idx] = mapEntry.getKey();
                idx++;
            }
            adapter =
                    new ArrayAdapter<String>(ComposeActivity.this,
                            android.R.layout.simple_spinner_dropdown_item, studentArray);

            adapter.setDropDownViewResource(android.R.layout.simple_expandable_list_item_1);
        }

        mRadioCompose = (RadioGroup) findViewById(R.id.radio_compose);
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
            mToView.setText("Teacher");
        } else {
            mToView.setThreshold(1);
            mToView.setAdapter(adapter);
        }
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
        //TODO: Handle the Send of Compose message.
        //Perform DB operation.
        String[] senderIds = getResources().getStringArray(R.array.toList);

        String textTo = senderIds.toString();
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
        networkConn = new NetworkConnectionUtility();

        NetworkResp networkResp = new NetworkResp();
        networkConn.setNetworkListener(networkResp);

        String[] toStringArray = (String[]) Arrays.asList(textTo.split(",")).toArray();
        JSONArray toSenderIds = new JSONArray();
        for (int i = 0; i < toStringArray.length-1; i++) {
            toSenderIds.put(mStudentsMap.get(toStringArray[i]));
        }
        c.set(mSelectedYear, mSelectedMonth, mSelectedDay, mSelectedHour, mSelectedMinute);

        JSONObject compJsonObj = new JSONObject();
        JSONObject msgJsonObj = new JSONObject();
        try {
            compJsonObj.put("subject", textTitle);
            compJsonObj.put("body", textDesc);
            compJsonObj.put("message_type", mType);
            compJsonObj.put("sender_id", mLoginName);
            compJsonObj.put("sender_profile_image", Base64.encodeToString(new SchoolDataUtility(mLoginName, true).getMemberProfilePic(this), 0));
            if (mType == 2) {
                compJsonObj.put("start_date", c.getTimeInMillis());
                compJsonObj.put("end_date", c.getTimeInMillis() + 10000);
            } else if (mType == 1) {
                compJsonObj.put("start_date", System.currentTimeMillis());
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

        /*ContentValues msgValues = new ContentValues();
        msgValues.put("subject", textTitle);
        msgValues.put("body", textDesc);
        msgValues.put("type", mType);
        msgValues.put("sender_id", mLoginName);
        msgValues.put("sender_profile_image", new SchoolDataUtility().getMemberProfilePic(this, mLoginName));
        msgValues.put("start_date", c.getTimeInMillis());
        msgValues.put("end_date", c.getTimeInMillis() + 10000);
        msgValues.put("member_ids", String.valueOf(toStringArray));

        msgValues.put("msg_id", randInt());
        msgValues.put("to_name", textTo);
        if (mIsTeacher) {
            msgValues.put("from_name", getResources().getString(R.string.teacher_name));
            Bitmap proBitmap = null;
            if (((BitmapDrawable) getResources().getDrawable(R.drawable.teacher_)) != null) {
                proBitmap = ((BitmapDrawable) getResources().getDrawable(R.drawable.teacher_)).getBitmap();
            }
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            proBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            msgValues.put("from_image", outputStream.toByteArray());
        } else {
            msgValues.put("from_name", mLoginName);
            //TODO: Put the blob content as well.
            if (mLoginName.equalsIgnoreCase("abc")) {
                Bitmap proBitmap = null;
                if (((BitmapDrawable) getResources().getDrawable(R.drawable.student)) != null) {
                    proBitmap = ((BitmapDrawable) getResources().getDrawable(R.drawable.student)).getBitmap();
                }
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                proBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                msgValues.put("from_image", outputStream.toByteArray());
            } else {
                Bitmap proBitmap = null;
                if (((BitmapDrawable) getResources().getDrawable(R.drawable.student)) != null) {
                    proBitmap = ((BitmapDrawable) getResources().getDrawable(R.drawable.student)).getBitmap();
                }
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                proBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                msgValues.put("from_image", outputStream.toByteArray());
            }
        }

        if (mType == 1) {
            c.set(mSelectedYear, mSelectedMonth, mSelectedDay, mSelectedHour, mSelectedMinute);
            msgValues.put("noti_title", textTitle);
            msgValues.put("noti_description", textDesc);
            msgValues.put("noti_date", c.getTimeInMillis());

            getContentResolver().insert(Uri.parse("content://com.education.schoolapp/sent_notifications"), msgValues);

            getContentResolver().insert(Uri.parse("content://com.education.schoolapp/received_notifications"), msgValues);

            Notification.Builder builder = new Notification.Builder(this);
            builder.setContentTitle(textTitle);
            builder.setContentText(textDesc);
            builder.setSmallIcon(R.drawable.school_logo);
            Notification notification = builder.build();

            notification.defaults |= Notification.DEFAULT_ALL;

            Intent notificationIntent = new Intent(this.getApplicationContext(), NotificationReceiver.class);
            notificationIntent.putExtra(NotificationReceiver.NOTIFICATION_ID, 1);
            notificationIntent.putExtra(NotificationReceiver.NOTIFICATION, notification);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);

            Toast.makeText(getApplicationContext(), "Notification has been sent Successfully", Toast.LENGTH_SHORT).show();
        } else {
            msgValues.put("msg_title", textTitle);
            msgValues.put("msg_description", textDesc);
            msgValues.put("msg_date", System.currentTimeMillis());

            getContentResolver().insert(Uri.parse("content://com.education.schoolapp/sent_messages"), msgValues);

            getContentResolver().insert(Uri.parse("content://com.education.schoolapp/received_messages"), msgValues);
            Toast.makeText(getApplicationContext(), "Message has been sent Successfully", Toast.LENGTH_SHORT).show();
        }
        finish();*/
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
