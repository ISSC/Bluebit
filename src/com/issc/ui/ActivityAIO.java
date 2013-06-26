// vim: et sw=4 sts=4 tabstop=4
package com.issc.ui;

import com.issc.Bluebit;
import com.issc.data.BLEDevice;
import com.issc.impl.AlgorithmAIO;
import com.issc.impl.GattProxy;
import com.issc.impl.GattTransaction;
import com.issc.R;
import com.issc.util.Log;
import com.issc.util.TransactionQueue;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.SeekBar;
import android.widget.ToggleButton;

import com.samsung.android.sdk.bt.gatt.BluetoothGatt;
import com.samsung.android.sdk.bt.gatt.BluetoothGattAdapter;
import com.samsung.android.sdk.bt.gatt.BluetoothGattCallback;
import com.samsung.android.sdk.bt.gatt.BluetoothGattCharacteristic;
import com.samsung.android.sdk.bt.gatt.BluetoothGattService;

public class ActivityAIO extends Activity
    implements SeekBar.OnSeekBarChangeListener,
    TransactionQueue.Consumer<GattTransaction>,
    AlgorithmAIO.Controllable {

    private BluetoothDevice mDevice;
    private BluetoothGatt mGatt;
    private GattProxy.Listener mListener;

    private ProgressDialog mConnectionDialog;
    private ProgressDialog mAutomationDialog;
    protected ViewHandler  mViewHandler;
    protected SeekBar mRed, mGreen, mBlue;
    protected int mRedVal, mGreenVal, mBlueVal;

    private List<BluetoothGattService> mServices;
    private BluetoothGattService mServiceAIO;
    private BluetoothGattCharacteristic mChrDOut;
    private BluetoothGattCharacteristic mChrCustomAOut1;
    private BluetoothGattCharacteristic mChrAOut1;
    private BluetoothGattCharacteristic mChrAOut2;
    private BluetoothGattCharacteristic mChrAOut3;
    private ToggleButton[] mToggles;

    private TransactionQueue mQueue;

    private final static int NUM = 7;

    private final static int CONNECTION_DIALOG = 1;
    private final static int AUTOMATION_DIALOG = 2;

    private final static int SHOW_CONNECTION_DIALOG     = 0x1000;
    private final static int DISMISS_CONNECTION_DIALOG  = 0x1001;
    private final static int CONSUME_TRANSACTION        = 0x1002;

    private final int[] INDEX = {
        5, // LED 1
        3, // LED 2
        2, // LED 3
        6, // LED 4
        1, // LED 5
        0, // LED 6
        4  // LED 7
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aio);

        mRed   = (SeekBar) findViewById(R.id.aio_seekbar_r);
        mGreen = (SeekBar) findViewById(R.id.aio_seekbar_g);
        mBlue  = (SeekBar) findViewById(R.id.aio_seekbar_b);
        mRed.setOnSeekBarChangeListener(this);
        mGreen.setOnSeekBarChangeListener(this);
        mBlue.setOnSeekBarChangeListener(this);

        mQueue = new TransactionQueue(this);

        BLEDevice device = getIntent().getParcelableExtra(Bluebit.CHOSEN_DEVICE);
        mDevice = device.getDevice();
        mServices = new ArrayList<BluetoothGattService>();
        mViewHandler = new ViewHandler();
        mListener = new GattListener();

        setToggles();
    }

    private void setToggles() {
        mToggles = new ToggleButton[NUM];
        mToggles[0] = (ToggleButton) findViewById(R.id.aio_ctrl_1);
        mToggles[1] = (ToggleButton) findViewById(R.id.aio_ctrl_2);
        mToggles[2] = (ToggleButton) findViewById(R.id.aio_ctrl_3);
        mToggles[3] = (ToggleButton) findViewById(R.id.aio_ctrl_4);
        mToggles[4] = (ToggleButton) findViewById(R.id.aio_ctrl_5);
        mToggles[5] = (ToggleButton) findViewById(R.id.aio_ctrl_6);
        mToggles[6] = (ToggleButton) findViewById(R.id.aio_ctrl_7);
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
        mQueue.clear();
    }

    public void onClickAutoPattern1(View v) {
        showDialog(AUTOMATION_DIALOG);
        AlgorithmAIO.startAutoPattern1(this);
    }

    public void onClickAutoPattern2(View v) {
        showDialog(AUTOMATION_DIALOG);
        AlgorithmAIO.startAutoPattern2(this);
    }

    public void onClickAutoPattern3(View v) {
        showDialog(AUTOMATION_DIALOG);
        AlgorithmAIO.startAutoPattern3(this);
    }

    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {
        /*FIXME: this function is deprecated. */
        if (id == CONNECTION_DIALOG) {
            mConnectionDialog = new ProgressDialog(this);
            mConnectionDialog.setMessage(this.getString(R.string.connecting));
            mConnectionDialog.setCancelable(true);
            return mConnectionDialog;
        } else if (id == AUTOMATION_DIALOG) {
            mAutomationDialog = new ProgressDialog(this);
            mAutomationDialog.setMessage(this.getString(R.string.running_automation));
            mAutomationDialog.setOnCancelListener(new Dialog.OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                    AlgorithmAIO.stopAutoPattern();
                }
            });
            return mAutomationDialog;
        }
        return null;
    }

    private void onSetAnalogValue() {
        AlgorithmAIO.ctrlPWM(mRedVal, mGreenVal, mBlueVal, this);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)  {
        if (seekBar == mRed) {
            mRedVal = progress;
        } else if (seekBar == mGreen) {
            mGreenVal = progress;
        } else if (seekBar == mBlue) {
            mBlueVal = progress;
        }

        if (fromUser) {
            onSetAnalogValue();
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    public void onToggleClicked(View v) {
        controlDigital();
    }

    @Override
    public void onTransact(GattTransaction t) {
        t.chr.setValue(t.value);
        if (t.isWrite) {
            mGatt.writeCharacteristic(t.chr);
        }
    }

    private void controlDigital() {
        boolean[] leds = new boolean[mToggles.length];
        for (int i = 0; i < mToggles.length; i++) {
            leds[i] = mToggles[i].isChecked();
        }

        AlgorithmAIO.ctrlDigital(leds, this);
    }

    @Override
    public void onControllDigital(byte[] ctrl) {
        BluetoothGattService srv = mGatt.getService(mDevice, Bluebit.SERVICE_AUTOMATION_IO);
        BluetoothGattCharacteristic chr = srv.getCharacteristic(Bluebit.CHR_DIGITAL_OUT);

        GattTransaction t = new GattTransaction(chr, ctrl);
        mQueue.add(t);
    }

    @Override
    public void onControllPWM(int r, int g, int b, byte[][] ctrl) {
        Log.d(String.format("To set: R=%d, G=%d, B=%d", r, g, b));
        GattTransaction t = new GattTransaction(mChrCustomAOut1, ctrl[0], 200);
        Log.d(String.format("desc:0x%02x 0x%02x 0x%02x 0x%02x", ctrl[0][0], ctrl[0][1], ctrl[0][2], ctrl[0][3]));
        mQueue.add(t);
        for (int i = 1; i < ctrl.length; i++) {
            if (r != 0) {
                GattTransaction c = new GattTransaction(mChrAOut1, ctrl[i], 200);
                mQueue.add(c);
                Log.d(String.format("[%d(R)]:0x%02x 0x%02x", i, ctrl[i][0], ctrl[i][1]));
                r = 0;
            } else if (g != 0) {
                GattTransaction c = new GattTransaction(mChrAOut2, ctrl[i], 200);
                mQueue.add(c);
                Log.d(String.format("[%d(G)]:0x%02x 0x%02x", i, ctrl[i][0], ctrl[i][1]));
                g = 0;
            } else if (b != 0) {
                GattTransaction c = new GattTransaction(mChrAOut3, ctrl[i], 200);
                mQueue.add(c);
                Log.d(String.format("[%d(B)]:0x%02x 0x%02x", i, ctrl[i][0], ctrl[i][1]));
                b = 0;
            }
        }
    }

    @Override
    public void onStopControll() {
        Log.d("Stopped automation");
        controlDigital();
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
            } else if (tag == CONSUME_TRANSACTION) {
                // mQueue itself will consume next transaction
                //mQueue.process();
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

        mServiceAIO = mGatt.getService(mDevice, Bluebit.SERVICE_AUTOMATION_IO);
        List<BluetoothGattCharacteristic> chrs = mServiceAIO.getCharacteristics();
        Iterator<BluetoothGattCharacteristic> it = chrs.iterator();
        while (it.hasNext()) {
            BluetoothGattCharacteristic chr = it.next();
            UUID uuid = chr.getUuid();
            if (uuid.equals(Bluebit.CHR_DIGITAL_OUT)) {
                mChrDOut = chr;
            } else if (uuid.equals(Bluebit.CUSTOM_CHR_AO1_DESC)) {
                mChrCustomAOut1 = chr;
            } else if (uuid.equals(Bluebit.CHR_ANALOG_OUT)) {
                // assign characteristic since they have the same UUID. :-(
                if (mChrAOut1 == null) {
                    mChrAOut1 = chr;
                } else if (mChrAOut2 == null) {
                    mChrAOut2 = chr;
                } else if (mChrAOut3 == null) {
                    mChrAOut3 = chr;
                }
            } else {
                Log.d("Char:" + chr.getUuid().toString());
            }
        }

        BluetoothGattService proprietary = mGatt.getService(mDevice, Bluebit.SERVICE_ISSC_PROPRIETARY);
        List<BluetoothGattCharacteristic> pChrs = proprietary.getCharacteristics();
        it = pChrs.iterator();
        while (it.hasNext()) {
            BluetoothGattCharacteristic chr = it.next();
            if (chr.getUuid().equals(Bluebit.CUSTOM_CHR_AO1_DESC)) {
                mChrCustomAOut1 = chr;
            }
        }

        Log.d("found services:" + mServices.size());
        Log.d(String.format("found Characteristic for Desc:%b, Red:%b, Greeb:%b, Blue:%b",
                mChrCustomAOut1 != null,
                mChrAOut1 != null,
                mChrAOut2 != null,
                mChrAOut3 != null));
        controlDigital();
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

        @Override
        public void onCharacteristicRead(BluetoothGattCharacteristic charac, int status) {
            Log.d("read char, uuid=" + charac.getUuid().toString());
            byte[] value = charac.getValue();
            Log.d("get value, byte length:" + value.length);
            for (int i = 0; i < value.length; i++) {
                Log.d("[" + i + "]" + Byte.toString(value[i]));
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGattCharacteristic charac, int status) {
            Log.d("on consumed!!");
            mQueue.onConsumed();
            updateView(CONSUME_TRANSACTION, null);
        }
    }
}
