package com.pos.swoop;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.pos.swoop.objects.Constants;
import com.pos.swoop.utils.POSCommonUtils;
import com.pos.swoop.utils.SaveSharedPreference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UserDetailsActivity extends AppCompatActivity {

    ImageButton scanBoardingPassBtn;
    EditText passengerName;
    EditText pnrNo;
    EditText seatNo;
    EditText emailId;
    Button submitBtn;
    String category;
    boolean isEmailSuggested = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);
        scanBoardingPassBtn = findViewById(R.id.scanBoardingPass);
        scanBoardingPassBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scan();
            }
        });
        passengerName = findViewById(R.id.passengerNameFld);
        pnrNo = findViewById(R.id.pnrNumberFld);
        seatNo = findViewById(R.id.seatNoFld);
        emailId = findViewById(R.id.emailFld);
        submitBtn = (Button) findViewById(R.id.submitBtn);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClickAnimation);
                clickSubmitBtn();
            }
        });
        category = getIntent().getStringExtra("category");
        TextView headerText = findViewById(R.id.headerId);
        headerText.setText("Passenger Details - " + category.substring(0, 1).toUpperCase() + category.substring(1));
        ImageButton backButton = findViewById(R.id.backPressBtn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        suggestEmailAddress();
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
    }

    private void clickSubmitBtn(){
        if(passengerName.getText() != null && !passengerName.getText().equals("") &&
                pnrNo.getText() != null && pnrNo.getText().toString() != null
                 && seatNo.getText() != null && !seatNo.getText().toString().equals("")) {
            String email;
            if(emailId.getText().toString() == null || emailId.getText().toString().isEmpty())  email = "empty";
            else email = emailId.getText().toString();

            String userDetailsStr = passengerName.getText().toString() + "==" + pnrNo.getText().toString() +"==" +
                    seatNo.getText().toString() +"=="+email;
            SaveSharedPreference.setStringValues(this,Constants.SHARED_PREFERENCE_USER_DETAILS,userDetailsStr);
            if(category.equalsIgnoreCase("compensation")){
                Intent intent = new Intent(this, ConpensationSelectionActivity.class);
                startActivity(intent);
            }
            else {
                Intent intent = new Intent(this, GateItemSelectionActivity.class);
                intent.putExtra("category",category);
                startActivity(intent);
            }


        }
        else{
            Toast.makeText(getApplicationContext(), "Please fill required details.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void suggestEmailAddress(){
        emailId.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if(s != null && s.toString().contains("@")) {
                    String[] str = s.toString().split("@");
                    if(str.length > 1 && str[1].length() == 1 && !isEmailSuggested){
                        if(str[1].equalsIgnoreCase("g")){
                            emailId.setText(s.toString()+"mail.com");
                            isEmailSuggested = true;
                        }
                        else if(str[1].equalsIgnoreCase("y")){
                            emailId.setText(s.toString()+"ahoo.com");
                            isEmailSuggested = true;
                        }
                        else if(str[1].equalsIgnoreCase("o")){
                            emailId.setText(s.toString()+"utlook.com");
                            isEmailSuggested = true;
                        }
                    }
                }

            }
            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void populateSeatNumberFromBoardingPass(final Map<String,String> qrCodeDetails){
        if(qrCodeDetails != null) {
            String fileNames = "";
            for(Map.Entry entry : qrCodeDetails.entrySet()){
                fileNames += entry.getKey() + " - " + entry.getValue() + "\n";
            }
            new AlertDialog.Builder(UserDetailsActivity.this)
                    .setTitle("QR Code details")
                    .setMessage(fileNames)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            passengerName.setText(qrCodeDetails.get("Name"));
                            pnrNo.setText(qrCodeDetails.get("PNR"));
                            seatNo.setText(qrCodeDetails.get("Seat No"));
                        }})
                    .setNegativeButton(android.R.string.cancel, null).show();
        }
    }

    private void scan(){
        Intent intent = new Intent();
        intent.setAction("com.summi.scan");
        intent.setPackage("com.sunmi.sunmiqrcodescanner");
        intent.putExtra("IS_SHOW_SETTING", false);      // whether to display the setting button, default true
        intent.putExtra("IDENTIFY_MORE_CODE", true);    // identify multiple qr code in the screen
        intent.putExtra("IS_AZTEC_ENABLE", true);       // allow read of AZTEC code
        intent.putExtra("IS_PDF417_ENABLE", true);      // allow read of PDF417 code
        intent.putExtra("IS_DATA_MATRIX_ENABLE", true); // allow read of DataMatrix code
        PackageManager packageManager = getPackageManager();
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, 100);
        } else {
            Toast.makeText(this,"Error",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100 && data != null) {
            Bundle bundle = data.getExtras();
            ArrayList<HashMap<String, String>> result = (ArrayList< HashMap<String, String> >) bundle.getSerializable("data");
            if (result != null && result.size() > 0) {
                String value = result.get(0).get("VALUE");
                populateSeatNumberFromBoardingPass(POSCommonUtils.readBarcodeDetails(value,this));
            } else {
                Toast.makeText(this,"Scan Failed",Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
