// vim: et sw=4 sts=4 tabstop=4
package com.issc.ui;

import com.issc.Bluebit;
import com.issc.R;
import com.issc.util.Log;
import com.issc.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class ActivityDeviceDetail extends ListActivity {

    private final static String sKey = "key";
    private final static String sVal = "value";

    private ArrayList<Map<String, Object>> mEntries;
    private BaseAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_detail);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (!extras.containsKey(Bluebit.CHOSEN_DEVICE)) {
            finish();
        }

        initAdapter();

        BluetoothDevice bd = intent.getParcelableExtra(Bluebit.CHOSEN_DEVICE);
        init(bd);
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

        setListAdapter(mAdapter);
    }


    public void onClickBtnMore(View v) {
    }

    private void init(BluetoothDevice device) {
        append(getString(R.string.detail_name), device.getName());
        append(getString(R.string.detail_addr), device.getAddress());
        setDeviceClass(device);
    }

    private void setDeviceClass(BluetoothDevice device) {
        BluetoothClass clazz = device.getBluetoothClass();
        if (clazz == null) {
            Log.e("No Bluetooth Class");
            append(getString(R.string.detail_device_class), "None");
        } else {
            int major = clazz.getMajorDeviceClass();
            int res = Util.getDeviceClassRes(major);
            if (res == -1) {
                append(getString(R.string.detail_device_class), "Unknown");
            } else {
                append(getString(R.string.detail_device_class),
                            getString(res));
            }
        }
    }

    private void append(String key, String value) {
        Map<String, Object> entry = new HashMap<String, Object>();
        entry.put(sKey, key);
        entry.put(sVal, value);
        mEntries.add(entry);
        mAdapter.notifyDataSetChanged();
    }
}
