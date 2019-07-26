package com.pos.swoop;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.pos.swoop.utils.POSCommonUtils;

import java.util.Set;

public class VerifyInventoryActivity extends AppCompatActivity {

    String parent;
    Set<String> serviceTypes;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_inventory);
        parent = getIntent().getExtras().getString("parent");
        serviceTypes = POSCommonUtils.getServiceTypeKitCodeMap(this).keySet();
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

    private void registerLayoutClickEvents(){

        LinearLayout printBOBInventory = (LinearLayout) findViewById(R.id.verifyBuyOnBoard);
        if(!serviceTypes.contains("BOB")){
            printBOBInventory.getChildAt(0).setBackgroundResource(R.drawable.buy_on_board_icon_grey);
        }
        printBOBInventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isItemsAvailableToSell("BOB")) {
                    Intent intent = new Intent(VerifyInventoryActivity.this, VerifyCartsActivity.class);
                    intent.putExtra("serviceType", "BOB");
                    intent.putExtra("parent", parent);
                    startActivity(intent);
                }
            }
        });
        LinearLayout printDTPInventory = (LinearLayout) findViewById(R.id.verifyDutyPaid);
        if(!serviceTypes.contains("DTP")){
            printDTPInventory.getChildAt(0).setBackgroundResource(R.drawable.duty_paid_icon_grey);
        }
        printDTPInventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isItemsAvailableToSell("DTP")) {
                    Intent intent = new Intent(VerifyInventoryActivity.this, VerifyCartsActivity.class);
                    intent.putExtra("serviceType", "DTP");
                    intent.putExtra("parent", parent);
                    startActivity(intent);
                }
            }
        });
        LinearLayout printDTFInventory = (LinearLayout) findViewById(R.id.verifyDutyFree);
        if(!serviceTypes.contains("DTF")){
            printDTFInventory.getChildAt(0).setBackgroundResource(R.drawable.duty_free_icon_grey);
        }
        printDTFInventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isItemsAvailableToSell("DTF")) {
                    Intent intent = new Intent(VerifyInventoryActivity.this, VerifyCartsActivity.class);
                    intent.putExtra("serviceType", "DTF");
                    intent.putExtra("parent", parent);
                    startActivity(intent);
                }
            }
        });
        LinearLayout printVRTInventory = (LinearLayout) findViewById(R.id.verifyVirtualInventory);
        if(!serviceTypes.contains("VRT")){
            //printVRTInventory.setBackground(getResources().getDrawable(R.drawable.layout_grey_out_backgroud));
            printVRTInventory.getChildAt(0).setBackgroundResource(R.drawable.virtual_inventory_icon_grey);
        }
        printVRTInventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isItemsAvailableToSell("VRT")) {
                    Intent intent = new Intent(VerifyInventoryActivity.this, VerifyCartsActivity.class);
                    intent.putExtra("serviceType", "VRT");
                    intent.putExtra("parent", parent);
                    startActivity(intent);
                }
            }
        });
    }

    private boolean isItemsAvailableToSell(String service){
        if(!serviceTypes.contains(service)){
            showAlertDialog("No items available.");
            return false;
        }
        else {
            return true;
        }
    }

    private void showAlertDialog(String body){
        Toast.makeText(this,body , Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed()
    {
        if("VerifyFlightByAdminActivity".equals(parent)) {
            Intent intent = new Intent(this, VerifyFlightByAdminActivity.class);
            startActivity(intent);
        }
        else if("CloseFlightActivity".equals(parent)) {
            Intent intent = new Intent(this, CloseFlightActivity.class);
            startActivity(intent);
        }
        else if("AttCheckInfo".equals(parent)) {
            Intent intent = new Intent(this, AttCheckInfo.class);
            startActivity(intent);
        }
        else{
            super.onBackPressed();
        }
    }
}
