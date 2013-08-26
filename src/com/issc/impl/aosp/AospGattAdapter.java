// vim: et sw=4 sts=4 tabstop=4
package com.issc.impl.aosp;

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
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;

public class AospGattAdapter implements GattAdapter {

    private Context mContext;

    private AospGatt mGattInterface;
    private BluetoothGatt mGatt;
    private BluetoothGattCallback mCallback;
    private BluetoothDevice mDevice;
    private Listener mListener;
    private ScanCallback mScanCallback;

    private Object mLock;

    public AospGattAdapter(Context ctx, Listener listener) {
        mContext = ctx;
        mLock = new Object();
        mCallback = new AospCallback();
        mScanCallback = new ScanCallback();
        mListener = listener;
    }

    @Override
    public Gatt connectGatt(Context ctx, boolean autoConnect, Listener listener, BluetoothDevice dev) {
        mListener = listener;
        mGatt = dev.connectGatt(ctx, autoConnect, mCallback);
        mGattInterface = new AospGatt(mGatt);
        return mGattInterface;
    }


    @Override
    public boolean startLeScan(GattAdapter.LeScanCallback callback) {
        mScanCallback.setListener(callback);
        BluetoothManager mgr = (BluetoothManager)mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = mgr.getAdapter();
        return adapter.startLeScan(mScanCallback);
    }

    @Override
    public void stopLeScan(GattAdapter.LeScanCallback callback) {
        mScanCallback.setListener(null);
        BluetoothManager mgr = (BluetoothManager)mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = mgr.getAdapter();
        adapter.stopLeScan(mScanCallback);
    }

    @Override
    public int getConnectionState(BluetoothDevice device) {
        BluetoothManager mgr = (BluetoothManager)mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        return mgr.getConnectionState(device, BluetoothProfile.GATT);
    }

    @Override
    public List<BluetoothDevice> getConnectedDevices() {
        BluetoothManager mgr = (BluetoothManager)mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        return mgr.getConnectedDevices(BluetoothProfile.GATT);
    }

    class ScanCallback implements BluetoothAdapter.LeScanCallback {
        private GattAdapter.LeScanCallback mScanCallback;

        public void setListener(GattAdapter.LeScanCallback clbk) {
            mScanCallback = clbk;
        }

        public void onLeScan(BluetoothDevice dev, int rssi, byte[] records) {
            if (mScanCallback != null) {
                mScanCallback.onLeScan(dev, rssi, records);
            }
        }
    }

    /* This is the only one callback that register to GATT Profile. It dispatch each
     * of returen value to listeners. */
    class AospCallback extends BluetoothGattCallback {

        @Override
        public void onCharacteristicChanged(BluetoothGatt Gatt, BluetoothGattCharacteristic chrc) {
            GattCharacteristic c = new AospGattCharacteristic(chrc);
            mListener.onCharacteristicChanged(mGattInterface, c);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt Gatt, BluetoothGattCharacteristic chrc, int status) {
            GattCharacteristic c = new AospGattCharacteristic(chrc);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mListener.onCharacteristicRead(mGattInterface, c, Gatt.GATT_SUCCESS);
            } else {
                mListener.onCharacteristicRead(mGattInterface, c, status);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt Gatt, BluetoothGattCharacteristic chrc, int status) {
            GattCharacteristic c = new AospGattCharacteristic(chrc);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mListener.onCharacteristicWrite(mGattInterface, c, Gatt.GATT_SUCCESS);
            } else {
                mListener.onCharacteristicWrite(mGattInterface, c, status);
            }
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt Gatt, int status, int newState) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mListener.onConnectionStateChange(mGattInterface, Gatt.GATT_SUCCESS, newState);
            } else {
                mListener.onConnectionStateChange(mGattInterface, status, newState);
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt Gatt, BluetoothGattDescriptor descriptor, int status) {
            GattDescriptor dsc = new AospGattDescriptor(descriptor);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mListener.onDescriptorRead(mGattInterface, dsc, Gatt.GATT_SUCCESS);
            } else {
                mListener.onDescriptorRead(mGattInterface, dsc, status);
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt Gatt, BluetoothGattDescriptor descriptor, int status) {
            GattDescriptor dsc = new AospGattDescriptor(descriptor);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mListener.onDescriptorWrite(mGattInterface, dsc, Gatt.GATT_SUCCESS);
            } else {
                mListener.onDescriptorWrite(mGattInterface, dsc, status);
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt Gatt, int rssi, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mListener.onReadRemoteRssi(mGattInterface, rssi, Gatt.GATT_SUCCESS);
            } else {
                mListener.onReadRemoteRssi(mGattInterface, rssi, status);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mListener.onServicesDiscovered(mGattInterface, Gatt.GATT_SUCCESS);
            } else {
                mListener.onServicesDiscovered(mGattInterface, status);
            }
        }
    }
}

