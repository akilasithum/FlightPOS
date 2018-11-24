package com.pos.flightpos;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.pos.flightpos.objects.Constants;
import com.pos.flightpos.objects.XMLMapper.PreOrder;
import com.pos.flightpos.utils.POSCommonUtils;
import com.pos.flightpos.utils.POSDBHandler;
import com.pos.flightpos.utils.SaveSharedPreference;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SellItemsActivity extends AppCompatActivity {

    long mExitTime = 0;
    POSDBHandler handler;
    Set<String> serviceType;
    String taxPercentage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sell_items);
        handler = new POSDBHandler(this);
        taxPercentage = SaveSharedPreference.getStringValues(this,
                Constants.SHARED_PREFERENCE_TAX_PERCENTAGE);
        availableServiceType();
        registerLayoutClickEvents();
    }

    private void showBOBNotification(){
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.icon_buy_onboard)
                        .setContentTitle("Pre order items")
                        .setContentText("You have 2 pre order items to proceed.");
        Intent intent = new Intent(this,BuyItemFromCategoryActivity.class);
        intent.putExtra("serviceType","POD");
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        mBuilder.setContentIntent(pendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                mNotificationManager.notify(001, mBuilder.build());
    }

    private void availableServiceType(){
        serviceType = POSCommonUtils.getServiceTypeKitCodeMap(this).keySet();
    }

    private void registerLayoutClickEvents(){

        LinearLayout buyOnBoardLayout = (LinearLayout) findViewById(R.id.buyOnBoardItems);
        if(!serviceType.contains("BOB")){
            buyOnBoardLayout.setBackground(getResources().getDrawable(R.drawable.layout_grey_out_backgroud));
        }
        buyOnBoardLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isItemsAvailableToSell("BOB")) {
                    Intent intent = new Intent(SellItemsActivity.this, BuyOnBoardItemsActivity.class);
                    intent.putExtra("serviceType", "BOB");
                    startActivity(intent);
                }
            }
        });
        LinearLayout dutyPaidLayout = (LinearLayout) findViewById(R.id.dutyPaidItems);
        if(!serviceType.contains("DTP") || taxPercentage == null){
            dutyPaidLayout.setBackground(getResources().getDrawable(R.drawable.layout_grey_out_backgroud));
        }
        dutyPaidLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isItemsAvailableToSell("DTP") && taxPercentage != null) {
                    Intent intent = new Intent(SellItemsActivity.this, BuyItemFromCategoryActivity.class);
                    intent.putExtra("serviceType", "DTP");
                    startActivity(intent);
                }
            }
        });
        LinearLayout dutyFreeLayout = (LinearLayout) findViewById(R.id.dutyFreeItems);
        if(!serviceType.contains("DTF") || taxPercentage != null){
            dutyFreeLayout.setBackground(getResources().getDrawable(R.drawable.layout_grey_out_backgroud));
        }
        dutyFreeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isItemsAvailableToSell("DTF") && taxPercentage == null) {
                    Intent intent = new Intent(SellItemsActivity.this, BuyOnBoardItemsActivity.class);
                    intent.putExtra("serviceType", "DTF");
                    startActivity(intent);
                }
            }
        });
        LinearLayout virtualInventoryLayout = (LinearLayout) findViewById(R.id.virtualInventory);
        if(!serviceType.contains("VRT")){
            virtualInventoryLayout.setBackground(getResources().getDrawable(R.drawable.layout_grey_out_backgroud));
        }
        virtualInventoryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isItemsAvailableToSell("VRT")) {
                    Intent intent = new Intent(SellItemsActivity.this, BuyOnBoardItemsActivity.class);
                    intent.putExtra("serviceType", "VRT");
                    startActivity(intent);
                }
            }
        });
        final LinearLayout preOrderLayout = (LinearLayout) findViewById(R.id.preOrderDelivery);
        /*final Map<String,List<PreOrder>> preOrders = getPreOrderList();
        if(preOrders == null || preOrders.isEmpty()) {
            preOrderLayout.setBackground(getResources().getDrawable(R.drawable.layout_grey_out_backgroud));
        }*/
        preOrderLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if(preOrders != null && !preOrders.isEmpty()) {
                    Intent intent = new Intent(SellItemsActivity.this, SelectPreOrderTypeActivity.class);
                    /*Bundle args = new Bundle();
                    args.putSerializable("preOrders",(Serializable)preOrders);
                    intent.putExtra("BUNDLE",args);*/
                    startActivity(intent);
                //}
                /*else{
                    showAlertDialog("No Items","No pre order items available for this flight");
                }*/
            }
        });

        LinearLayout voidOrdersLayout = (LinearLayout) findViewById(R.id.voidOrder);
        voidOrdersLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SellItemsActivity.this, VoidOrderActivity.class);
                startActivity(intent);
            }
        });

        LinearLayout exchangeRatesLayout = (LinearLayout) findViewById(R.id.exchangeRates);
        exchangeRatesLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SellItemsActivity.this, ExchangeRateActivity.class);
                startActivity(intent);
            }
        });

        LinearLayout announcementLayout = (LinearLayout) findViewById(R.id.announcements);
        announcementLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SellItemsActivity.this, AnnouncementActivity.class);
                startActivity(intent);
            }
        });

        LinearLayout closeFlightLayout = (LinearLayout) findViewById(R.id.closeFlight);
        closeFlightLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SellItemsActivity.this, CloseFlightActivity.class);
                startActivity(intent);
            }
        });
    }

    private boolean isItemsAvailableToSell(String service){
        if(!serviceType.contains(service)){
            showAlertDialog("No items available to sell.");
            return false;
        }
        else {
            return true;
        }
    }

    private void showAlertDialog(String body){
        Toast.makeText(this,body , Toast.LENGTH_SHORT).show();
    }

    /*private Map<String,List<PreOrder>> getPreOrderList(){
        return handler.getAvailablePreOrders(serviceType,"faUser");
    }*/

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
}
