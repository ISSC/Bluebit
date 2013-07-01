// vim: et sw=4 sts=4 tabstop=4
package com.issc;

import com.issc.util.Util;
import java.util.UUID;

/**
 * This is the class to store global constant.
 */

public final class Bluebit {

    public final static String TAG = "Bluebit";

    public final static String CHOSEN_DEVICE = "the_device_been_choosen";

    /* request code for Activities-communication */
    private final static int _REQ_START = 0x9527; // just random number
    public final static int REQ_CHOOSE_DEVICE = _REQ_START + 1;

    private Bluebit() {
        // Hide constructor since you should
        // never instantiate me.
    }

    /* Automation IO service */
    public final static UUID SERVICE_AUTOMATION_IO = Util.uuidFromStr("1815");
    public final static UUID CHR_DIGITAL_IN  = Util.uuidFromStr("2a56");
    public final static UUID CHR_DIGITAL_OUT = Util.uuidFromStr("2a57");
    public final static UUID CHR_ANALOG_IN   = Util.uuidFromStr("2a58");
    public final static UUID CHR_ANALOG_OUT  = Util.uuidFromStr("2a59");
    public final static UUID CHR_AGG_INPUT   = Util.uuidFromStr("2a5a");
    public final static UUID CUSTOM_CHR_DI_DESC = UUID.fromString("49535343-6C1F-401D-BAA3-EC966D1A3AA1");
    public final static UUID CUSTOM_CHR_DO_DESC = UUID.fromString("49535343-F82E-4B2B-847C-DBEA67318E35");
    public final static UUID CUSTOM_CHR_AO1_DESC = UUID.fromString("49535343-A742-442B-9D20-24C6709FBD16");
    public final static UUID CUSTOM_CHR_AI1_DESC = UUID.fromString("49535343-B011-4081-9C96-C3990D17A69E");

    public final static UUID DES_USER_DESCRIPTION      = Util.uuidFromStr("2901");
    public final static UUID DES_DIGITAL_NUMBER        = Util.uuidFromStr("2909");
    public final static UUID DES_INPUT_TRIGGER_SETTING = Util.uuidFromStr("290A");

    /* battery service */
    public final static UUID SERVICE_BATTERY       = Util.uuidFromStr("180F");
    public final static UUID CHR_BATTERY_LEVEL     = Util.uuidFromStr("2A19");

    /* Tx Power service */
    public final static UUID SERVICE_TX_POWER      = Util.uuidFromStr("1804");
    public final static UUID CHR_TX_POWER_LEVEL    = Util.uuidFromStr("2A07");

    /* Link Loss service */
    public final static UUID SERVICE_LINK_LOSS     = Util.uuidFromStr("1803");
    public final static UUID CHR_ALERT_LEVEL       = Util.uuidFromStr("2A06");

    /* Immediate Alert service */
    public final static UUID SERVICE_IMMEDIATE_ALERT = Util.uuidFromStr("1802");

    /* Device Info service */
    public final static UUID SERVICE_DEVICE_INFO     = Util.uuidFromStr("180A");
    public final static UUID CHR_MANUFACTURE_NAME   = Util.uuidFromStr("2A29");
    public final static UUID CHR_MODEL_NUMBER       = Util.uuidFromStr("2A24");
    public final static UUID CHR_SERIAL_NUMBER      = Util.uuidFromStr("2A25");
    public final static UUID CHR_HARDWARE_REVISION  = Util.uuidFromStr("2A27");
    public final static UUID CHR_FIRMWARE_REVISION  = Util.uuidFromStr("2A26");
    public final static UUID CHR_SOFTWARE_REVISION  = Util.uuidFromStr("2A28");

    /* ISSC Proprietary */
    public final static  UUID SERVICE_ISSC_PROPRIETARY  = UUID.fromString("49535343-FE7D-4AE5-8FA9-9FAFD205E455");
    public final static  UUID CHR_CONNECTION_PARAMETER  = UUID.fromString("49535343-6DAA-4D02-ABF6-19569ACA69FE");
    public final static  UUID CHR_AIR_PATCH             = UUID.fromString("49535343-ACA3-481C-91EC-D85E28A60318");
    public final static  UUID CHR_ISSC_TRANS_TX         = UUID.fromString("49535343-1E4D-4BD9-BA61-23C647249616");
    public final static  UUID CHR_ISSC_TRANS_RX         = UUID.fromString("49535343-8841-43F4-A8D4-ECBE34729BB3");
    public final static  UUID CHR_ISSC_MP               = UUID.fromString("49535343-ACA3-481C-91EC-D85E28A60318");

    public final static byte[] CMD_WRITE_E2PROM = {(byte)0x0b};
    public final static byte[] CMD_READ_E2PROM  = {(byte)0x0a};
    public final static byte[] CMD_WRITE_MEMORY = {(byte)0x09};
    public final static byte[] ADDR_E2PROM_NAME = {(byte)0x00, (byte)0x0b};
    public final static byte[] ADDR_MEMORY_NAME = {(byte)0x4e, (byte)0x0b};
    public final static int NAME_MAX_SIZE = 16;


    /* Client Characteristic Configuration Descriptor */
    public final static UUID DES_CLIENT_CHR_CONFIG = Util.uuidFromStr("2902");

    public final static UUID[] UUIDS_OF_LIGHTING = {
        SERVICE_AUTOMATION_IO,
        CHR_DIGITAL_OUT,
        CHR_ANALOG_OUT,
        CUSTOM_CHR_AO1_DESC,
        SERVICE_ISSC_PROPRIETARY
    };

    public final static UUID[] UUIDS_OF_TRANSPARENT = {
        SERVICE_ISSC_PROPRIETARY,
        CHR_ISSC_TRANS_TX,
        CHR_ISSC_TRANS_RX
    };

}



