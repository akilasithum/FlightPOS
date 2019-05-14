package com.pos.flightpos;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.pos.flightpos.utils.POSCommonUtils;

import java.util.List;
import java.util.Map;

public class SelectServiceTypeForSealActivity extends AppCompatActivity {

    Map<String,List<String>> serviceTypeKitCodeMap;
    String parent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_service_type_for_seal);
        serviceTypeKitCodeMap = POSCommonUtils.getServiceTypeKitCodeMap(this);
        parent = getIntent().getExtras().getString("parent");
        registerLayoutClickEvents();
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        ImageButton backButton = findViewById(R.id.backPressBtn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        /*ImageButton forwardBtn = findViewById(R.id.forwardPressBtn);
        forwardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });*/
    }

    private void registerLayoutClickEvents(){
        LinearLayout addBOBSeals = (LinearLayout) findViewById(R.id.verifyBuyOnBoard);
        if(!serviceTypeKitCodeMap.containsKey("BOB")){
            //addBOBSeals.setBackground(getResources().getDrawable(R.drawable.layout_grey_out_backgroud));
            addBOBSeals.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(SelectServiceTypeForSealActivity.this,"Items not available for sale",Toast.LENGTH_SHORT).show();;
                }
            });
        }
        else{
            addBOBSeals.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = getIntentFromServiceType();
                    intent.putExtra("serviceType", "BOB");
                    intent.putExtra("parent", parent);
                    startActivity(intent);
                }
            });
        }
        LinearLayout addDTPSeals = (LinearLayout) findViewById(R.id.verifyDutyPaid);
        if(!serviceTypeKitCodeMap.containsKey("DTP")){
            //addDTPSeals.setBackground(getResources().getDrawable(R.drawable.layout_grey_out_backgroud));
            addDTPSeals.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(SelectServiceTypeForSealActivity.this,"Items not available for sale",Toast.LENGTH_SHORT).show();
                }
            });
        }
        else{
            addDTPSeals.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = getIntentFromServiceType();
                    intent.putExtra("serviceType", "DTP");
                    intent.putExtra("parent", parent);
                    startActivity(intent);
                }
            });
        }
        LinearLayout addDTFSeals = (LinearLayout) findViewById(R.id.verifyDutyFree);
        if(!serviceTypeKitCodeMap.containsKey("DTF")){
            //addDTFSeals.setBackground(getResources().getDrawable(R.drawable.layout_grey_out_backgroud));
            addDTFSeals.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(SelectServiceTypeForSealActivity.this,"Items not available for sale",Toast.LENGTH_SHORT).show();;
                }
            });
        }
        else{
            addDTFSeals.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = getIntentFromServiceType();
                    intent.putExtra("serviceType", "DTF");
                    intent.putExtra("parent", parent);
                    startActivity(intent);
                }
            });
        }
        LinearLayout addVRTSeals = (LinearLayout) findViewById(R.id.verifyVirtualInventory);
        if(!serviceTypeKitCodeMap.containsKey("VRT")){
            //addVRTSeals.setBackground(getResources().getDrawable(R.drawable.layout_grey_out_backgroud));
            addVRTSeals.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(SelectServiceTypeForSealActivity.this,"Items not available for sale",Toast.LENGTH_SHORT).show();;
                }
            });
        }
        else{
            addVRTSeals.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = getIntentFromServiceType();
                    intent.putExtra("serviceType", "VRT");
                    intent.putExtra("parent", parent);
                    startActivity(intent);
                }
            });
        }
    }

    private Intent getIntentFromServiceType(){
        Intent addSeal = new Intent(SelectServiceTypeForSealActivity.this, AddSeal.class);
        Intent closeSeal = new Intent(SelectServiceTypeForSealActivity.this, ClosingSealsActivity.class);
        return parent.equals("VerifyFlightByAdminActivity") || parent.equals("AttCheckInfo") ? addSeal : closeSeal;
    }
}
