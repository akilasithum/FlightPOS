package com.pos.flightpos;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.pos.flightpos.objects.Constants;
import com.pos.flightpos.utils.SaveSharedPreference;

import java.util.concurrent.TimeUnit;

        public class OptionsActivity extends AppCompatActivity {

            FrameLayout progressBarHolder;
            AlphaAnimation inAnimation;
            AlphaAnimation outAnimation;
            private ProgressDialog dialog;

            @Override
            protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);
        progressBarHolder = findViewById(R.id.progressBarHolder);
        setClickListeners();
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

            private class SyncData extends AsyncTask<Void, Void, Void> {

                public SyncData(Context context) {
                    dialog = new ProgressDialog(context);
                }

                protected void onPreExecute() {
                    dialog.setMessage("Data upload in progress. Please wait ...");
                    dialog.show();
                }

                @Override
                protected Void doInBackground(Void... voids) {
                    try {
                        Thread.sleep(4000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                }
            }

    private void setClickListeners(){

        ImageButton logoutLayout = findViewById(R.id.logoutLayout);
        logoutLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new android.support.v7.app.AlertDialog.Builder(OptionsActivity.this)
                        .setTitle("Logout")
                        .setMessage("Are you sure you want to logout from the system?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                SaveSharedPreference.removeValue(OptionsActivity.this, Constants.SHARED_PREFERENCE_FA_NAME);
                                SaveSharedPreference.removeValue(OptionsActivity.this, Constants.SHARED_PREFERENCE_FLIGHT_MODE);
                                Intent intent = new Intent(OptionsActivity.this, FlightAttendentLogin.class);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null).show();
            }
        });

        LinearLayout syncLayout = findViewById(R.id.syncDataLayout);
        syncLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OptionsActivity.this, SyncActivity.class);
                intent.putExtra("parent","optionActivity");
                startActivity(intent);
            }
        });

        LinearLayout voidOrderLayout = findViewById(R.id.voidOrderLayout);
        voidOrderLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OptionsActivity.this, VoidOrderActivity.class);
                startActivity(intent);
            }
        });

        LinearLayout printTransactionsLayout = findViewById(R.id.printTransactionsLayout);
        printTransactionsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OptionsActivity.this, PrintTransactionActivity.class);
                startActivity(intent);
            }
        });

        LinearLayout uploadSalesDataLayout = findViewById(R.id.uploadSalesData);
        uploadSalesDataLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SyncData(OptionsActivity.this).execute();
            }
        });
    }

    private class MyTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            inAnimation = new AlphaAnimation(0f, 1f);
            inAnimation.setDuration(200);
            progressBarHolder.setAnimation(inAnimation);
            progressBarHolder.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            outAnimation = new AlphaAnimation(1f, 0f);
            outAnimation.setDuration(200);
            progressBarHolder.setAnimation(outAnimation);
            progressBarHolder.setVisibility(View.GONE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                for (int i = 0; i < 5; i++) {
                    TimeUnit.SECONDS.sleep(1);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    public void onBackPressed()
    {

        Intent intent = new Intent(this, GateUserMainActivity.class);
        startActivity(intent);

    }
}
