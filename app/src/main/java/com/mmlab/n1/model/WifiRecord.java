package com.mmlab.n1.model;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;

import org.json.JSONException;
import org.json.JSONObject;

public class WifiRecord {

    public static final String AUTHENTICATING = "WifiRecord.AUTHENTICATING";
    public static final String COMPLETED = "WifiRecord.COMPLETED";
    public static final String DISCONNECTED = "WifiRecord.DISCONNECTED";
    public static final String FINISHED = "WifiRecord.FINISHED";
    /**
     * 加密用常數
     */
    public static final int WPA = 0;
    public static final int WPA2 = 1;
    public static final int WEP = 2;
    public static final int WPS = 3;
    public static final int NOPASS = 4;
    public String state = "";

    public String FBID = "";
    public String SSID = "";
    public String BSSID = "";
    public int level = 0;
    public String capabilities = "";

    public String SSIDpwd = "";

    public boolean isHost = false;

    public WifiRecord() {

    }

    public WifiRecord(ScanResult scanResult) {
        updateScanResult(scanResult);
    }

    public WifiRecord(WifiConfiguration wifiConfiguration) {
        this.SSID = wifiConfiguration.SSID;
        this.BSSID = wifiConfiguration.BSSID;
        if (wifiConfiguration.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.NONE)) {
            this.capabilities = WifiConfiguration.KeyMgmt.strings[WifiConfiguration.KeyMgmt.NONE];
        }
        if (wifiConfiguration.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK)) {
            this.capabilities = WifiConfiguration.KeyMgmt.strings[WifiConfiguration.KeyMgmt.WPA_PSK];
        }
        if (wifiConfiguration.allowedKeyManagement.get(4)) {
            this.capabilities = WifiConfiguration.KeyMgmt.strings[4];
        }
        if (wifiConfiguration.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.IEEE8021X)) {
            this.capabilities = WifiConfiguration.KeyMgmt.strings[WifiConfiguration.KeyMgmt.IEEE8021X];
        }
        this.capabilities = wifiConfiguration.toString();
        this.level = 5;
        this.isHost = true;
    }

    public void updateScanResult(ScanResult scanResult) {
        this.SSID = scanResult.SSID;
        this.BSSID = scanResult.BSSID;
        this.capabilities = scanResult.capabilities;
        this.level = scanResult.level;
    }

    public WifiRecord(JSONObject jsonObject) {
        try {
            this.SSID = jsonObject.getString("SSID");
            this.FBID = jsonObject.getString("FBID");
            this.capabilities = jsonObject.getString("encrypt");
            this.SSIDpwd = jsonObject.getString("SSIDpwd");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public boolean isWifiScanned() {
        return !SSID.isEmpty();
    }

    public String getStatus() {
        String security = "";
        switch (state) {
            case FINISHED:
                security = "已連線";
                break;
            case COMPLETED:
                security = "認證中";
                break;
            case AUTHENTICATING:
                security = "認證中";
                break;
            case DISCONNECTED:
                security = "中斷連線";
                break;
            default:
                String capabilities = this.capabilities;
                if (capabilities.contains("WPA") && capabilities.contains("WPA2")) {
                    security = "透過WPA/WPA2加密保護";
                    if (capabilities.contains("WPS")) {
                        security += "(可使用WPS)";
                    }
                } else if (capabilities.contains("WPA")) {
                    security = "透過WPA加密保護";
                    if (capabilities.contains("WPS")) {
                        security += "(可使用WPS)";
                    }
                } else if (capabilities.contains("WEP") || capabilities.contains("IEEE")) {
                    security = "透過WEP加密保護";
                    if (capabilities.contains("WPS")) {
                        security += "(可使用WPS)";
                    }
                } else if (capabilities.contains("WPA2")) {
                    security = "透過WPA2加密保護";
                    if (capabilities.contains("WPS")) {
                        security += "(可使用WPS)";
                    }
                }
        }
        return security;
    }

    public static int getType(String capabilities) {
        if (capabilities.contains("WEP") || capabilities.contains("IEEE")) {
            return WifiRecord.WEP;
        } else if (capabilities.contains("WPA") || capabilities.contains("WPA2"))
            return WifiRecord.WPA;
        else {
            return WifiRecord.NOPASS;
        }
    }

    public String getSecurity() {
        String status = "";
        String capabilities = this.capabilities;
        if (capabilities.contains("WPA") && capabilities.contains("WPA2")) {
            status = "WPA/WPA2";
            if (capabilities.contains("PSK")) {
                status += " PSK";
            }
        } else if (capabilities.contains("WPA")) {
            status = "WPA";
            if (capabilities.contains("PSK")) {
                status += " PSK";
            }
        } else if (capabilities.contains("WPA2")) {
            status = "WPA2";
            if (capabilities.contains("PSK")) {
                status += " PSK";
            }
        } else if (capabilities.contains("WEP") || capabilities.contains("IEEE")) {
            status = "WEP";
            if (capabilities.contains("PSK")) {
                status += " PSK";
            }
        } else {
            status = "無";
        }
        return status;
    }

    public int getWifiEncrypt() {
        if (capabilities.contains("WPA") || capabilities.contains("WPA2")) {
            return WPA;
        }
        if (capabilities.contains("WEP") || capabilities.contains("IEEE")) {
            return WEP;
        }
        return NOPASS;
    }

    /**
     * 給WifiInfo、WifiConfiguration使用
     *
     * @param SSID 服務設定識別碼
     * @return 無雙引號的服務設定識別碼
     */
    public static String normalizedSSID(String SSID) {
        return SSID.replaceFirst("^\"", "").replaceFirst("\"$", "");
    }
}
