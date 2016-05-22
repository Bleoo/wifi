package com.leo.wifi;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private Button btn_wifi_switch;
    private ListView lv_wifi_list;

    private InputDialog mInputDialog;
    private WifiUtils mWifiUtils;
    private List<ScanResult> mScanResults = new ArrayList<>();
    private WifiAdapter mWifiAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_wifi_switch = (Button) findViewById(R.id.btn_wifi_switch);
        lv_wifi_list = (ListView) findViewById(R.id.lv_wifi_list);
        btn_wifi_switch.setOnClickListener(this);
        findViewById(R.id.btn_wifi_disconnect).setOnClickListener(this);

        mWifiAdapter = new WifiAdapter(this, mScanResults);
        lv_wifi_list.setAdapter(mWifiAdapter);
        lv_wifi_list.setOnItemClickListener(this);

        mWifiUtils = new WifiUtils(this);
        mWifiUtils.setStatusListener(new WifiUtils.StatusListener() {
            @Override
            public void statusChanged() {
                setWifiStatus();
            }
        });
        mWifiUtils.setNetworkStatusListener(new WifiUtils.NetworkStatusListener() {
            @Override
            public void connected(String SSID) {
                mWifiAdapter.setConnectedSSID(SSID);
                mWifiAdapter.notifyDataSetChanged();
            }

            @Override
            public void disConnected() {
                mWifiAdapter.setConnectedSSID(null);
                mWifiAdapter.notifyDataSetChanged();
            }
        });
        setWifiStatus();
        startScanResult();
    }

    private void startScanResult() {
        mWifiUtils.openWifi();
        mWifiUtils.startScan(new WifiUtils.ScanResultListener() {
            @Override
            public void getScanResults(List<ScanResult> scanResults) {
                mScanResults.clear();
                mScanResults.addAll(scanResults);
                mWifiAdapter.notifyDataSetChanged();
            }
        });
    }

    private void setWifiStatus() {
        if (mWifiUtils.isWifiEnabled()) {
            btn_wifi_switch.setText("close");
        } else {
            btn_wifi_switch.setText("open");
            mScanResults.clear();
            mWifiAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_wifi_switch:
                if (mWifiUtils.isWifiEnabled()) {
                    mWifiUtils.closeWifi();
                    mScanResults.clear();
                    mWifiAdapter.notifyDataSetChanged();
                } else {
                    startScanResult();
                }
                break;
            case R.id.btn_wifi_disconnect:
                mWifiUtils.disconnectWifi(mWifiUtils.getConnectWifiId());
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ScanResult result = mScanResults.get(position);
        String capabilities = result.capabilities;
        if (!TextUtils.isEmpty(capabilities)) {
            if (capabilities.contains("WPA") || capabilities.contains("wpa")) {
                showInputDialog(result.SSID, WifiUtils.WIFICIPHER_WPA);
            } else if (capabilities.contains("WEP") || capabilities.contains("wep")) {
                showInputDialog(result.SSID, WifiUtils.WIFICIPHER_WEP);
            } else {
                mWifiUtils.connectWifi(result.SSID, null, WifiUtils.WIFICIPHER_NOPASS);
            }
        }
    }

    private void showInputDialog(final String SSID, final int capabilities) {
        if (mInputDialog == null) {
            mInputDialog = new InputDialog(this);
        }
        mInputDialog.setTitle("请输入" + SSID + "的密码：");
        mInputDialog.setDoneListener(new InputDialog.DoneListener() {
            @Override
            public void done(String context) {
                if (context.length() > 7) {
                    mWifiUtils.connectWifi(SSID, context, capabilities);
                    mInputDialog.dismiss();
                } else {
                    Toast.makeText(MainActivity.this, "密码必须八位及以上", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mInputDialog.show();
    }
}