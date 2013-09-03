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

public class FakeGattDeviceInfo implements Gatt {

    private FakeGattService mService;
    private ArrayList<GattService> mSrvs;
    private BluetoothDevice mDevice;
    private Listener mListener;

    public FakeGattDeviceInfo(BluetoothDevice dev, Listener listener) {
        mDevice = dev;
        mListener = listener;
        mSrvs = new ArrayList<GattService>();
        initServices();
    }

    private void initServices() {
        mService = new FakeGattService(Bluebit.SERVICE_DEVICE_INFO);
        FakeGattCharacteristic chr;

        chr = new FakeGattCharacteristic(mService, Bluebit.CHR_MANUFACTURE_NAME);
        chr.setValue(new String("ISSC").getBytes());
        mService.addCharacteristic(chr);

        chr = new FakeGattCharacteristic(mService, Bluebit.CHR_MODEL_NUMBER);
        chr.setValue(new String("FAKE").getBytes());
        mService.addCharacteristic(chr);

        chr = new FakeGattCharacteristic(mService, Bluebit.CHR_SERIAL_NUMBER);
        chr.setValue(new String("9527").getBytes());
        mService.addCharacteristic(chr);

        chr = new FakeGattCharacteristic(mService, Bluebit.CHR_HARDWARE_REVISION);
        chr.setValue(new String("1984").getBytes());
        mService.addCharacteristic(chr);

        chr = new FakeGattCharacteristic(mService, Bluebit.CHR_FIRMWARE_REVISION);
        chr.setValue(new String("2001").getBytes());
        mService.addCharacteristic(chr);

        chr = new FakeGattCharacteristic(mService, Bluebit.CHR_SOFTWARE_REVISION);
        chr.setValue(new String("42").getBytes());
        mService.addCharacteristic(chr);

        mSrvs.add(mService);
    }

    @Override
    public void close() {
        Log.d("FakeGatt: close");
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
        return new FakeGattService(uuid);
    }

    @Override
    public List<GattService> getServices() {
        return mSrvs;
    }

    @Override
    public boolean readCharacteristic(GattCharacteristic chr) {
        Log.d("FakeGatt: read characteristic: " + Util.BytesToHex(chr.getValue()));
        return true;
    }

    @Override
    public boolean readDescriptor(GattDescriptor dsc) {
        Log.d("FakeGatt: read descriptor: " + Util.BytesToHex(dsc.getValue()));
        return true;
    }

    @Override
    public boolean setCharacteristicNotification(GattCharacteristic chr, boolean enable) {
        Log.d("FakeGatt: set characteristic notification: " + enable);
        return true;
    }

    @Override
    public boolean writeCharacteristic(GattCharacteristic chr) {
        Log.d("FakeGatt: write characteristic: " + Util.BytesToHex(chr.getValue()));
        return true;
    }

    @Override
    public boolean writeDescriptor(GattDescriptor dsc) {
        Log.d("FakeGatt: write descriptor: " + Util.BytesToHex(dsc.getValue()));
        return true;
    }
}

