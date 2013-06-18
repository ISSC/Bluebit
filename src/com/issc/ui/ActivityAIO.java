// vim: et sw=4 sts=4 tabstop=4
package com.issc.ui;

import com.issc.Bluebit;
import com.issc.data.BLEDevice;
import com.issc.R;
import com.issc.util.Log;

import java.util.ArrayList;
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
import com.samsung.android.sdk.bt.gatt.BluetoothGattService;

public class ActivityAIO extends Activity implements
        BluetoothProfile.ServiceListener {

    private BluetoothDevice mDevice;
    private BluetoothGatt mGatt;
    private BluetoothGattCallback mCallback;

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
        mCallback = new GattCallback();
    }

    @Override
    protected void onActivityResult(int request, int result, Intent data) {
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
        // since there is not a onAppUnregistered, do cancel connection here.
        mGatt.cancelConnection(mDevice);
        BluetoothGattAdapter.closeProfileProxy(BluetoothGattAdapter.GATT,
                mGatt);
    }

    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {
        /*FIXME: this function is deprecated. */
        if (id == CONNECTION_DIALOG) {
            mConnectionDialog = new ProgressDialog(this);
            mConnectionDialog.setMessage(this.getString(R.string.connecting));
            mConnectionDialog.setCancelable(false);
            return mConnectionDialog;
        }
        return null;
    }

    public void onToggleClicked(View v) {
        ToggleButton toggle = (ToggleButton)v;
        Log.d("is checked:" + toggle.isChecked());
    }

    @Override
    public void onServiceConnected(int profile, BluetoothProfile proxy) {
        Log.d("Service connected");
        if (profile == BluetoothGattAdapter.GATT) {
            mGatt = (BluetoothGatt) proxy;
            Log.d("registering the callback");
            mGatt.registerApp(mCallback);
        } else {
            Log.d("Not GATT? How come?");
        }
    }

    @Override
    public void onServiceDisconnected(int profile) {
        if (profile == BluetoothGattAdapter.GATT) {
            mGatt.unregisterApp();
            mGatt = null;
            mServices.clear();
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
                dismissDialog(CONNECTION_DIALOG);
            }
        }
    }

    class GattCallback extends BluetoothGattCallback {
        @Override
        public void onAppRegistered(int status) {
            updateView(SHOW_CONNECTION_DIALOG, null);
            Log.d("callback registered, connecting to device");
            mGatt.connect(mDevice, true);
        }

        @Override
        public void onConnectionStateChange(BluetoothDevice device,
                int status, int newState) {
            if (newState ==  BluetoothProfile.STATE_CONNECTED) {
                List<BluetoothGattService> list = mGatt.getServices(device);
                if ((list == null) || (list.size() == 0)) {
                    mGatt.discoverServices(mDevice);
                } else {
                    updateView(DISMISS_CONNECTION_DIALOG, null);
                    mServices.addAll(mGatt.getServices(device));
                }
            } else if (newState ==  BluetoothProfile.STATE_DISCONNECTED) {
                updateView(SHOW_CONNECTION_DIALOG, null);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothDevice device, int status) {
            updateView(DISMISS_CONNECTION_DIALOG, null);
            mServices.addAll(mGatt.getServices(device));
            Log.d("found services:" + mServices.size());
        }
    }
}
