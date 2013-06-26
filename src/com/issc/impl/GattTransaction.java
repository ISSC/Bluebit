// vim: et sw=4 sts=4 tabstop=4
package com.issc.impl;

import com.issc.Bluebit;
import com.issc.util.Log;
import com.issc.util.TransactionQueue;

import com.samsung.android.sdk.bt.gatt.BluetoothGatt;
import com.samsung.android.sdk.bt.gatt.BluetoothGattCharacteristic;
import com.samsung.android.sdk.bt.gatt.BluetoothGattDescriptor;

public class GattTransaction implements TransactionQueue.Transaction {

    public BluetoothGattCharacteristic chr;
    public byte[] value;
    public boolean isWrite;
    public boolean isOrdered;

    public GattTransaction(BluetoothGattCharacteristic c, byte[] v) {
        this(c, v, true);
    }

    public GattTransaction(BluetoothGattCharacteristic c, byte[] v, boolean w) {
        chr = c;
        value = v;
        isWrite = w;
    }

    public void setOrdered(boolean ordered) {
        isOrdered = false;
    }

    @Override
    public boolean isKeepingOrder() {
        return isOrdered;
    }
}

