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

import com.pos.flightpos.objects.Constants;
import com.pos.flightpos.objects.SoldItem;
import com.pos.flightpos.utils.POSCommonUtils;
import com.pos.flightpos.utils.POSDBHandler;
import com.pos.flightpos.utils.PrintJob;
import com.pos.flightpos.utils.SaveSharedPreference;

import java.io.File;
import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class RemovalComponsationActivity extends AppCompatActivity {

    Button purchaseItemsBtn;
    TableLayout contentTable;
    private int itemCount = 0;
    private float subtotal = 0;
    TextView subTotalView;
    List<SoldItem> soldItemList;
    POSDBHandler handler;
    String voluntaryType;
    LinearLayout currentSelection;
    List<String> itemIds;
    String orderNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_removal_componsation);
        contentTable = (TableLayout) findViewById(R.id.contentTable);
        subTotalView = (TextView)  findViewById(R.id.subTotalTextView);
        purchaseItemsBtn = (Button) findViewById(R.id.purchaseItems);
        purchaseItemsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                purchaseItems();
            }
        });
        soldItemList = new ArrayList<>();
        handler = new POSDBHandler(getApplicationContext());
        Intent intent = getIntent();
        voluntaryType = intent.getExtras().get("voluntaryType").toString();
        loadItemCategoryImages();
        ImageButton backButton = findViewById(R.id.backPressBtn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
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
        updateSale(soldItems);
        printReceipt(soldItems);
    }

    private void printReceipt(List<SoldItem> soldItems){
        generateOrderNumber();
        PrintJob.printVoluntaryRemovalReceipt(this,orderNumber,soldItems);
        Intent intent = new Intent(RemovalComponsationActivity.this, GateUserMainActivity.class);
        startActivity(intent);
    }

    private void generateOrderNumber(){
        String orderNumberStr = SaveSharedPreference.getStringValues(this,"orderNumber");
        if(orderNumberStr != null){
            int newVal = Integer.parseInt(orderNumberStr) + 1;
            orderNumber = SaveSharedPreference.getStringValues(this, Constants.SHARED_PREFERENCE_FLIGHT_NAME).replace(" ","_") +
                    "_"+POSCommonUtils.getDateString()+"_" + String.valueOf(newVal);
            SaveSharedPreference.updateValue(this,"orderNumber",newVal+"");
        }
        else{
            SaveSharedPreference.setStringValues(this,"orderNumber","1");
            orderNumber = SaveSharedPreference.getStringValues(this,Constants.SHARED_PREFERENCE_FLIGHT_NAME).replace(" ","_") +
                    "_"+POSCommonUtils.getDateString()+"_" + "1";
        }
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
            subtotal += Float.parseFloat(total.getText().toString());
            soldItem.setPrice(price.getText().toString());
            soldList.add(soldItem);
            itemIds.add(itemID.getText().toString());
        }
        return soldList;
    }

    private void loadItemCategoryImages(){
        LinearLayout itemCatRow = findViewById(R.id.itemCatTableRow);
        Map<String, String> itemCategories;
        if(voluntaryType.equalsIgnoreCase("flightDelay")){
            itemCategories = POSCommonUtils.getFlightDelaysCatList();
        }
        else{
            itemCategories = POSCommonUtils.getVoluntoryCatList();
        }
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
                    populateItemImages(categories.getKey());
                    if(currentSelection != null)currentSelection.setBackground(getResources().getDrawable(R.drawable.textinputborder));
                    currentSelection = layout;
                    layout.setBackground(getResources().getDrawable(R.drawable.textinputborderlight));
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

    private void populateItemImages(String selectedCat){
        List<SoldItem> itemList = handler.getItemListFromItemCategory(selectedCat);
        LinearLayout innerLayout =  findViewById(R.id.itemLayout);
        innerLayout.removeAllViews();
        for(final SoldItem item : itemList){
            LinearLayout layout = new LinearLayout(this);
            /*LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);*/
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.MATCH_PARENT,1);
            LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(90,75);
            LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            layout.setGravity(Gravity.CENTER);
            params.setMargins(5,0,5,0);
            layout.setLayoutParams(params);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setBackground(ContextCompat.getDrawable(this, R.drawable.textinputborder));
            layout.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    clickSubmitBtn(item);
                }
            });

            ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(params1);
            imageView.setPadding(4,4,4,0);
            //imageView.setImageResource(getItemResource(this,item.getItemDesc()));
            imageView.setImageBitmap(getImageFromItemCode(item.getItemId()));

            TextView textView = new TextView(this);
            textView.setLayoutParams(params2);
            textView.setText(item.getItemDesc());

            TextView priceText = new TextView(this);
            priceText.setLayoutParams(params2);
            priceText.setText("$"+POSCommonUtils.getTwoDecimalFloatFromString(item.getPrice()));

            layout.addView(imageView);
            layout.addView(textView);
            layout.addView(priceText);
            innerLayout.addView(layout);
        }
    }

    private void clickSubmitBtn(final SoldItem item){
        if(item == null || item.equals("")){
            Toast.makeText(getApplicationContext(), "select item first.",
                    Toast.LENGTH_SHORT).show();
            return;
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
        TextView equipmentNo = new TextView(this);
        TextView drawer = new TextView(this);
        Button removeItemBtn = new Button(this);
        removeItemBtn.setLayoutParams(cellParams3);
        removeItemBtn.setBackground(getResources().getDrawable(R.drawable.icon_cancel));
        removeItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(RemovalComponsationActivity.this)
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

        /*Button lookupBtn = new Button(this);
        lookupBtn.setLayoutParams(cellParams3);
        lookupBtn.setBackground(getResources().getDrawable(R.drawable.icon_llokup));
        lookupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                POSCommonUtils.showDrawerAndEquipment(item,RemovalComponsationActivity.this);
            }
        });*/

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

        price.setText(POSCommonUtils.getTwoDecimalFloatFromString(item.getPrice()));
        price.setTextSize(20);
        price.setLayoutParams(cellParams2);
        tr.addView(price);

        float total = Float.parseFloat(qty.getText().toString()) * Float.parseFloat(price.getText().toString());
        totalTextField.setText(POSCommonUtils.getTwoDecimalFloatFromString(String.valueOf(total)));
        totalTextField.setTextSize(20);
        totalTextField.setLayoutParams(cellParams2);
        tr.addView(totalTextField);

        equipmentNo.setText(item.getEquipmentNo());
        equipmentNo.setVisibility(View.GONE);
        tr.addView(equipmentNo);

        drawer.setText(item.getDrawer());
        drawer.setVisibility(View.GONE);
        tr.addView(drawer);

        //tr.addView(lookupBtn);
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

    private void updateSale(List<SoldItem> items){
        String passengerName = SaveSharedPreference.getStringValues(this,Constants.SHARED_PREFERENCE_USER_DETAILS);
        String[] str = passengerName.split("==");
        String flightId = SaveSharedPreference.getStringValues(this,Constants.SHARED_PREFERENCE_FLIGHT_NAME);
        Date date = new Date();
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        String dateStr = df.format(date);
        for(SoldItem item : items){
            handler.insertVoluntaryRemovalItems(orderNumber,item.getItemId(),item.getQuantity(),item.getTotal(),
                    str[0],flightId,dateStr);
        }
    }

    private void updateTotalWhenChangeItemQty(Float diff){
        String currentSubTotal = subTotalView.getText().toString();
        subtotal = Float.parseFloat(currentSubTotal) + diff;
        subTotalView.setText(String.valueOf(subtotal));
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
}
