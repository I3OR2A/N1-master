package com.mmlab.n1.network;


import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.mmlab.n1.helper.ExternalStorage;
import com.mmlab.n1.constant.PLAYBACK;
import com.mmlab.n1.model.POIModel;
import com.mmlab.n1.helper.Preset;
import com.mmlab.n1.save_data.SavePOI;
import com.mmlab.n1.save_data.Utils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import com.mmlab.n1.model.Package;

public class MemberService extends Service {
    private static final String TAG = "MemberService";

    public static final String CONNECT_ACTION = "MemberService.CONNECT_ACTION";
    public static final String FILE_COMPLETE__ACTION = "MemberService.FILE_COMPLETE_ACTION";
    public static final String VIDEO_START_ACTION = "MemberService.VIDEO_START_ACTION";
    public static final String PHOTO_START_ACTION = "MemberService.PHOTO_START_ACTION";

    /**
     * V2 Start
     */
    public static final String DISCONNECT_FROM_PROXY = "MemberService.DISCONNECT_FROM_PROXY";
    public static final String CONNECT_TO_PROXY = "MemberService.CONNECT_TO_PROXY";
    /**
     * V2 End
     **/
    private MemberBinder binder = new MemberBinder();
    public static ClientThread clientThread = null;

    private static ArrayList<POIModel> poiList = new ArrayList<>();
    private static POIModel curPOI = null;
    private static HandlerThread pHandlerThread = null;

