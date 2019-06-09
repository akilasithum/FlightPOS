package com.pos.flightpos;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.design.widget.TabLayout;
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
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
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
    LinearLayout contentLayout;
    View currentSelection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gate_item_selection);
        handler = new POSDBHandler(this);
        contentTable =  findViewById(R.id.contentTable);
        subTotalView =  findViewById(R.id.subTotalTextView);
        purchaseItemsBtn =  findViewById(R.id.purchaseItems);
        category = getIntent().getExtras().get("category").toString();
        TextView headerText = findViewById(R.id.headerId);
        headerText.setText("Item Sales - " + category);
        ImageButton backButton = findViewById(R.id.backPressBtn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        soldItemList = new ArrayList<>();
        loadItemCategoryImages();
        contentLayout = findViewById(R.id.contentLayout);
        purchaseItemsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                purchaseItems();
            }
        });
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
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
        intent.putExtra("category",category);
        startActivity(intent);
    }

    private void loadItemCategoryImages(){
        LinearLayout itemCatRow = findViewById(R.id.itemCatTableRow);

        List<SoldItem> itemList = handler.getItemListFromItemCategory(category);
        if(itemList != null && !itemList.isEmpty()){
            addItemsWithCategory(itemList,itemCatRow);
        }
    }

    private void addItemsWithCategory(List<SoldItem> itemList,LinearLayout itemCatRow){
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.MATCH_PARENT,1);
        params.setMargins(5,0,5,0);
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(70,70);
        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams params3 = new LinearLayout.LayoutParams(110,110);
        params3.setMargins(0,5,0,5);

        for(final SoldItem item : itemList){
            LinearLayout layout = new LinearLayout(this);

            layout.setGravity(Gravity.CENTER);
            layout.setLayoutParams(params);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    clickSubmitBtn(item);
                }
            });

            LinearLayout imageLayout = new LinearLayout(this);
            imageLayout.setGravity(Gravity.CENTER);
            imageLayout.setLayoutParams(params3);
            imageLayout.setOrientation(LinearLayout.VERTICAL);
            imageLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.sellitemimagebg));

            ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(params1);
            imageView.setPadding(4,4,4,4);
            imageView.setImageBitmap(getImageFromItemCode(item.getItemId()));

            imageLayout.addView(imageView);

            TextView textView = new TextView(this);
            textView.setLayoutParams(params2);
            textView.setTextSize(13);
            textView.setTextColor(getResources().getColor(R.color.white));
            textView.setText(item.getItemDesc());

            TextView priceText = new TextView(this);
            priceText.setLayoutParams(params2);
            priceText.setTextColor(getResources().getColor(R.color.white));
            priceText.setText("$"+POSCommonUtils.getTwoDecimalFloatFromString(item.getPrice()));

            layout.addView(textView);
            layout.addView(imageLayout);
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
                    //clickSubmitBtn(categories.getKey(),"30");
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

    private void clickSubmitBtn(SoldItem item){

        final FrameLayout frameLayout = new FrameLayout(this);
        LinearLayout.LayoutParams frameLayoutParams = new LinearLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,1);
        frameLayoutParams.setMargins(8,0,0,8);
        frameLayout.setLayoutParams(frameLayoutParams);

        final ImageView closeBtn = new ImageView(this);
        closeBtn.setClickable(true);
        FrameLayout.LayoutParams closeBtnParam = new FrameLayout.LayoutParams(45, 45);
        closeBtnParam.gravity = Gravity.TOP|Gravity.RIGHT;
        closeBtn.setBackground(getResources().getDrawable(R.drawable.icon_cancel));
        closeBtn.setLayoutParams(closeBtnParam);

        final LinearLayout linearLayout = new LinearLayout(this);
        FrameLayout.LayoutParams linearLayoutParams = new FrameLayout.LayoutParams(700,
                TableRow.LayoutParams.WRAP_CONTENT);
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setBackgroundColor(getResources().getColor(R.color.sellitembg));
        linearLayoutParams.setMargins(0,23,23,0);
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


        if(item == null || item.getItemDesc().equals("")){
            Toast.makeText(getApplicationContext(), "select item first.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        boolean isOverWgtBag = false;
        if(item.getItemDesc().equals("Over wgt Bags")){
            isOverWgtBag = true;
        }
        itemCount++;
        TableRow tr = new TableRow(this);
        TableRow tr1 = new TableRow(this);
        tr.setId(itemCount);
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

        TextView itemIdHdn = new TextView(this);
        TextView itemDesc = new TextView(this);
        EditText qty = new EditText(this);
        final TextView price = new TextView(this);
        final TextView totalTextField = new TextView(this);
        Button removeItemBtn = new Button(this);
        removeItemBtn.setLayoutParams(cellParams3);
        removeItemBtn.setBackground(getResources().getDrawable(R.drawable.icon_cancel));
        final boolean finalIsOverWgtBag = isOverWgtBag;
        closeBtn.setOnClickListener(new View.OnClickListener() {
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
                                contentLayout.removeView(frameLayout);
                                if(finalIsOverWgtBag){
                                    overWeightBagCount--;
                                }
                            }})
                        .setNegativeButton(android.R.string.no, null).show();
            }
        });

        itemIdHdn.setText(item.getItemId());
        itemIdHdn.setVisibility(View.GONE);
        tr.addView(itemIdHdn);
        String itemName = item.getItemDesc();
        if(isOverWgtBag){
            overWeightBagCount++;
            itemName = "Bag " + overWeightBagCount;
        }
        itemDesc.setText(itemName);
        itemDesc.setTextSize(20);
        itemDesc.setLayoutParams(cellParams1);
        itemDesc.setGravity(Gravity.CENTER);
        tr.addView(itemDesc);

        TextView itemDescStr = new TextView(this);
        itemDescStr.setText("Item Desc");
        itemDescStr.setTextSize(15);
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
        qty.setTextSize(20);
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
        if(isOverWgtBag){
            qty.setEnabled(false);
        }
        tr.addView(qty);

        TextView qtyDesc = new TextView(this);
        qtyDesc.setText("Qty");
        qtyDesc.setTextSize(15);
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
        price.setTextSize(20);
        price.setLayoutParams(cellParams2);
        price.setGravity(Gravity.CENTER);
        tr.addView(price);

        TextView priceDesc = new TextView(this);
        priceDesc.setText("Unit Price");
        priceDesc.setTextSize(15);
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
        totalTextField.setTextSize(20);
        totalTextField.setLayoutParams(cellParams2);
        totalTextField.setGravity(Gravity.CENTER);
        tr.addView(totalTextField);

        TextView totalDesc = new TextView(this);
        totalDesc.setText("Total");
        totalDesc.setTextSize(15);
        totalDesc.setLayoutParams(cellParams2);
        totalDesc.setGravity(Gravity.CENTER);
        tr1.addView(totalDesc);

        subtotal += total;
        SoldItem soldItem = new SoldItem();
        soldItem.setItemDesc(itemName);
        soldItem.setQuantity("1");
        soldItem.setPrice(item.getPrice());
        soldItemList.add(soldItem);
        String subTotalText = POSCommonUtils.getTwoDecimalFloatFromFloat(subtotal).replace(",","");
        subTotalView.setText(subTotalText);
        linearLayout.addView(tr);
        linearLayout.addView(tr1);
        frameLayout.addView(linearLayout);
        frameLayout.addView(closeBtn);
        contentLayout.addView(frameLayout);
    }

    private void updateTotalWhenChangeItemQty(Float diff){
        String currentSubTotal = subTotalView.getText().toString();
        subtotal = Float.parseFloat(currentSubTotal) + diff;
        String subTotalText = POSCommonUtils.getTwoDecimalFloatFromFloat(subtotal).replace(",","");
        subTotalView.setText(subTotalText);
    }
}
