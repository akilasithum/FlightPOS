package com.pos.swoop;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import sunmi.paylib.SunmiPayKernel;

public class SplashActivity extends AppCompatActivity {


    private boolean isDisConnectService = true;
    private SunmiPayKernel mSMPayKernel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        setContentView(R.layout.activity_splash);
        connectPayService();
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */
                Intent mainIntent = new Intent(SplashActivity.this,LoginActivity.class);
                mainIntent.putExtra("parent","SplashActivity");
                SplashActivity.this.startActivity(mainIntent);
                SplashActivity.this.finish();
            }
        }, 1000);
    }

    private void connectPayService() {
        mSMPayKernel = SunmiPayKernel.getInstance();
        mSMPayKernel.initPaySDK(this, mConnectCallback);
    }

    private SunmiPayKernel.ConnectCallback mConnectCallback = new SunmiPayKernel.ConnectCallback() {

        @Override
        public void onConnectPaySDK() {
            try {
                BootUpReceiver.mReadCardOptV2 = mSMPayKernel.mReadCardOptV2;
                BootUpReceiver.mBasicOptV2 = mSMPayKernel.mBasicOptV2;
                isDisConnectService = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnectPaySDK() {
            isDisConnectService = true;
        }

    };
}
