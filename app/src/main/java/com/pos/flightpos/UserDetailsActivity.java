package com.pos.flightpos;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.pos.flightpos.objects.Constants;
import com.pos.flightpos.utils.POSCommonUtils;
import com.pos.flightpos.utils.SaveSharedPreference;

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
                populateSeatNumberFromBoardingPass();
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

    private void populateSeatNumberFromBoardingPass(){
        final Map<String,String> qrCodeDetails = POSCommonUtils.scanQRCode(this);
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
}
