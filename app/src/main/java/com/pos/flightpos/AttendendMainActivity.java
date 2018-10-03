package com.pos.flightpos;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.pos.flightpos.objects.Constants;
import com.pos.flightpos.objects.Flight;
import com.pos.flightpos.utils.POSDBHandler;
import com.pos.flightpos.utils.SaveSharedPreference;

public class AttendendMainActivity extends AppCompatActivity {

    TextView flightFrom;
    TextView flightTo;
    Button submitBtn;
    EditText flightDate;
    EditText flightTextView;
    TextView paxCount;
    long mExitTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendend_main);
        flightFrom = (TextView) findViewById(R.id.fromTextField);
        flightTo = (TextView) findViewById(R.id.toTextField);
        flightDate = (EditText) findViewById(R.id.flightDateSpinner);
        flightTextView = (EditText) findViewById(R.id.flightList);
        paxCount = (EditText) findViewById(R.id.paxContField);
        submitBtn = (Button) findViewById(R.id.submitBtn);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickSubmitBtn();
            }
        });
        setFlightDetails();
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
    }

    private void clickSubmitBtn(){

        if(paxCount.getText() != null &&! paxCount.getText().toString().equals("")) {
            Intent intent = new Intent(this, AttCheckInfo.class);
            SaveSharedPreference.setStringValues(this,"paxCount",paxCount.getText().toString());
            startActivity(intent);
        }
        else{
            Toast.makeText(getApplicationContext(), "Please fill required details.",
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
