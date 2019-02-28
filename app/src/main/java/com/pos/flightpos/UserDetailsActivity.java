package com.pos.flightpos;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.pos.flightpos.objects.Constants;
import com.pos.flightpos.utils.POSCommonUtils;
import com.pos.flightpos.utils.SaveSharedPreference;

import java.util.Map;

public class UserDetailsActivity extends AppCompatActivity {

    Button scanBoardingPassBtn;
    EditText passengerName;
    EditText pnrNo;
    EditText seatNo;
    EditText emailId;
    Button submitBtn;
    String category;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);
        scanBoardingPassBtn = (Button) findViewById(R.id.scanBoardingPass);
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
                clickSubmitBtn();
            }
        });
        category = getIntent().getStringExtra("category");
        ImageButton backButton = findViewById(R.id.backPressBtn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void clickSubmitBtn(){
        if(passengerName.getText() != null && !passengerName.getText().equals("") &&
                pnrNo.getText() != null && pnrNo.getText().toString() != null
                 && seatNo.getText() != null && !seatNo.getText().toString().equals("") && emailId.getText() != null && !emailId.getText().toString().equals("")) {

            String userDetailsStr = passengerName.getText().toString() + "==" + pnrNo.getText().toString() +"==" +
                    seatNo.getText().toString() +"=="+emailId.getText().toString();
            SaveSharedPreference.setStringValues(this,Constants.SHARED_PREFERENCE_USER_DETAILS,userDetailsStr);
            Intent intent = new Intent(this, GateItemSelectionActivity.class);
            intent.putExtra("category",category);
            startActivity(intent);

        }
        else{
            Toast.makeText(getApplicationContext(), "Please fill required details.",
                    Toast.LENGTH_SHORT).show();
        }
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
                            passengerName.setText(qrCodeDetails.get("name"));
                            pnrNo.setText(qrCodeDetails.get("PNR"));
                            seatNo.setText(qrCodeDetails.get("seatNo"));
                        }})
                    .setNegativeButton(android.R.string.cancel, null).show();
        }
    }
}
