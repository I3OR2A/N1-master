package com.mmlab.n1.service;

import android.app.Activity;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.mmlab.n1.constant.HTTPREQUEST;
import com.mmlab.n1.model.WifiRecord;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FindPassword extends AsyncTask<String, Void, Void> {
    private static final String TAG = "FindPassword";

    private WeakReference<Activity> weakReference = null;
    private HttpURLConnection httpURLConnection = null;
    private BufferedReader bufferedReader = null;
    private OutputStreamWriter outputStreamWriter = null;
    private List<String> SSID = new ArrayList<>();
    private HashMap<String, WifiRecord> original = new HashMap<String, WifiRecord>();

    public void setOnFinishedListener(OnFinishedListener onFinishedListener) {
        this.onFinishedListener = onFinishedListener;
    }

    private OnFinishedListener onFinishedListener = null;

    public interface OnFinishedListener {
        void onFinished(HashMap<String, WifiRecord> hashmap);
    }

    public FindPassword(Activity activity, HashMap<String, WifiRecord> original, List<String> SSID) {
        weakReference = new WeakReference<Activity>(activity);
        if (SSID != null) {
            this.SSID = SSID;
        }
        this.original = new HashMap<>(original);
    }

    protected Void doInBackground(String... params) {

        String url = HTTPREQUEST.HTTP_REQUEST;
        try {

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", "findPassword");
            jsonObject.put("content", new JSONObject()
                    .put("FBID", params[0])
                    .put("SSIDlist", new JSONArray(SSID)));
            // Log.d(TAG, "request : " + url);
            // Log.d(TAG, "data : " + jsonObject.toString());

            URL obj = new URL(url);
            httpURLConnection = (HttpURLConnection) obj.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setRequestProperty("User-Agent", HTTPREQUEST.USER_AGENT);
            httpURLConnection.connect();

            outputStreamWriter = new OutputStreamWriter(httpURLConnection.getOutputStream());
            outputStreamWriter.write("request=" + jsonObject.toString());
            outputStreamWriter.flush();

            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                String line;
                StringBuffer stringBuffer = new StringBuffer();
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuffer.append(line);
                }

                // Log.d(TAG, "receive : " + stringBuffer.toString());

                HashMap<String, WifiRecord> hashMap = new HashMap<>();
                JSONArray jsonArray = new JSONObject(stringBuffer.toString()).getJSONArray("content");
                for (int i = 0; i < jsonArray.length(); ++i) {
                    JSONObject object = jsonArray.getJSONObject(i);
                    WifiRecord record = new WifiRecord(object);
                    hashMap.put(record.SSID, record);
                }

                for (Map.Entry<String, WifiRecord> entry : hashMap.entrySet()) {
                    WifiRecord record = entry.getValue();
                    if (original.containsKey(record.SSID)) {
                        WifiRecord revise = original.get(record.SSID);
                        if (!TextUtils.isEmpty(record.SSIDpwd)) {
                            revise.SSIDpwd = record.SSIDpwd;
                            // Log.d(TAG, "update : " + record.SSIDpwd);
                        }
                    }
                }

                // Log.d(TAG, "Find size : " + hashMap.size());
                if (onFinishedListener != null) {
                    onFinishedListener.onFinished(original);
                }
            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }

            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (outputStreamWriter != null) {
                try {
                    outputStreamWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }
}