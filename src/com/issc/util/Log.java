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

package com.issc.util;

import com.issc.Bluebit;

public final class Log {

    private final static String TAG = Bluebit.TAG;

    private static boolean sDebug = true;

    private Log() {
        // this is just a helper class.
    }

    public static void turnOnDebug() {
        sDebug = true;
    }

    public static void turnOffDebug() {
        sDebug = false;
    }

    public static void w(String msg) {
        w(TAG, msg);
    }

    public static void d(String msg) {
        d(TAG, msg);
    }

    public static void e(String msg) {
        e(TAG, msg);
    }

    public static void i(String msg) {
        i(TAG, msg);
    }

    public static void v(String msg) {
        v(TAG, msg);
    }

    public static void w(String msg, Exception e) {
        w(TAG, msg, e);
    }

    public static void d(String msg, Exception e) {
        d(TAG, msg, e);
    }

    public static void e(String msg, Exception e) {
        e(TAG, msg, e);
    }

    public static void i(String msg, Exception e) {
        i(TAG, msg, e);
    }

    public static void v(String msg, Exception e) {
        v(TAG, msg, e);
    }

    public static void w(String tag, String msg) {
        if (sDebug) {
            android.util.Log.w(TAG, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (sDebug) {
            android.util.Log.d(TAG, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (sDebug) {
            android.util.Log.e(TAG, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (sDebug) {
            android.util.Log.i(TAG, msg);
        }
    }

    public static void v(String tag, String msg) {
        if (sDebug) {
            android.util.Log.v(TAG, msg);
        }
    }

    public static void w(String tag, String msg, Exception e) {
        if (sDebug) {
            android.util.Log.w(TAG, msg, e);
        }
    }

    public static void d(String tag, String msg, Exception e) {
        if (sDebug) {
            android.util.Log.d(TAG, msg, e);
        }
    }

    public static void e(String tag, String msg, Exception e) {
        if (sDebug) {
            android.util.Log.e(TAG, msg, e);
        }
    }

    public static void i(String tag, String msg, Exception e) {
        if (sDebug) {
            android.util.Log.i(TAG, msg, e);
        }
    }

    public static void v(String tag, String msg, Exception e) {
        if (sDebug) {
            android.util.Log.v(TAG, msg, e);
        }
    }
}

