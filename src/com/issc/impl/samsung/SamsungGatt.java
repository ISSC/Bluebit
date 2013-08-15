// vim: et sw=4 sts=4 tabstop=4
package com.issc.impl.samsung;

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
import android.bluetooth.BluetoothProfile;
import android.content.Context;

import com.samsung.android.sdk.bt.gatt.BluetoothGatt;
import com.samsung.android.sdk.bt.gatt.BluetoothGattAdapter;
import com.samsung.android.sdk.bt.gatt.BluetoothGattCallback;
import com.samsung.android.sdk.bt.gatt.BluetoothGattCharacteristic;
import com.samsung.android.sdk.bt.gatt.BluetoothGattDescriptor;
import com.samsung.android.sdk.bt.gatt.BluetoothGattService;

public class SamsungGatt implements Gatt {

    private BluetoothGatt mGatt;
    private Listener mListener;

    public SamsungGatt(BluetoothGatt gatt) {
        mGatt = gatt;
    }

    @Override
    public void close() {
        BluetoothGattAdapter.closeProfileProxy(BluetoothGattAdapter.GATT, mGatt);
    }

    @Override
    public boolean connect(BluetoothDevice device, boolean auto) {
        return mGatt.connect(device, auto);
    }

    @Override
    public void disconnect(BluetoothDevice device) {
        mGatt.cancelConnection(device);
    }

    @Override
    public boolean discoverServices(BluetoothDevice device) {
        return mGatt.discoverServices(device);
    }

    @Override
    public GattService getService(BluetoothDevice device, UUID uuid) {
        return new SamsungGattService(mGatt.getService(device, uuid));
    }

    @Override
    public List<GattService> getServices(BluetoothDevice device) {
        List<BluetoothGattService> srvs = mGatt.getServices(device);
        ArrayList<GattService> list = new ArrayList<GattService>();
        for (BluetoothGattService srv: srvs) {
            list.add(new SamsungGattService(srv));
        }

        return list;
    }

    @Override
    public boolean isBLEDevice(BluetoothDevice device) {
        return mGatt.isBLEDevice(device);
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
    public boolean removeBond(BluetoothDevice device) {
        return mGatt.removeBond(device);
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

