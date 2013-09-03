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

package com.issc.data;

import com.issc.util.Log;
import com.issc.util.UuidMatcher;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import android.content.Intent;

/**
 * This class helps us to know what a set of uuids can do.
 */

public class UuidsHandler {

    private UuidMatcher mMatcher;
    private String mPkgName;
    private String mActName;

    private CharSequence mTitle;
    private CharSequence mDesc;

    public UuidsHandler(String pkg, String act) {
        mMatcher = new UuidMatcher();
        mPkgName = pkg;
        mActName = act;
    }

    public void setInfo(CharSequence title, CharSequence desc) {
        mTitle = title;
        mDesc  = desc;
    }

    public Intent createIntent() {
        Intent i = new Intent();
        i.setClassName(mPkgName, mActName);
        return i;
    }

    public void addRule(UUID uuid) {
        mMatcher.addRule(uuid);
    }

    public void addStrRule(String uuid) {
        mMatcher.addRule(uuid);
    }

    public void addShortRule(String uuid) {
        mMatcher.addShortRule(uuid);
    }

    public void addRules(List<UUID> uuids) {
        Iterator<UUID> it = uuids.iterator();
        while(it.hasNext()) {
            mMatcher.addRule(it.next());
        }
    }

    public void addRules(UUID[] uuids) {
        for (int i = 0; i < uuids.length; i++) {
            mMatcher.addRule(uuids[i]);
        }
    }

    public void addStrRules(List<String> uuids) {
        Iterator<String> it = uuids.iterator();
        while(it.hasNext()) {
            mMatcher.addRule(it.next());
        }
    }

    public void addStrRules(String[] uuids) {
        for (int i = 0; i < uuids.length; i++) {
            mMatcher.addRule(uuids[i]);
        }
    }

    public void addShortRules(List<String> uuids) {
        Iterator<String> it = uuids.iterator();
        while(it.hasNext()) {
            mMatcher.addShortRule(it.next());
        }
    }

    public void addShortRules(String[] uuids) {
        for (int i = 0; i < uuids.length; i++) {
            mMatcher.addShortRule(uuids[i]);
        }
    }

    public CharSequence getTitle() {
        return mTitle;
    }

    public CharSequence getDesc() {
        return mDesc;
    }
}
