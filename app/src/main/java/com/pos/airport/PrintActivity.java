package com.pos.airport;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.pt.printer.Printer;
import android.view.View;
import android.widget.LinearLayout;

import com.pos.airport.objects.Constants;
import com.pos.airport.utils.POSCommonUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PrintActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print);

        final LinearLayout printingLayout = (LinearLayout) findViewById(R.id.printLayout);
        printingLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doPrint();
            }
        });


    }

    private void doPrint() {
       Printer printer = new Printer();
       printer.open();
       printer.setAlignment(1);
        printer.printPictureByRelativePath(Constants.PRINTER_LOGO_LOCATION, 200, 70);
        printer.printString(" ");
        printer.printBlankLines(4);
        printer.printString(POSCommonUtils.getFlightDetailsStr(this));
        Date date = new Date();
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
        printer.printString(df.format(date));
        printer.printBlankLines(7);
        printer.printString("Sale transaction");
        printer.setAlignment(0);
        printer.printString("Seat Number : 5");
        printer.printString("Absolute Vodka        10.00");
        printer.printString("sku 123456 Each $10");
        printer.printBlankLines(7);
        printer.setAlignment(2);
        printer.printString("Total USD 10.00");
        printer.printString("Cash USD 10.00");
        printer.printBlankLines(10);

       printer.close();
    }
}
