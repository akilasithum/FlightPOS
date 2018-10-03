package com.pos.flightpos;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.pos.flightpos.objects.Constants;
import com.pos.flightpos.utils.POSCommonUtils;
import com.pos.flightpos.utils.POSDBHandler;
import com.pos.flightpos.utils.PrintJob;
import com.pos.flightpos.utils.SaveSharedPreference;

public class VerifyFlightByAdminActivity extends AppCompatActivity {
    LinearLayout flightUserLoginLayout;
    LinearLayout verifyInventoryLayout;
    LinearLayout printReport;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_flight_by_admin);
        flightUserLoginLayout = (LinearLayout) findViewById(R.id.userLoginLayout);
        verifyInventoryLayout = (LinearLayout) findViewById(R.id.verifyInventoryByAdmin);
        printReport = (LinearLayout) findViewById(R.id.printReportByAdmin);
        registerLayoutClickEvents();
    }

    private void registerLayoutClickEvents() {

        flightUserLoginLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    Intent intent = new Intent(VerifyFlightByAdminActivity.this, FlightAttendentLogin.class);
                    startActivity(intent);
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
    }

    private void printInventoryReport(){
        PrintJob job = new PrintJob();
        String kitCode = SaveSharedPreference.getStringValues(this, Constants.SHARED_PREFERENCE_KIT_CODE);
        POSDBHandler handler = new POSDBHandler(this);
        String serviceType = handler.getServiceTypeFromKITCode(kitCode);
        job.printInventoryReports(this,"OPENING INVENTORY",kitCode,
                POSCommonUtils.getServiceTypeDescFromServiceType(serviceType));
    }
}
