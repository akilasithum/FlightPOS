package com.pos.airport;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.pos.airport.objects.Constants;
import com.pos.airport.utils.SaveSharedPreference;

public class VoucherSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voucher_selection);
        registerLayoutClickEvents();
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        ImageButton backButton = findViewById(R.id.backPressBtn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void registerLayoutClickEvents() {
        LinearLayout transportCategoryLayout = findViewById(R.id.transaportCatLayout);
        transportCategoryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoNextView("Transport");
            }
        });
        LinearLayout mealCategoryLayout = findViewById(R.id.mealCatLayout);
        mealCategoryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoNextView("Meals");
            }
        });
        LinearLayout hotelCategoryLayout = findViewById(R.id.hotelCatLayout);
        hotelCategoryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoNextView("Hotels");
            }
        });
    }

    private void gotoNextView(String category) {
        String nextView = SaveSharedPreference.getStringValues(this, Constants.SHARED_PREFERENCE_KEEP_SAME_FLIGHT);
        if (nextView != null && nextView.equals("yes")) {
            Intent intent = new Intent(VoucherSelectionActivity.this, UserDetailsActivity.class);
            intent.putExtra("category", category);
            startActivity(intent);
        } else {
            Intent intent = new Intent(VoucherSelectionActivity.this, ConfigureFlightActivity.class);
            intent.putExtra("category", category);
            startActivity(intent);
        }
    }
}
