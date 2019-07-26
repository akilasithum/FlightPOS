package com.pos.swoop;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.pos.swoop.objects.XMLMapper.Currency;
import com.pos.swoop.utils.POSDBHandler;

import java.util.List;

public class ExchangeRateActivity extends AppCompatActivity {

    TableLayout currencyTable;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exchange_rate);
        currencyTable = (TableLayout) findViewById(R.id.showAvaiableCurrency);
        showCurrencyInTable();
        ImageButton backButton = findViewById(R.id.backPressBtn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
    }

    private void showCurrencyInTable(){

        POSDBHandler handler = new POSDBHandler(this);
        List<Currency> currencyList = handler.getCurrencyList();
        for(Currency currency : currencyList){
            TableRow tr = new TableRow(this);
            tr.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));

            TableRow.LayoutParams cellParams1 = new TableRow.LayoutParams(0,
                    TableRow.LayoutParams.WRAP_CONTENT, 3f);
            TableRow.LayoutParams cellParams2 = new TableRow.LayoutParams(0,
                    TableRow.LayoutParams.WRAP_CONTENT, 4f);
            TableRow.LayoutParams cellParams3 = new TableRow.LayoutParams(0,
                    TableRow.LayoutParams.WRAP_CONTENT, 3f);

            TextView currencyCode = new TextView(this);
            currencyCode.setGravity(Gravity.CENTER);
            currencyCode.setBackgroundColor(getResources().getColor(R.color.tableLight));
            currencyCode.setText(currency.getCurrencyCode());
            currencyCode.setTextSize(14);
            currencyCode.setLayoutParams(cellParams1);
            tr.addView(currencyCode);

            TextView currencyDesc = new TextView(this);
            currencyDesc.setGravity(Gravity.CENTER);
            currencyDesc.setBackgroundColor(getResources().getColor(R.color.tableDark));
            currencyDesc.setText(String.valueOf(currency.getCurrencyDesc()));
            currencyDesc.setTextSize(14);
            currencyDesc.setLayoutParams(cellParams2);
            tr.addView(currencyDesc);

            TextView currencyRate = new TextView(this);
            currencyRate.setGravity(Gravity.CENTER);
            currencyRate.setBackgroundColor(getResources().getColor(R.color.tableLight));
            currencyRate.setText(String.valueOf(currency.getCurrencyRate()));
            currencyRate.setTextSize(14);
            currencyRate.setLayoutParams(cellParams3);
            tr.addView(currencyRate);

            currencyTable.addView(tr);
        }
    }
}
