package com.pos.flightpos;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.pos.flightpos.objects.Constants;
import com.pos.flightpos.objects.Flight;
import com.pos.flightpos.utils.POSCommonUtils;
import com.pos.flightpos.utils.POSDBHandler;
import com.pos.flightpos.utils.SaveSharedPreference;

public class AttendendMainActivity extends AppCompatActivity {

    TextView flightFrom;
    TextView flightTo;
    Button submitBtn;
    EditText flightDate;
    EditText flightTextView;
    TextView eClassPaxCount;
    TextView bClassPaxCount;
    EditText taxPercentage;
    long mExitTime = 0;
    String serviceType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendend_main);
        flightFrom = (TextView) findViewById(R.id.fromTextField);
        flightTo = (TextView) findViewById(R.id.toTextField);
        flightDate = (EditText) findViewById(R.id.flightDateSpinner);
        flightTextView = (EditText) findViewById(R.id.flightList);
        eClassPaxCount = (EditText) findViewById(R.id.eClassPaxContField);
        bClassPaxCount = (EditText) findViewById(R.id.bClassPaxContField);
        taxPercentage = findViewById(R.id.taxPercentage);
        submitBtn = (Button) findViewById(R.id.submitBtn);
        serviceType = POSCommonUtils.getServiceType(this);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickSubmitBtn();
            }
        });
        setFlightDetails();
        showHideTaxPercentage();
    }

    private void showHideTaxPercentage(){
        TableRow tableRow = findViewById(R.id.taxPercentageRow);
        if(serviceType == null || !serviceType.equals("DTP")){
            tableRow.setVisibility(View.GONE);
        }
    }

    private void setFlightDetails(){

        String flightName = SaveSharedPreference.getStringValues(this, Constants.SHARED_PREFERENCE_FLIGHT_NAME);
        flightTextView.setText(flightName);
        flightTextView.setEnabled(false);
        String flightDateStr = SaveSharedPreference.getStringValues(this, Constants.SHARED_PREFERENCE_FLIGHT_DATE);
        flightDate.setText(flightDateStr);
        flightDate.setEnabled(false);
        POSDBHandler handler = new POSDBHandler(this);
        Flight flight = handler.getFlightFromFlightName(flightName);
        flightFrom.setText(flight.getFlightFrom());
        flightTo.setText(flight.getFlightTo());
        flightFrom.setEnabled(false);
        flightTo.setEnabled(false);
        showPaxCount();
    }

    private void showPaxCount(){
        String eClassPaxCountStr = SaveSharedPreference.getStringValues(this,"eClassPaxCount");
        String paxCountStr = SaveSharedPreference.getStringValues(this,"bClassPaxCount");
        if(eClassPaxCountStr != null && !eClassPaxCountStr.equals("")){
            eClassPaxCount.setText(eClassPaxCountStr);
        }
        if(paxCountStr != null && !paxCountStr.equals("")){
            bClassPaxCount.setText(paxCountStr);
        }
    }

    private void clickSubmitBtn(){

        if(eClassPaxCount.getText() != null &&! eClassPaxCount.getText().toString().equals("")) {
            if(bClassPaxCount.getText() != null &&! bClassPaxCount.getText().toString().equals("")){
                SaveSharedPreference.setStringValues(this,"bClassPaxCount", bClassPaxCount.getText().toString());
            }
            if(serviceType != null && serviceType.equals("DTP")){
                if(taxPercentage.getText() != null && !taxPercentage.getText().toString().equals("")){
                    SaveSharedPreference.setStringValues(this,
                            Constants.SHARED_PREFERENCE_TAX_PERCENTAGE,taxPercentage.getText().toString());
                }
                else{
                    Toast.makeText(getApplicationContext(), "Please specify tax percentage",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            Intent intent = new Intent(this, AttCheckInfo.class);
            SaveSharedPreference.setStringValues(this,"eClassPaxCount", eClassPaxCount.getText().toString());
            startActivity(intent);
        }
        else{
            Toast.makeText(getApplicationContext(), "Please specify pax count.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed()
    {
        if((System.currentTimeMillis() - mExitTime) < 2000)
        {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        else
        {
            Toast.makeText(this, "Press the back button once again to close the application.", Toast.LENGTH_SHORT).show();
            mExitTime = System.currentTimeMillis();
        }
    }

}
