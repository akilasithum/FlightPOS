package com.pos.flightpos;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.pos.flightpos.objects.Constants;
import com.pos.flightpos.utils.SaveSharedPreference;

public class BaseStationActivity extends AppCompatActivity {

    EditText baseStationText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_station);
        baseStationText = findViewById(R.id.baseStation);

        Button baseStationLayout = findViewById(R.id.changeBaseStationBtn);
        baseStationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateBaseStation();
            }
        });
        String baseStation = SaveSharedPreference.getStringValues(this,Constants.SHARED_PREFERENCE_BASE_STATION);
        if(baseStation != null && !baseStation.isEmpty()){
            baseStationText.setText(baseStation);
        }
    }

    private void updateBaseStation(){
        if(baseStationText.getText() != null && !baseStationText.getText().toString().isEmpty()){
            String baseStation = baseStationText.getText().toString();
            SaveSharedPreference.setStringValues(this, Constants.SHARED_PREFERENCE_BASE_STATION,baseStation);
            Toast.makeText(this, "Base station updated", Toast.LENGTH_SHORT).show();
            onBackPressed();
        }
        else {
            Toast.makeText(this, "Fill base station value", Toast.LENGTH_SHORT).show();
        }

    }
}
