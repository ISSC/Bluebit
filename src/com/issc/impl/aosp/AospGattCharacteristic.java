// vim: et sw=4 sts=4 tabstop=4
package com.issc.impl.aosp;

import com.issc.Bluebit;
import com.issc.gatt.GattCharacteristic;
import com.issc.gatt.GattDescriptor;
import com.issc.gatt.GattService;
import com.issc.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

public class AospGattCharacteristic implements GattCharacteristic {

    private BluetoothGattCharacteristic mChr;

    public AospGattCharacteristic(BluetoothGattCharacteristic chr) {
        mChr = chr;
    }

    @Override
    public Object getImpl() {
        return mChr;
    }

    @Override
    public GattDescriptor getDescriptor(UUID uuid) {
        return new AospGattDescriptor(mChr.getDescriptor(uuid));
    }

    @Override
    public List<GattDescriptor> getDescriptors() {
        List<BluetoothGattDescriptor> dscs = mChr.getDescriptors();
        ArrayList<GattDescriptor> list = new ArrayList<GattDescriptor>();
        for (BluetoothGattDescriptor dsc: dscs) {
            list.add(new AospGattDescriptor(dsc));
        }

        return list;
    }

    @Override
    public Integer getIntValue(int type, int offset) {
        return mChr.getIntValue(type, offset);
    }

    @Override
    public int getPermissions() {
        return mChr.getPermissions();
    }

    @Override
    public int getProperties() {
        return mChr.getProperties();
    }

    @Override
    public UUID getUuid() {
        return mChr.getUuid();
    }

    @Override
    public byte[] getValue() {
        return mChr.getValue();
    }

    @Override
    public boolean setValue(byte[] value) {
        return mChr.setValue(value);
    }

    @Override
    public void setWriteType(int writeType) {
        if (writeType == WRITE_TYPE_NO_RESPONSE) {
            mChr.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        } else {
            mChr.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        }
    }
}