    private VideoService videoService = null;
    private VideoService.VideoBinder videoBinder = null;
    private ServiceConnection videoConnection = new ServiceConnection() {

        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected()...");
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected()...");
            videoBinder = (VideoService.VideoBinder) service;
            videoService = videoBinder.getVideoInstance();
        }
    };

    public void onCreate() {
        super.onCreate();

        pHandlerThread = new HandlerThread("Client");
        pHandlerThread.start();
        pHandler = new ProcessHandler(MemberService.this, pHandlerThread.getLooper());

        startClient();
        // start service
        Intent startVideoIntent = new Intent(MemberService.this, VideoService.class);
        startService(startVideoIntent);
        // bind service
        Intent bindVideoIntent = new Intent(MemberService.this, VideoService.class);
        bindService(bindVideoIntent, videoConnection, BIND_AUTO_CREATE);
        Log.d(TAG, "onCreate()...");
    }

    public String getServerIpAddress() {
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        int serverAddress = wifiManager.getDhcpInfo().serverAddress;
        return (serverAddress & 0xFF) + "." + ((serverAddress >> 8) & 0xFF) + "."
                + ((serverAddress >> 16) & 0xFF) + "." + ((serverAddress >> 24) & 0xFF);
    }

    public void onDestroy() {
        super.onDestroy();
        stopClient();
        Log.d(TAG, "onDestroy()...");

        unbindService(videoConnection);
        // stop service
        Intent intent = new Intent(MemberService.this, VideoService.class);
        stopService(intent);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "onStartCommand()...");
        startClient();
        // return super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    public MemberService() {
    }

    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind()...");
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnBind()...");
        return super.onUnbind(intent);
    }

    public class MemberBinder extends Binder {
        public MemberService getMemberInstance() {
            return MemberService.this;
        }
    }

    public void sendRequestPlayback(String pRemoteUri) {
        long pMediaLength;
        if ((pMediaLength = Preset.loadFilePreferences(getApplicationContext(), Utils.urlToFilename(pRemoteUri))) == -1) {
            clientThread.sendRequestPlayback(pRemoteUri);
        } else {
            Intent intent = new Intent();
            intent.putExtra("mediaLength", pMediaLength);
            intent.putExtra("remoteUri", pRemoteUri);
            PLAYBACK.remoteUri = pRemoteUri;
            PLAYBACK.mediaLength = pMediaLength;
            PLAYBACK.isReady = false;
            PLAYBACK.isDownloaded = false;
            PLAYBACK.readSize = 0;
            PLAYBACK.errorCount = 0;
            PLAYBACK.currentPosition = 0;
            PLAYBACK.isError = false;
            intent.setAction(VIDEO_START_ACTION);
            sendBroadcast(intent);
        }
    }

    public boolean isConnected() {
        if (clientThread != null) {
            return clientThread.isConnected;
        }
        return false;
    }

    public void startClient() {
        String serverAddress = getServerIpAddress();
        if (clientThread == null) {
            clientThread = new ClientThread(serverAddress);
            clientThread.start();
        } else if (!clientThread.isAlive() || !clientThread.getServerAddress().equals(serverAddress)) {
            clientThread.interrupt();
            clientThread = new ClientThread(serverAddress);
            clientThread.start();
        }
    }

    public void stopClient() {
        clientThread.interrupt();
    }

    /**
     * Created by mmlab on 2015/5/27.
     */
    public class ClientThread extends Thread {

        private static final String TAG = "Client";

        private static final int PORT = 9001;

        private Socket socket = null;

        private ObjectInputStream objectInputStream;

        private ObjectOutputStream objectOutputStream;

        private String serverAddress = "";

        public boolean isConnected = false;

        public ClientThread(String serverAddress) {
            this.serverAddress = serverAddress;
        }

        public String getServerAddress() {
            return serverAddress;
        }

        /**
         * 關閉Socket，block在 ObjectInputStream method的地方會throw a IOException
         */
        public void interrupt() {
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
//
//                Intent intent = new Intent();
//                intent.setAction(CONNECT_ACTION);
//                intent.putExtra("status", "disconnected");
//                sendBroadcast(intent);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void sendRequestPlayback(String pRemoteUri) {
            // videoService.startReceiveThread(pRemoteUri, -1, 0);
            try {
                objectOutputStream.write(1);
                objectOutputStream.writeObject("fileName " + pRemoteUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * Send the location information to server
         */
        public void sendPackageToServer() {

        }

        /**
         * Connects to the server then enters the processing loop.
         */
        public void run() {
            try {
                Log.i(TAG, serverAddress);
                SocketAddress socketAddress = new InetSocketAddress(serverAddress, PORT);
                socket = new Socket();
                socket.connect(socketAddress, 1000);
                isConnected = true;
//                Intent intent = new Intent();
//                intent.setAction(CONNECT_ACTION);
//                intent.putExtra("status", "connected");
//                sendBroadcast(intent);

                objectInputStream = new ObjectInputStream(socket.getInputStream());
                objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                objectOutputStream.write(1);
                objectOutputStream.writeObject(new Package(Package.TAG_NONE, Package.TYPE_NONE, "", Utils.stringToByteArray("Hello this is client")));
                Log.i(TAG, "client is running");

                /** V2 Start **/
                Intent intent = new Intent();
                intent.setAction(MemberService.CONNECT_TO_PROXY);
                sendBroadcast(intent);
                /** V2 End **/

                // Process all messages from server, according to the protocol.
                while (!Thread.currentThread().isInterrupted()) {
                    Log.i(TAG, "I am here");
                    int input = objectInputStream.read();

                    // Disconnect with the server
                    if (input < 0) {
                        break;
                    }

                    Log.i(TAG, "Package number: " + input);
                    for (int i = 0; i < input; ++i) {
                        Package pack = (Package) objectInputStream.readObject();
                        switch (pack.tag) {
                            case Package.TAG_COMMAND:
                                pHandler.sendMessage(pHandler.obtainMessage(3, pack));
                                break;
                            case Package.TAG_DATA:
                                pHandler.sendMessage(pHandler.obtainMessage(2, pack));
                                break;
                            case Package.TAG_NONE:
                                break;
                            default:
                        }
                    }
                }
                Log.i(TAG, "create clientHandler");
            } catch (SocketTimeoutException e) {
                Log.d(TAG, "I catch you");
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                isConnected = false;
            }

            /**
             * V2
             */
            disconnectFromProxy();

            Log.i(TAG, "client socket close properly");
        }

        public void sendPackageToServer(int cnt, Package[] packages) {

            if (socket != null && !socket.isClosed())
                try {
                    objectOutputStream.write(packages.length);
                    // Check whether the data length is correct
                    for (int i = 0; i < packages.length; ++i) {
                        objectOutputStream.writeObject(packages[i]);
                    }
                } catch (IOException e) {
                    Log.i(TAG, "outputStream fail: " + e.toString());
                    e.printStackTrace();
                }

        }
    }

    public static Handler pHandler = null;

    /**
     * 處理費時任務
     */
    private static class ProcessHandler extends Handler {

        private WeakReference<Service> weakReference = null;

        public ProcessHandler(Service service, Looper looper) {
            super(looper);
            weakReference = new WeakReference<Service>(service);
        }

        public void handleMessage(Message msg) {
            Package pack = null;
            switch (msg.what) {
                case 0:
                    // reserve
                    break;
                case 1:
                    // reserve
                    break;
                case 2:
                    // 處理單個景點資訊，更新附近景點列表
                    pack = (Package) msg.obj;

                    if (pack.type == Package.TYPE_POI_SIN) {
                        SavePOI savePOI = new SavePOI();
                        POIModel poi = savePOI.parsePoiJSONObject(Utils.byteArrayToString(pack.payload));
                        poiList.add(poi);
                        if (pack.show == Package.SHOW_AUTO)
                            curPOI = poi;
                        Intent intent = new Intent();
                        intent.putExtra("show", pack.show);
                        intent.setAction(CONNECT_ACTION);
                        if (weakReference.get() != null)
                            weakReference.get().sendBroadcast(intent);
                    } else if (pack.type == Package.TYPE_IMAGE || pack.type == Package.TYPE_AUDIO || pack.type == Package.TYPE_VIDEO) {
                        Log.d("TAG", "multimedia file : " + pack.name);
                        ExternalStorage.writeToSDcard(pack.name, pack.payload);
                        Intent intent = new Intent();
                        intent.putExtra("file", pack.name);
                        intent.setAction(FILE_COMPLETE__ACTION);
                        if (weakReference.get() != null)
                            weakReference.get().sendBroadcast(intent);
                    }
                    break;
                case 3:
                    // 處理server要求播放的多媒體檔
                    pack = (Package) msg.obj;
                    String object = Utils.byteArrayToString(pack.payload);

                    if (object.contains("start video")) {
                        Log.d(TAG, "start VideoReceiverThread");
                        // new VideoReceiverThread(Environment.getExternalStorageDirectory() + "/video.mp4").start();

                        String arrayObject[] = object.split("\\s+");
                        if (arrayObject.length < 4) break;

                        Intent intent = new Intent();
                        intent.putExtra("mediaLength", Long.valueOf(arrayObject[2]));
                        intent.putExtra("remoteUri", arrayObject[3]);
                        PLAYBACK.remoteUri = arrayObject[3];
                        PLAYBACK.mediaLength = Long.valueOf(arrayObject[2]);
                        PLAYBACK.isReady = false;
                        PLAYBACK.isDownloaded = false;
                        PLAYBACK.readSize = 0;
                        PLAYBACK.errorCount = 0;
                        PLAYBACK.currentPosition = 0;
                        PLAYBACK.isError = false;
                        intent.setAction(VIDEO_START_ACTION);
                        if (weakReference.get() != null)
                            weakReference.get().sendBroadcast(intent);
                    } else if (object.contains("request video")) {
                        Log.d(TAG, "start VideoReceiverThread request");
                        // new VideoReceiverThread(Environment.getExternalStorageDirectory() + "/video.mp4").start();

                        String arrayObject[] = object.split("\\s+");
                        if (arrayObject.length < 4) break;

                        Intent intent = new Intent();
                        intent.putExtra("mediaLength", Long.valueOf(arrayObject[2]));
                        intent.putExtra("remoteUri", arrayObject[3]);
                        PLAYBACK.remoteUri = arrayObject[3];
                        PLAYBACK.mediaLength = Long.valueOf(arrayObject[2]);
                        PLAYBACK.isReady = false;
                        PLAYBACK.isDownloaded = false;
                        PLAYBACK.readSize = 0;
                        PLAYBACK.errorCount = 0;
                        PLAYBACK.currentPosition = 0;
                        PLAYBACK.isError = false;
                        intent.setAction(VIDEO_START_ACTION);
                        intent.putExtra("data", 1);
                        if (weakReference.get() != null)
                            weakReference.get().sendBroadcast(intent);
                    } else if (object.contains("start photo")) {
                        Intent intent = new Intent();
                        intent.setAction(PHOTO_START_ACTION);
                        if (weakReference.get() != null)
                            weakReference.get().sendBroadcast(intent);
                    }
                    break;
                default:
            }
        }
    }

    public void setPoiList(String result) {
        pHandler.sendMessage(pHandler.obtainMessage(3, result));
    }

    public ArrayList<POIModel> getPOIList() {
        return this.poiList;
    }

    public POIModel getCurPOI() {
        return curPOI;
    }

    /**
     * V2
     */
    public void disconnectFromProxy() {
        Log.d(TAG, "disconnectFromProxy()...");
        Intent intent = new Intent();
        intent.setAction(MemberService.DISCONNECT_FROM_PROXY);
        sendBroadcast(intent);
    }
}