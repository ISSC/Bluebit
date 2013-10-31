// vim: et sw=4 sts=4 tabstop=4
package com.issc.impl.samsung;

import com.issc.Bluebit;
import com.issc.gatt.GattCharacteristic;
import com.issc.gatt.GattDescriptor;
import com.issc.gatt.GattService;
import com.issc.util.Log;

import java.util.List;
import java.util.UUID;

import android.bluetooth.BluetoothDevice;

import com.samsung.android.sdk.bt.gatt.BluetoothGatt;
import com.samsung.android.sdk.bt.gatt.BluetoothGattCallback;
import com.samsung.android.sdk.bt.gatt.BluetoothGattCharacteristic;
import com.samsung.android.sdk.bt.gatt.BluetoothGattDescriptor;
import com.samsung.android.sdk.bt.gatt.BluetoothGattService;

public class SamsungGattDescriptor implements GattDescriptor {

    private BluetoothGattDescriptor mDsc;

    public SamsungGattDescriptor(BluetoothGattDescriptor dsc) {
        mDsc = dsc;
    }

    @Override
    public Object getImpl() {
        return mDsc;
    }

    @Override
    public GattCharacteristic getCharacteristic() {
        return new SamsungGattCharacteristic(mDsc.getCharacteristic());
    }

    @Override
    public byte[] getConstantBytes(int type) {
        if (type == ENABLE_NOTIFICATION_VALUE) {
            return BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
        } else if (type == DISABLE_NOTIFICATION_VALUE) {
            return BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
        }

        return null;
    }

    @Override
    public int getPermissions() {
        return mDsc.getPermissions();
    }

    @Override
    public UUID getUuid() {
        return mDsc.getUuid();
    }

    @Override
    public byte[] getValue() {
        return mDsc.getValue();
    }

    @Override
    public boolean setValue(byte[] value) {
        return mDsc.setValue(value);
    }
}

