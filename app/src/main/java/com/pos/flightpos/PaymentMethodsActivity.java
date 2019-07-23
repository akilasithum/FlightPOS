package com.pos.flightpos;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.pt.msr.Msr;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
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
import com.pos.flightpos.objects.LoyaltyCard;
import com.pos.flightpos.objects.SoldItem;
import com.pos.flightpos.objects.XMLMapper.Currency;
import com.pos.flightpos.objects.XMLMapper.Sector;
import com.pos.flightpos.objects.XMLMapper.Voucher;
import com.pos.flightpos.utils.POSCommonUtils;
import com.pos.flightpos.utils.POSDBHandler;
import com.pos.flightpos.utils.PrintJob;
import com.pos.flightpos.utils.PrintUtils;
import com.pos.flightpos.utils.SaveSharedPreference;
import com.sunmi.pay.hardware.aidlv2.AidlConstantsV2;
import com.sunmi.pay.hardware.aidlv2.readcard.CheckCardCallbackV2;
import com.sunmi.pay.hardware.aidlv2.readcard.ReadCardOptV2;

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
    String seatNumber;
    String orderNumber;
    POSDBHandler handler;
    String serviceType;
    String discount;
    String taxPercentage;
    String subTotalAfterTax;
    Float totalBeforeTax;
    TextView discountText;
    Button cancelSaleBtn;
    float discountFromVoucher = 0;
    TextView subTotalTextView;
    PrintUtils printUtils;
    LinearLayout contentLayout;
    private ReadCardOptV2 mReadCardOptV2;
    ProgressDialog dia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_methods);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        handler = new POSDBHandler(this);
        paymentTable = (TableLayout) findViewById(R.id.paymentMethodTable);
        confirmPaymentBtn = (Button) findViewById(R.id.printReceipt);
        balanceDueTextView = (TextView)  findViewById(R.id.balanceDueTextView);
        subTotalTextView = findViewById(R.id.subTotalTextView);
        printUtils = new PrintUtils(this);
        confirmPaymentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
        seatNumber = intent.getExtras().get("SeatNumber").toString();
        serviceType = intent.getExtras().get("serviceType").toString();
        discount = intent.getExtras().get("discount").toString();
        String serviceType = POSCommonUtils.getServiceType(this);
        TableRow totalTextRow = findViewById(R.id.totalTextRow);
        TableRow taxTextRow = findViewById(R.id.serviceTaxRow);
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
        subTotalAfterTax = String.valueOf(dueBalance);
        subTotalTextView.setText(POSCommonUtils.getTwoDecimalFloatFromFloat(dueBalance));
        discountText = findViewById(R.id.discountTextView);
        discountText.setText((discount == null || discount.isEmpty()) ? "0.00" :
                POSCommonUtils.getTwoDecimalFloatFromString(discount));
        creditCardList = new ArrayList<>();
        paymentMethodsMap = new HashMap<>();
        contentLayout = findViewById(R.id.contentLayout);
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
        LinearLayout loyaltyLayout = (LinearLayout) findViewById(R.id.loyaltyPaymentLayout);
        loyaltyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addLoyaltyCardDetails();
            }
        });
        LinearLayout voucherLayout = (LinearLayout) findViewById(R.id.voucherPaymentLayout);
        voucherLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

        cancelSaleBtn = (Button) findViewById(R.id.cancelSale);
        cancelSaleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new android.support.v7.app.AlertDialog.Builder(PaymentMethodsActivity.this)
                        .setTitle("Cancel Sale")
                        .setMessage("Are you sure you want to cancel the sale?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                Intent intent = new Intent(PaymentMethodsActivity.this, SellItemsActivity.class);
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
        dialog.setContentView(R.layout.voucher_layout);
        Window window = dialog.getWindow();
        window.setLayout(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        dialog.setTitle("Vouchers");
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
        Button okBtn = (Button) dialog.findViewById(R.id.voucherOkBtn);
        Button cancelBtn = (Button) dialog.findViewById(R.id.cancelBtn);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (voucherDiscountText.getText() != null && voucherDiscountText.getText().toString() != null &&
                        !voucherDiscountText.getText().toString().equals("0.0")) {

                        dueBalance -= Float.parseFloat(voucherDiscountText.getText().toString());
                        balanceDueTextView.setText(POSCommonUtils.getTwoDecimalFloatFromFloat(dueBalance));
                        subTotalTextView.setText(POSCommonUtils.getTwoDecimalFloatFromFloat(dueBalance));
                        discount = POSCommonUtils.getTwoDecimalFloatFromString(voucherDiscountText.getText().toString());
                        discountFromVoucher = Float.parseFloat(discount);
                        discountText.setText(POSCommonUtils.getTwoDecimalFloatFromFloat(discountFromVoucher));
                        dialog.dismiss();

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
                    String[] ratioArr = discountStr.split(">");
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
                //readMSR("loyalty");
                initPaySDK();
                checkCard();
            }
        });
        dialog.show();
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
        final Button cancelBtn = (Button) dialog.findViewById(R.id.cancelBtn);
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
                closeMSR();
                dialog.dismiss();
            }
        });

        dialog.show();
        initPaySDK();
        checkCard();
    }

    public void setmsg(int i, String track1Str,boolean isCreditCard) {
        if(i == 1)
        {
            if(isCreditCard) {
                if(track1Str != null){
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
                        cancelCheckCard();
                        dia.cancel();
                    } else {
                        showToastMsg("Given credit card is expired.");
                    }
                }
            }
            else{
                String[] track1Details = track1Str.split("\\^");
                String expireDateStr = track1Details[2].substring(2, 4) + "/" + track1Details[2].substring(0, 2);
                    cardNumber = (EditText) dialog.findViewById(R.id.cardNumber);
                    cardHolderName = (EditText) dialog.findViewById(R.id.cardHolderNameText);
                    cardNumber.setText(track1Details[0].substring(1));
                    cardHolderName.setText(track1Details[1]);
                    cancelCheckCard();
                    dia.cancel();
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

    private void addCashSettlement(){

        final Dialog cashSettleDialog = new Dialog(this);
        cashSettleDialog.setContentView(R.layout.cash_settle_layout);
        Window window = cashSettleDialog.getWindow();
        window.setLayout(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        cashSettleDialog.setTitle("Settle by Cash");

        final EditText amount = cashSettleDialog.findViewById(R.id.cashSettleAmountTextField);
        final Spinner currency =  cashSettleDialog.findViewById(R.id.currencyText);
        final TextView errorMsgText = cashSettleDialog.findViewById(R.id.errorMsgText);
        final TextView initialAmount = cashSettleDialog.findViewById(R.id.initialAmount);
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

        amount.setText(POSCommonUtils.getTwoDecimalFloatFromFloat(dueBalance));
        initialAmount.setText(POSCommonUtils.getTwoDecimalFloatFromFloat(dueBalance));

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
        handler = new POSDBHandler(this);
        List<Currency> options=new ArrayList<>();
        List<Currency> equipmentList = handler.getCurrencyList();
        options.addAll(equipmentList);
        return new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,options);
    }

    private void addPaymentMethodToTable(final String type, final String currency, String rate, String amount,final String USD){

        final FrameLayout frameLayout = new FrameLayout(this);
        LinearLayout.LayoutParams frameLayoutParams = new LinearLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,1);
        frameLayoutParams.setMargins(8,0,0,5);
        frameLayout.setLayoutParams(frameLayoutParams);

        ImageView closeBtn = new ImageView(this);
        closeBtn.setClickable(true);
        FrameLayout.LayoutParams closeBtnParam = new FrameLayout.LayoutParams(45, 45);
        closeBtnParam.gravity = Gravity.TOP|Gravity.RIGHT;
        closeBtn.setBackground(getResources().getDrawable(R.drawable.icon_cancel));
        closeBtn.setLayoutParams(closeBtnParam);

        LinearLayout linearLayout = new LinearLayout(this);
        FrameLayout.LayoutParams linearLayoutParams = new FrameLayout.LayoutParams(600,
                TableRow.LayoutParams.WRAP_CONTENT);
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setBackgroundColor(getResources().getColor(R.color.sellitembg));
        linearLayoutParams.setMargins(0,15,15,0);
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

        TableRow.LayoutParams cellParams3 = new TableRow.LayoutParams(0,
                50, 1f);

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
        currencyTextView.setTextSize(15);
        currencyTextView.setGravity(Gravity.CENTER);
        currencyTextView.setLayoutParams(cellParams3);

        TextView exchangeRate = new TextView(this);
        exchangeRate.setText(rate);
        exchangeRate.setTextSize(15);
        exchangeRate.setGravity(Gravity.CENTER);
        exchangeRate.setLayoutParams(cellParams3);

        TextView value = new TextView(this);
        String amountText = POSCommonUtils.getTwoDecimalFloatFromFloat(Float.valueOf(amount.replace(",","")));
        value.setText(amountText);
        value.setTextSize(15);
        value.setGravity(Gravity.CENTER);
        value.setLayoutParams(cellParams3);

        TextView usdVal = new TextView(this);
        usdVal.setText(POSCommonUtils.getTwoDecimalFloatFromFloat(Float.valueOf(USD)));
        usdVal.setTextSize(15);
        usdVal.setGravity(Gravity.CENTER);
        usdVal.setLayoutParams(cellParams3);

        tr.addView(typeText);

        TextView itemDescStr = new TextView(this);
        itemDescStr.setText("Type");
        itemDescStr.setTextSize(10);
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
        qtyDesc.setTextSize(10);
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
        priceDesc.setTextSize(10);
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
        totalDesc.setTextSize(10);
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
            handler.insertDailySalesEntry(orderNumber, soldItem.getItemId(), serviceType,
                    soldItem.getEquipmentNo(), soldItem.getDrawer(), soldItem.getQuantity(),
                    soldItem.getTotal(), "Passenger", userID,currentDateStr);
            handler.updateSoldItemQty(soldItem.getItemId(), soldItem.getQuantity(),soldItem.getEquipmentNo(),
                    soldItem.getDrawer());
        }

        for(Map.Entry<String,String> entry : paymentMethodsMap.entrySet()){
            handler.insertPaymentMethods(orderNumber,entry.getKey(),entry.getValue());
        }
        String flightId = SaveSharedPreference.getStringValues(this,Constants.SHARED_PREFERENCE_FLIGHT_ID);
        handler.insertOrderMainDetails(orderNumber,taxPercentage,discount,seatNumber,subTotalAfterTax+"",serviceType,flightId);
        if(creditCardList != null && !creditCardList.isEmpty()){
            CreditCard creditCard = creditCardList.get(0);
            handler.insertCreditCardDetails(orderNumber,creditCard.getCreditCardNumber(),creditCard.getCardHolderName(),
                    creditCard.getExpireDate(),paymentMethodsMap.get("Credit Card CAD"));
        }
        if(loyaltyCard != null){
            handler.insertLoyaltyCardDetails(orderNumber,loyaltyCard.getLoyaltyCardNumber(),
                    loyaltyCard.getCardHolderName(),String.valueOf(loyaltyCard.getAmount()));
        }
    }

    private void printReceipt(){

            if(confirmPaymentBtn.getText().equals("Print Card Holder copy")){
                printUtils.printOrderDetails(PaymentMethodsActivity.this,orderNumber,
                        seatNumber,soldItems,paymentMethodsMap,
                        creditCardList.isEmpty() ? null : creditCardList.get(0),true,discount,taxPercentage);
                Intent intent = new Intent(PaymentMethodsActivity.this, SellItemsActivity.class);
                startActivity(intent);
            }
            else{
                generateOrderNumber();
                updateSale();
                printUtils.printOrderDetails(this,orderNumber,seatNumber,soldItems,paymentMethodsMap,
                        creditCardList.isEmpty() ? null : creditCardList.get(0),false,discount,taxPercentage);
                if(!creditCardList.isEmpty()) {
                    confirmPaymentBtn.setText("Print Card Holder copy");
                }
                else {
                    Intent intent = new Intent(PaymentMethodsActivity.this, SellItemsActivity.class);
                    startActivity(intent);
                }
            }
    }

    private void generateOrderNumber(){
        String orderNumberStr = SaveSharedPreference.getStringValues(this,"orderNumber");
        if(orderNumberStr != null){
            int newVal = Integer.parseInt(orderNumberStr) + 1;
            orderNumber = SaveSharedPreference.getStringValues(this,Constants.SHARED_PREFERENCE_FLIGHT_NAME).replace(" ","_") +
                    "_"+POSCommonUtils.getFlightDateString(this)+"_" + String.valueOf(newVal);
            SaveSharedPreference.updateValue(this,"orderNumber",newVal+"");
        }
        else{
            SaveSharedPreference.setStringValues(this,"orderNumber","1");
            orderNumber = SaveSharedPreference.getStringValues(this,Constants.SHARED_PREFERENCE_FLIGHT_NAME).replace(" ","_") +
                    "_"+POSCommonUtils.getDateString()+"_" + "1";
        }
    }

    private void initPaySDK(){
        mReadCardOptV2 = BootUpReceiver.mReadCardOptV2;
    }

    private void checkCard() {

        dia = new ProgressDialog(this);
        dia.setTitle("MSR");
        dia.setMessage("please swipe MSR card...");
        dia.show();
        try {
            mReadCardOptV2.checkCard(AidlConstantsV2.CardType.MAGNETIC.getValue(), mCheckCardCallback, 60);
        } catch (Exception e) {
            showToastMsg("Card reader failed.");
            e.printStackTrace();
        }
    }

    private CheckCardCallbackV2 mCheckCardCallback = new CheckCardCallbackV2.Stub() {

        @Override
        public void findMagCard(Bundle bundle) throws RemoteException {
            handleResult(bundle);
        }
        @Override
        public void findICCard(String s) throws RemoteException {

        }
        @Override
        public void findRFCard(String s) throws RemoteException {

        }
        @Override
        public void onError(int code, String message) throws RemoteException {
            handleResult(null);
            if(dia != null){
                dia.cancel();
            }
        }
    };

    private void handleResult(final Bundle bundle) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (bundle != null) {
                    String track1 = bundle.getString("TRACK1");
                    setmsg(1,track1,true);
                }
            }
        });
    }

    private void cancelCheckCard() {
        try {
            mReadCardOptV2.cancelCheckCard();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
