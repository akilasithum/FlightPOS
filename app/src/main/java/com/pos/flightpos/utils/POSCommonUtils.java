package com.pos.flightpos.utils;

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
}