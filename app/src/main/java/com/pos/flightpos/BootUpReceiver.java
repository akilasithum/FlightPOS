package com.pos.flightpos;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.sunmi.pay.hardware.aidlv2.readcard.ReadCardOptV2;

public class BootUpReceiver extends BroadcastReceiver {

    public static ReadCardOptV2 mReadCardOptV2;

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, SplashActivity.class);  //MyActivity can be anything which you want to start on bootup...
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }
}
