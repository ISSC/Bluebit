// vim: et sw=4 sts=4 tabstop=4
package com.issc.ui;

import com.issc.Bluebit;
import com.issc.gatt.Gatt;
import com.issc.gatt.GattCharacteristic;
import com.issc.gatt.GattService;
import com.issc.impl.LeService;
import com.issc.impl.AlgorithmAIO;
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
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.widget.SeekBar;
import android.widget.ToggleButton;

public class ActivityAIO extends Activity
    implements SeekBar.OnSeekBarChangeListener,
    TransactionQueue.Consumer<GattTransaction>,
    AlgorithmAIO.Controllable {

    private BluetoothDevice mDevice;
    private LeService mService;
    private Gatt.Listener mListener;
    private SrvConnection mConn;

    private ProgressDialog mConnectionDialog;
    private ProgressDialog mAutomationDialog;
    protected ViewHandler  mViewHandler;
    protected SeekBar mRed, mGreen, mBlue;
    protected int mRedVal, mGreenVal, mBlueVal;

    private List<GattService> mServices;
    private GattService mServiceAIO;
    private GattCharacteristic mChrDOut;
    private GattCharacteristic mChrCustomAOut1;
    private GattCharacteristic mChrAOut1;
    private GattCharacteristic mChrAOut2;
    private GattCharacteristic mChrAOut3;
    private ToggleButton[] mToggles;

    private TransactionQueue mQueue;

    private final static int NUM = 7;

    private final static int CONNECTION_DIALOG = 1;
    private final static int AUTOMATION_DIALOG = 2;

    private final static int SHOW_CONNECTION_DIALOG     = 0x1000;
    private final static int DISMISS_CONNECTION_DIALOG  = 0x1001;

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

        mDevice = getIntent().getParcelableExtra(Bluebit.CHOSEN_DEVICE);
        mServices = new ArrayList<GattService>();
        mViewHandler = new ViewHandler();
        mListener = new GattListener();

        setToggles();

        mConn = new SrvConnection();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mQueue.clear();
        mViewHandler.removeCallbacksAndMessages(null);
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
        Log.d("automation io activity disconnected, closing");
        mQueue.clear();
        this.setResult(Bluebit.RESULT_REMOTE_DISCONNECT);
        this.finish();
    }

    private void onDiscovered() {
        updateView(DISMISS_CONNECTION_DIALOG, null);
        mServices.clear();
        mServices.addAll(mService.getServices(mDevice));

        mServiceAIO = mService.getService(mDevice, Bluebit.SERVICE_AUTOMATION_IO);
        List<GattCharacteristic> chrs = mServiceAIO.getCharacteristics();
        Iterator<GattCharacteristic> it = chrs.iterator();
        while (it.hasNext()) {
            GattCharacteristic chr = it.next();
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

        GattService proprietary = mService.getService(mDevice, Bluebit.SERVICE_ISSC_PROPRIETARY);
        List<GattCharacteristic> pChrs = proprietary.getCharacteristics();
        it = pChrs.iterator();
        while (it.hasNext()) {
            GattCharacteristic chr = it.next();
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
        onSetDigitalValue();
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

    public void onToggleClicked(View v) {
        onSetDigitalValue();
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

    private void onSetAnalogValue() {
        AlgorithmAIO.ctrlPWM(mRedVal, mGreenVal, mBlueVal, this);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)  {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (seekBar == mRed) {
            mRedVal = seekBar.getProgress();
        } else if (seekBar == mGreen) {
            mGreenVal = seekBar.getProgress();
        } else if (seekBar == mBlue) {
            mBlueVal = seekBar.getProgress();
        }

        onSetAnalogValue();
    }

    /**
     * Change LEDs state according to UI.
     */
    private void onSetDigitalValue() {
        boolean[] leds = new boolean[mToggles.length];
        for (int i = 0; i < mToggles.length; i++) {
            leds[i] = mToggles[i].isChecked();
        }

        AlgorithmAIO.ctrlDigital(leds, this);
    }

    @Override
    public void onControllDigital(byte[] ctrl) {
        GattService srv = mService.getService(mDevice, Bluebit.SERVICE_AUTOMATION_IO);
        GattCharacteristic chr = srv.getCharacteristic(Bluebit.CHR_DIGITAL_OUT);

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
        onSetDigitalValue();
        onSetAnalogValue();
    }

    /**
     * Send message to handler of UI thread.
     */
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

    @Override
    public void onTransact(GattTransaction t) {
        t.chr.setValue(t.value);
        if (t.isWrite) {
            mService.writeCharacteristic(t.chr);
        }
    }

    class SrvConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            /* bind to LeService, retrieve Gatt Profile of the device */
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

    class GattListener extends Gatt.ListenerHelper {

        GattListener() {
            super("ActivityAIO");
        }

        @Override
        public void onConnectionStateChange(Gatt gatt,
                int status, int newState) {
            if (!mDevice.getAddress().equals(gatt.getDevice().getAddress())) {
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
        public void onServicesDiscovered(Gatt gatt, int status) {
            onDiscovered();
        }

        @Override
        public void onCharacteristicRead(Gatt gatt, GattCharacteristic charac, int status) {
            Log.d("read char, uuid=" + charac.getUuid().toString());
            byte[] value = charac.getValue();
            Log.d("get value, byte length:" + value.length);
            for (int i = 0; i < value.length; i++) {
                Log.d("[" + i + "]" + Byte.toString(value[i]));
            }
        }

        @Override
        public void onCharacteristicWrite(Gatt gatt, GattCharacteristic charac, int status) {
            Log.d("on consumed!!");
            mQueue.onConsumed();
        }
    }
}
