package com.pos.flightpos;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.pos.flightpos.objects.Constants;
import com.pos.flightpos.objects.Flight;
import com.pos.flightpos.objects.SoldItem;
import com.pos.flightpos.objects.XMLMapper.ComboDiscount;
import com.pos.flightpos.objects.XMLMapper.Promotion;
import com.pos.flightpos.objects.XMLMapper.Sector;
import com.pos.flightpos.utils.POSCommonUtils;
import com.pos.flightpos.utils.POSDBHandler;
import com.pos.flightpos.utils.SaveSharedPreference;

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
    EditText flightDateText;
    EditText flightNumberText;
    Calendar myCalendar;
    Spinner flightSectorSpinner;
    EditText paxName;
    TableRow taxPercentageRow;
    boolean hasSectors = false;
    EditText taxPercentage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accept_pre_orders);
        itemCatSpinner = (Spinner) findViewById(R.id.itemCategorySpinner);
        //serviceTypeSpinner = findViewById(R.id.serviceTypeSpinner);
        itemSpinner = (Spinner) findViewById(R.id.itemSpinner);
        submitBtn = (Button) findViewById(R.id.addItemBtn);
        contentTable = (TableLayout) findViewById(R.id.contentTable);
        subTotalView = (TextView) findViewById(R.id.subTotalTextView);
        purchaseItemsBtn = (Button) findViewById(R.id.purchaseItems);
        taxPercentageRow = findViewById(R.id.taxPercentageRow);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickSubmitBtn();
            }
        });
        purchaseItemsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                purchaseItems();
            }
        });
        ImageButton backButton = findViewById(R.id.backPressBtn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        discountItemList = new ArrayList<>();
        soldItemList = new ArrayList<>();
        handler = new POSDBHandler(getApplicationContext());
        //populateServiceTypes();
        taxPercentage = findViewById(R.id.taxPercentage);
        serviceType = getIntent().getExtras().getString("serviceType");
        populateItemCatField(serviceType);

        itemCatSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override/*serviceTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                populateItemCatField(serviceTypeSpinner.getSelectedItem().toString());
                serviceType = serviceTypeSpinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });*/
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                populateItemsFromCat(itemCatSpinner.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });
        flightDateText = findViewById(R.id.flightDate);
        flightNumberText = findViewById(R.id.flightNumber);
        flightSectorSpinner = findViewById(R.id.flightSector);
        paxName = findViewById(R.id.paxName);
        setDatePicker();
        configureFlightNumber();
        showHideTaxPercentage(true);
        flightSectorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(flightSectorSpinner.getSelectedItem() != null && !flightSectorSpinner.
                        getSelectedItem().toString().isEmpty()){
                    showHideTaxPercentage(!(flightSectorSpinner.getSelectedItem().toString().split("\\*")[1].equalsIgnoreCase("Domestic")));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void showHideTaxPercentage(boolean isNotVisible){
        if(isNotVisible) {
            taxPercentage.setText("");
            taxPercentageRow.setVisibility(View.GONE);
        }
        else{
            taxPercentageRow.setVisibility(View.VISIBLE);
            taxPercentage.setText("13");

        }
    }

    private void configureFlightNumber(){
        flightNumberText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if(s.length() == 3) {
                    populateFlightList(flightNumberText.getText().toString());
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }
    private void showSectorSelectionSpinner(String sectors){
        ArrayList<String> options=new ArrayList<String>();
        options.add("");
        String[] sectorArr = sectors.split(",");
        for(int i=0;i<sectorArr.length;i++){
            if(sectorArr[i] != null && !sectorArr[i].isEmpty())
                options.add(sectorArr[i].replace("+","->"));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,options);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        flightSectorSpinner.setAdapter(adapter);
    }
    private void populateFlightList(String flightNumber){

        Flight flight = handler.getFlightFromFlightName(flightNumber);
        if(flight != null) {
            flightNumberText.setText(flight.getFlightName());
            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            if(flight.getSectorStr() != null && !flight.getSectorStr().isEmpty()){
                hasSectors = true;
                showSectorSelectionSpinner(flight.getSectorStr());
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

    private void updateLabel() {
        String myFormat = "dd/MM/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat);

        flightDateText.setText(sdf.format(myCalendar.getTime()));
    }

    private void purchaseItems() {
        String paxNameStr = paxName.getText() == null ? null : paxName.getText().toString();
        String flightNumberStr = flightNumberText.getText() == null ? null : flightNumberText.getText().toString();
        String flightDateStr = flightDateText.getText() == null ? null : flightDateText.getText().toString();
        String flightSectorStr = "";
        if(hasSectors){
            flightSectorStr = flightSectorSpinner.getSelectedItem() == null ? null : flightSectorSpinner.getSelectedItem().toString();
        }
        if (itemCount == 0) {
            Toast.makeText(getApplicationContext(), "Add items before purchase items.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (paxNameStr == null || paxNameStr.isEmpty() || flightNumberStr == null || flightNumberStr.isEmpty() ||
                flightDateStr == null || flightDateStr.isEmpty() || flightSectorStr == null) {
            Toast.makeText(getApplicationContext(), "Enter pax details",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if(!isDateLaterThreeDays(flightDateStr)){
            Toast.makeText(getApplicationContext(), "Pre order flight date should be three dates later.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

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
        intent.putExtra("paxName", paxName.getText().toString());
        intent.putExtra("flightNumber", flightNumberText.getText().toString());
        intent.putExtra("flightDate", flightDateText.getText().toString());
        intent.putExtra("sector", hasSectors ? flightSectorSpinner.getSelectedItem().toString() : "");
        intent.putExtra("orderNumber", orderNumber);
        intent.putExtra("serviceType", serviceType);
        intent.putExtra("discount", "");
        intent.putExtra("serviceTax",taxPercentage.getText() != null ? taxPercentage.getText().toString() : "");
        startActivity(intent);
    }

    private List<SoldItem> getSellDataFromTable() {

        int rowCount = contentTable.getChildCount();
        List<Promotion> promotions = handler.getPromotionsFromServiceType(serviceType);
        List<SoldItem> soldList = new ArrayList<>();
        itemIds = new ArrayList<>();
        for (int i = 1; i < rowCount - 2; i++) {
            TableRow tableRow = (TableRow) contentTable.getChildAt(i);
            TextView itemID = (TextView) tableRow.getChildAt(0);
            TextView itemDesc = (TextView) tableRow.getChildAt(1);
            EditText qty = (EditText) tableRow.getChildAt(2);
            TextView price = (TextView) tableRow.getChildAt(3);
            TextView total = (TextView) tableRow.getChildAt(4);
            TextView category = (TextView) tableRow.getChildAt(5);
            itemDesc.getText();
            SoldItem soldItem = new SoldItem();
            soldItem.setItemId(itemID.getText().toString());
            soldItem.setItemDesc(itemDesc.getText().toString());
            soldItem.setQuantity(qty.getText().toString());
            soldItem.setTotal(total.getText().toString());
            soldItem.setItemCategory(category.getText().toString());
            float discount = getDiscount(promotions, itemID.getText().toString());
            if (discount != 0) {
                float newPrice = Float.parseFloat(price.getText().toString()) * ((100 - discount) / 100);
                String newPriceStr = POSCommonUtils.getTwoDecimalFloatFromFloat(newPrice);
                soldItem.setPrice(newPriceStr);
                soldItem.setPriceBeforeDiscount(price.getText().toString());
                discountItemList.add(soldItem);
                subtotal -= (Float.parseFloat(price.getText().toString()) - Float.parseFloat(newPriceStr));
            } else {
                soldItem.setPrice(price.getText().toString());
            }
            itemIds.add(itemID.getText().toString());
            soldList.add(soldItem);
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
