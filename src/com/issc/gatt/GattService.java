// vim: et sw=4 sts=4 tabstop=4
package com.issc.gatt;

import com.issc.Bluebit;
import com.issc.util.Log;

import java.util.List;
import java.util.UUID;

/**
 * This is a wrapper.
 *
 * It will be an interface to help us to avoid depending on any specific
 * platform.
 **/
public interface GattService {
    public Object getImpl();

    public GattCharacteristic getCharacteristic(UUID uuid);
    public List<GattCharacteristic> getCharacteristics();
    public int getInstanceId();
    public int getType();
    public UUID getUuid();
}

