package com.pos.swoop;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.pos.swoop.objects.Constants;
import com.pos.swoop.utils.POSCommonUtils;
import com.pos.swoop.utils.PrintJob;
import com.pos.swoop.utils.SaveSharedPreference;

import java.util.Set;

public class PrintInventorActivity extends AppCompatActivity {

    String parent;
    Set<String> serviceTypes;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_inventor);
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
    }

    private void registerLayoutClickEvents(){

        LinearLayout printBOBInventory = (LinearLayout) findViewById(R.id.verifyBuyOnBoard);
        if(!serviceTypes.contains("BOB")){
            printBOBInventory.setBackground(getResources().getDrawable(R.drawable.layout_grey_out_backgroud));
        }
        printBOBInventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isItemsAvailableToSell("BOB")) {
                    print("BOB");
                }
            }
        });
        LinearLayout printDTPInventory = (LinearLayout) findViewById(R.id.verifyDutyPaid);
        if(!serviceTypes.contains("DTP")){
            printDTPInventory.setBackground(getResources().getDrawable(R.drawable.layout_grey_out_backgroud));
        }
        printDTPInventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isItemsAvailableToSell("DTP")) {
                    print("DTP");
                }
            }
        });
        LinearLayout printDTFInventory = (LinearLayout) findViewById(R.id.verifyDutyFree);
        if(!serviceTypes.contains("DTF")){
            printDTFInventory.setBackground(getResources().getDrawable(R.drawable.layout_grey_out_backgroud));
        }
        printDTFInventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isItemsAvailableToSell("DTF")) {
                    print("DTF");
                }
            }
        });
        LinearLayout printVRTInventory = (LinearLayout) findViewById(R.id.verifyVirtualInventory);
        if(!serviceTypes.contains("VRT")){
            printVRTInventory.setBackground(getResources().getDrawable(R.drawable.layout_grey_out_backgroud));
        }
        printVRTInventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isItemsAvailableToSell("VRT")) {
                    print("VRT");
                }
            }
        });
    }

    private void print(String serviceType){
        PrintJob job = new PrintJob(this);
        String openCloseType;
        String userName;
        if(parent.equals("VerifyFlightByAdminActivity") || parent.equals("AttCheckInfo")){
            openCloseType = "OPENING INVENTORY";
        }
        else{
            openCloseType = "CLOSING INVENTORY";
        }

        if(parent.equals("AttCheckInfo") || parent.equals("CloseFlightActivity") ){
            userName = SaveSharedPreference.getStringValues(this, Constants.SHARED_PREFERENCE_FA_NAME);
        }
        else {
            userName = SaveSharedPreference.getStringValues(this, Constants.SHARED_PREFERENCE_ADMIN_USER_NAME);
        }
        job.printInventoryReports(this, openCloseType,
                POSCommonUtils.getServiceTypeDescFromServiceType(serviceType),serviceType,userName);
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
        else{
            super.onBackPressed();
        }
    }
}
