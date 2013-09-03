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
import com.issc.gatt.Gatt;
import com.issc.gatt.GattAdapter;
import com.issc.gatt.Gatt.Listener;
import com.issc.gatt.GattCharacteristic;
import com.issc.gatt.GattDescriptor;
import com.issc.gatt.GattService;
import com.issc.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;

public class FakeGattAdapter implements GattAdapter {

    private List<BluetoothDevice> mDevs;

    private BluetoothAdapter mAdapter;
    private BluetoothDevice mDevInfo;
    private BluetoothDevice mDevTransparent;
    private Gatt mGattInterface;

    public FakeGattAdapter(Context ctx, Listener listener) {
        BluetoothManager mgr = (BluetoothManager)ctx.getSystemService(Context.BLUETOOTH_SERVICE);
        mAdapter = mgr.getAdapter();
        initDevices();
    }

    private void initDevices() {
        mDevInfo = mAdapter.getRemoteDevice("00:11:DE:AD:BE:EF");
        mDevTransparent = mAdapter.getRemoteDevice("00:11:00:FF:FF:00");
    }

    @Override
    public Gatt connectGatt(Context ctx, boolean autoConnect, Listener listener, BluetoothDevice dev) {
        if (dev.getAddress().equals(mDevTransparent.getAddress())) {
            mGattInterface = new FakeGattDeviceTransparent(mDevTransparent, listener);
        } else {
            mGattInterface = new FakeGattDeviceInfo(mDevInfo, listener);
        }
        return mGattInterface;
    }


    @Override
    public boolean startLeScan(GattAdapter.LeScanCallback callback) {
        callback.onLeScan(mDevInfo, 0, new byte[1]);
        callback.onLeScan(mDevTransparent, 0, new byte[1]);
        return true;
    }

    @Override
    public void stopLeScan(GattAdapter.LeScanCallback callback) {
    }

    @Override
    public int getConnectionState(BluetoothDevice device) {
        return BluetoothAdapter.STATE_CONNECTED;
    }

    @Override
    public List<BluetoothDevice> getConnectedDevices() {
        List<BluetoothDevice> devs = new ArrayList<BluetoothDevice>();
        return devs;
    }
}

