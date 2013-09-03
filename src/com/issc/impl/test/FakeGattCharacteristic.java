// vim: et sw=4 sts=4 tabstop=4
/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
    private GattService mSrv;
    private List<GattDescriptor> mDescs;
    private byte[] mValue;

    private int mWriteType;
    private int mPermissions;
    private int mProperties;

    public FakeGattCharacteristic(GattService srv, UUID uuid) {
        this(srv, uuid, null);
    }

    public FakeGattCharacteristic(GattService srv, UUID uuid, List<UUID> descs) {
        mSrv = srv;
        mUuid = uuid;
        mDescs = new ArrayList<GattDescriptor>();
        if (descs != null) {
            createDescriptors(mDescs, descs);
        }
    }

    private void createDescriptors(List<GattDescriptor> container, List<UUID> uuids) {
        Iterator<UUID> it = uuids.iterator();
        while(it.hasNext()) {
            UUID uuid = it.next();
            container.add(new FakeGattDescriptor(this, uuid));
        }
    }

    @Override
    public Object getImpl() {
        return this;
    }

    @Override
    public GattService getService() {
        return mSrv;
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

