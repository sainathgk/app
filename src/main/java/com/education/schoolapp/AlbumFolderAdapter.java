package com.education.schoolapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.education.database.schoolapp.SchoolDataUtility;

import java.util.ArrayList;

/**
 * Created by Sainath on 18-12-2015.
 */
public class AlbumFolderAdapter extends BaseAdapter {

    private final LayoutInflater infalter;
    private Context mContext = null;
    private SharedPreferences sharePrefs;
    private String loginName;
    private SchoolDataUtility mSchoolDB;
    private ArrayList<FolderDetails> mFolderData;

    private static final String APP_SHARED_PREFS = "school_preferences";
    private static final String SHARED_LOGIN_NAME = "schoolUserLoginName";

    public AlbumFolderAdapter(Context context) {
        mContext = context;

        sharePrefs = mContext.getSharedPreferences(APP_SHARED_PREFS, Context.MODE_PRIVATE);

        loginName = sharePrefs.getString(SHARED_LOGIN_NAME, "");
        mSchoolDB = new SchoolDataUtility(loginName, false);

        infalter = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setData() {
        mFolderData = mSchoolDB.getAlbumsByDate(mContext);
    }

    @Override
    public int getCount() {
        if (mFolderData != null)
            return mFolderData.size();

        return 0;
    }

    @Override
    public FolderDetails getItem(int position) {
        return mFolderData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            convertView = infalter.inflate(R.layout.folder_item, null);

            holder = new ViewHolder();
            holder.folderName = (TextView) convertView.findViewById(R.id.folder_name);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.folderName.setText(mFolderData.get(position).albumDate);

        return convertView;
    }

    public class ViewHolder{
        TextView folderName;
    }
}
