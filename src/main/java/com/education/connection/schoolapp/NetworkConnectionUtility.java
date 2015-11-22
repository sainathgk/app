package com.education.connection.schoolapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.google.common.io.CharStreams;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Sainath on 23-10-2015.
 */
public class NetworkConnectionUtility {

    private NetworkResponseListener mCallbackListener;
    private ImageResponseListener mImageCbListener;
    private final static String TAG = NetworkConnectionUtility.class.toString();

    public void loginUser(String loginPayload) {
        if (NetworkConstants.isServerON) {
            Log.i(TAG, "Login User API");
            new AsyncNetwork().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "SEARCH", NetworkConstants.AUTHENTICATE, loginPayload);
        } else {
            //DB insert is already done.
        }

    }

    public void getStudents(String className, String sectionName) {
        if (NetworkConstants.isServerON) {
            new AsyncNetwork().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "GET", NetworkConstants.GET_CLASS_STUDENTS + className + "/" + sectionName + ".json");
        }
    }

    public void postMessage(String messagePayload) {
        if (NetworkConstants.isServerON) {
            Log.i(TAG, "Post Message API" + messagePayload);
            new AsyncNetwork().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "SEARCH", NetworkConstants.POST_MESSAGE, messagePayload);
        }

        /*new Thread(new Runnable() {
            @Override
            public void run() {

            }
        }).run();*/
    }

    public void getMessage(String messageId) {
        if (NetworkConstants.isServerON) {
            Log.i(TAG, "Get Message API for Message ID : " + messageId);
            new AsyncNetwork().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "GET", NetworkConstants.GET_MESSAGE + messageId + ".json");
        }
    }

    public void createAlbum(String albumDetails) {
        if (NetworkConstants.isServerON) {
            Log.i(TAG, "Create Album");
            new AsyncNetwork().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "SEARCH", NetworkConstants.CREATE_ALBUM, albumDetails);
        }
    }

    public void createMultimedia(String multimediaDetais) {
        if (NetworkConstants.isServerON) {
            Log.i(TAG, "Create Multimedia");
            new AsyncNetwork().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "SEARCH", NetworkConstants.CREATE_MULTIMEDIA, multimediaDetais);
        }
    }

    public void getAlbum(String albumId) {
        if (NetworkConstants.isServerON) {
            Log.i(TAG, "Get Album");
            new AsyncNetwork().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "GET", NetworkConstants.GET_ALBUM + albumId + ".json");
        }
    }

    public void getMultimedia(String multimediaId) {
        if (NetworkConstants.isServerON) {
            Log.i(TAG, "Get Multimedia");
            new AsyncNetwork().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "GET", NetworkConstants.GET_MULTIMEDIA + multimediaId + ".json");
        }
    }

    public interface NetworkResponseListener {
        void onResponse(String urlString, String networkResult);
    }

    public interface ImageResponseListener {
        void onImageResponse(String urlString, Bitmap imageResult);
    }

    public NetworkConnectionUtility() {
    }

    public void setNetworkListener(NetworkResponseListener listener) {
        mCallbackListener = listener;
    }

    public void setImageListener(ImageResponseListener listener) {
        mImageCbListener = listener;
    }

    private class AsyncNetwork extends AsyncTask<String, Void, String> {
        private String urlString = null;

        @Override
        protected String doInBackground(String... params) {
            String method = params[0];
            urlString = params[1];
            String result = null;
            URL url = null;

            try {
                url = new URL(urlString);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            if (method.equalsIgnoreCase("POST")) {
                String payload = params[2];
                result = doPostMethod(url, payload);
            } else if (method.equalsIgnoreCase("GET")) {
                result = doGetMethod(url);
            } else if (method.equalsIgnoreCase("SEARCH")) {
                String payload = params[2];
                result = doSearchMethod(url, payload);
            }

            return result;
        }

        @Override
        protected void onPostExecute(String resResult) {
            super.onPostExecute(resResult);
            Log.i(TAG, urlString);
            if (resResult != null) {
                Log.i(TAG, resResult);
                mCallbackListener.onResponse(urlString, resResult);
            } else {
                Log.i(TAG, "Some Network Issues");
                mCallbackListener.onResponse(urlString, null);
                //Toast.makeText(mContext, "Network issues, Please check your Internet", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class AsyncImageDownloadTask extends AsyncTask<String, Void, Bitmap> {
        private String urlString = null;

        @Override
        protected Bitmap doInBackground(String... params) {
            String method = params[0];
            urlString = params[1];
            Bitmap result = null;
            URL url = null;

            try {
                url = new URL(urlString);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            if (method.equalsIgnoreCase("GET")) {
                result = doGetImageBlob(url);
            }

            return result;
        }

        @Override
        protected void onPostExecute(Bitmap resResult) {
            super.onPostExecute(resResult);
            mImageCbListener.onImageResponse(urlString, resResult);
        }
    }

    private String doPostMethod(URL postUrl, String payLoad) {
        HttpURLConnection urlConnect = createHttpConnection(postUrl);
        String responseMsg = null;
        try {
            urlConnect.setRequestMethod("POST");
            urlConnect.setDoOutput(true);

            OutputStreamWriter postOutput = new OutputStreamWriter(urlConnect.getOutputStream());
            postOutput.write(payLoad);
            postOutput.flush();
            postOutput.close();

            urlConnect.connect();

            int responseCode = urlConnect.getResponseCode();
            responseMsg = urlConnect.getResponseMessage();

            Log.i(TAG, "Post Http Connection is Succeeded" + responseCode);
            Log.i(TAG, "Post Http Connection Response is " + responseMsg);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnect != null) {
                urlConnect.disconnect();
            }
        }

        return responseMsg;
    }

    private String doSearchMethod(URL postUrl, String payLoad) {
        HttpURLConnection urlConnect = createHttpConnection(postUrl);
        String responseMsg = null;
        String searchResult = null;
        try {
            urlConnect.setRequestMethod("POST");
            urlConnect.setDoOutput(true);

            OutputStreamWriter postOutput = new OutputStreamWriter(urlConnect.getOutputStream());
            postOutput.write(payLoad);
            postOutput.flush();
            postOutput.close();

            urlConnect.connect();

            InputStreamReader getOutput = new InputStreamReader(urlConnect.getInputStream());
            searchResult = CharStreams.toString(getOutput);

            int responseCode = urlConnect.getResponseCode();
            responseMsg = urlConnect.getResponseMessage();

            Log.i(TAG, "Search Http Connection is Succeeded" + responseCode);
            Log.i(TAG, "Search Http Connection Response is " + responseMsg);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnect != null) {
                urlConnect.disconnect();
            }
        }

        return searchResult;
    }

    private String doGetMethod(URL getUrl) {
        HttpURLConnection urlConnect = createHttpConnection(getUrl);

        String getResult = null;
        try {
            urlConnect.setRequestMethod("GET");
            urlConnect.connect();

            int responseCode = urlConnect.getResponseCode();

            Log.i(TAG, "Get Http Connection is Succeeded" + responseCode);
            Log.i(TAG, "Get Http Connection Response is " + urlConnect.getResponseMessage());

            InputStreamReader getOutput = new InputStreamReader(urlConnect.getInputStream());

            getResult = CharStreams.toString(getOutput);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnect != null) {
                urlConnect.disconnect();
            }
        }

        return getResult;//new String(output);
    }

    private Bitmap doGetImageBlob(URL imageUrl) {
        HttpURLConnection urlConnect = createHttpConnection(imageUrl);
        Bitmap myBitmap = null;

        try {
            urlConnect.setRequestMethod("GET");
            urlConnect.connect();

            InputStream input = urlConnect.getInputStream();
            myBitmap = BitmapFactory.decodeStream(input);

            Log.i("Sainath", "Image Download is ended");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnect != null) {
                urlConnect.disconnect();
            }
        }

        return myBitmap;
    }

    private HttpURLConnection createHttpConnection(URL url) {
        HttpURLConnection urlConnection = null;

        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.setDoInput(true);
            urlConnection.setRequestProperty("Connection", "close");
            urlConnection.setRequestProperty("Content-Type", "application/json; text/plain; charset=utf-8");

        } catch (IOException e) {
            e.printStackTrace();
        }

        return urlConnection;
    }

}
