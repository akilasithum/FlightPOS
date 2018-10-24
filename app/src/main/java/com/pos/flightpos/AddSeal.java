package com.pos.flightpos;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.pos.flightpos.objects.Constants;
import com.pos.flightpos.utils.POSDBHandler;
import com.pos.flightpos.utils.SaveSharedPreference;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AddSeal extends AppCompatActivity {

    String parent;
    String noOfSeals;
    LinearLayout outbonundLayout;
    LinearLayout inboundLayout;
    Button addSealBtn;
    POSDBHandler handler;
    String flightMode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_seal);
        parent = getIntent().getExtras().getString("parent");
        noOfSeals = SaveSharedPreference.getStringValues(this,Constants.SHARED_PREFERENCE_NO_OF_SEAL);
        outbonundLayout = (LinearLayout) findViewById(R.id.layout_addSeal);
        inboundLayout = findViewById(R.id.layout_addInboundSeal);
        addSealBtn = (Button) findViewById(R.id.bt_addSeal);
        handler = new POSDBHandler(this);
        flightMode = SaveSharedPreference.getStringValues(this,Constants.SHARED_PREFERENCE_FLIGHT_MODE);
        if("faUser".equals(flightMode)){
            inboundLayout.setVisibility(View.GONE);
        }
        else{
            findViewById(R.id.layout_addRemark).setVisibility(View.GONE);
        }
        addSealTextBoxes();
        if(flightMode.equals("faUser")){
            addSealBtn.setText("Verify Seals");
            String flightType = SaveSharedPreference.getStringValues(this,
                    Constants.SHARED_PREFERENCE_FLIGHT_TYPE);
            if("CloseFlightActivity".equals(parent)){
                flightType = "inBound";
            }
            String storedSeals = SaveSharedPreference.getStringValues(this,flightType+"SealList");
            String[] storedSealsArray = storedSeals.split(",");
            for(int i = 0; i<storedSealsArray.length ; i++){
                EditText editText = (EditText) outbonundLayout.getChildAt(i);
                editText.setText(storedSealsArray[i]);
            }
        }
        else{
            addSealBtn.setText("Add Outbound seals");
        }
    }

    private void addSealTextBoxes(){
        int sealCount = Integer.parseInt(noOfSeals);
        LinearLayout.LayoutParams mRparams = new LinearLayout.LayoutParams
                (RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        for(int i=1 ; i< sealCount;i++){
            EditText myEditText = new EditText(this);
            myEditText.setLayoutParams(mRparams);
            myEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
            outbonundLayout.addView(myEditText,0);
            if(flightMode != null && flightMode.equals("admin")) {
                EditText inboundSealText = new EditText(this);
                inboundSealText.setLayoutParams(mRparams);
                inboundSealText.setInputType(InputType.TYPE_CLASS_NUMBER);
                inboundLayout.addView(inboundSealText, 0);
            }
        }
    }

    private List<String> getSealListFromLayout(LinearLayout layout){
        int childCount = outbonundLayout.getChildCount();
        List<String> sealList = new ArrayList<>();
        for (int i = 0; i < childCount - 1; i++) {
            EditText editText = (EditText) layout.getChildAt(i);
            String textVal = editText.getText() == null ? null : editText.getText().toString();
            if (textVal != null && !textVal.isEmpty()) {
                sealList.add(textVal);
            }

        }
        return sealList;
    }
    public void addSeal(View view) {
        List<String> sealList = getSealListFromLayout(outbonundLayout);
        if(flightMode.equals("faUser")){
            SaveSharedPreference.setStringValues(this, Constants.SHARED_PREFERENCE_IS_SEAL_VERIFIED,"yes");
            Toast.makeText(getApplicationContext(), "Seal numbers are verified.",
                    Toast.LENGTH_SHORT).show();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    onBackPressed();
                }
            }, 1000);
        }
        else {
            saveSealDetails("outBoundSealList",sealList);

        }
    }

    public void addInboundSeal(View view) {
        List<String> sealList = getSealListFromLayout(inboundLayout);
        saveSealDetails("inBoundSealList",sealList);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                onBackPressed();
            }
        }, 1000);
    }

    private void saveSealDetails(String storedName,List<String> sealList){
        String seals = TextUtils.join(",", sealList);
        SaveSharedPreference.setStringValues(this, storedName, seals);
        Toast.makeText(getApplicationContext(), "Successfully added " + sealList.size() + " seals.",
                Toast.LENGTH_SHORT).show();
        Date date = new Date();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentDateStr = df.format(date);
        String flightName = SaveSharedPreference.getStringValues(this,Constants.SHARED_PREFERENCE_FLIGHT_NAME);
        String flightDate = SaveSharedPreference.getStringValues(this,Constants.SHARED_PREFERENCE_FLIGHT_DATE);
        handler.insertSealData("outbound",String.valueOf(sealList.size()),seals,currentDateStr,flightName,flightDate);
    }
}
