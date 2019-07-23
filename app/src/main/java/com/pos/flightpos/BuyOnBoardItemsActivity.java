package com.pos.flightpos;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.pt.nfc.Nfc;
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
import com.pos.flightpos.objects.SoldItem;
import com.pos.flightpos.objects.XMLMapper.Item;
import com.pos.flightpos.objects.XMLMapper.Promotion;
import com.pos.flightpos.utils.POSCommonUtils;
import com.pos.flightpos.utils.POSDBHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BuyOnBoardItemsActivity extends AppCompatActivity {

    Button purchaseItemsBtn;
    Button scanBoardingPassBtn;
    Spinner itemCatSpinner;
    TableLayout contentTable;
    private int itemCount = 0;
    private float subtotal = 0;
    TextView subTotalView;
    EditText seatNumber;
    EditText rfidValue;
    List<SoldItem> soldItemList;
    POSDBHandler handler;
    String serviceType;
    List<SoldItem> discountItemList;
    //LinearLayout currentSelection;
    List<String> itemIds;
    LinearLayout scanRFIDLayout;
    View currentSelection;
    LinearLayout contentLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_on_board_items);
        itemCatSpinner = (Spinner) findViewById(R.id.itemCategorySpinner);
        contentTable = (TableLayout) findViewById(R.id.contentTable);
        subTotalView = (TextView)  findViewById(R.id.subTotalTextView);
        seatNumber = (EditText) findViewById(R.id.seatNumber);
        purchaseItemsBtn = (Button) findViewById(R.id.purchaseItems);
        purchaseItemsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                purchaseItems();
            }
        });
        scanBoardingPassBtn = (Button) findViewById(R.id.scanBoardingPass);
        scanBoardingPassBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scan();
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
        /*scanRFIDLayout = findViewById(R.id.scanRFIDLayout);
        scanRFIDLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //showNFCDetails();
                *//*Intent intent = new Intent(BuyOnBoardItemsActivity.this, Main2Activity.class);
                startActivity(intent);*//*
                //PrintUtils printUtils = new PrintUtils();
               //printUtils.printQr("akila sithum",10,0,BuyOnBoardItemsActivity.this);
                Intent intent = new Intent(BuyOnBoardItemsActivity.this, ScannerMainActivity.class);
                startActivity(intent);

            }
        });*/
        //setItemCatClickListeners();
        ImageButton backButton = findViewById(R.id.backPressBtn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void showNFCDetails(){
        final Nfc nfc = new Nfc();
        nfc.open();
        final ProgressDialog dia = new ProgressDialog(this);
        dia.setTitle("NFC");
        dia.setMessage("please touch the nfc tag...");
        dia.show();
        dia.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                nfc.close();
            }
        });
        final byte[] data = new byte[1024];
        new Thread(){
            public void run() {
                int ret = -1;
                Calendar c = Calendar.getInstance();
                long startTime = c.getTimeInMillis();
                while(Calendar.getInstance().getTimeInMillis()<startTime+10000) {
                    ret = nfc.seek(data);
                    if(ret >= 0 && ret<=3){
                    String str = "00,a4,04,00,0e,32,50,41,59,2e,53,59,53,2e,44,44,46,30,31,00";
                    String temp_str[] = str.split(",");
                    byte[] out_dat = new byte[1024];
                    byte[] intbyte_dat = new byte[temp_str.length];
                    for (int i = 0; i < intbyte_dat.length; i++) {
                        intbyte_dat[i] = (byte) Integer.parseInt(temp_str[i], 16);
                    }
                    nfc.activate();
                    ret = nfc.exeAPDU(intbyte_dat, out_dat);
                    if (ret > 0) {
                        String out_temp = "";
                        for (int i = 0; i < ret; i++) {
                            out_temp += String.format("%02x", out_dat[i] & 0xff) + " ";
                        }
                        setData(out_temp);
                    }
                    else{
                        setData("");
                    }
                        nfc.close();
                        dia.dismiss();
                        break;
                }
                    /*if(ret >= 0 && ret<=3)
                    {
                        String string1 ="" ;
                        *//*for (int i = 0; i < data.length; i++) {
                            String str = Integer.toHexString(data[i]&0xff);
                            //String str = String.format("%02x",data[i]&0xff);
                            if(str != null && !str.equals("00")){
                                string1 += str;
                            }
                        }*//*

                        for (int i = 1; i < data[0]+1; i++) {
                            //dat_card += Integer.toHexString(data[i]&0xff);
                            string1 += String.format("%02x", (data[i]&0xff));
                        }
                        nfc.close();
                        dia.dismiss();
                        setData(string1);
                        break;
                    }*/
                }
                nfc.close();
                dia.dismiss();

            }
        }.start();
    }

    private void setData(final String str){
        Item item = handler.getItemFromNFCTag(str);
        List<SoldItem> itemList = handler.getItemListFromItemCategory(item.getCategory(),getKitCodes());
        SoldItem selectedItem = null;
        for(SoldItem soldItem : itemList){
            if(soldItem.getItemDesc().equals(item.getItemName())){
                selectedItem = soldItem;
            }
        }

        if(selectedItem != null) {
            final SoldItem item1 = selectedItem;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    clickSubmitBtn(item1, true);
                }
            });
        }
        else{
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "No item available for the NFC tag",
                            Toast.LENGTH_SHORT).show();
                }
            });

        }
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
            imageView.setImageResource(this.getResources().getIdentifier(categories.getValue(), "drawable", "com.pos.flightpos"));

            LinearLayout imageLayout = new LinearLayout(this);
            imageLayout.setGravity(Gravity.CENTER);
            imageLayout.setLayoutParams(params3);
            imageLayout.setOrientation(LinearLayout.VERTICAL);
            imageLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.sellitemimagebg));
            imageLayout.addView(imageView);

            TextView textView = new TextView(this);
            textView.setLayoutParams(params2);
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
        List<SoldItem> soldItems = getSellDataFromTable();
        String discount = POSCommonUtils.getIfDiscountsAvailable(itemIds,handler);
        if(discount != null && !discount.isEmpty()){
            subtotal -= Float.parseFloat(discount);
            showComboDiscount(soldItems,seatNumberVal,discount);
        }
        else{
            redirectToPaymentPage(soldItems,seatNumberVal,"");
        }
    }

    private void showComboDiscount(final List<SoldItem> soldItems, final String seatNumberVal,
                                   final String discount) {
        new AlertDialog.Builder(BuyOnBoardItemsActivity.this)
                .setTitle("Combo Discount")
                .setMessage("You have saved $"+discount + " ")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        redirectToPaymentPage(soldItems, seatNumberVal,discount);
                    }}).show();
    }

    private void redirectToPaymentPage(List<SoldItem> soldItems,String seatNumberVal,String discount){
        Intent intent = new Intent(this, PaymentMethodsActivity.class);
        intent.putExtra("subTotal", subtotal);
        Bundle args = new Bundle();
        args.putSerializable("soldItemList",(Serializable)soldItems);
        intent.putExtra("BUNDLE",args);
        intent.putExtra("SeatNumber",seatNumberVal);
        intent.putExtra("serviceType",serviceType);
        intent.putExtra("discount",discount);
        startActivity(intent);
    }

    /*private List<SoldItem> getSellDataFromTable(){
        List<Promotion> promotions = handler.getPromotionsFromServiceType(serviceType);
        int rowCount = contentTable.getChildCount();
        List<SoldItem> soldList = new ArrayList<>();
        itemIds = new ArrayList<>();
        subtotal = 0;
        for(int i=1;i<rowCount-3;i++) {
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
            soldItem.setTotal(total.getText().toString());
            float discount = getDiscount(promotions,itemID.getText().toString());
            *//*if(discount != 0){
                float newPrice = Float.parseFloat(price.getText().toString()) * ((100-discount)/100);
                String newPriceStr = POSCommonUtils.getTwoDecimalFloatFromFloat(newPrice);
                soldItem.setPrice(newPriceStr);
                soldItem.setPriceBeforeDiscount(price.getText().toString());
                discountItemList.add(soldItem);
                subtotal -= (Float.parseFloat(price.getText().toString()) - Float.parseFloat(newPriceStr));
            }
            else{*//*
            subtotal += Float.parseFloat(total.getText().toString());
                soldItem.setPrice(price.getText().toString());
            //}
            soldList.add(soldItem);
            itemIds.add(itemID.getText().toString());
        }
        return soldList;
    }*/

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

    private float getDiscount(List<Promotion> promotions,String itemId){

        for(Promotion promotion : promotions){
            if(promotion.getItemId().equals(itemId)){
                return Float.parseFloat(promotion.getDiscount());
            }
        }
        return 0;
    }
    private void showDiscountData(final List<SoldItem> soldItems,final String seatNumberVal){
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
                redirectToPaymentPage(soldItems,seatNumberVal,"");
            }
        });
        discountDialog.show();
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
                new AlertDialog.Builder(BuyOnBoardItemsActivity.this)
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
                POSCommonUtils.showDrawerAndEquipment(item,BuyOnBoardItemsActivity.this);
            }
        });
        lookupBtn.setGravity(Gravity.CENTER);
        tr.addView(lookupBtn);

        TextView empty = new TextView(this);
        empty.setText("");
        empty.setTextSize(10);
        empty.setLayoutParams(cellParams3);
        empty.setGravity(Gravity.CENTER);
        tr1.addView(empty);

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

    /*private void clickSubmitBtn(final SoldItem item,boolean isNFC){
        if(item == null || item.equals("")){
            Toast.makeText(getApplicationContext(), "select item first.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        //POSCommonUtils.showDrawerAndEquipment(item,this);
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
        removeItemBtn.setPadding(0,4,0,0);
        removeItemBtn.setBackground(getResources().getDrawable(R.drawable.icon_cancel));
        removeItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(BuyOnBoardItemsActivity.this)
                        .setTitle("Remove selection")
                        .setMessage("Do you want to remove this item from selection?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                subtotal -= Float.parseFloat(totalTextField.getText().toString());
                                subTotalView.setText(String.valueOf(subtotal));
                                itemCount--;
                                contentTable.removeView(tr);
                            }})
                        .setNegativeButton(android.R.string.no, null).show();
            }
        });

        Button lookupBtn = new Button(this);
        lookupBtn.setLayoutParams(cellParams3);
        lookupBtn.setBackground(getResources().getDrawable(R.drawable.icon_llokup));
        lookupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                POSCommonUtils.showDrawerAndEquipment(item,BuyOnBoardItemsActivity.this);
            }
        });

        itemIdHdn.setText(item.getItemId());
        itemIdHdn.setVisibility(View.GONE);
        tr.addView(itemIdHdn);

        itemDesc.setText(item.getItemDesc());
        itemDesc.setTextSize(13);
        itemDesc.setLayoutParams(cellParams1);
        tr.addView(itemDesc);

        qty.setText("1");
        qty.setTextSize(13);
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

        price.setText(POSCommonUtils.getTwoDecimalFloatFromString(item.getPrice()));
        price.setTextSize(13);
        price.setLayoutParams(cellParams2);
        tr.addView(price);

        float total = Float.parseFloat(qty.getText().toString()) * Float.parseFloat(price.getText().toString());
        totalTextField.setText(POSCommonUtils.getTwoDecimalFloatFromString(String.valueOf(total)));
        totalTextField.setTextSize(13);
        totalTextField.setLayoutParams(cellParams2);
        tr.addView(totalTextField);

        equipmentNo.setText(item.getEquipmentNo());
        equipmentNo.setVisibility(View.GONE);
        tr.addView(equipmentNo);

        drawer.setText(item.getDrawer());
        drawer.setVisibility(View.GONE);
        tr.addView(drawer);

        if(isNFC) {
            tr.addView(lookupBtn);
        }
        tr.addView(removeItemBtn);

        subtotal += total;
        SoldItem soldItem = new SoldItem();
        soldItem.setItemDesc(item.getItemDesc());
        soldItem.setQuantity("1");
        soldItem.setPrice(item.getPrice());
        soldItemList.add(soldItem);
        subTotalView.setText(POSCommonUtils.getTwoDecimalFloatFromFloat(subtotal));
        contentTable.addView(tr,itemCount);
    }*/

    private void updateTotalWhenChangeItemQty(Float diff){
        String currentSubTotal = subTotalView.getText().toString();
        subtotal = Float.parseFloat(currentSubTotal) + diff;
        subTotalView.setText(String.valueOf(subtotal));
    }

    private String getKitCodes(){
        List<String> kitCodes = POSCommonUtils.getServiceTypeKitCodeMap(this).get(serviceType);
       return POSCommonUtils.getCommaSeparateStrFromList(kitCodes);
    }

    /*private void populateItemImages(String selectedCat){
        String kitCodesStr = getKitCodes();
        List<SoldItem> itemList = handler.getItemListFromItemCategory(selectedCat,kitCodesStr);
        LinearLayout innerLayout = (LinearLayout) findViewById(R.id.innerLay);
        innerLayout.removeAllViews();
        for(final SoldItem item : itemList){
            LinearLayout layout = new LinearLayout(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(150,150);
            layout.setLayoutParams(params1);
            layout.setGravity(Gravity.CENTER);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setBackground(ContextCompat.getDrawable(this, R.drawable.textinputborder));
            layout.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    clickSubmitBtn(item,false);
                }
            });

            ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(params);
            imageView.setPadding(4,4,4,0);
            //imageView.setImageResource(getItemResource(this,item.getItemDesc()));
            imageView.setImageBitmap(getImageFromItemCode(item.getItemId()));

            TextView textView = new TextView(this);
            textView.setLayoutParams(params);
            textView.setTextSize(10);
            textView.setText(item.getItemDesc());

            TextView priceText = new TextView(this);
            priceText.setLayoutParams(params);
            priceText.setTextSize(10);
            priceText.setText("$"+POSCommonUtils.getTwoDecimalFloatFromString(item.getPrice()));

            layout.addView(imageView);
            layout.addView(textView);
            layout.addView(priceText);
            innerLayout.addView(layout);
        }
    }*/

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
            textView.setText(item.getItemDesc());

            TextView priceText = new TextView(this);
            priceText.setLayoutParams(params2);
            priceText.setTextColor(getResources().getColor(R.color.white));
            priceText.setText("$"+POSCommonUtils.getTwoDecimalFloatFromString(item.getPrice()));

            layout.addView(textView);
            layout.addView(imageLayout);
            layout.addView(priceText);
            innerLayout.addView(layout);
        }
    }

    public int getItemResource(Context context, String itemName) {
        itemName = itemName.toLowerCase().replace("&","and").replace("’","")
                .replace("(","").replace(")","").
                        replace(" ","_").replace("-","");
        int resId = context.getResources().getIdentifier(itemName, "drawable", "com.pos.flightpos");
        return resId;
    }

    private Bitmap getImageFromItemCode(String itemCode)
    {
        try {
            File f=new File("/data/data/com.pos.flightpos/app_imageDir", itemCode+".png");
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            return b;
        }
        catch (Exception e)
        {
            return null;
        }

    }

    private void populateItemCatField(){

        List<String> options=new ArrayList<String>();
        options.add("");
        List<String> catList = handler.getItemCatFromItems(serviceType);
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

    private void populateSeatNumberFromBoardingPass(String qrCode){
        final Map<String,String> qrCodeDetails = POSCommonUtils.readBarcodeDetails(qrCode,this);
        if(qrCodeDetails != null) {
            String fileNames = "";
            for(Map.Entry entry : qrCodeDetails.entrySet()){
                fileNames += entry.getKey() + " - " + entry.getValue() + "\n";
            }
            new AlertDialog.Builder(BuyOnBoardItemsActivity.this)
                    .setTitle("QR Code details")
                    .setMessage(fileNames)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            seatNumber.setText(qrCodeDetails.get("seatNo"));
                        }})
                    .setNegativeButton(android.R.string.cancel, null).show();
        }
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
}
