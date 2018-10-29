package com.pos.flightpos;

import android.content.Context;
import android.graphics.Color;
import android.pt.iccardEMV.IcCardEMV;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ICCardReader extends AppCompatActivity {

    private Button bt_openIcCard = null;
    private Button bt_close = null;
    private EditText ed_ResponseData = null;

    private IcCardEMV icCard = null;
    long mExitTime   = 0;
    boolean open_flg = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iccard_reader);
        icCard = new IcCardEMV();
        init_id();
    }
    public void show(Context context ,String msg)
    {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();

    }
    private void show_result(int ret) {
        // TODO Auto-generated method stub
        switch(ret)
        {
            case 0:
                show(ICCardReader.this,"success ");
                break;
            case -1:
                show(ICCardReader.this,"fail");
                break;
            case -2:
                show(ICCardReader.this,"time out");
                break;
            case -3:
                show(ICCardReader.this,"in parameters error");
                break;
            default:
                show(ICCardReader.this,"fail");
                break;

        }
    }
    public void openIcCard(View view) {
        if(open_flg)
        {
            show(ICCardReader.this,"IcCard already open");
            return;
        }
        int ret = icCard.open();
        if(ret == 0)
        {
            open_flg = true;
        }
        show_result(ret);
        bt_openIcCard.setBackgroundColor(Color.rgb(0, 0, 255));
    }
    public void activate(View view) {
        if(!open_flg)
        {
            show(ICCardReader.this,"Please open IcCard");
            return;
        }
        int ret = icCard.activate();
        show_result(ret);
    }
    public void seek(View view) {
        if(!open_flg)
        {
            show(ICCardReader.this,"Please open IcCard");
            return;
        }
        int ret = icCard.seek();

        show_result(ret);
    }
    public void exeAPDU(View view) {
        if(!open_flg)
        {
            show(ICCardReader.this,"Please open IcCard");
            return;
        }
        int ret;
        //int[] in_dat = null;
		/*
		in_dat = new int[]{0,164,4,0,14,49,
						   80,65,89,46,83,89,
						   83,46,68,68,70,48,49,0
				};
		*/
        //0 164 4 0 14 50 80 65 89 46 83 89 83 46 68 68 70 48 49 0
        String str = "00,a4,04,00,0e,31,50,41,59,2e,53,59,53,2e,44,44,46,30,31,00";
        String temp_str[] = str.split(",");
        byte[] out_dat = new byte[1024];

        byte[] intbyte_dat = new byte[temp_str.length];
        for(int i = 0; i < intbyte_dat.length; i++)
        {
            intbyte_dat[i] = (byte) Integer.parseInt(temp_str[i], 16);
        }
        ret = icCard.exeAPDU(intbyte_dat, intbyte_dat.length,out_dat);
        if(ret>0)
        {
            String out_temp = "";
            for(int i = 0; i < ret; i++)
            {
                out_temp +=Integer.toHexString(out_dat[i]&0xff)+" ";
            }
            String track1Str = new String(out_dat);
            ed_ResponseData.setText(track1Str);
            show(ICCardReader.this,"read length:"+ret);
        }else
        {
            show(ICCardReader.this,"fail");
        }
    }

    public void move(View view) {
        if(!open_flg)
        {
            show(ICCardReader.this,"Please open IcCard");
            return;
        }
        int ret = icCard.move();
        show_result(ret);
    }
    public void close(View view) {
        int ret = icCard.close();
        if(ret == 0)
        {
            open_flg = false;
        }
        show_result(ret);
        bt_close.setBackgroundColor(Color.rgb(0, 0, 255));
        System.exit(0);
    }
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub

        if (keyCode == KeyEvent.KEYCODE_BACK ) {

            if ((System.currentTimeMillis() - mExitTime) > 2000)
            {
                Toast.makeText(this, "Once press again,exit program", Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
            }
            else
            {
                icCard.close();
                icCard = null;
                System.exit(0);
            }

            return true;

        }

        return super.onKeyDown(keyCode, event);
    }
    public void init_id(){


        //ed_sendAPDU = (EditText)findViewById(R.id.ed_sendAPDU);
        bt_openIcCard = (Button)findViewById(R.id.bt_openIcCard);
        bt_close = (Button)findViewById(R.id.bt_close);
        ed_ResponseData = (EditText)findViewById(R.id.ed_ResponseData);
    }


}
