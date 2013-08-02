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
public class GattCharacteristic {

    public final static int WRITE_TYPE_DEFAULT = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT;
    public final static int WRITE_TYPE_NO_RESPONSE = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE;

    private BluetoothGattCharacteristic mChr;

    public GattCharacteristic(BluetoothGattCharacteristic chr) {
        mChr = chr;
    }

    public BluetoothGattCharacteristic getCharacteristic() {
        return mChr;
    }

    public GattDescriptor getDescriptor(UUID uuid) {
        return new GattDescriptor(mChr.getDescriptor(uuid));
    }

    public List<GattDescriptor> getDescriptors() {
        List<BluetoothGattDescriptor> dscs = mChr.getDescriptors();
        ArrayList<GattDescriptor> list = new ArrayList<GattDescriptor>();
        for (BluetoothGattDescriptor dsc: dscs) {
            list.add(new GattDescriptor(dsc));
        }

        return list;
    }

    public Integer getIntValue(int type, int offset) {
        return mChr.getIntValue(type, offset);
    }

    public int getPermissions() {
        return mChr.getPermissions();
    }

    public int getProperties() {
        return mChr.getProperties();
    }

    public UUID getUuid() {
        return mChr.getUuid();
    }

    public byte[] getValue() {
        return mChr.getValue();
    }

    public boolean setValue(byte[] value) {
        return mChr.setValue(value);
    }

    public void setWriteType(int writeType) {
        if (writeType == WRITE_TYPE_NO_RESPONSE) {
            mChr.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        } else {
            mChr.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        }
    }
}

