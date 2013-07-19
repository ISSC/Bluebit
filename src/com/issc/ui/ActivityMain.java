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
import android.content.Context;
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

public class ActivityMain extends Activity {

    private BluetoothGatt mGatt;
    private GattProxy.Listener mListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        GattProxy proxy = GattProxy.get(ActivityMain.this);
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

    @Override
    protected void onSaveInstanceState(Bundle b) {
        super.onSaveInstanceState(b);
    }

    //@Override
    //public void onBackPressed() {
    //    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    //    builder.setTitle("Exit");
    //    builder.setMessage("Are you going to exit?");
    //    builder.setPositiveButton(android.R.string.yes,
    //            new DialogInterface.OnClickListener() {
    //                public void onClick(DialogInterface dialog, int whichButton) {
    //                    finish();
    //                }
    //            });
    //    builder.setNegativeButton(android.R.string.no,null);
    //    builder.setCancelable(true);
    //    builder.show();
    //}

    @Override
    protected void onRestoreInstanceState(Bundle b) {
        super.onRestoreInstanceState(b);
    }

    public void onClickScan(View v) {
        if (Util.isBluetoothEnabled()) {
            Intent i = new Intent(this, ActivityDevicesList.class);
            startActivity(i);
        } else {
            Log.d("Trying to enable Bluetooth");
            Util.enableBluetooth(this, 0);
        }

    }

    public void onClickWeight(View v) {
        if (Util.isBluetoothEnabled()) {
            Intent i = new Intent(this, ActivityWeight.class);
            startActivity(i);
        } else {
            Log.d("Trying to enable Bluetooth");
            Util.enableBluetooth(this, 0);
        }
    }

    class GattListener extends GattProxy.ListenerHelper {
        GattListener() {
            super("ActivityMain");
        }

        @Override
        public void onRetrievedGatt(BluetoothGatt gatt) {
            super.onRetrievedGatt(gatt);
            mGatt = gatt;
        }
    }
}
