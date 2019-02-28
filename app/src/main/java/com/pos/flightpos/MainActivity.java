package com.pos.flightpos;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.pos.flightpos.objects.Constants;
import com.pos.flightpos.utils.SaveSharedPreference;


public class MainActivity extends AppCompatActivity {

    long mExitTime = 0;
    String parent = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AWSMobileClient.getInstance().initialize(this).execute();
        parent = getIntent().getExtras().getString("parent");
        final ImageButton configureFlightLayout =  findViewById(R.id.backPressBtn);
        configureFlightLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isSyncClicked()){
                    redirectToAttMode();
                }
                else{
                    Toast.makeText(getApplicationContext(), "Sync items first.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        LinearLayout uploadLayout = findViewById(R.id.uploadDataLayout);
        uploadLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, UploadSalesDataActivity.class);
                startActivity(intent);
            }
        });
        if(parent == null || parent.isEmpty() || !parent.equals("SelectModeActivity") || parent.equals("UploadSalesDataActivity")){
            uploadLayout.setVisibility(View.GONE);
        }

        LinearLayout syncLayout = (LinearLayout) findViewById(R.id.syncLayout);
        syncLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SyncActivity.class);
                startActivity(intent);
            }
        });

        LinearLayout changeBaseStationLayout = findViewById(R.id.changeBaseStation);
        changeBaseStationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, BaseStationActivity.class);
                startActivity(intent);
            }
        });

        LinearLayout adminLogoutLayout = (LinearLayout) findViewById(R.id.adminLogutLayout);
        adminLogoutLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logoutAdmin();
            }
        });
    }

    private void redirectToAttMode(){
        new AlertDialog.Builder(this)
                .setTitle("Logging out")
                .setMessage("You are about to log out from admin mode. Do you wish to continue?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        SaveSharedPreference.removeValue(MainActivity.this, Constants.SHARED_PREFERENCE_ADMIN_USER);
                        SaveSharedPreference.setStringValues(MainActivity.this,
                                Constants.SHARED_PREFERENCE_CAN_ATT_LOGIN,"yes");
                        Intent intent = new Intent(MainActivity.this, FlightAttendentLogin.class);
                        startActivity(intent);

                    }
                })
                .setNegativeButton(android.R.string.no, null).show();
    }

    private boolean isSyncClicked(){
        return "true".equals(SaveSharedPreference.getStringValues(this,"syncKeyPressed"));
    }

    private void logoutAdmin(){
        SaveSharedPreference.removeValue(this,"userName");
        SaveSharedPreference.removeValue(this, Constants.SHARED_PREFERENCE_FLIGHT_NAME);
        SaveSharedPreference.removeValue(this, Constants.SHARED_PREFERENCE_FLIGHT_DATE);
        Intent intent = new Intent(this,LoginActivity.class);
        intent.putExtra("parent","MainActivity");
        startActivity(intent);
    }

    @Override
    public void onBackPressed()
    {
        if((System.currentTimeMillis() - mExitTime) < 2000)
        {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        else
        {
            Toast.makeText(this, "Press the back button once again to close the application.", Toast.LENGTH_SHORT).show();
            mExitTime = System.currentTimeMillis();
        }
    }
}
