package com.leo.wifi;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Administrator on 2016/5/18.
 */
public class WifiAdapter extends BaseAdapter {

    private Context mContext;
    private List<ScanResult> mScanResults;
    private String mConnectedSSID;

    public WifiAdapter(Context context, List<ScanResult> scanResults) {
        mContext = context;
        mScanResults = scanResults;
    }

    @Override
    public int getCount() {
        return mScanResults.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_wifilist, null);
            holder.tv_ssid = (TextView) convertView.findViewById(R.id.tv_ssid);
            holder.iv_connected = (ImageView) convertView.findViewById(R.id.iv_connected);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        String SSID = mScanResults.get(position).SSID;
        holder.tv_ssid.setText(SSID);
        if (SSID.equals(mConnectedSSID)) {
            holder.iv_connected.setVisibility(View.VISIBLE);
        } else {
            holder.iv_connected.setVisibility(View.INVISIBLE);
        }
        return convertView;
    }

    private class ViewHolder {
        TextView tv_ssid;
        ImageView iv_connected;
    }

    public void setConnectedSSID(String connectedSSID) {
        mConnectedSSID = connectedSSID;
    }

}