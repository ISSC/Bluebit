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
public class GattDescriptor {

    public final static byte[] ENABLE_NOTIFICATION_VALUE = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
    public final static byte[] DISABLE_NOTIFICATION_VALUE = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;

    private BluetoothGattDescriptor mDsc;

    public GattDescriptor(BluetoothGattDescriptor dsc) {
        mDsc = dsc;
    }

    public BluetoothGattDescriptor getDescriptor() {
        return mDsc;
    }

    public int getPermissions() {
        return mDsc.getPermissions();
    }

    public UUID getUuid() {
        return mDsc.getUuid();
    }

    public byte[] getValue() {
        return mDsc.getValue();
    }

    public boolean setValue(byte[] value) {
        return mDsc.setValue(value);
    }
}

