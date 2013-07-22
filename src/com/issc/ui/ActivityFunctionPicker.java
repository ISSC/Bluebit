// vim: et sw=4 sts=4 tabstop=4
package com.issc.ui;

import com.issc.Bluebit;
import com.issc.impl.FunctionAdapter;
import com.issc.impl.GattProxy;
import com.issc.R;
import com.issc.util.Log;
import com.issc.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.samsung.android.sdk.bt.gatt.BluetoothGatt;
import com.samsung.android.sdk.bt.gatt.BluetoothGattAdapter;
import com.samsung.android.sdk.bt.gatt.BluetoothGattCallback;
import com.samsung.android.sdk.bt.gatt.BluetoothGattCharacteristic;
import com.samsung.android.sdk.bt.gatt.BluetoothGattService;

public class ActivityFunctionPicker extends ListActivity {

    private BluetoothDevice mDevice;
    private FunctionAdapter mAdapter;

    private BluetoothGatt mGatt;
    private GattProxy.Listener mListener;

    private final static int DISCOVERY_DIALOG = 1;
    private ProgressDialog mDiscoveringDialog;

    private boolean mDiscovered = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_function_picker);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (!extras.containsKey(Bluebit.CHOSEN_DEVICE)) {
            finish();
        }

        mListener = new GattListener();
        initAdapter();

        mDevice = intent.getParcelableExtra(Bluebit.CHOSEN_DEVICE);

        TextView tv = (TextView) findViewById(R.id.picker_dev_name);
        tv.setText(mDevice.getName());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mGatt != null) {
            mGatt.cancelConnection(mDevice);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        GattProxy proxy = GattProxy.get(this);
        proxy.addListener(mListener);
        proxy.retrieveGatt(mListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        GattProxy proxy = GattProxy.get(this);
        proxy.rmListener(mListener);
    }

    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {
        if (id == DISCOVERY_DIALOG) {
            mDiscoveringDialog = new ProgressDialog(this);
            mDiscoveringDialog.setMessage(this.getString(R.string.discovering));
            mDiscoveringDialog.setOnCancelListener(new Dialog.OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                    stopDiscovery();
                }
            });
            return mDiscoveringDialog;
        }
        return null;
    }

    private void connectToDevice() {
        runOnUiThread(new Runnable() {
            public void run() {
                showDialog(DISCOVERY_DIALOG);
            }
        });
        if (mGatt != null) {
            if (mGatt.getConnectionState(mDevice) == BluetoothProfile.STATE_CONNECTED) {
                Log.d("connected");
                List<BluetoothGattService> list = mGatt.getServices(mDevice);
                if ((list == null) || (list.size() == 0)) {
                    Log.d("start discovery");
                    mGatt.discoverServices(mDevice);
                } else {
                    onDiscovered(mDevice);
                }
            } else {
                boolean init = mGatt.connect(mDevice, false);
                Log.d("connecting to device, is this BLE? " + mGatt.isBLEDevice(mDevice));
                Log.d("does connection initialize successfully? " + init);
            }
        } else {
            Log.e("mGatt is null!!");
        }
    }

    private void stopDiscovery() {
        runOnUiThread(new Runnable() {
            public void run() {
                dismissDialog(DISCOVERY_DIALOG);
            }
        });
    }

    private void initAdapter() {
        mAdapter = new FunctionAdapter(this);
        setListAdapter(mAdapter);
    }


    private void onDiscovered(BluetoothDevice device) {
        Log.d("on discovered:");
        stopDiscovery();
        mDiscovered = true;
        if (mGatt != null) {
            List<BluetoothGattService> srvs = mGatt.getServices(device);
            Log.d("discovered result:" + srvs.size());
            Iterator<BluetoothGattService> it = srvs.iterator();
            while (it.hasNext()) {
                BluetoothGattService s = it.next();
                appendService(s);
            }
        }
    }

    private void appendService(BluetoothGattService srv) {
        Log.d("append Service:" + srv.getUuid().toString());
        appendUuid(srv.getUuid());
        List<BluetoothGattCharacteristic> list = srv.getCharacteristics();
        Iterator<BluetoothGattCharacteristic> it = list.iterator();
        while (it.hasNext()) {
            BluetoothGattCharacteristic chr = it.next();
            Log.d("  append chr:" + chr.getUuid().toString());
            appendUuid(chr.getUuid());
        }
    }

    private void appendUuid(final UUID uuid) {
        runOnUiThread(new Runnable() {
            public void run() {
                mAdapter.addUuidInUiThread(uuid);
            }
        });
    }

    @Override
    protected void onListItemClick(ListView l, View v, int pos, long id) {
        Intent i = mAdapter.createIntent(pos);
        i.putExtra(Bluebit.CHOSEN_DEVICE, mDevice);
        startActivity(i);
    }

    class GattListener extends GattProxy.ListenerHelper {
        GattListener() {
            super("ActivityFunctionPicker");
        }

        @Override
        public void onRetrievedGatt(BluetoothGatt gatt) {
            Log.d(String.format("onRetrievedGatt"));
            mGatt = gatt;
            if (mAdapter.getCount() == 0) {
                connectToDevice();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothDevice device, int status) {
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
                Log.d("disconnected!!!");
            }
        }
    }
}
