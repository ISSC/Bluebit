// vim: et sw=4 sts=4 tabstop=4
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
     *
     * In AOSP, it just invoke *connect* without any parameter since the Gatt instance is already bind
     * to the remote device. However, in Samsung SDK the Gatt profile binds to several remote devices.
     * Since we want to be compatible with Samsung SDK, we should specify the BluetoothDevice.
     */
    public boolean connect(BluetoothDevice device, boolean auto);

    /**
     * Disconnects an established connection, or cancels a connection attempt currently in progress.
     *
     * In AOSP, it just invoke *disconnect* without any parameter since the Gatt instance is already bind
     * to the remote device. However, in Samsung SDK the Gatt profile binds to several remote devices.
     * Since we want to be compatible with Samsung SDK, we should specify the BluetoothDevice.
     */
    public void disconnect(BluetoothDevice device);

    public List<BluetoothDevice> getConnectedDevices();
    public boolean discoverServices(BluetoothDevice device);
    public int getConnectionState(BluetoothDevice device);
    public GattService getService(BluetoothDevice device, UUID uuid);
    public List<GattService> getServices(BluetoothDevice device);
    public boolean isBLEDevice(BluetoothDevice device);
    public boolean readCharacteristic(GattCharacteristic chr);
    public boolean readDescriptor(GattDescriptor dsc);
    public boolean removeBond(BluetoothDevice device);

    public boolean setCharacteristicNotification(GattCharacteristic chr, boolean enable);
    public boolean writeCharacteristic(GattCharacteristic chr);
    public boolean writeDescriptor(GattDescriptor dsc);

    public interface Listener {
        /* This function will be called if ready to use Gatt functions */
        public void onGattReady(Gatt gatt);
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord);
        public void onCharacteristicChanged(GattCharacteristic chrc);
        public void onCharacteristicRead(GattCharacteristic chrc, int status);
        public void onCharacteristicWrite(GattCharacteristic chrc, int status);
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState);
        public void onDescriptorRead(GattDescriptor descriptor, int status);
        public void onDescriptorWrite(GattDescriptor descriptor, int status);
        public void onReadRemoteRssi(BluetoothDevice device, int rssi, int status);
        public void onServicesDiscovered(BluetoothDevice device, int status);
    }

    public static class ListenerHelper implements Listener {
        String iTag;
        public ListenerHelper(String tag) {
            iTag = tag;
        }

        public void onGattReady(Gatt gatt) {
            Log.d(String.format("%s, onGattReady", iTag));
        }

        public void onCharacteristicChanged(GattCharacteristic chrc) {
            Log.d(String.format("%s, onCharChanged", iTag));
        }

        public void onCharacteristicRead(GattCharacteristic chrc, int status) {
            Log.d(String.format("%s, onCharacteristicRead, status:%d", iTag, status));
        }

        public void onCharacteristicWrite(GattCharacteristic chrc, int status) {
            Log.d(String.format("%s, onCharacteristicWrite, status:%d", iTag, status));
        }

        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            Log.d(String.format("%s, onConnectionStateChange, status:%d, newState:%d", iTag, status, newState));
        }

        public void onDescriptorRead(GattDescriptor descriptor, int status) {
            Log.d(String.format("%s, onDescriptorRead, status:%d", iTag, status));
        }

        public void onDescriptorWrite(GattDescriptor descriptor, int status) {
            Log.d(String.format("%s, onDescriptorWrite, status:%d", iTag, status));
        }

        public void onReadRemoteRssi(BluetoothDevice device, int rssi, int status) {
            Log.d(String.format("%s, onReadRemoteRssi, rssi:%d", iTag, rssi));
        }

        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            Log.d(String.format("%s, onScanResult, rssi:%d", iTag, rssi));
        }

        public void onServicesDiscovered(BluetoothDevice device, int status) {
            Log.d(String.format("%s, onServicesDiscovered", iTag));
        }
    }
}

