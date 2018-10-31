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
import com.pos.flightpos.utils.POSCommonUtils;
import com.pos.flightpos.utils.POSDBHandler;
import com.pos.flightpos.utils.SaveSharedPreference;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class InventoryReportActivity extends AppCompatActivity {

    String openCloseType;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_report);
        openCloseType = getIntent().getExtras().getString("reportType");
        registerLayoutClickEvents();
    }

    private void registerLayoutClickEvents(){

        final LinearLayout printBOBInventory = (LinearLayout) findViewById(R.id.buyOnBoardReports);
        printBOBInventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                printInventoryReports("BOB", "BUY ON BOARD INVENTORY",printBOBInventory);
            }
        });
        final LinearLayout printDTPInventory = (LinearLayout) findViewById(R.id.dutyPaidReports);
        printDTPInventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                printInventoryReports("DTP", "DUTY PAID INVENTORY",printDTPInventory);
            }
        });
        final LinearLayout printDTFInventory = (LinearLayout) findViewById(R.id.dutyFreeReports);
        printDTFInventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                printInventoryReports("DTF", "DUTY FREE INVENTORY",printDTFInventory);
            }
        });
        final LinearLayout printVRTInventory = (LinearLayout) findViewById(R.id.virtualInventoryReports);
        printVRTInventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                printInventoryReports("VRT", "VIRTUAL INVENTORY",printVRTInventory);
            }
        });
    }

    private void printInventoryReports(String reportType,String inventoryDisplayName,LinearLayout layout){
        layout.setEnabled(false);
        Printer printer = new Printer();
        printer.open();
        int printerStatus = printer.queState();
        if(printerStatus == 1){
            Toast.makeText(this, "Paper is not available. Please insert some papers.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        else if(printerStatus == 2){
            Toast.makeText(this, "Printer is too hot. Please wait.",
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
        printer.printString(openCloseType);
        printer.printString(" ");
        printer.printString(inventoryDisplayName);
        POSDBHandler handler = new POSDBHandler(this);
        Map<String,Map<String,List<KITItem>>> drawerKitItemMap = handler.getDrawerKitItemMapFromServiceType(reportType);
        for(Map.Entry<String,Map<String,List<KITItem>>> eqEntry : drawerKitItemMap.entrySet()) {

            String equipmentName = eqEntry.getKey();
            Map<String,List<KITItem>> treeMap = new TreeMap<>(eqEntry.getValue());
            for (Map.Entry<String, List<KITItem>> entry : treeMap.entrySet()) {
                int total = 0;
                printer.setAlignment(0);
                printer.printString(entry.getKey());
                printer.setBold(false);
                for (KITItem item : entry.getValue()) {
                    total += Integer.parseInt(item.getQuantity());
                    if (item.getQuantity().length() == 1)
                        item.setQuantity("0" + item.getQuantity());
                    int spaceLength = 29 - (item.getItemNo().length() + item.getItemDescription().length());
                    if (spaceLength < 0) {
                        item.setItemDescription(item.getItemDescription().substring(0, (27 - item.getItemNo().length())) + "..");
                        spaceLength = 0;
                    }
                    printer.printString(item.getItemNo() + "-" + item.getItemDescription() +
                            new String(new char[spaceLength]).replace("\0", " ") + item.getQuantity());
                }
                printer.setAlignment(2);
                printer.setBold(true);
                printer.printString("Total " + total);
            }
        }
        printer.setAlignment(0);
        printer.printString("CART NUMBERS");
        printer.printString("100");
        printer.printString(" ");
        printer.printString("Operated Staff");
        printer.printString(SaveSharedPreference.getStringValues(this, Constants.SHARED_PREFERENCE_FA_NAME));
        printer.printString(dateTimeFormat.format(date));
        printer.printString(" ");
        printer.printString(" ");
        printer.printString(" ");
        printer.close();
        layout.setEnabled(true);
    }

    @Override
    public void onBackPressed()
    {
        if("OPENING INVENTORY".equals(openCloseType)) {
            Intent intent = new Intent(this, AttCheckInfo.class);
            startActivity(intent);
        }
        else if("CLOSING INVENTORY".equals(openCloseType)){
            Intent intent = new Intent(this, CloseFlightActivity.class);
            startActivity(intent);
        }
        else{
            super.onBackPressed();
        }
    }
}
