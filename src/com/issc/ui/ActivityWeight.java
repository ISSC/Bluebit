// vim: et sw=4 sts=4 tabstop=4

package com.issc.ui;

import com.issc.Bluebit;
import com.issc.data.BLEDevice;
import com.issc.impl.GattProxy;
import com.issc.R;
import com.issc.util.Log;
import com.issc.util.Util;

import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.samsung.android.sdk.bt.gatt.BluetoothGatt;
import com.samsung.android.sdk.bt.gatt.BluetoothGattAdapter;
import com.samsung.android.sdk.bt.gatt.BluetoothGattCallback;
import com.samsung.android.sdk.bt.gatt.BluetoothGattCharacteristic;
import com.samsung.android.sdk.bt.gatt.BluetoothGattDescriptor;
import com.samsung.android.sdk.bt.gatt.BluetoothGattService;

public class ActivityWeight extends Activity {

    private BluetoothGatt mGatt;
    private GattProxy.Listener mListener;

    private final static double LB_BASE = 2.2046; // 1 kg is about 2.2046 lb
    private final static double ST_BASE = 0.1574; // 1 kg is about 0.1574 st

    private final static int UPDATE_VALUE = 0x9527;
    private final static int UPDATE_NAME  = 0x1984;

    private final static String VALUE_IN_MSG = "value_in_message_instance";
    private final static String NAME_IN_MSG  = "name_message_instance";

    private final static UUID mAdvData = Util.uuidFromStr("FFF0");
    private final static String ADDR = "78:C5:E5:6E:19:F2";

    /* use 0xFFF4 descriptor of 0xFFF0 characteristic to enable
     * notification from target */
    private final static UUID mUuidFFF0 = Util.uuidFromStr("FFF0");
    private final static UUID mUuidFFF4 = Util.uuidFromStr("FFF4");

    private BluetoothDevice mDevice;
    private BluetoothGattService        mFFF0;
    private BluetoothGattCharacteristic mFFF4;
    private BluetoothGattDescriptor     mCCC;

    private TextView mKg;
    private TextView mLb;
    private TextView mSt;
    private TextView mName;

