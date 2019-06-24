package com.pos.flightpos;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.pos.flightpos.objects.SoldItem;
import com.pos.flightpos.objects.XMLMapper.Item;
import com.pos.flightpos.objects.XMLMapper.PreOrder;
import com.pos.flightpos.objects.XMLMapper.PreOrderItem;
import com.pos.flightpos.utils.POSCommonUtils;
import com.pos.flightpos.utils.POSDBHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LoadPreOrderAdminActivity extends AppCompatActivity {

    POSDBHandler posdbHandler;
    Map<String,List<PreOrder>> preOrders;
    TableLayout preOrderTable;
    LinearLayout contentLayout;
    TableLayout preOrderItemsTable;
    int i = 0;
    View currentSelection;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_pre_order_admin);
        posdbHandler = new POSDBHandler(this);
        //serviceType = getIntent().getExtras().getString("serviceType");
        preOrderTable = (TableLayout) findViewById(R.id.preOrdersTable);
        contentLayout = findViewById(R.id.contentLayout);
        preOrderItemsTable = findViewById(R.id.preOrderDetails);
        preOrderItemsTable.setVisibility(View.INVISIBLE);
        preOrders = posdbHandler.getAvailablePreOrders("admin");
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

        TableRow.LayoutParams cellParams1 = new TableRow.LayoutParams(0,
                TableRow.LayoutParams.WRAP_CONTENT, 9f);
        TableRow.LayoutParams cellParams2 = new TableRow.LayoutParams(0,
                TableRow.LayoutParams.WRAP_CONTENT, 4f);
        TableRow.LayoutParams cellParams3 = new TableRow.LayoutParams(0,
                35, 3f);
        FrameLayout.LayoutParams linearLayoutParams = new FrameLayout.LayoutParams(710,
                TableRow.LayoutParams.WRAP_CONTENT);

        for(final PreOrder preOrder : preOrders) {

            final FrameLayout frameLayout = new FrameLayout(this);
            LinearLayout.LayoutParams frameLayoutParams = new LinearLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,1);
            frameLayoutParams.setMargins(8,0,0,4);
            frameLayout.setLayoutParams(frameLayoutParams);

            final LinearLayout layout = new LinearLayout(this);
            layout.setLayoutParams(linearLayoutParams);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setGravity(Gravity.CENTER);
            layout.setBackgroundColor(getResources().getColor(R.color.sellitembg));

            frameLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showOrder(preOrder.getPreOrderId());
                    layout.setBackgroundColor(getResources().getColor(R.color.white));
                    if(currentSelection == null) {
                        currentSelection = layout;
                    }
                    else {
                        layout.setBackgroundColor(getResources().getColor(R.color.sellitembg));
                        currentSelection = layout;
                    }
                }
            });

            TableRow tr = new TableRow(this);
            TableRow tr1 = new TableRow(this);
            tr.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.FILL_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));
            tr.setPadding(0,5,0,0);
            tr1.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.FILL_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));
            tr1.setPadding(0,0,0,5);

            TextView orderNo = new TextView(this);
            TextView pnr = new TextView(this);
            TextView customerName = new TextView(this);
            TextView serviceTypeTxt = new TextView(this);

            orderNo.setText(preOrder.getPreOrderId());
            orderNo.setTextSize(20);
            orderNo.setLayoutParams(cellParams3);
            orderNo.setGravity(Gravity.CENTER);
            tr.addView(orderNo);

            TextView itemDescStr = new TextView(this);
            itemDescStr.setText("Order No");
            itemDescStr.setTextSize(15);
            itemDescStr.setLayoutParams(cellParams3);
            itemDescStr.setGravity(Gravity.CENTER);
            tr1.addView(itemDescStr);

            View view  = new View(this);
            view.setLayoutParams(new TableRow.LayoutParams(3, TableRow.LayoutParams.MATCH_PARENT));
            view.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
            tr.addView(view);

            View viewDesc  = new View(this);
            viewDesc.setLayoutParams(new TableRow.LayoutParams(3, TableRow.LayoutParams.MATCH_PARENT));
            viewDesc.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
            tr1.addView(viewDesc);

            pnr.setText(preOrder.getPNR());
            pnr.setTextSize(20);
            pnr.setLayoutParams(cellParams2);
            pnr.setGravity(Gravity.CENTER);
            tr.addView(pnr);

            TextView pnrDesc = new TextView(this);
            pnrDesc.setText("PNR");
            pnrDesc.setTextSize(15);
            pnrDesc.setLayoutParams(cellParams2);
            pnrDesc.setGravity(Gravity.CENTER);
            tr1.addView(pnrDesc);

            View view2  = new View(this);
            view2.setLayoutParams(new TableRow.LayoutParams(3, TableRow.LayoutParams.MATCH_PARENT));
            view2.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
            tr.addView(view2);

            View viewDesc2  = new View(this);
            viewDesc2.setLayoutParams(new TableRow.LayoutParams(3, TableRow.LayoutParams.MATCH_PARENT));
            viewDesc2.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
            tr1.addView(viewDesc2);

            customerName.setText(preOrder.getCustomerName());
            customerName.setTextSize(20);
            customerName.setLayoutParams(cellParams1);
            customerName.setGravity(Gravity.CENTER);
            tr.addView(customerName);

            TextView customerNameDesc = new TextView(this);
            customerNameDesc.setText("Customer Name");
            customerNameDesc.setTextSize(15);
            customerNameDesc.setLayoutParams(cellParams1);
            customerNameDesc.setGravity(Gravity.CENTER);
            tr1.addView(customerNameDesc);

            View view3  = new View(this);
            view3.setLayoutParams(new TableRow.LayoutParams(3, TableRow.LayoutParams.MATCH_PARENT));
            view3.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
            tr.addView(view3);

            View viewDesc3  = new View(this);
            viewDesc3.setLayoutParams(new TableRow.LayoutParams(3, TableRow.LayoutParams.MATCH_PARENT));
            viewDesc3.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
            tr1.addView(viewDesc3);

            serviceTypeTxt.setText(service);
            serviceTypeTxt.setTextSize(20);
            serviceTypeTxt.setLayoutParams(cellParams2);
            serviceTypeTxt.setGravity(Gravity.CENTER);
            tr.addView(serviceTypeTxt);

            TextView serviceTypeTxtDesc = new TextView(this);
            serviceTypeTxtDesc.setText("Service Type");
            serviceTypeTxtDesc.setTextSize(15);
            serviceTypeTxtDesc.setLayoutParams(cellParams2);
            serviceTypeTxtDesc.setGravity(Gravity.CENTER);
            tr1.addView(serviceTypeTxtDesc);

            layout.addView(tr);
            layout.addView(tr1);
            frameLayout.addView(layout);
            contentLayout.addView(frameLayout);
        }
    }

    private void showAvailablePreOrder(List<PreOrder> preOrders,String service){
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
                    TableRow.LayoutParams.WRAP_CONTENT, 9f);
            TableRow.LayoutParams cellParams2 = new TableRow.LayoutParams(0,
                    TableRow.LayoutParams.WRAP_CONTENT, 4f);
            TableRow.LayoutParams cellParams3 = new TableRow.LayoutParams(0,
                    TableRow.LayoutParams.WRAP_CONTENT, 3f);
            TextView customerDetails = new TextView(this);
            customerDetails.setText(preOrder.getPreOrderId());
            customerDetails.setTextSize(15);
            customerDetails.setLayoutParams(cellParams2);
            customerDetails.setPadding(10,0,0,0);
            tr.addView(customerDetails);

            TextView pnr = new TextView(this);
            pnr.setText(preOrder.getPNR());
            pnr.setLayoutParams(cellParams2);
            pnr.setTextSize(15);
            tr.addView(pnr);

            TextView customerName = new TextView(this);
            customerName.setText(preOrder.getCustomerName());
            customerName.setLayoutParams(cellParams1);
            customerName.setTextSize(15);
            tr.addView(customerName);

            TextView serviceTypeText = new TextView(this);
            serviceTypeText.setText(service);
            serviceTypeText.setLayoutParams(cellParams3);
            serviceTypeText.setTextSize(15);
            tr.addView(serviceTypeText);

            //tr.addView(flightDateSpinner);
            tr.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showOrder(preOrder.getPreOrderId());
                }
            });
            preOrderTable.addView(tr);
            i++;
        }
    }

    private LinearLayout getImageBtnLayout(Map<String,Integer> buttonList, final String preOrderId,
                                           final String itemNo, final String deliveryStatus){

        TableRow.LayoutParams cellParams = new TableRow.LayoutParams(0,
                TableRow.LayoutParams.WRAP_CONTENT, 2f);
        TableRow.LayoutParams cellParams1 = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT);
        cellParams1.setMargins(2,2,2,2);
        TableRow.LayoutParams cellParams2 = new TableRow.LayoutParams(40,40);
        final LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setLayoutParams(cellParams);
        mainLayout.setOrientation(LinearLayout.HORIZONTAL);
        mainLayout.setGravity(Gravity.LEFT);
        for(final Map.Entry<String,Integer> btnMap : buttonList.entrySet()) {
            final LinearLayout clickLayout = new LinearLayout(this);
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
                    posdbHandler.updatePreOrderAdminStatus(btnMap.getKey(),preOrderId,itemNo);
                    Toast.makeText(getApplicationContext(), "Pre order item " + btnMap.getKey(),
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
        List<PreOrderItem> items = posdbHandler.getPreOrderItemsFromPreOrderId(orderId,"admin");

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
                TableRow.LayoutParams.WRAP_CONTENT, 3f);
        TextView headerText = new TextView(this);
        headerText.setText("Order No : " + orderId);
        headerText.setTextSize(16);
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
            itemDesc.setTextSize(12);
            itemDesc.setLayoutParams(cellParams);
            itemDesc.setPadding(0,10,0,0);
            itemDesc.setGravity(Gravity.CENTER);
            tr.addView(itemDesc);

            TextView category = new TextView(this);
            category.setText(item.getCategory());
            category.setTextSize(12);
            category.setGravity(Gravity.CENTER);
            category.setLayoutParams(cellParams);
            category.setPadding(0,10,0,0);
            tr.addView(category);

            TextView quantity = new TextView(this);
            quantity.setText(item.getQuantity());
            quantity.setTextSize(12);
            quantity.setLayoutParams(cellParams1);
            quantity.setGravity(Gravity.CENTER);
            quantity.setPadding(0,10,0,0);
            tr.addView(quantity);

            Map<String,Integer> buttonMap = new HashMap<>();
            buttonMap.put("Rejected",R.drawable.icon_reject);
            buttonMap.put("Loaded",R.drawable.icon_delivered);

            tr.addView(getImageBtnLayout(buttonMap,item.getPreOrderId(),item.getItemNo(),item.getAdminStatus()));

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
        TableRow.LayoutParams cellParams2 = new TableRow.LayoutParams(0,
                TableRow.LayoutParams.WRAP_CONTENT, 3f);
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT,4f);
        TextView itemDesc = new TextView(this);
        itemDesc.setText("Item");
        itemDesc.setTextSize(15);
        itemDesc.setGravity(Gravity.CENTER);
        itemDesc.setLayoutParams(cellParams);
        itemDesc.setPadding(5,5,5,5);
        itemDesc.setBackgroundColor(getResources().getColor(R.color.lightAsh));
        tr.addView(itemDesc);

        TextView totalPrice = new TextView(this);
        totalPrice.setText("Category");
        totalPrice.setTextSize(15);
        totalPrice.setGravity(Gravity.CENTER);
        totalPrice.setLayoutParams(cellParams);
        totalPrice.setPadding(5,5,5,5);
        totalPrice.setBackgroundColor(getResources().getColor(R.color.tableDark));
        tr.addView(totalPrice);

        TextView quantity = new TextView(this);
        quantity.setText("Quantity");
        quantity.setTextSize(15);
        quantity.setGravity(Gravity.CENTER);
        quantity.setLayoutParams(cellParams2);
        quantity.setPadding(5,5,5,5);
        quantity.setBackgroundColor(getResources().getColor(R.color.lightAsh));
        tr.addView(quantity);

        TextView space = new TextView(this);
        space.setLayoutParams(cellParams1);
        space.setPadding(5,5,5,5);
        tr.addView(space);
        preOrderItemsTable.addView(tr);
    }

    private void setBackGroundColors(LinearLayout layout){
        for(int i=0;i<layout.getChildCount();i++){
            layout.getChildAt(i).setBackgroundColor(getResources().getColor(R.color.ash));
        }
    }
}
