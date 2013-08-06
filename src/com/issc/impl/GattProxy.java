// vim: et sw=4 sts=4 tabstop=4
package com.issc.impl;

import com.issc.Bluebit;
import com.issc.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.issc.gatt.Gatt;
import com.issc.gatt.Gatt.Listener;
import com.issc.gatt.GattCharacteristic;
import com.issc.gatt.GattDescriptor;

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
public class GattProxy {

    private static GattProxy mMe = null;

    private Context mAppContext;
    private BluetoothGattCallback mCallback;
    private SystemProfileServiceListener mSystemListener;

    private Gatt mGatt = null;
    private Gatt mOngoingGatt = null;
    private List<Listener> mListeners;
    private List<Retriever> mRetrievers;

    private GattProxy(Context app) {
        super();
        mAppContext = app.getApplicationContext();
        mCallback   = new TheCallback();
        mListeners  = new ArrayList<Listener>();
        mRetrievers = new ArrayList<Retriever>();
        mSystemListener = new SystemProfileServiceListener();
    }

    synchronized static public GattProxy get(Context ctx) {
        if (mMe == null) {
            mMe = new GattProxy(ctx);
        }
        return mMe;
    }

    public boolean addListener(Listener l) {
        synchronized(mListeners) {
            return mListeners.add(l);
        }
    }

    public boolean rmListener(Listener l) {
        synchronized(mListeners) {
            return mListeners.remove(l);
        }
    }

    synchronized public boolean retrieveGatt(Retriever rtr) {
        if (mGatt != null) {
            // already connected to service, return it
            syncOnRetrievedGatt(rtr);
            return true;
        } else {
            Log.d("add to retrievers");
            mRetrievers.add(rtr);
            if (mOngoingGatt == null) {
                BluetoothGattAdapter.getProfileProxy(mAppContext,
                        mSystemListener, BluetoothGattAdapter.GATT);
            }
            return false;
        }
    }

    synchronized public void releaseGatt() {
        if (mGatt != null) {
            Log.d("Gatt Releasing");
            BluetoothGattAdapter.closeProfileProxy(BluetoothGattAdapter.GATT, mGatt.getGatt());

            /* This is a hack because we are supposed to do this in
             * onServiceDisconnected. But, holy F! it never be called */
            syncReleaseGatt();
        }
    }

    private void syncReleaseGatt() {
        mGatt.unregisterApp();
        mGatt = null;
        mMe = null;
    }

    synchronized private void onGattReady() {
        mGatt = mOngoingGatt;
        mOngoingGatt = null;
        if (mRetrievers.size() != 0) {
            Iterator<Retriever> it = mRetrievers.iterator();
            while(it.hasNext()) {
                syncOnRetrievedGatt(it.next());
            }
            mRetrievers.clear();
        }
    }

    private void syncOnRetrievedGatt(Retriever rtr) {
        rtr.onRetrievedGatt(mGatt);
    }

    class SystemProfileServiceListener implements BluetoothProfile.ServiceListener {

        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            Log.d("connection to service of System Profile created");
            Log.d("registering callback to system service.");
            if (profile == BluetoothGattAdapter.GATT) {
                /* Gatt is not completely ready */
                mOngoingGatt = new Gatt((BluetoothGatt) proxy);
                mOngoingGatt.registerApp(mCallback);
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
                    it.next().onScanResult(device, rssi, scanRecord);
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

    public interface Retriever {
        /**
         * To Retrieve ready-to-use Gatt Proxy.
         *
         * It will be called if
         * 1) This class got Profile Proxy from system
         * 2) and this class registered its own callback to System Proxy.
         *
         * Since this instance of class, GattProxy, is a singleton object, the
         * Gatt Proxy will be kept until this instance be destroy or the method
         * {@link GattProxy#releaseGatt()} be called.
         * */
        public void onRetrievedGatt(Gatt gatt);
    }
}
