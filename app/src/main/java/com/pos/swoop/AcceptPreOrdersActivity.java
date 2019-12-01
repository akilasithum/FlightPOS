package com.pos.swoop;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.support.v4.content.ContextCompat;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
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

import com.pos.swoop.objects.Flight;
import com.pos.swoop.objects.PreOrderUser;
import com.pos.swoop.objects.Sector;
import com.pos.swoop.objects.SoldItem;
import com.pos.swoop.objects.User;
import com.pos.swoop.objects.XMLMapper.ComboDiscount;
import com.pos.swoop.objects.XMLMapper.Promotion;
import com.pos.swoop.utils.POSCommonUtils;
import com.pos.swoop.utils.POSDBHandler;
import com.pos.swoop.utils.SaveSharedPreference;

import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AcceptPreOrdersActivity extends AppCompatActivity {

    Button submitBtn;
    Button purchaseItemsBtn;
    //Spinner serviceTypeSpinner;
    Spinner itemCatSpinner;
    Spinner itemSpinner;
    TableLayout contentTable;
    private int itemCount = 0;
    private float subtotal = 0;
    TextView subTotalView;
    List<SoldItem> soldItemList;
    POSDBHandler handler;
    String serviceType;
    List<SoldItem> discountItemList;
    List<String> itemIds;
    Calendar myCalendar;
    EditText paxName;
    TableRow taxPercentageRow;
    boolean hasSectors = false;
    EditText taxPercentage;
    View currentSelection;
    LinearLayout contentLayout;
    EditText seatNumber;
    EditText pnr;
    Button enterUserDetails;
    Button scanBoardingPass;
    PreOrderUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accept_pre_orders);
        itemCatSpinner = (Spinner) findViewById(R.id.itemCategorySpinner);
        contentTable = (TableLayout) findViewById(R.id.contentTable);
        subTotalView = (TextView)  findViewById(R.id.subTotalTextView);
        seatNumber = (EditText) findViewById(R.id.seatNumber);
        pnr = findViewById(R.id.pnr);
        purchaseItemsBtn = (Button) findViewById(R.id.purchaseItems);
        purchaseItemsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(user == null){
                    Toast.makeText(getApplicationContext(), "User details does not added yet.",
                            Toast.LENGTH_SHORT).show();
                }
                else if(itemCount == 0){
                    Toast.makeText(getApplicationContext(), "Please add items before continue",
                            Toast.LENGTH_SHORT).show();
                }
                else {
                    purchaseItems();
                }
            }
        });
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        discountItemList = new ArrayList<>();
        soldItemList = new ArrayList<>();
        handler = new POSDBHandler(getApplicationContext());
        Intent intent = getIntent();
        contentLayout = findViewById(R.id.contentLayout);
        serviceType = intent.getExtras().get("serviceType").toString();
        loadItemCategoryImages();
        ImageButton backButton = findViewById(R.id.backPressBtn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        paxName = findViewById(R.id.paxName);
        taxPercentage = findViewById(R.id.taxPercentage);
        taxPercentageRow = findViewById(R.id.taxPercentageRow);
        //showHideTaxPercentage(true);

        contentLayout = findViewById(R.id.contentLayout);
        enterUserDetails = findViewById(R.id.enterUserDetails);
        scanBoardingPass = findViewById(R.id.scanBoardingPass);
        enterUserDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUserDetailsPopup();
            }
        });
        scanBoardingPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scan();
            }
        });
    }
    private void loadItemCategoryImages(){
        LinearLayout itemCatRow = findViewById(R.id.itemCatTableRow);
        itemCatRow.removeAllViews();
        Map<String, String> itemCategories = new HashMap<>();
        if(serviceType.equals("BOB")) {
            itemCategories = POSCommonUtils.getBOBItemCategories();
        }
        else if(serviceType.equals("DTF")){
            itemCategories = POSCommonUtils.getDTFItemCategories();
        }
        else if(serviceType.equals("VRT")){
            itemCategories = POSCommonUtils.getVRTItemCategories();
        }
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.MATCH_PARENT,1);
        params.setMargins(5,0,5,0);
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(70,70);
        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams params3 = new LinearLayout.LayoutParams(110,110);
        params3.setMargins(0,5,0,25);

        for(final Map.Entry<String,String> categories : itemCategories.entrySet()){
            final LinearLayout layout = new LinearLayout(this);
            layout.setLayoutParams(params);
            layout.setGravity(Gravity.CENTER);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(10,3,10,3);
            layout.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    populateItemImages(categories.getKey());
                }
            });

            ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(params1);
            imageView.setImageResource(this.getResources().getIdentifier(categories.getValue(), "drawable", "com.pos.swoop"));

            LinearLayout imageLayout = new LinearLayout(this);
            imageLayout.setGravity(Gravity.CENTER);
            imageLayout.setLayoutParams(params3);
            imageLayout.setOrientation(LinearLayout.VERTICAL);
            imageLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.sellitemimagebg));
            imageLayout.addView(imageView);

            TextView textView = new TextView(this);
            textView.setLayoutParams(params2);
            textView.setTextSize(10);
            textView.setTextColor(getResources().getColor(R.color.white));
            String textVal = categories.getKey();
            if(textVal.contains("and")){
                textVal = textVal.replace("and","and \n");
            }
            textView.setText(textVal);
            layout.addView(textView);
            layout.addView(imageLayout);
            itemCatRow.addView(layout);
        }
    }

    private String getKitCodes(){
        List<String> kitCodes = POSCommonUtils.getServiceTypeKitCodeMap(this).get(serviceType);
        return POSCommonUtils.getCommaSeparateStrFromList(kitCodes);
    }

    private void populateItemImages(String selectedCat){
        List<SoldItem> itemList = handler.getItemListFromItemCategory(selectedCat,getKitCodes());
        LinearLayout innerLayout =  findViewById(R.id.itemCatTableRow);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.MATCH_PARENT,2);
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(70,70);
        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams params3 = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.MATCH_PARENT,1);

        LinearLayout.LayoutParams params4 = new LinearLayout.LayoutParams(110,110);
        params4.setMargins(0,5,0,5);

        innerLayout.removeAllViews();

        final LinearLayout backBtnLayout = new LinearLayout(this);
        backBtnLayout.setLayoutParams(params3);
        backBtnLayout.setGravity(Gravity.CENTER);
        backBtnLayout.setOrientation(LinearLayout.VERTICAL);
        backBtnLayout.setPadding(5,3,5,3);
        backBtnLayout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                loadItemCategoryImages();

            }
        });

        ImageView backArrow = new ImageView(this);
        backArrow.setLayoutParams(new LinearLayout.LayoutParams(40,40));
        backArrow.setBackground(getResources().getDrawable(R.drawable.back_arrow_icon));


        backBtnLayout.addView(backArrow);
        innerLayout.addView(backBtnLayout);

        for(final SoldItem item : itemList){
            LinearLayout layout = new LinearLayout(this);
            layout.setGravity(Gravity.CENTER);
            params.setMargins(5,0,5,0);
            layout.setLayoutParams(params);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    clickSubmitBtn(item,false);
                }
            });

            ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(params1);
            imageView.setPadding(4,4,4,0);
            imageView.setImageBitmap(getImageFromItemCode(item.getItemId()));

            LinearLayout imageLayout = new LinearLayout(this);
            imageLayout.setGravity(Gravity.CENTER);
            imageLayout.setLayoutParams(params4);
            imageLayout.setOrientation(LinearLayout.VERTICAL);
            imageLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.sellitemimagebg));

            imageLayout.addView(imageView);

            TextView textView = new TextView(this);
            textView.setTextColor(getResources().getColor(R.color.white));
            textView.setLayoutParams(params2);
            textView.setTextSize(10);
            textView.setText(item.getItemDesc());

            TextView priceText = new TextView(this);
            priceText.setLayoutParams(params2);
            priceText.setTextSize(10);
            priceText.setTextColor(getResources().getColor(R.color.white));
            priceText.setText("$"+POSCommonUtils.getTwoDecimalFloatFromString(item.getPrice()));

            layout.addView(textView);
            layout.addView(imageLayout);
            layout.addView(priceText);
            innerLayout.addView(layout);
        }
    }

    private Bitmap getImageFromItemCode(String itemCode)
    {
        try {
            File f=new File("/data/data/com.pos.swoop/app_imageDir", itemCode+".png");
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            return b;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    private void clickSubmitBtn(final SoldItem item,boolean isNFC){
        if(item == null || item.equals("")){
            Toast.makeText(getApplicationContext(), "select item first.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        itemCount++;
        final FrameLayout frameLayout = new FrameLayout(this);
        LinearLayout.LayoutParams frameLayoutParams = new LinearLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,1);
        frameLayoutParams.setMargins(8,0,0,4);
        frameLayout.setLayoutParams(frameLayoutParams);

        ImageView closeBtn = new ImageView(this);
        closeBtn.setClickable(true);
        FrameLayout.LayoutParams closeBtnParam = new FrameLayout.LayoutParams(45, 45);
        closeBtnParam.gravity = Gravity.TOP|Gravity.RIGHT;
        closeBtn.setBackground(getResources().getDrawable(R.drawable.icon_cancel));
        closeBtn.setLayoutParams(closeBtnParam);

        final LinearLayout linearLayout = new LinearLayout(this);
        FrameLayout.LayoutParams linearLayoutParams = new FrameLayout.LayoutParams(600,
                TableRow.LayoutParams.WRAP_CONTENT);
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setBackgroundColor(getResources().getColor(R.color.sellitembg));
        linearLayoutParams.setMargins(0,10,10,0);
        linearLayout.setLayoutParams(linearLayoutParams);

        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setBackgroundColor(getResources().getColor(R.color.white));
                if(currentSelection == null) {
                    currentSelection = v;
                }
                else {
                    currentSelection.setBackgroundColor(getResources().getColor(R.color.sellitembg));
                    currentSelection = v;
                }
            }
        });

        TableRow tr = new TableRow(this);
        TableRow tr1 = new TableRow(this);
        tr.setId(itemCount);
        tr.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.FILL_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));
        tr.setPadding(0,5,0,0);
        tr1.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.FILL_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));
        tr1.setPadding(0,0,0,5);


        TableRow.LayoutParams cellParams1 = new TableRow.LayoutParams(0,
                TableRow.LayoutParams.WRAP_CONTENT, 6f);
        TableRow.LayoutParams cellParams2 = new TableRow.LayoutParams(0,
                TableRow.LayoutParams.WRAP_CONTENT, 3f);
        TableRow.LayoutParams cellParams3 = new TableRow.LayoutParams(0,
                35, 1f);

        TableRow.LayoutParams cellParams4 = new TableRow.LayoutParams(0,
                45, 1f);
        cellParams4.setMargins(0,40,0,0);

        TextView itemIdHdn = new TextView(this);
        TextView itemDesc = new TextView(this);
        EditText qty = new EditText(this);
        final TextView price = new TextView(this);
        final TextView totalTextField = new TextView(this);
        Button removeItemBtn = new Button(this);
        removeItemBtn.setLayoutParams(cellParams3);
        removeItemBtn.setBackground(getResources().getDrawable(R.drawable.icon_cancel));
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(AcceptPreOrdersActivity.this)
                        .setTitle("Remove selection")
                        .setMessage("Do you want to remove this item from selection?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                subtotal -= Float.parseFloat(totalTextField.getText().toString());
                                subTotalView.setText(String.valueOf(subtotal));
                                itemCount--;
                                contentLayout.removeView(frameLayout);

                            }})
                        .setNegativeButton(android.R.string.no, null).show();
            }
        });

        itemIdHdn.setText(item.getItemId());
        itemIdHdn.setVisibility(View.GONE);
        tr.addView(itemIdHdn);

        itemDesc.setText(item.getItemDesc());
        itemDesc.setTextSize(15);
        itemDesc.setLayoutParams(cellParams1);
        itemDesc.setGravity(Gravity.CENTER);
        tr.addView(itemDesc);

        TextView itemDescStr = new TextView(this);
        itemDescStr.setText("Item Desc");
        itemDescStr.setTextSize(10);
        itemDescStr.setLayoutParams(cellParams1);
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

        qty.setText("1");
        qty.setTextSize(15);
        qty.setLayoutParams(cellParams2);
        qty.setGravity(Gravity.CENTER);
        qty.setInputType(InputType.TYPE_CLASS_NUMBER);
        qty.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
                linearLayout.setBackgroundColor(getResources().getColor(R.color.white));
                if(currentSelection == null) {
                    currentSelection = linearLayout;
                }
                else {
                    if(currentSelection != linearLayout) {
                        currentSelection.setBackgroundColor(getResources().getColor(R.color.sellitembg));
                        currentSelection = linearLayout;
                    }
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {

                if(s != null && !s.toString().equals("")) {
                    Float currentItemTotal = Float.parseFloat(totalTextField.getText().toString());
                    Float newItemTotal = Float.parseFloat(s.toString())* Float.parseFloat(price.getText().toString());
                    totalTextField.setText(String.valueOf(newItemTotal));
                    updateTotalWhenChangeItemQty(newItemTotal - currentItemTotal);
                }
            }
        });
        tr.addView(qty);

        TextView qtyDesc = new TextView(this);
        qtyDesc.setText("Qty");
        qtyDesc.setTextSize(10);
        qtyDesc.setLayoutParams(cellParams2);
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

        price.setText(POSCommonUtils.getTwoDecimalFloatFromString(item.getPrice()));
        price.setTextSize(15);
        price.setLayoutParams(cellParams2);
        price.setGravity(Gravity.CENTER);
        tr.addView(price);

        TextView priceDesc = new TextView(this);
        priceDesc.setText("Unit Price");
        priceDesc.setTextSize(10);
        priceDesc.setLayoutParams(cellParams2);
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

        float total = Float.parseFloat(qty.getText().toString()) * Float.parseFloat(price.getText().toString());
        totalTextField.setText(POSCommonUtils.getTwoDecimalFloatFromString(String.valueOf(total)));
        totalTextField.setTextSize(15);
        totalTextField.setLayoutParams(cellParams2);
        totalTextField.setGravity(Gravity.CENTER);
        tr.addView(totalTextField);

        TextView totalDesc = new TextView(this);
        totalDesc.setText("Total");
        totalDesc.setTextSize(10);
        totalDesc.setLayoutParams(cellParams2);
        totalDesc.setGravity(Gravity.CENTER);
        tr1.addView(totalDesc);

        Button lookupBtn = new Button(this);
        lookupBtn.setLayoutParams(cellParams4);
        lookupBtn.setBackground(getResources().getDrawable(R.drawable.icon_llokup));
        lookupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                POSCommonUtils.showDrawerAndEquipment(item, AcceptPreOrdersActivity.this);
            }
        });
        lookupBtn.setGravity(Gravity.CENTER);
        //tr.addView(lookupBtn);

        TextView empty = new TextView(this);
        empty.setText("");
        empty.setTextSize(10);
        empty.setLayoutParams(cellParams3);
        empty.setGravity(Gravity.CENTER);
        //tr1.addView(empty);

        subtotal += total;
        SoldItem soldItem = new SoldItem();
        soldItem.setItemDesc(item.getItemDesc());
        soldItem.setQuantity("1");
        soldItem.setPrice(item.getPrice());
        soldItemList.add(soldItem);
        subTotalView.setText(POSCommonUtils.getTwoDecimalFloatFromFloat(subtotal));
        linearLayout.addView(tr);
        linearLayout.addView(tr1);
        frameLayout.addView(linearLayout);
        frameLayout.addView(closeBtn);
        contentLayout.addView(frameLayout);


    }


    private void configureFlightNumber(final EditText flightNumberText, final Spinner flightSectorSpinner){
        flightNumberText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if(s.length() == 3) {
                    populateFlightList(flightNumberText.getText().toString(),flightNumberText,flightSectorSpinner);
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }
    private void showSectorSelectionSpinner(List<Sector> options,final Spinner flightSectorSpinner){

        ArrayAdapter<Sector> adapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,options);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        flightSectorSpinner.setAdapter(adapter);
    }
    private void populateFlightList(String flightNumber,EditText flightNumberText,Spinner flightSectorSpinner){

        Flight flight = handler.getFlightFromFlightName(flightNumber);
        if(flight != null) {
            flightNumberText.setText(flight.getFlightName());
            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            if(flight.getSectorList() != null && !flight.getSectorList().isEmpty()){
                hasSectors = true;
                showSectorSelectionSpinner(flight.getSectorList(),flightSectorSpinner);
            }
            else{
                hasSectors = false;
                TableRow tableRow = findViewById(R.id.sectorRow);
                tableRow.setVisibility(View.GONE);
            }
        }
        else{
            Toast.makeText(getApplicationContext(), "Invalid flight number",
                    Toast.LENGTH_SHORT).show();
        }

    }

    private void setDatePicker(final EditText flightDateText){
        myCalendar = Calendar.getInstance();
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel(flightDateText);
            }
        };
        flightDateText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(AcceptPreOrdersActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    private void updateLabel(EditText flightDateText) {
        String myFormat = "dd/MM/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat);

        flightDateText.setText(sdf.format(myCalendar.getTime()));
    }

    private void purchaseItems() {


        String orderNumber = SaveSharedPreference.getStringValues(this, "orderNumber");
        if (orderNumber != null) {
            int newVal = Integer.parseInt(orderNumber) + 1;
            orderNumber = String.valueOf(newVal);
            SaveSharedPreference.updateValue(this, "orderNumber", orderNumber);
        } else {
            SaveSharedPreference.setStringValues(this, "orderNumber", "1");
            orderNumber = "1";
        }

        List<SoldItem> soldItems = getSellDataFromTable();
        String discount = getIfDiscountsAvailable();
        if (discount != null && !discount.isEmpty()) {
            showComboDiscount(soldItems, orderNumber,discount);
        } else {
            redirectToPaymentPage(soldItems, orderNumber);
        }
    }

    private boolean isDateLaterThreeDays(String dateStr){

        try {
            Date date = new SimpleDateFormat("dd/MM/yyyy").parse(dateStr);
            Date today = new Date();
            Calendar c = Calendar.getInstance();
            c.setTime(today);
            c.add(Calendar.DATE, 3);
            if(c.getTime().equals(date) || c.getTime().before(date) ){
                return true;
            }
        }
        catch (Exception e){
            return false;
        }

        return false;
    }

    private void showComboDiscount(final List<SoldItem> soldItems,
                                   final String orderNumber,String discount) {
        new AlertDialog.Builder(AcceptPreOrdersActivity.this)
                .setTitle("Combo Discount")
                .setMessage("You have saved $"+discount + " ")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        redirectToPaymentPage(soldItems, orderNumber);
                    }}).show();
    }

    private void showDiscountData(final List<SoldItem> soldItems, final String seatNumberVal, final String orderNumber) {
        final Dialog discountDialog = new Dialog(this);
        discountDialog.setContentView(R.layout.discount_details_layout);
        Window window = discountDialog.getWindow();
        TableRow.LayoutParams params1 = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT);
        window.setLayout(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        discountDialog.setTitle("Discount");

        TableLayout tableLayout = discountDialog.findViewById(R.id.discountTable);
        TableRow.LayoutParams params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1);
        int i = 0;
        for (SoldItem soldItem : discountItemList) {
            i++;
            TableRow tableRow = new TableRow(this);
            tableRow.setLayoutParams(params1);

            TextView itemName = new TextView(this);
            itemName.setText(soldItem.getItemDesc());
            itemName.setLayoutParams(params);
            itemName.setTextSize(20);

            TextView price = new TextView(this);
            price.setText(soldItem.getPrice());
            price.setLayoutParams(params);
            price.setTextSize(20);

            TextView discountPrice = new TextView(this);
            discountPrice.setText(soldItem.getPrice());
            discountPrice.setLayoutParams(params);
            discountPrice.setTextSize(20);

            tableRow.addView(itemName);
            tableRow.addView(price);
            tableRow.addView(discountPrice);

            tableLayout.addView(tableRow, i);
        }

        Button okBtn = (Button) discountDialog.findViewById(R.id.cardSubmitBtn);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                discountDialog.dismiss();
                redirectToPaymentPage(soldItems, orderNumber);
            }
        });
        discountDialog.show();
    }

    private void redirectToPaymentPage(List<SoldItem> soldItems, String orderNumber) {
        Intent intent = new Intent(this, PreOrderPaymentsActivity.class);
        intent.putExtra("subTotal", subtotal);
        Bundle args = new Bundle();
        args.putSerializable("soldItemList", (Serializable) soldItems);
        intent.putExtra("BUNDLE", args);
        intent.putExtra("paxName", user.getPaxName());
        intent.putExtra("flightNumber", user.getFlightNo());
        intent.putExtra("flightDate", user.getFlightDate());
        intent.putExtra("pnr",user.getPnr());
        intent.putExtra("sector", hasSectors ? user.getSector() : "");
        intent.putExtra("orderNumber", orderNumber);
        intent.putExtra("serviceType", serviceType);
        intent.putExtra("discount", "");
        intent.putExtra("serviceTax",user.getServiceTax() != null ? user.getServiceTax() : "");
        startActivity(intent);
    }

    private List<SoldItem> getSellDataFromTable(){
        int rowCount = contentLayout.getChildCount();
        List<SoldItem> soldList = new ArrayList<>();
        itemIds = new ArrayList<>();
        subtotal = 0;
        for(int i=0;i<rowCount;i++) {
            TableRow tableRow = (TableRow)((LinearLayout)((FrameLayout) contentLayout.getChildAt(i)).getChildAt(0)).getChildAt(0);
            TextView itemID = (TextView) tableRow.getChildAt(0);
            TextView itemDesc = (TextView) tableRow.getChildAt(1);
            EditText qty = (EditText) tableRow.getChildAt(3);
            TextView price = (TextView) tableRow.getChildAt(5);
            TextView total = (TextView) tableRow.getChildAt(7);
            SoldItem soldItem = new SoldItem();
            soldItem.setItemId(itemID.getText().toString());
            soldItem.setItemDesc(itemDesc.getText().toString());
            soldItem.setQuantity(qty.getText().toString());
            soldItem.setTotal(total.getText().toString());

            subtotal += Float.parseFloat(total.getText().toString());
            soldItem.setPrice(price.getText().toString());
            soldList.add(soldItem);
            itemIds.add(itemID.getText().toString());
        }
        return soldList;
    }

    private float getDiscount(List<Promotion> promotions, String itemId) {

        for (Promotion promotion : promotions) {
            if (promotion.getItemId().equals(itemId)) {
                return Float.parseFloat(promotion.getDiscount());
            }
        }
        return 0;
    }

    private void clickSubmitBtn() {

        SoldItem item = (SoldItem) itemSpinner.getSelectedItem();
        if (item == null || item.equals("")) {
            Toast.makeText(getApplicationContext(), "select item first.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        itemCatSpinner.setSelection(0);
        itemSpinner.setSelection(0);
        itemCount++;
        final TableRow tr = new TableRow(this);
        tr.setId(itemCount);
        tr.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));

        TableRow.LayoutParams cellParams1 = new TableRow.LayoutParams(0,
                TableRow.LayoutParams.WRAP_CONTENT, 6f);
        TableRow.LayoutParams cellParams2 = new TableRow.LayoutParams(0,
                TableRow.LayoutParams.WRAP_CONTENT, 3f);
        TableRow.LayoutParams cellParams3 = new TableRow.LayoutParams(0,
                35, 1f);

        TextView itemIdHdn = new TextView(this);
        TextView itemDesc = new TextView(this);
        EditText qty = new EditText(this);
        final TextView price = new TextView(this);
        final TextView totalTextField = new TextView(this);
        TextView itemCategory = new TextView(this);
        Button removeItemBtn = new Button(this);
        removeItemBtn.setLayoutParams(cellParams3);
        removeItemBtn.setBackground(getResources().getDrawable(R.drawable.icon_cancel));
        removeItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                subtotal -= Float.parseFloat(totalTextField.getText().toString());
                subTotalView.setText(String.valueOf(subtotal));
                itemCount--;
                contentTable.removeView(tr);
            }
        });

        itemIdHdn.setText(item.getItemId());
        itemIdHdn.setVisibility(View.GONE);
        tr.addView(itemIdHdn);

        itemDesc.setText(item.getItemDesc());
        itemDesc.setTextSize(20);
        itemDesc.setLayoutParams(cellParams1);
        tr.addView(itemDesc);

        qty.setText("1");
        qty.setTextSize(20);
        qty.setLayoutParams(cellParams2);
        qty.setInputType(InputType.TYPE_CLASS_NUMBER);
        qty.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {

                if (s != null && !s.toString().equals("")) {
                    Float currentItemTotal = Float.parseFloat(totalTextField.getText().toString());
                    Float newItemTotal = Float.parseFloat(s.toString()) * Float.parseFloat(price.getText().toString());
                    totalTextField.setText(String.valueOf(newItemTotal));
                    updateTotalWhenChangeItemQty(newItemTotal - currentItemTotal);
                }
            }
        });
        tr.addView(qty);

        price.setText(item.getPrice());
        price.setTextSize(20);
        price.setLayoutParams(cellParams2);
        tr.addView(price);

        float total = Float.parseFloat(qty.getText().toString()) * Float.parseFloat(price.getText().toString());
        totalTextField.setText(String.valueOf(total));
        totalTextField.setTextSize(20);
        totalTextField.setLayoutParams(cellParams2);
        tr.addView(totalTextField);

        itemCategory.setText(item.getItemCategory());
        itemCategory.setVisibility(View.GONE);
        tr.addView(itemCategory);

        tr.addView(removeItemBtn);

        subtotal += total;
        SoldItem soldItem = new SoldItem();
        soldItem.setItemDesc(item.getItemDesc());
        soldItem.setQuantity("1");
        soldItem.setPrice(item.getPrice());
        soldItemList.add(soldItem);

        subTotalView.setText(POSCommonUtils.getTwoDecimalFloatFromFloat(subtotal));
        contentTable.addView(tr, itemCount);
    }

    private void updateTotalWhenChangeItemQty(Float diff) {
        String currentSubTotal = subTotalView.getText().toString();
        subtotal = Float.parseFloat(currentSubTotal) + diff;
        subTotalView.setText(String.valueOf(subtotal));
    }

    private void showUserDetailsPopup(){

        final Dialog userDetailsDialog = new Dialog(this);
        userDetailsDialog.setContentView(R.layout.pre_order_user_details_layout);
        Window window = userDetailsDialog.getWindow();
        window.setLayout(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        userDetailsDialog.setTitle("User Details");

        final EditText paxName = userDetailsDialog.findViewById(R.id.paxName);
        final TextView pnr =  userDetailsDialog.findViewById(R.id.pnr);
        final EditText flightNumber = userDetailsDialog.findViewById(R.id.flightNumber);
        final Spinner flightSectorSpinner = userDetailsDialog.findViewById(R.id.flightSector);
        final EditText flightDate = userDetailsDialog.findViewById(R.id.flightDate);
        setDatePicker(flightDate);
        configureFlightNumber(flightNumber,flightSectorSpinner);
        Button okBtn =  userDetailsDialog.findViewById(R.id.cardSubmitBtn);
        Button cancelBtn = userDetailsDialog.findViewById(R.id.cancelBtn);
        final EditText taxPercentage = userDetailsDialog.findViewById(R.id.taxPercentage);;
        final TableRow taxPercentageRow = userDetailsDialog.findViewById(R.id.taxPercentageRow);
        taxPercentageRow.setVisibility(View.GONE);
        taxPercentage.setText("");

        if(user != null){
            paxName.setText(user.getPaxName());
            pnr.setText(user.getPnr());
            String flightNoVal = user.getFlightNo();
            if(flightNoVal != null){
                String[]  arr = flightNoVal.split(" ");
                if (arr.length == 2)flightNumber.setText(arr[1]);
                else {
                    if(flightNoVal.length() > 3){
                        flightNumber.setText(flightNoVal.substring(1,3));
                    }
                }
            }
            flightDate.setText(user.getFlightDate());
            taxPercentage.setText(user.getServiceTax());
        }

        flightSectorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(flightSectorSpinner.getSelectedItem() != null && !flightSectorSpinner.
                        getSelectedItem().toString().isEmpty()){
                    Sector sector = (Sector)flightSectorSpinner.getSelectedItem();
                    if(!sector.getSectorType().equalsIgnoreCase("Domestic")) {
                        taxPercentage.setText("");
                        taxPercentageRow.setVisibility(View.GONE);
                    }
                    else{
                        taxPercentageRow.setVisibility(View.VISIBLE);
                        taxPercentage.setText("13");

                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String paxNameStr = paxName.getText() == null ? null : paxName.getText().toString();
                String flightNumberStr = flightNumber.getText() == null ? null : flightNumber.getText().toString();
                String flightDateStr = flightDate.getText() == null ? null : flightDate.getText().toString();
                String flightSectorStr = "";
                String pnrStr = pnr.getText() == null ? null : pnr.getText().toString();
                String tax = taxPercentage.getText() == null ? null : taxPercentage.getText().toString();
                if(hasSectors){
                    flightSectorStr = flightSectorSpinner.getSelectedItem() == null ? null : flightSectorSpinner.getSelectedItem().toString();
                }
                if (paxNameStr == null || paxNameStr.isEmpty() || flightNumberStr == null || flightNumberStr.isEmpty() ||
                        flightDateStr == null || flightDateStr.isEmpty() || flightSectorStr == null || pnrStr == null || pnrStr.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Enter pax details",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!isDateLaterThreeDays(flightDateStr)){
                    Toast.makeText(getApplicationContext(), "Pre order flight date should be three dates later.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                user = new PreOrderUser();
                user.setPaxName(paxNameStr);
                user.setPnr(pnrStr);
                user.setFlightNo(flightNumberStr);
                user.setFlightDate(flightDateStr);
                user.setSector(flightSectorStr);
                user.setServiceTax(tax);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Toast.makeText(getApplicationContext(), "User Details saved successfully",
                        Toast.LENGTH_SHORT).show();
                userDetailsDialog.dismiss();

            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userDetailsDialog.dismiss();
            }
        });
        userDetailsDialog.show();
    }

    private void scan(){
        Intent intent = new Intent();
        intent.setAction("com.summi.scan");
        intent.setPackage("com.sunmi.sunmiqrcodescanner");
        intent.putExtra("IS_SHOW_SETTING", false);      // whether to display the setting button, default true
        intent.putExtra("IDENTIFY_MORE_CODE", true);    // identify multiple qr code in the screen
        intent.putExtra("IS_AZTEC_ENABLE", true);       // allow read of AZTEC code
        intent.putExtra("IS_PDF417_ENABLE", true);      // allow read of PDF417 code
        intent.putExtra("IS_DATA_MATRIX_ENABLE", true); // allow read of DataMatrix code
        PackageManager packageManager = getPackageManager();
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, 100);
        } else {
            Toast.makeText(this,"Error",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100 && data != null) {
            Bundle bundle = data.getExtras();
            ArrayList<HashMap<String, String>> result = (ArrayList< HashMap<String, String> >) bundle.getSerializable("data");
            if (result != null && result.size() > 0) {
                String value = result.get(0).get("VALUE");
                populateSeatNumberFromBoardingPass(value);
            } else {
                Toast.makeText(this,"Scan Failed",Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void populateSeatNumberFromBoardingPass(String qrCode){
        final Map<String,String> qrCodeDetails = POSCommonUtils.readBarcodeDetails(qrCode,this);
        if(qrCodeDetails != null) {
            user = new PreOrderUser();
            user.setPnr(qrCodeDetails.get("PNR"));
            user.setPaxName(qrCodeDetails.get("name"));
            showUserDetailsPopup();
        }
    }


    private void populateItemsFromCat(String selectedCat) {
        List<SoldItem> options = new ArrayList<>();
        ArrayAdapter<SoldItem> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, options);
        itemSpinner.setAdapter(adapter);
        List<SoldItem> itemList = handler.getItemListFromItemCategoryForPreOrder(selectedCat, serviceType);
        SoldItem item = new SoldItem();
        options.add(item);
        options.addAll(itemList);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        itemSpinner.setAdapter(adapter);
    }

    /*private void populateServiceTypes(){
        List<String> options = new ArrayList<String>();
        options.add("");
        options.add("BOB");
        options.add("DTP");
        options.add("DTF");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        serviceTypeSpinner.setAdapter(adapter);
    }*/

    private void populateItemCatField(String serviceType) {

        List<String> options = new ArrayList<String>();
        options.add("");
        List<String> catList = handler.getItemCatFromServiceType(serviceType);
        if (catList.size() > 0) {
            final ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
            tg.startTone(ToneGenerator.TONE_PROP_BEEP);
            tg.release();
            options.addAll(catList);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, options);
            adapter.setDropDownViewResource(R.layout.spinner_item);
            itemCatSpinner.setAdapter(adapter);
        }
    }
    private String getIfDiscountsAvailable() {

        if (itemIds != null) {
            List<ComboDiscount> discounts = handler.getComboDiscounts();
            List<Integer> discountList = new ArrayList<>();
            for (ComboDiscount comboDiscount : discounts) {
                String items = comboDiscount.getItems();
                List<String> andList = new ArrayList<>();
                Map<Integer, List<String>> orList = new HashMap<>();
                String[] andItems = items.split("and");
                int orCount = 0;
                for (int i = 0; i < andItems.length; i++) {
                    if (andItems[i].contains("or")) {
                        String[] orItems = andItems[i].split("or");
                        orList.put(orCount, new ArrayList<String>());
                        for (int j = 0; j < orItems.length; j++) {
                            orList.get(orCount).add(orItems[j].trim());
                        }
                        orCount++;
                    } else {
                        andList.add(andItems[i].trim());
                    }
                }
                if (itemIds.size() >= andList.size() + orList.size()) {
                    for (String itemId : itemIds) {
                        if (andList.contains(itemId)) {
                            andList.remove(itemId);
                        } else {
                            for (Map.Entry<Integer, List<String>> entry : orList.entrySet()) {
                                if (entry.getValue().contains(itemId)) {
                                    orList.remove(entry.getKey());
                                }
                            }
                        }
                    }
                }
                if (andList.size() == 0 && orList.size() == 0) {
                    discountList.add(Integer.valueOf(comboDiscount.getDiscount()));
                }
            }
            if (discountList.size() != 0) {
                Collections.sort(discountList);
                return String.valueOf(discountList.get(discountList.size() - 1));
            }
        }
        return null;
    }
}
