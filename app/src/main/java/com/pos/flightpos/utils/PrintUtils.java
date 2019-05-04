package com.pos.flightpos.utils;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.os.RemoteException;
import android.widget.Toast;

import com.pos.flightpos.R;
import com.pos.flightpos.objects.Constants;
import com.pos.flightpos.objects.CreditCard;
import com.pos.flightpos.objects.SoldItem;

import java.io.File;
import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import woyou.aidlservice.jiuiv5.IWoyouService;

import static com.pos.flightpos.utils.POSCommonUtils.getFlightDetailsStr;

public class PrintUtils {

    Context context;
    private IWoyouService woyouService;
    private static final String SERVICE＿PACKAGE = "woyou.aidlservice.jiuiv5";
    private static final String SERVICE＿ACTION = "woyou.aidlservice.jiuiv5.IWoyouService";

    public PrintUtils(Context context) {
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

    public void printQRCode(){
        if (woyouService == null) {
            Toast.makeText(context, "woyouService not available", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            woyouService.setAlignment(1, null);
            woyouService.printQRCode("akila Sithum", 10, 1, null);
            woyouService.lineWrap(3, null);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public boolean printOrderDetails(Context context, String orderNumber, String seatNumber, List<SoldItem> soldItems
            , Map<String,String> paymentMethodsMap, CreditCard card, boolean isCustomerCopy,
                                            String discount, String taxPercentage){

        try {
            initwoyouService();
            woyouService.setAlignment(1,null);
        String baseCurrency = SaveSharedPreference.getStringValues(context,Constants.SHARED_PREFERENCE_BASE_CURRENCY);
            Bitmap b = BitmapFactory.decodeResource(context.getResources(), R.drawable.porter_print_bg);
        woyouService.printBitmap(b,null);
        woyouService.printText("\n",null);
        woyouService.printText(getFlightDetailsStr(context) + "\n",null);
        Date date = new Date();
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        DateFormat dateTimeFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss aa");
        woyouService.printText(df.format(date)+"\n",null);
        woyouService.printText(" \n",null);
        woyouService.printText("Sale transaction\n",null);
        woyouService.setAlignment(0,null);
        woyouService.printText("Order No: " + orderNumber +"\n",null);
        woyouService.printText("Seat Number : " + seatNumber + "\n",null);
        float total = 0;
        for(SoldItem item : soldItems){
            total += Float.parseFloat(item.getPrice())* Integer.parseInt(item.getQuantity());
            int itemNameLength = item.getItemDesc().length();
            String totalAmount = POSCommonUtils.getTwoDecimalFloatFromFloat(
                    Float.parseFloat(item.getPrice())*Integer.parseInt(item.getQuantity()));
            int totalAmountLength = totalAmount.length();
            int spaceLength = 32 - (itemNameLength+totalAmountLength);
            woyouService.printText(item.getItemDesc()
                    + new String(new char[spaceLength]).replace("\0", " ") + totalAmount +"\n",null);
            woyouService.printText(item.getItemId() + " Each $" + item.getPrice() +"\n",null);
        }
        woyouService.printText(" ",null);
        woyouService.setAlignment(2,null);
        woyouService.printText("Total  " +baseCurrency +" "+ POSCommonUtils.getTwoDecimalFloatFromFloat(total)+"\n",null);
        if(discount != null && !discount.isEmpty()) {
            total -= Float.parseFloat(discount);
            woyouService.printText("Discount "+baseCurrency +" "+ POSCommonUtils.getTwoDecimalFloatFromString(discount)+"\n",null);
        }
        Float dueBalance = total;
        if(taxPercentage != null && !"null".equals(taxPercentage) && !taxPercentage.isEmpty()){
            woyouService.printText("Service Tax " + taxPercentage + "%\n",null);
            dueBalance = total * ((100 + Float.parseFloat(taxPercentage)) / 100);
        }
        woyouService.printText("Sub Total "+baseCurrency+" " + POSCommonUtils.getTwoDecimalFloatFromFloat(dueBalance)+"\n",null);
        //woyouService.setBold(false);
        woyouService.setAlignment(0,null);
        Iterator it = paymentMethodsMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            woyouService.printText(pair.getKey() + " " + pair.getValue()+"\n",null);
        }
        if(paymentMethodsMap.get("Credit Card USD") != null){

            int numOfDigits = card.getCreditCardNumber().length();
            woyouService.printText(" \n",null);
            woyouService.printText(new String(new char[numOfDigits-4]).replace("\0", "*")
                    + card.getCreditCardNumber().substring(numOfDigits-4,numOfDigits)+"\n",null);
            woyouService.printText(card.getExpireDate()+"\n",null);
            if(!isCustomerCopy) {
                woyouService.printText(card.getCardHolderName(),null);
                woyouService.printText("\n ",null);
                woyouService.printText("\n ",null);
                woyouService.printText(".......................\n",null);
                woyouService.printText("Card Holder Signature\n",null);
                woyouService.printText("I agree to pay above total\n",null);
                woyouService.printText("amount according to card issuer\n",null);
                woyouService.printText("agreement\n",null);
                woyouService.printText(" ",null);
                woyouService.printText("Merchant Copy\n",null);
            }
            else{
                woyouService.printText("Card holder copy\n",null);
            }
        }
        woyouService.printText(" \n",null);
        woyouService.printText(" \n",null);
        //woyouService.setBold(true);
        woyouService.printText("Operated Staff\n",null);
        woyouService.printText(SaveSharedPreference.getStringValues(context, Constants.SHARED_PREFERENCE_FA_NAME),null);
        woyouService.printText(dateTimeFormat.format(date),null);
        woyouService.printText(" \n",null);
        woyouService.printText(" \n",null);
            woyouService.printText(".",null);
        //woyouService.close();
            disconnectwoyouServiceService();
        return true;
        } catch (Exception e) {
            return false;
        }
        
    }

    public void printQr(String data, int modulesize, int errorlevel,Context context) {
        connectwoyouServiceService(context);
    }

}
