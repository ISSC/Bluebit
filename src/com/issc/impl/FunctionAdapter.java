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

import com.issc.Bluebit;
import com.issc.R;
import com.issc.util.UuidMatcher;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class FunctionAdapter extends BaseAdapter {

    private final static int sLayoutRes = R.layout.row_detail;
    private final LayoutInflater mInflater;

    private Context mContext;

    private ArrayList<UuidMatcher> mOptions;
    private ArrayList<UuidMatcher> mResults;
    private ArrayList<UUID> mUuids;

    public FunctionAdapter(Context context) {
        super();
        mContext = context;
        mOptions  = new ArrayList<UuidMatcher>();
        mResults  = new ArrayList<UuidMatcher>();
        mUuids    = new ArrayList<UUID>();
        mInflater = LayoutInflater.from(mContext);

        initOptions(mOptions);
    }

    public void addUuidInUiThread(UUID uuid) {
        if (mUuids.contains(uuid)) {
            return;
        }

        mUuids.add(uuid);
        updateDataSet();
    }

    private void updateDataSet() {
        mResults.clear();
        notifyDataSetChanged();
        Iterator<UuidMatcher> it = mOptions.iterator();
        while (it.hasNext()) {
            UuidMatcher target = it.next();
            if (target.isEnclosedBy(mUuids)) {
                mResults.add(target);
                notifyDataSetChanged();
            }
        }
    }

    @Override
    public int getCount() {
        return mResults.size();
    }

    @Override
    public Object getItem(int pos) {
        return mResults.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return pos;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(sLayoutRes, null);
        }

        TextView title = (TextView)convertView.findViewById(R.id.row_title);
        TextView desc = (TextView)convertView.findViewById(R.id.row_description);
        UuidMatcher handler = mResults.get(pos);
        title.setText(handler.getTitle());
        desc.setText(handler.getDesc());

        return convertView;
    }

    public Intent createIntent(int pos) {
        UuidMatcher target = (UuidMatcher)getItem(pos);
        return target.createIntent();
    }

    private void initOptions(ArrayList<UuidMatcher> options) {
        addLighting(options);
        addTransparent(options);
        addKeyboard(options);
    }

    private void addLighting(ArrayList<UuidMatcher> options) {
        UuidMatcher matcher = new UuidMatcher();
        matcher.setTarget("com.issc", "com.issc.ui.ActivityAIO");
        for (int i = 0; i < Bluebit.UUIDS_OF_LIGHTING.length; i++) {
            matcher.addRule(Bluebit.UUIDS_OF_LIGHTING[i]);
        }

        matcher.setInfo(mContext.getString(R.string.func_light),
                mContext.getString(R.string.func_light_desc));
        options.add(matcher);
    }

    private void addTransparent(ArrayList<UuidMatcher> options) {
        UuidMatcher matcher = new UuidMatcher();
        matcher.setTarget("com.issc", "com.issc.ui.ActivityTransparent");
        for (int i = 0; i < Bluebit.UUIDS_OF_TRANSPARENT.length; i++) {
            matcher.addRule(Bluebit.UUIDS_OF_TRANSPARENT[i]);
        }

        matcher.setInfo("Transparent", "Transfer data to device");
        options.add(matcher);
    }

    private void addKeyboard(ArrayList<UuidMatcher> options) {
        UuidMatcher matcher = new UuidMatcher();
        matcher.setTarget("com.issc", "com.issc.ui.ActivityKeyboard");
        for (int i = 0; i < Bluebit.UUIDS_OF_TRANSPARENT.length; i++) {
            matcher.addRule(Bluebit.UUIDS_OF_TRANSPARENT[i]);
        }

        matcher.setInfo("Keyboard", "Send Key event to device");
        options.add(matcher);
    }
}
