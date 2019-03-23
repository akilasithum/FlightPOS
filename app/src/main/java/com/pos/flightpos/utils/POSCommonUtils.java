package com.pos.flightpos.utils;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.pt.scan.Scan;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.pos.flightpos.objects.Constants;
import com.pos.flightpos.objects.Flight;
import com.pos.flightpos.objects.SoldItem;
import com.pos.flightpos.objects.XMLMapper.ComboDiscount;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class POSCommonUtils {

    public static String getCreditCardTypeFromFirstDigit(String digit) {

        switch (digit) {
            case "3":
                return "Amex";
            case "4":
                return "Visa";
            case "5":
                return "MasterCard";
            case "6":
                return "discoverCard";
            default:
                return "";
        }
    }

    public static String getDateString() {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(date);
    }

    public static String getDateTimeString(){
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(date);
    }

    public static String getTwoDecimalFloatFromFloat(float floatVal) {
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        String formatStr = df.format(floatVal);
        if(!formatStr.contains(".")){
            formatStr = formatStr + ".00";
        }
        return getTwoDecimalFloatFromString(formatStr);
    }

    public static String getTwoDecimalFloatFromString(String floatStr) {
        String[] dividedStr = floatStr.split("\\.");
        if (dividedStr.length == 1) {
            return floatStr + ".00";
        } else {
            if (dividedStr[1].length() == 1) {
                return dividedStr[0] + "." + dividedStr[1] + "0";
            }
        }
        return floatStr;
    }

    public static String getServiceTypeDescFromServiceType(String serviceType) {
        switch (serviceType) {
            case "BOB":
                return "BUY ON BOARD INVENTORY";
            case "DTP":
                return "DUTY PAID INVENTORY";
            case "DTF":
                return "DUTY FREE INVENTORY";
            case "VRT":
                return "VIRTUAL INVENTORY";
            default:
                return "";
        }
    }

    public static String getServiceTypeFromServiceType(String serviceType) {
        switch (serviceType) {
            case "BOB":
                return "Buy on board";
            case "DTP":
                return "Duty paid";
            case "DTF":
                return "Duty free";
            case "VRT":
                return "Virtual inventory";
            default:
                return "";
        }
    }

    public static Map<String, String> scanQRCode(Context context) {
        Scan scan = new Scan();
        String str = "";
        Map<String, String> retMap = null;
        int ret = scan.open();
        if (ret < 0) {
            Toast.makeText(context, "Scanner open fails.", Toast.LENGTH_SHORT).show();
        } else {
            str = scan.scan(5000);
            if (scan == null) {
                Toast.makeText(context, "QR code not found.", Toast.LENGTH_SHORT).show();
            }
        }
        scan.close();
        return readBarcodeDetails(str, context);
    }

    public static String scanBarCode(Context context) {
        Scan scan = new Scan();
        String str = "";
        Map<String, String> retMap = null;
        int ret = scan.open();
        if (ret < 0) {
            Toast.makeText(context, "Scanner open fails.", Toast.LENGTH_SHORT).show();
        } else {
            str = scan.scan(5000);
            if (scan == null) {
                Toast.makeText(context, "QR code not found.", Toast.LENGTH_SHORT).show();
            }
        }
        scan.close();
        return str;
    }

    private static Map<String, String> readBarcodeDetails(String code, Context context) {
        try {
            String[] spaceArr = code.split(" ");
            String name = "";
            String PNR = "";
            String seatNo = "";
            if (spaceArr[0].length() <= 22) {
                name = spaceArr[0].substring(2);
                PNR = spaceArr[1].substring(2);
                String seatStr = spaceArr[4];
                seatNo = seatStr.substring(4, 8);
            } else {
                name = spaceArr[0].substring(2, 22);
                PNR = spaceArr[0].substring(spaceArr[0].length() - 7, spaceArr[0].length() - 1);
                String seatStr = spaceArr[3];
                seatNo = seatStr.substring(4, 8);
            }
            if (seatNo.substring(0, 1).equals("0")) {
                seatNo = seatNo.substring(1, 4);
            }
            Map<String, String> returnMap = new HashMap<>();
            returnMap.put("name", name);
            returnMap.put("PNR", PNR);
            returnMap.put("seatNo", seatNo);
            return returnMap;
        } catch (Exception e) {
            Toast.makeText(context, "Not a proper boarding pass.", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private static Map<String, String> getQRCodeDetailsFromStr(String qrCode, Context context) {

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
            if (divideBySpace.length == 7) {
                firstName = divideBySpace[0];
                PNR = divideBySpace[2].substring(1);
                seatNo = divideBySpace[5].substring(4, 8);
            } else if (divideBySpace.length == 6) {
                firstName = divideBySpace[0];
                PNR = divideBySpace[1].substring(1);
                seatNo = divideBySpace[4].substring(4, 8);
            } else {
                firstName = divideBySpace[0].substring(0, divideBySpace[0].length() - 7);
                PNR = divideBySpace[0].substring(divideBySpace[0].length() - 8, divideBySpace[0].length());
                seatNo = divideBySpace[3].substring(4, 8);
            }
            returnMap.put("name", lastName + "/" + firstName);
            returnMap.put("PNR", PNR);
            returnMap.put("seatNo", seatNo);
            return returnMap;
        } catch (Exception e) {
            Toast.makeText(context, "Not a proper boarding pass.", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    public static void showDrawerAndEquipment(SoldItem item, Context context) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
        builder1.setTitle("Item Location");
        builder1.setMessage("Equipment No  : " + item.getEquipmentNo() + " \n Drawer         : " + item.getDrawer());
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

    public static String getServiceType(Context context) {
        String kitCode = SaveSharedPreference.getStringValues(context, Constants.SHARED_PREFERENCE_KIT_CODE);
        POSDBHandler handler = new POSDBHandler(context);
        return handler.getKitNumberListFieldValueFromKitCode(kitCode, Constants.FILED_NAME_SERVICE_TYPE);

    }

    public static List<String> availableKitCodes(Context context) {
        String kitCode = SaveSharedPreference.getStringValues(context, Constants.SHARED_PREFERENCE_KIT_CODE);
        String[] kitCodes = kitCode.split(",");
        return Arrays.asList(kitCodes);
    }

    public static Map<String, List<String>> getServiceTypeKitCodeMap(Context context) {
        POSDBHandler handler = new POSDBHandler(context);
        return handler.getServiceTypeKitCodesMap(availableKitCodes(context));
    }

    public static String getCommaSeparateStrFromList(List<String> list) {
        String returnStr = "";
        for (String str : list) {
            returnStr += "'" + str + "',";
        }
        return returnStr.substring(0, returnStr.length() - 1);
    }

    public static String getDrawerValidationMode(String parent) {

        if (parent.equals("VerifyFlightByAdminActivity")) {
            return "admin";
        } else if (parent.equals("AttCheckInfo")) {
            return "faOpen";
        } else {
            return "faClose";
        }
    }

    public static String getIfDiscountsAvailable(List<String> itemIds, POSDBHandler handler) {

        if (itemIds != null) {
            List<ComboDiscount> discounts = handler.getComboDiscounts();
            List<Integer> discountList = new ArrayList<>();
            for (ComboDiscount comboDiscount : discounts) {
                String items = comboDiscount.getItems();
                List<String> andList = new ArrayList<>();
                Map<Integer, List<String>> orList = new HashMap<>();
                String[] andItems = items.split("and");
                int orCount = 0;
                for (int i = 0; i < andItems.length; i++) {
                    if (andItems[i].contains("or")) {
                        String[] orItems = andItems[i].split("or");
                        orList.put(orCount, new ArrayList<String>());
                        for (int j = 0; j < orItems.length; j++) {
                            orList.get(orCount).add(orItems[j].trim().replace("(", "").replace(")", ""));
                        }
                        orCount++;
                    } else {
                        andList.add(andItems[i].trim());
                    }
                }
                if (itemIds.size() >= andList.size() + orList.size()) {
                    for (String itemId : itemIds) {
                        if (andList.contains(itemId)) {
                            andList.remove(itemId);
                        } else {
                            for (Map.Entry<Integer, List<String>> entry : orList.entrySet()) {
                                if (entry.getValue().contains(itemId)) {
                                    orList.remove(entry.getKey());
                                }
                            }
                        }
                    }
                }
                if (andList.size() == 0 && orList.size() == 0) {
                    discountList.add(Integer.valueOf(comboDiscount.getDiscount()));
                }
            }
            if (discountList.size() != 0) {
                Collections.sort(discountList);
                return String.valueOf(discountList.get(discountList.size() - 1));
            }
        }
        return null;
    }

    public static String getFlightDetailsStr(Context context) {
        POSDBHandler handler = new POSDBHandler(context);
        String flightNo = SaveSharedPreference.getStringValues(context, Constants.SHARED_PREFERENCE_FLIGHT_NAME);
        Flight flight = handler.getFlightFromFlightName(flightNo);
        return flightNo + " " + flight.getFlightFrom() + "-" + flight.getFlightTo();
    }

    public static Map<String, String> getBOBItemCategories() {

        Map<String, String> catList = new HashMap<>();
        catList.put("Main", "icon_bob_main");
        catList.put("Snack", "icon_bob_snack");
        catList.put("Beverages", "icon_bob_beverage");
        catList.put("Other", "icon_bob_other");
        return catList;
    }

    public static Map<String, String> getDTFItemCategories() {

        Map<String, String> catList = new HashMap<>();
        catList.put("Liquor and Tobacco", "icon_liquor_tobacco");
        catList.put("Perfumes and Cosmetics", "icon_perfume_cosmetics");
        catList.put("Watches and Jewellery", "icon_watch_jewellery");
        catList.put("Gifts and Souvenir", "icon_gifts_souvnior");
        catList.put("Other", "icon_bob_other");
        return catList;
    }

    public static Map<String, String> getVRTItemCategories() {

        Map<String, String> catList = new HashMap<>();
        catList.put("Upgrade", "icon_upgrade");
        catList.put("Travel", "icon_travel");
        catList.put("Executions", "icon_executions");
        catList.put("Gift Cards", "icon_gifts_souvnior");
        return catList;
    }

    public static Map<String, String> getFlightDelaysCatList() {

        Map<String, String> catList = new HashMap<>();
        catList.put("Meals", "icon_bob_main");
        catList.put("Hotels", "icon_hotel");
        catList.put("Transport", "icon_transport");
        return catList;
    }

    public static Map<String, String> getVoluntoryCatList() {

        Map<String, String> catList = new HashMap<>();
        catList.put("Meals", "icon_bob_main");
        catList.put("Hotels", "icon_hotel");
        catList.put("Transport", "icon_transport");
        catList.put("Credit", "icon_credit");
        return catList;
    }

    public static Map<String, String> getBagCatList() {

        Map<String, String> catList = new HashMap<>();
        catList.put("Gate check free", "icon_gate_check_free");
        catList.put("Gate check paid", "icon_gate_check_paid");
        catList.put("Over wgt Bags", "icon_over_wgt_bags");
        catList.put("Checked Bags", "icon_check_bag");
        return catList;
    }

    public static Map<String, String> getUpgradeCatList() {

        Map<String, String> catList = new HashMap<>();
        catList.put("Economy - Business", "economy__business");
        catList.put("Economy - Premium", "economy__premium");
        catList.put("Premium - Business", "premium__business");
        return catList;
    }

    public static Map<String, String> getTransportCatList() {

        Map<String, String> catList = new HashMap<>();
        catList.put("Shuttle", "icon_shuttle");
        catList.put("Taxi", "taxi_zone_1");
        catList.put("Rail", "rail");
        catList.put("Bus", "icon_bus");
        return catList;
    }

    public static Document loadXMLFromString(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xml));
            return builder.parse(is);
        } catch (Exception e) {
            return null;
        }
    }


    public static String getDeviceId(Context context) {
        TelephonyManager telephonyManager;

        telephonyManager =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            return  telephonyManager.getDeviceId();
        }
        return null;
    }
}
