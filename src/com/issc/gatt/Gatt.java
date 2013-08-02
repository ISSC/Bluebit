// vim: et sw=4 sts=4 tabstop=4
package com.issc.gatt;

import com.issc.Bluebit;
import com.issc.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.bluetooth.BluetoothDevice;

import com.samsung.android.sdk.bt.gatt.BluetoothGatt;
import com.samsung.android.sdk.bt.gatt.BluetoothGattCallback;
import com.samsung.android.sdk.bt.gatt.BluetoothGattCharacteristic;
import com.samsung.android.sdk.bt.gatt.BluetoothGattDescriptor;
import com.samsung.android.sdk.bt.gatt.BluetoothGattService;

/**
 * This is a wrapper.
 *
 * It will be an interface to help us to avoid depending on any specific
 * platform.
 **/
public class Gatt {

    public final static int GATT_SUCCESS = BluetoothGatt.GATT_SUCCESS;

    private BluetoothGatt mGatt;

    public Gatt(BluetoothGatt gatt) {
        mGatt = gatt;
    }

    public BluetoothGatt getGatt() {
        return mGatt;
    }

    public void cancelConnection(BluetoothDevice device) {
        mGatt.cancelConnection(device);
    }

    public boolean connect(BluetoothDevice device, boolean auto) {
        return mGatt.connect(device, auto);
    }

    public List<BluetoothDevice> getConnectedDevices() {
        return mGatt.getConnectedDevices();
    }

    public boolean discoverServices(BluetoothDevice device) {
        return mGatt.discoverServices(device);
    }

    public int getConnectionState(BluetoothDevice device) {
        return mGatt.getConnectionState(device);
    }

    public GattService getService(BluetoothDevice device, UUID uuid) {
        return new GattService(mGatt.getService(device, uuid));
    }

    public List<GattService> getServices(BluetoothDevice device) {
        List<BluetoothGattService> srvs = mGatt.getServices(device);
        ArrayList<GattService> list = new ArrayList<GattService>();
        for (BluetoothGattService srv: srvs) {
            list.add(new GattService(srv));
        }

        return list;
    }

    public boolean isBLEDevice(BluetoothDevice device) {
        return mGatt.isBLEDevice(device);
    }

    public boolean readCharacteristic(GattCharacteristic chr) {
        return mGatt.readCharacteristic(chr.getCharacteristic());
    }

    public boolean readDescriptor(GattDescriptor dsc) {
        return mGatt.readDescriptor(dsc.getDescriptor());
    }

    public boolean registerApp(BluetoothGattCallback callback) {
        return mGatt.registerApp(callback);
    }

    public boolean removeBond(BluetoothDevice device) {
        return mGatt.removeBond(device);
    }

    public boolean setCharacteristicNotification(GattCharacteristic chr, boolean enable) {
        return mGatt.setCharacteristicNotification(chr.getCharacteristic(), enable);
    }

    public boolean startScan() {
        return mGatt.startScan();
    }

    public void stopScan() {
        mGatt.stopScan();
    }

    public void unregisterApp() {
        mGatt.unregisterApp();
    }

    public boolean writeCharacteristic(GattCharacteristic chr) {
        return mGatt.writeCharacteristic(chr.getCharacteristic());
    }

    public boolean writeDescriptor(GattDescriptor dsc) {
        return mGatt.writeDescriptor(dsc.getDescriptor());
    }
}

