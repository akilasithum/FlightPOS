package com.pos.flightpos;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.AWSStartupHandler;
import com.amazonaws.mobile.client.AWSStartupResult;
import com.amazonaws.mobileconnectors.s3.transferutility.*;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.util.StringUtils;
import com.pos.flightpos.objects.Constants;
import com.pos.flightpos.objects.SoldItem;
import com.pos.flightpos.objects.XMLMapper.Item;
import com.pos.flightpos.utils.HttpHandler;
import com.pos.flightpos.utils.POSCommonUtils;
import com.pos.flightpos.utils.POSDBHandler;
import com.pos.flightpos.utils.SaveSharedPreference;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Result;

public class SyncActivity extends AppCompatActivity {

    LinearLayout syncLayout;
    POSDBHandler handler;
    List<String> completedFiles;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync);
        syncLayout = (LinearLayout) findViewById(R.id.syncLayout);
        handler = new POSDBHandler(this);

        if(isNetworkAvailable()){
            handler.clearTable();
            ContextWrapper cw = new ContextWrapper(getApplicationContext());
            File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
            directory.delete();
            SaveSharedPreference.setStringValues(this,"syncKeyPressed","true");



            completedFiles = new ArrayList<>();
            AsyncTask<Void, Void, Void> task = new GetContacts().execute();
        }
        else{
            new AlertDialog.Builder(SyncActivity.this)
                    .setTitle("Network not available")
                    .setMessage("Please switch on wifi.")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            Intent intent = new Intent(SyncActivity.this, MainActivity.class);
                            intent.putExtra("parent", "");
                            startActivity(intent);
                        }}).show();
        }
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivity =(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivity == null) {
            return false;
        } else {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private String getSIFRequest() {

        String deviceId = POSCommonUtils.getDeviceId(this);
        SaveSharedPreference.setStringValues(this, Constants.SHARED_PREFERENCE_DEVICE_ID,deviceId);
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("sifDetails");
        root.addElement("deviceId").addText("10");
        return document.asXML();
    }

    private class GetContacts extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            HttpHandler sh = new HttpHandler();
            String sifNo = sh.postRequest(getSIFRequest(),"sifDetails");
            SaveSharedPreference.setStringValues(SyncActivity.this,Constants.SHARED_PREFERENCE_SIF_NO,sifNo);
            handler.insertSIFDetails(sifNo,SaveSharedPreference.getStringValues(SyncActivity.this,Constants.SHARED_PREFERENCE_DEVICE_ID));

            handler.insertFlightData(sh.makeGetCallWithParams
                    ("flights","baseStation="+SaveSharedPreference.getStringValues(SyncActivity.this,Constants.SHARED_PREFERENCE_BASE_STATION)));
            completedFiles.add("flights");
            handler.insertSectors(sh.makeGetCallWithParams
                    ("sectors","baseStation="+SaveSharedPreference.getStringValues(SyncActivity.this,Constants.SHARED_PREFERENCE_BASE_STATION)));
            completedFiles.add("sectors");
            handler.insertItemData(sh.makeServiceCall("items"));
            completedFiles.add("items");
            handler.insertKITNumbersList(sh.makeServiceCall("kitCodes"));
            completedFiles.add("kitCodes");
            handler.insertKITList(sh.makeServiceCall("kitItems"));
            completedFiles.add("kitItems");
            handler.insertCurrencyData(sh.makeServiceCall("currencies"),SyncActivity.this);
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
            String fileNames = "";
            for(String str : completedFiles){
                fileNames += str + "\n";
            }
            new AlertDialog.Builder(SyncActivity.this)
                    .setTitle("Sync Completed")
                    .setMessage("Following files successfully synced. \n" +fileNames+
                            ". Click ok to go to main window")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            Intent intent = new Intent(SyncActivity.this, MainActivity.class);
                            intent.putExtra("parent", "");
                            startActivity(intent);
                        }})
                    .setNegativeButton(android.R.string.cancel, null).show();
        }
    }

    private String saveToInternalStorage(Bitmap bitmapImage,String itemCode){
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
