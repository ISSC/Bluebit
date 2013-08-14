// vim: et sw=4 sts=4 tabstop=4
package com.issc.gatt;

import com.issc.Bluebit;
import com.issc.util.Log;

import java.util.List;
import java.util.UUID;

import android.bluetooth.BluetoothDevice;

/**
 * This is a wrapper.
 *
 * It will be an interface to help us to avoid depending on any specific
 * platform.
 **/
public interface GattDescriptor {

    public final static int ENABLE_NOTIFICATION_VALUE  = 0x5987; // just a type
    public final static int DISABLE_NOTIFICATION_VALUE = ENABLE_NOTIFICATION_VALUE + 1;

    public Object getImpl();

    /* we cannot provide byte[] data directly since the implementation might be different
     * from various platform.*/
    public byte[] getConstantBytes(int type);

    public int getPermissions();
    public UUID getUuid();
    public byte[] getValue();
    public boolean setValue(byte[] value);
}

