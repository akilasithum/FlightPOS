package com.pos.flightpos;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pos.flightpos.objects.Constants;
import com.pos.flightpos.utils.POSCommonUtils;
import com.pos.flightpos.utils.POSDBHandler;
import com.pos.flightpos.utils.SaveSharedPreference;

import java.util.ArrayList;
import java.util.List;

public class DefineCartNumbersActivity extends AppCompatActivity {

    String noOfCarts;
    LinearLayout mainLayout;
    Button addCartNumBtn;
    TextView cart1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_define_cart_numbers);
        mainLayout = (LinearLayout) findViewById(R.id.layoutDefineCars);
        addCartNumBtn = (Button) findViewById(R.id.defineCartsBtn);
        addCartNumBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                defineCarts();
            }
        });
        POSDBHandler handler = new POSDBHandler(this);
        String kitCode = SaveSharedPreference.getStringValues(this, Constants.SHARED_PREFERENCE_KIT_CODE);
        noOfCarts = handler.getKitNumberListFieldValueFromKitCode(kitCode,"noOfEq");
        cart1 = findViewById(R.id.cart1Text);
        ImageButton scanCart1 = findViewById(R.id.scanCart1);
        scanCart1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setBarcodeValue(cart1);
            }
        });
        addCartTextBoxes();
    }

    public void defineCarts() {
            int childCount = mainLayout.getChildCount();
            List<String> sealList = new ArrayList<>();
            for (int i = 0; i < childCount - 1; i++) {
                EditText editText = (EditText) mainLayout.getChildAt(i);
                String textVal = editText.getText() == null ? null : editText.getText().toString();
                if (textVal != null && !textVal.isEmpty()) {
                    sealList.add(textVal);
                }
                else{
                    Toast.makeText(getApplicationContext(), "Please define " + noOfCarts + " cart numbers.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        SaveSharedPreference.setStringValues(this, Constants.SHARED_PREFERENCE_CART_NUM_LIST,
                TextUtils.join(",", sealList));
        Toast.makeText(getApplicationContext(), "Successfully defined " + sealList.size() + " cart numbers.",
                Toast.LENGTH_SHORT).show();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                onBackPressed();
            }
        }, 1000);
    }

    private void addCartTextBoxes(){
        int sealCount = Integer.parseInt(noOfCarts);
        LinearLayout.LayoutParams mRparams = new LinearLayout.LayoutParams
                (40, 40);
        for(int i=1 ; i< sealCount;i++){
            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.HORIZONTAL);
            layout.setLayoutParams(Constants.COMMON_LAYOUT_PARAMS);
            final EditText myEditText = new EditText(this);
            myEditText.setLayoutParams(Constants.COMMON_LAYOUT_PARAMS);
            Button button = new Button(this);
            button.setLayoutParams(mRparams);
            //button.setPadding(30,0,0,0);
            button.setBackground(getResources().getDrawable(R.drawable.icon_barcode_reader));
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setBarcodeValue(myEditText);
                }
            });

            layout.addView(myEditText);
            layout.addView(button);
            mainLayout.addView(layout,0);
        }
    }

    private void setBarcodeValue(TextView textView){
        textView.setText(POSCommonUtils.scanBarCode(this));
    }
}
