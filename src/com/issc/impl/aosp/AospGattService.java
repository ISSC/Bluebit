// vim: et sw=4 sts=4 tabstop=4
package com.issc.impl.aosp;

import com.issc.Bluebit;
import com.issc.gatt.GattCharacteristic;
import com.issc.gatt.GattService;
import com.issc.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

public class AospGattService implements GattService {

    private BluetoothGattService mSrv;

    public AospGattService(BluetoothGattService srv) {
        mSrv = srv;
    }

    @Override
    public Object getImpl() {
        return mSrv;
    }

    @Override
    public GattCharacteristic getCharacteristic(UUID uuid) {
        return new AospGattCharacteristic(mSrv.getCharacteristic(uuid));
    }

    @Override
    public List<GattCharacteristic> getCharacteristics() {
        List<BluetoothGattCharacteristic> chrs = mSrv.getCharacteristics();
        ArrayList<GattCharacteristic> list = new ArrayList<GattCharacteristic>();
        for (BluetoothGattCharacteristic chr: chrs) {
            list.add(new AospGattCharacteristic(chr));
        }

        return list;
    }

    @Override
    public int getInstanceId() {
        return mSrv.getInstanceId();
    }

    @Override
    public int getType() {
        return mSrv.getType();
    }

    @Override
    public UUID getUuid() {
        return mSrv.getUuid();
    }
}

