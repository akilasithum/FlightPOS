package com.pos.flightpos;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.pos.flightpos.objects.Constants;
import com.pos.flightpos.objects.XMLMapper.KITItem;
import com.pos.flightpos.utils.POSDBHandler;
import com.pos.flightpos.utils.SaveSharedPreference;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class VerifyCartsActivity extends AppCompatActivity {

    POSDBHandler posdbHandler;
    TableLayout cartsTable;
    String parent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_carts);
        posdbHandler = new POSDBHandler(this);
        cartsTable = (TableLayout) findViewById(R.id.cartsTable);
        parent = getIntent().getExtras().getString("parent");
        showAvailableCarts();
    }

    private void showAvailableCarts() {

        String kitCode = SaveSharedPreference.getStringValues(this, Constants.SHARED_PREFERENCE_KIT_CODE);
        Map<String, Map<String, List<KITItem>>> drawerKitItemMap = posdbHandler.getDrawerKitItemMapFromServiceType(kitCode);

        TableRow.LayoutParams cellParams1 = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT);

        int cartCount = 0;
        for (final Map.Entry map : drawerKitItemMap.entrySet()) {

            cartCount++;
            TableRow tr = new TableRow(this);
            tr.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));

            LinearLayout linearLayout = new LinearLayout(this);
            linearLayout.setLayoutParams(cellParams1);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.setPadding(0, 8, 0, 8);
            linearLayout.setClickable(true);
            linearLayout.setBackground(getResources().getDrawable(R.drawable.textinputborder));
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
            Bitmap resized = Bitmap.createScaledBitmap(yourBitmap, (int) (yourBitmap.getWidth() * 0.2),
                    (int) (yourBitmap.getHeight() * 0.2), true);
            imageView.setImageBitmap(resized);

            TextView textView = new TextView(this);
            textView.setLayoutParams(cellParams1);
            textView.setText("Cart " + cartCount);
            textView.setGravity(Gravity.CENTER);
            textView.setTextSize(25);

            linearLayout.addView(imageView);
            linearLayout.addView(textView);
            tr.addView(linearLayout);
            cartsTable.addView(tr);
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
            super.onBackPressed();
        }
    }
}
