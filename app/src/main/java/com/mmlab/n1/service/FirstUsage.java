package com.mmlab.n1.service;

import android.app.Activity;
import android.net.wifi.WifiConfiguration;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;


import com.mmlab.n1.constant.HTTPREQUEST;

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

public class FirstUsage extends AsyncTask<String, Void, Void> {
    private static final String TAG = "FirstUsage";

    private WeakReference<Activity> weakReference = null;
    private HttpURLConnection httpURLConnection = null;
    private BufferedReader bufferedReader = null;
    private OutputStreamWriter outputStreamWriter = null;
    private WifiConfiguration wifiConfiguration;

    public FirstUsage(Activity activity, WifiConfiguration wifiConfiguration) {
        weakReference = new WeakReference<Activity>(activity);
        this.wifiConfiguration = wifiConfiguration;
    }

    protected Void doInBackground(String... params) {

        String url = HTTPREQUEST.HTTP_REQUEST;
        try {

            if (TextUtils.isEmpty(params[0]) || params.length < 4)
                return null;

            String SSID = "", SSIDpwd = "", encrypt = "";
            SSID = wifiConfiguration.SSID;
            Log.d(TAG, "SSSSSSSSSID : " + wifiConfiguration.SSID);
            if (wifiConfiguration.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.NONE)) {
                encrypt = "";
                SSIDpwd = "";
            } else if (wifiConfiguration.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK)) {
                encrypt = "WPA";
                SSIDpwd = wifiConfiguration.preSharedKey;
            } else if (wifiConfiguration.allowedKeyManagement.get(4)) {
                encrypt = "WPA";
                SSIDpwd = wifiConfiguration.preSharedKey;
            } else if (wifiConfiguration.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.IEEE8021X)) {
                encrypt = "WEP";
                SSIDpwd = wifiConfiguration.wepKeys[wifiConfiguration.wepTxKeyIndex];
            }

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", "firstUsage");
            jsonObject.put("content", new JSONObject()
                    .put("FBID", params[0])
                    .put("SSID", SSID)
                    .put("SSIDpwd", SSIDpwd)
                    .put("name", params[1])
                    .put("share", params[3])
                    .put("encrypt", encrypt)
                    .put("list", new JSONArray(params[2])));
            Log.d(TAG, "request : " + url);
            Log.d(TAG, "data : " + jsonObject.toString());

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

                Log.d(TAG, "receive : " + stringBuffer.toString());
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