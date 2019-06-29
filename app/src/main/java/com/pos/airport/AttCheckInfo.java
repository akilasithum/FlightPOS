package com.pos.airport;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.pos.airport.objects.Constants;
import com.pos.airport.utils.POSCommonUtils;
import com.pos.airport.utils.POSDBHandler;
import com.pos.airport.utils.PrintJob;
import com.pos.airport.utils.SaveSharedPreference;

public class AttCheckInfo extends AppCompatActivity {

    POSDBHandler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_att_check_info);
        handler = new POSDBHandler(this);
        registerLayoutClickEvents();
    }

    private void registerLayoutClickEvents(){

        /*LinearLayout addSealLayout = (LinearLayout) findViewById(R.id.sealInfoLayout);
        addSealLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AttCheckInfo.this, SelectServiceTypeForSealActivity.class);
                intent.putExtra("parent","AttCheckInfo");
                startActivity(intent);
            }
        });*/
        LinearLayout sellerInfoLayout = (LinearLayout) findViewById(R.id.sellerInfoLayout);
        sellerInfoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AttCheckInfo.this, SellarInfoActivity.class);
                startActivity(intent);
            }
        });
        LinearLayout verifyInventory = (LinearLayout) findViewById(R.id.verifyInventoryLayout);
        verifyInventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AttCheckInfo.this, VerifyCartsActivity.class);
                intent.putExtra("serviceType", "VRT");
                intent.putExtra("parent", "AttCheckInfo");
                startActivity(intent);
            }
        });
        LinearLayout inventoryReport = (LinearLayout) findViewById(R.id.inventoryReportLayout);
        inventoryReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(AttCheckInfo.this)
                        .setTitle("Print Inventory Report")
                        .setMessage("You are sure you want to print inventory report?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                PrintJob job = new PrintJob(AttCheckInfo.this);
                                String userName = SaveSharedPreference.getStringValues(AttCheckInfo.this, Constants.SHARED_PREFERENCE_FA_NAME);
                                job.printInventoryReports(AttCheckInfo.this, "OPENING INVENTORY",
                                        POSCommonUtils.getServiceTypeDescFromServiceType("VRT"),"VRT",userName);

                            }
                        })
                        .setNegativeButton(android.R.string.no, null).show();
            }
        });



        LinearLayout openFlight =  findViewById(R.id.openFlight);
        openFlight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showConfirmation();
            }
        });
    }

    private void showConfirmation(){
        /*String isSealsVerified = SaveSharedPreference.getStringValues(this,Constants.SHARED_PREFERENCE_IS_SEAL_VERIFIED);
        if(isSealsVerified == null || isSealsVerified.length() == 0){
            Toast.makeText(getApplicationContext(), "Verify Seals before open the flight.",
                    Toast.LENGTH_SHORT).show();
            return;
        }*/

        new AlertDialog.Builder(this)
                .setTitle("Open Flight")
                .setMessage("Do you want to open the flight?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        String deviceId = SaveSharedPreference.getStringValues(AttCheckInfo.this,Constants.SHARED_PREFERENCE_DEVICE_ID);
                        handler.updateSIFDetails("crewOpenedDateTime",POSCommonUtils.getDateTimeString(),deviceId);
                        SaveSharedPreference.setStringValues(AttCheckInfo.this,"isOpenFlight","yes");
                        Intent intent = new Intent(AttCheckInfo.this, SellItemsActivity.class);
                        startActivity(intent);
                    }})
                .setNegativeButton(android.R.string.no, null).show();
    }

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(this, AttendendMainActivity.class);
        startActivity(intent);

    }
}
