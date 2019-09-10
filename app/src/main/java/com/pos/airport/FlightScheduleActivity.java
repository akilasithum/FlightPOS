package com.pos.airport;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pos.airport.fragements.ArrivalFlightsFragment;
import com.pos.airport.fragements.DepartureFlightsFragment;
import com.pos.airport.objects.XMLMapper.ArrivalFlight;
import com.pos.airport.objects.XMLMapper.DepartureFlight;
import com.pos.airport.utils.HttpHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import java.util.ArrayList;
import java.util.List;

public class FlightScheduleActivity extends AppCompatActivity {

    LinearLayout contentLayout;
    List<DepartureFlight> departureFlights;
    List<ArrivalFlight> arrivalFlights;
    private ProgressDialog dia;
    final AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.8F);
    DepartureFlightsFragment flightsFragment;
    Fragment fragment = null;
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight_schedule);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        contentLayout = findViewById(R.id.contentLayout);
        ImageButton backButton = findViewById(R.id.backPressBtn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        tabLayout=findViewById(R.id.tabLayout);
        RelativeLayout frameLayout= findViewById(R.id.frameLayout);

        fragment = new DepartureFlightsFragment();
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.commit();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Fragment fragment = null;
                switch (tab.getPosition()) {
                    case 0:
                        fragment = new DepartureFlightsFragment();
                        showFlightSchedule(departureFlights);
                        break;
                    case 1:
                        fragment = new ArrivalFlightsFragment();
                        showArrivalFlightSchedule(arrivalFlights);
                        break;
                }
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.frameLayout, fragment);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                ft.commit();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        if(isNetworkAvailable()){

            AsyncTask<Void, Void, Void> task = new GetFlightList().execute();
            AsyncTask<Void, Void, Void> task2 = new GetArrivalFlightList().execute();
            dia = new ProgressDialog(FlightScheduleActivity.this);
            dia.show();
        }
        else{
            new AlertDialog.Builder(FlightScheduleActivity.this)
                    .setTitle("Network not available")
                    .setMessage("Please switch on wifi.")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {

                        }}).show();
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        flightsFragment = new DepartureFlightsFragment();
        adapter.addFragment(new DepartureFlightsFragment(), "Departures");
        adapter.addFragment(new ArrivalFlightsFragment(), "Arrivals");
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
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
                departureFlights = gson.fromJson(itemsArr.toString(), new TypeToken<List<DepartureFlight>>(){}.getType());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            if(dia != null) {dia.dismiss();}
            showFlightSchedule(departureFlights);
        }
    }

    private class GetArrivalFlightList extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            HttpHandler sh = new HttpHandler();
            String xml =  sh.makeServiceCall("arrivalFlightSchedule");
            JSONObject jsonObj  = null;
            try {
                jsonObj = XML.toJSONObject(xml);
                Gson gson = new Gson();
                JSONObject data = new JSONObject(jsonObj.toString()).getJSONObject("flights");
                JSONArray itemsArr = data.getJSONArray("flight");
                arrivalFlights = gson.fromJson(itemsArr.toString(), new TypeToken<List<ArrivalFlight>>(){}.getType());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            if(dia != null) {dia.dismiss();}
            //showArrivalFlightSchedule(arrivalFlights);
        }
    }


    private void showFlightSchedule(List<DepartureFlight> departureFlights){
        contentLayout.removeAllViews();
        for(final DepartureFlight flight : departureFlights){
            final FrameLayout frameLayout = new FrameLayout(this);
            LinearLayout.LayoutParams frameLayoutParams = new LinearLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,1);
            frameLayoutParams.setMargins(8,0,0,4);
            frameLayout.setLayoutParams(frameLayoutParams);

            final LinearLayout linearLayout = new LinearLayout(this);
            FrameLayout.LayoutParams linearLayoutParams = new FrameLayout.LayoutParams(600,
                    90);
            linearLayout.setGravity(Gravity.CENTER);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            if(flight.getStatus() != null && flight.getStatus().equalsIgnoreCase("On Time")){
                linearLayout.setBackgroundColor(getResources().getColor(R.color.ontime));
            }
            else if(flight.getStatus() != null && flight.getStatus().contains("Delay")){
                linearLayout.setBackgroundColor(getResources().getColor(R.color.delay));
            }
            else {
                linearLayout.setBackgroundColor(getResources().getColor(R.color.cancel));
            }

            linearLayoutParams.setMargins(0,10,10,0);
            linearLayout.setLayoutParams(linearLayoutParams);

            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showFlight(flight);
                }
            });

            TableRow tr = new TableRow(this);

            tr.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.FILL_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));
            tr.setPadding(0,5,0,0);


            TableRow.LayoutParams cellParams1 = new TableRow.LayoutParams(0,
                    TableRow.LayoutParams.WRAP_CONTENT, 1f);

            TableRow.LayoutParams cellParams4 = new TableRow.LayoutParams(0,
                    45, 1f);
            cellParams4.setMargins(0,40,0,0);

            TextView flightNoText = new TextView(this);
            TextView flightTimeText = new TextView(this);
            TextView statusText = new TextView(this);

            flightNoText.setText(flight.getFlightNo());
            flightNoText.setTextSize(15);
            flightNoText.setLayoutParams(cellParams1);
            flightNoText.setGravity(Gravity.CENTER);
            tr.addView(flightNoText);

            View view  = new View(this);
            view.setLayoutParams(new TableRow.LayoutParams(3, TableRow.LayoutParams.MATCH_PARENT));
            view.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
            tr.addView(view);

            if(flight.getFlightTime() != null && !flight.getFlightTime().isEmpty()){
                String[] flightTime = flight.getFlightTime().split(" ");
                flightTimeText.setText(flightTime[1]);
            }
            else{
                flightTimeText.setText("");
            }
            flightTimeText.setTextSize(15);
            flightTimeText.setLayoutParams(cellParams1);
            flightTimeText.setGravity(Gravity.CENTER);
            tr.addView(flightTimeText);

            View view1  = new View(this);
            view1.setLayoutParams(new TableRow.LayoutParams(3, TableRow.LayoutParams.MATCH_PARENT));
            view1.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
            tr.addView(view1);

            statusText.setText(flight.getStatus());
            statusText.setTextSize(15);
            statusText.setLayoutParams(cellParams1);
            statusText.setGravity(Gravity.CENTER);
            tr.addView(statusText);

            linearLayout.addView(tr);
            frameLayout.addView(linearLayout);
            contentLayout.addView(frameLayout);
        }
    }

    private void showArrivalFlightSchedule(List<ArrivalFlight> departureFlights){
        contentLayout.removeAllViews();
        for(final ArrivalFlight flight : departureFlights){
            final FrameLayout frameLayout = new FrameLayout(this);
            LinearLayout.LayoutParams frameLayoutParams = new LinearLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,1);
            frameLayoutParams.setMargins(8,0,0,4);
            frameLayout.setLayoutParams(frameLayoutParams);

            final LinearLayout linearLayout = new LinearLayout(this);
            FrameLayout.LayoutParams linearLayoutParams = new FrameLayout.LayoutParams(600,
                    90);
            linearLayout.setGravity(Gravity.CENTER);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            if(flight.getStatus() != null && flight.getStatus().equalsIgnoreCase("On Time")){
                linearLayout.setBackgroundColor(getResources().getColor(R.color.ontime));
            }
            else if(flight.getStatus() != null && flight.getStatus().contains("Delay")){
                linearLayout.setBackgroundColor(getResources().getColor(R.color.delay));
            }
            else {
                linearLayout.setBackgroundColor(getResources().getColor(R.color.cancel));
            }

            linearLayoutParams.setMargins(0,10,10,0);
            linearLayout.setLayoutParams(linearLayoutParams);

            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showArrivalFlight(flight);
                }
            });

            TableRow tr = new TableRow(this);

            tr.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.FILL_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));
            tr.setPadding(0,5,0,0);


            TableRow.LayoutParams cellParams1 = new TableRow.LayoutParams(0,
                    TableRow.LayoutParams.WRAP_CONTENT, 1f);

            TableRow.LayoutParams cellParams4 = new TableRow.LayoutParams(0,
                    45, 1f);
            cellParams4.setMargins(0,40,0,0);

            TextView flightNoText = new TextView(this);
            TextView flightTimeText = new TextView(this);
            TextView statusText = new TextView(this);

            flightNoText.setText(flight.getFlightNo());
            flightNoText.setTextSize(15);
            flightNoText.setLayoutParams(cellParams1);
            flightNoText.setGravity(Gravity.CENTER);
            tr.addView(flightNoText);

            View view  = new View(this);
            view.setLayoutParams(new TableRow.LayoutParams(3, TableRow.LayoutParams.MATCH_PARENT));
            view.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
            tr.addView(view);

            if(flight.getFlightTime() != null && !flight.getFlightTime().isEmpty()){
                String[] flightTime = flight.getFlightTime().split(" ");
                flightTimeText.setText(flightTime[1]);
            }
            else{
                flightTimeText.setText("");
            }
            flightTimeText.setTextSize(15);
            flightTimeText.setLayoutParams(cellParams1);
            flightTimeText.setGravity(Gravity.CENTER);
            tr.addView(flightTimeText);

            View view1  = new View(this);
            view1.setLayoutParams(new TableRow.LayoutParams(3, TableRow.LayoutParams.MATCH_PARENT));
            view1.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
            tr.addView(view1);

            statusText.setText(flight.getStatus());
            statusText.setTextSize(15);
            statusText.setLayoutParams(cellParams1);
            statusText.setGravity(Gravity.CENTER);
            tr.addView(statusText);

            linearLayout.addView(tr);
            frameLayout.addView(linearLayout);
            contentLayout.addView(frameLayout);
        }
    }

    private void showFlight(DepartureFlight flight){

        final Dialog cashSettleDialog = new Dialog(this);
        cashSettleDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        cashSettleDialog.setContentView(R.layout.flight_details_layout);
        Window window = cashSettleDialog.getWindow();
        window.setLayout(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        cashSettleDialog.setCanceledOnTouchOutside(false);

        TextView flightNo = cashSettleDialog.findViewById(R.id.flightNoId);
        TextView flightTime = cashSettleDialog.findViewById(R.id.flightTimeId);
        TextView airLine = cashSettleDialog.findViewById(R.id.airLineId);
        TextView destination = cashSettleDialog.findViewById(R.id.destinationId);
        TextView checkin = cashSettleDialog.findViewById(R.id.checkinId);
        TextView gate = cashSettleDialog.findViewById(R.id.gateId);
        flightNo.setText(flight.getFlightNo());
        flightTime.setText(flight.getFlightTime());
        airLine.setText(flight.getAirline());
        destination.setText(flight.getDestination());
        checkin.setText(flight.getCheckin());
        gate.setText(flight.getGate());

        Button okBtn =  cashSettleDialog.findViewById(R.id.cardSubmitBtn);

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);
                cashSettleDialog.dismiss();
            }
        });

        cashSettleDialog.show();
    }

    private void showArrivalFlight(ArrivalFlight flight){

        final Dialog cashSettleDialog = new Dialog(this);
        cashSettleDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        cashSettleDialog.setContentView(R.layout.flight_details_layout);
        Window window = cashSettleDialog.getWindow();
        window.setLayout(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        cashSettleDialog.setCanceledOnTouchOutside(false);

        TextView destinationFld = cashSettleDialog.findViewById(R.id.destinationFldId);
        destinationFld.setText("From");
        TextView checkinFld = cashSettleDialog.findViewById(R.id.checkinFldId);
        checkinFld.setText("Belt");

        TextView flightNo = cashSettleDialog.findViewById(R.id.flightNoId);
        TextView flightTime = cashSettleDialog.findViewById(R.id.flightTimeId);
        TextView airLine = cashSettleDialog.findViewById(R.id.airLineId);
        TextView destination = cashSettleDialog.findViewById(R.id.destinationId);
        TextView checkin = cashSettleDialog.findViewById(R.id.checkinId);
        TextView gate = cashSettleDialog.findViewById(R.id.gateId);
        flightNo.setText(flight.getFlightNo());
        flightTime.setText(flight.getFlightTime());
        airLine.setText(flight.getAirline());
        destination.setText(flight.getFrom());
        checkin.setText(flight.getBelt());
        gate.setText(flight.getGate());

        Button okBtn =  cashSettleDialog.findViewById(R.id.cardSubmitBtn);

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);
                cashSettleDialog.dismiss();
            }
        });

        cashSettleDialog.show();
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
}
