// vim: et sw=4 sts=4 tabstop=4
package com.issc.ui;

import com.issc.Bluebit;
import com.issc.data.BLEDevice;
import com.issc.R;
import com.issc.util.Log;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ToggleButton;

public class ActivityAIO extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aio);
    }

    public void onClickBtn(View v) {
        Intent i = new Intent(this, ActivityDevicesList.class);
        startActivityForResult(i, Bluebit.REQ_CHOOSE_DEVICE);
    }

    @Override
    protected void onActivityResult(int request, int result, Intent data) {
        if (request == Bluebit.REQ_CHOOSE_DEVICE) {
            if (result == Activity.RESULT_OK) {
                BLEDevice bd = data.getParcelableExtra(Bluebit.CHOSEN_DEVICE);
                Log.d("Chosen the device:" + bd.getDevice().getName());
            }
        }
    }

    public void onToggleClicked(View v) {
        ToggleButton toggle = (ToggleButton)v;
        Log.d("is checked:" + toggle.isChecked());
    }
}
