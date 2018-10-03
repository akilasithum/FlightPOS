package com.pos.flightpos.utils;

import android.content.Context;
import android.pt.printer.Printer;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.pos.flightpos.objects.Constants;
import com.pos.flightpos.objects.XMLMapper.KITItem;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class PrintJob {

    public void printInventoryReports(Context context,String openCloseType,String kitCode,
                                       String inventoryDisplayName){
        Printer printer = new Printer();
        printer.open();
        int printerStatus = printer.queState();
        if(printerStatus == 1){
            Toast.makeText(context, "Paper is not available. Please insert some papers.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        else if(printerStatus == 2){
            Toast.makeText(context, "Printer is too hot. Please wait.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(context, "Printing started. Please wait.",
                Toast.LENGTH_SHORT).show();
        printer.init();
        printer.setAlignment(1);
        printer.printPictureByRelativePath(Constants.PRINTER_LOGO_LOCATION, 150, 150);
        printer.setBold(true);
        printer.printString("CMB123 CMB-KUL");
        Date date = new Date();
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        DateFormat dateTimeFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss aa");
        printer.printString(df.format(date));
        printer.printString(openCloseType);
        printer.printString(" ");
        printer.printString(inventoryDisplayName);
        POSDBHandler handler = new POSDBHandler(context);
        Map<String,Map<String,List<KITItem>>> drawerKitItemMap = handler.getDrawerKitItemMapFromServiceType(kitCode);
        for(Map.Entry<String,Map<String,List<KITItem>>> eqEntry : drawerKitItemMap.entrySet()) {
            String equipmentName = eqEntry.getKey();
            printer.setAlignment(0);
            printer.setBold(true);
            printer.printString("Equipment No : " + equipmentName);
            printer.printString(" ");
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
        printer.printString(" ");
        printer.printString("Operated Staff");
        printer.printString(SaveSharedPreference.getStringValues(context, Constants.SHARED_PREFERENCE_KEY));
        printer.printString(dateTimeFormat.format(date));
        printer.printString(" ");
        printer.printString(" ");
        printer.printString(" ");
        printer.close();
        Toast.makeText(context, "Printing finished.",
                Toast.LENGTH_SHORT).show();
    }
}
