package com.pos.flightpos.objects;

import android.widget.LinearLayout;
import android.widget.TableRow;

public class Constants {

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

    public static final String PRINTER_LOGO_LOCATION = "/res/drawable/no_back.jpg";
    public static final String FILED_NAME_SERVICE_TYPE = "serviceType";

    public static final LinearLayout.LayoutParams COMMON_LAYOUT_PARAMS = new LinearLayout.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.WRAP_CONTENT);

}
