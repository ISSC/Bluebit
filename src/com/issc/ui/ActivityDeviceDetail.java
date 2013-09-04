// vim: et sw=4 sts=4 tabstop=4
/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.issc.ui;

import com.issc.Bluebit;
import com.issc.gatt.Gatt;
import com.issc.gatt.GattCharacteristic;
import com.issc.gatt.GattDescriptor;
import com.issc.gatt.GattService;
import com.issc.impl.LeService;
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
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.SimpleAdapter;
import android.widget.TextView;

/**
 * List details of a BLE device.
 *
 * Includes its name, address and provided GattServices, GattCharacteristics.
 */
public class ActivityDeviceDetail extends ListActivity {

    private final static String sKey = "key";
    private final static String sVal = "value";

    private BluetoothDevice mDevice;
    private ArrayList<Map<String, Object>> mEntries;
    private SimpleAdapter mAdapter;

    private LeService mService;
    private Gatt.Listener mListener;
    private SrvConnection mConn;

    private final static int DISCOVERY_DIALOG = 1;
    private ProgressDialog mDiscoveringDialog;
    private boolean mDiscovered = false;

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
        mConn = new SrvConnection();

        mDevice = intent.getParcelableExtra(Bluebit.CHOSEN_DEVICE);
        init(mDevice);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindService(new Intent(this, LeService.class), mConn, 0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mService != null) {
            mService.rmListener(mListener);
            mService.disconnect(mDevice);
            mService.closeGatt(mDevice);
        }

        mService = null;
        unbindService(mConn);
    }

    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {
        if (id == DISCOVERY_DIALOG) {
            mDiscoveringDialog = new ProgressDialog(this);
            mDiscoveringDialog.setMessage(this.getString(R.string.scanning));
            mDiscoveringDialog.setOnCancelListener(new Dialog.OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                    // this dialog should be closed, do not need to dismiss again
                    // stopDiscovery();
                }
            });
            return mDiscoveringDialog;
        }
        return null;
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

    private void init(BluetoothDevice device) {
        if (device.getName() == null) {
            append(getString(R.string.detail_name), "");
        } else {
            append(getString(R.string.detail_name), device.getName());
        }
        append(getString(R.string.detail_addr), device.getAddress());
    }

    public void onClickBtnMore(View v) {
        if (mService != null && !mDiscovered) {
            startDiscovery();
        }
    }

    private void startDiscovery() {
        if (mService!= null) {
            showDialog(DISCOVERY_DIALOG);
            if (mService.getConnectionState(mDevice) == BluetoothProfile.STATE_CONNECTED) {
                List<GattService> list = mService.getServices(mDevice);
                if ((list == null) || (list.size() == 0)) {
                    Log.d("start discovery");
                    mService.discoverServices(mDevice);
                } else {
                    onDiscovered(mDevice);
                }
            } else {
                mService.connectGatt(this, false, mDevice);
                mService.connect(mDevice, false);
            }
        }
    }

    private void stopDiscovery() {
        dismissDialog(DISCOVERY_DIALOG);
    }

    private void onDiscovered(BluetoothDevice device) {
        mDiscovered = true;
        Log.d("on discovered");
        if (mService != null) {
            List<GattService> srvs = mService.getServices(device);
            Iterator<GattService> it = srvs.iterator();
            while (it.hasNext()) {
                appendServices(it.next());
            }
        }
    }

    /**
     * To list supported Gatt Services.
     */
    private void appendServices(GattService srv) {
        append(getString(R.string.title_srv), srv.getUuid().toString(), R.color.important);
        List<GattCharacteristic> chars = srv.getCharacteristics();
        Iterator<GattCharacteristic> it = chars.iterator();
        while (it.hasNext()) {
            appendCharacteristic(it.next());
        }
    }

    /**
     * To list supported Gatt Characteristics.
     */
    private void appendCharacteristic(GattCharacteristic ch) {
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

        Iterator<GattDescriptor> it = ch.getDescriptors().iterator();
        while (it.hasNext()) {
            appendDescriptor(it.next());
        }
    }

    /**
     * To list supported Gatt Descriptors.
     */
    private void appendDescriptor(GattDescriptor desc) {
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

    /**
     * Append a row to List with specific style.
     */
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

    class MyBinder implements SimpleAdapter.ViewBinder {
        // By default, SimpleAdapter just invoke toString to fill content of TextView.
        // However, the data might be spannable-string, so use setText instead of
        // using toString to avoid information losing.
        public boolean setViewValue(View view, Object data, String textRepresentation) {
            if (view instanceof TextView) {
                ((TextView)view).setText((CharSequence)data);
            }
            return true;
        }
    }

    class GattListener extends Gatt.ListenerHelper {

        GattListener() {
            super("ActivityDeviceDetail");
        }

        @Override
        public void onServicesDiscovered(Gatt gatt, int status) {
            stopDiscovery();
            onDiscovered(mDevice);
        }

        @Override
        public void onConnectionStateChange(Gatt gatt,
                int status, int newState) {

            if (mService == null) {
                Log.d("There is no Gatt to be used, skip");
                return;
            }

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mService.discoverServices(mDevice);
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            }
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

