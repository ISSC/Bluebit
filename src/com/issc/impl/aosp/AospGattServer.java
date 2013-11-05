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

package com.issc.impl.aosp;

import com.issc.Bluebit;
import com.issc.gatt.Gatt;
import com.issc.gatt.Gatt.Listener;
import com.issc.gatt.GattCharacteristic;
import com.issc.gatt.GattDescriptor;
import com.issc.gatt.GattServer;
import com.issc.gatt.GattService;
import com.issc.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;

public class AospGattServer implements GattServer {

    private BluetoothGattServer mGattSrv;

    public AospGattServer(BluetoothGattServer srv) {
        mGattSrv = srv;
    }

    @Override
    public void close() {
        mGattSrv.close();
    }

    @Override
    public boolean connect(BluetoothDevice dev, boolean auto) {
        return mGattSrv.connect(dev, auto);
    }

    @Override
    public void cancelConnection(BluetoothDevice dev) {
        mGattSrv.cancelConnection(dev);
    }

    @Override
    public void clearServices() {
        mGattSrv.clearServices();
    }

    @Override
    public GattService getService(UUID uuid) {
        return new AospGattService(mGattSrv.getService(uuid));
    }

    @Override
    public List<GattService> getServices() {
        List<BluetoothGattService> srvs = mGattSrv.getServices();
        ArrayList<GattService> list = new ArrayList<GattService>();
        for (BluetoothGattService srv: srvs) {
            list.add(new AospGattService(srv));
        }

        return list;
    }

    @Override
    public boolean notifyCharacteristicChanged(BluetoothDevice dev, GattCharacteristic chr, boolean confirm) {
        BluetoothGattCharacteristic c = (BluetoothGattCharacteristic) chr.getImpl();
        return mGattSrv.notifyCharacteristicChanged(dev, c, confirm);
    }

    @Override
    public boolean removeService(GattService srv) {
        BluetoothGattService s = (BluetoothGattService) srv.getImpl();
        return mGattSrv.removeService(s);
    }

    @Override
    public boolean sendResponse(BluetoothDevice dev, int id, int status, int offset, byte[] value) {
        return mGattSrv.sendResponse(dev, id, status, offset, value);
    }
}

