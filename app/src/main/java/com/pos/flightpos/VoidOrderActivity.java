package com.pos.flightpos;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.pos.flightpos.objects.Constants;
import com.pos.flightpos.objects.SoldItem;
import com.pos.flightpos.utils.POSDBHandler;
import com.pos.flightpos.utils.SaveSharedPreference;

import java.util.List;

public class VoidOrderActivity extends AppCompatActivity {

    TableLayout orderDetailsTableLayout;
    POSDBHandler handler;
    List<SoldItem> items;
    String orderIdStr;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_void_order);
        Button submitBtn = findViewById(R.id.submitBtn);
        orderDetailsTableLayout = findViewById(R.id.orderDetails);
        orderDetailsTableLayout.setVisibility(View.INVISIBLE);
        handler = new POSDBHandler(this);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOrder();
            }
        });
        Button cancelOrderBtn = findViewById(R.id.voidOrderBtn);
        cancelOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(VoidOrderActivity.this)
                        .setTitle("Close Flight")
                        .setMessage("Do you want to close the flight?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                cancelOrder();
                            }})
                        .setNegativeButton(android.R.string.no, null).show();
            }
        });
        Button receiptRePrintBtn = findViewById(R.id.receiptRePrintBtn);
        receiptRePrintBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        ImageButton backButton = findViewById(R.id.backPressBtn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void cancelOrder(){
        if(items == null || items.isEmpty()){
            Toast.makeText(this, "No items to cancel", Toast.LENGTH_SHORT).show();
            return;
        }
        for(SoldItem item : items){
            handler.updateSoldItemQty(item.getItemId(),"-"+item.getQuantity(),item.getEquipmentNo(),
            item.getDrawer());
        }
        handler.clearDailySalesItem(orderIdStr);
        Toast.makeText(this, "Order successfully canceled.", Toast.LENGTH_SHORT).show();

    }

    private void showOrder(){
        EditText orderId = findViewById(R.id.orderNumberField);

        if(orderId.getText() == null || orderId.getText().toString().equals("")){
            Toast.makeText(this, "Enter order number first", Toast.LENGTH_SHORT).show();
            return;
        }
        orderIdStr = orderId.getText().toString();
        items = handler.getSoldItemsFromOrderId(orderIdStr);
        if(items == null || items.isEmpty()){
            Toast.makeText(this, "Order number is invalid.", Toast.LENGTH_SHORT).show();
            return;
        }
        int i = 1;
        orderDetailsTableLayout.setVisibility(View.VISIBLE);
        for(SoldItem item : items){
            TableRow tr = new TableRow(this);
            tr.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));

            TableRow.LayoutParams cellParams = new TableRow.LayoutParams(0,
                    TableRow.LayoutParams.WRAP_CONTENT, 1f);
            TextView itemDesc = new TextView(this);
            itemDesc.setText(item.getItemDesc());
            itemDesc.setTextSize(16);
            itemDesc.setLayoutParams(cellParams);
            itemDesc.setPadding(0,10,0,0);
            tr.addView(itemDesc);

            TextView quantity = new TextView(this);
            quantity.setText(item.getQuantity());
            quantity.setTextSize(16);
            quantity.setLayoutParams(cellParams);
            quantity.setPadding(0,10,0,0);
            tr.addView(quantity);

            TextView totalPrice = new TextView(this);
            totalPrice.setText(item.getPrice());
            totalPrice.setTextSize(16);
            totalPrice.setLayoutParams(cellParams);
            totalPrice.setPadding(0,10,0,0);
            tr.addView(totalPrice);
            orderDetailsTableLayout.addView(tr,i);
            i++;
        }
    }
}
