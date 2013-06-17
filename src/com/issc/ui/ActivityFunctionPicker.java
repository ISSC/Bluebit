// vim: et sw=4 sts=4 tabstop=4
package com.issc.ui;

import com.issc.Bluebit;
import com.issc.data.BLEDevice;
import com.issc.R;
import com.issc.util.Log;
import com.issc.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.ListActivity;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.samsung.android.sdk.bt.gatt.BluetoothGatt;
import com.samsung.android.sdk.bt.gatt.BluetoothGattAdapter;
import com.samsung.android.sdk.bt.gatt.BluetoothGattCallback;
import com.samsung.android.sdk.bt.gatt.BluetoothGattCharacteristic;
import com.samsung.android.sdk.bt.gatt.BluetoothGattService;

public class ActivityFunctionPicker extends ListActivity
        implements BluetoothProfile.ServiceListener {

    private final static String sKey = "key";
    private final static String sVal = "value";

    private BluetoothDevice mDevice;
    private ArrayList<Map<String, Object>> mEntries;
    private BaseAdapter mAdapter;

    private BluetoothGatt mGatt;
    private BluetoothGattCallback mCallback;

    private final static int DISCOVERY_DIALOG = 1;
    private ProgressDialog mDiscoveringDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_function_picker);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (!extras.containsKey(Bluebit.CHOSEN_DEVICE)) {
            finish();
        }

        mCallback = new GattCallback();
        initAdapter();

        BLEDevice device = intent.getParcelableExtra(Bluebit.CHOSEN_DEVICE);
        mDevice = device.getDevice();

        TextView tv = (TextView) findViewById(R.id.picker_dev_name);
        tv.setText(mDevice.getName());
    }

    @Override
    protected void onResume() {
        super.onResume();
        BluetoothGattAdapter.getProfileProxy(this, this,
                BluetoothGattAdapter.GATT);
    }

    @Override
    protected void onPause() {
        super.onPause();
        BluetoothGattAdapter.closeProfileProxy(BluetoothGattAdapter.GATT,
                mGatt);
    }

    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {
        if (id == DISCOVERY_DIALOG) {
            mDiscoveringDialog = new ProgressDialog(this);
            mDiscoveringDialog.setMessage(this.getString(R.string.scanning));
            mDiscoveringDialog.setOnCancelListener(new Dialog.OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                    stopDiscovery();
                }
            });
            return mDiscoveringDialog;
        }
        return null;
    }

    private void startDiscovery() {
        runOnUiThread(new Runnable() {
            public void run() {
                showDialog(DISCOVERY_DIALOG);
            }
        });
        if (mGatt != null) {
            if (mGatt.getConnectionState(mDevice) == BluetoothProfile.STATE_CONNECTED) {
                Log.d("connected, start discovery");
                mGatt.discoverServices(mDevice);
            } else {
                mGatt.connect(mDevice, false);
                Log.d("trying to connect");
            }
        } else {
            Log.d("mGatt is null!!");
        }
    }

    private void stopDiscovery() {
        runOnUiThread(new Runnable() {
            public void run() {
                dismissDialog(DISCOVERY_DIALOG);
            }
        });
        if (mGatt != null) {
            mGatt.cancelConnection(mDevice);
        }
    }

    private void initAdapter() {
        String[] from = {sKey, sVal};
        int[] to = {R.id.row_title, R.id.row_description};

        mEntries = new ArrayList<Map<String, Object>>();
        mAdapter = new SimpleAdapter(
                    this,
                    mEntries,
                    R.layout.row_detail,
                    from,
                    to
                );

        setListAdapter(mAdapter);
    }


    private void append(String key, String value) {
        Map<String, Object> entry = new HashMap<String, Object>();
        entry.put(sKey, key);
        entry.put(sVal, value);
        mEntries.add(entry);

        runOnUiThread(new Runnable() {
            public void run() {
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    private void onDiscovered(BluetoothDevice device) {
        Log.d("on discovered:");
        if (mGatt != null) {
            List<BluetoothGattService> srvs = mGatt.getServices(device);
            Iterator<BluetoothGattService> it = srvs.iterator();
            while (it.hasNext()) {
                appendServices(it.next());
            }
        }
    }

    private void appendServices(BluetoothGattService srv) {
        append("Service", srv.getUuid().toString());
    }

    @Override
    public void onServiceConnected(int profile, BluetoothProfile proxy) {
        if (profile == BluetoothGattAdapter.GATT) {
            mGatt = (BluetoothGatt) proxy;
            mGatt.registerApp(mCallback);
        }
    }

    @Override
    public void onServiceDisconnected(int profile) {
        if (profile == BluetoothGattAdapter.GATT) {
            mGatt.unregisterApp();
            mGatt = null;
        }
    }

    class GattCallback extends BluetoothGattCallback {

        @Override
        public void onAppRegistered(int status) {
            startDiscovery();
        }

        @Override
        public void onServicesDiscovered(BluetoothDevice device, int status) {
            stopDiscovery();
            onDiscovered(device);
        }

        @Override
        public void onConnectionStateChange(BluetoothDevice device,
                int status, int newState) {

            if (mGatt == null) {
                Log.d("There is no Gatt to be used, skip");
                return;
            }

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d("connected to device, start discovery");
                mGatt.discoverServices(mDevice);
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            }
        }
    }
}
