package com.pos.swoop;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.pos.swoop.objects.XMLMapper.PreOrder;
import com.pos.swoop.objects.XMLMapper.PreOrderItem;
import com.pos.swoop.utils.POSCommonUtils;
import com.pos.swoop.utils.POSDBHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PreOrderDeliveryActivity extends AppCompatActivity {

    POSDBHandler posdbHandler;
    Map<String,List<PreOrder>> preOrders;
    TableLayout preOrderTable;
    TableLayout preOrderItemsTable;
    //String serviceType;
    int i = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_order_delivery);
        posdbHandler = new POSDBHandler(this);
        //serviceType = getIntent().getExtras().getString("serviceType");
        preOrderTable = (TableLayout) findViewById(R.id.preOrdersTable);
        preOrders = posdbHandler.getAvailablePreOrders("faUser");
        preOrderItemsTable = findViewById(R.id.preOrderDetails);
        preOrderItemsTable.setVisibility(View.INVISIBLE);
        showPreOrdersByPriority();
        ImageButton backButton = findViewById(R.id.backPressBtn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
    }

    private void showPreOrdersByPriority(){
        Set<String> serviceTypesList = POSCommonUtils.getServiceTypeKitCodeMap(this).keySet();
        for(String serviceType : serviceTypesList){
            List<PreOrder> priorityPreOrders = preOrders.get(serviceType);
            if(priorityPreOrders != null) {
                showAvailablePreOrders(priorityPreOrders, serviceType);
                preOrders.remove(serviceType);
            }
        }
        for(Map.Entry<String,List<PreOrder>> orders : preOrders.entrySet()){
            showAvailablePreOrders(orders.getValue(),orders.getKey());
        }
    }

    private void showAvailablePreOrders(List<PreOrder> preOrders,String service){
        for(final PreOrder preOrder : preOrders) {
            final TableRow tr = new TableRow(this);
            tr.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));
            tr.setPadding(0,15,0,15);
            if(i%2 == 0) {
                tr.setBackgroundColor(getResources().getColor(R.color.white));
            }
            else{
                tr.setBackgroundColor(getResources().getColor(R.color.tableDark));
            }

            TableRow.LayoutParams cellParams1 = new TableRow.LayoutParams(0,
                    TableRow.LayoutParams.WRAP_CONTENT, 3f);
            TableRow.LayoutParams cellParams2 = new TableRow.LayoutParams(0,
                    TableRow.LayoutParams.WRAP_CONTENT, 1f);
            TextView customerDetails = new TextView(this);
            customerDetails.setText(preOrder.getPNR() + " - " + preOrder.getCustomerName());
            customerDetails.setTextSize(12);
            customerDetails.setLayoutParams(cellParams1);
            customerDetails.setPadding(10,0,0,0);
            customerDetails.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showOrder(preOrder.getPreOrderId());
                }
            });
            tr.addView(customerDetails);

            TextView serviceTypeText = new TextView(this);
            serviceTypeText.setText(service);
            serviceTypeText.setLayoutParams(cellParams2);
            serviceTypeText.setTextSize(12);
            tr.addView(serviceTypeText);

            Map<String,Integer> buttonMap = new HashMap<>();
            buttonMap.put("Not Delivered",R.drawable.icon_not_delivered);
            buttonMap.put("Delivered",R.drawable.icon_delivered);
            buttonMap.put("Rejected",R.drawable.icon_reject);
            buttonMap.put("Pax not onboard",R.drawable.icon_passenger_not_available);
            tr.addView(getImageBtnLayout(buttonMap,preOrder.getPreOrderId(),preOrder.getDelivered()));

            Spinner flightDateSpinner = new Spinner(this);
            final ArrayList<String> options=new ArrayList<String>();
            options.add("Not Delivered");
            options.add("Delivered");
            options.add("Rejected");
            options.add("Pax not onboard");
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,options);
            flightDateSpinner.setAdapter(adapter);
            flightDateSpinner.setSelection(options.indexOf(preOrder.getDelivered()));
            flightDateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    posdbHandler.updatePreOrderDeliveryStatus(options.get(i),preOrder.getItemId());
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            CheckBox checkBox = new CheckBox(this);
            checkBox.setLayoutParams(cellParams2);
            checkBox.setGravity(Gravity.CENTER);
            checkBox.setChecked(!preOrder.getDelivered().equals("No"));
            checkBox.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                    if(isChecked){
                        posdbHandler.updatePreOrderDeliveryStatus("Yes",preOrder.getItemId());
                    }
                    else{
                        posdbHandler.updatePreOrderDeliveryStatus("No",preOrder.getItemId());
                    }
                }
            });
            //tr.addView(flightDateSpinner);

            preOrderTable.addView(tr);
            i++;
        }
    }

    private LinearLayout getImageBtnLayout(Map<String,Integer> buttonList,
                                           final String itemId, final String deliveryStatus){

        TableRow.LayoutParams cellParams = new TableRow.LayoutParams(0,
                TableRow.LayoutParams.WRAP_CONTENT, 2f);
        TableRow.LayoutParams cellParams1 = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT);
        TableRow.LayoutParams cellParams2 = new TableRow.LayoutParams(40,40);
        final LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setLayoutParams(cellParams);
        mainLayout.setOrientation(LinearLayout.HORIZONTAL);
        mainLayout.setGravity(Gravity.LEFT);
        for(final Map.Entry<String,Integer> btnMap : buttonList.entrySet()) {
            final LinearLayout clickLayout = new LinearLayout(this);
            cellParams1.setMargins(2,2,2,2);
            clickLayout.setLayoutParams(cellParams1);
            if(btnMap.getKey().equals(deliveryStatus)) {
                clickLayout.setBackgroundColor(getResources().getColor(R.color.monsoon));
            }
            else{
                clickLayout.setBackgroundColor(getResources().getColor(R.color.ash));
            }
            clickLayout.setClickable(true);


            ImageButton button = new ImageButton(this);
            button.setBackground(getResources().getDrawable(btnMap.getValue()));
            button.setLayoutParams(cellParams2);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    posdbHandler.updatePreOrderDeliveryStatus(btnMap.getKey(),itemId);
                    Toast.makeText(getApplicationContext(), "Pre order items " + btnMap.getKey(),
                            Toast.LENGTH_SHORT).show();
                    setBackGroundColors(mainLayout);
                    clickLayout.setBackgroundColor(getResources().getColor(R.color.monsoon));
                }
            });
            clickLayout.addView(button);
            mainLayout.addView(clickLayout);
        }
        return mainLayout;
    }

    private void showOrder(String orderId){
        List<PreOrderItem> items = posdbHandler.getPreOrderItemsFromPreOrderId(orderId,"faUser");

        if(preOrderItemsTable.getChildCount() > 0) {
            preOrderItemsTable.removeAllViews();
        }
        TableRow headerRow = new TableRow(this);
        headerRow.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));

        TableRow.LayoutParams cellParams = new TableRow.LayoutParams(0,
                TableRow.LayoutParams.WRAP_CONTENT, 5f);
        TableRow.LayoutParams cellParams1 = new TableRow.LayoutParams(0,
                50, 2f);
        TextView headerText = new TextView(this);
        headerText.setText("Order No : " + orderId);
        headerText.setTextSize(14);
        headerText.setLayoutParams(cellParams);
        headerText.setPadding(0,10,0,15);
        headerRow.addView(headerText);
        preOrderItemsTable.addView(headerRow);
        addHeaderAndButton();
        int j = 2;
        preOrderItemsTable.setVisibility(View.VISIBLE);
        for(final PreOrderItem item : items){
            final TableRow tr = new TableRow(this);
            tr.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));

            TextView itemDesc = new TextView(this);
            String itemName = posdbHandler.getItemDescFromItemNo(item.getItemNo());
            itemDesc.setText(itemName);
            itemDesc.setTextSize(13);
            itemDesc.setLayoutParams(cellParams);
            itemDesc.setPadding(0,10,0,0);
            itemDesc.setGravity(Gravity.CENTER);
            tr.addView(itemDesc);

            TextView category = new TextView(this);
            category.setText(item.getCategory());
            category.setTextSize(13);
            category.setGravity(Gravity.CENTER);
            category.setLayoutParams(cellParams);
            category.setPadding(0,10,0,0);
            tr.addView(category);

            TextView quantity = new TextView(this);
            quantity.setText(item.getQuantity());
            quantity.setTextSize(13);
            quantity.setLayoutParams(cellParams);
            quantity.setGravity(Gravity.CENTER);
            quantity.setPadding(0,10,0,0);
            tr.addView(quantity);
            preOrderItemsTable.addView(tr,j);
            j++;
        }
    }

    private void addHeaderAndButton(){
        TableRow tr = new TableRow(this);
        tr.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));
        TableRow.LayoutParams cellParams = new TableRow.LayoutParams(0,
                TableRow.LayoutParams.WRAP_CONTENT, 5f);
        TableRow.LayoutParams cellParams1 = new TableRow.LayoutParams(0,
                50, 2f);
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT,4f);
        TextView itemDesc = new TextView(this);
        itemDesc.setText("Item Desc");
        itemDesc.setTextSize(16);
        itemDesc.setGravity(Gravity.CENTER);
        itemDesc.setLayoutParams(cellParams);
        itemDesc.setPadding(5,5,5,5);
        itemDesc.setBackgroundColor(getResources().getColor(R.color.lightAsh));
        tr.addView(itemDesc);

        TextView totalPrice = new TextView(this);
        totalPrice.setText("Category");
        totalPrice.setTextSize(16);
        totalPrice.setGravity(Gravity.CENTER);
        totalPrice.setLayoutParams(cellParams);
        totalPrice.setPadding(5,5,5,5);
        totalPrice.setBackgroundColor(getResources().getColor(R.color.tableDark));
        tr.addView(totalPrice);

        TextView quantity = new TextView(this);
        quantity.setText("Quantity");
        quantity.setTextSize(16);
        quantity.setGravity(Gravity.CENTER);
        quantity.setLayoutParams(cellParams);
        quantity.setPadding(5,5,5,5);
        quantity.setBackgroundColor(getResources().getColor(R.color.lightAsh));
        tr.addView(quantity);
        preOrderItemsTable.addView(tr);
    }

    private void setBackGroundColors(LinearLayout layout){
        for(int i=0;i<layout.getChildCount();i++){
            layout.getChildAt(i).setBackgroundColor(getResources().getColor(R.color.ash));
        }
    }
}
