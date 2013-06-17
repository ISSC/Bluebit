// vim: et sw=4 sts=4 tabstop=4

package com.issc.ui;

import com.issc.Bluebit;
import com.issc.data.BLEDevice;
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
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
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

import com.samsung.android.sdk.bt.gatt.BluetoothGatt;
import com.samsung.android.sdk.bt.gatt.BluetoothGattAdapter;
import com.samsung.android.sdk.bt.gatt.BluetoothGattCallback;

public class ActivityDevicesList extends Activity implements
        BluetoothProfile.ServiceListener {

    private ListView mListView;
    private Button mBtnScan;
    private BroadcastReceiver mReceiver;

    private ProgressDialog mScanningDialog;

    private BaseAdapter mAdapter;
    private List<Map<String, Object>> mRecords;
    private List<BLEDevice> mDevices;
    private final static String sName = "_name";
    private final static String sAddr = "_address";
    private final static String sSavedDevices = "_devices_info_in_bundle";

    private final static int SCAN_DIALOG = 1;

    private final static int MENU_DETAIL = 0;
    private final static int MENU_CHOOSE = 1;

    private BluetoothGatt mGatt;
    private BluetoothGattCallback mCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices_list);

        mBtnScan = (Button) findViewById(R.id.btn_scan);
        mListView = (ListView) findViewById(R.id.devices_list);
        mReceiver = new ActionReceiver();

        mListView.setOnItemClickListener(new ItemClickListener());
        registerForContextMenu(mListView);

        mCallback = new GattCallback();
        initAdapter();
    }

    private void initAdapter() {
        String[] from = {sName, sAddr};
        int[] to = {R.id.row_title, R.id.row_description};

        mRecords = new ArrayList<Map<String, Object>>();
        mDevices = new ArrayList<BLEDevice>();
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

    @Override
    protected void onResume() {
        super.onResume();
        BluetoothGattAdapter.getProfileProxy(this, this,
                BluetoothGattAdapter.GATT);
    }

    @Override
    protected void onPause() {
        super.onPause();
        BluetoothGattAdapter.closeProfileProxy(BluetoothGattAdapter.GATT,
                mGatt);
    }

    @Override
    protected void onSaveInstanceState(Bundle b) {
        super.onSaveInstanceState(b);
        ArrayList<BLEDevice> devices;
        /* cache scanned results if Activity being rotated */
        if (mDevices.size() > 0) {
            devices = new ArrayList<BLEDevice>(mDevices);
            b.putParcelableArrayList(sSavedDevices, devices);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle b) {
        super.onRestoreInstanceState(b);
        ArrayList<BLEDevice> devices;
        /* restore scanned results if any */
        devices = b.getParcelableArrayList(sSavedDevices);
        if (devices != null) {
            for (int i = 0; i < devices.size(); i++) {
                appendDevice(devices.get(i).getDevice());
            }
        }
    }

    public void onClickBtnScan(View v) {
        if (Util.isBluetoothEnabled()) {
            startDiscovery();
        } else {
            Log.d("Trying to enable Bluetooth");
            Util.enableBluetooth(this, 0);
        }
    }

    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {
        if (id == SCAN_DIALOG) {
            mScanningDialog = new ProgressDialog(this);
            mScanningDialog.setMessage(this.getString(R.string.scanning));
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

    @Override
    public void onCreateContextMenu(ContextMenu menu,
                                        View v,
                                        ContextMenuInfo info) {
        super.onCreateContextMenu(menu, v, info);
        if (v == mListView) {
            menu.setHeaderTitle(R.string.device_menu_title);
            menu.add(0, MENU_DETAIL, Menu.NONE, R.string.device_menu_detail);
            menu.add(0, MENU_CHOOSE, Menu.NONE, R.string.device_menu_choose);
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
            i.putExtra(Bluebit.CHOSEN_DEVICE, mDevices.get(pos));
            startActivity(i);
        } else if (id == MENU_CHOOSE) {
            Intent i = new Intent(this, ActivityFunctionPicker.class);
            i.putExtra(Bluebit.CHOSEN_DEVICE, mDevices.get(pos));
            startActivity(i);
        }
        return true;
    }

    private void startDiscovery() {
        Log.d("Scanning Devices");
        mRecords.clear();
        mDevices.clear();
        showDialog(SCAN_DIALOG);

        mAdapter.notifyDataSetChanged();

        if (mGatt != null) {
            mGatt.startScan();
        } else {
            Log.e("No Gatt instance");
        }
    }

    private void stopDiscovery() {
        Log.d("Stop scanning");
        if (mGatt != null) {
            mGatt.stopScan();
        } else {
            Log.e("No Gatt instance");
        }
    }

    private void appendBondedDevices() {
        Set<BluetoothDevice> bonded = Util.getBondedDevices();
        if (bonded != null) {
            Iterator<BluetoothDevice> it = bonded.iterator();
            while(it.hasNext()) {
                appendDevice(it.next());
            }
        }

    }

    private void onFoundDevice(BluetoothDevice device) {
        if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
            if (isInList(mRecords, device)) {
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
        mRecords.add(record);
        mDevices.add(new BLEDevice(device));
        this.runOnUiThread(new Runnable() {
            public void run() {
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    class ItemClickListener implements OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent,
                                    View view,
                                    int position,
                                    long id) {
            view.showContextMenu();
        }
    }

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

    @Override
    public void onServiceConnected(int profile, BluetoothProfile proxy) {
        if (profile == BluetoothGattAdapter.GATT) {
            mGatt = (BluetoothGatt) proxy;
            mGatt.registerApp(mCallback);
        }
    }

    @Override
    public void onServiceDisconnected(int profile) {
        if (profile == BluetoothGattAdapter.GATT) {
            mGatt.unregisterApp();
            mGatt = null;
        }
    }

    class GattCallback extends BluetoothGattCallback {
        @Override
        public void onScanResult(BluetoothDevice device, int rssi, byte[] scanRecord) {
            onFoundDevice(device);
        }
    }
}
