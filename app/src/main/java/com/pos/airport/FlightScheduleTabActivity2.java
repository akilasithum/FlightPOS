package com.pos.airport;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.pos.airport.ui.flightscheduletabactivity2.FlightScheduleTabActivity2Fragment;

public class FlightScheduleTabActivity2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.flight_schedule_tab_activity2_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, FlightScheduleTabActivity2Fragment.newInstance())
                    .commitNow();
        }
    }
}