    private ViewHandler mViewHandler;
    private final static DecimalFormat sDF = new DecimalFormat("0.0");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weight);

        mKg = (TextView) findViewById(R.id.kg);
        mLb = (TextView) findViewById(R.id.lb);
        mSt = (TextView) findViewById(R.id.st);
        mName = (TextView) findViewById(R.id.weight_name);
        mListener = new GattListener();
        mViewHandler = new ViewHandler();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GattProxy proxy = GattProxy.get(ActivityWeight.this);
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
        stopScanningTarget();
        GattProxy proxy = GattProxy.get(this);
        proxy.rmListener(mListener);
    }

    public void onClickName(View v) {
        /*TODO: using Dialog is not good, we should improve it */
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Set New Name");

        // Set an EditText view to get user input 
        final EditText input = new EditText(this);
        alert.setView(input);
        input.setText(mName.getText());

        alert.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if(input.getText().length() <= 0) {
                    // input nothing, aborting
                    return;
                }
                onChangingName(input.getText());
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();
    }

    private void onChangingName(CharSequence newName) {
        mName.setText(newName);
    }

    private void connect() {
        mGatt.connect(mDevice, false);
    }

    private void scanTarget() {
        mDevice = null;
        if (mGatt != null) {
            Log.d("Scanning Target");
            // does not work yet.
            //UUID[] uuids = new UUID[1];
            //uuids[0] = mAdvData;
            //mGatt.startScan(uuids);
            mGatt.startScan();
        } else {
            Log.e("No Gatt instance");
        }
    }

    private void stopScanningTarget() {
        Log.d("Stop scanning");
        if (mGatt != null) {
            mGatt.stopScan();
        } else {
            Log.e("No Gatt instance");
        }
    }

    private void onConnected() {
        List<BluetoothGattService> list = mGatt.getServices(mDevice);
        if ((list == null) || (list.size() == 0)) {
            Log.d("no services, do discovery");
            mGatt.discoverServices(mDevice);
        } else {
            onDiscovered();
        }
    }

    private void onDiscovered() {
        Log.d("Discovered services, enable notification");
        mFFF0 = mGatt.getService(mDevice, mUuidFFF0);
        mFFF4 = mFFF0.getCharacteristic(mUuidFFF4);
        mGatt.setCharacteristicNotification(mFFF4, true);

        mCCC = mFFF4.getDescriptor(Bluebit.DES_CLIENT_CHR_CONFIG);
        mCCC.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mGatt.writeDescriptor(mCCC);
    }

    private void onDisconnected() {
        mFFF0 = null;
        mFFF4 = null;
        mCCC = null;
        scanTarget();
    }

    private void onFoundTarget(BluetoothDevice dev, byte[] records) {
        if (mDevice == null) {
            mDevice = dev;
            stopScanningTarget();
            connect();
        }
    }

    private boolean isTheTarget(BluetoothDevice device, byte[] records) {
        String name = "Electronic Scales";
        final int idx = 19; // the char 'E', we got it by dumping Scan response
        ByteBuffer buf = ByteBuffer.allocate(name.length());
        buf.put(records, idx, buf.limit());
        String target = new String(buf.array());

        if (name.equals(target)) {
            return true;
        }
        return false;
    }

    public void updateValue(int value) {
        Bundle info = new Bundle();
        info.putInt(VALUE_IN_MSG, value);
        mViewHandler.removeMessages(UPDATE_VALUE);
        Message msg = mViewHandler.obtainMessage(UPDATE_VALUE);
        msg.what = UPDATE_VALUE;
        msg.setData(info);
        mViewHandler.sendMessage(msg);
    }

    private void onUpdateValue(int value) {
        double f = (double)value;
        double carry = 10.0; // if we got 102, it means 10.2 kg
        double kg = f / carry;
        double lb = (f * LB_BASE) / carry;
        double st = (f * ST_BASE) / carry;

        mKg.setText(sDF.format(kg));
        mLb.setText(sDF.format(lb));
        mSt.setText(sDF.format(st));
    }

    private void updateValue(final float num) {

        runOnUiThread(new Runnable() {
            public void run() {
                mKg.setText("" + num);
            }
        });
    }

    class ViewHandler extends Handler {
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            if (bundle == null) {
                Log.d("ViewHandler handled a message without information");
                return;
            }

            int tag = msg.what;
            if (tag == UPDATE_VALUE) {
                int value = bundle.getInt(VALUE_IN_MSG, 0);
                onUpdateValue(value);
            } else if (tag == UPDATE_NAME) {
            }
        }
    }

    class GattListener extends GattProxy.ListenerHelper {
        GattListener() {
            super("ActivityWeight");
        }

        @Override
        public void onRetrievedGatt(BluetoothGatt gatt) {
            Log.d(String.format("onRetrievedGatt"));
            mGatt = gatt;
            scanTarget();
        }

        @Override
        public void onScanResult(BluetoothDevice device, int rssi, byte[] scanRecord) {
            Log.d("Scanned:" + device.getAddress());
            if (isTheTarget(device, scanRecord)) {
                onFoundTarget(device, scanRecord);
            }
        }

        @Override
        public void onConnectionStateChange(BluetoothDevice device,
                int status, int newState) {
            if (!mDevice.getAddress().equals(device.getAddress())) {
                // not the device I care about
                return;
            }
            if (newState ==  BluetoothProfile.STATE_CONNECTED) {
                Log.d("connected to device");
                onConnected();
            } else if (newState ==  BluetoothProfile.STATE_DISCONNECTED) {
                onDisconnected();
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGattCharacteristic chrc) {
            Log.d("on chr changed");
            final int index = 4; // got this index from Frontline
            byte[] data = chrc.getValue();
            int value = ((0xFF & data[index]) << 8) + (0xFF & data[index + 1]);
            updateValue(value);
        }


        @Override
        public void onServicesDiscovered(BluetoothDevice device, int status) {
            onDiscovered();
        }
    }
}