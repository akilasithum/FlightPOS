package com.pos.flightpos;

import android.app.Dialog;
import android.content.Intent;
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

import com.pos.flightpos.objects.Constants;
import com.pos.flightpos.objects.XMLMapper.Currency;
import com.pos.flightpos.utils.POSCommonUtils;
import com.pos.flightpos.utils.POSDBHandler;
import com.pos.flightpos.utils.SaveSharedPreference;

public class GateUserMainActivity extends AppCompatActivity {

    POSDBHandler handler;

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

        final Dialog cashSettleDialog = new Dialog(this);
        cashSettleDialog.setContentView(R.layout.activity_manager_login);
        Window window = cashSettleDialog.getWindow();
        window.setLayout(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        cashSettleDialog.setTitle("Manager Login");

        final EditText userName = cashSettleDialog.findViewById(R.id.userNameFld);
        final EditText password = cashSettleDialog.findViewById(R.id.passwordFld);
        final TextView errorMsgText = cashSettleDialog.findViewById(R.id.errorMsgText);

        Button okBtn =  cashSettleDialog.findViewById(R.id.submitBtn);
        Button cancelBtn =  cashSettleDialog.findViewById(R.id.cancelBtn);

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
                        Intent intent = new Intent(GateUserMainActivity.this, ConpensationSelectionActivity.class);
                        startActivity(intent);
                        cashSettleDialog.dismiss();
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
                cashSettleDialog.dismiss();
            }
        });
        cashSettleDialog.show();
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
