// vim: et sw=4 sts=4 tabstop=4

package com.issc.ui;

import com.issc.util.Log;
import com.issc.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityDevicesList extends Activity {
    private ListView mListView;
    private Button mBtnScan;
    private BroadcastReceiver mReceiver;
    private BaseAdapter mAdapter;

    private List<Target> mDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices_list);

        mBtnScan = (Button) findViewById(R.id.btn_scan);
        mListView = (ListView) findViewById(R.id.devices_list);

        mReceiver = new ActionReceiver();

        mDevices = new ArrayList<Target>();
        mAdapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1, mDevices);

        mListView.setAdapter(mAdapter);
        mListView.setEmptyView(findViewById(R.id.empty));
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        this.registerReceiver(mReceiver, filter);

    }

    @Override
    public void onStop() {
        super.onStop();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void onClickBtnScan(View v) {
        if (Util.isBluetoothEnabled()) {
            if (Util.isDiscovering()) {
                stopDiscovery();
            } else {
                startDiscovery();
            }
        } else {
            Log.d("Trying to enable Bluetooth");
            Util.enableBluetooth(this, 0);
        }
    }

    private void startDiscovery() {
        Log.d("Scanning Devices");
        mDevices.clear();
        mAdapter.notifyDataSetChanged();
        Util.startDiscovery();
        mBtnScan.setText(R.string.text_stop_scan);
    }

    private void stopDiscovery() {
        Log.d("Stop scanning");
        Util.stopDiscovery();
        mBtnScan.setText(R.string.text_scan);
    }

    private void appendDevice(BluetoothDevice device) {
        if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
            Target t = new Target(device);
            mDevices.add(t);
            mAdapter.notifyDataSetChanged();
            Log.d(t.toString());
        }
    }

    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        }
    };

    class ActionReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                appendDevice(device);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                stopDiscovery();
            }
        }
    }

    private class Target {
        BluetoothDevice iDevice;

        Target(BluetoothDevice device) {
            iDevice = device;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(iDevice.getName());
            sb.append("(" + iDevice.getBluetoothClass().toString() + ")");
            sb.append("  " + iDevice.getAddress());
            return sb.toString();
        }
    }
}
