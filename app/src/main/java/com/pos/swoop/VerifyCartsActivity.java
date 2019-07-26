package com.pos.swoop;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import com.pos.swoop.objects.Constants;
import com.pos.swoop.objects.XMLMapper.KITItem;
import com.pos.swoop.utils.POSCommonUtils;
import com.pos.swoop.utils.POSDBHandler;
import com.pos.swoop.utils.SaveSharedPreference;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VerifyCartsActivity extends AppCompatActivity {

    POSDBHandler posdbHandler;
    TableLayout cartsTable;
    String parent;
    String serviceType;
    EditText currentTextField;
    String currentEquipmentType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_carts);
        posdbHandler = new POSDBHandler(this);
        cartsTable = (TableLayout) findViewById(R.id.cartsTable);
        parent = getIntent().getExtras().getString("parent");
        serviceType = getIntent().getExtras().getString("serviceType");
        showAvailableCarts();
        ImageButton backButton = findViewById(R.id.backPressBtn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
    }

    private void showAvailableCarts() {

        List<String> kitCodesList = POSCommonUtils.getServiceTypeKitCodeMap(this).get(serviceType);
        Map<String, Map<String, List<KITItem>>> drawerKitItemMap = posdbHandler.
                getDrawerKitItemMapFromServiceType(POSCommonUtils.getCommaSeparateStrFromList(kitCodesList));

        TableRow.LayoutParams cellParams1 = new TableRow.LayoutParams(0,
                TableRow.LayoutParams.WRAP_CONTENT,1);

        cellParams1.setMargins(0,0,0,10);
        int cartCount = 0;
        for (final Map.Entry map : drawerKitItemMap.entrySet()) {

            cartCount++;
            TableRow tr = new TableRow(this);
            TableRow.LayoutParams trp = new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT);
            tr.setLayoutParams(trp);

            LinearLayout linearLayout = new LinearLayout(this);
            linearLayout.setLayoutParams(cellParams1);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.setPadding(0, 8, 0, 8);
            linearLayout.setClickable(true);
            linearLayout.setBackground(ContextCompat.getDrawable(this, R.color.white));
            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent intent = new Intent(VerifyCartsActivity.this, CheckInventoryActivity.class);
                    Bundle args = new Bundle();
                    args.putSerializable("cartItems", (Serializable) map.getValue());
                    intent.putExtra("BUNDLE", args);
                    intent.putExtra("cartName", map.getKey().toString());
                    intent.putExtra("parent", parent);
                    startActivity(intent);
                }
            });

            ImageView imageView = new ImageView(this);
            Bitmap yourBitmap = BitmapFactory.decodeResource(getResources(),
                    R.drawable.image_cart);
            Bitmap resized = Bitmap.createScaledBitmap(yourBitmap, (int) (yourBitmap.getWidth() * 0.13),
                    (int) (yourBitmap.getHeight() * 0.13), true);
            imageView.setImageBitmap(resized);

            /*TextView textView = new TextView(this);
            textView.setLayoutParams(cellParams1);
            textView.setText("Cart " + cartCount);
            textView.setGravity(Gravity.CENTER);
            textView.setTextSize(25);*/

            LinearLayout.LayoutParams mRparams = new LinearLayout.LayoutParams
                    (45, 45);

            final EditText myEditText = new EditText(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT,1);
            myEditText.setLayoutParams(params);
            Button button = new Button(this);
            button.setLayoutParams(mRparams);
            button.setPadding(30,0,0,0);
            button.setBackground(getResources().getDrawable(R.drawable.icon_barcode_reader));
            button.setClickable(true);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    currentTextField = myEditText;
                    currentEquipmentType = map.getKey().toString();
                    scan();
                    //setBarcodeValue(map.getKey().toString());
                }
            });
            myEditText.setText(posdbHandler.getBarcodeFromEquipmentType(map.getKey().toString()));
            myEditText.getBackground().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
            myEditText.setTextSize(20);

            TableLayout tableLayout = new TableLayout(this);

            LinearLayout llh=new LinearLayout(this);
            llh.setOrientation(LinearLayout.HORIZONTAL);
            llh.setPadding(10,10,10,0);
            llh.addView(myEditText);
            llh.addView(button);
            tableLayout.addView(llh);
            linearLayout.addView(imageView);
            if(parent.equals("VerifyFlightByAdminActivity")) {
                linearLayout.addView(tableLayout);
            }
            tr.addView(linearLayout);
            cartsTable.addView(tr);
        }
    }

    private void setBarcodeValue(String barcode){
        if(posdbHandler.isCartNumberEntered(barcode)) {
            if(posdbHandler.getBarcodeFromEquipmentType(currentEquipmentType).isEmpty()) {
                posdbHandler.insertCartNumbers(currentEquipmentType, barcode,serviceType,currentEquipmentType);
            }
            else{
                posdbHandler.updateCartNumber(currentEquipmentType, barcode);
            }
            currentTextField.setText(barcode);
            SaveSharedPreference.setStringValues(this,Constants.SHARED_PREFERENCE_CART_SCAN,"yes");
        }
        else{
            Toast.makeText(getApplicationContext(), "Cart number already scanned.",
                    Toast.LENGTH_SHORT).show();
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
                setBarcodeValue(value);
            } else {
                Toast.makeText(this,"Scan Failed",Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed()
    {
        if("AttCheckInfo".equals(parent)) {
            Intent intent = new Intent(this, AttCheckInfo.class);
            startActivity(intent);
        }
        else if("CloseFlightActivity".equals(parent)) {
            Intent intent = new Intent(this, CloseFlightActivity.class);
            startActivity(intent);
        }
        else{
            Intent intent = new Intent(this, VerifyFlightByAdminActivity.class);
            startActivity(intent);
        }
    }
}
