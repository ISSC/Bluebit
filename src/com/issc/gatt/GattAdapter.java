// vim: et sw=4 sts=4 tabstop=4
package com.issc.gatt;

import com.issc.Bluebit;
import com.issc.gatt.Gatt.Listener;
import com.issc.util.Log;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;

import java.util.List;

/**
 * Encapsulate Gatt initial procedures.
 */
public interface GattAdapter {

    /**
     * Connect to a device and retrieve Gatt profile from the device.
     *
     * For the reason to be compatible with Samsung, we cannot return Gatt
     * instance directly although that is AOSP way. Instead of that, we return
     * Gatt instance in {@link #onGattReady}.
     */
    public void connectGatt(Context ctx, boolean autoConnect, Listener listener, BluetoothDevice dev);

    public boolean startLeScan();
    public void stopLeScan();

    public List<BluetoothDevice> getConnectedDevices();
    public int getConnectionState(BluetoothDevice device);
}

