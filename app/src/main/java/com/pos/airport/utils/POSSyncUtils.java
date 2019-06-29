package com.pos.airport.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.AWSStartupHandler;
import com.amazonaws.mobile.client.AWSStartupResult;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.pos.airport.objects.Constants;

import java.io.File;

public class POSSyncUtils {

    Context context;
    public POSSyncUtils(Context context){
        this.context = context;
    }

    public void downloadData(String file1,String file2){
        downloadData(file1);
        downloadData(file2);
    }

    public void downloadData(final String fileName) {
        AWSMobileClient.getInstance().initialize(context, new AWSStartupHandler() {
            @Override
            public void onComplete(AWSStartupResult awsStartupResult) {
                downloadWithTransferUtility(fileName);
            }
        }).execute();
    }

    public void downloadWithTransferUtility(final String fileName) {

        TransferUtility transferUtility =
                TransferUtility.builder()
                        .context(context)
                        .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                        .s3Client(new AmazonS3Client(AWSMobileClient.getInstance().getCredentialsProvider()))
                        .build();

        TransferObserver downloadObserver =
                transferUtility.download("posappbucket",
                        fileName+".xml",
                        new File(context.getFilesDir(),fileName+".xml"));

        // Attach a listener to the observer to get state update and progress notifications
        downloadObserver.setTransferListener(new TransferListener() {

            @Override
            public void onStateChanged(int id, TransferState state) {
                if (TransferState.COMPLETED == state) {
                    insertDataIntoSQLIteDB(fileName);
                    if(fileName.equals("pre_order_items")){
                        SaveSharedPreference.setStringValues(context, Constants.SHARED_PREFERENCE_SYNC_PRE_ORDERS,"yes");
                        new AlertDialog.Builder(context)
                                .setTitle("Sync Completed")
                                .setMessage("Pre Orders sync completed.")
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        dialog.dismiss();
                                    }}).show();
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
                Toast.makeText(context, ex.toString(),
                        Toast.LENGTH_SHORT).show();
            }

        });

        // If you prefer to poll for the data, instead of attaching a
        // listener, check for the state and progress in the observer.
        if (TransferState.COMPLETED == downloadObserver.getState()) {
        }

        Toast.makeText(context, "Bytes Transferred: " + downloadObserver.getBytesTransferred(),
                Toast.LENGTH_SHORT).show();
    }

    private void insertDataIntoSQLIteDB(String fileName) {

        POSDBHandler handler = new POSDBHandler(context);
        if(fileName.equals("pre_order_items"))handler.insertPreOrders(context);
    }
}
