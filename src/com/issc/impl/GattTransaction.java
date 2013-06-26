// vim: et sw=4 sts=4 tabstop=4
package com.issc.impl;

import com.issc.Bluebit;
import com.issc.util.Log;
import com.issc.util.TransactionQueue;
import com.issc.util.TransactionQueue.Transaction;

import com.samsung.android.sdk.bt.gatt.BluetoothGatt;
import com.samsung.android.sdk.bt.gatt.BluetoothGattCharacteristic;
import com.samsung.android.sdk.bt.gatt.BluetoothGattDescriptor;

public class GattTransaction implements Transaction {

    public BluetoothGattCharacteristic chr;
    public byte[] value;
    public boolean isWrite;
    public long timeout;

    public GattTransaction(BluetoothGattCharacteristic c, byte[] v) {
        this(c, v, true);
    }

    public GattTransaction(BluetoothGattCharacteristic c, byte[] v, long t) {
        this(c, v, true, t);
    }

    public GattTransaction(BluetoothGattCharacteristic c, byte[] v, boolean w) {
        this(c, v, w, Transaction.TIMEOUT_NONE);
    }

    public GattTransaction(BluetoothGattCharacteristic c,
            byte[] v,
            boolean w,
            long t) {
        chr = c;
        value = v;
        isWrite = w;
        timeout = t;
    }

    @Override
    public long getTimeout() {
        return timeout;
    }
}

