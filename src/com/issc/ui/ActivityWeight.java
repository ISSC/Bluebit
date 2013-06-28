// vim: et sw=4 sts=4 tabstop=4

package com.issc.ui;

import com.issc.Bluebit;
import com.issc.data.BLEDevice;
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
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.samsung.android.sdk.bt.gatt.BluetoothGatt;
import com.samsung.android.sdk.bt.gatt.BluetoothGattAdapter;
import com.samsung.android.sdk.bt.gatt.BluetoothGattCallback;

public class ActivityWeight extends Activity {

    private BluetoothGatt mGatt;
    private GattProxy.Listener mListener;

    private TextView mKg;
    private TextView mLb;
    private TextView mSt;
    private TextView mName;

    private CharSequence mInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weight);

        mKg = (TextView) findViewById(R.id.kg);
        mLb = (TextView) findViewById(R.id.lb);
        mSt = (TextView) findViewById(R.id.st);
        mName = (TextView) findViewById(R.id.weight_name);
        mListener = new GattListener();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GattProxy proxy = GattProxy.get(ActivityWeight.this);
        proxy.releaseGatt();
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

    public void onClickName(View v) {
        /*TODO: using Dialog is not good, we should improve it */
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Set New Name");

        // Set an EditText view to get user input 
        final EditText input = new EditText(this);
        alert.setView(input);
        input.setText(mName.getText());

        alert.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if(input.getText().length() <= 0) {
                    // input nothing, aborting
                    return;
                }
                onChangingName(input.getText());
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();
    }

    private void onChangingName(CharSequence newName) {
        mName.setText(newName);
    }

    private void startDiscovery() {
        Log.d("Scanning Devices");
        if (mGatt != null) {
            /* connected device will not be ignored when scanning */
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

    class GattListener extends GattProxy.ListenerHelper {
        GattListener() {
            super("ActivityWeight");
        }

        @Override
        public void onRetrievedGatt(BluetoothGatt gatt) {
            Log.d(String.format("onRetrievedGatt"));
            mGatt = gatt;
        }

        @Override
        public void onScanResult(BluetoothDevice device, int rssi, byte[] scanRecord) {
        }
    }
}
