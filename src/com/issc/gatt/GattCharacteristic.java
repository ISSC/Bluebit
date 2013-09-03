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

