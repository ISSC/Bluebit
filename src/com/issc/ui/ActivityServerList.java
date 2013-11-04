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
import com.issc.R;
import com.issc.util.Log;
import com.issc.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.Dialog;
import android.app.ListActivity;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.view.View;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class ActivityServerList extends ListActivity {

    private final static String sTitle = "title";
    private final static String sDesc  = "desc";

    private BluetoothDevice mDevice;
    private ArrayList<Map<String, Object>> mEntries;
    private SimpleAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_list);
        initAdapter();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initAdapter() {
        String[] from = {sTitle, sDesc};
        int[] to = {R.id.row_title, R.id.row_description};

        mEntries = new ArrayList<Map<String, Object>>();
        mAdapter = new SimpleAdapter(
                    this,
                    mEntries,
                    R.layout.row_normal,
                    from,
                    to
                );

        //mAdapter.setViewBinder(new MyBinder());
        setListAdapter(mAdapter);
        addSupportedServers();
    }

    private void addSupportedServers() {
        Map<String, Object> anp = new HashMap<String, Object>();
        anp.put(sTitle, "Alert Notification Server");
        anp.put(sDesc, "To send information to client");
        mEntries.add(anp);
    }

    class MyBinder implements SimpleAdapter.ViewBinder {
        // By default, SimpleAdapter just invoke toString to fill content of TextView.
        // However, the data might be spannable-string, so use setText instead of
        // using toString to avoid information losing.
        public boolean setViewValue(View view, Object data, String textRepresentation) {
            if (view instanceof TextView) {
                ((TextView)view).setText((CharSequence)data);
            }
            return true;
        }
    }

}

