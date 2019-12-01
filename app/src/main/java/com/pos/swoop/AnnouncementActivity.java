package com.pos.swoop;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.pos.swoop.utils.POSDBHandler;

import java.util.Arrays;
import java.util.List;

public class AnnouncementActivity extends AppCompatActivity {

    POSDBHandler handler;
    TableLayout tableLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcement);
        ImageButton backButton = findViewById(R.id.backPressBtn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        handler = new POSDBHandler(this);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        tableLayout = findViewById(R.id.showAnnoucements);
        showAnnouncements();
    }

    private void showAnnouncements(){

        List<String> announcements = handler.getBondMessages();
        for(String msg : announcements){
            TableRow tr = new TableRow(this);

            tr.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.FILL_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));

            TextView typeText = new TextView(this);
            typeText.setText("*. "+msg);
            typeText.setTextSize(20);
            typeText.setGravity(Gravity.LEFT);
            typeText.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT));
            tr.addView(typeText);
            tableLayout.addView(tr);
        }

    }
}
