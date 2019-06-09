package com.pos.flightpos;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.pt.msr.Msr;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.pos.flightpos.objects.Constants;
import com.pos.flightpos.objects.CreditCard;
import com.pos.flightpos.objects.Flight;
import com.pos.flightpos.objects.LoyaltyCard;
import com.pos.flightpos.objects.SoldItem;
import com.pos.flightpos.objects.XMLMapper.Currency;
import com.pos.flightpos.objects.XMLMapper.Sector;
import com.pos.flightpos.objects.XMLMapper.Voucher;
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

public class PaymentMethodsActivity extends AppCompatActivity {

    private float dueBalance = 0;
    private ArrayList<SoldItem> soldItems;
    TableLayout paymentTable;
    TextView balanceDueTextView;
    Button confirmPaymentBtn;
    int paymentMethodsCount = 0;
    private Msr msr = null;
    private Dialog dialog = null;
    EditText cardNumber;
    EditText cardHolderName;
    EditText expiryDate;
    EditText cardType;
    List<CreditCard> creditCardList;
    LoyaltyCard loyaltyCard;
    Map<String,String> paymentMethodsMap;
    String orderNumber;
    POSDBHandler handler;
    String discount;
    String taxPercentage;
    String subTotalAfterTax;
    Float totalBeforeTax;
    TextView discountText;
    Button cancelSaleBtn;
    float discountFromVoucher = 0;
    TextView subTotalTextView;
    String category;
    LinearLayout contentLayout;
    final AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.8F);
    List<Currency> availableCurrencies;
    Currency usdCurrency;
    String seatNumber;
    String paxName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_methods);
        handler = new POSDBHandler(this);
        paymentTable = findViewById(R.id.paymentMethodTable);
        confirmPaymentBtn = findViewById(R.id.printReceipt);
        balanceDueTextView = findViewById(R.id.balanceDueTextView);
        subTotalTextView = findViewById(R.id.subTotalTextView);
        confirmPaymentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);
                if(confirmPaymentBtn.getText().equals("Confirm Payment")) {
                    Float dispDueBalance = Float.parseFloat(POSCommonUtils.getTwoDecimalFloatFromFloat(dueBalance));
                    if (dispDueBalance <= 0.1) {
                        new android.support.v7.app.AlertDialog.Builder(PaymentMethodsActivity.this)
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
        discount = intent.getExtras().get("discount").toString();
        category = intent.getExtras().get("category").toString();
        String serviceType = POSCommonUtils.getServiceType(this);
        TableRow totalTextRow = findViewById(R.id.totalTextRow);
        TableRow taxTextRow = findViewById(R.id.serviceTaxRow);
        TextView initialTotalText = findViewById(R.id.initialTotalTextView);
        totalBeforeTax = Float.parseFloat(subTotal);
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
        initialTotalText.setText(POSCommonUtils.getTwoDecimalFloatFromFloat(dueBalance));
        subTotalAfterTax = String.valueOf(dueBalance);
        subTotalTextView.setText(POSCommonUtils.getTwoDecimalFloatFromFloat(dueBalance));
        discountText = findViewById(R.id.discountTextView);
        discountText.setText((discount == null || discount.isEmpty()) ? "0.00" :
                POSCommonUtils.getTwoDecimalFloatFromString(discount));
        creditCardList = new ArrayList<>();
        paymentMethodsMap = new HashMap<>();
        contentLayout = findViewById(R.id.contentLayout);
        registerLayoutClickEvents();
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        availableCurrencies = handler.getCurrencyList();
        for(Currency currency : availableCurrencies){
            if(currency.getCurrencyCode().equals("USD")) usdCurrency = currency;
        }
    }

    private void registerLayoutClickEvents() {
        LinearLayout cashSettleLayout = (LinearLayout) findViewById(R.id.cashSettleLayout);
        cashSettleLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(buttonClick);
                addCashSettlement();
            }
        });
        LinearLayout creditCardLayout = (LinearLayout) findViewById(R.id.creditCardPaymentLayout);
        creditCardLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(buttonClick);
                addCreditCardSettlementDetails();
            }
        });
        LinearLayout voucherLayout = (LinearLayout) findViewById(R.id.voucherPaymentLayout);
        voucherLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(buttonClick);
                showVoucherDetails();
            }
        });
        ImageButton backButton = findViewById(R.id.backPressBtn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        cancelSaleBtn =  findViewById(R.id.cancelSale);
        cancelSaleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(buttonClick);
                new android.support.v7.app.AlertDialog.Builder(PaymentMethodsActivity.this)
                        .setTitle("Cancel Sale")
                        .setMessage("Are you sure you want to cancel the sale?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                Intent intent = new Intent(PaymentMethodsActivity.this, GateUserMainActivity.class);
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

    private void showVoucherDetails(){
        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.voucher_layout);
        Window window = dialog.getWindow();
        window.setLayout(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        dialog.setTitle("Vouchers");
        dialog.setCanceledOnTouchOutside(false);
        final EditText voucherDiscountText = dialog.findViewById(R.id.discountText);
        final Spinner sectorSelectionSpinner = dialog.findViewById(R.id.voucherTypeSpinner);
        sectorSelectionSpinner.setAdapter(getVoucherTypes());
        sectorSelectionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                voucherDiscountText.setEnabled(true);
                voucherDiscountText.setText(getDiscountFromVoucher((Voucher) sectorSelectionSpinner.getSelectedItem()));
                voucherDiscountText.setEnabled(false);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        Button okBtn =  dialog.findViewById(R.id.voucherOkBtn);
        Button cancelBtn =  dialog.findViewById(R.id.cancelBtn);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);
                if (voucherDiscountText.getText() != null && voucherDiscountText.getText().toString() != null &&
                        !voucherDiscountText.getText().toString().equals("0.0")) {
                    if (dueBalance == totalBeforeTax || discountFromVoucher != 0) {
                        if(discountFromVoucher != 0){
                            dueBalance = totalBeforeTax;
                        }
                        dueBalance -= Float.parseFloat(voucherDiscountText.getText().toString());
                        balanceDueTextView.setText(POSCommonUtils.getTwoDecimalFloatFromFloat(dueBalance));
                        subTotalTextView.setText(POSCommonUtils.getTwoDecimalFloatFromFloat(dueBalance));
                        discount = POSCommonUtils.getTwoDecimalFloatFromString(voucherDiscountText.getText().toString());
                        discountFromVoucher = Float.parseFloat(discount);
                        discountText.setText(discount);
                        dialog.dismiss();
                    }
                }
                else{
                    TextView errText = dialog.findViewById(R.id.voucherErrorText);
                    errText.setText("Discount is zero or not applied.");
                }
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private String getDiscountFromVoucher(Voucher voucher){
        if(voucher.toString() != null) {
            if (voucher.getVoucherType().equalsIgnoreCase("Percentage")) {
                return String.valueOf(dueBalance * (Float.parseFloat(voucher.getDiscount())) / 100);
            } else {
                String discountStr = voucher.getDiscount();
                Map<Float, Float> ratioMap = new HashMap<>();
                if (discountStr.contains(",")) {
                    String[] rationStr = discountStr.split(",");
                    for (int i = 0; i < rationStr.length; i++) {
                        String[] ratioArr = rationStr[i].split("\\+");
                        ratioMap.put(Float.parseFloat(ratioArr[0]), Float.parseFloat(ratioArr[1]));
                    }
                } else {
                    String[] ratioArr = discountStr.split("\\+");
                    ratioMap.put(Float.parseFloat(ratioArr[0]), Float.parseFloat(ratioArr[1]));
                }
                float discountVal = 0;
                for (Map.Entry<Float, Float> discountMap : ratioMap.entrySet()) {
                    if (discountMap.getKey() < totalBeforeTax) {
                        discountVal = discountMap.getValue();
                    }
                }
                return String.valueOf(discountVal);
            }
        }
        return "";
    }

    private ArrayAdapter<Voucher> getVoucherTypes(){
        List<Voucher> options = new ArrayList<>();
        Voucher voucher = new Voucher();
        options.add(voucher);
        options.addAll(handler.getVouchers());
        ArrayAdapter<Voucher> adapter = new ArrayAdapter<Voucher>(this,android.R.layout.simple_spinner_item,options);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        return adapter;
    }

    private void addLoyaltyCardDetails(){

        dialog = new Dialog(this);
        dialog.setContentView(R.layout.loyalty_card_details_layout);
        Window window = dialog.getWindow();
        window.setLayout(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        dialog.setTitle("Loyalty Card");

        cardNumber = (EditText) dialog.findViewById(R.id.cardNumber);
        cardHolderName = (EditText) dialog.findViewById(R.id.cardHolderNameText);
        final EditText amount = (EditText) dialog.findViewById(R.id.amountText);
        amount.setText(String.valueOf(POSCommonUtils.getTwoDecimalFloatFromString(subTotalAfterTax)));
        amount.setEnabled(false);

        Button okBtn = (Button) dialog.findViewById(R.id.cardSubmitBtn);
        Button cancelBtn = (Button) dialog.findViewById(R.id.cancelBtn);
        Button swipeCardBtn = (Button) dialog.findViewById(R.id.swipeCard);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cardHolderName.getText() == null || cardHolderName.getText().toString().isEmpty() ||
                        cardNumber.getText() == null || cardNumber.getText().toString().isEmpty()){
                    showToastMsg("Enter card details.");
                }
                else{
                    loyaltyCard = new LoyaltyCard();
                    loyaltyCard.setCardHolderName(cardHolderName.getText().toString());
                    loyaltyCard.setLoyaltyCardNumber(cardNumber.getText().toString());
                    loyaltyCard.setAmount(Float.parseFloat(amount.getText().toString()));
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

        swipeCardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readMSR("loyalty");
            }
        });

        dialog.show();
    }

    private void addCreditCardSettlementDetails(){

        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.credit_card_accept_layout);
        Window window = dialog.getWindow();
        window.setLayout(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        dialog.setTitle("Settle by Credit Card");
        dialog.setCanceledOnTouchOutside(false);

        cardNumber = dialog.findViewById(R.id.cardNumber);
        cardHolderName =  dialog.findViewById(R.id.cardHolderNameText);
        expiryDate =  dialog.findViewById(R.id.expireDateField);
        cardType =  dialog.findViewById(R.id.cardTypeText);
        final EditText amount = dialog.findViewById(R.id.amountText);
        amount.setText(String.valueOf(dueBalance));

        Button okBtn = (Button) dialog.findViewById(R.id.cardSubmitBtn);
        final Button cancelBtn = (Button) dialog.findViewById(R.id.cancelBtn);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);
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
                    addPaymentMethodToTable("Credit Card", "CAD", "1", amount.getText().toString(), amount.getText().toString());
                    closeMSR();
                    dialog.dismiss();
                    cancelSaleBtn.setVisibility(View.GONE);
                }
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);
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
                    cardNumber =  dialog.findViewById(R.id.cardNumber);
                    cardHolderName =  dialog.findViewById(R.id.cardHolderNameText);
                    expiryDate =  dialog.findViewById(R.id.expireDateField);
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
        dia.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                msr.close();
            }
        });
        final Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub

                if(msg.what == 1){
                    dia.cancel();
                    for(int i = 1; i < 4; i++)
                    {
                        if(msr.getTrackError(i) == 0)
                        {
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

    private void addCashSettlement(){

        final Dialog cashSettleDialog = new Dialog(this);
        cashSettleDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        cashSettleDialog.setContentView(R.layout.cash_settle_style_layout);
        Window window = cashSettleDialog.getWindow();
        window.setLayout(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        cashSettleDialog.setCanceledOnTouchOutside(false);

        final EditText amount = cashSettleDialog.findViewById(R.id.cashSettleAmountTextField);
        final Spinner currency = cashSettleDialog.findViewById(R.id.currencyText);
        final TextView errorMsgText = cashSettleDialog.findViewById(R.id.errorMsgText);
        final TextView initialAmount = cashSettleDialog.findViewById(R.id.initialAmount);
        currency.setAdapter(loadCurrencies());
        handler = new POSDBHandler(this);;
        currency.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,availableCurrencies));
        Currency baseCurrency = null;
        for(Currency currency1 : availableCurrencies){
            if(currency1.getCurrencyCode().equals(getBaseCurrency())) baseCurrency = currency1;
        }
        currency.setSelection(availableCurrencies.indexOf(baseCurrency));
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

        amount.setText(POSCommonUtils.getTwoDecimalFloatFromFloat(dueBalance));
        initialAmount.setText(POSCommonUtils.getTwoDecimalFloatFromFloat(dueBalance));

        Button okBtn =  cashSettleDialog.findViewById(R.id.cardSubmitBtn);
        Button cancelBtn =  cashSettleDialog.findViewById(R.id.cancelBtn);

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);
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
                v.startAnimation(buttonClick);
                cashSettleDialog.dismiss();
            }
        });
        cashSettleDialog.show();
    }

    private String getBaseCurrency(){
        return "CAD";
    }

    private String getAmountInUSD(Currency currency, String amount){
        amount.replace(",","");
        return POSCommonUtils.getTwoDecimalFloatFromFloat(Float.parseFloat(amount) /
                Float.parseFloat(currency.getCurrencyRate()));
    }

    private String updateAmountBasedOnCurrency(Currency currency,String currentAmount){
        currentAmount = currentAmount.replace(",","");
        return POSCommonUtils.getTwoDecimalFloatFromFloat(Float.parseFloat(currentAmount) *
                Float.parseFloat(currency.getCurrencyRate()));
    }
    private ArrayAdapter<Currency> loadCurrencies(){
        handler = new POSDBHandler(this);
        List<Currency> options=new ArrayList<>();
        options.indexOf("CAD");
        List<Currency> equipmentList = handler.getCurrencyList();
        options.addAll(equipmentList);
        return new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,options);
    }

    private void addPaymentMethodToTable(final String type, final String currency, String rate, String amount,final String USD){

        final FrameLayout frameLayout = new FrameLayout(this);
        LinearLayout.LayoutParams frameLayoutParams = new LinearLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,1);
        frameLayoutParams.setMargins(8,0,0,8);
        frameLayout.setLayoutParams(frameLayoutParams);

        ImageView closeBtn = new ImageView(this);
        closeBtn.setClickable(true);
        FrameLayout.LayoutParams closeBtnParam = new FrameLayout.LayoutParams(45, 45);
        closeBtnParam.gravity = Gravity.TOP|Gravity.RIGHT;
        closeBtn.setBackground(getResources().getDrawable(R.drawable.icon_cancel));
        closeBtn.setLayoutParams(closeBtnParam);

        LinearLayout linearLayout = new LinearLayout(this);
        FrameLayout.LayoutParams linearLayoutParams = new FrameLayout.LayoutParams(700,
                TableRow.LayoutParams.WRAP_CONTENT);
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setBackgroundColor(getResources().getColor(R.color.sellitembg));
        linearLayoutParams.setMargins(0,23,23,0);
        linearLayout.setLayoutParams(linearLayoutParams);

        TableRow tr = new TableRow(this);
        TableRow tr1 = new TableRow(this);

        tr.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.FILL_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));
        tr.setPadding(0,10,0,0);
        tr1.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.FILL_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));
        tr1.setPadding(0,0,0,10);


        TableRow.LayoutParams cellParams1 = new TableRow.LayoutParams(0,
                TableRow.LayoutParams.WRAP_CONTENT, 6f);
        TableRow.LayoutParams cellParams2 = new TableRow.LayoutParams(0,
                TableRow.LayoutParams.WRAP_CONTENT, 3f);
        TableRow.LayoutParams cellParams3 = new TableRow.LayoutParams(0,
                35, 1f);

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(PaymentMethodsActivity.this)
                        .setTitle("Remove selection")
                        .setMessage("Do you want to remove this item from selection?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                dueBalance += Float.parseFloat(USD);
                                String dueBalanceStr = POSCommonUtils.getTwoDecimalFloatFromFloat(dueBalance);

                                balanceDueTextView.setText(dueBalanceStr);
                                paymentMethodsCount++;
                                paymentMethodsMap.remove(type+" "+currency);
                                contentLayout.removeView(frameLayout);
                            }})
                        .setNegativeButton(android.R.string.no, null).show();
            }
        });
        TextView typeText = new TextView(this);
        typeText.setText(type);
        typeText.setTextSize(20);
        typeText.setGravity(Gravity.CENTER);
        typeText.setLayoutParams(cellParams3);

        TextView currencyTextView = new TextView(this);
        currencyTextView.setText(currency);
        currencyTextView.setTextSize(20);
        currencyTextView.setGravity(Gravity.CENTER);
        currencyTextView.setLayoutParams(cellParams3);

        TextView exchangeRate = new TextView(this);
        exchangeRate.setText(rate);
        exchangeRate.setTextSize(20);
        exchangeRate.setGravity(Gravity.CENTER);
        exchangeRate.setLayoutParams(cellParams3);

        TextView value = new TextView(this);
        String amountText = POSCommonUtils.getTwoDecimalFloatFromFloat(Float.valueOf(amount.replace(",","")));
        value.setText(amountText);
        value.setTextSize(20);
        value.setGravity(Gravity.CENTER);
        value.setLayoutParams(cellParams3);

        TextView usdVal = new TextView(this);
        usdVal.setText(POSCommonUtils.getTwoDecimalFloatFromFloat(Float.valueOf(USD)));
        usdVal.setTextSize(20);
        usdVal.setGravity(Gravity.CENTER);
        usdVal.setLayoutParams(cellParams3);

        tr.addView(typeText);

        TextView itemDescStr = new TextView(this);
        itemDescStr.setText("Type");
        itemDescStr.setTextSize(15);
        itemDescStr.setLayoutParams(cellParams3);
        itemDescStr.setGravity(Gravity.CENTER);
        tr1.addView(itemDescStr);

        View view  = new View(this);
        view.setLayoutParams(new TableRow.LayoutParams(3, TableRow.LayoutParams.MATCH_PARENT));
        view.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        tr.addView(view);

        View viewDesc  = new View(this);
        viewDesc.setLayoutParams(new TableRow.LayoutParams(3, TableRow.LayoutParams.MATCH_PARENT));
        viewDesc.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        tr1.addView(viewDesc);

        tr.addView(currencyTextView);

        TextView qtyDesc = new TextView(this);
        qtyDesc.setText("Currency");
        qtyDesc.setTextSize(15);
        qtyDesc.setLayoutParams(cellParams3);
        qtyDesc.setGravity(Gravity.CENTER);
        tr1.addView(qtyDesc);

        View view1  = new View(this);
        view1.setLayoutParams(new TableRow.LayoutParams(3, TableRow.LayoutParams.MATCH_PARENT));
        view1.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        tr.addView(view1);

        View view1Desc  = new View(this);
        view1Desc.setLayoutParams(new TableRow.LayoutParams(3, TableRow.LayoutParams.MATCH_PARENT));
        view1Desc.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        tr1.addView(view1Desc);

        tr.addView(exchangeRate);

        TextView priceDesc = new TextView(this);
        priceDesc.setText("Exchange Rate");
        priceDesc.setTextSize(15);
        priceDesc.setLayoutParams(cellParams3);
        priceDesc.setGravity(Gravity.CENTER);
        tr1.addView(priceDesc);

        View view2  = new View(this);
        view2.setLayoutParams(new TableRow.LayoutParams(3, TableRow.LayoutParams.MATCH_PARENT));
        view2.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        tr.addView(view2);

        View view2Desc  = new View(this);
        view2Desc.setLayoutParams(new TableRow.LayoutParams(3, TableRow.LayoutParams.MATCH_PARENT));
        view2Desc.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        tr1.addView(view2Desc);

        tr.addView(value);

        TextView totalDesc = new TextView(this);
        totalDesc.setText("Value");
        totalDesc.setTextSize(15);
        totalDesc.setLayoutParams(cellParams3);
        totalDesc.setGravity(Gravity.CENTER);
        tr1.addView(totalDesc);

        linearLayout.addView(tr);
        linearLayout.addView(tr1);
        frameLayout.addView(linearLayout);
        frameLayout.addView(closeBtn);
        contentLayout.addView(frameLayout);

        dueBalance -= Float.parseFloat(USD);
        String dueBalanceStr = POSCommonUtils.getTwoDecimalFloatFromFloat(dueBalance);
        if(dueBalanceStr.equals("-0.00")){
            dueBalanceStr = "0.00";
        }
        balanceDueTextView.setText(dueBalanceStr);
        paymentMethodsCount++;
        paymentMethodsMap.put(type+" "+currency,amountText);
    }

    private void updateSale(){
        Date date = new Date();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentDateStr = df.format(date);
        for(SoldItem soldItem : soldItems) {
            String userID = SaveSharedPreference.getStringValues(this, Constants.SHARED_PREFERENCE_FA_NAME);
            handler.insertDailySalesEntry(orderNumber, soldItem.getItemId(), soldItem.getQuantity(),
                    soldItem.getTotal(), "Passenger", userID,currentDateStr,category);
            handler.updateSoldItemQty(soldItem.getItemId(), soldItem.getQuantity(),soldItem.getEquipmentNo(),
                    soldItem.getDrawer());
        }

        for(Map.Entry<String,String> entry : paymentMethodsMap.entrySet()){
            handler.insertPaymentMethods(orderNumber,entry.getKey(),entry.getValue());
        }
        String flightId = SaveSharedPreference.getStringValues(this,Constants.SHARED_PREFERENCE_FLIGHT_NAME);
        handler.insertOrderMainDetails(orderNumber,taxPercentage,discount,subTotalAfterTax+"",flightId,category);
        if(creditCardList != null && !creditCardList.isEmpty()){
            for(CreditCard creditCard : creditCardList){
                handler.insertCreditCardDetails(orderNumber,creditCard.getCreditCardNumber(),creditCard.getCardHolderName(),
                        creditCard.getExpireDate(),paymentMethodsMap.get("Credit Card USD"));
            }

        }
        String[] passengerDetails = SaveSharedPreference.getStringValues(this,Constants.SHARED_PREFERENCE_USER_DETAILS).split("==");
        seatNumber = passengerDetails[2];
        paxName = passengerDetails[0];
        handler.insertPassengerDetails(orderNumber,passengerDetails[0],passengerDetails[1],passengerDetails[2],passengerDetails[3],
                flightId,SaveSharedPreference.getStringValues(this,Constants.SHARED_PREFERENCE_FLIGHT_DATE));
    }

    private void printReceipt(){
            if(confirmPaymentBtn.getText().equals("Print Card Holder copy")){
                PrintJob.printOrderDetails(PaymentMethodsActivity.this,orderNumber,
                        seatNumber,soldItems,paymentMethodsMap,
                        creditCardList.isEmpty() ? null : creditCardList.get(0),true,discount,taxPercentage,paxName);
                redirectToMainPage();
            }
            else{
                generateOrderNumber();
                updateSale();
                PrintJob.printOrderDetails(this,orderNumber,seatNumber,soldItems,paymentMethodsMap,
                        creditCardList.isEmpty() ? null : creditCardList.get(0),false,discount,taxPercentage,paxName);
                if(!creditCardList.isEmpty()) {
                    confirmPaymentBtn.setText("Print Card Holder copy");
                }
                else {
                    redirectToMainPage();
                }
            }
    }

    private void redirectToMainPage(){
        if(category.equalsIgnoreCase("Bags")){
            new android.support.v7.app.AlertDialog.Builder(PaymentMethodsActivity.this)
                    .setTitle("Print baggage tag")
                    .setMessage("Do you want to print baggage tag?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            printBaggageTag();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            redirect();
                        }
                    }).show();
        }
        else{
            new android.support.v7.app.AlertDialog.Builder(PaymentMethodsActivity.this)
                    .setTitle("Print vouchers")
                    .setMessage("Do you want to print vouchers?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            printVoucher();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            redirect();
                        }
                    }).show();
        }


    }

    private void redirect(){
        new android.support.v7.app.AlertDialog.Builder(PaymentMethodsActivity.this)
                .setTitle("Leave")
                .setMessage("Do you wish to keep same flight?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        SaveSharedPreference.setStringValues(PaymentMethodsActivity.this,Constants.SHARED_PREFERENCE_KEEP_SAME_FLIGHT,"yes");
                        Intent intent = new Intent(PaymentMethodsActivity.this, GateUserMainActivity.class);
                        startActivity(intent);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        SaveSharedPreference.setStringValues(PaymentMethodsActivity.this,Constants.SHARED_PREFERENCE_KEEP_SAME_FLIGHT,"no");
                        Intent intent = new Intent(PaymentMethodsActivity.this, GateUserMainActivity.class);
                        startActivity(intent);
                    }
                }).show();
    }

    private void printBaggageTag(){
        String  userDetails = SaveSharedPreference.getStringValues(this,Constants.SHARED_PREFERENCE_USER_DETAILS);
        String[] detailsArr = userDetails.split("==");
        String flightNo = SaveSharedPreference.getStringValues(this,Constants.SHARED_PREFERENCE_FLIGHT_NAME);
        Flight flight = handler.getFlightFromFlightName(flightNo);
        String flightTo = flight.getFlightTo();
        if(flightTo.length() > 3){
            flightTo = flightTo.substring(0,3);
        }
        for(int i = 0 ;i<soldItems.size();i++) {
            PrintJob.printBaggageTag(this, flightTo, detailsArr[0], detailsArr[1], flightNo,i);
        }
        redirect();
    }

    private void printVoucher() {

        List<SoldItem> items = new ArrayList<>();
        for(SoldItem item : soldItems){
            int qty = Integer.parseInt(item.getQuantity());
            for(int i = 1; i <= qty ; i++){
                SoldItem item1 = item;
                item1.setQuantity("1");
                items.add(item1);
            }
        }
        final List<SoldItem> finalItemsList = items;
        int i = 0;
        for (final SoldItem soldItem : items) {
            if (i != 0) {
                final int finalI = i;
                new android.support.v7.app.AlertDialog.Builder(PaymentMethodsActivity.this)
                        .setTitle("Print next voucher")
                        .setMessage("Do you wants print next voucher?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                PrintJob.printVoluntaryRemovalReceipt(PaymentMethodsActivity.this, orderNumber, soldItem);
                                if (finalI == finalItemsList.size() - 1) {
                                    redirect();
                                }
                            }
                        })
                        .show();
            } else {
                PrintJob.printVoluntaryRemovalReceipt(this, orderNumber, soldItem);
            }
            i++;
        }
    }

    private void generateOrderNumber(){
        String orderNumberStr = SaveSharedPreference.getStringValues(this,"orderNumber");
        if(orderNumberStr != null){
            int newVal = Integer.parseInt(orderNumberStr) + 1;
            orderNumber = SaveSharedPreference.getStringValues(this,Constants.SHARED_PREFERENCE_FLIGHT_NAME).replace(" ","_") +
                    "_"+POSCommonUtils.getDateString()+"_" + String.valueOf(newVal);
            SaveSharedPreference.updateValue(this,"orderNumber",newVal+"");
        }
        else{
            SaveSharedPreference.setStringValues(this,"orderNumber","1");
            orderNumber = SaveSharedPreference.getStringValues(this,Constants.SHARED_PREFERENCE_FLIGHT_NAME).replace(" ","_") +
                    "_"+POSCommonUtils.getDateString()+"_" + "1";
        }
    }
}
