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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class ActivityFileChooser extends ListActivity {

    private final static String PATH = "/sdcard/issc/";
    private final static String sFile = "_file";
    private final static String sName = "_file_name";
    private final static String sSize = "_file_size";

    private SimpleAdapter mAdapter;
    private ArrayList<Map<String, Object>> mEntries;
    private Button   mBtnChoose;
    private TextView mEmptyMsg;
    private TextView mFilename;
    private File mChosen;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_chooser);

        mBtnChoose = (Button) findViewById(R.id.btn_choose);
        mEmptyMsg  = (TextView) findViewById(R.id.empty_msg);
        mFilename  = (TextView) findViewById(R.id.filename);

        String[] from = {sName, sSize};
        int[] to = {R.id.row_title, R.id.row_description};
        mEntries = new ArrayList<Map<String, Object>>();
        mAdapter = new SimpleAdapter(
                    this,
                    mEntries,
                    R.layout.row_normal,
                    from,
                    to);
        setListAdapter(mAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras == null || !extras.containsKey(Bluebit.CHOOSE_PATH)) {
            fail("Please specify a directory to choose");
        } else {
            listFiles(extras.getString(Bluebit.CHOOSE_PATH));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int pos, long id) {
        mBtnChoose.setClickable(true);
        mChosen = (File)mEntries.get(pos).get(sFile);
        mFilename.setText(mChosen.getName());
    }

    public void onClickChoose(View v) {
        if (mChosen != null) {
            Intent intent = new Intent();
            intent.setData(Uri.fromFile(mChosen));
            this.setResult(Activity.RESULT_OK, intent);
            this.finish();
        }
    }

    private void fail(CharSequence msg) {
        mEntries.clear();
        mBtnChoose.setClickable(false);
        mEmptyMsg.setText(msg);
    }

    private void listFiles(String path) {
        File dir = new File(path);

        if (!dir.canRead()) {
            fail("Cannot read: " + path);
            return;
        }

        if (!dir.isDirectory()) {
            fail("Not directory: " + path);
            return;
        }

        File[] files = dir.listFiles();

        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                continue;
            }
            final Map<String, Object> entry = new HashMap<String, Object>();
            entry.put(sFile, files[i]);
            entry.put(sName, files[i].getName());
            entry.put(sSize, files[i].length() + " bytes");
            mEntries.add(entry);
        }

        mAdapter.notifyDataSetChanged();

        if (mEntries.size() == 0) {
            fail("There is not any file under: " + path);
            return;
        } else {
            mBtnChoose.setEnabled(true);
        }
    }
}

