// vim: et sw=4 sts=4 tabstop=4
package com.issc.ui;

import com.issc.Bluebit;
import com.issc.data.BLEDevice;
import com.issc.impl.GattProxy;
import com.issc.impl.GattQueue;
import com.issc.R;
import com.issc.util.Log;
import com.issc.util.Util;

import java.io.IOException;
import java.nio.ByteBuffer;
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
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.samsung.android.sdk.bt.gatt.BluetoothGatt;
import com.samsung.android.sdk.bt.gatt.BluetoothGattAdapter;
import com.samsung.android.sdk.bt.gatt.BluetoothGattCallback;
import com.samsung.android.sdk.bt.gatt.BluetoothGattCharacteristic;
import com.samsung.android.sdk.bt.gatt.BluetoothGattService;

public class ActivityTransparent extends Activity implements
    GattQueue.Consumer {
    private BluetoothDevice mDevice;
    private BluetoothGatt mGatt;
    private GattProxy.Listener mListener;

    private ProgressDialog mConnectionDialog;
    private ProgressDialog mTimerDialog;
    protected ViewHandler  mViewHandler;

    private GattQueue mQueue;

    private final static int PAYLOAD_MAX = 20; // 20 bytes is max?

    private final static int CONNECTION_DIALOG = 1;
    private final static int TIMER_DIALOG      = 2;
    private final static int CHOOSE_FILE = 0x101;

    private final static String INFO_CONTENT = "the_information_body";

    private final static int SHOW_CONNECTION_DIALOG     = 0x1000;
    private final static int DISMISS_CONNECTION_DIALOG  = 0x1001;
    private final static int CONSUME_TRANSACTION        = 0x1002;
    private final static int DISMISS_TIMER_DIALOG       = 0x1003;
    private final static int APPEND_MESSAGE             = 0x1004;

    private TabHost mTabHost;
    private TextView mMsg;
    private EditText mInput;
    private Button   mBtnSend;
    private ToggleButton mToggle;

    private Spinner mSpinnerDelta;
    private Spinner mSpinnerSize;
    private Spinner mSpinnerRepeat;

    private int[] mValueDelta;
    private int[] mValueSize;
    private int[] mValueRepeat;

    private BluetoothGattCharacteristic mTransTx;
    private BluetoothGattCharacteristic mTransRx;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trans);

        mQueue = new GattQueue(this);

        mMsg     = (TextView)findViewById(R.id.trans_msg);
        mInput   = (EditText)findViewById(R.id.trans_input);
        mBtnSend = (Button)findViewById(R.id.trans_btn_send);
        mToggle  = (ToggleButton)findViewById(R.id.trans_type);

        mViewHandler = new ViewHandler();

        mTabHost = (TabHost) findViewById(R.id.tabhost);
        mTabHost.setup();
        addTab(mTabHost, "Tab1", "Raw", R.id.tab_raw);
        addTab(mTabHost, "Tab2", "Timer", R.id.tab_timer);
        addTab(mTabHost, "Tab3", "Echo", R.id.tab_echo);

        mMsg.setMovementMethod(ScrollingMovementMethod.getInstance());
        BLEDevice device = getIntent().getParcelableExtra(Bluebit.CHOSEN_DEVICE);
        mDevice = device.getDevice();

        mListener = new GattListener();
        initSpinners();
    }

    private void initSpinners() {
        Resources res = getResources();

        mSpinnerDelta  = (Spinner)findViewById(R.id.timer_delta);
        mSpinnerSize   = (Spinner)findViewById(R.id.timer_size);
        mSpinnerRepeat = (Spinner)findViewById(R.id.timer_repeat);

        mValueDelta  = res.getIntArray(R.array.delta_value);
        mValueSize   = res.getIntArray(R.array.size_value);
        mValueRepeat = res.getIntArray(R.array.repeat_value);

        initSpinner(R.array.delta_text, mSpinnerDelta);
        initSpinner(R.array.size_text, mSpinnerSize);
        initSpinner(R.array.repeat_text, mSpinnerRepeat);

        mSpinnerDelta.setSelection(2);  // supposed to select 1000ms
        mSpinnerSize.setSelection(19);  // supposed to select 20bytes
        mSpinnerRepeat.setSelection(0); // supposed to select Unlimited
    }

    private void initSpinner(int textArrayId, Spinner spinner) {
        ArrayAdapter<CharSequence> adapter;
        adapter = ArrayAdapter.createFromResource(
                this, textArrayId, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
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

    private void addTab(TabHost host, String tag, CharSequence text, int viewResource) {
        View indicator = getLayoutInflater().inflate(R.layout.tab_indicator, null);
        TextView tv = (TextView)indicator.findViewById(R.id.indicator_text);
        tv.setText(text);

        TabHost.TabSpec spec = host.newTabSpec(tag);
        spec.setIndicator(indicator);
        spec.setContent(viewResource);
        host.addTab(spec);
    }

    public void onClickSend(View v) {
        CharSequence cs = mInput.getText();
        msgSend(cs);
        write(cs);
    }

    public void onClickStartTimer(View v) {
        showDialog(TIMER_DIALOG);
        startTimer();
    }

    public void onClickChoose(View v) {
        /* you should install a file chooser beforehand. */
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("text/plain");
        startActivityForResult(intent, CHOOSE_FILE);
    }

    public void onClickToggle(View v) {
        onSetType(mToggle.isChecked());
    }

    private void onSetType(boolean withResponse) {
        Log.d("set write with response:" + withResponse);
    }

    @Override
    protected void onActivityResult(int request, int result, Intent data) {
        if (request == CHOOSE_FILE) {
            if (result == Activity.RESULT_OK) {
                Uri uri = data.getData();
                String filePath = uri.getPath();
                Log.d("chosen file:" + filePath);
                try {
                    write(Util.readStrFromFile(filePath));
                    msgSend(filePath);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("IO Exception");
                }
            }
        }
    }

    private void msgSend(CharSequence cs) {
        StringBuilder sb = new StringBuilder();
        sb.append("send:");
        sb.append(cs);

        Log.d(sb.toString());
        Bundle msg = new Bundle();
        msg.putCharSequence(INFO_CONTENT, sb);
        updateView(APPEND_MESSAGE, msg);
    }

    private void write(CharSequence cs) {
        byte[] bytes = cs.toString().getBytes();
        ByteBuffer buf = ByteBuffer.allocate(bytes.length);
        while(buf.remaining() != 0) {
            int size = (buf.remaining() > PAYLOAD_MAX) ? PAYLOAD_MAX: buf.remaining();
            byte[] dst = new byte[size];
            buf.get(dst, 0, size);
            mQueue.add(mTransRx, dst);
            mQueue.consume();
        }
    }

    @Override
    public void onTransact(BluetoothGattCharacteristic chr, byte[] value) {
        chr.setValue(value);
        int type = mToggle.isChecked() ?
            BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT:
            BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE;
        chr.setWriteType(type);
        mGatt.writeCharacteristic(chr);
    }

    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {
        /*FIXME: this function is deprecated. */
        if (id == CONNECTION_DIALOG) {
            mConnectionDialog = new ProgressDialog(this);
            mConnectionDialog.setMessage(this.getString(R.string.connecting));
            mConnectionDialog.setCancelable(true);
            return mConnectionDialog;
        } else if (id == TIMER_DIALOG) {
            mTimerDialog = new ProgressDialog(this);
            mTimerDialog.setMessage("Timer is running");
            mTimerDialog.setOnCancelListener(new Dialog.OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                    Log.d("some one canceled me");
                    stopTimer();
                }
            });
            return mTimerDialog;
        }
        return null;
    }

    private void onTimerSend(int count, int size) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < size; i++) {
            sb.append("" + count);
        }

        msgSend(sb);
        write(sb);
    }

    private boolean mRunning;

    private void startTimer() {

        final int delta  = mValueDelta[mSpinnerDelta.getSelectedItemPosition()];
        final int size   = mValueSize[mSpinnerSize.getSelectedItemPosition()];
        final int repeat = mValueRepeat[mSpinnerRepeat.getSelectedItemPosition()];
        mRunning = true;
        Thread runner = new Thread() {
            public void run() {
                int counter = 0;
                try {
                    while(mRunning) {
                        if (repeat != 0 && repeat == counter) {
                            stopTimer();
                        } else {
                            onTimerSend(counter, size);
                            sleep(delta);
                            counter++;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                updateView(DISMISS_TIMER_DIALOG, null);
            }
        };
        runner.start();
    }

    private void stopTimer() {
        mRunning = false;
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
            } else if (tag == DISMISS_TIMER_DIALOG) {
                if (mTimerDialog != null && mTimerDialog.isShowing()) {
                    dismissDialog(TIMER_DIALOG);
                }
            } else if (tag == CONSUME_TRANSACTION) {
                mQueue.consume();
            } else if (tag == APPEND_MESSAGE) {
                CharSequence content = bundle.getCharSequence(INFO_CONTENT);
                if (content != null) {
                    mMsg.append(content);
                    mMsg.append("\n");

                    /*fot automaticaly scrolling to end*/
                    final int amount = mMsg.getLayout().getLineTop(mMsg.getLineCount())
                        - mMsg.getHeight();
                    if (amount > 0) {
                        mMsg.scrollTo(0, amount);
                    }
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

        BluetoothGattService proprietary = mGatt.getService(mDevice, Bluebit.SERVICE_ISSC_PROPRIETARY);
        mTransTx = proprietary.getCharacteristic(Bluebit.CHR_ISSC_TRANS_TX);
        mTransRx = proprietary.getCharacteristic(Bluebit.CHR_ISSC_TRANS_RX);
        Log.d(String.format("found Tx:%b, Rx:%b", mTransTx != null, mTransRx != null));
    }

    class GattListener extends GattProxy.ListenerHelper {

        GattListener() {
            super("ActivityTransparent");
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
            mQueue.consumedOne();
            updateView(CONSUME_TRANSACTION, null);
        }
    }
}
