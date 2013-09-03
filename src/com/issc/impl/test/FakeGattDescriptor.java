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
import com.issc.util.Log;

import java.util.List;
import java.util.UUID;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattDescriptor;

public class FakeGattDescriptor implements GattDescriptor {

    private GattCharacteristic mChrIf;  // interface
    private UUID mUuid;
    private int mPermissions;
    private byte[] mValue;

    public FakeGattDescriptor(GattCharacteristic chr, UUID uuid) {
        this(chr, uuid, BluetoothGattDescriptor.PERMISSION_READ);
    }

    public FakeGattDescriptor(GattCharacteristic chr, UUID uuid, int permission) {
        mChrIf = chr;
        mUuid = uuid;
        mPermissions = permission;
    }

    @Override
    public Object getImpl() {
        return this;
    }

    @Override
    public GattCharacteristic getCharacteristic() {
        return mChrIf;
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
        return mPermissions;
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
}

