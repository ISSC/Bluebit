// vim: et sw=4 sts=4 tabstop=4
package com.issc.ui;

import com.issc.Bluebit;
import com.issc.impl.GattProxy;
import com.issc.R;
import com.issc.util.Log;
import com.issc.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.issc.gatt.Gatt;

import com.samsung.android.sdk.bt.gatt.BluetoothGattAdapter;
import com.samsung.android.sdk.bt.gatt.BluetoothGattCharacteristic;
import com.samsung.android.sdk.bt.gatt.BluetoothGattDescriptor;
import com.samsung.android.sdk.bt.gatt.BluetoothGattService;

public class ActivityDeviceDetail extends ListActivity {

    private final static String sKey = "key";
    private final static String sVal = "value";

    private BluetoothDevice mDevice;
    private ArrayList<Map<String, Object>> mEntries;
    private SimpleAdapter mAdapter;

    private Gatt mGatt;
    private GattProxy.Listener mListener;

    private final static int DISCOVERY_DIALOG = 1;
    private ProgressDialog mDiscoveringDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_detail);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (!extras.containsKey(Bluebit.CHOSEN_DEVICE)) {
            finish();
        }

        mListener = new GattListener();
        initAdapter();

        mDevice = intent.getParcelableExtra(Bluebit.CHOSEN_DEVICE);
        init(mDevice);
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
            mDiscoveringDialog.setMessage(this.getString(R.string.scanning));
            mDiscoveringDialog.setOnCancelListener(new Dialog.OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                    stopDiscovery();
                }
            });
            return mDiscoveringDialog;
        }
        return null;
    }

    private void startDiscovery() {
        if (mGatt != null) {
            showDialog(DISCOVERY_DIALOG);
            if (mGatt.getConnectionState(mDevice) == BluetoothProfile.STATE_CONNECTED) {
                List<BluetoothGattService> list = mGatt.getServices(mDevice);
                if ((list == null) || (list.size() == 0)) {
                    Log.d("start discovery");
                    mGatt.discoverServices(mDevice);
                } else {
                    onDiscovered(mDevice);
                }
            } else {
                mGatt.connect(mDevice, false);
            }
        }

    }

    private void stopDiscovery() {
        dismissDialog(DISCOVERY_DIALOG);
        if (mGatt != null) {
            mGatt.cancelConnection(mDevice);
        }
    }

    private void initAdapter() {
        String[] from = {sKey, sVal};
        int[] to = {R.id.row_title, R.id.row_description};

        mEntries = new ArrayList<Map<String, Object>>();
        mAdapter = new SimpleAdapter(
                    this,
                    mEntries,
                    R.layout.row_detail,
                    from,
                    to
                );

        mAdapter.setViewBinder(new MyBinder());
        setListAdapter(mAdapter);
    }


    public void onClickBtnMore(View v) {
        if (mGatt != null) {
            startDiscovery();
        }
    }

    private void init(BluetoothDevice device) {
        append(getString(R.string.detail_name), device.getName());
        append(getString(R.string.detail_addr), device.getAddress());
    }

    private void append(String key, String value) {
        append(key, value, R.color.black);
    }

    private void append(String key, String value, int res) {
        int color = getResources().getColor(res);
        SpannableString span = new SpannableString(value);
        span.setSpan(new ForegroundColorSpan(color),
                0,
                value.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        final Map<String, Object> entry = new HashMap<String, Object>();
        entry.put(sKey, key);
        entry.put(sVal, span);

        runOnUiThread(new Runnable() {
            public void run() {
                mEntries.add(entry);
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    private void onDiscovered(BluetoothDevice device) {
        Log.d("on discovered:");
        if (mGatt != null) {
            List<BluetoothGattService> srvs = mGatt.getServices(device);
            Iterator<BluetoothGattService> it = srvs.iterator();
            while (it.hasNext()) {
                appendServices(it.next());
            }
        }
    }

    private void appendServices(BluetoothGattService srv) {
        append(getString(R.string.title_srv), srv.getUuid().toString(), R.color.important);
        List<BluetoothGattCharacteristic> chars = srv.getCharacteristics();
        Iterator<BluetoothGattCharacteristic> it = chars.iterator();
        while (it.hasNext()) {
            appendCharacteristic(it.next());
        }
    }

    private void appendCharacteristic(BluetoothGattCharacteristic ch) {
        StringBuilder sb = new StringBuilder();
        sb.append(ch.getUuid().toString());
        byte[] value = ch.getValue();
        if (value != null) {
            sb.append("(");
            for (int i = 0; i < value.length; i++) {
                sb.append(String.format(" 0x%02x", value[i]));
            }
            sb.append(")");
        }
        append(getString(R.string.title_chr), sb.toString(), R.color.normal);

        Iterator<BluetoothGattDescriptor> it = ch.getDescriptors().iterator();
        while (it.hasNext()) {
            appendDescriptor(it.next());
        }
    }

    private void appendDescriptor(BluetoothGattDescriptor desc) {
        StringBuilder sb = new StringBuilder();
        sb.append(desc.getUuid().toString());
        byte[] value = desc.getValue();
        if (value != null) {
            sb.append("(");
            for (int i = 0; i < value.length; i++) {
                sb.append(String.format(" 0x%02x", value[i]));
            }
            sb.append(")");
        }
        append(getString(R.string.title_dsc), sb.toString(), R.color.trivial);
    }

    class MyBinder implements SimpleAdapter.ViewBinder {
        public boolean setViewValue(View view, Object data, String textRepresentation) {
            if (view instanceof TextView) {
                ((TextView)view).setText((CharSequence)data);
            }
            return true;
        }
    }

    class GattListener extends GattProxy.ListenerHelper {

        GattListener() {
            super("ActivityDeviceDetail");
        }

        @Override
        public void onRetrievedGatt(Gatt gatt) {
            Log.d(String.format("onRetrievedGatt"));
            mGatt = gatt;
        }

        @Override
        public void onServicesDiscovered(BluetoothDevice device, int status) {
            stopDiscovery();
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
                mGatt.discoverServices(mDevice);
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            }
        }
    }
}

