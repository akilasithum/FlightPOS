package com.pos.flightpos.objects;

import android.widget.LinearLayout;
import android.widget.TableRow;

public class Constants {

    public static final String SHARED_PREFERENCE_ADMIN_USER_NAME = "userName";
    public static final String SHARED_PREFERENCE_FA_NAME = "flightAttendedName";
    public static final String SHARED_PREFERENCE_KIT_CODE = "kitCode";
    public static final String SHARED_PREFERENCE_FLIGHT_NAME = "flightName";
    public static final String SHARED_PREFERENCE_FLIGHT_DATE = "flightDate";
    public static final String SHARED_ADMIN_CONFIGURE_FLIGHT = "IsAdminConfiguredFlight";
    public static final String SHARED_PREFERENCE_IS_SEAL_VERIFIED = "isSealVerified";
    public static final String SHARED_PREFERENCE_NO_OF_SEAL = "noOfSeals";
    public static final String SHARED_PREFERENCE_CART_NUM_LIST = "cartNumbersList";
    public static final String SHARED_PREFERENCE_ADMIN_USER = "userName";
    public static final String SHARED_PREFERENCE_CAN_ATT_LOGIN = "canAttLogin";
    public static final String SHARED_PREFERENCE_CLOSED_FLIGHT = "isFlightClosed";
    public static final String SHARED_PREFERENCE_FLIGHT_MODE = "flightMode";
    public static final String SHARED_PREFERENCE_FLIGHT_TYPE = "flightType";
    public static final String SHARED_PREFERENCE_TAX_PERCENTAGE = "taxPercentage";
    public static final String SHARED_PREFERENCE_FLIGHT_SECTOR = "flightSector";
    public static final String SHARED_PREFERENCE_SYNC_PRE_ORDERS = "isPreOrderSynced";
    public static final String SHARED_PREFERENCE_OUT_BOUND_SEAL_LIST = "outBoundSealList";
    public static final String SHARED_PREFERENCE_IN_BOUND_SEAL_LIST = "adminAdditionalSealList";
    public static final String SHARED_PREFERENCE_FLIGHT_ID = "flightId";
    public static final String SHARED_PREFERENCE_DEVICE_ID = "deviceId";
    public static final String SHARED_PREFERENCE_SIF_NO = "SIFNo";

    public static final String PRINTER_LOGO_LOCATION = "/res/drawable/porter_print_bg.PNG";
    public static final String FILED_NAME_SERVICE_TYPE = "serviceType";

    public static final LinearLayout.LayoutParams COMMON_LAYOUT_PARAMS = new LinearLayout.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.WRAP_CONTENT);

    public static final String PUBLIC_STATIC_STRING_IDENTIFIER = "intentIdentifier";
    //public static final String webServiceURL = "http://192.168.8.102:8080/";
    public static final String webServiceURL = "http://192.168.1.177:8080/back-office-ws/";
    //public static final String webServiceURL = "http://192.186.116.61:8080/back-office-ws/";

}
