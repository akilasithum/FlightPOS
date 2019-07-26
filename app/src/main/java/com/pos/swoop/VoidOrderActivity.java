package com.pos.swoop;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.pos.swoop.objects.OrderDetails;
import com.pos.swoop.objects.SoldItem;
import com.pos.swoop.utils.POSDBHandler;
import com.pos.swoop.utils.PrintUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VoidOrderActivity extends AppCompatActivity {

    TableLayout orderDetailsTableLayout;
    TableLayout voidItemsBtnTable;
    TableLayout paymentMethodsTable;
    TableLayout selectItemsBtnTable;
    POSDBHandler handler;
    List<SoldItem> items;
    String orderIdStr;
    Map<String,TableRow> voidItemsList;
    TableLayout mainOrderDetailsTable;
    PrintUtils printUtils;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_void_order);
        mainOrderDetailsTable = findViewById(R.id.mainOrderDetails);
        orderDetailsTableLayout = findViewById(R.id.orderDetails);
        orderDetailsTableLayout.setVisibility(View.INVISIBLE);
        voidItemsBtnTable = findViewById(R.id.voidItemsBtnTable);
        voidItemsBtnTable.setVisibility(View.INVISIBLE);
        paymentMethodsTable = findViewById(R.id.paymentMethodsTable);
        paymentMethodsTable.setVisibility(View.INVISIBLE);
        selectItemsBtnTable = findViewById(R.id.selectItemsBtnTable);
        selectItemsBtnTable.setVisibility(View.INVISIBLE);
        handler = new POSDBHandler(this);
        printUtils = new PrintUtils(this);
        voidItemsList = new HashMap<>();
        Button selectItemsVoidBtn = findViewById(R.id.selectItemsVoidBtn);
        selectItemsVoidBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPayments();
            }
        });
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
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
    }

    private void showAvailableOrders(){

        List<OrderDetails> orderDetails = handler.getOrders();
        TableRow.LayoutParams cellParams = new TableRow.LayoutParams(0,
                TableRow.LayoutParams.WRAP_CONTENT, 4f);
        TableRow.LayoutParams cellParams1 = new TableRow.LayoutParams(0,
                50, 1f);
        TableRow.LayoutParams cellParams2 = new TableRow.LayoutParams(0,
                TableRow.LayoutParams.WRAP_CONTENT, 2f);
        for(final OrderDetails detail : orderDetails){
            final TableRow row = new TableRow(this);
            TextView orderNo = new TextView(this);
            orderNo.setText(detail.getOrderNumber());
            orderNo.setLayoutParams(cellParams);
            orderNo.setTextSize(15);
            orderNo.setGravity(Gravity.CENTER);

            TextView seatNo = new TextView(this);
            seatNo.setText(detail.getSeatNo());
            seatNo.setLayoutParams(cellParams2);
            seatNo.setTextSize(15);
            seatNo.setGravity(Gravity.CENTER);

            TextView total = new TextView(this);
            total.setText(detail.getSubTotal());
            total.setLayoutParams(cellParams2);
            total.setTextSize(15);
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
                    paymentMethodsTable.setVisibility(View.INVISIBLE);
                    voidItemsBtnTable.setVisibility(View.INVISIBLE);
                    showOrder(detail.getOrderNumber());
                }
            });
            mainOrderDetailsTable.addView(row);
        }
    }

    private void rePrintReceipt(String orderId){

        OrderDetails details = handler.getOrderDetailsFromOrderNumber(orderId);
        printUtils.printOrderDetails(this,orderId,details.getSeatNo(),items,
                handler.getPaymentMethodsMapFromOrderNumber(orderId),handler.getCreditCardDetailsFromOrderNumber(orderId),
                true,details.getDiscount(),details.getTax());
    }

    private void cancelOrder(final String orderId){
        items = handler.getSoldItemsFromOrderId(orderId);
        if(items == null || items.isEmpty()){
            Toast.makeText(this, "No items to cancel", Toast.LENGTH_SHORT).show();
            return;
        }
        for(SoldItem item : items){
            handler.updateSoldItemQty(item.getItemId(),"-"+item.getQuantity(),item.getEquipmentNo(),
            item.getDrawer());
        }
        Toast.makeText(this, "Order successfully canceled.", Toast.LENGTH_SHORT).show();
        final OrderDetails details = handler.getOrderDetailsFromOrderNumber(orderId);
        boolean isSuccess = printUtils.printVoidOrderReceipt(this,orderId,details.getSeatNo(),items,handler.getPaymentMethodsMapFromOrderNumber(orderId)
        ,details.getDiscount(),details.getTax(),false);
        if(isSuccess){
            new AlertDialog.Builder(this)
                    .setTitle("Print customer copy")
                    .setMessage("Do you want to print customer copy?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            printUtils.printVoidOrderReceipt(VoidOrderActivity.this,orderId,details.getSeatNo(),items,handler.getPaymentMethodsMapFromOrderNumber(orderId)
                                    ,details.getDiscount(),details.getTax(),true);
                        }})
                    .setNegativeButton(android.R.string.cancel, null).show();
        }
        handler.clearOrderSalesTables(orderId);
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
        selectItemsBtnTable.setVisibility(View.VISIBLE);
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
            float refundAmount = getVoidAmount();
            float amountToRefund = 0;
            for(SoldItem item : items) {
                if (voidItemsList.containsKey(item.getItemId())) {
                    amountToRefund += Float.parseFloat(item.getPrice());
                }
            }
            if(amountToRefund != refundAmount){
                Toast.makeText(VoidOrderActivity.this, "Amount to refund should be equal to cancel items total.", Toast.LENGTH_SHORT).show();
                return;
            }
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

    private void showPayments(){
        if (voidItemsList != null && !voidItemsList.isEmpty()) {
            items = handler.getSoldItemsFromOrderId(orderIdStr);
            if (items.size() == voidItemsList.size()) {

                new AlertDialog.Builder(VoidOrderActivity.this)
                        .setTitle("Cancel Order")
                        .setMessage("Do you want to cancel the whole order?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                cancelOrder(orderIdStr);
                            }
                        })
                        .setNegativeButton(android.R.string.no, null).show();

                cancelOrder(orderIdStr);
                return;
            }
            Map<String, String> paymentMethodsMap = handler.getPaymentMethodsMapFromOrderNumber(orderIdStr);
            paymentMethodsTable.setVisibility(View.VISIBLE);
            voidItemsBtnTable.setVisibility(View.VISIBLE);
            showPaymentMethodsTable(paymentMethodsMap);
        }
    }

    private void voidItemsFromList(){
        float newTotal = 0;
        final List<SoldItem> refundItems = new ArrayList<>();
        for(SoldItem item : items){
            if(voidItemsList.containsKey(item.getItemId())) {
                handler.updateSoldItemQty(item.getItemId(), "-" + item.getQuantity(), item.getEquipmentNo(),
                        item.getDrawer());
                handler.updateDailySalesTable(orderIdStr,item.getItemId());
                orderDetailsTableLayout.removeView(voidItemsList.get(item.getItemId()));
                refundItems.add(item);
            }
            else{
                newTotal += Float.parseFloat(item.getPrice());
            }
        }
        updatePaymentMethods();
        handler.updateOrderMainDetails(orderIdStr,newTotal+"");
        final OrderDetails details = handler.getOrderDetailsFromOrderNumber(orderIdStr);
        boolean isSuccess = printUtils.printVoidOrderByReceipt(this,orderIdStr,details.getSeatNo(),refundItems,false);

        if(isSuccess){
            new AlertDialog.Builder(this)
                    .setTitle("Print customer copy")
                    .setMessage("Do you want to print customer copy?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            printCustomerCopy(refundItems,details);
                        }})
                    .setNegativeButton(android.R.string.cancel, null).show();


        }

        Toast.makeText(VoidOrderActivity.this, "Void items successfully.", Toast.LENGTH_SHORT).show();
    }

    private void printCustomerCopy(List<SoldItem> refundItems,OrderDetails details){

        boolean isSuccess = printUtils.printVoidOrderByReceipt(VoidOrderActivity.this,orderIdStr,details.getSeatNo(),refundItems,true);
        if(isSuccess) {
            Intent intent = new Intent(this, SellItemsActivity.class);
            startActivity(intent);
        }
    }

    private void showPaymentMethodsTable(Map<String,String> paymentMethodsMap){
        int i = 1;
        int count = paymentMethodsTable.getChildCount();
        if(count > 1 && paymentMethodsTable.getVisibility() == View.VISIBLE){
            for(int j=1;j<count;j++){
                paymentMethodsTable.removeViewAt(1);
            }
        }
        TableRow.LayoutParams cellParams = new TableRow.LayoutParams(0,
                TableRow.LayoutParams.WRAP_CONTENT, 1f);
        for(Map.Entry<String,String> map : paymentMethodsMap.entrySet()){
            final TableRow tr = new TableRow(this);
            tr.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));

            TextView itemDesc = new TextView(this);
            itemDesc.setText(map.getKey());
            itemDesc.setTextSize(16);
            itemDesc.setLayoutParams(cellParams);
            itemDesc.setPadding(0,10,0,0);
            itemDesc.setGravity(Gravity.CENTER);
            tr.addView(itemDesc);

            TextView quantity = new TextView(this);
            quantity.setText(map.getValue());
            quantity.setTextSize(16);
            quantity.setLayoutParams(cellParams);
            quantity.setGravity(Gravity.CENTER);
            quantity.setPadding(0,10,0,0);
            tr.addView(quantity);

            EditText refundValue = new EditText(this);
            refundValue.setTextSize(16);
            refundValue.setLayoutParams(cellParams);
            refundValue.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
            tr.addView(refundValue);
            paymentMethodsTable.addView(tr,i);
            i++;
        }
    }

    private void updatePaymentMethods(){
        int count = paymentMethodsTable.getChildCount();
        for (int i = 1; i < count; i++) {
            TextView paymentMethod = (TextView) ((TableRow) paymentMethodsTable.getChildAt(i)).getChildAt(0);
            TextView initialAmount = (TextView) ((TableRow) paymentMethodsTable.getChildAt(i)).getChildAt(1);
            EditText refundAmount = (EditText) ((TableRow) paymentMethodsTable.getChildAt(i)).getChildAt(2);
            if (refundAmount.getText() != null && !refundAmount.getText().toString().isEmpty()) {
                float initialAmountFlt = Float.parseFloat(initialAmount.getText().toString());
                float refundFlt = Float.parseFloat(refundAmount.getText().toString());
                    handler.updatePaymentMethods(orderIdStr,paymentMethod.getText().toString(),String.valueOf(initialAmountFlt-refundFlt));
                if(paymentMethod.getText().toString().equals("Credit Card CAD")){
                    handler.updateCreditCardDetails(orderIdStr,String.valueOf(initialAmountFlt-refundFlt));
                }

            }
        }
    }

    private float getVoidAmount(){
        int count = paymentMethodsTable.getChildCount();
        float voidAmount = 0;
        if(count > 1){
            try {
                for (int i = 1; i < count; i++) {
                    EditText text = (EditText) ((TableRow) paymentMethodsTable.getChildAt(i)).getChildAt(2);
                    if (text.getText() != null && !text.getText().toString().isEmpty()) {
                        voidAmount += Float.parseFloat(text.getText().toString());
                    }
                }
            }catch (Exception e){
                return 0;
            }
        }
        return voidAmount;
    }
}
