package com.pos.swoop;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.pt.msr.Msr;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MSReader extends AppCompatActivity {

    private EditText ed_track1 = null;
    private EditText ed_track2 = null;
    private ProgressDialog dia = null;

    private Msr msr = null;
    boolean open_flg = false;
    long mExitTime   = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_msreader);
        msr = new Msr();
        init_id();
    }
    public void show(Context context , String msg)
    {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();

    }
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub

            if(msg.what == 1){
                dia.dismiss();
                for(int i = 1; i < 4; i++)
                {
                    if(msr.getTrackError(i) == 0)
                    {
                        //Log.i("123", "i:"+i);
                        byte[] out_data = new byte[msr.getTrackDataLength(i)];
                        msr.getTrackData(i, out_data);
                        setmsg(i,out_data);
                        return;
                    }
                }
            }
            super.handleMessage(msg);
        }
    };

    private void show_result(int ret) {
        // TODO Auto-generated method stub
        switch(ret)
        {
            case 0:
                show(MSReader.this,"success ");
                break;
            case -1:
                show(MSReader.this,"fail");
                break;
            case -2:
                show(MSReader.this,"time out");
                break;
            case -3:
                show(MSReader.this,"in parameters error");
                break;
            default:
                show(MSReader.this,"fail");
                break;

        }
    }
    public void openMsr(View view) {
        if(open_flg)
        {
            show(MSReader.this,"Msr already open");
            return;
        }
        int ret = msr.open();
        if(ret == 0)
        {
            open_flg = true;
        }
        show_result(ret);
    }
    public String byteToHexString(byte[] out_data)
    {
        String str_dat = "";
        for(int j = 0 ;j < out_data.length; j++)
        {
            str_dat +=Integer.toHexString(out_data[j]&0xff)+" ";
        }
        return str_dat;
    }
    public void setmsg(int i, byte[] out_data) {
        if(i == 1)
        {
            //ed_track1.setText(byteToHexString(out_data));
            String track1Str = new String(out_data);
            String[] track1Details = track1Str.split("\\^");
            ed_track1.setText(track1Details[0]);
            ed_track2.setText(track1Details[1]);
            ed_track1.setEnabled(false);
            ed_track2.setEnabled(false);
            close();
        }
    }

    public void getTrackInfo(View view) {
        if(!open_flg)
        {
            openMsr(view);
        }
        ed_track1.setEnabled(true);
        ed_track2.setEnabled(true);
        ed_track1.setText("");
        ed_track2.setText("");
        dia = new ProgressDialog(MSReader.this);
        dia.setTitle("MSR");
        dia.setMessage("please swipe MSR card...");
        dia.show();
        new Thread(){
            public void run() {
                int ret = -1;

                while(true)
                {
                    ret = msr.poll(1000);
                    if(ret == 0)
                    {

                        Message msg = new Message();
                        msg.what    = 1;
                        handler.sendMessage(msg);
                        break;
                    }
                }
            }
        }.start();

    }

    public void close() {
        int ret = msr.close();
        if(ret == 0)
        {
            open_flg = false;
        }
        show_result(ret);
    }

    public void init_id(){
        ed_track1 = (EditText)findViewById(R.id.ed_track1);
        ed_track2 = (EditText)findViewById(R.id.ed_track2);

    }
}
