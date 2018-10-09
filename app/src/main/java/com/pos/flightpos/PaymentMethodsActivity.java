package com.pos.flightpos;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.pt.msr.Msr;
import android.pt.printer.Printer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.pos.flightpos.objects.Constants;
import com.pos.flightpos.objects.CreditCard;
import com.pos.flightpos.objects.SoldItem;
import com.pos.flightpos.objects.XMLMapper.Currency;
import com.pos.flightpos.utils.POSCommonUtils;
import com.pos.flightpos.utils.POSDBHandler;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PaymentMethodsActivity extends AppCompatActivity {

    private float dueBalance = 0;
    private ArrayList<SoldItem> soldItems;
    TableLayout paymentTable;
    TextView balanceDueTextView;
    Button printReceiptBtn;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_methods);

        paymentTable = (TableLayout) findViewById(R.id.paymentMethodTable);
        printReceiptBtn = (Button) findViewById(R.id.printReceipt);
        balanceDueTextView = (TextView)  findViewById(R.id.balanceDueTextView);
        printReceiptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                printReceipt();
            }
        });
        Intent intent = getIntent();
        Bundle args = intent.getBundleExtra("BUNDLE");
        soldItems = (ArrayList<SoldItem>) args.getSerializable("soldItemList");
        String subTotal = intent.getExtras().get("subTotal").toString();
        seatNumber = intent.getExtras().get("SeatNumber").toString();
        orderNumber = intent.getExtras().get("orderNumber").toString();
        dueBalance = Float.parseFloat(subTotal);
        balanceDueTextView.setText(String.valueOf(dueBalance));
        creditCardList = new ArrayList<>();
        paymentMethodsMap = new HashMap<>();
        registerLayoutClickEvents();
    }

    private void registerLayoutClickEvents() {

        LinearLayout cashSettleLayout = (LinearLayout) findViewById(R.id.cashSettleLayout);
        cashSettleLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addCashSettlement();
            }
        });
        LinearLayout creditCardLayout = (LinearLayout) findViewById(R.id.creditCardPaymentLayout);
        creditCardLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addCreditCardSettlementDetails();
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
        readMSR();
    }

    public void setmsg(int i, byte[] out_data) {
        if(i == 1)
        {
            String track1Str = new String(out_data);
            String[] track1Details = track1Str.split("\\^");
            String expireDateStr =track1Details[2].substring(2,4) + "/" +  track1Details[2].substring(0,2);
            if(!isExpired(expireDateStr)) {
                cardNumber = (EditText) dialog.findViewById(R.id.cardNumber);
                cardHolderName = (EditText) dialog.findViewById(R.id.cardHolderNameText);
                expiryDate = (EditText) dialog.findViewById(R.id.expireDateField);
                cardNumber.setText(track1Details[0]);
                cardHolderName.setText(track1Details[1]);
                expiryDate.setText(expireDateStr);
                cardType.setText(POSCommonUtils.getCreditCardTypeFromFirstDigit(track1Details[0].substring(0, 1)));
            }
            else{
                showToastMsg("Given credit card is expired.");
                closeMSR();
                readMSR();
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
            return false;
        }
    }

    private void closeMSR(){
        if(msr != null)
        msr.close();
    }

    private void readMSR(){
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
                            setmsg(i,out_data);
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

    private void addCashSettlement(){

        final Dialog cashSettleDialog = new Dialog(this);
        cashSettleDialog.setContentView(R.layout.cash_settle_layout);
        Window window = cashSettleDialog.getWindow();
        window.setLayout(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        cashSettleDialog.setTitle("Settle by Cash");

        final EditText amount = (EditText) cashSettleDialog.findViewById(R.id.cashSettleAmountTextField);
        final Spinner currency = (Spinner) cashSettleDialog.findViewById(R.id.currencyText);
        final TextView errorMsgText = (TextView)  cashSettleDialog.findViewById(R.id.errorMsgText);
        final TextView initialAmount = (TextView)  cashSettleDialog.findViewById(R.id.initialAmount);
        currency.setAdapter(loadCurrencies());
        currency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                amount.setText(updateAmountBasedOnCurrency((Currency)currency.getSelectedItem(),
                        initialAmount.getText().toString()));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        amount.setText(String.valueOf(POSCommonUtils.getTwoDecimalFloatFromFloat(dueBalance)));
        initialAmount.setText(String.valueOf(POSCommonUtils.getTwoDecimalFloatFromFloat(dueBalance)));

        Button okBtn = (Button) cashSettleDialog.findViewById(R.id.cardSubmitBtn);
        Button cancelBtn = (Button) cashSettleDialog.findViewById(R.id.cancelBtn);

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                errorMsgText.setText("");
                if(amount.getText() == null || amount.getText().toString().isEmpty()){
                    errorMsgText.setText("Please Enter amount.");
                }
                else if(currency.getSelectedItem() == null || currency.getSelectedItem().toString().isEmpty()){
                    errorMsgText.setText("Please Enter currency.");
                }
                else {
                    Currency selectedCurrency = (Currency)currency.getSelectedItem();
                    addPaymentMethodToTable("Cash",currency.getSelectedItem().toString(),
                            selectedCurrency.getCurrencyRate()
                            ,amount.getText().toString(), getAmountInUSD(selectedCurrency,amount.getText().toString()));
                    cashSettleDialog.dismiss();
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

    private String getAmountInUSD(Currency currency, String amount){
        amount.replace(",","");
        return POSCommonUtils.getTwoDecimalFloatFromFloat(Float.parseFloat(amount) /
                Float.parseFloat(currency.getCurrencyRate()));
    }

    private String updateAmountBasedOnCurrency(Currency currency,String currentAmount){
        currentAmount.replace(",","");
        return POSCommonUtils.getTwoDecimalFloatFromFloat(Float.parseFloat(currentAmount) *
                Float.parseFloat(currency.getCurrencyRate()));
    }
    private ArrayAdapter<Currency> loadCurrencies(){
        POSDBHandler handler = new POSDBHandler(this);
        List<Currency> options=new ArrayList<>();
        List<Currency> equipmentList = handler.getCurrencyList();
        options.addAll(equipmentList);
        return new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,options);
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
        typeText.setTextSize(20);
        typeText.setGravity(Gravity.CENTER);
        typeText.setLayoutParams(cellParams);
        tr.addView(typeText);

        TextView currencyTextView = new TextView(this);
        currencyTextView.setText(currency);
        currencyTextView.setTextSize(20);
        currencyTextView.setGravity(Gravity.CENTER);
        currencyTextView.setLayoutParams(cellParams);
        tr.addView(currencyTextView);

        TextView exchangeRate = new TextView(this);
        exchangeRate.setText(rate);
        exchangeRate.setTextSize(20);
        exchangeRate.setGravity(Gravity.CENTER);
        exchangeRate.setLayoutParams(cellParams);
        tr.addView(exchangeRate);

        TextView value = new TextView(this);
        value.setText(POSCommonUtils.getTwoDecimalFloatFromFloat(Float.valueOf(amount.replace(",",""))));
        value.setTextSize(20);
        value.setGravity(Gravity.CENTER);
        value.setLayoutParams(cellParams);
        tr.addView(value);

        TextView usdVal = new TextView(this);
        usdVal.setText(POSCommonUtils.getTwoDecimalFloatFromFloat(Float.valueOf(USD)));
        usdVal.setTextSize(20);
        usdVal.setGravity(Gravity.CENTER);
        usdVal.setLayoutParams(cellParams);
        tr.addView(usdVal);

        dueBalance -= Float.parseFloat(USD);
        balanceDueTextView.setText(POSCommonUtils.getTwoDecimalFloatFromFloat(dueBalance));
        paymentMethodsCount++;
        paymentMethodsMap.put(type,amount);
        paymentTable.addView(tr,paymentMethodsCount);
    }

    private void printReceipt(){
        Float dispDueBalance = Float.parseFloat(POSCommonUtils.getTwoDecimalFloatFromFloat(dueBalance));
        if(dispDueBalance <= 0){
            Printer printer = new Printer();
            printer.open();
            int printerStatus = printer.queState();
            if(printerStatus == 1){
                Toast.makeText(this, "Paper is not available. Please insert some papers.",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            else if(printerStatus == 2){
                Toast.makeText(this, "Printer is too hot. Please wait.",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, "Printing started. Please wait.",
                    Toast.LENGTH_SHORT).show();
            printer.init();
            printer.setAlignment(1);
            printer.printPictureByRelativePath(Constants.PRINTER_LOGO_LOCATION, 150, 150);
            printer.printString("");
            printer.setBold(true);
            printer.printString("CMB123 CMB-KUL");
            Date date = new Date();
            DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
            DateFormat dateTimeFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss aa");
            printer.printString(df.format(date));
            printer.printString(" ");
            printer.printString(" ");
            printer.printString("Sale transaction");
            printer.setAlignment(0);
            printer.printString("Order Number : " + orderNumber);
            printer.printString("Seat Number : " + seatNumber);
            float total = 0;
            for(SoldItem item : soldItems){
                total += Float.parseFloat(item.getPrice())* Integer.parseInt(item.getQuantity());
                int itemNameLength = item.getItemDesc().length();
                String totalAmount = POSCommonUtils.getTwoDecimalFloatFromFloat(
                        Float.parseFloat(item.getPrice())*Integer.parseInt(item.getQuantity()));
                int totalAmountLength = totalAmount.length();
                int spaceLength = 32 - (itemNameLength+totalAmountLength);
                printer.printString(item.getItemDesc()
                        + new String(new char[spaceLength]).replace("\0", " ") + totalAmount);
                printer.printString(item.getItemId() + " Each $" + item.getPrice() );
            }
            printer.printString(" ");
            printer.setAlignment(2);
            printer.printString("Total USD " + POSCommonUtils.getTwoDecimalFloatFromFloat(total));
            printer.setBold(false);
            printer.setAlignment(0);
            Iterator it = paymentMethodsMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();
                printer.printString(pair.getKey() + " USD " + pair.getValue());
            }
            if(paymentMethodsMap.get("Credit Card") != null){
                CreditCard card = creditCardList.get(0);
                int numOfDigits = card.getCreditCardNumber().length();
                printer.printString(new String(new char[numOfDigits-4]).replace("\0", "*")
                        + card.getCreditCardNumber().substring(numOfDigits-4,numOfDigits));
                printer.printString(card.getExpireDate());
                printer.printString(card.getCardHolderName());
                printer.printString(" ");
                printer.printString(" ");
                printer.printString(".......................");
                printer.printString("Card Holder Signature");
                printer.printString("I agree to pay above total");
                printer.printString("amount according to card issuer");
                printer.printString("agreement");
                printer.printString(" ");
                printer.printString("Merchant Copy");
            }
            printer.printString(" ");
            printer.printString(" ");
            printer.setBold(true);
            printer.printString("Operated Staff");
            printer.printString("User 1");
            printer.printString(dateTimeFormat.format(date));
            printer.printString(" ");
            printer.printString(" ");
            printer.close();

            Intent intent = new Intent(this,SellItemsActivity.class);
            startActivity(intent);
        }
        else{
            showToastMsg("Due balance should be zero");
        }
    }
}
