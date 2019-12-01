package com.pos.swoop;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.pos.swoop.objects.Constants;
import com.pos.swoop.objects.XMLMapper.FADetails;
import com.pos.swoop.utils.POSCommonUtils;
import com.pos.swoop.utils.POSDBHandler;
import com.pos.swoop.utils.SaveSharedPreference;

import java.util.ArrayList;
import java.util.List;

public class MessageToBondActivity extends AppCompatActivity {
    Spinner faNameSpinner;
    POSDBHandler handler;
    Button submitBtn;
    EditText msgBody;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_to_bond);
        ImageButton backButton = findViewById(R.id.backPressBtn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        Button sendMsgToBondBtn = findViewById(R.id.sendMsgToBondBtn);
        sendMsgToBondBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Message Successfully Saved",
                        Toast.LENGTH_SHORT).show();
            }
        });
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        handler = new POSDBHandler(this);
        List<FADetails> options = handler.getFADetails();
        List<String> faNames = new ArrayList<>();
        for(FADetails fa : options){
            faNames.add(fa.getFaName());
        }
        faNameSpinner = findViewById(R.id.faNameSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, faNames);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        faNameSpinner.setAdapter(adapter);
        msgBody = findViewById(R.id.messageToBond);
        submitBtn = findViewById(R.id.sendMsgToBondBtn);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveMsg();
            }
        });
    }

    private void saveMsg(){
        String faName = String.valueOf(faNameSpinner.getSelectedItem());
        String messageBody = String.valueOf(msgBody.getText());
        if(faName == null || faName.isEmpty() || faName.equalsIgnoreCase("null")){
            Toast.makeText(this,"Please specify FA name",Toast.LENGTH_SHORT).show();
        }
        else if(messageBody == null || messageBody.isEmpty() || messageBody.equalsIgnoreCase("null")){
            Toast.makeText(this,"Please specify message",Toast.LENGTH_SHORT).show();
        }
        else {
            String flightNo = SaveSharedPreference.getStringValues(this, Constants.SHARED_PREFERENCE_FLIGHT_NAME);
            String flightDate = POSCommonUtils.getFlightDateString(this);
            handler.insertMsgToBond(messageBody,flightNo,flightDate,faName);
            Toast.makeText(this,"Message saved successfully",Toast.LENGTH_SHORT).show();
        }
    }


}
