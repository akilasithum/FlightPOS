package com.pos.flightpos;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.pos.flightpos.objects.Constants;
import com.pos.flightpos.objects.SoldItem;
import com.pos.flightpos.objects.XMLMapper.Item;
import com.pos.flightpos.objects.XMLMapper.Promotion;
import com.pos.flightpos.utils.POSCommonUtils;
import com.pos.flightpos.utils.POSDBHandler;
import com.pos.flightpos.utils.SaveSharedPreference;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuyItemFromCategoryActivity extends AppCompatActivity {

    Button submitBtn;
    Button purchaseItemsBtn;
    Button scanBoardingPassBtn;
    Spinner itemCatSpinner;
    Spinner itemSpinner;
    TableLayout contentTable;
    private int itemCount = 0;
    private float subtotal = 0;
    TextView subTotalView;
    EditText seatNumber;
    List<SoldItem> soldItemList;
    POSDBHandler handler;
    String serviceType;
    String kitCode;
    List<SoldItem> discountItemList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_item_from_category);
        kitCode = SaveSharedPreference.getStringValues(this, Constants.SHARED_PREFERENCE_KIT_CODE);
        itemCatSpinner = (Spinner) findViewById(R.id.itemCategorySpinner);
        itemSpinner = (Spinner) findViewById(R.id.itemSpinner);
        submitBtn = (Button) findViewById(R.id.addItemBtn);
        contentTable = (TableLayout) findViewById(R.id.contentTable);
        subTotalView = (TextView)  findViewById(R.id.subTotalTextView);
        seatNumber = (EditText) findViewById(R.id.seatNumber);
        purchaseItemsBtn = (Button) findViewById(R.id.purchaseItems);
        scanBoardingPassBtn = (Button) findViewById(R.id.scanBoardingPass);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickSubmitBtn(); }
        });
        purchaseItemsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                purchaseItems();
            }
        });
        scanBoardingPassBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                populateSeatNumberFromBoardingPass();
            }
        });
        discountItemList = new ArrayList<>();
        soldItemList = new ArrayList<>();
        handler = new POSDBHandler(getApplicationContext());
        Intent intent = getIntent();
        serviceType = intent.getExtras().get("serviceType").toString();
        populateItemCatField();

        itemCatSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                populateItemsFromCat(itemCatSpinner.getSelectedItem().toString());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                    // your code here
            }
        });
    }

        private void purchaseItems(){
            String seatNumberVal = seatNumber.getText() == null ? null : seatNumber.getText().toString();
            if(itemCount == 0){
                Toast.makeText(getApplicationContext(), "Add items before purchase items.",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if(seatNumberVal == null || seatNumberVal.equals("")){
                Toast.makeText(getApplicationContext(), "Enter seat number",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            String orderNumber = SaveSharedPreference.getStringValues(this,"orderNumber");
            if(orderNumber != null){
                int newVal = Integer.parseInt(orderNumber) + 1;
                orderNumber = String.valueOf(newVal);
                SaveSharedPreference.updateValue(this,"orderNumber",orderNumber);
            }
            else{
                SaveSharedPreference.setStringValues(this,"orderNumber","1");
                orderNumber = "1";
            }

            List<SoldItem> soldItems = getSellDataFromTable(orderNumber);
            if(discountItemList != null && !discountItemList.isEmpty()){
                showDiscountData(soldItems,seatNumberVal,orderNumber);
            }
            else{
                redirectToPaymentPage(soldItems,seatNumberVal,orderNumber);
            }
        }

        private void showDiscountData(final List<SoldItem> soldItems,final String seatNumberVal,final String orderNumber){
            final Dialog discountDialog = new Dialog(this);
            discountDialog.setContentView(R.layout.discount_details_layout);
            Window window = discountDialog.getWindow();
            TableRow.LayoutParams params1 = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT);
            window.setLayout(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
            discountDialog.setTitle("Discount");

            TableLayout tableLayout = discountDialog.findViewById(R.id.discountTable);
            TableRow.LayoutParams params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT,1);
            int i = 0;
            for(SoldItem soldItem : discountItemList){
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

                tableLayout.addView(tableRow,i);
            }

            Button okBtn = (Button) discountDialog.findViewById(R.id.cardSubmitBtn);
            okBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    discountDialog.dismiss();
                    redirectToPaymentPage(soldItems,seatNumberVal,orderNumber);
                }
            });
            discountDialog.show();
        }

        private void redirectToPaymentPage(List<SoldItem> soldItems,String seatNumberVal,String orderNumber){
            Intent intent = new Intent(this, PaymentMethodsActivity.class);
            intent.putExtra("subTotal", subtotal);
            Bundle args = new Bundle();
            args.putSerializable("soldItemList",(Serializable)soldItems);
            intent.putExtra("BUNDLE",args);
            intent.putExtra("SeatNumber",seatNumberVal);
            intent.putExtra("orderNumber",orderNumber);
            startActivity(intent);
        }

        private List<SoldItem> getSellDataFromTable(String orderNumber){

            int rowCount = contentTable.getChildCount();
            List<Promotion> promotions = handler.getPromotionsFromServiceType(serviceType);
            List<SoldItem> soldList = new ArrayList<>();
            for(int i=1;i<rowCount-4;i++) {
                TableRow tableRow = (TableRow) contentTable.getChildAt(i);
                TextView itemID = (TextView) tableRow.getChildAt(0);
                TextView itemDesc = (TextView) tableRow.getChildAt(1);
                EditText qty = (EditText) tableRow.getChildAt(2);
                TextView price = (TextView) tableRow.getChildAt(3);
                TextView total = (TextView) tableRow.getChildAt(4);
                TextView equipmentNo = (TextView) tableRow.getChildAt(5);
                TextView drawer = (TextView) tableRow.getChildAt(6);
                itemDesc.getText();
                SoldItem soldItem = new SoldItem();
                soldItem.setItemId(itemID.getText().toString());
                soldItem.setItemDesc(itemDesc.getText().toString());
                soldItem.setQuantity(qty.getText().toString());
                soldItem.setEquipmentNo(equipmentNo.getText().toString());
                soldItem.setDrawer(drawer.getText().toString());
                float discount = getDiscount(promotions,itemID.getText().toString());
                if(discount != 0){
                    float newPrice = Float.parseFloat(price.getText().toString()) * ((100-discount)/100);
                    String newPriceStr = POSCommonUtils.getTwoDecimalFloatFromFloat(newPrice);
                    soldItem.setPrice(newPriceStr);
                    soldItem.setPriceBeforeDiscount(price.getText().toString());
                    discountItemList.add(soldItem);
                    subtotal -= (Float.parseFloat(price.getText().toString()) - Float.parseFloat(newPriceStr));
                }
                else{
                    soldItem.setPrice(price.getText().toString());
                }
                soldList.add(soldItem);
                String userID = SaveSharedPreference.getStringValues(this,Constants.SHARED_PREFERENCE_KEY);
                handler.insertDailySalesEntry(orderNumber,itemID.getText().toString(),serviceType,
                        equipmentNo.getText().toString(),drawer.getText().toString(),qty.getText().toString(),
                        total.getText().toString(),"Passenger",userID);
                handler.updateSoldItemQty(itemID.getText().toString(),qty.getText().toString(),
                        equipmentNo.getText().toString(),drawer.getText().toString());
            }
            return soldList;

        }

        private float getDiscount(List<Promotion> promotions,String itemId){

            for(Promotion promotion : promotions){
                if(promotion.getItemId().equals(itemId)){
                    return Float.parseFloat(promotion.getDiscount());
                }
            }
            return 0;
        }

        private void clickSubmitBtn(){

            SoldItem item = (SoldItem)itemSpinner.getSelectedItem();
            if(item == null || item.equals("")){
                Toast.makeText(getApplicationContext(), "select item first.",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            POSCommonUtils.showDrawerAndEquipment(item,this);
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
            TextView equipmentNo = new TextView(this);
            TextView drawer = new TextView(this);
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
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
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

            price.setText(item.getPrice());
            price.setTextSize(20);
            price.setLayoutParams(cellParams2);
            tr.addView(price);

            float total = Float.parseFloat(qty.getText().toString()) * Float.parseFloat(price.getText().toString());
            totalTextField.setText(String.valueOf(total));
            totalTextField.setTextSize(20);
            totalTextField.setLayoutParams(cellParams2);
            tr.addView(totalTextField);

            equipmentNo.setText(item.getEquipmentNo());
            equipmentNo.setVisibility(View.GONE);
            tr.addView(equipmentNo);

            drawer.setText(item.getDrawer());
            drawer.setVisibility(View.GONE);
            tr.addView(drawer);

            tr.addView(removeItemBtn);

            subtotal += total;
            SoldItem soldItem = new SoldItem();
            soldItem.setItemDesc(item.getItemDesc());
            soldItem.setQuantity("1");
            soldItem.setPrice(item.getPrice());
            soldItemList.add(soldItem);

            subTotalView.setText(POSCommonUtils.getTwoDecimalFloatFromFloat(subtotal));
            contentTable.addView(tr,itemCount);
        }

        private void updateTotalWhenChangeItemQty(Float diff){
            String currentSubTotal = subTotalView.getText().toString();
            subtotal = Float.parseFloat(currentSubTotal) + diff;
            subTotalView.setText(String.valueOf(subtotal));
        }

        private void populateItemsFromCat(String selectedCat){
            List<SoldItem> options=new ArrayList<>();
            ArrayAdapter<SoldItem> adapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,options);
            itemSpinner.setAdapter(adapter);
            List<SoldItem> itemList = handler.getItemListFromItemCategory(selectedCat,kitCode);
            SoldItem item = new SoldItem();
            options.add(item);
            options.addAll(itemList);
            adapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,options);
            adapter.setDropDownViewResource(R.layout.spinner_item);
            itemSpinner.setAdapter(adapter);
        }

        private void populateItemCatField(){

            List<String> options=new ArrayList<String>();
            options.add("");
            List<String> catList = handler.getItemCatFromItems(kitCode);
            if(catList.size() > 0) {
                final ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_MUSIC , 100);
                tg.startTone(ToneGenerator.TONE_PROP_BEEP);
                tg.release();
                options.addAll(catList);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, options);
                adapter.setDropDownViewResource(R.layout.spinner_item);
                itemCatSpinner.setAdapter(adapter);
            }
            else{
                Toast.makeText(getApplicationContext(), "No items available in this category.",
                        Toast.LENGTH_SHORT).show();
                ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
                tg.startTone(ToneGenerator.TONE_CDMA_PIP, 1000);
                tg.release();
            }
        }

        private void populateSeatNumberFromBoardingPass(){
            String qrCodeDetails = POSCommonUtils.scanQRCode(this);
            seatNumber.setText(qrCodeDetails);
        }
    }
