package com.education.connection.schoolapp;
import android.content.ContentValues;
import android.database.Cursor;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Created by Sainath on 23-08-2015.
 */
public class JSONUtility {

    private static final String TAG = "JSONParser";
    private String[] mColumns = null;

    public void setColumsList(final String[] columnList)
    {
        mColumns = columnList;
    }

    public JSONObject toJSON(final Cursor cursor) throws JSONException {
        final JSONObject jsonObj = new JSONObject();

        for (String column : mColumns) {
            final int columnIndex = cursor.getColumnIndex(column);
            if(columnIndex == -1)continue;
            switch (cursor.getType(columnIndex)) {
                case Cursor.FIELD_TYPE_BLOB:
                    final byte[] temp = cursor.getBlob(columnIndex);
                    final String imageBase64 = Base64.encodeToString(temp, 0);
                    jsonObj.put(column, imageBase64);
                    break;
                case Cursor.FIELD_TYPE_FLOAT:

                    break;
                case Cursor.FIELD_TYPE_INTEGER:
                    jsonObj.put(column, Long.toString(cursor.getLong(columnIndex)));
                    break;

                case Cursor.FIELD_TYPE_NULL:
                    break;

                case Cursor.FIELD_TYPE_STRING:
                    jsonObj.put(column, cursor.getString(columnIndex));
                    break;
                default:
                    break;
            }
        }
        return jsonObj;
    }

    public ContentValues fromJSONString(final String jsonString) throws JSONException
    {
        String value = null;
        final ContentValues contentValues = new ContentValues();

        final JSONObject jsonObject = (JSONObject) new JSONTokener(jsonString).nextValue();

        for (String column : mColumns) {
            try{
                if(jsonObject.has(column))
                {
                    value = jsonObject.getString(column);
                    contentValues.put(column, value);
                }

            }catch (JSONException e) {
                e.printStackTrace();
                continue;
            }
        }
        return contentValues;
    }
    public ContentValues fromJSON(final JSONObject jsonObject) throws JSONException
    {
        String value = null;
        final ContentValues contentValues = new ContentValues();

        for (String column : mColumns) {
            try{
                if(jsonObject.has(column))
                {
                    value = jsonObject.getString(column);
                    contentValues.put(column, value);
                }

            }catch (JSONException e) {
                e.printStackTrace();
                continue;
            }
        }
        return contentValues;
    }
}