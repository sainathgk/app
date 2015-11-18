package com.education.schoolapp;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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
    private long mDate;
    private Bitmap mSenderImage;
    private String mToText;
    private String mFromText;
    private String msgBox;
    private String msgId;
    private int msgType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Message View");
        /*getActionBar().setHomeButtonEnabled(true);
        getActionBar().setIcon(R.drawable.school_logo);*/
        setContentView(R.layout.activity_message_view);

        msgId = getIntent().getStringExtra("msg_id");
        msgBox = getIntent().getStringExtra("msg_box");
        msgType = getIntent().getIntExtra("msg_type", 1);

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
            }
            if (!msgJsonObj.optString("body").isEmpty()) {
                mDescription = msgJsonObj.getString("body");
            }
            if (!msgJsonObj.optString("start_date").isEmpty()) {
                mDate = msgJsonObj.getLong("start_date");
            }
            byte[] imageBytes = Base64.decode(msgJsonObj.getString("sender_profile_image"), 0);
            mSenderImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

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
        mDateView.setText(HomeMainActivity.getDateString(String.valueOf(mDate)));

        ContentValues msgUpdate = new ContentValues();
        String selection = "message_id like '" + msgId + "'";

        msgUpdate.put("read_status", 0);

        getContentResolver().update(Uri.parse("content://com.education.schoolapp/received_messages_all"), msgUpdate, selection, null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.message_detail, menu);
        if (msgBox.equalsIgnoreCase("Saved") || msgBox.equalsIgnoreCase("Outbox")) {
            menu.findItem(R.id.action_save).setVisible(false);
        }
        if (msgType == 2) {
            menu.findItem(R.id.action_save).setVisible(false);
            menu.findItem(R.id.action_calendar).setVisible(true);
            menu.findItem(R.id.action_reply).setVisible(false);
            menu.findItem(R.id.action_reply_all).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent replyIntent = new Intent(getApplicationContext(), ComposeActivity.class);
        replyIntent.putExtra("msg_id", msgId);
        replyIntent.putExtra("msg_box", msgBox);
        replyIntent.putExtra("Type", msgType);

        switch (item.getItemId()) {
            case R.id.action_save:
                ContentValues msgUpdate = new ContentValues();
                String selection = "message_id like '" + msgId + "'";

                msgUpdate.put("saved", 1);

                getContentResolver().update(Uri.parse("content://com.education.schoolapp/received_messages_all"), msgUpdate, selection, null);
                Toast.makeText(this, "Message is saved", Toast.LENGTH_SHORT).show();
                break;

            case R.id.action_reply:
                replyIntent.putExtra("compose_type", "reply");
                startActivity(replyIntent);
                break;

            case R.id.action_reply_all:
                replyIntent.putExtra("compose_type", "replyAll");
                startActivity(replyIntent);
                break;

            case R.id.action_delete:
                //TODO : make a Network call to server for deleting the message.
                String delete_selection = "message_id like '" + msgId + "'";

                getContentResolver().delete(Uri.parse("content://com.education.schoolapp/received_messages_all"), delete_selection, null);
                break;

            case R.id.action_calendar:
                Intent intent = new Intent(Intent.ACTION_EDIT);
                intent.setType("vnd.android.cursor.item/event");
                intent.putExtra("beginTime", mDate);
                intent.putExtra("endTime", mDate + 60 * 60 * 1000);
                intent.putExtra("title", mTitle);
                intent.putExtra("description", mDescription);
                startActivity(intent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

}
