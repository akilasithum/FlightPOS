package com.pos.flightpos.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.pt.scan.Scan;
import android.widget.Toast;

import com.pos.flightpos.ExchangeRateActivity;
import com.pos.flightpos.objects.Constants;
import com.pos.flightpos.objects.SoldItem;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class POSCommonUtils {

    public static String getCreditCardTypeFromFirstDigit(String digit){

        switch(digit){
            case "3": return "Amex";
            case "4": return "Visa";
            case "5": return "MasterCard";
            case "6": return "discoverCard";
            default: return "";
        }
    }

    public static String getTwoDecimalFloatFromFloat(float floatVal){
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        return df.format(floatVal);
    }

    public static String getServiceTypeDescFromServiceType(String serviceType){
        switch (serviceType){
            case "BOB" : return "BUY ON BOARD INVENTORY";
            case "DTP" : return "DUTY PAID INVENTORY";
            case "DTF" : return "DUTY FREE INVENTORY";
            case "VRT" : return "VIRTUAL INVENTORY";
            default: return "";
        }
    }

    public static Map<String,String> scanQRCode(Context context){
        Scan scan = new Scan();
        String str = "";
        Map<String,String> retMap = null;
        int ret= scan.open();
        if (ret<0) {
            Toast.makeText(context, "Scanner open fails.", Toast.LENGTH_SHORT).show();
        }
        else
        {
            str = scan.scan(5000);
            if(scan == null){
                Toast.makeText(context, "QR code not found.", Toast.LENGTH_SHORT).show();
            }
        }
        scan.close();
        return getQRCodeDetailsFromStr(str,context);
    }

    public static String scanBarCode(Context context){
        Scan scan = new Scan();
        String str = "";
        Map<String,String> retMap = null;
        int ret= scan.open();
        if (ret<0) {
            Toast.makeText(context, "Scanner open fails.", Toast.LENGTH_SHORT).show();
        }
        else
        {
            str = scan.scan(5000);
            if(scan == null){
                Toast.makeText(context, "QR code not found.", Toast.LENGTH_SHORT).show();
            }
        }
        scan.close();
        return str;
    }

    private static Map<String,String> getQRCodeDetailsFromStr(String qrCode, Context context){

        try {
            Map<String, String> returnMap = new HashMap<>();
            String[] divideBySlash = qrCode.split("/");
            if (divideBySlash.length != 2) {
                Toast.makeText(context, "Not a proper boarding pass.", Toast.LENGTH_SHORT).show();
                return null;
            }
            String lastName = divideBySlash[0].substring(2);
            String[] divideBySpace = divideBySlash[1].split(" ");
            String firstName = "";
            String PNR = "";
            String seatNo = "";
            if(divideBySpace.length == 7){
                firstName = divideBySpace[0];
                PNR = divideBySpace[2].substring(1);
                seatNo = divideBySpace[5].substring(4, 8);
            }
            else if (divideBySpace.length==6){
                firstName = divideBySpace[0];
                PNR = divideBySpace[1].substring(1);
                seatNo = divideBySpace[4].substring(4, 8);
            }
            else {
                firstName = divideBySpace[0].substring(0, divideBySpace[0].length() - 7);
                PNR = divideBySpace[0].substring(divideBySpace[0].length() - 8, divideBySpace[0].length());
                seatNo = divideBySpace[3].substring(4, 8);
            }
            returnMap.put("name", lastName + "/" + firstName);
            returnMap.put("PNR", PNR);
            returnMap.put("seatNo", seatNo);
            return returnMap;
        }
        catch (Exception e){
            Toast.makeText(context, "Not a proper boarding pass.", Toast.LENGTH_SHORT).show();
            return null;
        }

    }

    public static void showDrawerAndEquipment(SoldItem item,Context context){
        AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
        builder1.setTitle("Item Location");
        builder1.setMessage("Equipment No  : "+item.getEquipmentNo() +" \n Drawer         : " + item.getDrawer());
        builder1.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    public static String getServiceType(Context context){
        String kitCode = SaveSharedPreference.getStringValues(context, Constants.SHARED_PREFERENCE_KIT_CODE);
        POSDBHandler handler = new POSDBHandler(context);
       return handler.getKitNumberListFieldValueFromKitCode(kitCode,Constants.FILED_NAME_SERVICE_TYPE);

    }
}
