package com.pos.flightpos;

import android.content.DialogInterface;
import android.content.Intent;
import android.pt.printer.Printer;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.pos.flightpos.objects.Constants;
import com.pos.flightpos.utils.POSCommonUtils;
import com.pos.flightpos.utils.POSDBHandler;
import com.pos.flightpos.utils.PrintJob;
import com.pos.flightpos.utils.SaveSharedPreference;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CloseFlightActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_close_flight);
        registerLayoutClickEvents();
    }

    private void registerLayoutClickEvents() {

        LinearLayout verifyCloseInventory = (LinearLayout) findViewById(R.id.verifyCloseFightInventory);
        verifyCloseInventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CloseFlightActivity.this, VerifyInventoryActivity.class);
                intent.putExtra("parent", "CloseFlightActivity");
                startActivity(intent);
            }
        });
        LinearLayout closingInventoryReport = (LinearLayout) findViewById(R.id.closeInventoryReport);
        closingInventoryReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CloseFlightActivity.this, PrintInventorActivity.class);
                intent.putExtra("parent","CloseFlightActivity");
                startActivity(intent);
            }
        });
        LinearLayout closeFlightSalesReport = (LinearLayout) findViewById(R.id.closeFlightSalesReport);
        closeFlightSalesReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                printClosingSalesReport();
            }
        });
        LinearLayout messageToBond = (LinearLayout) findViewById(R.id.messageToBond);
        messageToBond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CloseFlightActivity.this, MessageToBondActivity.class);
                startActivity(intent);
            }
        });
        LinearLayout closingSeals = (LinearLayout) findViewById(R.id.closingSeals);
        closingSeals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CloseFlightActivity.this, SelectServiceTypeForSealActivity.class);
                intent.putExtra("parent","CloseFlightActivity");
                startActivity(intent);
            }
        });
        LinearLayout closeFlight = (LinearLayout) findViewById(R.id.closeFlightFinal);
        closeFlight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showConfirmation();
            }
        });

        ImageButton backButton = findViewById(R.id.backPressBtn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void showConfirmation(){

        new AlertDialog.Builder(this)
                .setTitle("Close Flight")
                .setMessage("Do you want to close the flight?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        SaveSharedPreference.removeValue(CloseFlightActivity.this,"isOpenFlight");
                        SaveSharedPreference.removeValue(CloseFlightActivity.this,"eClassPaxCount");
                        SaveSharedPreference.removeValue(CloseFlightActivity.this,"bClassPaxCount");
                        SaveSharedPreference.removeValue(CloseFlightActivity.this,Constants.SHARED_PREFERENCE_FA_NAME);
                        SaveSharedPreference.removeValue(CloseFlightActivity.this,Constants.SHARED_PREFERENCE_TAX_PERCENTAGE);
                        //SaveSharedPreference.removeValue(CloseFlightActivity.this,Constants.SHARED_PREFERENCE_KIT_CODE);
                        SaveSharedPreference.removeValue(CloseFlightActivity.this,Constants.SHARED_PREFERENCE_FLIGHT_MODE);
                        SaveSharedPreference.removeValue(CloseFlightActivity.this,Constants.SHARED_PREFERENCE_IS_SEAL_VERIFIED);
                        SaveSharedPreference.removeValue(CloseFlightActivity.this,Constants.SHARED_PREFERENCE_FLIGHT_SECTOR);
                        SaveSharedPreference.removeValue(CloseFlightActivity.this,Constants.SHARED_ADMIN_CONFIGURE_FLIGHT);
                        SaveSharedPreference.removeValue(CloseFlightActivity.this,Constants.SHARED_PREFERENCE_CAN_ATT_LOGIN);
                        SaveSharedPreference.setStringValues(CloseFlightActivity.this,
                                Constants.SHARED_PREFERENCE_CLOSED_FLIGHT,"yes");
                        Intent intent = new Intent(CloseFlightActivity.this, SelectModeActivity.class);
                        startActivity(intent);
                    }})
                .setNegativeButton(android.R.string.no, null).show();
    }

    private void printClosingSalesReport(){
        POSDBHandler handler = new POSDBHandler(this);
        Printer printer = new Printer();
        printer.open();
        int printerStatus = printer.queState();
        if(printerStatus == 1){
            Toast.makeText(getApplicationContext(), "Paper is not available. Please insert some papers.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        else if(printerStatus == 2){
            Toast.makeText(getApplicationContext(), "Printer is too hot. Please wait.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        printer.init();
        printer.setAlignment(1);
        printer.printPictureByRelativePath(Constants.PRINTER_LOGO_LOCATION, 200, 70);
        printer.printString(" ");
        printer.setBold(true);
        printer.printString(POSCommonUtils.getFlightDetailsStr(this));
        Date date = new Date();
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        DateFormat dateTimeFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss aa");
        printer.printString(df.format(date));
        printer.printString(" ");
        printer.printString("SALES SUMMARY");
        Map<String,String> map = getServiceTypeDescMap();
        for(Map.Entry<String,String> details : map.entrySet()){
            printer.printString(" ");
            printer.printString(details.getValue());
            printer.setAlignment(2);
            printer.printString("Passenger - Total USD "+POSCommonUtils.getTwoDecimalFloatFromString(
                    handler.getTotalSaleFromServiceType(details.getKey(),"Passenger")));
            printer.setAlignment(2);
            printer.printString("Staff - Total USD "+POSCommonUtils.getTwoDecimalFloatFromString(
                    handler.getTotalSaleFromServiceType(details.getKey(),"Staff")));
            printer.setAlignment(1);
            printer.printString(" ");
        }
        /*printer.printString("SALES SUMMARY [STAFF]");
        for(Map.Entry<String,String> details : map.entrySet()){
            printer.printString(" ");
            printer.printString(details.getValue());
            printer.setAlignment(2);
            printer.printString(" ");
            printer.printString("Total USD "+handler.getTotalSaleFromServiceType(details.getKey(),"Staff"));
            printer.setAlignment(1);
        }*/
        printer.setAlignment(0);
        printer.printString("SELLERS INFORMATION");
        printer.printString(SaveSharedPreference.getStringValues(this,Constants.SHARED_PREFERENCE_FA_NAME));
        printer.printString(" ");
        printer.printString("Please handover to cashier");
        printer.printString(" ");
        printer.printString(" ");
        printer.close();
    }

    private Map<String,String> getServiceTypeDescMap(){
        Map<String,String> map = new HashMap<>();
        map.put("BOB","BUY ON BOARD SALES");
        map.put("DTP","DUTY FREE SALES");
        map.put("DTF","DUTY PAID SALES");
        map.put("VRT","VIRTUAL INVENTORY SALES");
        return map;
    }

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(this, SellItemsActivity.class);
        startActivity(intent);

    }
}
