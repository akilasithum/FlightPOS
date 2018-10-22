package com.pos.flightpos.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.pt.scan.Scan;
import android.widget.Toast;

import com.pos.flightpos.objects.Constants;
import com.pos.flightpos.objects.SoldItem;

import java.text.DecimalFormat;

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

    public static String scanQRCode(Context context){
        Scan scan = new Scan();
        String str = "";
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
}
