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
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
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

import com.issc.gatt.Gatt;

import com.samsung.android.sdk.bt.gatt.BluetoothGattAdapter;
import com.samsung.android.sdk.bt.gatt.BluetoothGattCallback;

public class ActivityDevicesList extends Activity {

    private ListView mListView;
    private Button mBtnScan;

    private ProgressDialog mScanningDialog;

    private BaseAdapter mAdapter;
    private List<Map<String, Object>> mRecords;
    private final static String sName = "_name";
    private final static String sAddr = "_address";
    private final static String sExtra = "_come_from";
    private final static String sDevice = "_bluetooth_device";

    private final static int SCAN_DIALOG = 1;

    private final static int MENU_DETAIL = 0;
    private final static int MENU_CHOOSE = 1;
    private final static int MENU_RMBOND = 2;

    private Gatt mGatt;
    private GattProxy.Listener mListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices_list);

        mBtnScan = (Button) findViewById(R.id.btn_scan);
        mListView = (ListView) findViewById(R.id.devices_list);

        mListView.setOnItemClickListener(new ItemClickListener());
        registerForContextMenu(mListView);

        mListener = new GattListener();
        initAdapter();
    }

    private void initAdapter() {
        String[] from = {sName, sAddr, sExtra};
        int[] to = {R.id.row_title, R.id.row_description, R.id.row_extra};

        mRecords = new ArrayList<Map<String, Object>>();
        mAdapter = new SimpleAdapter(
                    this,
                    mRecords,
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

    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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

    public void onClickBtnScan(View v) {
        startScan();
    }

    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {
        if (id == SCAN_DIALOG) {
            mScanningDialog = new ProgressDialog(this);
            mScanningDialog.setMessage(this.getString(R.string.scanning));
            mScanningDialog.setOnCancelListener(new Dialog.OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                    stopScan();
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

    @Override
    public void onCreateContextMenu(ContextMenu menu,
                                        View v,
                                        ContextMenuInfo info) {
        super.onCreateContextMenu(menu, v, info);
        if (v == mListView) {
            menu.setHeaderTitle(R.string.device_menu_title);
            menu.add(0, MENU_DETAIL, Menu.NONE, R.string.device_menu_detail);
            menu.add(0, MENU_CHOOSE, Menu.NONE, R.string.device_menu_choose);
            menu.add(0, MENU_RMBOND, Menu.NONE, "Remove bond");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info =
            (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int pos = info.position;
        int id = item.getItemId();
        if (id == MENU_DETAIL) {
            Intent i = new Intent(this, ActivityDeviceDetail.class);
            i.putExtra(Bluebit.CHOSEN_DEVICE, (BluetoothDevice)mRecords.get(pos).get(sDevice));
            startActivity(i);
        } else if (id == MENU_CHOOSE) {
            Intent i = new Intent(this, ActivityFunctionPicker.class);
            i.putExtra(Bluebit.CHOSEN_DEVICE, (BluetoothDevice)mRecords.get(pos).get(sDevice));
            startActivity(i);
        } else if (id == MENU_RMBOND) {
            BluetoothDevice target = (BluetoothDevice)mRecords.get(pos).get(sDevice);
            boolean r = mGatt.removeBond(target);
            Log.d("Remove bond:" + r);
            resetList();
        }
        return true;
    }

    private void startScan() {
        Log.d("Scanning Devices");
        showDialog(SCAN_DIALOG);

        if (mGatt != null) {
            /* connected device will not be ignored when scanning */
            resetList();
            appendConnectedDevices();
            mGatt.startScan();
        } else {
            Log.e("No Gatt instance");
        }
    }

    private void appendConnectedDevices() {
        this.runOnUiThread(new Runnable() {
            public void run() {
                appendDevices(mGatt.getConnectedDevices(), "connected");
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    private void resetList() {
        this.runOnUiThread(new Runnable() {
            public void run() {
                mRecords.clear();
                appendBondDevices();
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    private void stopScan() {
        Log.d("Stop scanning");
        if (mGatt != null) {
            mGatt.stopScan();
        } else {
            Log.e("No Gatt instance");
        }
    }

    private void appendBondDevices() {
        Set<BluetoothDevice> bonded = Util.getBondedDevices();
        if (bonded != null) {
            Iterator<BluetoothDevice> it = bonded.iterator();
            while(it.hasNext()) {
                BluetoothDevice device = it.next();
                Log.d("Bonded device:" + device.getName() + ", " + device.getAddress());
                appendDevice(device, "bonded");
            }
        }

    }

    private void appendDevices(Iterable<BluetoothDevice> bonded, String type) {
        if (bonded == null) {
            return;
        }

        Iterator<BluetoothDevice> it = bonded.iterator();
        while(it.hasNext()) {
            appendDevice(it.next(), type);
        }
    }

    private void appendDevice(final BluetoothDevice device, final String type) {
        this.runOnUiThread(new Runnable() {
            public void run() {
                uiAppendDevice(device, type);
            }
        });
    }

    /* There is just only one UI thread so it guarantee single thread. */
    private void uiAppendDevice(BluetoothDevice device, String type) {
        if (uiIsInList(device)) {
            return;
        }

        final Map<String, Object> record = new HashMap<String, Object>();
        record.put(sName, device.getName());
        record.put(sAddr, device.getAddress());
        record.put(sDevice, device);
        record.put(sExtra, type);
        mRecords.add(record);
        mAdapter.notifyDataSetChanged();
    }

    private boolean uiIsInList(BluetoothDevice device) {
        Iterator<Map<String, Object>> it = mRecords.iterator();
        while(it.hasNext()) {
            Map<String, Object> item = it.next();
            if (item.get(sAddr).toString().equals(device.getAddress())) {
                return true;
            }
        }

        return false;
    }

    class ItemClickListener implements OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent,
                                    View view,
                                    int position,
                                    long id) {
            Intent i = new Intent(ActivityDevicesList.this, ActivityFunctionPicker.class);
            i.putExtra(Bluebit.CHOSEN_DEVICE, (BluetoothDevice)mRecords.get(position).get(sDevice));
            startActivity(i);

        }
    }

    class GattListener extends GattProxy.ListenerHelper {
        GattListener() {
            super("ActivityDevicesList");
        }

        @Override
        public void onRetrievedGatt(Gatt gatt) {
            Log.d(String.format("onRetrievedGatt"));
            mGatt = gatt;
            resetList();
        }

        @Override
        public void onScanResult(BluetoothDevice device, int rssi, byte[] scanRecord) {
            appendDevice(device, "");
        }
    }
}
