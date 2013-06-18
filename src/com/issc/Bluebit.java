// vim: et sw=4 sts=4 tabstop=4
package com.issc;

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

    public final static String[] UUIDS_OF_LIGHTING = {
        "49535343-fe7d-4ae5-8fa9-9fafd205e455",
        "00001800-0000-1000-8000-00805f9b34fb",
        "0000180a-0000-1000-8000-00805f9b34fb",
        "00001802-0000-1000-8000-00805f9b34fb",
        "00001803-0000-1000-8000-00805f9b34fb",
        "00001804-0000-1000-8000-00805f9b34fb",
        "00001815-0000-1000-8000-00805f9b34fb"
    };
}



