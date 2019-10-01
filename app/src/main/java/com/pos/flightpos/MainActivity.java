package com.pos.flightpos;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.pos.flightpos.objects.Constants;
import com.pos.flightpos.utils.HttpHandler;
import com.pos.flightpos.utils.POSCommonUtils;
import com.pos.flightpos.utils.POSDBHandler;
import com.pos.flightpos.utils.SaveSharedPreference;
import com.sunmi.pay.hardware.aidlv2.AidlConstantsV2;
import com.sunmi.pay.hardware.aidlv2.system.BasicOptV2;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    long mExitTime = 0;
    String parent = null;
    private ProgressDialog dialog;
    POSDBHandler handler;
    List<String> completedFiles;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        handler = new POSDBHandler(this);
        AWSMobileClient.getInstance().initialize(this).execute();
        parent = getIntent().getExtras().getString("parent");
        final LinearLayout configureFlightLayout = (LinearLayout) findViewById(R.id.configureFlightLayout);
        configureFlightLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isSyncClicked()){
                    Intent intent = new Intent(MainActivity.this, ConfigureFlightActivity.class);
                    startActivity(intent);
                }
                else{
                    Toast.makeText(getApplicationContext(), "Sync items first.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        LinearLayout syncLayout = (LinearLayout) findViewById(R.id.syncLayout);
        syncLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(POSCommonUtils.isNetworkAvailable(MainActivity.this)){
                    handler.clearTable();
                    ContextWrapper cw = new ContextWrapper(getApplicationContext());
                    File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
                    directory.delete();
                    SaveSharedPreference.setStringValues(MainActivity.this,"syncKeyPressed","true");
                    completedFiles = new ArrayList<>();
                    new SyncData(MainActivity.this).execute();
                }
                else{
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Network not available")
                            .setMessage("Please switch on wifi.")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {
                                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                                    intent.putExtra("parent", "");
                                    startActivity(intent);
                                }}).show();
                }

            }
        });

        LinearLayout uploadLayout = findViewById(R.id.uploadDataLayout);
        uploadLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(POSCommonUtils.isNetworkAvailable(MainActivity.this)){
                    Intent intent = new Intent(MainActivity.this, UploadSalesDataActivity.class);
                    startActivity(intent);
                }
                else{
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Network not available")
                            .setMessage("Please switch on wifi.")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {
                                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                                    intent.putExtra("parent", "");
                                    startActivity(intent);
                                }}).show();
                }
            }
        });
        if(parent == null || parent.isEmpty() || !parent.equals("SelectModeActivity") || parent.equals("UploadSalesDataActivity")){
            uploadLayout.setVisibility(View.GONE);
            syncLayout.setVisibility(View.VISIBLE);
        }
        else{
            syncLayout.setVisibility(View.GONE);
            uploadLayout.setVisibility(View.VISIBLE);
        }



        ImageButton adminLogoutLayout =  findViewById(R.id.adminLogutLayout);
        adminLogoutLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logoutAdmin();
            }
        });
    }

    private boolean isSyncClicked(){
        return "true".equals(SaveSharedPreference.getStringValues(this,"syncKeyPressed"));
    }

    private void logoutAdmin(){
        SaveSharedPreference.removeValue(this,"userName");
        SaveSharedPreference.removeValue(this, Constants.SHARED_PREFERENCE_FLIGHT_NAME);
        SaveSharedPreference.removeValue(this, Constants.SHARED_PREFERENCE_FLIGHT_DATE);
        Intent intent = new Intent(this,LoginActivity.class);
        intent.putExtra("parent","MainActivity");
        startActivity(intent);
    }

    @Override
    public void onBackPressed()
    {
        if((System.currentTimeMillis() - mExitTime) < 2000)
        {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        else
        {
            Toast.makeText(this, "Press the back button once again to close the application.", Toast.LENGTH_SHORT).show();
            mExitTime = System.currentTimeMillis();
        }
    }

    private String getSIFRequest() {


        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("sifDetails");
        BasicOptV2 basicOptV2 = BootUpReceiver.mBasicOptV2;
        try {
            String deviceId = basicOptV2.getSysParam(AidlConstantsV2.SysParam.SN);
            SaveSharedPreference.setStringValues(this, Constants.SHARED_PREFERENCE_DEVICE_ID,deviceId);
            root.addElement("deviceId").addText(deviceId);
        } catch (RemoteException e) {
            Toast.makeText(this,"Error while reading system data",Toast.LENGTH_SHORT);
        }
        return document.asXML();
    }

    private class SyncData extends AsyncTask<Void, Void, Void> {

        public SyncData(Context context) {
            dialog = new ProgressDialog(context);
        }

        protected void onPreExecute() {
            dialog.setMessage("Sync in progress. Please wait ...");
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            HttpHandler sh = new HttpHandler();
            String sifNo = sh.postRequest(getSIFRequest(),"sifDetails");
            SaveSharedPreference.setStringValues(MainActivity.this,Constants.SHARED_PREFERENCE_SIF_NO,sifNo);
            handler.insertSIFDetails(sifNo,SaveSharedPreference.getStringValues(MainActivity.this,Constants.SHARED_PREFERENCE_DEVICE_ID));

            handler.insertFlightData(sh.makeGetCallWithParams
                    ("flights","baseStation="+SaveSharedPreference.getStringValues(MainActivity.this,Constants.SHARED_PREFERENCE_BASE_STATION)));
            completedFiles.add("flights");
            handler.insertSectors(sh.makeGetCallWithParams
                    ("sectors","baseStation="+SaveSharedPreference.getStringValues(MainActivity.this,Constants.SHARED_PREFERENCE_BASE_STATION)));
            completedFiles.add("sectors");
            handler.insertItemData(sh.makeServiceCall("items"));
            completedFiles.add("items");
            handler.insertKITNumbersList(sh.makeServiceCall("kitCodes"));
            completedFiles.add("kitCodes");
            handler.insertKITList(sh.makeServiceCall("kitItems"));
            completedFiles.add("kitItems");
            handler.insertCurrencyData(sh.makeServiceCall("currencies"),MainActivity.this);
            completedFiles.add("currencies");
            handler.insertEquipmentTypeList(sh.makeServiceCall("equipmentType"));
            completedFiles.add("equipmentType");
            handler.insertVoucherDetails(sh.makeServiceCall("vouchers"));
            completedFiles.add("vouchers");
            handler.insertComboDiscount(sh.makeServiceCall("promotions"));
            completedFiles.add("promotions");
            handler.insertUserData(sh.makeServiceCall("users"));
            completedFiles.add("users");
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            new saveItemImages().execute();
        }
    }

    private class saveItemImages extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            HttpHandler sh = new HttpHandler();
            List<String> itemCodesList = handler.getItemCodesList();
            for(String item : itemCodesList){
                saveToInternalStorage(sh.makeServiceCallForImage(item,"itemImages"),item);
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            String fileNames = "";
            for(String str : completedFiles){
                fileNames += str + "\n";
            }
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Sync Completed")
                    .setMessage("Following files successfully synced. \n" +fileNames+
                            ". Click ok to go to main window")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            Intent intent = new Intent(MainActivity.this, MainActivity.class);
                            intent.putExtra("parent", "");
                            startActivity(intent);
                        }})
                    .setNegativeButton(android.R.string.cancel, null).show();
        }
    }

    private String saveToInternalStorage(Bitmap bitmapImage, String itemCode){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,itemCode+".png");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }

}
