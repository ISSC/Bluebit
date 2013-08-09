// vim: et sw=4 sts=4 tabstop=4
package com.issc.impl;

import com.issc.Bluebit;
import com.issc.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

import com.issc.gatt.Gatt;
import com.issc.gatt.Gatt.Listener;
import com.issc.gatt.GattCharacteristic;
import com.issc.gatt.GattDescriptor;
import com.issc.gatt.GattService;

import com.samsung.android.sdk.bt.gatt.BluetoothGatt;
import com.samsung.android.sdk.bt.gatt.BluetoothGattAdapter;
import com.samsung.android.sdk.bt.gatt.BluetoothGattCallback;
import com.samsung.android.sdk.bt.gatt.BluetoothGattCharacteristic;
import com.samsung.android.sdk.bt.gatt.BluetoothGattDescriptor;

/**
 * This class is a wrapper and singleton to solve Samsung BLE conneciton problem.
 *
 * This singleton will try to keep the Gatt instance to be used via each Activity.
 */
public class LeService extends Service {

    private IBinder mBinder;

    private BluetoothGattCallback mCallback;
    private SystemProfileServiceListener mSystemListener;

    private boolean mGattReady = false;
    private Gatt mGatt = null;
    private List<Listener> mListeners;
    private List<Listener> mPending;
    private Object mLock;

