package com.education.schoolapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.education.database.schoolapp.ProfileItem;
import com.education.database.schoolapp.SchoolDataUtility;

public class ProfileScrollingActivity extends AppCompatActivity {

    private static final String APP_SHARED_PREFS = "school_preferences";
    private static final String SHARED_LOGIN_NAME = "schoolUserLoginName";
    private SharedPreferences sharePrefs;
    private String mLoginName;
    private ProfileItem mProfileDetails;
    private TextView mNameView;
    private TextView mClassView;
    private TextView mSectionView;
    private TextView mBloodGroupView;
    private TextView mFatherView;
    private TextView mMotherView;
    private TextView mGuardianView;
    private TextView mTeacherView;
    private ImageView mProfileImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_scrolling);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Attendance UI appear on this action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mNameView = ((TextView) findViewById(R.id.actual_name));
        mClassView = ((TextView) findViewById(R.id.actual_class));
        mSectionView = ((TextView) findViewById(R.id.actual_section));
        mBloodGroupView = ((TextView) findViewById(R.id.actual_blood_group));
        mFatherView = ((TextView) findViewById(R.id.actual_father_name));
        mMotherView = ((TextView) findViewById(R.id.actual_mother_name));
        mGuardianView = ((TextView) findViewById(R.id.actual_guardian_name));
        mTeacherView = ((TextView) findViewById(R.id.actual_teacher_name));
        mProfileImageView = ((ImageView) findViewById(R.id.actual_profile_image));
    }


    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        sharePrefs = getApplicationContext().getSharedPreferences(APP_SHARED_PREFS, Context.MODE_PRIVATE);

        mLoginName = sharePrefs.getString(SHARED_LOGIN_NAME, "");
        if (!mLoginName.isEmpty()) {
            mProfileDetails = new SchoolDataUtility(mLoginName, false).getStudentProfileDetails(this);

            if (mProfileDetails != null) {
                mNameView.setText(mProfileDetails.studentName);
                mClassView.setText(mProfileDetails.studentClass);
                mSectionView.setText(mProfileDetails.studentSection);
                mBloodGroupView.setText(mProfileDetails.studentBloodGroup);
                mFatherView.setText(mProfileDetails.studentFatherName);
                mMotherView.setText(mProfileDetails.studentMotherName);
                mGuardianView.setText(mProfileDetails.studentGuardianName);
                mProfileImageView.setImageBitmap(mProfileDetails.studentImage);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.menu_profile_scrolling, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_edit_profile:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
