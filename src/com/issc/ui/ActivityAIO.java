// vim: et sw=4 sts=4 tabstop=4
package com.issc.ui;

import com.issc.util.Log;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class ActivityAIO extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aio);
    }

    public void onClickBtn(View v) {
        Intent i = new Intent(this, ActivityDevicesList.class);
        startActivity(i);
    }
}
