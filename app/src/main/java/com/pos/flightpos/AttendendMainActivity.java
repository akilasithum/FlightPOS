package com.pos.flightpos;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.pos.flightpos.objects.Flight;
import com.pos.flightpos.utils.POSDBHandler;
import com.pos.flightpos.utils.SaveSharedPreference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class AttendendMainActivity extends AppCompatActivity {

    private POSDBHandler handler;
    TextView flightFrom;
    TextView flightTo;
    Button submitBtn;
    Spinner flightDateSpinner;
    AutoCompleteTextView flightListTextView;
    TextView paxCount;
    long mExitTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new POSDBHandler(this);
        setContentView(R.layout.activity_attendend_main);
        flightFrom = (TextView) findViewById(R.id.fromTextField);
        flightTo = (TextView) findViewById(R.id.toTextField);
        flightDateSpinner = (Spinner) findViewById(R.id.flightDateSpinner);
        flightListTextView = (AutoCompleteTextView) findViewById(R.id.flightList);
        paxCount = (EditText) findViewById(R.id.paxContField);
        flightFrom.setEnabled(false);
        flightTo.setEnabled(false);
        submitBtn = (Button) findViewById(R.id.submitBtn);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickSubmitBtn();
            }
        });
        populateDateField();
        populateFlightList();
    }

    private void clickSubmitBtn(){

        if(flightDateSpinner.getSelectedItem() != null && !flightDateSpinner.getSelectedItem().equals("") &&
                flightListTextView.getText() != null && flightListTextView.getText().toString() != null
                && !flightListTextView.getText().toString().equals("") && flightFrom.getText() != null &&
                !flightFrom.getText().toString().equals("") && paxCount.getText() != null &&
                ! paxCount.getText().toString().equals("")) {
            Intent intent = new Intent(this, AttCheckInfo.class);
            SaveSharedPreference.setStringValues(this,"paxCount",paxCount.getText().toString());
            intent.putExtra("userName", "");
            startActivity(intent);
        }
        else{
            Toast.makeText(getApplicationContext(), "Please fill required details.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void populateDateField(){

        ArrayList<String> options=new ArrayList<String>();
        options.add("");
        options.add(getDateWithoutTimeUsingFormat(0));
        options.add(getDateWithoutTimeUsingFormat(1));
        options.add(getDateWithoutTimeUsingFormat(2));
        options.add(getDateWithoutTimeUsingFormat(3));
        options.add(getDateWithoutTimeUsingFormat(4));
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,options);
        flightDateSpinner.setAdapter(adapter);
    }

    private void populateFlightList(){

        final String[] flights = handler.getFlightNames();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, flights);
        flightListTextView.setAdapter(adapter);
        flightListTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Flight flight = handler.getFlightFromFlightName(flights[i]);
                flightFrom.setText(flight.getFlightFrom());
                flightTo.setText(flight.getFlightTo());
            }
        });
    }

    public String getDateWithoutTimeUsingFormat(int dateCount){
        Date today = new Date();
        Date reqDate = new Date(today.getTime() + dateCount * (1000 * 60 * 60 * 24));
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        return formatter.format(reqDate);
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
