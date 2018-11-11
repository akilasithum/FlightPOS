package com.pos.flightpos;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.pt.msr.Msr;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.pos.flightpos.objects.AcceptPreOrder;
import com.pos.flightpos.objects.Constants;
import com.pos.flightpos.objects.CreditCard;
import com.pos.flightpos.objects.LoyaltyCard;
import com.pos.flightpos.objects.SoldItem;
import com.pos.flightpos.utils.POSCommonUtils;
import com.pos.flightpos.utils.POSDBHandler;
import com.pos.flightpos.utils.PrintJob;
import com.pos.flightpos.utils.SaveSharedPreference;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PreOrderPaymentsActivity extends AppCompatActivity {

    private float dueBalance = 0;
    private ArrayList<SoldItem> soldItems;
    TableLayout paymentTable;
    TextView balanceDueTextView;
    Button confirmOrderBtn;
    Button cancelSaleBtn;
    int paymentMethodsCount = 0;
    private Msr msr = null;
    private Dialog dialog = null;
    EditText cardNumber;
    EditText cardHolderName;
    EditText expiryDate;
    EditText cardType;
    List<CreditCard> creditCardList;
    Map<String,String> paymentMethodsMap;
    String seatNumber;
    String orderNumber;
    POSDBHandler handler;
    String serviceType;
    String discount;
    String taxPercentage;
    String subTotalAfterTax;
    String paxName;
    String flightName;
    String flightDate;
    String flightSector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_order_payments);
        handler = new POSDBHandler(this);
        paymentTable = (TableLayout) findViewById(R.id.paymentMethodTable);
        confirmOrderBtn = (Button) findViewById(R.id.printReceipt);
        balanceDueTextView = (TextView)  findViewById(R.id.balanceDueTextView);
        confirmOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(confirmOrderBtn.getText().equals("Confirm Payment")) {
                    Float dispDueBalance = Float.parseFloat(POSCommonUtils.getTwoDecimalFloatFromFloat(dueBalance));
                    if (dispDueBalance <= 0.1) {
                        new android.support.v7.app.AlertDialog.Builder(PreOrderPaymentsActivity.this)
                                .setTitle("Confirm Payment")
                                .setMessage("Are you sure you want to confirm the payment?")
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        printReceipt();
                                    }
                                })
                                .setNegativeButton(android.R.string.cancel, null).show();
                    } else {
                        showToastMsg("Due balance should be zero");
                    }
                }
                else{
                    printReceipt();
                }
            }
        });
        Intent intent = getIntent();
        Bundle args = intent.getBundleExtra("BUNDLE");
        soldItems = (ArrayList<SoldItem>) args.getSerializable("soldItemList");
        String subTotal = intent.getExtras().get("subTotal").toString();
        paxName = intent.getExtras().get("paxName").toString();
        flightDate = intent.getExtras().get("flightDate").toString();
        flightName = intent.getExtras().get("flightNumber").toString();
        flightSector = intent.getExtras().get("sector").toString();
        discount = intent.getExtras().get("discount").toString();
        String serviceType = POSCommonUtils.getServiceType(this);
        TableRow totalTextRow = findViewById(R.id.totalTextRow);
        TableRow taxTextRow = findViewById(R.id.serviceTaxRow);
        if(serviceType != null && (serviceType.equals("DTP")||serviceType.equals("BOB"))){
            taxPercentage = SaveSharedPreference.getStringValues(this,
                    Constants.SHARED_PREFERENCE_TAX_PERCENTAGE);
            if(taxPercentage != null) {
                dueBalance = Float.parseFloat(subTotal) * ((100 + Float.parseFloat(taxPercentage)) / 100);
                balanceDueTextView.setText(POSCommonUtils.getTwoDecimalFloatFromFloat(dueBalance));
            }
            else{
                dueBalance = Float.parseFloat(subTotal);
                balanceDueTextView.setText(POSCommonUtils.getTwoDecimalFloatFromFloat(dueBalance));
            }
            TextView tax = findViewById(R.id.serviceTaxTextView);
            tax.setText(taxPercentage == null ? "0":taxPercentage + "%");
            TextView total = findViewById(R.id.totalTextView);
            total.setText(POSCommonUtils.getTwoDecimalFloatFromString(subTotal));
        }
        else {
            totalTextRow.setVisibility(View.GONE);
            taxTextRow.setVisibility(View.GONE);
            dueBalance = Float.parseFloat(subTotal);
            balanceDueTextView.setText(POSCommonUtils.getTwoDecimalFloatFromFloat(dueBalance));
        }
        subTotalAfterTax = String.valueOf(dueBalance);
        TextView discountText = findViewById(R.id.discountTextView);
        discountText.setText((discount == null || discount.isEmpty()) ? "0.00" :
                POSCommonUtils.getTwoDecimalFloatFromString(discount));
        creditCardList = new ArrayList<>();
        paymentMethodsMap = new HashMap<>();
        registerLayoutClickEvents();
    }

    private void registerLayoutClickEvents() {

        LinearLayout creditCardLayout = (LinearLayout) findViewById(R.id.creditCardPaymentLayout);
        creditCardLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addCreditCardSettlementDetails();
            }
        });

        ImageButton backButton = findViewById(R.id.backPressBtn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        cancelSaleBtn = (Button) findViewById(R.id.cancelSale);
        cancelSaleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new android.support.v7.app.AlertDialog.Builder(PreOrderPaymentsActivity.this)
                        .setTitle("Cancel Sale")
                        .setMessage("Are you sure you want to cancel the sale?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                Intent intent = new Intent(PreOrderPaymentsActivity.this, SellItemsActivity.class);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null).show();
            }
        });
    }

    private void showToastMsg(String msg){
        Toast.makeText(getApplicationContext(), msg,
                Toast.LENGTH_SHORT).show();
    }

    private void addCreditCardSettlementDetails(){

        dialog = new Dialog(this);
        dialog.setContentView(R.layout.credit_card_accept_layout);
        Window window = dialog.getWindow();
        window.setLayout(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        dialog.setTitle("Settle by Credit Card");

        cardNumber = (EditText) dialog.findViewById(R.id.cardNumber);
        cardHolderName = (EditText) dialog.findViewById(R.id.cardHolderNameText);
        expiryDate = (EditText) dialog.findViewById(R.id.expireDateField);
        cardType = (EditText) dialog.findViewById(R.id.cardTypeText);
        final EditText amount = (EditText) dialog.findViewById(R.id.amountText);
        amount.setText(String.valueOf(POSCommonUtils.getTwoDecimalFloatFromFloat(dueBalance)));

        Button okBtn = (Button) dialog.findViewById(R.id.cardSubmitBtn);
        Button cancelBtn = (Button) dialog.findViewById(R.id.cancelBtn);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(amount.getText() == null || amount.getText().toString().isEmpty()){
                    showToastMsg("Enter paid amount.");
                }
                else if(cardHolderName.getText() == null || cardHolderName.getText().toString().isEmpty() ||
                        cardNumber.getText() == null || cardNumber.getText().toString().isEmpty()){
                    showToastMsg("Please swipe a valid card.");
                }
                else{
                    CreditCard creditCard = new CreditCard();
                    creditCard.setCardHolderName(cardHolderName.getText().toString());
                    creditCard.setCreditCardNumber(cardNumber.getText().toString());
                    creditCard.setCreditCardType(cardType.getText().toString());
                    creditCard.setExpireDate(expiryDate.getText().toString());
                    creditCard.setPaidAmount(Float.parseFloat(amount.getText().toString()));
                    creditCardList.add(creditCard);
                    addPaymentMethodToTable("Credit Card", "USD", "1", amount.getText().toString(), amount.getText().toString());
                    closeMSR();
                    dialog.dismiss();
                }
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeMSR();
                dialog.dismiss();
            }
        });

        dialog.show();
        readMSR("credit");
    }

    public void setmsg(int i, byte[] out_data,boolean isCreditCard) {
        if(i == 1)
        {
            String track1Str = new String(out_data);
            if(isCreditCard) {
                String[] track1Details = track1Str.split("\\^");
                String expireDateStr = track1Details[2].substring(2, 4) + "/" + track1Details[2].substring(0, 2);
                if (!isExpired(expireDateStr)) {
                    cardNumber = (EditText) dialog.findViewById(R.id.cardNumber);
                    cardHolderName = (EditText) dialog.findViewById(R.id.cardHolderNameText);
                    expiryDate = (EditText) dialog.findViewById(R.id.expireDateField);
                    cardNumber.setText(track1Details[0].substring(1));
                    cardHolderName.setText(track1Details[1]);
                    expiryDate.setText(expireDateStr);
                    cardType.setText(POSCommonUtils.getCreditCardTypeFromFirstDigit(track1Details[0].substring(1, 2)));
                } else {
                    showToastMsg("Given credit card is expired.");
                    closeMSR();
                    readMSR("credit");
                }
            }
            else{
                String[] track1Details = track1Str.split("\\^");
                String expireDateStr = track1Details[2].substring(2, 4) + "/" + track1Details[2].substring(0, 2);
                cardNumber = (EditText) dialog.findViewById(R.id.cardNumber);
                cardHolderName = (EditText) dialog.findViewById(R.id.cardHolderNameText);
                cardNumber.setText(track1Details[0].substring(1));
                cardHolderName.setText(track1Details[1]);
                closeMSR();
            }
        }
    }

    private boolean isExpired(String expireStr){

        String[] str = expireStr.split("/");
        int month = Integer.parseInt(str[0]);
        int year = Integer.parseInt(str[1]);
        Date date = new Date();
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        String currentDateStr = df.format(date);
        String[] currentStr = currentDateStr.split("/");
        int currentMonth = Integer.parseInt(currentStr[1]);
        int currentYear = Integer.parseInt(currentStr[2].substring(2));
        if(currentYear < year){
            return false;
        }
        else if(currentYear == year){
            if(currentMonth <= month){
                return false;
            }
            else{
                return true;
            }
        }
        else {
            return true;
        }
    }

    private void closeMSR(){
        if(msr != null)
            msr.close();
    }

    private void readMSR(final String cardType){
        final Msr msr = new Msr();
        msr.open();
        final ProgressDialog dia = new ProgressDialog(this);
        dia.setTitle("MSR");
        dia.setMessage("please swipe MSR card...");
        dia.show();
        final Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub

                if(msg.what == 1){
                    dia.dismiss();
                    for(int i = 1; i < 4; i++)
                    {
                        if(msr.getTrackError(i) == 0)
                        {
                            //Log.i("123", "i:"+i);
                            byte[] out_data = new byte[msr.getTrackDataLength(i)];
                            msr.getTrackData(i, out_data);
                            setmsg(i,out_data,cardType.equals("credit"));
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

    private void addPaymentMethodToTable(String type, String currency, String rate, String amount, String USD){

        TableRow tr = new TableRow(this);
        tr.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));

        TableRow.LayoutParams cellParams = new TableRow.LayoutParams(0,
                TableRow.LayoutParams.WRAP_CONTENT, 1f);

        TextView typeText = new TextView(this);
        typeText.setText(type);
        typeText.setTextSize(15);
        typeText.setGravity(Gravity.CENTER);
        typeText.setLayoutParams(cellParams);
        tr.addView(typeText);

        TextView currencyTextView = new TextView(this);
        currencyTextView.setText(currency);
        currencyTextView.setTextSize(15);
        currencyTextView.setGravity(Gravity.CENTER);
        currencyTextView.setLayoutParams(cellParams);
        tr.addView(currencyTextView);

        TextView exchangeRate = new TextView(this);
        exchangeRate.setText(rate);
        exchangeRate.setTextSize(15);
        exchangeRate.setGravity(Gravity.CENTER);
        exchangeRate.setLayoutParams(cellParams);
        tr.addView(exchangeRate);

        TextView value = new TextView(this);
        String amountText = POSCommonUtils.getTwoDecimalFloatFromFloat(Float.valueOf(amount.replace(",","")));
        value.setText(amountText);
        value.setTextSize(15);
        value.setGravity(Gravity.CENTER);
        value.setLayoutParams(cellParams);
        tr.addView(value);

        TextView usdVal = new TextView(this);
        usdVal.setText(POSCommonUtils.getTwoDecimalFloatFromFloat(Float.valueOf(USD)));
        usdVal.setTextSize(15);
        usdVal.setGravity(Gravity.CENTER);
        usdVal.setLayoutParams(cellParams);
        tr.addView(usdVal);

        dueBalance -= Float.parseFloat(USD);
        String dueBalanceStr = POSCommonUtils.getTwoDecimalFloatFromFloat(dueBalance);
        if(dueBalanceStr.equals("-0.00")){
            dueBalanceStr = "0.00";
        }
        balanceDueTextView.setText(dueBalanceStr);
        paymentMethodsCount++;
        paymentMethodsMap.put(type+" "+currency,amountText);
        paymentTable.addView(tr,paymentMethodsCount);
    }

    private void updateSale(){
        Date date = new Date();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentDateStr = df.format(date);
        AcceptPreOrder acceptPreOrder = new AcceptPreOrder();
        acceptPreOrder.setOrderNumber(orderNumber);
        acceptPreOrder.setPaxName(paxName);
        acceptPreOrder.setFlightNumber(flightName);
        acceptPreOrder.setFlightDate(flightDate);
        acceptPreOrder.setFlightSector(flightSector);
        handler.insertAcceptPreOrders(soldItems,acceptPreOrder);
        for(Map.Entry<String,String> entry : paymentMethodsMap.entrySet()){
            handler.insertPaymentMethods(orderNumber,entry.getKey(),entry.getValue());
        }
        if(creditCardList != null && !creditCardList.isEmpty()){
            CreditCard creditCard = creditCardList.get(0);
            handler.insertCreditCardDetails(orderNumber,creditCard.getCreditCardNumber(),creditCard.getCardHolderName(),
                    creditCard.getExpireDate(),paymentMethodsMap.get("Credit Card USD"));
        }
    }

    private void printReceipt(){

        if(confirmOrderBtn.getText().equals("Print Card Holder copy")){
            PrintJob.printOrderDetails(PreOrderPaymentsActivity.this,orderNumber,
                    paxName,soldItems,paymentMethodsMap,
                    creditCardList.isEmpty() ? null : creditCardList.get(0),true,discount,taxPercentage);
            Intent intent = new Intent(PreOrderPaymentsActivity.this, SellItemsActivity.class);
            startActivity(intent);
        }
        else{
            generateOrderNumber();
            updateSale();
            PrintJob.printOrderDetails(this,orderNumber,paxName,soldItems,paymentMethodsMap,
                    creditCardList.isEmpty() ? null : creditCardList.get(0),false,discount,taxPercentage);
            if(!creditCardList.isEmpty()) {
                confirmOrderBtn.setText("Print Card Holder copy");
                cancelSaleBtn.setClickable(false);

            }
            else {
                Intent intent = new Intent(PreOrderPaymentsActivity.this, SellItemsActivity.class);
                startActivity(intent);
            }
        }
    }

    private void generateOrderNumber(){
        orderNumber = SaveSharedPreference.getStringValues(this,"orderNumber");
        if(orderNumber != null){
            int newVal = Integer.parseInt(orderNumber) + 1;
            orderNumber = String.valueOf(newVal);
            SaveSharedPreference.updateValue(this,"orderNumber",orderNumber);
        }
        else{
            SaveSharedPreference.setStringValues(this,"orderNumber","1");
            orderNumber = "1";
        }
    }
}