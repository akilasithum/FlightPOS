package com.pos.flightpos;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.pos.flightpos.objects.OrderDetails;
import com.pos.flightpos.objects.SoldItem;
import com.pos.flightpos.utils.POSDBHandler;
import com.pos.flightpos.utils.PrintJob;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VoidOrderActivity extends AppCompatActivity {

    TableLayout orderDetailsTableLayout;
    TableLayout voidItemsBtnTable;
    POSDBHandler handler;
    List<SoldItem> items;
    String orderIdStr;
    Map<String,TableRow> voidItemsList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_void_order);
        orderDetailsTableLayout = findViewById(R.id.orderDetails);
        orderDetailsTableLayout.setVisibility(View.INVISIBLE);
        voidItemsBtnTable = findViewById(R.id.voidItemsBtnTable);
        voidItemsBtnTable.setVisibility(View.INVISIBLE);
        handler = new POSDBHandler(this);
        voidItemsList = new HashMap<>();
        Button cancelOrderBtn = findViewById(R.id.voidOrderBtn);
        cancelOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                voidItems();
            }
        });

        ImageButton backButton = findViewById(R.id.backPressBtn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        showAvailableOrders();
    }

    private void showAvailableOrders(){

        List<OrderDetails> orderDetails = handler.getOrders();
        final TableLayout mainOrderDetailsTable = findViewById(R.id.mainOrderDetails);
        TableRow.LayoutParams cellParams = new TableRow.LayoutParams(0,
                TableRow.LayoutParams.WRAP_CONTENT, 4f);
        TableRow.LayoutParams cellParams1 = new TableRow.LayoutParams(0,
                50, 1f);
        for(final OrderDetails detail : orderDetails){
            final TableRow row = new TableRow(this);
            TextView orderNo = new TextView(this);
            orderNo.setText(detail.getOrderNumber());
            orderNo.setLayoutParams(cellParams);
            orderNo.setTextSize(20);
            orderNo.setGravity(Gravity.CENTER);

            TextView seatNo = new TextView(this);
            seatNo.setText(detail.getSeatNo());
            seatNo.setLayoutParams(cellParams);
            seatNo.setTextSize(20);
            seatNo.setGravity(Gravity.CENTER);

            TextView total = new TextView(this);
            total.setText(detail.getSubTotal());
            total.setLayoutParams(cellParams);
            total.setTextSize(20);
            total.setGravity(Gravity.CENTER);

            Button removeItemBtn = new Button(this);
            removeItemBtn.setLayoutParams(cellParams1);
            removeItemBtn.setBackground(getResources().getDrawable(R.drawable.icon_cancel));
            removeItemBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new AlertDialog.Builder(VoidOrderActivity.this)
                            .setTitle("Cancel Order")
                            .setMessage("Do you want to cancel the whole order?")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {
                                    cancelOrder(detail.getOrderNumber());
                                    mainOrderDetailsTable.removeView(row);
                                }})
                            .setNegativeButton(android.R.string.no, null).show();
                }
            });

            Button printBtn = new Button(this);
            printBtn.setLayoutParams(cellParams1);
            printBtn.setBackground(getResources().getDrawable(R.drawable.icon_printer));
            printBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new AlertDialog.Builder(VoidOrderActivity.this)
                            .setTitle("Re-print Receipt")
                            .setMessage("Do you want to re print the receipt?")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {
                                    rePrintReceipt(detail.getOrderNumber());
                                }})
                            .setNegativeButton(android.R.string.no, null).show();
                }
            });
            row.addView(orderNo);
            row.addView(seatNo);
            row.addView(total);
            row.addView(printBtn);
            row.addView(removeItemBtn);
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showOrder(detail.getOrderNumber());
                }
            });
            mainOrderDetailsTable.addView(row);
        }
    }

    private void rePrintReceipt(String orderId){

        OrderDetails details = handler.getOrderDetailsFromOrderNumber(orderId);
        PrintJob.printOrderDetails(this,orderId,details.getSeatNo(),items,
                handler.getPaymentMethodsMapFromOrderNumber(orderId),handler.getCreditCardDetailsFromOrderNumber(orderId),
                true,details.getDiscount());
    }

    private void cancelOrder(String orderId){
        items = handler.getSoldItemsFromOrderId(orderId);
        if(items == null || items.isEmpty()){
            Toast.makeText(this, "No items to cancel", Toast.LENGTH_SHORT).show();
            return;
        }
        for(SoldItem item : items){
            handler.updateSoldItemQty(item.getItemId(),"-"+item.getQuantity(),item.getEquipmentNo(),
            item.getDrawer());
        }
        handler.clearOrderSalesTables(orderId);
        Toast.makeText(this, "Order successfully canceled.", Toast.LENGTH_SHORT).show();
    }

    private void showOrder(String orderId){
        items = handler.getSoldItemsFromOrderId(orderId);
        if(items == null || items.isEmpty()){
            Toast.makeText(this, "Order number is invalid.", Toast.LENGTH_SHORT).show();
            return;
        }
        if(orderDetailsTableLayout.getChildCount() > 2) {
            orderDetailsTableLayout.removeAllViews();
            addHeaderAndButton();
        }
        int i = 1;
        TableRow headerRow = new TableRow(this);
        headerRow.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));

        TableRow.LayoutParams cellParams = new TableRow.LayoutParams(0,
                TableRow.LayoutParams.WRAP_CONTENT, 4f);
        TableRow.LayoutParams cellParams1 = new TableRow.LayoutParams(0,
                50, 1f);
        TextView headerText = new TextView(this);
        headerText.setText("Order No : " + orderId);
        headerText.setTextSize(16);
        headerText.setLayoutParams(cellParams);
        headerText.setPadding(0,10,0,15);
        headerRow.addView(headerText);
        orderDetailsTableLayout.addView(headerRow,i-1);
        i++;
        orderDetailsTableLayout.setVisibility(View.VISIBLE);
        voidItemsBtnTable.setVisibility(View.VISIBLE);
        for(final SoldItem item : items){
            final TableRow tr = new TableRow(this);
            tr.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));

            TextView itemDesc = new TextView(this);
            itemDesc.setText(item.getItemDesc());
            itemDesc.setTextSize(16);
            itemDesc.setLayoutParams(cellParams);
            itemDesc.setPadding(0,10,0,0);
            itemDesc.setGravity(Gravity.CENTER);
            tr.addView(itemDesc);

            TextView quantity = new TextView(this);
            quantity.setText(item.getQuantity());
            quantity.setTextSize(16);
            quantity.setLayoutParams(cellParams);
            quantity.setGravity(Gravity.CENTER);
            quantity.setPadding(0,10,0,0);
            tr.addView(quantity);

            TextView totalPrice = new TextView(this);
            totalPrice.setText(item.getPrice());
            totalPrice.setTextSize(16);
            totalPrice.setGravity(Gravity.CENTER);
            totalPrice.setLayoutParams(cellParams);
            totalPrice.setPadding(0,10,0,0);
            tr.addView(totalPrice);

            CheckBox checkBox = new CheckBox(this);
            checkBox.setLayoutParams(cellParams1);
            checkBox.setGravity(Gravity.CENTER);
            checkBox.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                    if(isChecked)voidItemsList.put(item.getItemId(),tr);
                    else voidItemsList.remove(item.getItemId());
                }
            });
            tr.addView(checkBox);
            orderDetailsTableLayout.addView(tr,i);
            voidItemsList = new HashMap<>();
            orderIdStr = orderId;
            i++;
        }
    }
    private void addHeaderAndButton(){
        TableRow tr = new TableRow(this);
        tr.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));
        TableRow.LayoutParams cellParams = new TableRow.LayoutParams(0,
                TableRow.LayoutParams.WRAP_CONTENT, 4f);
        TableRow.LayoutParams cellParams1 = new TableRow.LayoutParams(0,
                50, 1f);
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT,4f);
        TextView itemDesc = new TextView(this);
        itemDesc.setText("Item Desc");
        itemDesc.setTextSize(20);
        itemDesc.setGravity(Gravity.CENTER);
        itemDesc.setLayoutParams(cellParams);
        itemDesc.setPadding(5,5,5,5);
        itemDesc.setBackgroundColor(getResources().getColor(R.color.lightAsh));
        tr.addView(itemDesc);

        TextView quantity = new TextView(this);
        quantity.setText("Quantity");
        quantity.setTextSize(20);
        quantity.setGravity(Gravity.CENTER);
        quantity.setLayoutParams(cellParams);
        quantity.setPadding(5,5,5,5);
        quantity.setBackgroundColor(getResources().getColor(R.color.tableDark));
        tr.addView(quantity);

        TextView totalPrice = new TextView(this);
        totalPrice.setText("Price");
        totalPrice.setTextSize(20);
        totalPrice.setGravity(Gravity.CENTER);
        totalPrice.setLayoutParams(cellParams);
        totalPrice.setPadding(5,5,5,5);
        totalPrice.setBackgroundColor(getResources().getColor(R.color.lightAsh));
        tr.addView(totalPrice);

        TextView space = new TextView(this);
        space.setLayoutParams(cellParams1);
        space.setPadding(5,5,5,5);
        tr.addView(space);
        orderDetailsTableLayout.addView(tr);
    }

    private void voidItems(){
        if (voidItemsList != null && !voidItemsList.isEmpty()) {
            new AlertDialog.Builder(VoidOrderActivity.this)
                    .setTitle("Void Items")
                    .setMessage("Do you want to void selected items from the order?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            voidItemsFromList();
                        }
                    })
                    .setNegativeButton(android.R.string.no, null).show();
        }
        else{
            Toast.makeText(VoidOrderActivity.this, "No items selected.", Toast.LENGTH_SHORT).show();
        }
    }

    private void voidItemsFromList(){
        items = handler.getSoldItemsFromOrderId(orderIdStr);
        float newTotal = 0;
        if(items.size() == voidItemsList.size()){
            cancelOrder(orderIdStr);
            return;
        }
        for(SoldItem item : items){
            if(voidItemsList.containsKey(item.getItemId())) {
                handler.updateSoldItemQty(item.getItemId(), "-" + item.getQuantity(), item.getEquipmentNo(),
                        item.getDrawer());
                handler.updateDailySalesTable(orderIdStr,item.getItemId());
                orderDetailsTableLayout.removeView(voidItemsList.get(item.getItemId()));
            }
            else{
                newTotal += Float.parseFloat(item.getPrice());
            }
        }
        handler.updateOrderMainDetails(orderIdStr,newTotal+"");
        Toast.makeText(VoidOrderActivity.this, "Void items successfully.", Toast.LENGTH_SHORT).show();
    }
}
