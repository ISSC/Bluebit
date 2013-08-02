// vim: et sw=4 sts=4 tabstop=4
package com.issc.gatt;

import com.issc.Bluebit;
import com.issc.util.Log;

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
public class GattService {

    private BluetoothGattService mSrv;

    public GattService(BluetoothGattService srv) {
        mSrv = srv;
    }

    public BluetoothGattService getService() {
        return mSrv;
    }

    public BluetoothGattCharacteristic getCharacteristic(UUID uuid) {
        return mSrv.getCharacteristic(uuid);
    }

    public List<BluetoothGattCharacteristic> getCharacteristics() {
        return mSrv.getCharacteristics();
    }

    public int getInstanceId() {
        return mSrv.getInstanceId();
    }

    public int getType() {
        return mSrv.getType();
    }

    public UUID getUuid() {
        return mSrv.getUuid();
    }
}

