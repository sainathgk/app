package com.education.schoolapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.education.database.schoolapp.SchoolDataUtility;

import org.json.JSONException;
import org.json.JSONObject;

public class MessageViewActivity extends AppCompatActivity {

    private TextView mTitleView;
    private TextView mMessageBoxView;
    private TextView mDescriptionView;
    private TextView mDateView;
    private TextView mSenderNameView;
    private ImageView mSenderImageView;
    private TextView mToView;
    private TextView mAttachmentView;

    private String mTitle;
    private String mDescription;
    private String mDate;
    private Bitmap mSenderImage;
    private String mToText;
    private String mFromText;
    private String msgBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Message View");
        /*getActionBar().setHomeButtonEnabled(true);
        getActionBar().setIcon(R.drawable.school_logo);*/
        setContentView(R.layout.activity_message_view);

        //String msgId = getIntent().getStringExtra("msg_id");
        String msgId = getIntent().getStringExtra("msg_id");
        msgBox = getIntent().getStringExtra("msg_box");
        int msgType = getIntent().getIntExtra("msg_type", 1);

        mTitleView = (TextView) findViewById(R.id.message_title);
        mMessageBoxView = (TextView) findViewById(R.id.message_box);
        mDescriptionView = (TextView) findViewById(R.id.message_description);
        mDateView = (TextView) findViewById(R.id.message_date);
        mSenderNameView = (TextView) findViewById(R.id.message_sender_name);
        mSenderImageView = (ImageView) findViewById(R.id.message_sender_image);
        mToView = (TextView) findViewById(R.id.message_to_names);
        mAttachmentView = (TextView) findViewById(R.id.message_attachment);

        String msgItem = new SchoolDataUtility().getMessage(this.getApplicationContext(), msgBox, msgType, msgId);
        if (msgItem == null) {
            Toast.makeText(getApplicationContext(), "Message is null", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        try {
            JSONObject msgJsonObj = new JSONObject(msgItem);
            if (!msgJsonObj.optString("subject").isEmpty()) {
                mTitle = msgJsonObj.getString("subject");
            } /*else if (!msgJsonObj.optString("noti_title").isEmpty()) {
                mTitle = msgJsonObj.getString("noti_title");
            }*/
            if (!msgJsonObj.optString("body").isEmpty()) {
                mDescription = msgJsonObj.getString("body");
            } /*else if (!msgJsonObj.optString("noti_description").isEmpty()) {
                mDescription = msgJsonObj.getString("noti_description");
            }*/
            if (!msgJsonObj.optString("start_date").isEmpty()) {
                mDate = msgJsonObj.getString("start_date");
            } /*else if (!msgJsonObj.optString("noti_date").isEmpty()) {
                mDate = msgJsonObj.getString("noti_date");
            }*/
            byte[] imageBytes = Base64.decode(msgJsonObj.getString("sender_profile_image"), 0);
            mSenderImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            /*JSONArray toArray = msgJsonObj.getJSONArray("member_ids");
            HashMap<String, String> membersMap = new SchoolDataUtility().getClassStudents(this);
            for (int i = 0; i < toArray.length(); i++) {
                mToText = mToText.concat(membersMap.get(toArray.getString(i)) + ",");
            }*/

            mToText = msgJsonObj.getString("member_names");
            mFromText = msgJsonObj.getString("sender_name");
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "JSON Exception", Toast.LENGTH_SHORT).show();
        }


        mTitleView.setText(mTitle);
        mMessageBoxView.setText(msgBox);
        mDescriptionView.setText(mDescription);
        mSenderImageView.setImageBitmap(HomeMainActivity.GetBitmapClippedCircle(mSenderImage));
        mSenderNameView.setText(mFromText);
        mAttachmentView.setVisibility(View.GONE);
        mToView.setText("to " + mToText);
        mDateView.setText(HomeMainActivity.getDateString(mDate));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.message_detail, menu);
        if (msgBox.equalsIgnoreCase("Saved")) {
            menu.findItem(R.id.action_save).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                break;

            case R.id.action_reply:
                break;

            case R.id.action_reply_all:
                break;

            case R.id.action_delete:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

}
