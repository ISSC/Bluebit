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

package com.issc.data;

import com.issc.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * This class encapsulate BluetoothDevice and BLE related information.
 */

public class BLEDevice implements Parcelable {

    private BluetoothDevice mDevice;
    private Bundle mData;

    private final String sKeyUUID = "uuids_of_services";

    public BLEDevice(BluetoothDevice device) {
        mDevice = device;
        mData = new Bundle();
    }

    public BLEDevice(Parcel src) {
        mDevice = BluetoothDevice.CREATOR.createFromParcel(src);
        mData = Bundle.CREATOR.createFromParcel(src);
    }

    public BluetoothDevice getDevice() {
        return mDevice;
    }

    public Bundle getData() {
        return mData;
    }

    public boolean hasSameAddr(BLEDevice target) {
        return hasSameAddr(target.getDevice());
    }

    public boolean hasSameAddr(BluetoothDevice target) {
        return hasSameAddr(target.getAddress());
    }

    public boolean hasSameAddr(String addr) {
        return mDevice.getAddress().equals(addr);
    }

    @Override
    public void writeToParcel(Parcel dst, int flags) {
        mDevice.writeToParcel(dst, flags);
        mData.writeToParcel(dst, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<BLEDevice> CREATOR =
        new Parcelable.Creator<BLEDevice>() {
            public BLEDevice createFromParcel(Parcel src) {
                return new BLEDevice(src);
            }

            public BLEDevice[] newArray(int size) {
                return new BLEDevice[size];
            }
        };

    public void setServicesUuid(List<UUID> list) {
        ArrayList<String> uuids = new ArrayList<String>();
        Iterator<UUID> it = list.iterator();
        while (it.hasNext()) {
            UUID uuid = it.next();
            uuids.add(uuid.toString());
        }

        mData.putStringArrayList(sKeyUUID, uuids);
    }

    public List<UUID> getServicesUuid() {
        List<String> uuids = mData.getStringArrayList(sKeyUUID);
        if (uuids == null) {
            return null;
        }

        List<UUID> list = new ArrayList<UUID>();

        Iterator<String> it = uuids.iterator();
        while (it.hasNext()) {
            list.add(UUID.fromString(it.next()));
        }

        return list;
    }
}
