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
import com.issc.gatt.GattCharacteristic;
import com.issc.gatt.GattDescriptor;
import com.issc.gatt.GattService;
import com.issc.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;

public interface GattServer {

    /**
     * Close this Bluetooth Gatt server.
     */
    public void close();

    /**
     * Connect to a remote device.
     */
    public boolean connect(BluetoothDevice dev, boolean auto);

    /**
     * Disconnects an established connection, or cancels a connection attempt currently in progress.
     */
    public void cancelConnection(BluetoothDevice dev);
    public void clearServices();

    public GattService getService(UUID uuid);
    public List<GattService> getServices();

    public boolean notifyCharacteristicChanged(BluetoothDevice dev, GattCharacteristic chr, boolean confirm);
    public boolean removeService(GattService srv);
    public boolean sendResponse(BluetoothDevice dev, int id, int status,int offset, byte[] value);

    public interface Callback {
        public void onCharacteristicReadRequest(BluetoothDevice dev, int reqId, int offset, GattCharacteristic chrc);
        public void onCharacteristicWriteRequest(BluetoothDevice dev, int reqId, GattCharacteristic chrc, boolean preparedWrite,
                boolean responseNeeded, int offset, byte[] value);

        public void onConnectionStateChange(BluetoothDevice dev, int status, int newState);

        public void onDescriptorReadRequest(BluetoothDevice dev, int reqId, int offset,  GattDescriptor desc);
        public void onDescriptorWriteRequest(BluetoothDevice dev, int reqId, GattDescriptor desc, boolean prepareWrite,
                boolean responseNeeded, int offset, byte[] value);

        public void onExecuteWrite(BluetoothDevice dev, int reqId, boolean execute);
        public void onServiceAdded(int status, GattService service);
    }
}

