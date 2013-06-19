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
public class GattProxy implements BluetoothProfile.ServiceListener {

    private Context mAppContext;

    private BluetoothGatt mGatt = null;
    private BluetoothGatt mOngoingGatt = null;
    private List<Listener> mListeners;
    private List<Listener> mRetrievers;

    private static GattProxy mMe = null;
    private BluetoothGattCallback mCallback;

    private GattProxy(Context app) {
        super();
        mAppContext = app;
        mCallback   = new TheCallback();
        mListeners  = new ArrayList<Listener>();
        mRetrievers = new ArrayList<Listener>();

        BluetoothGattAdapter.getProfileProxy(mAppContext, this, BluetoothGattAdapter.GATT);
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

    synchronized public boolean retrieveGatt(Listener lstnr) {
        if (mGatt != null) {
            // already connected to service, return it
            syncOnRetrievedGatt(lstnr);
            return true;
        } else {
            // still connecting to service, cache it
            mRetrievers.add(lstnr);
            Log.d("add to retrievers");
            return false;
        }
    }

    synchronized private void onGattReady() {
        mGatt = mOngoingGatt;
        mOngoingGatt = null;
        if (mRetrievers.size() != 0) {
            Iterator<Listener> it = mRetrievers.iterator();
            while(it.hasNext()) {
                syncOnRetrievedGatt(it.next());
            }
            mRetrievers.clear();
        }
    }

    private void syncOnRetrievedGatt(Listener lstnr) {
        lstnr.onRetrievedGatt(mGatt);
    }

    @Override
    public void onServiceConnected(int profile, BluetoothProfile proxy) {
        Log.d("onServiceConnected");
        if (profile == BluetoothGattAdapter.GATT) {
            /* Gatt is not completely ready */
            mOngoingGatt = (BluetoothGatt) proxy;
            mOngoingGatt.registerApp(mCallback);
        }
    }

    @Override
    public void onServiceDisconnected(int profile) {
        Log.d("onServiceDisconnected, you cannot use Gatt anymore in this application");
        if (profile == BluetoothGattAdapter.GATT) {
            mGatt = null;
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
                Iterator<Listener> it = mListeners.iterator();
                while(it.hasNext()) {
                    it.next().onCharacteristicChanged(chrc);
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGattCharacteristic chrc, int status) {
            synchronized(mListeners) {
                Iterator<Listener> it = mListeners.iterator();
                while(it.hasNext()) {
                    it.next().onCharacteristicRead(chrc, status);
                }
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGattCharacteristic chrc, int status) {
            synchronized(mListeners) {
                Iterator<Listener> it = mListeners.iterator();
                while(it.hasNext()) {
                    it.next().onCharacteristicWrite(chrc, status);
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
                Iterator<Listener> it = mListeners.iterator();
                while(it.hasNext()) {
                    it.next().onDescriptorRead(descriptor, status);
                }
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGattDescriptor descriptor, int status) {
            synchronized(mListeners) {
                Iterator<Listener> it = mListeners.iterator();
                while(it.hasNext()) {
                    it.next().onDescriptorWrite(descriptor, status);
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

    public interface Listener {
          public void onRetrievedGatt(BluetoothGatt gatt);

          /* to keep compatibility to Samsung SDK */
          public void onAppRegistered(int status);
          public void onCharacteristicChanged(BluetoothGattCharacteristic chrc);
          public void onCharacteristicRead(BluetoothGattCharacteristic chrc, int status);
          public void onCharacteristicWrite(BluetoothGattCharacteristic chrc, int status);
          public void onConnectionStateChange(BluetoothDevice device, int status, int newState);
          public void onDescriptorRead(BluetoothGattDescriptor descriptor, int status);
          public void onDescriptorWrite(BluetoothGattDescriptor descriptor, int status);
          public void onReadRemoteRssi(BluetoothDevice device, int rssi, int status);
          public void onScanResult(BluetoothDevice device, int rssi, byte[] scanRecord);
          public void onServicesDiscovered(BluetoothDevice device, int status);
    }

    public static class ListenerHelper implements Listener {
        String iTag;
        public ListenerHelper(String tag) {
            iTag = tag;
        }

        public void onRetrievedGatt(BluetoothGatt gatt) {
            Log.d(String.format("%s, onRetrievedGatt", iTag));
        }

        public void onAppRegistered(int status) {
            Log.d(String.format("%s, onAppRegistered, status:%d", iTag, status));
        }

        public void onCharacteristicChanged(BluetoothGattCharacteristic chrc) {
            Log.d(String.format("%s, onCharChanged", iTag));
        }

        public void onCharacteristicRead(BluetoothGattCharacteristic chrc, int status) {
            Log.d(String.format("%s, onCharacteristicRead, status:%d", iTag, status));
        }

        public void onCharacteristicWrite(BluetoothGattCharacteristic chrc, int status) {
            Log.d(String.format("%s, onCharacteristicWrite, status:%d", iTag, status));
        }

        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            Log.d(String.format("%s, onConnectionStateChange, status:%d, newState:%d", iTag, status, newState));
        }

        public void onDescriptorRead(BluetoothGattDescriptor descriptor, int status) {
            Log.d(String.format("%s, onDescriptorRead, status:%d", iTag, status));
        }

        public void onDescriptorWrite(BluetoothGattDescriptor descriptor, int status) {
            Log.d(String.format("%s, onDescriptorWrite, status:%d", iTag, status));
        }

        public void onReadRemoteRssi(BluetoothDevice device, int rssi, int status) {
            Log.d(String.format("%s, onReadRemoteRssi, rssi:%d", iTag, rssi));
        }

        public void onScanResult(BluetoothDevice device, int rssi, byte[] scanRecord) {
            Log.d(String.format("%s, onScanResult, rssi:%d", iTag, rssi));
        }

        public void onServicesDiscovered(BluetoothDevice device, int status) {
            Log.d(String.format("%s, onServicesDiscovered", iTag));
        }
    }
}
