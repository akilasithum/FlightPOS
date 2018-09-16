package com.pos.flightpos;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.pos.flightpos.utils.SaveSharedPreference;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        AWSMobileClient.getInstance().initialize(this).execute();

        final LinearLayout userLoginLayout = (LinearLayout) findViewById(R.id.userLoginLayout);
        userLoginLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isSyncClicked()){
                    Intent intent = new Intent(MainActivity.this, FlightAttendentLogin.class);
                    startActivity(intent);
                }
                else{
                    Toast.makeText(getApplicationContext(), "Sync items first.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        LinearLayout syncLayout = (LinearLayout) findViewById(R.id.syncLayout);
        syncLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SyncActivity.class);
                startActivity(intent);
            }
        });
    }

    private boolean isSyncClicked(){
        return "true".equals(SaveSharedPreference.getStringValues(this,"syncKeyPressed"));
    }
}
