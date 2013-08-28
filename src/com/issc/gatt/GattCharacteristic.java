// vim: et sw=4 sts=4 tabstop=4
package com.issc.gatt;

import com.issc.Bluebit;

import java.util.List;
import java.util.UUID;

/**
 * This is a wrapper.
 *
 * It will be an interface to help us to avoid depending on any specific
 * platform.
 **/
public interface GattCharacteristic {

    public final static int WRITE_TYPE_DEFAULT = 2;
    public final static int WRITE_TYPE_NO_RESPONSE = 1;

    public Object getImpl();

    public GattService getService();
    public GattDescriptor getDescriptor(UUID uuid);
    public List<GattDescriptor> getDescriptors();
    public Integer getIntValue(int type, int offset);
    public int getPermissions();
    public int getProperties();
    public UUID getUuid();
    public byte[] getValue();
    public boolean setValue(byte[] value);
    public void setWriteType(int writeType);
}

