// vim: et sw=4 sts=4 tabstop=4
package com.issc.util;

import com.issc.util.Log;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

public class TransactionQueue {

    private Consumer mConsumer;

    private Handler mHandler;

    private ArrayDeque<Transaction> mQueue;
    private Transaction mWorkingTransaction;

    private final static int TRANSACTION_DONE = 0x9527;
    private final static int REQUEST_CONSUME  = 0x9528;

    public TransactionQueue(Consumer consumer) {
        HandlerThread thread = new HandlerThread("TransactionQueue");
        thread.start();
        mHandler = new QueueHandler(thread.getLooper());
        mQueue = new ArrayDeque<Transaction>();
        mConsumer = consumer;
    }

    public void add(Transaction t) {
        addTransaction(t);
        process();
    }

    public void clear() {
        synchronized(mQueue) {
            mQueue.clear();
            mWorkingTransaction = null;
        }
    }

    public void process() {
        requestConsume();
    }

    public void onConsumed() {
        doneTransaction(0);
    }

    public void destroy() {
        clear();
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
        mConsumer = null;
    }

    private void addTransaction(Transaction trans) {
        synchronized(mQueue) {
            mQueue.add(trans);
        }
    }

    private void requestConsume() {
        mHandler.removeMessages(REQUEST_CONSUME);

        Message msg = mHandler.obtainMessage(REQUEST_CONSUME);
        msg.what = REQUEST_CONSUME;
        mHandler.sendMessage(msg);
    }

    private void doneTransaction(long ms) {
        mHandler.removeMessages(TRANSACTION_DONE);
        Message msg = mHandler.obtainMessage(TRANSACTION_DONE);
        msg.what = TRANSACTION_DONE;

        if (ms > 0) {
            mHandler.sendMessageDelayed(msg, ms);
        } else {
            mHandler.sendMessage(msg);
        }
    }

    class QueueHandler extends Handler {
        QueueHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            int tag = msg.what;
            if (tag == REQUEST_CONSUME) {
                onRequestConsume();
            } else if (tag == TRANSACTION_DONE) {
                onWorkingTransactionDone();
            }
        }

        private void onRequestConsume() {
            synchronized(mQueue) {
                if (mWorkingTransaction != null) {
                    // there is already an ongoing transaction
                    return;
                }

                mWorkingTransaction = mQueue.poll();
                if (mWorkingTransaction != null) {
                    long timeout = mWorkingTransaction.getTimeout();
                    if (timeout != Transaction.TIMEOUT_NONE) {
                        // this request will not cause onConsumed although
                        // it already complete the transaction.
                        // we need request next transaction manually.
                        doneTransaction(timeout);
                    }

                    // found transaction
                    Log.d("ask consumer to transact one transaction");
                    mConsumer.onTransact(mWorkingTransaction);
                }
            }
        }

        private void onWorkingTransactionDone() {
            synchronized(mQueue) {
                mWorkingTransaction = null;
            }
            // finish one, request next transaction
            requestConsume();
        }
    }

    public interface Transaction {
        public final static long TIMEOUT_NONE = -999;

        public long getTimeout();
    }

    public interface Consumer<T extends Transaction> {
        public void onTransact(T transaction);
    }

}
