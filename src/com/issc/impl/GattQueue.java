// vim: et sw=4 sts=4 tabstop=4
package com.issc.impl;

import com.issc.Bluebit;
import com.issc.util.Log;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Context;

import com.samsung.android.sdk.bt.gatt.BluetoothGattCallback;
import com.samsung.android.sdk.bt.gatt.BluetoothGattCharacteristic;
import com.samsung.android.sdk.bt.gatt.BluetoothGattDescriptor;

public class GattQueue {

    private Object mLock;
    private Consumer mConsumer;

    private ArrayDeque<Transaction> mQueue;
    private Transaction mWorkingTransaction;

    public GattQueue(Consumer consumer) {
        mLock = new Object();
        mQueue = new ArrayDeque<Transaction>();
        mConsumer = consumer;
    }

    public void add(BluetoothGattCharacteristic chr, byte[] value) {
        /* by default, it is write*/
        add(chr, value, true);
    }

    public void add(BluetoothGattCharacteristic chr, byte[] value, boolean isWrite) {
        Transaction t = new Transaction();
        t.chr = chr;
        t.value = value;
        t.isWrite = isWrite;
        addTransaction(t);
    }

    private void addTransaction(Transaction trans) {
        synchronized(mQueue) {
            mQueue.add(trans);
        }
    }

    public void consume() {
        synchronized(mQueue) {
            if (mWorkingTransaction == null) {
                mWorkingTransaction = mQueue.poll();
                if (mWorkingTransaction != null) {
                    // found transaction
                    BluetoothGattCharacteristic chr = mWorkingTransaction.chr;
                    byte[] value = mWorkingTransaction.value;
                    boolean write = mWorkingTransaction.isWrite;
                    Log.d("consumer transact one");
                    mConsumer.onTransact(chr, value, write);
                }
            }
        }
    }

    public void consumedOne() {
        synchronized(mQueue) {
            mWorkingTransaction = null;
        }
    }

    class Transaction {
        BluetoothGattCharacteristic chr;
        byte[] value;
        boolean isWrite;
    }

    public interface Consumer {
        public void onTransact(BluetoothGattCharacteristic chr, byte[] value, boolean isWrite);
    }
}
