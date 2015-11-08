package com.education.schoolapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class WelcomeActivity extends AppCompatActivity {
    private boolean isLoggedIn;

    private static final String APP_SHARED_PREFS = "school_preferences";
    private static final String SHARED_LOGIN_KEY = "schoolUserLoginState";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_welcome);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
        isLoggedInFunction();
    }

    private void isLoggedInFunction() {
        SharedPreferences sharePrefs = getApplicationContext().getSharedPreferences(APP_SHARED_PREFS, Context.MODE_PRIVATE);

        isLoggedIn = sharePrefs.getBoolean(SHARED_LOGIN_KEY, false);
    }

    private final Handler mHideHandler = new Handler();

    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            //hide();
            Intent launchIntent = new Intent();
            launchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            if (isLoggedIn) {
                launchIntent.setClass(getApplicationContext(), HomeMainActivity.class);
            } else {
                launchIntent.setClass(getApplicationContext(), LoginActivity.class);
            }
            startActivity(launchIntent);
            finish();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}
