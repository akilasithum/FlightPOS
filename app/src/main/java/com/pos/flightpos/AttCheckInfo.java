package com.pos.flightpos;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

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
                Intent intent = new Intent(AttCheckInfo.this, VerifyInventoryActivity.class);
                startActivity(intent);
            }
        });
        LinearLayout inventoryReport = (LinearLayout) findViewById(R.id.inventoryReportLayout);
        inventoryReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AttCheckInfo.this, InventoryReportActivity.class);
                startActivity(intent);
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

    private void showConfirmation(){

        new AlertDialog.Builder(this)
                .setTitle("Open Flight")
                .setMessage("Do you want to open the flight?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        Intent intent = new Intent(AttCheckInfo.this, SellItemsActivity.class);
                        startActivity(intent);
                    }})
                .setNegativeButton(android.R.string.no, null).show();
    }
}
