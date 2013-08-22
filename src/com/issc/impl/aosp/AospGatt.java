// vim: et sw=4 sts=4 tabstop=4
package com.issc.impl.aosp;

import com.issc.Bluebit;
import com.issc.gatt.Gatt;
import com.issc.gatt.Gatt.Listener;
import com.issc.gatt.GattCharacteristic;
import com.issc.gatt.GattDescriptor;
import com.issc.gatt.GattService;
import com.issc.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;

public class AospGatt implements Gatt {

    private BluetoothGatt mGatt;
    private BluetoothDevice mDevice;
    private Listener mListener;

    public AospGatt(BluetoothGatt gatt) {
        mGatt = gatt;
    }

    @Override
    public void close() {
        mGatt.close();
    }

    @Override
    public boolean connect() {
        return mGatt.connect();
    }

    @Override
    public void disconnect() {
        mGatt.disconnect();
    }

    @Override
    public boolean discoverServices() {
        return mGatt.discoverServices();
    }

    @Override
    public BluetoothDevice getDevice() {
        return mGatt.getDevice();
    }

    @Override
    public GattService getService(UUID uuid) {
        return new AospGattService(mGatt.getService(uuid));
    }

    @Override
    public List<GattService> getServices() {
        List<BluetoothGattService> srvs = mGatt.getServices();
        ArrayList<GattService> list = new ArrayList<GattService>();
        for (BluetoothGattService srv: srvs) {
            list.add(new AospGattService(srv));
        }

        return list;
    }

    @Override
    public boolean readCharacteristic(GattCharacteristic chr) {
        return mGatt.readCharacteristic(
                (BluetoothGattCharacteristic)chr.getImpl());
    }

    @Override
    public boolean readDescriptor(GattDescriptor dsc) {
        return mGatt.readDescriptor((BluetoothGattDescriptor)dsc.getImpl());
    }

    @Override
    public boolean setCharacteristicNotification(GattCharacteristic chr, boolean enable) {
        return mGatt.setCharacteristicNotification(
                (BluetoothGattCharacteristic)chr.getImpl(), enable);
    }

    @Override
    public boolean writeCharacteristic(GattCharacteristic chr) {
        return mGatt.writeCharacteristic(
                (BluetoothGattCharacteristic)chr.getImpl());
    }

    @Override
    public boolean writeDescriptor(GattDescriptor dsc) {
        return mGatt.writeDescriptor((BluetoothGattDescriptor)dsc.getImpl());
    }
}

