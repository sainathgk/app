package com.education.schoolapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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

import com.education.connection.schoolapp.NetworkConnectionUtility;
import com.education.connection.schoolapp.NetworkConstants;
import com.education.database.schoolapp.SchoolDataConstants;
import com.education.database.schoolapp.SchoolDataUtility;
import com.education.service.schoolapp.SchoolDataContentObserver;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

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
    private EditText mAlbumEdit;
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
            }
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
                    if (mIsTeacher) {
                        mUploadMenuItem.setVisible(true);
                    } else {
                        mDownloadMenuItem.setVisible(true);
                    }
                } else {
                    fab.setImageResource(android.R.drawable.ic_dialog_email);
                    mUploadMenuItem.setVisible(false);
                    mDownloadMenuItem.setVisible(false);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        mAlbumEdit = new EditText(this);
        mAlbumEdit.setInputType(InputType.TYPE_CLASS_TEXT);

        mToView = new MultiAutoCompleteTextView(this);
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
                    new ArrayAdapter<String>(HomeMainActivity.this,
                            android.R.layout.simple_spinner_dropdown_item, mStudentIdArray);

            adapter.setDropDownViewResource(android.R.layout.simple_expandable_list_item_1);
        }

        mToView.setThreshold(1);
        mToView.setAdapter(adapter);

        progress = new ProgressDialog(this);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.setTitle("Uploading ...");

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

    public static String getDateString(Long timeMilliSeconds) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MMM-yyyy hh:mm a");

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
            buildAlbumNameDialog();
            return true;
        } else if (id == R.id.action_download) {
            downloadImagesFromServer();
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

    private void buildAlbumNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Enter Album Name");
        builder.setView(mAlbumEdit);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mAlbumName = mAlbumEdit.getText().toString();
                if (mAlbumName != null && !mAlbumName.isEmpty()) {
                    dialog.dismiss();
                    createAlbumInServer(mAlbumName);
                    //buildMembersDialog();
                }
            }
        });

        builder.show();
    }

    private void buildMembersDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Select Members to Upload");
        builder.setView(mToView);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mToNames = mToView.getText().toString();
                if (mToNames != null && !mToNames.isEmpty()) {
                    dialog.dismiss();
                    createAlbumInServer(mAlbumName);
                }
            }
        });

        builder.show();
    }

    private void createAlbumInServer(String albumName) {
        JSONObject albumJsonObj = new JSONObject();
        JSONObject albumObj = new JSONObject();

        progress.show();
        try {
            albumJsonObj.put("name", albumName);
            albumJsonObj.put("date", HomeMainActivity.getDateString(System.currentTimeMillis()));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.school_logo);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream);

            albumJsonObj.put("image_data_arr", new JSONArray().put(Base64.encodeToString(outputStream.toByteArray(), 0)));

            //TODO - Multimedia should be shared to selected members
            /*String[] toStringArray = (String[]) Arrays.asList(mToNames.split(",")).toArray();
            JSONArray toSenderIds = new JSONArray();
            for (int i = 0; i < toStringArray.length - 1; i++) {
                toSenderIds.put(mStudentsMap.get(toStringArray[i]));
            }*/
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
                networkConn.getMultimedia(imageId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class NetworkResp implements NetworkConnectionUtility.NetworkResponseListener {
        @Override
        public void onResponse(String urlString, String networkResult) {
            if (urlString.equalsIgnoreCase(NetworkConstants.CREATE_ALBUM)) {
                if (networkResult == null) {
                    return;
                }
                try {
                    JSONObject albumResp = new JSONObject(networkResult);
                    ContentValues albumValues = new ContentValues();
                    String selection = " status == 0";

                    albumValues.put("album_name", albumResp.getString("name"));
                    mAlbumId = albumResp.getJSONObject("_id").getString("$oid");
                    albumValues.put("album_id", mAlbumId);

                    getContentResolver().update(Uri.parse(SchoolDataConstants.CONTENT_URI + SchoolDataConstants.ALBUM_IMAGES),
                            albumValues, selection, null);
                    uploadImagesToServer();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (urlString.equalsIgnoreCase(NetworkConstants.CREATE_MULTIMEDIA)) {
                mCurrentImg++;
                if (networkResult == null) {
                    return;
                }
                progress.setTitle("Uploading ... " + mCurrentImg + "/" + mImageFinalCount);

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

                /*try {
                    JSONObject multimediaResp = new JSONObject(networkResult);
                    ContentValues multimediaValues = new ContentValues();

                    String selection = "";
                    multimediaValues.put("image_id", multimediaResp.getString("oid"));
                    multimediaValues.put("status", 1);

                    getContentResolver().update(Uri.parse(SchoolDataConstants.CONTENT_URI + SchoolDataConstants.ALBUM_IMAGES),
                            multimediaValues, selection, null);

                } catch (JSONException e) {
                    e.printStackTrace();
                }*/
            } else if (urlString.startsWith(NetworkConstants.GET_MULTIMEDIA)) {
                mCurrentImg++;
                if (networkResult == null) {
                    return;
                }
                progress.setTitle("Downloading ... " + mCurrentImg + "/" + mDownloadImagesLength);
                progress.show();
                try {
                    JSONObject imageJsonObj = new JSONObject(networkResult);
                    String imageString = imageJsonObj.getString("content");
                    String imagePath = saveImageToGallery(imageString);
                    String imageId = imageJsonObj.getJSONObject("_id").getString("$oid");

                    ContentValues imgValues = new ContentValues();
                    String selection = " image_id like '" + imageId + "'";

                    imgValues.put("image_local_path", imagePath);
                    imgValues.put("image_date", imageJsonObj.getString("date"));
                    imgValues.put("status", 1);

                    getContentResolver().update(Uri.parse(SchoolDataConstants.CONTENT_URI + SchoolDataConstants.ALBUM_IMAGES),
                            imgValues, selection, null);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (mCurrentImg == mDownloadImagesLength) {
                    progress.dismiss();
                }
            }
        }
    }

    private String saveImageToGallery(String imgString) {
        verifyStoragePermissions(this);
        String imageName = System.currentTimeMillis() + ".jpg";
        File imageFile = new File(mAppDir, imageName);
        byte[] imageBytes = Base64.decode(imgString, 0);

        Bitmap imgBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        if (imgBitmap == null) {
            return "";
        }
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(imageFile);
            // Compress into png format image from 0% - 100%
            imgBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
            output.flush();
            output.close();

            String url = MediaStore.Images.Media.insertImage(getContentResolver(), imgBitmap,
                    imageName, imageName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.i("Album", "Image Path - "+imageFile.getAbsolutePath());

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
                            Action.ACTION_MULTIPLE_PICK);
                    startActivityForResult(intent,
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
        String single_path = data.getStringExtra("single_path");
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
        ContentValues imgValues = new ContentValues();
        imgValues.put("image_local_path", single_path);
        imgValues.put("image_date", HomeMainActivity.getDateString(System.currentTimeMillis()));
        imgValues.put("type", "sent");

        getContentResolver().insert(Uri.parse(SchoolDataConstants.CONTENT_URI + SchoolDataConstants.ALBUM_IMAGES), imgValues);
    }

    private void onSelectFromGalleryResult(Intent data) {

    }/*{
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
    }*/
}
