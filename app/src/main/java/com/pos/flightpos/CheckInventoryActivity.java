package com.pos.flightpos;

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.pos.flightpos.objects.Constants;
import com.pos.flightpos.objects.XMLMapper.KITItem;
import com.pos.flightpos.utils.POSDBHandler;
import com.pos.flightpos.utils.SaveSharedPreference;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CheckInventoryActivity extends AppCompatActivity {

    TableLayout checkInventoryTable;
    String parent;
    String equipmentName;
    Map<String,List<KITItem>> cartItems;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_inventory);
        checkInventoryTable = (TableLayout) findViewById(R.id.checkInventoryTable);
        parent = getIntent().getExtras().getString("parent");
        equipmentName = getIntent().getExtras().getString("cartName");
        Bundle args = getIntent().getBundleExtra("BUNDLE");
        cartItems  = (Map<String,List<KITItem>>) args.getSerializable("cartItems");
        showDataInTable();
        ImageButton backButton = findViewById(R.id.backPressBtn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void addTableHeader(TableRow.LayoutParams cellParams1,TableRow.LayoutParams cellParams2){
        TableRow tr = new TableRow(this);
        tr.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));
        TextView drawer = new TextView(this);
        drawer.setText("Drawer ID");
        drawer.setBackgroundColor(ContextCompat.getColor(this, R.color.lightAsh));
        drawer.setTextSize(22);
        drawer.setLayoutParams(cellParams1);
        drawer.setGravity(Gravity.CENTER);
        tr.addView(drawer);

        TextView quantity = new TextView(this);
        quantity.setText("Quantity");
        quantity.setBackgroundColor(ContextCompat.getColor(this, R.color.ash));
        quantity.setTextSize(22);
        quantity.setLayoutParams(cellParams2);
        quantity.setGravity(Gravity.CENTER);
        tr.addView(quantity);

        if(!parent.equals("VerifyFlightByAdminActivity")) {
            TextView isValidated = new TextView(this);
            isValidated.setText("Is Validated");
            isValidated.setBackgroundColor(ContextCompat.getColor(this, R.color.lightAsh));
            isValidated.setTextSize(22);
            isValidated.setLayoutParams(cellParams2);
            isValidated.setGravity(Gravity.CENTER);
            tr.addView(isValidated);
        }
        checkInventoryTable.addView(tr);

    }

    private void showDataInTable(){
        POSDBHandler handler = new POSDBHandler(this);
        String kitCode = SaveSharedPreference.getStringValues(this, Constants.SHARED_PREFERENCE_KIT_CODE);
        Map<String,Map<String,List<KITItem>>> drawerKitItemMap = handler.getDrawerKitItemMapFromServiceType(kitCode);

        TableRow.LayoutParams cellParams1 = new TableRow.LayoutParams(0,
                TableRow.LayoutParams.WRAP_CONTENT, 3f);
        TableRow.LayoutParams cellParams2 = new TableRow.LayoutParams(0,
                TableRow.LayoutParams.WRAP_CONTENT, 2f);

        //for(Map.Entry<String,Map<String,List<KITItem>>> eqEntry : drawerKitItemMap.entrySet()) {

            //String equipmentName = eqEntry.getKey();
            TableRow headerRow = new TableRow(this);
            headerRow.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));
            TextView header = new TextView(this);
            header.setText("Equipment No : " + equipmentName);
            header.setTextSize(24);
            header.setLayoutParams(cellParams2);
            header.setPadding(0,0,0,20);
            headerRow.addView(header);
            checkInventoryTable.addView(headerRow);

            addTableHeader(cellParams1,cellParams2);

            Map<String,List<KITItem>> treeMap = new TreeMap<>(cartItems);
            for (Map.Entry<String, List<KITItem>> entry : treeMap.entrySet()) {
                final List<KITItem> kitItems = entry.getValue();
                final String drawerName = entry.getKey();
                final String cartNo = kitItems.get(0).getEquipmentNo();
                int qty = 0;
                for (KITItem kitItem : entry.getValue()) {
                    qty += Integer.parseInt(kitItem.getQuantity());
                }

                TableRow tr = new TableRow(this);
                tr.setLayoutParams(new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT));
                tr.setClickable(true);
                tr.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(CheckInventoryActivity.this, VerifyDrawerActivity.class);
                        Bundle args = new Bundle();
                        args.putSerializable("kitItems", (Serializable) kitItems);
                        intent.putExtra("BUNDLE", args);
                        intent.putExtra("drawerName", drawerName);
                        intent.putExtra("parent", parent);
                        startActivity(intent);
                    }
                });
                TextView drawer = new TextView(this);
                drawer.setText(entry.getKey());
                drawer.setTextSize(20);
                drawer.setLayoutParams(cellParams1);
                drawer.setGravity(Gravity.CENTER);
                drawer.setPadding(0, 10, 0, 0);
                tr.addView(drawer);

                TextView qtyView = new TextView(this);
                qtyView.setText(String.valueOf(qty));
                qtyView.setTextSize(20);
                qtyView.setLayoutParams(cellParams2);
                qtyView.setGravity(Gravity.CENTER);
                tr.addView(qtyView);

                if(!parent.equals("VerifyFlightByAdminActivity")) {
                    TextView isValidated = new TextView(this);
                    String validatedText = handler.isDrawerValidated(cartNo, drawerName) ? "OK" : "NV";
                    isValidated.setText(validatedText);
                    isValidated.setTextSize(20);
                    isValidated.setLayoutParams(cellParams2);
                    isValidated.setGravity(Gravity.CENTER);
                    tr.addView(isValidated);
                }
                checkInventoryTable.addView(tr);
            }
       // }
    }

    /*@Override
    public void onBackPressed()
    {
        if("AttCheckInfo".equals(parent)) {
            Intent intent = new Intent(this, AttCheckInfo.class);
            startActivity(intent);
        }
        else if("CloseFlightActivity".equals(parent)) {
            Intent intent = new Intent(this, CloseFlightActivity.class);
            startActivity(intent);
        }
        else{
            super.onBackPressed();
        }
    }*/
}
