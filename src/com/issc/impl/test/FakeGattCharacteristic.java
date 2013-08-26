// vim: et sw=4 sts=4 tabstop=4
package com.issc.impl.test;

import com.issc.Bluebit;
import com.issc.gatt.GattCharacteristic;
import com.issc.gatt.GattDescriptor;
import com.issc.gatt.GattService;
import com.issc.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import android.bluetooth.BluetoothGattCharacteristic;

public class FakeGattCharacteristic implements GattCharacteristic {

    private UUID mUuid;
    private List<GattDescriptor> mDescs;
    private byte[] mValue;

    private int mWriteType;
    private int mPermissions;
    private int mProperties;

    public FakeGattCharacteristic(UUID uuid) {
        this(uuid, null);
    }

    public FakeGattCharacteristic(UUID uuid, List<UUID> descs) {
        mUuid = uuid;
        mDescs = new ArrayList<GattDescriptor>();
        createDescriptors(mDescs, descs);
    }

    private void createDescriptors(List<GattDescriptor> container, List<UUID> uuids) {
        if (uuids == null) {
            return;
        }
        Iterator<UUID> it = uuids.iterator();
        while(it.hasNext()) {
            UUID uuid = it.next();
            container.add(new FakeGattDescriptor(uuid));
        }
    }

    @Override
    public Object getImpl() {
        return this;
    }

    @Override
    public GattDescriptor getDescriptor(UUID uuid) {
        Iterator<GattDescriptor> it = mDescs.iterator();
        while(it.hasNext()) {
            GattDescriptor desc = it.next();
            if (desc.getUuid().equals(uuid)) {
                return desc;
            }
        }

        return null;
    }

    @Override
    public List<GattDescriptor> getDescriptors() {
        return mDescs;
    }

    @Override
    public Integer getIntValue(int type, int offset) {
        return new Integer(0);
    }

    @Override
    public int getPermissions() {
        return mPermissions;
    }

    @Override
    public int getProperties() {
        return mProperties;
    }

    @Override
    public UUID getUuid() {
        return mUuid;
    }

    @Override
    public byte[] getValue() {
        return mValue;
    }

    @Override
    public boolean setValue(byte[] value) {
        mValue = new byte[value.length];
        for (int i = 0; i < value.length; i++) {
            mValue[i] = value[i];
        }
        return true;
    }

    @Override
    public void setWriteType(int writeType) {
        if (writeType == WRITE_TYPE_NO_RESPONSE) {
            mWriteType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE;
        } else {
            mWriteType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT;
        }
    }
}

