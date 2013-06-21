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
import android.widget.SeekBar;
import android.widget.ToggleButton;

import com.samsung.android.sdk.bt.gatt.BluetoothGatt;
import com.samsung.android.sdk.bt.gatt.BluetoothGattAdapter;
import com.samsung.android.sdk.bt.gatt.BluetoothGattCallback;
import com.samsung.android.sdk.bt.gatt.BluetoothGattCharacteristic;
import com.samsung.android.sdk.bt.gatt.BluetoothGattService;

public class ActivityAIO extends Activity
    implements SeekBar.OnSeekBarChangeListener {

    private BluetoothDevice mDevice;
    private BluetoothGatt mGatt;
    private GattProxy.Listener mListener;

    private ProgressDialog mConnectionDialog;
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
    private List<Integer> mToggleIds;

    private final static int CONNECTION_DIALOG = 1;

    private final static int SHOW_CONNECTION_DIALOG     = 0x1000;
    private final static int DISMISS_CONNECTION_DIALOG  = 0x1001;

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

        BLEDevice device = getIntent().getParcelableExtra(Bluebit.CHOSEN_DEVICE);
        mDevice = device.getDevice();
        mServices = new ArrayList<BluetoothGattService>();
        mViewHandler = new ViewHandler();
        mListener = new GattListener();

        setToggleIds();
    }

    private void setToggleIds() {
        mToggleIds = new ArrayList<Integer>();
        mToggleIds.add(new Integer(R.id.aio_ctrl_1));
        mToggleIds.add(new Integer(R.id.aio_ctrl_2));
        mToggleIds.add(new Integer(R.id.aio_ctrl_3));
        mToggleIds.add(new Integer(R.id.aio_ctrl_4));
        mToggleIds.add(new Integer(R.id.aio_ctrl_5));
        mToggleIds.add(new Integer(R.id.aio_ctrl_6));
        mToggleIds.add(new Integer(R.id.aio_ctrl_7));
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

    private byte[] getled(int level) {
        int duty = (9 - level)<<4 + 0xf;
        switch(level) {
            case 0:
                duty = 0x9f;
                break;
            case 1:
                duty = 0x8f;
                break;
            case 2:
                duty = 0x7f;
                break;
            case 3:
                duty = 0x6f;
                break;
            case 4:
                duty = 0x5f;
                break;
            case 5:
                duty = 0x4f;
                break;
            case 6:
                duty = 0x3f;
                break;
            case 7:
                duty = 0x2f;
                break;
            default:
                duty = 0x1f;
                break;
        }
        byte[] r = {(byte)duty, (byte)0x00};
        Log.d(String.format("[0x%02x, 0x%02x]",r[0], r[1]));
        return r;
    }

    private void onSetAnalogValue() {
        Log.d(String.format("To set: R=%d, G=%d, B=%d", mRedVal, mGreenVal, mBlueVal));
        byte[] disable = {(byte)0x00, (byte)0xf8, (byte)0x9f, (byte)0x00};
        mChrCustomAOut1.setValue(disable);
        mGatt.writeCharacteristic(mChrCustomAOut1);

        mChrAOut1.setValue(getled(mRedVal));
        mGatt.writeCharacteristic(mChrAOut1);
        mChrAOut1.setValue(getled(mGreenVal));
        mGatt.writeCharacteristic(mChrAOut2);
        mChrAOut3.setValue(getled(mBlueVal));
        mGatt.writeCharacteristic(mChrAOut3);
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
        ToggleButton toggle = (ToggleButton)v;
        int select = mToggleIds.indexOf(v.getId());
        controlDigital(select, toggle.isChecked());
    }

    private void controlDigital(int target, boolean on) {
        Log.d(String.format("Digital: set LED[%d] %b", target, on));

        byte[] value = getLEDControlValue(target, on);
        BluetoothGattService srv = mGatt.getService(mDevice, Bluebit.SERVICE_AUTOMATION_IO);
        BluetoothGattCharacteristic chr = srv.getCharacteristic(Bluebit.CHR_DIGITAL_OUT);
        mChrDOut.setValue(value);
        mGatt.writeCharacteristic(mChrDOut);
    }

    private byte[] getLEDControlValue(int target, boolean on) {
        int idx = INDEX[target];
        int offset = 2; // each LED occupy 2 bits
        int value = 0xFFFF;
        // on=01b off=00b, but we will use XOR later.
        // so, on=10b off=11b
        int ctrl = on ? 0x2 : 0x3;

        // if index = 2 and turning off (ctrl=11b)
        // 11111111b ^ 00110000b -> 11001111b
        value = value ^ (ctrl << (idx * offset));
        byte[] b = new byte[2];
        b[1] = (byte)(value);
        b[0] = (byte)(value>>8);
        return b;
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
