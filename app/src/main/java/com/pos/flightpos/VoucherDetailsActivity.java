package com.pos.flightpos;

import android.app.DatePickerDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.pos.flightpos.objects.Constants;
import com.pos.flightpos.utils.POSDBHandler;
import com.pos.flightpos.utils.PrintJob;
import com.pos.flightpos.utils.SaveSharedPreference;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class VoucherDetailsActivity extends AppCompatActivity {

    Calendar myCalendar;
    EditText expireDateFld;
    EditText voucherNameFld;
    EditText voucherNumberFld;
    EditText amountText;
    Button submitBtn;
    POSDBHandler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voucher_details);
        expireDateFld = findViewById(R.id.expireDateField);
        voucherNameFld = findViewById(R.id.voucherNameFld);
        voucherNumberFld = findViewById(R.id.voucherNumberFld);
        amountText = findViewById(R.id.amountText);
        submitBtn = findViewById(R.id.submitBtn);
        setDatePicker();
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickSubmitBtn();
            }
        });
    }

    private void clickSubmitBtn(){
        String voucherName = String.valueOf(voucherNameFld.getText());
        String voucherNumber = String.valueOf(voucherNumberFld.getText());
        String amount = String.valueOf(amountText.getText());
        String expireDate = String.valueOf(expireDateFld.getText());

        if(voucherName != null && !voucherName.isEmpty() && voucherNumber != null && !voucherNumber.isEmpty() &&
                amount != null && !amount.isEmpty() && expireDate != null && !expireDate.isEmpty()){
            String passangerDetails = SaveSharedPreference.getStringValues(this, Constants.SHARED_PREFERENCE_USER_DETAILS);
            String[] passengerArr = passangerDetails.split("==");
            handler.insertCompensationVouchers(voucherNumber,voucherName,amount,expireDate,passengerArr[0],passengerArr[1],
                    SaveSharedPreference.getStringValues(this, Constants.SHARED_PREFERENCE_FA_NAME));
            PrintJob.printVoucherDetails(this,voucherName,amount,voucherNumber,expireDate,passengerArr[0]);
        }
        else {
            Toast.makeText(getApplicationContext(), "Please fill required details.",
                    Toast.LENGTH_SHORT).show();
        }

    }

    private void setDatePicker(){
        myCalendar = Calendar.getInstance();
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }
        };
        expireDateFld.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(VoucherDetailsActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    private void updateLabel() {
        String myFormat = "dd/MM/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat);

        expireDateFld.setText(sdf.format(myCalendar.getTime()));
    }
}
