package com.pos.flightpos;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.pos.flightpos.objects.Constants;
import com.pos.flightpos.objects.SoldItem;
import com.pos.flightpos.objects.XMLMapper.Promotion;
import com.pos.flightpos.utils.POSCommonUtils;
import com.pos.flightpos.utils.POSDBHandler;
import com.pos.flightpos.utils.SaveSharedPreference;

import java.io.Serializable;
import java.util.ArrayList;
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
    List<SoldItem> soldItemList;
    POSDBHandler handler;
    String serviceType;
    List<SoldItem> discountItemList;
    LinearLayout currentSelection;
    List<String> itemIds;

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
                populateSeatNumberFromBoardingPass();
            }
        });
        discountItemList = new ArrayList<>();
        soldItemList = new ArrayList<>();
        handler = new POSDBHandler(getApplicationContext());
        Intent intent = getIntent();
        serviceType = intent.getExtras().get("serviceType").toString();
        loadItemCategoryImages();
        //setItemCatClickListeners();
        ImageButton backButton = findViewById(R.id.backPressBtn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void loadItemCategoryImages(){
        LinearLayout itemCatRow = findViewById(R.id.itemCatTableRow);
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

    /*private void setItemCatClickListeners(){

        final LinearLayout mainMealLayout = findViewById(R.id.mainBOBItemsLayout);
        mainMealLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                populateItemImages("Main");
                if(currentSelection != null)currentSelection.setBackground(getResources().getDrawable(R.drawable.textinputborderlight));
                currentSelection = mainMealLayout;
                mainMealLayout.setBackground(getResources().getDrawable(R.drawable.textinputborder));
            }
        });
        final LinearLayout otherMealLayout = findViewById(R.id.otherBOBLayout);
        otherMealLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                populateItemImages("Other");
                if(currentSelection != null)currentSelection.setBackground(getResources().getDrawable(R.drawable.textinputborderlight));
                currentSelection = otherMealLayout;
                otherMealLayout.setBackground(getResources().getDrawable(R.drawable.textinputborder));
            }
        });
        final LinearLayout snackLayout = findViewById(R.id.snackBOBLayour);
        snackLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                populateItemImages("Snack");
                if(currentSelection != null)currentSelection.setBackground(getResources().getDrawable(R.drawable.textinputborderlight));
                currentSelection = snackLayout;
                snackLayout.setBackground(getResources().getDrawable(R.drawable.textinputborder));
            }
        });
        final LinearLayout beverageLayout = findViewById(R.id.beverageBOBLayout);
        beverageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                populateItemImages("Beverages");
                if(currentSelection != null)currentSelection.setBackground(getResources().getDrawable(R.drawable.textinputborderlight));
                beverageLayout.setBackground(getResources().getDrawable(R.drawable.textinputborder));
                currentSelection = beverageLayout;
            }
        });
    }*/

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

    private List<SoldItem> getSellDataFromTable(){
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
            /*if(discount != 0){
                float newPrice = Float.parseFloat(price.getText().toString()) * ((100-discount)/100);
                String newPriceStr = POSCommonUtils.getTwoDecimalFloatFromFloat(newPrice);
                soldItem.setPrice(newPriceStr);
                soldItem.setPriceBeforeDiscount(price.getText().toString());
                discountItemList.add(soldItem);
                subtotal -= (Float.parseFloat(price.getText().toString()) - Float.parseFloat(newPriceStr));
            }
            else{*/
            subtotal += Float.parseFloat(total.getText().toString());
                soldItem.setPrice(price.getText().toString());
            //}
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

    private void clickSubmitBtn(final SoldItem item){
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

        tr.addView(lookupBtn);
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

    private void populateItemImages(String selectedCat){
        List<String> kitCodes = POSCommonUtils.getServiceTypeKitCodeMap(this).get(serviceType);
        String kitCodesStr = POSCommonUtils.getCommaSeparateStrFromList(kitCodes);
        List<SoldItem> itemList = handler.getItemListFromItemCategory(selectedCat,kitCodesStr);
        LinearLayout innerLayout = (LinearLayout) findViewById(R.id.innerLay);
        innerLayout.removeAllViews();
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
                    clickSubmitBtn(item);
                }
            });

            ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(params);
            imageView.setPadding(4,4,4,0);
            imageView.setImageResource(getItemResource(this,item.getItemDesc()));

            TextView textView = new TextView(this);
            textView.setLayoutParams(params);
            textView.setText(item.getItemDesc());

            TextView priceText = new TextView(this);
            priceText.setLayoutParams(params);
            priceText.setText("$"+POSCommonUtils.getTwoDecimalFloatFromString(item.getPrice()));

            layout.addView(imageView);
            layout.addView(textView);
            layout.addView(priceText);
            innerLayout.addView(layout);
        }
    }

    public int getItemResource(Context context, String itemName) {
        itemName = itemName.toLowerCase().replace("&","and").replace("’","")
                .replace("(","").replace(")","").replace(" ","_");
        int resId = context.getResources().getIdentifier(itemName, "drawable", "com.pos.flightpos");
        return resId;
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

    private void populateSeatNumberFromBoardingPass(){
        final Map<String,String> qrCodeDetails = POSCommonUtils.scanQRCode(this);
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
}
