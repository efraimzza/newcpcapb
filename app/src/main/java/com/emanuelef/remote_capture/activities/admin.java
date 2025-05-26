package com.emanuelef.remote_capture.activities;

import android.app.admin.DeviceAdminReceiver;
import android.content.Intent;
import android.content.Context;

public class admin extends DeviceAdminReceiver {

    @Override
    public void onEnabled(Context context, Intent intent) {
        super.onEnabled(context, intent);
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        super.onDisabled(context, intent);
    }
    
}
