// vim: et sw=4 sts=4 tabstop=4
package com.issc.ui;

import com.issc.Bluebit;
import com.issc.R;
import com.issc.util.Log;
import com.issc.util.Util;

import android.app.Activity;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.view.View;
import android.widget.TextView;

public class ActivityDeviceDetail extends Activity {
    TextView mName;
    TextView mAddr;
    TextView mDeviceClass;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_detail);

        mName  = (TextView) findViewById(R.id.detail_name);
        mAddr  = (TextView) findViewById(R.id.detail_addr);
        mDeviceClass = (TextView) findViewById(R.id.detail_device_class);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (!extras.containsKey(Bluebit.CHOSEN_DEVICE)) {
            finish();
        }

        BluetoothDevice bd = intent.getParcelableExtra(Bluebit.CHOSEN_DEVICE);
        init(bd);
    }

    private void init(BluetoothDevice device) {
        mName.setText(device.getName());
        mAddr.setText(device.getAddress());
        setDeviceClass(device);
    }

    private void setDeviceClass(BluetoothDevice device) {
        BluetoothClass clazz = device.getBluetoothClass();
        if (clazz == null) {
            Log.e("No Bluetooth Class");
            mDeviceClass.setText("None");
        } else {
            int major = clazz.getMajorDeviceClass();
            int res = Util.getDeviceClassRes(major);
            if (res == -1) {
                mDeviceClass.setText("Unknown");
            } else {
                mDeviceClass.setText(res);
            }
        }
    }
}
