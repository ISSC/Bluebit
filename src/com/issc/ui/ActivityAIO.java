// vim: et sw=4 sts=4 tabstop=4
package com.issc.ui;

import com.issc.Bluebit;
import com.issc.data.BLEDevice;
import com.issc.impl.GattProxy;
import com.issc.R;
import com.issc.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ToggleButton;

import com.samsung.android.sdk.bt.gatt.BluetoothGatt;
import com.samsung.android.sdk.bt.gatt.BluetoothGattAdapter;
import com.samsung.android.sdk.bt.gatt.BluetoothGattCallback;
import com.samsung.android.sdk.bt.gatt.BluetoothGattCharacteristic;
import com.samsung.android.sdk.bt.gatt.BluetoothGattService;

public class ActivityAIO extends Activity {

    private BluetoothDevice mDevice;
    private BluetoothGatt mGatt;
    private GattProxy.Listener mListener;

    private ProgressDialog mConnectionDialog;
    protected ViewHandler  mViewHandler;

    private List<BluetoothGattService> mServices;

    private final static int CONNECTION_DIALOG = 1;

    private final static int SHOW_CONNECTION_DIALOG     = 0x1000;
    private final static int DISMISS_CONNECTION_DIALOG  = 0x1001;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aio);

        BLEDevice device = getIntent().getParcelableExtra(Bluebit.CHOSEN_DEVICE);
        mDevice = device.getDevice();
        mServices = new ArrayList<BluetoothGattService>();
        mViewHandler = new ViewHandler();
        mListener = new GattListener();
    }

    @Override
    protected void onActivityResult(int request, int result, Intent data) {
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
        /*FIXME: this function is deprecated. */
        if (id == CONNECTION_DIALOG) {
            mConnectionDialog = new ProgressDialog(this);
            mConnectionDialog.setMessage(this.getString(R.string.connecting));
            mConnectionDialog.setCancelable(true);
            return mConnectionDialog;
        }
        return null;
    }

    private final static UUID SERVICE_AUTOMATION_IO = UUID.fromString("00001815-0000-1000-8000-00805f9b34fb");
    private final static UUID CHAR_DI = UUID.fromString("00002a56-0000-1000-8000-00805f9b34fb");
    private final static UUID CHAR_DO = UUID.fromString("00002a57-0000-1000-8000-00805f9b34fb");

    public void onToggleClicked(View v) {
        ToggleButton toggle = (ToggleButton)v;
        Log.d("is checked:" + toggle.isChecked());

        BluetoothGattService srv = mGatt.getService(mDevice, SERVICE_AUTOMATION_IO);
        if (srv == null) {
            Log.d("Get Service failed");
        } else {
            dumpService(srv);
        }
    }

    private void dumpService(BluetoothGattService srv) {
        BluetoothGattCharacteristic ch = srv.getCharacteristic(CHAR_DO);
        if (ch == null) {
            Log.d("get char failed");
        } else {
            byte ctrl = (byte)(int)(Math.random() * 2);
            byte[] value = {(byte)0xfc, (byte)0xfe};
            value[0] += ctrl;
            value[1] += ctrl;
            ch.setValue(value);
            boolean r = mGatt.writeCharacteristic(ch);
            Log.d(String.format("ctrl:%1x,%02x %02x, %b", ctrl, value[0], value[1], r));
        }
    }

    public void updateView(int tag, Bundle info) {
        if (info == null) {
            info = new Bundle();
        }
        mViewHandler.removeMessages(tag);
        Message msg = mViewHandler.obtainMessage(tag);
        msg.what = tag;
        msg.setData(info);
        mViewHandler.sendMessage(msg);
    }

    class ViewHandler extends Handler {
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            if (bundle == null) {
                Log.d("ViewHandler handled a message without information");
                return;
            }

            int tag = msg.what;
            if (tag == SHOW_CONNECTION_DIALOG) {
                showDialog(CONNECTION_DIALOG);
            } else if (tag == DISMISS_CONNECTION_DIALOG) {
                if (mConnectionDialog != null && mConnectionDialog.isShowing()) {
                    dismissDialog(CONNECTION_DIALOG);
                }
            }
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
        updateView(DISMISS_CONNECTION_DIALOG, null);
        mServices.clear();
        mServices.addAll(mGatt.getServices(mDevice));
        Log.d("found services:" + mServices.size());
    }

    class GattListener extends GattProxy.ListenerHelper {

        GattListener() {
            super("ActivityAIO");
        }

        @Override
        public void onRetrievedGatt(BluetoothGatt gatt) {
            Log.d(String.format("onRetrievedGatt"));
            mGatt = gatt;

            int conn = mGatt.getConnectionState(mDevice);
            if (conn == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d("disconnected, connecting to device");
                updateView(SHOW_CONNECTION_DIALOG, null);
                mGatt.connect(mDevice, true);
            } else {
                Log.d("already connected");
                onConnected();
            }
        }

        @Override
        public void onConnectionStateChange(BluetoothDevice device,
                int status, int newState) {
            if (newState ==  BluetoothProfile.STATE_CONNECTED) {
                onConnected();
            } else if (newState ==  BluetoothProfile.STATE_DISCONNECTED) {
                updateView(SHOW_CONNECTION_DIALOG, null);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothDevice device, int status) {
            onDiscovered();
        }

        public void onCharacteristicRead(BluetoothGattCharacteristic charac, int status) {
            Log.d("read char, uuid=" + charac.getUuid().toString());
            byte[] value = charac.getValue();
            Log.d("get value, byte length:" + value.length);
            for (int i = 0; i < value.length; i++) {
                Log.d("[" + i + "]" + Byte.toString(value[i]));
            }
        }
    }
}
