// vim: et sw=4 sts=4 tabstop=4
package com.issc.impl.aosp;

import com.issc.Bluebit;
import com.issc.gatt.GattDescriptor;
import com.issc.util.Log;

import java.util.List;
import java.util.UUID;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattDescriptor;

public class AospGattDescriptor implements GattDescriptor {

    private BluetoothGattDescriptor mDsc;

    public AospGattDescriptor(BluetoothGattDescriptor dsc) {
        mDsc = dsc;
    }

    @Override
    public Object getImpl() {
        return mDsc;
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

