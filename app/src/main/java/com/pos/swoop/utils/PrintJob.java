package com.pos.swoop.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.os.RemoteException;
import android.pt.printer.Printer;
import android.widget.Toast;

import com.pos.swoop.R;
import com.pos.swoop.objects.Constants;
import com.pos.swoop.objects.CreditCard;
import com.pos.swoop.objects.Flight;
import com.pos.swoop.objects.SoldItem;
import com.pos.swoop.objects.XMLMapper.KITItem;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import woyou.aidlservice.jiuiv5.IWoyouService;

public class PrintJob {

    Context context;
    private IWoyouService woyouService;
    private static final String SERVICE＿PACKAGE = "woyou.aidlservice.jiuiv5";
    private static final String SERVICE＿ACTION = "woyou.aidlservice.jiuiv5.IWoyouService";

    public PrintJob(Context context) {
        connectwoyouServiceService(context);
    }

    public void connectwoyouServiceService(Context context) {
        this.context = context.getApplicationContext();
        Intent intent = new Intent();
        intent.setPackage(SERVICE＿PACKAGE);
        intent.setAction(SERVICE＿ACTION);
        context.getApplicationContext().startService(intent);
        context.getApplicationContext().bindService(intent, connService, Context.BIND_AUTO_CREATE);
    }

    public void disconnectwoyouServiceService() {
        if (woyouService != null) {
            context.getApplicationContext().unbindService(connService);
            woyouService = null;
        }
    }

