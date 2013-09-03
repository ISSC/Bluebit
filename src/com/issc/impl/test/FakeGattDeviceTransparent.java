// vim: et sw=4 sts=4 tabstop=4
/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.issc.impl.test;

import com.issc.Bluebit;
import com.issc.gatt.Gatt;
import com.issc.gatt.Gatt.Listener;
import com.issc.gatt.GattCharacteristic;
import com.issc.gatt.GattDescriptor;
import com.issc.gatt.GattService;
import com.issc.util.Log;
import com.issc.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.content.Context;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

public class FakeGattDeviceTransparent implements Gatt {

    private final static long DELAY = 200; // 200ms

    private final static String KEY_CHR_UUID = "_key_for_picking_characteristic_by_uuid_";
    private final static String KEY_DSC_UUID = "_key_for_picking_descriptor_by_uuid_";
    private final static int READ_CHR     = 0x0010;
    private final static int READ_DSC     = 0x0011;
    private final static int WRITE_CHR    = 0x0012;
    private final static int WRITE_DSC    = 0x0013;
    private final static int SET_CHR_NOTI = 0x0014;

    private FakeGattService mService;
    private ArrayList<GattService> mSrvs;
    private BluetoothDevice mDevice;
    private Listener mListener;
    private Handler mHandler;
    private HandlerThread mThread;

    public FakeGattDeviceTransparent(BluetoothDevice dev, Listener listener) {
        mDevice = dev;
        mListener = listener;
        mSrvs = new ArrayList<GattService>();

        mThread = new HandlerThread("FakeTransparent");
        mThread.start();
        mHandler = new TransferHandler(mThread.getLooper());
        initServices();
    }

    private void initServices() {
        mService = new FakeGattService(Bluebit.SERVICE_ISSC_PROPRIETARY);
        FakeGattCharacteristic chr;
        List<UUID> uuids = new ArrayList<UUID>();

        uuids.add(Bluebit.DES_CLIENT_CHR_CONFIG);
        chr = new FakeGattCharacteristic(mService, Bluebit.CHR_ISSC_TRANS_TX, uuids);
        chr.setValue(new String("ISSC").getBytes());
        mService.addCharacteristic(chr);

        uuids.clear();
        chr = new FakeGattCharacteristic(mService, Bluebit.CHR_ISSC_TRANS_RX, uuids);
        chr.setValue(new String("FAKE").getBytes());
        mService.addCharacteristic(chr);

        mSrvs.add(mService);
    }

    @Override
    public void close() {
        Log.d("FakeGatt: close");
        mThread.quitSafely();
        mThread = null;
        mHandler = null;
    }

    @Override
    public boolean connect() {
        Log.d("FakeGatt: connect");
        return true;
    }

    @Override
    public void disconnect() {
        Log.d("FakeGatt: disconnect");
    }

    @Override
    public boolean discoverServices() {
        Log.d("FakeGatt: discoverServices");
        return true;
    }

    @Override
    public BluetoothDevice getDevice() {
        return mDevice;
    }

    @Override
    public GattService getService(UUID uuid) {
        return mService;
    }

    @Override
    public List<GattService> getServices() {
        return mSrvs;
    }

    @Override
    public boolean readCharacteristic(GattCharacteristic chr) {
        Log.d("FakeGatt: read characteristic: " + Util.BytesToHex(chr.getValue()));

        Message msg = mHandler.obtainMessage(READ_CHR);
        Bundle data = msg.getData();
        data.putSerializable(KEY_CHR_UUID, chr.getUuid());
        mHandler.sendMessageDelayed(msg, DELAY);

        return true;
    }

    @Override
    public boolean readDescriptor(GattDescriptor dsc) {
        Log.d("FakeGatt: read descriptor: " + Util.BytesToHex(dsc.getValue()));

        Message msg = mHandler.obtainMessage(READ_DSC);
        Bundle data = msg.getData();
        data.putSerializable(KEY_CHR_UUID, dsc.getCharacteristic().getUuid());
        data.putSerializable(KEY_DSC_UUID, dsc.getUuid());
        mHandler.sendMessageDelayed(msg, DELAY);

        return true;
    }

    @Override
    public boolean setCharacteristicNotification(GattCharacteristic chr, boolean enable) {
        Log.d("FakeGatt: set characteristic notification: " + enable);

        Message msg = mHandler.obtainMessage(SET_CHR_NOTI);
        Bundle data = msg.getData();
        data.putSerializable(KEY_CHR_UUID, chr.getUuid());
        mHandler.sendMessageDelayed(msg, DELAY);

        return true;
    }

    @Override
    public boolean writeCharacteristic(GattCharacteristic chr) {
        Log.d("FakeGatt: write characteristic: " + Util.BytesToHex(chr.getValue()));

        Message msg = mHandler.obtainMessage(WRITE_CHR);
        Bundle data = msg.getData();
        data.putSerializable(KEY_CHR_UUID, chr.getUuid());
        mHandler.sendMessageDelayed(msg, DELAY);

        return true;
    }

    @Override
    public boolean writeDescriptor(GattDescriptor dsc) {
        Log.d("FakeGatt: write descriptor: " + Util.BytesToHex(dsc.getValue()));

        Message msg = mHandler.obtainMessage(WRITE_DSC);
        Bundle data = msg.getData();
        data.putSerializable(KEY_CHR_UUID, dsc.getCharacteristic().getUuid());
        data.putSerializable(KEY_DSC_UUID, dsc.getUuid());
        mHandler.sendMessageDelayed(msg, DELAY);

        return true;
    }

    class TransferHandler extends Handler {
        TransferHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            int tag = msg.what;
            UUID uuidChr = (UUID)msg.getData().getSerializable(KEY_CHR_UUID);
            UUID uuidDsc = (UUID)msg.getData().getSerializable(KEY_DSC_UUID);
            if (tag == READ_CHR) {
                GattCharacteristic chr = mService.getCharacteristic(uuidChr);
                mListener.onCharacteristicRead(
                        FakeGattDeviceTransparent.this, chr, Gatt.GATT_SUCCESS);
            } else if (tag == READ_DSC) {
                GattCharacteristic chr = mService.getCharacteristic(uuidChr);
                GattDescriptor dsc = chr.getDescriptor(uuidDsc);
                mListener.onDescriptorRead(
                        FakeGattDeviceTransparent.this, dsc, Gatt.GATT_SUCCESS);
            } else if (tag == WRITE_CHR) {
                GattCharacteristic chr = mService.getCharacteristic(uuidChr);
                mListener.onCharacteristicWrite(
                        FakeGattDeviceTransparent.this, chr, Gatt.GATT_SUCCESS);
            } else if (tag == WRITE_DSC) {
                GattCharacteristic chr = mService.getCharacteristic(uuidChr);
                GattDescriptor dsc = chr.getDescriptor(uuidDsc);
                mListener.onDescriptorWrite(
                        FakeGattDeviceTransparent.this, dsc, Gatt.GATT_SUCCESS);
            } else if (tag == SET_CHR_NOTI) {
                GattCharacteristic chr = mService.getCharacteristic(uuidChr);
                mListener.onCharacteristicChanged(
                        FakeGattDeviceTransparent.this, chr);
            } else {
                Log.e("Oops, message with unknown tag");
            }
        }
    }

 }

