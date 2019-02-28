package com.pos.flightpos;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.pos.flightpos.objects.Constants;
import com.pos.flightpos.objects.Flight;
import com.pos.flightpos.objects.XMLMapper.KitNumber;
import com.pos.flightpos.utils.MultiSelectionSpinner;
import com.pos.flightpos.utils.POSDBHandler;
import com.pos.flightpos.utils.SaveSharedPreference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ConfigureFlightActivity extends AppCompatActivity {

    POSDBHandler handler;
    TextView flightFrom;
    TextView flightTo;
    Button submitBtn;
    Spinner flightDateSpinner;
    EditText flightListTextView;
    String category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_flight);
        handler = new POSDBHandler(this);
        flightFrom = findViewById(R.id.fromTextField);
        flightTo =  findViewById(R.id.toTextField);
        flightDateSpinner =  findViewById(R.id.flightDateSpinner);
        flightListTextView =  findViewById(R.id.flightList);
        flightFrom.setEnabled(false);
        flightTo.setEnabled(false);
        submitBtn =  findViewById(R.id.submitBtn);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickSubmitBtn();
            }
        });
        category = getIntent().getStringExtra("category").toString();
        populateDateField();
        flightListTextView.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if(s.length() == 3) {
                    populateFlightList(flightListTextView.getText().toString());
                }
                else{
                    clearFlightFromToDetails();
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        ImageButton backButton = findViewById(R.id.backPressBtn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
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
        adapter.setDropDownViewResource(R.layout.spinner_item);
        flightDateSpinner.setAdapter(adapter);
    }

    private void populateFlightList(String flightNumber){

        Flight flight = handler.getFlightFromFlightName(flightNumber);
        if(flight != null) {
            flightListTextView.setText(flight.getFlightName());
            flightFrom.setText(flight.getFlightFrom());
            flightTo.setText(flight.getFlightTo());
        }
        else{
            Toast.makeText(getApplicationContext(), "Invalid flight number",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void clearFlightFromToDetails(){
        flightFrom.setText("");
        flightTo.setText("");
    }

    private void clickSubmitBtn(){
        if(flightDateSpinner.getSelectedItem() != null && !flightDateSpinner.getSelectedItem().equals("") &&
                flightListTextView.getText() != null && flightListTextView.getText().toString() != null
                && !flightListTextView.getText().toString().equals("") && flightFrom.getText() != null &&
                !flightFrom.getText().toString().equals("")) {
            Intent intent = new Intent(this, UserDetailsActivity.class);
            SaveSharedPreference.setStringValues(this,Constants.SHARED_PREFERENCE_FLIGHT_NAME,
                    flightListTextView.getText().toString());
            SaveSharedPreference.setStringValues(this,Constants.SHARED_PREFERENCE_FLIGHT_DATE,
                    flightDateSpinner.getSelectedItem().toString());
            String deviceId = SaveSharedPreference.getStringValues(this,Constants.SHARED_PREFERENCE_DEVICE_ID);
            handler.updateSIFDetails("packedFor",flightListTextView.getText().toString(),deviceId);
            intent.putExtra("category",category);
            SaveSharedPreference.removeValue(this,Constants.SHARED_PREFERENCE_KEEP_SAME_FLIGHT);
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
