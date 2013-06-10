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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class Util {

    private final static String TAG = Bluebit.TAG;

    private final static Map<Integer, BtClass> sMap
                            = new HashMap<Integer, BtClass>();

    private Util() {
        // this is just a helper class.
    }

    public static boolean isBluetoothSupported() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        return (adapter!= null);
    }

    public static boolean isBluetoothEnabled() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        return (adapter != null) && (adapter.isEnabled());
    }

    public static void enableBluetooth(Activity act, int code) {
        Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        act.startActivityForResult(i, code);
    }

    public static boolean startDiscovery() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null && adapter.isEnabled()) {
            // according to documentation, it is highly recommendation
            // to cancel ongoing discovery before start new one.
            adapter.cancelDiscovery();
            return adapter.startDiscovery();
        }
        return false;
    }

    public static boolean stopDiscovery() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null && adapter.isEnabled()) {
            return adapter.cancelDiscovery();
        }
        return false;
    }

    public static boolean isDiscovering() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null && adapter.isEnabled()) {
            return adapter.isDiscovering();
        }

        return false;
    }

    public static Set<BluetoothDevice> getBondedDevices() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null && adapter.isEnabled()) {
            return adapter.getBondedDevices();
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

