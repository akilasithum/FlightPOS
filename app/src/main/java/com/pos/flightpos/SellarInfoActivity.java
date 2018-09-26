package com.pos.flightpos;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.pos.flightpos.objects.Constants;
import com.pos.flightpos.utils.SaveSharedPreference;

public class SellarInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sellar_info);
        EditText currentUser = (EditText) findViewById(R.id.currentUser);
        currentUser.setText(SaveSharedPreference.getStringValues(this, Constants.SHARED_PREFERENCE_KEY)
        + " - Logged User");
        currentUser.setEnabled(false);
    }

    public void addUser(View view) {
        LinearLayout mRlayout = (LinearLayout) findViewById(R.id.layout_user);
        LinearLayout.LayoutParams mRparams = new LinearLayout.LayoutParams
                (LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        EditText myEditText = new EditText(this);
        myEditText.setLayoutParams(mRparams);
        mRlayout.addView(myEditText,2);
    }
}
