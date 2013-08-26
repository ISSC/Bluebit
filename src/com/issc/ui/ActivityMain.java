// vim: et sw=4 sts=4 tabstop=4

package com.issc.ui;

import com.issc.Bluebit;
import com.issc.gatt.Gatt;
import com.issc.impl.LeService;
import com.issc.R;
import com.issc.util.Log;
import com.issc.util.Util;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;

public class ActivityMain extends Activity {

    private LeService mService;
    private Gatt.Listener mListener;
    private SrvConnection mConn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mListener = new GattListener();
        mConn = new SrvConnection();

        Intent intent = getIntent();
        boolean fake = intent.getBooleanExtra(Bluebit.USE_FAKE, false);

        Intent launch = new Intent(this, LeService.class);
        launch.putExtra(Bluebit.USE_FAKE, fake);
        startService(launch);
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
        stopService(new Intent(this, LeService.class));
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

    @Override
    protected void onSaveInstanceState(Bundle b) {
        super.onSaveInstanceState(b);
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

    class GattListener extends Gatt.ListenerHelper {
        GattListener() {
            super("ActivityMain");
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
