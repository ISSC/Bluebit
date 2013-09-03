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

package com.issc.gatt;

import com.issc.Bluebit;
import com.issc.gatt.Gatt;
import com.issc.gatt.Gatt.Listener;
import com.issc.util.Log;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;

import java.util.List;

/**
 * Encapsulate Gatt initial procedures.
 */
public interface GattAdapter {

    /**
     * Connect to a device and retrieve Gatt profile from the device.
     *
     * For the reason to be compatible with Samsung, we cannot return Gatt
     * instance directly although that is AOSP way. Instead of that, we return
     * Gatt instance in {@link #onGattReady}.
     */
    public Gatt connectGatt(Context ctx, boolean autoConnect, Listener listener, BluetoothDevice dev);

    public boolean startLeScan(LeScanCallback clbk);
    public void stopLeScan(LeScanCallback clbk);

    public List<BluetoothDevice> getConnectedDevices();
    public int getConnectionState(BluetoothDevice device);

    public interface LeScanCallback {
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord);
    }
}

