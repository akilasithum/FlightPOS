package com.pos.flightpos;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.pos.flightpos.objects.Constants;
import com.pos.flightpos.objects.XMLMapper.KITItem;
import com.pos.flightpos.utils.POSCommonUtils;
import com.pos.flightpos.utils.POSDBHandler;
import com.pos.flightpos.utils.SaveSharedPreference;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static android.text.Spanned.SPAN_INCLUSIVE_INCLUSIVE;

public class CheckInventoryActivity extends AppCompatActivity {

    TableLayout checkInventoryTable;
    String parent;
    String equipmentName;
    Map<String,List<KITItem>> cartItems;
    final int STATIC_INTEGER_VALUE = 100;
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
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
    }

    private void addTableHeader(TableRow.LayoutParams cellParams1,TableRow.LayoutParams cellParams2){
        TableRow tr = new TableRow(this);
        tr.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));
        TextView drawer = new TextView(this);
        drawer.setText("Drawer ID");
        drawer.setBackgroundColor(ContextCompat.getColor(this, R.color.lightAsh));
        drawer.setTextSize(18);
        drawer.setLayoutParams(cellParams1);
        drawer.setGravity(Gravity.CENTER);
        tr.addView(drawer);

        TextView quantity = new TextView(this);
        quantity.setText("Quantity");
        quantity.setBackgroundColor(ContextCompat.getColor(this, R.color.ash));
        quantity.setTextSize(18);
        quantity.setLayoutParams(cellParams2);
        quantity.setGravity(Gravity.CENTER);
        tr.addView(quantity);

        //if(!parent.equals("VerifyFlightByAdminActivity")) {
            TextView isValidated = new TextView(this);
            isValidated.setText("Validated");
            isValidated.setBackgroundColor(ContextCompat.getColor(this, R.color.lightAsh));
            isValidated.setTextSize(18);
            isValidated.setLayoutParams(cellParams2);
            isValidated.setGravity(Gravity.CENTER);
            tr.addView(isValidated);
        //}
        checkInventoryTable.addView(tr);

    }

    private void showDataInTable(){
        POSDBHandler handler = new POSDBHandler(this);

        TableRow.LayoutParams cellParams1 = new TableRow.LayoutParams(0,
                TableRow.LayoutParams.WRAP_CONTENT, 3f);
        TableRow.LayoutParams cellParams2 = new TableRow.LayoutParams(0,
                TableRow.LayoutParams.WRAP_CONTENT, 2f);

            TextView header = findViewById(R.id.headerId);
            header.setText("Equipment No : " + equipmentName);
            header.setTextSize(20);
            header.setLayoutParams(cellParams2);
            header.setPadding(0,0,0,20);

            Map<String,List<KITItem>> treeMap = new TreeMap<>(cartItems);
            int i = 0;
            for (Map.Entry<String, List<KITItem>> entry : treeMap.entrySet()) {

                final int rowIndex = i;
                i = i+2;
                final List<KITItem> kitItems = entry.getValue();
                final String drawerName = entry.getKey();
                final String cartNo = kitItems.get(0).getEquipmentNo();
                int qty = 0;
                for (KITItem kitItem : entry.getValue()) {
                    qty += Integer.parseInt(kitItem.getQuantity());
                }

                TableRow tr = new TableRow(this);
                tr.setBackground(ContextCompat.getDrawable(this, R.color.white));

                TableRow.LayoutParams rowParam = new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT);

                tr.setPadding(0,15,0,15);
                tr.setLayoutParams(rowParam);

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
                        intent.putExtra("rowIndex",rowIndex+"");
                        //startActivity(intent);
                        startActivityForResult(intent, STATIC_INTEGER_VALUE);
                    }
                });
                TextView drawer = new TextView(this);

                SpannableString span1 = new SpannableString(entry.getKey());
                span1.setSpan(new AbsoluteSizeSpan(35), 0, entry.getKey().length(), SPAN_INCLUSIVE_INCLUSIVE);
                SpannableString span2 = new SpannableString("\n Drawer ID");
                span2.setSpan(new AbsoluteSizeSpan(20), 0, "\n Drawer ID".length(), SPAN_INCLUSIVE_INCLUSIVE);
                CharSequence finalText = TextUtils.concat(span1, " ", span2);
                drawer.setText(finalText);
                drawer.setLayoutParams(cellParams1);
                drawer.setGravity(Gravity.CENTER);
                drawer.setPadding(0, 10, 0, 0);
                tr.addView(drawer);

                View view  = new View(this);
                view.setLayoutParams(new TableRow.LayoutParams(3, TableRow.LayoutParams.MATCH_PARENT));
                view.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                tr.addView(view);

                SpannableString span3 = new SpannableString(String.valueOf(qty));
                span3.setSpan(new AbsoluteSizeSpan(35), 0, String.valueOf(qty).length(), SPAN_INCLUSIVE_INCLUSIVE);
                SpannableString span4 = new SpannableString("\n Qty");
                span4.setSpan(new AbsoluteSizeSpan(20), 0, "\n Qty".length(), SPAN_INCLUSIVE_INCLUSIVE);
                CharSequence qtyFinalStr = TextUtils.concat(span3, " ", span4);

                TextView qtyView = new TextView(this);
                qtyView.setText(qtyFinalStr);
                qtyView.setTextSize(15);
                qtyView.setLayoutParams(cellParams2);
                qtyView.setGravity(Gravity.CENTER);
                tr.addView(qtyView);

                View view1  = new View(this);
                view1.setLayoutParams(new TableRow.LayoutParams(3, TableRow.LayoutParams.MATCH_PARENT));
                view1.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                tr.addView(view1);


                String userMode  = POSCommonUtils.getDrawerValidationMode(parent);
                String validatedText = handler.isDrawerValidated(cartNo, drawerName,userMode) ? "OK" : "Not";
                SpannableString span5 = new SpannableString(validatedText);
                span5.setSpan(new AbsoluteSizeSpan(35), 0, validatedText.length(), SPAN_INCLUSIVE_INCLUSIVE);
                SpannableString span6 = new SpannableString("\n Is Validated");
                span6.setSpan(new AbsoluteSizeSpan(20), 0, "\n Is Validated".length(), SPAN_INCLUSIVE_INCLUSIVE);
                CharSequence isValidatedStr = TextUtils.concat(span5, " ", span6);

                    TextView isValidated = new TextView(this);

                    isValidated.setText(isValidatedStr);
                    isValidated.setTextSize(15);
                    isValidated.setLayoutParams(cellParams2);
                    isValidated.setGravity(Gravity.CENTER);
                    tr.addView(isValidated);
                checkInventoryTable.addView(tr);
                TableRow tr1 = new TableRow(this);
                tr1.setPadding(0,15,0,15);
                tr1.setLayoutParams(rowParam);
                checkInventoryTable.addView(tr1);
            }
       // }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (STATIC_INTEGER_VALUE) : {
                if (resultCode == Activity.RESULT_OK) {
                    String drawerId = data.getStringExtra("drawerId");
                    String resultStr = data.getStringExtra("returnStr");
                    String rowIndex = data.getStringExtra("rowIndex");
                    String total = data.getStringExtra("itemTotal");
                    updateCartWithUpdatedDetails(rowIndex,total);
                }
                break;
            }
        }
    }

    private void updateCartWithUpdatedDetails(String rowIndex,String total){
        TableRow row = (TableRow)checkInventoryTable.getChildAt(Integer.valueOf(rowIndex));
        TextView textView = (TextView)row.getChildAt(2);

        SpannableString span3 = new SpannableString(String.valueOf(total));
        span3.setSpan(new AbsoluteSizeSpan(35), 0, String.valueOf(total).length(), SPAN_INCLUSIVE_INCLUSIVE);
        SpannableString span4 = new SpannableString("\n Qty");
        span4.setSpan(new AbsoluteSizeSpan(20), 0, "\n Qty".length(), SPAN_INCLUSIVE_INCLUSIVE);
        CharSequence qtyFinalStr = TextUtils.concat(span3, " ", span4);
        textView.setText(qtyFinalStr);

        TextView isValidatedText = (TextView)row.getChildAt(4);

        SpannableString span5 = new SpannableString("OK");
        span5.setSpan(new AbsoluteSizeSpan(35), 0, "OK".length(), SPAN_INCLUSIVE_INCLUSIVE);
        SpannableString span6 = new SpannableString("\n Is Validated");
        span6.setSpan(new AbsoluteSizeSpan(20), 0, "\n Is Validated".length(), SPAN_INCLUSIVE_INCLUSIVE);
        CharSequence isValidatedStr = TextUtils.concat(span5, " ", span6);

        isValidatedText.setText(isValidatedStr);
    }

    private Map<String,KITItem> getKitCodeMap(String result){

        String[] updateItems = result.split(",");
        Map<String,KITItem> kitItemMap = new HashMap<>();
        for(int i=0;i<updateItems.length;i++){
            String[] kitItemArr = updateItems[i].split("-#");
            KITItem kitItem = new KITItem();
            kitItem.setItemNo(kitItemArr[0]);
            kitItem.setEquipmentNo(kitItemArr[1]);
            kitItem.setQuantity(kitItemArr[2]);
            kitItemMap.put(kitItemArr[0],kitItem);
        }
        return kitItemMap;
    }
}
