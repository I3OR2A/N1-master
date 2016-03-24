package com.mmlab.n1.network;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class CacheService extends Service {
    private Thread cacheDaemon;
    public CacheService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        cacheDaemon = new CacheDaemon(this, "893455034080174");
        cacheDaemon.start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        cacheDaemon.interrupt();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private class CacheDaemon extends Thread {
        private static final long UPDATE_DURATION = 5000;
        private static final float UPDATE_DISTANCE = 5.0f;
        private static final int CACHE_RADIUS = 70;
        private static final int CACHE_POI_NUM = 10;
        private static final int CACHE_LOI_NUM = 5;
        private static final String MSN_SERVER = "http://140.116.82.149:8080/MSNDB/msn.php";
        private static final String NEARBY_POI_URL = "http://deh.csie.ncku.edu.tw/dehencode/json/nearbyPOIs_AES";
        private static final String NEARBY_LOI_URL = "http://deh.csie.ncku.edu.tw/dehencode/json/nearbyLois_AES.php";
        private static final String LOI_SEQUENCE_URL = "http://deh.csie.ncku.edu.tw/dehencode/json/loisequence_AES.php";
        private LocationManager locationManager;
        private String fbId;
        private Location proxyLocation;

        public CacheDaemon(Context context, String fbId) {
            this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            this.fbId = fbId;
        }

        @Override
        public void run() {
            Looper.prepare();

            LocationListener locationListener = new LocationListener() {

                @Override
                public void onLocationChanged(Location location) {
                    proxyLocation = location;

                    try {
                        HttpURLConnection updateProxyLocation = (HttpURLConnection) (new URL(MSN_SERVER)).openConnection();
                        updateProxyLocation.setRequestMethod("POST");
                        updateProxyLocation.setDoOutput(true);
                        updateProxyLocation.setDoInput(true);
                        BufferedWriter writer = new BufferedWriter((new OutputStreamWriter(updateProxyLocation.getOutputStream())));
                        JSONObject jsonRequest = new JSONObject(), jsonContent = new JSONObject();
                        jsonContent.put("location", String.valueOf(proxyLocation.getLatitude()) + "|" + String.valueOf(proxyLocation.getLongitude()));
                        jsonContent.put("FBID", fbId);
                        jsonRequest.put("name", "updateLocation");
                        jsonRequest.put("content", jsonContent);
                        String para = "request=" + jsonRequest.toString();
                        writer.write(para);
                        writer.flush();
                        writer.close();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(updateProxyLocation.getInputStream()));
                        reader.read();
                        reader.close();
                        updateProxyLocation.disconnect();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (ProtocolException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };

            try {
                //noinspection ResourceType
                this.locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, UPDATE_DURATION, UPDATE_DISTANCE, locationListener);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Looper.loop();
        }
    }
}