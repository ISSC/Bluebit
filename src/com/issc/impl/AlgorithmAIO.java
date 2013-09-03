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

package com.issc.impl;

import com.issc.util.Log;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class AlgorithmAIO {

    private final static int NUM = 7;

    /* LED# -> data index */
    private final static int[] INDEX = {
        5, // LED 0
        3, // LED 1
        2, // LED 2
        6, // LED 3
        1, // LED 4
        0, // LED 5
        4  // LED 6
    };

    /* reverse: data index -> LED#*/
    private final static int[] R_INDEX = {
        5,
        4,
        2,
        1,
        6,
        0,
        3
    };

    /**
     * Get control value in byte arrary to Trun on/off one LED.
     */
    public static byte[] ctrlDigital(boolean[] leds, Controllable callback) {
        final int width = 2; // each LED occupy 2 bits
        int value = 0x0000; // 0000 0000 0000 0000
        for (int i = 0; i < NUM; i++) {
            int idx = INDEX[i];
            // on=01b off=00b
            int ctrl = leds[i] ? 0x1 : 0x0;
            value = value | (ctrl<<(idx * width));
        }
        byte[] b = {(byte)(value>>8), (byte)(value)};
        callback.onControllDigital(b);
        return b;
    }

    /**
     * To know the LEDs are turned on/off.
     */
    public static boolean[] parseLEDState(byte[] state) {
        int v = 0xff00 & (state[0]<<8)
            | 0xff & state[1];
        boolean[] result = new boolean[NUM];
        int last = result.length - 1;
        for (int i = 0; i <= last ; i++) {
            result[R_INDEX[i]] = ((v>>(i*2)) & 0x1) > 0;
        }

        return result;
    }

    /**
     * get 2 dimention array to control PWM.
     */
    public static byte[][] ctrlPWM(int r, int g, int b, Controllable callback) {
        ByteBuffer bf;
        ArrayList<ByteBuffer> bufs = new ArrayList<ByteBuffer>();

        bufs.add(getPWMSetting(r, g, b));
        if (r > 0) {
            bufs.add(getDuty(r));
        }

        if (g > 0) {
            bufs.add(getDuty(g));
        }

        if (b > 0) {
            bufs.add(getDuty(b));
        }

        byte[][] result = new byte[bufs.size()][];
        for (int i = 0; i < bufs.size(); i++) {
            result[i] = bufs.get(i).array();
        }

        callback.onControllPWM(r, g, b, result);
        return result;
    }

    private static boolean sRunning = false;

    public static void stopAutoPattern() {
        sRunning = false;
    }

    /**
     * start Auto pattern to control LED
     */
    public static void startAutoPattern1(final Controllable ctrl) {
        final long mPeriod = 200;
        resetLED(ctrl);
        sRunning = true;
        Thread runner = new Thread() {
            public void run() {
                int now = 0;
                int inc = 1;
                boolean[] leds = new boolean[NUM];
                try {
                    while (sRunning) {
                        sleep(mPeriod);
                        if (now == (NUM - 1) && (inc == 1)) {
                            /* increasing and reach max, start decreasing */
                            inc = -1;
                        } else if ((now == 0) && (inc == -1)) {
                            /* decreasing and reach min, start increasing */
                            inc = 1;
                        }
                        leds[now] = false;
                        now += inc;
                        leds[now] = true;
                        ctrlDigital(leds, ctrl);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                sRunning = false;
                ctrl.onStopControll();
            }
        };

        runner.start();
    }

    /**
     * start Auto pattern to control LED
     */
    public static void startAutoPattern2(final Controllable ctrl) {
        final long mPeriod = 500;
        resetLED(ctrl);
        sRunning = true;
        Thread runner = new Thread() {
            public void run() {
                boolean mFlip = true;
                boolean[] leds = new boolean[NUM];
                try {
                    while (sRunning) {
                        sleep(mPeriod);
                        leds[0] = mFlip;
                        leds[1] = !mFlip;
                        leds[2] = mFlip;
                        leds[3] = !mFlip;
                        leds[4] = mFlip;
                        leds[5] = !mFlip;
                        leds[6] = mFlip;
                        mFlip = !mFlip;
                        ctrlDigital(leds, ctrl);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                sRunning = false;
                ctrl.onStopControll();
            }
        };

        runner.start();
    }

    /**
     * start Auto pattern to control PWM
     */
    public static void startAutoPattern3(final Controllable ctrl) {
        final long mPeriod = 500;
        resetLED(ctrl);
        sRunning = true;
        Thread runner = new Thread() {
            public void run() {
                int[] rgb = new int[3];
                int inc = 1;
                int idx = 0, level = 0;
                try {
                    while (sRunning) {
                        sleep(mPeriod);
                        if (inc == 1 && rgb[idx] >= 9) {
                            idx++;
                        } else if (inc==-1 && rgb[idx] <= 0) {
                            idx++;
                        }

                        if (idx == 3) {
                            idx = 0;
                            inc = (inc == 1) ? -1 : 1;
                        }

                        rgb[idx] = rgb[idx] + inc;
                        ctrlPWM(rgb[0], rgb[1], rgb[2], ctrl);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                sRunning = false;
                ctrl.onStopControll();
            }
        };

        runner.start();
    }

    private static String byteHex(byte b) {
        return String.format("%02x", b);
    }

    private static String byteHex0x(byte b) {
        return String.format("0x%02x", b);
    }

    private static String byteBinary(byte b) {
        final int s = 32 - 9;
        return Integer.toBinaryString(0x7F000000 + b).substring(s);
    }

    private static void printLEDCtrlValue(byte[] val) {
        boolean[] s = parseLEDState(val);

        out(String.format("0x%02x  0x%02x - ", val[0], val[1]));
        out(String.format("LED: %b\t%b\t%b\t%b,\t%b\t%b\t%b\n",
                    s[0], s[1], s[2], s[3], s[4], s[5], s[6]));
    }

    private static void printLEDReadValue(byte[] v, boolean[] s) {
        out(String.format("%s %s -> [%b %b %b %b] [%b %b %b]\n",
                    byteBinary(v[0]),
                    byteBinary(v[1]),
                    s[0], s[1], s[2], s[3], s[4], s[5], s[6]
                    ));
    }

    private static void out(String msg) {
        //System.out.print(msg);
        Log.d(msg);
    }

    private static ByteBuffer getPWMSetting(int r, int g, int b) {
        final int total = 4;
        ByteBuffer bb = ByteBuffer.allocate(total);

        byte ctrl = (byte)0xf8; // 0b11111000, disable rgb
        ctrl = (r > 0) ? (byte)(ctrl | 0x1) : ctrl;
        ctrl = (g > 0) ? (byte)(ctrl | 0x2) : ctrl;
        ctrl = (b > 0) ? (byte)(ctrl | 0x4) : ctrl;
        bb.put((byte)0x00); //  Reserved Uint8
        bb.put(ctrl);
        bb.put((byte)0x9f); // fixed TOP
        bb.put((byte)0x00);
        return bb;
    }

    private static ByteBuffer getDuty(int level) {
        final int total = 2;
        ByteBuffer bb = ByteBuffer.allocate(total);
        bb.put((byte)((9 - level)<<4 | 0x0F));
        bb.put((byte)0x00);
        return bb;
    }

    private static void goDigital(Controllable callback) {
        boolean[] v = new boolean[NUM];
        for (int i = 0; i < 7; i++) {
            v[i] = true;
            ctrlDigital(v, callback);
        }

        for (int i = 0; i < 7; i++) {
            v[i] = false;
            ctrlDigital(v, callback);
        }

        byte[] v1 = {(byte)0xf3, (byte)0xf2};
        boolean[] r1 = parseLEDState(v1);
        printLEDReadValue(v1, r1);
    }

    private static void goAnalog(Controllable callback) {
        for (int r = 0; r < 2; r++) {
            for (int g = 0; g < 2; g++) {
                ctrlPWM(r, g, 0, callback);
                ctrlPWM(r, g, 3, callback);
                ctrlPWM(r, g, 5, callback);
            }
        }
    }

    private static void resetLED(Controllable ctrl) {
        /* Turn off all */
        boolean[] leds = new boolean[NUM];
        for (int i = 0; i < NUM; i++) {
            leds[i] = false;
        }
        ctrlDigital(leds, ctrl);
    }

    public interface Controllable {
        /**
         * Callback for Algorighm to handle operations of digital LED
         */
        public void onControllDigital(byte[] ctrl);
        /**
         * Callback for Algorighm to handle operations of PWM Light
         */
        public void onControllPWM(int r, int g, int b, byte[][] ctrl);
        public void onStopControll();
    }

    public static void main(String arg[]) {
        Controllable callback = new Controllable() {

            public void onControllDigital(byte[] ctrl) {
                printLEDCtrlValue(ctrl);
            }

            public void onControllPWM(int r, int g, int b, byte[][] ctrl) {
                out(String.format("\t\t(%d, %d, %d)\n", r, g, b));
                for (int i = 0; i < ctrl.length; i++) {
                    out("[" + i + "]: ");
                    for (int j = 0; j < ctrl[i].length; j++) {
                        out(byteHex0x(ctrl[i][j]) + ", ");
                    }
                    out("\n");
                }
            }

            public void onStopControll() {
                out("finished");
            }
        };

        //goDigital(callback);
        //goAnalog(callback);
        //startAutoPattern1(callback);
        //startAutoPattern2(callback);
        startAutoPattern3(callback);
    }
}
