package com.pos.flightpos;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.pos.flightpos.objects.Constants;
import com.pos.flightpos.utils.POSCommonUtils;
import com.pos.flightpos.utils.POSDBHandler;
import com.pos.flightpos.utils.PrintJob;
import com.pos.flightpos.utils.SaveSharedPreference;

public class AttCheckInfo extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_att_check_info);
        registerLayoutClickEvents();
    }

    private void registerLayoutClickEvents(){

        LinearLayout addSealLayout = (LinearLayout) findViewById(R.id.sealInfoLayout);
        addSealLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AttCheckInfo.this, AddSeal.class);
                startActivity(intent);
            }
        });
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
                Intent intent = new Intent(AttCheckInfo.this, CheckInventoryActivity.class);
                intent.putExtra("parent","AttCheckInfo");
                startActivity(intent);
            }
        });
        LinearLayout inventoryReport = (LinearLayout) findViewById(R.id.inventoryReportLayout);
        inventoryReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                printInventoryReport();
            }
        });

        LinearLayout openFlight = (LinearLayout) findViewById(R.id.openFlight);
        openFlight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showConfirmation();
            }
        });
    }

    private void printInventoryReport(){
        PrintJob job = new PrintJob();
        String kitCode = SaveSharedPreference.getStringValues(this, Constants.SHARED_PREFERENCE_KIT_CODE);
        POSDBHandler handler = new POSDBHandler(this);
        String serviceType = handler.getServiceTypeFromKITCode(kitCode);
        job.printInventoryReports(this,"OPENING INVENTORY",kitCode,
                POSCommonUtils.getServiceTypeDescFromServiceType(serviceType));
    }

    private void showConfirmation(){

        new AlertDialog.Builder(this)
                .setTitle("Open Flight")
                .setMessage("Do you want to open the flight?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
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
