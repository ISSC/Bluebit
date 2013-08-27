// vim: et sw=4 sts=4 tabstop=4

package com.issc.ui;

import com.issc.Bluebit;
import com.issc.gatt.Gatt;
import com.issc.gatt.GattAdapter;
import com.issc.impl.LeService;
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
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

/**
 * To scan BLE devices around user.
 *
 * The scanned results will be put into a List.
 * We can select one of these devices to discover services of it.
 * Or to get more detail by long-pressing.
 */
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

    /* request code to ask a activity to connect to a device */
    private final static int REQUEST_CONNECT_DEVICE = 0x201;

    private final static int MENU_DETAIL = 0;
    private final static int MENU_CHOOSE = 1;

    private LeService mService;
    private ScanCallback mScanCallback;
    private Gatt.Listener mListener;
    private SrvConnection mConn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices_list);

        mBtnScan = (Button) findViewById(R.id.btn_scan);
        mListView = (ListView) findViewById(R.id.devices_list);

        mListView.setOnItemClickListener(new ItemClickListener());
        registerForContextMenu(mListView);

        mListener = new GattListener();
        mScanCallback = new ScanCallback();
        initAdapter();
        mConn = new SrvConnection();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // once we bound to LeService, register our listener
        bindService(new Intent(this, LeService.class), mConn, 0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // this activity is invisible, remove listener
        mService.rmListener(mListener);
        mService = null;
        unbindService(mConn);
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

    public void onClickBtnScan(View v) {
        startScan();
    }

    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {
        /* To show a loading icon when scanning */
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
            i.putExtra(Bluebit.CHOSEN_DEVICE,
                    (BluetoothDevice)mRecords.get(pos).get(sDevice));
            startActivity(i);
        } else if (id == MENU_CHOOSE) {
            Intent i = new Intent(this, ActivityFunctionPicker.class);
            i.putExtra(Bluebit.CHOSEN_DEVICE,
                    (BluetoothDevice)mRecords.get(pos).get(sDevice));
            startActivityForResult(i, REQUEST_CONNECT_DEVICE);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int request, int result, Intent data) {
        if (request == REQUEST_CONNECT_DEVICE) {
            if (result == Bluebit.RESULT_REMOTE_DISCONNECT) {
                Toast.makeText(this, R.string.disconnected, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startScan() {
        Log.d("Scanning Devices");
        showDialog(SCAN_DIALOG);

        // connected device will be ingored when scanning.
        resetList();
        appendConnectedDevices();
        appendBondDevices();
        mService.startScan(mScanCallback);
    }

    private void stopScan() {
        Log.d("Stop scanning");
        mService.stopScan(mScanCallback);
    }

    private void appendConnectedDevices() {
        this.runOnUiThread(new Runnable() {
            public void run() {
                appendDevices(mService.getConnectedDevices(), "connected");
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * Clear and append bond device to List.
     *
     * We should list bond device although we might not connect to it, so user
     * is able to remove bond.
     */
    private void resetList() {
        this.runOnUiThread(new Runnable() {
            public void run() {
                mRecords.clear();
                mAdapter.notifyDataSetChanged();
            }
        });
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

    /**
     * Append Device to List in UI thread.
     *
     * There is just only one UI thread(main thread) so it guarantee single
     * thread. Only the UI thread could modify List.
     */
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

    /**
     * To check whether a device is already in List.
     *
     * This method should be called in UI thread(main thread) so it does not
     * worry about the List will be modify form another thread.
     */
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
            startActivityForResult(i, REQUEST_CONNECT_DEVICE);

        }
    }

    class ScanCallback implements GattAdapter.LeScanCallback {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            appendDevice(device, "");
        }
    }

    class GattListener extends Gatt.ListenerHelper {
        GattListener() {
            super("ActivityDevicesList");
        }
    }

    class SrvConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mService = ((LeService.LocalBinder)service).getService();
            mService.addListener(mListener);
            resetList();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.e("Gatt Service disconnected");
        }
    }
}
