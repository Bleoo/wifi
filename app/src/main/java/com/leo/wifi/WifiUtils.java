package com.leo.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Parcelable;
import android.util.Log;

import java.util.List;

/**
 * Created by Administrator on 2016/5/8.
 */
public class WifiUtils {

    public static final int WIFICIPHER_NOPASS = 0;
    public static final int WIFICIPHER_WEP = 1;
    public static final int WIFICIPHER_WPA = 2;

    private WifiManager mWifiManager;
    private BroadcastReceiver mWifiStatusReceiver;
    private ScanResultListener mScanResultListener;
    private List<ScanResult> mScanResults;
    private StatusListener mStatusListener;
    private NetworkStatusListener mNetworkStatusListener;

    public WifiUtils(Context context) {
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        mWifiStatusReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                    mScanResults = mWifiManager.getScanResults();
                    if (mScanResultListener != null) {
                        mScanResultListener.getScanResults(mScanResults);
                    }
                    for (ScanResult result : mScanResults) {
                        Log.e("result", result.toString());
                    }
                }
                if (intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION) && mStatusListener != null) {
                    mStatusListener.statusChanged();
                }
                if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION) && mNetworkStatusListener != null) {
                    Parcelable parcelableExtra = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                    if (parcelableExtra != null) {
                        NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
                        if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                            Log.e("NetworkInfo.State", "CONNECTED");
                            mNetworkStatusListener.connected(getConnectWifiSSID());
                        } else if (networkInfo.getState() == NetworkInfo.State.DISCONNECTED) {
                            Log.e("NetworkInfo.State", "DISCONNECTED");
                            mNetworkStatusListener.disConnected();
                        }
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION); // 监听扫描成功广播
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION); // 监听开关广播
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION); // 监听状态广播
        context.registerReceiver(mWifiStatusReceiver, filter);
    }

    /**
     * 打开wifi
     */
    public void openWifi() {
        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
        }
    }

    /**
     * 关闭Wifi
     **/
    public void closeWifi() {
        if (mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(false);
        }
    }

    /**
     * 扫描
     */
    public void startScan(ScanResultListener scanResultListener) {
        mWifiManager.startScan();
        mScanResultListener = scanResultListener;
    }

    /**
     * 是否开启
     *
     * @return
     */
    public boolean isWifiEnabled() {
        return mWifiManager.isWifiEnabled();
    }

    /**
     * 设置开关监听
     *
     * @param statusListener
     */
    public void setStatusListener(StatusListener statusListener) {
        mStatusListener = statusListener;
    }

    /**
     * 设置状态监听
     *
     * @param networkStatusListener
     */
    public void setNetworkStatusListener(NetworkStatusListener networkStatusListener) {
        mNetworkStatusListener = networkStatusListener;
    }

    /**
     * 获取当前连接的SSID
     * @return
     */
    public String getConnectWifiSSID() {
        if (isWifiEnabled()) {
            WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
            if (wifiInfo != null) {
                String WifiName = wifiInfo.getSSID();
                String WifiSSID = WifiName.substring(1, WifiName.length() - 1);
                if (!"unknown ssid".equals(WifiSSID)) {
                    return WifiSSID;
                }
            }
        }
        return null;
    }

    /**
     * 获取当前连接的WifiID
     * @return
     */
    public int getConnectWifiId(){
        if (isWifiEnabled()) {
            WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
            if (wifiInfo != null) {
                return wifiInfo.getNetworkId();
            }
        }
        return -1;
    }

    /**
     * 断开连接
     * @param netId
     * @return
     */
    public boolean disconnectWifi(int netId) {
        if (mWifiManager.isWifiEnabled()) {
            return mWifiManager.disableNetwork(netId) && mWifiManager.disconnect();
        }
        return true;
    }

    /**
     * 移除对应wifi配置信息
     * @param netId
     * @return
     */
    public boolean removeWifi(int netId) {
        return disconnectWifi(netId) && mWifiManager.removeNetwork(netId);
    }

    /**
     * 连接wifi
     * @param ssid
     * @param password
     * @param type
     * @return
     */
    public boolean connectWifi(String ssid, String password, int type) {
        if (removeWifi(getConnectWifiId())) {
            Log.e("disconnectWifi", "true");
        }
        int wifiId = mWifiManager.addNetwork(CreateWifiInfo(ssid, password, type));
        Log.e("wifiId", wifiId + "");
        if (wifiId == -1) {
            Log.e("connectWifi", "false");
            return false;
        }
        Log.e("connectWifi", "true");
        return true;
    }

    /**
     * 创建wifi配置信息
     * @param ssid
     * @param password
     * @param type
     * @return
     */
    public WifiConfiguration CreateWifiInfo(String ssid, String password, int type) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + ssid + "\"";

        WifiConfiguration tempConfig = this.IsExsits(ssid);

        if (tempConfig != null) {
            mWifiManager.removeNetwork(tempConfig.networkId);
        }

        if (type == WIFICIPHER_NOPASS) {
            //config.wepKeys[0] = "";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            //config.wepTxKeyIndex = 0;
        } else if (type == WIFICIPHER_WEP) {
            config.hiddenSSID = true;
            config.wepKeys[0] = "\"" + password + "\"";
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        } else if (type == WIFICIPHER_WPA) {
            config.hiddenSSID = true;
            config.preSharedKey = "\"" + password + "\"";
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }
        Log.e("config", config.toString());
        return config;
    }

    /**
     * 查看是否已经配置过该SSID
     * @param ssid
     * @return
     */
    private WifiConfiguration IsExsits(String ssid) {
        List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals("\"" + ssid + "\"")) {
                return existingConfig;
            }
        }
        return null;
    }

    /**
     * wifi扫描结果监听接口
     */
    public interface ScanResultListener {
        void getScanResults(List<ScanResult> scanResults);
    }

    /**
     * wifi开关监听接口
     */
    public interface StatusListener {
        void statusChanged();
    }

    /**
     * wifi网络状态监听接口
     */
    public interface NetworkStatusListener {
        void connected(String SSID);

        void disConnected();
    }

}