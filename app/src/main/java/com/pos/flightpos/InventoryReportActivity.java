package com.pos.flightpos;

import android.content.Intent;
import android.pt.printer.Printer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.pos.flightpos.objects.Constants;
import com.pos.flightpos.objects.XMLMapper.KITItem;
import com.pos.flightpos.utils.POSDBHandler;
import com.pos.flightpos.utils.SaveSharedPreference;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class InventoryReportActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_report);
        registerLayoutClickEvents();
    }

    private void registerLayoutClickEvents(){

        LinearLayout printBOBInventory = (LinearLayout) findViewById(R.id.buyOnBoardReports);
        printBOBInventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                printInventoryReports("BOB", "BUY ON BOARD INVENTORY");
            }
        });
        LinearLayout printDTPInventory = (LinearLayout) findViewById(R.id.dutyPaidReports);
        printDTPInventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                printInventoryReports("DTP", "DUTY PAID INVENTORY");
            }
        });
        LinearLayout printDTFInventory = (LinearLayout) findViewById(R.id.dutyFreeReports);
        printDTFInventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                printInventoryReports("DTF", "DUTY FREE INVENTORY");
            }
        });
        LinearLayout printVRTInventory = (LinearLayout) findViewById(R.id.virtualInventoryReports);
        printVRTInventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                printInventoryReports("VRT", "VIRTUAL INVENTORY");
            }
        });
    }

    private void printInventoryReports(String reportType,String inventoryDisplayName){
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
        printer.printPictureByRelativePath("/res/drawable/no_back.jpg", 150, 150);
        printer.setBold(true);
        printer.printString("CMB123 CMB-KUL");
        Date date = new Date();
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        DateFormat dateTimeFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss aa");
        printer.printString(df.format(date));
        printer.printString("OPENING INVENTORY");
        printer.printString(" ");
        printer.printString(inventoryDisplayName);
        POSDBHandler handler = new POSDBHandler(this);
        Map<String,List<KITItem>> drawerKitItemMap = handler.getDrawerKitItemMapFromServiceType(reportType);
        for (Map.Entry<String, List<KITItem>> entry : drawerKitItemMap.entrySet())
        {
            int total = 0;
            printer.setAlignment(0);
            printer.printString(entry.getKey());
            printer.setBold(false);
            for(KITItem item : entry.getValue()){
                total += Integer.parseInt(item.getQuantity());
                if(item.getQuantity().length()==1)item.setQuantity("0"+item.getQuantity());
                int spaceLength = 29 - (item.getItemNo().length()+item.getItemDescription().length());
                if(spaceLength < 0){
                    item.setItemDescription(item.getItemDescription().substring(0,(27-item.getItemNo().length())) + "..");
                    spaceLength = 0;
                }
                printer.printString(item.getItemNo()+"-"+item.getItemDescription() +
                         new String(new char[spaceLength]).replace("\0", " ")+item.getQuantity());
            }
            printer.setAlignment(2);
            printer.setBold(true);
            printer.printString("Total " + total);
        }
        printer.setAlignment(0);
        printer.printString("CART NUMBERS");
        printer.printString("100");
        printer.printString(" ");
        printer.printString("Operated Staff");
        printer.printString(SaveSharedPreference.getStringValues(this, Constants.SHARED_PREFERENCE_KEY));
        printer.printString(dateTimeFormat.format(date));
        printer.printString(" ");
        printer.printString(" ");
        printer.printString(" ");
        printer.close();
    }
}
