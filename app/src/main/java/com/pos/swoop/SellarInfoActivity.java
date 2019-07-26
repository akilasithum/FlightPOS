package com.pos.swoop;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.pos.swoop.objects.Constants;
import com.pos.swoop.objects.XMLMapper.FADetails;
import com.pos.swoop.utils.POSDBHandler;
import com.pos.swoop.utils.SaveSharedPreference;

import java.util.ArrayList;
import java.util.List;

public class SellarInfoActivity extends AppCompatActivity {

    POSDBHandler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sellar_info);
        handler = new POSDBHandler(this);
        EditText currentUser = (EditText) findViewById(R.id.currentUser);
        currentUser.setText(SaveSharedPreference.getStringValues(this, Constants.SHARED_PREFERENCE_FA_NAME)
        + " - Logged User");
        currentUser.setEnabled(false);
        ImageButton backButton = findViewById(R.id.backPressBtn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        final Button addBtn = findViewById(R.id.bt_saveSeller);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addSellers();
            }
        });
        fillDetailsIfAddedAlready();
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
    }

    private void fillDetailsIfAddedAlready(){
        List<FADetails> details = handler.getFADetails();
        if(details != null && !details.isEmpty()){
            LinearLayout mRlayout = findViewById(R.id.layout_user);
            LinearLayout.LayoutParams mRparams = new LinearLayout.LayoutParams
                    (LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            for(FADetails user : details){
                if(!user.getFaName().equals(SaveSharedPreference.getStringValues(this, Constants.SHARED_PREFERENCE_FA_NAME))){
                    EditText myEditText = new EditText(this);
                    myEditText.setLayoutParams(mRparams);
                    myEditText.setText(user.getFaName());
                    mRlayout.addView(myEditText,0);
                }
            }
        }
    }

    public void addUser(View view) {
        LinearLayout mRlayout = findViewById(R.id.layout_user);
        LinearLayout.LayoutParams mRparams = new LinearLayout.LayoutParams
                (LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        EditText myEditText = new EditText(this);
        myEditText.setLayoutParams(mRparams);
        mRlayout.addView(myEditText,0);
    }

    private void addSellers(){
        LinearLayout mRlayout = findViewById(R.id.layout_user);
        int textCount = mRlayout.getChildCount();
        List<String> usersList = new ArrayList<>();
        for(int i = 0 ;i<textCount-2;i++){
            EditText text = (EditText)mRlayout.getChildAt(i);
            if(text.getText() != null && !text.getText().toString().isEmpty()){
                String name = text.getText().toString();
                if(name.contains("Logged")){
                    usersList.add(name.split(" -")[0]);
                }
                else {
                    usersList.add(name);
                }
            }
        }
        handler.deleteFADetails();
        handler.insertFADetails(SaveSharedPreference.getStringValues(this,Constants.SHARED_PREFERENCE_FLIGHT_NAME),
                SaveSharedPreference.getStringValues(this,Constants.SHARED_PREFERENCE_FLIGHT_SECTOR),
                SaveSharedPreference.getStringValues(this,Constants.SHARED_PREFERENCE_FLIGHT_DATE),usersList);

        Toast.makeText(this,"Sellers added successfully",Toast.LENGTH_SHORT);
        onBackPressed();
    }
}
