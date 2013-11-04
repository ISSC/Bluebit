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

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class ActivityGattServer extends Activity {

    private int mId;
    private int mLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        if (!i.hasExtra(Bluebit.EXTRA_ID)) {
            Log.d("No extra id");
            finish();
        }
        if (!i.hasExtra(Bluebit.EXTRA_LAYOUT)) {
            Log.d("No extra layout");
            finish();
        }

        mId = i.getExtras().getInt(Bluebit.EXTRA_ID);
        mLayout = i.getExtras().getInt(Bluebit.EXTRA_LAYOUT);
        setContentView(mLayout);
    }

    @Override
    public void onActivityResult(int req, int result, Intent data) {
        Fragment gattsrv = getFragmentManager().findFragmentById(mId);
        if (gattsrv != null) {
            gattsrv.onActivityResult(req, result, data);
        }
    }
}
