package com.pos.swoop;

import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.pos.swoop.utils.POSCommonUtils;
import com.pos.swoop.utils.POSDBHandler;

import java.util.ArrayList;
import java.util.List;

public class ClosingSealsActivity extends AppCompatActivity {

    String noOfSeals;
    LinearLayout closeSealLayout;
    Spinner spinner;
    POSDBHandler handler;
    String serviceType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_closing_seals);
        handler = new POSDBHandler(this);
        serviceType = getIntent().getExtras().getString("serviceType");
        List<String> kitCodes = POSCommonUtils.getServiceTypeKitCodeMap(this).get(serviceType);
        noOfSeals = handler.getNoOfSealsFromKitCodes(kitCodes);
        closeSealLayout = (LinearLayout) findViewById(R.id.layout_addSeal);
        spinner = findViewById(R.id.spinner1);
        populateAvailableSeals(spinner);
        addSealSpinner();
        ImageButton backButton = findViewById(R.id.backPressBtn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        TextView serviceTypeView = findViewById(R.id.sealServiceTypeId);
        serviceTypeView.setText("Closing Seals - "+POSCommonUtils.getServiceTypeFromServiceType(serviceType));
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
    }

    private void addSealSpinner(){
        int sealCount = Integer.parseInt(noOfSeals);
        LinearLayout.LayoutParams mRparams = new LinearLayout.LayoutParams
                (RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        for(int i=1 ; i< sealCount;i++){
            Spinner newSpinner = new Spinner(this);
            newSpinner.setLayoutParams(mRparams);
            newSpinner.setPadding(0,0,30,0);
            closeSealLayout.addView(newSpinner,0);
            populateAvailableSeals(newSpinner);
        }
    }

    private void populateAvailableSeals(Spinner sealSpinner){
        String seals = handler.getSealList(serviceType,"inbound");
        if(seals != null) {
            String[] storedSealsArray = seals.split(",");
            ArrayList<String> options = new ArrayList<String>();
            options.add("");
            for (int i = 0; i < storedSealsArray.length; i++) {
                options.add(storedSealsArray[i]);
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, options);
            adapter.setDropDownViewResource(R.layout.spinner_item);
            sealSpinner.setAdapter(adapter);
        }
    }

    public void addSeal(View view) {
        List<String> sealList = getSealListFromLayout(closeSealLayout);
        handler.deleteOutboundSeals(serviceType);
        if(sealList != null) {
            for(String seal : sealList) {
                handler.updateSealTable(seal,"sealType","outbound");
            }
        }
            Toast.makeText(getApplicationContext(), "Closing seals added.",
                    Toast.LENGTH_SHORT).show();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    onBackPressed();
                }
            }, 1000);
    }

    private List<String> getSealListFromLayout(LinearLayout layout){
        int childCount = layout.getChildCount();
        List<String> sealList = new ArrayList<>();
        for (int i = 0; i < childCount - 1; i++) {
            Spinner editText = (Spinner) layout.getChildAt(i);
            String textVal = editText.getSelectedItem() == null ? null : editText.getSelectedItem().toString();
            if (textVal != null && !textVal.isEmpty()) {
                sealList.add(textVal);
            }

        }
        return sealList;
    }
}
