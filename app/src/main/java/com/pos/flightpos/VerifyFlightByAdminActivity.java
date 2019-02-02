package com.pos.flightpos;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
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

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public class VerifyFlightByAdminActivity extends AppCompatActivity {
    ImageButton flightUserLoginLayout;
    LinearLayout verifyInventoryLayout;
    LinearLayout printReport;
    LinearLayout addSealsLayout;
    LinearLayout syncPreOrderLayout;
    LinearLayout defineCartNumbersLayout;
    POSDBHandler handler;
    List<String> kitCode;
    long mExitTime = 0;
    Set<String> serviceType;
    LinearLayout preOrderPackLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_flight_by_admin);
        flightUserLoginLayout = (ImageButton) findViewById(R.id.userLoginLayout);
        verifyInventoryLayout = (LinearLayout) findViewById(R.id.verifyInventoryByAdmin);
        printReport = (LinearLayout) findViewById(R.id.printReportByAdmin);
        addSealsLayout = (LinearLayout) findViewById(R.id.addAdminSeal);
        syncPreOrderLayout = (LinearLayout) findViewById(R.id.syncPreOrderLayout);
        defineCartNumbersLayout = (LinearLayout) findViewById(R.id.defineCartNumbers);
        preOrderPackLayout = findViewById(R.id.packPreOrderLayout);
        handler = new POSDBHandler(this);
        kitCode = POSCommonUtils.availableKitCodes(this);
        serviceType = POSCommonUtils.getServiceTypeKitCodeMap(this).keySet();
        registerLayoutClickEvents();
        disablePackPreOrderLayout(false);
        String deviceId = SaveSharedPreference.getStringValues(this,Constants.SHARED_PREFERENCE_DEVICE_ID);
        handler.updateSIFDetails("packedDateTime",POSCommonUtils.getDateTimeString(),deviceId);
    }

    private void disablePackPreOrderLayout(boolean isEnable){
        String isPreOrderSynced = SaveSharedPreference.getStringValues(this,Constants.SHARED_PREFERENCE_SYNC_PRE_ORDERS);
        if((isPreOrderSynced == null || !isPreOrderSynced.equals("yes")) && !isEnable) {
            preOrderPackLayout.setBackground(getResources().getDrawable(R.drawable.layout_grey_out_backgroud));
        }
        else{
            preOrderPackLayout.setBackground(getResources().getDrawable(R.drawable.textinputborder));
        }
    }

    private void registerLayoutClickEvents() {

        flightUserLoginLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showConfirmation();
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
                Intent intent = new Intent(VerifyFlightByAdminActivity.this, VerifyInventoryActivity.class);
                intent.putExtra("parent", "VerifyFlightByAdminActivity");
                startActivity(intent);
            }
        });

        defineCartNumbersLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(VerifyFlightByAdminActivity.this, DefineCartNumbersActivity.class);
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
        preOrderPackLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String isPreOrderSynced = SaveSharedPreference.getStringValues(VerifyFlightByAdminActivity.this,
                        Constants.SHARED_PREFERENCE_SYNC_PRE_ORDERS);
                if(isPreOrderSynced != null && isPreOrderSynced.equals("yes")) {
                    Intent intent = new Intent(VerifyFlightByAdminActivity.this, LoadPreOrderAdminActivity.class);
                    /*Bundle args = new Bundle();
                    args.putSerializable("cartItems", (Serializable) serviceType);
                    intent.putExtra("serviceType", args)*/;
                    startActivity(intent);
                }
                else{
                    Toast.makeText(getApplicationContext(), "Please sync pre orders.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showConfirmation() {
        String openSeals = handler.getSealList(null,"outbound");
        if (openSeals == null || openSeals.length() == 0) {
            Toast.makeText(getApplicationContext(), "Add opening seals before login.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Logging out")
                .setMessage("You are about to log out from admin mode. Do you wish to continue?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        SaveSharedPreference.removeValue(VerifyFlightByAdminActivity.this, Constants.SHARED_PREFERENCE_ADMIN_USER);
                        SaveSharedPreference.setStringValues(VerifyFlightByAdminActivity.this,
                                Constants.SHARED_PREFERENCE_CAN_ATT_LOGIN,"yes");
                        Intent intent = new Intent(VerifyFlightByAdminActivity.this, FlightAttendentLogin.class);
                        startActivity(intent);

                    }
                })
                .setNegativeButton(android.R.string.no, null).show();
    }

    private void syncPreOrders() {
        Toast.makeText(getApplicationContext(), "Pre orders sync started ",
                Toast.LENGTH_SHORT).show();
        POSSyncUtils syncActivity = new POSSyncUtils(this);
        syncActivity.downloadData("pre_orders","pre_order_items");
        disablePackPreOrderLayout(true);
    }

    private void addAdminSeals() {
        String noOfSealsStr = handler.getNoOfSealsFromKitCodes(kitCode);
        SaveSharedPreference.setStringValues(this, Constants.SHARED_PREFERENCE_NO_OF_SEAL, noOfSealsStr);
        Intent intent = new Intent(this, SelectServiceTypeForSealActivity.class);
        intent.putExtra("parent","VerifyFlightByAdminActivity");
        startActivity(intent);
    }

    private void printInventoryReport() {
        Intent intent = new Intent(this, PrintInventorActivity.class);
        intent.putExtra("parent", "VerifyFlightByAdminActivity");
        startActivity(intent);

    }

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(this, ConfigureFlightActivity.class);
        startActivity(intent);
    }
}
