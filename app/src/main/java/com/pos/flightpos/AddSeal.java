package com.pos.flightpos;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
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
    LinearLayout verifySealsLayout;
    Button addSealBtn;
    POSDBHandler handler;
    String flightMode;
    Button addAnotherSeal;
    int inboundSealCount = 1;
    boolean outBoundSealsAdded = false;
    boolean inBoundSealsAdded = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_seal);
        parent = getIntent().getExtras().getString("parent");
        noOfSeals = SaveSharedPreference.getStringValues(this,Constants.SHARED_PREFERENCE_NO_OF_SEAL);
        outbonundLayout = (LinearLayout) findViewById(R.id.layout_addSeal);
        inboundLayout = findViewById(R.id.layout_addInboundSeal);
        verifySealsLayout = findViewById(R.id.layout_verifySeals);
        addSealBtn = (Button) findViewById(R.id.bt_addSeal);
        handler = new POSDBHandler(this);
        flightMode = SaveSharedPreference.getStringValues(this,Constants.SHARED_PREFERENCE_FLIGHT_MODE);

        if("faUser".equals(flightMode)){
            inboundLayout.setVisibility(View.GONE);
            outbonundLayout.setVisibility(View.GONE);
            addVerifySealTextBoxes();
            String storedSeals = SaveSharedPreference.getStringValues(this,"outBoundSealList");
            String[] storedSealsArray = storedSeals.split(",");
            for(int i = 0; i<storedSealsArray.length ; i++){
                LinearLayout layout = (LinearLayout)verifySealsLayout.getChildAt(i);
                EditText editText = (EditText) layout.getChildAt(0);
                editText.setText(storedSealsArray[i]);
            }
        }
        else{
            verifySealsLayout.setVisibility(View.GONE);
            addAnotherSeal = findViewById(R.id.bt_addAnotherSeal);
            addAnotherSeal.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    addAdditionalSealsByAdmin();
                }
            });
            addSealTextBoxes();
            findViewById(R.id.layout_addRemark).setVisibility(View.GONE);
            EditText firstSeal = findViewById(R.id.editText);
            firstSeal.addTextChangedListener(new TextWatcher() {

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }
                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {

                }
                @Override
                public void afterTextChanged(Editable editable) {
                    updateSealSequence(editable.toString());
                }
            });
        }

        ImageButton backButton = findViewById(R.id.backPressBtn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        Button addRemarkBtn = findViewById(R.id.addSealRemark);
        addRemarkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addRemark();
            }
        });
    }

    private void updateSealSequence(String firstSeal){
        if(firstSeal != null && !firstSeal.isEmpty()) {
            Integer firstSealNo = Integer.valueOf(firstSeal);
            int childCount = outbonundLayout.getChildCount();
            for (int i = 1; i < childCount - 1; i++) {
                EditText editText = (EditText) outbonundLayout.getChildAt(i);
                editText.setText(firstSealNo + i + "");
            }
        }
    }

    private void addRemark(){
        EditText remarkText = findViewById(R.id.addSealRemarkText);
        if(remarkText.getText() == null || remarkText.getText().toString().isEmpty()){
            Toast.makeText(getApplicationContext(), "Please add a remark",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        String userName = SaveSharedPreference.getStringValues(this,Constants.SHARED_PREFERENCE_FA_NAME);
        String comment = remarkText.getText().toString();
        handler.insertUserComments(userName,"Add Seals",comment);
        Toast.makeText(getApplicationContext(), "Remark added successfully",
                Toast.LENGTH_SHORT).show();
    }

    private void addAdditionalSealsByAdmin(){
        LinearLayout.LayoutParams mRparams = new LinearLayout.LayoutParams
                (RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        EditText inboundSealText = new EditText(this);
        inboundSealText.setLayoutParams(mRparams);
        inboundSealText.setInputType(InputType.TYPE_CLASS_NUMBER);
        EditText previousText = (EditText)inboundLayout.getChildAt(inboundSealCount-1);
        if(previousText.getText() != null && !previousText.getText().toString().isEmpty()){
            inboundSealText.setText(Integer.parseInt(previousText.getText().toString())+1+"");
        }
        inboundLayout.addView(inboundSealText, inboundSealCount);
        inboundSealCount++;
    }

    private void addSealTextBoxes(){
        int sealCount = Integer.parseInt(noOfSeals);
        LinearLayout.LayoutParams mRparams = new LinearLayout.LayoutParams
                (RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        for(int i=1 ; i< sealCount;i++){
            EditText myEditText = new EditText(this);
            myEditText.setLayoutParams(mRparams);
            myEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
            outbonundLayout.addView(myEditText,i);
        }
    }

    private void addVerifySealTextBoxes(){
        int sealCount = Integer.parseInt(noOfSeals);
        LinearLayout.LayoutParams mRparams = new LinearLayout.LayoutParams
                (RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams mRparams1 = new LinearLayout.LayoutParams
                (0, RelativeLayout.LayoutParams.WRAP_CONTENT,10);
        LinearLayout.LayoutParams mRparams2 = new LinearLayout.LayoutParams
                (0, RelativeLayout.LayoutParams.WRAP_CONTENT,1);
        for(int i=1 ; i< sealCount;i++){
            LinearLayout layout = new LinearLayout(this);
            layout.setLayoutParams(mRparams);
            layout.setOrientation(LinearLayout.HORIZONTAL);

            EditText myEditText = new EditText(this);
            myEditText.setLayoutParams(mRparams1);
            myEditText.setInputType(InputType.TYPE_CLASS_NUMBER);

            CheckBox checkBox = new CheckBox(this);
            checkBox.setLayoutParams(mRparams2);
            layout.addView(myEditText);
            layout.addView(checkBox);
            verifySealsLayout.addView(layout,i);
        }
    }

    private List<String> getSealListFromLayout(LinearLayout layout,int minusChildCount){
        int childCount = layout.getChildCount();
        List<String> sealList = new ArrayList<>();
        for (int i = 0; i < childCount - minusChildCount; i++) {
            EditText editText = (EditText) layout.getChildAt(i);
            String textVal = editText.getText() == null ? null : editText.getText().toString();
            if (textVal != null && !textVal.isEmpty()) {
                sealList.add(textVal);
            }

        }
        return sealList;
    }
    public void addSeal(View view) {
        List<String> sealList = getSealListFromLayout(outbonundLayout,1);
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
        List<String> sealList = getSealListFromLayout(inboundLayout,2);
        saveSealDetails("adminAdditionalSealList",sealList);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                onBackPressed();
            }
        }, 1000);
    }

    private void saveSealDetails(String storedName,List<String> sealList){
        if(sealList != null && !sealList.isEmpty()) {
            String seals = TextUtils.join(",", sealList);
            SaveSharedPreference.setStringValues(this, storedName, seals);
            Toast.makeText(getApplicationContext(), "Successfully added " + sealList.size() + " seals.",
                    Toast.LENGTH_SHORT).show();
            Date date = new Date();
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentDateStr = df.format(date);
            String flightName = SaveSharedPreference.getStringValues(this, Constants.SHARED_PREFERENCE_FLIGHT_NAME);
            String flightDate = SaveSharedPreference.getStringValues(this, Constants.SHARED_PREFERENCE_FLIGHT_DATE);
            handler.insertSealData("outbound", String.valueOf(sealList.size()), seals, currentDateStr, flightName, flightDate);
            if(storedName.equals("outBoundSealList")) outBoundSealsAdded = true;
            else inBoundSealsAdded = true;
        }
        else{
            Toast.makeText(getApplicationContext(), "No seals added. Enter seals in the text boxes",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed(){

        if((!inBoundSealsAdded || !outBoundSealsAdded) && !"faUser".equals(flightMode)){
            new AlertDialog.Builder(this)
                    .setTitle("Seals missing")
                    .setMessage("Missing inbound or outbonud seals. Do you wish to continue?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            AddSeal.super.onBackPressed();
                        }})
                    .setNegativeButton(android.R.string.no, null).show();
        }
        else{
            super.onBackPressed();
        }
    }
}
