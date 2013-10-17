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

package com.issc.ui;

import com.issc.Bluebit;
import com.issc.gatt.Gatt;
import com.issc.impl.LeService;
import com.issc.R;
import com.issc.util.Log;
import com.issc.util.Util;
import com.issc.widget.LoadingFragment;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class FragGattServer extends Fragment {

    private Context mCtx;
    private SrvConnection mConn;
    private Object mLock;

    private BluetoothDevice mDevice = null;
    private LeService mService;
    private Gatt.Listener mListener;

    private static String TAG_LOADING = "loading";
    private DialogFragment mLoading;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        mCtx  = getActivity();
        mLock = new Object();
        mLoading = new LoadingFragment();

        Intent intent = getActivity().getIntent();
        mListener = new GattListener();
        mConn = new SrvConnection();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        View root = getView();
        onDisconnected();

        if (!Util.isBluetoothEnabled()) {
            Util.enableBluetooth(getActivity(), 0);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("Service binding");
        mCtx.bindService(new Intent(mCtx, LeService.class), mConn, 0);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mService != null) {
            mService.rmListener(mListener);
            mService.disconnect(mDevice);
            mService.closeGatt(mDevice);
        }

        mService = null;
        mCtx.unbindService(mConn);
        Log.d("Service unbind");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_gatt_server, container, false);
    }

    private void init(BluetoothDevice device) {
        if (device.getName() == null) {
            Log.d(getString(R.string.detail_name), "");
        } else {
            Log.d(getString(R.string.detail_name), device.getName());
        }
        Log.d(getString(R.string.detail_addr), device.getAddress());
    }

    private void doConnect(BluetoothDevice dev) {
        ConnectTask task = new ConnectTask();
        task.execute(dev);
    }

    private void finish() {
        getActivity().getFragmentManager()
            .beginTransaction().remove(this).commit();
    }

    private void onConnected() {
    }

    private void onDisconnected() {
    }

    class GattListener extends Gatt.ListenerHelper {

        GattListener() {
            super("ActivityDeviceDetail");
        }

        @Override
        public void onServicesDiscovered(Gatt gatt, int status) {
        }

        @Override
        public void onConnectionStateChange(Gatt gatt,
                int status, int newState) {

            if (mService == null) {
                Log.d("There is no Gatt to be used, skip");
                return;
            }

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mService.discoverServices(mDevice);
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            }
        }
    }
    class SrvConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mService = ((LeService.LocalBinder)service).getService();
            mService.addListener(mListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.e("Gatt Service disconnected");
        }
    }

    class ConnectTask extends AsyncTask<BluetoothDevice, Object, Object> {
        @Override
        protected Object doInBackground(BluetoothDevice... devs) {
            BluetoothDevice dev = devs[0];
            mService.connect(dev, false);
            return null;
        }

        @Override
        protected void onPreExecute() {
            mLoading.show(getFragmentManager(), TAG_LOADING);
        }

        @Override
        protected void onPostExecute(Object result) {
            if (mLoading.isAdded()) {
                mLoading.dismiss();
            }
        }
    }
}