    @Override
    public void onCreate() {
        super.onCreate();
        mLock = new Object();
        mCallback   = new TheCallback();
        mListeners  = new ArrayList<Listener>();
        mPending    = new ArrayList<Listener>();
        mSystemListener = new SystemProfileServiceListener();

        mBinder = new LocalBinder();
        init();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseGatt();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private void init() {
        /* for samsung, try to init Gatt when this Service created*/
        BluetoothGattAdapter.getProfileProxy(this,
                mSystemListener, BluetoothGattAdapter.GATT);
    }

    public void addListener(Listener l) {
        synchronized(mLock) {
            mListeners.add(l);
            if (mGattReady) {
                l.onGattReady();
            } else {
                mPending.add(l);
            }
        }
    }

    public boolean rmListener(Listener l) {
        synchronized(mLock) {
            return mListeners.remove(l);
        }
    }

    private void onGattReady() {
        synchronized(mLock) {
            mGattReady = true;
            if (mPending.size() != 0) {
                Iterator<Listener> it = mPending.iterator();
                while(it.hasNext()) {
                    it.next().onGattReady();
                }
                mPending.clear();
            }
        }
    }

    private void releaseGatt() {
        synchronized(mLock) {
            mGattReady = false;
            if (mGatt != null) {
                Log.d("Gatt Releasing");
                BluetoothGattAdapter.closeProfileProxy(BluetoothGattAdapter.GATT, mGatt.getGatt());

                /* This is a hack because we are supposed to do this in
                 * onServiceDisconnected. But, holy F! it never be called */
                syncReleaseGatt();
            }
        }
    }

    private void syncReleaseGatt() {
        mGatt.unregisterApp();
        mGatt = null;
    }

    public boolean startScan() {
        return mGatt.startScan();
    }

    public void stopScan() {
        mGatt.stopScan();
    }

    public boolean connect(BluetoothDevice device, boolean auto) {
        return mGatt.connect(device, auto);
    }

    public void disconnect(BluetoothDevice device) {
        mGatt.cancelConnection(device);
    }

    public List<BluetoothDevice> getConnectedDevices() {
        return mGatt.getConnectedDevices();
    }

    public boolean discoverServices(BluetoothDevice device) {
        return mGatt.discoverServices(device);
    }

    public int getConnectionState(BluetoothDevice device) {
        return mGatt.getConnectionState(device);
    }

    public GattService getService(BluetoothDevice device, UUID uuid) {
        return mGatt.getService(device, uuid);
    }

    public List<GattService> getServices(BluetoothDevice device) {
        return mGatt.getServices(device);
    }

    public boolean isBLEDevice(BluetoothDevice device) {
        return mGatt.isBLEDevice(device);
    }

    public boolean readCharacteristic(GattCharacteristic chr) {
        return mGatt.readCharacteristic(chr);
    }

    public boolean writeCharacteristic(GattCharacteristic chr) {
        return mGatt.writeCharacteristic(chr);
    }

    public boolean readDescriptor(GattDescriptor dsc) {
        return mGatt.readDescriptor(dsc);
    }

    public boolean writeDescriptor(GattDescriptor dsc) {
        return mGatt.writeDescriptor(dsc);
    }

    public boolean removeBond(BluetoothDevice device) {
        return mGatt.removeBond(device);
    }

    public boolean setCharacteristicNotification(GattCharacteristic chr, boolean enable) {
        return mGatt.setCharacteristicNotification(chr, enable);
    }

    class SystemProfileServiceListener implements BluetoothProfile.ServiceListener {

        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            Log.d("connection to service of System Profile created");
            Log.d("registering callback to system service.");
            if (profile == BluetoothGattAdapter.GATT) {
                /* Gatt is not completely ready */
                mGatt = new Gatt((BluetoothGatt) proxy);
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

    /* This is the only one callback that register to GATT Profile. It dispatch each
     * of returen value to listeners. */
    class TheCallback extends BluetoothGattCallback {
        @Override
        public void onAppRegistered(int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("GattProxy Regitered its callback to BluetoothGATT Profile");
                onGattReady();
            } else {
                Log.e("Register callback to GATT failed!!");
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGattCharacteristic chrc) {
            synchronized(mListeners) {
                GattCharacteristic c = new GattCharacteristic(chrc);
                Iterator<Listener> it = mListeners.iterator();
                while(it.hasNext()) {
                    it.next().onCharacteristicChanged(c);
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGattCharacteristic chrc, int status) {
            synchronized(mListeners) {
                GattCharacteristic c = new GattCharacteristic(chrc);
                Iterator<Listener> it = mListeners.iterator();
                while(it.hasNext()) {
                    it.next().onCharacteristicRead(c, status);
                }
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGattCharacteristic chrc, int status) {
            synchronized(mListeners) {
                GattCharacteristic c = new GattCharacteristic(chrc);
                Iterator<Listener> it = mListeners.iterator();
                while(it.hasNext()) {
                    it.next().onCharacteristicWrite(c, status);
                }
            }
        }

        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            synchronized(mListeners) {
                Iterator<Listener> it = mListeners.iterator();
                while(it.hasNext()) {
                    it.next().onConnectionStateChange(device, status, newState);
                }
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGattDescriptor descriptor, int status) {
            synchronized(mListeners) {
                GattDescriptor dsc = new GattDescriptor(descriptor);
                Iterator<Listener> it = mListeners.iterator();
                while(it.hasNext()) {
                    it.next().onDescriptorRead(dsc, status);
                }
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGattDescriptor descriptor, int status) {
            synchronized(mListeners) {
                GattDescriptor dsc = new GattDescriptor(descriptor);
                Iterator<Listener> it = mListeners.iterator();
                while(it.hasNext()) {
                    it.next().onDescriptorWrite(dsc, status);
                }
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothDevice device, int rssi, int status) {
            synchronized(mListeners) {
                Iterator<Listener> it = mListeners.iterator();
                while(it.hasNext()) {
                    it.next().onReadRemoteRssi(device, rssi, status);
                }
            }
        }

        @Override
        public void onScanResult(BluetoothDevice device, int rssi, byte[] scanRecord) {
            synchronized(mListeners) {
                Iterator<Listener> it = mListeners.iterator();
                while(it.hasNext()) {
                    it.next().onLeScan(device, rssi, scanRecord);
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothDevice device, int status) {
            synchronized(mListeners) {
                Iterator<Listener> it = mListeners.iterator();
                while(it.hasNext()) {
                    it.next().onServicesDiscovered(device, status);
                }
            }
        }
    }

    public class LocalBinder extends Binder {
        public LeService getService() {
            return LeService.this;
        }
    }
}
