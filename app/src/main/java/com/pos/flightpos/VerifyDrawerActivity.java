package com.pos.flightpos;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.pos.flightpos.objects.Constants;
import com.pos.flightpos.objects.XMLMapper.KITItem;
import com.pos.flightpos.utils.POSCommonUtils;
import com.pos.flightpos.utils.POSDBHandler;
import com.pos.flightpos.utils.SaveSharedPreference;

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
    String equipmentNo = "";
    String parent;
    String rowIndex;
    POSDBHandler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_drawer);
        verifyDrawerTable =  findViewById(R.id.verifyDrawerTable);
        handler = new POSDBHandler(this);
        Intent intent = getIntent();
        Bundle args = intent.getBundleExtra("BUNDLE");
        drawerItems = (ArrayList<KITItem>) args.getSerializable("kitItems");
        drawerName = intent.getExtras().get("drawerName").toString();
        parent = intent.getExtras().get("parent").toString();
        rowIndex = intent.getExtras().get("rowIndex").toString();
        TextView drawerNameText = (TextView) findViewById(R.id.drawerNameText);
        drawerNameText.setText("Verify " +drawerName);
        Button verifyDrawerBtn =  findViewById(R.id.verifyDrawerBtn);
        verifyDrawerBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    verifyDrawer();
                }
            });
        updatedMap = new HashMap<>();
        equipmentNo = drawerItems.get(0).getEquipmentNo();
        showDataInTable(drawerItems);
        ImageButton backButton = findViewById(R.id.backPressBtn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        Button addRemarkBtn = findViewById(R.id.addInventoryRemark);
        addRemarkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addRemark();
            }
        });
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
    }

    private void addRemark(){
        EditText remarkText = findViewById(R.id.verifyDrawerRemark);
        if(remarkText.getText() == null || remarkText.getText().toString().isEmpty()){
            Toast.makeText(getApplicationContext(), "Please add a remark",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        String userName = (parent.equals("AttCheckInfo") || parent.equals("CloseFlightActivity")) ?
                SaveSharedPreference.getStringValues(this,Constants.SHARED_PREFERENCE_FA_NAME) :
                SaveSharedPreference.getStringValues(this,Constants.SHARED_PREFERENCE_ADMIN_USER);
        String comment = remarkText.getText().toString();
        handler.insertUserComments(userName,"Verify drawer",comment);
        Toast.makeText(getApplicationContext(), "Remark added successfully",
                Toast.LENGTH_SHORT).show();
    }

    private void verifyDrawer(){
        if(isValueUpdated && updatedMap != null && !updatedMap.isEmpty()){
            for(Map.Entry<String,String> entry : updatedMap.entrySet()) {
                KITItem kitItem = new KITItem();
                kitItem.setQuantity(entry.getValue());
                String[] strings = entry.getKey().split("-#");
                kitItem.setItemNo(strings[0]);
                kitItem.setEquipmentNo(strings[1]);
                kitItem.setDrawer(drawerName);
                handler.updateItemCountOfKITItems(kitItem);
            }
        }
        String userMode  = POSCommonUtils.getDrawerValidationMode(parent);
        handler.updateDrawerValidation(equipmentNo,drawerName,"YES",userMode);
        Toast.makeText(getApplicationContext(), "Validated successful",
                Toast.LENGTH_SHORT).show();
    }

    private void showDataInTable(List<KITItem> kitItems){

        TableRow.LayoutParams cellParams1 = new TableRow.LayoutParams(0,
                TableRow.LayoutParams.WRAP_CONTENT, 2f);
        TableRow.LayoutParams cellParams2 = new TableRow.LayoutParams(0,
                TableRow.LayoutParams.WRAP_CONTENT, 3f);
        TableRow.LayoutParams cellParams3 = new TableRow.LayoutParams(0,
                TableRow.LayoutParams.WRAP_CONTENT, 2f);
        cellParams3.setMargins(5,0,5,0);

        for(KITItem item : kitItems){
            TableRow tr = new TableRow(this);
            TableRow.LayoutParams rowParam = new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT);

            tr.setPadding(0,15,0,15);
            tr.setLayoutParams(rowParam);
            tr.setBackground(ContextCompat.getDrawable(this, R.color.white));

            final String itemNoStr = item.getItemNo();
            final String equipmentNo = item.getEquipmentNo();
            TextView itemNO = new TextView(this);
            itemNO.setText(itemNoStr);
            itemNO.setTextSize(16);
            itemNO.setLayoutParams(cellParams1);
            itemNO.setPadding(0,10,0,0);
            itemNO.setGravity(Gravity.CENTER);
            tr.addView(itemNO);

            View view  = new View(this);
            view.setLayoutParams(new TableRow.LayoutParams(3, TableRow.LayoutParams.MATCH_PARENT));
            view.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
            tr.addView(view);

            TextView itemDesc = new TextView(this);
            itemDesc.setText(String.valueOf(item.getItemDescription()));
            itemDesc.setTextSize(16);
            itemDesc.setLayoutParams(cellParams2);
            itemDesc.setGravity(Gravity.CENTER);
            tr.addView(itemDesc);

            View view1  = new View(this);
            view1.setLayoutParams(new TableRow.LayoutParams(3, TableRow.LayoutParams.MATCH_PARENT));
            view1.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
            tr.addView(view1);

                EditText qtyTextBox = new EditText(this);
                qtyTextBox.setText(item.getQuantity());
                qtyTextBox.setTextSize(16);
                qtyTextBox.setLayoutParams(cellParams3);
                qtyTextBox.setPadding(20, 0, 20, 0);
                qtyTextBox.setInputType(InputType.TYPE_CLASS_NUMBER);
            qtyTextBox.setBackground(ContextCompat.getDrawable(this, R.drawable.textinputborderlight));
                qtyTextBox.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        updatedMap.put(itemNoStr + "-#" + equipmentNo, charSequence.toString());
                        isValueUpdated = true;
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                    }
                });
                tr.addView(qtyTextBox);
            TableRow tr1 = new TableRow(this);
            tr1.setPadding(0,15,0,15);
            tr1.setLayoutParams(rowParam);
            verifyDrawerTable.addView(tr);
            verifyDrawerTable.addView(tr1);
        }
    }

    private String getTotalCount(){
        int childCount = verifyDrawerTable.getChildCount();
        int total = 0;
        for(int i=1;i<childCount;i++){
            TableRow row = (TableRow)verifyDrawerTable.getChildAt(i);
            EditText text = (EditText)row.getChildAt(4);
            if(text != null && text.getText() != null && !text.getText().toString().equals("")){
                total += Integer.parseInt(text.getText().toString());
            }
            i++;
        }
        return total+"";
    }

    @Override
    public void onBackPressed() {
        if(isValueUpdated) {
            String retStr = "";
            for(Map.Entry<String,String> entry : updatedMap.entrySet()){
                retStr += entry.getKey()+"-#" + entry.getValue()+",";
            }
            retStr.substring(0,retStr.length()-2);
            Intent resultIntent = new Intent();
            resultIntent.putExtra("drawerId", drawerName);
            resultIntent.putExtra("returnStr", retStr);
            resultIntent.putExtra("rowIndex",rowIndex);
            resultIntent.putExtra("itemTotal",getTotalCount());
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        }
        super.onBackPressed();
    }
}
