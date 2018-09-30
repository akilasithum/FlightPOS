package com.pos.flightpos;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.pos.flightpos.objects.XMLMapper.KITItem;
import com.pos.flightpos.utils.POSDBHandler;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class CheckInventoryActivity extends AppCompatActivity {

    TableLayout checkInventoryTable;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_inventory);
        checkInventoryTable = (TableLayout) findViewById(R.id.checkInventoryTable);
        Intent intent = getIntent();
        String serviceType = intent.getExtras().get("ServiceType").toString();
        showDataInTable(serviceType);

    }

    private void showDataInTable(final String serviceType){
        POSDBHandler handler = new POSDBHandler(this);
        Map<String,List<KITItem>> drawerKitItemMap = handler.getDrawerKitItemMapFromServiceType(serviceType);

        for(Map.Entry<String, List<KITItem>> entry : drawerKitItemMap.entrySet()){
            final List<KITItem> kitItems = entry.getValue();
            final String drawerName = entry.getKey();
            final String cartNo = kitItems.get(0).getEquipmentNo();
            int qty = 0;
            for(KITItem kitItem : entry.getValue()){
                qty += Integer.parseInt(kitItem.getQuantity());
            }

            TableRow tr = new TableRow(this);
            tr.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));
            tr.setClickable(true);
            tr.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Intent intent = new Intent(CheckInventoryActivity.this, VerifyDrawerActivity.class);
                    Bundle args = new Bundle();
                    args.putSerializable("kitItems",(Serializable)kitItems);
                    intent.putExtra("BUNDLE",args);
                    intent.putExtra("drawerName",drawerName);
                    intent.putExtra("serviceType",serviceType);
                    startActivity(intent);
                }
            });
            TableRow.LayoutParams cellParams1 = new TableRow.LayoutParams(0,
                    TableRow.LayoutParams.WRAP_CONTENT, 3f);
            TableRow.LayoutParams cellParams2 = new TableRow.LayoutParams(0,
                    TableRow.LayoutParams.WRAP_CONTENT, 2f);
            TextView drawer = new TextView(this);
            drawer.setText(entry.getKey());
            drawer.setTextSize(20);
            drawer.setLayoutParams(cellParams1);
            drawer.setGravity(Gravity.CENTER);
            drawer.setPadding(0,10,0,0);
            tr.addView(drawer);

            TextView qtyView = new TextView(this);
            qtyView.setText(String.valueOf(qty));
            qtyView.setTextSize(20);
            qtyView.setLayoutParams(cellParams2);
            qtyView.setGravity(Gravity.CENTER);
            tr.addView(qtyView);

            TextView isValidated = new TextView(this);
            String validatedText = handler.isDrawerValidated(cartNo,drawerName) ? "OK" : "NV";
            isValidated.setText(validatedText);
            isValidated.setTextSize(20);
            isValidated.setLayoutParams(cellParams2);
            isValidated.setGravity(Gravity.CENTER);
            tr.addView(isValidated);
            checkInventoryTable.addView(tr);
        }
    }

}
