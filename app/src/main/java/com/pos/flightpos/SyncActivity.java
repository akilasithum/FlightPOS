package com.pos.flightpos;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.AWSStartupHandler;
import com.amazonaws.mobile.client.AWSStartupResult;
import com.amazonaws.mobileconnectors.s3.transferutility.*;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.util.StringUtils;
import com.pos.flightpos.utils.POSDBHandler;
import com.pos.flightpos.utils.SaveSharedPreference;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SyncActivity extends AppCompatActivity {

    LinearLayout syncLayout;
    POSDBHandler handler;
    ProgressDialog dia;
    List<String> completedFiles;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync);
        syncLayout = (LinearLayout) findViewById(R.id.syncLayout);
        handler = new POSDBHandler(this);
        SaveSharedPreference.setStringValues(this,"syncKeyPressed","true");
        dia = new ProgressDialog(this);
        dia.setTitle("Sync");
        dia.setMessage("POS sync is in progress. Please wait...");
        dia.show();
        completedFiles = new ArrayList<>();
        downloadData("users");
        downloadData("flights");
        downloadData("item_list");
        downloadData("kit_number_list");
        downloadData("kit_list");
        downloadData("currency");
        downloadData("promotions");
        downloadData("combo_discount");
        downloadData("equipment_type");

    }

    public void downloadData(final String fileName) {
        AWSMobileClient.getInstance().initialize(this, new AWSStartupHandler() {
            @Override
            public void onComplete(AWSStartupResult awsStartupResult) {
                downloadWithTransferUtility(fileName);
            }
        }).execute();
    }

    public void downloadWithTransferUtility(final String fileName) {

        TransferUtility transferUtility =
                TransferUtility.builder()
                        .context(getApplicationContext())
                        .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                        .s3Client(new AmazonS3Client(AWSMobileClient.getInstance().getCredentialsProvider()))
                        .build();

        TransferObserver downloadObserver =
                transferUtility.download("posappbucket",
                        fileName+".xml",
                        new File(getApplicationContext().getFilesDir(),fileName+".xml"));

        // Attach a listener to the observer to get state update and progress notifications
        downloadObserver.setTransferListener(new TransferListener() {

            @Override
            public void onStateChanged(int id, TransferState state) {
                if (TransferState.COMPLETED == state) {
                    insertDataIntoSQLIteDB(fileName);
                    //showCompletedFiles(fileName);
                    completedFiles.add(fileName);
                    if(fileName.equals("equipment_type")){
                        //dia.cancel();
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
                                        startActivity(intent);
                                    }})
                                .setNegativeButton(android.R.string.cancel, null).show();
                    }
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                float percentDonef = ((float)bytesCurrent/(float)bytesTotal) * 100;
                int percentDone = (int)percentDonef;

                /*Toast.makeText(getApplicationContext(),
                        "File Name: "+fileName+"   ID:" + id + "   bytesCurrent: " + bytesCurrent + "   bytesTotal: " + bytesTotal + " " + percentDone + "%",
                        Toast.LENGTH_SHORT).show();*/
            }

            @Override
            public void onError(int id, Exception ex) {
                Toast.makeText(getApplicationContext(), ex.toString(),
                        Toast.LENGTH_SHORT).show();
            }

        });

        // If you prefer to poll for the data, instead of attaching a
        // listener, check for the state and progress in the observer.
        if (TransferState.COMPLETED == downloadObserver.getState()) {
            insertDataIntoSQLIteDB(fileName);
        }

       /* Toast.makeText(getApplicationContext(), "Bytes Transferred: " + downloadObserver.getBytesTransferred(),
                Toast.LENGTH_SHORT).show();*/
    }

    private void showCompletedFiles(String fileName){

        TextView view = new TextView(this);
        view.setText("Sync " +fileName+" completed.");
        view.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));
        view.setTextSize(25);
        view.setTextColor(Color.GREEN);
        syncLayout.addView(view);
    }

    private void insertDataIntoSQLIteDB(String fileName){
        if(fileName.equals("users"))handler.insertUserData(getApplicationContext());
        if(fileName.equals("flights"))handler.insertFlightData(getApplicationContext());
        if(fileName.equals("item_list"))handler.insertItemData(getApplicationContext());
        if(fileName.equals("kit_number_list"))handler.insertKITNumbersList(getApplicationContext());
        if(fileName.equals("kit_list"))handler.insertKITList(getApplicationContext());
        if(fileName.equals("currency")) handler.insertCurrencyData(getApplicationContext());
        if(fileName.equals("equipment_type"))handler.insertEquipmentTypeList(getApplicationContext());
        if(fileName.equals("pre_orders"))handler.insertPreOrders(getApplicationContext());
        if(fileName.equals("promotions"))handler.insertPromotions(getApplicationContext());
        if(fileName.equals("combo_discount"))handler.insertComboDiscount(getApplicationContext());
    }
}
