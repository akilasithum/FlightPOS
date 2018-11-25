package com.pos.flightpos;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.pos.flightpos.utils.POSCommonUtils;

import java.util.List;
import java.util.Map;

public class SelectServiceTypeForSealActivity extends AppCompatActivity {

    Map<String,List<String>> serviceTypeKitCodeMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_service_type_for_seal);
        serviceTypeKitCodeMap = POSCommonUtils.getServiceTypeKitCodeMap(this);
        registerLayoutClickEvents();
        ImageButton backButton = findViewById(R.id.backPressBtn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void registerLayoutClickEvents(){
        LinearLayout addBOBSeals = (LinearLayout) findViewById(R.id.verifyBuyOnBoard);
        if(!serviceTypeKitCodeMap.containsKey("BOB")){
            addBOBSeals.setBackground(getResources().getDrawable(R.drawable.layout_grey_out_backgroud));
        }
        else{
            addBOBSeals.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(SelectServiceTypeForSealActivity.this, AddSeal.class);
                    intent.putExtra("serviceType", "BOB");
                    intent.putExtra("parent", "VerifyFlightByAdminActivity");
                    startActivity(intent);
                }
            });
        }
        LinearLayout addDTPSeals = (LinearLayout) findViewById(R.id.verifyDutyPaid);
        if(!serviceTypeKitCodeMap.containsKey("DTP")){
            addDTPSeals.setBackground(getResources().getDrawable(R.drawable.layout_grey_out_backgroud));
        }
        else{
            addDTPSeals.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(SelectServiceTypeForSealActivity.this, AddSeal.class);
                    intent.putExtra("serviceType", "DTP");
                    intent.putExtra("parent", "VerifyFlightByAdminActivity");
                    startActivity(intent);
                }
            });
        }
        LinearLayout addDTFSeals = (LinearLayout) findViewById(R.id.verifyDutyFree);
        if(!serviceTypeKitCodeMap.containsKey("DTF")){
            addDTFSeals.setBackground(getResources().getDrawable(R.drawable.layout_grey_out_backgroud));
        }
        else{
            addDTFSeals.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(SelectServiceTypeForSealActivity.this, AddSeal.class);
                    intent.putExtra("serviceType", "DTF");
                    intent.putExtra("parent", "VerifyFlightByAdminActivity");
                    startActivity(intent);
                }
            });
        }
        LinearLayout addVRTSeals = (LinearLayout) findViewById(R.id.verifyVirtualInventory);
        if(!serviceTypeKitCodeMap.containsKey("VRT")){
            addVRTSeals.setBackground(getResources().getDrawable(R.drawable.layout_grey_out_backgroud));
        }
        else{
            addVRTSeals.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(SelectServiceTypeForSealActivity.this, AddSeal.class);
                    intent.putExtra("serviceType", "VRT");
                    intent.putExtra("parent", "VerifyFlightByAdminActivity");
                    startActivity(intent);
                }
            });
        }
    }
}
