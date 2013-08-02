// vim: et sw=4 sts=4 tabstop=4
package com.issc.impl;

import com.issc.Bluebit;
import com.issc.util.Log;
import com.issc.util.TransactionQueue;
import com.issc.util.TransactionQueue.Transaction;

import com.issc.gatt.GattCharacteristic;
import com.issc.gatt.GattDescriptor;

import com.samsung.android.sdk.bt.gatt.BluetoothGatt;

public class GattTransaction implements Transaction {

    public GattCharacteristic chr;
    public GattDescriptor     desc;
    public byte[] value;
    public boolean isWrite;
    public long timeout;

    public GattTransaction(GattCharacteristic c, byte[] v) {
        this(c, v, true);
    }

    public GattTransaction(GattCharacteristic c, byte[] v, long t) {
        this(c, v, true, t);
    }

    public GattTransaction(GattCharacteristic c, byte[] v, boolean w) {
        this(c, v, w, Transaction.TIMEOUT_NONE);
    }

    public GattTransaction(GattCharacteristic c,
            byte[] v,
            boolean w,
            long t) {
        chr = c;
        value = v;
        isWrite = w;
        timeout = t;
    }

    public boolean isForCharacteristic() {
        return (chr != null);
    }

    public GattTransaction(GattDescriptor d, byte[] v) {
        this(d, v, true);
    }

    public GattTransaction(GattDescriptor d, byte[] v, long t) {
        this(d, v, true, t);
    }

    public GattTransaction(GattDescriptor d, byte[] v, boolean w) {
        this(d, v, w, Transaction.TIMEOUT_NONE);
    }

    public GattTransaction(GattDescriptor d,
            byte[] v,
            boolean w,
            long t) {
        desc = d;
        value = v;
        isWrite = w;
        timeout = t;
    }

    public boolean isForDescriptor() {
        return (desc != null);
    }

    @Override
    public long getTimeout() {
        return timeout;
    }
}

