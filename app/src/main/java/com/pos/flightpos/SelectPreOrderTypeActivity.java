package com.pos.flightpos;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.pos.flightpos.objects.XMLMapper.PreOrder;
import com.pos.flightpos.utils.POSDBHandler;

import java.util.List;
import java.util.Map;

public class SelectPreOrderTypeActivity extends AppCompatActivity {
    //String serviceType;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_pre_order_type);
        LinearLayout deliverLayout = findViewById(R.id.deliverPreOrderLayout);
        LinearLayout acceptOrderLayout = findViewById(R.id.acceptPreOrderLayout);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        final Map<String,List<PreOrder>> preOrders = getPreOrderList();
        if(preOrders == null || preOrders.isEmpty()) {
            //deliverLayout.setBackground(getResources().getDrawable(R.drawable.deliver_pre_order_icon_grey));
            deliverLayout.getChildAt(0).setBackgroundResource(R.drawable.deliver_pre_order_icon_grey);
        }

        deliverLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(preOrders != null && !preOrders.isEmpty()) {
                    Intent intent = new Intent(SelectPreOrderTypeActivity.this, PreOrderDeliveryActivity.class);
                    //intent.putExtra("serviceType", serviceType);
                    startActivity(intent);
                }
                else{
                    Toast.makeText(SelectPreOrderTypeActivity.this, "No pre order items available for this flight", Toast.LENGTH_SHORT).show();
                }
            }
        });
        acceptOrderLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SelectPreOrderTypeActivity.this, SelectServiceTypeForPreOrdersActivity.class);
                startActivity(intent);
            }
        });
        ImageButton backButton = findViewById(R.id.backPressBtn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private Map<String,List<PreOrder>> getPreOrderList(){
        POSDBHandler handler = new POSDBHandler(this);
        return handler.getAvailablePreOrders("faUser");
    }
}
