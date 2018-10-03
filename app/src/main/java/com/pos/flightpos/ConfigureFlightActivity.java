package com.pos.flightpos;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.pos.flightpos.objects.Constants;
import com.pos.flightpos.objects.Flight;
import com.pos.flightpos.objects.XMLMapper.KitNumber;
import com.pos.flightpos.utils.POSDBHandler;
import com.pos.flightpos.utils.SaveSharedPreference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ConfigureFlightActivity extends AppCompatActivity {

    Spinner equipmentSpinner;
    POSDBHandler handler;
    TextView flightFrom;
    TextView flightTo;
    Button submitBtn;
    Spinner flightDateSpinner;
    AutoCompleteTextView flightListTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_flight);
        handler = new POSDBHandler(this);
        equipmentSpinner = (Spinner) findViewById(R.id.equipmentNumber);
        flightFrom = (TextView) findViewById(R.id.fromTextField);
        flightTo = (TextView) findViewById(R.id.toTextField);
        flightDateSpinner = (Spinner) findViewById(R.id.flightDateSpinner);
        flightListTextView = (AutoCompleteTextView) findViewById(R.id.flightList);
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
        loadEquipmentNumbers();
    }

    private void loadEquipmentNumbers(){

        List<KitNumber> options=new ArrayList<>();
        List<KitNumber> equipmentList = handler.getKITCodeList();
        KitNumber item = new KitNumber();
        options.add(item);
        options.addAll(equipmentList);
        ArrayAdapter<KitNumber> adapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,options);
        equipmentSpinner.setAdapter(adapter);

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

    private void clickSubmitBtn(){
        if(flightDateSpinner.getSelectedItem() != null && !flightDateSpinner.getSelectedItem().equals("") &&
                flightListTextView.getText() != null && flightListTextView.getText().toString() != null
                && !flightListTextView.getText().toString().equals("") && flightFrom.getText() != null &&
                !flightFrom.getText().toString().equals("") && equipmentSpinner.getSelectedItem() != null &&
                ! equipmentSpinner.getSelectedItem().equals("")) {
            Intent intent = new Intent(this, VerifyFlightByAdminActivity.class);
            KitNumber kitNumber = (KitNumber)equipmentSpinner.getSelectedItem();
            SaveSharedPreference.setStringValues(this, Constants.SHARED_PREFERENCE_KIT_CODE,kitNumber.getKitCode());
            SaveSharedPreference.setStringValues(this,Constants.SHARED_PREFERENCE_FLIGHT_NAME,
                    flightListTextView.getText().toString());
            SaveSharedPreference.setStringValues(this,Constants.SHARED_PREFERENCE_FLIGHT_DATE,
                    flightDateSpinner.getSelectedItem().toString());
            SaveSharedPreference.setStringValues(this,Constants.SHARED_ADMIN_CONFIGURE_FLIGHT,"yes");
            startActivity(intent);
        }
        else{
            Toast.makeText(getApplicationContext(), "Please fill required details.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public String getDateWithoutTimeUsingFormat(int dateCount){
        Date today = new Date();
        Date reqDate = new Date(today.getTime() + dateCount * (1000 * 60 * 60 * 24));
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        return formatter.format(reqDate);
    }
}
