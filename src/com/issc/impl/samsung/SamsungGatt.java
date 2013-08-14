// vim: et sw=4 sts=4 tabstop=4
package com.issc.impl.samsung;

import com.issc.Bluebit;
import com.issc.gatt.Gatt;
import com.issc.gatt.Gatt.Listener;
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

import com.samsung.android.sdk.bt.gatt.BluetoothGatt;
import com.samsung.android.sdk.bt.gatt.BluetoothGattAdapter;
import com.samsung.android.sdk.bt.gatt.BluetoothGattCallback;
import com.samsung.android.sdk.bt.gatt.BluetoothGattCharacteristic;
import com.samsung.android.sdk.bt.gatt.BluetoothGattDescriptor;
import com.samsung.android.sdk.bt.gatt.BluetoothGattService;

public class SamsungGatt implements Gatt {

    private BluetoothGatt mGatt;
    private BluetoothGattCallback mCallback;
    private SystemProfileServiceListener mSystemListener;
    private Listener mListener;

    public SamsungGatt() {
        mCallback = new TheCallback();
        mSystemListener = new SystemProfileServiceListener();
    }

    @Override
    public void connectGatt(Context ctx, boolean autoConnect, Listener listener) {
        mListener = listener;
        /* for samsung, try to init Gatt when this Service created*/
        BluetoothGattAdapter.getProfileProxy(ctx,
                mSystemListener, BluetoothGattAdapter.GATT);
    }

    @Override
    public void close() {
        BluetoothGattAdapter.closeProfileProxy(BluetoothGattAdapter.GATT, mGatt);
    }

    @Override
    public boolean connect(BluetoothDevice device, boolean auto) {
        return mGatt.connect(device, auto);
    }

    @Override
    public void cancelConnection(BluetoothDevice device) {
        mGatt.cancelConnection(device);
    }

    @Override
    public List<BluetoothDevice> getConnectedDevices() {
        return mGatt.getConnectedDevices();
    }

    @Override
    public boolean discoverServices(BluetoothDevice device) {
        return mGatt.discoverServices(device);
    }

    @Override
    public int getConnectionState(BluetoothDevice device) {
        return mGatt.getConnectionState(device);
    }

    @Override
    public GattService getService(BluetoothDevice device, UUID uuid) {
        return new SamsungGattService(mGatt.getService(device, uuid));
    }

    @Override
    public List<GattService> getServices(BluetoothDevice device) {
        List<BluetoothGattService> srvs = mGatt.getServices(device);
        ArrayList<GattService> list = new ArrayList<GattService>();
        for (BluetoothGattService srv: srvs) {
            list.add(new SamsungGattService(srv));
        }

        return list;
    }

    @Override
    public boolean isBLEDevice(BluetoothDevice device) {
        return mGatt.isBLEDevice(device);
    }

    @Override
    public boolean readCharacteristic(GattCharacteristic chr) {
        return mGatt.readCharacteristic(
                (BluetoothGattCharacteristic)chr.getImpl());
    }

    @Override
    public boolean readDescriptor(GattDescriptor dsc) {
        return mGatt.readDescriptor((BluetoothGattDescriptor)dsc.getImpl());
    }

    @Override
    public boolean removeBond(BluetoothDevice device) {
        return mGatt.removeBond(device);
    }

    @Override
    public boolean setCharacteristicNotification(GattCharacteristic chr, boolean enable) {
        return mGatt.setCharacteristicNotification(
                (BluetoothGattCharacteristic)chr.getImpl(), enable);
    }

    @Override
    public boolean startScan() {
        return mGatt.startScan();
    }

    @Override
    public void stopScan() {
        mGatt.stopScan();
    }

    @Override
    public boolean writeCharacteristic(GattCharacteristic chr) {
        return mGatt.writeCharacteristic(
                (BluetoothGattCharacteristic)chr.getImpl());
    }

    @Override
    public boolean writeDescriptor(GattDescriptor dsc) {
        return mGatt.writeDescriptor((BluetoothGattDescriptor)dsc.getImpl());
    }

    /* This is the only one callback that register to GATT Profile. It dispatch each
     * of returen value to listeners. */
    class TheCallback extends BluetoothGattCallback {
        @Override
        public void onAppRegistered(int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("GattProxy Regitered its callback to BluetoothGATT Profile");
                mListener.onGattReady();
            } else {
                Log.e("Register callback to GATT failed!!");
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGattCharacteristic chrc) {
            GattCharacteristic c = new SamsungGattCharacteristic(chrc);
            mListener.onCharacteristicChanged(c);
        }

        @Override
        public void onCharacteristicRead(BluetoothGattCharacteristic chrc, int status) {
            GattCharacteristic c = new SamsungGattCharacteristic(chrc);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mListener.onCharacteristicRead(c, Gatt.GATT_SUCCESS);
            } else {
                mListener.onCharacteristicRead(c, status);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGattCharacteristic chrc, int status) {
            GattCharacteristic c = new SamsungGattCharacteristic(chrc);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mListener.onCharacteristicWrite(c, Gatt.GATT_SUCCESS);
            } else {
                mListener.onCharacteristicWrite(c, status);
            }
        }

        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mListener.onConnectionStateChange(device, Gatt.GATT_SUCCESS, newState);
            } else {
                mListener.onConnectionStateChange(device, status, newState);
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGattDescriptor descriptor, int status) {
            GattDescriptor dsc = new SamsungGattDescriptor(descriptor);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mListener.onDescriptorRead(dsc, Gatt.GATT_SUCCESS);
            } else {
                mListener.onDescriptorRead(dsc, status);
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGattDescriptor descriptor, int status) {
            GattDescriptor dsc = new SamsungGattDescriptor(descriptor);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mListener.onDescriptorWrite(dsc, Gatt.GATT_SUCCESS);
            } else {
                mListener.onDescriptorWrite(dsc, status);
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothDevice device, int rssi, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mListener.onReadRemoteRssi(device, rssi, Gatt.GATT_SUCCESS);
            } else {
                mListener.onReadRemoteRssi(device, rssi, status);
            }
        }

        @Override
        public void onScanResult(BluetoothDevice device, int rssi, byte[] scanRecord) {
            mListener.onLeScan(device, rssi, scanRecord);
        }

        @Override
        public void onServicesDiscovered(BluetoothDevice device, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mListener.onServicesDiscovered(device, Gatt.GATT_SUCCESS);
            } else {
                mListener.onServicesDiscovered(device, status);
            }
        }
    }

    class SystemProfileServiceListener implements BluetoothProfile.ServiceListener {

        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            Log.d("connection to service of System Profile created");
            Log.d("registering callback to system service.");
            if (profile == BluetoothGattAdapter.GATT) {
                /* Gatt is not completely ready */
                mGatt = (BluetoothGatt) proxy;
                mGatt.registerApp(mCallback);
            }
        }

        @Override
        public void onServiceDisconnected(int profile) {
            // Unfortunately, this callback seems never been called
            // from SDK for unknown reason.
            Log.d("connection to service of System Profile removed");
            Log.d("you cannot use Gatt anymore in this application");
            if (profile == BluetoothGattAdapter.GATT) {
            }
        }
    }
}

