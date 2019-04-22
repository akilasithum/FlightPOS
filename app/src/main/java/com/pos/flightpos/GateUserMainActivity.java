package com.pos.flightpos;

import android.app.Dialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.pt.msr.Msr;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.pos.flightpos.objects.Constants;
import com.pos.flightpos.objects.XMLMapper.Currency;
import com.pos.flightpos.utils.POSCommonUtils;
import com.pos.flightpos.utils.POSDBHandler;
import com.pos.flightpos.utils.SaveSharedPreference;

public class GateUserMainActivity extends AppCompatActivity {

    POSDBHandler handler;
    private Msr msr = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gate_user_main);
        handler = new POSDBHandler(this);
        registerLayoutClickEvents();
    }

    private void registerLayoutClickEvents() {

        LinearLayout bagsCategoryLayout = findViewById(R.id.bagsCategoryLayout);
        bagsCategoryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoNextView("Bags");
            }
        });
        LinearLayout upgradesCategoryLayout = findViewById(R.id.upgradesCatLayout);
        upgradesCategoryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoNextView("Upgrade");
            }
        });
        LinearLayout compensationCategoryLayout = findViewById(R.id.compensationCatLayout);
        compensationCategoryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showUserPasswordView();
            }
        });
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
        LinearLayout excursionCategoryLayout = findViewById(R.id.excurtionCatLayout);
        excursionCategoryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoNextView("Excursions");
            }
        });
        LinearLayout refundCategoryLayout = findViewById(R.id.refundsCatLayout);
        refundCategoryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GateUserMainActivity.this, OptionsActivity.class);
                startActivity(intent);
            }
        });
    }

    private void showUserPasswordView(){
        msr = new Msr();
        readMSR();
        final Dialog managerLoginDialog = new Dialog(this);
        managerLoginDialog.setContentView(R.layout.activity_manager_login);
        Window window = managerLoginDialog.getWindow();
        window.setLayout(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        managerLoginDialog.setTitle("Manager Login");

        final EditText userName = managerLoginDialog.findViewById(R.id.userNameFld);
        final EditText password = managerLoginDialog.findViewById(R.id.passwordFld);
        final TextView errorMsgText = managerLoginDialog.findViewById(R.id.errorMsgText);

        Button okBtn =  managerLoginDialog.findViewById(R.id.submitBtn);
        Button cancelBtn =  managerLoginDialog.findViewById(R.id.cancelBtn);

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                errorMsgText.setText("");
                if(userName.getText() == null || userName.getText().toString().isEmpty()){
                    errorMsgText.setText("UserName is empty");
                }
                else if(password.getText() == null || password.getText().toString().isEmpty()){
                    errorMsgText.setText("Password is empty.");
                }
                else {
                    if(handler.isLoginSuccess(userName.getText().toString(),password.getText().toString(),"10")){
                        Intent intent = new Intent(GateUserMainActivity.this, ConfigureFlightActivity.class);
                        intent.putExtra("category","compensation");
                        startActivity(intent);
                        managerLoginDialog.dismiss();
                    }
                    else{
                        errorMsgText.setText("UserName or password is incorrect");
                    }

                }
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                managerLoginDialog.dismiss();
            }
        });
        managerLoginDialog.show();
    }

    private void closeMSR(){
        if(msr != null)
            msr.close();
    }

    private void readMSR(){
        msr.open();
        final Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub

                if(msg.what == 1){
                    for(int i = 1; i < 4; i++)
                    {
                        if(msr.getTrackError(i) == 0)
                        {
                            //Log.i("123", "i:"+i);
                            byte[] out_data = new byte[msr.getTrackDataLength(i)];
                            msr.getTrackData(i, out_data);
                            readMsrData(i,out_data);
                            return;
                        }
                    }
                }
                super.handleMessage(msg);
            }
        };

        new Thread(){
            public void run() {
                int ret = -1;
                while(true)
                {
                    ret = msr.poll(1000);
                    if(ret == 0)
                    {
                        Message msg = new Message();
                        msg.what    = 1;
                        handler.sendMessage(msg);
                        break;
                    }
                }
            }
        }.start();
    }

    private void readMsrData(int i, byte[] out_data){
        if(i == 1)
        {
            String track1Str = new String(out_data);
            String[] credentials = track1Str.split(" ");
            if(credentials.length > 3){
                if(handler.isLoginSuccess(credentials[1].toLowerCase(),credentials[2].toLowerCase(),"10")){
                    closeMSR();
                    Intent intent = new Intent(GateUserMainActivity.this, ConfigureFlightActivity.class);
                    intent.putExtra("category","compensation");
                    startActivity(intent);
                }
                else{
                    Toast.makeText(this, "Not a valid card.", Toast.LENGTH_SHORT).show();
                }
            }
            else {
                Toast.makeText(this, "Not a valid card.", Toast.LENGTH_SHORT).show();
            }
            readMSR();
        }
    }

    private void gotoNextView(String category) {
        String nextView = SaveSharedPreference.getStringValues(this, Constants.SHARED_PREFERENCE_KEEP_SAME_FLIGHT);
        if (nextView != null && nextView.equals("yes")) {
            Intent intent = new Intent(GateUserMainActivity.this, UserDetailsActivity.class);
            intent.putExtra("category", category);
            startActivity(intent);
        } else {
            Intent intent = new Intent(GateUserMainActivity.this, ConfigureFlightActivity.class);
            intent.putExtra("category", category);
            startActivity(intent);
        }
    }
}
