package com.education.schoolapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
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
import android.support.v4.app.ActivityCompat;
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
import android.text.InputType;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.education.connection.schoolapp.JSONUtility;
import com.education.connection.schoolapp.NetworkConnectionUtility;
import com.education.connection.schoolapp.NetworkConstants;
import com.education.database.schoolapp.SchoolDataConstants;
import com.education.database.schoolapp.SchoolDataUtility;
import com.education.service.schoolapp.SchoolDataContentObserver;
import com.google.common.base.Joiner;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;

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

    int REQUEST_CAMERA = 0, SELECT_FILE = 100;
    private NetworkConnectionUtility networkConn;
    private ProgressDialog progress;
    private String[] toStringArray;

    ImageLoader imageLoader;
    private MenuItem mUploadMenuItem;
    private String mAlbumName = "";
    private MultiAutoCompleteTextView mToView;
    private HashMap<String, String> mStudentsMap;
    private String[] mStudentIdArray;
    private ArrayAdapter<String> adapter;
    private String mToNames;
    private int mImageFinalCount;
    private int mCurrentImg = 0;
    private MenuItem mDownloadMenuItem;
    private int mDownloadImagesLength;
    private File mAppDir;
    private String mAlbumId;
    private String[] receivedMsgs;
    private String[] pendingMsgs;
    private int mMessageArrayLength;
    private int mCurrentMsg;
    private int mMessageFinalCount;
    private ArrayList<String> mAlbumIds = new ArrayList<String>();
    private int mAlbumIdx = 0;
    private ArrayList<String> mSelectedStudentsArray;
    private HashMap<String, ContentValues> mAlbumMessageMap = new HashMap<String, ContentValues>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setLogo(R.drawable.home_logo);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int viewPagerItem = mViewPager.getCurrentItem();
                if (viewPagerItem == 2) {
                    selectImage();
                    return;
                }

                if (viewPagerItem == 0) {
                    buildStudentsListDialog();
                    return;
                }

                Intent composeIntent = new Intent(getBaseContext(), ComposeActivity.class);
                int composeType = mViewPager.getCurrentItem();
                if (viewPagerItem > 1) {
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
                /*navigationView.getMenu().findItem(R.id.nav_student_profile).setVisible(false);
                navigationView.getMenu().findItem(R.id.nav_saved_messages).setTitle("Outbox Messages");*/
            }
            SchoolDataUtility schoolData = new SchoolDataUtility(mLoginName, mIsTeacher);

            String[] studentDetails = schoolData.getStudentName(this);
            byte[] profilePic = schoolData.getMemberProfilePic(this);

            mNavLoginName.setText(studentDetails[0]);
            mNavClassName.setText(getString(R.string.nav_drawer_class) + " " + studentDetails[1]);
            mNavProfileImage.setImageBitmap(BitmapFactory.decodeByteArray(profilePic, 0, profilePic.length));
        }


        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        //mViewPager.setCurrentItem(1, true);

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
                        /*fab.setVisibility(View.VISIBLE);*/
                        fab.setVisibility(View.GONE);
                    } else {
                        fab.setVisibility(View.GONE);
                    }
                }
                if (position == 2) {
                    fab.setImageResource(android.R.drawable.ic_menu_camera);
                    /*if (mIsTeacher) {
                        if (mUploadMenuItem != null) {
                            mUploadMenuItem.setVisible(true);
                        }
                    } else {
                        if (mDownloadMenuItem != null) {
                            mDownloadMenuItem.setVisible(true);
                        }
                    }*/
                } else {
                    fab.setImageResource(android.R.drawable.ic_dialog_email);
                    /*if (mUploadMenuItem != null) {
                        mUploadMenuItem.setVisible(false);
                    }
                    if (mDownloadMenuItem != null) {
                        mDownloadMenuItem.setVisible(false);
                    }*/
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        if (Locale.getDefault().getLanguage().equalsIgnoreCase("ar")) {
            tabLayout.setTabMode(TabLayout.MODE_FIXED);
        }
        tabLayout.setupWithViewPager(mViewPager);

        int msgType = getIntent().getIntExtra("message_type", 2);
        if (msgType == 4) {
            msgType--;
        }
        mViewPager.setCurrentItem(msgType - 1);

        mToView = new MultiAutoCompleteTextView(this);
        mToView.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

        mStudentsMap = new SchoolDataUtility().getClassStudents(this);
        if (mStudentsMap != null) {
            mStudentIdArray = new String[mStudentsMap.size()];
            mSelectedStudentsArray = new ArrayList<String>(mStudentsMap.size());
            int idx = 0;

            for (Map.Entry<String, String> mapEntry : mStudentsMap.entrySet()) {
                mStudentIdArray[idx] = mapEntry.getKey();
                idx++;
            }

            adapter =
                    new ArrayAdapter<String>(HomeMainActivity.this,
                            android.R.layout.simple_spinner_dropdown_item, mStudentIdArray);

            adapter.setDropDownViewResource(android.R.layout.simple_expandable_list_item_1);
        }

        mToView.setThreshold(1);
        mToView.setAdapter(adapter);

        progress = new ProgressDialog(this);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.setTitle(R.string.album_upload_progress);
        progress.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                updateFragments();
            }
        });
        progress.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                updateFragments();
            }
        });

        networkConn = new NetworkConnectionUtility();

        NetworkResp networkResp = new NetworkResp();
        networkConn.setNetworkListener(networkResp);

        // Find the SD Card path
        File filepath = Environment.getExternalStorageDirectory();

        // Create a new folder in SD Card
        mAppDir = new File(filepath.getAbsolutePath() + "/" + getResources().getString(R.string.app_name));
        if (!mAppDir.exists()) {
            mAppDir.mkdirs();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        downloadImagesFromServer();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        String newLang = newConfig.locale.getDisplayLanguage();
        Toast.makeText(getApplicationContext(), "Selected Language is : " + newLang, Toast.LENGTH_SHORT).show();
        Log.i("Language", "Current Language is " + newLang);
    }

    private String getFragmentTag(int pos) {
        return "android:switcher:" + R.id.container + ":" + pos;
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

        dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        Calendar calendar = Calendar.getInstance();
        try {
            Date date = dateFormatter.parse(timeMilliSeconds);
            Long mLongTime = date.getTime();
            calendar.setTimeInMillis(mLongTime);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return "";
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
        dateFormatter.setTimeZone(TimeZone.getDefault());

        return dateFormatter.format(calendar.getTime());
    }

    public static String getDateString(Long timeMilliSeconds) {
        //TODO - To be changed once the server side is changed
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MMM-yyyy hh:mm a");
        //SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

        dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTimeInMillis(timeMilliSeconds);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return "";
        }

        return dateFormatter.format(calendar.getTime());
    }

    public static String getDateInString(Long timeMilliSeconds) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MMM-yyyy");

        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTimeInMillis(timeMilliSeconds);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return "";
        }

        return dateFormatter.format(calendar.getTime());
    }

    public static String getTimeInString(Long timeMilliSeconds) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("hh:mm a");

        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTimeInMillis(timeMilliSeconds);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return "";
        }

        return dateFormatter.format(calendar.getTime());
    }

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * Checks if the app has permission to write to device storage
     * <p/>
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.home_main, menu);
        mUploadMenuItem = menu.findItem(R.id.action_upload);
        mDownloadMenuItem = menu.findItem(R.id.action_download);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_upload) {
            //buildAlbumNameDialog();
            createAlbumInServer(randomString(6));
            return true;
        } else if (id == R.id.action_download) {
            //downloadImagesFromServer();
            return true;
        } else if (id == R.id.menu_refresh) {
            progress.setTitle(R.string.messages_refreshing);
            progress.show();

            networkConn.getUpdateMessages(mLoginName);
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
            /*Snackbar.make(, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();*/
        } else if (id == R.id.nav_manage) {
            Toast.makeText(this, "App Settings will be launched", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_sign_out) {
            SharedPreferences.Editor editor = sharePrefs.edit();
            editor.putBoolean(SHARED_LOGIN_KEY, false);
            editor.apply();

            getContentResolver().delete(Uri.parse("content://com.education.schoolapp/identity"), null, null);

            finish();
        } else if (id == R.id.nav_calendar) {
            Toast.makeText(this, "Academic Calendar will be shown here", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_address) {
            Toast.makeText(this, "School Address will be shown here", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_contacts) {
            Toast.makeText(this, "Important Contacts will be shown here", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_facebook) {
            Toast.makeText(this, "School's Facebook page will be launched", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_website) {
            Toast.makeText(this, "School's Website will be launched", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_messages) {
            mViewPager.setCurrentItem(0, true);
        } else if (id == R.id.nav_notifications) {
            mViewPager.setCurrentItem(1, true);
        } else if (id == R.id.nav_albums) {
            mViewPager.setCurrentItem(2, true);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void buildStudentsListDialog() {
        final AlertDialog.Builder stuBuilder = new AlertDialog.Builder(this);
        final AlertDialog stuAlertDialog = stuBuilder.create();

        mSelectedStudentsArray.clear();
        stuBuilder.setTitle(R.string.students_title_dialog_title);
        stuBuilder.setMultiChoiceItems(mStudentIdArray, null, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                if (mSelectedStudentsArray != null) {
                    if (isChecked) {
                        mSelectedStudentsArray.add(mStudentIdArray[which]);
                    } else {
                        int index = mSelectedStudentsArray.indexOf(mStudentIdArray[which]);
                        mSelectedStudentsArray.remove(index);
                    }
                }
            }
        });
        stuBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Toast.makeText(getApplicationContext(), "Students are selected", Toast.LENGTH_SHORT).show();
                Intent messageViewIntent = new Intent();
                messageViewIntent.putExtra("msg_title", TextUtils.join(",", mSelectedStudentsArray));
                String dbGroupId = new SchoolDataUtility(mLoginName, mIsTeacher).getGroupIdForMembers(getApplicationContext(), mSelectedStudentsArray);
                messageViewIntent.putExtra("msg_members", dbGroupId);
                if (dbGroupId != null && dbGroupId.isEmpty()) {
                    messageViewIntent.putExtra("new_group", true);
                }

                messageViewIntent.setClass(getApplicationContext(), MessageChatViewActivity.class);

                startActivity(messageViewIntent);
            }
        });

        stuBuilder.show();
    }

    private void buildAlbumNameDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog alertDialog = builder.create();

        builder.setTitle(R.string.album_title_dialog_title);

        final EditText mAlbumEdit = new EditText(this);
        mAlbumEdit.setInputType(InputType.TYPE_CLASS_TEXT);

        builder.setView(mAlbumEdit);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mAlbumName = mAlbumEdit.getText().toString();
                if (mAlbumName != null && !mAlbumName.isEmpty()) {
                    dialog.cancel();
                    alertDialog.dismiss();
                    createAlbumInServer(mAlbumName);
                }
            }
        });

        builder.show();
    }

    static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    static Random rnd = new Random();

    private String randomString(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        return sb.toString();
    }

    //TODO - To send only the selected images not all the images in pending state.
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

    private void uploadImagesToServer() {
        JSONArray imagesArray = new SchoolDataUtility().getPendingImages(this);
        int imagesCount = imagesArray.length();
        mImageFinalCount = imagesCount;
        for (int imgIdx = 0; imgIdx < imagesCount; imgIdx++) {
            try {
                networkConn.createMultimedia(imagesArray.getJSONObject(imgIdx).toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void downloadImagesFromServer() {
        JSONArray imagesIdArray = new SchoolDataUtility().getPendingImagesToDownload(this);
        int imagesLength = imagesIdArray.length();
        mDownloadImagesLength = imagesLength;
        mCurrentImg = 0;

        for (int i = 0; i < imagesLength; i++) {
            try {
                String imageId = imagesIdArray.getString(i);
                Log.i("Network", "Download Image ID is " + imageId);
                progress.setTitle(R.string.multimedia_download_progress);
                progress.show();
                networkConn.getMultimedia(imageId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateFragments() {
        for (int i = 0; i < mViewPager.getAdapter().getCount(); i++) {
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(getFragmentTag(i));
            if (i == 2) {
                //((AlbumFragment) fragment).updateAlbum(getApplicationContext());
                ((AlbumFragment) fragment).updateFolder(getApplicationContext());
            } else {
                ((MessagesFragment) fragment).updateMessages();
            }

            mViewPager.getAdapter().notifyDataSetChanged();
            mViewPager.invalidate();
        }
    }

    private class NetworkResp implements NetworkConnectionUtility.NetworkResponseListener {
        @Override
        public void onResponse(String urlString, String networkResult) {
            if (urlString.equalsIgnoreCase(NetworkConstants.CREATE_ALBUM)) {
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
                    }

                    Intent composeIntent = new Intent(getApplicationContext(), ComposeActivity.class);
                    composeIntent.putExtra("Type", 4);
                    composeIntent.putExtra("albumId", mAlbumId);

                    startActivity(composeIntent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                progress.dismiss();

            } else if (urlString.equalsIgnoreCase(NetworkConstants.CREATE_MULTIMEDIA)) {
                mCurrentImg++;
                if (networkResult == null) {
                    return;
                }
                progress.setTitle(getString(R.string.album_upload_progress) + mCurrentImg + "/" + mImageFinalCount);

                if (mCurrentImg == mImageFinalCount) {
                    ContentValues multimediaValues = new ContentValues();
                    multimediaValues.put("status", 1);
                    getContentResolver().update(Uri.parse(SchoolDataConstants.CONTENT_URI + SchoolDataConstants.ALBUM_IMAGES),
                            multimediaValues, null, null);

                    progress.dismiss();

                    Intent composeIntent = new Intent(getApplicationContext(), ComposeActivity.class);
                    composeIntent.putExtra("Type", 4);
                    composeIntent.putExtra("albumId", mAlbumId);

                    startActivity(composeIntent);
                }
            } else if (urlString.startsWith(NetworkConstants.GET_MULTIMEDIA)) {
                mCurrentImg++;
                progress.setTitle(getString(R.string.multimedia_download_progress) + mCurrentImg + "/" + mDownloadImagesLength);

                if (networkResult == null) {
                    if (mCurrentImg == mDownloadImagesLength) {
                        progress.dismiss();
                        //updateFragments();
                    }
                    return;
                }
                try {
                    JSONObject imageJsonObj = new JSONObject(networkResult);
                    String imageName = imageJsonObj.getString("name");
                    String imageString = imageJsonObj.getString("content");
                    String imagePath = saveImageToGallery(imageString, imageName);
                    String imageId = imageJsonObj.getJSONObject("_id").getString("$oid");

                    ContentValues imgValues = new ContentValues();
                    String selection = " image_id like '" + imageId + "'";

                    imgValues.put("image_local_path", imagePath);
                    imgValues.put("image_name", imageName);
                    imgValues.put("image_date", imageJsonObj.getString("date"));
                    imgValues.put("status", 1);

                    getContentResolver().update(Uri.parse(SchoolDataConstants.CONTENT_URI + SchoolDataConstants.ALBUM_IMAGES),
                            imgValues, selection, null);

                    getContentResolver().update(Uri.parse(SchoolDataConstants.CONTENT_URI + SchoolDataConstants.RECEIVED_MESSAGES_ALL),
                            imgValues, selection, null);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (mCurrentImg == mDownloadImagesLength) {
                    progress.dismiss();
                    //updateFragments();
                }
            } else if (urlString.startsWith(NetworkConstants.GET_UPDATE_MESSAGES)) {
                if (networkResult == null) {
                    progress.dismiss();
                    return;
                }

                try {
                    JSONObject userJsonObj = new JSONObject(networkResult);
                    JSONArray messageArray = userJsonObj.getJSONArray("messages");
                    int msgArrayLength = messageArray.length();
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
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                pendingMsgs = new SchoolDataUtility().getPendingMessages(getApplicationContext());
                if (pendingMsgs != null) {
                    mMessageFinalCount = mMessageArrayLength = pendingMsgs.length;
                    if (mMessageArrayLength > 0) {
                        for (int msg = 0; msg < mMessageArrayLength; msg++) {
                            networkConn.getMessage(pendingMsgs[msg]);
                        }
                    } else {
                        progress.dismiss();
                    }
                } else {
                    progress.dismiss();
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

                    String memberIdString = Joiner.on(",").skipNulls().join(memberIds);
                    msgValues.put("member_ids", memberIdString);
                    msgValues.put("member_names", Joiner.on(",").skipNulls().join(memberNames));
                    msgValues.put("members_count", membersArray.length());

                    msgValues.put("sender_profile_image", Base64.decode(messageObj.getString("sender_profile_image"), 0));
                    msgValues.put("sender_name", messageObj.getString("sender_name"));

                    if (messageObj.getString("album_ids") != null && !messageObj.getString("album_ids").equalsIgnoreCase("null")) {
                        JSONArray albumArray = messageObj.getJSONArray("album_ids");
                        if (albumArray != null) {
                            String[] albumIds = new String[albumArray.length()];
                            for (int i = 0; i < albumIds.length; i++) {
                                albumIds[i] = albumArray.getString(i);
                                mAlbumIds.add(mAlbumIdx, albumArray.getString(i));
                                mAlbumIdx++;
                                mAlbumMessageMap.put(albumIds[i], msgValues);
                            }
                            msgValues.put("album_id", Joiner.on(",").skipNulls().join(albumIds));
                        }
                    }

                    if (!msgValues.containsKey("album_id")) {
                        getContentResolver().insert(Uri.parse("content://com.education.schoolapp/received_messages_all"), msgValues);
                    } else {
                        Log.i("Sainath", "Album id is present");
                    }
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
                            for (int msg = 0; msg < mMessageArrayLength; msg++) {
                                networkConn.getAlbum(mAlbumIds.get(msg));
                            }
                        } else {
                            progress.dismiss();
                        }
                    } else {
                        progress.dismiss();
                    }
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
                                String albumId = albumObj.getJSONObject("_id").getString("$oid");
                                albumValues[albIdx].put("album_id", albumId);
                                albumValues[albIdx].put("image_id", albumArray.getJSONObject(albIdx).getJSONObject("_id").getString("$oid"));

                                ContentValues msgValues = mAlbumMessageMap.get(albumId);

                                albumValues[albIdx].putAll(msgValues);
                            }
                            //TODO - To be checked again
                            //getContentResolver().bulkInsert(Uri.parse(SchoolDataConstants.CONTENT_URI + SchoolDataConstants.ALBUM_IMAGES), albumValues);

                            getContentResolver().bulkInsert(Uri.parse(SchoolDataConstants.CONTENT_URI + SchoolDataConstants.RECEIVED_MESSAGES_ALL), albumValues);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (mMessageArrayLength == 0) {
                    progress.dismiss();
                    downloadImagesFromServer();
                }
            }
        }
    }

    private String saveImageToGallery(String imgString, String imgName) {
        if (imgString == null) {
            return "";
        }
        verifyStoragePermissions(this);
        String imageName = imgName;
        File imageFile = new File(mAppDir, imageName);
        FileOutputStream output = null;
        try {
            byte[] imageBytes = Base64.decode(imgString, 0);

            Bitmap imgBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            if (imgBitmap == null) {
                return "";
            }

            output = new FileOutputStream(imageFile);
            // Compress into png format image from 0% - 100%
            imgBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
            output.flush();
            output.close();

            //TODO - Check if the image is already saved in gallery & don't save again if exists.
            String url = MediaStore.Images.Media.insertImage(getContentResolver(), imgBitmap,
                    imageName, imageName);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.i("Album", "Image Path - " + imageFile.getAbsolutePath());

        return imageFile.getAbsolutePath();

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
                return AlbumFragment.newInstance("Album", mLoginName, mIsTeacher);
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
                    return getString(R.string.message_fragment_title);
                case 1:
                    return getString(R.string.notifications_fragment_title);
                case 2:
                    return getString(R.string.album_fragment_title);
            }
            return null;
        }
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

        createAlbumInServer(randomString(6));
    }

    private void onSelectFromGalleryResult(Intent data) {
        createAlbumInServer(randomString(6));
    }
}
