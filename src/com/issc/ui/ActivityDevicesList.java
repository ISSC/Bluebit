// vim: et sw=4 sts=4 tabstop=4

package com.issc.ui;

import com.issc.util.Log;
import com.issc.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityDevicesList extends Activity {
    private ListView mListView;
    private Button mBtnScan;
    private BroadcastReceiver mReceiver;

    private ProgressDialog mScanningDialog;

    private BaseAdapter mAdapter;
    private List<Map<String, Object>> mDevices;
    private final static String sName = "_name";
    private final static String sAddr = "_address";

    private final static int SCAN_DIALOG = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices_list);

        mBtnScan = (Button) findViewById(R.id.btn_scan);
        mListView = (ListView) findViewById(R.id.devices_list);
        mReceiver = new ActionReceiver();

        initAdapter();
    }

    private void initAdapter() {
        String[] from = {sName, sAddr};
        int[] to = {R.id.row_title, R.id.row_description};

        mDevices = new ArrayList<Map<String, Object>>();
        mAdapter = new SimpleAdapter(
                    this,
                    mDevices,
                    R.layout.row_device,
                    from,
                    to
                );

        mListView.setAdapter(mAdapter);
        mListView.setEmptyView(findViewById(R.id.empty));
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        this.registerReceiver(mReceiver, filter);

    }

    @Override
    public void onStop() {
        super.onStop();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void onClickBtnScan(View v) {
        if (Util.isBluetoothEnabled()) {
            if (Util.isDiscovering()) {
                stopDiscovery();
            } else {
                startDiscovery();
            }
        } else {
            Log.d("Trying to enable Bluetooth");
            Util.enableBluetooth(this, 0);
        }
    }

    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {
        if (id == SCAN_DIALOG) {
            mScanningDialog = new ProgressDialog(this);
            mScanningDialog.setMessage(this.getString(R.string.text_scanning));
            mScanningDialog.setOnCancelListener(new Dialog.OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                    stopDiscovery();
                }
            });
            return mScanningDialog;
        }
        return null;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
        if (id == SCAN_DIALOG) {
        }
    }

    private void startDiscovery() {
        Log.d("Scanning Devices");
        mDevices.clear();
        mAdapter.notifyDataSetChanged();
        showDialog(SCAN_DIALOG);
        Util.startDiscovery();
    }

    private void stopDiscovery() {
        Log.d("Stop scanning");
        Util.stopDiscovery();
    }

    private void onFoundDevice(BluetoothDevice device) {
        if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
            if (isInList(mDevices, device)) {
                Log.d(device.getName() + " already be in list, skip it");
            } else {
                appendDevice(device);
            }
        }
    }

    private boolean isInList(List<Map<String, Object>> list, BluetoothDevice device) {
        synchronized(list) {
            Iterator<Map<String, Object>> it = list.iterator();
            while(it.hasNext()) {
                Map<String, Object> item = it.next();
                if (item.get(sAddr).toString().equals(device.getAddress())) {
                    return true;
                }
            }

            return false;
        }
    }

    private void appendDevice(BluetoothDevice device) {
        Map<String, Object> record = new HashMap<String, Object>();
        record.put(sName, device.getName());
        record.put(sAddr, device.getAddress());
        mDevices.add(record);
        mAdapter.notifyDataSetChanged();
    }

    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        }
    };

    class ActionReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                onFoundDevice(device);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                mScanningDialog.cancel();
            }
        }
    }
}
