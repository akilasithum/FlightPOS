package com.pos.flightpos;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.pos.flightpos.objects.XMLMapper.KITItem;
import com.pos.flightpos.utils.POSDBHandler;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VerifyDrawerActivity extends AppCompatActivity {

    TableLayout verifyDrawerTable;
    List<KITItem> drawerItems;
    boolean isValueUpdated = false;
    Map<String,String> updatedMap;
    String drawerName;
    String serviceType;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_drawer);
        verifyDrawerTable = (TableLayout) findViewById(R.id.verifyDrawerTable);
        Intent intent = getIntent();
        Bundle args = intent.getBundleExtra("BUNDLE");
        drawerItems = (ArrayList<KITItem>) args.getSerializable("kitItems");
        drawerName = intent.getExtras().get("drawerName").toString();
        serviceType = intent.getExtras().get("serviceType").toString();
        TextView drawerNameText = (TextView) findViewById(R.id.drawerNameText);
        drawerNameText.setText("Verify " +drawerName);
        LinearLayout verifyDrawerBtn = (LinearLayout) findViewById(R.id.verifyDrawerBtn);
        verifyDrawerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verifyDrawer();
            }
        });
        updatedMap = new HashMap<>();
        showDataInTable(drawerItems);
    }

    private void verifyDrawer(){
        if(isValueUpdated && updatedMap != null && !updatedMap.isEmpty()){

            for(Map.Entry<String,String> entry : updatedMap.entrySet()) {
                POSDBHandler handler = new POSDBHandler(this);
                KITItem kitItem = new KITItem();
                kitItem.setQuantity(entry.getValue());
                String[] strings = entry.getKey().split("-");
                kitItem.setItemNo(strings[0]);
                kitItem.setEquipmentNo(strings[1]);
                kitItem.setDrawer(drawerName);
                handler.updateItemCountOfKITItems(kitItem);
            }
        }
        else {
            super.onBackPressed();
        }
        Intent intent = new Intent(this,CheckInventoryActivity.class);
        intent.putExtra("ServiceType",serviceType);
        startActivity(intent);
    }

    private void showDataInTable(List<KITItem> kitItems){

        for(KITItem item : kitItems){
            TableRow tr = new TableRow(this);
            tr.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));

            TableRow.LayoutParams cellParams1 = new TableRow.LayoutParams(0,
                    TableRow.LayoutParams.WRAP_CONTENT, 2f);
            TableRow.LayoutParams cellParams2 = new TableRow.LayoutParams(0,
                    TableRow.LayoutParams.WRAP_CONTENT, 6f);
            TableRow.LayoutParams cellParams3 = new TableRow.LayoutParams(0,
                    TableRow.LayoutParams.WRAP_CONTENT, 3f);
            final String itemNoStr = item.getItemNo();
            final String equipmentNo = item.getEquipmentNo();
            TextView itemNO = new TextView(this);
            itemNO.setText(itemNoStr);
            itemNO.setTextSize(16);
            itemNO.setLayoutParams(cellParams1);
            itemNO.setPadding(0,10,0,0);
            tr.addView(itemNO);

            TextView itemDesc = new TextView(this);
            itemDesc.setText(String.valueOf(item.getItemDescription()));
            itemDesc.setTextSize(16);
            itemDesc.setLayoutParams(cellParams2);
            tr.addView(itemDesc);

            EditText qtyTextBox = new EditText(this);
            qtyTextBox.setText(item.getQuantity());
            qtyTextBox.setTextSize(16);
            qtyTextBox.setLayoutParams(cellParams3);
            qtyTextBox.setPadding(20,0,20,0);
            qtyTextBox.setInputType(InputType.TYPE_CLASS_NUMBER);
            qtyTextBox.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    updatedMap.put(itemNoStr + "-" +equipmentNo,charSequence.toString());
                    isValueUpdated = true;
                }
                @Override
                public void afterTextChanged(Editable editable) {
                }
            });
            tr.addView(qtyTextBox);
            verifyDrawerTable.addView(tr);
        }
    }
}
