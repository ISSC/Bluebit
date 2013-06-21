// vim: et sw=4 sts=4 tabstop=4
package com.issc.util;

import com.issc.Bluebit;
import com.issc.R;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
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

