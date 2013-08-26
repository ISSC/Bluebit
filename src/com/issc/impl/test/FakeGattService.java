// vim: et sw=4 sts=4 tabstop=4
package com.issc.impl.test;

import com.issc.Bluebit;
import com.issc.gatt.GattCharacteristic;
import com.issc.gatt.GattService;
import com.issc.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

public class FakeGattService implements GattService {

    private static int COUNTER = 0;

    private UUID mUuid;
    private List<GattCharacteristic> mChrcs;
    private int mType;
    private int mInstanceId;

    public FakeGattService(UUID uuid) {
        mUuid = uuid;
        mChrcs = new ArrayList<GattCharacteristic>();
        mInstanceId = COUNTER++;
    }

    public void addCharacteristic(FakeGattCharacteristic chrc) {
        mChrcs.add(chrc);
    }

    public void addCharacteristic(UUID uuid, List<UUID> descs) {
        mChrcs.add(new FakeGattCharacteristic(uuid, descs));
    }

    @Override
    public Object getImpl() {
        return this;
    }

    @Override
    public GattCharacteristic getCharacteristic(UUID uuid) {
        Iterator<GattCharacteristic> it = mChrcs.iterator();
        while(it.hasNext()) {
            GattCharacteristic chrc = it.next();
            if (chrc.getUuid().equals(uuid)) {
                return chrc;
            }
        }

        return null;
    }

    @Override
    public List<GattCharacteristic> getCharacteristics() {
        return mChrcs;
    }

    @Override
    public int getInstanceId() {
        return mInstanceId;
    }

    @Override
    public int getType() {
        return mType;
    }

    @Override
    public UUID getUuid() {
        return mUuid;
    }
}

