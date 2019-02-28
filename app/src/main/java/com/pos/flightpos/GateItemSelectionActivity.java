package com.pos.flightpos;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.pos.flightpos.objects.SoldItem;
import com.pos.flightpos.objects.XMLMapper.Promotion;
import com.pos.flightpos.utils.POSCommonUtils;
import com.pos.flightpos.utils.POSDBHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GateItemSelectionActivity extends AppCompatActivity {

    TableLayout contentTable;
    TextView subTotalView;
    Button purchaseItemsBtn;
    String category;

    private int itemCount = 0;
    private float subtotal = 0;
    List<SoldItem> soldItemList;
    List<String> itemIds;
    int overWeightBagCount = 0;
    POSDBHandler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gate_item_selection);
        handler = new POSDBHandler(this);
        contentTable =  findViewById(R.id.contentTable);
        subTotalView =  findViewById(R.id.subTotalTextView);
        purchaseItemsBtn =  findViewById(R.id.purchaseItems);
        category = getIntent().getExtras().get("category").toString();
        ImageButton backButton = findViewById(R.id.backPressBtn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        soldItemList = new ArrayList<>();
        loadItemCategoryImages();
        purchaseItemsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                purchaseItems();
            }
        });
    }

    private void purchaseItems(){

        if(itemCount == 0){
            Toast.makeText(getApplicationContext(), "Add items before purchase items.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        List<SoldItem> soldItems = getSellDataFromTable();
        redirectToPaymentPage(soldItems,"");
    }

    private List<SoldItem> getSellDataFromTable(){
        int rowCount = contentTable.getChildCount();
        List<SoldItem> soldList = new ArrayList<>();
        itemIds = new ArrayList<>();
        subtotal = 0;
        for(int i=1;i<rowCount-2;i++) {
            TableRow tableRow = (TableRow) contentTable.getChildAt(i);
            TextView itemID = (TextView) tableRow.getChildAt(0);
            TextView itemDesc = (TextView) tableRow.getChildAt(1);
            EditText qty = (EditText) tableRow.getChildAt(2);
            TextView price = (TextView) tableRow.getChildAt(3);
            TextView total = (TextView) tableRow.getChildAt(4);
            SoldItem soldItem = new SoldItem();
            soldItem.setItemId(itemID.getText().toString());
            soldItem.setItemDesc(itemDesc.getText().toString());
            soldItem.setQuantity(qty.getText().toString());
            soldItem.setTotal(total.getText().toString());

            subtotal += Float.parseFloat(total.getText().toString());
            soldItem.setPrice(price.getText().toString());
            //}
            soldList.add(soldItem);
            itemIds.add(itemID.getText().toString());
        }
        return soldList;
    }

    private void redirectToPaymentPage(List<SoldItem> soldItems,String discount){
        Intent intent = new Intent(this, PaymentMethodsActivity.class);
        intent.putExtra("subTotal", subtotal);
        Bundle args = new Bundle();
        args.putSerializable("soldItemList",(Serializable)soldItems);
        intent.putExtra("BUNDLE",args);
        intent.putExtra("discount",discount);
        startActivity(intent);
    }

    private void loadItemCategoryImages(){
        LinearLayout itemCatRow = findViewById(R.id.itemCatTableRow);
        Map<String, String> itemCategories = new HashMap<>();
        /*if(category.equals("Bags")) {
            itemCategories = POSCommonUtils.getBagCatList();
        }
        else if(category.equals("Upgrade")){
            itemCategories = POSCommonUtils.getUpgradeCatList();
        }
        else if(category.equals("Transport")){
            itemCategories = POSCommonUtils.getTransportCatList();
        }
        else if(category.equals("Meals") || category.equals("Hotels") || category.equals("Excursions") ){
            itemList = handler.getItemListFromItemCategory(category);
        }*/
        List<SoldItem> itemList = handler.getItemListFromItemCategory(category);
        /*if(itemCategories != null && !itemCategories.isEmpty()) {
            addItemsWithoutCategory(itemCategories, itemCatRow);
        }*/
        if(itemList != null && !itemList.isEmpty()){
            addItemsWithCategory(itemList,itemCatRow);
        }
    }

    private void addItemsWithCategory(List<SoldItem> itemList,LinearLayout itemCatRow){
        for(final SoldItem item : itemList){
            LinearLayout layout = new LinearLayout(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(200,200);
            layout.setLayoutParams(params1);
            layout.setGravity(Gravity.CENTER);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setBackground(ContextCompat.getDrawable(this, R.drawable.textinputborder));
            layout.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    clickSubmitBtn(item.getItemDesc(),item.getPrice());
                }
            });

            ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(params);
            imageView.setPadding(4,4,4,0);
            //imageView.setImageResource(getItemResource(this,item.getItemDesc()));
            imageView.setImageBitmap(getImageFromItemCode(item.getItemId()));

            TextView textView = new TextView(this);
            textView.setLayoutParams(params);
            textView.setText(item.getItemDesc());

            TextView priceText = new TextView(this);
            priceText.setLayoutParams(params);
            priceText.setText("$"+POSCommonUtils.getTwoDecimalFloatFromString(item.getPrice()));

            layout.addView(imageView);
            layout.addView(textView);
            layout.addView(priceText);
            itemCatRow.addView(layout);
        }
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

    private void addItemsWithoutCategory(Map<String,String> itemCategories,LinearLayout itemCatRow){
        for(final Map.Entry<String,String> categories : itemCategories.entrySet()){
            final LinearLayout layout = new LinearLayout(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.MATCH_PARENT,1);
            params.setMargins(5,0,5,0);
            LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(120,90);
            LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            layout.setLayoutParams(params);
            layout.setGravity(Gravity.CENTER);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setBackground(ContextCompat.getDrawable(this, R.drawable.textinputborder));
            layout.setPadding(10,3,10,3);
            layout.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    clickSubmitBtn(categories.getKey(),"30");
                }
            });

            ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(params1);
            imageView.setImageResource(this.getResources().getIdentifier(categories.getValue(), "drawable", "com.pos.flightpos"));

            TextView textView = new TextView(this);
            textView.setLayoutParams(params2);
            String textVal = categories.getKey();
            if(textVal.contains("and")){
                textVal = textVal.replace("and","and \n");
            }
            textView.setText(textVal);
            layout.addView(imageView);
            layout.addView(textView);
            itemCatRow.addView(layout);
        }
    }

    private void clickSubmitBtn(String itemName,String itemPrice){
        if(itemName == null || itemName.equals("")){
            Toast.makeText(getApplicationContext(), "select item first.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        boolean isOverWgtBag = false;
        if(itemName.equals("Over wgt Bags")){
            isOverWgtBag = true;
        }
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
        Button removeItemBtn = new Button(this);
        removeItemBtn.setLayoutParams(cellParams3);
        removeItemBtn.setBackground(getResources().getDrawable(R.drawable.icon_cancel));
        final boolean finalIsOverWgtBag = isOverWgtBag;
        removeItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(GateItemSelectionActivity.this)
                        .setTitle("Remove selection")
                        .setMessage("Do you want to remove this item from selection?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                subtotal -= Float.parseFloat(totalTextField.getText().toString());
                                subTotalView.setText(String.valueOf(subtotal));
                                itemCount--;
                                contentTable.removeView(tr);
                                if(finalIsOverWgtBag){
                                    overWeightBagCount--;
                                }
                            }})
                        .setNegativeButton(android.R.string.no, null).show();
            }
        });

        itemIdHdn.setText(itemName);
        itemIdHdn.setVisibility(View.GONE);
        tr.addView(itemIdHdn);
        if(isOverWgtBag){
            overWeightBagCount++;
            itemName = "Bag " + overWeightBagCount;
        }
        itemDesc.setText(itemName);
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
        if(isOverWgtBag){
            qty.setEnabled(false);
        }
        tr.addView(qty);

        price.setText(POSCommonUtils.getTwoDecimalFloatFromString(itemPrice));
        price.setTextSize(20);
        price.setLayoutParams(cellParams2);
        tr.addView(price);

        float total = Float.parseFloat(qty.getText().toString()) * Float.parseFloat(price.getText().toString());
        totalTextField.setText(POSCommonUtils.getTwoDecimalFloatFromString(String.valueOf(total)));
        totalTextField.setTextSize(20);
        totalTextField.setLayoutParams(cellParams2);
        tr.addView(totalTextField);

        tr.addView(removeItemBtn);

        subtotal += total;
        SoldItem soldItem = new SoldItem();
        soldItem.setItemDesc(itemName);
        soldItem.setQuantity("1");
        soldItem.setPrice(itemPrice);
        soldItemList.add(soldItem);
        subTotalView.setText(POSCommonUtils.getTwoDecimalFloatFromFloat(subtotal));
        contentTable.addView(tr,itemCount);
    }

    private void updateTotalWhenChangeItemQty(Float diff){
        String currentSubTotal = subTotalView.getText().toString();
        subtotal = Float.parseFloat(currentSubTotal) + diff;
        subTotalView.setText(String.valueOf(subtotal));
    }
}
