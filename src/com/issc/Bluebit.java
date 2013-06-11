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
}