    private ServiceConnection connService = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            woyouService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            woyouService = IWoyouService.Stub.asInterface(service);
        }
    };

    public void initwoyouService() {
        if (woyouService == null) {
            Toast.makeText(context, "woyouService not available", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            woyouService. printerInit(null);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

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

    public boolean printVoluntaryRemovalReceipt(Context context,String orderNumber,SoldItem item){

        try {
            initwoyouService();
            woyouService.setAlignment(1, null);
            Bitmap b = BitmapFactory.decodeResource(context.getResources(), R.drawable.print_bg);
            woyouService.printBitmap(b,null);

            woyouService.printText(" \n",null);
            woyouService.printText(getFlightDetailsStr(context)+" \n",null);
            Date date = new Date();
            DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
            DateFormat dateTimeFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss aa");
            woyouService.printText(df.format(date)+" \n",null);
            woyouService.printText("Voucher Details"+" \n",null);
            woyouService.printText(" \n",null);
            woyouService.setAlignment(0,null);
            woyouService.printText("Voucher No : " + orderNumber+" \n",null);
            woyouService.printText(" \n",null);
            float total = 0;

            total += Float.parseFloat(item.getPrice())* Integer.parseInt(item.getQuantity());
            int itemNameLength = item.getItemDesc().length();
            String totalAmount = POSCommonUtils.getTwoDecimalFloatFromFloat(
                    Float.parseFloat(item.getPrice())*Integer.parseInt(item.getQuantity()));
            int totalAmountLength = totalAmount.length();
            int spaceLength = 32 - (itemNameLength+totalAmountLength);
            woyouService.printText(item.getItemDesc()
                    + new String(new char[spaceLength]).replace("\0", " ") + totalAmount+" \n",null);
            woyouService.printText(item.getItemId() + " Each $" + item.getPrice() +" Complementary"+" \n",null);

            woyouService.printText(" \n",null);
            woyouService.setAlignment(2,null);
            woyouService.printText("Total : " + POSCommonUtils.getTwoDecimalFloatFromFloat(total)+" \n",null);
            woyouService.printText(" \n",null);
            woyouService.printText(" \n",null);
            woyouService.setAlignment(0,null);
            Bitmap barcode = BitmapFactory.decodeResource(context.getResources(), R.drawable.barcode);
            woyouService.printBitmap(barcode,null);

            woyouService.printText(" \n",null);
            woyouService.printText("Operated Staff"+" \n",null);
            woyouService.printText(SaveSharedPreference.getStringValues(context, Constants.SHARED_PREFERENCE_FA_NAME)+" \n",null);
            woyouService.printText(dateTimeFormat.format(date)+" \n",null);
            woyouService.printText(" \n",null);
            woyouService.printText(" \n",null);
            woyouService.printText(" \n",null);
            return true;
        }
        catch (Exception e){
            return false;
        }

    }

    public  boolean printOrderDetails(Context context,String orderNumber,String seatNumber, List<SoldItem> soldItems
                       ,Map<String,String> paymentMethodsMap,CreditCard card,boolean isCustomerCopy,
                                            String discount,String taxPercentage,String paxName){

        /*Printer printer = new Printer();
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
        }*/

        try {
            initwoyouService();
            woyouService.setAlignment(1,null);

        Toast.makeText(context, "Printing started. Please wait.",
                Toast.LENGTH_SHORT).show();

            Bitmap b = BitmapFactory.decodeResource(context.getResources(), R.drawable.print_bg);
            woyouService.printBitmap(b,null);

            woyouService.printText("\n",null);
            woyouService.printText(getFlightDetailsStr(context)+"\n",null);
        Date date = new Date();
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy"+"\n");
        DateFormat dateTimeFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss aa"+"\n");
            woyouService.printText(df.format(date),null);
            woyouService.printText("\n",null);
            woyouService.printText("Sale transaction"+"\n",null);
        woyouService.setAlignment(0,null);
            woyouService.printText("Order No : " + orderNumber+"\n",null);
        if(paxName != null) {
            woyouService.printText("Pax Name : " + paxName +"\n",null);
        }
            woyouService.printText("Seat Number : " + seatNumber +"\n",null);
        float total = 0;
        for(SoldItem item : soldItems){
            total += Float.parseFloat(item.getPrice())* Integer.parseInt(item.getQuantity());
            int itemNameLength = item.getItemDesc().length();
            String totalAmount = POSCommonUtils.getTwoDecimalFloatFromFloat(
                    Float.parseFloat(item.getPrice())*Integer.parseInt(item.getQuantity()));
            int totalAmountLength = totalAmount.length();
            int spaceLength = 31 - (itemNameLength+totalAmountLength+item.getQuantity().length());
            woyouService.printText(item.getItemDesc() + " " + item.getQuantity()
                    + new String(new char[spaceLength]).replace("\0", " ") + totalAmount+"\n",null);
            woyouService.printText(item.getItemId() + " Each $" + item.getPrice() +"\n",null);
        }
            woyouService.printText("\n",null);
        woyouService.setAlignment(2,null);
            woyouService.printText("Total CAD " + POSCommonUtils.getTwoDecimalFloatFromFloat(total)+"\n",null);
        if(discount != null && !discount.isEmpty()) {
            total -= Float.parseFloat(discount);
            woyouService.printText("Discount CAD " + POSCommonUtils.getTwoDecimalFloatFromString(discount)+"\n",null);
        }
        Float dueBalance = total;
        if(taxPercentage != null && !"null".equals(taxPercentage) && !taxPercentage.isEmpty()){
            woyouService.printText("Service Tax " + taxPercentage + "%"+"\n",null);
            dueBalance = total * ((100 + Float.parseFloat(taxPercentage)) / 100);
        }
            woyouService.printText("Sub Total CAD " + POSCommonUtils.getTwoDecimalFloatFromFloat(dueBalance) +"\n",null);
        woyouService.setAlignment(0,null);
        Iterator it = paymentMethodsMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            woyouService.printText(pair.getKey() + " " + pair.getValue()+"\n",null);
        }
        if(paymentMethodsMap.get("Credit Card CAD") != null){

            int numOfDigits = card.getCreditCardNumber().length();
            woyouService.printText("\n",null);
            woyouService.printText(new String(new char[numOfDigits-4]).replace("\0", "*")
                    + card.getCreditCardNumber().substring(numOfDigits-4,numOfDigits)+"\n",null);
            woyouService.printText(card.getExpireDate(),null);
            if(!isCustomerCopy) {
                woyouService.printText(card.getCardHolderName(),null);
                woyouService.printText("\n",null);
                woyouService.printText("\n",null);
                woyouService.printText(".......................",null);
                woyouService.printText("Card Holder Signature" +"\n",null);
                woyouService.printText("I agree to pay above total" +"\n",null);
                woyouService.printText("amount according to card issuer"+"\n",null);
                woyouService.printText("agreement"+"\n",null);
                woyouService.printText("\n",null);
                woyouService.printText("Merchant Copy"+"\n",null);
            }
            else{
                woyouService.printText("Card holder copy",null);
            }
        }
            woyouService.printText("\n",null);
            woyouService.printText("\n",null);
            woyouService.printText("Operated Staff"+"\n",null);
            woyouService.printText(SaveSharedPreference.getStringValues(context, Constants.SHARED_PREFERENCE_FA_NAME)+"\n",null);
            woyouService.printText(dateTimeFormat.format(date)+"\n",null);
            woyouService.printText("\n",null);
            woyouService.printText("\n",null);
            woyouService.printText("\n",null);
        }
        catch (Exception e){
            return false;
        }
        return true;
    }

    private static String getFlightDetailsStr(Context context){
        POSDBHandler handler = new POSDBHandler(context);
        String flightNo = SaveSharedPreference.getStringValues(context,Constants.SHARED_PREFERENCE_FLIGHT_NAME);
        Flight flight = handler.getFlightFromFlightName(flightNo);
        return flightNo + " " + flight.getFlightFrom() +"-"+flight.getFlightTo();
    }

    public boolean printBaggageTag(Context context,String destination,String name,String PNR, String flightNo,int i){

        try {
            initwoyouService();
            woyouService.setAlignment(1, null);
            Bitmap b = BitmapFactory.decodeResource(context.getResources(), R.drawable.print_bg);
            woyouService.printBitmap(b,null);
            woyouService.printText("\n",null);
            Bitmap baggageTag = BitmapFactory.decodeResource(context.getResources(), R.drawable.baggage_tag);
            woyouService.printBitmap(baggageTag,null);
            //printer.printCODE128("20160601");
            woyouService.printText("\n",null);
            woyouService.setFontSize(30,null);
            woyouService.printText(destination+"\n",null);
            woyouService.setAlignment(0,null);
            woyouService.setFontSize(20,null);
            woyouService.printText("\n",null);
            woyouService.printText(name+"\n",null);
            woyouService.printText(PNR + " " + i+"\n",null);
            Date date = new Date();
            DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
            woyouService.printText(df.format(date)+"\n",null);
            woyouService.printText(flightNo+"\n",null);
            woyouService.printText("\n",null);
            woyouService.printText("\n",null);
            woyouService.printText("\n",null);
            woyouService.printText("\n",null);
            return true;
        }
        catch (Exception e){
            return false;
        }

    }

    public static boolean printVoucherDetails(Context context,String voucherName,String amount,String voucherNo,
                                              String expireDate,String passngerName){
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
        printer.setAlignment(0);
        String totalAmount = POSCommonUtils.getTwoDecimalFloatFromFloat(Float.parseFloat(amount));
        int totalAmountLength = totalAmount.length();
        int spaceLength = 32 - (voucherName.length()+totalAmountLength);
        printer.printString(voucherName
                + new String(new char[spaceLength]).replace("\0", " ") + amount);
        printer.printString("Voucher No : "+voucherNo);
        printer.printString("Expire on : " + expireDate);
        printer.printString("Passenger Name : ");
        printer.printString(passngerName);
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
}
