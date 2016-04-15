package com.mmlab.n1.network;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;


import com.mmlab.n1.model.WifiRecord;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class NetworkManagerN2 {

    private static final String TAG = "NetworkManagerN2";

    private Context mContext;
    private WifiManager mWifiManager;
    private static final Map<String, Method> methodMap = new HashMap<String, Method>();
    public boolean isHtc = false;

    public NetworkManagerN2(Context context) {
        mContext = context;
        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);

        // check whether this is a HTC device
        try {
            Field field = WifiConfiguration.class.getDeclaredField("mWifiApProfile");
            isHtc = field != null;
        } catch (Exception ignored) {
        }

        try {
            Method method = WifiManager.class.getMethod("getWifiApState");
            methodMap.put("getWifiApState", method);
        } catch (SecurityException | NoSuchMethodException ignored) {
        }

        try {
            Method method = WifiManager.class.getMethod("getWifiApConfiguration");
            methodMap.put("getWifiApConfiguration", method);
        } catch (SecurityException | NoSuchMethodException ignored) {
        }

        try {
            Method method = WifiManager.class.getMethod(getSetWifiApConfigName(), WifiConfiguration.class);
            methodMap.put("setWifiApConfiguration", method);
        } catch (SecurityException | NoSuchMethodException ignored) {
        }

        try {
            Method method = WifiManager.class.getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            methodMap.put("setWifiApEnabled", method);
        } catch (SecurityException | NoSuchMethodException ignored) {
        }
    }

    public boolean isWifiEnabled() {
        return mWifiManager.isWifiEnabled();
    }

    public void startScan() {
        mWifiManager.startScan();
    }

    public List<ScanResult> getScanResults() {
        return mWifiManager.getScanResults();
    }

    public List<WifiConfiguration> getConfiguredNetworks() {
        return mWifiManager.getConfiguredNetworks();
    }

    public WifiInfo getConnectionInfo() {
        return mWifiManager.getConnectionInfo();
    }

    /**
     * 取得當前所連無線網路SSID
     */
    public String getActivedSSID() {
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        String ssid = wifiInfo.getSSID();
        return WifiRecord.normalizedSSID(ssid);
    }

    /**
     * 計算訊號強度
     *
     * @param level ScanResult object field : level
     * @return 強度1-5
     */
    public static int calculateSignalStength(int level) {
        return WifiManager.calculateSignalLevel(level, 5);
    }

    /**
     * 查看以前是否配置過這個網絡
     *
     * @param SSID 服務設定識別碼
     * @return 回傳WifiConfiguration，可以為null
     */
    public WifiConfiguration getConfiguredNetwork(String SSID) {
        List<WifiConfiguration> configurations = mWifiManager.getConfiguredNetworks();
        if (configurations != null) {
            for (WifiConfiguration wifiConfiguration : configurations) {
                if (wifiConfiguration.SSID != null && wifiConfiguration.SSID.equals("\"" + SSID + "\"")) {
                    return wifiConfiguration;
                }
            }
        }
        return null;
    }

    /**
     * 提供一個外部接口，傳入已經認證的無線網絡
     * enableNetwork()方法回傳值為真，只能說明密碼沒有輸錯，並且網路可用，但不一定連接上
     * 最後需要在呼叫reconnect()連接上次關聯的無線網絡
     *
     * @param wifiRecord WifiRecord
     * @return 回傳認證是否成功
     */
    public boolean connect(WifiRecord wifiRecord) {
        WifiConfiguration wifiConfiguration = getConfiguredNetwork(wifiRecord.SSID);
        disableNetwork();
        if (wifiConfiguration != null) {
            Log.d(TAG, "connect()...success");
            return connectConfigured(wifiRecord.SSID);
        } else {
            Log.d(TAG, "connect()...fail");
            return connectUnconfigured(wifiRecord);
        }
    }

    /**
     * 提供一個外部接口，傳入要連接的尚未認證或想更改認證的無線網絡
     * 回傳值為真，只能說明密碼沒有輸錯，並且網路可用，但不一定連接上
     */
    public boolean connectUnconfigured(WifiRecord wifiRecord) {
        // 更新認證的狀態
        WifiConfiguration wifiConfiguration = getConfiguredNetwork(wifiRecord.SSID);
        if (wifiConfiguration != null) {
            mWifiManager.removeNetwork(wifiConfiguration.networkId);
        }

        WifiConfiguration wifiConfig = this.createWifiInfo(wifiRecord.SSID, wifiRecord.SSIDpwd, WifiRecord.getType(wifiRecord.capabilities));
        if (wifiConfig == null) {
            Log.i(TAG, "wifiConfig is null");
            return false;
        }
        int netID = mWifiManager.addNetwork(wifiConfig);

        return mWifiManager.enableNetwork(netID, true) && mWifiManager.saveConfiguration() && mWifiManager.reconnect();
    }

    /**
     * 提供一個外部接口，傳入已經認證的無線網絡
     * enableNetwork()方法回傳值為真，只能說明密碼沒有輸錯，並且網路可用，但不一定連接上
     * 最後需要在呼叫reconnect()連接上次關聯的無線網絡
     */
    public boolean connectConfigured(String SSID) {
        WifiConfiguration wifiConfiguration = getConfiguredNetwork(SSID);
        return (mWifiManager.enableNetwork(wifiConfiguration.networkId, true) && mWifiManager.reconnect());
    }

    public boolean disableNetwork() {
        int netId = mWifiManager.getConnectionInfo().getNetworkId();
        if (netId >= 0) {
            return mWifiManager.disableNetwork(netId);
        } else {
            return true;
        }
    }

    public boolean disconnectNetwork(int netId) {
        if (netId >= 0) {
            return mWifiManager.removeNetwork(netId);
        } else {
            return true;
        }
    }

    public boolean disconnect() {
        return mWifiManager.disconnect();
    }

    public LinkedHashMap<String, WifiRecord> getWifiRecord() {

        List<ScanResult> scanResults = getScanResults();
        LinkedHashMap<String, WifiRecord> hashMap = new LinkedHashMap<>();

        for (ScanResult scanResult : scanResults) {
            WifiRecord wifiRecord;
            if (scanResult.SSID != null && !scanResult.SSID.equals("")) {
                if (hashMap.containsKey(scanResult.SSID)) {
                    wifiRecord = hashMap.get(scanResult.SSID);
                    if (calculateSignalStength(scanResult.level) >
                            calculateSignalStength(wifiRecord.level)) {
                        wifiRecord.updateScanResult(scanResult);
                    }
                } else {
                    wifiRecord = new WifiRecord();
                    wifiRecord.updateScanResult(scanResult);
                }
                hashMap.put(scanResult.SSID, wifiRecord);
            }
        }

        return hashMap;
    }

    private WifiConfiguration createWifiInfo(String SSID, String SSIDpwd,
                                             int type) {

        WifiConfiguration config = new WifiConfiguration();

        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();

        config.SSID = "\"" + SSID + "\"";
        config.status = WifiConfiguration.Status.DISABLED;
        config.priority = 40;

        if (type == WifiRecord.NOPASS) {
            config.wepKeys[0] = "";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
            return config;
        } else if (type == WifiRecord.WEP) {
            config.wepKeys[0] = "\"" + SSIDpwd + "\"";
            config.hiddenSSID = true;
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.wepTxKeyIndex = 0;

            return config;
        } else if (type == WifiRecord.WPA) {
            config.preSharedKey = "\"" + SSIDpwd + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            config.status = WifiConfiguration.Status.ENABLED;
            return config;
        } else {
            return null;
        }
    }

    public int getWifiApState() {
        try {
            Method method = methodMap.get("getWifiApState");
            return (Integer) method.invoke(mWifiManager);
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        }
        return -1;
    }

    private WifiConfiguration getHtcWifiApConfiguration(WifiConfiguration standard) {
        WifiConfiguration htcWifiConfig = standard;
        try {
            Object mWifiApProfileValue = getFieldValue(standard, "mWifiApProfile");
            if (mWifiApProfileValue != null) {
                htcWifiConfig.SSID = (String) getFieldValue(mWifiApProfileValue, "SSID");
                switch ((String) getFieldValue(mWifiApProfileValue, "secureType")) {
                    case "open":
                        htcWifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                        htcWifiConfig.wepKeys[0] = "";
                        htcWifiConfig.wepTxKeyIndex = 0;
                        htcWifiConfig.preSharedKey = (String) getFieldValue(mWifiApProfileValue, "key");
                        break;
                    case "wpa-psk":
                        htcWifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                        htcWifiConfig.preSharedKey = (String) getFieldValue(mWifiApProfileValue, "key");
                        break;
                    case "wpa2-psk":
                        int WPA2_PSK = 4;
                        htcWifiConfig.allowedKeyManagement.set(WPA2_PSK);
                        htcWifiConfig.preSharedKey = (String) getFieldValue(mWifiApProfileValue, "key");
                        break;
                    case "wep":
                        htcWifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.IEEE8021X);
                        htcWifiConfig.wepKeys[0] = (String) getFieldValue(mWifiApProfileValue, "key");
                        htcWifiConfig.wepTxKeyIndex = 0;
                        break;
                    default:
                }
            }
        } catch (Exception ignored) {
        }
        return htcWifiConfig;
    }

    public String getPrevKey() {
        WifiConfiguration configuration = null;
        String preKey = "";
        try {
            Method method = methodMap.get("getWifiApConfiguration");
            configuration = (WifiConfiguration) method.invoke(mWifiManager);
            if (isHtc)
                configuration = getHtcWifiApConfiguration(configuration);
        } catch (Exception ignored) {
        }

        if (configuration.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.NONE)) {
            preKey = configuration.preSharedKey;
        } else if (configuration.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK)) {
            preKey = configuration.preSharedKey;
        } else if (configuration.allowedKeyManagement.get(4)) {
            preKey = configuration.preSharedKey;
        } else if (configuration.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.IEEE8021X)) {
            preKey = configuration.wepKeys[configuration.wepTxKeyIndex];
        }
        return preKey;
    }

    public WifiConfiguration getWifiApConfiguration() {
        WifiConfiguration configuration = new WifiConfiguration();
        try {
            Method method = methodMap.get("getWifiApConfiguration");
            configuration = (WifiConfiguration) method.invoke(mWifiManager);
        } catch (Exception ignored) {
        }

        if (configuration == null || configuration.SSID == null) {
            if (isHtc)
                configuration = getHtcWifiApConfiguration(configuration);
        }

        return configuration;
    }
    private void setupHtcWifiConfiguration(WifiConfiguration config) {
        try {
            Object mWifiApProfileValue = getFieldValue(config, "mWifiApProfile");

            if (mWifiApProfileValue != null) {
                setFieldValue(mWifiApProfileValue, "SSID", config.SSID);
                if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.NONE)) {
                    setFieldValue(mWifiApProfileValue, "secureType", "open");
                    setFieldValue(mWifiApProfileValue, "key", config.preSharedKey);
                } else if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK)) {
                    setFieldValue(mWifiApProfileValue, "secureType", "wpa-psk");
                    setFieldValue(mWifiApProfileValue, "key", config.preSharedKey);
                } else if (config.allowedKeyManagement.get(4)) {
                    setFieldValue(mWifiApProfileValue, "secureType", "wpa2-psk");
                    setFieldValue(mWifiApProfileValue, "key", config.preSharedKey);
                } else if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.IEEE8021X)) {
                    setFieldValue(mWifiApProfileValue, "secureType", "wep");
                    setFieldValue(mWifiApProfileValue, "key", config.wepKeys[config.wepTxKeyIndex]);
                }
                setFieldValue(mWifiApProfileValue, "dhcpEnable", 1);
                setFieldValue(mWifiApProfileValue, "ipAddress", "192.168.1.1");
                setFieldValue(mWifiApProfileValue, "dhcpSubnetMask", "255.255.255.0");
                setFieldValue(mWifiApProfileValue, "startingIP", "192.168.1.100");
            }
        } catch (Exception e) {
        }
    }

    public boolean setWifiApConfiguration(WifiConfiguration config) {
        boolean result = false;
        try {
            if (isHtc)
                setupHtcWifiConfiguration(config);

            Method method = methodMap.get("setWifiApConfiguration");

            if (isHtc) {
                int value = (Integer) method.invoke(mWifiManager, config);
                result = value > 0;
            } else {
                result = (Boolean) method.invoke(mWifiManager, config);
            }
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
        return result;
    }


    private String getSetWifiApConfigName() {
        return isHtc ? "setWifiApConfig" : "setWifiApConfiguration";
    }

    public WifiManager wifiManager() {
        return mWifiManager;
    }

    private Object getFieldValue(Object object, String propertyName)
            throws IllegalAccessException, NoSuchFieldException {
        Field field = object.getClass().getDeclaredField(propertyName);
        field.setAccessible(true);
        return field.get(object);
    }

    private void setFieldValue(Object object, String propertyName, Object value)
            throws IllegalAccessException, NoSuchFieldException {
        Field field = object.getClass().getDeclaredField(propertyName);
        field.setAccessible(true);
        field.set(object, value);
        field.setAccessible(false);
    }
}
