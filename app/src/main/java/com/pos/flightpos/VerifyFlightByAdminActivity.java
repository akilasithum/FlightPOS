package com.pos.flightpos;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.AWSStartupHandler;
import com.amazonaws.mobile.client.AWSStartupResult;
import com.pos.flightpos.objects.Constants;
import com.pos.flightpos.utils.POSCommonUtils;
import com.pos.flightpos.utils.POSDBHandler;
import com.pos.flightpos.utils.POSSyncUtils;
import com.pos.flightpos.utils.PrintJob;
import com.pos.flightpos.utils.SaveSharedPreference;

public class VerifyFlightByAdminActivity extends AppCompatActivity {
    LinearLayout flightUserLoginLayout;
    LinearLayout verifyInventoryLayout;
    LinearLayout printReport;
    LinearLayout addSealsLayout;
    LinearLayout syncPreOrderLayout;
    POSDBHandler handler;
    String kitCode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_flight_by_admin);
        flightUserLoginLayout = (LinearLayout) findViewById(R.id.userLoginLayout);
        verifyInventoryLayout = (LinearLayout) findViewById(R.id.verifyInventoryByAdmin);
        printReport = (LinearLayout) findViewById(R.id.printReportByAdmin);
        addSealsLayout = (LinearLayout) findViewById(R.id.addAdminSeal);
        syncPreOrderLayout = (LinearLayout) findViewById(R.id.syncPreOrderLayout);
        handler = new POSDBHandler(this);
        kitCode = SaveSharedPreference.getStringValues(this, Constants.SHARED_PREFERENCE_KIT_CODE);
        registerLayoutClickEvents();
    }

    private void registerLayoutClickEvents() {

        flightUserLoginLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String openSeals = SaveSharedPreference.getStringValues(VerifyFlightByAdminActivity.this,"openSealList");
                if(openSeals != null && openSeals.length() != 0) {
                    Intent intent = new Intent(VerifyFlightByAdminActivity.this, FlightAttendentLogin.class);
                    startActivity(intent);
                }
                else{
                    Toast.makeText(getApplicationContext(), "Add opening seals before login.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        syncPreOrderLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                syncPreOrders();
            }
        });

        verifyInventoryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(VerifyFlightByAdminActivity.this, CheckInventoryActivity.class);
                intent.putExtra("parent","VerifyFlightByAdminActivity");
                startActivity(intent);
            }
        });

        printReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                printInventoryReport();
            }
        });

        addSealsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addAdminSeals();
            }
        });
    }

    private void syncPreOrders(){
        Toast.makeText(getApplicationContext(), "Pre orders sync started ",
                Toast.LENGTH_SHORT).show();
        POSSyncUtils syncActivity = new POSSyncUtils(this);
        syncActivity.downloadData("pre_orders");
    }

    private void addAdminSeals(){
        String noOfSealsStr = handler.getKitNumberListFieldValueFromKitCode(kitCode,"noOfSeals");
        SaveSharedPreference.setStringValues(this,Constants.SHARED_PREFERENCE_NO_OF_SEAL,noOfSealsStr);
        Intent intent = new Intent(this, AddSeal.class);
        intent.putExtra("parent","VerifyFlightByAdminActivity");
        startActivity(intent);
    }

    private void printInventoryReport(){
        PrintJob job = new PrintJob();
        String serviceType = handler.getKitNumberListFieldValueFromKitCode(kitCode,Constants.FILED_NAME_SERVICE_TYPE);
        job.printInventoryReports(this,"OPENING INVENTORY",kitCode,
                POSCommonUtils.getServiceTypeDescFromServiceType(serviceType));
    }
}
