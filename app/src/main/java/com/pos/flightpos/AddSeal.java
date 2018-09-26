package com.pos.flightpos;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class AddSeal extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_seal);
    }

    public void addSeal(View view) {
        LinearLayout mRlayout = (LinearLayout) findViewById(R.id.layout_addSeal);
        LinearLayout.LayoutParams mRparams = new LinearLayout.LayoutParams
                (RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        EditText myEditText = new EditText(this);
        myEditText.setLayoutParams(mRparams);
        mRlayout.addView(myEditText,0);
    }
}
