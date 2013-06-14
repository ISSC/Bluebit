// vim: et sw=4 sts=4 tabstop=4
package com.issc.ui;

import com.issc.Bluebit;
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

public class ActivityDeviceDetail extends ListActivity {

    private final static String sKey = "key";
    private final static String sVal = "value";

    private BluetoothDevice mDevice;
    private ArrayList<Map<String, Object>> mEntries;
    private BaseAdapter mAdapter;

    private BluetoothGatt mGatt;
    private GattServiceListener mGattListener;
    private BluetoothGattCallback mCallback;

    private final static int DISCOVERY_DIALOG = 1;
    private ProgressDialog mDiscoveringDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_detail);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (!extras.containsKey(Bluebit.CHOSEN_DEVICE)) {
            finish();
        }

        mCallback = new GattCallback();
        initAdapter();

        mDevice = intent.getParcelableExtra(Bluebit.CHOSEN_DEVICE);
        init(mDevice);

        mGattListener = new GattServiceListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        BluetoothGattAdapter.getProfileProxy(this,
                mGattListener,
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
        if (mGatt != null) {
            showDialog(DISCOVERY_DIALOG);
            mGatt.connect(mDevice, false);
        }
    }

    private void stopDiscovery() {
        dismissDialog(DISCOVERY_DIALOG);
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


    public void onClickBtnMore(View v) {
        if (mGatt != null) {
            startDiscovery();
        }
    }

    private void init(BluetoothDevice device) {
        append(getString(R.string.detail_name), device.getName());
        append(getString(R.string.detail_addr), device.getAddress());
        setDeviceClass(device);
    }

    private void setDeviceClass(BluetoothDevice device) {
        BluetoothClass clazz = device.getBluetoothClass();
        if (clazz == null) {
            Log.e("No Bluetooth Class");
            append(getString(R.string.detail_device_class), "None");
        } else {
            int major = clazz.getMajorDeviceClass();
            int res = Util.getDeviceClassRes(major);
            if (res == -1) {
                append(getString(R.string.detail_device_class), "Unknown");
            } else {
                append(getString(R.string.detail_device_class),
                            getString(res));
            }
        }
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
        List<BluetoothGattCharacteristic> chars = srv.getCharacteristics();
        Iterator<BluetoothGattCharacteristic> it = chars.iterator();
        while (it.hasNext()) {
            appendCharacteristic(it.next());
        }
    }

    private void appendCharacteristic(BluetoothGattCharacteristic ch) {
        append("Char", ch.getUuid().toString());
    }

    class GattServiceListener implements BluetoothProfile.ServiceListener {
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile == BluetoothGattAdapter.GATT) {
                mGatt = (BluetoothGatt) proxy;
                mGatt.registerApp(mCallback);
            }
        }

        public void onServiceDisconnected(int profile) {
            if (profile == BluetoothGattAdapter.GATT) {
                mGatt.unregisterApp();
                mGatt = null;
            }
        }
    }

    class GattCallback extends BluetoothGattCallback {
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
                mGatt.discoverServices(mDevice);
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            }
        }
    }
}
