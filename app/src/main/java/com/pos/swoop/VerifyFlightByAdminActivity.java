package com.pos.swoop;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.pos.swoop.objects.Constants;
import com.pos.swoop.objects.XMLMapper.CartNumber;
import com.pos.swoop.objects.XMLMapper.KITItem;
import com.pos.swoop.objects.XMLMapper.SIFDetails;
import com.pos.swoop.utils.HttpHandler;
import com.pos.swoop.utils.POSCommonUtils;
import com.pos.swoop.utils.POSDBHandler;
import com.pos.swoop.utils.SaveSharedPreference;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class VerifyFlightByAdminActivity extends AppCompatActivity {
    ImageButton flightUserLoginLayout;
    LinearLayout verifyInventoryLayout;
    LinearLayout printReport;
    LinearLayout addSealsLayout;
    LinearLayout syncPreOrderLayout;
    //LinearLayout defineCartNumbersLayout;
    POSDBHandler handler;
    List<String> kitCode;
    long mExitTime = 0;
    Set<String> serviceType;
    LinearLayout preOrderPackLayout;
    private ProgressDialog dialog;
    ProgressDialog dia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_flight_by_admin);
        flightUserLoginLayout = (ImageButton) findViewById(R.id.userLoginLayout);
        verifyInventoryLayout = (LinearLayout) findViewById(R.id.verifyInventoryByAdmin);
        printReport = (LinearLayout) findViewById(R.id.printReportByAdmin);
        addSealsLayout = (LinearLayout) findViewById(R.id.addAdminSeal);
        syncPreOrderLayout = (LinearLayout) findViewById(R.id.syncPreOrderLayout);
        //defineCartNumbersLayout = (LinearLayout) findViewById(R.id.defineCartNumbers);
        preOrderPackLayout = findViewById(R.id.packPreOrderLayout);
        handler = new POSDBHandler(this);
        kitCode = POSCommonUtils.availableKitCodes(this);
        serviceType = POSCommonUtils.getServiceTypeKitCodeMap(this).keySet();
        registerLayoutClickEvents();
        //disablePackPreOrderLayout(false);
        String deviceId = SaveSharedPreference.getStringValues(this,Constants.SHARED_PREFERENCE_DEVICE_ID);
        handler.updateSIFDetails("packedDateTime",POSCommonUtils.getDateTimeString(),deviceId);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
    }

    private void disablePackPreOrderLayout(boolean isEnable){
        String isPreOrderSynced = SaveSharedPreference.getStringValues(this,Constants.SHARED_PREFERENCE_SYNC_PRE_ORDERS);
        if((isPreOrderSynced == null || !isPreOrderSynced.equals("yes")) && !isEnable) {
            preOrderPackLayout.getChildAt(0).setBackgroundResource(R.drawable.pack_pre_order_grey);

        }
        else{
            preOrderPackLayout.getChildAt(0).setBackgroundResource(R.drawable.pack_pre_order_icon);
        }
    }

    private void registerLayoutClickEvents() {

        flightUserLoginLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showConfirmation();
            }
        });

        syncPreOrderLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AsyncTask<Void, Void, Void> task = new SyncPreOrders(VerifyFlightByAdminActivity.this).execute();
            }
        });

        verifyInventoryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(VerifyFlightByAdminActivity.this, VerifyInventoryActivity.class);
                intent.putExtra("parent", "VerifyFlightByAdminActivity");
                startActivity(intent);
            }
        });

        printReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                printInventoryReport();
            }
        });
        addSealsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addAdminSeals();
            }
        });
        preOrderPackLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String isPreOrderSynced = SaveSharedPreference.getStringValues(VerifyFlightByAdminActivity.this,
                        Constants.SHARED_PREFERENCE_SYNC_PRE_ORDERS);
                if(isPreOrderSynced != null && isPreOrderSynced.equals("yes")) {
                    Intent intent = new Intent(VerifyFlightByAdminActivity.this, LoadPreOrderAdminActivity.class);
                    startActivity(intent);
                }
                else{
                    Toast.makeText(getApplicationContext(), "Please sync pre orders.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showConfirmation() {
        String openSeals = handler.getSealList(null,"outbound");
        String cartScan = SaveSharedPreference.getStringValues(this,Constants.SHARED_PREFERENCE_CART_SCAN);
        if (openSeals == null || openSeals.length() == 0) {
            Toast.makeText(getApplicationContext(), "Add opening seals before continue.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        else if(cartScan == null || cartScan.isEmpty()){
            Toast.makeText(getApplicationContext(), "Scan carts before continue",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Logging out")
                .setMessage("You are about to log out from admin mode. Do you wish to continue?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        secondSync();
                    }
                })
                .setNegativeButton(android.R.string.no, null).show();
    }

    private class SyncPreOrders extends AsyncTask<Void, Void, Void> {

        public SyncPreOrders(Context context) {
            dialog = new ProgressDialog(context);
        }

        protected void onPreExecute() {
            dialog.setMessage("Sync in progress. Please wait ...");
            dialog.show();
        }
        @Override
        protected Void doInBackground(Void... voids) {
            HttpHandler sh = new HttpHandler();
            handler.insertPreOrders(sh.makeGetCallWithParams("preOrders","flightNumber="+
                    SaveSharedPreference.getStringValues(VerifyFlightByAdminActivity.this,Constants.SHARED_PREFERENCE_FLIGHT_NAME).replace(" ","--")+"&flightDate="+
                    SaveSharedPreference.getStringValues(VerifyFlightByAdminActivity.this,Constants.SHARED_PREFERENCE_FLIGHT_DATE).replace("/","-")));
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            SaveSharedPreference.setStringValues(VerifyFlightByAdminActivity.this,Constants.SHARED_PREFERENCE_SYNC_PRE_ORDERS,"yes");
            disablePackPreOrderLayout(true);
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }

    private void addAdminSeals() {
        String noOfSealsStr = handler.getNoOfSealsFromKitCodes(kitCode);
        SaveSharedPreference.setStringValues(this, Constants.SHARED_PREFERENCE_NO_OF_SEAL, noOfSealsStr);
        Intent intent = new Intent(this, SelectServiceTypeForSealActivity.class);
        intent.putExtra("parent","VerifyFlightByAdminActivity");
        startActivity(intent);
    }

    private void printInventoryReport() {
        Intent intent = new Intent(this, PrintInventorActivity.class);
        intent.putExtra("parent", "VerifyFlightByAdminActivity");
        startActivity(intent);

    }

    public  void secondSync(){
        new GetContacts(this).execute();
    }

    private class GetContacts extends AsyncTask<Void, Void, Void> {

        public GetContacts(Context context) {
            dialog = new ProgressDialog(context);
        }

        protected void onPreExecute() {
            dialog.setMessage("Sync in progress. Please wait ...");
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            HttpHandler httpHandler = new HttpHandler();
            httpHandler.postRequest(getSIFDetailsXML(),"sifDetails");
            httpHandler.postRequest(getCartNumbers(),"cartNumbers");
            httpHandler.postRequest(getSealDetails(),"sealDetails");
            httpHandler.postRequest(getOpeningInventory(),"openingInventory");
            handler.insertBondMessages(httpHandler.makeGetCallWithParams("messagesToHHC","flightNumber="+
                    SaveSharedPreference.getStringValues(VerifyFlightByAdminActivity.this,Constants.SHARED_PREFERENCE_FLIGHT_NAME).replace(" ","--")+"&flightDate="+
                    SaveSharedPreference.getStringValues(VerifyFlightByAdminActivity.this,Constants.SHARED_PREFERENCE_FLIGHT_DATE).replace("/","-")));
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            SaveSharedPreference.removeValue(VerifyFlightByAdminActivity.this, Constants.SHARED_PREFERENCE_ADMIN_USER);
            SaveSharedPreference.setStringValues(VerifyFlightByAdminActivity.this,
                                Constants.SHARED_PREFERENCE_CAN_ATT_LOGIN,"yes");
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            wifiManager.setWifiEnabled(false);
            Intent intent = new Intent(VerifyFlightByAdminActivity.this, FlightAttendentLogin.class);
            startActivity(intent);
        }
    }

    private String getSIFDetailsXML(){
        String deviceId = SaveSharedPreference.getStringValues(this, Constants.SHARED_PREFERENCE_DEVICE_ID);
        String sifNo = SaveSharedPreference.getStringValues(this, Constants.SHARED_PREFERENCE_SIF_NO);
        SIFDetails sif = handler.getSIFDetails(sifNo);
        org.dom4j.Document document = DocumentHelper.createDocument();
        Element root = document.addElement("sifDetails");
        root.addElement("sifNo").addText(sifNo);
        root.addElement("deviceId").addText(deviceId);
        root.addElement("packedFor").addText(sif.getPackedFor());
        root.addElement("packedTime").addText(sif.getPackedTime());
        root.addElement("programs").addText(sif.getPrograms());
        root.addElement("flightDate").addText(SaveSharedPreference.getStringValues(this,Constants.SHARED_PREFERENCE_FLIGHT_DATE));
        root.addElement("packedUser").addText(SaveSharedPreference.getStringValues(this,Constants.SHARED_PREFERENCE_ADMIN_USER_NAME));
        String kitCode = SaveSharedPreference.getStringValues(this, Constants.SHARED_PREFERENCE_KIT_CODE);
        root.addElement("kitCodes").addText(kitCode);
        return document.asXML();
    }

    private String getCartNumbers(){
        List<CartNumber> cartNumbers = handler.getCartNumbers();
        String sifNo = SaveSharedPreference.getStringValues(this, Constants.SHARED_PREFERENCE_SIF_NO);
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("cartNumbers");
        for(CartNumber cartNumber : cartNumbers){
            Element orderMainDetail = root.addElement("cartNumber");
            orderMainDetail.addElement("cartNumber").addText(cartNumber.getCartNumber());
            orderMainDetail.addElement("serviceType").addText(cartNumber.getServiceType());
            orderMainDetail.addElement("sifNo").addText(sifNo);
            orderMainDetail.addElement("packType").addText(cartNumber.getEquipmentType());
        }
        if(cartNumbers.size() == 1){
            Element orderMainDetail = root.addElement("cartNumber");
            orderMainDetail.addElement("cartNumber").addText("");
            orderMainDetail.addElement("serviceType").addText("");
            orderMainDetail.addElement("sifNo").addText("");
            orderMainDetail.addElement("packType").addText("");
        }
        return document.asXML();
    }

    private String getSealDetails(){

        String outboundSeals = handler.getSealList(null,"outbound");
        List<String> sealsList = Arrays.asList(outboundSeals.split(","));
        String sifNo = SaveSharedPreference.getStringValues(this, Constants.SHARED_PREFERENCE_SIF_NO);
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("seals");
        for(String cartNumber : sealsList){
            Element orderMainDetail = root.addElement("seal");
            orderMainDetail.addElement("sealNumber").addText(cartNumber);
            orderMainDetail.addElement("sifNo").addText(sifNo);
            //orderMainDetail.addElement("cartNumber").addText(cartNumber);
        }
        if(sealsList.size() == 1){
            Element orderMainDetail = root.addElement("seal");
            orderMainDetail.addElement("sealNumber").addText("");
            orderMainDetail.addElement("sifNo").addText("");
            //orderMainDetail.addElement("cartNumber").addText("");
        }
        return document.asXML();
    }

    private String getOpeningInventory(){
        String sifNo = SaveSharedPreference.getStringValues(this, Constants.SHARED_PREFERENCE_SIF_NO);
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("inventories");
        List<String> eqNoList = POSCommonUtils.getAvailableEquipmentTypes(this);
        Map<String,String> eqNoBarcodeMap = handler.getEqNoCartNoMap();
        List<KITItem> items = handler.getAllKitItems(eqNoList);
        for(KITItem item : items){
            Element orderMainDetail = root.addElement("inventory");
            orderMainDetail.addElement("itemId").addText(item.getItemNo());
            orderMainDetail.addElement("quantity").addText(item.getQuantity());
            orderMainDetail.addElement("cartNo").addText(eqNoBarcodeMap.get(item.getEquipmentNo()));
            orderMainDetail.addElement("drawer").addText(item.getDrawer());
            orderMainDetail.addElement("sifNo").addText(sifNo);
        }
        if(items.size() == 1){
            Element orderMainDetail = root.addElement("inventory");
            orderMainDetail.addElement("itemId").addText("");
            orderMainDetail.addElement("quantity").addText("");
            orderMainDetail.addElement("cartNo").addText("");
            orderMainDetail.addElement("drawer").addText("");
            orderMainDetail.addElement("sifNo").addText("");
        }
        return document.asXML();
    }

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(this, ConfigureFlightActivity.class);
        startActivity(intent);
    }
}
