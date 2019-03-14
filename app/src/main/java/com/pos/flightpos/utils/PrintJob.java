package com.pos.flightpos.utils;

import android.content.Context;
import android.pt.printer.Printer;
import android.widget.Toast;

import com.pos.flightpos.objects.Constants;
import com.pos.flightpos.objects.CreditCard;
import com.pos.flightpos.objects.Flight;
import com.pos.flightpos.objects.SoldItem;
import com.pos.flightpos.objects.XMLMapper.KITItem;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class PrintJob {

    public void printInventoryReports(Context context,String openCloseType, String inventoryDisplayName,
                                      String serviceType,String userName){
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
        printer.printPictureByRelativePath(Constants.PRINTER_LOGO_LOCATION, 200, 70);
        printer.printString(" ");
        printer.setBold(true);
        printer.printString(getFlightDetailsStr(context));
        Date date = new Date();
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        DateFormat dateTimeFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss aa");
        printer.printString(df.format(date));
        printer.printString(openCloseType);
        printer.printString(" ");
        printer.printString(inventoryDisplayName);
        POSDBHandler handler = new POSDBHandler(context);
        List<String> kitCodesList = POSCommonUtils.getServiceTypeKitCodeMap(context).get(serviceType);
        Map<String, Map<String, List<KITItem>>> drawerKitItemMap = handler.
                getDrawerKitItemMapFromServiceType(POSCommonUtils.getCommaSeparateStrFromList(kitCodesList));
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
        printer.printString(userName);
        printer.printString(dateTimeFormat.format(date));
        printer.printString(" ");
        printer.printString(" ");
        printer.printString(" ");
        printer.close();
        Toast.makeText(context, "Printing finished.",
                Toast.LENGTH_SHORT).show();
    }

    public static boolean printVoidOrderByReceipt(Context context,String orderNumber,String seatNumber,
                                                  List<SoldItem> soldItems, boolean isCustomerCopy){
        Printer printer = new Printer();
        printer.open();
        int printerStatus = printer.queState();
        if(printerStatus == 1){
            Toast.makeText(context, "Paper is not available. Please insert some papers.",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(printerStatus == 2){
            Toast.makeText(context, "Printer is too hot. Please wait.",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        Toast.makeText(context, "Printing started. Please wait.",
                Toast.LENGTH_SHORT).show();
        printer.init();
        printer.setAlignment(1);
        printer.printPictureByRelativePath(Constants.PRINTER_LOGO_LOCATION, 200, 70);
        printer.printString(" ");
        printer.setBold(true);
        printer.printString(getFlightDetailsStr(context));
        Date date = new Date();
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        DateFormat dateTimeFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss aa");
        printer.printString(df.format(date));
        printer.printString(" ");
        printer.printString("Void transaction");
        printer.setAlignment(0);
        printer.printString("Order Number : " + orderNumber);
        printer.printString("Seat Number : " + seatNumber);
        printer.printString(" ");
        float total = 0;
        for(SoldItem item : soldItems){
            total += Float.parseFloat(item.getPrice())* Integer.parseInt(item.getQuantity());
            int itemNameLength = item.getItemDesc().length();
            String totalAmount = POSCommonUtils.getTwoDecimalFloatFromFloat(
                    Float.parseFloat(item.getPrice())*Integer.parseInt(item.getQuantity()));
            int totalAmountLength = totalAmount.length();
            int spaceLength = 32 - (itemNameLength+totalAmountLength+1);
            printer.printString(item.getItemDesc()
                    + new String(new char[spaceLength]).replace("\0", " ") +"-"+ totalAmount);
        }
        printer.printString(" ");
        printer.setAlignment(2);
        printer.printString("Total USD -" + POSCommonUtils.getTwoDecimalFloatFromFloat(total));

        printer.setAlignment(0);
        if(!isCustomerCopy) {
            printer.printString(" ");
            printer.printString(" ");
            printer.printString(".......................");
            printer.printString("Card Holder Signature");
            printer.printString("I got the refund");
            printer.printString("for this void items");
            printer.printString(" ");
            printer.printString("Merchant Copy");
        }
        else{
            printer.printString("Card holder copy");
        }
        printer.printString(" ");
        printer.printString(" ");
        printer.setBold(true);
        printer.printString("Operated Staff");
        printer.printString(SaveSharedPreference.getStringValues(context, Constants.SHARED_PREFERENCE_FA_NAME));
        printer.printString(dateTimeFormat.format(date));
        printer.printString(" ");
        printer.printString(" ");
        printer.close();
        return true;
    }

    public static boolean printVoidOrderReceipt(Context context,String orderNumber,String seatNumber,List<SoldItem> soldItems
            ,Map<String,String> paymentMethodsMap,String discount,String taxPercentage,boolean isCustomerCopy){
        Printer printer = new Printer();
        printer.open();
        int printerStatus = printer.queState();
        if(printerStatus == 1){
            Toast.makeText(context, "Paper is not available. Please insert some papers.",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(printerStatus == 2){
            Toast.makeText(context, "Printer is too hot. Please wait.",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        Toast.makeText(context, "Printing started. Please wait.",
                Toast.LENGTH_SHORT).show();
        printer.init();
        printer.setAlignment(1);
        printer.printPictureByRelativePath(Constants.PRINTER_LOGO_LOCATION, 200, 70);
        printer.printString(" ");
        printer.setBold(true);
        printer.printString(getFlightDetailsStr(context));
        Date date = new Date();
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        DateFormat dateTimeFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss aa");
        printer.printString(df.format(date));
        printer.printString(" ");
        printer.printString(" ");
        printer.printString("Void transaction");
        printer.setAlignment(0);
        printer.printString("Order Number : " + orderNumber);
        printer.printString("Seat Number : " + seatNumber);
        float total = 0;
        for(SoldItem item : soldItems){
            total += Float.parseFloat(item.getPrice())* Integer.parseInt(item.getQuantity());
            int itemNameLength = item.getItemDesc().length();
            String totalAmount = POSCommonUtils.getTwoDecimalFloatFromFloat(
                    Float.parseFloat(item.getPrice())*Integer.parseInt(item.getQuantity()));
            int totalAmountLength = totalAmount.length();
            int spaceLength = 32 - (itemNameLength+totalAmountLength);
            printer.printString(item.getItemDesc()
                    + new String(new char[spaceLength]).replace("\0", " ") + totalAmount);
            printer.printString(item.getItemId() + " Each $" + item.getPrice() );
        }
        printer.printString(" ");
        printer.setAlignment(2);
        printer.printString("Total USD " + POSCommonUtils.getTwoDecimalFloatFromFloat(total));
        if(discount != null && !discount.isEmpty()) {
            total -= Float.parseFloat(discount);
            printer.printString("Discount USD " + POSCommonUtils.getTwoDecimalFloatFromString(discount));
        }
        Float dueBalance = total;
        if(taxPercentage != null && !"null".equals(taxPercentage) && !taxPercentage.isEmpty()){
            printer.printString("Service Tax " + taxPercentage + "%");
            dueBalance = total * ((100 + Float.parseFloat(taxPercentage)) / 100);
        }
        printer.printString("Sub Total USD -" + POSCommonUtils.getTwoDecimalFloatFromFloat(dueBalance));
        printer.setBold(false);
        printer.setAlignment(0);
        Iterator it = paymentMethodsMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            printer.printString(pair.getKey() + " " + pair.getValue());
        }
        if(!isCustomerCopy) {
            printer.printString(" ");
            printer.printString(" ");
            printer.printString(".......................");
            printer.printString("Card Holder Signature");
            printer.printString("I got the full refund");
            printer.printString("for this order");
            printer.printString(" ");
            printer.printString("Merchant Copy");
        }
        else{
            printer.printString("Card holder copy");
        }
        printer.printString(" ");
        printer.printString(" ");
        printer.setBold(true);
        printer.printString("Operated Staff");
        printer.printString(SaveSharedPreference.getStringValues(context, Constants.SHARED_PREFERENCE_FA_NAME));
        printer.printString(dateTimeFormat.format(date));
        printer.printString(" ");
        printer.printString(" ");
        printer.close();
        return true;
    }

    public static boolean printOrderDetails(Context context,String orderNumber,String seatNumber, List<SoldItem> soldItems
                       ,Map<String,String> paymentMethodsMap,CreditCard card,boolean isCustomerCopy,
                                            String discount,String taxPercentage){

        Printer printer = new Printer();
        printer.open();
        int printerStatus = printer.queState();
        if(printerStatus == 1){
            Toast.makeText(context, "Paper is not available. Please insert some papers.",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(printerStatus == 2){
            Toast.makeText(context, "Printer is too hot. Please wait.",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        Toast.makeText(context, "Printing started. Please wait.",
                Toast.LENGTH_SHORT).show();
        printer.init();
        printer.setAlignment(1);
        printer.printPictureByRelativePath(Constants.PRINTER_LOGO_LOCATION, 200, 70);
        printer.printString(" ");
        printer.setBold(true);
        printer.printString(getFlightDetailsStr(context));
        Date date = new Date();
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        DateFormat dateTimeFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss aa");
        printer.printString(df.format(date));
        printer.printString(" ");
        printer.printString(" ");
        printer.printString("Sale transaction");
        printer.setAlignment(0);
        printer.printString("Order Number : " + orderNumber);
        printer.printString("Seat Number : " + seatNumber);
        float total = 0;
        for(SoldItem item : soldItems){
            total += Float.parseFloat(item.getPrice())* Integer.parseInt(item.getQuantity());
            int itemNameLength = item.getItemDesc().length();
            String totalAmount = POSCommonUtils.getTwoDecimalFloatFromFloat(
                    Float.parseFloat(item.getPrice())*Integer.parseInt(item.getQuantity()));
            int totalAmountLength = totalAmount.length();
            int spaceLength = 32 - (itemNameLength+totalAmountLength);
            printer.printString(item.getItemDesc()
                    + new String(new char[spaceLength]).replace("\0", " ") + totalAmount);
            printer.printString(item.getItemId() + " Each $" + item.getPrice() );
        }
        printer.printString(" ");
        printer.setAlignment(2);
        printer.printString("Total USD " + POSCommonUtils.getTwoDecimalFloatFromFloat(total));
        if(discount != null && !discount.isEmpty()) {
            total -= Float.parseFloat(discount);
            printer.printString("Discount USD " + POSCommonUtils.getTwoDecimalFloatFromString(discount));
        }
        Float dueBalance = total;
        if(taxPercentage != null && !"null".equals(taxPercentage) && !taxPercentage.isEmpty()){
            printer.printString("Service Tax " + taxPercentage + "%");
            dueBalance = total * ((100 + Float.parseFloat(taxPercentage)) / 100);
        }
        printer.printString("Sub Total USD " + POSCommonUtils.getTwoDecimalFloatFromFloat(dueBalance));
        printer.setBold(false);
        printer.setAlignment(0);
        Iterator it = paymentMethodsMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            printer.printString(pair.getKey() + " " + pair.getValue());
        }
        if(paymentMethodsMap.get("Credit Card USD") != null){

            int numOfDigits = card.getCreditCardNumber().length();
            printer.printString(" ");
            printer.printString(new String(new char[numOfDigits-4]).replace("\0", "*")
                    + card.getCreditCardNumber().substring(numOfDigits-4,numOfDigits));
            printer.printString(card.getExpireDate());
            if(!isCustomerCopy) {
                printer.printString(card.getCardHolderName());
                printer.printString(" ");
                printer.printString(" ");
                printer.printString(".......................");
                printer.printString("Card Holder Signature");
                printer.printString("I agree to pay above total");
                printer.printString("amount according to card issuer");
                printer.printString("agreement");
                printer.printString(" ");
                printer.printString("Merchant Copy");
            }
            else{
                printer.printString("Card holder copy");
            }
        }
        printer.printString(" ");
        printer.printString(" ");
        printer.setBold(true);
        printer.printString("Operated Staff");
        printer.printString(SaveSharedPreference.getStringValues(context, Constants.SHARED_PREFERENCE_FA_NAME));
        printer.printString(dateTimeFormat.format(date));
        printer.printString(" ");
        printer.printString(" ");
        printer.close();
        return true;
    }

    private static String getFlightDetailsStr(Context context){
        POSDBHandler handler = new POSDBHandler(context);
        String flightNo = SaveSharedPreference.getStringValues(context,Constants.SHARED_PREFERENCE_FLIGHT_NAME);
        Flight flight = handler.getFlightFromFlightName(flightNo);
        return flightNo + " " + flight.getFlightFrom() +"-"+flight.getFlightTo();
    }

    public static boolean printBaggageTag(Context context,String destination,String name,String PNR, String flightNo){
        Printer printer = new Printer();
        printer.open();
        int printerStatus = printer.queState();
        if(printerStatus == 1){
            Toast.makeText(context, "Paper is not available. Please insert some papers.",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(printerStatus == 2){
            Toast.makeText(context, "Printer is too hot. Please wait.",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        Toast.makeText(context, "Printing started. Please wait.",
                Toast.LENGTH_SHORT).show();

        printer.init();
        printer.setAlignment(1);
        printer.printPictureByRelativePath(Constants.PRINTER_LOGO_LOCATION, 200, 70);
        printer.printString(" ");
        printer.printCODE128("20160601");
        printer.printString(" ");
        printer.setBold(true);
        printer.setFontwidthZoomIn(4);
        printer.setFontHeightZoomIn(4);
        printer.printString(destination);
        printer.setAlignment(0);
        printer.setFontwidthZoomIn(1);
        printer.setFontHeightZoomIn(1);
        printer.printString(" ");
        printer.setBold(false);
        printer.printString(name);
        printer.printString(PNR);
        Date date = new Date();
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        printer.printString(df.format(date));
        printer.printString(flightNo);
        printer.printString(" ");
        printer.printString(" ");
        printer.printString(" ");
        printer.printString(" ");
        printer.close();
        return true;
    }
}
