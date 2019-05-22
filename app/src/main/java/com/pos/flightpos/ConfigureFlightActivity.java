package com.pos.flightpos;

import android.content.Intent;
import android.support.v7.app.ActionBar;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.pos.flightpos.objects.Constants;
import com.pos.flightpos.objects.Flight;
import com.pos.flightpos.objects.XMLMapper.KitNumber;
import com.pos.flightpos.utils.MultiSelectionSpinner;
import com.pos.flightpos.utils.POSCommonUtils;
import com.pos.flightpos.utils.POSDBHandler;
import com.pos.flightpos.utils.SaveSharedPreference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class ConfigureFlightActivity extends AppCompatActivity {

    MultiSelectionSpinner equipmentSpinner;
    POSDBHandler handler;
    TextView flightFrom;
    TextView flightTo;
    Button submitBtn;
    Spinner flightDateSpinner;
    EditText flightListTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_flight);
        handler = new POSDBHandler(this);
        equipmentSpinner =  findViewById(R.id.equipmentNumber);
        flightFrom = (TextView) findViewById(R.id.fromTextField);
        flightTo = (TextView) findViewById(R.id.toTextField);
        flightDateSpinner = (Spinner) findViewById(R.id.flightDateSpinner);
        flightDateSpinner.setPrompt("Flight Date");
        flightListTextView =  findViewById(R.id.flightList);
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
        loadEquipmentNumbers();
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
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
    }

    private void loadEquipmentNumbers(){

        /*List<KitNumber> options=new ArrayList<>();
        List<KitNumber> equipmentList = posdbHandler.getKITCodeList();
        KitNumber item = new KitNumber();
        options.add(item);
        options.addAll(equipmentList);
        ArrayAdapter<KitNumber> adapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,options);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        equipmentSpinner.setAdapter(adapter);*/

        equipmentSpinner= findViewById(R.id.equipmentNumber);
        List<KitNumber> equipmentList = handler.getKITCodeList();
        List<String> list = new ArrayList<>();
        //list.add("");
        for(KitNumber kitNumber : equipmentList)list.add(kitNumber.getKitCode());
        equipmentSpinner.setItems(list);
    }

    private void populateDateField(){

        ArrayList<String> options=new ArrayList<String>();
        options.add("Flight Date");
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
            POSCommonUtils.hideKeyboard(ConfigureFlightActivity.this);
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
        if(flightDateSpinner.getSelectedItem() != null && !flightDateSpinner.getSelectedItem().equals("Flight Date") &&
                flightListTextView.getText() != null && flightListTextView.getText().toString() != null
                && !flightListTextView.getText().toString().equals("") && flightFrom.getText() != null &&
                !flightFrom.getText().toString().equals("") && equipmentSpinner.getSelectedItem() != null &&
                ! equipmentSpinner.getSelectedItem().equals("")) {
            Intent intent = new Intent(this, VerifyFlightByAdminActivity.class);
            String kitNumber = String.valueOf(equipmentSpinner.getSelectedItem());
            String[] kitCodes = kitNumber.split(",");
            String kitCode = "";
            for(int i =0;i<kitCodes.length;i++){
                kitCode += kitCodes[i].trim()+",";
            }
            SaveSharedPreference.setStringValues(this,
                    Constants.SHARED_PREFERENCE_KIT_CODE,kitCode.substring(0,kitCode.length()-1));
            SaveSharedPreference.setStringValues(this,Constants.SHARED_PREFERENCE_FLIGHT_NAME,
                    flightListTextView.getText().toString());
            SaveSharedPreference.setStringValues(this,Constants.SHARED_PREFERENCE_FLIGHT_DATE,
                    flightDateSpinner.getSelectedItem().toString());
            SaveSharedPreference.setStringValues(this,Constants.SHARED_ADMIN_CONFIGURE_FLIGHT,"yes");
            startActivity(intent);
            String deviceId = SaveSharedPreference.getStringValues(this,Constants.SHARED_PREFERENCE_DEVICE_ID);
            //handler.updateSIFDetails("packedFor",flightListTextView.getText().toString(),deviceId);
            Set<String> serviceTypes = POSCommonUtils.getServiceTypeKitCodeMap(this).keySet();
            String serviceTypesStr = "";
            for(String str : serviceTypes){
                serviceTypesStr += str + ",";
            }
            handler.updateSIFDetailsFromConfigureFlight(flightListTextView.getText().toString(),serviceTypesStr.substring(0,serviceTypesStr.length()-1) ,deviceId);
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
