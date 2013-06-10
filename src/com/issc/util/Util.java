// vim: et sw=4 sts=4 tabstop=4
package com.issc.util;

import com.issc.Bluebit;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;

import java.util.Set;

public final class Util {

    private final static String TAG = Bluebit.TAG;

    private Util() {
        // this is just a helper class.
    }

    public static boolean isBluetoothSupported() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        return (adapter!= null);
    }

    public static boolean isBluetoothEnabled() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        return (adapter != null) && (adapter.isEnabled());
    }

    public static void enableBluetooth(Activity act, int code) {
        Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        act.startActivityForResult(i, code);
    }

    public static boolean startDiscovery() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null && adapter.isEnabled()) {
            // according to documentation, it is highly recommendation
            // to cancel ongoing discovery before start new one.
            adapter.cancelDiscovery();
            return adapter.startDiscovery();
        }
        return false;
    }

    public static boolean stopDiscovery() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null && adapter.isEnabled()) {
            return adapter.cancelDiscovery();
        }
        return false;
    }

    public static boolean isDiscovering() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null && adapter.isEnabled()) {
            return adapter.isDiscovering();
        }

        return false;
    }

    public static Set<BluetoothDevice> getBondedDevices() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null && adapter.isEnabled()) {
            return adapter.getBondedDevices();
        }

        return null;
    }
}

