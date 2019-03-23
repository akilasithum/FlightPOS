package com.pos.flightpos;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class ConpensationSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conpensation_selection);
        ImageButton backButton = findViewById(R.id.backPressBtn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        setLayoutClickListeners();
    }

    private void setLayoutClickListeners(){
        LinearLayout flightDelayLayout = findViewById(R.id.flightDelaysLayout);
        flightDelayLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ConpensationSelectionActivity.this, RemovalComponsationActivity.class);
                intent.putExtra("voluntaryType","flightDelay");
                startActivity(intent);
            }
        });

        LinearLayout involuntaryRemovalLayout = findViewById(R.id.involuntaryRemoval);
        involuntaryRemovalLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ConpensationSelectionActivity.this, RemovalComponsationActivity.class);
                intent.putExtra("voluntaryType","involuntaryRemoval");
                startActivity(intent);
            }
        });

        LinearLayout voluntaryRemovalLayout = findViewById(R.id.voluntaryRemovalLayout);
        voluntaryRemovalLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ConpensationSelectionActivity.this, RemovalComponsationActivity.class);
                intent.putExtra("voluntaryType","voluntaryRemoval");
                startActivity(intent);
            }
        });
    }
}
