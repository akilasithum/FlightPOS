package com.pos.flightpos;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.util.StringUtils;
import com.pos.flightpos.objects.Constants;
import com.pos.flightpos.utils.SaveSharedPreference;

import java.util.ArrayList;
import java.util.List;

public class AddSeal extends AppCompatActivity {

    String parent;
    String noOfSeals;
    LinearLayout mRlayout;
    Button addSealBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_seal);
        parent = getIntent().getExtras().getString("parent");
        noOfSeals = SaveSharedPreference.getStringValues(this,Constants.SHARED_PREFERENCE_NO_OF_SEAL);
        mRlayout = (LinearLayout) findViewById(R.id.layout_addSeal);
        addSealBtn = (Button) findViewById(R.id.bt_addSeal);
        if(parent.equals("AttCheckInfo")){
            addSealBtn.setText("Verify Seals");
        }
        else{
            addSealBtn.setText("Add Seals");
        }
        addSealTextBoxes();
    }

    private void addSealTextBoxes(){
        int sealCount = Integer.parseInt(noOfSeals);
        LinearLayout.LayoutParams mRparams = new LinearLayout.LayoutParams
                (RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        for(int i=1 ; i< sealCount;i++){
            EditText myEditText = new EditText(this);
            myEditText.setLayoutParams(mRparams);
            mRlayout.addView(myEditText,0);
        }
    }

    public void addSeal(View view) {
        int childCount = mRlayout.getChildCount();
        List<String> sealList = new ArrayList<>();
        for (int i = 0; i < childCount - 1; i++) {
            EditText editText = (EditText) mRlayout.getChildAt(i);
            String textVal = editText.getText() == null ? null : editText.getText().toString();
            if (textVal == null) {
                Toast.makeText(getApplicationContext(), "Add " + noOfSeals + " seals.",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            sealList.add(textVal);
        }
        if(parent.equals("AttCheckInfo")){
            String storedSeals = SaveSharedPreference.getStringValues(this,"openSealList");
            String[] storedSealsArray = storedSeals.split(",");
            for(int i = 0; i<storedSealsArray.length ; i++){
                if(!sealList.contains(storedSealsArray[i])){
                    Toast.makeText(getApplicationContext(), "Seal numbers are not matched. Enter correct seal numbers",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            SaveSharedPreference.setStringValues(this, Constants.SHARED_PREFERENCE_IS_SEAL_VERIFIED,"yes");
            Toast.makeText(getApplicationContext(), "Seal numbers are matched. You can continue.",
                    Toast.LENGTH_SHORT).show();
        }
        else {

            String storedName = parent.equals("VerifyFlightByAdminActivity") ? "openSealList" : "closeSealList";
            SaveSharedPreference.setStringValues(this, storedName, TextUtils.join(",", sealList));
            Toast.makeText(getApplicationContext(), "Successfully added " + noOfSeals + " seals.",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
