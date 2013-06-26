// vim: et sw=4 sts=4 tabstop=4
package com.issc.util;

import com.issc.util.Log;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TransactionQueue {

    private Object mLock;
    private Consumer mConsumer;

    private ArrayDeque<Transaction> mQueue;
    private Transaction mWorkingTransaction;

    public TransactionQueue(Consumer consumer) {
        mLock = new Object();
        mQueue = new ArrayDeque<Transaction>();
        mConsumer = consumer;
    }

    public void add(Transaction t) {
        addTransaction(t);
    }

    public void clear() {
        synchronized(mQueue) {
            mQueue.clear();
            mWorkingTransaction = null;
        }
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
                    Log.d("ask consumer to transact one transaction");
                    mConsumer.onTransact(mWorkingTransaction);
                }
            }
        }
    }

    public void consumedOne() {
        synchronized(mQueue) {
            mWorkingTransaction = null;
        }
        consume();
    }

    public interface Transaction {
        public boolean isKeepingOrder();
    }

    public interface Consumer<T extends Transaction> {
        public void onTransact(T transaction);
    }
}
