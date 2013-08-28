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
import java.util.List;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.View;

public class ActivityKeyboard extends Activity implements
    TransactionQueue.Consumer<GattTransaction> {

    private LeService mService;
    private BluetoothDevice mDevice;
    private Gatt.Listener mListener;
    private SrvConnection mConn;

    private TransactionQueue mQueue;

    private final static int PAYLOAD_MAX = 20; // 90 bytes might be max

    private final static byte[] ESC   = {(byte)01};
    private final static byte[] ENTER = {(byte)28};
    private final static byte[] SPACE = {(byte)57};
    private final static byte[] HOME  = {(byte)102};
    private final static byte[] END   = {(byte)107};
    private final static byte[] UP    = {(byte)103};
    private final static byte[] LEFT  = {(byte)105};
    private final static byte[] RIGHT = {(byte)106};
    private final static byte[] DOWN  = {(byte)108};

    private final static String INFO_CONTENT = "the_information_body";
    private final static String ECHO_ENABLED = "echo_function_is_enabled";

    private GattCharacteristic mTransTx;
    private GattCharacteristic mTransRx;

    private int mSuccess = 0;
    private int mFail    = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keyboard);

        mQueue = new TransactionQueue(this);
        mDevice = getIntent().getParcelableExtra(Bluebit.CHOSEN_DEVICE);
        mListener = new GattListener();
        mConn = new SrvConnection();
    }

    @Override
    public void onDestroy() {
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
        mQueue.clear();
        mService.rmListener(mListener);
        mService = null;
        unbindService(mConn);
    }

    @Override
    public boolean onKeyDown(int code, KeyEvent event) {
        /* to prevent really volume control */
        if (code == KeyEvent.KEYCODE_VOLUME_UP) {
            return true;
        } else if (code == KeyEvent.KEYCODE_VOLUME_DOWN) {
            return true;
        }

        return super.onKeyDown(code, event);
    }

    @Override
    public boolean onKeyUp(int code, KeyEvent event) {
        if (code == KeyEvent.KEYCODE_VOLUME_UP) {
            write(UP);
            return true;
        } else if (code == KeyEvent.KEYCODE_VOLUME_DOWN) {
            write(DOWN);
            return true;
        }

        return super.onKeyUp(code, event);
    }

    public void onClickEsc(View v) {
        write(ESC);
    }

    public void onClickEnter(View v) {
        write(ENTER);
    }

    public void onClickSpace(View v) {
        write(SPACE);
    }

    public void onClickHome(View v) {
        write(HOME);
    }

    public void onClickEnd(View v) {
        write(END);
    }

    public void onClickUp(View v) {
        write(UP);
    }

    public void onClickLeft(View v) {
        write(LEFT);
    }

    public void onClickRight(View v) {
        write(RIGHT);
    }

    public void onClickDown(View v) {
        write(DOWN);
    }

    /**
     * Write data to remote device.
     */
    private void write(byte[] bytes) {
        ByteBuffer buf = ByteBuffer.allocate(bytes.length);
        buf.put(bytes);
        buf.position(0);
        while(buf.remaining() != 0) {
            int size = (buf.remaining() > PAYLOAD_MAX) ? PAYLOAD_MAX: buf.remaining();
            byte[] dst = new byte[size];
            buf.get(dst, 0, size);
            GattTransaction t = new GattTransaction(mTransRx, dst);
            mQueue.add(t);
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

    private void onDisconnected() {
        Log.d("keyboard activity disconnected, closing");
        mQueue.clear();
        this.setResult(Bluebit.RESULT_REMOTE_DISCONNECT);
        this.finish();
    }

    private void onDiscovered() {
        GattService proprietary = mService.getService(mDevice, Bluebit.SERVICE_ISSC_PROPRIETARY);
        mTransTx = proprietary.getCharacteristic(Bluebit.CHR_ISSC_TRANS_TX);
        mTransRx = proprietary.getCharacteristic(Bluebit.CHR_ISSC_TRANS_RX);
        Log.d(String.format("found Tx:%b, Rx:%b", mTransTx != null, mTransRx != null));
    }

    @Override
    public void onTransact(GattTransaction t) {
        t.chr.setValue(t.value);
        if (t.isWrite) {
            mService.writeCharacteristic(t.chr);
        } else {
            mService.readCharacteristic(t.chr);
        }
    }

    class GattListener extends Gatt.ListenerHelper {

        GattListener() {
            super("ActivityTransparent");
        }

        @Override
        public void onConnectionStateChange(Gatt gatt, int status, int newState) {
            if (!mDevice.getAddress().equals(gatt.getDevice().getAddress())) {
                // not the device I care about
                return;
            }

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                onConnected();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                onDisconnected();
            }
        }

        @Override
        public void onServicesDiscovered(Gatt gatt, int status) {
            onDiscovered();
        }

        @Override
        public void onCharacteristicWrite(Gatt gatt, GattCharacteristic charac, int status) {
            if (status == Gatt.GATT_SUCCESS) {
                Log.d("sent event successful");
            } else {
                Log.d("sent event fail");
            }
            mQueue.onConsumed();
        }
    }

    class SrvConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mService = ((LeService.LocalBinder)service).getService();
            mService.addListener(mListener);

            int conn = mService.getConnectionState(mDevice);
            if (conn == BluetoothProfile.STATE_DISCONNECTED) {
                onDisconnected();
            } else {
                Log.d("already connected");
                onConnected();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.e("Gatt Service disconnected");
        }
    }
}
