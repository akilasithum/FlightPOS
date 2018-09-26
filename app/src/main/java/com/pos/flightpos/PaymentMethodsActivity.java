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
import android.text.InputType;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.pos.flightpos.objects.CreditCard;
import com.pos.flightpos.objects.SoldItem;
import com.pos.flightpos.utils.POSCommonUtils;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PaymentMethodsActivity extends AppCompatActivity {

    Button addPaymentMethodBtn;
    Spinner paymentMethodSpinner;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_methods);

        paymentMethodSpinner = (Spinner) findViewById(R.id.paymentMethodSpinner);
        addPaymentMethodBtn = (Button) findViewById(R.id.addPaymentMethodBtn);
        paymentTable = (TableLayout) findViewById(R.id.paymentMethodTable);
        printReceiptBtn = (Button) findViewById(R.id.printReceipt);
        balanceDueTextView = (TextView)  findViewById(R.id.balanceDueTextView);

        addPaymentMethodBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPurchaseItem();
            }
        });
        printReceiptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                printReceipt();
            }
        });
        populatePaymentMethodField();
        Intent intent = getIntent();
        Bundle args = intent.getBundleExtra("BUNDLE");
        soldItems = (ArrayList<SoldItem>) args.getSerializable("soldItemList");
        String subTotal = intent.getExtras().get("subTotal").toString();
        seatNumber = intent.getExtras().get("SeatNumber").toString();
        dueBalance = Float.parseFloat(subTotal);
        balanceDueTextView.setText(String.valueOf(dueBalance));
        creditCardList = new ArrayList<>();
        paymentMethodsMap = new HashMap<>();
    }

    private void addPurchaseItem() {

        String paymentMethod = paymentMethodSpinner.getSelectedItem() == null ? null : paymentMethodSpinner.getSelectedItem().toString();

        if (paymentMethod == null || paymentMethod.equals("")) {
            showToastMsg("Please choose a payment method.");
            return;
        }
        if(paymentMethod.equals("Cash")){
            addCashSettlement();
        }
        else if(paymentMethod.equals("Credit Card")){
            addCreditCardSettlementDetails();
        }
        paymentMethodSpinner.setSelection(0);
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
                else {
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
            cardNumber = (EditText) dialog.findViewById(R.id.cardNumber);
            cardHolderName = (EditText) dialog.findViewById(R.id.cardHolderNameText);
            expiryDate = (EditText) dialog.findViewById(R.id.expireDateField);
            cardNumber.setText(track1Details[0]);
            cardHolderName.setText(track1Details[1]);
            String expireDateStr =track1Details[2].substring(2,4) + "/" +  track1Details[2].substring(0,2);
            expiryDate.setText(expireDateStr);
            cardType.setText(POSCommonUtils.getCreditCardTypeFromFirstDigit(track1Details[0].substring(0,1)));
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
        final EditText currency = (EditText) cashSettleDialog.findViewById(R.id.currencyText);
        final TextView errorMsgText = (TextView)  cashSettleDialog.findViewById(R.id.errorMsgText);

        amount.setText(String.valueOf(POSCommonUtils.getTwoDecimalFloatFromFloat(dueBalance)));

        Button okBtn = (Button) cashSettleDialog.findViewById(R.id.cardSubmitBtn);
        Button cancelBtn = (Button) cashSettleDialog.findViewById(R.id.cancelBtn);

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                errorMsgText.setText("");
                if(amount.getText() == null || amount.getText().toString().isEmpty()){
                    errorMsgText.setText("Please Enter amount.");
                }
                else if(currency.getText() == null || currency.getText().toString().isEmpty()){
                    errorMsgText.setText("Please Enter currency.");
                }
                else {
                    addPaymentMethodToTable("Cash",currency.getText().toString(),"1"
                            ,amount.getText().toString(),amount.getText().toString());
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

    private void addPaymentMethodToTable(String type, String currency, String rate, String amount, String USD){

        TableRow tr = new TableRow(this);
        tr.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));

        TableRow.LayoutParams cellParams = new TableRow.LayoutParams(0,
                TableRow.LayoutParams.WRAP_CONTENT, 1f);

        TextView itemDesc = new TextView(this);
        itemDesc.setText(type);
        itemDesc.setTextSize(20);
        itemDesc.setLayoutParams(cellParams);
        tr.addView(itemDesc);

        TextView currencyTextView = new TextView(this);
        currencyTextView.setText(currency);
        currencyTextView.setTextSize(20);
        currencyTextView.setLayoutParams(cellParams);
        tr.addView(currencyTextView);

        TextView exchangeRate = new TextView(this);
        exchangeRate.setText(rate);
        exchangeRate.setTextSize(20);
        exchangeRate.setLayoutParams(cellParams);
        tr.addView(exchangeRate);

        TextView value = new TextView(this);
        value.setText(POSCommonUtils.getTwoDecimalFloatFromFloat(Float.valueOf(amount)));
        value.setTextSize(20);
        value.setLayoutParams(cellParams);
        tr.addView(value);

        TextView usdVal = new TextView(this);
        usdVal.setText(POSCommonUtils.getTwoDecimalFloatFromFloat(Float.valueOf(USD)));
        usdVal.setTextSize(20);
        usdVal.setLayoutParams(cellParams);
        tr.addView(usdVal);

        dueBalance -= Float.parseFloat(amount);
        balanceDueTextView.setText(String.valueOf(dueBalance));
        paymentMethodsCount++;
        paymentMethodsMap.put(type,amount);
        paymentTable.addView(tr,paymentMethodsCount);
    }

    private void populatePaymentMethodField(){

        ArrayList<String> options=new ArrayList<String>();
        options.add("");
        options.add("Cash");
        options.add("Credit Card");
        options.add("Voucher");
        options.add("Loyalty");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,options);
        paymentMethodSpinner.setAdapter(adapter);
    }

    private void printReceipt(){
        if(dueBalance <= 0){
            Printer printer = new Printer();
            printer.open();
            printer.init();
            printer.setAlignment(1);
            printer.printPictureByRelativePath("/res/drawable/no_back.jpg", 150, 150);
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
            printer.printString("Seat Number : " + seatNumber);
            float total = 0;
            for(SoldItem item : soldItems){
                total += item.getPrice()*item.getQuantity();
                int itemNameLength = item.getItemDesc().length();
                String totalAmount = POSCommonUtils.getTwoDecimalFloatFromFloat(item.getPrice()*item.getQuantity());
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
        }
        else{
            showToastMsg("Due balance should be zero");
        }
    }
}
