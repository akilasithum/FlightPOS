package com.pos.airport;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pos.airport.objects.Constants;
import com.pos.airport.objects.Flight;
import com.pos.airport.objects.XMLMapper.DepartureFlight;
import com.pos.airport.objects.XMLMapper.Item;
import com.pos.airport.utils.HttpHandler;
import com.pos.airport.utils.POSCommonUtils;
import com.pos.airport.utils.POSDBHandler;
import com.pos.airport.utils.SaveSharedPreference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigureFlightActivity extends AppCompatActivity {

    POSDBHandler handler;
    TextView flightFrom;
    TextView flightTo;
    Button submitBtn;
    Spinner flightDateSpinner;
    EditText flightListTextView;
    String category;
    Map<String,DepartureFlight> flightMap;

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
                v.startAnimation(Constants.buttonClickAnimation);
                clickSubmitBtn();
            }
        });
        category = getIntent().getStringExtra("category");
        TextView headerText = findViewById(R.id.headerId);
        headerText.setText("Flight Details - " + category.substring(0, 1).toUpperCase() + category.substring(1));
        populateDateField();
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
        flightMap = new HashMap<>();
        if(isNetworkAvailable()){

            AsyncTask<Void, Void, Void> task = new GetFlightList().execute();
        }
        else{
            new AlertDialog.Builder(ConfigureFlightActivity.this)
                    .setTitle("Network not available")
                    .setMessage("Please switch on wifi.")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {

                        }}).show();
        }
        ImageButton backButton = findViewById(R.id.backPressBtn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivity =(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivity == null) {
            return false;
        } else {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
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

    private class GetFlightList extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            HttpHandler sh = new HttpHandler();
            String xml =  sh.makeServiceCall("departureFlightSchedule");
            JSONObject jsonObj  = null;
            try {
                jsonObj = XML.toJSONObject(xml);

            Gson gson = new Gson();
            JSONObject data = new JSONObject(jsonObj.toString()).getJSONObject("flights");
            JSONArray itemsArr = data.getJSONArray("flight");
            List<DepartureFlight> list = gson.fromJson(itemsArr.toString(), new TypeToken<List<DepartureFlight>>(){}.getType());
            for(DepartureFlight flight : list){
                String flightNo = flight.getFlightNo();
                int length = flightNo.length();
                String id = flightNo.substring(length-3,length);
                flightMap.put(id,flight);
            }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

        }
    }

    private void populateFlightList(String flightNumber){

        DepartureFlight flight = flightMap.get(flightNumber);//handler.getFlightFromFlightName(flightNumber);
        if(flight != null) {
            flightListTextView.setText(flight.getFlightNo());
            String baseStationVal = SaveSharedPreference.getStringValues(this, Constants.SHARED_PREFERENCE_BASE_STATION);
            flightFrom.setText(baseStationVal);
            flightTo.setText(flight.getDestination());
            POSCommonUtils.hideKeyboard(this);
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
