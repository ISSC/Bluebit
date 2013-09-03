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

package com.issc.util;

import com.issc.Bluebit;
import com.issc.gatt.GattCharacteristic;
import com.issc.gatt.GattDescriptor;
import com.issc.gatt.GattService;
import com.issc.R;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class Util {

    private final static String TAG = Bluebit.TAG;

    private final static String ENCODE = "UTF-8";
    private final static String sPREFIX = "0000";
    private final static String sPOSTFIX = "-0000-1000-8000-00805f9b34fb";

    private final static Map<Integer, BtClass> sMap
                            = new HashMap<Integer, BtClass>();

    private final static BluetoothAdapter sAdapter
                            = BluetoothAdapter.getDefaultAdapter();
    private Util() {
        // this is just a helper class.
    }

    public static boolean isBluetoothSupported() {
        return (sAdapter!= null);
    }

    public static boolean isBluetoothEnabled() {
        return (sAdapter != null) && (sAdapter.isEnabled());
    }

    public static void enableBluetooth(Activity act, int code) {
        Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        act.startActivityForResult(i, code);
    }

    public static boolean startDiscovery() {
        if (isBluetoothEnabled()) {
            // according to documentation, it is highly recommendation
            // to cancel ongoing discovery before start new one.
            // FIXME: on S4, cancel discovery cause one more DISCOVERY_FINISHED
            sAdapter.cancelDiscovery();
            return sAdapter.startDiscovery();
        }
        return false;
    }

    public static boolean stopDiscovery() {
        if (isBluetoothEnabled()) {
            return sAdapter.cancelDiscovery();
        }
        return false;
    }

    public static boolean isDiscovering() {
        if (isBluetoothEnabled()) {
            return sAdapter.isDiscovering();
        }

        return false;
    }

    public static Set<BluetoothDevice> getBondedDevices() {
        if (isBluetoothEnabled()) {
            return sAdapter.getBondedDevices();
        }

        return null;
    }

    public static int getDeviceClassRes(int major) {
        BtClass c = sMap.get(new Integer(major));
        if (c == null) {
            return -1;
        } else {
            return c.iDescRes;
        }
    }

    public static UUID uuidFromStr(String str) {
        if (!str.matches(".{4}")) {
            return null;
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(sPREFIX);
            sb.append(str);
            sb.append(sPOSTFIX);
            return UUID.fromString(sb.toString());
        }
    }

    public static void writeStrToFile(String path, CharSequence data) throws IOException {
        File file = new File(path);
        FileOutputStream fos = new FileOutputStream(file);
        writeStrToStream(fos, data);
    }

    public static byte[] readBytesFromFile(String path) throws IOException {
        File file = new File(path);
        FileInputStream fis = new FileInputStream(file);
        return readBytesFromStream(fis);
    }

    public static byte[] readBytesFromStream(InputStream str) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int count = 0;
        while ((count = str.read(buf, 0, buf.length)) != -1) {
            output.write(buf, 0, count);
        }

        output.flush();
        return output.toByteArray();
    }

    public static CharSequence readStrFromFile(String path) throws IOException {
        File file = new File(path);
        FileInputStream fis = new FileInputStream(file);
        return readStrFromStream(fis);
    }

    public static CharSequence readStrFromStream(InputStream str) throws IOException {
        InputStreamReader isr = new InputStreamReader(str, ENCODE);
        BufferedReader reader = new BufferedReader(isr);
        StringBuilder builder = new StringBuilder();
        String line = null;
        while((line = reader.readLine()) != null) {
            builder.append(line);
        }

        return builder;
    }

    public static void writeStrToStream(OutputStream str, CharSequence data) throws IOException {
        OutputStreamWriter osr = new OutputStreamWriter(str, ENCODE);
        BufferedWriter writer  = new BufferedWriter(osr);
        System.out.println("length:" + data.length());
        writer.write(data.toString(), 0, data.length());
        writer.close();
        return;
    }

    public static String BytesToHex(byte[] data) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            sb.append(String.format("%02x", data[i]));
        }

        return sb.toString();
    }

    public static String getMD5FromBytes(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hash = digest.digest(data);
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < hash.length; i++) {
                sb.append(String.format("%02x", hash[i]));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void dumpServices(List<GattService> list) {
        Iterator<GattService> it = list.iterator();
        while (it.hasNext()) {
            dumpService(it.next());
        }
    }

    public static void dumpService(GattService srv) {
        Log.d(String.format("  Service uuid: %s,", srv.getUuid().toString()));
        List<GattCharacteristic> list = srv.getCharacteristics();
        if (list == null || list.size() <= 0) {
            Log.d("    ...without characteristic");
        }

        Iterator<GattCharacteristic> it = list.iterator();
        while (it.hasNext()) {
            dumpChr(it.next());
        }
    }

    public static void dumpChr(GattCharacteristic chr) {
        Log.d(String.format("    chr uuid: %s,", chr.getUuid().toString()));
        List<GattDescriptor> list = chr.getDescriptors();
        if (list == null || list.size() <= 0) {
            Log.d("    ...without descriptor");
        }

        Iterator<GattDescriptor> it = list.iterator();
        while (it.hasNext()) {
            dumpDesc(it.next());
        }
    }

    public static void dumpDesc(GattDescriptor desc) {
        Log.d(String.format("        desc uuid: %s, permission:0x%x",
                    desc.getUuid().toString(),
                    desc.getPermissions()));

        if (desc.getValue() != null) {
            Log.d("          value length =" + desc.getValue().length);
        }
    }


    private static int[] sMajorType = {
        BluetoothClass.Device.Major.AUDIO_VIDEO,
        BluetoothClass.Device.Major.COMPUTER,
        BluetoothClass.Device.Major.HEALTH,
        BluetoothClass.Device.Major.IMAGING,
        BluetoothClass.Device.Major.MISC,
        BluetoothClass.Device.Major.NETWORKING,
        BluetoothClass.Device.Major.PERIPHERAL,
        BluetoothClass.Device.Major.PHONE,
        BluetoothClass.Device.Major.TOY,
        BluetoothClass.Device.Major.UNCATEGORIZED,
        BluetoothClass.Device.Major.WEARABLE
    };

    private static int[] sMajorDescriptorRes = {
        R.string.device_class_audiovideo,
        R.string.device_class_computer,
        R.string.device_class_health,
        R.string.device_class_imaging,
        R.string.device_class_misc,
        R.string.device_class_networking,
        R.string.device_class_peripheral,
        R.string.device_class_phone,
        R.string.device_class_toy,
        R.string.device_class_uncategorized,
        R.string.device_class_wearable
    };

    static {
        for (int i = 0; i < sMajorType.length; i++) {
            BtClass c = new BtClass(sMajorType[i],
                                sMajorDescriptorRes[i]);
            sMap.put(new Integer(c.iType), c);
        }
    }

    static class BtClass {
        int iType = -1;
        int iDescRes = -1;
        BtClass(int type, int descres) {
            iType = type;
            iDescRes = descres;
        }
    }
}

