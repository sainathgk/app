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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.education.connection.schoolapp.JSONUtility;
import com.education.connection.schoolapp.NetworkConnectionUtility;
import com.education.service.schoolapp.QuickstartPreferences;
import com.education.service.schoolapp.RegistrationIntentService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

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
        progress.setTitle("Authenticating ...");

        networkConn = new NetworkConnectionUtility();

        NetworkResp networkResp = new NetworkResp();
        networkConn.setNetworkListener(networkResp);
    }

    private class NetworkResp implements NetworkConnectionUtility.NetworkResponseListener {
        @Override
        public void onResponse(String urlString, String networkResult) {
            //Toast.makeText(getApplicationContext(), "Posted Successfully " + networkResult, Toast.LENGTH_SHORT).show();
            if (networkResult == null) {
                return;
            }
            //TODO : Add user profile data to DB.
            progress.dismiss();
            launhHomeActivity();
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

        progress.show();

        int mSelectedRadioId = mRadioLayout.getCheckedRadioButtonId();
        switch (mSelectedRadioId) {
            case R.id.parent_radioButton:
                mLoginType = "Parent";
                addStudentDetails(userNameText);
                break;

            case R.id.teacher_radioButton2:
                mLoginType = "Teacher";
                userNameText = getResources().getString(R.string.teacher_name);
                break;
        }

        SharedPreferences sharePrefs = getApplicationContext().getSharedPreferences(APP_SHARED_PREFS, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharePrefs.edit();
        editor.putBoolean(SHARED_LOGIN_KEY, true);
        editor.putString(SHARED_LOGIN_TYPE, mLoginType);
        editor.putString(SHARED_LOGIN_NAME, userNameText.toLowerCase());
        editor.apply();

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
        Intent homeIntent = new Intent(this, HomeMainActivity.class);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
