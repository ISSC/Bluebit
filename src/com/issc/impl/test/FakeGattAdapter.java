// vim: et sw=4 sts=4 tabstop=4
package com.issc.impl.test;

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
import android.bluetooth.BluetoothManager;
import android.content.Context;

public class FakeGattAdapter implements GattAdapter {

    private List<BluetoothDevice> mDevs;

    private BluetoothAdapter mAdapter;
    private BluetoothDevice mDevInfo;
    private Gatt mGattInterface;

    public FakeGattAdapter(Context ctx, Listener listener) {
        BluetoothManager mgr = (BluetoothManager)ctx.getSystemService(Context.BLUETOOTH_SERVICE);
        mAdapter = mgr.getAdapter();
        initDevices();
    }

    private void initDevices() {
        mDevInfo = mAdapter.getRemoteDevice("00:11:DE:AD:BE:EF");
    }

    @Override
    public Gatt connectGatt(Context ctx, boolean autoConnect, Listener listener, BluetoothDevice dev) {
        mGattInterface = new FakeGattDeviceInfo(mDevInfo, listener);
        return mGattInterface;
    }


    @Override
    public boolean startLeScan(GattAdapter.LeScanCallback callback) {
        callback.onLeScan(mDevInfo, 0, new byte[1]);
        return true;
    }

    @Override
    public void stopLeScan(GattAdapter.LeScanCallback callback) {
    }

    @Override
    public int getConnectionState(BluetoothDevice device) {
        return BluetoothAdapter.STATE_CONNECTED;
    }

    @Override
    public List<BluetoothDevice> getConnectedDevices() {
        List<BluetoothDevice> devs = new ArrayList<BluetoothDevice>();
        devs.add(mDevInfo);
        return devs;
    }

    @Override
    public boolean removeBond(BluetoothDevice dev) {
        return true;
    }
}

