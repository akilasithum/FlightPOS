package com.pos.flightpos;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.pos.flightpos.objects.Constants;
import com.pos.flightpos.utils.POSDBHandler;
import com.pos.flightpos.utils.SaveSharedPreference;

import java.util.ArrayList;
import java.util.List;

public class DefineCartNumbersActivity extends AppCompatActivity {

    String noOfCarts;
    LinearLayout mainLayout;
    Button addCartNumBtn;
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
        addSealTextBoxes();
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

    private void addSealTextBoxes(){
        int sealCount = Integer.parseInt(noOfCarts);
        LinearLayout.LayoutParams mRparams = new LinearLayout.LayoutParams
                (RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        for(int i=1 ; i< sealCount;i++){
            EditText myEditText = new EditText(this);
            myEditText.setLayoutParams(mRparams);
            mainLayout.addView(myEditText,0);
        }
    }
}
