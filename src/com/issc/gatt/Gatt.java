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

public interface Gatt {

    public final static int GATT_SUCCESS = 0;


    /**
     * Close this Bluetooth Gatt client.
     */
    public void close();

    /**
     * Connect to a remote device.
     */
    public boolean connect();

    /**
     * Disconnects an established connection, or cancels a connection attempt currently in progress.
     */
    public void disconnect();

    public boolean discoverServices();
    public BluetoothDevice getDevice();
    public GattService getService(UUID uuid);
    public List<GattService> getServices();
    public boolean readCharacteristic(GattCharacteristic chr);
    public boolean readDescriptor(GattDescriptor dsc);

    public boolean setCharacteristicNotification(GattCharacteristic chr, boolean enable);
    public boolean writeCharacteristic(GattCharacteristic chr);
    public boolean writeDescriptor(GattDescriptor dsc);

    public interface Listener {
        public void onCharacteristicChanged(Gatt gatt, GattCharacteristic chrc);
        public void onCharacteristicRead(Gatt gatt, GattCharacteristic chrc, int status);
        public void onCharacteristicWrite(Gatt gatt, GattCharacteristic chrc, int status);
        public void onConnectionStateChange(Gatt gatt, int status, int newState);
        public void onDescriptorRead(Gatt gatt, GattDescriptor descriptor, int status);
        public void onDescriptorWrite(Gatt gatt, GattDescriptor descriptor, int status);
        public void onReadRemoteRssi(Gatt gatt, int rssi, int status);
        public void onServicesDiscovered(Gatt gatt, int status);
    }

    public static class ListenerHelper implements Listener {
        String iTag;
        public ListenerHelper(String tag) {
            iTag = tag;
        }

        public void onCharacteristicChanged(Gatt gatt, GattCharacteristic chrc) {
            Log.d(String.format("%s, onCharChanged", iTag));
        }

        public void onCharacteristicRead(Gatt gatt, GattCharacteristic chrc, int status) {
            Log.d(String.format("%s, onCharacteristicRead, status:%d", iTag, status));
        }

        public void onCharacteristicWrite(Gatt gatt, GattCharacteristic chrc, int status) {
            Log.d(String.format("%s, onCharacteristicWrite, status:%d", iTag, status));
        }

        public void onConnectionStateChange(Gatt gatt, int status, int newState) {
            Log.d(String.format("%s, onConnectionStateChange, status:%d, newState:%d", iTag, status, newState));
        }

        public void onDescriptorRead(Gatt gatt, GattDescriptor descriptor, int status) {
            Log.d(String.format("%s, onDescriptorRead, status:%d", iTag, status));
        }

        public void onDescriptorWrite(Gatt gatt, GattDescriptor descriptor, int status) {
            Log.d(String.format("%s, onDescriptorWrite, status:%d", iTag, status));
        }

        public void onReadRemoteRssi(Gatt gatt, int rssi, int status) {
            Log.d(String.format("%s, onReadRemoteRssi, rssi:%d", iTag, rssi));
        }

        public void onServicesDiscovered(Gatt gatt, int status) {
            Log.d(String.format("%s, onServicesDiscovered", iTag));
        }
    }
}

