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
    private BluetoothGattCallback mClientCb;
    private BluetoothGattServerCallback mServerCb;
    private BluetoothDevice mDevice;
    private Listener mClientCbWrapper;
    private ScanCallback mScanCallback;

    private Object mLock;

    public AospGattAdapter(Context ctx, Listener listener) {
        mContext = ctx;
        mLock = new Object();
        mClientCb = new AospClientCallback();
        mClientCbWrapper = listener;
        mScanCallback = new ScanCallback();
    }

    @Override
    public Gatt connectGatt(Context ctx, boolean autoConnect, Listener listener, BluetoothDevice dev) {
        mClientCbWrapper = listener;
        mGatt = dev.connectGatt(ctx, autoConnect, mClientCb);
        mGattInterface = new AospGatt(mGatt);
        return mGattInterface;
    }

    @Override
    public GattServer openGattServer(Context ctx, GattServer.Callback clbk) {
        mServerCbWrapper = clbk;
        BluetoothManager mgr = (BluetoothManager)ctx.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothGattServer srv = mgr.openGattServer(ctx, mServerCb);
        mGattServerInterface = new AospGattServer(srv);
        return mGattServerInterface;
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
    class AospClientCallback extends BluetoothGattCallback {

        @Override
        public void onCharacteristicChanged(BluetoothGatt Gatt, BluetoothGattCharacteristic chrc) {
            GattCharacteristic c = new AospGattCharacteristic(chrc);
            mClientCbWrapper.onCharacteristicChanged(mGattInterface, c);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt Gatt, BluetoothGattCharacteristic chrc, int status) {
            GattCharacteristic c = new AospGattCharacteristic(chrc);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mClientCbWrapper.onCharacteristicRead(mGattInterface, c, Gatt.GATT_SUCCESS);
            } else {
                mClientCbWrapper.onCharacteristicRead(mGattInterface, c, status);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt Gatt, BluetoothGattCharacteristic chrc, int status) {
            GattCharacteristic c = new AospGattCharacteristic(chrc);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mClientCbWrapper.onCharacteristicWrite(mGattInterface, c, Gatt.GATT_SUCCESS);
            } else {
                mClientCbWrapper.onCharacteristicWrite(mGattInterface, c, status);
            }
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt Gatt, int status, int newState) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mClientCbWrapper.onConnectionStateChange(mGattInterface, Gatt.GATT_SUCCESS, newState);
            } else {
                mClientCbWrapper.onConnectionStateChange(mGattInterface, status, newState);
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt Gatt, BluetoothGattDescriptor descriptor, int status) {
            GattDescriptor dsc = new AospGattDescriptor(descriptor);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mClientCbWrapper.onDescriptorRead(mGattInterface, dsc, Gatt.GATT_SUCCESS);
            } else {
                mClientCbWrapper.onDescriptorRead(mGattInterface, dsc, status);
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt Gatt, BluetoothGattDescriptor descriptor, int status) {
            GattDescriptor dsc = new AospGattDescriptor(descriptor);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mClientCbWrapper.onDescriptorWrite(mGattInterface, dsc, Gatt.GATT_SUCCESS);
            } else {
                mClientCbWrapper.onDescriptorWrite(mGattInterface, dsc, status);
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt Gatt, int rssi, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mClientCbWrapper.onReadRemoteRssi(mGattInterface, rssi, Gatt.GATT_SUCCESS);
            } else {
                mClientCbWrapper.onReadRemoteRssi(mGattInterface, rssi, status);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mClientCbWrapper.onServicesDiscovered(mGattInterface, Gatt.GATT_SUCCESS);
            } else {
                mClientCbWrapper.onServicesDiscovered(mGattInterface, status);
            }
        }
    }
}

