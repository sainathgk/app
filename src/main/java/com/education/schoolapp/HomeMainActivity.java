package com.education.schoolapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Path;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.education.connection.schoolapp.NetworkConnectionUtility;
import com.education.database.schoolapp.SchoolDataUtility;
import com.education.service.schoolapp.SchoolDataContentObserver;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class HomeMainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private FloatingActionButton fab;

    private static final String APP_SHARED_PREFS = "school_preferences";
    private static final String SHARED_LOGIN_KEY = "schoolUserLoginState";
    private static final String SHARED_LOGIN_TYPE = "schoolUserLoginType";
    private static final String SHARED_LOGIN_NAME = "schoolUserLoginName";

    private SharedPreferences sharePrefs;
    private String mLoginType = "";
    private boolean mIsTeacher = false;
    private String mLoginName;
    private TextView mNavLoginName;
    private TextView mNavClassName;
    private ImageView mNavProfileImage;

    int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    private NetworkConnectionUtility networkConn;
    private ProgressDialog progress;
    private String[] toStringArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mViewPager.getCurrentItem() == 2) {
                    selectImage();
                    return;
                }
                Intent composeIntent = new Intent(getBaseContext(), ComposeActivity.class);
                int composeType = mViewPager.getCurrentItem();
                if (mViewPager.getCurrentItem() > 1) {
                    composeType = -1;
                }
                composeIntent.putExtra("Type", composeType + 1);
                startActivity(composeIntent);

                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
            }
        });
        fab.setVisibility(View.GONE);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mNavLoginName = (TextView) navigationView.findViewById(R.id.login_name);
        mNavClassName = (TextView) navigationView.findViewById(R.id.class_name);
        mNavProfileImage = (ImageView) navigationView.findViewById(R.id.profile_image);

        getContentResolver().registerContentObserver(Uri.parse("content://com.education.schoolapp/received_notifications"), true, new SchoolDataContentObserver(new Handler(), this));

        sharePrefs = getApplicationContext().getSharedPreferences(APP_SHARED_PREFS, Context.MODE_PRIVATE);

        mLoginName = sharePrefs.getString(SHARED_LOGIN_NAME, "");
        mLoginType = sharePrefs.getString(SHARED_LOGIN_TYPE, "");
        if (!mLoginType.isEmpty()) {
            mIsTeacher = mLoginType.equalsIgnoreCase("Teacher");
            if (mIsTeacher) {
                //Action button should be enable for all the fragments.
                fab.setVisibility(View.VISIBLE);
                //Hide the Student profile item from Navigation drawer.
                navigationView.getMenu().findItem(R.id.nav_student_profile).setVisible(false);
                navigationView.getMenu().findItem(R.id.nav_saved_messages).setTitle("Outbox Messages");
                /*mNavLoginName.setText(getResources().getString(R.string.teacher_name));
                mNavClassName.setText("Class Teacher : UKG");
                mNavProfileImage.setImageBitmap(GetBitmapClippedCircle(BitmapFactory.decodeResource(getResources(), R.drawable.teacher_)));*/
            } /*else {
                String[] studentDetails = new SchoolDataUtility(mLoginName, false).getStudentName(this);

                mNavLoginName.setText(studentDetails[0]);
                mNavClassName.setText("Class : " + studentDetails[1]);
                if (mLoginName.equalsIgnoreCase("abc")) {
                    mNavProfileImage.setImageBitmap(GetBitmapClippedCircle(BitmapFactory.decodeResource(getResources(), R.drawable.student)));
                } else {
                    mNavProfileImage.setImageBitmap(GetBitmapClippedCircle(BitmapFactory.decodeResource(getResources(), R.drawable.student)));
                }
            }*/
            SchoolDataUtility schoolData = new SchoolDataUtility(mLoginName, false);

            String[] studentDetails = schoolData.getStudentName(this);
            byte[] profilePic = schoolData.getMemberProfilePic(this);

            mNavLoginName.setText(studentDetails[0]);
            mNavClassName.setText("Class : " + studentDetails[1]);
            mNavProfileImage.setImageBitmap(BitmapFactory.decodeByteArray(profilePic, 0, profilePic.length));
        }


        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(1, true);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (mIsTeacher) {
                    fab.setVisibility(View.VISIBLE);
                } else {
                    if (position == 0) {
                        fab.setVisibility(View.VISIBLE);
                    } else {
                        fab.setVisibility(View.GONE);
                    }
                }
                if (position == 2) {
                    fab.setImageResource(android.R.drawable.ic_menu_camera);
                } else {
                    fab.setImageResource(android.R.drawable.ic_dialog_email);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
/*

        progress = new ProgressDialog(this);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.setTitle("Fetching Data ...");
        progress.show();

        networkConn = new NetworkConnectionUtility();

        NetworkResp networkResp = new NetworkResp();
        networkConn.setNetworkListener(networkResp);
*/

        /*String className = getIntent().getStringExtra("class");
        if (className == null) {
            progress.dismiss();
            return;
        }
        toStringArray = (String[]) Arrays.asList(className.split(",")).toArray();

        for (int clas = 0; clas < toStringArray.length; clas++)
            networkConn.getStudents(toStringArray[clas]);*/

        /*String[] receivedMsgs = new SchoolDataUtility().getPendingMessages(this);
        if (receivedMsgs != null && receivedMsgs.length > 0) {
            for (int msg = 0; msg < receivedMsgs.length; msg++) {
                progress.show();
                networkConn.getMessage(receivedMsgs[msg]);
            }
        }*/
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /*private class NetworkResp implements NetworkConnectionUtility.NetworkResponseListener {
        @Override
        public void onResponse(String urlString, String networkResult) {
            if (urlString.startsWith(NetworkConstants.GET_CLASS_STUDENTS)) {
                if (networkResult == null) {
                    Log.i("Network", "Get Class Students API");
                    return;
                }
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
                        progress.dismiss();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } *//*else if (urlString.startsWith(NetworkConstants.GET_MESSAGE)) {
                if (networkResult == null) {
                    Log.i("Network", "Get Message API");
                    return;
                }
                try {
                    JSONObject messageObj = new JSONObject(networkResult);
                    String[] messageProjection = {"subject", "body", "sender_id", *//**//*"start_date", "end_date", *//**//*"message_type"};
                    JSONUtility jsonUtility = new JSONUtility();
                    jsonUtility.setColumsList(messageProjection);

                    ContentValues msgValues = jsonUtility.fromJSON(messageObj);
                    msgValues.put("message_id", messageObj.getJSONObject("_id").getString("$oid"));

                    JSONArray membersArray = messageObj.getJSONArray("member_ids");
                    String[] memberIds = new String[membersArray.length()];
                    String[] memberNames = new String[membersArray.length()];
                    HashMap<String, String> mStudentsMap = new SchoolDataUtility().getClassStudents(getApplicationContext());
                    if (mStudentsMap != null) {
                        for (int i = 0; i < memberIds.length; i++) {
                            memberIds[i] = membersArray.getJSONObject(i).getString("$oid");
                            for (Map.Entry<String, String> entry : mStudentsMap.entrySet()) {
                                if (entry.getValue().equals(memberIds[i])) {
                                    System.out.println(entry.getKey());
                                    memberNames[i] = entry.getKey();
                                }
                            }
                        }
                    }

                    String memberIdString = Joiner.on(",").skipNulls().join(memberIds);
                    msgValues.put("member_ids", memberIdString);
                    msgValues.put("member_names", Joiner.on(",").skipNulls().join(memberNames));
                    msgValues.put("sender_profile_image", Base64.decode(messageObj.getString("sender_profile_image"), 0));
                    //TODO : To be fixed, get the sender name from the Message Response only
                    msgValues.put("sender_name", new SchoolDataUtility().getTeacherNameforStudent(getApplicationContext(), mLoginName));

                    getContentResolver().insert(Uri.parse("content://com.education.schoolapp/received_messages_all"), msgValues);

                    ContentValues msgIdUpdate = new ContentValues();
                    String selection = " message_id like '" + messageObj.getJSONObject("_id").getString("$oid") + "'";
                    msgIdUpdate.put("status", 1);

                    getContentResolver().update(Uri.parse("content://com.education.schoolapp/server_message_ids"), msgIdUpdate, selection, null);
                    progress.dismiss();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }*//*
        }
    }
*/
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public static Bitmap GetBitmapClippedCircle(Bitmap bitmap) {

        if (bitmap == null) {
            return null;
        }
        final int width = bitmap.getWidth();
        final int height = bitmap.getHeight();
        final Bitmap outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        final Path path = new Path();
        path.addCircle((float) (width / 2), (float) (height / 2), (float) (Math.min((width), (height / 2)) * 0.8),
                Path.Direction.CCW);

        final Canvas canvas = new Canvas(outputBitmap);
        canvas.clipPath(path);
        canvas.drawBitmap(bitmap, 0, 0, null);
        return outputBitmap;
    }

    public static String getDateString(String timeMilliSeconds) {
        if (timeMilliSeconds == null || timeMilliSeconds.equalsIgnoreCase("null"))
            return "";

        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MMM-yyyy hh:mm a");

        Calendar calendar = Calendar.getInstance();
        try {
            Long mLongTime = Long.parseLong(timeMilliSeconds);
            calendar.setTimeInMillis(mLongTime);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return "";
        }

        return dateFormatter.format(calendar.getTime());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.home_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_student_profile) {
            Intent profileIntent = new Intent(this, ProfileScrollingActivity.class);
            startActivity(profileIntent);
        } else if (id == R.id.nav_saved_messages) {
            Intent profileIntent = new Intent(this, SavedMessagesActivity.class);
            startActivity(profileIntent);
        } else if (id == R.id.nav_downloads) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_sign_out) {
            SharedPreferences.Editor editor = sharePrefs.edit();
            editor.putBoolean(SHARED_LOGIN_KEY, false);
            editor.commit();

            getContentResolver().delete(Uri.parse("content://com.education.schoolapp/identity"), null, null);

            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            if (position == 0) {
                return MessagesFragment.newInstance("Messages", mLoginName, mIsTeacher);
            } else if (position == 1) {
                return MessagesFragment.newInstance("Notifications", mLoginName, mIsTeacher);
            } else if (position == 2) {
                return AlbumFragment.newInstance("Album", mLoginName);
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Messages";
                case 1:
                    return "Notifications";
                case 2:
                    return "Albums";
            }
            return null;
        }
    }

    private void selectImage() {
        final CharSequence[] items = {"Take Photo", "Choose from Library",
                "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, REQUEST_CAMERA);
                } else if (items[item].equals("Choose from Library")) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(
                            Intent.createChooser(intent, "Select File"),
                            SELECT_FILE);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data);
        }
    }

    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

        File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");

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

        //TODO. set the image to Album Grid view
        //ivImage.setImageBitmap(thumbnail);
    }

    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {
        Uri selectedImageUri = data.getData();
        String[] projection = {MediaStore.MediaColumns.DATA};
        Cursor cursor = managedQuery(selectedImageUri, projection, null, null,
                null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        cursor.moveToFirst();

        String selectedImagePath = cursor.getString(column_index);

        Bitmap bm;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(selectedImagePath, options);
        final int REQUIRED_SIZE = 200;
        int scale = 1;
        while (options.outWidth / scale / 2 >= REQUIRED_SIZE
                && options.outHeight / scale / 2 >= REQUIRED_SIZE)
            scale *= 2;
        options.inSampleSize = scale;
        options.inJustDecodeBounds = false;
        bm = BitmapFactory.decodeFile(selectedImagePath, options);

        //TODO. set the image to Album Grid view
        //ivImage.setImageBitmap(bm);
    }
}
