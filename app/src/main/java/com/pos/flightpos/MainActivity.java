package com.pos.flightpos;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
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
        final LinearLayout configureFlightLayout = (LinearLayout) findViewById(R.id.configureFlightLayout);
        configureFlightLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isSyncClicked()){
                    Intent intent = new Intent(MainActivity.this, ConfigureFlightActivity.class);
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
            syncLayout.setVisibility(View.VISIBLE);
        }
        else{
            syncLayout.setVisibility(View.GONE);
            uploadLayout.setVisibility(View.VISIBLE);
        }



        LinearLayout adminLogoutLayout = (LinearLayout) findViewById(R.id.adminLogutLayout);
        adminLogoutLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logoutAdmin();
            }
        });
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
