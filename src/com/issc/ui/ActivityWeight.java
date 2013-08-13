// vim: et sw=4 sts=4 tabstop=4

package com.issc.ui;

import com.issc.Bluebit;
import com.issc.gatt.Gatt;
import com.issc.gatt.GattCharacteristic;
import com.issc.gatt.GattDescriptor;
import com.issc.gatt.GattService;
import com.issc.impl.LeService;
import com.issc.impl.GattTransaction;
import com.issc.R;
import com.issc.util.Log;
import com.issc.util.Util;
import com.issc.util.TransactionQueue;

import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
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

public class ActivityWeight extends Activity implements
    TransactionQueue.Consumer<GattTransaction> {

    private LeService mService;
    private Gatt.Listener mListener;
    private SrvConnection mConn;

    private final static double LB_BASE = 2.2046; // 1 kg is about 2.2046 lb
    private final static double ST_BASE = 0.1574; // 1 kg is about 0.1574 st

    private final static int UPDATE_VALUE = 0x9527;
    private final static int UPDATE_NAME  = 0x1984;
    private final static int SHOW_LOADER  = 0x2013;
    private final static int HIDE_LOADER  = 0x2014;

    private final static String VALUE_IN_MSG = "value_in_message_instance";
    private final static String NAME_IN_MSG  = "name_message_instance";

    private final static String SEARCH_PATTERN = "Electronic Scales";

    private final static UUID mAdvData = Util.uuidFromStr("FFF0");

    /* use 0xFFF4 descriptor of 0xFFF0 characteristic to enable
     * notification from target */
    private final static UUID mUuidFFF0 = Util.uuidFromStr("FFF0");
    private final static UUID mUuidFFF4 = Util.uuidFromStr("FFF4");

    private BluetoothDevice mDevice;
    private GattService        mFFF0;
    private GattCharacteristic mFFF4;
    private GattDescriptor     mCCC;
    private GattService        mProprietary;
    private GattCharacteristic mAirPatch;

    private TransactionQueue mQueue;

    private TextView mKg;
    private TextView mLb;
    private TextView mSt;
    private TextView mName;
    private View     mLoader;

    private ViewHandler mViewHandler;
    private final static DecimalFormat sDF = new DecimalFormat("0.0");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weight);

        mKg = (TextView) findViewById(R.id.kg);
        mLb = (TextView) findViewById(R.id.lb);
        mSt = (TextView) findViewById(R.id.st);
        mLoader = findViewById(R.id.loader);
        mName = (TextView) findViewById(R.id.weight_name);

        mQueue = new TransactionQueue(this);

        mViewHandler = new ViewHandler();
        mListener = new GattListener();
        mConn = new SrvConnection();
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
        mQueue.clear();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindService(new Intent(this, LeService.class), mConn, 0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopScanningTarget();
        mService.disconnect(mDevice);
        mService.rmListener(mListener);
        mQueue.clear();
        unbindService(mConn);
    }

    public void onClickName(View v) {
        if (!isProprietary()) {
            Toast.makeText(this, "This device does not support changing name",
                    Toast.LENGTH_SHORT).show();
            return;
        }

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
        writeName(newName);
    }

    private void connect() {
        mService.connect(mDevice, false);
    }

    private void scanTarget() {
        mDevice = null;
        if (mService != null) {
            Log.d("Scanning Target");
            mService.startScan();
            updateView(SHOW_LOADER, null);
        } else {
            Log.e("No Gatt instance");
        }
    }

    private void stopScanningTarget() {
        Log.d("Stop scanning");
        updateView(HIDE_LOADER, null);
        if (mService != null) {
            mService.stopScan();
        } else {
            Log.e("No Gatt instance");
        }
    }

    private void onConnected() {
        List<GattService> list = mService.getServices(mDevice);
        if ((list == null) || (list.size() == 0)) {
            Log.d("no services, do discovery");
            mService.discoverServices(mDevice);
        } else {
            onDiscovered();
        }
    }

    private void onDiscovered() {
        Log.d("Discovered services, enable notification");
        mQueue.clear();
        diggServices();
        mService.setCharacteristicNotification(mFFF4, true);
        GattTransaction t = new GattTransaction(mCCC,
                GattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mQueue.add(t);

        enableAirPatch();
    }

    private boolean isProprietary() {
        return (mProprietary != null) && (mAirPatch != null);
    }

    private void enableAirPatch() {
        if (!isProprietary()) {
            return;
        }

        Log.d("proprietary, enabling air patch");
        byte[] enable = {(byte)0x03};
        GattTransaction t = new GattTransaction(mAirPatch, enable);
        mQueue.add(t);
    }

    private void writeName(CharSequence name) {
        if (!isProprietary()) {
            Log.d("not proprietary, do not write name");
            return;
        }
        updateView(SHOW_LOADER, null);
        Log.d("proprietary, write name:" + name);

        final int max = Bluebit.NAME_MAX_SIZE;
        final byte empty = (byte)0x00;
        byte[] nameData = name.toString().getBytes();
        ByteBuffer data = ByteBuffer.allocate(max);
        for (int i = 0; i < data.limit(); i++) {
            if (i >= nameData.length) {
                data.put(empty);
            } else {
                data.put(nameData[i]);
            }
        }

        ByteBuffer e2prom = ByteBuffer.allocate(
                Bluebit.CMD_WRITE_E2PROM.length +
                Bluebit.ADDR_E2PROM_NAME.length +
                1 +  // 1 byte for length
                max);
        e2prom.put(Bluebit.CMD_WRITE_E2PROM);  // write e2prom
        e2prom.put(Bluebit.ADDR_E2PROM_NAME);  // 0x000b -> addr of name on e2prom
        e2prom.put((byte)max);
        e2prom.put(data.array());
        GattTransaction t1 = new GattTransaction(mAirPatch, e2prom.array());
        mQueue.add(t1);

        ByteBuffer memory = ByteBuffer.allocate(
                Bluebit.CMD_WRITE_MEMORY.length +
                Bluebit.ADDR_MEMORY_NAME.length +
                1 +  // 1 byte for length
                max);
        memory.put(Bluebit.CMD_WRITE_MEMORY);  // write memory
        memory.put(Bluebit.ADDR_MEMORY_NAME);  // 0x4e0b -> addr of name on memory
        memory.put((byte)max);
        memory.put(data.array());
        GattTransaction t2 = new GattTransaction(mAirPatch, memory.array());
        mQueue.add(t2);
    }

    @Override
    public void onTransact(GattTransaction t) {
        if (t.isForCharacteristic()) {
            if (t.isWrite) {
                Log.d("gatt writing characteristic");
                t.chr.setValue(t.value);
                mService.writeCharacteristic(t.chr);
            } else {
                t.chr.setValue(t.value);
                boolean r = mService.readCharacteristic(t.chr);
                Log.d("gatt reading characteristic:" + r);
            }
        } else if (t.isForDescriptor()) {
            if (t.isWrite) {
                t.desc.setValue(t.value);
                mService.writeDescriptor(t.desc);
            }
        }
    }

    private void diggServices() {
        mFFF0 = null;
        mFFF4 = null;
        mCCC  = null;
        mProprietary = null;

        List<GattService> list = mService.getServices(mDevice);
        Iterator<GattService> it = list.iterator();
        while(it.hasNext()) {
            GattService srv = it.next();
            if (srv.getUuid().equals(mUuidFFF0)) {
                mFFF0 = srv;
                mFFF4 = mFFF0.getCharacteristic(mUuidFFF4);
                mCCC  = mFFF4.getDescriptor(Bluebit.DES_CLIENT_CHR_CONFIG);
            } else if (srv.getUuid().equals(Bluebit.SERVICE_ISSC_PROPRIETARY)) {
                mProprietary = srv;
                mAirPatch = mProprietary.getCharacteristic(Bluebit.CHR_AIR_PATCH);
            }
        }
    }

    private void onDisconnected() {
        mFFF0 = null;
        mFFF4 = null;
        mCCC = null;
        mProprietary = null;
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
        String response = new String(records);
        if (response.contains(SEARCH_PATTERN)) {
            return true;
        }
        return false;
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

    public void updateValue(int value) {
        Bundle info = new Bundle();
        info.putInt(VALUE_IN_MSG, value);
        updateView(UPDATE_VALUE, info);
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
            } else if (tag == SHOW_LOADER) {
                mLoader.setVisibility(View.VISIBLE);
            } else if (tag == HIDE_LOADER) {
                mLoader.setVisibility(View.INVISIBLE);
            }
        }
    }

    class GattListener extends Gatt.ListenerHelper {
        GattListener() {
            super("ActivityWeight");
        }

        @Override
        public void onGattReady() {
            Log.d(String.format("onRetrievedGatt"));
            scanTarget();
        }

        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
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
        public void onCharacteristicChanged(GattCharacteristic chrc) {
            Log.d("on chr changed");
            if (chrc.getUuid().equals(mFFF4.getUuid())) {
                Log.d("Got Weight value, update view");
                final int index = 4; // got this index from Frontline
                byte[] data = chrc.getValue();
                int value = ((0xFF & data[index]) << 8) + (0xFF & data[index + 1]);
                updateValue(value);
            } else if (chrc.getUuid().equals(mAirPatch.getUuid())){
                Log.d("Got Value update from AirPatch");
                byte[] data = chrc.getValue();
                for (int i = 0; i < data.length; i++) {
                    Log.d(String.format("[%d]: 0x%02x", i, data[i]));
                }

                if (data.length >= 2 && data[1] == Bluebit.CMD_WRITE_MEMORY[0]) {
                    // finished writing memory
                    updateView(HIDE_LOADER, null);
                }
            } else {
                Log.d("Unknown chr:" + chrc.getUuid().toString());
            }
        }

        @Override
        public void onCharacteristicRead(GattCharacteristic charac, int status) {
            Log.d("on chr read:" + status);
            byte[] value = charac.getValue();
            mQueue.onConsumed();
        }

        @Override
        public void onCharacteristicWrite(GattCharacteristic charac, int status) {
            Log.d("on chr write:" + status);
            mQueue.onConsumed();
        }

        @Override
        public void onDescriptorRead(GattDescriptor desc, int status) {
            Log.d("on desc read:" + status);
            mQueue.onConsumed();
        }

        @Override
        public void onDescriptorWrite(GattDescriptor desc, int status) {
            Log.d("on desc write:" + status);
            mQueue.onConsumed();
        }

        @Override
        public void onServicesDiscovered(BluetoothDevice device, int status) {
            onDiscovered();
        }
    }

    class SrvConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mService = ((LeService.LocalBinder)service).getService();
            mService.addListener(mListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.e("Gatt Service disconnected");
        }
    }
}
