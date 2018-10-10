package com.pos.flightpos;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.pos.flightpos.objects.Constants;
import com.pos.flightpos.objects.SoldItem;
import com.pos.flightpos.objects.XMLMapper.PreOrder;
import com.pos.flightpos.utils.POSDBHandler;
import com.pos.flightpos.utils.SaveSharedPreference;

import java.util.ArrayList;
import java.util.List;

public class PreOrderDeliveryActivity extends AppCompatActivity {

    POSDBHandler posdbHandler;
    List<PreOrder> preOrders;
    TableLayout preOrderTable;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_order_delivery);
        posdbHandler = new POSDBHandler(this);
        Bundle args = getIntent().getBundleExtra("BUNDLE");
        preOrders = (ArrayList<PreOrder>) args.getSerializable("preOrders");
        preOrderTable = (TableLayout) findViewById(R.id.preOrdersTable);
        showAvailablePreOrders();
    }

    private void showAvailablePreOrders(){

        int i = 0;
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
            customerDetails.setTextSize(20);
            customerDetails.setLayoutParams(cellParams1);
            customerDetails.setPadding(10,0,0,0);
            customerDetails.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(PreOrderDeliveryActivity.this);
                    builder1.setTitle("Pre order items");
                    builder1.setMessage("Customer Name  : "+preOrder.getCustomerName() +"\n" +
                            "Item Desc          : " + posdbHandler.getItemDescFromItemNo(preOrder.getItemId()) +"\n" +
                            "Item Category      : " +preOrder.getItemCategory() + "\n"+
                    "Quantity              : " + preOrder.getQuantity());
                    builder1.setPositiveButton(
                            "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alert11 = builder1.create();
                    alert11.show();
                }
            });
            tr.addView(customerDetails);

            CheckBox checkBox = new CheckBox(this);
            checkBox.setLayoutParams(cellParams2);
            checkBox.setGravity(Gravity.CENTER);
            checkBox.setChecked(!preOrder.getDelivered().equals("No"));
            checkBox.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                    if(isChecked){
                        posdbHandler.updatePreOrderDeliveryStatus("Yes",preOrder.getPNR(),preOrder.getItemId());
                    }
                    else{
                        posdbHandler.updatePreOrderDeliveryStatus("No",preOrder.getPNR(),preOrder.getItemId());
                    }
                }
            });
            tr.addView(checkBox);

            preOrderTable.addView(tr);
            i++;
        }
    }
}
