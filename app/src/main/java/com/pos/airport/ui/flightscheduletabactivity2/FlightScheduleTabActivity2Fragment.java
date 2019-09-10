package com.pos.airport.ui.flightscheduletabactivity2;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pos.airport.R;

public class FlightScheduleTabActivity2Fragment extends Fragment {

    private FlightScheduleTabActivity2ViewModel mViewModel;

    public static FlightScheduleTabActivity2Fragment newInstance() {
        return new FlightScheduleTabActivity2Fragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.flight_schedule_tab_activity2_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(FlightScheduleTabActivity2ViewModel.class);
        // TODO: Use the ViewModel
    }

}
