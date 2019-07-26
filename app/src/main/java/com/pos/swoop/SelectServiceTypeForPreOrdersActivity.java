package com.pos.swoop;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class SelectServiceTypeForPreOrdersActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_service_type_for_pre_orders);
        registerLayoutClickEvents();
        ImageButton backButton = findViewById(R.id.backPressBtn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
    }

    private void registerLayoutClickEvents() {
        final Intent intent = new Intent(SelectServiceTypeForPreOrdersActivity.this, AcceptPreOrdersActivity.class);

        LinearLayout addBOBSeals = findViewById(R.id.verifyBuyOnBoard);
        addBOBSeals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.putExtra("serviceType", "BOB");
                startActivity(intent);
            }
        });
        LinearLayout addDTPSeals =  findViewById(R.id.verifyDutyPaid);
        addDTPSeals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.putExtra("serviceType", "DTP");
                startActivity(intent);
            }
        });
        LinearLayout addDTFSeals = findViewById(R.id.verifyDutyFree);
        addDTFSeals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.putExtra("serviceType", "DTF");
                startActivity(intent);
            }
        });
        LinearLayout addVRTSeals = findViewById(R.id.verifyVirtualInventory);
        addVRTSeals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.putExtra("serviceType", "VRT");
                startActivity(intent);
            }
        });
    }
}
