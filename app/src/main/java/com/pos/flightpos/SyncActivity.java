package com.pos.flightpos;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import com.pos.flightpos.utils.HttpHandler;
import com.pos.flightpos.utils.POSDBHandler;
import com.pos.flightpos.utils.SaveSharedPreference;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class SyncActivity extends AppCompatActivity {

    LinearLayout syncLayout;
    POSDBHandler handler;
    List<String> completedFiles;
    AlphaAnimation inAnimation;
    AlphaAnimation outAnimation;
    FrameLayout progressBarHolder;
    String parent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync);
        syncLayout = findViewById(R.id.syncLayout);
        handler = new POSDBHandler(this);
        parent = getIntent().getExtras().getString("parent");
        progressBarHolder = findViewById(R.id.progressBarHolder);
        if(handler.isUsersSynced() && parent.equalsIgnoreCase("SplashActivity")){
            Intent mainIntent = new Intent(SyncActivity.this,FlightAttendentLogin.class);
            SyncActivity.this.startActivity(mainIntent);
        }
        else{
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
                                redirectToParentPage();
                            }}).show();
            }
        }
    }

    private void redirectToParentPage(){
        if(parent.equalsIgnoreCase("SplashActivity")){
            Intent intent = new Intent(SyncActivity.this, FlightAttendentLogin.class);
            startActivity(intent);
        }
        else{
            Intent intent = new Intent(SyncActivity.this, OptionsActivity.class);
            startActivity(intent);
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

    private class GetContacts extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            inAnimation = new AlphaAnimation(0f, 1f);
            inAnimation.setDuration(200);
            progressBarHolder.setAnimation(inAnimation);
            progressBarHolder.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            HttpHandler sh = new HttpHandler();
            handler.insertComboDiscount(sh.makeServiceCall("promotions"));
            completedFiles.add("promotions");
            handler.insertFlightData(sh.makeServiceCall("flights"));
            completedFiles.add("flights");
            handler.insertItemData(sh.makeServiceCall("items"));
            completedFiles.add("items");
            handler.insertCurrencyData(sh.makeServiceCall("currencies"));
            completedFiles.add("currencies");
            handler.insertVoucherDetails(sh.makeServiceCall("vouchers"));
            completedFiles.add("vouchers");
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
            super.onPostExecute(aVoid);
            outAnimation = new AlphaAnimation(1f, 0f);
            outAnimation.setDuration(200);
            progressBarHolder.setAnimation(outAnimation);
            progressBarHolder.setVisibility(View.GONE);
            new AlertDialog.Builder(SyncActivity.this)
                    .setMessage("Sync Completed")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            redirectToParentPage();
                        }})
                    .show();
        }
    }

    private String saveToInternalStorage(Bitmap bitmapImage,String itemCode){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
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
