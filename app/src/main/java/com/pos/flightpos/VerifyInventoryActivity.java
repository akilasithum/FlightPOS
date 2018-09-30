package com.pos.flightpos;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

public class VerifyInventoryActivity extends AppCompatActivity {

    String parent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_inventory);
        parent = getIntent().getExtras().getString("parent");
        registerLayoutClickEvents();
    }

    private void registerLayoutClickEvents(){

        LinearLayout printBOBInventory = (LinearLayout) findViewById(R.id.verifyBuyOnBoard);
        printBOBInventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(VerifyInventoryActivity.this, CheckInventoryActivity.class);
                intent.putExtra("ServiceType","BOB");
                startActivity(intent);
            }
        });
        LinearLayout printDTPInventory = (LinearLayout) findViewById(R.id.verifyDutyPaid);
        printDTPInventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(VerifyInventoryActivity.this, CheckInventoryActivity.class);
                intent.putExtra("ServiceType","DTP");
                startActivity(intent);
            }
        });
        LinearLayout printDTFInventory = (LinearLayout) findViewById(R.id.verifyDutyFree);
        printDTFInventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(VerifyInventoryActivity.this, CheckInventoryActivity.class);
                intent.putExtra("ServiceType","DTF");
                startActivity(intent);
            }
        });
        LinearLayout printVRTInventory = (LinearLayout) findViewById(R.id.verifyVirtualInventory);
        printVRTInventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(VerifyInventoryActivity.this, CheckInventoryActivity.class);
                intent.putExtra("ServiceType","VRT");
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed()
    {
        if("AttCheckInfo".equals(parent)) {
            Intent intent = new Intent(this, AttCheckInfo.class);
            startActivity(intent);
        }
        else if("CloseFlightActivity".equals(parent)) {
            Intent intent = new Intent(this, CloseFlightActivity.class);
            startActivity(intent);
        }
        else{
            super.onBackPressed();
        }
    }
}
