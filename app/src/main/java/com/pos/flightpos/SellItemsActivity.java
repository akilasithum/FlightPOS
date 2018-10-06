package com.pos.flightpos;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.pos.flightpos.objects.Constants;
import com.pos.flightpos.utils.POSDBHandler;
import com.pos.flightpos.utils.SaveSharedPreference;

public class SellItemsActivity extends AppCompatActivity {

    long mExitTime = 0;
    POSDBHandler handler;
    String serviceType;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sell_items);
        handler = new POSDBHandler(this);
        availableServiceType();
        registerLayoutClickEvents();
    }

    private void availableServiceType(){
        String kitCode = SaveSharedPreference.getStringValues(this, Constants.SHARED_PREFERENCE_KIT_CODE);
        serviceType = handler.getKitNumberListFieldValueFromKitCode(kitCode,Constants.FILED_NAME_SERVICE_TYPE);
    }

    private void registerLayoutClickEvents(){

        LinearLayout buyOnBoardLayout = (LinearLayout) findViewById(R.id.buyOnBoardItems);
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
        dutyPaidLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isItemsAvailableToSell("DTP")) {
                    Intent intent = new Intent(SellItemsActivity.this, BuyItemFromCategoryActivity.class);
                    intent.putExtra("serviceType", "DTP");
                    startActivity(intent);
                }
            }
        });
        LinearLayout dutyFreeLayout = (LinearLayout) findViewById(R.id.dutyFreeItems);
        dutyFreeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isItemsAvailableToSell("DTF")) {
                    Intent intent = new Intent(SellItemsActivity.this, BuyItemFromCategoryActivity.class);
                    intent.putExtra("serviceType", "DTF");
                    startActivity(intent);
                }
            }
        });
        LinearLayout virtualInventoryLayout = (LinearLayout) findViewById(R.id.virtualInventory);
        virtualInventoryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isItemsAvailableToSell("VRT")) {
                    Intent intent = new Intent(SellItemsActivity.this, BuyItemFromCategoryActivity.class);
                    intent.putExtra("serviceType", "VRT");
                    startActivity(intent);
                }
            }
        });
        LinearLayout preOrderLayout = (LinearLayout) findViewById(R.id.preOrderDelivery);
        preOrderLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SellItemsActivity.this, BuyItemFromCategoryActivity.class);
                intent.putExtra("serviceType","POD");
                startActivity(intent);
            }
        });

        LinearLayout exchangeRatesLayout = (LinearLayout) findViewById(R.id.exchangeRates);
        exchangeRatesLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Intent intent = new Intent(SellItemsActivity.this, PrintActivity.class);
                startActivity(intent);*/
            }
        });

        LinearLayout announcementLayout = (LinearLayout) findViewById(R.id.announcements);
        announcementLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Intent intent = new Intent(SellItemsActivity.this, PrintActivity.class);
                startActivity(intent);*/
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
        if(!serviceType.equals(service)){
            AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
            builder1.setTitle("Invalid Selection");
            builder1.setMessage("No items available to sell.");
            builder1.setPositiveButton(
                    "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert11 = builder1.create();
            alert11.show();
            return false;
        }
        else {
            return true;
        }
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
}
