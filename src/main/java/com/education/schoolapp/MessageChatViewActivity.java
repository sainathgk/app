package com.education.schoolapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public class MessageChatViewActivity extends AppCompatActivity {

    private RecyclerView mChatList;
    private MessageChatViewAdapter mChatListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_chat_view);

        mChatList = (RecyclerView) findViewById(R.id.chat_messages_list);
        mChatList.setHasFixedSize(false);

        mChatList.setLayoutManager(new LinearLayoutManager(this));
        mChatList.setItemAnimator(new DefaultItemAnimator());

        mChatListAdapter = new MessageChatViewAdapter();
        mChatListAdapter.updateData(this);
        mChatList.setAdapter(mChatListAdapter);

    }
}
