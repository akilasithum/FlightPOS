package com.pos.flightpos;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

public class SellItemsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sell_items);
        registerLayoutClickEvents();
    }

    private void registerLayoutClickEvents(){

        LinearLayout buyOnBoardLayout = (LinearLayout) findViewById(R.id.buyOnBoardItems);
        buyOnBoardLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SellItemsActivity.this, BuyItemsFromServiceTypeActivity.class);
                intent.putExtra("serviceType","BOB");
                startActivity(intent);
            }
        });
        LinearLayout dutyPaidLayout = (LinearLayout) findViewById(R.id.dutyPaidItems);
        dutyPaidLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SellItemsActivity.this, BuyItemsFromServiceTypeActivity.class);
                intent.putExtra("serviceType","DTP");
                startActivity(intent);
            }
        });
        LinearLayout dutyFreeLayout = (LinearLayout) findViewById(R.id.dutyFreeItems);
        dutyFreeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SellItemsActivity.this, BuyItemsFromServiceTypeActivity.class);
                intent.putExtra("serviceType","DTF");
                startActivity(intent);
            }
        });
        LinearLayout virtualInventoryLayout = (LinearLayout) findViewById(R.id.virtualInventory);
        virtualInventoryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SellItemsActivity.this, BuyItemsFromServiceTypeActivity.class);
                intent.putExtra("serviceType","VRT");
                startActivity(intent);
            }
        });
        LinearLayout preOrderLayout = (LinearLayout) findViewById(R.id.preOrderDelivery);
        preOrderLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SellItemsActivity.this, BuyItemsFromServiceTypeActivity.class);
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
    }
}
